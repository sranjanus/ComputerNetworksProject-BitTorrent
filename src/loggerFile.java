import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

/*
 * @author Varun
 * A customized logger for the BitTorrrent client project to log entries according to the operations
 */
public class loggerFile
{

	private Timestamp logTime; 
	int peerId;
	private File file;
	private FileWriter writer;
	private BufferedWriter buffer;
	private boolean downloadComplete = false;
	private Vector<Integer> noOfPiecesWritten;
	
	public loggerFile(int peerId) throws IOException
	{
		this.peerId = peerId;
		noOfPiecesWritten = new Vector<Integer>();
		Date date = new Date();
		this.logTime = new Timestamp(date.getTime());
		
		file = new File(System.getProperty("user.home") + "/project/log_peer_"+ peerId +".log");
		if(file.exists()) file.delete();
		file.createNewFile();
		
		try 
		{
			writer = new FileWriter(file.getAbsoluteFile());
			buffer = new BufferedWriter(writer);
		} 		
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		
	} 
	/*
	 * This function writes the log messages into the log file
	 * @param string content of the message
	 */
	public synchronized void writeToFile(String content)
	{
		try 
		{
			buffer.write(content);
			buffer.flush();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		
	}
	/*
	 * This function closes all the buffer and file writer objects
	 */
	public synchronized void close()
	{
		try 
		{
			buffer.close();
			writer.close();
		} 
		catch (IOException e) 
		{
		
			e.printStackTrace();
		} 
		
	} 
	/*
	 * This function returns the current date/time
	 */
	public synchronized String getTime()
	{
		Date date = new Date();
		this.logTime.setTime(date.getTime());
		return logTime.toString();
	} 
	
	public synchronized void tcpConnectionEstablishedLog(int peer_2)
	{
	String str = getTime() + ": Peer " + peerId + " makes a connection to Peer "+ peer_2 + ".\n";
	writeToFile(str);
	}
	
	public synchronized void tcpConnectedLog(int peer_2)
	{
		String str = getTime() + ": Peer " + peerId + " is connected from Peer "+ peer_2 + ".\n";
		writeToFile(str);
	}
	
	public synchronized void changeOfPreferredNeighbourLog(Vector<Integer> prefList)
	{
		String peersArr = "";
		for(int peer : prefList){
			if(peersArr == "")
				peersArr = peersArr + peer;
			else
				peersArr = peersArr + ", " + peer;
		}
		String str = getTime() + ": Peer " + peerId + " has the preferred neighbours "+ peersArr + ".\n";
		writeToFile(str);
	}
	
	public synchronized void changeOfOptUnchokedNeighbourLog(int peer_2)
	{
		String str = getTime() + ": Peer " + peerId + " has the optimistically unchoked neighbour "+ peer_2 + ".\n";
		writeToFile(str);
	}
	
	public synchronized void unchokeLog(int peer_2)
	{
		String str = getTime() + ": Peer " + peerId + " is unchoked by the "+ peer_2 + ".\n";
		writeToFile(str);
	}
	
	public synchronized void chokeLog(int peer_2)
	{
		String str = getTime() + ": Peer " + peerId + " is choked by "+ peer_2 + ".\n";
		writeToFile(str);
	}
	
	public synchronized void haveLog(int peer_2, int pieceIndex)
	{
		String str = getTime() + ": Peer " + peerId + " received the 'have' message from "+ peer_2 +  " for the piece " + pieceIndex + ".\n";
		writeToFile(str);
	}
	
	public synchronized void interestedLog(int peer_2)
	{
		String str = getTime() + ": Peer " + peerId + " received the 'interested' message from "+ peer_2 + ".\n";
		writeToFile(str);
	}
	
	public synchronized void notInterestedLog(int peer_2)
	{
		String str = getTime() + ": Peer " + peerId + " received the 'not interested' message from "+ peer_2 + ".\n";
		writeToFile(str);
	}
	
	public synchronized void downloadingLog(int peer_2, int pieceIndex, int numPieces)
	{
		if(!noOfPiecesWritten.contains(numPieces)) {
			noOfPiecesWritten.add(numPieces);
			String str = getTime() + ": Peer " + peerId + " has downloaded the piece " + pieceIndex + " from " + peer_2 + "." + "\nNow the number of pieces it has is " + numPieces + ".\n";
			writeToFile(str);
		}
	}
	
	public synchronized void completeDownloadLog()
	{
		
		if(!downloadComplete) {
			String str = getTime() + ": Peer " + peerId + " has downloaded the complete file.\n";
			writeToFile(str);
			downloadComplete = true;
		}
	}
	
	}