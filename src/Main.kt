import kotlin.concurrent.thread
import java.util.concurrent.*
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.atomic.AtomicInteger

fun main() {
    while (true) {
        println("\n" +
            """
            Выберите задачу (1-12). Для выхода напишите 0:
            1. Синхронизация счётчика
            2. Генерация последовательности чисел
            3. Распределение задач с использованием пула потоков
            4. Симуляция работы банка
            5. Барьер синхронизации
            6. Ограниченный доступ к ресурсу
            7. Обработка результатов задач
            8. Симуляция производственной линии
            9. Многопоточная сортировка
            10. Обед философов
            11. Расчёт матрицы в параллельных потоках
            12. Таймер с многопоточностью
            0. Выход
            """.trimIndent()
        )
        when (readlnOrNull()?.toIntOrNull()) {
            1 -> task1()
            2 -> task2()
            3 -> task3()
            4 -> task4()
            5 -> task5()
            6 -> task6()
            7 -> task7()
            8 -> task8()
            9 -> task9()
            10 -> task10()
            11 -> task11()
            12 -> task12()
            0 -> return
            else -> println("Введено некорректное значение.\nВведите значение еще раз:")
        }
    }
}

class Account(var balance: Int) {
    private val lock = ReentrantLock()

    fun transfer(target: Account, amount: Int) {
        lock.lock()
        try {
            if (balance >= amount) {
                balance -= amount
                target.balance += amount
            }
        } finally {
            lock.unlock()
        }
    }
}

fun factorial(n: Int): Long {
    return if (n <= 1) 1 else n * factorial(n - 1)
}

fun task1() {
    val counter = AtomicInteger(0)
    val lock = ReentrantLock()
    val threads = List(5) {
        thread {
            repeat(1000) {
                lock.lock()
                try {
                    counter.incrementAndGet()
                } finally {
                    lock.unlock()
                }
            }
        }
    }
    threads.forEach { it.join() }
    println("Итоговое значение счётчика: ${counter.get()}")
}

fun task2() {
    val list = CopyOnWriteArrayList<Int>()
    val threads = List(10) {
        thread {
            for (i in 1..100) {
                list.add(i)
            }
        }
    }
    threads.forEach { it.join() }
    println("Размер списка: ${list.size}")
}

fun task3() {
    val executor = Executors.newFixedThreadPool(4)
    for (i in 1..20) {
        executor.submit {
            println("Задача $i выполняется в потоке ${Thread.currentThread().name}")
        }
    }
    executor.shutdown()
    executor.awaitTermination(1, TimeUnit.MINUTES)
}

fun task4() {
    val accounts = listOf(Account(1000), Account(2000), Account(3000))
    val executor = Executors.newFixedThreadPool(4)
    repeat(10) {
        executor.submit {
            val from = accounts.random()
            val to = accounts.random()
            if (from != to) {
                from.transfer(to, 100)
            }
        }
    }
    executor.shutdown()
    executor.awaitTermination(1, TimeUnit.MINUTES)
    accounts.forEachIndexed { index, account ->
        println("Аккаунт $index: ${account.balance}")
    }
}

fun task5() {
    val barrier = CyclicBarrier(5) {
        println("Все потоки достигли барьера")
    }
    val threads = List(5) {
        thread {
            println("Поток $it выполняет задачу")
            barrier.await()
            println("Поток $it продолжает выполнение")
        }
    }
    threads.forEach { it.join() }
}

fun task6() {
    val semaphore = Semaphore(2)
    val threads = List(5) {
        thread {
            semaphore.acquire()
            println("Поток $it получил доступ к ресурсу")
            Thread.sleep(1000)
            println("Поток $it освобождает ресурс")
            semaphore.release()
        }
    }
    threads.forEach { it.join() }
}

fun task7() {
    val executor = Executors.newFixedThreadPool(10)
    val futures = List(10) {
        executor.submit(Callable {
            (1..10).random().also { num ->
                println("Поток $it вычисляет факториал $num")
                factorial(num)
            }
        })
    }
    futures.forEachIndexed { index, future ->
        println("Результат задачи $index: ${future.get()}")
    }
    executor.shutdown()
}

fun task8() {
    val queue = LinkedBlockingQueue<Int>()
    val producer = thread {
        for (i in 1..10) {
            queue.put(i)
            println("Произведено: $i")
        }
    }
    val consumer = thread {
        while (true) {
            val item = queue.take()
            println("Обработано: $item")
            if (item == 10) break
        }
    }
    producer.join()
    consumer.join()
}

fun task9() {
    val array = (1..100).shuffled().toIntArray()
    val chunks = array.size / 4
    val executor = Executors.newFixedThreadPool(4)
    val futures = (0..<4).map { i ->
        val start = i * chunks
        val end = if (i == 3) array.size else start + chunks
        executor.submit(Callable {
            array.copyOfRange(start, end).sorted().toIntArray()
        })
    }
    val sortedArray = futures.flatMap { it.get().toList() }.sorted().toIntArray()
    executor.shutdown()
    executor.awaitTermination(1, TimeUnit.MINUTES)

    println("Отсортированный массив: ${sortedArray.joinToString()}")
}

fun task10() {
    val forks = List(5) { ReentrantLock() }
    val philosophers = List(5) { i ->
        thread {
            val leftFork = forks[i]
            val rightFork = forks[(i + 1) % 5]
            while (true) {
                if (leftFork.tryLock()) {
                    try {
                        if (rightFork.tryLock()) {
                            try {
                                println("Философ $i ест")
                                Thread.sleep(100)
                                break
                            } finally {
                                rightFork.unlock()
                            }
                        }
                    } finally {
                        leftFork.unlock()
                    }
                }
                Thread.sleep(100)
            }
        }
    }
    philosophers.forEach { it.join() }
}

fun task11() {
    val matrix1 = arrayOf(intArrayOf(1, 2), intArrayOf(3, 4))
    val matrix2 = arrayOf(intArrayOf(5, 6), intArrayOf(7, 8))
    val result = Array(2) { IntArray(2) }
    val executor = Executors.newFixedThreadPool(2)
    for (i in 0..<2) {
        executor.submit {
            for (j in 0..<2) {
                result[i][j] = matrix1[i][0] * matrix2[0][j] + matrix1[i][1] * matrix2[1][j]
            }
        }
    }
    executor.shutdown()
    executor.awaitTermination(1, TimeUnit.MINUTES)
    println("Результат умножения матриц:")
    result.forEach { row ->
        println(row.joinToString())
    }
}

fun task12() {
    var running = true
    val timerThread = thread {
        var seconds = 0
        while (running) {
            println("Прошло $seconds секунд")
            Thread.sleep(1000)
            seconds++
        }
    }
    thread {
        Thread.sleep(10000)
        running = false
        println("Таймер остановлен")
    }
    timerThread.join()
}
