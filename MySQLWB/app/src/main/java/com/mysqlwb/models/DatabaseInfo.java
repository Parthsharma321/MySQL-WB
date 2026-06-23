package com.mysqlwb.models;

public class DatabaseInfo {
    private String name;
    private long sizeBytes;
    private long lastModified;

    public DatabaseInfo(String name, long sizeBytes, long lastModified) {
        this.name = name;
        this.sizeBytes = sizeBytes;
        this.lastModified = lastModified;
    }

    public String getName() { return name; }
    public long getSizeBytes() { return sizeBytes; }
    public long getLastModified() { return lastModified; }

    public String getFormattedSize() {
        if (sizeBytes < 1024) return sizeBytes + " B";
        if (sizeBytes < 1024 * 1024) return String.format("%.1f KB", sizeBytes / 1024.0);
        return String.format("%.1f MB", sizeBytes / (1024.0 * 1024));
    }
}
