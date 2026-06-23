package com.mysqlwb.utils;

import com.mysqlwb.database.DatabaseEngine;

public class SampleDatabaseCreator {

    public static boolean createSchoolDatabase(DatabaseEngine engine) {
        engine.createDatabase("school_db");
        engine.openDatabase("school_db");

        String[] sqls = {
            "CREATE TABLE IF NOT EXISTS students (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, age INTEGER, grade TEXT, email TEXT UNIQUE, enrollment_date TEXT DEFAULT (date('now')))",
            "CREATE TABLE IF NOT EXISTS teachers (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, subject TEXT, experience_years INTEGER, salary REAL)",
            "CREATE TABLE IF NOT EXISTS courses (id INTEGER PRIMARY KEY AUTOINCREMENT, course_name TEXT NOT NULL, teacher_id INTEGER, credits INTEGER DEFAULT 3, FOREIGN KEY(teacher_id) REFERENCES teachers(id))",
            "CREATE TABLE IF NOT EXISTS enrollments (student_id INTEGER, course_id INTEGER, grade TEXT, PRIMARY KEY(student_id, course_id), FOREIGN KEY(student_id) REFERENCES students(id), FOREIGN KEY(course_id) REFERENCES courses(id))",
            "INSERT INTO teachers (name, subject, experience_years, salary) VALUES ('Dr. Alice Smith', 'Mathematics', 12, 75000.00)",
            "INSERT INTO teachers (name, subject, experience_years, salary) VALUES ('Prof. Bob Johnson', 'Physics', 8, 65000.00)",
            "INSERT INTO teachers (name, subject, experience_years, salary) VALUES ('Ms. Carol Williams', 'English', 5, 55000.00)",
            "INSERT INTO teachers (name, subject, experience_years, salary) VALUES ('Mr. David Brown', 'Computer Science', 10, 80000.00)",
            "INSERT INTO students (name, age, grade, email) VALUES ('Emma Watson', 16, '10th', 'emma@school.edu')",
            "INSERT INTO students (name, age, grade, email) VALUES ('James Miller', 17, '11th', 'james@school.edu')",
            "INSERT INTO students (name, age, grade, email) VALUES ('Sophia Garcia', 15, '9th', 'sophia@school.edu')",
            "INSERT INTO students (name, age, grade, email) VALUES ('Liam Davis', 18, '12th', 'liam@school.edu')",
            "INSERT INTO students (name, age, grade, email) VALUES ('Olivia Martinez', 16, '10th', 'olivia@school.edu')",
            "INSERT INTO courses (course_name, teacher_id, credits) VALUES ('Algebra II', 1, 4)",
            "INSERT INTO courses (course_name, teacher_id, credits) VALUES ('Physics 101', 2, 3)",
            "INSERT INTO courses (course_name, teacher_id, credits) VALUES ('English Literature', 3, 3)",
            "INSERT INTO courses (course_name, teacher_id, credits) VALUES ('Introduction to CS', 4, 4)",
            "INSERT INTO enrollments (student_id, course_id, grade) VALUES (1, 1, 'A')",
            "INSERT INTO enrollments (student_id, course_id, grade) VALUES (1, 4, 'B+')",
            "INSERT INTO enrollments (student_id, course_id, grade) VALUES (2, 1, 'B')",
            "INSERT INTO enrollments (student_id, course_id, grade) VALUES (2, 2, 'A-')",
            "INSERT INTO enrollments (student_id, course_id, grade) VALUES (3, 3, 'A+')",
            "INSERT INTO enrollments (student_id, course_id, grade) VALUES (4, 4, 'A')",
            "INSERT INTO enrollments (student_id, course_id, grade) VALUES (5, 1, 'C+')"
        };

        for (String sql : sqls) {
            engine.executeQuery(sql);
        }
        return true;
    }

