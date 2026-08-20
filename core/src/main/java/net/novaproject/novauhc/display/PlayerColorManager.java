package net.novaproject.novauhc.display;

import java.util.Collections;

import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_8_R3.ScoreboardTeam;
import net.minecraft.server.v1_8_R3.ScoreboardTeamBase;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.scoreboard.CraftScoreboard;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class PlayerColorManager {

    public static final String PASTILLE_GLYPH = "●";

    private static final int MAX_PREFIX_LENGTH = 16;

    private static final Map<UUID, Set<String>> createdTeams = new HashMap<>();

    private static final Map<UUID, Set<UUID>> hiddenNameTags = new HashMap<>();

    private static final Map<UUID, Map<UUID, ChatColor>> glowColors = new HashMap<>();

    private static final Map<UUID, Map<UUID, ChatColor>> perceptionColors = new HashMap<>();

    private static final Map<UUID, Map<UUID, ChatColor>> pastilleColors = new HashMap<>();

    private static final Map<UUID, Map<UUID, String>> roleLabels = new HashMap<>();

    private static final String ROLE_HOLOGRAM_KEY = "colorRole";

    private static final int ROLE_TAG_OVERHEAD = 1;

    private static final int MAX_ROLE_LABEL_LENGTH = 32;

    private static final DisplaySpi.IPlayerColorManager INSTANCE = new DisplaySpi.IPlayerColorManager() {
        @Override public void applyColor(Player viewer, Player target, ChatColor color) { PlayerColorManager.applyColor(viewer, target, color); }
        @Override public void reapplyColorsForViewer(Player viewer) { PlayerColorManager.reapplyColorsForViewer(viewer); }
        @Override public void reapplyColorsForTarget(Player target) { PlayerColorManager.reapplyColorsForTarget(target); }
        @Override public void removeTeamForTarget(Player target) { PlayerColorManager.removeTeamForTarget(target); }
        @Override public void removeColorForViewer(Player viewer, Player target) { PlayerColorManager.removeColorForViewer(viewer, target); }
        @Override public void cleanupViewer(UUID viewerUUID) { PlayerColorManager.cleanupViewer(viewerUUID); }
    };

    public static DisplaySpi.IPlayerColorManager instance() { return INSTANCE; }

    private static String teamName(Player target) {
        String name = target.getName();
        return "clr_" + (name.length() > 11 ? name.substring(0, 11) : name);
    }

    public static ChatColor getNameColor(Player viewer, Player target) {
        ChatColor glow = lookup(glowColors, viewer, target);
        if (glow != null) return glow;
        UHCPlayer targetUhc = UHCPlayerManager.get().getPlayer(target);
        ChatColor manual = targetUhc == null ? null : targetUhc.getTabColorPrefs().get(viewer.getUniqueId());
        if (manual != null && manual != ChatColor.RESET) return manual;
        return lookup(perceptionColors, viewer, target);
    }

    public static ChatColor getPastilleColor(Player viewer, Player target) {
        return lookup(pastilleColors, viewer, target);
    }

    public static boolean hasAnyRoleLabel() {
        for (Map<UUID, String> byTarget : roleLabels.values()) {
            if (!byTarget.isEmpty()) return true;
        }
        return false;
    }

    public static String getRoleLabel(Player viewer, Player target) {
        return roleLabels.getOrDefault(viewer.getUniqueId(), Collections.emptyMap()).get(target.getUniqueId());
    }

    public static void applyRoleLabel(Player viewer, Player target, String label) {
        if (!viewer.isOnline() || !target.isOnline()) return;
        Map<UUID, String> byTarget = roleLabels.computeIfAbsent(viewer.getUniqueId(), id -> new HashMap<>());
        if (label == null || label.isEmpty()) byTarget.remove(target.getUniqueId());
        else byTarget.put(target.getUniqueId(), trimLabel(label));
        refresh(viewer, target);
    }

    private static String trimLabel(String label) {
        return cut(label, MAX_ROLE_LABEL_LENGTH);
    }

    private static String cut(String text, int max) {
        if (text.length() <= max) return text;
        String cut = text.substring(0, max);
        return cut.endsWith(String.valueOf(ChatColor.COLOR_CHAR)) ? cut.substring(0, cut.length() - 1) : cut;
    }

    private static ChatColor lookup(Map<UUID, Map<UUID, ChatColor>> store, Player viewer, Player target) {
        return store.getOrDefault(viewer.getUniqueId(), Collections.emptyMap()).get(target.getUniqueId());
    }

    public static String prefixFor(Player viewer, Player target) {
        Team realTeam = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(target);
        return prefixFor(viewer, target, realTeam == null ? null : realTeam.getPrefix());
    }

    public static String prefixFor(Player viewer, Player target, String teamPrefix) {
        String base = teamPrefix == null ? "" : teamPrefix;
        return fit(base, tailFor(base, getPastilleColor(viewer, target), getNameColor(viewer, target)));
    }

    public static String tabSuffixFor(Player viewer, Player target, String teamSuffix) {
        return suffixWithLabel(teamSuffix, getRoleLabel(viewer, target), Integer.MAX_VALUE);
    }

    static String suffixWithLabel(String teamSuffix, String label, int budget) {
        String base = teamSuffix == null ? "" : teamSuffix;
        if (label == null || label.isEmpty()) return base;
        String colored = colored(label);
        int room = budget - base.length() - ROLE_TAG_OVERHEAD;
        if (room <= 0) return base;
        return base + " " + cut(colored, room);
    }

    private static String colored(String label) {
        return label.indexOf(ChatColor.COLOR_CHAR) == 0 ? label : ChatColor.GRAY + label;
    }

    static String tailFor(String base, ChatColor pastille, ChatColor name) {
        String nameCode = name != null ? name.toString() : ChatColor.getLastColors(base);
        if (pastille == null) return name == null ? "" : nameCode;
        return pastille + PASTILLE_GLYPH + " " + nameCode;
    }

    static String fit(String base, String tail) {
        if (tail.length() >= MAX_PREFIX_LENGTH) {
            return tail.length() > MAX_PREFIX_LENGTH ? tail.substring(0, MAX_PREFIX_LENGTH) : tail;
        }
        int room = MAX_PREFIX_LENGTH - tail.length();
        if (base.length() <= room) return base + tail;
        String cut = base.substring(0, room);
        if (cut.endsWith("§")) cut = cut.substring(0, cut.length() - 1);
        return cut + tail;
    }

    private static boolean hasOverride(Player viewer, Player target) {
        return getNameColor(viewer, target) != null
                || getPastilleColor(viewer, target) != null
                || getRoleLabel(viewer, target) != null
                || isNameTagHidden(viewer, target);
    }

    public static void refresh(Player viewer, Player target) {
        if (viewer == null || target == null || !viewer.isOnline() || !target.isOnline()) return;
        if (!viewer.equals(target)) syncRoleHologram(viewer, target);
        if (hasOverride(viewer, target)) sendColorPacket(viewer, target);
        else dropFakeTeam(viewer, target);
    }

    public static boolean isTagVisible(Player viewer, Player target) {
        return target.isOnline() && !target.equals(viewer)
                && viewer.canSee(target)
                && !target.isSneaking()
                && !target.hasPotionEffect(PotionEffectType.INVISIBILITY)
                && target.getWorld().equals(viewer.getWorld());
    }

    private static void syncRoleHologram(Player viewer, Player target) {
        String label = getRoleLabel(viewer, target);
        if (label == null || label.isEmpty()) {
            PerViewerHologramManager.get().hide(viewer, target, ROLE_HOLOGRAM_KEY);
            return;
        }
        PerViewerHologramManager.get().show(viewer, target, ROLE_HOLOGRAM_KEY, watched -> {
            if (!isTagVisible(viewer, watched)) return null;
            String current = getRoleLabel(viewer, watched);
            return current == null || current.isEmpty() ? null : colored(current);
        });
    }

    public static void applyColor(Player viewer, Player target, ChatColor color) {
        if (!viewer.isOnline() || !target.isOnline()) return;

        UHCPlayer targetUhc = UHCPlayerManager.get().getPlayer(target);
        if (targetUhc != null) {
            targetUhc.getTabColorPrefs().put(viewer.getUniqueId(), color);
        }

        refresh(viewer, target);
    }

    public static void applyGlowColor(Player viewer, Player target, ChatColor color) {
        put(glowColors, viewer, target, color);
    }

    public static void applyPerceptionColor(Player viewer, Player target, ChatColor color) {
        put(perceptionColors, viewer, target, color);
    }

    public static void applyPastille(Player viewer, Player target, ChatColor color) {
        put(pastilleColors, viewer, target, color);
    }

    private static void put(Map<UUID, Map<UUID, ChatColor>> store, Player viewer, Player target, ChatColor color) {
        if (viewer == null || target == null || !viewer.isOnline() || !target.isOnline()) return;
        if (color == null || color == ChatColor.RESET) {
            Map<UUID, ChatColor> byTarget = store.get(viewer.getUniqueId());
            if (byTarget != null) byTarget.remove(target.getUniqueId());
        } else {
            store.computeIfAbsent(viewer.getUniqueId(), k -> new HashMap<>())
                    .put(target.getUniqueId(), color);
        }
        refresh(viewer, target);
    }

    private static void sendColorPacket(Player viewer, Player target) {
        String name = teamName(target);
        UUID viewerUUID = viewer.getUniqueId();

        net.minecraft.server.v1_8_R3.Scoreboard fakeScoreboard = new net.minecraft.server.v1_8_R3.Scoreboard();
        ScoreboardTeam nmsTeam = new ScoreboardTeam(fakeScoreboard, name);
        nmsTeam.setPrefix(prefixFor(viewer, target));
        Team realTeam = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(target);
        String realSuffix = realTeam == null ? null : realTeam.getSuffix();
        nmsTeam.setSuffix(realSuffix == null || realSuffix.isEmpty() ? "§r" : realSuffix);
        if (target.hasPotionEffect(PotionEffectType.INVISIBILITY)
                || isNameTagHidden(viewer, target)) {
            nmsTeam.setNameTagVisibility(ScoreboardTeamBase.EnumNameTagVisibility.NEVER);
        }

        Set<String> created = createdTeams.computeIfAbsent(viewerUUID, k -> new HashSet<>());

        if (!created.contains(name)) {
            nmsTeam.getPlayerNameSet().add(target.getName());
            created.add(name);
            sendTo(viewer, new PacketPlayOutScoreboardTeam(nmsTeam, 0));
            return;
        }

        sendTo(viewer, new PacketPlayOutScoreboardTeam(nmsTeam, 2));
        sendTo(viewer, new PacketPlayOutScoreboardTeam(
                nmsTeam, Collections.singletonList(target.getName()), 3));
    }

    private static void sendTo(Player viewer, PacketPlayOutScoreboardTeam packet) {
        ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(packet);
    }

    public static void reapplyColorsForViewer(Player viewer) {
        for (Player target : Bukkit.getOnlinePlayers()) refresh(viewer, target);
    }

    public static void reapplyColorsForTarget(Player target) {
        for (Player viewer : Bukkit.getOnlinePlayers()) refresh(viewer, target);
    }

    public static void removeTeamForTarget(Player target) {
        String name = teamName(target);
        net.minecraft.server.v1_8_R3.Scoreboard fakeScoreboard = new net.minecraft.server.v1_8_R3.Scoreboard();
        ScoreboardTeam nmsTeam = new ScoreboardTeam(fakeScoreboard, name);
        PacketPlayOutScoreboardTeam removePacket = new PacketPlayOutScoreboardTeam(nmsTeam, 1);

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            Set<String> created = createdTeams.get(viewer.getUniqueId());
            if (created != null && created.contains(name)) {
                ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(removePacket);
                created.remove(name);
            }
        }
    }

    public static void removeColorForViewer(Player viewer, Player target) {
        if (!viewer.isOnline()) return;

        UHCPlayer targetUhc = UHCPlayerManager.get().getPlayer(target);
        if (targetUhc != null) {
            targetUhc.getTabColorPrefs().remove(viewer.getUniqueId());
        }

        Map<UUID, String> labels = roleLabels.get(viewer.getUniqueId());
        if (labels != null) labels.remove(target.getUniqueId());

        refresh(viewer, target);
    }

    private static void dropFakeTeam(Player viewer, Player target) {
        String fakeName = teamName(target);
        Set<String> created = createdTeams.get(viewer.getUniqueId());
        if (created == null || !created.contains(fakeName)) return;

        net.minecraft.server.v1_8_R3.Scoreboard fakeScoreboard = new net.minecraft.server.v1_8_R3.Scoreboard();
        ScoreboardTeam fakeNmsTeam = new ScoreboardTeam(fakeScoreboard, fakeName);
        ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(
                new PacketPlayOutScoreboardTeam(fakeNmsTeam, 1));
        created.remove(fakeName);

        restoreOriginalTeam(viewer, target);
    }

    private static void restoreOriginalTeam(Player viewer, Player target) {
        Scoreboard scoreboard = viewer.getScoreboard();
        Team origTeam = scoreboard == null ? null : scoreboard.getPlayerTeam(target);

        if (origTeam == null && TeamsTagsManager.scoreboard != null) {
            scoreboard = TeamsTagsManager.scoreboard;
            origTeam = scoreboard.getPlayerTeam(target);
        }
        if (origTeam == null) {
            scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            origTeam = scoreboard.getPlayerTeam(target);
        }
        if (origTeam == null) return;

        net.minecraft.server.v1_8_R3.Scoreboard nmsScoreboard = ((CraftScoreboard) scoreboard).getHandle();
        ScoreboardTeam nmsOrigTeam = nmsScoreboard.getTeam(origTeam.getName());
        if (nmsOrigTeam == null) return;

        PacketPlayOutScoreboardTeam addPacket = new PacketPlayOutScoreboardTeam(
                nmsOrigTeam, Collections.singletonList(target.getName()), 3);
        ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(addPacket);
    }

    public static boolean isNameTagHidden(Player viewer, Player target) {
        Set<UUID> hidden = hiddenNameTags.get(viewer.getUniqueId());
        return hidden != null && hidden.contains(target.getUniqueId());
    }

    public static void setNameTagHidden(Player viewer, Player target, boolean hidden) {
        if (!viewer.isOnline() || !target.isOnline()) return;

        Set<UUID> hiddenFor = hiddenNameTags.computeIfAbsent(viewer.getUniqueId(), k -> new HashSet<>());
        if (hidden) hiddenFor.add(target.getUniqueId());
        else hiddenFor.remove(target.getUniqueId());

        refresh(viewer, target);
    }

    public static void clearHiddenNameTags(Player viewer) {
        Set<UUID> hidden = hiddenNameTags.get(viewer.getUniqueId());
        if (hidden == null) return;
        for (UUID targetUuid : new HashSet<>(hidden)) {
            Player target = Bukkit.getPlayer(targetUuid);
            if (target != null && target.isOnline()) setNameTagHidden(viewer, target, false);
            else hidden.remove(targetUuid);
        }
        hiddenNameTags.remove(viewer.getUniqueId());
    }

    public static void cleanupViewer(UUID viewerUUID) {
        createdTeams.remove(viewerUUID);
        hiddenNameTags.remove(viewerUUID);
        glowColors.remove(viewerUUID);
        perceptionColors.remove(viewerUUID);
        pastilleColors.remove(viewerUUID);
        roleLabels.remove(viewerUUID);
    }
}