package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;

import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.scenario.lang.ScenarioLangManager;
import net.novaproject.novauhc.scenario.lang.lang.GenieLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Genie extends Scenario {

    private final Map<UUID, Integer> playerWishes = new HashMap<>();
    private final Map<UUID, Integer> playerKills = new HashMap<>();

    @ScenarioVariable(
            name = "Nombre maximal de souhaits",
            description = "Nombre de souhaits disponibles par joueur",
            type = VariableType.INTEGER
    )
    private int maxWishes = 3;

    @ScenarioVariable(
            name = "Durée Speed II (ticks)",
            description = "Durée de l'effet Speed II en ticks",
            type = VariableType.INTEGER
    )
    private int speedDuration = 20 * 60 * 5;

    @ScenarioVariable(
            name = "Durée Strength I (ticks)",
            description = "Durée de l'effet Strength I en ticks",
            type = VariableType.INTEGER
    )
    private int strengthDuration = 20 * 60 * 5;

    @ScenarioVariable(
            name = "Kills requis pour souhaits basiques",
            description = "Nombre de kills requis pour débloquer les souhaits basiques",
            type = VariableType.INTEGER
    )
    private int basicKillRequirement = 0;

    @ScenarioVariable(
            name = "Kills requis pour souhaits moyens",
            description = "Nombre de kills requis pour débloquer les souhaits moyens",
            type = VariableType.INTEGER
    )
    private int mediumKillRequirement = 1;

    @ScenarioVariable(
            name = "Kills requis pour souhaits avancés",
            description = "Nombre de kills requis pour débloquer les souhaits avancés",
            type = VariableType.INTEGER
    )
    private int advancedKillRequirement = 2;

    @ScenarioVariable(
            name = "Kills requis pour souhaits légendaires",
            description = "Nombre de kills requis pour débloquer les souhaits légendaires",
            type = VariableType.INTEGER
    )
    private int legendaryKillRequirement = 3;

    @Override
    public String getName() {
        return "Genie";
    }

    @Override
    public String getDescription() {
        return "3 souhaits par partie ! Les options dépendent du nombre de kills.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.NETHER_STAR);
    }

    @Override
    public String getPath() {
        return "genie";
    }

    @Override
    public ScenarioLang[] getLang() {
        return GenieLang.values();
    }

    @Override
    public void onStart(Player player) {
        if (!isActive()) return;

        playerWishes.put(player.getUniqueId(), maxWishes);
        playerKills.put(player.getUniqueId(), 0);

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        ScenarioLangManager.send(uhcPlayer, GenieLang.WISHES_RECEIVED);
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (!isActive()) return;

        if (killer != null) {
            UUID killerUuid = killer.getPlayer().getUniqueId();
            playerKills.put(killerUuid, playerKills.getOrDefault(killerUuid, 0) + 1);
            killer.getPlayer().sendMessage("§6[Genie] §fVos options de souhaits se sont améliorées avec ce kill !");
        }
    }

    public boolean makeWish(Player player, String wishType) {
        if (!isActive()) return false;

        UUID playerUuid = player.getUniqueId();
        int wishesLeft = playerWishes.getOrDefault(playerUuid, 0);

        if (wishesLeft <= 0) {
            ScenarioLangManager.send(UHCPlayerManager.get().getPlayer(player), GenieLang.NO_WISHES_LEFT);
            return false;
        }

        int kills = playerKills.getOrDefault(playerUuid, 0);

        if (!canMakeWish(wishType, kills)) {
            ScenarioLangManager.send(UHCPlayerManager.get().getPlayer(player), GenieLang.NOT_ENOUGH_KILLS);
            return false;
        }

        boolean success = grantWish(player, wishType);

        if (success) {
            playerWishes.put(playerUuid, wishesLeft - 1);
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("%remaining%", String.valueOf(wishesLeft - 1));
            ScenarioLangManager.send(UHCPlayerManager.get().getPlayer(player), GenieLang.WISH_GRANTED, placeholders);

            Map<String, Object> broadcastPlaceholders = new HashMap<>();
            broadcastPlaceholders.put("%player%", player.getName());
            ScenarioLangManager.sendAll(GenieLang.WISH_ANNOUNCED, broadcastPlaceholders);
        }

        return success;
    }

    private boolean canMakeWish(String wishType, int kills) {
        switch (wishType.toLowerCase()) {
            case "heal":
            case "food":
            case "speed":
                return kills >= basicKillRequirement;
            case "strength":
            case "resistance":
            case "invisibility":
            case "arrows":
                return kills >= mediumKillRequirement;
            case "diamond":
            case "enchanted_book":
            case "golden_apple":
            case "teleport":
                return kills >= advancedKillRequirement;
            case "full_diamond":
            case "enchanted_sword":
            case "notch_apple":
            case "flight":
                return kills >= legendaryKillRequirement;
            default:
                return false;
        }
    }

    private boolean grantWish(Player player, String wishType) {
        switch (wishType.toLowerCase()) {
            case "heal":
                player.setHealth(player.getMaxHealth());
                ScenarioLangManager.send(UHCPlayerManager.get().getPlayer(player), GenieLang.HEAL_GRANTED);
                return true;
            case "food":
                player.setFoodLevel(20);
                player.setSaturation(20);
                ScenarioLangManager.send(UHCPlayerManager.get().getPlayer(player), GenieLang.FOOD_GRANTED);
                return true;
            case "speed":
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, speedDuration, 1));
                Map<String, Object> speedPlaceholders = new HashMap<>();
                speedPlaceholders.put("%duration%", String.valueOf(speedDuration / 20 / 60));
                ScenarioLangManager.send(UHCPlayerManager.get().getPlayer(player), GenieLang.SPEED_GRANTED, speedPlaceholders);
                return true;
            case "strength":
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, strengthDuration, 0));
                Map<String, Object> strengthPlaceholders = new HashMap<>();
                strengthPlaceholders.put("%duration%", String.valueOf(strengthDuration / 20 / 60));
                ScenarioLangManager.send(UHCPlayerManager.get().getPlayer(player), GenieLang.STRENGTH_GRANTED, strengthPlaceholders);
                return true;
            case "resistance":
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 6000, 0));
                player.sendMessage("§6[Genie] §fVous avez reçu Resistance I pendant 5 minutes !");
                return true;
            case "invisibility":
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1200, 0));
                player.sendMessage("§6[Genie] §fVous êtes invisible pendant 1 minute !");
                return true;
            case "arrows":
                player.getInventory().addItem(new ItemStack(Material.ARROW, 32));
                player.sendMessage("§6[Genie] §fVous avez reçu 32 flèches !");
                return true;
            default:
                return false;
        }
    }

    public int getRemainingWishes(Player player) {
        return playerWishes.getOrDefault(player.getUniqueId(), 0);
    }

    public int getPlayerKills(Player player) {
        return playerKills.getOrDefault(player.getUniqueId(), 0);
    }
}
