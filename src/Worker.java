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
    DataFileHandler _fileHandler;
    peerConfig _configInfo;
    Map<Integer, Peer> _peerList;
    ArrayList<Integer> _peerIdList;
    
    
    public Worker(Socket socket, peerConfig configInfo, int selfId, int peerId, Map<Integer, Peer> peerList, ArrayList<Integer> peerIdList){
        _socket = socket;
        _selfId = selfId;
        _peerId = peerId;
        _configInfo = configInfo;
        _peerList = peerList;
        _peerIdList = peerIdList;
    }
    
    public Worker(Socket socket, peerConfig configInfo, int selfId, Map<Integer, Peer> peerList, ArrayList<Integer> peerIdList){
        _socket = socket;
        _selfId = selfId;
        _peerId = -1;
        _configInfo = configInfo;
        _peerList = peerList;
        _peerIdList = peerIdList;
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
            bfMsg.setType((byte)5);
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
            }catch(Exception e){
                System.out.println("Error: Peer " +  _selfId + " not able to receive bitfield message from " + _peerId);
            }
        }
        // do this in a loop until both have complete file.
        while(!_peerList.get(_selfId).hasCompleteFile() && !_peerList.get(_peerId).hasCompleteFile()){
            // set preferred neighbours
//            try{
//                while(_msgReader.available() == 0){
//                    Thread.sleep(5);
//                }
//            }catch(Exception e){
//                System.out.println("Error: Waiting for msgReader. Message : " + e.getMessage());
//            }

        }
        
    }
}