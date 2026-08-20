package net.novaproject.novauhc.player.utils;

import net.novaproject.novauhc.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;

public class PlayerUtils {

    public static void giveOrDrop(Player player, ItemStack item) {
        if (player == null || item == null || item.getAmount() <= 0) return;
        for (ItemStack overflow : player.getInventory().addItem(item).values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), overflow);
        }
    }

    public static void pushBlocks(Player target, Vector direction, double blocks, double verticalBoost) {
        if (target == null || direction == null || blocks <= 0) return;
        final Vector dir = new Vector(direction.getX(), 0, direction.getZ());
        if (dir.lengthSquared() < 1.0e-6) return;
        dir.normalize();
        final int ticks = (int) Math.ceil(blocks);
        target.setFallDistance(0f);
        target.setVelocity(new Vector(dir.getX(), Math.max(0.0, verticalBoost), dir.getZ()));
        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (target == null || !target.isOnline() || target.isDead() || t++ >= ticks) { cancel(); return; }
                target.setFallDistance(0f);
                Vector v = target.getVelocity();
                target.setVelocity(new Vector(dir.getX(), v.getY(), dir.getZ()));
            }
        }.runTaskTimer(Main.get(), 1L, 1L);
    }

    public static void knockbackFrom(Player target, Location source, double blocks, double verticalBoost) {
        if (target == null || source == null) return;
        Vector away = target.getLocation().toVector().subtract(source.toVector());
        if (away.lengthSquared() < 1.0e-6) away = target.getLocation().getDirection();
        pushBlocks(target, away, blocks, verticalBoost);
    }

    public static void setAbsorptionHearts(Player player, int hearts) {
        ((CraftPlayer) player).getHandle().setAbsorptionHearts(hearts);
    }

    public static void setAbsorptionHearts(Player player, double hearts) {
        ((CraftPlayer) player).getHandle().setAbsorptionHearts((float) hearts);
    }

    public static double getAbsorptionHearts(Player player) {
        return ((CraftPlayer) player).getHandle().getAbsorptionHearts();
    }

    public static void showHealthBelowName(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective("HP", "health");
        objective.setDisplayName(ChatColor.RED + "❤");
        objective.setDisplaySlot(DisplaySlot.BELOW_NAME);

        player.setScoreboard(board);
    }

    public static void hideHealthBelowName(Player player) {
        player.getScoreboard().clearSlot(DisplaySlot.BELOW_NAME);
    }
}

