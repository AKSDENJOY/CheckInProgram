package joy.aksd.listenAndVerifyThread;

import joy.aksd.ECC.ECC;
import joy.aksd.coreThread.BroadcastBlock;
import joy.aksd.coreThread.BroadcastRecord;
import joy.aksd.coreThread.WriteBlock;
import joy.aksd.data.Block;
import joy.aksd.data.Record;
import sun.security.ec.ECPublicKeyImpl;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.spec.ECPoint;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static joy.aksd.data.dataInfo.*;
import static joy.aksd.data.protocolInfo.*;
import static joy.aksd.tools.toByte.intToByte;
import static joy.aksd.tools.toInt.byteToInt;
import static joy.aksd.tools.toLong.byteToLong;
import static joy.aksd.tools.toString.byteToString;

/** 监听线程
 * Created by EnjoyD on 2017/5/2.
 */
public class Listener extends Thread {
    @Override
    public void run() {
        System.out.println("-----123123123123");
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("start listen");
        } catch (IOException e) {
            System.err.println("servsocket error");
            System.exit(1);
        }
        while (true) {
            Socket socket = null;
            try {
                assert serverSocket != null;
                socket = serverSocket.accept();
                System.out.println("receive a connection");
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            new handleThread(socket);
        }
    }
}

class handleThread implements Runnable {
    private Socket socket;

    handleThread(Socket socket) {
        this.socket = socket;
        new Thread(this).start();
    }

