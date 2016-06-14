package logTailer;


import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 2016/6/13.
 */
public class RestorPosition {
    private final static String default_meta_file = ".meta";
    private final static String default_time = "0", default_inode = "-1", default_position = "-1";
    private List<String> fileData;  //0 time, 1 lastInode, 2 lastPosition

    private File meta_file;
    private FileInputStream fileInputStream;
    private FileOutputStream fileOutputStream;

    public RestorPosition() {
        this(default_meta_file);
    }

    /**
     * 构造恢复文件
     *
     * @param meta_file_path
     */
    public RestorPosition(String meta_file_path) {
        init(meta_file_path);
    }

    /**
     * 初始化,读取文件内容,如果为空,新建文件,如果不为空,返回值
     */
    private void init(String meta_file_path) {
        meta_file = new File(meta_file_path);
        try {
            if (meta_file.exists()) {
                fileInputStream = new FileInputStream(meta_file);
                fileOutputStream = new FileOutputStream(meta_file);
                if (validateFile())
                    resetFile();
            } else {
               resetFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取文件内容
     */
    private void readFile() {
        try {
            fileData = IOUtils.readLines(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * return last version restorData
     *
     * @return
     */
    public List<String> getLastVersion() {
        readFile();
        if (fileData.get(0).equals("0"))
            return null;
        return fileData;
    }

    /**
     * write to file
     * @param inode
     * @param position
     */
    private void writeFile(String inode, String position) {
        writeFile(String.valueOf(System.currentTimeMillis()), inode, position);
    }

    /**
     * write to file
     *
     * @param inode
     * @param position
     */
    private void writeFile(String time, String inode, String position) {
        try {
            List<String> storeList = new ArrayList<>();
            storeList.add(String.valueOf(time));
            storeList.add(String.valueOf(inode));
            storeList.add(String.valueOf(position));

            IOUtils.writeLines(storeList, "\n", fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setInodeAndPosition(long inode, long position) {
        writeFile(String.valueOf(inode), String.valueOf(position));
    }

    /**
     * 校验文件是否正确
     * @return
     */
    private boolean validateFile() {
        List<String> lines = null;
        try {
            lines = IOUtils.readLines(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (lines != null && lines.size() == 3)
            return true;
        return false;
    }

    /**
     * 重置文件内容
     */
    private void resetFile() {
        try {
            meta_file.delete();
            meta_file.createNewFile();
            fileInputStream = new FileInputStream(meta_file);
            fileOutputStream = new FileOutputStream(meta_file);
            writeFile(default_time, default_inode, default_position);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        RestorPosition restorPosition = new RestorPosition("d:\\abc.txt");
        restorPosition.readFile();
        System.out.println(restorPosition.getLastVersion());
        restorPosition.writeFile("1", "2");
        restorPosition.readFile();
        System.out.println(restorPosition.getLastVersion());
    }

}
