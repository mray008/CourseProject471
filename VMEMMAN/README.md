The virtual memory management problem required the simulation of four different replacement 
algorithms that are used in virtual memory management. I was given a file titled “Sample input 
data files.txt”, which contained 3000 memory addresses and was tasked to convert them into 
page numbers for different page sizes and then to run the four replacement algorithms on them. 
The replacement algorithms were FIFO (First In First Out), LRU (Least Recently Used), MRU 
(Most Recently Used), and OPT (Optimal).  The purpose of running the algorithms on the 
different page numbers was to find and analyze the percentage of page faults faced with each 
algorithm to attempt to determine which algorithm was best in different scenarios. The page 
sizes tested were 512 bytes, 1024 bytes, and 2048 bytes. The frame counts tested were 4, 8, and 
12 pages. When running the program’s executable file “VMEMMAN.jar”, the results are printed 
to the console, as shown below, and can be compared to the “Sample output file.txt” to ensure 
accuracy.

TLDR:
The executable file is "VMEMMAN.jar"
To run the program:
cd into CourseProject471/VMEMMAN
then run: java -jar VMEMMAN.jar
Results will show in the terminal, see "Sample output file.txt" to ensure it ran correctly.