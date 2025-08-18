package net.novaproject.novauhc;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import net.novaproject.novauhc.database.DatabaseManager;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.utils.nms.NMSPatcher;
import net.novaproject.novauhc.world.generation.BiomeReplacer;
import org.bson.Document;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private static UHCManager uhcManager;
    private static Common common;
    private static DatabaseManager databaseManager;
    private MongoClient mongoClient;
    private MongoDatabase database;


    public static Main get() {
        return instance;
    }

    public static UHCManager getUHCManager() {
        return uhcManager;
    }

    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public static Common getCommon() {
        return common;
    }

    public MongoDatabase getMongoDB() {
        return database;
    }

    @Override
    public void onLoad() {
        instance = this;
        uhcManager = new UHCManager();
        common = new Common();
        ConfigUtils.setup();
        BiomeReplacer.init();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        connectToMongoDB();
        common.setup();
        uhcManager.setup();
        databaseManager = new DatabaseManager();
        new NMSPatcher(this);

    }

    @Override
    public void onDisable() {
        if (mongoClient != null) {
            mongoClient.close();
            getLogger().info(CommonString.MONGODB_CONNECTION_CLOSED.getRawMessage());
        }
    }

    public void connectToMongoDB() {
        String connectionString = getConfig().getString("mongodb.connectionString",
                "mongodb://username:password@host:port/?authSource=admin");

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .build();

        try {
            String name = getConfig().getString("mongodb.name");
            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(name);
            database.runCommand(new Document("ping", 1));
            getLogger().info(CommonString.MONGODB_CONNECTED.getRawMessage());
        } catch (MongoException e) {
            String errorMessage = CommonString.MONGODB_CONNECTION_FAILED.getRawMessage().replace("%error%", e.getMessage());
            getLogger().severe(errorMessage);
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}
