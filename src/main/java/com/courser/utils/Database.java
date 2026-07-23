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
        try {
            Statement stmt = getConnection().createStatement();
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT UNIQUE NOT NULL,
                        name TEXT NOT NULL,
                        email TEXT NOT NULL
                        )
                    """);

        } catch (Exception e) {
            System.out.println(e);
            return;
        }
    }
}
