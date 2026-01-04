package net.novaproject.novauhc.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import net.novaproject.novauhc.Main;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static final ReplaceOptions UPSERT_OPTION = new ReplaceOptions().upsert(true);

    private final MongoCollection<Document> players;
    private final MongoCollection<Document> uhc_config;
    private final MongoCollection<Document> server_info;
    private final UHCConfigManager configManager;

    public DatabaseManager() {
        this.players = Main.get().getMongoDB().getCollection("players");
        this.uhc_config = Main.get().getMongoDB().getCollection("uhc_config");
        this.server_info = Main.get().getMongoDB().getCollection("server_info");
        this.configManager = new UHCConfigManager(uhc_config);
    }

    public void saveUHCConfig(UUID uuid, UHCGameConfiguration config) {
        configManager.saveConfig(uuid, config);
    }

    public UHCGameConfiguration getUHCConfig(UUID uuid, String configName) {
        return configManager.getConfig(uuid, configName);
    }

    public boolean deleteUHCConfig(UUID uuid, String configName) {
        return configManager.deleteConfig(uuid, configName);
    }

    public List<String> getPlayerUHCConfigNames(UUID uuid) {
        return configManager.getPlayerConfigNames(uuid);
    }

    public UHCConfigManager getConfigManager() {
        return configManager;
    }

    private Document getPlayerDocument(UUID uuid) {
        try {
            return players.find(Filters.eq("uuid", uuid.toString())).first();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get player document", e);
            return null;
        }
    }


    private void updatePlayerField(UUID uuid, String field, Object value) {
        try {
            Document doc = getPlayerDocument(uuid);
            if (doc == null) {
                createNewPlayerDocument(uuid);
                doc = getPlayerDocument(uuid);
            }

            doc.put(field, value);
            players.replaceOne(Filters.eq("uuid", uuid.toString()), doc, UPSERT_OPTION);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to update player field: " + field, e);
        }
    }


    private int getPlayerIntStat(UUID uuid, String field, int defaultValue) {
        Document doc = getPlayerDocument(uuid);
        return doc != null ? doc.getInteger(field, defaultValue) : defaultValue;
    }


    private String getPlayerStringStat(UUID uuid, String field, String defaultValue) {
        Document doc = getPlayerDocument(uuid);
        return doc != null ? doc.getString(field) : defaultValue;
    }


    private void createNewPlayerDocument(UUID uuid) {
        String playerName = Bukkit.getOfflinePlayer(uuid).getName();
        Document doc = new Document()
                .append("uuid", uuid.toString())
                .append("grade", "default")
                .append("coins", 0)
                .append("kill", 0)
                .append("win", 0)
                .append("lose", 0)
                .append("death", 0)
                .append("host", 0)
                .append("parties", 0)
                .append("name", playerName)
                .append("temps", 0);

        try {
            players.insertOne(doc);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create new player document", e);
        }
    }

    // ===== GRADE METHODS =====


    public String getGrade(UUID uuid) {
        return getPlayerStringStat(uuid, "grade", "default");
    }

    public void setGrade(UUID uuid, String grade) {
        updatePlayerField(uuid, "grade", grade);
    }


    public String gradeName(UUID uuid) {
        String grade = getGrade(uuid);
        switch (grade) {
            case "vip":
                return "§6VIP";
            case "host":
                return "§dHost";
            case "build":
                return "§bBuild";
            case "modo":
                return "§5Modo";
            case "dev":
                return "§2Dev";
            case "admin":
                return "§cAdmin";
            default:
                return "§fJoueur";
        }
    }

    // ===== COINS METHODS =====


    public int getCoins(UUID uuid) {
        return getPlayerIntStat(uuid, "coins", 0);
    }


    public void setCoins(UUID uuid, int coins) {
        updatePlayerField(uuid, "coins", coins);
    }


    public void addCoins(UUID uuid, int amount) {
        int current = getCoins(uuid);
        setCoins(uuid, current + amount);
    }


    public void removeCoins(UUID uuid, int amount) {
        int current = getCoins(uuid);
        setCoins(uuid, Math.max(0, current - amount));
    }

    // ===== WINS METHODS =====


    public int getWin(UUID uuid) {
        return getPlayerIntStat(uuid, "win", 0);
    }


    public void setWins(UUID uuid, int amount) {
        updatePlayerField(uuid, "win", amount);
    }


    public void addWins(UUID uuid, int amount) {
        int current = getWin(uuid);
        setWins(uuid, current + amount);
    }


    public void removeWins(UUID uuid, int amount) {
        int current = getWin(uuid);
        setWins(uuid, Math.max(0, current - amount));
    }

    // ===== KILLS METHODS =====


    public int getKills(UUID uuid) {
        return getPlayerIntStat(uuid, "kill", 0);
    }


    public void setKill(UUID uuid, int amount) {
        updatePlayerField(uuid, "kill", amount);
    }


    public void addKills(UUID uuid, int amount) {
        int current = getKills(uuid);
        setKill(uuid, current + amount);
    }


    public void removeKills(UUID uuid, int amount) {
        int current = getKills(uuid);
        setKill(uuid, Math.max(0, current - amount));
    }

    // ===== LOSES METHODS =====


    public int getLose(UUID uuid) {
        return getPlayerIntStat(uuid, "lose", 0);
    }


    public void setLose(UUID uuid, int amount) {
        updatePlayerField(uuid, "lose", amount);
    }


    public void addLose(UUID uuid, int amount) {
        int current = getLose(uuid);
        setLose(uuid, current + amount);
    }


    public void removeLose(UUID uuid, int amount) {
        int current = getLose(uuid);
        setLose(uuid, Math.max(0, current - amount));
    }

    // ===== DEATHS METHODS =====


    public int getDeath(UUID uuid) {
        return getPlayerIntStat(uuid, "death", 0);
    }


    public void setDeath(UUID uuid, int amount) {
        updatePlayerField(uuid, "death", amount);
    }


    public void addDeath(UUID uuid, int amount) {
        int current = getDeath(uuid);
        setDeath(uuid, current + amount);
    }


    public void removeDeath(UUID uuid, int amount) {
        int current = getDeath(uuid);
        setDeath(uuid, Math.max(0, current - amount));
    }

    // ===== HOST METHODS =====


    public int getHost(UUID uuid) {
        return getPlayerIntStat(uuid, "host", 0);
    }


    public void setHost(UUID uuid, int amount) {
        updatePlayerField(uuid, "host", amount);
    }


    public void addHost(UUID uuid, int amount) {
        int current = getHost(uuid);
        setHost(uuid, current + amount);
    }

    public void removeHost(UUID uuid, int amount) {
        int current = getHost(uuid);
        setHost(uuid, Math.max(0, current - amount));
    }

    // ===== PARTIES METHODS =====


    public int getPartie(UUID uuid) {
        return getPlayerIntStat(uuid, "parties", 0);
    }


    public void setPartie(UUID uuid, int amount) {
        updatePlayerField(uuid, "parties", amount);
    }


    public void addPartie(UUID uuid, int amount) {
        int current = getPartie(uuid);
        setPartie(uuid, current + amount);
    }


    public void removePartie(UUID uuid, int amount) {
        int current = getPartie(uuid);
        setPartie(uuid, Math.max(0, current - amount));
    }

    // ===== NAME METHODS =====


    public String getName(UUID uuid) {
        return getPlayerStringStat(uuid, "name", Bukkit.getOfflinePlayer(uuid).getName());
    }


    public void setName(UUID uuid, String name) {
        updatePlayerField(uuid, "name", name);
    }
    // ===== JUMP METHODS =====

    public void setJump(UUID uuid, int amount) {
        updatePlayerField(uuid, "jump", amount);
    }

    public int getJump(UUID uuid) {
        return getPlayerIntStat(uuid, "jump", 0);
    }

    // ===== OTHER METHODS =====


    public void connectPlayer(UUID uuid) {
        Document existing = getPlayerDocument(uuid);
        if (existing == null) {
            createNewPlayerDocument(uuid);
        }
    }


    public List<Document> getTop10(String fieldName) {
        try {
            return players.find()
                    .sort(Sorts.descending(fieldName))
                    .limit(10)
                    .into(new ArrayList<>());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get top 10 for field: " + fieldName, e);
            return new ArrayList<>();
        }
    }

    public Document getTop1Oof(String fieldName) {
        try {
            return players.find()
                    .sort(Sorts.descending(fieldName))
                    .limit(1)
                    .first();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get top for field: " + fieldName, e);
            return null;
        }
    }
}