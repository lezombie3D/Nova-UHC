package net.novaproject.novauhc.display;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class InvisibilityTagService {

    private static final Map<UUID, String> hiddenPrevTeam = new HashMap<>();

    private static BukkitTask task;

    private static final DisplaySpi.IInvisibilityTagService INSTANCE = new DisplaySpi.IInvisibilityTagService() {
        @Override public void start(JavaPlugin plugin) { InvisibilityTagService.start(plugin); }
        @Override public void stop() { InvisibilityTagService.stop(); }
        @Override public void clear() { InvisibilityTagService.clear(); }
    };

    public static DisplaySpi.IInvisibilityTagService instance() { return INSTANCE; }

    public static void start(JavaPlugin plugin) {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    tick(player);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public static void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        clear();
    }

    @SuppressWarnings("deprecation")
    private static void tick(Player player) {
        boolean invisible = player.hasPotionEffect(PotionEffectType.INVISIBILITY);
        UUID uuid = player.getUniqueId();
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();

        if (invisible && !hiddenPrevTeam.containsKey(uuid)) {
            Team prev = sb.getPlayerTeam(player);
            hiddenPrevTeam.put(uuid, prev != null ? prev.getName() : "");
            String hideName = hideTeamName(player);
            Team hide = TeamsTagsManager.ScoreboardTeams.getOrCreate(sb, hideName);
            if (prev != null) {
                hide.setPrefix(prev.getPrefix());
                hide.setSuffix(prev.getSuffix());
            }
            hide.setNameTagVisibility(NameTagVisibility.NEVER);
            hide.addPlayer(player);
            PlayerColorManager.reapplyColorsForTarget(player);
        } else if (!invisible && hiddenPrevTeam.containsKey(uuid)) {
            String prevName = hiddenPrevTeam.remove(uuid);
            Team hide = sb.getTeam(hideTeamName(player));
            if (hide != null) {
                hide.removePlayer(player);
                if (hide.getEntries().isEmpty()) hide.unregister();
            }
            if (prevName != null && !prevName.isEmpty()) {
                Team prev = sb.getTeam(prevName);
                if (prev != null) prev.addPlayer(player);
            }
            PlayerColorManager.reapplyColorsForTarget(player);
        }
    }

    private static String hideTeamName(Player player) {
        String base = "iv_" + player.getName();
        return base.length() > 16 ? base.substring(0, 16) : base;
    }

    public static void clear() {
        hiddenPrevTeam.clear();
    }
}

