package com.courser.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

public class Server {
    public static void serve(int PORT) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", exchange -> respond(exchange, "template/index.html", Map.of()));
        server.createContext("/admin",
                exchange -> ServerHandles.handleAdminDashboard(exchange, "template/admin/dashboard.html"));
        server.createContext("/admin/students",
                exchange -> ServerHandles.handleAdminStudents(exchange, "template/admin/students.html"));
        server.createContext("/admin/students/add",
                exchange -> ServerHandles.handleAdminStudentsAdd(exchange, "template/admin/student-add.html"));
        server.createContext("/admin/students/edit",
                exchange -> ServerHandles.handleAdminStudentsEdit(exchange, "template/admin/student-edit.html"));
        server.createContext("/admin/courses",
                exchange -> ServerHandles.handleAdminCourses(exchange, "template/admin/courses.html"));
        server.createContext("/admin/courses/add",
                exchange -> respond(exchange, "template/admin/course-add.html", Map.of("H", "H")));
        server.createContext("/admin/courses/edit",
                exchange -> respond(exchange, "template/admin/course-edit.html", Map.of("H", "H")));
        server.createContext("/admin/registrations",
                exchange -> respond(exchange, "template/admin/registrations.html", Map.of("H", "H")));
        server.setExecutor(null);
        server.start();
        System.out.println("Server started listening on: " + PORT);
    }

    private static final Map<String, String> templateCache = new ConcurrentHashMap<>();

    private static String loadTemplate(String path) {
        return templateCache.computeIfAbsent(path, p -> {
            try (InputStream in = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream(p)) {

                if (in == null) {
                    throw new IOException("Resource not found: " + p);
                }

                return new String(in.readAllBytes(), StandardCharsets.UTF_8);

            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    static void redirect(HttpExchange exchange, String location)
            throws IOException {

        exchange.getResponseHeaders().add("Location", location);

        exchange.sendResponseHeaders(302, -1);

        exchange.close();
    }

    static void respond(HttpExchange exchange, String templatePath, Map<String, String> values)
            throws IOException {
        String template = loadTemplate(templatePath);
        String html = fillTemplate(template, values);
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }

    }

    private static String fillTemplate(String template, Map<String, String> values) {
        String result = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }
}
