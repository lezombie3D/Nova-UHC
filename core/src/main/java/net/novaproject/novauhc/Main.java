package net.novaproject.novauhc;

import lombok.Getter;
import net.novaproject.novauhc.cloudnet.CloudNet;
import net.novaproject.novauhc.command.CommandManager;
import net.novaproject.novauhc.api.ApiManager;
import net.novaproject.novauhc.api.DatabaseManager;
import net.novaproject.novauhc.lang.*;
import net.novaproject.novauhc.lang.lang.*;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScoreboardLang;
import net.novaproject.novauhc.lang.scenario.*;
import net.novaproject.novauhc.lang.special.*;
import net.novaproject.novauhc.lang.ui.*;
import net.novaproject.novauhc.lang.ui.ScenarioVariableUiLang;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.utils.ReconnectionManager;
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
            Bukkit.shutdown();
            return;
        }
        apiManager = new ApiManager(this, apiUrl, apiKey);
        databaseManager = new DatabaseManager(apiManager);

        
        String defaultLocale = getConfig().getString("language", "fr_FR");
        LangManager langManager = new LangManager(getDataFolder(), defaultLocale);

        langManager.register(CommonLang.values());

        
        langManager.register(DefaultUiLang.values());
        langManager.register(GameUiLang.values());
        langManager.register(WhiteListUiLang.values());
        langManager.register(PreconfigUiLang.values());
        langManager.register(MumbleUiLang.values());
        langManager.register(ScoreboardLang.values());
        langManager.register(ConfigVarUiLang.values());
        langManager.register(CenterUiLang.values());
        langManager.register(OrePopulatorUiLang.values());
        langManager.register(ScenariosUiLang.values());
        langManager.register(TeamConfigUiLang.values());
        langManager.register(BorderConfigUiLang.values());
        langManager.register(WorldUiLang.values());
        langManager.register(LimiteStuffUiLang.values());
        langManager.register(ScenarioVariableUiLang.values());
        langManager.register(UiTitleLang.values());

        
        langManager.register(CommandLang.values());
        langManager.register(TaskLang.values());

        
        langManager.register(ScenarioVarLang.values());         

        
        langManager.register(ScenarioDescLang.values());
        langManager.register(AcidRainLang.values());
        langManager.register(BestPvELang.values());
        langManager.register(BlizzardLang.values());
        langManager.register(BlockRushLang.values());
        langManager.register(BloodCycleLang.values());
        langManager.register(BloodLustLang.values());
        langManager.register(DemocracyLang.values());
        langManager.register(net.novaproject.novauhc.lang.special.DragonFallLang.values());          
        langManager.register(FalloutLang.values());
        langManager.register(net.novaproject.novauhc.lang.special.FireForceLang.values());
        langManager.register(GenieLang.values());
        langManager.register(GladiatorLang.values());
        langManager.register(GoldenHeadLang.values());
        langManager.register(InventorsLang.values());
        langManager.register(LongShootLang.values());
        langManager.register(LootCrateLang.values());
        langManager.register(net.novaproject.novauhc.lang.special.LoupGarouLang.values());
        langManager.register(LuckyOreLang.values());
        langManager.register(MagnetLang.values());
        langManager.register(NineSlotLang.values());
        langManager.register(NinjaLang.values());
        langManager.register(NoEndLang.values());
        langManager.register(NoNetherLang.values());
        langManager.register(OreRouletteLang.values());
        langManager.register(OreSwapLang.values());
        langManager.register(ParkourMasterLang.values());
        langManager.register(PotentialPermanentLang.values());
        langManager.register(SimonSaysLang.values());
        langManager.register(VampireLang.values());
        langManager.register(WeakestLinkLang.values());
        langManager.register(BeatTheSantaLang.values());
        langManager.register(DragonFallLang.values());
        langManager.register(FireForceLang.values());
        langManager.register(FKLang.values());
        langManager.register(FlowerPowerLang.values());
        langManager.register(KingLang.values());
        langManager.register(LegendLang.values());
        langManager.register(LoupGarouLang.values());
        langManager.register(MysteryTeamLang.values());
        langManager.register(SkyDefLang.values());
        langManager.register(SkyHighLang.values());
        langManager.register(SlaveMarketLang.values());
        langManager.register(SoulBrotherLang.values());
        langManager.register(TaupeGunLang.values());
        langManager.register(TrueLoveLang.values());
        langManager.generateAndLoad();


        commandManager = new CommandManager(this);
        common.setup();
        uhcManager.setup();
        new ReconnectionManager();
        if (Bukkit.getPluginManager().getPlugin("CloudNet-Bridge") != null) {
            cloudNet = new CloudNet();
        }

        new NMSPatcher(this);
    }

    @Override
    public void onDisable() {
        if (apiManager != null) {
            apiManager.shutdown();
        }
    }
}