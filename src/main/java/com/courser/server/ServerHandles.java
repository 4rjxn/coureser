package com.courser.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.courser.model.Student;
import com.courser.services.StudentServices;
import com.courser.services.SummaryService;
import com.sun.net.httpserver.HttpExchange;

class ServerHandles {
    static void handleAdminDashboard(HttpExchange exchange, String path) throws IOException {
        Map<String, String> summary = SummaryService.getSummary();
        Server.respond(exchange, path, summary);

    }

    static void handleAdminStudentsEdit(HttpExchange exchange, String path) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            Server.respond(exchange, path, Map.of());
        }
    }

    static void handleAdminStudentsAdd(HttpExchange exchange, String path) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            Server.respond(exchange, path, Map.of());
        }

        if ("POST".equals(exchange.getRequestMethod())) {

            Map<String, String> form = ServerUtils.getFormData(exchange);

            Student student = new Student(
                    0,
                    form.get("username"),
                    form.get("studentId"),
                    form.get("name"),
                    form.get("email"),
                    Integer.parseInt(form.get("maxCredits")), null);

            StudentServices.registerNewStudent(student, null);
            Server.redirect(exchange, "/admin/students");
            return;
        }
        exchange.sendResponseHeaders(405, -1);
    }

    static void handleAdminStudents(HttpExchange exchange, String path) throws IOException {
        Map<String, String> summary = SummaryService.getSummary();
        Map<String, String> params = ServerUtils.getOueryParams(exchange);
        Map<String, String> values = new HashMap<String, String>();
        int studentCount = Integer.valueOf(summary.get("studentCount"));
        final int limit = 10;
        int totalPages = (int) Math.ceil((double) studentCount / limit);
        String query = ServerUtils.getStringParam(params, "query", "");
        int page = ServerUtils.getIntParam(params, "page", 1);
        int offset = (page - 1) * limit;
        Student[] students = StudentServices.searchStudents(query, limit, offset);
        String rows = "";
        for (Student s : students) {
            rows += """
                    <tr>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%d</td>
                        <td>
                            <a href="/admin/students/edit?userid=%d">Edit</a>
                        </td>
                    </tr>
                            """.formatted(s.getStudentId(),
                    s.getName(),
                    s.getUserName(),
                    s.getEmail(),
                    s.getMaxCredits(),
                    s.getUserId());
        }
        values.putAll(Map.of(
                "studentRows", rows,
                "previousPage", "/admin/students?page=" + (page - 1),
                "currentPage", String.valueOf(page),
                "totalPages", String.valueOf(totalPages),
                "nextPage", "/admin/students?page=" + (page + 1),
                "studentCount", String.valueOf(studentCount)));
        Server.respond(exchange, path, values);
    }

}
