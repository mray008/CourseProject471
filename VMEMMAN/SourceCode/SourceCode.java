package VMEMMAN.SourceCode;
import java.util.*;
import java.io.*;
public class SourceCode{

    static ArrayList<Integer> dataStorage = new ArrayList<>();

    static int[] pageSizes = {512, 1024, 2048};
    static int[] frameCount = {4, 8, 12};
    static String fileString = "";

private static void loadFile(File p) throws IOException {//standard file reader + buffered reader combo. not much to say here
		try (BufferedReader br = new BufferedReader(new FileReader(p))) {
			String line;
			while ((line = br.readLine()) != null) {
				try {
					dataStorage.add(Integer.parseInt(line));
				} catch (NumberFormatException e) {
					System.err.println("Failed to read %s".formatted(line.trim()));
					System.err.println("Skipping...");
				}
			}
			
		} catch (IOException e) {
			System.err.println("Error reading from file: %s".formatted(p.getAbsolutePath().toString()));
			throw e;
		}
	
	}

   

 private static ArrayList<Integer> convertToPageNumbers(int pageSize, ArrayList<Integer> addresses) {
        ArrayList<Integer> pageSeq = new ArrayList<>();
        for (int addr : addresses) {//simple loop to convert to page number
            pageSeq.add(addr / pageSize);
        }
        return pageSeq;
    }
//fifo run
public static int fifo(ArrayList<Integer> pages, int frames){
    Set<Integer> mem = new HashSet<>(); //pages in ram
    Queue<Integer> queue = new LinkedList<>(); //order of pages
    int faultCount = 0; //fault count

    for (int p : pages){
        if (!mem.contains(p)){ //check if loaded, if not proceed
            faultCount++;
            if(mem.size() == frames){ //check if memory is full
                int victim = queue.poll(); //murder the earliest page D:
                mem.remove(victim);
            }
            mem.add(p); //new page
            queue.add(p); //add to queue
        }

    }
    return faultCount;
}

//lru run
public static int lru(ArrayList<Integer> pages, int frames){
    LinkedHashMap<Integer, Integer> order = new LinkedHashMap<>(frames, 0.75f, true); //access order to ensure most recently accessed goes to the end
    int faultCount = 0; //counter for faults
    for (int p : pages){
        if(!order.containsKey(p)){ //check if loaded, if not proceed
            faultCount++;
            if (order.size() == frames) { //check if memory is full
                Integer victim = order.keySet().iterator().next(); //murder the oldest page D:
                order.remove(victim);
            }
                
            }
            order.put(p,1); //add page to order
        }
        return faultCount;
 
    }

    //mru run
    public static int mru(ArrayList<Integer> pages, int frames){
    LinkedList<Integer> stack = new LinkedList<>();//linked list to act like a stack end of list is MRU front of list is LRU
    Set<Integer> mem = new HashSet<>();
    int faultCount = 0; //fault count

    for (int p : pages){
        if (!mem.contains(p)){ //check if loaded, if not proceed
            faultCount++;
            if(mem.size() == frames){ //check if memory is full
                int victim = stack.removeLast(); //murder the newest page D:
                mem.remove(victim);
            }
            mem.add(p); //new page
        
        }
        else{
            stack.remove((Integer)p);//remove from stack
        }
        stack.add(p);//add to stack

    }
    return faultCount;
}
//optimal run
public static int optimal(ArrayList<Integer> pages, int frames){
    Set<Integer> mem = new HashSet<>();//pages in ram
    int faultCount = 0;//fault counter
    for(int i = 0; i<pages.size(); i++){//loop pages accesses by index
        int p = pages.get(i);
        if(!mem.contains(p)){//check for fault
            faultCount++;
        if(mem.size()==frames){//check if mem is full
            int victim = -1, farthest = -1;//victim + how far in the future the page is loaded again
            for(int m : mem){//loop all pages in ram
                int next = pages.subList(i+1, pages.size()).indexOf(m);//check when page is used again
                if (next == -1){//if never used again evict
                    victim = m;
                    break;
                }
                if (next > farthest){//evict farthest in the future
                    farthest = next;
                    victim = m;
                }
            }
            mem.remove(victim);//kill the victim
        }
        mem.add(p);//add new page to mem
    }
}
return faultCount;//return fault count
}
    public static void runSim(ArrayList<Integer> addresses){
        System.out.println("PageSize | Frames | Algorithm | Fault %");
        for(int pageSize : pageSizes){
            ArrayList<Integer> pages = convertToPageNumbers(pageSize, addresses);
            for(int frames : frameCount){
                int total = pages.size();
                int f1 = fifo(pages, frames);//fault count for each of the replacement algos
                int f2 = lru(pages, frames);
                int f3 = mru(pages, frames);
                int f4 = optimal(pages, frames);

                printStats(pageSize, frames, "FIFO", f1, total); //print the stats !
                printStats(pageSize, frames, "LRU", f2, total);
                printStats(pageSize, frames, "MRU", f3, total);
                printStats(pageSize, frames, "OPT", f4, total);
                
                // fileString += printStatsForFile(pageSize, frames, "FIFO", f1, total) +
                //              printStatsForFile(pageSize, frames, "LRU", f2, total) + 
                //              printStatsForFile(pageSize, frames, "MRU", f3, total) +
                //              printStatsForFile(pageSize, frames, "OPT", f4, total);
                
                
            }
        }

    }
    public static void printStats(int size, int frames, String alg, int faults, int total){
        double fpr = (faults * 100.0)/total;//get the percent of faults
        System.out.printf("%8d | %6d | %9s | %7.2f%%%n", size, frames, alg, fpr);//formatted print
    }
//      public static String printStatsForFile(int size, int frames, String alg, int faults, int total){
//         double fpr = (faults * 100.0)/total;//get the percent of faults
//         return String.format("%8d | %6d | %9s | %7.2f%%%n", size, frames, alg, fpr);//formatted string
//     }
//     public static void writeToFile(File file, String string){
//         try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))){//append false for overwrite
//             bw.write(fileString);//write to the file
//             System.out.println("Content overwritten");//success
//         }
//         catch (IOException e) {
//             System.err.println("error overwriting file" + e.getMessage());//fail
//         }
// }
    

 public static void main(String[] args) throws Exception {
 File filePath = new File("Sample input data files.txt");//feed the file path
//  File filePath2 = new File("Sample output file.txt");//feed the file path
        loadFile(filePath);//load the file
        runSim(dataStorage);//run the sim
        // writeToFile(filePath2, fileString);//overwite sample output
}
    
}

