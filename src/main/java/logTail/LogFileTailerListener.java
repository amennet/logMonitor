package logTail;

public abstract interface LogFileTailerListener {
    public abstract void newLogFileLine(String line);      
}  