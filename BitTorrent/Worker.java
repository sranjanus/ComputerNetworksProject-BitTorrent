
/********************************************************************************
 * File: Worker.java
 * ******************************************************************************
 * Purpose: The Worker class is the Thread existing between
 * each Active Connection
 * Description: The peerProcess creates one Thread per Active Connection and Worker.java
 * is the Thread class.
 *
 * Class: Computer Networks, Spring 2016 (CNT5106C)
 *
 * Authors: Sarath Francis, Shashank Ranjan, Varun Kumar
 *
 *******************************************************************************/

import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.Thread;
import java.lang.Exception;

public class Worker extends Thread{
    public Socket _socket;
    public int _selfId;
    public int _peerId;
    InputStream _msgReader;
    OutputStream _msgWriter;
    peerConfig _configInfo;
    Map<Integer, Peer> _peerList;
    ArrayList<Integer> _peerIdList;
    DataFileHandler _fileHandler;
    ArrayList<Integer> _newlyAddedPieces;
    loggerFile _logFile;
    boolean _prevChokedStatus;
    boolean _isFirstTimeCheck;

    public Worker(Socket socket, peerConfig configInfo, int selfId, int peerId, Map<Integer, Peer> peerList, ArrayList<Integer> peerIdList, DataFileHandler fileHandler, loggerFile logFile){
        _socket = socket;
        _selfId = selfId;
        _peerId = peerId;
        _configInfo = configInfo;
        _peerList = peerList;
        _peerIdList = peerIdList;
        _fileHandler = fileHandler;
        _newlyAddedPieces = new ArrayList<Integer>();
        for(int i = 0 ; i < _configInfo.getTotalPieces() ; i ++) {
            _newlyAddedPieces.add(0);
        }
        _logFile =logFile;
        _prevChokedStatus = true;
        _isFirstTimeCheck = true;
    }

    public Worker(Socket socket, peerConfig configInfo, int selfId, Map<Integer, Peer> peerList, ArrayList<Integer> peerIdList, DataFileHandler fileHandler, loggerFile logFile){
        _socket = socket;
        _selfId = selfId;
        _peerId = -1;
        _configInfo = configInfo;
        _peerList = peerList;
        _peerIdList = peerIdList;
        _fileHandler = fileHandler;
        _newlyAddedPieces = new ArrayList<Integer>();
        for(int i = 0 ; i < _configInfo.getTotalPieces() ; i ++) {
            _newlyAddedPieces.add(0);
        }
        _logFile = logFile;
        _prevChokedStatus = true;
        _isFirstTimeCheck = true;
    }

    public void setPeerId(int peerId){
        _peerId = peerId;
    }

