package joy.aksd.coreThread;

import joy.aksd.ECC.ECC;
import joy.aksd.data.Record;
import sun.security.ec.ECPrivateKeyImpl;
import sun.security.ec.ECPublicKeyImpl;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.*;
import java.security.spec.ECPoint;
import java.util.Arrays;
import java.util.Enumeration;

import static joy.aksd.data.dataInfo.*;
import static joy.aksd.data.protocolInfo.QUERYSTAMPANDTIME;
import static joy.aksd.data.protocolInfo.RECIVERECORD;
import static joy.aksd.tools.toByte.hexStringToByteArray;
import static joy.aksd.tools.toByte.intTo3Byte;
import static joy.aksd.tools.toByte.intToByte;
import static joy.aksd.tools.toInt.byteToInt;
import static joy.aksd.tools.toString.byteToString;

/**
 * Created by EnjoyD on 2017/4/25.
 */
public class CreatRecord {
    public void start(int i,String path) {

        System.out.println("--------------------------");
        //找到自己的公私秘钥
        ECPublicKeyImpl publicKey = null;
        ECPrivateKeyImpl privateKey = null;
        try {
            KeyPair keyPair = getKeyPair(path);
            publicKey = (ECPublicKeyImpl) keyPair.getPublic();
            privateKey = (ECPrivateKeyImpl) keyPair.getPrivate();
        } catch (Exception e) {
            System.out.println("读取秘钥对失败");
            return;
        }
        //获取mac地址
        byte[] macAddress = getMacAddress();
        if (macAddress == null) {
            System.out.println("mac 获取错误");
            macAddress=new byte[6];
        }
        System.out.println("mac is:"+byteToString(macAddress));
        //生成锁定脚本
        byte[] lockScript = new byte[0];
        try {
            lockScript = getLockScript(publicKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        System.out.println("32 lockScript is:" + lockScript.length+byteToString(lockScript));
        //向全节点根据pubkey查找顺序戳
        byte[] orderStampAndTime;
        try {
            orderStampAndTime = getOrderStampAndTime(lockScript);
        } catch (IOException e) {
            System.out.println("error in order stamp or time");
            return;
        }
        byte state= (byte) i;
        byte []orderStamp=new byte[3];
        System.arraycopy(orderStampAndTime,0,orderStamp,0,orderStamp.length);
        if (byteToInt(orderStamp)==0) {
            System.out.println("error in creat record,user not exist");
            return;
        }
        orderStamp=intTo3Byte(byteToInt(orderStamp)+1);
        System.out.println("3 orderStamp is:" +byteToInt(orderStamp));

        //获取时间节点
        byte []time=new byte[4];
        System.arraycopy(orderStampAndTime,orderStamp.length,time,0,time.length);
//        byte[] time = intToByte(getSystemTime());
        System.out.println("4 time is:"+byteToInt(time));
        if (Arrays.equals(time,new byte[4])) {
            System.out.println("not exist");
            return;
        }
        //生成解锁脚本
        byte[] unLockScript = null;
        try {
            unLockScript = getUnlockScript(publicKey, privateKey, macAddress,state, orderStamp, time);
            System.out.println("unlockScript is:"+byteToString(unLockScript));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //mac 锁定脚本 解锁脚本 顺序戳填充 生成一条传播记录
        Record record = new Record();
        record.setMac(macAddress);
        record.setTime(time);
        record.setState(state);
        record.setOrderStamp(orderStamp);
        record.setLockScript(lockScript);
        record.setUnLockScript(unLockScript);
        System.out.println("record info is:\n"+record);
        Socket socket = null;
        OutputStream out = null;
        InputStream in = null;
        try {
            socket = new Socket(ROOTIP, PORT);
            out = socket.getOutputStream();
            in = socket.getInputStream();
            out.write(RECIVERECORD);
            out.write(intToByte(TTL));
            out.write(record.getBytesData());
//            out.write(record.getMac());
//            out.write(record.getOrderStamp());
//            out.write(record.getTime());
//            out.write(record.getLockScript());
//            out.write(record.getUnLockScript());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        CreatRecord creatRecord = new CreatRecord();
        creatRecord.start(0,null);
    }

    private byte[] getOrderStampAndTime(byte[] lockScrpit) throws IOException {
        Socket socket=new Socket(ROOTIP,PORT);
        OutputStream out=socket.getOutputStream();
        InputStream in=socket.getInputStream();
        out.write(QUERYSTAMPANDTIME);
        out.write(lockScrpit);
        byte orderStamp[] = new byte[3];
        in.read(orderStamp);
        byte time[]=new byte[4];
        in.read(time);
        socket.close();
//        int tem = byteToInt(orderStamp);
//        if (tem == 0)
//            System.out.println("order Stamp 查询失败");
//        tem++;
        byte []orderAndTime=new byte[7];
        System.arraycopy(orderStamp,0,orderAndTime,0,orderStamp.length);
        System.arraycopy(time,0,orderAndTime,orderStamp.length,time.length);
        return orderAndTime;
    }

    private byte[] getUnlockScript(ECPublicKeyImpl publicKey, ECPrivateKeyImpl privateKey, byte[] macAddress,byte state, byte[] orderStamp, byte[] time) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] tem = new byte[macAddress.length +1+ orderStamp.length + time.length];
        System.arraycopy(macAddress, 0, tem, 0, macAddress.length);
        tem[macAddress.length]=state;
        System.arraycopy(orderStamp, 0, tem, macAddress.length+1, orderStamp.length);
        System.arraycopy(time, 0, tem, macAddress.length+1 + orderStamp.length, time.length);
        byte[] sha = MessageDigest.getInstance("SHA-256").digest(tem);
        Signature sign = Signature.getInstance("SHA1withECDSA", "SunEC");
        sign.initSign(privateKey);
        sign.update(sha);
        byte[] signature = sign.sign();
        byte[] x = hexStringToByteArray(String.format("%040x", publicKey.getW().getAffineX()));
        byte[] y = hexStringToByteArray(String.format("%040x", publicKey.getW().getAffineY()));
        byte[] finalResult = new byte[signature.length + x.length + y.length];
        System.arraycopy(x, 0, finalResult, 0, x.length);
        System.arraycopy(y, 0, finalResult, x.length, y.length);
        System.arraycopy(signature, 0, finalResult, x.length + y.length, signature.length);
        return finalResult;
    }

    private byte[] getLockScript(ECPublicKeyImpl publicKey) throws NoSuchAlgorithmException {
        byte[] x = hexStringToByteArray(String.format("%040x", publicKey.getW().getAffineX()));
        byte[] y = hexStringToByteArray(String.format("%040x", publicKey.getW().getAffineY()));
        byte[] result = new byte[x.length + y.length];
        System.arraycopy(x, 0, result, 0, x.length);
        System.arraycopy(y, 0, result, x.length, y.length);
        return MessageDigest.getInstance("SHA-256").digest(result);
    }

    private byte[] getMacAddress() {
        InetAddress ia;
        try {
            ia = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println("network error");
            return null;
        }
        byte mac[] = null;
        try {
            mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
        } catch (SocketException | NullPointerException e) {
            System.out.println("net is wrong or maybe is Linux operation system");
        }
        if (mac != null) {
            return mac;
        }

        Enumeration<NetworkInterface> ni;
        try {
            ni = NetworkInterface.getNetworkInterfaces();
            while (ni.hasMoreElements()) {
                NetworkInterface netI = ni.nextElement();
                byte[] bytes = netI.getHardwareAddress();
                if (netI.isUp() && bytes != null && bytes.length == 6) {
                    return bytes;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    private KeyPair getKeyPair(String path) throws IOException, InvalidKeyException {
        if (path==null)
            path="./key";
        DataInputStream in = new DataInputStream(new FileInputStream(path));
        String first = in.readUTF();
        String second = in.readUTF();
        String third = in.readUTF();
        System.out.println(first);
        in.close();
        ECPrivateKeyImpl privateKey = new ECPrivateKeyImpl(new BigInteger(first, 16),ECC.spec);
        ECPublicKeyImpl publicKey = new ECPublicKeyImpl(new ECPoint(new BigInteger(second, 16), new BigInteger(third, 16)), ECC.spec);
        KeyPair keyPair = new KeyPair(publicKey, privateKey);
        return keyPair;
    }

    public Record registRecord(ECPublicKeyImpl publicKey, ECPrivateKeyImpl privateKey) throws NoSuchAlgorithmException {
        //获取mac地址
        byte[] macAddress = getMacAddress();
//        System.out.println("set mac:"+byteToString(macAddress));
        byte state=0;
        //向全节点根据pubkey查找顺序戳
        byte[] orderStamp = new byte[3];
//        System.out.println("set order stamp:"+byteToString(orderStamp));
        //获取时间节点
        byte[] time = intToByte(getTime());
//        System.out.println("set time:"+byteToString(time));
        //生成锁定脚本
        byte[] lockScript = getLockScript(publicKey);
//        System.out.println("set lockScript:"+byteToString(lockScript));
        //生成解锁脚本
        byte[] unLockScript = null;
        try {
            unLockScript = getUnlockScript(publicKey, privateKey, macAddress,state, orderStamp, time);
//            System.out.println("set unlockScript:"+byteToString(unLockScript));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Record record = new Record();
        record.setMac(macAddress);
        record.setTime(time);
        record.setOrderStamp(orderStamp);
        record.setLockScript(lockScript);
        record.setUnLockScript(unLockScript);
        return record;

    }

}
