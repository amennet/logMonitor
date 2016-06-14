package fileMonitor;

import fileMonitor.util.InodeUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Admin on 2016/6/12.
 */
public class CurrentFile {
    private Map<Long, String> inode_filename;
    private Path last_inode;
    private String monitor_dir;
    private String monitor_file;

    public CurrentFile(String path) {
        inode_filename = new HashMap<>();
        monitor_dir = path;
//        new Thread(new DirMonitor(monitor_dir)).start();
    }

//    public File getFile() {
//        if (DirMonitor.DIR_CHANGE_FLAG) {
//
//        }
//    }


    public String getFilename(String inode) {
        return inode_filename.get(inode);
    }

    // 刷新监控目录下的文件列表,获取inode
    public void refresh_files() throws IOException {
        inode_filename.clear();
        File directory = new File(monitor_dir);
        if (directory.exists() && directory.isDirectory()) {
            File[] fileList = directory.listFiles();
            for (File file : fileList) {
                long inode = InodeUtil.getInode(file.getAbsolutePath());
                inode_filename.put(inode, file.getName());
            }
        }
    }

    public static void main(String[] args) throws IOException {
        CurrentFile currentFile = new CurrentFile("/home/hadoop/");
        currentFile.refresh_files();
    }
}

