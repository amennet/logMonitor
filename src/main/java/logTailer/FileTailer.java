package logTailer;

import java.io.File;
import java.util.List;

/**
 * Created by Admin on 2016/6/13.
 */
public class FileTailer {
    public static void main(String[] args) {
        String meta_file = args[0];
        String monitor_file = args[1];

        RestorPosition restorPosition = new RestorPosition(meta_file);
        long inode = -1;
        long position = 0;
//        if (restorPosition.hasRestore()) {
//            inode = restorPosition.getLastInode();
//            position = restorPosition.getLastPosition();
//        }

        TailerListener1 tailerListener1 = new TailerListener1();

        File file = new File(monitor_file);
        Tailer tailer = TailerHelper.createTailer(file, tailerListener1, 0, );

        Thread thread = new Thread(tailer);
        thread.start();


        try {
            while (true) {
                Thread.sleep(1000);
                List<String> resultList = tailerListener1.getResult();
                System.out.println(resultList);
            }
//            tailer.stop();
//            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
