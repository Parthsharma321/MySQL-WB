package com.mysqlwb.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SQLAutoComplete {

    public static final String[] KEYWORDS = {
        // DDL
        "CREATE", "ALTER", "DROP", "TRUNCATE", "RENAME",
        "CREATE TABLE", "CREATE DATABASE", "CREATE INDEX", "CREATE VIEW",
        "ALTER TABLE", "ALTER TABLE ADD", "ALTER TABLE MODIFY", "ALTER TABLE DROP COLUMN",
        "DROP TABLE", "DROP DATABASE", "DROP INDEX",
        // DML
        "INSERT", "INSERT INTO", "UPDATE", "DELETE", "REPLACE",
        "INSERT INTO ... VALUES", "INSERT INTO ... SELECT",
        // DQL
        "SELECT", "SELECT *", "SELECT DISTINCT", "FROM", "WHERE", "AND", "OR", "NOT",
        "ORDER BY", "GROUP BY", "HAVING", "LIMIT", "OFFSET",
        "JOIN", "INNER JOIN", "LEFT JOIN", "RIGHT JOIN", "FULL OUTER JOIN", "CROSS JOIN",
        "ON", "AS", "UNION", "UNION ALL", "INTERSECT", "EXCEPT",
        // DCL
        "GRANT", "REVOKE",
        // TCL
        "BEGIN", "COMMIT", "ROLLBACK", "SAVEPOINT",
        // Functions
        "COUNT", "SUM", "AVG", "MIN", "MAX", "LENGTH", "UPPER", "LOWER",
        "SUBSTRING", "REPLACE", "TRIM", "LTRIM", "RTRIM", "CONCAT",
        "NOW", "DATE", "TIME", "YEAR", "MONTH", "DAY",
        "ROUND", "CEIL", "FLOOR", "ABS", "MOD",
        "IFNULL", "COALESCE", "NULLIF", "CAST", "CONVERT",
        // Conditions
        "IS NULL", "IS NOT NULL", "BETWEEN", "LIKE", "IN", "EXISTS", "NOT IN",
        "CASE", "WHEN", "THEN", "ELSE", "END",
        // Data Types
        "INTEGER", "INT", "SMALLINT", "BIGINT", "FLOAT", "DOUBLE", "DECIMAL",
        "TEXT", "VARCHAR", "CHAR", "BLOB", "BOOLEAN", "DATE", "DATETIME", "TIMESTAMP",
        // Constraints
        "PRIMARY KEY", "FOREIGN KEY", "UNIQUE", "NOT NULL", "DEFAULT", "CHECK",
        "AUTO_INCREMENT", "AUTOINCREMENT", "REFERENCES",
        // MySQL-specific
        "SHOW DATABASES", "SHOW TABLES", "SHOW COLUMNS FROM", "DESCRIBE", "USE",
        "ENGINE=InnoDB", "ENGINE=MyISAM", "DEFAULT CHARSET=utf8",
        // Other
        "IF NOT EXISTS", "IF EXISTS", "ALL", "ANY", "SOME",
        "INTO", "VALUES", "SET", "DISTINCT"
    };

    public static final String[] TEMPLATES = {
        "SELECT * FROM table_name;",
        "SELECT column1, column2 FROM table_name WHERE condition;",
        "INSERT INTO table_name (col1, col2) VALUES (val1, val2);",
        "UPDATE table_name SET col1 = val1 WHERE condition;",
        "DELETE FROM table_name WHERE condition;",
        "CREATE TABLE table_name (\n  id INTEGER PRIMARY KEY AUTOINCREMENT,\n  name TEXT NOT NULL,\n  created_at TEXT\n);",
        "ALTER TABLE table_name ADD COLUMN column_name datatype;",
        "DROP TABLE IF EXISTS table_name;",
        "SELECT t1.col, t2.col FROM table1 t1 INNER JOIN table2 t2 ON t1.id = t2.id;",
        "SELECT COUNT(*) FROM table_name;",
        "SELECT column, COUNT(*) FROM table_name GROUP BY column HAVING COUNT(*) > 1;",
        "CREATE INDEX idx_name ON table_name(column_name);",
        "SELECT * FROM table_name ORDER BY column_name DESC LIMIT 10;"
    };

    public static List<String> getSuggestions(String prefix, List<String> tableNames, List<String> columnNames) {
        List<String> suggestions = new ArrayList<>();
        String upperPrefix = prefix.toUpperCase().trim();

        if (upperPrefix.isEmpty()) return suggestions;

        // Keywords
        for (String keyword : KEYWORDS) {
            if (keyword.toUpperCase().startsWith(upperPrefix)) {
                suggestions.add(keyword);
            }
        }

        // Table names
        if (tableNames != null) {
            for (String table : tableNames) {
                if (table.toUpperCase().startsWith(upperPrefix)) {
                    suggestions.add(table);
                }
            }
        }

        // Column names
        if (columnNames != null) {
            for (String col : columnNames) {
                if (col.toUpperCase().startsWith(upperPrefix)) {
                    suggestions.add(col);
                }
            }
        }

        return suggestions;
    }

    public static List<String> getTemplates() {
        return Arrays.asList(TEMPLATES);
    }
}
