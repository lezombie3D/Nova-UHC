package net.novaproject.novauhc.scenario;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.scenario.lang.ScenarioLangManager;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.ui.config.ScenariosUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
@Setter
public abstract class Scenario {


    private FileConfiguration config;
    public abstract String getName();

    public abstract String getDescription();

    public abstract ItemCreator getItem();

    private boolean active = false;

    public boolean isSpecial() {
        return false;
    }

    public boolean isWin() {
        return false;
    }

    public boolean hascustomDeathMessage() {
        return false;
    }

    public boolean hasCustomTeamTchat() {
        return false;
    }

    public String getPath() {
        return null;
    }

    public void enable() {
        if (!isActive()) {
            active = true;
        }
    }

    public boolean needRooft() {
        return false;
    }

    public CustomInventory getMenu(Player player) {
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ScenarioVariable.class)) {
                return new ScenarioVariableUi(player,this,new ScenariosUi(player,isSpecial()));
            }
        }
        return null;
    }


    public void toggleActive() {
        active = !active;
    }

    public Document scenarioToDoc() {
        Document doc = new Document();
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ScenarioVariable.class)) {
                field.setAccessible(true);
                try {
                    doc.append(field.getName(), field.get(this));
                } catch (IllegalAccessException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Could not access field " + field.getName(), e);
                }
            }
        }
        if(this instanceof ScenarioRole role){
            doc.append("isRole", true);
            doc.append("rolesConfig",role.getRolesDocument());
        }
        return doc;
    }

    public void docToScenario(Document doc) {
        if (doc == null) return;
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ScenarioVariable.class)) {
                if (doc.containsKey(field.getName())) {
                    field.setAccessible(true);
                    try {
                        Object value = doc.get(field.getName());
                        if (value instanceof Number) {
                            if (field.getType() == int.class || field.getType() == Integer.class) {
                                field.set(this, ((Number) value).intValue());
                            } else if (field.getType() == double.class || field.getType() == Double.class) {
                                field.set(this, ((Number) value).doubleValue());
                            } else if (field.getType() == float.class || field.getType() == Float.class) {
                                field.set(this, ((Number) value).floatValue());
                            } else if (field.getType() == long.class || field.getType() == Long.class) {
                                field.set(this, ((Number) value).longValue());
                            } else {
                                field.set(this, value);
                            }
                        } else {
                            field.set(this, value);
                        }
                    } catch (IllegalAccessException e) {
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Could not set field " + field.getName(), e);
                    }
                }
            }
            if (this instanceof ScenarioRole role && doc.containsKey("isRole") && doc.getBoolean("isRole")) {
                Document rolesDoc = (Document) doc.get("rolesConfig");
                role.loadRolesDocument(rolesDoc);
            }
        }
    }


    public void setup() {
        if (getPath() == null) return;

        String configPath = "api/scenario/" + getPath() + ".yml";
        File file = new File(Main.get().getDataFolder(), configPath);

        YamlConfiguration config = file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();

        if (getLang() != null) {
            for (ScenarioLang lang : getLang()) {
                lang.setConfig(config);
                if (!config.contains(lang.getPath())) {
                    config.set(lang.getPath(), lang.getDefaultMessage());
                }
            }
            ScenarioLangManager.loadMessages(config, getLang());
        }

        config.options().copyDefaults(true);

        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                config.save(file);
                Bukkit.getLogger().info("Config scenario générée automatiquement : " + getPath() + ".yml");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.config = config;
    }



    public ScenarioLang[] getLang() {
        return null;
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

    public void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event) {

    }

    public void onPortal(PlayerPortalEvent event) {

    }

    public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {

    }

    public void onBlockIgnite(Block block, BlockIgniteEvent event) {

    }

    public void onEtityExplose(Entity entity, EntityExplodeEvent event) {

    }

    public void onEnchant(Player player, Enchantment enchant, ItemStack item, EnchantItemEvent event) {

    }

    public void onAnvilUse(ItemStack result, InventoryClickEvent event) {

    }

    public void onFurnace(ItemStack result, FurnaceSmeltEvent event) {
    }

    public void onFurnaceBurn(FurnaceBurnEvent event) {

    }

    public void onChatSpeek(Player player, String message, AsyncPlayerChatEvent event) {

    }

    public void onPlayerInteractonEntity(Player player, PlayerInteractEntityEvent event) {

    }

    public void onPvP() {

    }

    public void onDamage(Player player, EntityDamageEvent event) {

    }

    public void onCroCMD(Player player, String subCommand, String[] args) {

    }


    public void onTeamUpdate() {
    }

    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
    }


    public void onTeamCordCMD(Player player, int x, int y, int z, String coordsMessage) {

    }

    public void onTaupeTcCMD(Player player, int x, int y, int z, String coordsMessage) {

    }

    public void onPickUp(Player player, Item item, PlayerPickupItemEvent event) {
    }

    public void onAfterDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {

    }

    public void sendCustomDeathMessage(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
    }

    public void onFfCMD(Player player, String subCommand, String[] args) {

    }

    public void onDeathNoteCMD(Player player, String subCommand, String[] args) {

    }

    public void onKill(UHCPlayer killer, UHCPlayer victim) {

    }
}
