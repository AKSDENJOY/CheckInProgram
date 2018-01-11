package admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import static joy.aksd.data.dataInfo.PORT;
import static joy.aksd.data.dataInfo.ROOTIP;
import static joy.aksd.tools.toInt.byteToInt;
import static joy.aksd.tools.toString.byteToString;

/**
 * Created by EnjoyD on 2017/5/3.
 */
public class queryIdentifiedRecord {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket(ROOTIP, PORT);

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            out.write(0x02);
            byte tem[]=new byte[4];
            in.read(tem);
            int count=byteToInt(tem);
            for (int i=0;i<count;i++){
                tem=new byte[32];
                in.read(tem);
                System.out.println(byteToString(tem));
            }
            socket.close();


        }catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
