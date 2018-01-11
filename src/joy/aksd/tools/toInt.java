package joy.aksd.tools;

/**
 * Created by EnjoyD on 2017/4/19.
 */
public class toInt {
    public static int byteToInt(byte[]res){
        if (res.length==4)
            return res[3] & 0xff |
                (res[2] & 0xff) << 8 |
                (res[1] & 0xff) << 16 |
                (res[0] & 0xff) << 24;
        if (res.length==3)
            return res[2] & 0xff |
                    (res[1] & 0xff) << 8 |
                    (res[0] & 0xff) << 16 ;
        else
            return res[1] & 0xff | (res[0] & 0xff) << 8 ;
    }
}
