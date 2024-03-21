import java.util.*;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

class Process {
    public int pid; // process ID
    public int size; // process size
    public int start; // start address
}

class Memory {
    private int max; // maximum memory size
    private List<Process> processes; // list of allocated processes
    private List<Process> holes; // list of free holes

    public Memory(int max) {
        this.max = max;
        processes = new ArrayList<Process>();
        holes = new ArrayList<Process>();
        holes.add(new Process());
        holes.get(0).size = max;
    }

    public List<Process> getProcesses() {
        return processes;
    }

    public void allocate(Process p, int fit) {
        int i = 0, j = -1;
        int min = max;
        for (Process hole : holes) {
            int gap = hole.size - p.size;
            if (gap >= 0 && gap < min && (fit == 0 || gap == min || fit * gap < fit * min)) {
                j = i;
                min = gap;
            }
            i++;
        }
        if (j >= 0) {
            Process hole = holes.get(j);
            p.start = hole.start;
            processes.add(p);
            if (min == 0) {
                holes.remove(j);
            } else {
                hole.start += p.size;
                hole.size -= p.size;
            }
        }
    }

    public void deallocate(Process p) {
        int i = 0;
        for (Process process : processes) {
            if (process.pid == p.pid) {
                processes.remove(i);
                break;
            }
            i++;
        }
        if (holes.size() > 0) {
            i = 0;
            for (Process hole : holes) {
                if (hole.start > p.start) {
                    holes.add(i, p);
                    mergeHoles(i - 1);
                    mergeHoles(i);
                    break;
                }
                i++;
            }
        } else {
            holes.add(p);
        }
    }

    private void mergeHoles(int i) {
        if (i >= 0 && i < holes.size() - 1) {
            Process p1 = holes.get(i);
            Process p2 = holes.get(i + 1);
            if (p1.start + p1.size == p2.start) {
                p1.size += p2.size;
                holes.remove(i + 1);
            }
        }
    }

    public void report() {
        System.out.println("Memory Usage:");
        System.out.println("==============");
        for (Process process : processes) {
            System.out.println("Process " + process.pid + " starts at address " + process.start + " with size "
                    + process.size + ".");
        }
        if (holes.size() > 0) {
            System.out.println("Free Holes:");
            System.out.println("===========");
            for (Process hole : holes) {
                System.out.println("Free hole starts at address " + hole.start + " with size " + hole.size + ".");
            }
        }
    }
}

public class MemoryAllocation {
    public static void main(String[] args) {
        int MEMORY_MAX = 1024; // default maximum memory size
        int PROC_SIZE_MAX = 512; // default maximum process size
        int NUM_PROC = 10; // default number of processes
        int MAX_PROC_SIZE = 10000; // default maximum process duration
        // Read configuration file
        Properties props = new Properties();
        try {
            FileInputStream in = new FileInputStream("config.properties");
            props.load(in);
            in.close();
        } catch (IOException e) {
            // Use default values
        }
        if (props.containsKey("MEMORY_MAX")) {
            MEMORY_MAX = Integer.parseInt(props.getProperty("MEMORY_MAX"));
        }
        if (props.containsKey("PROC_SIZE_MAX")) {
            PROC_SIZE_MAX = Integer.parseInt(props.getProperty("PROC_SIZE_MAX"));
        }
        if (props.containsKey("NUM_PROC")) {
            NUM_PROC = Integer.parseInt(props.getProperty("NUM_PROC"));
        }
        if (props.containsKey("MAX_PROC_SIZE")) {
            MAX_PROC_SIZE = Integer.parseInt(props.getProperty("MAX_PROC_SIZE"));
        }

        // Initialize memory
        Memory memory = new Memory(MEMORY_MAX);

        // Initialize processes
        Random rand = new Random();
        for (int i = 1; i <= NUM_PROC; i++) {
            Process p = new Process();
            p.pid = i;
            p.size = rand.nextInt(PROC_SIZE_MAX) + 1;
            memory.allocate(p, 0);
        }

        // Run simulation
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter command (A = allocate, D = deallocate, R = report, Q = quit):");
            String command = scanner.nextLine().trim().toUpperCase();
            if (command.equals("A")) {
                Process p = new Process();
                p.pid = memory.getProcesses().size() + 1;
                System.out.print("Enter process size: ");
                p.size = Integer.parseInt(scanner.nextLine().trim());
                memory.allocate(p, 0);
            } else if (command.equals("D")) {
                System.out.print("Enter process ID: ");
                int pid = Integer.parseInt(scanner.nextLine().trim());
                Process p = new Process();
                p.pid = pid;
                memory.deallocate(p);
            } else if (command.equals("R")) {
                memory.report();
            } else if (command.equals("Q")) {
                break;
            } else {
                System.out.println("Invalid command!");
            }
        }

        System.out.println("Simulation terminated.");
    }
}
