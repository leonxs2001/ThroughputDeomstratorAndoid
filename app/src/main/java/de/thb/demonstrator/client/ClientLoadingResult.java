package de.thb.demonstrator.client;

public class ClientLoadingResult {

    private final long milliseconds;
    private final long seconds;
    private final long minutes;
    private final String path;
    private final String filename;

    public ClientLoadingResult(long milliseconds, String path, String filename){
        this.path = path;
        this.filename = filename;

        long seconds = (long)(milliseconds / 1000.0);
        this.milliseconds =  milliseconds - seconds * 1000;
        this.minutes = seconds / 60;
        this.seconds = seconds - this.minutes * 60;
    }

    public ClientLoadingResult(long milliseconds){
        this(milliseconds, null, null);
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    public long getSeconds() {
        return seconds;
    }

    public long getMinutes() {
        return minutes;
    }

    public String getPath() {
        return path;
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public String toString() {
        return "ExecutionTime{" +
                "milliseconds=" + milliseconds +
                ", seconds=" + seconds +
                ", minutes=" + minutes +
                '}';
    }
}
