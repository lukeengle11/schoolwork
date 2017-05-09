import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Luke on 4/18/2017.
 * Section 3
 */
public class Assignment3 {
    /*
     * Each process will contain the following:
     *      process[0]: process number
     *      process[1]: Arrival time
     *      process[2]: CPU burst time
     *      process[3]: priority
     *      process[4]: previous cpu time (the CPU time when the process lost the CPU)
     *                  This is initially set to cpu time - arrival time
     */
    private static List<int[]> processes;

    public static void main(String[] args){
        processes = new ArrayList<int[]>();

        String algorithm = "";
        int timeQuantum = 0;

        // Pull input from input.txt file
        FileReader fr = null;
        try {
            fr = new FileReader("input.txt");
        }
        catch(FileNotFoundException e){
            System.out.println("file not found");
        }
        try(BufferedReader br = new BufferedReader(fr)) {

            // get algorithm
            // if it's RR, get the time quantum
            String algorithmLine = br.readLine().trim();
            String[] algorithmPieces = algorithmLine.split("\\s+");
            algorithm = algorithmPieces[0];
            if (algorithmPieces.length != 1){
                timeQuantum = Integer.parseInt(algorithmPieces[1]);
            }

            // get number of processes
            int numProcesses = Integer.parseInt(br.readLine().trim());

            // Add the processes to a two dimensional array
            for (int i = 0; i < numProcesses; i++){

                int[] process = new int[5];
                String processString = br.readLine().trim();
                String[] processArray = processString.split("\\s+");
                for (int k = 0; k < 4; k++){
                    process[k] = Integer.parseInt(processArray[k]);
                }
                process[4] = process[1]; // Initialize previous cpu time to its arrival time
                processes.add(process);
            }
        }
        catch(IOException e){
            System.out.println("There was an IO error");
        }

        // Add a case statement for the algorithm to go into the correct one
        switch (algorithm){
            case "RR":
                roundRobin(timeQuantum);
                break;
            case "SJF":
                shortestJobFirst();
                break;
            case "PR_noPREMP":
                priorityNoPreempt();
                break;
            case "PR_withPREMP":
                priorityWithPreempt();
                break;
        }
    }

    private static void roundRobin(int timeQuantum){
        // Implement round robin
        int cpuTime = 0;
        boolean done = false;
        StringBuilder outputString = new StringBuilder();
        int totalWaitTime = 0;

        outputString.append("RR ");
        outputString.append(timeQuantum);
        outputString.append("\n");
        // loop until all processes are done
        while (!done){
            done = true;
            for (int[] process : processes){
                if (process[2] != 0)
                    done = false;
            }
            for (int[] process : processes){
                // If zero, continue
                if (process[2] == 0)
                    continue;

                // Multiple appends is more efficient than a single append with String concatenations
                outputString.append(cpuTime);
                outputString.append("\t");
                outputString.append(process[0]);
                outputString.append("\n");

                // Add cpuTime - oldCpuTime to total process wait time.
                totalWaitTime += (cpuTime - process[4]);

                // subtract timeQuantum from process time
                if (process[2] - timeQuantum >= 0){
                    cpuTime += timeQuantum;
                    process[2] -= timeQuantum;
                }
                else{
                    cpuTime += process[2];
                    process[2] = 0;
                }
                process[4] = cpuTime;
            }
        }
        // Calculate average wait time
        double averageWaitTime = (double)totalWaitTime / processes.size();
        outputString.append("AVG Waiting Time: ");
        outputString.append(Math.round(averageWaitTime * 100.0) / 100.0);
        outputString.append("\n");

        sendToOutputFile(outputString);
    }

    private static void shortestJobFirst(){
        int cpuTime = 0;
        boolean done = false;
        StringBuilder outputString = new StringBuilder();
        int totalWaitTime = 0;
        int minProcess = 1;
        int minTimeToCheck;
        int lastProcessRan = 0;

        outputString.append("SJF\n");

        while (!done){
            done = true;
            for (int[] process : processes){
                if (process[2] != 0) {
                    done = false;
                    break;
                }
            }
            if (done)
                break;

            // Find smallest process that can go
            // Find when to check again (when the next process becomes available)
            minTimeToCheck = Integer.MAX_VALUE;
            for (int[] process : processes){
                // No need to check since it already has 0 cpu burst
                if (process[2] == 0)
                    continue;

                // If the current minProcess is already zero, then update the minProcess
                if (processes.get(minProcess - 1)[2] == 0 && process[1] <= cpuTime)
                    minProcess = process[0];
                else if (process[1] <= cpuTime){
                    // Process is the min process if it isn't already and its CPU burst time is less than
                    // the current min process
                    if (process != processes.get(minProcess - 1) && process[2] < processes.get(minProcess - 1)[2])
                        minProcess = process[0];
                }
                // Calculate the time until the next process arrives
                else{
                    int timeToCheck = process[1] - cpuTime;
                    if (timeToCheck < minTimeToCheck){
                        minTimeToCheck = timeToCheck;
                    }
                }
            }
            if (processes.get(minProcess - 1)[2] == 0){
                cpuTime++;
                continue;
            }

            // Run some process stuff!
            int[] process = processes.get(minProcess - 1);

            if (lastProcessRan != process[0]) {
                // This process ran last time, don't add a new lines
                outputString.append(cpuTime);
                outputString.append("\t");
                outputString.append(process[0]);
                outputString.append("\n");
            }

            totalWaitTime += cpuTime - process[4];
            if (process[2] <= minTimeToCheck){
                cpuTime += process[2];
                process[2] = 0;
            }
            else{
                cpuTime += minTimeToCheck;
                process[2] -= minTimeToCheck;
            }
            process[4] = cpuTime;
            lastProcessRan = process[0];
        }

        // Calculate average wait time
        double averageWaitTime = (double)totalWaitTime / processes.size();
        outputString.append("AVG Waiting Time: ");
        outputString.append(Math.round(averageWaitTime * 100.0) / 100.0);
        outputString.append("\n");

        sendToOutputFile(outputString);
    }

