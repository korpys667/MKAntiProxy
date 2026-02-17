package ru.millyofficial.mkantiproxy.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ru.millyofficial.mkantiproxy.MKAntiProxy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class APIClient {

    private final MKAntiProxy plugin;
    private final String apiUrl;
    private final String apiKey;
    private final int timeout;
    private final int retryAttempts;

    public APIClient(MKAntiProxy plugin) {
        this.plugin = plugin;
        this.apiUrl = plugin.getConfigManager().getConfig().getString("api.url");
        this.apiKey = plugin.getConfigManager().getConfig().getString("api.key");
        this.timeout = plugin.getConfigManager().getConfig().getInt("api.timeout-ms", 5000);
        this.retryAttempts = plugin.getConfigManager().getConfig().getInt("api.retry-attempts", 3);
    }

    public CompletableFuture<CheckResult> checkIP(String ip, String playerName, int serverPort, int onlineCount) {
        return CompletableFuture.supplyAsync(() -> {
            for (int attempt = 1; attempt <= retryAttempts; attempt++) {
                try {
                    return performCheck(ip, playerName, serverPort, onlineCount);
                } catch (Exception e) {
                    if (attempt == retryAttempts) {
                        plugin.getLogger().warning("API check failed after " + retryAttempts + " attempts: " + e.getMessage());
                        return new CheckResult(ip, null, false, "error");
                    }
                    try { Thread.sleep(1000 * attempt); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
            return new CheckResult(ip, null, false, "error");
        });
    }

    private CheckResult performCheck(String ip, String playerName, int serverPort, int onlineCount) throws Exception {
        URL url = new URL(apiUrl + "/api/v1/check");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("X-API-Key", apiKey);
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        connection.setDoOutput(true);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("ip", ip);
        requestBody.addProperty("player_name", playerName);
        requestBody.addProperty("server_port", serverPort);
        requestBody.addProperty("online", onlineCount);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();

        if (responseCode == 403) {
            String errorBody = "";
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) errorBody += line;
            }

            if (errorBody.contains("blocked") || errorBody.contains("block")) {
                return new CheckResult(ip, null, true, "blocked_by_server");
            }
            return new CheckResult(ip, null, false, "api_error_403");
        }

        if (responseCode != 200) {
            throw new Exception("HTTP error: " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
        }

        JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();

        String asn = jsonResponse.has("asn") && !jsonResponse.get("asn").isJsonNull()
                ? jsonResponse.get("asn").getAsString() : null;
        boolean blocked = jsonResponse.has("blocked") && jsonResponse.get("blocked").getAsBoolean();
        String country = jsonResponse.has("country") && !jsonResponse.get("country").isJsonNull()
                ? jsonResponse.get("country").getAsString() : null;
        boolean cached = jsonResponse.has("cached") && jsonResponse.get("cached").getAsBoolean();

        return new CheckResult(ip, asn, blocked, null, country, cached);
    }

    public static class CheckResult {
        private final String ip;
        private final String asn;
        private final boolean blocked;
        private final String error;
        private final String country;
        private final boolean cached;

        public CheckResult(String ip, String asn, boolean blocked, String error) {
            this(ip, asn, blocked, error, null, false);
        }

        public CheckResult(String ip, String asn, boolean blocked, String error,
                           String country, boolean cached) {
            this.ip = ip;
            this.asn = asn;
            this.blocked = blocked;
            this.error = error;
            this.country = country;
            this.cached = cached;
        }

        public String getIp() { return ip; }
        public String getAsn() { return asn; }
        public boolean isBlocked() { return blocked; }
        public String getError() { return error; }
        public String getCountry() { return country; }
        public boolean isCached() { return cached; }
        public boolean hasError() { return error != null; }
    }
}