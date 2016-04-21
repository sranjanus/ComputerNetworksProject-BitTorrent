
/********************************************************************************
 * File: LoggerFile.java
 * ******************************************************************************
 * Purpose: Create a log file for the corresponding peer and create log for all
 * important actions.
 *
 * Description: Each method in this file corresponds one of the actions.
 * and write the corresponding log into the log file.
 *
 * Class: Computer Networks, Spring 2016 (CNT5106C)
 *
 * Authors: Sarath Francis, Shashank Ranjan, Varun Kumar
 *
 ******************************************************************************/

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class loggerFile {

    private Timestamp logTime;
    int selfID;
    private File file;
    private FileWriter writer;
    private BufferedWriter buffer;
    private boolean downloadComplete = false;
    private Vector<Integer> noOfPiecesWritten;

    /**
     *
     * creates the log file for the peer
     */
    public loggerFile(int selfId) throws IOException {
        this.selfID = selfId;
        noOfPiecesWritten = new Vector<Integer>();
        Date date = new Date();
        this.logTime = new Timestamp(date.getTime());

        file = new File(System.getProperty("user.dir") + "/log_peer_" + selfId + ".log");
        if (file.exists()) file.delete();
        file.createNewFile();

        try {
            writer = new FileWriter(file.getAbsoluteFile());
            buffer = new BufferedWriter(writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * This function writes the log messages into the log file
     */
    public synchronized void writeToFile(String content) {
        try {
            buffer.write(content);
            buffer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * This function closes all the buffer and file writer objects
     */
    public synchronized void close() {
        try {
            buffer.close();
            writer.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    /**
     * This function returns the current date/time
     */
    public synchronized String getTime() {
        StringBuilder line = new StringBuilder();
        DateFormat timeformat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date cur = new Date();
        line.append("[");
        line.append(timeformat.format(cur));
        line.append("] ");
        return line.toString();
    }

    public synchronized void makesTCPConnection(int peer_2) {
        String str = getTime() + ": Peer " + selfID + " makes a connection to Peer " + peer_2 + ".\n";
        writeToFile(str);
    }

    public synchronized void connectedTo(int peer_2) {
        String str = getTime() + ": Peer " + selfID + " is connected to Peer " + peer_2 + ".\n";
        writeToFile(str);
    }

    public synchronized void changeOfPreferredNeighbourLog(ArrayList<Integer> prefList) {
        String peersArr = "";
        for (int peer : prefList) {
            if (peersArr == "")
                peersArr = peersArr + peer;
            else
                peersArr = peersArr + ", " + peer;
        }
        String str = getTime() + ": Peer " + selfID + " has the preferred neighbours " + peersArr + ".\n";
        writeToFile(str);
    }

    public synchronized void changeOfOptUnchokedNeighbourLog(int peer_2) {
        String str = getTime() + ": Peer " + selfID + " has the optimistically unchoked neighbour " + peer_2 + ".\n";
        writeToFile(str);
    }

    public synchronized void unchokeLog(int peer_2) {
        String str = getTime() + ": Peer " + selfID + " is unchoked by the " + peer_2 + ".\n";
        writeToFile(str);
    }

    public synchronized void chokeLog(int peer_2) {
        String str = getTime() + ": Peer " + selfID + " is choked by " + peer_2 + ".\n";
        writeToFile(str);
    }

    public synchronized void haveLog(int peer_2, int pieceIndex) {
        String str = getTime() + ": Peer " + selfID + " received the 'have' message from " + peer_2 + " for the piece " + pieceIndex + ".\n";
        writeToFile(str);
    }

    public synchronized void interestedLog(int peer_2) {
        String str = getTime() + ": Peer " + selfID + " received the 'interested' message from " + peer_2 + ".\n";
        writeToFile(str);
    }

    public synchronized void notInterestedLog(int peer_2) {
        String str = getTime() + ": Peer " + selfID + " received the 'not interested' message from " + peer_2 + ".\n";
        writeToFile(str);
    }

    public synchronized void downloadingLog(int peer_2, int pieceIndex, int numPieces) {
        if (!noOfPiecesWritten.contains(numPieces)) {
            noOfPiecesWritten.add(numPieces);
            String str = getTime() + ": Peer " + selfID + " has downloaded the piece " + pieceIndex + " from " + peer_2 + "." + "\nNow the number of pieces it has is " + numPieces + ".\n";
            writeToFile(str);
        }
    }

    public synchronized void completeDownloadLog() {

        if (!downloadComplete) {
            String str = getTime() + ": Peer " + selfID + " has downloaded the complete file.\n";
            writeToFile(str);
            downloadComplete = true;
        }
    }

}