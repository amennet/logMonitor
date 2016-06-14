package logTailer;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangge on 2016/6/13.
 */
public class MetaData {

    private final static String default_meta_file = ".meta";

    private File file;
    private FileInputStream fileInputStream;
    private FileOutputStream fileOutputStream;

    public MetaData() {
        this(default_meta_file);
    }

    public MetaData(String meta_file) {
        file = new File(meta_file);
    }

    /**
     * get file path
     * @return
     */
    public String getPath() {
        if (file.exists())
            return file.getAbsolutePath();
        return null;
    }

    /**
     * 初始化,读取文件内容,如果为空,新建文件,如果不为空,返回值
     */
    public List<String> readFile() {
        try {
            if (file.exists()) {
                fileInputStream = new FileInputStream(file);
                List<String> lines = IOUtils.readLines(fileInputStream);
                if (lines != null && lines.size() == 3)
                    return lines;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    /**
     * read file not null
     */
    public List<String> readFileNotNull() {
        try {
            if (file.exists()) {
                fileInputStream = new FileInputStream(file);
                List<String> lines = IOUtils.readLines(fileInputStream);
                if (lines != null && lines.size() > 0)
                    return lines;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    /**
     * write to file
     *
     * @param inode
     * @param position
     */
    public void writeFile(String time, String inode, String position) {
        try {
            if (!file.exists())
                file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            ArrayList<String> list = new ArrayList<>();
            list.add(String.valueOf(time));
            list.add(String.valueOf(inode));
            list.add(String.valueOf(position));
            IOUtils.writeLines(list, "\n", fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * write to file
     *
     * @param inode
     * @param position
     */
    public void writeFile(String inode, String position) {
        writeFile(String.valueOf(System.currentTimeMillis()), inode, position);
    }

    /**
     * write to file
     *
     * @param inode
     * @param position
     */
    public void writeFile(long inode, long position) {
        writeFile(String.valueOf(System.currentTimeMillis()), String.valueOf(inode), String.valueOf(position));
    }

    /**
     * write to file
     *
     * @param list
     */
    public void writeFile(List<String> list) {
        try {
            if (!file.exists())
                file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            list.add(0, String.valueOf(System.currentTimeMillis()));
            IOUtils.writeLines(list, "\n", fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MetaData restore = new MetaData("d:\\123.txt");
        List<String> strings = restore.readFile();
        System.out.println(strings);

        restore.writeFile("A1", "B1");
        List<String> strings1 = restore.readFile();
        System.out.println(strings1);
    }

}
