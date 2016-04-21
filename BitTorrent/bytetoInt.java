/**
 * This class is used to convert a byte array to int and vice versa(Taken logic from stack overflow
 * 
 */
public class bytetoInt {

	/*
	 *This function converts byte array to int 
	 */
	public synchronized static int byteArrayToInt(byte[] b) 
	{
	    int value = 0;
	    for (int i = 0; i < 4; i++) {
	        int shift = (4 - 1 - i) * 8;
	        value += (b[i] & 0x000000FF) << shift;
	    }
	    return value;
	}
	/*
	 *This function converts int to byte array 
	 */
	public synchronized static byte[] intToByteArray(int a)
	{
	    byte[] ret = new byte[4];
	    ret[3] = (byte) (a & 0xFF);   
	    ret[2] = (byte) ((a >> 8) & 0xFF);   
	    ret[1] = (byte) ((a >> 16) & 0xFF);   
	    ret[0] = (byte) ((a >> 24) & 0xFF);
	    return ret;
	}

}
