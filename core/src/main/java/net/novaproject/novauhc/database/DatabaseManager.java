package net.novaproject.novauhc.database;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private final ApiManager api;
    private final ApiConfigManager configManager;

    public DatabaseManager(ApiManager api) {
        this.api = api;
        this.configManager = new ApiConfigManager(api);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  STATS
    // ═══════════════════════════════════════════════════════════════════

    public void addKills(UUID uuid, int n) { api.addKills(uuid.toString(), n); }
    public void addWins(UUID uuid, int n) { api.addWins(uuid.toString(), n); }
    public void addLose(UUID uuid, int n) { api.addLose(uuid.toString(), n); }
    public void addDeath(UUID uuid, int n) { api.addDeath(uuid.toString(), n); }
    public void addCoins(UUID uuid, int n) { api.addCoins(uuid.toString(), n); }
    public void removeCoins(UUID uuid, int n) { api.addCoins(uuid.toString(), -n); }

    public void setKill(UUID uuid, int n) { addKills(uuid, n); }
    public void setWins(UUID uuid, int n) { addWins(uuid, n); }
    public void setLose(UUID uuid, int n) { addLose(uuid, n); }
    public void setDeath(UUID uuid, int n) { addDeath(uuid, n); }
    public void setCoins(UUID uuid, int n) { addCoins(uuid, n); }

    public void removeKills(UUID uuid, int n) { addKills(uuid, -n); }
    public void removeWins(UUID uuid, int n) { addWins(uuid, -n); }
    public void removeLose(UUID uuid, int n) { addLose(uuid, -n); }
    public void removeDeath(UUID uuid, int n) { addDeath(uuid, -n); }

    // ═══════════════════════════════════════════════════════════════════
    //  CONFIGS UHC
    // ═══════════════════════════════════════════════════════════════════

    public void saveUHCConfig(UUID uuid, UHCGameConfiguration config) {
        configManager.saveConfig(uuid, config);
    }

    public CompletableFuture<UHCGameConfiguration> getUHCConfig(UUID uuid, String configName) {
        return configManager.getConfig(uuid, configName);
    }

    public CompletableFuture<List<String>> getPlayerUHCConfigNames(UUID uuid) {
        return configManager.getPlayerConfigNames(uuid);
    }

    public CompletableFuture<Boolean> deleteUHCConfig(UUID uuid, String configName) {
        return configManager.deleteConfig(uuid, configName);
    }

    public ApiConfigManager getConfigManager() {
        return configManager;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  MÉTHODES NON SUPPORTÉES
    // ═══════════════════════════════════════════════════════════════════

    public void connectPlayer(UUID uuid) {}
    public String gradeName(UUID uuid) { return "§fJoueur"; }
    public String getName(UUID uuid) { return org.bukkit.Bukkit.getOfflinePlayer(uuid).getName(); }
}