    private static void priorityNoPreempt(){
        int cpuTime = 0;
        boolean done = false;
        StringBuilder outputString = new StringBuilder();
        int totalWaitTime = 0;
        int[] priority = new int[processes.size()];
        boolean priorityFlag = true; // if there is a priority conflict (same priority)
        int lastProcessRan = 0;

        outputString.append("PR_noPREMP\n");

        while (!done) {
            done = true;
            for (int[] process : processes) {
                if (process[2] != 0) {
                    done = false;
                    break;
                }
            }
            if (done)
                break;

            // Build the priority queue
            if (priorityFlag) {
                priorityFlag = buildQueue(priority);
            }

            int processNumber = getNextPriorityProcess(cpuTime, priority);
            if (processNumber == -1){
                // No process has arrived yet
                cpuTime++;
                continue;
            }

            int[] process = processes.get(processNumber);

            outputString.append(cpuTime);
            outputString.append("\t");
            outputString.append(process[0]);
            outputString.append("\n");

            totalWaitTime += cpuTime - process[4];

            cpuTime += process[2];
            process[2] = 0;
        }

        // Calculate average wait time
        double averageWaitTime = (double)totalWaitTime / processes.size();
        outputString.append("AVG Waiting Time: ");
        outputString.append(Math.round(averageWaitTime * 100.0) / 100.0);
        outputString.append("\n");

        sendToOutputFile(outputString);
    }

    private static void priorityWithPreempt(){
        int cpuTime = 0;
        boolean done = false;
        StringBuilder outputString = new StringBuilder();
        int totalWaitTime = 0;
        int[] priority = new int[processes.size()];
        boolean priorityFlag = true; // if there is a priority conflict (same priority)
        int minTimeToCheck;
        int lastProcessRan = 0;

        outputString.append("PR_withPREMP\n");

        while (!done) {
            done = true;
            for (int[] process : processes) {
                if (process[2] != 0) {
                    done = false;
                    break;
                }
            }
            if (done)
                break;

            // Build the priority queue
            if (priorityFlag) {
                priorityFlag = buildQueue(priority);
            }

            minTimeToCheck = getTimeUntilNextProcess(cpuTime);

            int processNumber = getNextPriorityProcess(cpuTime, priority);
            if (processNumber == -1){
                // No process has arrived yet
                cpuTime++;
                continue;
            }

            int[] process = processes.get(processNumber);

            if (lastProcessRan != process[0]) {
                outputString.append(cpuTime);
                outputString.append("\t");
                outputString.append(process[0]);
                outputString.append("\n");
            }

            totalWaitTime += cpuTime - process[4];
            if (process[2] <= minTimeToCheck){
                cpuTime += process[2];
                process[2] = 0;
            }
            else{
                cpuTime += minTimeToCheck;
                process[2] -= minTimeToCheck;
            }
            process[4] = cpuTime;
            lastProcessRan = process[0];
        }

        // Calculate average wait time
        double averageWaitTime = (double)totalWaitTime / processes.size();
        outputString.append("AVG Waiting Time: ");
        outputString.append(Math.round(averageWaitTime * 100.0) / 100.0);
        outputString.append("\n");

        sendToOutputFile(outputString);
    }

    private static void sendToOutputFile(StringBuilder output){
        BufferedWriter bw = null;
        FileWriter fw = null;

        try{
            fw = new FileWriter("output.txt");
            bw = new BufferedWriter(fw);
            bw.write(output.toString());
        }
        catch(IOException e){
            e.printStackTrace();
        }
        finally{
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Takes an array and builds the priority list.
    // Returns a boolean
    //      True - if there were collisions in priority
    //      False - if there were no collisions in priority
    private static boolean buildQueue(int[] priority){
        for (int i = 0; i < priority.length; i++){
            // Reset priority array to all zeros
            priority[i] = 0;
        }
        boolean priorityFlag = false;
        for (int[] process : processes) {
            if (process[2] == 0)
                continue;
            if (priority[process[3] - 1] != 0)
                priorityFlag = true;
            else
                priority[process[3] - 1] = process[0];
        }
        return priorityFlag;
    }

    // Returns the amount of time until the next process arrives
    private static int getTimeUntilNextProcess(int cpuTime){
        int minTimeToCheck = Integer.MAX_VALUE;
        // Check which processes have been started
        for (int[] process : processes){
            if (process[1] > cpuTime){
                int timeToCheck = process[1] - cpuTime;
                if (timeToCheck < minTimeToCheck){
                    minTimeToCheck = timeToCheck;
                }
            }
        }
        return minTimeToCheck;
    }

    // Returns the process number with the highest priority that also has arrived
    private static int getNextPriorityProcess(int cpuTime, int[] priority){
        int processNumber = 0;
        for (int p : priority) {
            processNumber = p - 1;
            if (processNumber == -1)
                // There is no process with this priority, move along
                continue;
            // The process has already ran
            if (processes.get(processNumber)[2] == 0) {
                continue;
            }
            if (processes.get(processNumber)[1] > cpuTime) {
                continue;
            }
            break;
        }
        return processNumber;
    }
}
