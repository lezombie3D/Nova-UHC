package net.novaproject.novauhc.mumble;

import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public interface IMumbleManager {

    boolean isEnabled();

    boolean hasSession();

    String getSessionUuid();

    CompletableFuture<Void> provision(int teams, String title, Player host);

    void close();

    void playerDeath(String playerName);

    void broadcastJoinLinks();

    void sendJoinLink(Player player);
}
