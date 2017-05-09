import java.io.*;
import java.util.*;

/**
 * Created by Luke on 5/2/2017.
 */
public class Assignment4 {
    private static StringBuilder output = new StringBuilder();

    public static void main(String[] args){
        int[] requests = new int[4];
        int numPages = 0;
        int frames = 0;
        
        // Pull input from input.txt file
        FileReader fr = null;
        try {
            fr = new FileReader("input.txt");
        }
        catch(FileNotFoundException e){
            System.out.println("file not found");
            System.exit(-1);
        }
        try(BufferedReader br = new BufferedReader(fr)) {

            // get the first line and split to get the setup
            String firstLine = br.readLine().trim();
            String[] setup = firstLine.split("\\s+");
            numPages = Integer.parseInt(setup[0]);
            frames = Integer.parseInt(setup[1]);

            int numRequests = Integer.parseInt(setup[2]);
            requests = new int[numRequests];

            for (int i = 0; i < numRequests; i++){
                requests[i] = Integer.parseInt(br.readLine().trim());
            }
        }
        catch(IOException e){
            System.out.println("There was an IO error");
            System.exit(-1);
        }

        fifo(numPages, frames, requests);
        optimal(numPages, frames, requests);
        lru(numPages, frames, requests);
        sendToOutputFile();
    }

    private static void fifo(int numPages, int numFrames, int[] requests){
        int[] frames = new int[numFrames];
        Arrays.fill(frames, Integer.MAX_VALUE);
        int counter = 0;
        int faults = 0;

        output.append("FIFO\n");

        // For each request, if it's not in the array, we need to replace something
        for (int request : requests){
            boolean inFrames = false;
            for (int i = 0; i < frames.length; i++){
                if (request == frames[i]){
                    output.append("Page " + request + " already in Frame " + i + "\n");
                    inFrames = true;
                    break;
                }
            }
            if (inFrames)
                continue;

            if (frames[counter] == Integer.MAX_VALUE){
                // There is a free frame at counter's position
                output.append("Page " + request + " loaded into Frame " + counter + "\n");
            }
            else{
                output.append("Page " + frames[counter] + " unloaded from Frame " + counter + ", ");
                output.append("Page " + request + " loaded into Frame " + counter + "\n");
            }
            frames[counter] = request;
            counter = (counter + 1) % numFrames;
            faults++;
        }
        output.append(faults + " page faults\n\n");
    }

    private static void optimal(int numPages, int numFrames, int[] requests){
        ArrayList<Integer> frames = new ArrayList<Integer>(numFrames);
        int faults = 0;

        output.append("Optimal\n");

        for (int k = 0; k < requests.length; k++) {
            int request = requests[k];
            boolean inFrames = false;
            for (int i = 0; i < frames.size(); i++) {
                if (request == frames.get(i)) {
                    output.append("Page " + request + " already in Frame " + i + "\n");
                    inFrames = true;
                    break;
                }
            }
            if (inFrames)
                continue;
            if (frames.size() < numFrames){
                // Frames are not filled up, load the page into the frame
                output.append("Page " + request + " loaded into Frame " + frames.size() + "\n");
                frames.add(request);
            }
            else{
                // There is going to be a swap here.
                // Check the future to see which page in the frame will be used last
                ArrayList<Integer> frameList = (ArrayList<Integer>)frames.clone();
                for (int i = k + 1; i < requests.length; i++){
                    if (frameList.size() == 1){
                        break;
                    }
                    if (frameList.contains(requests[i])){
                        frameList.remove(frameList.indexOf(requests[i]));
                    }
                }
                int replacementPage = frameList.get(0);
                int frameIndex = frames.indexOf(replacementPage);
                output.append("Page " + replacementPage + " unloaded from Frame " + frameIndex + ", ");
                output.append("Page " + request + " loaded into Frame " + frameIndex + "\n");
                frames.set(frameIndex, request);
            }
            faults++;
        }
        output.append(faults + " page faults\n\n");
    }

    private static void lru(int numPages, int numFrames, int[] requests){
        ArrayList<Integer> frames = new ArrayList<Integer>(numFrames);
        int faults = 0;

        output.append("LRU\n");

        for (int k = 0; k < requests.length; k++) {
            int request = requests[k];
            boolean inFrames = false;
            for (int i = 0; i < frames.size(); i++) {
                if (request == frames.get(i)) {
                    output.append("Page " + request + " already in Frame " + i + "\n");
                    inFrames = true;
                    break;
                }
            }
            if (inFrames)
                continue;
            if (frames.size() < numFrames){
                // Frames are not filled up, load the page into the frame
                output.append("Page " + request + " loaded into Frame " + frames.size() + "\n");
                frames.add(request);
            }
            else{
                // There is going to be a swap here.
                // Check the past to see which frame was used the longest time ago
                ArrayList<Integer> frameList = (ArrayList<Integer>)frames.clone();
                for (int i = k - 1; i < requests.length; i--){
                    if (frameList.size() == 1){
                        break;
                    }
                    if (frameList.contains(requests[i])){
                        frameList.remove(frameList.indexOf(requests[i]));
                    }
                }
                int replacementPage = frameList.get(0);
                int frameIndex = frames.indexOf(replacementPage);
                output.append("Page " + replacementPage + " unloaded from Frame " + frameIndex + ", ");
                output.append("Page " + request + " loaded into Frame " + frameIndex + "\n");
                frames.set(frameIndex, request);
            }
            faults++;
        }
        output.append(faults + " page faults");
    }

    // Send the StringBuilder output to an output file
    private static void sendToOutputFile(){
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
}