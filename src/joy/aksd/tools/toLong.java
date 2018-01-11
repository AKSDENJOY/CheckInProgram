package joy.aksd.tools;

import java.nio.ByteBuffer;

/**
 * Created by EnjoyD on 2017/6/16.
 */
public class toLong {
    private static ByteBuffer buffer=ByteBuffer.allocate(8);
    public static long byteToLong(byte[]src){
        buffer.put(src,0,8);
        buffer.flip();
        return buffer.getLong(0);
    }
}
