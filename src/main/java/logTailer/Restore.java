package logTailer;

import fileMonitor.util.InodeUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Admin on 2016/6/13.
 */
public class Restore {

    private MetaData md_current;
    private MetaData md_history;
    private String monitor_file;

    public Restore(MetaData metaDatadCurrent, MetaData metaDatadhistory, String monitorFile) {
        md_current = metaDatadCurrent;
        md_history = metaDatadhistory;
        monitor_file = monitorFile;
    }

    public long restore() {
        long position = -1;
        // 恢复目录
        List<String> history_list = md_history.readFileNotNull();
        if (history_list != null && history_list.size() > 0) {
            List<String> inodes = InodeUtil.getInodes(md_history.getPath());
           if (!listEqual(history_list, inodes)) {
               System.out.println("上次停止采集后,目录又产生新的文件,需要手工恢复.上次停止采集时目录文件inode为: " + history_list.toString());
           }
        }
        // 恢复文件
        List<String> current_list = md_current.readFile();
        if (current_list != null && current_list.size() == 3) {
            long inode = InodeUtil.getInode(monitor_file);
            if (inode == -1 || inode != Long.parseLong(current_list.get(1))) {
                System.out.println("待恢复文件不存在.待恢复文件信息为: " + current_list);
            } else {
                position = Long.parseLong(current_list.get(2));
            }
        }
        return position;
    }

    private void restoreHistory(List<String> history_list, List<String> current_list) {

    }

    private void restoreCurrent(List<String> history_list, List<String> current_list) {

    }

    private boolean listEqual(List<String> list1, List<String> list2) {
        return list1.containsAll(list2) && list2.containsAll(list1);
    }

}