    public static boolean createEcommerceDatabase(DatabaseEngine engine) {
        engine.createDatabase("ecommerce_db");
        engine.openDatabase("ecommerce_db");

        String[] sqls = {
            "CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, description TEXT)",
            "CREATE TABLE IF NOT EXISTS products (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, price REAL NOT NULL, stock INTEGER DEFAULT 0, category_id INTEGER, description TEXT, FOREIGN KEY(category_id) REFERENCES categories(id))",
            "CREATE TABLE IF NOT EXISTS customers (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, email TEXT UNIQUE NOT NULL, phone TEXT, address TEXT, joined_date TEXT DEFAULT (date('now')))",
            "CREATE TABLE IF NOT EXISTS orders (id INTEGER PRIMARY KEY AUTOINCREMENT, customer_id INTEGER, order_date TEXT DEFAULT (datetime('now')), total_amount REAL, status TEXT DEFAULT 'pending', FOREIGN KEY(customer_id) REFERENCES customers(id))",
            "CREATE TABLE IF NOT EXISTS order_items (id INTEGER PRIMARY KEY AUTOINCREMENT, order_id INTEGER, product_id INTEGER, quantity INTEGER, unit_price REAL, FOREIGN KEY(order_id) REFERENCES orders(id), FOREIGN KEY(product_id) REFERENCES products(id))",
            "INSERT INTO categories (name, description) VALUES ('Electronics', 'Gadgets and devices')",
            "INSERT INTO categories (name, description) VALUES ('Books', 'Educational and fiction books')",
            "INSERT INTO categories (name, description) VALUES ('Clothing', 'Fashion and apparel')",
            "INSERT INTO products (name, price, stock, category_id, description) VALUES ('Smartphone X1', 29999.99, 50, 1, 'Latest model with 5G')",
            "INSERT INTO products (name, price, stock, category_id, description) VALUES ('Laptop Pro', 75000.00, 20, 1, '16GB RAM, 512GB SSD')",
            "INSERT INTO products (name, price, stock, category_id, description) VALUES ('SQL Fundamentals', 599.99, 100, 2, 'Learn SQL from scratch')",
            "INSERT INTO products (name, price, stock, category_id, description) VALUES ('Android Dev Guide', 799.99, 75, 2, 'Complete Android development')",
            "INSERT INTO products (name, price, stock, category_id, description) VALUES ('Cotton T-Shirt', 299.99, 200, 3, 'Comfortable everyday wear')",
            "INSERT INTO customers (name, email, phone, address) VALUES ('Raj Sharma', 'raj@example.com', '9876543210', 'Mumbai, Maharashtra')",
            "INSERT INTO customers (name, email, phone, address) VALUES ('Priya Patel', 'priya@example.com', '9876543211', 'Delhi, NCR')",
            "INSERT INTO customers (name, email, phone, address) VALUES ('Amit Singh', 'amit@example.com', '9876543212', 'Bangalore, Karnataka')",
            "INSERT INTO orders (customer_id, total_amount, status) VALUES (1, 30599.98, 'delivered')",
            "INSERT INTO orders (customer_id, total_amount, status) VALUES (2, 75000.00, 'shipped')",
            "INSERT INTO orders (customer_id, total_amount, status) VALUES (3, 1099.98, 'pending')",
            "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (1, 1, 1, 29999.99)",
            "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (1, 3, 1, 599.99)",
            "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (2, 2, 1, 75000.00)",
            "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (3, 3, 1, 599.99)",
            "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (3, 4, 1, 799.99)"
        };

        for (String sql : sqls) {
            engine.executeQuery(sql);
        }
        return true;
    }

