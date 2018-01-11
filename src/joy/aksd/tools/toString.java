package joy.aksd.tools;


/**
 * Created by EnjoyD on 2017/4/27.
 */
public class toString {
    public static String byteToString(byte []array){
        StringBuilder sb=new StringBuilder();
        for (byte b:array){
            sb.append(Character.forDigit(b>>4&0xf,16));
            sb.append(Character.forDigit(b&0xf,16));
        }
        return sb.toString();

    }

    public static void main(String[] args) {
    }
}
