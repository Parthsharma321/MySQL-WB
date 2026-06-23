package com.mysqlwb.utils;

import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLHighlighter {

    // Colors
    private static final int COLOR_KEYWORD = Color.parseColor("#569CD6");    // Blue
    private static final int COLOR_FUNCTION = Color.parseColor("#DCDCAA");   // Yellow
    private static final int COLOR_STRING = Color.parseColor("#CE9178");     // Orange-red
    private static final int COLOR_NUMBER = Color.parseColor("#B5CEA8");     // Green
    private static final int COLOR_COMMENT = Color.parseColor("#6A9955");    // Green (comments)
    private static final int COLOR_OPERATOR = Color.parseColor("#D4D4D4");   // White
    private static final int COLOR_TABLE = Color.parseColor("#4EC9B0");      // Teal

    private static final String[] KEYWORDS = {
            // Core DQL
            "SELECT", "FROM", "WHERE", "AND", "OR", "NOT", "IN", "IS", "NULL",
            "BETWEEN", "LIKE", "ILIKE", "REGEXP", "GLOB", "EXISTS",
            "ORDER", "BY", "GROUP", "HAVING", "LIMIT", "OFFSET", "DISTINCT",
            "AS", "UNION", "ALL", "INTERSECT", "EXCEPT", "WITH",
            "CASE", "WHEN", "THEN", "ELSE", "END",

            // Core DML
            "INSERT", "INTO", "VALUES", "UPDATE", "SET", "DELETE",
            "REPLACE", "IGNORE", "LOW_PRIORITY", "HIGH_PRIORITY", "DELAYED",

            // Core DDL
            "CREATE", "TABLE", "DATABASE", "SCHEMA", "DROP", "ALTER", "ADD",
            "TRUNCATE", "RENAME", "MODIFY", "CHANGE", "INDEX", "VIEW",
            "TRIGGER", "PROCEDURE", "FUNCTION", "EVENT",
            "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "UNIQUE", "CHECK",
            "DEFAULT", "AUTOINCREMENT", "AUTO_INCREMENT", "SERIAL",
            "CONSTRAINT", "CASCADE", "RESTRICT", "NO", "ACTION",
            "COLUMN", "COLUMNS", "AFTER", "BEFORE", "FIRST",
            "UNSIGNED", "ZEROFILL", "BINARY", "VARBINARY",

            // JOINs
            "JOIN", "INNER", "LEFT", "RIGHT", "OUTER", "FULL", "CROSS",
            "NATURAL", "STRAIGHT_JOIN", "ON", "USING",

            // Data Types - MySQL specific
            "INT", "INTEGER", "TINYINT", "SMALLINT", "MEDIUMINT", "BIGINT",
            "FLOAT", "DOUBLE", "DECIMAL", "NUMERIC", "BIT", "YEAR",
            "TEXT", "TINYTEXT", "MEDIUMTEXT", "LONGTEXT",
            "BLOB", "TINYBLOB", "MEDIUMBLOB", "LONGBLOB",
            "VARCHAR", "CHAR", "BOOLEAN", "BOOL", "REAL",
            "DATE", "DATETIME", "TIMESTAMP", "TIME", "ENUM", "SET",

            // MySQL Engine / Table options
            "ENGINE", "CHARSET", "CHARACTER", "COLLATE", "COMMENT",
            "INNODB", "MYISAM", "MEMORY", "ARCHIVE", "CSV",
            "AUTO_INCREMENT", "ROW_FORMAT", "CHECKSUM",

            // Transaction / TCL
            "BEGIN", "COMMIT", "ROLLBACK", "TRANSACTION", "SAVEPOINT",
            "RELEASE", "WORK", "CHAIN", "ONLY",

            // DCL / User management
            "GRANT", "REVOKE", "PRIVILEGES", "ALL", "FLUSH",
            "IDENTIFIED", "PASSWORD", "USER", "ROLE", "SUPER",

            // SHOW / Admin commands
            "SHOW", "DESCRIBE", "DESC", "EXPLAIN", "USE",
            "DATABASES", "TABLES", "PROCESSLIST", "VARIABLES",
            "STATUS", "WARNINGS", "ERRORS", "INDEXES", "KEYS",
            "OPEN", "PLUGINS", "ENGINES", "PROFILES", "GRANTS",

            // MySQL clauses / options
            "FORCE", "STRAIGHT_JOIN", "SQL_CACHE", "SQL_NO_CACHE",
            "OUTFILE", "INFILE", "DUMPFILE", "ENCLOSED", "TERMINATED",
            "ESCAPED", "LINES", "STARTING", "FIELDS",

            // Stored programs
            "CALL", "RETURNS", "DETERMINISTIC", "CONTAINS", "MODIFIES",
            "READS", "DEFINER", "INVOKER", "DELIMITER",

            // Misc MySQL
            "FULLTEXT", "SPATIAL", "ANALYZE", "OPTIMIZE", "REPAIR",
            "PARTITION", "SUBPARTITION", "TABLESPACE", "LOGFILE",
            "IF", "IFNULL", "NULLIF", "COALESCE",
            "LOCK", "UNLOCK", "READ", "WRITE",
            "TEMPORARY", "VIRTUAL", "STORED", "GENERATED", "ALWAYS",
            "PRECEDING", "FOLLOWING", "UNBOUNDED", "CURRENT", "ROW", "ROWS",
            "RANGE", "OVER", "WINDOW", "PARTITION", "RECURSIVE",
            "MASTER", "SLAVE", "RESET", "PURGE", "BINARY", "LOGS", "RELAY"
    };

    private static final String[] FUNCTIONS = {
            // Aggregate
            "COUNT", "SUM", "AVG", "MIN", "MAX",
            "GROUP_CONCAT", "BIT_AND", "BIT_OR", "BIT_XOR",
            "STD", "STDDEV", "STDDEV_POP", "STDDEV_SAMP",
            "VAR_POP", "VAR_SAMP", "VARIANCE",

            // String - SQLite
            "LENGTH", "UPPER", "LOWER", "SUBSTR", "REPLACE",
            "TRIM", "LTRIM", "RTRIM", "INSTR", "PRINTF",
            "QUOTE", "HEX", "UNICODE", "CHAR", "GLOB",

            // String - MySQL specific
            "SUBSTRING", "CONCAT", "CONCAT_WS", "LOCATE", "POSITION",
            "LPAD", "RPAD", "REPEAT", "REVERSE", "SOUNDEX",
            "FORMAT", "FIELD", "FIND_IN_SET", "MAKE_SET",
            "ELT", "EXPORT_SET", "OCTET_LENGTH", "CHAR_LENGTH",
            "CHARACTER_LENGTH", "BIT_LENGTH", "INSERT",
            "SPACE", "STRCMP", "MID", "LEFT", "RIGHT",

            // Numeric - SQLite
            "ABS", "ROUND", "FLOOR", "CEIL", "CEILING",
            "MOD", "POWER", "POW", "RANDOM", "SIGN",

            // Numeric - MySQL specific
            "TRUNCATE", "LOG", "LOG2", "LOG10", "EXP",
            "SQRT", "PI", "SIN", "COS", "TAN",
            "ASIN", "ACOS", "ATAN", "ATAN2", "COT",
            "RADIANS", "DEGREES", "RAND", "CRC32",
            "CONV", "BIN", "OCT", "HEX",

            // Date - SQLite
            "DATE", "TIME", "DATETIME", "JULIANDAY", "STRFTIME",

            // Date - MySQL specific
            "NOW", "CURDATE", "CURTIME", "SYSDATE",
            "DATEDIFF", "TIMESTAMPDIFF", "TIMEDIFF",
            "DATE_FORMAT", "STR_TO_DATE", "DATE_ADD", "DATE_SUB",
            "ADDDATE", "SUBDATE", "FROM_UNIXTIME", "UNIX_TIMESTAMP",
            "PERIOD_ADD", "PERIOD_DIFF", "QUARTER", "YEARWEEK",
            "WEEKDAY", "DAYOFWEEK", "DAYOFMONTH", "DAYOFYEAR",
            "DAYNAME", "MONTHNAME", "LAST_DAY", "MAKEDATE",
            "MAKETIME", "SEC_TO_TIME", "TIME_TO_SEC",
            "ADDTIME", "SUBTIME", "CONVERT_TZ", "GET_FORMAT",
            "YEAR", "MONTH", "DAY", "HOUR", "MINUTE", "SECOND",
            "MICROSECOND", "EXTRACT", "WEEK",

            // Null handling
            "IFNULL", "NULLIF", "COALESCE", "ISNULL",

            // Type conversion
            "CAST", "CONVERT", "TYPEOF", "BINARY",

            // Control flow - MySQL
            "IF", "IIF",

            // Info functions - MySQL
            "DATABASE", "SCHEMA", "VERSION", "USER", "CURRENT_USER",
            "CONNECTION_ID", "LAST_INSERT_ID", "ROW_COUNT",
            "FOUND_ROWS", "BENCHMARK", "CHARSET", "COLLATION",

            // SQLite special
            "LAST_INSERT_ROWID", "CHANGES", "TOTAL_CHANGES",

            // Window functions - MySQL 8+
            "ROW_NUMBER", "RANK", "DENSE_RANK", "PERCENT_RANK",
            "CUME_DIST", "NTILE", "LAG", "LEAD",
            "FIRST_VALUE", "LAST_VALUE", "NTH_VALUE"
    };

    public static void highlight(Editable editable) {
        // Remove existing spans
        ForegroundColorSpan[] existingSpans = editable.getSpans(0, editable.length(), ForegroundColorSpan.class);
        for (ForegroundColorSpan span : existingSpans) {
            editable.removeSpan(span);
        }

        String text = editable.toString();

        // Keywords
        for (String keyword : KEYWORDS) {
            Pattern pattern = Pattern.compile("\\b" + keyword + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                editable.setSpan(new ForegroundColorSpan(COLOR_KEYWORD),
                        matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        // Functions
        for (String func : FUNCTIONS) {
            Pattern pattern = Pattern.compile("\\b" + func + "\\s*\\(", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                editable.setSpan(new ForegroundColorSpan(COLOR_FUNCTION),
                        matcher.start(), matcher.end() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        // String literals
        Pattern stringPattern = Pattern.compile("'[^']*'|\"[^\"]*\"");
        Matcher stringMatcher = stringPattern.matcher(text);
        while (stringMatcher.find()) {
            editable.setSpan(new ForegroundColorSpan(COLOR_STRING),
                    stringMatcher.start(), stringMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // Numbers
        Pattern numberPattern = Pattern.compile("\\b\\d+\\.?\\d*\\b");
        Matcher numberMatcher = numberPattern.matcher(text);
        while (numberMatcher.find()) {
            editable.setSpan(new ForegroundColorSpan(COLOR_NUMBER),
                    numberMatcher.start(), numberMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // Comments
        Pattern commentPattern = Pattern.compile("--[^\n]*|/\\*.*?\\*/", Pattern.DOTALL);
        Matcher commentMatcher = commentPattern.matcher(text);
        while (commentMatcher.find()) {
            editable.setSpan(new ForegroundColorSpan(COLOR_COMMENT),
                    commentMatcher.start(), commentMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}