package com.mysqlwb.models;

public class TableInfo {
    private String name;
    private int rowCount;

    public TableInfo(String name, int rowCount) {
        this.name = name;
        this.rowCount = rowCount;
    }

    public String getName() { return name; }
    public int getRowCount() { return rowCount; }
}
