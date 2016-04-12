import java.io.*;
import java.net.*;
import java.lang.Thread;
import java.lang.Exception;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Worker extends Thread{
    public Socket _socket;
    public int _selfId;
    public int _peerId;
    InputStream _msgReader;
    OutputStream _msgWriter;
    DataFileHandler _fileHandler;
    peerConfig _configInfo;
    
    
    public Worker(Socket socket, peerConfig configInfo, int selfId, int peerId){
        _socket = socket;
        _selfId = selfId;
        _peerId = peerId;
        _configInfo = configInfo;
    }
    
    public Worker(Socket socket, peerConfig configInfo, int selfId){
        _socket = socket;
        _selfId = selfId;
        _peerId = -1;
        _configInfo = configInfo;
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
        
        // check and send bitfield messages
        if(peerConfig.peerList.get(_selfId).hasCompleteFile()){
            Message bfMsg = new Message();
            bfMsg.setType((byte)5);
            byte[] bfPayload = peerConfig.peerList.get(_selfId).getBitfield().changeBitToByteField();
            int bfLength = peerConfig.peerList.get(_selfId).getBitfield().getLengthInBytes();
            bfMsg.setPayload(bfPayload);
            bfMsg.setLength(bfLength);
            try{
                bfMsg.sendMessage(_msgWriter);
                System.out.println("Info: Peer " +  _selfId + " sent bitfiedl message to " + _peerId);
            } catch(Exception e){
                System.out.println("Error: Peer " +  _selfId + " not able to send bifield message to " + _peerId);
            }
            
        }
        
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
            System.out.println("Info: Peer " +  _selfId + " received bitfiedl message from " + _peerId);
        }catch(Exception e){
            System.out.println("Error: Peer " +  _selfId + " not able to receive bifield message from " + _peerId);
        }
        
    }
}