package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.player.utils.PlayerUtils;
import net.novaproject.novauhc.player.UHCPlayerManager;
import java.util.Random;
import java.util.List;import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.command.CommandManager;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioLang;
import org.bukkit.enchantments.Enchantment;

public class Genie extends Scenario {

    @Override
    public Family getFamily() { return Family.LOOT; }

    private final Map<UUID, Integer> playerWishes = new HashMap<>();
    private final Map<UUID, Integer> playerKills = new HashMap<>();

    @Var(lang = ScenarioVarLang.class, nameKey = "GENIE_VAR_MAX_WISHES_NAME", descKey = "GENIE_VAR_MAX_WISHES_DESC", type = VariableType.INTEGER)
    private int maxWishes = 3;

    @Var(lang = ScenarioVarLang.class, nameKey = "GENIE_VAR_SPEED_DURATION_NAME", descKey = "GENIE_VAR_SPEED_DURATION_DESC", type = VariableType.TIME)
    private int speedDuration = 300;

    @Var(lang = ScenarioVarLang.class, nameKey = "GENIE_VAR_STRENGTH_DURATION_NAME", descKey = "GENIE_VAR_STRENGTH_DURATION_DESC", type = VariableType.TIME)
    private int strengthDuration = 300;

    @Var(name = "Durée résistance", desc = "Durée en secondes du vœu Résistance.", type = VariableType.TIME, min = 1)
    private int resistanceDuration = 300;

    @Var(name = "Durée invisibilité", desc = "Durée en secondes du vœu Invisibilité.", type = VariableType.TIME, min = 1)
    private int invisibilityDuration = 60;

    @Var(lang = ScenarioVarLang.class, nameKey = "GENIE_VAR_BASIC_KILL_REQUIREMENT_NAME", descKey = "GENIE_VAR_BASIC_KILL_REQUIREMENT_DESC", type = VariableType.INTEGER)
    private int basicKillRequirement = 0;

    @Var(lang = ScenarioVarLang.class, nameKey = "GENIE_VAR_MEDIUM_KILL_REQUIREMENT_NAME", descKey = "GENIE_VAR_MEDIUM_KILL_REQUIREMENT_DESC", type = VariableType.INTEGER)
    private int mediumKillRequirement = 1;

    @Var(lang = ScenarioVarLang.class, nameKey = "GENIE_VAR_ADVANCED_KILL_REQUIREMENT_NAME", descKey = "GENIE_VAR_ADVANCED_KILL_REQUIREMENT_DESC", type = VariableType.INTEGER)
    private int advancedKillRequirement = 2;

    @Var(lang = ScenarioVarLang.class, nameKey = "GENIE_VAR_LEGENDARY_KILL_REQUIREMENT_NAME", descKey = "GENIE_VAR_LEGENDARY_KILL_REQUIREMENT_DESC", type = VariableType.INTEGER)
    private int legendaryKillRequirement = 3;

