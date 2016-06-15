package logTailer;

import java.io.File;
import java.util.List;

/**
 * Created by Admin on 2016/6/13.
 */
public class FileTailer {
    public static void main(String[] args) {
        String monitor_file = args[0];
        String meta_file = args[1];
        String meta_dir = args[2];
        String monitor_dir = args[3];

        MetaData metaDataFile = new MetaData(meta_file);
        MetaData metaDataDir = new MetaData(meta_dir);

        DirectoryMonitor directoryMonitor = new DirectoryMonitor(monitor_dir, metaDataDir);

        Restore restore = new Restore(metaDataFile, metaDataDir, monitor_file);
        long restore_postion = restore.restore();
        long position = 0;
        if (restore_postion > -1)
            position = restore_postion;

        TailerListener1 tailerListener1 = new TailerListener1();

        File file = new File(monitor_file);
        Tailer tailer = TailerHelper.createTailer(file, tailerListener1, position, metaDataFile);

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
