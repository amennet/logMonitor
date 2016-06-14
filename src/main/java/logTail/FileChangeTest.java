package logTail;

import java.io.File;
import java.io.IOException;

/**
 * Created by Admin on 2016/6/8.
 */
public class FileChangeTest {
    public static void main(String[] args) throws InterruptedException, IOException {
        File file = new File("/home/hadoop/e");
        while (true) {
            if (file.exists()) {
                System.out.println("file exist");
            }
            else {
//                file.createNewFile();
                System.out.println("file not exist");
            }
            Thread.sleep(1000);
        }
    }


}

