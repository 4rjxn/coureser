package com.courser.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String URL = "jdbc:sqlite:courser.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void init() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("PRAGMA foreign_keys = ON;");

            // 1. Users table
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT UNIQUE NOT NULL,
                        name TEXT NOT NULL,
                        email TEXT NOT NULL
                    );
                    """);

            // 2. Students table
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS students (
                        user_id INTEGER PRIMARY KEY,
                        student_id TEXT UNIQUE NOT NULL,
                        max_credits INTEGER NOT NULL,
                        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                    );
                    """);

            // 3. Registrars table
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS registrars (
                        user_id INTEGER PRIMARY KEY,
                        registrar_id TEXT UNIQUE NOT NULL,
                        department TEXT,
                        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                    );
                    """);

            // 4. Courses table
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS courses (
                        course_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        course_code TEXT UNIQUE NOT NULL,
                        title TEXT NOT NULL,
                        credits INTEGER NOT NULL,
                        instructor_name TEXT NOT NULL,
                        capacity INTEGER DEFAULT 30
                    );
                    """);

            // Schema migration: Add capacity column to courses if it didn't exist in older versions
            try {
                stmt.execute("ALTER TABLE courses ADD COLUMN capacity INTEGER DEFAULT 30;");
            } catch (SQLException ignored) {
                // Column already exists
            }

            // 5. Course Prerequisites table
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS course_prereq (
                        course_code TEXT NOT NULL,
                        prerequisite_id TEXT NOT NULL,
                        PRIMARY KEY (course_code, prerequisite_id)
                    );
                    """);

            // 6. Completed Courses table (to verify prerequisites for student)
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS completed_courses (
                        student_id TEXT NOT NULL,
                        course_code TEXT NOT NULL,
                        completion_date TEXT,
                        PRIMARY KEY (student_id, course_code)
                    );
                    """);

            // 7. Course Registrations table
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS registrations (
                        registration_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        student_id TEXT NOT NULL,
                        course_code TEXT NOT NULL,
                        registration_date TEXT NOT NULL,
                        status TEXT NOT NULL DEFAULT 'ACTIVE'
                    );
                    """);

            // 8. reg_courses (maintained for backward compatibility)
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS reg_courses (
                        user_id TEXT,
                        course_code TEXT,
                        PRIMARY KEY (user_id, course_code)
                    );
                    """);

        } catch (Exception e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }
}
