package net.novaproject.novauhc.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class PlayerUtils {

    public static void setAbsorptionHearts(Player player, int hearts) {
        ((CraftPlayer) player).getHandle().setAbsorptionHearts(hearts);
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
