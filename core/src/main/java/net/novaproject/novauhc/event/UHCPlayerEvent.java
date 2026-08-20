package net.novaproject.novauhc.event;

import net.novaproject.novauhc.player.UHCPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.UUID;

public abstract class UHCPlayerEvent extends Event {

    protected final UHCPlayer uhcPlayer;

    protected UHCPlayerEvent(UHCPlayer uhcPlayer) {
        this.uhcPlayer = uhcPlayer;
    }

    public final UHCPlayer getUhcPlayer() {
        return uhcPlayer;
    }

    public final UUID getUniqueId() {
        return uhcPlayer.getUniqueId();
    }

    public final Player getBukkitPlayer() {
        return uhcPlayer.getPlayer();
    }
}
