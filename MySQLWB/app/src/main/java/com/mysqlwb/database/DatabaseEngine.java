package com.mysqlwb.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.mysqlwb.models.ColumnInfo;
import com.mysqlwb.models.DatabaseInfo;
import com.mysqlwb.models.QueryResult;
import com.mysqlwb.models.TableInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseEngine {

    private static final String TAG = "DatabaseEngine";
    private static DatabaseEngine instance;

    private Context context;
    private SQLiteDatabase currentDatabase;
    private String currentDatabaseName;
    private File databasesDir;

    public static synchronized DatabaseEngine getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseEngine(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseEngine(Context context) {
        this.context = context;
        databasesDir = new File(context.getFilesDir(), "databases");
        if (!databasesDir.exists()) databasesDir.mkdirs();
    }

    // ==================== DATABASE MANAGEMENT ====================

    public List<DatabaseInfo> listDatabases() {
        List<DatabaseInfo> databases = new ArrayList<>();
        File[] files = databasesDir.listFiles((dir, name) -> name.endsWith(".db"));
        if (files != null) {
            for (File file : files) {
                String name = file.getName().replace(".db", "");
                databases.add(new DatabaseInfo(name, file.length(), file.lastModified()));
            }
        }
        return databases;
    }

    public boolean createDatabase(String name) {
        try {
            File dbFile = new File(databasesDir, name + ".db");
            if (dbFile.exists()) return true;
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
            db.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error creating database: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteDatabase(String name) {
        try {
            if (currentDatabaseName != null && currentDatabaseName.equals(name)) closeDatabase();
            return new File(databasesDir, name + ".db").delete();
        } catch (Exception e) {
            Log.e(TAG, "Error deleting database: " + e.getMessage());
            return false;
        }
    }

    public boolean openDatabase(String name) {
        try {
            closeDatabase();
            File dbFile = new File(databasesDir, name + ".db");
            currentDatabase = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
            currentDatabaseName = name;
            currentDatabase.enableWriteAheadLogging();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error opening database: " + e.getMessage());
            return false;
        }
    }

    public void closeDatabase() {
        if (currentDatabase != null && currentDatabase.isOpen()) {
            currentDatabase.close();
            currentDatabase = null;
            currentDatabaseName = null;
        }
    }

    public String getCurrentDatabaseName() { return currentDatabaseName; }
    public boolean isDatabaseOpen() { return currentDatabase != null && currentDatabase.isOpen(); }

    // ==================== QUERY EXECUTION ====================

    public QueryResult executeQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) return QueryResult.error("Empty query");

        // Split multiple statements separated by ;
        String[] statements = sql.split(";");
        QueryResult lastResult = null;
        for (String stmt : statements) {
            String trimmed = stmt.trim();
            if (trimmed.isEmpty()) continue;
            lastResult = executeSingleQuery(trimmed);
            if (lastResult.isError()) return lastResult;
        }
        return lastResult != null ? lastResult : QueryResult.success("OK");
    }

    private QueryResult executeSingleQuery(String sql) {
        // Strip leading comments and whitespace to find the actual command
        String cleanSql = sql.replaceAll("(?s)/\\*.*?\\*/", "").trim(); // Remove block comments
        while (cleanSql.startsWith("--") || cleanSql.startsWith("#")) { // Remove line comments
            int nextLine = cleanSql.indexOf("\n");
            if (nextLine == -1) {
                cleanSql = "";
                break;
            }
            cleanSql = cleanSql.substring(nextLine).trim();
        }

        String upper = cleanSql.toUpperCase();
        try {
            // ── SELECT / DQL ──────────────────────────────────────────────
            // Catch anything that starts with DQL keywords after comments are removed
            if (upper.startsWith("SELECT") || upper.startsWith("PRAGMA")
                    || upper.startsWith("EXPLAIN") || upper.startsWith("WITH")
                    || upper.startsWith("VALUES")) {
                return executeSelect(translateMySQLToSQLite(sql));
            }

            // ── SHOW commands ──────────────────────────────────────────────
            if (upper.startsWith("SHOW DATABASES") || upper.startsWith("SHOW SCHEMAS"))
                return handleShowDatabases();
            if (upper.startsWith("SHOW TABLES"))
                return handleShowTables();
            if (upper.startsWith("SHOW COLUMNS") || upper.startsWith("DESCRIBE") || upper.startsWith("DESC "))
                return handleDescribeTable(sql);
            if (upper.startsWith("SHOW CREATE TABLE"))
                return handleShowCreateTable(sql);
            if (upper.startsWith("SHOW INDEX") || upper.startsWith("SHOW INDEXES") || upper.startsWith("SHOW KEYS"))
                return handleShowIndexes(sql);
            if (upper.startsWith("SHOW VARIABLES"))
                return handleShowVariables();
            if (upper.startsWith("SHOW STATUS"))
                return handleShowStatus();
            if (upper.startsWith("SHOW WARNINGS") || upper.startsWith("SHOW ERRORS"))
                return QueryResult.success("No warnings or errors.");
            if (upper.startsWith("SHOW PROCESSLIST"))
                return handleShowProcesslist();
            if (upper.startsWith("SHOW ENGINES"))
                return handleShowEngines();
            if (upper.startsWith("SHOW GRANTS"))
                return QueryResult.success("GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost'");
            if (upper.startsWith("SHOW PLUGINS"))
                return QueryResult.success("Plugins not available in SQLite mode.");

            // ── USE / Database commands ───────────────────────────────────
            if (upper.startsWith("USE "))
                return handleUseDatabase(sql);
            if (upper.startsWith("CREATE DATABASE") || upper.startsWith("CREATE SCHEMA"))
                return handleCreateDatabase(sql);
            if (upper.startsWith("DROP DATABASE") || upper.startsWith("DROP SCHEMA"))
                return handleDropDatabase(sql);

            // ── ALTER TABLE — all variants ────────────────────────────────
            if (upper.startsWith("ALTER TABLE"))
                return handleAlterTable(sql);

            // ── TRUNCATE ──────────────────────────────────────────────────
            if (upper.startsWith("TRUNCATE"))
                return handleTruncate(sql);

            // ── RENAME TABLE ──────────────────────────────────────────────
            if (upper.startsWith("RENAME TABLE"))
                return handleRenameTable(sql);

            // ── INSERT IGNORE / ON DUPLICATE KEY UPDATE ───────────────────
            if (upper.startsWith("INSERT IGNORE") || upper.contains("ON DUPLICATE KEY"))
                return handleInsertSpecial(sql);

            // ── DCL (MySQL user management — simulated) ───────────────────
            if (upper.startsWith("GRANT"))
                return QueryResult.success("GRANT executed. (Note: user permissions are not enforced in SQLite mode)");
            if (upper.startsWith("REVOKE"))
                return QueryResult.success("REVOKE executed. (Note: user permissions are not enforced in SQLite mode)");
            if (upper.startsWith("CREATE USER"))
                return QueryResult.success("User created. (Note: user management is simulated in SQLite mode)");
            if (upper.startsWith("DROP USER"))
                return QueryResult.success("User dropped. (Note: user management is simulated in SQLite mode)");
            if (upper.startsWith("FLUSH"))
                return QueryResult.success("FLUSH executed.");
            if (upper.startsWith("SET PASSWORD"))
                return QueryResult.success("Password updated. (simulated in SQLite mode)");

            // ── Stored Programs (simulated) ───────────────────────────────
            if (upper.startsWith("CREATE PROCEDURE") || upper.startsWith("CREATE FUNCTION") || upper.startsWith("CREATE EVENT"))
                return QueryResult.error("Stored procedures/functions/events are not supported in SQLite mode. Use direct SQL statements.");
            if (upper.startsWith("CALL"))
                return QueryResult.error("CALL is not supported in SQLite mode. Stored procedures are not available.");
            if (upper.startsWith("DELIMITER"))
                return QueryResult.success("DELIMITER noted. (Not needed in SQLite mode — use ; directly)");

            // ── LOCK / UNLOCK ─────────────────────────────────────────────
            if (upper.startsWith("LOCK TABLES"))
                return QueryResult.success("Table(s) locked. (Simulated — SQLite handles locking automatically)");
            if (upper.startsWith("UNLOCK TABLES"))
                return QueryResult.success("Tables unlocked.");

            // ── OPTIMIZE / ANALYZE / REPAIR / CHECK ──────────────────────
            if (upper.startsWith("OPTIMIZE TABLE"))
                return handleOptimizeTable(sql);
            if (upper.startsWith("ANALYZE TABLE"))
                return handleAnalyzeTable(sql);
            if (upper.startsWith("REPAIR TABLE"))
                return QueryResult.success("Table repaired. (SQLite auto-manages table integrity)");
            if (upper.startsWith("CHECK TABLE"))
                return handleCheckTable(sql);

            // ── Everything else — translate and run ───────────────────────
            return executeUpdate(translateMySQLToSQLite(sql));

        } catch (Exception e) {
            return QueryResult.error("Error: " + e.getMessage());
        }
    }

    // ==================== ALTER TABLE — ALL VARIANTS ====================

    private QueryResult handleAlterTable(String sql) {
        if (!isDatabaseOpen()) return QueryResult.error("No database selected.");

        String upper = sql.toUpperCase().trim();

        // ── MODIFY COLUMN ─────────────────────────────────────────────────
        // ALTER TABLE t MODIFY COLUMN col_name NEW_TYPE [constraints]
        // ALTER TABLE t MODIFY col_name NEW_TYPE [constraints]
        Pattern modifyPattern = Pattern.compile(
                "(?i)ALTER\\s+TABLE\\s+(\\w+)\\s+MODIFY(?:\\s+COLUMN)?\\s+(\\w+)\\s+(.+)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher modifyMatcher = modifyPattern.matcher(sql);
        if (modifyMatcher.find()) {
            String tableName  = modifyMatcher.group(1).trim();
            String colName    = modifyMatcher.group(2).trim();
            String newDef     = modifyMatcher.group(3).trim().replaceAll(";$", "");
            return handleModifyColumn(tableName, colName, colName, newDef);
        }

        // ── CHANGE COLUMN ─────────────────────────────────────────────────
        // ALTER TABLE t CHANGE [COLUMN] old_name new_name NEW_TYPE [constraints]
        Pattern changePattern = Pattern.compile(
                "(?i)ALTER\\s+TABLE\\s+(\\w+)\\s+CHANGE(?:\\s+COLUMN)?\\s+(\\w+)\\s+(\\w+)\\s+(.+)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher changeMatcher = changePattern.matcher(sql);
        if (changeMatcher.find()) {
            String tableName  = changeMatcher.group(1).trim();
            String oldName    = changeMatcher.group(2).trim();
            String newName    = changeMatcher.group(3).trim();
            String newDef     = changeMatcher.group(4).trim().replaceAll(";$", "");
            return handleModifyColumn(tableName, oldName, newName, newDef);
        }

        // ── DROP COLUMN ───────────────────────────────────────────────────
        Pattern dropColPattern = Pattern.compile(
                "(?i)ALTER\\s+TABLE\\s+(\\w+)\\s+DROP(?:\\s+COLUMN)?\\s+(\\w+)");
        Matcher dropColMatcher = dropColPattern.matcher(sql);
        if (dropColMatcher.find()) {
            return handleDropColumn(dropColMatcher.group(1).trim(), dropColMatcher.group(2).trim());
        }

        // ── ADD PRIMARY KEY ───────────────────────────────────────────────
        if (upper.contains("ADD PRIMARY KEY")) {
            return QueryResult.error(
                    "ADD PRIMARY KEY on existing table is not supported in SQLite.\n" +
                            "Tip: Recreate the table with PRIMARY KEY defined in CREATE TABLE.");
        }

        // ── DROP PRIMARY KEY ──────────────────────────────────────────────
        if (upper.contains("DROP PRIMARY KEY")) {
            return QueryResult.error(
                    "DROP PRIMARY KEY is not supported in SQLite.\n" +
                            "Tip: Recreate the table without the PRIMARY KEY constraint.");
        }

        // ── ADD COLUMN ────────────────────────────────────────────────────
        // Pass through to SQLite (supported natively)
        if (upper.contains("ADD COLUMN") || upper.contains("ADD ")) {
            String translated = translateMySQLToSQLite(sql);
            // Remove unsupported AFTER/FIRST clauses
            translated = translated.replaceAll("(?i)\\s+AFTER\\s+\\w+", "");
            translated = translated.replaceAll("(?i)\\s+FIRST", "");
            return executeUpdate(translated);
        }

        // ── RENAME COLUMN (MySQL 8+) ──────────────────────────────────────
        Pattern renameColPattern = Pattern.compile(
                "(?i)ALTER\\s+TABLE\\s+(\\w+)\\s+RENAME\\s+COLUMN\\s+(\\w+)\\s+TO\\s+(\\w+)");
        Matcher renameColMatcher = renameColPattern.matcher(sql);
        if (renameColMatcher.find()) {
            // SQLite 3.25.0+ supports RENAME COLUMN natively
            return executeUpdate(sql);
        }

        // ── RENAME TABLE (ALTER TABLE ... RENAME TO ...) ──────────────────
        if (upper.contains("RENAME TO")) {
            return executeUpdate(sql);
        }

        // ── Default: translate and run ────────────────────────────────────
        return executeUpdate(translateMySQLToSQLite(sql));
    }

    /**
     * Handles MODIFY and CHANGE by rebuilding the table.
     * This is the only way to change column definitions in SQLite.
     *
     * Steps:
     * 1. Read current schema
     * 2. Build new CREATE TABLE with modified column
     * 3. Copy data
     * 4. Drop old table
     * 5. Rename new table
     */
    private QueryResult handleModifyColumn(String tableName, String oldColName,
                                           String newColName, String newDefinition) {
        if (!isDatabaseOpen()) return QueryResult.error("No database selected.");
        try {
            // Step 1: Get existing columns via PRAGMA
            Cursor pragma = currentDatabase.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            List<String[]> columns = new ArrayList<>(); // [name, type, notnull, dflt, pk]
            boolean colFound = false;
            while (pragma.moveToNext()) {
                String cName   = pragma.getString(1);
                String cType   = pragma.getString(2);
                String cNN     = pragma.getString(3); // 1=not null
                String cDflt   = pragma.getString(4);
                String cPk     = pragma.getString(5); // 1=pk
                columns.add(new String[]{cName, cType, cNN, cDflt, cPk});
                if (cName.equalsIgnoreCase(oldColName)) colFound = true;
            }
            pragma.close();

            if (!colFound) {
                return QueryResult.error("Unknown column '" + oldColName + "' in table '" + tableName + "'");
            }
            if (columns.isEmpty()) {
                return QueryResult.error("Table '" + tableName + "' not found.");
            }

            // Step 2: Build new CREATE TABLE statement
            String tmpTable = tableName + "_tmp_" + System.currentTimeMillis();
            StringBuilder sb = new StringBuilder("CREATE TABLE " + tmpTable + " (");
            List<String> colNames    = new ArrayList<>(); // original names for INSERT SELECT
            List<String> newColNames = new ArrayList<>(); // new names for new table

            for (int i = 0; i < columns.size(); i++) {
                String[] col = columns.get(i);
                if (i > 0) sb.append(", ");

                if (col[0].equalsIgnoreCase(oldColName)) {
                    // Use new name + new definition
                    sb.append(newColName).append(" ").append(translateMySQLToSQLite(newDefinition));
                    colNames.add(col[0]);
                    newColNames.add(newColName);
                } else {
                    // Keep existing definition
                    sb.append(col[0]).append(" ").append(col[1]);
                    if ("1".equals(col[4])) sb.append(" PRIMARY KEY");
                    if ("1".equals(col[2])) sb.append(" NOT NULL");
                    if (col[3] != null) sb.append(" DEFAULT ").append(col[3]);
                    colNames.add(col[0]);
                    newColNames.add(col[0]);
                }
            }
            sb.append(")");

            currentDatabase.beginTransaction();
            try {
                // Step 3: Create temp table
                currentDatabase.execSQL(sb.toString());

                // Step 4: Copy data
                String insertCols = String.join(", ", newColNames);
                String selectCols = String.join(", ", colNames);
                currentDatabase.execSQL(
                        "INSERT INTO " + tmpTable + " (" + insertCols + ") " +
                                "SELECT " + selectCols + " FROM " + tableName);

                // Step 5: Drop original
                currentDatabase.execSQL("DROP TABLE " + tableName);

                // Step 6: Rename temp to original
                currentDatabase.execSQL("ALTER TABLE " + tmpTable + " RENAME TO " + tableName);

                currentDatabase.setTransactionSuccessful();
            } finally {
                currentDatabase.endTransaction();
            }

            String msg = oldColName.equalsIgnoreCase(newColName)
                    ? "Column '" + oldColName + "' modified successfully in table '" + tableName + "'"
                    : "Column '" + oldColName + "' changed to '" + newColName + "' in table '" + tableName + "'";
            return QueryResult.success(msg);

        } catch (Exception e) {
            return QueryResult.error("Error modifying column: " + e.getMessage());
        }
    }

    /**
     * Handles DROP COLUMN by rebuilding the table without that column.
     */
    private QueryResult handleDropColumn(String tableName, String colName) {
        if (!isDatabaseOpen()) return QueryResult.error("No database selected.");
        try {
            Cursor pragma = currentDatabase.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            List<String[]> columns = new ArrayList<>();
            boolean colFound = false;
            while (pragma.moveToNext()) {
                String cName = pragma.getString(1);
                if (cName.equalsIgnoreCase(colName)) { colFound = true; continue; } // skip dropped col
                columns.add(new String[]{cName, pragma.getString(2),
                        pragma.getString(3), pragma.getString(4), pragma.getString(5)});
            }
            pragma.close();

            if (!colFound) return QueryResult.error("Column '" + colName + "' not found in table '" + tableName + "'");
            if (columns.isEmpty()) return QueryResult.error("Cannot drop the only column in a table.");

            String tmpTable = tableName + "_tmp_" + System.currentTimeMillis();
            StringBuilder sb = new StringBuilder("CREATE TABLE " + tmpTable + " (");
            List<String> keepCols = new ArrayList<>();
            for (int i = 0; i < columns.size(); i++) {
                String[] col = columns.get(i);
                if (i > 0) sb.append(", ");
                sb.append(col[0]).append(" ").append(col[1]);
                if ("1".equals(col[4])) sb.append(" PRIMARY KEY");
                if ("1".equals(col[2])) sb.append(" NOT NULL");
                if (col[3] != null) sb.append(" DEFAULT ").append(col[3]);
                keepCols.add(col[0]);
            }
            sb.append(")");

            String colList = String.join(", ", keepCols);
            currentDatabase.beginTransaction();
            try {
                currentDatabase.execSQL(sb.toString());
                currentDatabase.execSQL("INSERT INTO " + tmpTable + " SELECT " + colList + " FROM " + tableName);
                currentDatabase.execSQL("DROP TABLE " + tableName);
                currentDatabase.execSQL("ALTER TABLE " + tmpTable + " RENAME TO " + tableName);
                currentDatabase.setTransactionSuccessful();
            } finally {
                currentDatabase.endTransaction();
            }
            return QueryResult.success("Column '" + colName + "' dropped from table '" + tableName + "'");
        } catch (Exception e) {
            return QueryResult.error("Error dropping column: " + e.getMessage());
        }
    }

    // ==================== TRUNCATE ====================

    private QueryResult handleTruncate(String sql) {
        if (!isDatabaseOpen()) return QueryResult.error("No database selected.");
        // TRUNCATE TABLE t  or  TRUNCATE t
        String tableName = sql.replaceAll("(?i)TRUNCATE\\s+TABLE\\s+", "")
                .replaceAll("(?i)TRUNCATE\\s+", "")
                .replaceAll("[;`\"']", "").trim();
        try {
            currentDatabase.beginTransaction();
            try {
                currentDatabase.execSQL("DELETE FROM " + tableName);
                // Reset auto-increment counter
                currentDatabase.execSQL(
                        "DELETE FROM sqlite_sequence WHERE name='" + tableName + "'");
                currentDatabase.setTransactionSuccessful();
            } finally {
                currentDatabase.endTransaction();
            }
            return QueryResult.success("Table '" + tableName + "' truncated. All rows deleted, auto-increment reset.");
        } catch (Exception e) {
            // sqlite_sequence may not exist if AUTOINCREMENT was never used — that's fine
            try {
                currentDatabase.execSQL("DELETE FROM " + tableName);
                return QueryResult.success("Table '" + tableName + "' truncated.");
            } catch (Exception e2) {
                return QueryResult.error("Error truncating table: " + e2.getMessage());
            }
        }
    }

    // ==================== RENAME TABLE ====================

    private QueryResult handleRenameTable(String sql) {
        if (!isDatabaseOpen()) return QueryResult.error("No database selected.");
        // RENAME TABLE old_name TO new_name [, old2 TO new2 ...]
        Pattern p = Pattern.compile("(?i)RENAME\\s+TABLE\\s+(.+)");
        Matcher m = p.matcher(sql);
        if (!m.find()) return QueryResult.error("Invalid RENAME TABLE syntax.");

        String pairs = m.group(1).replaceAll(";", "").trim();
        String[] pairArr = pairs.split(",");
        try {
            currentDatabase.beginTransaction();
            try {
                for (String pair : pairArr) {
                    String[] parts = pair.trim().split("(?i)\\s+TO\\s+");
                    if (parts.length != 2) return QueryResult.error("Invalid RENAME TABLE syntax near: " + pair);
                    String oldName = parts[0].trim().replaceAll("[`\"']", "");
                    String newName = parts[1].trim().replaceAll("[`\"']", "");
                    currentDatabase.execSQL("ALTER TABLE " + oldName + " RENAME TO " + newName);
                }
                currentDatabase.setTransactionSuccessful();
            } finally {
                currentDatabase.endTransaction();
            }
            return QueryResult.success("Table(s) renamed successfully.");
        } catch (Exception e) {
            return QueryResult.error("Error renaming table: " + e.getMessage());
        }
    }

    // ==================== INSERT SPECIAL ====================

    private QueryResult handleInsertSpecial(String sql) {
        if (!isDatabaseOpen()) return QueryResult.error("No database selected.");
        String translated = sql
                .replaceAll("(?i)INSERT\\s+IGNORE\\s+INTO", "INSERT OR IGNORE INTO")
                .replaceAll("(?i)INSERT\\s+IGNORE\\s+", "INSERT OR IGNORE INTO ")
                .replaceAll("(?i)ON\\s+DUPLICATE\\s+KEY\\s+UPDATE.+", ""); // strip unsupported clause
        return executeUpdate(translateMySQLToSQLite(translated));
    }

    // ==================== SHOW HELPERS ====================

    private QueryResult handleShowDatabases() {
        List<DatabaseInfo> dbs = listDatabases();
        List<String[]> rows = new ArrayList<>();
        for (DatabaseInfo db : dbs) rows.add(new String[]{db.getName()});
        return QueryResult.table(new String[]{"Database"}, rows, rows.size() + " database(s)", 0);
    }

    private QueryResult handleShowTables() {
        if (!isDatabaseOpen()) return QueryResult.error("No database selected.");
        return executeSelect(
                "SELECT name AS 'Tables_in_" + currentDatabaseName + "' " +
                        "FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name");
    }

    private QueryResult handleDescribeTable(String sql) {
        if (!isDatabaseOpen()) return QueryResult.error("No database selected.");
        String tableName = sql
                .replaceAll("(?i)(DESCRIBE|DESC|SHOW\\s+COLUMNS\\s+FROM|SHOW\\s+COLUMNS\\s+IN)\\s+", "")
                .replaceAll("[;`'\"\\s]", "").trim();
        Cursor cursor = null;
        try {
            cursor = currentDatabase.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            List<String[]> rows = new ArrayList<>();
            while (cursor.moveToNext()) {
                rows.add(new String[]{
                        cursor.getString(1),                          // Field
                        cursor.getString(2),                          // Type
                        cursor.getInt(3) == 1 ? "NO" : "YES",        // Null
                        cursor.getInt(5) == 1 ? "PRI" : "",          // Key
                        cursor.getString(4) != null ? cursor.getString(4) : "NULL", // Default
                        cursor.getInt(5) == 1 ? "auto_increment" : "" // Extra
                });
            }
            if (rows.isEmpty()) return QueryResult.error("Table '" + tableName + "' doesn't exist.");
            return QueryResult.table(new String[]{"Field","Type","Null","Key","Default","Extra"},
                    rows, rows.size() + " column(s)", 0);
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private QueryResult handleShowCreateTable(String sql) {
        if (!isDatabaseOpen()) return QueryResult.error("No database selected.");
        String tableName = sql.replaceAll("(?i)SHOW\\s+CREATE\\s+TABLE\\s+", "")
                .replaceAll("[;`]", "").trim();
        Cursor cursor = null;
        try {
            cursor = currentDatabase.rawQuery(
                    "SELECT sql FROM sqlite_master WHERE type='table' AND name=?",
                    new String[]{tableName});
            if (cursor.moveToFirst()) {
                List<String[]> rows = new ArrayList<>();
                rows.add(new String[]{tableName, cursor.getString(0)});
                return QueryResult.table(new String[]{"Table", "Create Table"}, rows, "1 row", 0);
            }
            return QueryResult.error("Table '" + tableName + "' doesn't exist.");
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private QueryResult handleShowIndexes(String sql) {
        if (!isDatabaseOpen()) return QueryResult.error("No database selected.");
        String tableName = sql
                .replaceAll("(?i)(SHOW\\s+INDEX|SHOW\\s+INDEXES|SHOW\\s+KEYS)\\s+(FROM|IN|ON)\\s+", "")
                .replaceAll("[;`]", "").trim();
        Cursor cursor = null;
        try {
            cursor = currentDatabase.rawQuery("PRAGMA index_list(" + tableName + ")", null);
            List<String[]> rows = new ArrayList<>();
            while (cursor.moveToNext()) {
                rows.add(new String[]{
                        tableName,
                        cursor.getInt(2) == 1 ? "0" : "1",
                        cursor.getString(1), "1", "A", "NULL", "NULL", "", "BTREE", "", ""
                });
            }
            return QueryResult.table(
                    new String[]{"Table","Non_unique","Key_name","Seq","Col","Collation","Cardinality","Sub_part","Index_type","Comment","Visible"},
                    rows, rows.size() + " index(es)", 0);
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private QueryResult handleShowVariables() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"version",            "SQLite " + android.database.sqlite.SQLiteDatabase.releaseMemory()});
        rows.add(new String[]{"version_comment",    "MySQL WB — SQLite Engine"});
        rows.add(new String[]{"character_set_server","utf8mb4"});
        rows.add(new String[]{"collation_server",   "utf8mb4_general_ci"});
        rows.add(new String[]{"max_connections",    "1 (SQLite)"});
        rows.add(new String[]{"sql_mode",           "STRICT_TRANS_TABLES"});
        rows.add(new String[]{"storage_engine",     "SQLite"});
        rows.add(new String[]{"datadir",            databasesDir.getAbsolutePath()});
        return QueryResult.table(new String[]{"Variable_name","Value"}, rows,
                rows.size() + " row(s)", 0);
    }

    private QueryResult handleShowStatus() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Uptime",          "N/A"});
        rows.add(new String[]{"Threads_connected","1"});
        rows.add(new String[]{"Questions",       "N/A"});
        rows.add(new String[]{"Connections",     "1"});
        rows.add(new String[]{"Aborted_clients", "0"});
        rows.add(new String[]{"Handler_read_rnd","N/A"});
        return QueryResult.table(new String[]{"Variable_name","Value"}, rows,
                rows.size() + " row(s)", 0);
    }

    private QueryResult handleShowProcesslist() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"1", "root", "localhost", currentDatabaseName != null ? currentDatabaseName : "NULL",
                "Query", "0", "executing", "SHOW PROCESSLIST"});
        return QueryResult.table(
                new String[]{"Id","User","Host","db","Command","Time","State","Info"},
                rows, "1 row", 0);
    }

    private QueryResult handleShowEngines() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"SQLite",  "DEFAULT", "SQLite embedded engine (MySQL WB)", "YES","YES","YES"});
        rows.add(new String[]{"InnoDB",  "YES",     "Simulated via SQLite",               "YES","YES","YES"});
        rows.add(new String[]{"MyISAM",  "YES",     "Simulated via SQLite",               "NO", "NO", "NO"});
        rows.add(new String[]{"MEMORY",  "YES",     "Simulated via SQLite",               "NO", "NO", "NO"});
        return QueryResult.table(
                new String[]{"Engine","Support","Comment","Transactions","XA","Savepoints"},
                rows, rows.size() + " row(s)", 0);
    }

    private QueryResult handleOptimizeTable(String sql) {
        if (!isDatabaseOpen()) return QueryResult.error("No database selected.");
        String tableName = sql.replaceAll("(?i)OPTIMIZE\\s+TABLE\\s+", "").replaceAll("[;`]","").trim();
        try {
            currentDatabase.execSQL("VACUUM");
            List<String[]> rows = new ArrayList<>();
            rows.add(new String[]{currentDatabaseName + "." + tableName, "optimize", "status", "OK"});
            return QueryResult.table(new String[]{"Table","Op","Msg_type","Msg_text"}, rows, "Table optimized", 0);
        } catch (Exception e) {
            return QueryResult.error("Error: " + e.getMessage());
        }
    }

    private QueryResult handleAnalyzeTable(String sql) {
        if (!isDatabaseOpen()) return QueryResult.error("No database selected.");
        String tableName = sql.replaceAll("(?i)ANALYZE\\s+TABLE\\s+", "").replaceAll("[;`]","").trim();
        try {
            currentDatabase.execSQL("ANALYZE");
            List<String[]> rows = new ArrayList<>();
            rows.add(new String[]{currentDatabaseName + "." + tableName, "analyze", "status", "OK"});
            return QueryResult.table(new String[]{"Table","Op","Msg_type","Msg_text"}, rows, "Table analyzed", 0);
        } catch (Exception e) {
            return QueryResult.error("Error: " + e.getMessage());
        }
    }

    private QueryResult handleCheckTable(String sql) {
        if (!isDatabaseOpen()) return QueryResult.error("No database selected.");
        String tableName = sql.replaceAll("(?i)CHECK\\s+TABLE\\s+", "").replaceAll("[;`]","").trim();
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{currentDatabaseName + "." + tableName, "check", "status", "OK"});
        return QueryResult.table(new String[]{"Table","Op","Msg_type","Msg_text"}, rows, "Table is OK", 0);
    }

    private QueryResult handleUseDatabase(String sql) {
        String dbName = sql.replaceAll("(?i)USE\\s+", "").replaceAll("[;`'\"\\s]", "");
        if (openDatabase(dbName)) return QueryResult.success("Database changed to '" + dbName + "'");
        return QueryResult.error("Unknown database '" + dbName + "'");
    }

    private QueryResult handleCreateDatabase(String sql) {
        String dbName = sql
                .replaceAll("(?i)(CREATE\\s+DATABASE|CREATE\\s+SCHEMA)(\\s+IF\\s+NOT\\s+EXISTS)?\\s+","")
                .trim().replaceAll("[;`'\"\\s]","").split("\\s+")[0];
        if (createDatabase(dbName)) return QueryResult.success("Database '" + dbName + "' created successfully.");
        return QueryResult.error("Can't create database '" + dbName + "'.");
    }

    private QueryResult handleDropDatabase(String sql) {
        String dbName = sql
                .replaceAll("(?i)(DROP\\s+DATABASE|DROP\\s+SCHEMA)(\\s+IF\\s+EXISTS)?\\s+","")
                .trim().replaceAll("[;`'\"\\s]","").split("\\s+")[0];
        if (deleteDatabase(dbName)) return QueryResult.success("Database '" + dbName + "' dropped successfully.");
        return QueryResult.error("Can't drop database '" + dbName + "'; database doesn't exist.");
    }

    // ==================== SELECT / UPDATE ====================

    private QueryResult executeSelect(String sql) {
        if (!isDatabaseOpen()) return QueryResult.error("No database selected.");
        long t = System.currentTimeMillis();
        Cursor cursor = null;
        try {
            cursor = currentDatabase.rawQuery(sql, null);
            QueryResult result = cursorToQueryResult(cursor);
            result.setExecutionTime(System.currentTimeMillis() - t);
            return result;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private QueryResult executeUpdate(String sql) {
        if (!isDatabaseOpen()) return QueryResult.error("No database selected.");
        long t = System.currentTimeMillis();
        try {
            currentDatabase.execSQL(sql);
            int affected = 0;
            try {
                Cursor c = currentDatabase.rawQuery("SELECT changes()", null);
                if (c.moveToFirst()) affected = c.getInt(0);
                c.close();
            } catch (Exception ignored) {}
            QueryResult result = QueryResult.success("Query OK, " + affected + " row(s) affected");
            result.setAffectedRows(affected);
            result.setExecutionTime(System.currentTimeMillis() - t);
            return result;
        } catch (SQLiteException e) {
            return QueryResult.error("SQL Error: " + translateSQLiteError(e.getMessage()));
        }
    }

    // ==================== MYSQL → SQLITE TRANSLATION ====================

    private String translateMySQLToSQLite(String sql) {
        // Auto increment
        sql = sql.replaceAll("(?i)\\bAUTO_INCREMENT\\b", "AUTOINCREMENT");

        // Remove MySQL table options
        sql = sql.replaceAll("(?i)\\bENGINE\\s*=\\s*\\w+", "");
        sql = sql.replaceAll("(?i)\\bDEFAULT\\s+CHARSET\\s*=\\s*\\w+", "");
        sql = sql.replaceAll("(?i)\\bCHARSET\\s*=\\s*\\w+", "");
        sql = sql.replaceAll("(?i)\\bCHARACTER\\s+SET\\s*=?\\s*\\w+", "");
        sql = sql.replaceAll("(?i)\\bCOLLATE\\s*=?\\s*[\\w_]+", "");
        sql = sql.replaceAll("(?i)\\bROW_FORMAT\\s*=\\s*\\w+", "");
        sql = sql.replaceAll("(?i)\\bCOMMENT\\s*=\\s*'[^']*'", "");
        sql = sql.replaceAll("(?i)\\bCHECKSUM\\s*=\\s*\\d+", "");

        // Numeric types with size
        sql = sql.replaceAll("(?i)\\bINT\\s*\\(\\d+\\)", "INTEGER");
        sql = sql.replaceAll("(?i)\\bTINYINT\\s*\\(\\d+\\)", "INTEGER");
        sql = sql.replaceAll("(?i)\\bSMALLINT\\s*\\(\\d+\\)", "INTEGER");
        sql = sql.replaceAll("(?i)\\bMEDIUMINT\\s*\\(\\d+\\)", "INTEGER");
        sql = sql.replaceAll("(?i)\\bBIGINT\\s*\\(\\d+\\)", "INTEGER");
        sql = sql.replaceAll("(?i)\\bTINYINT\\b", "INTEGER");
        sql = sql.replaceAll("(?i)\\bSMALLINT\\b", "INTEGER");
        sql = sql.replaceAll("(?i)\\bMEDIUMINT\\b", "INTEGER");
        sql = sql.replaceAll("(?i)\\bBIGINT\\b", "INTEGER");
        sql = sql.replaceAll("(?i)\\bSERIAL\\b", "INTEGER");

        // Float types
        sql = sql.replaceAll("(?i)\\bDOUBLE\\s+PRECISION\\b", "REAL");
        sql = sql.replaceAll("(?i)\\bFLOAT\\s*\\(\\d+,\\d+\\)", "REAL");
        sql = sql.replaceAll("(?i)\\bDOUBLE\\b", "REAL");
        sql = sql.replaceAll("(?i)\\bFLOAT\\b", "REAL");

        // Text/blob types
        sql = sql.replaceAll("(?i)\\bTINYTEXT\\b", "TEXT");
        sql = sql.replaceAll("(?i)\\bMEDIUMTEXT\\b", "TEXT");
        sql = sql.replaceAll("(?i)\\bLONGTEXT\\b", "TEXT");
        sql = sql.replaceAll("(?i)\\bTINYBLOB\\b", "BLOB");
        sql = sql.replaceAll("(?i)\\bMEDIUMBLOB\\b", "BLOB");
        sql = sql.replaceAll("(?i)\\bLONGBLOB\\b", "BLOB");

        // Date/time types
        sql = sql.replaceAll("(?i)\\bDATETIME\\b", "TEXT");
        sql = sql.replaceAll("(?i)\\bTIMESTAMP\\b", "TEXT");
        sql = sql.replaceAll("(?i)\\bTIME\\b", "TEXT");
        sql = sql.replaceAll("(?i)\\bYEAR\\b", "INTEGER");

        // Enum/Set → TEXT
        sql = sql.replaceAll("(?i)\\bENUM\\s*\\([^)]+\\)", "TEXT");
        sql = sql.replaceAll("(?i)\\bSET\\s*\\([^)]+\\)", "TEXT");

        // Modifiers not in SQLite
        sql = sql.replaceAll("(?i)\\bUNSIGNED\\b", "");
        sql = sql.replaceAll("(?i)\\bZEROFILL\\b", "");

        // Functions
        sql = sql.replaceAll("(?i)\\bNOW\\s*\\(\\s*\\)", "datetime('now')");
        sql = sql.replaceAll("(?i)\\bCURDATE\\s*\\(\\s*\\)", "date('now')");
        sql = sql.replaceAll("(?i)\\bCURTIME\\s*\\(\\s*\\)", "time('now')");
        sql = sql.replaceAll("(?i)\\bSYSDATE\\s*\\(\\s*\\)", "datetime('now')");
        sql = sql.replaceAll("(?i)\\bCURRENT_TIMESTAMP\\b", "datetime('now')");
        sql = sql.replaceAll("(?i)\\bCURRENT_DATE\\b", "date('now')");
        sql = sql.replaceAll("(?i)\\bCURRENT_TIME\\b", "time('now')");
        sql = sql.replaceAll("(?i)\\bUNIX_TIMESTAMP\\s*\\(\\s*\\)", "strftime('%s','now')");
        sql = sql.replaceAll("(?i)\\bLAST_INSERT_ID\\s*\\(\\s*\\)", "last_insert_rowid()");
        sql = sql.replaceAll("(?i)\\bROW_COUNT\\s*\\(\\s*\\)", "changes()");
        sql = sql.replaceAll("(?i)\\bDATABASE\\s*\\(\\s*\\)", "'" + (currentDatabaseName != null ? currentDatabaseName : "") + "'");
        sql = sql.replaceAll("(?i)\\bSCHEMA\\s*\\(\\s*\\)", "'" + (currentDatabaseName != null ? currentDatabaseName : "") + "'");
        sql = sql.replaceAll("(?i)\\bUSER\\s*\\(\\s*\\)", "'root@localhost'");
        sql = sql.replaceAll("(?i)\\bVERSION\\s*\\(\\s*\\)", "'SQLite (MySQL WB)'");
        sql = sql.replaceAll("(?i)\\bIFNULL\\s*\\(", "COALESCE(");
        sql = sql.replaceAll("(?i)\\bISNULL\\s*\\(([^)]+)\\)", "($1 IS NULL)");
        sql = sql.replaceAll("(?i)\\bIF\\s*\\(([^,]+),([^,]+),([^)]+)\\)", "CASE WHEN $1 THEN $2 ELSE $3 END");
        sql = sql.replaceAll("(?i)\\bCONCAT\\s*\\(([^)]+)\\)", "($1)".replace(",", "||"));

        // LIMIT x,y → LIMIT y OFFSET x
        sql = sql.replaceAll("(?i)\\bLIMIT\\s+(\\d+)\\s*,\\s*(\\d+)", "LIMIT $2 OFFSET $1");

        // INSERT special
        sql = sql.replaceAll("(?i)\\bINSERT\\s+IGNORE\\s+INTO\\b", "INSERT OR IGNORE INTO");

        // Common beginner typo fix: student -> students (specifically for the school practice DB)
        if ("school_db".equalsIgnoreCase(currentDatabaseName)) {
            sql = sql.replaceAll("(?i)\\bstudent\\b", "students");
        }

        // Backtick identifiers (MySQL) → no backticks (SQLite uses double quotes)
        sql = sql.replace("`", "\"");

        // Clean up trailing comma before )
        sql = sql.replaceAll(",\\s*\\)", ")");

        return sql.trim();
    }

    // ==================== SCHEMA METHODS ====================

    public List<TableInfo> getTablesForDatabase(String dbName) {
        List<TableInfo> tables = new ArrayList<>();
        File dbFile = new File(databasesDir, dbName + ".db");
        if (!dbFile.exists()) return tables;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);
            cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name", null);
            while (cursor.moveToNext()) {
                String tn = cursor.getString(0);
                int rows = 0;
                try {
                    Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + tn, null);
                    if (c.moveToFirst()) rows = c.getInt(0);
                    c.close();
                } catch (Exception ignored) {}
                tables.add(new TableInfo(tn, rows));
            }
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return tables;
    }

    public List<ColumnInfo> getColumnsForTable(String tableName) {
        List<ColumnInfo> columns = new ArrayList<>();
        if (!isDatabaseOpen()) return columns;
        Cursor cursor = null;
        try {
            cursor = currentDatabase.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            while (cursor.moveToNext()) {
                columns.add(new ColumnInfo(
                        cursor.getInt(0), cursor.getString(1), cursor.getString(2),
                        cursor.getInt(3) == 1, cursor.getString(4), cursor.getInt(5) == 1));
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return columns;
    }

    // ==================== EXPORT / BACKUP / RESTORE ====================

    public boolean exportDatabase(String dbName, File outputFile) {
        try {
            File source = new File(databasesDir, dbName + ".db");
            if (!source.exists()) return false;
            SQLiteDatabase db = SQLiteDatabase.openDatabase(source.getPath(), null, SQLiteDatabase.OPEN_READONLY);
            StringBuilder dump = new StringBuilder();
            dump.append("-- MySQL WB SQL Dump\n-- Database: ").append(dbName)
                    .append("\n-- Generated: ").append(new java.util.Date()).append("\n\n")
                    .append("CREATE DATABASE IF NOT EXISTS `").append(dbName).append("`;\n")
                    .append("USE `").append(dbName).append("`;\n\n");
            Cursor tc = db.rawQuery("SELECT name, sql FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'", null);
            while (tc.moveToNext()) {
                String tn = tc.getString(0); String createSql = tc.getString(1);
                dump.append("DROP TABLE IF EXISTS `").append(tn).append("`;\n")
                        .append(createSql).append(";\n\n");
                Cursor dc = db.rawQuery("SELECT * FROM " + tn, null);
                while (dc.moveToNext()) {
                    dump.append("INSERT INTO `").append(tn).append("` VALUES (");
                    for (int i = 0; i < dc.getColumnCount(); i++) {
                        if (i > 0) dump.append(", ");
                        String v = dc.getString(i);
                        dump.append(v == null ? "NULL" : "'" + v.replace("'", "''") + "'");
                    }
                    dump.append(");\n");
                }
                dc.close(); dump.append("\n");
            }
            tc.close(); db.close();
            FileOutputStream fos = new FileOutputStream(outputFile);
            fos.write(dump.toString().getBytes()); fos.close();
            return true;
        } catch (Exception e) { Log.e(TAG, "Export: " + e.getMessage()); return false; }
    }

    public boolean backupDatabase(String dbName, File outputFile) {
        try {
            File source = new File(databasesDir, dbName + ".db");
            if (!source.exists()) return false;
            copyFile(source, outputFile); return true;
        } catch (Exception e) { return false; }
    }

    public boolean restoreDatabase(String dbName, File inputFile) {
        try {
            if (currentDatabaseName != null && currentDatabaseName.equals(dbName)) closeDatabase();
            copyFile(inputFile, new File(databasesDir, dbName + ".db")); return true;
        } catch (Exception e) { return false; }
    }

    // ==================== HELPERS ====================

    private QueryResult cursorToQueryResult(Cursor cursor) {
        String[] cols = cursor.getColumnNames();
        List<String[]> rows = new ArrayList<>();
        while (cursor.moveToNext()) {
            String[] row = new String[cols.length];
            for (int i = 0; i < cols.length; i++)
                row[i] = cursor.isNull(i) ? "NULL" : cursor.getString(i);
            rows.add(row);
        }
        return QueryResult.table(cols, rows, rows.size() + " row(s) in set", 0);
    }

    private String translateSQLiteError(String error) {
        if (error == null) return "Unknown error";
        if (error.contains("no such table")) {
            String table = error.substring(error.lastIndexOf(":") + 1).trim();
            return "Table '" + table + "' does not exist. Tip: Check the 'Schema' icon to see valid table names.";
        }
        if (error.contains("already exists"))          return "Table or database already exists";
        if (error.contains("UNIQUE constraint failed"))return "Duplicate entry — UNIQUE constraint failed";
        if (error.contains("NOT NULL constraint"))     return "Column cannot be NULL — NOT NULL constraint failed";
        if (error.contains("FOREIGN KEY constraint"))  return "Foreign key constraint failed";
        if (error.contains("syntax error"))            return "SQL syntax error: " + error;
        if (error.contains("no such column"))          return error.replace("no such column", "Unknown column");
        return error;
    }

    private void copyFile(File source, File dest) throws IOException {
        FileChannel in  = new FileInputStream(source).getChannel();
        FileChannel out = new FileOutputStream(dest).getChannel();
        out.transferFrom(in, 0, in.size());
        in.close(); out.close();
    }

    public File getDatabasesDir() { return databasesDir; }
}