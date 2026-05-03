package com.anticheat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class HttpHelper {

    private static final Gson gson = new Gson();
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();
    private static final Type LIST_TYPE = new TypeToken<List<Map<String, Object>>>() {}.getType();

    private final String apiBaseUrl;
    private final String apiKey;
    private final Logger logger;

    public HttpHelper(String apiBaseUrl, String apiKey, Logger logger) {
        this.apiBaseUrl = apiBaseUrl;
        this.apiKey = apiKey;
        this.logger = logger;
    }

    public Map<String, Object> get(String path) {
        return request("GET", path, null, false, MAP_TYPE);
    }

    public List<Map<String, Object>> getList(String path) {
        return request("GET", path, null, false, LIST_TYPE);
    }

    public Map<String, Object> post(String path, Map<String, Object> body) {
        return request("POST", path, body, false, MAP_TYPE);
    }

    public Map<String, Object> postWithRetry(String path, Map<String, Object> body) {
        return request("POST", path, body, true, MAP_TYPE);
    }

    @SuppressWarnings("unchecked")
    private <T> T request(String method, String path, Map<String, Object> body, boolean retry, Type responseType) {
        int attempts = retry ? MAX_RETRIES : 1;

        for (int i = 0; i < attempts; i++) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(apiBaseUrl + path);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);
                conn.setRequestProperty("X-Api-Key", apiKey);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);

                if (body != null && "POST".equals(method)) {
                    conn.setDoOutput(true);
                    String json = gson.toJson(body);
                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = json.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    String jsonResponse = readResponse(conn);
                    return gson.fromJson(jsonResponse, responseType);
                }

                String errorBody = readErrorStream(conn);
                logger.warning("HTTP请求失败: " + path + " (响应码 " + responseCode + ")"
                        + (errorBody != null ? " - " + errorBody : ""));

                if (retry && i < attempts - 1) {
                    logger.warning("重试 " + (i + 1) + "/" + attempts + ": " + path);
                    Thread.sleep(RETRY_DELAY_MS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.warning("HTTP请求异常: " + path + " - " + e.getMessage());
                if (retry && i < attempts - 1) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
        return null;
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        try (InputStream is = conn.getInputStream();
             BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return readStream(in);
        }
    }

    private String readErrorStream(HttpURLConnection conn) {
        try (InputStream es = conn.getErrorStream();
             BufferedReader in = new BufferedReader(new InputStreamReader(es, StandardCharsets.UTF_8))) {
            if (es == null) return null;
            return readStream(in);
        } catch (Exception e) {
            return null;
        }
    }

    private String readStream(BufferedReader in) throws Exception {
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        return response.toString();
    }
}
