package net.novaproject.novauhc.display;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.novaproject.novauhc.lobby.RankManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.nms.DisguiseService;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TabFreezeService {

    private record Ghost(PacketPlayOutPlayerInfo add, PacketPlayOutPlayerInfo remove, Map<UUID, Boolean> viewers) {
    }

    private static final Map<UUID, Ghost> FROZEN = new LinkedHashMap<>();

    public static boolean isFrozen(UUID uuid) {
        return FROZEN.containsKey(uuid);
    }

    public static int frozenCount() {
        return FROZEN.size();
    }

    public static void freeze(UHCPlayer victim) {
        Player player = victim == null ? null : victim.getPlayer();
        if (player == null) return;
        UUID uuid = player.getUniqueId();
        if (FROZEN.containsKey(uuid)) return;
        if (DisguiseService.get().isDisguised(uuid)) DisguiseService.get().undisguise(player);
        EntityPlayer handle = ((CraftPlayer) player).getHandle();
        FROZEN.put(uuid, new Ghost(
                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, handle),
                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, handle),
                new HashMap<>()));
        broadcast(uuid);
    }

    public static void release(UUID uuid) {
        Ghost ghost = FROZEN.remove(uuid);
        if (ghost == null) return;
        Player subject = Bukkit.getPlayer(uuid);
        for (Map.Entry<UUID, Boolean> entry : ghost.viewers().entrySet()) {
            Player viewer = Bukkit.getPlayer(entry.getKey());
            if (viewer == null || !viewer.isOnline()) continue;
            if (subject != null && entry.getValue()) viewer.showPlayer(subject);
            else sendPacket(viewer, ghost.remove());
        }
        ghost.viewers().clear();
        if (subject == null) return;
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(uuid);
        if (uhcPlayer != null && !uhcPlayer.isPlaying()) RankManager.get().applyTag(subject);
    }

    public static void releaseAll() {
        for (UUID uuid : List.copyOf(FROZEN.keySet())) release(uuid);
    }

    public static void applyTo(Player viewer) {
        if (viewer == null || !viewer.isOnline() || FROZEN.isEmpty()) return;
        UHCPlayer uhcViewer = UHCPlayerManager.get().getPlayer(viewer);
        if (uhcViewer == null || !uhcViewer.isPlaying()) return;
        for (Map.Entry<UUID, Ghost> entry : FROZEN.entrySet()) {
            if (entry.getKey().equals(viewer.getUniqueId())) continue;
            sendGhost(viewer, entry.getKey(), entry.getValue());
        }
    }

    public static void onJoin(Player player) {
        if (player == null || !player.isOnline() || FROZEN.isEmpty()) return;
        applyTo(player);
        broadcast(player.getUniqueId());
    }

    private static void broadcast(UUID uuid) {
        Ghost ghost = FROZEN.get(uuid);
        if (ghost == null) return;
        for (UHCPlayer alive : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player viewer = alive.getPlayer();
            if (viewer == null || !viewer.isOnline() || viewer.getUniqueId().equals(uuid)) continue;
            sendGhost(viewer, uuid, ghost);
        }
    }

    private static void sendGhost(Player viewer, UUID uuid, Ghost ghost) {
        Player subject = Bukkit.getPlayer(uuid);
        boolean hid = subject != null && viewer.canSee(subject);
        if (hid) viewer.hidePlayer(subject);
        ghost.viewers().merge(viewer.getUniqueId(), hid, (previous, current) -> previous || current);
        sendPacket(viewer, ghost.add());
    }

    private static void sendPacket(Player viewer, PacketPlayOutPlayerInfo packet) {
        ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(packet);
    }
}
