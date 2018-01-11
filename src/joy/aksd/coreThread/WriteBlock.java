package joy.aksd.coreThread;

import joy.aksd.data.Block;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import static joy.aksd.data.dataInfo.*;

/**
 * Created by EnjoyD on 2017/4/20.
 */
public class WriteBlock {
    private Block block;
    public WriteBlock(Block block){
        this.block=block;
    }
    public void start() throws FileNotFoundException {
        write();
    }

    private void write() throws FileNotFoundException {
        System.out.println("write");
        RandomAccessFile file=new RandomAccessFile(location,"rw");
        try {
            file.seek(file.length());
            indexBlock.add(file.length());
            file.write(block.getBlockDatas());
            file.close();
        }catch (IOException e) {
            System.out.println("error in writeBlock");
        }
    }
}
