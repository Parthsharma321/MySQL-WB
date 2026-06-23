package com.mysqlwb.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mysqlwb.R;
import com.mysqlwb.adapters.TutorialAdapter;
import com.mysqlwb.models.Tutorial;

import java.util.ArrayList;
import java.util.List;

public class TutorialsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorials);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("SQL Tutorials");
        }

        RecyclerView rv = findViewById(R.id.rv_tutorials);
        rv.setLayoutManager(new LinearLayoutManager(this));

        List<Tutorial> tutorials = buildTutorials();
        TutorialAdapter adapter = new TutorialAdapter(this, tutorials, tutorial -> {
            Intent intent = new Intent(this, TutorialDetailActivity.class);
            intent.putExtra("tutorial_index", tutorials.indexOf(tutorial));
            startActivity(intent);
        });
        rv.setAdapter(adapter);
    }

    public static List<Tutorial> buildTutorials() {
        List<Tutorial> list = new ArrayList<>();

        // === 1. BASICS & DBMS ===
        list.add(new Tutorial(
                "1. Introduction to DBMS",
                "Beginner",
                "Understand core concepts of Databases and Management Systems",
                "🏠",
                "A <b>Database Management System (DBMS)</b> is software that interacts with users and the database itself.<br><br>" +
                "<b>The SQL Language:</b><br>" +
                "1. <b>DDL:</b> Definition (CREATE, ALTER, DROP)<br>" +
                "2. <b>DML:</b> Manipulation (INSERT, UPDATE, DELETE)<br>" +
                "3. <b>DQL:</b> Query (SELECT)<br>" +
                "4. <b>DCL:</b> Control (GRANT, REVOKE)",
                ""
        ));

        // === 2. DDL ===
        list.add(new Tutorial(
                "2. DDL: CREATE & Data Types",
                "Beginner",
                "Defining table structures",
                "🏗️",
                "<b>CREATE TABLE</b> defines columns and types.<br><br>" +
                "<b>Common Data Types:</b><br>" +
                "• <b>INT:</b> Integers.<br>" +
                "• <b>VARCHAR(n):</b> Text strings.<br>" +
                "• <b>DATE:</b> YYYY-MM-DD.<br>" +
                "• <b>DECIMAL:</b> Precise numbers.",
                "CREATE TABLE students (id INT PRIMARY KEY, name VARCHAR(50), age INT);"
        ));

        list.add(new Tutorial(
                "3. DDL: ALTER, RENAME & DROP",
                "Beginner",
                "Modifying structures",
                "🔧",
                "• <b>ALTER:</b> `ALTER TABLE students ADD phone TEXT;`<br>" +
                "• <b>RENAME:</b> `ALTER TABLE students RENAME TO learners;`<br>" +
                "• <b>DROP:</b> Deletes everything.<br>" +
                "• <b>TRUNCATE:</b> Deletes data only.",
                "ALTER TABLE students ADD COLUMN email TEXT;"
        ));

        // === 3. DML ===
        list.add(new Tutorial(
                "4. DML: INSERT INTO",
                "Beginner",
                "Adding data",
                "📥",
                "<b>INSERT INTO</b> adds new rows.<br>```sql<br>INSERT INTO students (id, name)<br>VALUES (1, 'Alice');<br>```",
                "INSERT INTO students (id, name, age) VALUES (101, 'John', 22);"
        ));

        list.add(new Tutorial(
                "5. DML: UPDATE & DELETE",
                "Beginner",
                "Modifying data",
                "✏️",
                "<b>UPDATE</b> changes data. <b>DELETE</b> removes it.<br>⚠️ <b>WARNING:</b> Always use <b>WHERE</b> or you will wipe the whole table!",
                "UPDATE students SET age = 25 WHERE id = 1;"
        ));

        // === 4. DQL ===
        list.add(new Tutorial(
                "6. DQL: SELECT Foundation",
                "Beginner",
                "Retrieving data",
                "🔍",
                "Fetch data using <b>SELECT</b>, <b>FROM</b>, and <b>WHERE</b>.",
                "SELECT * FROM students WHERE age > 15 ORDER BY name ASC;"
        ));

        list.add(new Tutorial(
                "7. Filtering & Operators",
                "Beginner",
                "Mastering filters",
                "🔎",
                "Use <b>AND</b>, <b>OR</b>, <b>IN</b>, <b>BETWEEN</b>, and <b>LIKE</b> for filtering.",
                "SELECT * FROM students WHERE name LIKE 'A%' AND age BETWEEN 10 AND 20;"
        ));

        list.add(new Tutorial(
                "8. Understanding JOINs",
                "Intermediate",
                "Connecting tables",
                "🔗",
                "Link tables using <b>INNER JOIN</b>, <b>LEFT JOIN</b>, and <b>RIGHT JOIN</b>.",
                "SELECT s.name, c.course_name FROM students s JOIN enrollments e ON s.id = e.student_id JOIN courses c ON e.course_id = c.id;"
        ));

        // === 5. DCL ===
        list.add(new Tutorial(
                "9. DCL: Security & Control",
                "Intermediate",
                "User permissions",
                "🔐",
                "<b>GRANT</b> gives access. <b>REVOKE</b> takes it away.<br><br>`GRANT SELECT ON db.* TO 'user';`",
                ""
        ));

        // === 6. FUNCTIONS ===
        list.add(new Tutorial(
                "10. String Functions",
                "Beginner",
                "Text manipulation",
                "abc",
                "Use <b>UPPER()</b>, <b>LOWER()</b>, <b>LENGTH()</b>, and <b>SUBSTR()</b>.",
                "SELECT name, UPPER(name), LENGTH(name) FROM students;"
        ));

        list.add(new Tutorial(
                "11. Numeric & Math Functions",
                "Beginner",
                "Calculations",
                "🔢",
                "Use <b>ABS()</b>, <b>ROUND()</b>, <b>SQRT()</b>, and <b>MOD()</b>.",
                "SELECT id, ABS(-10), ROUND(3.14159, 2);"
        ));

        list.add(new Tutorial(
                "12. Date & Time Functions",
                "Beginner",
                "Time management",
                "📅",
                "Use <b>NOW()</b>, <b>CURDATE()</b>, and <b>DATEDIFF()</b>.",
                "SELECT NOW(), CURDATE();"
        ));

        list.add(new Tutorial(
                "13. Aggregate Functions",
                "Intermediate",
                "Summaries",
                "📊",
                "Use <b>COUNT()</b>, <b>SUM()</b>, <b>AVG()</b>, <b>MAX()</b>, and <b>MIN()</b>.",
                "SELECT COUNT(*), AVG(age) FROM students;"
        ));

        list.add(new Tutorial(
                "14. Logic: IF & CASE",
                "Intermediate",
                "Conditional SQL",
                "⚙️",
                "Use <b>CASE WHEN ... THEN ... ELSE END</b> for logic in SELECT.",
                "SELECT name, CASE WHEN age >= 18 THEN 'Adult' ELSE 'Minor' END as status FROM students;"
        ));

        // === 7. ADVANCED ===
        list.add(new Tutorial(
                "15. GROUP BY & HAVING",
                "Intermediate",
                "Summary grouping",
                "📁",
                "Group results with <b>GROUP BY</b> and filter groups with <b>HAVING</b>.",
                "SELECT grade, COUNT(*) FROM students GROUP BY grade HAVING COUNT(*) > 0;"
        ));

        list.add(new Tutorial(
                "16. Subqueries",
                "Advanced",
                "Nested queries",
                "🧩",
                "Write queries inside queries for complex data requirements.",
                "SELECT name FROM students WHERE age > (SELECT AVG(age) FROM students);"
        ));

        list.add(new Tutorial(
                "17. Indexes & Performance",
                "Advanced",
                "Speeding up lookups",
                "⚡",
                "<b>CREATE INDEX</b> to speed up searches on large tables.",
                "CREATE INDEX idx_name ON students(name);"
        ));

        list.add(new Tutorial(
                "18. Database Constraints",
                "Intermediate",
                "Data Integrity",
                "🛡️",
                "Rules like <b>NOT NULL</b>, <b>UNIQUE</b>, and <b>CHECK</b> keep data valid.",
                "CREATE TABLE users (id INT PRIMARY KEY, age INT CHECK(age > 0));"
        ));

        list.add(new Tutorial(
                "19. Views & Virtual Tables",
                "Advanced",
                "Reusing SELECTs",
                "🖼️",
                "Save complex SELECTs as <b>VIEW</b> for easy reuse.",
                "CREATE VIEW v_active AS SELECT * FROM students WHERE age > 10;"
        ));

        list.add(new Tutorial(
                "20. ACID & Transactions",
                "Advanced",
                "Reliability",
                "💎",
                "Use <b>COMMIT</b> and <b>ROLLBACK</b> to ensure consistency.",
                "BEGIN TRANSACTION; UPDATE students SET age = 20; ROLLBACK;"
        ));

        list.add(new Tutorial(
                "21. Relationships (PK/FK)",
                "Intermediate",
                "Linking tables",
                "🔑",
                "<b>Primary Key</b> is unique. <b>Foreign Key</b> links to another table.",
                "SELECT * FROM courses WHERE teacher_id IS NOT NULL;"
        ));

        return list;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
