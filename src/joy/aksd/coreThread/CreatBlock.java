package joy.aksd.coreThread;

import joy.aksd.data.Block;
import joy.aksd.data.Record;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static joy.aksd.data.dataInfo.*;
import static joy.aksd.tools.toByte.intToByte;
import static joy.aksd.tools.toInt.byteToInt;

/** 创建区块
 * Created by EnjoyD on 2017/4/18.
 */
public class CreatBlock {

    public Block start() throws NoSuchAlgorithmException {
        Block block=new Block();
        //获取前一区块哈希 32字节
        block.setLastHash(getLashHashValue());
        //获取默克尔树并填充blockdata 32字节
        block.setMerkle(generateMerkle(block));
        //获取难度值
        block.setDifficulty(getDifficulty(blocks.getLast().getDifficulty()));
        //设置累计区块难度
        block.setCumulativeDifficulty(getCurrentCumulativeDifficulty(block.getDifficulty(),blocks.getLast().getCumulativeDifficulty()));
        //返回未完成的block
        return block;
    }

    private byte[] getCurrentCumulativeDifficulty(byte difficulty, byte[] cumulativeDifficulty) {
        int diff=(int)difficulty+byteToInt(cumulativeDifficulty);
        System.out.println("diff is "+diff);
        return intToByte(diff);
    }

    private byte getDifficulty(byte difficulty) {
        if (timeRecord.size()==adjustCount){
            int avgTime=getAvgTime();
            byte Diff=difficulty;
            if (avgTime>exceptTime+errorTime){
                Diff=decrease(difficulty);
            }
            if (avgTime<exceptTime-errorTime){
                Diff=increase(difficulty);
            }
            timeRecord.clear();
            System.out.println("-----------------------------");
            System.out.println("avgtime is "+avgTime);
            System.out.println("-----------------------------");
            return Diff;
        }
        else {
            return difficulty;
        }
    }

    private byte increase(byte difficulty) {
        int i=difficulty&0xff;
        i+=1;
        return (byte) i;
    }

    private byte decrease(byte difficulty) {
        int i=difficulty&0xff;
        i-=1;
        return (byte) i;

    }

    private int getAvgTime() {
        int result=0;
        for (int i=1;i<timeRecord.size();i++){
            result+=(timeRecord.get(i)-timeRecord.get(i-1));
        }
        return result/(timeRecord.size()-1);

    }

    private byte[] generateMerkle(Block block) {
        //从池中取记录 打包成默克尔树这个地方需要进行全局操作，比如取记录
        ArrayDeque<byte []> result=new ArrayDeque<>();
        int bytes=0;
        //取出纪录
        synchronized (identifedRecord) {
            if (identifedRecord.size() == 0){
                block.setRecordCount(0);
                block.setData(new byte[0]);
                return new byte[32];
            }
            Iterator<Record> it = identifedRecord.iterator();
            int i = 0;
            while (i++ < merkleTreeLimitation && it.hasNext()) {
                Record record=it.next();
                byte []tem=record.getBytesData();
                bytes+=tem.length;
                result.add(tem);
                unPackageRecord.add(record);
                it.remove();
            }
        }
        //填充block recordCount数据
        block.setRecordCount(result.size());
        //填充block 的data数据
        byte BlockData[]=new byte[bytes];
        System.out.println("block data length is "+bytes);
        bytes=0;
        for (byte[] tem : result) {
            System.arraycopy(tem, 0, BlockData, bytes, tem.length);
            bytes += tem.length;
            System.out.println(Arrays.toString(tem));
        }
        block.setData(BlockData);
        //开始计算merkle tree root
        MessageDigest digest= null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        for (int i=0;i<result.size();i++){
            byte tem[]=result.removeFirst();
            tem=digest.digest(tem);
            result.addLast(tem);
        }
        while (result.size()!=1){
            ArrayDeque<byte []> temResult=new ArrayDeque<>();
            while (!result.isEmpty()){
                byte []left=result.removeFirst();
                byte []right=null;
                try {
                    right = result.removeFirst();
                }catch (NoSuchElementException e){}
                if (right==null){
                    temResult.addLast(digest.digest(left));
                }
                else {
                    byte []tem=new byte[left.length+right.length];
                    System.arraycopy(left,0,tem,0,left.length);
                    System.arraycopy(right,0,tem,left.length,right.length);
                    temResult.addLast(digest.digest(tem));
                }
            }
            result=temResult;
        }
        return result.getFirst();
    }

    private byte[] getLashHashValue() throws NoSuchAlgorithmException {
        Block block=blocks.getLast();
        byte[] lashHash=block.getLastHash();
        byte[] merkle=block.getMerkle();
        byte[] time=block.getTime();
        byte difficulty=block.getDifficulty();
        byte[]nonce=block.getNonce();
        byte[] tem=new byte[lashHash.length+merkle.length+time.length+1+nonce.length];
        System.arraycopy(lashHash,0,tem,0,lashHash.length);
        System.arraycopy(merkle,0,tem,lashHash.length,merkle.length);
        System.arraycopy(time,0,tem,lashHash.length+merkle.length,time.length);
        tem[lashHash.length+merkle.length+time.length]=difficulty;
        System.arraycopy(nonce,0,tem,lashHash.length+merkle.length+time.length+1,nonce.length);
        byte []result=SHA256x.digest(tem);
        return result;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
//        byte tem[]=new byte[0];
//        System.out.println(tem.length);
//        Block block=new Block();
//        Record record=new Record();
//        record.setLockScript(new byte[32]);
//        record.setMac(new byte[6]);
//        record.setTime(new byte[4]);
//        record.setOrderStamp(new byte[4]);
//        record.setUnLockScript(new byte[85]);
//        identifedRecord.add(record);
//        identifedRecord.add(record);
//        identifedRecord.add(record);
//        identifedRecord.add(record);
//        identifedRecord.add(record);
//
//        block.setMerkle(new CreatBlock().generateMerkle(block));
//        block.setLastHash(new byte[32]);
//        block.setDifficulty((byte) 0x01);
//        block.setTime(new byte[4]);
//        block.setNonce(new byte[4]);
//        block.setBlockNumber(1);
//        block.getBlockDatas();
    }

}
