
/********************************************************************************
 * File: handShakemsg.java
 * ******************************************************************************
 * Purpose: This class is used to create, send and receive handshake messages through sockets
 *
 * Description: The functions are responsible for sending and receiving handshakes
 * between the peers for establishing TCP connections.
 *
 *
 * Class: Computer Networks, Spring 2016 (CNT5106C)
 *
 * Authors: Sarath Francis, Shashank Ranjan, Varun Kumar
 *
 *******************************************************************************/

import java.io.*;
import java.net.Socket;

public class handShakemsg {
	
	private String header = "P2PFILESHARINGPROJ";
	private final byte[] zerobits = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	private int peerID;
	
	
	public String getHandshakeHeader(){
		return header;
	}
	
	public byte[] getZerobits(){
		return zerobits;
	}
	
	public void setPeerID(int id){
		peerID = id;
	}
	
	public int getPeerID(){
		return peerID;
	}
	 /**
	  * This function is used to send the handshake message through the socket
	  * param Socket of the peer to which the handshake message is sent
	  */
	 public void sendMessage(Socket socket){
		 try{
			OutputStream os = socket.getOutputStream();
			os.write(header.getBytes());
			os.write(zerobits);
			os.write(bytetoInt.intToByteArray(peerID));
		} 
		  catch (IOException e) {
			e.printStackTrace();
		}
	 }
	 /**
	  * This function is used to receive handshake message from the peer through the socket
	  * param Socket of the peer from which the handshake message is received
	  */
	 public void receiveMessage(Socket socket){
		 try{
			 InputStream is = socket.getInputStream();
             byte[] newHeader = new byte[18];
             byte[] newID = new byte[4];
             is.read(newHeader);
             header = new String(newHeader, "UTF-8");
             is.read(zerobits);
             is.read(newID);
             peerID = bytetoInt.byteArrayToInt(newID);
             //System.out.println("Connected with " + peerID);
		 }
		  catch (Exception e) {
				e.printStackTrace();
			}
	 }

}
