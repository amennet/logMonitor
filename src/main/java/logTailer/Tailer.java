package logTailer;


import fileMonitor.util.InodeUtil;

import java.io.*;

/**
 * Created by zhangge on 2016/6/12.
 */
public final class Tailer implements Runnable {
    /**
     * Open mode for RandomAccessFile.
     */
    private static final String RAF_MODE = "r";

    /**
     * buffer for read file
     */
    private final byte[] inbuf;

    /**
     * The file which will be tailed.
     */
    private final File file;

    /**
     * The amount of time to wait for the file to be updated.
     */
    private final long delayMillis;

    /**
     * The listener to notify of events when tailing.
     */
    private final TailerListener tailerListener;

    /**
     * The tailer will run as long as this value is true.
     */
    private volatile boolean run = true;

    /**
     * The tailer will pause as long as this value is true.
     */
    private volatile boolean pause = false;

    /**
     * The old inode of the file.
     */
    private long lastInode = -1;

    /**
     * Last position the tailer has read.
     */
    private long lastPosition = 0;

    private MetaData metaData;

    /**
     * Creates a Tailer for the given file, with a specified buffer size.
     *
     * @param file        the file to follow
     * @param listener    the TailerListener to use
     * @param position    position where tailer should start
     * @param delayMillis the delay between checks of the file for new content in
     *                    milliseconds
     * @param bufSize     buffer size
     */
    public Tailer(File file, TailerListener listener, long position, long delayMillis, int bufSize, MetaData metaData) {
        this.file = file;
        this.lastPosition = position;
        this.delayMillis = delayMillis;
        this.inbuf = new byte[bufSize];
        this.tailerListener = listener;
        this.tailerListener.init(this);
        this.metaData = metaData;
    }

    /**
     * Allows the tailer to complete its current loop and return.
     */
    public void stop() {
        this.run = false;
    }

    /**
     * Allows the tailer to complete its current loop and pause.
     */
    public void pause() {
        this.pause = true;
    }

    /**
     * Allows the tailer to continue its current loop.
     */
    public void resume() {
        this.pause = false;
    }

    /**
     * Return the file.
     *
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * Return the delay in milliseconds.
     *
     * @return the delay in milliseconds.
     */
    public long getDelay() {
        return delayMillis;
    }

    /**
     * Follows changes in the file, calling the FileListener's handle method
     * for each new line.
     */
    public void run() {
        RandomAccessFile reader = null;
        try {
            // Open the file
            while (run && reader == null) {
                try {
                    reader = new RandomAccessFile(file, RAF_MODE);
                } catch (FileNotFoundException e) {
                    tailerListener.fileNotFound();
                }

                if (reader == null) {
                    try {
                        Thread.sleep(delayMillis);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    // last modified and last position already set in
                    // constructor
                    reader.seek(lastPosition);
                    lastInode = InodeUtil.getInode(file.getAbsolutePath());
                }
            }

            metaData.writeFile(lastInode, lastPosition);

            while (run) {
                while (pause) {
                    Thread.sleep(delayMillis);
                }

                long inode = InodeUtil.getInode(file.getAbsolutePath());  //new file inode
                long size = reader.getChannel().size();  //old file length

                if (inode != lastInode) {
                    // new file created
                    if (size > lastPosition) {
                        // old file updated, read the update and discard the
                        // read position
                        readLines(reader);
                    }

                    // file was rotated
                    tailerListener.fileRotated();;

                    long length = file.length();
                    while (length == 0) {
                        // file does not exist or have nothing
                        try {
                            Thread.sleep(delayMillis);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        length = file.length();
                    }

                        /*
                         * make sure the file exist and have content, reopen the
                         * reader after rotation, ensure that the old file is
                         * closed iff we re-open it successfully
                         */
                    RandomAccessFile save = reader;
                    reader = null;

                    while (run && reader == null) {
                        try {
                            reader = new RandomAccessFile(file, RAF_MODE);
                        } catch (FileNotFoundException e) {
                            tailerListener.fileNotFound();
                        }

                        if (reader == null) {
                            try {
                                Thread.sleep(delayMillis);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    // use the old last modified time
                    lastPosition = 0;
                    lastInode = InodeUtil.getInode(file.getAbsolutePath());

                    metaData.writeFile(lastInode, lastPosition);

                    /*
                     * close old file explicitly rather than relying on GC
                     * picking up previous RAF
                     */
                    closeQuietly(save);
                    /*
                     * in this case we continue to use the previous reader
                     * and position values
                     */
                    tailerListener.fileNotFound();
                    continue;
                } else if (size > lastPosition) {
                    // old file changed, doesn't need to update lastInode
                    lastPosition = readLines(reader);
                } else {
                    tailerListener.fileNotFound();
                }

                metaData.writeFile(lastInode, lastPosition);

                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            tailerListener.stop();
        } catch (Exception e) {
            tailerListener.handle(e);
        } finally {
            closeQuietly(reader);
        }
    }

    /**
     * Read new lines.
     *
     * @param reader The file to read
     * @return The new position after the lines have been read
     * @throws IOException if an I/O error occurs.
     */
    protected long readLines(RandomAccessFile reader) throws IOException {
        StringBuilder sb = new StringBuilder();

        long pos = reader.getFilePointer();
        long rePos = pos;  // position to re-read

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int num;
        while (run && ((num = reader.read(inbuf)) != -1)) {
            for (int i = 0; i < num; i++) {
                byte ch = inbuf[i];
                switch (ch) {
                    case '\n':
                        sb.append(new String(out.toByteArray(), "utf-8"));
                        tailerListener.handle(sb.toString(), pos + i + 1, file.lastModified());
                        out.reset();
                        sb.setLength(0);
                        rePos = pos + i + 1;
                        break;
                    case '\r':
                        if (sb.length() != 0) {
                            sb.append(new String(out.toByteArray(), "utf-8"));
                            tailerListener.handle(sb.toString(), pos + i + 1, file.lastModified());
                            out.reset();
                            sb.setLength(0);
                        }
                        rePos = pos + i + 1;
                        break;
                    default:
                        out.write(ch);
                }
            }

            pos = reader.getFilePointer();
        }

        reader.seek(rePos); // Ensure we can re-read if necessary
        return rePos;
    }

    protected void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
