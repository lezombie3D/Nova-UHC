package net.novaproject.novauhc.display;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateHealth;
import org.bukkit.entity.Player;

public final class HealthPackets {

    public static void fakeHealth(Player viewer, double hearts) {
        if (viewer == null || !viewer.isOnline()) return;
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer,
                new WrapperPlayServerUpdateHealth((float) hearts, viewer.getFoodLevel(), viewer.getSaturation()));
    }

    public static void fakeFullHealth(Player viewer) {
        if (viewer == null || !viewer.isOnline()) return;
        fakeHealth(viewer, viewer.getMaxHealth());
    }
}
