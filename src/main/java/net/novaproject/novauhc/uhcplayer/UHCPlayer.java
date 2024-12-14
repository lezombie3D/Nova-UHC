package net.novaproject.novauhc.uhcplayer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class UHCPlayer {

    private final UUID uuid;

    private boolean playing = false;

    public UHCPlayer(Player player) {
        this.uuid = player.getUniqueId();
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public Player getPlayer(){
        return Bukkit.getPlayer(uuid);
    }

    public boolean isOnline() {
        return getPlayer() != null;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public void connect(Player player) {
        // VERIFIRA SI LE STATUS DE JEU EST INGAME OU EN LOBBY
    }

    public void disconnect(Player player) {

    }
}