    @Override
    public String getName() {
        return "Genie";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.GENIE, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.NETHER_STAR);
    }

    @Override
    public void onStart(Player player) {
        if (!isActive()) return;

        playerWishes.put(player.getUniqueId(), maxWishes);
        playerKills.put(player.getUniqueId(), 0);

        LangManager.get().send(ScenarioLang.GENIE_WISHES_RECEIVED, player);
    }

    @Override
    public void onGameStart() {
        CommandManager.get().register("wish", new WishCMD());
    }

    @Override
    public void onStop() {
        playerWishes.clear();
        playerKills.clear();
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (!isActive()) return;

        if (killer != null) {
            UUID killerUuid = killer.getPlayer().getUniqueId();
            playerKills.put(killerUuid, playerKills.getOrDefault(killerUuid, 0) + 1);
            LangManager.get().send(ScenarioLang.GENIE_WISHES_IMPROVED, killer.getPlayer());
        }
    }

    public boolean makeWish(Player player, String wishType) {
        if (!isActive()) return false;

        UUID playerUuid = player.getUniqueId();
        int wishesLeft = playerWishes.getOrDefault(playerUuid, 0);

        if (wishesLeft <= 0) {
            LangManager.get().send(ScenarioLang.GENIE_NO_WISHES_LEFT, player);
            return false;
        }

        int kills = playerKills.getOrDefault(playerUuid, 0);

        if (!canMakeWish(wishType, kills)) {
            LangManager.get().send(ScenarioLang.GENIE_NOT_ENOUGH_KILLS, player);
            return false;
        }

        boolean success = grantWish(player, wishType);

        if (success) {
            playerWishes.put(playerUuid, wishesLeft - 1);
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("%remaining%", String.valueOf(wishesLeft - 1));
            LangManager.get().send(ScenarioLang.GENIE_WISH_GRANTED, player, placeholders);

            Map<String, Object> broadcastPlaceholders = new HashMap<>();
            broadcastPlaceholders.put("%player%", player.getName());
            LangManager.get().sendAll(ScenarioLang.GENIE_WISH_ANNOUNCED, broadcastPlaceholders);
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
                return kills >= legendaryKillRequirement;
            default:
                return false;
        }
    }

    private boolean grantWish(Player player, String wishType) {
        switch (wishType.toLowerCase()) {
            case "heal":
                player.setHealth(player.getMaxHealth());
                LangManager.get().send(ScenarioLang.GENIE_HEAL_GRANTED, player);
                return true;
            case "food":
                player.setFoodLevel(20);
                player.setSaturation(20);
                LangManager.get().send(ScenarioLang.GENIE_FOOD_GRANTED, player);
                return true;
            case "speed":
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, speedDuration * 20, 1));
                Map<String, Object> speedPlaceholders = new HashMap<>();
                speedPlaceholders.put("%duration%", String.valueOf(speedDuration / 60));
                LangManager.get().send(ScenarioLang.GENIE_SPEED_GRANTED, player, speedPlaceholders);
                return true;
            case "strength":
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, strengthDuration * 20, 0));
                Map<String, Object> strengthPlaceholders = new HashMap<>();
                strengthPlaceholders.put("%duration%", String.valueOf(strengthDuration / 60));
                LangManager.get().send(ScenarioLang.GENIE_STRENGTH_GRANTED, player, strengthPlaceholders);
                return true;
            case "resistance":
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, resistanceDuration * 20, 0));
                LangManager.get().send(ScenarioLang.GENIE_RECEIVED_RESISTANCE, player);
                return true;
            case "invisibility":
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, invisibilityDuration * 20, 0));
                LangManager.get().send(ScenarioLang.GENIE_RECEIVED_INVISIBILITY, player);
                return true;
            case "arrows":
                PlayerUtils.giveOrDrop(player, new ItemStack(Material.ARROW, 32));
                LangManager.get().send(ScenarioLang.GENIE_RECEIVED_ARROWS, player);
                return true;
            case "diamond":
                PlayerUtils.giveOrDrop(player, new ItemStack(Material.DIAMOND, 5));
                LangManager.get().send(ScenarioLang.GENIE_WISH_GRANTED, player);
                return true;
            case "enchanted_book":
                PlayerUtils.giveOrDrop(player, new ItemStack(Material.ENCHANTED_BOOK, 1));
                LangManager.get().send(ScenarioLang.GENIE_WISH_GRANTED, player);
                return true;
            case "golden_apple":
                PlayerUtils.giveOrDrop(player, new ItemStack(Material.GOLDEN_APPLE, 3));
                LangManager.get().send(ScenarioLang.GENIE_WISH_GRANTED, player);
                return true;
            case "teleport": {
                List<UHCPlayer> players =
                        UHCPlayerManager.get().getPlayingOnlineUHCPlayers();
                if (players.size() > 1) {
                    UHCPlayer target = players.get(new Random().nextInt(players.size()));
                    while (target.getPlayer().equals(player) && players.size() > 1) {
                        target = players.get(new Random().nextInt(players.size()));
                    }
                    player.teleport(target.getPlayer().getLocation());
                }
                LangManager.get().send(ScenarioLang.GENIE_WISH_GRANTED, player);
                return true;
            }
            case "full_diamond": {
                PlayerUtils.giveOrDrop(player, new ItemStack(Material.DIAMOND_HELMET));
                PlayerUtils.giveOrDrop(player, new ItemStack(Material.DIAMOND_CHESTPLATE));
                PlayerUtils.giveOrDrop(player, new ItemStack(Material.DIAMOND_LEGGINGS));
                PlayerUtils.giveOrDrop(player, new ItemStack(Material.DIAMOND_BOOTS));
                LangManager.get().send(ScenarioLang.GENIE_WISH_GRANTED, player);
                return true;
            }
            case "enchanted_sword": {
                ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
                sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 5);
                PlayerUtils.giveOrDrop(player, sword);
                LangManager.get().send(ScenarioLang.GENIE_WISH_GRANTED, player);
                return true;
            }
            case "notch_apple":
                PlayerUtils.giveOrDrop(player, new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1));
                LangManager.get().send(ScenarioLang.GENIE_WISH_GRANTED, player);
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

    public static class WishCMD extends Command {

        @Override
        public void execute(CommandArguments arguments) {
            if (!(arguments.getSender() instanceof Player player)) return;

            Genie genie = UHCManager.get().getScenarioManager().getScenario(Genie.class);
            if (genie == null || !genie.isActive()) return;

            if (arguments.size() < 1) {
                player.sendMessage(LangManager.get().get(USAGE, player,
                        Map.of("%remaining%", genie.getRemainingWishes(player), "%kills%", genie.getPlayerKills(player))));
                return;
            }

            genie.makeWish(player, arguments.getArgument(0));
        }

        @Override
        public List<String> tabComplete(CommandArguments arguments) {
            return getStrings(arguments, WISHES);
        }
    }

    private static final String[] WISHES = {
            "heal", "food", "speed", "strength", "resistance", "invisibility", "arrows",
            "diamond", "enchanted_book", "golden_apple", "teleport",
            "full_diamond", "enchanted_sword", "notch_apple"
    };

    private static final DynamicLang USAGE = DynamicLang.of("scenario.genie.usage",
            "§b✦ §7Vœux restants §8: §f%remaining% §8| §7Kills §8: §f%kills%\n"
                    + "§70 kill §8» §fheal, food, speed\n"
                    + "§71 kill §8» §fstrength, resistance, invisibility, arrows\n"
                    + "§72 kills §8» §fdiamond, enchanted_book, golden_apple, teleport\n"
                    + "§73 kills §8» §ffull_diamond, enchanted_sword, notch_apple\n"
                    + "§8» §7Usage §8: §f/wish <vœu>");
}
