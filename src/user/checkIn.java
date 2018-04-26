package user;

import joy.aksd.coreThread.CreatRecord;

import java.io.IOException;
import java.net.Socket;

import static joy.aksd.data.dataInfo.PORT;
import static joy.aksd.data.dataInfo.ROOTIP;
import static joy.aksd.data.protocolInfo.LINKTEST;

/**
 * Created by EnjoyD on 2017/5/8.
 */
public class checkIn {
    public static void main(String[] args) {
        //检查连接
        Socket socket= null;
        try {
            socket = new Socket(ROOTIP,PORT);
            socket.getOutputStream().write(LINKTEST);
            socket.close();
        } catch (IOException e) {
            System.out.println("error in network please check the net connection");
            return;
        }
        new CreatRecord().start(1,null);
    }
    public static void startCheckIn(String path){
        //检查连接
        Socket socket= null;
        try {
            socket = new Socket(ROOTIP,PORT);
            socket.getOutputStream().write(LINKTEST);
            socket.close();
        } catch (IOException e) {
            System.out.println("error in network please check the net connection");
            return;
        }
        new CreatRecord().start(1,path);
    }

    public static void startCheckOut(String path){
        //检查连接
        Socket socket= null;
        try {
            socket = new Socket(ROOTIP,PORT);
            socket.getOutputStream().write(LINKTEST);
            socket.close();
        } catch (IOException e) {
            System.out.println("error in network please check the net connection");
            return;
        }
        new CreatRecord().start(2,path);
    }
}
