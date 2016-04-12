import java.util.*;
import java.lang.*;
import java.io.*;
import java.lang.Math.*;
import java.net.*;
import java.lang.Thread;
import java.util.Random;

public class peerProcess {
    public static int selfID;
    public static peerConfig configInfo;
    public static Vector<Worker> workers = new Vector<>();
    public static Vector<Thread> wThreads = new Vector<>();
    
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
            
        }
        
        //Debug
        //debugPrintPeerList();
        
        // Establish TCP Connections.
        
        // Create a listening socket if the peer is not the last one
        ServerSocket listenSocket = null;
        if(configInfo.lastPeerId != selfID){
            try{
                System.out.println("Info: Peer " + selfID + " is opening a listen socket");
                listenSocket = new ServerSocket(configInfo.peerList.get(selfID).getPort());
            } catch (Exception e){
                System.out.println("Error: Opening of Listen Socket Failed! " + e.getMessage());
                return;
            }
        }
        
        // Create connections to all the previous peers
        Socket requestSocket = null;
        if(configInfo.firstPeerId != selfID){
            for(int i = 0;i < configInfo.selfIndex;i++){
                    int peerId = configInfo.peerIdList.get(i);
                    try {
                        System.out.println("Info: Peer " + selfID + " is making TCP connection with Peer " + peerId);
                        requestSocket = new Socket(configInfo.peerList.get(peerId).getHostname(), configInfo.peerList.get(peerId).getPort());
                        System.out.println("Info: Peer " + selfID + " is made TCP connection with Peer " + peerId);
                        workers.add(new Worker(requestSocket, configInfo, selfID, peerId));
                        Thread thread = new Thread(workers.lastElement());
                        wThreads.add(thread);
                    } catch(Exception e){
                        System.out.println("Error: Peer " + selfID + " not able to make connection to Peer " + peerId);
                        try{
                            exit();
                        }catch(Exception e1){
                            
                        }
                        
                        return;
                    }
            }
        }
        
        // Accept connections from all the later peers if there are any
        if(listenSocket != null){
            Socket acceptSocket;
            System.out.println("Info: Peer " + selfID + " is listening for connection requests");
            int waitCount = configInfo.totalPeers - configInfo.selfIndex - 1;
            while(waitCount > 0){
                try{
                    acceptSocket = listenSocket.accept();
                    System.out.println("Info: Peer " + selfID + " accepted connection request.");
                    workers.add(new Worker(acceptSocket, configInfo, selfID));
                    Thread thread = new Thread(workers.lastElement());
                    wThreads.add(thread);
                } catch(Exception e){
                    System.out.println("Error: Peer " + selfID + " not able to receieve requests");
                    try{
                        exit();
                    }catch(Exception e1){
                        
                    }
                    return;
                }
                waitCount--;
            }
            
            if(waitCount > 0){
                try{
                    exit();
                }catch(Exception e){
                    
                }
                return;
            }
        }
        
        
        // close the listen socket
        try{
            listenSocket.close();
        } catch(Exception e){
            
        }
        
        for(int i = 0;i < wThreads.size();i++){
            wThreads.elementAt(i).start();
        }
        
        try{
            exit();
        }catch(Exception e){
            
        }
        return;
    }
    
    public static boolean initialize() throws Exception{
        configInfo = new peerConfig(selfID);
        return true;
    }
    
    public static void exit() throws Exception {
        for(int i = 0;i < wThreads.size();i++){
            try{
                wThreads.elementAt(i).join();
            } catch(Exception e){
                
            }
        }
        
        // close logfile
        
        for(int i = 0;i < workers.size();i++){
            try{
                workers.elementAt(i)._socket.close();
            } catch(Exception e){
                
            }
        }
        
        System.out.println("Info: Peer " + selfID + "is exiting!!");
    }
}