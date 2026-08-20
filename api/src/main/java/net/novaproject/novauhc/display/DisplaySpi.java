package net.novaproject.novauhc.display;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class DisplaySpi {

    private DisplaySpi() {
    }

    public interface IDisplayService {

        void title(Player player, String title, String subtitle, int ticks);

        void actionBar(Player player, String message);

        void nametag(Player player, String teamName, String prefix, String suffix);

        void floatingText(Player viewer, String text);

        void scoreboardLines(String key, Function<Player, List<String>> provider);

        void cooldown(Player player, String id, int seconds, Material icon);

        void fire(Player burning, java.awt.Color color);

        void resetFire(Player burning);

        void glow(Player viewer, Player target, java.awt.Color color);

        void resetGlow(Player viewer, Player target);

        void resetTeammates(Player viewer);

        boolean isLunar(Player viewer);

        void notification(Player viewer, String title, String description, int displaySeconds);

        void waypoint(Player viewer, String name, Location loc, java.awt.Color color, boolean beam);

        void removeWaypoint(Player viewer, String name);

        void vignette(Player viewer, String resourceLocation, float opacity);

        void resetVignette(Player viewer);

        void colorFor(Player viewer, Player target, ChatColor color);

        void resetColorFor(Player viewer, Player target);
    }

    public interface IFloatingText {

        void spawnFloatingDamage(Player viewer, String text);
    }

    public interface IInvisibilityTagService {

        void start(JavaPlugin plugin);

        void stop();

        void clear();
    }

    public interface IPerViewerHologramManager {

        void start();

        void show(Player viewer, Player target, String key, Function<Player, String> textSupplier);

        void hide(Player viewer, Player target, String key);

        void hideAllForViewer(Player viewer);

        void hideAllOnTarget(Player target);
    }

    public interface IPlayerColorManager {

        void applyColor(Player viewer, Player target, ChatColor color);

        void reapplyColorsForViewer(Player viewer);

        void reapplyColorsForTarget(Player target);

        void removeTeamForTarget(Player target);

        void removeColorForViewer(Player viewer, Player target);

        void cleanupViewer(UUID viewerUUID);
    }

    public interface IScoreboardService {

        void addPlayer(Player player);

        void removePlayer(UUID uuid);
    }

    public interface ITeamMarkerService {

        void start(JavaPlugin plugin);

        void stop();
    }
}