    /**
     * The Thread run method which will run as long as the Thread is Active
     */
    @Override
    public void run(){
        // initialize the input and output stream
        try{
            _msgReader = _socket.getInputStream();
            _msgWriter = _socket.getOutputStream();
        }catch(IOException e){
            System.out.println("Error: Intialization of data stream failed! Message : " + e.getMessage());
            _peerList.get(_peerId).setExited(true);
            System.out.println("Info: Exiting the thread between " + _selfId + " and " + _peerId);
            return;
        }

        /**
         *  if you have made the connection, send handshake message to your peer and wait for acknowledgement
         */

        handShakemsg hsMsg = new handShakemsg();
        if(_peerId != -1){
            hsMsg.setPeerID(_selfId);
            hsMsg.sendMessage(_socket);
            System.out.println("Info: Peer " +  _selfId + " sent handshake message to " + _peerId);

            try{
                while(_msgReader.available() == 0){
                    Thread.sleep(5);
                }
            } catch(Exception e){
                System.out.println("Error: Waiting for msgReader. Message : " + e.getMessage());
                _peerList.get(_peerId).setExited(true);
                System.out.println("Info: Exiting the thread between " + _selfId + " and " + _peerId);
                return;
            }

            hsMsg.receiveMessage(_socket);
            _peerId = hsMsg.getPeerID();

            System.out.println("Info: Peer " +  _selfId + " received handshake ack message from " + _peerId);

            /**
             * wait for handshake message from peer and send acknowledgment
             */

        } else {
            try{
                while(_msgReader.available() == 0){
                    Thread.sleep(5);
                }
            } catch(Exception e){
                System.out.println("Error: Waiting for msgReader. Message : " + e.getMessage());
                System.out.println("Info: Exiting the thread between " + _selfId + " and " + _peerId);
                return;
            }
            hsMsg.receiveMessage(_socket);
            _peerId = hsMsg.getPeerID();
            _logFile.connectedTo(_peerId);
            System.out.println("Info: Peer " +  _selfId + " received handshake message from " + _peerId);

            hsMsg.setPeerID(_selfId);
            hsMsg.sendMessage(_socket);

            System.out.println("Info: Peer " +  _selfId + " sent handshake ack message to " + _peerId);
        }

        /**
         *  check if we need to send bitfield message to our peer and send bitfield message
         */

        if(_peerList.get(_selfId).hasCompleteFile()){
            Message sendBitfieldMessage = new Message();
            sendBitfieldMessage.setType(Message.bitfield);
            byte[] bfPayload = _peerList.get(_selfId).getBitfield().changeBitToByteField();
            int bfLength = _peerList.get(_selfId).getBitfield().getLengthInBytes();
            sendBitfieldMessage.setPayload(bfPayload);
            sendBitfieldMessage.setLength(bfLength);
            try{
                sendBitfieldMessage.sendMessage(_msgWriter);
                System.out.println("Info: Peer " +  _selfId + " sent bitfield message to " + _peerId);
            } catch(Exception e){
                System.out.println("Error: Peer " +  _selfId + " not able to send bitfield message to " + _peerId + ". Message: " + e.getMessage());
                _peerList.get(_peerId).setExited(true);
                System.out.println("Info: Exiting the thread between " + _selfId + " and " + _peerId);
                return;
            }
            for(int i = 0;i < _configInfo.getTotalPieces();i++){
                _newlyAddedPieces.set(i, 1);
            }
        }
        /**
         *  check if we are expecting bitfield message from our peer and wait for the message and then set bitfield structure for that peer
         */
        if(_peerList.get(_peerId).getBitfield().getCountFinishedPieces() > 0){
            try{
                while(_msgReader.available() == 0){
                    Thread.sleep(5);
                }
            }catch(Exception e){
                System.out.println("Error: Waiting for msgReader. Message : " + e.getMessage());
                _peerList.get(_peerId).setExited(true);
                System.out.println("Info: Exiting the thread between " + _selfId + " and " + _peerId);
                return;
            }

            Message receiveBitfieldMessage = new Message();
            try{
                receiveBitfieldMessage.receiveMessage(_msgReader);
                System.out.println("Info: Peer " +  _selfId + " received bitfield message from " + _peerId);
                byte[] bfPayload = receiveBitfieldMessage.getPayload();
                _peerList.get(_peerId).getBitfield().setBitFromByte(bfPayload);

                /**
                 * If there is any interesting pieces, send interested  message else send not interested message
                 */

                if(_peerList.get(_selfId).getBitfield().checkPiecesInterested(_peerList.get(_peerId).getBitfield())) {
                    Message sendinterestedMessage = new Message();
                    sendinterestedMessage.setType((Message.interested));
                    try {
                        sendinterestedMessage.sendMessage(_msgWriter);
                        System.out.println("Info: Peer " +  _selfId + " sent Interested message to " + _peerId);
                    }catch (Exception e) {
                        System.out.println("Error: Peer " +  _selfId + " is not able to send Interested message to " + _peerId + ". Message: " + e.getMessage());
                        _peerList.get(_peerId).setExited(true);
                        System.out.println("Info: Exiting the thread between " + _selfId + " and " + _peerId);
                        return;

                    }
                } else {
                    Message sendNotInterestedMessage = new Message();
                    sendNotInterestedMessage.setType(Message.notInterested);
                    //TODO: length and payload for the interested message
                    try {
                        sendNotInterestedMessage.sendMessage(_msgWriter);
                        System.out.println("Info: Peer " +  _selfId + " sent Not Interested message to " + _peerId);
                    }catch (Exception e) {
                        System.out.println("Error: Peer " +  _selfId + " is not able to send Not Interested message to " + _peerId + ". Message: " + e.getMessage());
                        _peerList.get(_peerId).setExited(true);
                        System.out.println("Info: Exiting the thread between " + _selfId + " and " + _peerId);
                        return;
                    }
                }

            }catch(Exception e){
                System.out.println("Error: Peer " +  _selfId + " not able to receive bitfield message from " + _peerId + ". Message: " + e.getMessage());
                _peerList.get(_peerId).setExited(true);
                System.out.println("Info: Exiting the thread between " + _selfId + " and " + _peerId);
                return;
            }
        }
        /**
         * The following While needs to be executed until all the pieces are transferred both the peers have the complete file
         */
        while(!_peerList.get(_selfId).hasCompleteFile() || !_peerList.get(_peerId).hasCompleteFile()){
            /**
             * Check if 'is choked' property has changed and based on that send 'choke' or 'unchoke' message to the peer.
             */

            if((_isFirstTimeCheck) || (_prevChokedStatus != _peerList.get(_peerId).isChoked())){
                if(_peerList.get(_peerId).isChoked()) {
                    Message sendChokeMessage = new Message();
                    sendChokeMessage.setType(Message.choke);
                    
                    try {
                        sendChokeMessage.sendMessage(_msgWriter);
                        System.out.println("Info: Peer " +  _selfId + " sent Choke message to " + _peerId);
                        
                        
                    }catch (Exception e) {
                        System.out.println("Error: Peer " +  _selfId + " is not able to send Choke message to " + _peerId + ". Message: " + e.getMessage());
                        _peerList.get(_peerId).setExited(true);
                        System.out.println("Info: Exiting the thread between " + _selfId + " and " + _peerId);
                        return;
                    }
                } else {
                    Message sendUnchokeMessage = new Message();
                    sendUnchokeMessage.setType(Message.unchoke);
                    try {
                        sendUnchokeMessage.sendMessage(_msgWriter);
                        System.out.println("Info: Peer " +  _selfId + " sent Unchoke message to " + _peerId);
                    }catch (Exception e) {
                        System.out.println("Error: Peer " +  _selfId + " is not able to send Unchoke message to " + _peerId + ". Message: " + e.getMessage());
                        _peerList.get(_peerId).setExited(true);
                        System.out.println("Info: Exiting the thread between " + _selfId + " and " + _peerId);
                        return;
                    }
                }
                _prevChokedStatus = _peerList.get(_peerId).isChoked();
                _isFirstTimeCheck = false;
            }
            

            Message receivedMessage = new Message();
            try{

                if(_msgReader.available() != 0){
                    try {
                        /**
                         * Receive the message and then based on the message type different actions are taken.
                         */
                        receivedMessage.receiveMessage(_msgReader);
                        byte receivedMessageType = receivedMessage.getType();
                        
                        /**
                         * If an interested Message is received, Send an unchoke to begin the transfer :
                         * TODO: This is just for debugging . Actually, compute preferred neighnours and then send Unchoke
                         */
                        if(receivedMessageType == Message.interested) {
                            System.out.println("Info: Peer " + _selfId + " received Interested message from " + _peerId);
                            _peerList.get(_peerId).setInterested(true);
                            _logFile.interestedLog(_peerId);
                            
                        }
                        
                        /**
                         * If a not interested message is received.. update the Is interested flag for that peer and check if 'Self' need any
                         * piece from that peer and send request message.
                         */
                        else if (receivedMessageType == Message.notInterested) {
                            System.out.println("Info: Peer " + _selfId + " received Not Interested message from " + _peerId);
                            _peerList.get(_peerId).setInterested(false);
                            _logFile.notInterestedLog(_peerId);
                        }
                        
                        /**
                         * If a an Unchoke message is received, a 'request' message has to be send requesting for a random peice
                         * which is present in that peer and not in self.
                         */
                        else if (receivedMessageType == Message.unchoke) {
                            _logFile.unchokeLog(_peerId);
                            System.out.println("Info: Peer " + _selfId + " received Unchoke message from " + _peerId);
                                int interestedPieceIndex = _peerList.get(_selfId).getBitfield().setInterestedPiece(_peerList.get(_peerId).getBitfield());
                                if(interestedPieceIndex != -1){
                                    Message sendRequestMessage = new Message();
                                    sendRequestMessage.setType(Message.request);
                                    byte[] payload = bytetoInt.intToByteArray(interestedPieceIndex);
                                    sendRequestMessage.setPayload(payload);
                                    sendRequestMessage.setLength(payload.length);
                            
                                    try {
                                        sendRequestMessage.sendMessage(_msgWriter);
                                        System.out.println("Info: Peer " +  _selfId + " send request message to " + _peerId);
                                    } catch (Exception e) {
                                        System.out.println("Error: Peer " + _selfId + " is not able to send request message to " + _peerId + ". Message: " + e.getMessage());
                                        _peerList.get(_peerId).setExited(true);
                                        System.out.println("Info: Exiting the thread between " + _selfId + " and " + _peerId);
                                        return;
                                    }
                                } else {
                                        System.out.println("Error: Interested Piece Index out of bound (-1).");
                                }
                            
                        }
                        /**
                         * If the peer receives a choked message, Update the log and wait for an Unchoke
                         */
                        else if (receivedMessageType == Message.choke) {
                            _logFile.chokeLog(_peerId);
                            System.out.println("Info: Peer " + _selfId + " received Choke message from " + _peerId);
                        }
                        
                        /**
                         * If a request message is received with a piece index, the corresponding message needs to be send to the peer in a 'piece' message
                         */
                        else if(receivedMessageType == Message.request) {
                            System.out.println("Info: Peer " + _selfId + " received request message from " + _peerId);
                            if(_peerList.get(_peerId).isChoked() == false){
                                byte []requestMessagePayload = receivedMessage.getPayload();
                                int requestedpieceIndex = bytetoInt.byteArrayToInt(requestMessagePayload);
                                Piece piece = _fileHandler.readFile(requestedpieceIndex);
                                Message sendPieceMessage = new Message();
                                sendPieceMessage.setType(Message.piece);
                                /**
                                 * Converting Object to Byte array - logic taken from stackoverflow
                                */
                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                ObjectOutput out = null;
                                try {
                                    out = new ObjectOutputStream(bos);
                                    out.writeObject(piece);
                                    byte[] pieceMessagePayload = bos.toByteArray();
                                    sendPieceMessage.setPayload(pieceMessagePayload);
                                    sendPieceMessage.setLength(pieceMessagePayload.length);
                                } finally {
                                    try {
                                        if (out != null) {
                                            out.close();
                                        }
                                    } catch (IOException ex) {
                                        System.out.println("Error: IO Exception in sending request by " + _selfId + "to " + _peerId+ ". Message: " + ex.getMessage());
                                        _peerList.get(_peerId).setExited(true);
                                        System.out.println("Info: Exiting the thread between " + _selfId + " and " + _peerId);
                                        return;
                                    }
                                    try {
                                        bos.close();
                                    } catch (IOException ex) {
                                        System.out.println("Error: IO Exception in sending request by " + _selfId + "to " + _peerId+ ". Message: " + ex.getMessage());
                                        _peerList.get(_peerId).setExited(true);
                                        System.out.println("Info: Exiting the thread between " + _selfId + " and " + _peerId);
                                        return;
                                    }
                                }
        
                                try {
                                    sendPieceMessage.sendMessage(_msgWriter);
                                    System.out.println("Info: Peer " +  _selfId + " sent piece message of index " +  requestedpieceIndex + " to " + _peerId);
        
                                } catch (Exception e) {
                                    System.out.println("Error: Peer " + _selfId + " is not able to send piece message to " + _peerId + ". Message: " + e.getMessage());
                                    _peerList.get(_peerId).setExited(true);
                                    System.out.println("Info: Exiting the thread between " + _selfId + " and " + _peerId);
                                    return;
                                }
                            }
                        }
                        /**
                         * If a 'piece' message is recieved, get the piece from the the message and write it to the file and send 'have' messages to all the peers.
                         */
                        
                        else if(receivedMessageType == Message.piece) {
                            System.out.println("Info: Peer " + _selfId + " received piece message from " + _peerId);
                            byte []pieceMessagePayload = receivedMessage.getPayload();
                            Piece piece = null;
                            /**
                             * Covert byte array to object - logic taken from Stack overflow
                             */
                            ByteArrayInputStream bis = new ByteArrayInputStream(pieceMessagePayload);
                            ObjectInput in = null;
        
                            try {
                                in = new ObjectInputStream(bis);
                                Object o = in.readObject();
                                piece = (Piece)o;
                            } finally {
                                try {
                                    bis.close();
                                } catch (IOException ex) {
                                    // ignore close exception
                                    System.out.println("Error: IO Exception in writing piece to file. Message: " + ex.getMessage());
                                    _peerList.get(_peerId).setExited(true);
                                    System.out.println("Info: Exiting the thread between " + _selfId + " and " + _peerId);
                                    return;
                                }
                                try {
                                    if (in != null) {
                                        in.close();
                                    }
                                } catch (IOException ex) {
                                    // ignore close exception
                                    System.out.println("Error: IO Exception in writing piece to file. Message: " + ex.getMessage());
                                    _peerList.get(_peerId).setExited(true);
                                    System.out.println("Info: Exiting the thread between " + _selfId + " and " + _peerId);
                                    return;
                                }
                            }
                            _fileHandler.writeFile(piece);
        
                            /**
                             * Increment the speed value of the peer who just received a piece
                             */
                            _peerList.get(_peerId).incrementSpeed();
                            _peerList.get(_selfId).getBitfield().setBitToTrue(piece.getPieceNum());
                            _logFile.downloadingLog(_peerId,piece.getPieceNum(),_peerList.get(_selfId).getBitfield().getCountFinishedPieces());
                            if(_peerList.get(_selfId).hasCompleteFile()){
                                _logFile.completeDownloadLog();
                            }
        
                            int interestedPieceIndex = _peerList.get(_selfId).getBitfield().setInterestedPiece(_peerList.get(_peerId).getBitfield());
                            if(interestedPieceIndex != -1){
                                Message sendRequestMessage = new Message();
                                sendRequestMessage.setType(Message.request);
                                byte[] payload = bytetoInt.intToByteArray(interestedPieceIndex);
                                sendRequestMessage.setPayload(payload);
                                sendRequestMessage.setLength(payload.length);
        
                                try {
                                    sendRequestMessage.sendMessage(_msgWriter);
                                    System.out.println("Info: Peer " +  _selfId + " sent request message to " + _peerId);
        
                                } catch (Exception e) {
                                    System.out.println("Error: Peer " + _selfId + " is not able to send request message to " + _peerId + ". Message: " + e.getMessage());
                                    _peerList.get(_peerId).setExited(true);
                                    System.out.println("Info: Exiting the thread between " + _selfId + " and " + _peerId);
                                    return;
                                }
                            } else {
                                //System.out.println("Error: Interested Piece Index out of bound (-1).");
                            }
                        }
                        
                        /**
                         * If a have message is received, send Interested or not interested Message
                         */
                        else if(receivedMessageType == Message.have) {
                            System.out.println("Info: Peer " +  _selfId + " received have message from " + _peerId);
                            
                            byte []havePayload = receivedMessage.getPayload();
                            int index = bytetoInt.byteArrayToInt(havePayload);
                            _logFile.haveLog(_peerId,index);
        
                            _peerList.get(_peerId).getBitfield().setBitToTrue(index);
                            if(_peerList.get(_selfId).getBitfield().checkPiecesInterested(_peerList.get(_peerId).getBitfield())) {
                                /**
                                 * If there is any interesting pieces, send interested  message else send not interested message
                                 * TODO:Check if it is right : the if condition
                                 */
                                Message sendinterestedMessage = new Message();
                                sendinterestedMessage.setType((Message.interested));
                                //TODO: length and payload for the interested message
                                try {
                                    sendinterestedMessage.sendMessage(_msgWriter);
                                    System.out.println("Info: Peer " +  _selfId + " sent Interested message to " + _peerId);
                                }catch (Exception e) {
                                    System.out.println("Error: Peer " +  _selfId + " is not able to send Interested message to " + _peerId + ". Message: " + e.getMessage());
                                    _peerList.get(_peerId).setExited(true);
                                    System.out.println("Info: Exiting the thread between " + _selfId + " and " + _peerId);
                                    return;
        
                                }
                            } else {
                                Message sendNotInterestedMessage = new Message();
                                sendNotInterestedMessage.setType(Message.notInterested);
                                //TODO: length and payload for the interested message
                                try {
                                    sendNotInterestedMessage.sendMessage(_msgWriter);
                                    System.out.println("Info: Peer " +  _selfId + " sent Not Interested message to " + _peerId);
                                }catch (Exception e) {
                                    System.out.println("Error: Peer " +  _selfId + " is not able to send Not Interested message to " + _peerId + ". Message: " + e.getMessage());
                                    _peerList.get(_peerId).setExited(true);
                                    System.out.println("Info: Exiting the thread between " + _selfId + " and " + _peerId);
                                    return;
                                }
                            }
                        }
                    }catch (Exception e) {
                        System.out.println("Error: Peer " + _selfId + " not able to receive message from " + _peerId + ". Message: " + e.getMessage());
                        _peerList.get(_peerId).setExited(true);
                        System.out.println("Info: Exiting the thread between " + _selfId + " and " + _peerId);
                        return;
                    }
                }
                
                /**
                 * At the end of each Iteration of the loop, each peer finds the newly added pieces and send have messages to the other threads.
                 * Original have message is being send to only one peer which actually send the piece to self.
                 */
                
                for(int i = 0 ; i < _configInfo.getTotalPieces(); i++) {
                    if(_newlyAddedPieces.get(i) == 0 &&_peerList.get(_selfId).getBitfield().getBit(i) == 1) {
                        Message sendHaveMessageToOtherPeers = new Message();
                        sendHaveMessageToOtherPeers.setType(Message.have);
                        byte[] havePayload = bytetoInt.intToByteArray(i);
                        sendHaveMessageToOtherPeers.setPayload(havePayload);
                        sendHaveMessageToOtherPeers.setLength(havePayload.length);
                        try {
                            sendHaveMessageToOtherPeers.sendMessage(_msgWriter);
                            System.out.println("Info: Peer " +  _selfId + " sent have message to " + _peerId + "for index = " + i);
                            
                        } catch (Exception e) {
                            System.out.println("Error: Peer " + _selfId + " is not able to send have message to " + _peerId + ". Message: " + e.getMessage());
                            _peerList.get(_peerId).setExited(true);
                            System.out.println("Info: Exiting the thread between " + _selfId + " and " + _peerId);
                            return;
                        }
                        _newlyAddedPieces.set(i, 1);
                    }//end if
                }//end for
            }catch(Exception e1){
                System.out.println("Error: Checking msgReader for incoming message. Message : " + e1.getMessage());
                _peerList.get(_peerId).setExited(true);
                System.out.println("Info: Exiting the thread between " + _selfId + " and " + _peerId);
                return;
            }
        }//end While
        _peerList.get(_peerId).setExited(true);
        System.out.println("End of the thread between " + _selfId + " and " + _peerId);
    }
}