package net.novaproject.novauhc.scenario;

import net.novaproject.novauhc.utils.variable.Variables;
import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.random.RandomEventScheduler;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.ui.config.ScenariosUi;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.utils.variable.VariableDescriptor;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.utils.variable.Variables.VariableSerializer;
import net.novaproject.novauhc.ui.CustomInventory;
import net.novaproject.novauhc.ui.VariableEditorUi;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;

@Getter
@Setter
public abstract class Scenario implements ScenarioSpi.IScenario {

    private FileConfiguration config;
    public abstract String getName();

    public abstract String getDescription(Player player);

    public abstract ItemCreator getItem();

    private boolean active = false;

    private static final Set<String> GENERATION_HOOKS =
            Set.of("getPopulator", "getWorldType", "getGeneratorOverrides", "getChunkGenerator");

    public String getColor() {
        return "§6";
    }

    protected String t(Lang key) {
        return LangManager.get().get(key);
    }

    protected String t(Lang key, Map<String, Object> placeholders) {
        return LangManager.get().get(key, placeholders);
    }

    protected String t(Lang key, Player viewer) {
        return LangManager.get().get(key, viewer);
    }

    protected String t(Lang key, Player viewer, Map<String, Object> placeholders) {
        return LangManager.get().get(key, viewer, placeholders);
    }

    public String getPrefix() {
        return "";
    }

    public boolean isSpecial() {
        return false;
    }

    @Getter
    public enum Family {
        RESTRICTIONS(Material.BARRIER, (short) 0, "Restrictions"),
        MINAGE(Material.DIAMOND_PICKAXE, (short) 0, "Minage"),
        COMBAT(Material.DIAMOND_SWORD, (short) 0, "Combat"),
        VIE(Material.GOLDEN_APPLE, (short) 0, "Vie & effets"),
        NOURRITURE(Material.COOKED_BEEF, (short) 0, "Nourriture"),
        MOBS(Material.MONSTER_EGG, (short) 54, "Mobs"),
        CRAFT(Material.WORKBENCH, (short) 0, "Craft"),
        TIMERS(Material.WATCH, (short) 0, "Timers"),
        LOOT(Material.CHEST, (short) 0, "Loot & social"),
        MONDE(Material.GRASS, (short) 0, "Monde"),
        AUTRE(Material.PAPER, (short) 0, "Autre");

        private final Material icon;
        private final short data;
        private final String label;

        Family(Material icon, short data, String label) {
            this.icon = icon;
            this.data = data;
            this.label = label;
        }
    }

    public Family getFamily() {
        return Family.AUTRE;
    }

    public Map<String, String> getGeneratorOverrides() {
        return null;
    }

    public WorldType getWorldType() {
        return null;
    }

    public ChunkGenerator getChunkGenerator() {
        return null;
    }

    public Location resolveSpawn(World world) {
        return null;
    }

    public BlockPopulator getPopulator(World world) {
        return null;
    }

    public boolean touchesGeneration() {
        for (Class<?> type = getClass(); type != null && type != Scenario.class; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                if (GENERATION_HOOKS.contains(method.getName())) return true;
            }
        }
        return false;
    }

    public String getPath() {
        return null;
    }

    public boolean isWin() {
        return false;
    }
    public boolean overridesVictory() {
        return false;
    }
    public boolean hascustomDeathMessage() {
        return false;
    }

    public RandomEventScheduler getEventScheduler() {
        return null;
    }

    public boolean hasCustomTeamTchat() {
        return false;
    }

    public void enable() {
        if (!isActive()) {
            active = true;
        }
    }

    public boolean isRunning() {
        return isActive() && !UHCManager.get().isLobby();
    }

    public boolean needRooft() {
        return false;
    }

    public CustomInventory getMenu(Player player) {
        if (Variables.present(this.getClass())) {
            return new ScenarioVariableUi(player, this, new ScenariosUi(player, isSpecial()));
        }
        return null;
    }

    public boolean canOpenInGameTeamUi() {
        return true;
    }

    public void toggleActive() {
        active = !active;
    }

    public Document scenarioToDoc() {
        Document doc = VariableSerializer.toDoc(this, Variables.of(this));
        if (this instanceof ScenarioRole role) {
            doc.append("isRole", true);
            doc.append("rolesConfig", role.getRolesDocument());
        }
        return doc;
    }

    public void docToScenario(Document doc) {
        if (doc == null) return;
        VariableSerializer.fromDoc(this, doc, Variables.of(this));
        if (this instanceof ScenarioRole role && doc.containsKey("isRole") && doc.getBoolean("isRole")) {
            Document rolesDoc = (Document) doc.get("rolesConfig");
            role.loadRolesDocument(rolesDoc);
        }
    }

    public void setup() {
        if (this instanceof Listener listener) {
            Bukkit.getPluginManager().registerEvents(listener, Main.get());
        }

        if (getPath() == null) return;

        String configPath = "api/scenario/" + getPath() + ".yml";
        File file = new File(Main.get().getDataFolder(), configPath);

        if (!file.exists()) {
            ConfigUtils.createDefaultFiles(configPath);
            Bukkit.getLogger().info("Config scenario générée automatiquement : " + getPath() + ".yml");
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.options().copyDefaults(true);

        this.config = config;
    }

    public void onBreak(Player player, Block block, BlockBreakEvent event) {

    }

    public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {

    }
    public void onSec(Player p){

    }

    public void onStart(Player player) {

    }

    public void onGameStart() {

    }

    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {

    }
    public void onCraft(ItemStack result, CraftItemEvent event){

    }

    public void onDrop(PlayerDropItemEvent event) {

    }
    public void onPlayerInteract(Player player,PlayerInteractEvent event){

    }
    public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {

    }
    public void onPlace(Player player, Block block, BlockPlaceEvent event){

    }

    public void onMove(Player player, PlayerMoveEvent event) {

    }

    public void noFood(FoodLevelChangeEvent event) {

    }

    public void onBow(Entity entity, Player player, EntityShootBowEvent event) {

    }

    public void onProjectileHit(ProjectileHitEvent event) {

    }

    public void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event) {

    }

    public void onPortal(PlayerPortalEvent event) {

    }

    public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {

    }

    public void onBlockIgnite(Block block, BlockIgniteEvent event) {

    }

    public void onEntityExplode(Entity entity, EntityExplodeEvent event) {

    }

    public void onFurnace(ItemStack result, FurnaceSmeltEvent event) {
    }

    public void onFurnaceBurn(FurnaceBurnEvent event) {

    }

    public void onChatSpeak(Player player, String message, AsyncPlayerChatEvent event) {

    }

    public void onPlayerInteractEntity(Player player, PlayerInteractEntityEvent event) {

    }

    public void onPvP() {

    }

    public void onDamage(Player player, EntityDamageEvent event) {

    }

    public void onTeamUpdate() {
    }

    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
    }

    public void onTaupeTcCMD(Player player, int x, int y, int z, String coordsMessage) {

    }

    public void onPickUp(Player player, Item item, PlayerPickupItemEvent event) {
    }

    public void onAfterDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {

    }

    public void sendCustomDeathMessage(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
    }

    public void onKill(UHCPlayer killer, UHCPlayer victim) {

    }

    public void onStop() {

    }

    public static class ScenarioVariableUi extends VariableEditorUi {

        public ScenarioVariableUi(Player player, Scenario scenario, CustomInventory parent) {
            super(player, scenario, parent, "config-du-scenario");
        }

        @Override
        protected boolean includeDescriptor(VariableDescriptor d) {
            return d.type() != VariableType.RANDOM_EVENT;
        }
    }
}