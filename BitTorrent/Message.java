
/********************************************************************************
 * File: Datafilehandler.java
 * ******************************************************************************
 * Purpose: This class creates, send and receives messages through different streams
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
import java.net.*;

public class Message 
{
  private byte messageType;  // parse byte & get message type
  private int messageLength;  // length of message in bytes
  private byte [] messagePayload;
  
  public static final byte choke = 0;
  public static final byte unchoke = 1;
  public static final byte interested = 2;
  public static final byte notInterested = 3;
  public static final byte have = 4;
  public static final byte bitfield = 5;
  public static final byte request = 6;
  public static final byte piece = 7;
  
  public synchronized int getLength()
  {
    return messageLength;
  }
  
  public synchronized byte getType()
  {
    return messageType;
  }
  
  public synchronized byte [] getPayload()
  {
    return messagePayload;
  }
  
  public synchronized void setLength(int length)
  {
    this.messageLength = length;
  }
  
  public synchronized void setType(byte type)
  {
    this.messageType = type;
  }
  
  
  public synchronized void setPayload(byte[] payload)
  {
	  this.messagePayload = payload;
  }
  
  /**
   *  This function is used to send messages through output stream
   * The format is as given below:
   * First 4 bytes length of the payload, next 1 byte message type and then the rest is payload
   */
  public synchronized void sendMessage(OutputStream output) throws Exception
  {
    
	  try{
	  if(messagePayload == null) 
		  messageLength = 1;
	  else
		  messageLength = messagePayload.length + 1;
	  output.write(bytetoInt.intToByteArray(messageLength));
	  output.write(messageType);
	  if(messagePayload != null)
		  output.write(messagePayload);
	  output.flush();
	  }
	  catch(Exception e){
		  throw new Exception();
	  }
	  
  }
  /**
   * This function is used to receive messages through the input stream
   * The format is as given below:
   * First 4 bytes length of the payload, next 1 byte message type and then the rest is payload
   */
  public synchronized void receiveMessage(InputStream input) throws Exception
  {
	  try{
		  byte[] receivedLength = new byte[4];
		  byte[] receivedType = new byte[1];
		  int index;
		  int counter = 0;
		//Receive message length
		  while(counter < 4){
			  index = input.read(receivedLength, counter, 4 - counter);
			  counter += index;
		  }
		  messageLength = bytetoInt.byteArrayToInt(receivedLength);
		  counter = 0;
		//Receive message type
		  while(counter < 1){
			  index = input.read(receivedType, counter, 1 - counter);
			  counter += index;
		  }
		  messageType = receivedType[0];
		  if(messageLength > 1){
			  messagePayload = new byte[messageLength - 1];
		  }
		  else {
			  messagePayload = null;
		  }
		  counter = 0;
		//Receive message payload
		  while(counter < (messageLength - 1)){
			  index = input.read(messagePayload, counter, messageLength - 1 - counter);
			  counter += index;
		  }
	  }
	  catch(Exception e){
		  throw new Exception();
	  }
	  
  }
  
}