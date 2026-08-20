package net.novaproject.novauhc.display.apollo;

import java.util.Map;
import java.util.LinkedHashMap;
import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.common.icon.ItemStackIcon;
import com.lunarclient.apollo.common.location.ApolloBlockLocation;
import com.lunarclient.apollo.common.location.ApolloLocation;
import com.lunarclient.apollo.module.coloredfire.ColoredFireModule;
import com.lunarclient.apollo.module.cooldown.Cooldown;
import com.lunarclient.apollo.module.cooldown.CooldownModule;
import com.lunarclient.apollo.module.glow.GlowModule;
import com.lunarclient.apollo.module.modsetting.ModSettingModule;
import com.lunarclient.apollo.module.notification.Notification;
import com.lunarclient.apollo.module.notification.NotificationModule;
import com.lunarclient.apollo.module.staffmod.StaffModModule;
import com.lunarclient.apollo.module.team.TeamMember;
import com.lunarclient.apollo.module.team.TeamModule;
import com.lunarclient.apollo.module.vignette.Vignette;
import com.lunarclient.apollo.module.vignette.VignetteModule;
import com.lunarclient.apollo.module.waypoint.Waypoint;
import com.lunarclient.apollo.module.waypoint.WaypointModule;
import com.lunarclient.apollo.option.SimpleOption;
import com.lunarclient.apollo.recipients.Recipients;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.awt.Color;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


final class ApolloAdapter {

    static void fire(Player burning, Color color) {
        ColoredFireModule module = Apollo.getModuleManager().getModule(ColoredFireModule.class);
        module.overrideColoredFire(Recipients.ofEveryone(), burning.getUniqueId(), color);
    }

    static void resetFire(Player burning) {
        ColoredFireModule module = Apollo.getModuleManager().getModule(ColoredFireModule.class);
        module.resetColoredFire(Recipients.ofEveryone(), burning.getUniqueId());
    }

    static void notification(Player viewer, String title, String description, int displaySeconds) {
        Apollo.getPlayerManager().getPlayer(viewer.getUniqueId()).ifPresent(apolloPlayer -> {
            NotificationModule module = Apollo.getModuleManager().getModule(NotificationModule.class);
            module.displayNotification(apolloPlayer, Notification.builder()
                    .title(title)
                    .description(description)
                    .displayTime(Duration.ofSeconds(displaySeconds))
                    .build());
        });
    }

    static void waypoint(Player viewer, String name, Location loc, Color color, boolean beam) {
        Apollo.getPlayerManager().getPlayer(viewer.getUniqueId()).ifPresent(apolloPlayer -> {
            WaypointModule module = Apollo.getModuleManager().getModule(WaypointModule.class);
            module.displayWaypoint(apolloPlayer, Waypoint.builder()
                    .name(name)
                    .location(ApolloBlockLocation.builder()
                            .world(loc.getWorld().getName())
                            .x(loc.getBlockX())
                            .y(loc.getBlockY())
                            .z(loc.getBlockZ())
                            .build())
                    .color(color)
                    .preventRemoval(true)
                    .showBeam(beam)
                    .build());
        });
    }

    static void removeWaypoint(Player viewer, String name) {
        Apollo.getPlayerManager().getPlayer(viewer.getUniqueId()).ifPresent(apolloPlayer -> {
            WaypointModule module = Apollo.getModuleManager().getModule(WaypointModule.class);
            module.removeWaypoint(apolloPlayer, name);
        });
    }

    static void vignette(Player viewer, String resourceLocation, float opacity) {
        Apollo.getPlayerManager().getPlayer(viewer.getUniqueId()).ifPresent(apolloPlayer -> {
            VignetteModule module = Apollo.getModuleManager().getModule(VignetteModule.class);
            module.displayVignette(apolloPlayer, Vignette.builder()
                    .resourceLocation(resourceLocation)
                    .opacity(opacity)
                    .build());
        });
    }

    static void resetVignette(Player viewer) {
        Apollo.getPlayerManager().getPlayer(viewer.getUniqueId()).ifPresent(apolloPlayer -> {
            VignetteModule module = Apollo.getModuleManager().getModule(VignetteModule.class);
            module.resetVignette(apolloPlayer);
        });
    }

    private static final Map<String, SimpleOption<Boolean>> MOD_OPTIONS = new LinkedHashMap<>();

