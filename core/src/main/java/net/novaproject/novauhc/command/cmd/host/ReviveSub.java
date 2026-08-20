package net.novaproject.novauhc.command.cmd.host;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.game.PendingDeathManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.display.DisplayService;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class ReviveSub extends HostSub {

    public static boolean revive(Player staff, UHCPlayer target) {
        Player tp = target == null ? null : target.getPlayer();
        if (tp == null || !tp.isOnline()) {
            LangManager.get().send(CoreLang.CMD_INVALID_PLAYER, staff);
            return false;
        }

        if (PendingDeathManager.get().isPending(tp.getUniqueId())) {
            PendingDeathManager.get().cancelPendingDeath(target);
        }

        World world = Common.get().getArena();
        WorldBorder worldBorder = world.getWorldBorder();
        Random random = new Random();
        double radius = worldBorder.getSize() / 2;
        double x = worldBorder.getCenter().getX() + (random.nextDouble() * 2 - 1) * radius;
        double z = worldBorder.getCenter().getZ() + (random.nextDouble() * 2 - 1) * radius;
        double y = world.getHighestBlockYAt((int) x, (int) z);
        Location location = new Location(world, x, y, z);

        for (PotionEffect e : tp.getActivePotionEffects()) {
            tp.removePotionEffect(e.getType());
        }
        tp.setFireTicks(0);
        tp.setMaxHealth(target.getLastMaxHealth() > 0 ? target.getLastMaxHealth() : 20);
        tp.setHealth(tp.getMaxHealth());
        tp.setFoodLevel(20);
        tp.setSaturation(20f);
        tp.setExp(0);
        tp.setLevel(0);

        target.setPlaying(true);

        if (target.getTeam().isPresent()) {
            UHCTeam team = target.getTeam().get();
            target.setTeam(Optional.of(team));
            DisplayService.nametag(tp, team.name(), team.prefix(), "");
        } else {
            DisplayService.nametag(tp, "", "", "");
        }

        tp.setGameMode(GameMode.SURVIVAL);
        tp.teleport(location);

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer(tp);
        }

        PlayerInventory inventory = tp.getInventory();
        inventory.clear();
        inventory.setArmorContents(null);
        if (target.getLastInventoryContents() != null) {
            inventory.setContents(target.getLastInventoryContents());
            if (target.getLastArmorContents() != null) {
                inventory.setArmorContents(target.getLastArmorContents());
            }
            tp.setLevel(target.getLastXpLevel());
        } else {
            for (ItemStack item : target.getDeathItem()) {
                if (item == null || item.getType() == Material.AIR) continue;
                if (isHelmet(item)) {
                    if (inventory.getHelmet() == null) {
                        inventory.setHelmet(item);
                        continue;
                    }
                } else if (isChestplate(item)) {
                    if (inventory.getChestplate() == null) {
                        inventory.setChestplate(item);
                        continue;
                    }
                } else if (isLeggings(item)) {
                    if (inventory.getLeggings() == null) {
                        inventory.setLeggings(item);
                        continue;
                    }
                } else if (isBoots(item)) {
                    if (inventory.getBoots() == null) {
                        inventory.setBoots(item);
                        continue;
                    }
                }
                inventory.addItem(item);
            }
        }
        target.getDeathItem().clear();
        tp.setNoDamageTicks(300);
        Bukkit.broadcastMessage(LangManager.get().get(CoreLang.COMMON_REVIVE_MESSAGE,
                Map.of("%player%", tp.getName(), "%host%", staff.getName())));
        Main.get().getLogger().info("[REVIVE] " + staff.getName() + " revived " + tp.getName());
        return true;
    }

    @Override
    protected void run(Player player, CommandArguments args) {
        if (args.size() < 1) return;
        Player bukkitTarget = Bukkit.getPlayer(args.get(0, ""));
        if (bukkitTarget == null) {
            LangManager.get().send(CoreLang.CMD_INVALID_PLAYER, player);
            return;
        }
        UHCPlayer target = UHCPlayerManager.get().getPlayer(bukkitTarget);
        if (target == null) {
            LangManager.get().send(CoreLang.CMD_INVALID_PLAYER, player);
            return;
        }
        revive(player, target);
    }

    private static boolean isHelmet(ItemStack item) {
        Material type = item.getType();
        return type == Material.LEATHER_HELMET || type == Material.GOLD_HELMET
                || type == Material.CHAINMAIL_HELMET || type == Material.IRON_HELMET
                || type == Material.DIAMOND_HELMET;
    }

    private static boolean isChestplate(ItemStack item) {
        Material type = item.getType();
        return type == Material.LEATHER_CHESTPLATE || type == Material.GOLD_CHESTPLATE
                || type == Material.CHAINMAIL_CHESTPLATE || type == Material.IRON_CHESTPLATE
                || type == Material.DIAMOND_CHESTPLATE;
    }

    private static boolean isLeggings(ItemStack item) {
        Material type = item.getType();
        return type == Material.LEATHER_LEGGINGS || type == Material.GOLD_LEGGINGS
                || type == Material.CHAINMAIL_LEGGINGS || type == Material.IRON_LEGGINGS
                || type == Material.DIAMOND_LEGGINGS;
    }

    private static boolean isBoots(ItemStack item) {
        Material type = item.getType();
        return type == Material.LEATHER_BOOTS || type == Material.GOLD_BOOTS
                || type == Material.CHAINMAIL_BOOTS || type == Material.IRON_BOOTS
                || type == Material.DIAMOND_BOOTS;
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        return playersFirstArg(args);
    }
}
