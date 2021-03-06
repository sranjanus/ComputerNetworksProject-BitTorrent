
/********************************************************************************
 * File: peerProcess.java
 * ******************************************************************************
 * Purpose:This is the entry and exit point of the program.
 *
 * Description: Initializes the peers and starts the Thread for each Active
 * connection.
 * Once all the peers have the complete file, the threads are killed and exits
 * the process
 *
 * Class: Computer Networks, Spring 2016 (CNT5106C)
 *
 * Authors: Sarath Francis, Shashank Ranjan, Varun Kumar
 *
 *******************************************************************************/

import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;

public class peerProcess {
    public static int selfID;
    public static peerConfig configInfo;
    public static DataFileHandler fileHandler;
    public static Map<Integer, Peer> peerList;
    public static ArrayList<Integer> peerIdList;
//    public static ArrayList<Boolean> interestedOrNotInterestedList;
    public static int firstPeerId;
    public static int lastPeerId;
    public static int selfIndex;
    public static Vector<Worker> workers = new Vector<Worker>();
    public static Vector<Thread> wThreads = new Vector<Thread>();
    public static ArrayList<Integer> prefNeighbors;
    public static int optUnchokedNeighbor;
    public static loggerFile logfile;
    
    public static void main(String[] args){
        if(args.length == 0){
            System.out.println("Error: Peer ID not provided!!");
            return;
        }
        selfID = Integer.parseInt(args[0]);
        
        try {
            if(!initialize()){                                          // initialize config variables and peer list
                System.out.println("Error: Intialization Failed!!");
                return;
            }
        } catch(Exception e){
            System.out.println("Error: In Initialization. Message: " + e.getMessage());
        }
        /**
         * Establish TCP Connections.
         * Create a listening socket if the peer is not the last one
         */


        ServerSocket listenSocket = null;
        if(lastPeerId != selfID){
            try{
                System.out.println("Info: Peer " + selfID + " is opening a listen socket");
                listenSocket = new ServerSocket(peerList.get(selfID).getPort());
            } catch (Exception e){
                System.out.println("Error: Opening of Listen Socket Failed! " + e.getMessage());
                return;
            }
        }

        /**
         * Create connections to all the previous peers
         */

        Socket requestSocket = null;
        if(firstPeerId != selfID){
            for(int i = 0;i < selfIndex;i++){
                    int peerId = peerIdList.get(i);
                    try {
                        logfile.makesTCPConnection(peerId);
                        System.out.println("Info: Peer " + selfID + " is making TCP connection with Peer " + peerId);
                        requestSocket = new Socket(peerList.get(peerId).getHostname(), peerList.get(peerId).getPort());
                        System.out.println("Info: Peer " + selfID + " is made TCP connection with Peer " + peerId);
                        workers.add(new Worker(requestSocket, configInfo, selfID, peerId, peerList, peerIdList,fileHandler,logfile));
                        Thread thread = new Thread(workers.lastElement());
                        wThreads.add(thread);
                    } catch(Exception e){
                        System.out.println("Error: Peer " + selfID + " not able to make connection to Peer " + peerId + " Error: " +e.getMessage());
                        try{
                            exit();
                        }catch(Exception e1){
                            System.out.println("Error: While exiting. Message: " + e1.getMessage());
                        }
                        
                        return;
                    }
            }
        }

        /**
         *  Accept connections from all the later peers if there are any
         */

        if(listenSocket != null){
            Socket acceptSocket;
            System.out.println("Info: Peer " + selfID + " is listening for connection requests");
            int waitCount = configInfo.totalPeers - selfIndex - 1;
            while(waitCount > 0){
                try{
                    acceptSocket = listenSocket.accept();
                    System.out.println("Info: Peer " + selfID + " accepted connection request.");
                    workers.add(new Worker(acceptSocket, configInfo, selfID, peerList, peerIdList,fileHandler,logfile));
                    Thread thread = new Thread(workers.lastElement());
                    wThreads.add(thread);
                } catch(Exception e){
                    System.out.println("Error: Peer " + selfID + " not able to receieve requests. Message: " + e.getMessage());
                    try{
                        exit();
                    }catch(Exception e1){
                        System.out.println("Error: While exiting. Message: " + e1.getMessage());
                    }
                    return;
                }
                waitCount--;
            }
            
            if(waitCount > 0){
                try{
                    exit();
                }catch(Exception e){
                    System.out.println("Error: While exiting. Message: " + e.getMessage());
                }
                return;
            }
        }
        /**
         *  close the listen socket
         */
        
        try{
            listenSocket.close();
        } catch(Exception e){
            System.out.println("Error: Not able to close listen socket. Message: " + e.getMessage());
        }

        /**
         * Start all the Threads
         */

        for(int i = 0;i < wThreads.size();i++){
            wThreads.elementAt(i).start();
        }

        /**
         *  select peferred neighbours and optimisitc neighbour in a loop after every unchoking and optimistically unchoking interval
         */
        long prevUnchokingTime = System.currentTimeMillis();
        long prevOpUnchokingTime = prevUnchokingTime;
        long currentTime;
        optUnchokedNeighbor = -1;

        while(!allComplete()){
            currentTime = System.currentTimeMillis();
            if(currentTime - prevUnchokingTime > configInfo.getTimeUnchoke()*1000){
                selectPrefNeighbors();
                prevUnchokingTime = currentTime;
            }
            if(currentTime - prevOpUnchokingTime > configInfo.getTimeOptUnchoke()*1000) {
                selectOptimisticNeighbor();
                prevOpUnchokingTime = currentTime;
            }
            
            setChokedUnchoked();
        }
        
        try{
            exit();
        }catch(Exception e){
            System.out.println("Error: While exiting. Message: " + e.getMessage());
        }
        return;
    }
    
