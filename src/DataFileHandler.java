
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

/*
 * @author Shashank
 * @description: this class is responsible for file writing and file reading and fragmenting it into number of chunks 
 * depending on the chunk size.
 * 
 */

public class DataFileHandler{
	
	private RandomAccessFile filename;
	private peerConfig fileConfig;
	private Vector<Integer> noOfPiecesWritten;
	
	public DataFileHandler(int id, peerConfig fileConfig) throws FileNotFoundException {
		this.fileConfig = fileConfig;
		String path = System.getProperty("user.home") + "/project/peer_" + id + "/";
		File newFolder = new File(path);
		//Create new directory if not present
		if(!newFolder.exists()){
			newFolder.mkdir();
		}
		this.filename = new RandomAccessFile(path + fileConfig.getFileName(), "rw");
		noOfPiecesWritten = new Vector<Integer>();
	}
	/*
	 * This function returns the file pointer to close the file 
	 */
	public synchronized RandomAccessFile getFile(){
		return filename;
	} 
	/*
	 * This function writes a piece in a file
	 */
	public synchronized void writeFile(Piece p) throws IOException{
		if(!noOfPiecesWritten.contains(p.getPieceNum())){
			noOfPiecesWritten.add(p.getPieceNum());
			int size = fileConfig.getSizeOfPiece();
			int offset;
			byte[] temp = p.getPieceContent();
			int len;
			offset = size * p.getPieceNum();
			len = p.getPieceContent().length;
			getFile().seek(offset);
			int i = 0;
			while (i < len) {
				getFile().writeByte(temp[i]);
				i++;
			}
		}
	}
	/*
	 * This function reads a piece from a file for unchoke
	 */
	public synchronized Piece readFile(int id) throws IOException{
		int length;
		//Get the total length of the piece
		if(id == fileConfig.getTotalPieces() - 1)
			length = fileConfig.getSizeOfLastPiece();
		else
			length = fileConfig.getSizeOfPiece();
		int offset = fileConfig.getSizeOfPiece() * id;
		getFile().seek(offset); //Shifts the pointer to the desired location
		byte[] pieceContent = new byte[length];
		int i = 0;
		while(i < length){
			byte t = getFile().readByte();
			pieceContent[i] = t;
			i++;
		}
		Piece p = new Piece(pieceContent, id);
		return p;
	}
	/*
	 * This function reads a piece from a file for optUnchoke
	 */
		public synchronized Piece readFileForOpt(int id) throws IOException{
			int length;
			//Get the total length of the piece
			if(id == fileConfig.getTotalPieces() - 1)
				length = fileConfig.getSizeOfLastPiece();
			else
				length = fileConfig.getSizeOfPiece();
			int offset = fileConfig.getSizeOfPiece() * id;
			getFile().seek(offset); //Shifts the pointer to the desired location
			byte[] pieceContent = new byte[length];
			int i = 0;
			while(i < length){
				byte t = getFile().readByte();
				pieceContent[i] = t;
				i++;
			}
			Piece p = new Piece(pieceContent, id);
			return p;
		}
}
	 
	