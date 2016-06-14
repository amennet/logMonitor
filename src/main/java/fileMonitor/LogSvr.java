package fileMonitor;

import java.io.File;
import java.io.FileWriter;  
import java.io.IOException;  
import java.io.Writer;  
import java.text.SimpleDateFormat;  
import java.util.Date;  
import java.util.concurrent.Executors;  
import java.util.concurrent.ScheduledExecutorService;  
import java.util.concurrent.TimeUnit;  
/** 
 *<p>title: 日志服务器</p> 
 *<p>Description: 模拟日志服务器</p> 
 *<p>CopyRight: CopyRight (c) 2010</p> 
 *<p>Company: 99bill.com</p> 
 *<p>Create date: 2010-6-18</P> 
 *@author Tank Zhang<tank.zhang@99bill.com> 
 *@version v0.1 2010-6-18 
 */  
public class LogSvr {  

    private static int a1 = 0;

    private SimpleDateFormat dateFormat =   
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
  
    /** 
     * 将信息记录到日志文件 
     * @param logFile 日志文件 
     * @param mesInfo 信息 
     * @throws IOException  
     */  
    public void logMsg(File logFile,String mesInfo) throws IOException{  
        if(logFile == null) {  
            throw new IllegalStateException("logFile can not be null!");  
        }  
        Writer txtWriter = new FileWriter(logFile,true);  
        txtWriter.write(dateFormat.format(new Date()) +"\t"+mesInfo+"\n");  
        txtWriter.flush();
        txtWriter.close();
    }  
      
    public static void main(String[] args) throws Exception{  


        final LogSvr logSvr = new LogSvr();  

        //启动一个线程每5秒钟向日志文件写一次数据  
        ScheduledExecutorService exec =   
            Executors.newScheduledThreadPool(1);  
        exec.scheduleWithFixedDelay(new Runnable(){  
            public void run() {
                File tmpLogFile = new File("D:\\mock.log");
                if(!tmpLogFile.exists()) {
                    try {
                        tmpLogFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    for (int i = 0; i<10; i++)
                        logSvr.logMsg(tmpLogFile, " 99bill test !" + a1++);

                } catch (IOException e) {  
                    throw new RuntimeException(e);  
                }
            }
        }, 0, 5, TimeUnit.SECONDS);  
    }  
}  