public class Peer {
    private int _id;
    private String _hostname;
    private int _port;
    private Bitfield _bitfield;
    private boolean _isChoked;
    private boolean _isInterested;
    private int _speed;
    private boolean _peerExited;
    
    public Peer(int totPieces){
        _id = 0;
        _hostname = "";
        _port = 0;
        _bitfield = new Bitfield(totPieces);
        _isChoked = true;
        _isInterested = false;
        _speed = 0;
        _peerExited = false;
    }
    
    public Peer(int id, String hostname, int port, boolean hasCompleteFile, int totPieces){
        _id = id;
        _hostname = hostname;
        _port = port;
        _bitfield = new Bitfield(totPieces);
        if(hasCompleteFile){
            _bitfield.setAllBitsTrue();
        }
        _isChoked = true;
        _isInterested = false;
        _speed = 0;
        _peerExited = false;
    }
    
    public synchronized void setId(int id){
        _id = id;
    }
    
    public synchronized int getId(){
        return _id;
    }
    
    public synchronized void setHostname(String hostname){
        _hostname = hostname;
    }
    
    public synchronized String getHostname(){
        return _hostname;
    }
    
    public synchronized void setPort(int port){
        _port = port;
    }
    
    public synchronized int getPort(){
        return _port;
    }
    
    public synchronized void setCompleteFile(boolean hasCompleteFile){
        if(hasCompleteFile){
            _bitfield.setAllBitsTrue();
        } else {
            _bitfield.setAllBitsFalse();
        }
    }
    
    public synchronized boolean hasCompleteFile(){
        return _bitfield.getFinished();
    }
    
    public synchronized void setBitfield(Bitfield bitfield){
        _bitfield = bitfield;
    }
    
    public synchronized Bitfield getBitfield(){
        return _bitfield;
    }
    
    public synchronized void setChoked(boolean isChoked){
        _isChoked = isChoked;
    }
    
    public synchronized boolean isChoked(){
        return _isChoked;
    }
    
    public synchronized void setInterested(boolean isInterested){
        _isInterested = isInterested;
    }
    
    public synchronized boolean isInterested(){
        return _isInterested;
    }
    
    public synchronized void incrementSpeed(){
        _speed = _speed + 1;
    }

    public synchronized void resetSpeed() { _speed = 0; }
    
    public synchronized int getSpeed(){
        return _speed;
    }
    
    public synchronized void setExited(boolean exited){
        if(exited){
            setCompleteFile(true);
        }
        _peerExited = exited;
    }
    
    public synchronized boolean isExited(){
        return _peerExited;
    }
}