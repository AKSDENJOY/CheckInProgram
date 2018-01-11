package joy.aksd.data;

/** 协议设置
 * Created by EnjoyD on 2017/5/16.
 */
public class protocolInfo {
    private protocolInfo(){}
    //监听线程
    public static final byte REGISTER=0x00;//新用户注册
    public static final byte RECIVERECORD=0x01;//收到记录
    public static final byte QUERYSTAMPANDTIME=0x03;
    public static final byte SELFQUERY=0x05;//查询个人记录
    public static final byte RECEIVEBLOCK=0x06;//收到区块
    public static final byte ADMINQUERY=0x07;//管理员查询
    public static final byte DOWNLOADBLOCK=0x08;//下载区块
    public static final byte GETIPLIST=0x09;//获取IPlist

    public static final byte LINKTEST=0x0f;//测试连接

    //下载区块文件时使用
//    public static final byte HAVEBLOCKFILE=0x10;//区块文件存在
//    public static final byte NOBLICKFILE=0x11;//区块文件不存在

}
