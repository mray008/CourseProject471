import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class Producer_Consumer {

    private int bufferSize = 5;
    private Item[] buffer = new Item[bufferSize];
    private int head = 0;
    private int tail = 0;
    private double[] storeSales;

    double[] all_monthly_sales = new double[12];

    private double totalSales = 0;
    AtomicInteger totalProduced = new AtomicInteger(0);
    AtomicInteger totalConsumed = new AtomicInteger(0);

    private Semaphore empty = new Semaphore(bufferSize);
    private Semaphore full = new Semaphore(0);
    private Semaphore mutex = new Semaphore(1);
    volatile boolean done = false;

    public Producer_Consumer(int size) {
        this.bufferSize = size;
        buffer = new Item[bufferSize];
        empty = new Semaphore(bufferSize);
        full = new Semaphore(0);
        mutex = new Semaphore(1);
        all_monthly_sales = new double[12];
        storeSales = new double[11];

        totalSales = 0;//
        totalProduced.set(0);
        totalConsumed.set(0);
        head = 0;
        tail = 0;
    }

    public class Item {

        int day, month, year, register;
        double price;

        public Item(int day, int month, int year, int register, double price) {
            this.day = day;
            this.month = month;
            this.year = year;
            this.register = register;
            this.price = price;
        }
    }

    public class Producer extends Thread {

        int storeID;

        public Producer(int storeID) {
            this.storeID = storeID;
        }

        public void run() {
            Random random = new Random();
            while (true) {
                if (totalProduced.get() >= 1000) {
                    break;
                }

                int day = random.nextInt(30) + 1;
                int month = random.nextInt(12) + 1;
                int year = 16;
                int register = random.nextInt(6) + 1;
                double price = random.nextDouble() * 999.99 + 0.05;

                Item sales = new Item(day, month, year, register, price);

                try {
                    empty.acquire();
                    mutex.acquire();
                    if (totalProduced.get() >= 1000) {
                        mutex.release();
                        empty.release();
                        break;
                    }
                    buffer[tail] = sales;
                    tail = (tail + 1) % bufferSize;
                    storeSales[storeID - 1] += price;
                    totalProduced.incrementAndGet();

                    mutex.release();
                    full.release();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class Consumer extends Thread {

        int consumerID;
        double[] monthly_sales = new double[12];
        double localTotal = 0;

        public Consumer(int id) {
            this.consumerID = id;
        }

        public void run() {
            Random random = new Random();

            while (true) {
                try {
                    full.acquire();        // wait for item
                    mutex.acquire();       // enter critical section

                    // STOP condition: all items consumed AND producers are done
                    if (done && totalConsumed.get() >= totalProduced.get()) {
                        mutex.release();
                        break;
                    }

                    // Normal stop condition: 1000 consumed
                    if (totalConsumed.get() >= 1000) {
                        mutex.release();
                        break;
                    }

                    // Consume item
                    Item item = buffer[head];
                    head = (head + 1) % bufferSize;

                    totalConsumed.incrementAndGet();

                    // Track LOCAL stats
                    monthly_sales[item.month - 1] += item.price;
                    localTotal += item.price;

                    mutex.release();
                    empty.release();   // signal free slot

                    // Optional sleep (consumer is allowed to sleep)
                    Thread.sleep(random.nextInt(36) + 5);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // ==== AFTER EXITING LOOP: add to GLOBAL stats (protected by mutex) ====
            try {
                mutex.acquire();
                for (int i = 0; i < 12; i++) {
                    all_monthly_sales[i] += monthly_sales[i];
                }
                totalSales += localTotal;
                mutex.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // ==== PRINT LOCAL RESULTS ====
            synchronized (System.out){
            System.out.println("\nConsumer " + consumerID + " finished.");
          

            for (int i = 0; i < 12; i++) {
                System.out.printf("  Month %02d: $%.2f\n", i + 1, monthly_sales[i]);
            }
              System.out.printf("  Local Total: $%.2f\n", localTotal);
        }
    }
    }
    public void printMonthly(double[] monthly) {
        for (int i = 0; i < monthly.length; i++) {
            System.out.println((i + 1 < 10 ? "0" : "") + (i + 1) + " total monthly sales: " + monthly[i]+"/16");
        }
    }

    public void resetEverything() {
        totalProduced.set(0);
        totalConsumed.set(0);
        totalSales = 0;
        head = 0;
        tail = 0;
        buffer = new Item[bufferSize];

    }


public static void main(String[] args) {
    int[] bufferSizes = {3, 10};  // Possible buffer sizes
    int[] producerCounts = {2, 5, 10};  // Number of producers in each run
    int[] consumerCounts = {2, 5, 10};  // Number of consumers in each run

    // Wrap file writing in try-catch to handle potential IOException
    try (PrintWriter writer = new PrintWriter(new FileWriter("Sample output file.txt", true))) {
        for (int size : bufferSizes) {
            for (int pCount : producerCounts) {
                for (int cCount : consumerCounts) {

                    // Output the current test configuration to console and the file
                    System.out.println("=== RUN: buffer=" + size + " producers=" + pCount + " consumers=" + cCount + " ===");
                    writer.println("=== RUN: buffer=" + size + " producers=" + pCount + " consumers=" + cCount + " ===");

                    // Create a new instance of the Producer_Consumer class with the given buffer size
                    Producer_Consumer pc = new Producer_Consumer(size);

                    // Create and start the producer threads
                    Producer[] producers = new Producer[pCount];
                    for (int i = 0; i < pCount; i++) {
                        producers[i] = pc.new Producer(i + 1);
                    }

                    // Create and start the consumer threads
                    Consumer[] consumers = new Consumer[cCount];
                    for (int i = 0; i < cCount; i++) {
                        consumers[i] = pc.new Consumer(i + 1);
                    }

                    long programStart = System.currentTimeMillis();  // Start timing the execution

                    // Start the producer threads
                    for (Producer pr : producers) {
                        pr.start();
                    }

                    // Start the consumer threads
                    for (Consumer co : consumers) {
                        co.start();
                    }

                    // Wait for all producer threads to finish
                    try {
                        for (Producer pr : producers) {
                            pr.join();
                        }

                        // Tell consumers that no more items will be produced
                        pc.done = true;

                        // Wake up consumers to let them exit
                        for (int i = 0; i < cCount; i++) {
                            pc.full.release();
                        }

                        // Wait for all consumer threads to finish
                        for (Consumer co : consumers) {
                            co.join();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    long programEnd = System.currentTimeMillis();  // End timing the execution
                    long execTime = programEnd - programStart;

                    // Print the execution time and total sales to console and file
                    System.out.println("Execution time: " + execTime + "ms");
                    writer.println("Execution time: " + execTime + "ms");

                    System.out.printf("Total Sales: $%.2f%n", pc.totalSales);
                    writer.printf("Total Sales: $%.2f%n", pc.totalSales);

                    // Print the sales by each producer
                    System.out.println("Store Sales by Producer:");
                    writer.println("Store Sales by Producer:");
                    for (int i = 0; i < pCount; i++) {
                        System.out.printf("Store %d: $%.2f%n", i + 1, pc.storeSales[i]);
                        writer.printf("Store %d: $%.2f%n", i + 1, pc.storeSales[i]);
                    }

                    System.out.println("---------------------------------------");
                    writer.println("---------------------------------------");

                    // Print the monthly sales data
                    System.out.println("Monthly Sales: ");
                    for (int i = 0; i < 12; i++) {
                        System.out.printf("%02d %.2f%n", i + 1, pc.all_monthly_sales[i]);
                        writer.printf("%02d %.2f%n", i + 1, pc.all_monthly_sales[i]);
                    }

                    System.out.println();
                    writer.println();

                    // Reset the Producer_Consumer instance for the next run
                    pc.resetEverything();

                    // Optional sleep between runs (to simulate a delay between tests)
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    } catch (IOException e) {
        // Handle any IOException that may occur during file writing
        System.err.println("Error while writing to the file: " + e.getMessage());
        e.printStackTrace();
    }
}

}