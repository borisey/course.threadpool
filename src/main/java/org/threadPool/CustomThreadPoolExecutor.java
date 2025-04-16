package org.threadPool;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadPoolExecutor implements CustomExecutor {
    private final int corePoolSize;
    private final int maxPoolSize;
    private final int minSpareThreads;
    private final long keepAliveTimeMillis;

    private final List<Worker> workers = Collections.synchronizedList(new ArrayList<>());
    private final BlockingQueue<Runnable>[] queues;
    private final AtomicInteger threadCounter = new AtomicInteger(0);
    private final CustomThreadFactory threadFactory;
    private final RejectedExecutionHandler rejectionHandler;
    private final AtomicInteger nextQueueIndex = new AtomicInteger(0);
    private volatile boolean isShutdown = false;

    public CustomThreadPoolExecutor(int corePoolSize, int maxPoolSize, int queueSize,
                                    long keepAliveTime, TimeUnit timeUnit,
                                    int minSpareThreads, RejectedExecutionHandler rejectionHandler) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTimeMillis = timeUnit.toMillis(keepAliveTime);
        this.minSpareThreads = minSpareThreads;
        this.rejectionHandler = rejectionHandler;

        this.queues = new LinkedBlockingQueue[maxPoolSize];
        for (int i = 0; i < maxPoolSize; i++) {
            queues[i] = new LinkedBlockingQueue<>(queueSize);
        }

        this.threadFactory = new CustomThreadFactory("MyPool-worker");

        for (int i = 0; i < corePoolSize; i++) {
            createWorker(i);
        }
    }

    private void createWorker(int queueIndex) {
        if (workers.size() >= maxPoolSize) return;
        Worker worker = new Worker(queueIndex);
        workers.add(worker);
        Thread thread = threadFactory.newThread(worker);
        thread.start();
    }

    @Override
    public void execute(Runnable command) {
        if (isShutdown) throw new RejectedExecutionException("Executor is shut down");
        int queueIndex = nextQueueIndex.getAndIncrement() % queues.length;
        BlockingQueue<Runnable> queue = queues[queueIndex];
        if (!queue.offer(command)) {
            System.out.println("[Rejected] Task " + command + " was rejected due to overload!");
            rejectionHandler.rejectedExecution(command, null);
        } else {
            System.out.println("[Pool] Task accepted into queue #" + queueIndex + ": " + command);
            ensureMinSpareThreads();
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        FutureTask<T> futureTask = new FutureTask<>(task);
        execute(futureTask);
        return futureTask;
    }

    private void ensureMinSpareThreads() {
        long idleThreads = workers.stream().filter(w -> w.idle).count();
        if (idleThreads < minSpareThreads && workers.size() < maxPoolSize) {
            createWorker(workers.size());
        }
    }

    @Override
    public void shutdown() {
        isShutdown = true;
        System.out.println("[Pool] Shutdown initiated.");
    }

    @Override
    public void shutdownNow() {
        isShutdown = true;
        for (Worker w : workers) {
            w.stopNow = true;
        }
        System.out.println("[Pool] Immediate shutdown initiated.");
    }

    public AtomicInteger getThreadCounter() {
        return threadCounter;
    }

    private class Worker implements Runnable {
        final int queueIndex;
        volatile boolean idle = true;
        volatile boolean stopNow = false;

        Worker(int queueIndex) {
            this.queueIndex = queueIndex;
        }

        @Override
        public void run() {
            Thread current = Thread.currentThread();
            try {
                while (!isShutdown && !stopNow) {
                    Runnable task = queues[queueIndex].poll(keepAliveTimeMillis, TimeUnit.MILLISECONDS);
                    if (task != null) {
                        idle = false;
                        System.out.println("[Worker] " + current.getName() + " executes " + task);
                        task.run();
                        idle = true;
                    } else {
                        if (workers.size() > corePoolSize) {
                            System.out.println("[Worker] " + current.getName() + " idle timeout, stopping.");
                            workers.remove(this);
                            break;
                        }
                    }
                }
            } catch (InterruptedException ignored) {
            } finally {
                System.out.println("[Worker] " + current.getName() + " terminated.");
            }
        }
    }

    static class CustomThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private final AtomicInteger counter = new AtomicInteger(1);

        CustomThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            String name = namePrefix + "-" + counter.getAndIncrement();
            System.out.println("[ThreadFactory] Creating new thread: " + name);
            return new Thread(r, name);
        }
    }
}