package com.courser.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import com.courser.utils.Database;

public class SummaryService {
    public static Map<String, String> getSummary() {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM students) AS total_students,
                    (SELECT COUNT(*) FROM courses) AS total_courses,
                    (SELECT COUNT(*) FROM reg_courses) AS total_registrations;
                 """;
        Map<String, String> m = new HashMap<String, String>();
        try {
            Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int students = rs.getInt("total_students");
                int courses = rs.getInt("total_courses");
                int registrations = rs.getInt("total_registrations");
                m.put("studentCount", String.valueOf(students));
                m.put("courseCount", String.valueOf(courses));
                m.put("registrationCount", String.valueOf(registrations));
            }
            stmt.close();

        } catch (Exception e) {
            System.out.println(e);
        }
        return m;

    }

}
