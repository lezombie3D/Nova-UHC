package net.novaproject.novauhc;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import net.novaproject.novauhc.cloudnet.CloudNet;
import net.novaproject.novauhc.command.CommandManager;
import net.novaproject.novauhc.database.DatabaseManager;
import net.novaproject.novauhc.utils.ApolloUtils;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.utils.nms.NMSPatcher;
import net.novaproject.novauhc.world.generation.BiomeReplacer;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private static UHCManager uhcManager;
    @Getter
    private static Common common;
    @Getter
    private static DatabaseManager databaseManager;
    @Getter
    private CommandManager commandManager;
    private MongoClient mongoClient;
    private MongoDatabase database;
    @Getter
    private CloudNet cloudNet = null;


    public static Main get() {
        return instance;
    }

    public static UHCManager getUHCManager() {
        return uhcManager;
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
        commandManager = new CommandManager(this);
        common.setup();
        uhcManager.setup();
        if (Bukkit.getPluginManager().getPlugin("CloudNet-Bridge") != null) cloudNet = new CloudNet();
        if (Bukkit.getPluginManager().getPlugin("Apollo-Bukkit") != null) {
            ApolloUtils.initialize();
        }
        databaseManager = new DatabaseManager();
        new NMSPatcher(this);

    }

    @Override
    public void onDisable() {
        if (mongoClient != null) {
            mongoClient.close();
            getLogger().info("MongoDB connection closed.");
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
            getLogger().info("Successfully connected to MongoDB!");
        } catch (MongoException e) {
            getLogger().severe("Failed to connect to MongoDB: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

}
