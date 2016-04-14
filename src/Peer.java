public class Peer {
    private int _id;
    private String _hostname;
    private int _port;
    private boolean _hasCompleteFile;
    private Bitfield _bitfield;
    private boolean _isChoked;
    private boolean _isInterested;
    private int _speed;
    
    public Peer(int totPieces){
        _id = 0;
        _hostname = "";
        _port = 0;
        _bitfield = new Bitfield(totPieces);
        _isChoked = true;
        _isInterested = false;
        _speed = 0;
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
    }
    
    public void setId(int id){
        _id = id;
    }
    
    public int getId(){
        return _id;
    }
    
    public void setHostname(String hostname){
        _hostname = hostname;
    }
    
    public String getHostname(){
        return _hostname;
    }
    
    public void setPort(int port){
        _port = port;
    }
    
    public int getPort(){
        return _port;
    }
    
    public void setCompleteFile(boolean hasCompleteFile){
        if(hasCompleteFile){
            _bitfield.setAllBitsTrue();
        }
    }
    
    public boolean hasCompleteFile(){
        return _bitfield.getFinished();
    }
    
    public void setBitfield(Bitfield bitfield){
        _bitfield = bitfield;
    }
    
    public Bitfield getBitfield(){
        return _bitfield;
    }
    
    public void setChoked(boolean isChoked){
        _isChoked = isChoked;
    }
    
    public boolean isChoked(){
        return _isChoked;
    }
    
    public void setIntereted(boolean isInterested){
        _isInterested = isInterested;
    }
    
    public boolean isInterested(){
        return _isInterested;
    }
    
    public void setSpeed(int speed){
        _speed = speed;
    }
    
    public int getSpeed(){
        return _speed;
    }
}