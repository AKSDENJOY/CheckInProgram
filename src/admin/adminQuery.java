package admin;

import joy.aksd.data.Record;

import java.io.*;
import java.net.Socket;
import java.util.*;

import static joy.aksd.data.dataInfo.PORT;
import static joy.aksd.data.dataInfo.ROOTIP;
import static joy.aksd.data.protocolInfo.ADMINQUERY;
import static joy.aksd.tools.readAndPrintData.printRecord;
import static joy.aksd.tools.toInt.byteToInt;
import static joy.aksd.tools.toString.byteToString;

/** 管理员查询
 * Created by EnjoyD on 2017/5/25.
 */
public class adminQuery {
    private HashMap<String,String> lockSriptToName=new HashMap<>();

    public void start(){
        try {
            initName();
        } catch (IOException e) {
            System.out.println("init error");
        }
        Socket socket= null;
        ArrayList<Record> result=new ArrayList<>();
        try {
            socket = new Socket(ROOTIP,PORT);
            InputStream in=socket.getInputStream();
            OutputStream out=socket.getOutputStream();
            out.write(ADMINQUERY);
            try {
                while (true) {
                    byte[] tem = new byte[2];
                    int i=in.read(tem);
                    tem = new byte[byteToInt(tem)];
                    i=in.read(tem);
                    Record record=new Record(tem);
                    printRecord(record);
                    String key=byteToString(record.getLockScript());
                    System.out.println("lockScrpit: "+ key);
                    result.add(record);
                }
            }catch (Exception e){
                System.out.println("receive over");
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
                        printRecord(record);
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

    private void initName() throws IOException {
        DataInputStream in=new DataInputStream(new FileInputStream("./adminName"));
        while (true){
            try {
                String lockScript=in.readLine();//windows下可以正常使用，linux下未知
                String name=in.readLine();
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
