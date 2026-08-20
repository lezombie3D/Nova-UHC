package net.novaproject.novauhc.utils.nms;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnLivingEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.novaproject.novauhc.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class MobDisguisePackets extends PacketListenerAbstract implements Listener {

    private static final MobDisguisePackets INSTANCE = new MobDisguisePackets();

    private record Mob(UUID uuid, EntityType type) {
    }

    private final Map<Integer, Mob> active = new ConcurrentHashMap<>();

    private volatile EntityType allMobs = null;

    private MobDisguisePackets() {
        super(PacketListenerPriority.NORMAL);
    }

    static void start() {
        PacketEvents.getAPI().getEventManager().registerListener(INSTANCE);
        Bukkit.getPluginManager().registerEvents(INSTANCE, Main.get());
    }

    static boolean supports(EntityType type) {
        return type.isAlive()
                && type != EntityType.PLAYER
                && SpigotConversionUtil.fromBukkitEntityType(type) != null;
    }

    static boolean isDisguised(UUID uuid) {
        for (Mob mob : INSTANCE.active.values()) {
            if (mob.uuid().equals(uuid)) return true;
        }
        return false;
    }

    static void disguise(Player subject, EntityType type) {
        if (!supports(type)) return;
        INSTANCE.active.put(subject.getEntityId(), new Mob(subject.getUniqueId(), type));
        DisguiseService.PacketService.respawnPlayerEntity(subject);
    }

    static void undisguise(Player subject) {
        if (INSTANCE.active.remove(subject.getEntityId()) == null) return;
        DisguiseService.PacketService.respawnPlayerEntity(subject);
    }

    static void disguiseAllMobs(EntityType type) {
        if (!supports(type)) return;
        INSTANCE.allMobs = type;
    }

    static void undisguiseAllMobs() {
        INSTANCE.allMobs = null;
    }

    static EntityType disguisedMobType() {
        return INSTANCE.allMobs;
    }

    static void clearAll() {
        for (Mob mob : new ArrayList<>(INSTANCE.active.values())) {
            Player subject = Bukkit.getPlayer(mob.uuid());
            if (subject != null) undisguise(subject);
        }
        INSTANCE.active.clear();
        INSTANCE.allMobs = null;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        active.remove(event.getPlayer().getEntityId());
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (active.isEmpty() && allMobs == null) return;
        User user = event.getUser();
        if (user == null || user.getUUID() == null) return;

        if (event.getPacketType() == PacketType.Play.Server.SPAWN_PLAYER) {
            WrapperPlayServerSpawnPlayer spawn = new WrapperPlayServerSpawnPlayer(event);
            Mob mob = active.get(spawn.getEntityId());
            if (mob == null || mob.uuid().equals(user.getUUID())) return;
            event.setCancelled(true);
            user.sendPacketSilently(new WrapperPlayServerSpawnLivingEntity(
                    spawn.getEntityId(),
                    spawn.getUUID(),
                    SpigotConversionUtil.fromBukkitEntityType(mob.type()),
                    spawn.getPosition(),
                    spawn.getYaw(),
                    spawn.getPitch(),
                    spawn.getYaw(),
                    new Vector3d(0, 0, 0),
                    List.<EntityData<?>>of()));
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.SPAWN_LIVING_ENTITY) {
            EntityType type = allMobs;
            if (type == null) return;
            WrapperPlayServerSpawnLivingEntity spawn = new WrapperPlayServerSpawnLivingEntity(event);
            spawn.setEntityType(SpigotConversionUtil.fromBukkitEntityType(type));
            event.markForReEncode(true);
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
            cancelIfDisguised(event, user, new WrapperPlayServerEntityMetadata(event).getEntityId());
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.ENTITY_EQUIPMENT) {
            cancelIfDisguised(event, user, new WrapperPlayServerEntityEquipment(event).getEntityId());
        }
    }

    private void cancelIfDisguised(PacketSendEvent event, User user, int entityId) {
        Mob mob = active.get(entityId);
        if (mob != null && !mob.uuid().equals(user.getUUID())) event.setCancelled(true);
    }
}