    public static boolean initialize() throws Exception{
        /**
         * create log file and start logging
         */
        logfile = new loggerFile(selfID);
        
        peerList = new HashMap<Integer, Peer>();
        peerIdList = new ArrayList<Integer>();
        configInfo = new peerConfig(peerList, peerIdList);
        int totalNoPeers = peerIdList.size();
        for(int i = 0;i < peerIdList.size();i++){
            if(i == 0){
                firstPeerId = peerIdList.get(i);
            }
            
            if(i == totalNoPeers - 1){
                lastPeerId = peerIdList.get(i);
            }
            
            if(peerIdList.get(i) == selfID){
                selfIndex = i;
            }
        }
        /**
         * Read the input file if it has the file otherwise create new folder
         */
        fileHandler = new DataFileHandler(selfID, peerList, configInfo);

        return true;
    }
    
    public static void exit() throws Exception {
        //terminate the threads
        for(int i = 0;i < wThreads.size();i++){
            try{
                wThreads.elementAt(i).join();
            } catch(Exception e){
                System.out.println("Error: In Thread Join. Message: " + e.getMessage());
            }
        }
        
        // close data file handles
        fileHandler.closeFile();
        
        // close logfile
        for(int i = 0;i < workers.size();i++){
            try{
                workers.elementAt(i)._socket.close();
            } catch(Exception e){
                System.out.println("Error: While closing thread sockets. Message: " + e.getMessage());
            }
        }
        
        System.out.println("Info: Peer " + selfID + " is exiting!!");
    }
    