    public static boolean createEmployeeDatabase(DatabaseEngine engine) {
        engine.createDatabase("company_db");
        engine.openDatabase("company_db");

        String[] sqls = {
            "CREATE TABLE IF NOT EXISTS departments (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, location TEXT, budget REAL)",
            "CREATE TABLE IF NOT EXISTS employees (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, department_id INTEGER, job_title TEXT, salary REAL, hire_date TEXT, manager_id INTEGER, FOREIGN KEY(department_id) REFERENCES departments(id), FOREIGN KEY(manager_id) REFERENCES employees(id))",
            "CREATE TABLE IF NOT EXISTS projects (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, start_date TEXT, end_date TEXT, department_id INTEGER, budget REAL, FOREIGN KEY(department_id) REFERENCES departments(id))",
            "CREATE TABLE IF NOT EXISTS employee_projects (employee_id INTEGER, project_id INTEGER, role TEXT, hours_worked INTEGER, PRIMARY KEY(employee_id, project_id))",
            "INSERT INTO departments (name, location, budget) VALUES ('Engineering', 'Floor 3', 5000000.00)",
            "INSERT INTO departments (name, location, budget) VALUES ('Marketing', 'Floor 2', 2000000.00)",
            "INSERT INTO departments (name, location, budget) VALUES ('HR', 'Floor 1', 1000000.00)",
            "INSERT INTO departments (name, location, budget) VALUES ('Finance', 'Floor 4', 1500000.00)",
            "INSERT INTO employees (name, department_id, job_title, salary, hire_date) VALUES ('Anil Kumar', 1, 'CTO', 250000.00, '2018-01-15')",
            "INSERT INTO employees (name, department_id, job_title, salary, hire_date, manager_id) VALUES ('Sunita Rao', 1, 'Senior Developer', 150000.00, '2019-03-10', 1)",
            "INSERT INTO employees (name, department_id, job_title, salary, hire_date, manager_id) VALUES ('Vikram Patel', 1, 'Developer', 100000.00, '2021-06-01', 2)",
            "INSERT INTO employees (name, department_id, job_title, salary, hire_date) VALUES ('Meena Iyer', 2, 'Marketing Head', 180000.00, '2017-09-20')",
            "INSERT INTO employees (name, department_id, job_title, salary, hire_date, manager_id) VALUES ('Kiran Shah', 2, 'Marketing Analyst', 80000.00, '2022-01-10', 4)",
            "INSERT INTO projects (name, start_date, end_date, department_id, budget) VALUES ('App Redesign', '2024-01-01', '2024-06-30', 1, 500000.00)",
            "INSERT INTO projects (name, start_date, end_date, department_id, budget) VALUES ('Digital Campaign', '2024-02-01', '2024-04-30', 2, 200000.00)",
            "INSERT INTO employee_projects (employee_id, project_id, role, hours_worked) VALUES (2, 1, 'Lead Developer', 480)",
            "INSERT INTO employee_projects (employee_id, project_id, role, hours_worked) VALUES (3, 1, 'Developer', 320)",
            "INSERT INTO employee_projects (employee_id, project_id, role, hours_worked) VALUES (5, 2, 'Campaign Manager', 200)"
        };

        for (String sql : sqls) {
            engine.executeQuery(sql);
        }
        return true;
    }

    public static boolean createHospitalDatabase(DatabaseEngine engine) {
        engine.createDatabase("hospital_db");
        engine.openDatabase("hospital_db");

        String[] sqls = {
            "CREATE TABLE IF NOT EXISTS patients (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, age INTEGER, gender TEXT, blood_group TEXT, contact TEXT)",
            "CREATE TABLE IF NOT EXISTS doctors (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, specialization TEXT, experience INTEGER, room_no TEXT)",
            "CREATE TABLE IF NOT EXISTS appointments (id INTEGER PRIMARY KEY AUTOINCREMENT, patient_id INTEGER, doctor_id INTEGER, appointment_date TEXT, status TEXT, FOREIGN KEY(patient_id) REFERENCES patients(id), FOREIGN KEY(doctor_id) REFERENCES doctors(id))",
            "CREATE TABLE IF NOT EXISTS prescriptions (id INTEGER PRIMARY KEY AUTOINCREMENT, appointment_id INTEGER, medicine_name TEXT, dosage TEXT, duration TEXT, FOREIGN KEY(appointment_id) REFERENCES appointments(id))",
            "INSERT INTO patients (name, age, gender, blood_group, contact) VALUES ('John Doe', 45, 'Male', 'O+', '9876543210')",
            "INSERT INTO patients (name, age, gender, blood_group, contact) VALUES ('Jane Smith', 32, 'Female', 'A-', '9876543211')",
            "INSERT INTO doctors (name, specialization, experience, room_no) VALUES ('Dr. House', 'Diagnostics', 20, '101A')",
            "INSERT INTO doctors (name, specialization, experience, room_no) VALUES ('Dr. Strange', 'Neurosurgeon', 15, '202B')",
            "INSERT INTO appointments (patient_id, doctor_id, appointment_date, status) VALUES (1, 1, '2024-05-20', 'completed')",
            "INSERT INTO appointments (patient_id, doctor_id, appointment_date, status) VALUES (2, 2, '2024-05-21', 'pending')",
            "INSERT INTO prescriptions (appointment_id, medicine_name, dosage, duration) VALUES (1, 'Paracetamol', '500mg', '3 days')",
            "INSERT INTO prescriptions (appointment_id, medicine_name, dosage, duration) VALUES (1, 'Amoxicillin', '250mg', '5 days')"
        };

        for (String sql : sqls) {
            engine.executeQuery(sql);
        }
        return true;
    }

