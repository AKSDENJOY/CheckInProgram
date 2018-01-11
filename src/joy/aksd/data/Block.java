package joy.aksd.data;

import java.io.Serializable;

import static joy.aksd.tools.toByte.intToByte;
import static joy.aksd.tools.toInt.byteToInt;

/**
 * Created by EnjoyD on 2017/4/18.
 */
public class Block implements Serializable{
    private byte lastHash[];//32 bytes
    private byte Merkle[];// 32 bytes
    private byte time[];//4bytes
    private byte difficulty;//1 bytes难度目标，
    private byte nonce[];//4 bytes
    private byte cumulativeDifficulty[];//积累难度值，用于区块共识4 bytes

    private byte data[];

    private byte[] blockNumber;//3字节
    private byte[] recordCount;//2字节


    public Block(){

    }

    public Block(byte[] count) {
        byte tem[]=new byte[32];
        System.arraycopy(count,0,tem,0,32);
        setLastHash(tem);

        tem=new byte[32];
        System.arraycopy(count,lastHash.length,tem,0,32);
        setMerkle(tem);

        tem=new byte[4];
        System.arraycopy(count,lastHash.length+Merkle.length,tem,0,4);
        setTime(tem);

        setDifficulty(count[lastHash.length+Merkle.length+time.length]);

        tem=new byte[4];
        System.arraycopy(count,lastHash.length+Merkle.length+time.length+1,tem,0,4);
        setNonce(tem);

        tem=new byte[3];
        System.arraycopy(count,lastHash.length+Merkle.length+time.length+1+nonce.length,tem,0,3);
        setBlockNumber(tem);

        tem=new byte[4];
        System.arraycopy(count,lastHash.length+Merkle.length+time.length+1+nonce.length+blockNumber.length,tem,0,4);
        setCumulativeDifficulty(tem);

        tem=new byte[2];
        System.arraycopy(count,lastHash.length+Merkle.length+time.length+1+nonce.length+blockNumber.length+cumulativeDifficulty.length,tem,0,2);
        setRecordCount(tem);

        tem=new byte[count.length-(lastHash.length+Merkle.length+time.length+1+nonce.length+blockNumber.length+cumulativeDifficulty.length+recordCount.length)];
        System.arraycopy(count,lastHash.length+Merkle.length+time.length+1+nonce.length+blockNumber.length+cumulativeDifficulty.length+recordCount.length,tem,0,tem.length);
        setData(tem);

    }

    private void setRecordCount(byte[] tem) {
        this.recordCount=tem;
    }

    private void setBlockNumber(byte[] tem) {
        this.blockNumber=tem;
    }

    public byte[] getLastHash() {
        return lastHash;
    }

    public void setLastHash(byte[] lastHash) {
        this.lastHash = lastHash;
    }

    public byte[] getMerkle() {
        return Merkle;
    }

    public void setMerkle(byte[] merkle) {
        Merkle = merkle;
    }

    public byte[] getTime() {
        return time;
    }

    public void setTime(byte[] time) {
        this.time = time;
    }

    public byte getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(byte difficulty) {
        this.difficulty = difficulty;
    }

    public byte[] getNonce() {
        return nonce;
    }

    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
    }

    public byte[] getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        byte[]tem=intToByte(blockNumber);
        byte[]result=new byte[3];
        System.arraycopy(tem,1,result,0,3);
        this.blockNumber = result;
    }

    /**
     * 获取头部长度
     * @return 头部长度
     */
    public int getBlockByteNum(){
        return 32+32+4+4+1+4;
    }

    @Override
    public String toString() {
        return "num:"+byteToInt(blockNumber)+"\n"+
                "nonce:"+byteToInt(nonce)+"\n"+
                "diff:"+(difficulty&0xff)+"\n"+
                "time:"+byteToInt(time)+"\n"+
                "cumulativeDifficulty:"+byteToInt(cumulativeDifficulty)+"\n";

    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        byte[]tem=intToByte(recordCount);
        byte[]result=new byte[2];
        System.arraycopy(tem,2,result,0,2);
        this.recordCount = result;
    }

    /**
     * 获取整个block 字节数组
     * @return  block字节数组 读取时先读取前两字节得到整个区块长度，然后再读取剩余字节用构造函数生成区块
     */
    public byte[] getBlockDatas(){
        int i=lastHash.length+Merkle.length+time.length+1+nonce.length+blockNumber.length+cumulativeDifficulty.length+recordCount.length+data.length;
        byte result[]=new byte[2+i];
        byte tem[]=intToByte(i);
        System.arraycopy(tem,2,result,0,2);
        System.arraycopy(lastHash,0,result,2,lastHash.length);
        System.arraycopy(Merkle,0,result,2+lastHash.length,Merkle.length);
        System.arraycopy(time,0,result,2+lastHash.length+Merkle.length,time.length);
        result[2+lastHash.length+Merkle.length+time.length]=difficulty;
        System.arraycopy(nonce,0,result,2+lastHash.length+Merkle.length+time.length+1,nonce.length);
        System.arraycopy(blockNumber,0,result,2+lastHash.length+Merkle.length+time.length+1+nonce.length,blockNumber.length);
        System.arraycopy(cumulativeDifficulty,0,result,2+lastHash.length+Merkle.length+time.length+1+nonce.length+blockNumber.length,cumulativeDifficulty.length);
        System.arraycopy(recordCount,0,result,2+lastHash.length+Merkle.length+time.length+1+nonce.length+blockNumber.length+cumulativeDifficulty.length,recordCount.length);
        System.arraycopy(data,0,result,2+lastHash.length+Merkle.length+time.length+1+nonce.length+blockNumber.length+cumulativeDifficulty.length+recordCount.length,data.length);
        return result;
    }

    public byte[] getCumulativeDifficulty() {
        return cumulativeDifficulty;
    }

    public void setCumulativeDifficulty(byte [] cumulativeDifficulty) {
        this.cumulativeDifficulty = cumulativeDifficulty;
    }
}