    public  static void selectPrefNeighbors(){
        prefNeighbors = new ArrayList<Integer>();
        for(int i = 0;i < peerIdList.size();i++){
            int candidateId = peerIdList.get(i);
            if(candidateId != selfID && !peerList.get(candidateId).isExited() && !peerList.get(candidateId).hasCompleteFile() && peerList.get(candidateId).isInterested()){
                prefNeighbors.add(peerIdList.get(i));
            }
        }

        if(prefNeighbors.size() > configInfo.getPrefNeighbors()){
            /**
             * If available number of candidates is greater than preferred, select neighbors
             * if self has the full file, select neighbours randomly
             */

            int toRemove = prefNeighbors.size() - configInfo.getPrefNeighbors();
            if(peerList.get(selfID).hasCompleteFile()){
                Random rndm = new Random();
                for(int i = 0;i < toRemove;i++){
                    if(prefNeighbors.size() > 1){
                        int indexToRemove = Math.abs(rndm.nextInt(prefNeighbors.size()-1));
                        prefNeighbors.remove(indexToRemove);
                    } else {
                        System.out.println("Error: Preferred Neighbor size is <= 1");
                    }
                }
            }
            else {
                /**
                 *  sort the neighbours based on download rate and select top "peerConfig.prefNeighbors" candidates
                 */
                for(int i = 0;i < prefNeighbors.size();i++){
                    for(int j = 0;j < prefNeighbors.size() - i - 1;j++){
                        int id1 = prefNeighbors.get(j);
                        int id2 = prefNeighbors.get(j + 1);
                        int speed1 = peerList.get(id1).getSpeed();
                        int speed2 = peerList.get(id2).getSpeed();
                        if(speed2 > speed1){
                            prefNeighbors.set(j, id2);
                            prefNeighbors.set(j + 1, id1);
                        }
                    }
                }

                for(int i = prefNeighbors.size() - 1; toRemove != 0;i--){
                    prefNeighbors.remove(i);
                    toRemove--;
                }
            }
        }
        logfile.changeOfPreferredNeighbourLog(prefNeighbors);

        /**
         * Reset speed to zero after selecting the preferred neighbours
         */

        for(Map.Entry<Integer, Peer> peer : peerList.entrySet()) {
            peer.getValue().resetSpeed();
        };

    }
    public static void selectOptimisticNeighbor() {
        ArrayList<Integer> candidates = new ArrayList<Integer>();
        for(int i = 0; i < peerIdList.size(); i++) {
            int candidateId = peerIdList.get(i);
            if(candidateId != selfID && !peerList.get(candidateId).isExited() && !peerList.get(candidateId).hasCompleteFile() && peerList.get(candidateId).isInterested()){
                candidates.add(peerIdList.get(i));
            }
        }
        /**
         * All peer are candidates to become the optimistically unchoked Neighbour except self
         */
        
        if (prefNeighbors != null) {
            for (int i = 0; i<prefNeighbors.size(); i++) {
                if(candidates.contains(prefNeighbors.get(i))){
                    candidates.remove(prefNeighbors.get(i));
                }
            }
        }
        /**
         * Optimistic Preferred neighbour should not be any of the preferred neighbour
         */

        if(candidates.size() == 0) {
            optUnchokedNeighbor = -1;
        } else if(candidates.size() == 1){
            optUnchokedNeighbor = candidates.get(0);
        } else {
            Random random = new Random();
            random.setSeed(System.currentTimeMillis());
            optUnchokedNeighbor = candidates.get(random.nextInt(candidates.size()-1));
        }

        /**
         * select random candidate from the remaining candidates
         */

        logfile.changeOfOptUnchokedNeighbourLog(optUnchokedNeighbor);
    }

    /**
     * Set Choked and unchoked based on the preferred Neighbors and Optmistic Preferred Neighbors
     */
    public static void setChokedUnchoked(){
        for(int i = 0;i < peerIdList.size(); i++){
            if((prefNeighbors != null && prefNeighbors.contains(peerIdList.get(i))) || peerIdList.get(i) == optUnchokedNeighbor){
                peerList.get(peerIdList.get(i)).setChoked(false);
            } else {
                peerList.get(peerIdList.get(i)).setChoked(true);
            }
        }
    }

    /**
     * Check if all the peers have the complete file
     *
     */

    public static boolean allComplete(){
        boolean allComplete = true;
        for(int i = 0;i < peerIdList.size();i++){
            allComplete = allComplete && peerList.get(peerIdList.get(i)).hasCompleteFile();
        }
        return allComplete;
    }

}