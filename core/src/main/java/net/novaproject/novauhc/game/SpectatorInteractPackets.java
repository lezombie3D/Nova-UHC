package net.novaproject.novauhc.game;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import net.novaproject.novauhc.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class SpectatorInteractPackets extends PacketListenerAbstract {

    private SpectatorInteractPackets() {
        super(PacketListenerPriority.NORMAL);
    }

    public static void start() {
        PacketEvents.getAPI().getEventManager().registerListener(new SpectatorInteractPackets());
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) return;
        if (!(event.getPlayer() instanceof Player viewer)) return;
        WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
        if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;
        int entityId = wrapper.getEntityId();
        Bukkit.getScheduler().runTask(Main.get(), () -> SpectatorInteraction.openByEntityId(viewer, entityId));
    }
}
