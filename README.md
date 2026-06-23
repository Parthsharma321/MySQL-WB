<div align="center">

<img src="screenshots/Icon.png" width="120" height="120" alt="MySQL WB Logo"/>

# 🗄️ MySQL WB — Mobile SQL Workspace

**A full-featured offline SQL learning app for Android, inspired by MySQL Workbench and Adminer.**

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android)](https://android.com)
[![API](https://img.shields.io/badge/Min%20API-24%20(Android%207.0)-blue)](https://developer.android.com)
[![SQLite](https://img.shields.io/badge/Database-SQLite-003B57?style=flat&logo=sqlite)](https://sqlite.org)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.0.0-orange)](https://github.com)

*Learn SQL · DBMS · RDBMS — completely offline. No server. No internet. No setup.*

---

</div>

## 📱 Screenshots

| Dashboard |    Query Editor   |   Results   |   Tutorial   |
|---------------|-----------------|-------------|-------------------|
| ![Dashboard](<img src="screenshots/1.jpg" width="160"/>) | ![Editor](<img src="screenshots/6.jpg" width="160"/>) | ![Results](<img src="screenshots/3.jpg" width="160"/>) | ![Tutorial](<img src="screenshots/4.jpg" width="160"/>) |

---

## ✨ Features

### 🖊️ SQL Query Editor
- Full-featured SQL editor with **syntax highlighting**
- Execute **DDL, DML, DQL, DCL, TCL** commands
- Smart query execution — runs the statement at the cursor (MySQL Workbench style)
- **Run selected** text only
- **Format SQL** button to auto-indent queries
- **Undo / Redo** support
- Query auto-saved as draft per database

### 📊 Results & Output Panel
- Clean tabular output with **alternating row colors**
- Displays **row count** and **execution time**
- Clear error messages with error details
- **Copy results** to clipboard

### 🗂️ Schema Explorer
- Browse all tables and columns visually
- Tap any table to instantly run `SELECT * FROM table`
- Shows column names, data types, and constraints

### 🛠️ Table Designer
- Create tables **without writing SQL**
- Add columns with name, type, PRIMARY KEY, NOT NULL, AUTO INCREMENT
- Live SQL preview before creating
- One-tap table creation

### 📚 Tutorials (10 Lessons)
- What is SQL, DBMS, RDBMS?
- DDL — CREATE, ALTER, DROP
- DML — INSERT, UPDATE, DELETE
- DQL — SELECT, JOINs, GROUP BY
- Subqueries, Indexes, Transactions
- **"Try in Editor"** button opens example directly in the query editor

### 📋 SQL Command Reference
- 25+ commands documented with syntax and tips
- Categorized: **DDL · DML · DQL · DCL · Clauses · Functions**
- Search and filter by category
- Code snippets for every command

### 🗄️ Sample Databases (3 built-in)
| Database | Tables | Use Case |
|---|---|---|
| `school_db` | students, teachers, courses, enrollments | JOINs, GROUP BY practice |
| `ecommerce_db` | products, customers, orders, order_items | Subqueries, aggregates |
| `company_db` | employees, departments, projects | Self-JOINs, complex queries |

### 🕐 Query History
- Every executed query is saved automatically
- Searchable history with success/error status
- Tap any query to reload it instantly

### 🌙 Dark / Light Theme
- Full Material Design 3 dark mode support
- Toggle in Settings

### 💾 Export & Backup
- **Export SQL** — generates MySQL-compatible `.sql` dump to Downloads folder
- **Backup .db** — copies raw SQLite file to device storage
- **Restore** — import `.db` backup files

---

## 🔤 MySQL Syntax Support

MySQL WB accepts **MySQL syntax** and automatically translates it to SQLite:

| MySQL | Works in MySQL WB |
|-------|------------------|
| `AUTO_INCREMENT` | ✅ |
| `INT(11)`, `TINYINT`, `BIGINT` | ✅ |
| `ENGINE=InnoDB`, `CHARSET=utf8` | ✅ (ignored safely) |
| `SHOW DATABASES` | ✅ |
| `SHOW TABLES` | ✅ |
| `DESCRIBE table` | ✅ |
| `USE database` | ✅ |
| `TRUNCATE TABLE` | ✅ |
| `RENAME TABLE` | ✅ |
| `ALTER TABLE ... MODIFY COLUMN` | ✅ |
| `ALTER TABLE ... CHANGE COLUMN` | ✅ |
| `ALTER TABLE ... DROP COLUMN` | ✅ |
| `NOW()`, `CURDATE()`, `CURTIME()` | ✅ |
| `INSERT IGNORE` | ✅ |
| `LIMIT x, y` | ✅ |
| `IFNULL()`, `IF()`, `CONCAT()` | ✅ |
| `SHOW VARIABLES`, `SHOW STATUS` | ✅ |
| `ANALYZE TABLE`, `OPTIMIZE TABLE` | ✅ |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio **Hedgehog (2023.1.1)** or newer
- Android SDK **34**
- JDK **11** or newer
- Android device or emulator — **API 24+** (Android 7.0+)

### Installation

```bash
# Clone the repository
git clone https://github.com/yourusername/MySQLWB.git

# Open in Android Studio
# File → Open → select the MySQLWB folder

# Create local.properties (auto-created by Android Studio)
# Set your SDK path:
sdk.dir=/path/to/your/Android/sdk
```

### Build & Run
1. Open **Android Studio**
2. Click **File → Open** → select `MySQLWB` folder
3. Wait for **Gradle sync** to complete
4. Click **Run ▶** or press `Shift + F10`

### Quick Start (First Launch)
```
1. Tap "+ New Database"     → create your first database
2. Tap "Sample DBs"         → load a practice database
3. Tap "▶ New Query"        → open the SQL editor
4. Type your SQL and tap    → ▶ RUN
```

---

## 📁 Project Structure

```
MySQLWB/
├── app/src/main/
│   ├── java/com/mysqlwb/
│   │   ├── activities/          # 10 Activity screens
│   │   │   ├── SplashActivity.java
│   │   │   ├── DashboardActivity.java
│   │   │   ├── QueryEditorActivity.java
│   │   │   ├── TableDesignerActivity.java
│   │   │   ├── SchemaExplorerActivity.java
│   │   │   ├── TutorialsActivity.java
│   │   │   ├── TutorialDetailActivity.java
│   │   │   ├── SQLReferenceActivity.java
│   │   │   ├── QueryHistoryActivity.java
│   │   │   └── SettingsActivity.java
│   │   ├── adapters/            # RecyclerView adapters
│   │   ├── database/            # SQLite engine + history manager
│   │   │   ├── DatabaseEngine.java    # Core SQL execution + MySQL translation
│   │   │   └── HistoryManager.java
│   │   ├── models/              # Data models
│   │   └── utils/               # SQL highlighter, autocomplete, sample data
│   └── res/
│       ├── layout/              # 17 XML layouts
│       ├── drawable/            # Vector icons and backgrounds
│       ├── values/              # Colors, strings, styles, dimens
│       └── menu/                # Menu XML files
├── build.gradle
├── settings.gradle
└── README.md
```

---

## 🛠️ Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java |
| Database Engine | SQLite (Android built-in) |
| UI Framework | Material Design 3 |
| Min SDK | API 24 (Android 7.0) |
| Target SDK | API 34 (Android 14) |
| Architecture | Single-module, Activity-based |
| Internet Required | ❌ None — fully offline |

---

## 📖 SQL Commands Supported

| Category | Commands |
|----------|---------|
| **DDL** | `CREATE TABLE/DATABASE/INDEX/VIEW`, `ALTER TABLE`, `DROP TABLE/DATABASE`, `TRUNCATE`, `RENAME TABLE` |
| **DML** | `INSERT INTO`, `UPDATE`, `DELETE`, `REPLACE INTO`, `INSERT IGNORE` |
| **DQL** | `SELECT`, `JOIN` (INNER/LEFT/RIGHT), `GROUP BY`, `HAVING`, `ORDER BY`, `LIMIT`, `SUBQUERY`, `CASE` |
| **DCL** | `GRANT`, `REVOKE` (simulated) |
| **TCL** | `BEGIN`, `COMMIT`, `ROLLBACK` |
| **MySQL** | `SHOW DATABASES/TABLES/COLUMNS`, `DESCRIBE`, `USE`, `SHOW INDEXES`, `SHOW CREATE TABLE` |
| **Functions** | `COUNT`, `SUM`, `AVG`, `MIN`, `MAX`, `NOW`, `DATE_FORMAT`, `CONCAT`, `IFNULL`, `GROUP_CONCAT`, and 80+ more |

---

## ⚙️ Settings

| Setting | Description |
|---------|-------------|
| Dark Mode | Toggle dark/light theme |
| Editor Font Size | Adjustable from 10sp to 20sp |
| Syntax Highlighting | Enable/disable color coding |
| Auto-Complete | SQL keyword suggestions |
| Clear History | Delete all query history |

---

## 🤝 Contributing

Contributions are welcome! Here's how to get started:

```bash
# Fork the repo and clone your fork
git clone https://github.com/yourusername/MySQLWB.git

# Create a feature branch
git checkout -b feature/your-feature-name

# Make your changes, then commit
git add .
git commit -m "feat: describe your change"

# Push and open a Pull Request
git push origin feature/your-feature-name
```

### Areas open for contribution
- 🐛 Bug fixes
- 🌐 New language translations
- 📚 More tutorial lessons
- 🎨 UI improvements
- 📊 Additional sample databases
- ⚡ Performance optimizations
- 🔌 Remote MySQL/MariaDB server connection feature

---

## 🐛 Known Limitations

| Limitation | Reason | Workaround |
|-----------|--------|------------|
| `RIGHT JOIN` not supported | SQLite limitation | Use `LEFT JOIN` with tables swapped |
| `FULL OUTER JOIN` not supported | SQLite limitation | Combine `LEFT JOIN` + `UNION` |
| Stored procedures (`CREATE PROCEDURE`) | SQLite has no stored procs | Use direct SQL statements |
| User management (`CREATE USER`, `GRANT`) | SQLite has no user system | Simulated with feedback messages |
| `DATE_FORMAT()` | SQLite uses `strftime()` instead | Use `strftime('%Y-%m-%d', col)` |

---

## 📄 License

```
MIT License

Copyright (c) 2026 MySQL WB

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

## 🙏 Acknowledgements

- Inspired by **MySQL Workbench** (Oracle) and **Adminer**
- Built with **Android Jetpack** and **Material Design 3**
- Powered by **SQLite** — the world's most widely deployed database engine

---

<div align="center">

**Made with ❤️ for SQL learners everywhere**

⭐ Star this repo if MySQL WB helped you learn SQL!

</div>