    public static boolean createLibraryDatabase(DatabaseEngine engine) {
        engine.createDatabase("library_db");
        engine.openDatabase("library_db");

        String[] sqls = {
            "CREATE TABLE IF NOT EXISTS authors (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, country TEXT)",
            "CREATE TABLE IF NOT EXISTS books (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, author_id INTEGER, isbn TEXT, category TEXT, price REAL, FOREIGN KEY(author_id) REFERENCES authors(id))",
            "CREATE TABLE IF NOT EXISTS members (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, email TEXT, join_date TEXT)",
            "CREATE TABLE IF NOT EXISTS loans (id INTEGER PRIMARY KEY AUTOINCREMENT, book_id INTEGER, member_id INTEGER, loan_date TEXT, return_date TEXT, FOREIGN KEY(book_id) REFERENCES books(id), FOREIGN KEY(member_id) REFERENCES members(id))",
            "INSERT INTO authors (name, country) VALUES ('J.K. Rowling', 'UK')",
            "INSERT INTO authors (name, country) VALUES ('George R.R. Martin', 'USA')",
            "INSERT INTO books (title, author_id, isbn, category, price) VALUES ('Harry Potter and the Philosopher''s Stone', 1, '9780747532699', 'Fantasy', 599.0)",
            "INSERT INTO books (title, author_id, isbn, category, price) VALUES ('A Game of Thrones', 2, '9780553103540', 'Fantasy', 899.0)",
            "INSERT INTO members (name, email, join_date) VALUES ('Alice Wonderland', 'alice@example.com', '2023-01-15')",
            "INSERT INTO loans (book_id, member_id, loan_date, return_date) VALUES (1, 1, '2024-04-01', '2024-04-15')"
        };

        for (String sql : sqls) {
            engine.executeQuery(sql);
        }
        return true;
    }

