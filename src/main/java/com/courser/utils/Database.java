package com.courser.utils;

import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;

public class Database {

    private static final String URL = "jdbc:sqlite:courser.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void init() {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT UNIQUE NOT NULL,
                        name TEXT NOT NULL,
                        email TEXT NOT NULL
                        )
                    """);
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS students (
                        user_id INTEGER PRIMARY KEY,
                        student_id TEXT UNIQUE NOT NULL,
                        max_credits INTEGER NOT NULL,
                        FOREIGN KEY (user_id)
                            REFERENCES users(user_id)
                        )
                    """);
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS reg_courses (
                        user_id TEXT,
                        course_code TEXT,
                    PRIMARY KEY (user_id, course_code),
                    FOREIGN KEY (user_id)
                        REFERENCES students(user_id),
                    FOREIGN KEY (course_code)
                        REFERENCES courses(course_code)
                        )
                    """);
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS courses (
                        course_id INTEGER PRIMARY KEY,
                        course_code TEXT UNIQUE NOT NULL,
                        title TEXT NOT NULL,
                        credits INTEGER NOT NULL,
                        instructor_name TEXT NOT NULL
                        )
                    """);
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS course_prereq (
                        course_code INTEGER,
                        prerequisite_id INTEGER
                        )
                    """);

        } catch (Exception e) {
            System.out.println(e);
            return;
        }
    }
}
