package com.mysqlwb.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.mysqlwb.R;
import com.mysqlwb.adapters.SQLReferenceAdapter;
import com.mysqlwb.models.SQLCommand;

import java.util.ArrayList;
import java.util.List;

public class SQLReferenceActivity extends AppCompatActivity {

    private SQLReferenceAdapter adapter;
    private List<SQLCommand> allCommands;
    private String currentCategory = "ALL";
    private String currentSearch = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sql_reference);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("SQL Command Reference");
        }

        allCommands = buildCommandList();

        RecyclerView rv = findViewById(R.id.rv_commands);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SQLReferenceAdapter(this, allCommands);
        rv.setAdapter(adapter);

        // Search
        TextInputEditText etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                currentSearch = s.toString().trim();
                adapter.filter(currentSearch, currentCategory);
            }
            @Override public void afterTextChanged(Editable e) {}
        });

        // Category chips
        ChipGroup chipGroup = findViewById(R.id.chip_group_categories);
        String[] categories = {"ALL", "DDL", "DML", "DQL", "DCL", "Clauses", "Functions"};
        for (String cat : categories) {
            Chip chip = new Chip(this);
            chip.setText(cat);
            chip.setCheckable(true);
            chip.setChecked(cat.equals("ALL"));
            chip.setOnClickListener(v -> {
                currentCategory = cat;
                adapter.filter(currentSearch, currentCategory);
            });
            chipGroup.addView(chip);
        }
    }

    private List<SQLCommand> buildCommandList() {
        List<SQLCommand> cmds = new ArrayList<>();

        // DDL
        cmds.add(new SQLCommand("CREATE TABLE", "DDL",
                "Creates a new table in the database",
                "CREATE TABLE students (\n  id INTEGER PRIMARY KEY AUTOINCREMENT,\n  name TEXT NOT NULL,\n  age INTEGER,\n  email TEXT UNIQUE\n);",
                "Use AUTOINCREMENT for auto-numbering primary keys."));

        cmds.add(new SQLCommand("ALTER TABLE", "DDL",
                "Modifies an existing table structure",
                "ALTER TABLE students ADD COLUMN phone TEXT;\nALTER TABLE students RENAME TO learners;",
                "SQLite supports ADD COLUMN and RENAME TABLE/COLUMN."));

        cmds.add(new SQLCommand("DROP TABLE", "DDL",
                "Permanently removes a table and all its data",
                "DROP TABLE students;\nDROP TABLE IF EXISTS students;",
                "Use IF EXISTS to avoid errors. This cannot be undone!"));

        cmds.add(new SQLCommand("CREATE INDEX", "DDL",
                "Creates an index to speed up queries",
                "CREATE INDEX idx_name ON students(name);\nCREATE UNIQUE INDEX idx_email ON students(email);",
                "Indexes speed up SELECT but slow down INSERT/UPDATE."));

        cmds.add(new SQLCommand("CREATE VIEW", "DDL",
                "Creates a virtual table based on a SELECT query",
                "CREATE VIEW teen_students AS\nSELECT name, grade FROM students WHERE age < 18;",
                "Views don't store data; they run the query each time."));

        cmds.add(new SQLCommand("SHOW DATABASES", "DDL",
                "Lists all available databases",
                "SHOW DATABASES;\nSHOW SCHEMAS;",
                "MySQL command. Lists your local databases."));

        cmds.add(new SQLCommand("SHOW TABLES", "DDL",
                "Lists all tables in the current database",
                "SHOW TABLES;",
                "Must select a database first with USE database_name;"));

        cmds.add(new SQLCommand("DESCRIBE", "DDL",
                "Shows the structure of a table",
                "DESCRIBE students;\nDESC students;\nSHOW COLUMNS FROM students;",
                "Shows columns, types, null constraints, keys, defaults."));

        // DML
        cmds.add(new SQLCommand("INSERT INTO", "DML",
                "Adds new rows to a table",
                "INSERT INTO students (name, age) VALUES ('Alice', 16);\n\n-- Multiple rows\nINSERT INTO students (name, age) VALUES\n  ('Bob', 17), ('Carol', 15);",
                "Column order must match value order."));

        cmds.add(new SQLCommand("UPDATE", "DML",
                "Modifies existing rows in a table",
                "UPDATE students SET age = 17 WHERE id = 1;\nUPDATE students SET age = 18, grade = '12th' WHERE name = 'Alice';",
                "⚠️ Always use WHERE! Without it, ALL rows will be updated."));

        cmds.add(new SQLCommand("DELETE", "DML",
                "Removes rows from a table",
                "DELETE FROM students WHERE id = 1;\nDELETE FROM students WHERE age < 10;",
                "⚠️ Without WHERE, ALL rows are deleted!"));

        cmds.add(new SQLCommand("REPLACE INTO", "DML",
                "Inserts or replaces if primary key conflicts",
                "REPLACE INTO students (id, name, age) VALUES (1, 'Alice Updated', 17);",
                "Deletes and re-inserts if PK exists. Otherwise inserts."));

        // DQL
        cmds.add(new SQLCommand("SELECT", "DQL",
                "Retrieves data from one or more tables",
                "SELECT * FROM students;\nSELECT name, age FROM students;\nSELECT name AS student_name, age AS years FROM students;",
                "Use explicit column names instead of * in production."));

        cmds.add(new SQLCommand("JOIN", "DQL",
                "Combines rows from multiple tables",
                "-- INNER JOIN\nSELECT s.name, c.course_name\nFROM students s\nINNER JOIN enrollments e ON s.id = e.student_id\nINNER JOIN courses c ON e.course_id = c.id;\n\n-- LEFT JOIN\nSELECT s.name, e.grade\nFROM students s LEFT JOIN enrollments e ON s.id = e.student_id;",
                "INNER: only matching. LEFT: all from left + matching right."));

        cmds.add(new SQLCommand("SUBQUERY", "DQL",
                "A SELECT nested inside another query",
                "SELECT name FROM students\nWHERE id IN (SELECT student_id FROM enrollments WHERE grade = 'A');\n\nSELECT name,\n  (SELECT COUNT(*) FROM enrollments WHERE student_id = s.id) AS courses\nFROM students s;",
                "Can appear in SELECT, WHERE, FROM, HAVING."));

        cmds.add(new SQLCommand("CASE", "DQL",
                "Conditional logic inside queries",
                "SELECT name,\n  CASE\n    WHEN age < 13 THEN 'Child'\n    WHEN age < 18 THEN 'Teen'\n    ELSE 'Adult'\n  END AS age_group\nFROM students;",
                "Like an if-else in SQL. Great for data transformation."));

        // Clauses
        cmds.add(new SQLCommand("WHERE", "Clauses",
                "Filters rows based on conditions",
                "SELECT * FROM students WHERE age > 16;\nSELECT * FROM students WHERE name LIKE 'A%';\nSELECT * FROM students WHERE age BETWEEN 15 AND 18;\nSELECT * FROM students WHERE grade IN ('10th','11th');",
                "% = any chars, _ = single char in LIKE patterns."));

        cmds.add(new SQLCommand("ORDER BY", "Clauses",
                "Sorts the result set",
                "SELECT * FROM students ORDER BY age;\nSELECT * FROM students ORDER BY age DESC;\nSELECT * FROM students ORDER BY grade ASC, age DESC;",
                "Default is ASC. Chain multiple columns separated by commas."));

        cmds.add(new SQLCommand("GROUP BY", "Clauses",
                "Groups rows with matching values",
                "SELECT grade, COUNT(*) AS total FROM students GROUP BY grade;\nSELECT grade, AVG(age) FROM students GROUP BY grade HAVING AVG(age) > 16;",
                "Only GROUP BY columns or aggregates can be SELECTed."));

        cmds.add(new SQLCommand("HAVING", "Clauses",
                "Filters groups after GROUP BY (like WHERE for groups)",
                "SELECT grade, COUNT(*) AS total\nFROM students\nGROUP BY grade\nHAVING COUNT(*) > 2;",
                "WHERE filters rows before grouping; HAVING filters groups after."));

        cmds.add(new SQLCommand("LIMIT & OFFSET", "Clauses",
                "Controls the number of rows returned",
                "SELECT * FROM students LIMIT 10;\nSELECT * FROM students LIMIT 10 OFFSET 10; -- page 2\nSELECT * FROM students ORDER BY id DESC LIMIT 5;",
                "OFFSET (N-1)*LIMIT for page N. Always ORDER BY with LIMIT."));

        // DCL
        cmds.add(new SQLCommand("GRANT", "DCL",
                "Gives privileges to users (MySQL syntax)",
                "GRANT SELECT, INSERT ON students TO 'user'@'localhost';\nGRANT ALL PRIVILEGES ON school_db.* TO 'admin'@'%';",
                "MySQL-specific. SQLite has no user management."));

        cmds.add(new SQLCommand("REVOKE", "DCL",
                "Removes privileges from users",
                "REVOKE INSERT ON students FROM 'user'@'localhost';\nREVOKE ALL PRIVILEGES ON school_db.* FROM 'admin'@'%';",
                "Removes previously granted permissions."));

        cmds.add(new SQLCommand("TRANSACTIONS", "DDL",
                "Group operations atomically with BEGIN/COMMIT/ROLLBACK",
                "BEGIN;\nUPDATE accounts SET balance = balance - 500 WHERE id = 1;\nUPDATE accounts SET balance = balance + 500 WHERE id = 2;\nCOMMIT; -- or ROLLBACK;",
                "If any step fails, ROLLBACK undoes everything. Essential for data integrity."));

        // Functions
        cmds.add(new SQLCommand("COUNT()", "Functions",
                "Counts the number of rows",
                "SELECT COUNT(*) FROM students;\nSELECT COUNT(email) FROM students;\nSELECT COUNT(DISTINCT grade) FROM students;",
                "COUNT(*) includes NULLs; COUNT(col) excludes them."));

        cmds.add(new SQLCommand("SUM() / AVG()", "Functions",
                "Sum and average of numeric values",
                "SELECT SUM(salary) FROM employees;\nSELECT AVG(age) FROM students;\nSELECT department, AVG(salary) FROM employees GROUP BY department;",
                "Both ignore NULL values automatically."));

        cmds.add(new SQLCommand("MIN() / MAX()", "Functions",
                "Minimum and maximum values in a column",
                "SELECT MIN(age), MAX(age) FROM students;\nSELECT MAX(salary) FROM employees;",
                "Works with numbers, text, and dates."));

        cmds.add(new SQLCommand("String Functions", "Functions",
                "Text manipulation: UPPER, LOWER, LENGTH, SUBSTR, REPLACE, TRIM",
                "SELECT UPPER(name) FROM students;\nSELECT LENGTH(name) FROM students;\nSELECT SUBSTR(name, 1, 3) FROM students;\nSELECT TRIM('  hello  ');\nSELECT REPLACE(name, 'old', 'new') FROM students;",
                "SQLite uses SUBSTR instead of SUBSTRING."));

        cmds.add(new SQLCommand("Date Functions", "Functions",
                "Date and time operations",
                "SELECT date('now');\nSELECT datetime('now');\nSELECT strftime('%Y-%m', date_col) FROM orders;\nSELECT date('now', '+7 days');",
                "SQLite stores dates as TEXT. Use strftime() for formatting."));

        cmds.add(new SQLCommand("COALESCE() / IFNULL()", "Functions",
                "Handle NULL values gracefully",
                "SELECT COALESCE(phone, 'N/A') FROM students;\nSELECT IFNULL(email, 'no email') FROM customers;\nSELECT COALESCE(col1, col2, col3, 'fallback') FROM t;",
                "COALESCE takes first non-NULL. IFNULL is a 2-arg shorthand."));

        cmds.add(new SQLCommand("CAST() / TYPEOF()", "Functions",
                "Type conversion and type inspection",
                "SELECT CAST('42' AS INTEGER);\nSELECT CAST(price AS TEXT) FROM products;\nSELECT TYPEOF(age) FROM students;",
                "SQLite is dynamically typed; CAST forces interpretation."));

        return cmds;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
