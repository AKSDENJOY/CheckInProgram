package joy.aksd.coreThread;

import joy.aksd.data.Block;

import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static joy.aksd.data.dataInfo.*;
import static joy.aksd.data.protocolInfo.RECEIVEBLOCK;

/**
 * Created by EnjoyD on 2017/4/20.
 */
public class BroadcastBlock {
    private static ExecutorService broadCastThread= Executors.newFixedThreadPool(10);
    private Block block;
    public BroadcastBlock(Block block){
        this.block=block;
    }
    public void start(){
        broadcast();
    }

    private void broadcast() {
        System.out.println("broadcast IPlist size"+IPList.size());
        for(String ip:IPList) {
            if(!ip.equals(localIp))
                broadCastThread.execute(new broadCastThread(ip, this.block));
        }

        System.out.println("broadcast over");
    }
}

class broadCastThread implements Runnable{
    private String ip;
    private Block block;
    broadCastThread (String ip,Block block){
        this.ip=ip;
        this.block=block;
    }
    @Override
    public void run() {
        try {
            Socket socket = new Socket(ip, PORT);
            System.out.println("broadcast to"+ip);


            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            out.write(RECEIVEBLOCK);

//            out.write(intToByte(block.getBlockDatas().length));

            out.write(block.getBlockDatas());
            System.out.println("send block is ------");
            System.out.println(block.toString());
            System.out.println("------");

            byte tag[]=new byte[1];
            in.read(tag);

            if (tag[0] == 0x02) {//同步区块
                LinkedList<Block> copyBlock = new LinkedList<>();
                synchronized (blocks){
                    copyBlock.addAll(blocks);
                }
//                Collections.copy(copyBlock, blocks);

                //region 原本只同步部分区块，现更改为全部blocks
//                if (copyBlock.size() >= 20) {
//                    out.write(intToByte(20));
//                } else {
//                    out.write(intToByte(copyBlock.size()));
//                }
//
//                for (int i = 0; i < copyBlock.size() && i < 200; i++) {
//                    Block temBlock = copyBlock.removeLast();
//                    out.write(intToByte(temBlock.getBlockDatas().length));
//                    out.write(temBlock.getBlockDatas());
//                }
                //endregion
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
                objectOutputStream.writeObject(copyBlock);

                System.out.println(copyBlock.size());

                objectOutputStream.writeObject(freshRecord);
                objectOutputStream.writeObject(identifedRecord);
                objectOutputStream.writeObject(unPackageRecord);
                objectOutputStream.writeObject(indexBlock);
                objectOutputStream.writeObject(timeRecord);

                System.out.println(freshRecord.toString());
                System.out.println(identifedRecord.toString());
                System.out.println(unPackageRecord.toString());
                System.out.println(indexBlock.size());
                System.out.println(timeRecord.size());
            }
            else if (tag[0] == 0x01){
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
                objectOutputStream.writeObject(identifedRecord);
                objectOutputStream.writeObject(unPackageRecord);
                System.out.println("send rest data");
                System.out.println(identifedRecord.toString());
                System.out.println(unPackageRecord.toString());
                System.out.println("------");
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("connect " +ip+" error" );
            synchronized (IPList){
                IPList.remove(ip);
            }
        }
    }
}
