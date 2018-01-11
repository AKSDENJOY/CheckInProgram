package joy.aksd.coreThread;

import joy.aksd.data.Block;

import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;

import static joy.aksd.data.dataInfo.unPackageRecord;

/**
 * Created by EnjoyD on 2017/5/19.
 */
public class coreProcess implements Runnable{

    @Override
    public void run() {
        System.out.println("start joy.aksd.coreThread.coreProcess");
        while (true) {
            //创建区块
            CreatBlock creatBlock = new CreatBlock();
            Block block ;
            try {
                block = creatBlock.start();
            } catch (NoSuchAlgorithmException e) {
                System.out.println("创建区块失败");
                continue;
            }
            //pow找hash值完成区块的最后nonce值部分并加入链中
            powModule pow = new powModule();
            try {
                pow.start(block);
            } catch (NoSuchAlgorithmException e) {
                System.out.println("pow模块失败");
                continue;
            } catch (InterruptedException e) {
                System.out.println("pow 中断结束");
                break;
            }
            //广播区块
            BroadcastBlock broadcastBlock = new BroadcastBlock(block);
            try {
                broadcastBlock.start();

            }catch (Exception e){
                System.out.println("broadcast error ");
            }
            //写链中区块入硬盘
            WriteBlock writeBlock = new WriteBlock(block);
            try {
                writeBlock.start();
                unPackageRecord.clear();
            } catch (FileNotFoundException e) {
                System.out.println("写入失败");
                continue;
            }
            System.out.println(block);
        }
        System.out.println("joy.aksd.coreThread.coreProcess finished");
    }
}