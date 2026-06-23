package com.mysqlwb.models;

public class QueryHistory {
    private long id;
    private String sql;
    private String database;
    private long timestamp;
    private boolean success;
    private long executionTime;

    public QueryHistory(long id, String sql, String database, long timestamp, boolean success, long executionTime) {
        this.id = id;
        this.sql = sql;
        this.database = database;
        this.timestamp = timestamp;
        this.success = success;
        this.executionTime = executionTime;
    }

    public long getId() { return id; }
    public String getSql() { return sql; }
    public String getDatabase() { return database; }
    public long getTimestamp() { return timestamp; }
    public boolean isSuccess() { return success; }
    public long getExecutionTime() { return executionTime; }
}
