package fileMonitor.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 2016/6/13.
 */
public class InodeUtil {
    public static long getInode(String path) {
        long inode = -1;
        try {
            Path p = Paths.get(path);
            BasicFileAttributes basicFileAttributes = Files.readAttributes(p, BasicFileAttributes.class);
            Object str = basicFileAttributes.fileKey();
            String split = str.toString().split("=")[2];
            String inodeStr = split.substring(0, split.length() - 1);
            inode = Long.parseLong(inodeStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inode;
    }

    public static List<String> getInodes(String path) {
        ArrayList<String> list = null;
        File file = new File(path);
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
}
