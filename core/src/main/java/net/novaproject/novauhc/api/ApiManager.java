package net.novaproject.novauhc.api;
import net.novaproject.novauhc.debug.DebugLog;

import com.google.gson.*;
import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
@Getter
@Setter
public class ApiManager {

    private final Plugin plugin;
    private final String apiUrl;
    private final String apiKey;
    public final Logger log;
    private final Gson gson;
    private final ScheduledExecutorService scheduler;

    private String jwt;
    private String userUuid;
    private String serverUuid;
    private String currentGameUuid;

    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;
    private static final int MAX_RETRIES = 3;

    public static ApiManager get() { return Main.getApiManager(); }

    public ApiManager(Plugin plugin, String apiUrl, String apiKey) {
        String apiUrl1;
        this.plugin = plugin;
        apiUrl1 = apiUrl.endsWith("/") ? apiUrl.substring(0, apiUrl.length() - 1) : apiUrl;
        apiUrl1 = apiUrl1 + "/api/v1/plugin";
        this.apiUrl = apiUrl1;
        this.apiKey = apiKey;
        this.log = plugin.getLogger();
        this.gson = new GsonBuilder().create();
        this.scheduler = Executors.newScheduledThreadPool(2);

        try {
            authenticate();
            startHeartbeat();
            log.info("✅ API Manager initialized");
        } catch (IOException e) {
            log.severe("❌ API authentication failed: " + e.getMessage());
        }
    }

    private void authenticate() throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("serverName", plugin.getConfig().getString("api.server-name", "UHC Server"));
        body.addProperty("ip", getServerIP());
        body.addProperty("port", getServerPort());
        body.addProperty("minecraftVersion", "1.8.8");
        body.addProperty("pluginVersion", plugin.getDescription().getVersion());

        JsonObject response = postWithApiKey("/auth/login", body);
        JsonObject data = obj(response, "data");
        if (data == null) throw new IOException("Login response missing 'data'");

        String token = str(data, "token");
        if (token == null) throw new IOException("Login response missing 'data.token'");
        this.jwt = token;

        JsonObject user = obj(data, "user");
        JsonObject server = obj(data, "server");
        this.userUuid = user != null ? str(user, "uuid") : null;
        this.serverUuid = server != null ? str(server, "uuid") : null;

