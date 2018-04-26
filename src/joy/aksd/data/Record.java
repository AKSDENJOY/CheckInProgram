package joy.aksd.data;

import java.io.Serializable;

import static joy.aksd.tools.toByte.intToByte;
import static joy.aksd.tools.toInt.byteToInt;
import static joy.aksd.tools.toString.byteToString;

/**
 * Created by EnjoyD on 2017/4/18.
 */
public class Record implements Serializable{
    private byte []mac;//6字节
    private byte state;//1字节
    private byte []orderStamp;//3字节
    private byte []time;//4
    private byte []lockScript;//32
    private byte []unLockScript;//80-100

    public Record(){};

    public Record(byte []bytes){
        byte tem[]=new byte[6];
        System.arraycopy(bytes,0,tem,0,6);
        setMac(tem);
        tem=new byte[1];
        System.arraycopy(bytes,6,tem,0,1);
        setState(tem[0]);
        tem=new byte[3];
        System.arraycopy(bytes,7,tem,0,3);
        setOrderStamp(tem);
        tem=new byte[4];
        System.arraycopy(bytes,6+3+1,tem,0,4);
        setTime(tem);
        tem=new byte[32];
        System.arraycopy(bytes,6+3+1+4,tem,0,32);
        setLockScript(tem);
        tem=new byte[bytes.length-6-3-1-4-32];
        System.arraycopy(bytes,6+3+1+4+32,tem,0,tem.length);
        setUnLockScript(tem);
    }


    public byte[] getLockScript() {
        return lockScript;
    }

    public void setLockScript(byte[] lockScript) {
        this.lockScript = lockScript;
    }

    public byte[] getUnLockScript() {
        return unLockScript;
    }

    public void setUnLockScript(byte[] unLockScript) {
        this.unLockScript = unLockScript;
    }

    public byte[] getMac() {
        return mac;
    }

    public void setMac(byte[] mac) {
        this.mac = mac;
    }

    public byte[] getOrderStamp() {
        return orderStamp;
    }

    public void setOrderStamp(byte[] orderStamp) {
        this.orderStamp = orderStamp;
    }

    public byte[] getTime() {
        return time;
    }

    public void setTime(byte[] time) {
        this.time = time;
    }

    public byte [] getBytesData(){
        int i=mac.length+1+orderStamp.length+time.length+lockScript.length+unLockScript.length;
        byte result[]=new byte[2+i];
        byte tem[]=intToByte(i);
        System.arraycopy(tem,2,result,0,2);
        System.arraycopy(mac,0,result,2,mac.length);
        result[2+mac.length]=state;
        System.arraycopy(orderStamp,0,result,2+mac.length+1,orderStamp.length);
        System.arraycopy(time,0,result,2+mac.length+1+orderStamp.length,time.length);
        System.arraycopy(lockScript,0,result,2+mac.length+1+orderStamp.length+time.length,lockScript.length);
        System.arraycopy(unLockScript,0,result,2+mac.length+1+orderStamp.length+time.length+lockScript.length,unLockScript.length);
        return result;
    }

    @Override
    public String toString() {
        return "Record{" +
                "mac=" + byteToString(mac) +
                ", orderStamp=" + byteToInt(orderStamp) +
                ", time=" + byteToInt(time) +
                ",state=" +state+
                '}';
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }
}
