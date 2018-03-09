package joy.aksd.tools;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Created by EnjoyD on 2018/3/9.
 */
public class getNTP {
    private static int time;
    public static int start() throws FileNotFoundException, InterruptedException {
        time=-1;
        Scanner sc = new Scanner(new File("./ntpServer.txt"));
        ArrayList<String> iplists = new ArrayList<>();
        while (sc.hasNext()) {
            iplists.add(sc.nextLine().trim());
        }
        Thread t;
        for (String item : iplists) {
            t = new Thread(new timeThread(item));
            t.start();
            TimeUnit.SECONDS.sleep(1);
            if (time == -1)
                t.interrupt();
            else
                break;
        }
        return time;
    }


    static class timeThread implements Runnable{
        private String ip;
        timeThread(String item){
            this.ip=item;
        }
        @Override
        public void run() {
            int tem;
            try {
                tem=getNTPtime(this.ip);
            }catch (Exception e){
                return ;
            }
            if (time!=-1)
                return;
            time=tem;
        }
    }

    public static int getNTPtime(String ip) throws IOException {
        System.out.println("get ntp time");
        Date date;
        NTPUDPClient timeClient = new NTPUDPClient();
        InetAddress timeServerAddress = InetAddress.getByName(ip);
        TimeInfo timeInfo = timeClient.getTime(timeServerAddress);
        TimeStamp timestamp = timeInfo.getMessage().getTransmitTimeStamp();
        date = timestamp.getDate();
        return (int) (date.getTime() / 1000);
    }
}
