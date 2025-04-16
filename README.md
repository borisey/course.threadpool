<h1>Кастомный пул потоков на Java</h1>

<h2>Общее описание</h2>
<p>Проект реализует собственный пул потоков с расширенной функциональностью, логированием и гибкой настройкой параметров. Это альтернатива стандартному <code>ThreadPoolExecutor</code>, подходящая для высоконагруженных серверных приложений, где требуется точный контроль за поведением пула и прозрачное логирование.</p>

<h2>Ключевые возможности</h2>
<ul>
  <li>Настраиваемые параметры: <code>corePoolSize</code>, <code>maxPoolSize</code>, <code>keepAliveTime</code>, <code>minSpareThreads</code>, <code>queueSize</code></li>
  <li>Кастомный <code>ThreadFactory</code> с логами создания/завершения потоков</li>
  <li>Отказоустойчивость: собственный <code>RejectedExecutionHandler</code></li>
  <li>Round Robin балансировка задач по очередям</li>
  <li>Логирование всех ключевых событий</li>
  <li>Поддержка <code>execute</code>, <code>submit</code>, <code>shutdown</code>, <code>shutdownNow</code></li>
  <li>Масштабируемость: потоки создаются и завершаются динамически</li>
</ul>

<h2>Анализ производительности</h2>

<h3>Сравнение с <code>ThreadPoolExecutor</code></h3>

<table>
  <tr>
    <th>Метрика</th>
    <th>ThreadPoolExecutor</th>
    <th>Custom Executor</th>
  </tr>
  <tr>
    <td>Управление потоками</td>
    <td>Автоматическое</td>
    <td>Гибкое, кастомное</td>
  </tr>
  <tr>
    <td>Балансировка задач</td>
    <td>Одна очередь</td>
    <td>Несколько очередей (Round Robin)</td>
  </tr>
  <tr>
    <td>Контроль «резервных» потоков</td>
    <td>Нет</td>
    <td>Поддержка <code>minSpareThreads</code></td>
  </tr>
  <tr>
    <td>Логирование</td>
    <td>Ограниченное</td>
    <td>Расширенное и настраиваемое</td>
  </tr>
  <tr>
    <td>Гибкость настройки отказов</td>
    <td>Есть, но шаблонная</td>
    <td>Полностью кастомная</td>
  </tr>
  <tr>
    <td>Производительность (нагрузка 10–20 задач)</td>
    <td>Сравнимая</td>
    <td>Сравнимая, иногда выше</td>
  </tr>
</table>

<div class="note">
  <strong>Вывод:</strong> при средне-интенсивной нагрузке кастомный пул показывает производительность, сравнимую со стандартным, но предоставляет <strong>лучшую управляемость и расширенные функции</strong>.
</div>

<h2>Мини-исследование параметров</h2>

<p>Во время нагрузочного теста были выявлены следующие зависимости:</p>

<table>
  <tr>
    <th>Параметры</th>
    <th>Итог</th>
  </tr>
  <tr>
    <td><code>corePoolSize = 2</code>, <code>maxPoolSize = 4</code>, <code>queueSize = 5</code></td>
    <td>Идеально при среднем числе задач (10–12), задачи обрабатываются быстро, пул стабилен</td>
  </tr>
  <tr>
    <td><code>corePoolSize = 1</code>, <code>maxPoolSize = 2</code>, <code>queueSize = 2</code></td>
    <td>Частые отказы, высокая нагрузка на очередь</td>
  </tr>
  <tr>
    <td><code>corePoolSize = 4</code>, <code>maxPoolSize = 8</code>, <code>queueSize = 20</code></td>
    <td>Высокая устойчивость к пиковым нагрузкам, но рост накладных расходов</td>
  </tr>
  <tr>
    <td><code>minSpareThreads = 2</code></td>
    <td>Уменьшает пиковую задержку на старте, ускоряет реакцию пула</td>
  </tr>
</table>

<p><strong>Рекомендация:</strong> оптимальные значения — <code>corePoolSize = 2–4</code>, <code>maxPoolSize = 4–8</code>, <code>queueSize = 5–10</code>, <code>minSpareThreads = 1–2</code> для приложений с умеренной многозадачностью.</p>

<h2>Механизм распределения задач и балансировки</h2>

<ul>
  <li>Каждая рабочая нить привязана к <strong>своей очереди</strong> (<code>BlockingQueue&lt;Runnable&gt;</code>)</li>
  <li>Задачи распределяются по принципу <strong>Round Robin</strong>: <code>Task_0 → Queue_0</code>, <code>Task_1 → Queue_1</code> и т.д.</li>
  <li>Преимущества:
    <ul>
      <li>Уменьшение блокировок между потоками</li>
      <li>Равномерная загрузка при однородных задачах</li>
    </ul>
  </li>
  <li>Возможность модернизации на <strong>Least Loaded</strong> при сильно разнородных задачах</li>
</ul>

<h2>Заключение</h2>

<p>Собственный пул потоков:</p>
<ul>
  <li>Подходит для кастомных серверных решений</li>
  <li>Обеспечивает прозрачное логирование и контроль</li>
  <li>Демонстрирует хорошую масштабируемость и гибкость</li>
</ul>