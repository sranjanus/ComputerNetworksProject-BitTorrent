import java.io.Serializable;

/*
 * @author Sarath
 * This is the Piece class that has the information for each piece/chunk of the file
 */
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