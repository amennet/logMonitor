//package fileMonitor;
//
//import org.apache.flume.Context;
//import org.apache.flume.EventDrivenSource;
//import org.apache.flume.channel.ChannelProcessor;
//import org.apache.flume.conf.Configurable;
//import org.apache.flume.instrumentation.SourceCounter;
//import org.apache.flume.source.AbstractSource;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.RandomAccessFile;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Future;
//
//public class TailingFileSource extends AbstractSource implements
//        EventDrivenSource, Configurable {
//
//
//    private static final Logger logger = LoggerFactory
//            .getLogger(TailingFileSource.class);
//
//
//    private SourceCounter sourceCounter;
//    private long restartThrottle;
//    private boolean restart;
//    private Integer bufferCount;
//    private long batchTimeout;
//    private ExecutorService executor;
//    private Future<?> runnerFuture;
//    private String readingFilePath;
//    private String historyFilePath;
//    private String offsetRecordFile;
//    private ChannelProcessor channelProcessor;
//    private ReadingFile reader;
//
//
//    @Override
//    public void configure(Context context) {
//// TODO Auto-generated method stub
//
//
//        restartThrottle = context
//                .getLong(
//                        TailingFileSourceConfigurationConstants.CONFIG_RESTART_THROTTLE,
//                        TailingFileSourceConfigurationConstants.DEFAULT_RESTART_THROTTLE);
//
//
//        restart = context.getBoolean(
//                TailingFileSourceConfigurationConstants.CONFIG_RESTART,
//                TailingFileSourceConfigurationConstants.DEFAULT_RESTART);
//
//
//        bufferCount = context.getInteger(
//                TailingFileSourceConfigurationConstants.CONFIG_BATCH_SIZE,
//                TailingFileSourceConfigurationConstants.DEFAULT_BATCH_SIZE);
//
//
//        batchTimeout = context.getLong(
//                TailingFileSourceConfigurationConstants.CONFIG_BATCH_TIME_OUT,
//                TailingFileSourceConfigurationConstants.DEFAULT_BATCH_TIME_OUT);
//
//
//        readingFilePath = context
//                .getString(TailingFileSourceConfigurationConstants.CONFIG_READING_FILE_PATH);
//
//
//        Preconditions.checkState(readingFilePath != null,
//                "The parameter readingFilePath must be specified");
//
//
//        historyFilePath = context
//                .getString(TailingFileSourceConfigurationConstants.CONFIG_HISTORY_FILE_PATH);
//
//
//        Preconditions.checkState(historyFilePath != null,
//                "The parameter historyFilePath must be specified");
//
//
//        offsetRecordFile = context
//                .getString(TailingFileSourceConfigurationConstants.CONFIG_OFFSET_RECORD_FILE);
//
//
//        Preconditions.checkState(offsetRecordFile != null,
//                "The parameter offsetRecordFile must be specified");
//
//
//        if (sourceCounter == null) {
//            sourceCounter = new SourceCounter(getName());
//        }
//    }
//
//
//    private static class ReadingFile implements Runnable {
//
//
//        public ReadingFile(String readingFilePath, int bufferCount,
//                           long batchTimeout, SourceCounter sourceCounter,
//                           String historyFilePath, String offsetRecordFile,
//                           ChannelProcessor channelProcessor, boolean restart,
//                           long restartThrottle) {
//            this.readingFilePath = readingFilePath;
//            this.bufferCount = bufferCount;
//            this.batchTimeout = batchTimeout;
//            this.sourceCounter = sourceCounter;
//            this.historyFilePath = historyFilePath;
//            this.offsetRecordFile = offsetRecordFile;
//            this.channelProcessor = channelProcessor;
//            this.restart = restart;
//            this.restartThrottle = restartThrottle;
//        }
//
//
//        private final String readingFilePath;
//        private OffsetRecord offsetRecord;
//        private final Integer bufferCount;
//        private final long batchTimeout;
//        private final SourceCounter sourceCounter;
//        private final String historyFilePath;
//        private final String offsetRecordFile;
//        private final ChannelProcessor channelProcessor;
//        private final long restartThrottle;
//        private volatile boolean restart;
//        private BufferedWriter offsetAccessFile = null;
//        private File offsetFile = null;
//        public volatile boolean readFlag = true;
//
//
//        /**
//         * tailf式读取文件中内容:
//         * offset == file.length，读到文件末尾，持续读取，等待新的数据写入；
//         * offset>file.length，可能文件做切换，offset置0，从头开始读取文件；
//         * 读取文件，达到batchsize或超时时提交数据到channel中
//         */
//        public void readFile() {
//            RandomAccessFile file = null;
//            do {
//                try {
//// file = new RandomAccessFile(readingFilePath, "r");
//                    File f = new File(readingFilePath);
//                    file = new BufferedRandomAcessFile(f, "r", 1024 * 1024 * 5);
//                    logger.info("offset record offset : {}",
//                            offsetRecord.offset);
//                    long offset = offsetRecord.offset;
//                    logger.info("file size : {} , offset : {}", file.length(),
//                            offset);
//                    boolean flag = true;
//                    long time = System.currentTimeMillis();
//                    List<Event> eventList = new ArrayList<Event>();
//                    while (readFlag) {
//                        if (offset == file.length()) {
//                            if (eventList.size() != 0) {
//                                flushEventBatch(eventList);
//                                eventList.clear();
//                                recordOffset(file.getFilePointer(),
//                                        f.lastModified());
//                            }
//                        } else if (offset > file.length()) {
//                            offset = 0;
//                        } else {
//                            file.seek(offset);
//                            if (eventList.size() < bufferCount) {
//                                eventList.add(EventBuilder.withBody(file
//                                        .readLine().getBytes()));
//                                sourceCounter.incrementEventReceivedCount();
//                            } else {
//                                eventList.add(EventBuilder.withBody(file
//                                        .readLine().getBytes()));
//                                flushEventBatch(eventList);
//                                eventList.clear();
//                                recordOffset(file.getFilePointer(),
//                                        f.lastModified());
//                                time = System.currentTimeMillis();
//                            }
//                            if (System.currentTimeMillis() - time > batchTimeout) {
//                                flushEventBatch(eventList);
//                                eventList.clear();
//                                recordOffset(file.getFilePointer(),
//                                        f.lastModified());
//                                time = System.currentTimeMillis();
//                            }
//                            offset = file.getFilePointer();
//                        }
//                        if (flag && !f.exists()) {
//                            flag = false;
//                        }
//                        if (!flag && f.exists()) {
//                            try {
//                                try {
//                                    if (file != null) {
//                                        file.close();
//                                    }
//                                } catch (IOException e) {
//// TODO Auto-generated catch block
//                                    logger.error(
//                                            "close reading file exception ", e);
//                                }
//                                file = new BufferedRandomAcessFile(f, "r",
//                                        1024 * 1024 * 5);
//                                flag = true;
//                                if (offset > file.length())
//                                    offset = 0;
//                            } catch (FileNotFoundException e) {
//                                try {
//                                    Thread.sleep(100);
//                                } catch (InterruptedException ee) {
//// TODO Auto-generated catch block
//                                    logger.error("sleep error", ee);
//                                }
//                                logger.error("reopen reading file error ", e);
//                            }
//                        }
//                    }
//                } catch (FileNotFoundException e) {
//// TODO Auto-generated catch block
//                    logger.error("open reading file error ", e);
//                } catch (IOException e) {
//// TODO Auto-generated catch block
//                    logger.error("read opening file error", e);
//                } finally {
//                    if (file != null) {
//                        try {
//                            file.close();
//                        } catch (IOException e) {
//// TODO Auto-generated catch block
//                            logger.error("close reading file error", e);
//                        }
//                    }
//                }
//                if (restart) {
//                    logger.info("Restarting in {}ms, exit code {}",
//                            restartThrottle);
//                    try {
//                        Thread.sleep(restartThrottle);
//                    } catch (InterruptedException e) {
//                        Thread.currentThread().interrupt();
//                    }
//                }
//
//
//            } while (restart);
//        }
//
//
//        @Override
//        public void run() {
//// TODO Auto-generated method stub
//            getOffsetRecord();
//            if (offsetRecord.recordTime != -1) {
//                Map<Long, String> unReadedFiles = getUnReadedFiles();
//                if (unReadedFiles.size() != 0) {
//                    dealHistoryFiles(unReadedFiles);
//                    offsetRecord.offset = 0;
//                }
//            }
//            readFile();
//        }
//
//
//        /**
//         * 判断是否有跳过文件，如有，未读文件信息放入map中
//         *
//         * @return
//         */
//        public Map<Long, String> getUnReadedFiles() {
//            File file = new File(historyFilePath);
//            Map<Long, String> unReadedFiles = new TreeMap<Long, String>();
//            if (file.isDirectory()) {
//                for (File f : file.listFiles()) {
//                    if (f.lastModified() >= offsetRecord.recordTime) {
//                        unReadedFiles
//                                .put(f.lastModified(), f.getAbsolutePath());
//                    }
//                }
//            }
//            return unReadedFiles;
//        }
//
//
//        /**
//         * 处理历史未读文件，map为treeMap,读文件时，第一个文件从记录的offset开始读
//         * 处理历史文件时，记录的offset的时间默认为当前所读的文件的最后修改时间
//         *
//         * @param unReadedFiles
//         */
//        public void dealHistoryFiles(Map<Long, String> unReadedFiles) {
//            boolean flag = true;
//            if (unReadedFiles.size() != 0) {
//                RandomAccessFile file = null;
//                long offset = 0;
//                List<Event> eventList = new ArrayList<Event>();
//                for (long time : unReadedFiles.keySet()) {
//                    try {
//                        File f = new File(unReadedFiles.get(time));
//                        file = new BufferedRandomAcessFile(f, "r",
//                                1024 * 1024 * 5);
//                        if (flag) {
//                            offset = offsetRecord.offset;
//                            flag = false;
//                        } else {
//                            offset = 0;
//                        }
//                        while (offset < file.length()) {
//                            file.seek(offset);
//                            if (eventList.size() < bufferCount) {
//                                eventList.add(EventBuilder.withBody(file
//                                        .readLine().getBytes()));
//                                sourceCounter.incrementEventReceivedCount();
//                            } else {
//                                flushEventBatch(eventList);
//                                eventList.clear();
//                                recordOffset(file.getFilePointer(),
//                                        f.lastModified());
//                            }
//                            offset = file.getFilePointer();
//                        }
//                        if (eventList.size() != 0) {
//                            flushEventBatch(eventList);
//                            eventList.clear();
//                        }
//                    } catch (FileNotFoundException e) {
//// TODO Auto-generated catch block
//                        logger.error("deal history file , file not found ", e);
//                    } catch (IOException e) {
//// TODO Auto-generated catch block
//                        logger.error("deal history file , read file error ", e);
//                    }
//                }
//            }
//        }
//
//
//        /**
//         * 获取文件的offset信息，包含两部分，offset值和记录时间
//         */
//        public void getOffsetRecord() {
//            if (offsetFile == null) {
//                offsetFile = new File(offsetRecordFile);
//            }
//            if (!offsetFile.exists())
//                offsetRecord = new OffsetRecord(-1, 0);
//            else {
//                BufferedReader reader = null;
//                try {
//                    reader = new BufferedReader(new FileReader(offsetFile));
//                    String line = new String();
//                    StringBuffer sb = new StringBuffer();
//                    while ((line = reader.readLine()) != null) {
//                        sb.append(line);
//                    }
//                    logger.info("offset info : {}", sb.toString());
//                    String[] record = sb.toString().split("\\|");
//                    offsetRecord = new OffsetRecord(Long.parseLong(record[0]
//                            .trim()), Long.parseLong(record[1].trim()));
//                } catch (Exception e) {
//// TODO Auto-generated catch block
//                    logger.error("read offset record error", e);
//                    offsetRecord = new OffsetRecord(-1, 0);
//                } finally {
//                    if (reader != null) {
//                        try {
//                            reader.close();
//                        } catch (IOException e) {
//// TODO Auto-generated catch block
//                            logger.error("reader close error", e);
//                        }
//                    }
//                }
//            }
//        }
//
//
//        /**
//         * 记录offset到文件中
//         *
//         * @param offset
//         * @param time
//         */
//        public void recordOffset(long offset, long time) {
//            if (offsetAccessFile == null) {
//                File file = new File(offsetRecordFile);
//                if (!file.exists()) {
//                    try {
//                        file.createNewFile();
//                    } catch (IOException e) {
//// TODO Auto-generated catch block
//                        logger.error("create offset Record File error", e);
//                    }
//                }
//                try {
//                    offsetAccessFile = new BufferedWriter(new FileWriter(file,
//                            false));
//                } catch (IOException e) {
//// TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//
//
//            }
//            try {
//                offsetAccessFile.write(time + "|" + offset);
//                offsetAccessFile.flush();
//                offsetAccessFile.close();
//                offsetAccessFile = null;
//            } catch (IOException e) {
//// TODO Auto-generated catch block
//                logger.error("write offset Record File error", e);
//                if (offsetAccessFile != null) {
//                    try {
//                        offsetAccessFile.close();
//                    } catch (IOException e1) {
//// TODO Auto-generated catch block
//                        logger.error("close offset access file error", e1);
//                    }
//                }
//            }
//        }
//
//
//        public void flushEventBatch(List<Event> eventList) {
//            channelProcessor.processEventBatch(eventList);
//            sourceCounter.addToEventAcceptedCount(eventList.size());
//        }
//
//        public void setRestart(boolean restart) {
//            this.restart = restart;
//        }
//
//
//        public void setReadFlag(boolean readFlag) {
//            this.readFlag = readFlag;
//        }
//
//
//        public class OffsetRecord {
//            public long recordTime;
//
//
//            public long offset;
//
//
//            public OffsetRecord(long time, long offset) {
//                this.recordTime = time;
//                this.offset = offset;
//            }
//        }
//    }
//
//
//    @Override
//    public synchronized void start() {
//
//
//        executor = Executors.newSingleThreadExecutor();
//        sourceCounter.start();
//        channelProcessor = getChannelProcessor();
//        reader = new ReadingFile(readingFilePath, bufferCount, batchTimeout,
//                sourceCounter, historyFilePath, offsetRecordFile,
//                channelProcessor, restart, restartThrottle);
//
//
//        runnerFuture = executor.submit(reader);
//
//
//        super.start();
//    }
//
//
//    @Override
//    public synchronized void stop() {
//        reader.setReadFlag(false);
//        reader.setRestart(false);
//
//
//        if (reader != null) {
//            if (reader.offsetAccessFile != null) {
//                try {
//                    reader.offsetAccessFile.close();
//                } catch (IOException e1) {
//// TODO Auto-generated catch block
//                    logger.error("close offset access file error", e1);
//                }
//            }
//        }
//
//
//        if (runnerFuture != null) {
//            logger.debug("Stopping exec runner");
//            runnerFuture.cancel(true);
//            logger.debug("Exec runner stopped");
//        }
//        executor.shutdown();
//
//
//        while (!executor.isTerminated()) {
//            logger.debug("Waiting for exec executor service to stop");
//            try {
//                executor.awaitTermination(500, TimeUnit.MILLISECONDS);
//            } catch (InterruptedException e) {
//                logger.debug("Interrupted while waiting for exec executor service "
//                        + "to stop. Just exiting.");
//                Thread.currentThread().interrupt();
//            }
//        }
//
//
//        sourceCounter.stop();
//
//
//        super.stop();
//    }
//
//
//}