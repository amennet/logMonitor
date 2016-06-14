package logTailer;

import fileMonitor.util.InodeUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 2016/6/13.
 */
public class Restore {

    private MetaData md_current;
    private MetaData md_history;

    public Restore(MetaData metaDatadCurrent, MetaData metaDatadhistory) {
        md_current = metaDatadCurrent;
        md_history = metaDatadhistory;
        restore();
    }

    private void restore() {
        List<String> history_list = md_history.readFileNotNull();
        if (history_list != null && history_list.size() > 0)
            restoreHistory();

        List<String> current_list = md_current.readFile();
        if (current_list != null && current_list.size() == 3)
            restoreCurrent();
    }

    private void restoreCurrent() {

    }


    private void restoreHistory() {
    }

    private List<String> getInodes(MetaData metaData) {
        ArrayList<String> list = null;
        File file = new File(metaData.getPath());
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
