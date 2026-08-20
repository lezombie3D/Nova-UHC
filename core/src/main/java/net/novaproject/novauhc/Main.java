package net.novaproject.novauhc;

import net.novaproject.novauhc.debug.DebugLog;
import net.novaproject.novauhc.display.apollo.VisualFeedback;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import net.novaproject.novauhc.lang.lang.*;
import net.novaproject.novauhc.utils.CloudNet;
import net.novaproject.novauhc.command.CommandManager;
import net.novaproject.novauhc.api.ApiManager;
import net.novaproject.novauhc.api.ApiConfigManager;
import net.novaproject.novauhc.lang.*;
import net.novaproject.novauhc.minigame.ArenaUHC;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.game.Lifecycles;
import net.novaproject.novauhc.game.SpectatorInteractPackets;
import net.novaproject.novauhc.game.SpectatorInteraction;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.utils.PluginUpdater;
import net.novaproject.novauhc.utils.nms.MobDisguiseService;
import org.bson.Document;

import java.util.HashMap;
import java.util.Map;

import net.novaproject.novauhc.mumble.MumbleManager.MumbleListener;
import net.novaproject.novauhc.mumble.MumbleManager;
import net.novaproject.novauhc.ui.config.Enchants;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.player.utils.ReconnectionManager;
import net.novaproject.novauhc.world.generation.BiomeReplacer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin {

    private static Main instance;
    private static UHCManager uhcManager;

    @Getter
    private static Common common;

    @Getter
    private static ApiManager apiManager;

    @Getter
    private static ApiConfigManager configManager;

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

        com.github.retrooper.packetevents.PacketEvents.setAPI(
                io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder.build(this));
        com.github.retrooper.packetevents.PacketEvents.getAPI().load();
        uhcManager = new UHCManager();
        common = new Common();
        ConfigUtils.setup();
        BiomeReplacer.init();
    }

    @Override
    public void onEnable() {
        com.github.retrooper.packetevents.PacketEvents.getAPI().init();
        MobDisguiseService.start();
        SpectatorInteraction.start();
        SpectatorInteractPackets.start();
        VisualFeedback.init();
        saveDefaultConfig();
        if (!getConfig().contains("dev")) {
            getConfig().set("dev", false);
            saveConfig();
        }
        new PluginUpdater(this).checkForUpdates();

        String apiUrl = getConfig().getString("api.url");
        String apiKey = getConfig().getString("api.key");
        if (apiUrl == null || apiKey == null) {
            getLogger().severe("❌ Configuration API manquante dans config.yml !");
            Bukkit.shutdown();
            return;
        }
        apiManager = new ApiManager(this, apiUrl, apiKey);
        configManager = new ApiConfigManager(apiManager);

        String defaultLocale = getConfig().getString("language", "fr_FR");
        LangManager langManager = new LangManager(getDataFolder(), defaultLocale);

        langManager.register(CoreLang.values());

        langManager.register(UiLang.values());


        langManager.register(ScenarioVarLang.values());

        langManager.register(ScenarioDescLang.values());
        langManager.register(ScenarioLang.values());
        langManager.register(RandomEventLang.values());
        langManager.register(MumbleUiLang.values());
        langManager.register(DevLang.values());
        langManager.importShipped(this);
        langManager.generateAndLoad();

        commandManager = new CommandManager(this);
        common.setup();
        ArenaUHC.init();
        uhcManager.setup();

        Bukkit.getPluginManager().registerEvents(new MumbleListener(), this);

        langManager.generateAndLoad();
        ScenarioManager.get().markBooted();

        new ReconnectionManager();
        if (Bukkit.getPluginManager().getPlugin("CloudNet-Bridge") != null) {
            cloudNet = new CloudNet();
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                JsonObject config = buildLiveConfig();
                if (config != null) {
                    apiManager.pushLiveConfig(config);
                    getLogger().info("✅ Live config pushed on startup");
                }
            }
        }.runTaskLater(Main.get(), 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                MumbleManager mm = MumbleManager.get();
                if (mm.isEnabled()) {
                    mm.provision(0, getConfig().getString("api.server-name", "UHC Voice"), null);
                    getLogger().info("Vocal Mumble : provisioning de la session (demarrage)");
                    // Sonde l'etat vocal (alimente isLinked/isConnected) et affiche la pastille
                    // dans le tab. Demarres ici : PacketEvents est initialise et la session existe.
                    mm.startStatePolling();
                }
                // Le tab per-viewer sert aussi au role du /color, independant du vocal.
                net.novaproject.novauhc.mumble.TabVoiceStatus.start();
            }
        }.runTaskLater(Main.get(), 40);

        new BukkitRunnable() {
            @Override
            public void run() {
                JsonObject config = buildLiveConfig();
                if (config != null) {
                    apiManager.pushLiveConfig(config);
                }
            }
        }.runTaskTimer(Main.get(), 600L, 1200L);

    }

    public JsonObject buildLiveConfig() {
        try {
            JsonObject liveConfig = new JsonObject();

            JsonArray enabledArr = new JsonArray();
            ScenarioManager.get().getActiveScenarios().forEach(s -> enabledArr.add(new JsonPrimitive(s.getName())));
            liveConfig.add("enabledScenarios", enabledArr);

            Map<String, Document> scenarioDocs = new HashMap<>();
            for (Scenario s : ScenarioManager.get().getActiveScenarios()) {
                Document doc = s.scenarioToDoc();
                if (doc != null && !doc.isEmpty()) scenarioDocs.put(s.getName(), doc);
            }
            if (!scenarioDocs.isEmpty()) {
                liveConfig.add("scenarioConfigs",
                        configManager.documentMapToJson(scenarioDocs));
            }

            liveConfig.addProperty("teamSize", uhcManager.getTeam_size());
            liveConfig.addProperty("borderSize", (int) common.getArena().getWorldBorder().getSize());
            liveConfig.addProperty("pvpTime", uhcManager.getPvpTimer());
            liveConfig.addProperty("finalSize", (int) uhcManager.getTargetSize());
            liveConfig.addProperty("bordecactivation", uhcManager.getBorderTimer());
            liveConfig.addProperty("timereduc", (int) uhcManager.getReducSpeed());
            liveConfig.addProperty("slot", uhcManager.getSlot());
            liveConfig.addProperty("diamant", uhcManager.getDiamondLimit());
            liveConfig.addProperty("limiteD", uhcManager.getDiamondArmor());
            liveConfig.addProperty("protection", uhcManager.getProtectionMax());

            JsonArray limitesArr = new JsonArray();
            for (Enchants ench : Enchants.values()) {
                limitesArr.add(new JsonPrimitive(ench.getConfigValue()));
            }
            liveConfig.add("limites", limitesArr);

            Map<String, Boolean> potionStates = configManager.getCurrentPotionStates();
            if (potionStates != null && !potionStates.isEmpty()) {
                JsonObject potionJson = new JsonObject();
                for (Map.Entry<String, Boolean> entry : potionStates.entrySet()) {
                    potionJson.addProperty(entry.getKey(), entry.getValue());
                }
                liveConfig.add("potionStates", potionJson);
            }

            return liveConfig;
        } catch (Exception e) {
            getLogger().warning("buildLiveConfig failed: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void onDisable() {
        try {
            if (UHCManager.get() != null && UHCManager.get().isGame()) {
                ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
                    try {
                        scenario.onStop();
                    } catch (Throwable t) {
                        getLogger().warning("onStop en échec à l'arrêt pour " + scenario.getName() + " : " + t);
                    }
                });
                Lifecycles.stopAll();
            }
        } catch (Throwable error) {
            DebugLog.warnOnce("Boot", "echec silencieux", error);
        }
        try {
            MobDisguiseService.get().clearAll();
        } catch (Throwable error) {
            DebugLog.warnOnce("Boot", "echec silencieux", error);
        }
        // Ferme le serveur vocal AVANT d'arreter l'executeur de l'API : callMumble poste sur
        // ce meme executeur, et apiManager.shutdown() lui laisse 3 s pour vider ses taches.
        // L'ordre est donc porteur — inverser les deux lignes ferait perdre la fermeture.
        // Sans ca, rien cote plugin ne fermait jamais la session : le serveur virtuel Murmur
        // survivait a l'arret du serveur Minecraft jusqu'au passage du cron du service.
        try {
            MumbleManager.get().close();
        } catch (Throwable error) {
            DebugLog.warnOnce("Boot", "echec silencieux", error);
        }
        if (apiManager != null) {
            apiManager.shutdown();
        }
        try {
            com.github.retrooper.packetevents.PacketEvents.getAPI().terminate();
        } catch (Throwable error) {
            DebugLog.warnOnce("Boot", "echec silencieux", error);
        }
    }
}