    static {
        MOD_OPTIONS.put("minimap", com.lunarclient.apollo.mods.impl.ModMinimap.ENABLED);
        MOD_OPTIONS.put("waypoints", com.lunarclient.apollo.mods.impl.ModWaypoints.ENABLED);
        MOD_OPTIONS.put("coordinates", com.lunarclient.apollo.mods.impl.ModCoordinates.ENABLED);
        MOD_OPTIONS.put("directionhud", com.lunarclient.apollo.mods.impl.ModDirectionHud.ENABLED);
        MOD_OPTIONS.put("freelook", com.lunarclient.apollo.mods.impl.ModFreelook.ENABLED);
        MOD_OPTIONS.put("snaplook", com.lunarclient.apollo.mods.impl.ModSnaplook.ENABLED);
        MOD_OPTIONS.put("zoom", com.lunarclient.apollo.mods.impl.ModZoom.ENABLED);
        MOD_OPTIONS.put("chunkborders", com.lunarclient.apollo.mods.impl.ModChunkBorders.ENABLED);
        MOD_OPTIONS.put("f3display", com.lunarclient.apollo.mods.impl.ModF3Display.ENABLED);
        MOD_OPTIONS.put("teamview", com.lunarclient.apollo.mods.impl.ModTeamView.ENABLED);
        MOD_OPTIONS.put("replaymod", com.lunarclient.apollo.mods.impl.ModReplaymod.ENABLED);
        MOD_OPTIONS.put("rewind", com.lunarclient.apollo.mods.impl.ModRewind.ENABLED);
        MOD_OPTIONS.put("hitbox", com.lunarclient.apollo.mods.impl.ModHitbox.ENABLED);
        MOD_OPTIONS.put("reachdisplay", com.lunarclient.apollo.mods.impl.ModReachDisplay.ENABLED);
        MOD_OPTIONS.put("pvpinfo", com.lunarclient.apollo.mods.impl.ModPvpInfo.ENABLED);
        MOD_OPTIONS.put("uhcoverlay", com.lunarclient.apollo.mods.impl.ModUhcOverlay.ENABLED);
    }

    static void setModAllowed(String modId, boolean allowed) {
        SimpleOption<Boolean> option = MOD_OPTIONS.get(modId);
        if (option == null) return;
        ModSettingModule module = Apollo.getModuleManager().getModule(ModSettingModule.class);
        if (allowed) {
            module.getOptions().set(option, null);
        } else {
            module.getOptions().set(option, Boolean.FALSE);
        }
    }


    static void cooldown(Player player, String id, int seconds, Material icon) {
        Apollo.getPlayerManager().getPlayer(player.getUniqueId()).ifPresent(apolloPlayer -> {
            CooldownModule module = Apollo.getModuleManager().getModule(CooldownModule.class);
            module.displayCooldown(apolloPlayer, Cooldown.builder()
                    .name(id)
                    .duration(Duration.ofSeconds(seconds))
                    .icon(ItemStackIcon.builder()
                            .itemName(icon != null ? icon.name() : "NETHER_STAR")
                            .build())
                    .build());
        });
    }

    static void glow(Player viewer, Player target, Color color) {
        Apollo.getPlayerManager().getPlayer(viewer.getUniqueId()).ifPresent(apolloPlayer -> {
            GlowModule module = Apollo.getModuleManager().getModule(GlowModule.class);
            module.overrideGlow(apolloPlayer, target.getUniqueId(), color);
        });
    }

    static void resetGlow(Player viewer, Player target) {
        Apollo.getPlayerManager().getPlayer(viewer.getUniqueId()).ifPresent(apolloPlayer -> {
            GlowModule module = Apollo.getModuleManager().getModule(GlowModule.class);
            module.resetGlow(apolloPlayer, target.getUniqueId());
        });
    }

    static boolean isRegistered(Player viewer) {
        return Apollo.getPlayerManager().getPlayer(viewer.getUniqueId()).isPresent();
    }

    static void updateTeammates(Player viewer, List<VisualFeedback.TeammateMarker> mates) {
        Apollo.getPlayerManager().getPlayer(viewer.getUniqueId()).ifPresent(apolloPlayer -> {
            TeamModule module = Apollo.getModuleManager().getModule(TeamModule.class);
            List<TeamMember> members = new ArrayList<>();
            for (VisualFeedback.TeammateMarker m : mates) {
                members.add(TeamMember.builder()
                        .playerUuid(m.uuid())
                        .markerColor(m.color())
                        .location(ApolloLocation.builder()
                                .world(m.world())
                                .x(m.x())
                                .y(m.y())
                                .z(m.z())
                                .build())
                        .build());
            }
            module.updateTeamMembers(apolloPlayer, members);
        });
    }

    static void resetTeammates(Player viewer) {
        Apollo.getPlayerManager().getPlayer(viewer.getUniqueId()).ifPresent(apolloPlayer -> {
            TeamModule module = Apollo.getModuleManager().getModule(TeamModule.class);
            module.resetTeamMembers(apolloPlayer);
        });
    }

    static void enableStaffMod(Player viewer) {
        Apollo.getPlayerManager().getPlayer(viewer.getUniqueId()).ifPresent(apolloPlayer -> {
            StaffModModule module = Apollo.getModuleManager().getModule(StaffModModule.class);
            module.enableAllStaffMods(apolloPlayer);
        });
    }
    static void disableStaffMod(Player viewer) {
        Apollo.getPlayerManager().getPlayer(viewer.getUniqueId()).ifPresent(apolloPlayer -> {
            StaffModModule module = Apollo.getModuleManager().getModule(StaffModModule.class);
            module.disableAllStaffMods(apolloPlayer);
        });
    }
}