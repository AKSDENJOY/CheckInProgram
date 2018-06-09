package joy.aksd.coreThread;

import joy.aksd.data.Block;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import static joy.aksd.data.dataInfo.*;
import static joy.aksd.tools.toByte.intToByte;
import static joy.aksd.tools.toInt.byteToInt;

/** 工作量证明模块
 * Created by EnjoyD on 2017/4/10.
 */
public class powModule {
    /**
     * 工作量证明模块开始接口
     * @param block
     * @throws NoSuchAlgorithmException
     * @throws InterruptedException
     */
    public void start(Block block) throws NoSuchAlgorithmException, InterruptedException {
        findNonceAndTime(block);
    }

    /**
     * pow寻找合理的time字段与nonce字段
     * @param block
     * @throws NoSuchAlgorithmException
     * @throws InterruptedException
     */
    public static void findNonceAndTime(Block block) throws NoSuchAlgorithmException, InterruptedException {
        byte target[]=getTarget(block.getDifficulty());
        byte[] lashHash=block.getLastHash();
        byte[] merkle=block.getMerkle();
        byte[] cumulativeDiff=block.getCumulativeDifficulty();
        byte difficulty=block.getDifficulty();
        byte[] tem=new byte[block.getBlockByteNum()];
        System.arraycopy(lashHash,0,tem,0,lashHash.length);
        System.arraycopy(merkle,0,tem,lashHash.length,merkle.length);
        System.arraycopy(cumulativeDiff,0,tem,lashHash.length+merkle.length,cumulativeDiff.length);
        tem[lashHash.length+merkle.length+cumulativeDiff.length+4]=difficulty;//+4 为后面的4字节time字段
//        int unixTimeStart= getSystemTime();
//        int NTPTimeStart=getNTPtime();
        for (int i=0;true;i++){
            if (interupt){
                Thread.currentThread().interrupt();
                if (Thread.currentThread().isInterrupted()){
                    TimeUnit.SECONDS.sleep(1);
                }
            }
            if (i==Integer.MAX_VALUE)
                i=0;
            byte[]time=intToByte(getTime());
            byte[]nonce=intToByte(i);
            System.arraycopy(time,0,tem,lashHash.length+merkle.length+cumulativeDiff.length,time.length);
            System.arraycopy(nonce,0,tem,lashHash.length+merkle.length+cumulativeDiff.length+time.length+1,nonce.length);//+1 为上面diffculty的一字节字段
            if (isRight(SHA256x.digest(tem),target)){
                block.setNonce(nonce);
                block.setTime(time);
                while (blocks.size()>cacheBlockCount)//缓存最近cacheBlockCount个区块
                    blocks.remove(0);
                num=byteToInt(blocks.getLast().getBlockNumber())+1;
                blocks.addLast(block);
                timeRecord.add(byteToInt(time));
                block.setBlockNumber(num);
                return;
            }
        }

    }
    /**
     * 判断是否满足目标
     *
     * @param hash 要判断的值
     * @param target 目标值
     * @return 正确与否
     */
    public static boolean isRight(byte[] hash, byte[] target) {
        for (int i = 0; i < target.length - 1; i++) {
            if (hash[i] != target[i])
                return false;
        }
        int temHash=hash[target.length-1]&0xff;
        int temTarget=target[target.length-1]&0xff;

        return temHash <= temTarget;

//        if (target[target.length - 1] == -1) {
//            if (hash[target.length - 1] < 0 || hash[target.length - 1] > 16)//0x0f
//                return false;
//        } else {
//            if (hash[target.length - 1] != target[target.length - 1])//0x00
//                return false;
//        }
//        return true;

    }

    /**
     * 根据难度求目标
     * 难度值表示二进制中前多少位是0
     * @param difficulty 难度值
     * @return 难度前缀
     */
    public static byte[] getTarget(byte difficulty) {
        int num=difficulty&0xff;
        int tem1=num/8;
        int tem2=num%8;
        byte[] target;
        if (tem2==0){
            target=new byte[tem1];
            init(target);
        }
        else {
            target=new byte[tem1+1];
            init(target);
            switch (tem2){
                case 1:
                    target[tem1]=0x7f;
                    break;
                case 2:
                    target[tem1]=0x3f;
                    break;
                case 3:
                    target[tem1]=0x1f;
                    break;
                case 4:
                    target[tem1]=0x0f;
                    break;
                case 5:
                    target[tem1]=0x07;
                    break;
                case 6:
                    target[tem1]=0x03;
                    break;
                case 7:
                    target[tem1]=0x01;
                    break;
            }
        }
        return target;
    }

    private static void init(byte[] target) {
        for(int i=0;i<target.length;i++){
            target[i]=0x00;
        }
    }
}
