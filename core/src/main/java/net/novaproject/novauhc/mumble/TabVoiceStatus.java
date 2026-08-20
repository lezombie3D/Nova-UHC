package net.novaproject.novauhc.mumble;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import net.kyori.adventure.text.Component;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.display.PlayerColorManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TabVoiceStatus extends PacketListenerAbstract {

    private static final String DOT_LINKED = "§a✔";
    private static final String DOT_UNLINKED = "§c✖";
    private static final String DOT_ABSENT = "§8○";

    private static final Map<UUID, Map<String, String>> TAB_NAME = new ConcurrentHashMap<>();

    private record Subject(Player player, String key, String prefix, String suffix, String dot) {
    }

    private TabVoiceStatus() {
        super(PacketListenerPriority.MONITOR);
    }

    public static void start() {
        PacketEvents.getAPI().getEventManager().registerListener(new TabVoiceStatus());
        new BukkitRunnable() {
            @Override
            public void run() {
                refresh();
            }
        }.runTaskTimer(Main.get(), 20L, 20L);
    }

    private static void refresh() {
        MumbleManager mm = MumbleManager.get();
        boolean voice = mm.isEnabled() && mm.hasSession();
        if (!voice && !PlayerColorManager.hasAnyRoleLabel()) {
            releaseAll();
            return;
        }

        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        List<Subject> subjects = new ArrayList<>(online.size());
        Set<String> keys = new HashSet<>();
        Set<UUID> viewers = new HashSet<>();

        for (Player player : online) {
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(player);
            String prefix = team == null ? "" : team.getPrefix();
            String suffix = team == null ? "" : team.getSuffix();
            String dot = voice ? dot(mm, player.getName()) : "";
            String key = player.getName().toLowerCase();
            keys.add(key);
            viewers.add(player.getUniqueId());
            subjects.add(new Subject(player, key, prefix, suffix, dot));
        }

        for (Player viewer : online) {
            UUID viewerUuid = viewer.getUniqueId();
            Map<String, String> pushed = TAB_NAME.computeIfAbsent(viewerUuid, k -> new ConcurrentHashMap<>());
            List<WrapperPlayServerPlayerInfo.PlayerData> dirty = new ArrayList<>();

            for (Subject subject : subjects) {
                String wanted = PlayerColorManager.prefixFor(viewer, subject.player(), subject.prefix())
                        + subject.player().getName()
                        + PlayerColorManager.tabSuffixFor(viewer, subject.player(), subject.suffix())
                        + (subject.dot().isEmpty() ? "" : " " + subject.dot());
                if (wanted.equals(pushed.put(subject.key(), wanted))) continue;
                dirty.add(entry(subject.player(), wanted));
            }

            pushed.keySet().retainAll(keys);
            if (!dirty.isEmpty()) push(viewer, dirty);
        }

        TAB_NAME.keySet().retainAll(viewers);
    }

    private static void releaseAll() {
        if (TAB_NAME.isEmpty()) return;
        Set<UUID> stale = new HashSet<>(TAB_NAME.keySet());
        TAB_NAME.clear();

        List<WrapperPlayServerPlayerInfo.PlayerData> release = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) release.add(entry(player, null));
        if (release.isEmpty()) return;

        for (UUID viewerUuid : stale) {
            Player viewer = Bukkit.getPlayer(viewerUuid);
            if (viewer != null && viewer.isOnline()) push(viewer, release);
        }
    }


    private static String dot(MumbleManager mm, String playerName) {
        if (!mm.isConnected(playerName)) return DOT_ABSENT;
        return mm.isLinked(playerName) ? DOT_LINKED : DOT_UNLINKED;
    }

    private static WrapperPlayServerPlayerInfo.PlayerData entry(Player subject, String display) {
        return new WrapperPlayServerPlayerInfo.PlayerData(
                display == null ? null : AdventureSerializer.fromLegacyFormat(display),
                new UserProfile(subject.getUniqueId(), subject.getName()),
                GameMode.valueOf(subject.getGameMode().name()),
                0);
    }

    private static void push(Player viewer, List<WrapperPlayServerPlayerInfo.PlayerData> entries) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer,
                new WrapperPlayServerPlayerInfo(
                        WrapperPlayServerPlayerInfo.Action.UPDATE_DISPLAY_NAME, entries));
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.PLAYER_INFO) return;
        if (TAB_NAME.isEmpty()) return;

        User user = event.getUser();
        if (user == null || user.getUUID() == null) return;
        Map<String, String> pushed = TAB_NAME.get(user.getUUID());
        if (pushed == null || pushed.isEmpty()) return;

        WrapperPlayServerPlayerInfo wrapper = new WrapperPlayServerPlayerInfo(event);
        WrapperPlayServerPlayerInfo.Action action = wrapper.getAction();
        if (action != WrapperPlayServerPlayerInfo.Action.ADD_PLAYER
                && action != WrapperPlayServerPlayerInfo.Action.UPDATE_DISPLAY_NAME) {
            return;
        }

        boolean changed = false;
        for (WrapperPlayServerPlayerInfo.PlayerData data : wrapper.getPlayerDataList()) {
            UserProfile profile = data.getUser();
            if (profile == null || profile.getName() == null) continue;
            String wanted = pushed.get(profile.getName().toLowerCase());
            if (wanted == null) continue;
            Component current = data.getDisplayName();
            if (current != null && wanted.equals(AdventureSerializer.toLegacyFormat(current))) continue;
            data.setDisplayName(AdventureSerializer.fromLegacyFormat(wanted));
            changed = true;
        }
        if (changed) event.markForReEncode(true);
    }
}
