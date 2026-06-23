package com.mysqlwb.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mysqlwb.models.QueryHistory;

import java.util.ArrayList;
import java.util.List;

public class HistoryManager extends SQLiteOpenHelper {

    private static final String DB_NAME = "mysqlwb_history.db";
    private static final int DB_VERSION = 2;
    private static HistoryManager instance;

    public static synchronized HistoryManager getInstance(Context context) {
        if (instance == null) {
            instance = new HistoryManager(context.getApplicationContext());
        }
        return instance;
    }

    private HistoryManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE query_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "sql TEXT NOT NULL," +
                "database_name TEXT," +
                "timestamp INTEGER NOT NULL," +
                "success INTEGER NOT NULL DEFAULT 1," +
                "execution_time INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE saved_queries (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "sql TEXT NOT NULL," +
                "database_name TEXT," +
                "created_at INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE saved_queries ADD COLUMN database_name TEXT");
        }
    }

    public void addToHistory(String sql, String dbName, boolean success, long executionTime) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("sql", sql);
        cv.put("database_name", dbName);
        cv.put("timestamp", System.currentTimeMillis());
        cv.put("success", success ? 1 : 0);
        cv.put("execution_time", executionTime);
        db.insert("query_history", null, cv);

        // Keep only last 500 entries
        db.execSQL("DELETE FROM query_history WHERE id NOT IN " +
                "(SELECT id FROM query_history ORDER BY timestamp DESC LIMIT 500)");
    }

    public List<QueryHistory> getHistory(int limit, String dbName) {
        List<QueryHistory> list = new ArrayList<>();
        String query = "SELECT * FROM query_history ";
        String[] args = null;
        if (dbName != null) {
            query += "WHERE database_name = ? ";
            args = new String[]{dbName, String.valueOf(limit)};
        } else {
            args = new String[]{String.valueOf(limit)};
        }
        query += "ORDER BY timestamp DESC LIMIT ?";
        
        Cursor c = getReadableDatabase().rawQuery(query, args);
        while (c.moveToNext()) {
            list.add(new QueryHistory(
                    c.getLong(0), c.getString(1), c.getString(2),
                    c.getLong(3), c.getInt(4) == 1, c.getLong(5)));
        }
        c.close();
        return list;
    }

    public List<QueryHistory> searchHistory(String query) {
        List<QueryHistory> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM query_history WHERE sql LIKE ? ORDER BY timestamp DESC LIMIT 100",
                new String[]{"%" + query + "%"});
        while (c.moveToNext()) {
            list.add(new QueryHistory(
                    c.getLong(0), c.getString(1), c.getString(2),
                    c.getLong(3), c.getInt(4) == 1, c.getLong(5)));
        }
        c.close();
        return list;
    }

    public void clearHistory() {
        getWritableDatabase().execSQL("DELETE FROM query_history");
    }

    public boolean saveQuery(String name, String sql, String dbName) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("sql", sql);
        cv.put("database_name", dbName);
        cv.put("created_at", System.currentTimeMillis());
        return getWritableDatabase().insert("saved_queries", null, cv) != -1;
    }

    public List<QueryHistory> getSavedQueries(String dbName) {
        List<QueryHistory> list = new ArrayList<>();
        String query = "SELECT id, sql, name, created_at, 1, 0 FROM saved_queries ";
        String[] args = null;
        
        if (dbName != null) {
            query += "WHERE database_name = ? ";
            args = new String[]{dbName};
        }
        
        query += "ORDER BY created_at DESC";
        
        Cursor c = getReadableDatabase().rawQuery(query, args);
        while (c.moveToNext()) {
            list.add(new QueryHistory(c.getLong(0), c.getString(1), c.getString(2),
                    c.getLong(3), true, 0));
        }
        c.close();
        return list;
    }
}
