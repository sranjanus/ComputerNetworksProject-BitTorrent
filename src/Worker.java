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


    public Worker(Socket socket, peerConfig configInfo, int selfId, int peerId, Map<Integer, Peer> peerList, ArrayList<Integer> peerIdList, DataFileHandler fileHandler){
        _socket = socket;
        _selfId = selfId;
        _peerId = peerId;
        _configInfo = configInfo;
        _peerList = peerList;
        _peerIdList = peerIdList;
        _fileHandler = fileHandler;
    }

    public Worker(Socket socket, peerConfig configInfo, int selfId, Map<Integer, Peer> peerList, ArrayList<Integer> peerIdList, DataFileHandler fileHandler){
        _socket = socket;
        _selfId = selfId;
        _peerId = -1;
        _configInfo = configInfo;
        _peerList = peerList;
        _peerIdList = peerIdList;
        _fileHandler = fileHandler;
    }

    public void setPeerId(int peerId){
        _peerId = peerId;
    }

    @Override
    public void run(){
        // initialize the input and output stream
        try{
            _msgReader = _socket.getInputStream();
            _msgWriter = _socket.getOutputStream();
            //_fileHandler = new DataFileHandler(_selfId, _configInfo);
        }catch(IOException e){
            System.out.println("Error: Intialization of data stream failed! Message : " + e.getMessage());
        }

        // if you made the connection, send handshake message to your peer and wait for acknowledgement
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
            }

            hsMsg.receiveMessage(_socket);
            _peerId = hsMsg.getPeerID();

            System.out.println("Info: Peer " +  _selfId + " received handshake ack message from " + _peerId);

        } else { // wait for handshake message from peer and send acknowledgment
            try{
                while(_msgReader.available() == 0){
                    Thread.sleep(5);
                }
            } catch(Exception e){
                System.out.println("Error: Waiting for msgReader. Message : " + e.getMessage());
            }
            hsMsg.receiveMessage(_socket);
            _peerId = hsMsg.getPeerID();
            System.out.println("Info: Peer " +  _selfId + " received handshake message from " + _peerId);

            hsMsg.setPeerID(_selfId);
            hsMsg.sendMessage(_socket);

            System.out.println("Info: Peer " +  _selfId + " sent handshake ack message to " + _peerId);
        }

        // check if we need to send bitfield message to our peer and send bitfield message
        if(_peerList.get(_selfId).hasCompleteFile()){
            Message bfMsg = new Message();
            bfMsg.setType(Message.bitfield);
            byte[] bfPayload = _peerList.get(_selfId).getBitfield().changeBitToByteField();
            int bfLength = _peerList.get(_selfId).getBitfield().getLengthInBytes();
            bfMsg.setPayload(bfPayload);
            bfMsg.setLength(bfLength);
            try{
                bfMsg.sendMessage(_msgWriter);
                System.out.println("Info: Peer " +  _selfId + " sent bitfield message to " + _peerId);
            } catch(Exception e){
                System.out.println("Error: Peer " +  _selfId + " not able to send bitfield message to " + _peerId);
            }

        }

        // check if we are expecting bitfield message from our peer and wait for the message and then set bitfield structure for that peer
        if(_peerList.get(_peerId).getBitfield().getCountFinishedPieces() > 0){
            try{
                while(_msgReader.available() == 0){
                    Thread.sleep(5);
                }
            }catch(Exception e){
                System.out.println("Error: Waiting for msgReader. Message : " + e.getMessage());
            }


            Message rcvMsg = new Message();
            try{
                rcvMsg.receiveMessage(_msgReader);
                System.out.println("Info: Peer " +  _selfId + " received bitfield message from " + _peerId);
                byte[] bfPayload = rcvMsg.getPayload();
                _peerList.get(_peerId).getBitfield().setBitFromByte(bfPayload);

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
                        System.out.println("Info: Peer " +  _selfId + " send Interested message from " + _peerId);
                    }catch (Exception e) {
                        System.out.println("Info: Peer " +  _selfId + " is not able to send Interested message from " + _peerId);

                    }
                } else {
                    Message sendNotInterestedMessage = new Message();
                    sendNotInterestedMessage.setType(Message.notInterested);
                    //TODO: length and payload for the interested message
                    try {
                        sendNotInterestedMessage.sendMessage(_msgWriter);
                        System.out.println("Info: Peer " +  _selfId + " send Not Interested message from " + _peerId);
                    }catch (Exception e) {
                        System.out.println("Info: Peer " +  _selfId + " is not able to send Not Interested message from " + _peerId);

                    }
                }

            }catch(Exception e){
                System.out.println("Error: Peer " +  _selfId + " not able to receive bitfield message from " + _peerId);
            }
        }


        // -----------------------------------------------------------

        // do this in a loop until both have complete file.
        while(!_peerList.get(_selfId).hasCompleteFile() || !_peerList.get(_peerId).hasCompleteFile()){
            // set preferred neighbours
            /**
             * Check if 'is choked' property is set and based on that send 'choke' or 'unchoke' message to the peer.
             */

            System.out.println("Entered while loop of Thread between Self: " +_selfId + " and Peer: " +_peerId);
            try {
                Thread.sleep(100000);

            }catch (Exception e) {
                System.out.println("Did not stop at the Thread.Sleep");
            }
            if(_peerList.get(_peerId).isChoked()) {
                Message sendChokeMessage = new Message();
                sendChokeMessage.setType(Message.choke);

                try {
                    sendChokeMessage.sendMessage(_msgWriter);
                    System.out.println("Info: Peer " +  _selfId + " send Choke message from " + _peerId);


                }catch (Exception e) {
                    System.out.println("Info: Peer " +  _selfId + " is not able to send Choke message from " + _peerId);

                }
            } else {
                Message sendUnchokeMessage = new Message();
                sendUnchokeMessage.setType(Message.unchoke);

                try {
                    sendUnchokeMessage.sendMessage(_msgWriter);
                    System.out.println("Info: Peer " +  _selfId + " send Unchoke message from " + _peerId);


                }catch (Exception e) {
                    System.out.println("Info: Peer " +  _selfId + " is not able to send Unchoke message from " + _peerId);
                }
            }
//            try {
//                Thread.sleep(100000);
//                //TODO: for debugging
//
//            }catch (Exception e) {
//
//            }
            Message receivedMessage = new Message();
            try{
                while(_msgReader.available() == 0){
                    Thread.sleep(5);
                }
            }catch(Exception e){
                System.out.println("Error: Waiting for msgReader. Message : " + e.getMessage());
            }
            try {
                /**
                 * Receive the message and then based on the message type different actions are taken.
                 */
                receivedMessage.receiveMessage(_msgReader);
                byte receivedMessageType = receivedMessage.getType();

                /**
                 * If a an Unchoke message is received, a 'request' message has to be send requesting for a random peice
                 * which is present in that peer and not in self.
                 */
                if (receivedMessageType == Message.unchoke) {
                    System.out.println("Info: Peer " + _selfId + " received Unchoke message from " + _peerId);
                    int interestedPieceIndex = _peerList.get(_selfId).getBitfield().setInterestedPiece(_peerList.get(_peerId).getBitfield());
                    Message sendRequestMessage = new Message();
                    sendRequestMessage.setType(Message.request);
                    byte[] payload = bytetoInt.intToByteArray(interestedPieceIndex);
                    sendRequestMessage.setPayload(payload);
                    sendRequestMessage.setLength(payload.length);

                    try {
                        sendRequestMessage.sendMessage(_msgWriter);
                        System.out.println("Info: Peer " +  _selfId + " send request message from " + _peerId);

                    } catch (Exception e) {
                        System.out.println("Info: Peer " + _selfId + " is not able to send Unchoke message from " + _peerId);
                    }
                }

                /**
                 * If a request message is received with a piece index, the corresponding message needs to be send to the peer in a 'piece' message
                 */
                else if(receivedMessageType == Message.request) {
                    System.out.println("Info: Peer " + _selfId + " received request message from " + _peerId);
                    byte []requestMessagePayload = receivedMessage.getPayload();
                    int requestedpieceIndex = bytetoInt.byteArrayToInt(requestMessagePayload);
                    Piece piece = _fileHandler.readFile(requestedpieceIndex);
                    Message sendPieceMessage = new Message();
                    sendPieceMessage.setType(Message.piece);
                    byte[] pieceMessagePayload = ObjectToByteArrayandBack.serialize(piece);
                    sendPieceMessage.setPayload(pieceMessagePayload);
                    sendPieceMessage.setLength(pieceMessagePayload.length);
                    try {
                        sendPieceMessage.sendMessage(_msgWriter);
                        System.out.println("Info: Peer " +  _selfId + " send piece message from " + _peerId);

                    } catch (Exception e) {
                        System.out.println("Info: Peer " + _selfId + " is not able to send Piece message from " + _peerId);
                    }
                }
                /**
                 * If a 'piece' message is recieved, get the piece from the the message and write it to the file and send 'have' messages to all the peers.
                 */

                else if(receivedMessageType == Message.piece) {
                    System.out.println("Info: Peer " + _selfId + " received piece message from " + _peerId);
                    byte []pieceMessagePayload = receivedMessage.getPayload();
                    Piece piece = (Piece) ObjectToByteArrayandBack.deserialize(pieceMessagePayload);
                    _fileHandler.writeFile(piece);
                    Message sendHaveMessage = new Message();
                    sendHaveMessage.setType(Message.have);
                    byte[] havePayload = bytetoInt.intToByteArray(piece.getPieceNum());
                    sendHaveMessage.setPayload(havePayload);
                    sendHaveMessage.setLength(havePayload.length);
                    try {
                        sendHaveMessage.sendMessage(_msgWriter);
                        System.out.println("Info: Peer " +  _selfId + " send piece message from " + _peerId);

                    } catch (Exception e) {
                        System.out.println("Info: Peer " + _selfId + " is not able to send Piece message from " + _peerId);
                    }

                }

                /**
                 * If a have message is received, send Interested or not interested Message
                 */
                else if(receivedMessageType == Message.have) {

                    System.out.println("Info: Peer " +  _selfId + " received have message from " + _peerId);

                    byte []havePayload = receivedMessage.getPayload();
                    int index = bytetoInt.byteArrayToInt(havePayload);
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
                            System.out.println("Info: Peer " +  _selfId + " send" + sendinterestedMessage.getType() + " message from " + _peerId);
                        }catch (Exception e) {
                            System.out.println("Info: Peer " +  _selfId + " is not able to send" + sendinterestedMessage.getType() + " message from " + _peerId);

                        }
                    } else {
                        Message sendNotInterestedMessage = new Message();
                        sendNotInterestedMessage.setType(Message.notInterested);
                        //TODO: length and payload for the interested message
                        try {
                            sendNotInterestedMessage.sendMessage(_msgWriter);
                            System.out.println("Info: Peer " +  _selfId + " send" + sendNotInterestedMessage.getType() + " message from " + _peerId);
                        }catch (Exception e) {
                            System.out.println("Info: Peer " +  _selfId + " is not able to send" + sendNotInterestedMessage.getType() + " message from " + _peerId);

                        }
                    }
                }
                //receiving  messages . Both requires similar actions

            }catch (Exception e) {
                System.out.println("Error: Peer " + _selfId + " not able to receive message from " + _peerId);
            }


        }

    }
}