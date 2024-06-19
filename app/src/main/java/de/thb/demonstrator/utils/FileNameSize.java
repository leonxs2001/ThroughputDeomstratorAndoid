package de.thb.demonstrator.utils;

public class FileNameSize {
    private final String filename;
    private final int size;

    public FileNameSize(String filename, String size) {
        this.filename = filename;
        this.size = Integer.parseInt(size);
    }

    public String getFilename() {
        return filename;
    }

    public int getSize() {
        return size;
    }
}
