package com.mysqlwb.models;

public class ColumnInfo {
    private int cid;
    private String name;
    private String type;
    private boolean notNull;
    private String defaultValue;
    private boolean primaryKey;

    public ColumnInfo(int cid, String name, String type, boolean notNull, String defaultValue, boolean primaryKey) {
        this.cid = cid;
        this.name = name;
        this.type = type;
        this.notNull = notNull;
        this.defaultValue = defaultValue;
        this.primaryKey = primaryKey;
    }

    public int getCid() { return cid; }
    public String getName() { return name; }
    public String getType() { return type; }
    public boolean isNotNull() { return notNull; }
    public String getDefaultValue() { return defaultValue; }
    public boolean isPrimaryKey() { return primaryKey; }
}
