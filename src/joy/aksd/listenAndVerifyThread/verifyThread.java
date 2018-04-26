package joy.aksd.listenAndVerifyThread;

import joy.aksd.data.Block;
import joy.aksd.data.Record;

import static joy.aksd.data.dataInfo.*;
import static joy.aksd.tools.toInt.byteToInt;
import static joy.aksd.tools.toString.byteToString;

/** 验证线程
 * Created by EnjoyD on 2017/5/4.
 */
public class verifyThread extends Thread{
    @Override
    public void run() {
        while (true) {
            Record record;
            try {
                synchronized (effectiveRecord) {
                    record = effectiveRecord.take();
                }
            } catch (InterruptedException e) {
                return;
            }
            Block temBlock;
            synchronized (blocks){
                temBlock=blocks.getLast();
            }
            int recordTime=byteToInt(record.getTime());
            int timeNow=getTime();
            if (recordTime>timeNow+errorTime||recordTime<timeNow-errorTime){
                System.out.println("当前记录生成时间错误，丢弃");
                return;
            }
            if (verifyStamp(record) && verifyTime(record)) {
                String key = byteToString(record.getLockScript());
                freshRecord.put(key, record);
                synchronized (identifedRecord) {
                    identifedRecord.add(record);
                }
            }
            else {
                System.out.println("stamp or time error" );
            }
        }
    }

    private boolean verifyTime(Record record) {//此处验证需要扩展，比如时间上后面的先于前面的到达，需要进行重新入队处理，目前仅做简单处理
        if (byteToInt(record.getOrderStamp())==0)//注册消息
            return true;
        String key=byteToString(record.getLockScript());
        if (byteToInt(freshRecord.get(key).getTime())<byteToInt(record.getTime())) {
            return true;
        }
        else {
            System.out.println("当前记录时间小于等于末端记录时间阶段验证失败，丢弃");
            return false;
        }
    }
    private boolean verifyStamp(Record record) {//此处验证需要扩展，比如顺序上后面的先于前面的到达，需要进行重新入队处理
        if (byteToInt(record.getOrderStamp())==0)//注册消息
            return true;
        String key=byteToString(record.getLockScript());
        if (byteToInt(freshRecord.get(key).getOrderStamp())+1==byteToInt(record.getOrderStamp())) {
            return true;
        }
        else {
            System.out.println("当前记录顺序戳小于等于末端记录顺序戳验证阶段失败，丢弃");
            return false;
        }
    }

}
