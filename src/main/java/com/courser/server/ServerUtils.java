package com.courser.server;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

class ServerUtils {
    static Map<String, String> getOueryParams(HttpExchange exchange) {
        Map<String, String> params = new HashMap<String, String>();
        String query = exchange.getRequestURI().getQuery();
        if (query == null || query.isBlank()) {
            return params;
        }
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);

            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1
                    ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                    : "";

            params.put(key, value);
        }
        return params;

    }

    public static Map<String, String> getFormData(HttpExchange exchange) {
        String body;
        Map<String, String> data = new HashMap<>();
        try {
            body = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8);
        } catch (Exception e) {
            return data;
        }

        for (String pair : body.split("&")) {
            String[] kv = pair.split("=", 2);

            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1
                    ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                    : "";

            data.put(key, value);
        }

        return data;
    }

    public static int getIntParam(Map<String, String> params,
            String key,
            int defaultValue) {
        String value = params.get(key);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String getStringParam(Map<String, String> params,
            String key,
            String defaultValue) {

        String value = params.get(key);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }
}
