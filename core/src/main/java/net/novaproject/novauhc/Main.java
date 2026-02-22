package net.novaproject.novauhc;

import lombok.Getter;
import net.novaproject.novauhc.cloudnet.CloudNet;
import net.novaproject.novauhc.command.CommandManager;
import net.novaproject.novauhc.database.ApiManager;
import net.novaproject.novauhc.database.DatabaseManager;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.utils.nms.NMSPatcher;
import net.novaproject.novauhc.world.generation.BiomeReplacer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private static UHCManager uhcManager;

    @Getter
    private static Common common;

    @Getter
    private static ApiManager apiManager;

    @Getter
    private static DatabaseManager databaseManager;

    @Getter
    private CommandManager commandManager;

    @Getter
    private CloudNet cloudNet = null;

    public static Main get() {
        return instance;
    }

    public static UHCManager getUHCManager() {
        return uhcManager;
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

        String apiUrl = getConfig().getString("api.url");
        String apiKey = getConfig().getString("api.key");

        if (apiUrl == null || apiKey == null) {
            getLogger().severe("❌ Configuration API manquante dans config.yml !");
            getLogger().severe("Ajoutez : api.url et api.apiKey");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        apiManager = new ApiManager(this, apiUrl, apiKey);

        databaseManager = new DatabaseManager(apiManager);

        commandManager = new CommandManager(this);
        common.setup();
        uhcManager.setup();

        if (Bukkit.getPluginManager().getPlugin("CloudNet-Bridge") != null) {
            cloudNet = new CloudNet();
        }

        new NMSPatcher(this);

        getLogger().info("✅ NovaUHC démarré avec API REST");
    }

    @Override
    public void onDisable() {
        if (apiManager != null) {
            apiManager.shutdown();
        }
    }
}