package com.courser;

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

class Server {
    static void serve(int PORT) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", exchange -> respond(exchange, "template/index.html", Map.of("H", "H")));
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

    private static void respond(HttpExchange exchange, String templatePath, Map<String, String> values)
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
