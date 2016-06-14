package logTailer;

import fileMonitor.util.InodeUtil;
import scala.tools.nsc.Global;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 2016/6/13.
 */
public class DirectoryMonitor implements Runnable {
    private WatchService watchService;

    private String monitor_dir;

    private MetaData meta_Data;

    public DirectoryMonitor(String monitorDir, MetaData metaData) {
        monitor_dir = monitorDir;
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Paths.get(monitor_dir).register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE);
            meta_Data = metaData;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        WatchKey key = null;
        try {
            key = watchService.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (WatchEvent<?> event : key.pollEvents()) {
            System.out.println(event.context() + "发生了" + event.kind() + "事件");
            List<String> inodes = getInodes();
            if (inodes != null && inodes.size() > 0)
                meta_Data.writeFile(inodes);
        }
    }

    private List<String> getInodes() {
        ArrayList<String> list = null;
        File file = new File(monitor_dir);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            list = new ArrayList<>();
            for (File f : files) {
                long inode = InodeUtil.getInode(f.getAbsolutePath());
                list.add(String.valueOf(inode));
            }
        }
        return list;
    }

    public static void main(String[] args) {
        new Thread(new DirectoryMonitor("d:\\", new MetaData("d:\\123456.txt"))).start();
    }
}
