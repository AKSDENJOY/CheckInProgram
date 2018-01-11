package joy.aksd.tools;

/**
 * Created by EnjoyD on 2017/4/19.
 */
public class toByte {
    public static byte[] intToByte(long i) {
        byte []result=new byte[4];
        for (int j=0;j<4;j++){
            result[j]= (byte) ((i>>(8*(3-j)))&0xff);//低下标存高位，高下标存低位 i为整数，因此可以不带符号位右移
        }
        return result;
    }

    public static byte[]longToByte(long i){
        byte []result=new byte[8];
        for (int j=0;j<8;j++){
            result[j]= (byte) ((i>>(8*(7-j)))&0xff);
        }
        return result;
    }

    public static byte intToOneByte(int i){
        return (byte) (i&0xff);
    }

    public static byte[] hexStringToByteArray(String hexString){
        hexString=hexString.toLowerCase();
        byte [] result=new byte[hexString.length()/2];
        for (int i=0;i<hexString.length();i+=2){
            result[i/2]= (byte) (((Character.digit(hexString.charAt(i),16))<<4)+Character.digit(hexString.charAt(i+1),16));
        }
        return result;

    }

}
