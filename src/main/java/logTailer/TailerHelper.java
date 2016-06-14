package logTailer;

import java.io.File;

/**
 * Helper for create proper tailer.
 *
 * Created by zhangge on 2016/6/12.
 */
public class TailerHelper {

    /**
     * Default check interval.
     */
    private static final int DEFAULT_DELAY_MILLIS = 1000;

    /**
     * Default buffer size for reading.
     */
    private static final int DEFAULT_BUFSIZE = 4096;
    
    private TailerHelper() {}

    /**
     * Creates a Tailer for the given file, starting from the beginning, with
     * the default delay of 100ms.
     * 
     * @param file
     *            The file to follow
     * @param tailerListener
     *            the tailerListener to use
     * @param metaData
     *            resume from break-point
     */
    public static Tailer createTailer(File file, TailerListener tailerListener, MetaData metaData) {
        return createTailer(file, tailerListener, 0, metaData);
    }

    /**
     * Creates a Tailer for the given file, starting from the target position,
     * with the default delay of 100ms.
     * 
     * @param file
     *            The file to follow
     * @param tailerListener
     *            the tailerListener to use
     * @param position
     *            position where tailer should start
     * @param metaData
     *            resume from break-point
     */
    public static Tailer createTailer(File file, TailerListener tailerListener, long position, MetaData metaData) {
        return createTailer(file, tailerListener, position, DEFAULT_DELAY_MILLIS, metaData);
    }

    /**
     * Creates a Tailer for the given file, starting from the beginning.
     * 
     * @param file
     *            the file to follow
     * @param tailerListener
     *            the tailerListener to use
     * @param position
     *            position where tailer should start
     * @param delayMillis
     *            the delay between checks of the file for new content in
     *            milliseconds
     * @param metaData
     *            resume from break-point
     */
    public static Tailer createTailer(File file, TailerListener tailerListener, long position, long delayMillis, MetaData metaData) {
        return createTailer(file, tailerListener, position, delayMillis, DEFAULT_BUFSIZE, metaData);
    }

    /**
     * Creates a Tailer for the given file, with a specified buffer size.
     * 
     * @param file
     *            the file to follow
     * @param tailerListener
     *            the tailerListener to use
     * @param position
     *            position where tailer should start
     * @param delayMillis
     *            the delay between checks of the file for new content in
     *            milliseconds
     * @param bufSize
     *            buffer size
     * @param metaData
     *            resume from break-point
     */
    public static Tailer createTailer(File file, TailerListener tailerListener, long position, long delayMillis, int bufSize, MetaData metaData) {
        return new Tailer(file, tailerListener, position, delayMillis, bufSize, metaData);
    }
}
