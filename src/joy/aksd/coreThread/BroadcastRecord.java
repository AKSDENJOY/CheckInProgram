package joy.aksd.coreThread;

import joy.aksd.data.Record;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static joy.aksd.data.dataInfo.IPList;
import static joy.aksd.data.dataInfo.PORT;
import static joy.aksd.data.protocolInfo.RECIVERECORD;
import static joy.aksd.data.protocolInfo.REGISTER;
import static joy.aksd.tools.toByte.intToByte;

/**
 * Created by EnjoyD on 2017/11/22.
 */
public class BroadcastRecord {
    private static ExecutorService RecordBroadcast= Executors.newFixedThreadPool(10);
    private Record record;
    private String fromIp;
    private int ttl;
    private boolean isRegist=false;
    public BroadcastRecord(Record r,String ip,int ttl){
        this.record=r;
        this.fromIp=ip;
        this.ttl=ttl;
    }
    public BroadcastRecord(Record r,String ip,int ttl,boolean isRegist){
        this.record=r;
        this.fromIp=ip;
        this.ttl=ttl;
        this.isRegist=isRegist;
    }
    public void start(){
        System.out.println("start boratcast record ");
        System.out.println("list size is"+IPList.size());
        System.out.println("ttl is "+ttl);
        ArrayList<String> iplist=new ArrayList<>();
        synchronized (IPList){
            iplist.addAll(IPList);
        }
        for (String ip:iplist){
            if (!ip.equals(fromIp)){
                RecordBroadcast.execute(new RecordBroadcastThread(record,ip,this.ttl,isRegist));
            }
        }
    }

}

class RecordBroadcastThread implements Runnable{
    private Record record;
    private String ip;
    private int ttl;
    private boolean isRegist;
    RecordBroadcastThread(Record r,String ip,int ttl,boolean isRegist){
        this.record=r;
        this.ip=ip;
        this.ttl=ttl;
        this.isRegist=isRegist;
    }
    @Override
    public void run() {
        System.out.println("broadcast to"+ip);
        Socket socket = null;
        OutputStream out = null;
        InputStream in = null;
        try {
            socket=new Socket(this.ip,PORT);
            out = socket.getOutputStream();
            if (isRegist) {
                out.write(REGISTER);
                out.write(intToByte(this.ttl-1));
                out.write(record.getBytesData());
            }
            else {
                out.write(RECIVERECORD);

                out.write(intToByte(this.ttl - 1));
                out.write(record.getBytesData());
//                out.write(record.getMac());
//                out.write(record.getOrderStamp());
//                out.write(record.getTime());
//                out.write(record.getLockScript());
//                out.write(record.getUnLockScript());
            }

        } catch (IOException e) {
            System.out.println("broadcast record to "+ip+" error");
        }
    }
}
