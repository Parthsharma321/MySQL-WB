package com.mysqlwb;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for MySQL WB
 * Run with: ./gradlew test
 */
public class DatabaseEngineTest {

    @Test
    public void testQueryResultSuccess() {
        // Verify QueryResult.success works correctly
        com.mysqlwb.models.QueryResult result = com.mysqlwb.models.QueryResult.success("OK");
        assertTrue(result.isSuccess());
        assertFalse(result.isError());
        assertEquals("OK", result.getMessage());
    }

    @Test
    public void testQueryResultError() {
        com.mysqlwb.models.QueryResult result = com.mysqlwb.models.QueryResult.error("Syntax error");
        assertTrue(result.isError());
        assertFalse(result.isSuccess());
        assertEquals("Syntax error", result.getMessage());
    }

    @Test
    public void testDatabaseInfoFormattedSize() {
        com.mysqlwb.models.DatabaseInfo db = new com.mysqlwb.models.DatabaseInfo("test", 1536, 0);
        assertEquals("1.5 KB", db.getFormattedSize());
    }

    @Test
    public void testColumnInfoPrimaryKey() {
        com.mysqlwb.models.ColumnInfo col = new com.mysqlwb.models.ColumnInfo(0, "id", "INTEGER", true, null, true);
        assertTrue(col.isPrimaryKey());
        assertTrue(col.isNotNull());
        assertEquals("id", col.getName());
        assertEquals("INTEGER", col.getType());
    }
}