    @Override
    public void run() {
        DataInputStream in ;
        DataOutputStream out;
        try {
            in = new DataInputStream(socket.getInputStream());
            out=new DataOutputStream(socket.getOutputStream());
            byte tag = in.readByte();
            Record record ;
            byte[] receive ;
            switch (tag) {
                case REGISTER://新用户注册进区块链
                    //admin
                    System.out.println("register");
                    receive=new byte[4];
                    in.read(receive);
                    int ttl=byteToInt(receive);
                    receive =new byte[2];
                    in.read(receive);
                    receive=new byte[byteToInt(receive)];
                    in.read(receive);
                    record = new Record(receive);
                    if (ttl!=0)
                        dealRegistRecord(record,ttl);
                    this.socket.close();
                    break;
                case RECIVERECORD://收到纪录
                    System.out.println("received record");
                    receive=new byte[4];
                    in.read(receive);
                    ttl=byteToInt(receive);
                    receive =new byte[2];
                    in.read(receive);
                    receive=new byte[byteToInt(receive)];
                    in.read(receive);
                    record = new Record(receive);
                    if (ttl!=0)
                        dealRecord(record,ttl);
                    this.socket.close();
                    break;
                case 0x02://前期测试用
                    System.out.println("send result");
                    sendResult(out);
                    this.socket.close();
                    break;
                case QUERYSTAMPANDTIME://查询顺序戳
                    System.out.println("orderstamp and time");
                    sendOrderStampAndTime(in,out);
                    this.socket.close();
                    break;
                case LINKTEST://测试链接
                    System.out.println("test link");
                    break;
                case SELFQUERY://查询个人记录
                    System.out.println("query self informaiton");
                    startSearchIndividualRecord(in,out);
                    this.socket.close();
                    break;
                case RECEIVEBLOCK:
                    System.out.println("receive block");
                    startReceiveBlockProcess(in,out);
                    break;
                case ADMINQUERY:
                    System.out.println("admin query");
                    startAdminQueryProcess(in,out);
                    break;
                case DOWNLOADBLOCK:
                    System.out.println("receive down request");
                    startDownloadBlockProcess(in,out);
                    break;
                case GETIPLIST:
                    System.out.println("get list");
                    startGetIpListProcess(in,out);
                default:
                    break;
            }
        } catch (Exception e) {
            try {
                this.socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }finally {
            try {
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startGetIpListProcess(DataInputStream in, DataOutputStream out) throws IOException {

        String ip=this.socket.getRemoteSocketAddress().toString().split(":")[0];
        ip=ip.substring(1);
        System.out.println("reveive getip message "+ip);

        HashSet<String> tem=new HashSet<>();
        synchronized (IPList) {
            tem.addAll(IPList);
        }
        ObjectOutputStream o=new ObjectOutputStream(out);
        o.writeObject(tem);
        IPList.add(ip);
        System.out.println("receive a connection now ipList size is:"+IPList.size());

    }

    private void startReceiveBlockProcess(DataInputStream in, DataOutputStream out) throws IOException {
        byte []receive=new byte[2];
        System.out.println("receive a block,start process");
        try {
            in.read(receive);//读入区块长度
            receive=new byte[byteToInt(receive)];
            in.read(receive);//读入区块字节
        } catch (IOException e) {
            return;
        }
        Block receivedBlock=new Block(receive);

        System.out.println("receive block infomation:------");
        System.out.println(receivedBlock.toString());
        System.out.println("------");
        //区块累计难度更大，则为有效区块
        if (byteToInt(receivedBlock.getCumulativeDifficulty())>byteToInt(blocks.getLast().getCumulativeDifficulty())) {

            //接到的区块比期待区块号大，启动同步工作
            if (byteToInt(receivedBlock.getBlockNumber()) > byteToInt(blocks.getLast().getBlockNumber()) + 1) {
                interuptCoreThread();
                System.out.println("receive a higher block,interrupt core process");
                try {
                    out.write(0x02);
                    SycnFromOthers(in, out);//同步
                } catch (IOException e) {
                    System.err.println("error in sync first connection");
                }
//            backUpChainClear();
                reStartCoreThread();
                System.out.println("restart core process");
            }
            //接到的区块为期待区块，
            if (byteToInt(receivedBlock.getBlockNumber()) == byteToInt(blocks.getLast().getBlockNumber()) + 1) {
                Block last = blocks.getLast();
                System.out.println("reveive num is right");
                if (Arrays.equals(receivedBlock.getLastHash(), getLastHash(last))) {//验证区块是否为连接
                    out.write(0x01);
                    interuptCoreThread();
                    System.out.println("receive a expect block,now interrupt");

                    synchronized (blocks) {
                        blocks.add(receivedBlock);
                        timeRecord.add(byteToInt(receivedBlock.getTime()));
                        num = byteToInt(blocks.getLast().getBlockNumber());
                        System.out.println("update blocks and timerecord now size is " + num);
                    }
                    try {
                        updatefewData(in);
                    } catch (Exception e) {
                        System.err.println("error in updatefewdata");
                        ArrayList<Record> tem = new ArrayList<>();
                        tem.addAll(unPackageRecord);
                        tem.addAll(identifedRecord);
                        unPackageRecord.clear();
                        identifedRecord = tem;
                    }
                    System.out.println("start write received block");
                    try {
                        new WriteBlock(receivedBlock).start();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    System.out.println("start broad received block");
                    try {
                        new BroadcastBlock(receivedBlock).start();
                    } catch (Exception e) {
                        System.out.println("broadcast error ");
                    }
                    reStartCoreThread();
                    System.out.println("restart ");
                }
            }
        }
    }

    private void updatefewData(DataInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream=new ObjectInputStream(in);
        System.out.println("receive rest data ------");
        ArrayList<Record> temIdentifedRecord= (ArrayList<Record>) objectInputStream.readObject();
        ArrayList<Record> temUnPackageRecord= (ArrayList<Record>) objectInputStream.readObject();
        System.out.println(temIdentifedRecord.toString());
        System.out.println(temUnPackageRecord.toString());
        System.out.println("------");

        System.out.println("update few data");
        identifedRecord= temIdentifedRecord;
        unPackageRecord= temUnPackageRecord;
        ArrayList<Record> tem=new ArrayList<>();
        tem.addAll(unPackageRecord);
        tem.addAll(identifedRecord);
        unPackageRecord.clear();
        identifedRecord=tem;

    }

    private void reStartCoreThread() {
        interuptReset();
//        timeRecord.add(byteToInt(blocks.getLast().getTime()));
        coreWork.execute(new joy.aksd.coreThread.coreProcess());

    }

//    private void backUpChainClear() {
//        backUpChain.clear();
//    }

    private void updateProgramData(DataInputStream in, DataOutputStream out) throws IOException, ClassNotFoundException {
        //freshRecord   identifedRecord  unPackageRecord indexBlock timeRecord
        System.out.println("update data");

        ObjectInputStream objectInputStream=new ObjectInputStream(in);
        LinkedList<Block> receiveblocks= (LinkedList<Block>) objectInputStream.readObject();
        ConcurrentHashMap<String, Record> temVerifyRecord2=(ConcurrentHashMap<String, Record>) objectInputStream.readObject();
        ArrayList<Record> temIdentifedRecord= (ArrayList<Record>) objectInputStream.readObject();
        ArrayList<Record> temUnPackageRecord= (ArrayList<Record>) objectInputStream.readObject();
        ArrayList<Long> temIndexBlock= (ArrayList<Long>) objectInputStream.readObject();
        ArrayList<Integer> temTimeRecord= (ArrayList<Integer>) objectInputStream.readObject();

        System.out.println(receiveblocks.size());
        System.out.println(temVerifyRecord2.toString());
        System.out.println(temIdentifedRecord.toString());
        System.out.println(temUnPackageRecord.toString());
        System.out.println(temIndexBlock.size());
        System.out.println(temTimeRecord.size());

        blocks.clear();
        blocks.addAll(receiveblocks);

        freshRecord = temVerifyRecord2;
        identifedRecord= temIdentifedRecord;
        unPackageRecord= temUnPackageRecord;
        indexBlock= temIndexBlock;
        timeRecord= temTimeRecord;
        ArrayList<Record> tem=new ArrayList<>();
        tem.addAll(unPackageRecord);
        tem.addAll(identifedRecord);
        unPackageRecord.clear();
        identifedRecord=tem;
        try {
            reWriteToHardrie(receiveblocks);
        }catch (IOException e){
            System.out.println("error in writting");
            throw e;
        }


    }

    private void SycnFromOthers(DataInputStream in, DataOutputStream out) throws IOException {

        try {
            updateProgramData(in,out);
        } catch (Exception e) {
            System.err.println("error in sync");
            System.exit(1);
        }

    }

    private void reWriteToHardrie(LinkedList<Block> receivedBlock) throws IOException {
        Block first=receivedBlock.get(0);
        long cur=indexBlock.get(byteToInt(first.getBlockNumber()));
        RandomAccessFile f=new RandomAccessFile(location,"rw");
        f.seek(cur);
        for (Block b:receivedBlock){
            f.write(b.getBlockDatas());
        }
        f.close();
    }

//    private void useBack(Block receivedBlock) {
//        ArrayList<Block> arr=backUpChain.get(0);
//        LinkedList<Block> copyBlock=new LinkedList<>();
//        Collections.copy(copyBlock,blocks);
//        for (int i=0;i<copyBlock.size();i++){
//            if (byteToInt(copyBlock.getLast().getBlockNumber())==byteToInt(arr.get(0).getBlockNumber())){
//                copyBlock.removeLast();
//                break;
//            }
//            copyBlock.removeLast();
//        }
//        copyBlock.addAll(arr);
//        copyBlock.add(receivedBlock);
//        blocks.clear();
//        blocks.addAll(copyBlock);
//    }

//    private boolean backUpChainIsAvailable(Block receivedBlock) {//目前仅进行简单判断
//        for (ArrayList<Block> arr:backUpChain){
//            if (byteToInt(arr.get(arr.size()-1).getBlockNumber())+1==byteToInt(receivedBlock.getBlockNumber())){
//                if (Arrays.equals(getLastHash(arr.get(arr.size()-1)),receivedBlock.getLastHash())){
//                    backUpChain.clear();
//                    backUpChain.add(arr);
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    private byte[] getLastHash(Block block) {
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
        try {
            return MessageDigest.getInstance("SHA-256").digest(tem);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("error in \n"+e.getStackTrace());
            return null;
        }
    }

    private void startDownloadBlockProcess(DataInputStream in, DataOutputStream out) throws IOException {
        RandomAccessFile file=null;
        try {
            byte []lens=new byte[8];
            in.read(lens);
            file=new RandomAccessFile(location,"r");
            file.seek(byteToLong(lens));
            int i=0;
            byte []buff=new byte[1024];
            while ((i=file.read(buff))!=-1){
                out.write(buff,0,i);
            }
        }
        finally{
            file.close();
        }
    }

    private void startAdminQueryProcess(DataInputStream in, DataOutputStream out) throws IOException{
        ArrayList<Record> recordToBeSent=new ArrayList<>();
        synchronized (identifedRecord){//从后向前遍历保证顺序
            int size=identifedRecord.size();
            for (int i=0;i<size;i++){
                recordToBeSent.add(identifedRecord.get(size-1-i));
            }
        }
        for (Record record:recordToBeSent){
            out.write(record.getBytesData());
        }
        recordToBeSent.clear();

        //已打包但为建块
        synchronized (unPackageRecord){//从后向前遍历保证顺序
            int size=unPackageRecord.size();
            for (int i=0;i<size;i++){
                recordToBeSent.add(unPackageRecord.get(size-1-i));
            }
        }
        for (Record record:recordToBeSent){
            out.write(record.getBytesData());
        }
        recordToBeSent.clear();

        ArrayList<Block> cacheBlocks=new ArrayList<>();
        //再查缓存块
        synchronized (blocks){//从后向前遍历保证顺序，由于blocks是单向链表，此处以后优化
            cacheBlocks.addAll(blocks);
        }
        int BlockSize=cacheBlocks.size();
        for (int i=0;i<BlockSize;i++){
            int recordCount=byteToInt(cacheBlocks.get(BlockSize-1-i).getRecordCount());
            if (recordCount==0)// block contian no record
                continue;
            ArrayList<Record> cacheRecord=new ArrayList<>();
            byte []blockData=cacheBlocks.get(BlockSize-1-i).getData();
            int x =0;
            for (int j=0;j<recordCount;j++){//找到区块中的lockscript区块
                byte tem[]=new byte[2];
                System.arraycopy(blockData,x,tem,0,2);
                x+=2;
                tem=new byte[byteToInt(tem)];
                System.arraycopy(blockData,x,tem,0,tem.length);
                x+=tem.length;
                Record record=new Record(tem);
                cacheRecord.add(record);
            }
            //倒序发送
            for (int j=0;j<cacheRecord.size();j++){
                out.write(cacheRecord.get(cacheRecord.size()-1-j).getBytesData());
            }
            cacheRecord.clear();
        }
        //再查硬盘 不查了 太耗性能

    }

    private void startSearchIndividualRecord(DataInputStream in, DataOutputStream out) throws IOException {
        byte lockScrpit[]=new byte[32];
        in.read(lockScrpit);
        int count=10;//默认查询10条记录
//        ArrayDeque<Record> recordToBeSent=new ArrayDeque<>();
        ArrayList<Record> recordToBeSent=new ArrayList<>();
        //已验证但未打包
        synchronized (identifedRecord){//从后向前遍历保证顺序
            int size=identifedRecord.size();
            for (int i=0;i<size;i++){
                if (count==0)
                    break;
                if (Arrays.equals(identifedRecord.get(size-1-i).getLockScript(),lockScrpit)){
                    recordToBeSent.add(identifedRecord.get(size-1-i));
                    count--;
                }
            }
        }
        for (Record record:recordToBeSent){
            out.write(record.getBytesData());
        }
        recordToBeSent.clear();
        if (count==0){//不够默认数，继续查询
            return;
        }
        //已打包但为建块
        synchronized (unPackageRecord){//从后向前遍历保证顺序
            int size=unPackageRecord.size();
            for (int i=0;i<size;i++){
                if (count==0){
                    break;
                }
                if (Arrays.equals(unPackageRecord.get(size-1-i).getLockScript(),lockScrpit)){
                    recordToBeSent.add(unPackageRecord.get(size-1-i));
                    count--;
                }
            }
        }
        for (Record record:recordToBeSent){
            out.write(record.getBytesData());
        }
        recordToBeSent.clear();
        if (count==0){//不够默认数，继续查询
            return;
        }
        ArrayList<Block> cacheBlocks=new ArrayList<>();
        //再查缓存块
        synchronized (blocks){//从后向前遍历保证顺序，由于blocks是单向链表，此处以后优化
            for (Block block:blocks){
                cacheBlocks.add(block);
            }
        }
        int BlockSize=cacheBlocks.size();
        i:for (int i=0;i<BlockSize;i++){
            int recordCount=byteToInt(cacheBlocks.get(BlockSize-1-i).getRecordCount());
            if (recordCount==0)// block contian no record
                continue;
            ArrayList<Record> cacheRecord=new ArrayList<>();
            byte []blockData=cacheBlocks.get(BlockSize-1-i).getData();
            int x =0;
            for (int j=0;j<recordCount;j++){//找到区块中的lockscript区块
                byte tem[]=new byte[2];
                System.arraycopy(blockData,x,tem,0,2);
                x+=2;
                tem=new byte[byteToInt(tem)];
                System.arraycopy(blockData,x,tem,0,tem.length);
                x+=tem.length;
                Record record=new Record(tem);
                if (Arrays.equals(record.getLockScript(),lockScrpit))
                    cacheRecord.add(record);
            }
            //倒序发送
            for (int j=0;j<cacheRecord.size();j++){
                if (count==0)
                    break i;
                out.write(cacheRecord.get(cacheRecord.size()-1-j).getBytesData());
                count--;
            }
            cacheRecord.clear();
        }
        if (count==0){//不够默认数，继续查询
            return;
        }
        //再查硬盘 不查了 太耗性能
    }

    private void sendOrderStampAndTime(DataInputStream in, DataOutputStream out) throws IOException {
        byte [] receive=new byte[32];
        in.read(receive);
        String key=byteToString(receive);
        if (!freshRecord.containsKey(key)){
            System.out.println("not exist");
            out.write(new byte[8]);
        }else {
            Record record = freshRecord.get(key);
            out.write(record.getOrderStamp());
            out.write(intToByte(getTime()));
        }
    }

    private void sendResult(OutputStream out) throws IOException {
        synchronized (effectiveRecord){
            out.write(intToByte(effectiveRecord.size()));
            Iterator<Record> iterator= effectiveRecord.iterator();
            while (iterator.hasNext()){
                Record record=iterator.next();
                out.write(record.getLockScript());
            }
        }
    }

    public void dealRecord(Record record, int ttl){
        if (verifyScriptRecord(record)){
            effectiveRecord.add(record);
            System.out.println("handle one   "+ effectiveRecord.size());
            //转发
            System.out.println(this.socket.getRemoteSocketAddress().toString().split(":")[0]);
            new BroadcastRecord(record,(this.socket.getRemoteSocketAddress().toString().split(":")[0]).substring(1),ttl).start();
        }
    }

    public void dealRegistRecord(Record record, int ttl){
        if (verifyScriptRecord(record)) {
            String key=byteToString(record.getLockScript());
            if (!freshRecord.containsKey(key)) {
                freshRecord.put(key, record);
                System.out.println("already not exist");
            }
            System.out.println(freshRecord.toString());
            System.out.println("register success start broadcast");
            //转发
            new BroadcastRecord(record,(this.socket.getRemoteSocketAddress().toString().split(":")[0]).substring(1),ttl,true).start();
        }
    }

    private boolean verifyScriptRecord(Record record) {
        MessageDigest digest= null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        //获得解锁脚本
        byte [] unLockScrpit=record.getUnLockScript();
        //获得锁定脚本
        byte [] LockScript=record.getLockScript();
        //验证公钥hash
        byte [] tem=new byte[40];
        System.arraycopy(unLockScrpit,0,tem,0,40);
        byte [] temHash=digest.digest(tem);
        if (!Arrays.equals(temHash,LockScript))
            return false;
        //验证签名
        tem=new byte[14];
        byte [] mac=record.getMac();
        byte [] orderStamp=record.getOrderStamp();
        byte [] time=record.getTime();
        byte [] verifingSign=new byte[unLockScrpit.length-40];
        byte [] x=new byte[20];
        byte [] y=new byte[20];
        System.arraycopy(unLockScrpit,0,x,0,20);
        System.arraycopy(unLockScrpit,20,y,0,20);
        System.arraycopy(unLockScrpit,40,verifingSign,0,verifingSign.length);
        System.arraycopy(mac,0,tem,0,6);
        System.arraycopy(orderStamp,0,tem,6,4);
        System.arraycopy(time,0,tem,10,4);
        temHash= digest.digest(tem);
        boolean result=false;
        try {
            ECPublicKeyImpl publicKey = new ECPublicKeyImpl(new ECPoint(new BigInteger(1, x), new BigInteger(1, y)), ECC.spec);
            Signature s = Signature.getInstance("SHA1withECDSA", "SunEC");
            s.initVerify(publicKey);
            s.update(temHash);
            result= s.verify(verifingSign);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return result;
    }

}
