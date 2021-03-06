/********************************************************************************
 * File: Peer.java
 * ******************************************************************************
 * Purpose: This class is used for reading the configurations files and initilizing the structures
 * that will be used to store informations from the config files
 *
 *
 * Description:
 *
 *
 * Class: Computer Networks, Spring 2016 (CNT5106C)
 *
 * Authors: Sarath Francis, Shashank Ranjan, Varun Kumar
 *
 *******************************************************************************/


import java.io.*;
import java.util.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class peerConfig {
	private FileInputStream in = null;
	private static int noOfprefNeighbors;
	private static int timeUnchoke;
	private static int timeOptUnchoke;
	private static String fileName;
	private static int sizeOfFile;
	private static int sizeOfPiece;
	private static int sizeOfLastPiece;
	private static int totalPieces;
	private static int totalPeersWithEntireFile = 0;
    public static int totalPeers;
	
	public peerConfig(Map<Integer, Peer> peerList, ArrayList<Integer> peerIdList) throws Exception
	{
        String path = System.getProperty("user.dir");
		readCommonConfig(path + "/Common.cfg");
		readPeerInfoConfig(path + "/PeerInfo.cfg", peerList, peerIdList);
	}

	public int getPrefNeighbors(){
		 return noOfprefNeighbors;
	}
	public int getTotalPeersWithEntireFile(){
		return totalPeersWithEntireFile;
	}
	public int getTimeUnchoke(){
		return timeUnchoke;
	}
	public int getTimeOptUnchoke(){
		return timeOptUnchoke;
	}
	public String getFileName(){
		return fileName;
	}
	public int getSizeOfFile(){
		return sizeOfFile;
	}
	public int getSizeOfPiece(){
		return sizeOfPiece;
	}
	public int getSizeOfLastPiece(){
		return sizeOfLastPiece;
	}
	public int getTotalPieces(){
		return totalPieces;
	}
	public int getTotalPeers(){
		return totalPeers;
	}

	/**
	 * This function reads the Common.cfg file and populates the corresponding variables
	 * param The path of the Common.cfg file
	 */
	public void readCommonConfig(String commonConfigPath) throws Exception
	{
		try{
			in = new FileInputStream(commonConfigPath);
			Scanner scanner = new Scanner(in);
			
			String token = "";
			
			while(scanner.hasNext())
			{
				token = scanner.next();
				if(token.compareToIgnoreCase("NumberOfPreferredNeighbors") == 0)
				{
					if(!scanner.hasNextInt())
						throw new Exception("Error:  Integer value required for the number of preferred neighbors.");
					else
						noOfprefNeighbors = scanner.nextInt();
				}
				else if(token.compareToIgnoreCase("UnchokingInterval") == 0)
				{
					if(!scanner.hasNextInt())
						throw new Exception("Error:  Integer value required for the preferred neighbor selection interval.");
					else
						timeUnchoke = scanner.nextInt();
				}
				else if(token.compareToIgnoreCase("OptimisticUnchokingInterval") == 0)
				{
					if(!scanner.hasNextInt())
						throw new Exception("Error:  Integer value required for the optimistically unchoked neighbor selection interval.");
					else
						timeOptUnchoke = scanner.nextInt();
				}
				else if(token.compareToIgnoreCase("FileName") == 0)
				{
					if(!scanner.hasNext())
						throw new Exception("Error:  File name not correctly specified.");
					else
						fileName = scanner.next(); 
				}
				else if(token.compareToIgnoreCase("FileSize") == 0)
				{
					if(!scanner.hasNextInt())
						throw new Exception("Error:  Integer value required for the file size.");
					else
						sizeOfFile = scanner.nextInt();
				}	
				else if(token.compareToIgnoreCase("PieceSize") == 0)
				{
					if(!scanner.hasNextInt())
						throw new Exception("Error:  Integer value required for the piece size.");
					else
						sizeOfPiece = scanner.nextInt();
				}
				else
				{
					throw new Exception("Error.  Invalid string found in the common configuration file." + token);
				}
			}		
		
			if(in != null)
				in.close();
			
			if(sizeOfFile % sizeOfPiece == 0) {
				sizeOfLastPiece = sizeOfPiece;
				totalPieces = sizeOfFile/sizeOfPiece;
			}
			else{
				sizeOfLastPiece = sizeOfFile % sizeOfPiece;
				totalPieces = sizeOfFile/sizeOfPiece + 1;
			}
		}
		catch(IOException exception)
		{
			exception.printStackTrace();
		}
	}
	/**
	 * This function reads the PeerInfo.cfg file and populates the corresponding variables
	 * param The path of the PeerInfo.cfg file
	 */
	public void readPeerInfoConfig(String peerConfigPath, Map<Integer, Peer> peerList, ArrayList<Integer> peerIdList) throws Exception
	{
		try{
			
			in = new FileInputStream(peerConfigPath);
			Scanner scanner = new Scanner(in);

			int tokenNum = 0;
			int lineNum = 0;
			int modResult;
            
            Peer peer = null;
			while(scanner.hasNext()) {

				++tokenNum;
				lineNum = tokenNum / 4;

				modResult = tokenNum % 4;

				if (modResult == 0) // token indicates whether the peer has the whole file
				{
					int wholeFile;
					++totalPeers;

					if (scanner.hasNextInt()) {
						wholeFile = scanner.nextInt();
						if (wholeFile == 1) {
							//hasWholeFile.add(true);
							peer.setCompleteFile(true);
							totalPeersWithEntireFile++;
						} else if (wholeFile == 0) {
							//hasWholeFile.add(false);
							peer.setCompleteFile(false);
						}
						peerList.put(peer.getId(), peer);
						peer = null;
					} else
						throw new Exception("Error.  You must specify either a 0 or 1 to denote if the peer has the entire file.");
				} else if (modResult == 1) // token is the peer ID found in the current line of the file
				{
					if (scanner.hasNextInt()) {
						lineNum = (tokenNum - 1) / 4;
						peer = new Peer(totalPieces);
						peer.setInterested(false);
						int peerId = scanner.nextInt();
						peer.setId(peerId);
						peerIdList.add(peerId);
					} else
						throw new Exception("Error.  You must specify the peer id as an integer.");
				} else if (modResult == 2) // token is the host name/IP address found in the current line of the file
				{
					lineNum = (tokenNum - 1) / 4;
					peer.setHostname(scanner.next());
				} else if (modResult == 3) // token is the port number found in the current line of the file
				{

					if (scanner.hasNextInt()) {
						int portNum = scanner.nextInt();

						if (portNum < 0 || portNum > 65535) {
							throw new Exception("Error.  Invalid port specified in the PeerInfo configuration file.");
						}

						peer.setPort(portNum);
					} else
						throw new Exception("Error.  You must specify the port # as an integer.");
				}
			}
			if(in != null)
				in.close();
		}
		catch(IOException exception)
		{
			exception.printStackTrace();
		}
		
	}
}
