package net.novaproject.novauhc.display;

import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

import net.novaproject.novauhc.ability.toolbox.Particles;
import net.novaproject.novauhc.display.PlayerColorManager;
import net.novaproject.novauhc.lobby.RankManager;
import net.novaproject.novauhc.display.scoreboard.ScoreboardService;
import net.novaproject.novauhc.display.apollo.VisualFeedback;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.novaproject.novauhc.Main;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public final class DisplayService {

    private static final Titles TITLES = new Titles();

    private static final DisplaySpi.IDisplayService INSTANCE = new DisplaySpi.IDisplayService() {
        @Override public void title(Player player, String title, String subtitle, int ticks) { DisplayService.title(player, title, subtitle, ticks); }
        @Override public void actionBar(Player player, String message) { DisplayService.actionBar(player, message); }
        @Override public void nametag(Player player, String teamName, String prefix, String suffix) { DisplayService.nametag(player, teamName, prefix, suffix); }
        @Override public void floatingText(Player viewer, String text) { DisplayService.floatingText(viewer, text); }
        @Override public void scoreboardLines(String key, Function<Player, List<String>> provider) { DisplayService.scoreboardLines(key, provider); }
        @Override public void cooldown(Player player, String id, int seconds, Material icon) { DisplayService.cooldown(player, id, seconds, icon); }
        @Override public void fire(Player burning, java.awt.Color color) { DisplayService.fire(burning, color); }
        @Override public void resetFire(Player burning) { DisplayService.resetFire(burning); }
        @Override public void glow(Player viewer, Player target, java.awt.Color color) { DisplayService.glow(viewer, target, color); }
        @Override public void resetGlow(Player viewer, Player target) { DisplayService.resetGlow(viewer, target); }
        @Override public void resetTeammates(Player viewer) { DisplayService.resetTeammates(viewer); }
        @Override public boolean isLunar(Player viewer) { return DisplayService.isLunar(viewer); }
        @Override public void notification(Player viewer, String title, String description, int displaySeconds) { DisplayService.notification(viewer, title, description, displaySeconds); }
        @Override public void waypoint(Player viewer, String name, Location loc, java.awt.Color color, boolean beam) { DisplayService.waypoint(viewer, name, loc, color, beam); }
        @Override public void removeWaypoint(Player viewer, String name) { DisplayService.removeWaypoint(viewer, name); }
        @Override public void vignette(Player viewer, String resourceLocation, float opacity) { DisplayService.vignette(viewer, resourceLocation, opacity); }
        @Override public void resetVignette(Player viewer) { DisplayService.resetVignette(viewer); }
        @Override public void colorFor(Player viewer, Player target, ChatColor color) { DisplayService.colorFor(viewer, target, color); }
        @Override public void resetColorFor(Player viewer, Player target) { DisplayService.resetColorFor(viewer, target); }
    };

    public static DisplaySpi.IDisplayService instance() { return INSTANCE; }



    public static void title(Player player, String title, String subtitle, int ticks) {
        if (player == null || !player.isOnline()) return;
        TITLES.sendTitle(player, title, subtitle, ticks);
    }

    public static void actionBar(Player player, String message) {
        if (player == null || !player.isOnline()) return;
        TITLES.sendActionText(player, message);
    }

    public static void nametag(Player player, String teamName, String prefix, String suffix) {
        if (player == null) return;
        if (prefix != null && prefix.length() > 15) prefix = prefix.substring(0, 15);
        if (suffix != null && suffix.length() > 15) suffix = suffix.substring(0, 15);
        TeamsTagsManager.setNameTag(player, teamName, prefix, suffix);
    }

    public static void floatingText(Player viewer, String text) {
        if (viewer == null || !viewer.isOnline()) return;
        FloatingText.spawnFloatingDamage(viewer, text);
    }

    public static void scoreboardLines(String key, Function<Player, List<String>> provider) {
        ScoreboardService.setExtraLines(key, provider);
    }


    public static void cooldown(Player player, String id, int seconds, Material icon) {
        VisualFeedback.cooldown(player, id, seconds, icon);
    }

    public static void fire(Player burning, java.awt.Color color) {
        if (VisualFeedback.isActive(VisualFeedback.ApolloConfig.Capability.FIRE)) {
            VisualFeedback.fire(burning, color);
            return;
        }
        if (burning == null || color == null) return;
        for (Player viewer : burning.getWorld().getPlayers()) {
            Particles.redstone(burning.getLocation().add(0, 1, 0), color, viewer);
        }
    }

    public static void resetFire(Player burning) {
        VisualFeedback.resetFire(burning);
    }

    public static void glow(Player viewer, Player target, java.awt.Color color) {
        if (VisualFeedback.isActive(VisualFeedback.ApolloConfig.Capability.GLOW)) {
            VisualFeedback.glow(viewer, target, color);
            return;
        }
        if (viewer == null || target == null || color == null) return;
        if (RankManager.isStaffOrOp(viewer)) return;
        PlayerColorManager.applyGlowColor(viewer, target, nearestChatColor(color));
    }

    public static void resetGlow(Player viewer, Player target) {
        if (VisualFeedback.isActive(VisualFeedback.ApolloConfig.Capability.GLOW)) {
            VisualFeedback.resetGlow(viewer, target);
            return;
        }
        PlayerColorManager.applyGlowColor(viewer, target, null);
    }

    public static void staffMod(Player viewer, boolean enabled) {
        VisualFeedback.staffMod(viewer, enabled);
    }

    private static final Map<ChatColor, java.awt.Color> CHAT_COLOR_RGB = Map.ofEntries(
            Map.entry(ChatColor.BLACK, new java.awt.Color(0, 0, 0)),
            Map.entry(ChatColor.DARK_BLUE, new java.awt.Color(0, 0, 170)),
            Map.entry(ChatColor.DARK_GREEN, new java.awt.Color(0, 170, 0)),
            Map.entry(ChatColor.DARK_AQUA, new java.awt.Color(0, 170, 170)),
            Map.entry(ChatColor.DARK_RED, new java.awt.Color(170, 0, 0)),
            Map.entry(ChatColor.DARK_PURPLE, new java.awt.Color(170, 0, 170)),
            Map.entry(ChatColor.GOLD, new java.awt.Color(255, 170, 0)),
            Map.entry(ChatColor.GRAY, new java.awt.Color(170, 170, 170)),
            Map.entry(ChatColor.DARK_GRAY, new java.awt.Color(85, 85, 85)),
            Map.entry(ChatColor.BLUE, new java.awt.Color(85, 85, 255)),
            Map.entry(ChatColor.GREEN, new java.awt.Color(85, 255, 85)),
            Map.entry(ChatColor.AQUA, new java.awt.Color(85, 255, 255)),
            Map.entry(ChatColor.RED, new java.awt.Color(255, 85, 85)),
            Map.entry(ChatColor.LIGHT_PURPLE, new java.awt.Color(255, 85, 255)),
            Map.entry(ChatColor.YELLOW, new java.awt.Color(255, 255, 85)),
            Map.entry(ChatColor.WHITE, new java.awt.Color(255, 255, 255)));

    public static ChatColor nearestChatColor(java.awt.Color color) {
        ChatColor best = ChatColor.WHITE;
        int bestDistance = Integer.MAX_VALUE;
        for (Map.Entry<ChatColor, java.awt.Color> entry : CHAT_COLOR_RGB.entrySet()) {
            java.awt.Color candidate = entry.getValue();
            int dr = candidate.getRed() - color.getRed();
            int dg = candidate.getGreen() - color.getGreen();
            int db = candidate.getBlue() - color.getBlue();
            int distance = dr * dr + dg * dg + db * db;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = entry.getKey();
            }
        }
        return best;
    }

    public static void teammates(Player viewer, List<VisualFeedback.TeammateMarker> mates) {
        VisualFeedback.teammates(viewer, mates);
    }

    public static void resetTeammates(Player viewer) {
        VisualFeedback.resetTeammates(viewer);
    }

    public static boolean isLunar(Player viewer) {
        return VisualFeedback.isLunar(viewer);
    }

    public static void notification(Player viewer, String title, String description, int displaySeconds) {
        VisualFeedback.notification(viewer, title, description, displaySeconds);
    }

    public static void waypoint(Player viewer, String name, Location loc, java.awt.Color color, boolean beam) {
        VisualFeedback.waypoint(viewer, name, loc, color, beam);
    }

    public static void removeWaypoint(Player viewer, String name) {
        VisualFeedback.removeWaypoint(viewer, name);
    }

    public static void vignette(Player viewer, String resourceLocation, float opacity) {
        VisualFeedback.vignette(viewer, resourceLocation, opacity);
    }

    public static void resetVignette(Player viewer) {
        VisualFeedback.resetVignette(viewer);
    }

    public static void colorFor(Player viewer, Player target, ChatColor color) {
        PlayerColorManager.applyColor(viewer, target, color);
    }

    public static void resetColorFor(Player viewer, Player target) {
        PlayerColorManager.removeColorForViewer(viewer, target);
    }

    public static void perceptionColorFor(Player viewer, Player target, ChatColor color) {
        PlayerColorManager.applyPerceptionColor(viewer, target, color);
    }

    public static void pastilleFor(Player viewer, Player target, ChatColor color) {
        PlayerColorManager.applyPastille(viewer, target, color);
    }

    public static ChatColor pastilleOf(Player viewer, Player target) {
        return PlayerColorManager.getPastilleColor(viewer, target);
    }

    public static void roleLabelFor(Player viewer, Player target, String label) {
        PlayerColorManager.applyRoleLabel(viewer, target, label);
    }

    public static String roleLabelOf(Player viewer, Player target) {
        return PlayerColorManager.getRoleLabel(viewer, target);
    }

    public static void hideNameTagFor(Player viewer, Player target, boolean hidden) {
        PlayerColorManager.setNameTagHidden(viewer, target, hidden);
    }

    public static void showAllNameTagsFor(Player viewer) {
        PlayerColorManager.clearHiddenNameTags(viewer);
    }

    public static class Titles {

        public void sendTitle(Player player, String title, String subtitle, int ticks) {
            IChatBaseComponent basetitle = new ChatComponentText(title == null ? "" : title);
            IChatBaseComponent basesubtitle = new ChatComponentText(subtitle == null ? "" : subtitle);
            PacketPlayOutTitle titlepacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, basetitle);
            PacketPlayOutTitle subtitlepacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, basesubtitle);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(titlepacket);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(subtitlepacket);
            sendTime(player, ticks);
        }

        private void sendTime(Player player, int ticks) {
            PacketPlayOutTitle titlepacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, null, 20, ticks, 20);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(titlepacket);
        }

        public void sendActionText(Player player, String message) {
            PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(message), (byte) 2);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public static final class FloatingText {

        private static final DisplaySpi.IFloatingText INSTANCE = new DisplaySpi.IFloatingText() {
            @Override public void spawnFloatingDamage(Player viewer, String text) { FloatingText.spawnFloatingDamage(viewer, text); }
        };

        public static DisplaySpi.IFloatingText instance() { return INSTANCE; }

        public static void spawnFloatingDamage(Player viewer, String text) {
            Location eyeLocation = viewer.getEyeLocation();
            Vector direction = eyeLocation.getDirection().normalize();

            Location spawnLoc = eyeLocation.add(direction.multiply(2));

            int entityId = HologramPackets.spawn(viewer, spawnLoc, text);

            new BukkitRunnable() {
                @Override
                public void run() {
                    HologramPackets.destroy(viewer, entityId);
                }
            }.runTaskLater(Main.get(), 10L);
        }

        @Deprecated
        public static void showHealthAboveHead(Player viewer, Player target) {
            if (!viewer.isOnline() || !target.isOnline()) return;
            if (viewer.equals(target)) return;

            double pct = (target.getHealth() / target.getMaxHealth()) * 100.0;
            String color = pct > 75 ? "§a" : pct > 50 ? "§e" : pct > 25 ? "§6" : "§c";

            String text = color + String.format("%.0f%%", pct);

            Location loc = target.getLocation().clone().add(0, 2.3, 0);
            int entityId = HologramPackets.spawn(viewer, loc, text);

            new BukkitRunnable() {
                @Override
                public void run() {
                    HologramPackets.destroy(viewer, entityId);
                }
            }.runTaskLater(Main.get(), 6L);
        }
    }
}
