package org.threadPool;

import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        CustomExecutor executor = new CustomThreadPoolExecutor(
                2, 4,
                5,
                5, TimeUnit.SECONDS,
                1,
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        System.out.println("[CustomHandler] Rejected task: " + r);
                    }
                }
        );

        // Имитационные задачи
        for (int i = 0; i < 12; i++) {
            int taskId = i;
            executor.execute(() -> {
                System.out.println(">> Task #" + taskId + " started");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
                System.out.println("<< Task #" + taskId + " completed");
            });
        }

        Thread.sleep(15000);
        executor.shutdown();
        Thread.sleep(5000);

        System.out.println("Программа завершена");
    }
}