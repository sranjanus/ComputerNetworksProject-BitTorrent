
/********************************************************************************
 * File: Piece.java
 * ******************************************************************************
 * Purpose: This class holds the information about each piece being sent or received.
 *
 * Description: Used for sending and receiving pieces between peers.
 *  It has a byte array of contents and and int number showing the piece index number

 *
 * Class: Computer Networks, Spring 2016 (CNT5106C)
 *
 * Authors: Sarath Francis, Shashank Ranjan, Varun Kumar
 *
 *******************************************************************************/


import java.io.Serializable;


public class Piece  implements Serializable {
	private final byte[] pieceContent; //Contains the bytes of the chunk
	private final int pieceNum; //Contains the chunk index
	
	public Piece(byte[] pieceContent, int pieceNum){
		this.pieceContent = pieceContent;
		this.pieceNum = pieceNum;
	}


	public byte[] getPieceContent(){
		return pieceContent;
	}
	public int getPieceNum(){
		return pieceNum;
	}
}