    public static boolean createStockDatabase(DatabaseEngine engine) {
        engine.createDatabase("trading_db");
        engine.openDatabase("trading_db");

        String[] sqls = {
            "CREATE TABLE IF NOT EXISTS stocks (id INTEGER PRIMARY KEY AUTOINCREMENT, symbol TEXT UNIQUE, company_name TEXT, sector TEXT, current_price REAL)",
            "CREATE TABLE IF NOT EXISTS portfolios (id INTEGER PRIMARY KEY AUTOINCREMENT, user_name TEXT, balance REAL)",
            "CREATE TABLE IF NOT EXISTS transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, portfolio_id INTEGER, stock_id INTEGER, type TEXT, quantity INTEGER, price_at_time REAL, timestamp TEXT, FOREIGN KEY(portfolio_id) REFERENCES portfolios(id), FOREIGN KEY(stock_id) REFERENCES stocks(id))",
            "INSERT INTO stocks (symbol, company_name, sector, current_price) VALUES ('AAPL', 'Apple Inc.', 'Technology', 185.40)",
            "INSERT INTO stocks (symbol, company_name, sector, current_price) VALUES ('GOOGL', 'Alphabet Inc.', 'Technology', 140.20)",
            "INSERT INTO stocks (symbol, company_name, sector, current_price) VALUES ('TSLA', 'Tesla, Inc.', 'Automotive', 245.50)",
            "INSERT INTO stocks (symbol, company_name, sector, current_price) VALUES ('AMZN', 'Amazon.com, Inc.', 'Consumer Cyclical', 155.10)",
            "INSERT INTO portfolios (user_name, balance) VALUES ('Investor_One', 50000.0)",
            "INSERT INTO portfolios (user_name, balance) VALUES ('Day_Trader', 10000.0)",
            "INSERT INTO transactions (portfolio_id, stock_id, type, quantity, price_at_time, timestamp) VALUES (1, 1, 'BUY', 10, 180.0, '2024-01-10 10:30:00')",
            "INSERT INTO transactions (portfolio_id, stock_id, type, quantity, price_at_time, timestamp) VALUES (1, 2, 'BUY', 5, 135.0, '2024-01-12 11:45:00')",
            "INSERT INTO transactions (portfolio_id, stock_id, type, quantity, price_at_time, timestamp) VALUES (2, 3, 'SELL', 2, 250.0, '2024-02-01 09:15:00')"
        };

        for (String sql : sqls) {
            engine.executeQuery(sql);
        }
        return true;
    }

    public static boolean createMovieDatabase(DatabaseEngine engine) {
        engine.createDatabase("movies_db");
        engine.openDatabase("movies_db");

        String[] sqls = {
            "CREATE TABLE IF NOT EXISTS directors (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, nationality TEXT, birth_year INTEGER)",
            "CREATE TABLE IF NOT EXISTS movies (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, release_year INTEGER, genre TEXT, rating REAL, director_id INTEGER, FOREIGN KEY(director_id) REFERENCES directors(id))",
            "CREATE TABLE IF NOT EXISTS actors (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, gender TEXT)",
            "CREATE TABLE IF NOT EXISTS movie_cast (movie_id INTEGER, actor_id INTEGER, role_name TEXT, PRIMARY KEY(movie_id, actor_id), FOREIGN KEY(movie_id) REFERENCES movies(id), FOREIGN KEY(actor_id) REFERENCES actors(id))",
            "INSERT INTO directors (name, nationality, birth_year) VALUES ('Christopher Nolan', 'British', 1970)",
            "INSERT INTO directors (name, nationality, birth_year) VALUES ('Quentin Tarantino', 'American', 1963)",
            "INSERT INTO movies (title, release_year, genre, rating, director_id) VALUES ('Inception', 2010, 'Sci-Fi', 8.8, 1)",
            "INSERT INTO movies (title, release_year, genre, rating, director_id) VALUES ('Pulp Fiction', 1994, 'Crime', 8.9, 2)",
            "INSERT INTO actors (name, gender) VALUES ('Leonardo DiCaprio', 'Male')",
            "INSERT INTO actors (name, gender) VALUES ('Samuel L. Jackson', 'Male')",
            "INSERT INTO movie_cast (movie_id, actor_id, role_name) VALUES (1, 1, 'Cobb')",
            "INSERT INTO movie_cast (movie_id, actor_id, role_name) VALUES (2, 2, 'Jules Winnfield')"
        };

        for (String sql : sqls) {
            engine.executeQuery(sql);
        }
        return true;
    }

    public static void provisionByName(String name, DatabaseEngine engine) {
        if (name == null) return;
        switch (name) {
            case "school_db":    createSchoolDatabase(engine); break;
            case "ecommerce_db": createEcommerceDatabase(engine); break;
            case "company_db":   createEmployeeDatabase(engine); break;
            case "hospital_db":  createHospitalDatabase(engine); break;
            case "library_db":   createLibraryDatabase(engine); break;
            case "trading_db":   createStockDatabase(engine); break;
            case "movies_db":    createMovieDatabase(engine); break;
        }
    }
}
