package joy.aksd.tools;

import joy.aksd.data.Block;
import joy.aksd.data.Record;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

import static joy.aksd.data.dataInfo.indexBlock;
import static joy.aksd.data.dataInfo.location;
import static joy.aksd.tools.toInt.byteToInt;

/**
 * Created by EnjoyD on 2017/5/15.
 */
public class readAndPrintData {
    public static Block readSpecificBlock(int i) throws IOException {
        RandomAccessFile randomAccessFile=new RandomAccessFile(location, "r");
        long location=indexBlock.get(i);
        randomAccessFile.seek(location);
        byte []count=new byte[2];
        randomAccessFile.read(count);
        count=new byte[byteToInt(count)];
        randomAccessFile.read(count);
        Block block=new Block(count);
        randomAccessFile.close();
        System.out.println(block);
        return block;
    }

    public static void printRecord(Record record){
        System.out.print(record);
        long time=(long)byteToInt(record.getTime());
        System.out.println("   time : "+new SimpleDateFormat("YYYY-MM-dd-EEEE HH:mm:ss").format(new Date(time*1000))+"  ");
    }

    public static void main(String[] args) {
        try {
            readSpecificBlock(39);
        } catch (IOException e) {
            System.out.println("error");
        }
    }
}
