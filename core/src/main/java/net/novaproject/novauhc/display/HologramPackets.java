package net.novaproject.novauhc.display;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnLivingEntity;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class HologramPackets {

    private static final double SMALL_STAND_HEIGHT = 0.9875;
    private static final double NAMEPLATE_GAP = 0.5;

    private static final int FLAG_INVISIBLE = 0x20;
    private static final int STAND_SMALL = 0x01;
    private static final int STAND_NO_BASE_PLATE = 0x08;
    private static final int STAND_MARKER = 0x10;

    private static final AtomicInteger NEXT_ID = new AtomicInteger(1_000_000_000);

    public static double standY(double nameY) {
        return nameY - SMALL_STAND_HEIGHT - NAMEPLATE_GAP;
    }

    public static int spawn(Player viewer, Location nameAt, String text) {
        int entityId = NEXT_ID.incrementAndGet();
        send(viewer, new WrapperPlayServerSpawnLivingEntity(
                entityId,
                UUID.randomUUID(),
                EntityTypes.ARMOR_STAND,
                position(nameAt),
                0f, 0f, 0f,
                new Vector3d(0, 0, 0),
                metadata(text, text != null)));
        return entityId;
    }

    public static void text(Player viewer, int entityId, String text, boolean visible) {
        send(viewer, new WrapperPlayServerEntityMetadata(entityId, metadata(text, visible)));
    }

    public static void teleport(Player viewer, int entityId, Location nameAt) {
        send(viewer, new WrapperPlayServerEntityTeleport(entityId, position(nameAt), 0f, 0f, false));
    }

    public static void destroy(Player viewer, int entityId) {
        send(viewer, new WrapperPlayServerDestroyEntities(entityId));
    }

    private static Vector3d position(Location nameAt) {
        return new Vector3d(nameAt.getX(), standY(nameAt.getY()), nameAt.getZ());
    }

    private static List<EntityData<?>> metadata(String text, boolean visible) {
        List<EntityData<?>> data = new ArrayList<>();
        data.add(new EntityData<>(0, EntityDataTypes.BYTE, (byte) FLAG_INVISIBLE));
        data.add(new EntityData<>(2, EntityDataTypes.STRING, text == null ? "" : text));
        data.add(new EntityData<>(3, EntityDataTypes.BYTE, (byte) (visible && text != null ? 1 : 0)));
        data.add(new EntityData<>(10, EntityDataTypes.BYTE,
                (byte) (STAND_SMALL | STAND_NO_BASE_PLATE | STAND_MARKER)));
        return data;
    }

    private static void send(Player viewer, PacketWrapper<?> packet) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
    }
}
