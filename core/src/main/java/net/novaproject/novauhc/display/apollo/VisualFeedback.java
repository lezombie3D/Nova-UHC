package net.novaproject.novauhc.display.apollo;

import net.novaproject.novauhc.debug.DebugLog;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import net.novaproject.novauhc.Main;
import org.bson.Document;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.awt.Color;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;


public final class VisualFeedback {

    public record TeammateMarker(UUID uuid, String world, double x, double y, double z, Color color) {
    }

    private static boolean apolloPresent = false;
    private static boolean enabled = true;

    public static void init() {
        apolloPresent = Bukkit.getPluginManager().getPlugin("Apollo-Bukkit") != null
                || Bukkit.getPluginManager().getPlugin("Apollo") != null;
        ApolloConfig.load();
        if (apolloPresent) {
            Bukkit.getLogger().info("[NovaUHC] Apollo détecté — feedback visuel Lunar activé.");
            ApolloConfig.applyAllModSettings();
        }
    }

    public static void setEnabled(boolean e) {
        enabled = e;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isActive() {
        return enabled && apolloPresent;
    }

    public static boolean isApolloPresent() {
        return apolloPresent;
    }

    public static boolean isActive(ApolloConfig.Capability cap) {
        return isActive() && ApolloConfig.isCapabilityEnabled(cap);
    }

    public static void cooldown(Player player, String id, int seconds, Material icon) {
        if (!isActive(ApolloConfig.Capability.COOLDOWN) || player == null || seconds <= 0) return;
        try {
            ApolloAdapter.cooldown(player, id, seconds, icon);
        } catch (Throwable error) {
            DebugLog.warnOnce("Apollo", "echec silencieux", error);
        }
    }

    public static void glow(Player viewer, Player target, Color color) {
        if (!isActive(ApolloConfig.Capability.GLOW) || viewer == null || target == null) return;
        try {
            ApolloAdapter.glow(viewer, target, color);
        } catch (Throwable error) {
            DebugLog.warnOnce("Apollo", "echec silencieux", error);
        }
    }

    public static void resetGlow(Player viewer, Player target) {
        if (!isActive() || viewer == null || target == null) return;
        try {
            ApolloAdapter.resetGlow(viewer, target);
        } catch (Throwable error) {
            DebugLog.warnOnce("Apollo", "echec silencieux", error);
        }
    }

    public static void staffMod(Player viewer, boolean enabled) {
        if (!isActive(ApolloConfig.Capability.STAFF_MOD) || viewer == null) return;
        try {
            if (enabled) ApolloAdapter.enableStaffMod(viewer);
            else ApolloAdapter.disableStaffMod(viewer);
        } catch (Throwable error) {
            DebugLog.warnOnce("Apollo", "echec silencieux", error);
        }
    }

    public static void fire(Player burning, Color color) {
        if (!isActive(ApolloConfig.Capability.FIRE) || burning == null || color == null) return;
        try {
            ApolloAdapter.fire(burning, color);
        } catch (Throwable error) {
            DebugLog.warnOnce("Apollo", "echec silencieux", error);
        }
    }

    public static void resetFire(Player burning) {
        if (!isActive() || burning == null) return;
        try {
            ApolloAdapter.resetFire(burning);
        } catch (Throwable error) {
            DebugLog.warnOnce("Apollo", "echec silencieux", error);
        }
    }

    public static void notification(Player viewer, String title, String description, int displaySeconds) {
        if (!isActive(ApolloConfig.Capability.NOTIFICATION) || viewer == null || displaySeconds <= 0) return;
        try {
            ApolloAdapter.notification(viewer, title, description, displaySeconds);
        } catch (Throwable error) {
            DebugLog.warnOnce("Apollo", "echec silencieux", error);
        }
    }

    public static void waypoint(Player viewer, String name, Location loc, Color color, boolean beam) {
        if (!isActive(ApolloConfig.Capability.WAYPOINT) || viewer == null || name == null
                || loc == null || loc.getWorld() == null) return;
        try {
            ApolloAdapter.waypoint(viewer, name, loc, color != null ? color : Color.YELLOW, beam);
        } catch (Throwable error) {
            DebugLog.warnOnce("Apollo", "echec silencieux", error);
        }
    }

    public static void removeWaypoint(Player viewer, String name) {
        if (!isActive() || viewer == null || name == null) return;
        try {
            ApolloAdapter.removeWaypoint(viewer, name);
        } catch (Throwable error) {
            DebugLog.warnOnce("Apollo", "echec silencieux", error);
        }
    }

    public static void vignette(Player viewer, String resourceLocation, float opacity) {
        if (!isActive(ApolloConfig.Capability.VIGNETTE) || viewer == null || resourceLocation == null) return;
        try {
            ApolloAdapter.vignette(viewer, resourceLocation, opacity);
        } catch (Throwable error) {
            DebugLog.warnOnce("Apollo", "echec silencieux", error);
        }
    }

    public static void resetVignette(Player viewer) {
        if (!isActive() || viewer == null) return;
        try {
            ApolloAdapter.resetVignette(viewer);
        } catch (Throwable error) {
            DebugLog.warnOnce("Apollo", "echec silencieux", error);
        }
    }

    static void applyModSetting(String modId, boolean allowed) {
        if (!apolloPresent) return;
        try {
            ApolloAdapter.setModAllowed(modId, allowed);
        } catch (Throwable error) {
            DebugLog.warnOnce("Apollo", "echec silencieux", error);
        }
    }

    public static boolean isLunar(Player viewer) {
        if (!isActive() || viewer == null) return false;
        try {
            return ApolloAdapter.isRegistered(viewer);
        } catch (Throwable error) {
            DebugLog.warnOnce("Apollo", "echec silencieux", error);
            return false;
        }
    }

    public static void teammates(Player viewer, List<TeammateMarker> mates) {
        if (!isActive(ApolloConfig.Capability.TEAM_MARKERS) || viewer == null || mates == null) return;
        try {
            ApolloAdapter.updateTeammates(viewer, mates);
        } catch (Throwable error) {
            DebugLog.warnOnce("Apollo", "echec silencieux", error);
        }
    }

    public static void resetTeammates(Player viewer) {
        if (!isActive() || viewer == null) return;
        try {
            ApolloAdapter.resetTeammates(viewer);
        } catch (Throwable error) {
            DebugLog.warnOnce("Apollo", "echec silencieux", error);
        }
    }

    public static final class ApolloConfig {

        public enum Capability {
            COOLDOWN,
            FIRE,
            GLOW,
            TEAM_MARKERS,
            NOTIFICATION,
            WAYPOINT,
            VIGNETTE,
            STAFF_MOD
        }

        public static final List<String> MOD_IDS = List.of(
                "minimap", "waypoints", "coordinates", "directionhud", "freelook", "snaplook",
                "zoom", "chunkborders", "f3display", "teamview", "replaymod", "rewind",
                "hitbox", "reachdisplay", "pvpinfo", "uhcoverlay");

        private static final Map<Capability, Boolean> CAPABILITIES = new LinkedHashMap<>();
        private static final Map<String, Boolean> MODS = new LinkedHashMap<>();
        private static File file;


        public static void load() {
            file = new File(Main.get().getDataFolder(), "apollo.yml");
            YamlConfiguration yaml = file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();

            for (Capability cap : Capability.values()) {
                CAPABILITIES.put(cap, yaml.getBoolean("capabilities." + cap.name().toLowerCase(Locale.ROOT), true));
            }
            for (String mod : MOD_IDS) {
                MODS.put(mod, yaml.getBoolean("mods." + mod, true));
            }
            save();
        }

        public static boolean isCapabilityEnabled(Capability cap) {
            return CAPABILITIES.getOrDefault(cap, true);
        }

        public static void setCapabilityEnabled(Capability cap, boolean enabled) {
            CAPABILITIES.put(cap, enabled);
            save();
        }

        public static boolean isModAllowed(String modId) {
            return MODS.getOrDefault(modId, true);
        }

        public static void setModAllowed(String modId, boolean allowed) {
            if (!MODS.containsKey(modId)) return;
            MODS.put(modId, allowed);
            save();
            VisualFeedback.applyModSetting(modId, allowed);
        }

        public static void applyAllModSettings() {
            MODS.forEach(VisualFeedback::applyModSetting);
        }

        public static Document snapshot() {
            Document doc = new Document();
            doc.put("enabled", VisualFeedback.isEnabled());
            Document caps = new Document();
            CAPABILITIES.forEach((cap, on) -> caps.put(cap.name(), on));
            doc.put("capabilities", caps);
            Document mods = new Document();
            MODS.forEach(mods::put);
            doc.put("mods", mods);
            return doc;
        }

        public static void restore(Document doc) {
            if (doc == null) return;
            if (doc.get("enabled") instanceof Boolean e) VisualFeedback.setEnabled(e);
            if (doc.get("capabilities") instanceof Document caps) {
                for (Capability cap : Capability.values()) {
                    if (caps.get(cap.name()) instanceof Boolean b) CAPABILITIES.put(cap, b);
                }
            }
            if (doc.get("mods") instanceof Document mods) {
                for (String mod : MOD_IDS) {
                    if (mods.get(mod) instanceof Boolean b) MODS.put(mod, b);
                }
            }
            save();
            applyAllModSettings();
        }

        private static void save() {
            if (file == null) return;
            YamlConfiguration yaml = new YamlConfiguration();
            CAPABILITIES.forEach((cap, on) ->
                    yaml.set("capabilities." + cap.name().toLowerCase(Locale.ROOT), on));
            MODS.forEach((mod, on) -> yaml.set("mods." + mod, on));
            try {
                yaml.save(file);
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.SEVERE,
                        "[ApolloConfig] Impossible de sauvegarder apollo.yml", e);
            }
        }
    }
}
