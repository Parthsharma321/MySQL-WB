# 🗄️ MySQL WB — Mobile SQL Workspace

A full-featured Android application inspired by MySQL Workbench and Adminer,
built for learning and practicing SQL, DBMS, and RDBMS concepts.

## ✨ Features

### Core Features
- **SQL Query Editor** — syntax-highlighted, monospace editor with template chips
- **Query Execution** — full DDL, DML, DQL, DCL support via SQLite engine
- **Results Table** — scrollable tabular output with alternating rows
- **Query History** — searchable log of all executed queries with status
- **Schema Explorer** — browse databases, tables, and column details
- **Table Designer** — visual GUI to create tables without writing SQL

### Learning Features
- **10 Beginner–Advanced Tutorials** — What is SQL? → JOINs → Subqueries → Indexes
- **SQL Command Reference** — 25+ commands with syntax, description, and tips
- **3 Sample Databases** — School DB, E-Commerce DB, Company/HR DB pre-loaded with data

### App Features
- **Dark / Light Theme** — full Material Design dark mode support
- **Export SQL** — generates MySQL-compatible .sql dump to Downloads folder
- **Backup / Restore** — copies .db file to device storage
- **Offline Mode** — zero internet, zero server required
- **MySQL Syntax Translation** — AUTO_INCREMENT, INT(11), ENGINE=InnoDB all accepted

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 34 (API level 34)
- JDK 11 or newer
- Minimum Android device: API 24 (Android 7.0 Nougat)

### Setup Steps

1. **Clone / unzip** this project
2. **Set SDK path** — copy `local.properties.template` to `local.properties`  
   and set `sdk.dir` to your Android SDK location:
   ```
   sdk.dir=/Users/you/Library/Android/sdk          # macOS
   sdk.dir=C:\\Users\\you\\AppData\\Local\\Android\\Sdk  # Windows
   sdk.dir=/home/you/Android/Sdk                    # Linux
   ```
3. **Get Gradle wrapper JAR** — run this once from the project root:
   ```bash
   # Option A: Android Studio does this automatically on first sync
   # Option B: Manual download
   mkdir -p gradle/wrapper
   curl -L -o gradle/wrapper/gradle-wrapper.jar \
     https://github.com/gradle/gradle/raw/v8.4.0/gradle/wrapper/gradle-wrapper.jar
   ```
4. **Open in Android Studio** → File → Open → select the `MySQLWB` folder
5. **Let Gradle sync** (downloads ~50MB of standard dependencies)
6. **Run** on emulator or physical device (API 24+)

---

## 📁 Project Structure

```
MySQLWB/
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/mysqlwb/
│   │   ├── activities/        # 10 Activities (screens)
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
│   │   ├── adapters/          # 5 RecyclerView Adapters
│   │   ├── database/          # DatabaseEngine + HistoryManager
│   │   ├── models/            # 7 Data Models
│   │   └── utils/             # SQLHighlighter, AutoComplete, SampleDB
│   └── res/
│       ├── layout/            # 17 XML layouts
│       ├── drawable/          # 31 vector drawables
│       ├── menu/              # 3 menu files
│       ├── anim/              # 6 animations
│       ├── values/            # colors, strings, styles, dimens
│       └── xml/               # FileProvider, backup rules
```

---

## 🧪 Sample Databases

Load via Dashboard → **Sample DBs**:

| Database | Tables | Description |
|---|---|---|
| `school_db` | students, teachers, courses, enrollments | JOINs, GROUP BY practice |
| `ecommerce_db` | products, customers, orders, order_items | Subqueries, aggregates |
| `company_db` | employees, departments, projects | Self-JOINs, complex queries |

---

## 📝 SQL Commands Supported

| Category | Commands |
|---|---|
| **DDL** | CREATE TABLE/DATABASE/INDEX/VIEW, ALTER TABLE, DROP TABLE/DATABASE |
| **DML** | INSERT INTO, UPDATE, DELETE, REPLACE INTO |
| **DQL** | SELECT, JOIN (INNER/LEFT/RIGHT), SUBQUERY, GROUP BY, HAVING, ORDER BY, LIMIT |
| **DCL** | GRANT, REVOKE (documented; SQLite has no user system) |
| **TCL** | BEGIN, COMMIT, ROLLBACK |
| **MySQL extras** | SHOW DATABASES, SHOW TABLES, DESCRIBE, SHOW COLUMNS, USE, SHOW INDEXES |

---

## 🎨 Architecture

- **Language**: Java (Android)
- **Database Engine**: SQLite (via Android's built-in `android.database.sqlite`)
- **UI**: Material Design 3 (MaterialComponents)
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **No internet required** — fully offline

---

## 📄 License

MIT License — free for educational and personal use.
