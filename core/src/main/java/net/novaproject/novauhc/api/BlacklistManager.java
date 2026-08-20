package net.novaproject.novauhc.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.novaproject.novauhc.listener.PlayerConnectionEvent;
import org.bukkit.OfflinePlayer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlacklistManager {

    private static final BlacklistManager INSTANCE = new BlacklistManager();
    private static final String CONFIG_NAME = "__blacklist__";
    private static final Logger LOGGER = Logger.getLogger(BlacklistManager.class.getName());

    private final Map<UUID, String> entries = new ConcurrentHashMap<>();
    private volatile UUID hostUuid = null;
    private volatile boolean loaded = false;

    public static BlacklistManager get() { return INSTANCE; }

    public boolean isLoaded() { return loaded; }

    public void loadForHost(UUID host) {
        this.hostUuid = host;
        this.loaded = false;
        this.entries.clear();
        ApiManager.get().getConfig(host.toString(), CONFIG_NAME).whenComplete((response, ex) -> {
            try {
                if (ex != null) {
                    LOGGER.fine("No existing blacklist for host " + host + " (" + ex.getMessage() + ")");
                    return;
                }
                if (response == null || !response.has("success") || !response.get("success").getAsBoolean()) return;
                JsonObject data = response.getAsJsonObject("data");
                if (data == null) return;
                JsonObject config = data.getAsJsonObject("config");
                if (config == null) return;
                if (config.has("uuids")) {
                    JsonArray arr = config.getAsJsonArray("uuids");
                    JsonObject names = config.has("names") ? config.getAsJsonObject("names") : new JsonObject();
                    for (JsonElement e : arr) {
                        try {
                            UUID u = UUID.fromString(e.getAsString());
                            String n = names.has(u.toString()) ? names.get(u.toString()).getAsString() : null;
                            entries.put(u, n != null ? n : u.toString());
                        } catch (IllegalArgumentException ignored) {}
                    }
                }
                LOGGER.info("Loaded " + entries.size() + " blacklist entries for host " + host);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to parse blacklist response", e);
            } finally {
                loaded = true;
            }
        });
    }

    public boolean isBlacklisted(UUID uuid) {
        return uuid != null && entries.containsKey(uuid);
    }

    public Map<UUID, String> getEntries() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(entries));
    }

    public boolean add(OfflinePlayer p) {
        if (p == null || p.getUniqueId() == null) return false;
        if (entries.containsKey(p.getUniqueId())) return false;
        entries.put(p.getUniqueId(), p.getName() != null ? p.getName() : p.getUniqueId().toString());
        save();
        return true;
    }

    public boolean remove(UUID uuid) {
        if (uuid == null || !entries.containsKey(uuid)) return false;
        entries.remove(uuid);
        save();
        return true;
    }

    public boolean remove(OfflinePlayer p) {
        return p != null && remove(p.getUniqueId());
    }

    public void clear() {
        entries.clear();
        save();
    }

    private void save() {
        UUID host = currentHost();
        if (host == null) {
            LOGGER.warning("Skipping blacklist save: no host known");
            return;
        }
        JsonObject config = new JsonObject();
        JsonArray uuids = new JsonArray();
        JsonObject names = new JsonObject();
        for (Map.Entry<UUID, String> e : entries.entrySet()) {
            uuids.add(new JsonPrimitive(e.getKey().toString()));
            names.addProperty(e.getKey().toString(), e.getValue());
        }
        config.add("uuids", uuids);
        config.add("names", names);
        ApiManager.get().saveConfig(host.toString(), CONFIG_NAME, config)
                .exceptionally(ex -> {
                    LOGGER.log(Level.WARNING, "Failed to save blacklist: " + ex.getMessage());
                    return null;
                });
    }

    private UUID currentHost() {
        if (hostUuid != null) return hostUuid;
        OfflinePlayer host = PlayerConnectionEvent.getHost();
        return host != null ? host.getUniqueId() : null;
    }
}

