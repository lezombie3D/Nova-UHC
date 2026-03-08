package net.novaproject.novauhc.listener.player;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
// @author (Guillaume-BH)
public class FixedPlayerBucketConnection extends PlayerConnection {

    private final Queue<PacketPlayInBlockPlace> lastBucketPacket;

    public FixedPlayerBucketConnection(MinecraftServer server,
                                       NetworkManager manager,
                                       EntityPlayer entityPlayer) {
        super(server, manager, entityPlayer);
        this.lastBucketPacket = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void a(PacketPlayInBlockPlace packet) {
        // Check if item is a bucket
        ItemStack item = this.player.inventory.getItemInHand();
        if (item != null) {
            org.bukkit.inventory.ItemStack bukkitItem = CraftItemStack.asBukkitCopy(item);

            if (bukkitItem.getType() == Material.WATER_BUCKET
                    || bukkitItem.getType() == Material.LAVA_BUCKET) {

                // If the packet face is 255, it means it's a bucket placement packet without a block position, we need to wait for the movement packet to set the player's yaw and pitch before processing it
                if (packet.getFace() == 255) {
                    this.lastBucketPacket.add(packet);
                    return;
                }
            }
        }
        super.a(packet);
    }

    @Override
    public void a(PacketPlayInFlying packet) {

        float packetYaw = packet.d();
        float packetPitch = packet.e();

        // If the packet contains yaw and pitch information, we need to set the player's yaw and pitch to the values from the packet before processing any bucket placement packets that are waiting in the queue
        if (packetYaw != 0 || packetPitch != 0) {
            // Set the player's yaw and pitch player.setYawPitch(packetYaw, packetPitch) to the values from the packet (method is protected, use reflection)
            try {
                java.lang.reflect.Method setYawPitchMethod = Entity.class.getDeclaredMethod("setYawPitch", float.class, float.class);
                setYawPitchMethod.setAccessible(true);
                setYawPitchMethod.invoke(this.player, packetYaw, packetPitch);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Process any bucket placement packets that are waiting in the queue
        for (PacketPlayInBlockPlace packetPlayInBlockPlace : this.lastBucketPacket) {
            this.lastBucketPacket.remove(packetPlayInBlockPlace);
            super.a(packetPlayInBlockPlace);
        }

        super.a(packet);
    }
}