package fileMonitor;

import java.io.IOException;
import java.nio.file.*;

public class EventMonitor implements Runnable {
    private WatchService watchService;

    public static boolean event_flag;

    public EventMonitor(String path) {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Paths.get(path).register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE);
            event_flag = false;  //目录没有发生改变
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
            event_flag = true;  //目录发生改变
        }
    }
}
