package joy.aksd.listenAndVerifyThread;

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
                record = effectiveRecord.take();
            } catch (InterruptedException e) {
                return;
            }
            if (verifyStamp(record) && verifyTime(record)) {
                String key = byteToString(record.getLockScript());
                freshRecord.put(key, record);
                identifedRecord.add(record);
            }
        }
    }

    private boolean verifyTime(Record record) {//此处验证需要扩展，比如时间上后面的先于前面的到达，需要进行重新入队处理，目前仅做简单处理
        String key=byteToString(record.getLockScript());
        if (byteToInt(freshRecord.get(key).getTime())<byteToInt(record.getTime()))
            return true;
        return false;
    }
    private boolean verifyStamp(Record record) {//此处验证需要扩展，比如顺序上后面的先于前面的到达，需要进行重新入队处理
        String key=byteToString(record.getLockScript());
        if (byteToInt(freshRecord.get(key).getOrderStamp())+1==byteToInt(record.getOrderStamp()))
            return true;
        return false;
    }

}