        log.info("Server registered: " + (server != null ? str(server, "name") : "?"));
    }

    private void startHeartbeat() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("status", currentGameUuid != null ? "ingame" : "online");
                body.addProperty("playersOnline", plugin.getServer().getOnlinePlayers().size());

                if (currentGameUuid != null) {
                    JsonObject game = new JsonObject();
                    game.addProperty("uuid", currentGameUuid);
                    body.add("currentGame", game);
                }
                callAsync("POST", "/server/heartbeat", body);
            } catch (Exception error) { DebugLog.warnOnce("Api", "echec silencieux", error); }
        }, 30, 30, TimeUnit.SECONDS);
    }

    public void shutdown() {
        try {
            post("/server/shutdown", null);
            scheduler.shutdown();
            scheduler.awaitTermination(3, TimeUnit.SECONDS);
            log.info("Server shut down successfully");
        } catch (Exception error) { DebugLog.warnOnce("Api", "echec silencieux", error); }
    }


    public CompletableFuture<JsonObject> callMumble(String method, String endpoint, JsonObject body) {
        String mumbleKey = plugin.getConfig().getString("mumble.access-key", "");
        Map<String, String> extra = (mumbleKey == null || mumbleKey.isEmpty())
                ? null : Map.of("X-Mumble-Key", mumbleKey);
        return CompletableFuture.supplyAsync(() -> {
            try {
                return request(method, endpoint, body, false, extra);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, scheduler);
    }

    public CompletableFuture<String> gameStart(
            String mode,
            String scenario,
            List<String> scenarios,
            JsonObject scenarioConfig,
            int border,
            List<PlayerInfo> players
    ) {
        JsonObject body = new JsonObject();
        body.addProperty("mode", mode);

        if (scenario != null && !scenario.isEmpty()) {
            body.addProperty("scenario", scenario);
        }

        body.add("scenarios", gson.toJsonTree(scenarios));
        body.add("scenarioConfig", scenarioConfig);
        body.addProperty("border", border);

        JsonArray playerArray = new JsonArray();
        for (PlayerInfo p : players) {
            JsonObject playerJson = new JsonObject();
            playerJson.addProperty("uuid", p.uuid());
            playerJson.addProperty("name", p.name());
            playerArray.add(playerJson);
        }
        body.add("players", playerArray);

        log.info("Starting " + mode + " game" + (scenario != null ? " (" + scenario + ")" : ""));

        return callAsync("POST", "/game/start", body)
                .thenApply(response -> {
                    JsonObject data = obj(response, "data");
                    String gameUuid = data != null ? str(data, "gameUuid") : null;
                    if (gameUuid == null)
                        throw new IllegalStateException("game/start response missing 'data.gameUuid'");
                    currentGameUuid = gameUuid;
                    log.info("Game started with UUID: " + currentGameUuid);
                    return currentGameUuid;
                })
                .whenComplete((uuid, err) -> {
                    if (err != null) log.severe("❌ game/start failed: " + rootMessage(err));
                });
    }

    public CompletableFuture<JsonObject> gameEnd(
            String mode,
            String scenario,
            String winCondition,
            List<WinnerInfo> winners,
            List<PlayerStats> playerStats,
            int duration,
            Map<String, Object> specialData
    ) {
        if (currentGameUuid == null) {
            log.severe("❌ game/end aborted: no currentGameUuid (game/start never confirmed)");
            CompletableFuture<JsonObject> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalStateException("currentGameUuid is null"));
            return failed;
        }

        JsonObject body = new JsonObject();
        body.addProperty("gameUuid", currentGameUuid);
        body.addProperty("mode", mode);
        body.addProperty("winCondition", winCondition);

        if (scenario != null && !scenario.isEmpty()) {
            body.addProperty("scenario", scenario);
        }

        body.add("winners", gson.toJsonTree(winners));
        body.add("players", gson.toJsonTree(playerStats));
        body.addProperty("duration", duration);

        if (specialData != null && !specialData.isEmpty()) {
            JsonObject special = gson.toJsonTree(specialData).getAsJsonObject();
            body.add("specialData", special);
        }

        log.info("Ending " + mode + " game with " + winners.size() + " winner(s)" +
                (scenario != null ? " (scenario: " + scenario + ")" : ""));

        return callAsync("POST", "/game/end", body)
                .whenComplete((r, err) -> {
                    if (err != null) log.severe("❌ game/end failed: " + rootMessage(err));
                    else currentGameUuid = null;
                });
    }

    private CompletableFuture<JsonObject> addStat(String uuid, String field, int amount) {
        JsonObject body = new JsonObject();
        body.addProperty("uuid", uuid);
        body.addProperty("field", field);
        body.addProperty("amount", amount);
        return callAsync("POST", "/player/stat/add", body);
    }

    public void connectPlayer(UUID uuid, String name) {
        JsonObject body = new JsonObject();
        body.addProperty("uuid", uuid.toString());
        body.addProperty("name", name);

        callAsync("POST", "/player/connect", body)
                .thenAccept(response -> {
                    if (response != null && response.has("success") && response.get("success").getAsBoolean()) {
                        log.fine("Player connected: " + name);
                        applyLocaleFromResponse(uuid, response);
                    }
                })
                .exceptionally(ex -> {
                    log.warning("Failed to connect player: " + ex.getMessage());
                    return null;
                });
    }

    private void applyLocaleFromResponse(UUID uuid, JsonObject response) {
        try {
            if (!response.has("data")) return;
            JsonObject data = response.getAsJsonObject("data");
            if (data == null || !data.has("player")) return;
            JsonObject player = data.getAsJsonObject("player");
            if (player == null || !player.has("locale") || player.get("locale").isJsonNull()) return;
            String loc = player.get("locale").getAsString();
            Bukkit.getScheduler().runTask(Main.get(), () -> {
                UHCPlayer up = UHCPlayerManager.get().getPlayer(uuid);
                if (up != null) up.applyLoadedLocale(loc);
            });
        } catch (Exception error) {
            DebugLog.warnOnce("Api", "echec silencieux", error);
        }
    }

    public CompletableFuture<JsonObject> setLocale(String uuid, String locale) {
        JsonObject body = new JsonObject();
        body.addProperty("uuid", uuid);
        body.addProperty("locale", locale);
        return callAsync("POST", "/player/locale", body);
    }

    public CompletableFuture<JsonObject> verifyMinecraftLink(String code, UUID uuid, String username) {
        JsonObject body = new JsonObject();
        body.addProperty("code", code);
        body.addProperty("minecraftUuid", uuid.toString());
        body.addProperty("minecraftUsername", username);
        return callAsync("POST", "/player/link/verify", body);
    }

    public CompletableFuture<JsonObject> pushLiveConfig(JsonObject config) {
        return callAsync("POST", "/server/config", config);
    }

    public CompletableFuture<JsonObject> saveConfig(String playerUuid, String name, JsonObject config) {
        JsonObject body = new JsonObject();
        body.addProperty("playerUuid", playerUuid);
        body.addProperty("name", name);
        body.add("config", config);
        return callAsync("POST", "/config/save", body);
    }

    public CompletableFuture<JsonObject> listConfigs(String playerUuid) {
        return callAsync("GET", "/config/list?playerUuid=" + encode(playerUuid), null);
    }

    public CompletableFuture<JsonObject> getConfig(String playerUuid, String name) {
        return callAsync("GET", "/config/" + encode(name) + "?playerUuid=" + encode(playerUuid), null);
    }

    public CompletableFuture<JsonObject> deleteConfig(String playerUuid, String name) {
        return callAsync("DELETE", "/config/" + encode(name) + "?playerUuid=" + encode(playerUuid), null);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private JsonObject post(String endpoint, JsonObject body) throws IOException {
        return request("POST", endpoint, body, false);
    }

    private JsonObject postWithApiKey(String endpoint, JsonObject body) throws IOException {
        return request("POST", endpoint, body, true);
    }

    public CompletableFuture<JsonObject> callAsync(String method, String endpoint, JsonObject body) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return request(method, endpoint, body, false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, scheduler);
    }

    private JsonObject request(String method, String endpoint,
                               JsonObject body, boolean useApiKey) throws IOException {
        return request(method, endpoint, body, useApiKey, null);
    }

    private JsonObject request(String method, String endpoint,
                               JsonObject body, boolean useApiKey,
                               Map<String, String> extraHeaders) throws IOException {

        int retries = 0;
        boolean reauthTried = false;
        while (true) {
            try {
                return executeRequest(method, endpoint, body, useApiKey, extraHeaders);
            } catch (ApiHttpException e) {

                if (e.code == 401 && !useApiKey && !reauthTried) {
                    reauthTried = true;
                    try { authenticate(); } catch (IOException authErr) {
                        log.warning("Re-authentication failed: " + authErr.getMessage());
                        throw e;
                    }
                    continue;
                }

                if (e.code >= 400 && e.code < 500 && e.code != 429) throw e;
                if (++retries >= MAX_RETRIES) throw e;
                try { Thread.sleep((long) Math.pow(2, retries) * 1000); }
                catch (InterruptedException ignored) {}
            } catch (IOException e) {
                if (++retries >= MAX_RETRIES) throw e;
                try { Thread.sleep((long) Math.pow(2, retries) * 1000); }
                catch (InterruptedException ignored) {}
            }
        }
    }

    private JsonObject executeRequest(String method, String endpoint,
                                      JsonObject body, boolean useApiKey,
                                      Map<String, String> extraHeaders) throws IOException {

        if (!useApiKey && (jwt == null || jwt.isEmpty())) {

            try { authenticate(); } catch (IOException ignored) {}
            if (jwt == null || jwt.isEmpty()) {
                throw new ApiHttpException(401, "Not authenticated (jwt missing)");
            }
        }

        HttpURLConnection conn =
                (HttpURLConnection) new URL(buildEndpoint(endpoint)).openConnection();

        conn.setRequestMethod(method);
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestProperty("Content-Type", "application/json");

        if (useApiKey)
            conn.setRequestProperty("X-API-Key", apiKey);
        else
            conn.setRequestProperty("Authorization", "Bearer " + jwt);

        if (extraHeaders != null) {
            for (Map.Entry<String, String> h : extraHeaders.entrySet()) {
                conn.setRequestProperty(h.getKey(), h.getValue());
            }
        }

        if (body != null &&
                (method.equals("POST") || method.equals("PUT") || method.equals("PATCH"))) {

            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(gson.toJson(body).getBytes(StandardCharsets.UTF_8));
            }
        }

        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) {

            String errorBody = readStream(conn.getErrorStream() != null
                    ? conn.getErrorStream() : conn.getInputStream());
            throw new ApiHttpException(code,
                    "HTTP " + code + (errorBody.isEmpty() ? "" : " - " + errorBody));
        }

        try (BufferedReader br =
                     new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            return gson.fromJson(br, JsonObject.class);
        }
    }

    private String readStream(java.io.InputStream in) {
        if (in == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        } catch (IOException e) {
            return "";
        }
    }

    private static JsonObject obj(JsonObject parent, String field) {
        if (parent == null) return null;
        JsonElement e = parent.get(field);
        return e != null && e.isJsonObject() ? e.getAsJsonObject() : null;
    }

    private static String str(JsonObject parent, String field) {
        if (parent == null) return null;
        JsonElement e = parent.get(field);
        return e != null && e.isJsonPrimitive() ? e.getAsString() : null;
    }

    public static String rootMessage(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() != null ? t.getMessage() : t.toString();
    }

    private static class ApiHttpException extends IOException {
        final int code;
        ApiHttpException(int code, String message) {
            super(message);
            this.code = code;
        }
    }

    private String buildEndpoint(String path) {
        if (!path.startsWith("/")) path = "/" + path;
        return apiUrl + path;
    }

    private String getServerIP() {
        return plugin.getServer().getIp().isEmpty()
                ? "unknown"
                : plugin.getServer().getIp();
    }

    private int getServerPort() {
        return plugin.getServer().getPort();
    }

    public record PlayerInfo(String uuid, String name) {}
    public record PlayerStats(String uuid, String name, int kills, int deaths, int placement, String camp,
                              int assists, double damageDealt, double damageTaken, int playtime) {}
    public record WinnerInfo(String type, String uuid, String name, int kills, String camp) {}
}

