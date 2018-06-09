package usertest;

import joy.aksd.data.Block;
import joy.aksd.data.Record;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static joy.aksd.coreThread.powModule.getTarget;
import static joy.aksd.coreThread.powModule.isRight;
import static joy.aksd.tools.toInt.byteToInt;

public class verifyFunction {
    public boolean verifyTime(Block oldOne,Block newOne){
        int oldTime=byteToInt(oldOne.getTime());
        int newTime=byteToInt(newOne.getTime());
        return newTime>=oldTime-1;
    }
    public boolean verifyNonce(Block block) throws NoSuchAlgorithmException {
        byte target[]=getTarget(block.getDifficulty());
        byte[] lashHash=block.getLastHash();
        byte[] merkle=block.getMerkle();
        byte[] cumulativeDiff=block.getCumulativeDifficulty();
        byte difficulty=block.getDifficulty();
        byte[] tem=new byte[block.getBlockByteNum()];
        System.arraycopy(lashHash,0,tem,0,lashHash.length);
        System.arraycopy(merkle,0,tem,lashHash.length,merkle.length);
        System.arraycopy(cumulativeDiff,0,tem,lashHash.length+merkle.length,cumulativeDiff.length);
        tem[lashHash.length+merkle.length+cumulativeDiff.length+4]=difficulty;
        byte[]time=block.getTime();
        byte[]nonce=block.getNonce();
        System.arraycopy(time,0,tem,lashHash.length+merkle.length+cumulativeDiff.length,time.length);
        System.arraycopy(nonce,0,tem,lashHash.length+merkle.length+cumulativeDiff.length+time.length+1,nonce.length);//+1 为上面diffculty的一字节字段
        MessageDigest sha=MessageDigest.getInstance("SHA-256");
        return isRight(sha.digest(tem),target);
    }

    public boolean verifyLastHash(Block oldOne,Block newOne) throws NoSuchAlgorithmException {
        byte[] lashHash=oldOne.getLastHash();
        byte[] merkle=oldOne.getMerkle();
        byte[] time=oldOne.getTime();
        byte difficulty=oldOne.getDifficulty();
        byte[]nonce=oldOne.getNonce();
        byte[] tem=new byte[lashHash.length+merkle.length+time.length+1+nonce.length];
        System.arraycopy(lashHash,0,tem,0,lashHash.length);
        System.arraycopy(merkle,0,tem,lashHash.length,merkle.length);
        System.arraycopy(time,0,tem,lashHash.length+merkle.length,time.length);
        tem[lashHash.length+merkle.length+time.length]=difficulty;
        System.arraycopy(nonce,0,tem,lashHash.length+merkle.length+time.length+1,nonce.length);
        MessageDigest sha=MessageDigest.getInstance("SHA-256");
        byte []result=sha.digest(tem);

//        for (byte i:result){
//            System.out.print(i +" ");
//        }
//        System.out.println();
//        for (byte i:newOne.getLastHash()){
//            System.out.print(i+" ");
//        }

        return Arrays.equals(result,newOne.getLastHash());
    }

    public boolean verifyCumulaticeDiff(Block oldone,Block newone){
        int oldc=byteToInt(oldone.getCumulativeDifficulty());
        int newc=byteToInt(newone.getCumulativeDifficulty());
        return newc==(oldc+newone.getDifficulty());
    }

    public boolean verifyBlockNumber(Block oldone,Block newone){
        int oldn=byteToInt(oldone.getBlockNumber());
        int newn=byteToInt(newone.getBlockNumber());
        return (oldn+1)==newn;
    }

    public boolean verifyMerkle(Block block){
        if (byteToInt(block.getRecordCount())==0)
            return true;
        ArrayDeque<byte []> result=new ArrayDeque<>();
        byte blockData[]=block.getData();
        int x=0;
        for (int i=0;i<byteToInt(block.getRecordCount());i++){
            byte []tem=new byte[2];
            System.arraycopy(blockData,x,tem,0,2);
            x+=2;
            tem=new byte[byteToInt(tem)];
            System.arraycopy(blockData,x,tem,0,tem.length);
            x+=tem.length;
            Record r=new Record(tem);
            result.add(r.getBytesData());
//            System.out.println(r);
        }

        MessageDigest digest= null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
        for (int i=0;i<result.size();i++){
            byte tem[]=result.removeFirst();
            tem=digest.digest(tem);
            result.addLast(tem);
        }
        try {
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
            return Arrays.equals(result.getFirst(),block.getMerkle());
        }catch (Throwable e){
            return false;
        }


    }

    public void start() throws IOException, NoSuchAlgorithmException {
        DataInputStream in=new DataInputStream(new FileInputStream("./blockTest"));
        byte tem[];
        Block oldOne=null;
        Block newOne=null;
        verifyFunction vf=new verifyFunction();
        while (true){
            //读取区块
            tem=new byte[2];
            in.read(tem);
            int byteCount=byteToInt(tem);
            if (byteCount==0) {
                System.out.println("success");
                break;
            }
            tem=new byte[byteCount];
            in.read(tem);
            //复原区块
            Block block=new Block(tem);
            newOne=block;
            System.out.println(block);


            if (!vf.verifyNonce(block)){
                System.out.println("error in nonce");
                break;
            }
            if (!vf.verifyMerkle(block)){
                System.out.println("error in merkle root");
                break;
            }

            if (oldOne==null){
                oldOne=newOne;
                continue;
            }
            if (!vf.verifyCumulaticeDiff(oldOne,newOne)){
                System.out.println("error in cumulativeDiff");
                break;
            }
            if (!verifyBlockNumber(oldOne,newOne)){
                System.out.println("error in blockNumber");
                break;
            }
            if (!vf.verifyTime(oldOne,newOne)){
                System.out.println("error in time");
                System.exit(0);
                break;
            }
            if (!vf.verifyLastHash(oldOne,newOne)){
                System.out.println("error in lastHash");
                break;
            }
            System.out.println("this block success-----------");
            oldOne=newOne;
        }
        in.close();
    }

    public static void main(String []arg) throws IOException, NoSuchAlgorithmException {
       new verifyFunction().start();
    }
}
