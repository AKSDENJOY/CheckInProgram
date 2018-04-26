package admin;

import joy.aksd.data.Record;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

//import static joy.aksd.data.dataInfo.PORT;
//import static joy.aksd.data.dataInfo.ROOTIP;
import static joy.aksd.data.protocolInfo.ADMINQUERY;
import static joy.aksd.tools.readAndPrintData.printRecord;
import static joy.aksd.tools.toInt.byteToInt;
import static joy.aksd.tools.toString.byteToString;

/** 管理员查询
 * Created by EnjoyD on 2017/5/25.
 */
public class adminQuery {
    private HashMap<String,String> lockSriptToName=new HashMap<>();
    private HashMap<String,String> macToPlace=new HashMap<>();
    private static String ip;
    private static int port=49999;
    static {
        Scanner sc= null;
        try {
            sc = new Scanner(new File("./ip.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("error in set ip");
        }
        String tem=sc.nextLine().trim();
        sc.close();
        ip=tem;
        System.out.println(ip);
    }

    public void start(){
        try {
            initName();
            initPlace();
        } catch (IOException e) {
            System.out.println("init error");
            System.exit(0);
        }
        Socket socket= null;
        ArrayList<Record> result=new ArrayList<>();
        try {
            socket = new Socket(ip,port);
            InputStream in=socket.getInputStream();
            OutputStream out=socket.getOutputStream();
            out.write(ADMINQUERY);
            try {
                receiveRecord(result,in);
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("receive exception");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("search over");
        startClassifySearch(result);
    }

    private void receiveRecord(ArrayList<Record> result, InputStream in) throws IOException {
        byte []buff=new byte[1024];
        byte []tem;
        int AllLength=0;
        ArrayList<byte[]> Allbytes=new ArrayList<>();
        int count=0;
        while ((count=in.read(buff))!=-1){
            tem=new byte[count];
            System.arraycopy(buff,0,tem,0,tem.length);
            Allbytes.add(tem);
            AllLength+=count;
        }
        System.out.println("receive finish");
        byte []all=new byte[AllLength];
        count=0;
        for (byte[] item:Allbytes){
            System.arraycopy(item,0,all,count,item.length);
            count+=item.length;
        }
        count=0;
        byte []recordLength=new byte[2];
        byte []data;
        System.out.println("start parse received info");
        while (count<all.length){
            System.arraycopy(all,count,recordLength,0,recordLength.length);
            int length=byteToInt(recordLength);
            count+=recordLength.length;
            data=new byte[length];
            System.arraycopy(all,count,data,0,data.length);
            count+=data.length;
            Record r=new Record(data);
            result.add(r);
            System.out.println(r.toString());
        }
    }


    private void printItems(){
        System.out.println("======================");
        System.out.println(" 1 按人查询");
        System.out.println(" 2 按时间由后向前查询");
        System.out.println(" 0 结束");
        System.out.println("======================");
    }

    private void startClassifySearch(ArrayList<Record> result) {
        printItems();
        Scanner sc=new Scanner(System.in);
        while (sc.hasNext()){
            String input=sc.nextLine();
            if (input.length()!=1){
                System.out.println("illegal input,please retry");

            }
            else {
                try {
                    int i=Integer.parseInt(input);
                    if (dealClassifySearch(i,result))
                        return;
                }catch (NumberFormatException e){
                    System.out.println("illegal input,please retry");
                }
            }
            printItems();
        }
    }

    private boolean dealClassifySearch(int i, ArrayList<Record> result) {
        switch (i){
            case 0:
                return true;
            case 1:
                HashMap<String,ArrayList<Record>> peopleResult=new HashMap<>();
                Iterator<Record> it=result.iterator();
                while (it.hasNext()){
                    Record record=it.next();
                    String key=byteToString(record.getLockScript());
                    if (!peopleResult.containsKey(key)){
                        ArrayList<Record> temList=new ArrayList<>();
                        temList.add(record);
                        peopleResult.put(key,temList);
                    }
                    else {
                        peopleResult.get(key).add(record);
                    }
                }
                for (Map.Entry<String,ArrayList<Record>> entry:peopleResult.entrySet()){
                    String lockSript=entry.getKey();
                    String name=lockSript;
                    if (lockSriptToName.containsKey(lockSript)){
                        name=lockSriptToName.get(lockSript);
                    }
                    System.out.println(name+": 的信息");
                    for (Record record:entry.getValue()){
                        adminprintRecord(record,macToPlace);
                    }
                    System.out.println("-------------------------------------");
                }
                break;
            case 2:
                for (Record record:result){
                    printRecord(record);
                }
                break;
            default:
                throw new NumberFormatException();
        }
        return false;
    }

    private void adminprintRecord(Record record, HashMap<String, String> macToPlace) {
        System.out.print(record);
        long time=(long)byteToInt(record.getTime());
        System.out.print("时间 :"+new SimpleDateFormat("YYYY-MM-dd-EEEE HH:mm:ss").format(new Date(time*1000))+"  ");
        int state=record.getState();
        System.out.print("   记录类型:");
        if (state==0)
            System.out.print("注册");
        else if (state==1)
            System.out.print("签到");
        else if (state==2)
            System.out.print("签退");
        System.out.print("   生成电脑:"+byteToString(record.getMac()));
        System.out.println("  具体位置: "+macToPlace.get(byteToString(record.getMac())));
    }


    private void initPlace() throws FileNotFoundException {
        Scanner sc=new Scanner(new File("./place"));
        while (sc.hasNext()){
            String mac=sc.nextLine().trim();
            String place=sc.nextLine().trim();
            macToPlace.put(mac,place);
        }
        sc.close();
    }

    private void initName() throws IOException {
        Scanner in=new Scanner(new FileInputStream("./adminName"));
        while (true){
            try {
                String lockScript=in.nextLine();
                String name=in.nextLine();
                if (lockScript==null)
                    break;
                this.lockSriptToName.put(lockScript,name);
            }catch (Exception e){
                break;
            }
        }
    }

    public static void main(String[] args) {
        new adminQuery().start();
    }
}
