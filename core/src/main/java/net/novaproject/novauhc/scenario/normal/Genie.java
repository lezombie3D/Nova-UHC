package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.scenario.lang.ScenarioLangManager;
import net.novaproject.novauhc.scenario.lang.lang.GenieLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
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

        // Initialize player wishes
        int maxWishes = getConfig().getInt("max_wishes", 3);
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
            UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
            ScenarioLangManager.send(uhcPlayer, GenieLang.NO_WISHES_LEFT);
            return false;
        }

        int kills = playerKills.getOrDefault(playerUuid, 0);

        // Check if player can make this wish based on kills
        if (!canMakeWish(wishType, kills)) {
            UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
            ScenarioLangManager.send(uhcPlayer, GenieLang.NOT_ENOUGH_KILLS);
            return false;
        }

        // Grant the wish
        boolean success = grantWish(player, wishType);

        if (success) {
            playerWishes.put(playerUuid, wishesLeft - 1);

            UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("%remaining%", String.valueOf(wishesLeft - 1));
            ScenarioLangManager.send(uhcPlayer, GenieLang.WISH_GRANTED, placeholders);

            // Announce to all players
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
                return kills >= getConfig().getInt("wish_requirements.basic", 0);

            case "strength":
            case "resistance":
            case "invisibility":
            case "arrows":
                return kills >= getConfig().getInt("wish_requirements.medium", 1);

            case "diamond":
            case "enchanted_book":
            case "golden_apple":
            case "teleport":
                return kills >= getConfig().getInt("wish_requirements.advanced", 2);

            case "full_diamond":
            case "enchanted_sword":
            case "notch_apple":
            case "flight":
                return kills >= getConfig().getInt("wish_requirements.legendary", 3);

            default:
                return false;
        }
    }

    private boolean grantWish(Player player, String wishType) {
        switch (wishType.toLowerCase()) {
            // Basic wishes
            case "heal":
                player.setHealth(player.getMaxHealth());
                UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
                ScenarioLangManager.send(uhcPlayer, GenieLang.HEAL_GRANTED);
                return true;

            case "food":
                player.setFoodLevel(20);
                player.setSaturation(20);
                UHCPlayer uhcPlayer2 = UHCPlayerManager.get().getPlayer(player);
                ScenarioLangManager.send(uhcPlayer2, GenieLang.FOOD_GRANTED);
                return true;

            case "speed":
                int speedDuration = getConfig().getInt("wish_effects.speed_duration", 6000);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, speedDuration, 1));
                UHCPlayer uhcPlayer3 = UHCPlayerManager.get().getPlayer(player);
                Map<String, Object> speedPlaceholders = new HashMap<>();
                speedPlaceholders.put("%duration%", String.valueOf(speedDuration / 20 / 60));
                ScenarioLangManager.send(uhcPlayer3, GenieLang.SPEED_GRANTED, speedPlaceholders);
                return true;

            // Medium wishes
            case "strength":
                int strengthDuration = getConfig().getInt("wish_effects.strength_duration", 6000);
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, strengthDuration, 0));
                UHCPlayer uhcPlayer4 = UHCPlayerManager.get().getPlayer(player);
                Map<String, Object> strengthPlaceholders = new HashMap<>();
                strengthPlaceholders.put("%duration%", String.valueOf(strengthDuration / 20 / 60));
                ScenarioLangManager.send(uhcPlayer4, GenieLang.STRENGTH_GRANTED, strengthPlaceholders);
                return true;

            case "resistance":
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 6000, 0)); // 5 minutes Resistance I
                player.sendMessage("§6[Genie] §fVous avez reçu Resistance I pendant 5 minutes !");
                return true;

            case "invisibility":
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1200, 0)); // 1 minute Invisibility
                player.sendMessage("§6[Genie] §fVous êtes invisible pendant 1 minute !");
                return true;

            case "arrows":
                player.getInventory().addItem(new ItemStack(Material.ARROW, 32));
                player.sendMessage("§6[Genie] §fVous avez reçu 32 flèches !");
                return true;

            // Advanced wishes
            case "diamond":
                player.getInventory().addItem(new ItemStack(Material.DIAMOND, 5));
                player.sendMessage("§6[Genie] §fVous avez reçu 5 diamants !");
                return true;

            case "enchanted_book":
                ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
                player.getInventory().addItem(book);
                player.sendMessage("§6[Genie] §fVous avez reçu un livre enchanté !");
                return true;

            case "golden_apple":
                player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 3));
                player.sendMessage("§6[Genie] §fVous avez reçu 3 pommes d'or !");
                return true;

            case "teleport":
                // Teleport to a random safe location
                teleportToSafeLocation(player);
                player.sendMessage("§6[Genie] §fVous avez été téléporté !");
                return true;

            // Legendary wishes
            case "full_diamond":
                giveFullDiamondArmor(player);
                player.sendMessage("§6[Genie] §fVous avez reçu une armure complète en diamant !");
                return true;

            case "enchanted_sword":
                ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
                sword.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DAMAGE_ALL, 3);
                sword.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.FIRE_ASPECT, 2);
                player.getInventory().addItem(sword);
                player.sendMessage("§6[Genie] §fVous avez reçu une épée enchantée !");
                return true;

            case "notch_apple":
                ItemStack notchApple = new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1);
                player.getInventory().addItem(notchApple);
                player.sendMessage("§6[Genie] §fVous avez reçu une pomme d'or enchantée !");
                return true;

            case "flight":
                player.setAllowFlight(true);
                player.setFlying(true);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.setAllowFlight(false);
                        player.setFlying(false);
                    }
                }.runTaskLaterAsynchronously(Main.get(), 20 * 30);
                player.sendMessage("§6[Genie] §fVous pouvez voler pendant 30 secondes !");
                return true;

            default:
                return false;
        }
    }

    private void giveFullDiamondArmor(Player player) {
        player.getInventory().addItem(
                new ItemStack(Material.DIAMOND_HELMET),
                new ItemStack(Material.DIAMOND_CHESTPLATE),
                new ItemStack(Material.DIAMOND_LEGGINGS),
                new ItemStack(Material.DIAMOND_BOOTS)
        );
    }

    private void teleportToSafeLocation(Player player) {
        // Simple teleportation - in a real implementation, you'd find a safe location
        org.bukkit.Location currentLoc = player.getLocation();
        org.bukkit.Location newLoc = currentLoc.clone().add(
                (Math.random() - 0.5) * 200,
                0,
                (Math.random() - 0.5) * 200
        );

        // Find safe Y level
        newLoc.setY(player.getWorld().getHighestBlockYAt(newLoc) + 1);
        player.teleport(newLoc);
    }

    // Get available wishes for a player
    public List<String> getAvailableWishes(Player player) {
        int kills = playerKills.getOrDefault(player.getUniqueId(), 0);
        List<String> available = new ArrayList<>();

        // Basic wishes
        available.add("heal - Soigner complètement");
        available.add("food - Restaurer la faim");
        available.add("speed - Speed II (5 min)");

        if (kills >= 1) {
            available.add("strength - Strength I (5 min)");
            available.add("resistance - Resistance I (5 min)");
            available.add("invisibility - Invisibilité (1 min)");
            available.add("arrows - 32 flèches");
        }

        if (kills >= 2) {
            available.add("diamond - 5 diamants");
            available.add("enchanted_book - Livre enchanté");
            available.add("golden_apple - 3 pommes d'or");
            available.add("teleport - Téléportation aléatoire");
        }

        if (kills >= 3) {
            available.add("full_diamond - Armure diamant complète");
            available.add("enchanted_sword - Épée enchantée");
            available.add("notch_apple - Pomme d'or enchantée");
            available.add("flight - Vol temporaire");
        }

        return available;
    }

    // Get player's remaining wishes
    public int getRemainingWishes(Player player) {
        return playerWishes.getOrDefault(player.getUniqueId(), 0);
    }

    // Get player's kills
    public int getPlayerKills(Player player) {
        return playerKills.getOrDefault(player.getUniqueId(), 0);
    }

    // Admin command to give wishes
    public void giveWishes(Player player, int amount) {
        UUID playerUuid = player.getUniqueId();
        int current = playerWishes.getOrDefault(playerUuid, 0);
        playerWishes.put(playerUuid, Math.min(getConfig().getInt("max_wishes", 3), current + amount));

        player.sendMessage("§6[Genie] §fVous avez reçu " + amount + " souhait(s) !");
    }

    // Reset player wishes (admin command)
    public void resetWishes(Player player) {
        playerWishes.put(player.getUniqueId(), getConfig().getInt("max_wishes", 3));
        playerKills.put(player.getUniqueId(), 0);
        player.sendMessage("§6[Genie] §fVos souhaits ont été réinitialisés !");
    }
}
