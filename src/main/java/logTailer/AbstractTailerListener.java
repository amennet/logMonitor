package logTailer;

import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangge on 2016/6/13.
 */
abstract class AbstractTailerListener implements TailerListener {
    @Override
    public void init(Tailer tailer) {
    }

    @Override
    public void stop() {
    }

    @Override
    public void fileNotFound() {
    }

    @Override
    public void handle(Exception ex) {
    }
}

class TailerListener1 extends AbstractTailerListener {

    private List<String> resultList = new ArrayList<String>();

    public List<String> getResult() {
        return resultList;
    }

    @Override
    public void handle(String line, long position, long lastModified) {
        resultList.add(line);
    }

    @Override
    public void fileRotated() {
    }
}

class TailerListener2 extends AbstractTailerListener {
    private List<String> resultList = new ArrayList<String>();
    private boolean newFile = false;

    private volatile boolean readDone = false;

    public List<String> getResult() {
        return resultList;
    }

    public boolean isNewFile() {
        return newFile;
    }

    public boolean isReadDone() {
        return readDone;
    }

    @Override
    public void handle(String line, long position, long lastModified) {
        resultList.add(line);

        if (resultList.size() == 100) {
            readDone = true;
        }
    }

    @Override
    public void fileRotated() {
        newFile = true;
    }
}

