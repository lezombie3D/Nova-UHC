package net.novaproject.novauhc.utils;

import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.WorldBorder;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.display.TabFreezeService;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class UHCUtils {

    public static final int LOOPED_EFFECT_DURATION_TICKS = 80;
    public static final int LOOPED_NIGHT_VISION_DURATION_TICKS = 260;

    public static void applyInfiniteEffects(PotionEffect[] effects, Player player) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        for (PotionEffect activeEffect : effects) {
            player.getPlayer().removePotionEffect(activeEffect.getType());
        }
        for (PotionEffect effect : effects) {
            int amplifier = mirrorAmplifier(effect, uhcPlayer);
            int duration = effect.getType().equals(PotionEffectType.NIGHT_VISION)
                    ? LOOPED_NIGHT_VISION_DURATION_TICKS
                    : LOOPED_EFFECT_DURATION_TICKS;
            new PotionEffect(effect.getType(), duration, amplifier,
                    effect.isAmbient(), effect.hasParticles()).apply(player);
        }
    }

    private static int mirrorAmplifier(PotionEffect effect, UHCPlayer uhcPlayer) {
        if (uhcPlayer == null) return effect.getAmplifier();
        if (UHCManager.get() == null || !UHCManager.get().isMirroredEffects()) return effect.getAmplifier();
        PotionEffectType type = effect.getType();
        int level;
        if (type.equals(PotionEffectType.INCREASE_DAMAGE)) {
            level = strengthLevel(percentToInt(percentFor(type, uhcPlayer)));
        } else if (type.equals(PotionEffectType.SPEED)) {
            level = twentyStepLevel(percentToInt(percentFor(type, uhcPlayer)));
        } else if (type.equals(PotionEffectType.DAMAGE_RESISTANCE)) {
            level = twentyStepLevel(percentToInt(percentFor(type, uhcPlayer)));
        } else {
            return effect.getAmplifier();
        }
        return Math.max(effect.getAmplifier(), level - 1);
    }

    private static double percentFor(PotionEffectType type, UHCPlayer uhcPlayer) {
        Role role = activeRoleOf(uhcPlayer);
        if (type.equals(PotionEffectType.INCREASE_DAMAGE)) {
            return role != null ? role.getStrengthPercent() : uhcPlayer.getForcePercent();
        }
        if (type.equals(PotionEffectType.SPEED)) {
            return role != null ? role.getSpeedPercent() : uhcPlayer.getSpeedPercent();
        }
        return role != null ? role.getResistancePercent() : uhcPlayer.getResistancePercent();
    }

    private static Role activeRoleOf(UHCPlayer uhcPlayer) {
        ScenarioRole<?> scenarioRole = ScenarioManager.get()
                .getActiveSpecialScenarios()
                .stream()
                .filter(s -> s instanceof ScenarioRole)
                .map(s -> (ScenarioRole<?>) s)
                .findFirst()
                .orElse(null);
        return scenarioRole == null ? null : scenarioRole.getRoleByUHCPlayer(uhcPlayer);
    }

    private static int percentToInt(double percent) {
        return (int) Math.round(percent * 100);
    }

    public static int strengthLevel(int percent) {
        if (percent <= 0) return 0;
        return (percent + 14) / 15;
    }

    public static int twentyStepLevel(int percent) {
        if (percent <= 0) return 0;
        return (percent + 19) / 20;
    }

    public static final class Rng {

        private Rng() {
        }

        public static boolean chance(int percent) {
            return percent > 0 && ThreadLocalRandom.current().nextInt(100) < percent;
        }

        public static int range(int min, int max) {
            int bas = Math.min(min, max);
            int haut = Math.max(min, max);
            return bas + ThreadLocalRandom.current().nextInt(haut - bas + 1);
        }
    }

    public static final class GeoUtils {


        public static String getArrowDirection(Location from, Location to, float playerYaw) {
            double dx = to.getX() - from.getX();
            double dz = to.getZ() - from.getZ();

            double angle = Math.toDegrees(Math.atan2(-dx, dz));
            angle = (angle + 360) % 360;

            float normalizedYaw = (playerYaw + 360) % 360;

            double relativeAngle = (angle - normalizedYaw + 360) % 360;

            if (relativeAngle >= 337.5 || relativeAngle < 22.5) return ChatColor.BOLD + "↑";
            if (relativeAngle >= 22.5 && relativeAngle < 67.5) return ChatColor.BOLD + "↗";
            if (relativeAngle >= 67.5 && relativeAngle < 112.5) return ChatColor.BOLD + "→";
            if (relativeAngle >= 112.5 && relativeAngle < 157.5) return ChatColor.BOLD + "↘";
            if (relativeAngle >= 157.5 && relativeAngle < 202.5) return ChatColor.BOLD + "↓";
            if (relativeAngle >= 202.5 && relativeAngle < 247.5) return ChatColor.BOLD + "↙";
            if (relativeAngle >= 247.5 && relativeAngle < 292.5) return ChatColor.BOLD + "←";
            if (relativeAngle >= 292.5 && relativeAngle < 337.5) return ChatColor.BOLD + "↖";

            return "?";
        }

        public static boolean isInTeleportArea(Location location, Location center, int radius) {
            if (location == null || center == null) return false;
            return Math.abs(location.getBlockX() - center.getBlockX()) <= radius
                    && Math.abs(location.getBlockY() - center.getBlockY()) <= 1
                    && Math.abs(location.getBlockZ() - center.getBlockZ()) <= radius;
        }

        public static boolean isOutsideBorder(Location location) {
            if (location == null || location.getWorld() == null) return false;
            WorldBorder border = location.getWorld().getWorldBorder();
            double half = border.getSize() / 2.0;
            Location center = border.getCenter();
            return Math.abs(location.getX() - center.getX()) > half
                    || Math.abs(location.getZ() - center.getZ()) > half;
        }
    }

    public static final class LocationRegistry {

        private static final List<Entry> ENTRIES = new ArrayList<>();

        public static void register(String name, Location location) {
            register(name, location, "General");
        }

        public static void register(String name, Location location, String category) {
            if (name == null || location == null) return;

            ENTRIES.removeIf(e -> e.name.equals(name));
            ENTRIES.add(new Entry(name, location, category));
        }

        public static void remove(String name) {
            ENTRIES.removeIf(e -> e.name.equals(name));
        }

        public static List<Entry> getEntries() {
            return new ArrayList<>(ENTRIES);
        }

        public static List<Entry> getEntriesByCategory(String category) {
            List<Entry> result = new ArrayList<>();
            for (Entry e : ENTRIES) {
                if (e.category.equalsIgnoreCase(category)) result.add(e);
            }
            return result;
        }

        public static void clear() {
            ENTRIES.clear();
        }

        public record Entry(String name, Location location, String category) {
        }

    }

    public static final class ServerMonitor {


        public static double serverTps() {
            try {
                return MinecraftServer.getServer().recentTps[0];
            } catch (Throwable t) {
                return 20.0;
            }
        }

        public static int displayedPlayerCount(int live) {
            if (UHCManager.get() == null || !UHCManager.get().isGame()) return live;
            return live + TabFreezeService.frozenCount();
        }

        public static boolean isTabFige() {
            return ScenarioManager.get().getActiveSpecialScenarios().stream()
                    .anyMatch(s -> {
                        try { return (boolean) s.getClass().getMethod("isTabFige").invoke(s); }
                        catch (Throwable t) { return false; }
                    });
        }

        public static void sendTab(Player p, String header, String footer) {
            com.github.retrooper.packetevents.PacketEvents.getAPI().getPlayerManager().sendPacket(p,
                    new com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter(
                            com.github.retrooper.packetevents.util.adventure.AdventureSerializer.fromLegacyFormat(header == null ? "" : header),
                            com.github.retrooper.packetevents.util.adventure.AdventureSerializer.fromLegacyFormat(footer == null ? "" : footer)));
        }
    }
}
