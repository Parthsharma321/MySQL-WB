package com.mysqlwb.models;

import java.util.List;

public class QueryResult {
    public enum Type { TABLE, SUCCESS, ERROR }

    private Type type;
    private String[] columns;
    private List<String[]> rows;
    private String message;
    private long executionTime;
    private int affectedRows;

    private QueryResult(Type type) {
        this.type = type;
    }

    public static QueryResult table(String[] columns, List<String[]> rows, String message, long executionTime) {
        QueryResult r = new QueryResult(Type.TABLE);
        r.columns = columns;
        r.rows = rows;
        r.message = message;
        r.executionTime = executionTime;
        return r;
    }

    public static QueryResult success(String message) {
        QueryResult r = new QueryResult(Type.SUCCESS);
        r.message = message;
        return r;
    }

    public static QueryResult error(String message) {
        QueryResult r = new QueryResult(Type.ERROR);
        r.message = message;
        return r;
    }

    public Type getType() { return type; }
    public String[] getColumns() { return columns; }
    public List<String[]> getRows() { return rows; }
    public String getMessage() { return message; }
    public long getExecutionTime() { return executionTime; }
    public int getAffectedRows() { return affectedRows; }

    public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
    public void setAffectedRows(int affectedRows) { this.affectedRows = affectedRows; }

    public boolean isSuccess() { return type == Type.SUCCESS || type == Type.TABLE; }
    public boolean isError() { return type == Type.ERROR; }
    public boolean hasTable() { return type == Type.TABLE && columns != null; }

    public int getRowCount() { return rows != null ? rows.size() : 0; }
}
