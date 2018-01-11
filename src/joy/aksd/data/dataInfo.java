package joy.aksd.data;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by EnjoyD on 2017/4/18.
 */
public class dataInfo {
    private dataInfo(){}

    /**
     * 缓存区块
     */
    public static final LinkedList<Block> blocks= new LinkedList<>();
    /**
     * 验证完脚本后的纪录池
     */
    public static LinkedBlockingQueue<Record> effectiveRecord = new LinkedBlockingQueue<>();
    /**
     * 验证完时间与顺序戳后的未使用纪录池，类似于 未使用的UTXO
     */
    public static ConcurrentHashMap<String,Record> freshRecord =new ConcurrentHashMap<>();
    /**
     * 验证完成后的记录池，用以创建默克尔树。
     */
    public static ArrayList<Record> identifedRecord=new ArrayList<>();
    /**
     * 打包生成区块时保存的记录
     */
    public static ArrayList<Record> unPackageRecord=new ArrayList<>();

    /**
     * timeRecord  时间纪录，用以存储adjustCount个区块时间
     * adjustCount 需要多少个区块进行一次难度调整
     */
    public static ArrayList<Integer> timeRecord=new ArrayList<>();
    public static int adjustCount=10;
    /**
     * 区块能包含的最大条目数
     */
    public static int merkleTreeLimitation=64;

    public static MessageDigest SHA256x;

    /**
     *  exceptTime 目标时间，期望平均多长时间产生一个区块，proof of work中为定值。
     *  errorTime 时间误差，误差范围内时间不变
     */
//    public static final int exceptTime=1920;//单位：秒
    public static final int exceptTime=600;//单位：秒
    public static final int errorTime=50;//单位：秒
    public static final int TTL=5;
    /**
     * 区块存储位置
     */
    public static final String location="blockTest";
    /**
     * 区块索引
     */
    public static ArrayList<Long> indexBlock=new ArrayList<>();
    /**
     * 缓存区块数量
     */
    public static final int cacheBlockCount=1000;
    /**
     * 核心线程，用来进行pow 广播 写入硬盘的顺序执行，将其线程模块化用以同步block时进行
     */
    public static ExecutorService coreWork=Executors.newSingleThreadExecutor();
    /**
     * pow中断信号
     */
    public static boolean interupt =false;
    /**
     * 期待的块号
     */
    public static int num=0;

    public static final String ECNAME="secp160r1";

    public static final int PORT=49999;

    public static String ROOTIP="";
    public static final String IPLOCATION="ip.txt";
    public static String localIp;

    public static HashSet<String> IPList= new HashSet<>();
    static {
        try {
            setIP();
        } catch (IOException e) {
            System.out.println("IP设置error");
            System.exit(1);
        }
    }

    private static void setIP() throws IOException {
        BufferedReader reader=new BufferedReader(new FileReader(IPLOCATION));
        String IP=reader.readLine();
        ArrayList<String> ipList=new ArrayList<>();
        while (true){
            String temIP=reader.readLine();
            if (temIP==null)
                break;
            ipList.add(temIP);
        }
        reader.close();
        if (checkIP(IP))
            ROOTIP=IP;
        else
            throw new IOException();
        Iterator<String> it=ipList.iterator();
        while (it.hasNext()){
            String temIP=it.next();
            if (!checkIP(temIP))
                it.remove();
        }
        IPList.add(ROOTIP);
        IPList.addAll(ipList);
    }

    public static boolean checkIP(String ip) {
        return true;
    }


    public static int getUnixTime(){
        return (int) (System.currentTimeMillis()/1000);
    }
    public static int getNTPtime(){
        Date date=null;
        try {
            NTPUDPClient timeClient = new NTPUDPClient();
            String timeServerURL = "202.120.2.101";
            InetAddress timeServerAddress = InetAddress.getByName(timeServerURL);
            TimeInfo timeInfo = timeClient.getTime(timeServerAddress);
            TimeStamp timestamp = timeInfo.getMessage().getTransmitTimeStamp();
            date = timestamp.getDate();
        }catch (Exception e){
            System.out.println("get NTPtime error System shutDown");
            System.exit(1);
        }
        return (int) (date.getTime() / 1000);
    }

    public static void interuptCoreThread(){
        interupt=true;
    }
    public static void interuptReset(){
        interupt=false;
    }

}
