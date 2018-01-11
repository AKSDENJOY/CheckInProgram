import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

import static joy.aksd.data.dataInfo.*;
import static joy.aksd.data.protocolInfo.DOWNLOADBLOCK;
import static joy.aksd.tools.toByte.longToByte;

/**
 * Created by EnjoyD on 2017/6/15.
 */
public class DownLoadBlocks {
    public static void main(String[] args) {
        Socket socket = null;
        InputStream in;
        OutputStream out;
        try {
            socket = new Socket(ROOTIP, PORT);
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            System.out.println("error in network");
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;

        }


        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(location, "rw");
            out.write(DOWNLOADBLOCK);
            downLoadFromLocation(in,out,file,file.length());

        } catch (IOException e) {
            System.out.println("error in file");
            return;
        } finally {
            try {
                socket.close();
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void downLoadFromLocation(InputStream in, OutputStream out,RandomAccessFile file,long start) throws IOException {
        out.write(longToByte(start));
        file.seek(start);
        byte[] buff = new byte[1024];
        int i;
        while ((i = in.read(buff)) != -1) {
            file.write(buff, 0, i);
        }
        file.close();

    }
}
