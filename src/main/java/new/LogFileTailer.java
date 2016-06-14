//public class LogFileTailer extends Thread {
//    private Logger logger = LoggerFactory.getLogger(LogFileTailer.class);
//
//    /**
//     * How frequently to check for file changes; defaults to 5 seconds
//     */
//    private long sampleInterval = 5000;
//
//    /**
//     * The log file to tail
//     */
//    private File logfile;
//
//    /**
//     * Defines whether the log file tailer should include the entire contents of
//     * the exising log file or tail from the end of the file when the tailer
//     * starts
//     */
//    private boolean startAtBeginning = false;
//
//    /**
//     * Is the tailer currently tailing?
//     */
//    private boolean tailing = false;
//
//    /**
//     * Set of listeners
//     */
//    private Set listeners = new HashSet();
//
//    /**
//     * Creates a new log file tailer that tails an existing file and checks the
//     * file for updates every 5000ms
//     */
//    public LogFileTailer(File file) {
//        this.logfile = file;
//    }
//
//    /**
//     * Creates a new log file tailer
//     *
//     * @param file             The file to tail
//     * @param sampleInterval   How often to check for updates to the log file
//     *                         (default = 5000ms)
//     * @param startAtBeginning Should the tailer simply tail or should it
//     *                         process the entire file and continue tailing (true) or simply
//     *                         start tailing from the end of the file
//     */
//    public LogFileTailer(File file, long sampleInterval, boolean startAtBeginning) {
//        this.logfile = file;
//        this.sampleInterval = sampleInterval;
//    }
//
//    public void addLogFileTailerListener(LogFileTailerListener l) {
//        this.listeners.add(l);
//    }
//
//    public void removeLogFileTailerListener(LogFileTailerListener l) {
//        this.listeners.remove(l);
//    }
//
//    protected void fireNewLogFileLine(String line) {
//        for (Iterator i = this.listeners.iterator(); i.hasNext(); ) {
//            LogFileTailerListener l = (LogFileTailerListener) i.next();
//            l.newLogFileLine(line);
//        }
//    }
//
//    public void stopTailing() {
//        this.tailing = false;
//    }
//
//    public void run() {
//        long filePointer = 0;
//
//        if (this.startAtBeginning) {
//            filePointer = 0;
//        } else {
//            filePointer = this.logfile.length();
//        }
//
//        try {
//            this.tailing = true;
//            RandomAccessFile file = new RandomAccessFile(logfile, "r");
//            while (this.tailing) {
//                long fileLength = this.logfile.length();
//                if (fileLength < filePointer) {
//                    file = new RandomAccessFile(logfile, "r");
//                    filePointer = 0;
//                }
//                if (fileLength > filePointer) {
//                    file.seek(filePointer);
//                    String line = file.readLine();
//                    while (line != null) {
//                        this.fireNewLogFileLine(line);
//                        line = file.readLine();
//                    }
//                    filePointer = file.getFilePointer();
//                }
//                sleep(this.sampleInterval);
//            }
//            file.close();
//        } catch (IOException e) {
//            logger.error("IO ERROR:", e);
//        } catch (InterruptedException e) {
//            logger.error("InterruptedException:", e);
//        }
//
//    }
//}