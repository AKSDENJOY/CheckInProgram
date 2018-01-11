package admin;

import joy.aksd.coreThread.CreatRecord;
import joy.aksd.data.Record;
import sun.security.ec.ECPrivateKeyImpl;
import sun.security.ec.ECPublicKeyImpl;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Scanner;

import static joy.aksd.data.dataInfo.*;
import static joy.aksd.data.protocolInfo.REGISTER;
import static joy.aksd.tools.toByte.hexStringToByteArray;
import static joy.aksd.tools.toByte.intToByte;
import static joy.aksd.tools.toString.byteToString;

/**
 * Created by EnjoyD on 2017/5/3.
 */
public class registUser {
    public static void main(String[] args) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, IOException {
        KeyPairGenerator kpg;
        kpg=KeyPairGenerator.getInstance("EC","SunEC");
        ECGenParameterSpec ecsp;
        ecsp=new ECGenParameterSpec(ECNAME);
        kpg.initialize(ecsp);

        KeyPair keyPair=kpg.generateKeyPair();
        ECPrivateKeyImpl priKey= (ECPrivateKeyImpl) keyPair.getPrivate();
        ECPublicKeyImpl pubKey= (ECPublicKeyImpl) keyPair.getPublic();

        String privateKey=String.format("%040x",priKey.getS());
        String publicKeyX=String.format("%040x",pubKey.getW().getAffineX());
        String publicKeyY=String.format("%040x",pubKey.getW().getAffineY());

        DataOutputStream file=new DataOutputStream(new FileOutputStream("./key"));
        file.writeUTF(privateKey);
        file.writeUTF(publicKeyX);
        file.writeUTF(publicKeyY);
        file.close();
        System.out.println("saved");
        Record record=new CreatRecord().registRecord(pubKey,priKey);
        Socket socket=new Socket(ROOTIP,PORT);
        OutputStream out=socket.getOutputStream();
        out.write(REGISTER);
        out.write(intToByte(TTL));
        out.write(record.getBytesData());
        out.close();
        System.out.println("regist phrase1 over");

        System.out.println("please enter the name!");
        Scanner sc=new Scanner(System.in);
        String name=sc.nextLine();
        sc.close();

        //save copyOFLockSrcipt
        file=new DataOutputStream(new FileOutputStream("./adminName",true));
        file.writeBytes(byteToString(getLockScript(pubKey))+"\n");//windows 下的换行符，如果在linux下可能需要改变
        file.writeBytes(name+"\n");
        file.close();
        System.out.println("regist phrase2 over");

    }
    private static byte[] getLockScript(ECPublicKeyImpl publicKey) throws NoSuchAlgorithmException {

        byte[] y = hexStringToByteArray(String.format("%040x", publicKey.getW().getAffineY()));
        byte[] x = hexStringToByteArray(String.format("%040x", publicKey.getW().getAffineX()));
        byte[] result = new byte[x.length + y.length];
        System.arraycopy(x, 0, result, 0, x.length);
        System.arraycopy(y, 0, result, x.length, y.length);
        return MessageDigest.getInstance("SHA-256").digest(result);
    }
}
