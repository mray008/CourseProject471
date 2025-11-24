
The executable file is "Producer_Consumer.jar".
To run the program: 
cd into CourseProject471/Producer_Consumer
then run : java -jar Producer_Consumer.jar
Results will show in the terminal, see " runs_output.txt"






This part of the project implements a multi-threaded Producer-Consumer system using Java.
* the semaphores are empty, full and mutex
* Multiple producers and multiple consumers. (This will be explained later.)
* It has a thread-safe counter using AtomicInteger

The simulation runs using multiple test configurations which are outputted to the console.
* Buffer Sizes (3,10)
* Producers sizes (2,5,10)
* Consumer sizes (2,5,10)


Each producer generates a sales transcation while each consumer processes these transactions and accumulates: 
*Total Sales
*Monthly Sales

The program runs until 1000 items are produced and consumed



Producer generates a sales record based off random generation for day,month,register and price.
*day(1-30)
*month(1-12)
*year  (always 16)
*register(1-6)
*price(0.05-999.99)
-Waits for the empty semaphore, acquires the mutex, writes the buffer
--Then it will increment totalProduced
---Releases the mutex and signals full(semaphore)


Producers stop after 1000 items have been produced


Consumer 
* waits on full, acquires the mutex, reads from the buffer
* Increments totalConsumed
* adds price to:
    * total sales
    *monthly sales for the item's month
* Releases mutex, signals empty
