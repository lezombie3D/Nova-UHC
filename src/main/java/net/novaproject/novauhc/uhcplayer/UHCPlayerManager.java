package net.novaproject.novauhc.uhcplayer;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import org.bukkit.entity.Player;

import java.util.*;

public class UHCPlayerManager {

    public static UHCPlayerManager get(){
        return UHCManager.get().getUHCPlayerManager();
    }


    private final Map<UUID, UHCPlayer> players = new HashMap<>();

    public List<UHCPlayer> getOnlineUHCPlayers() {

        List<UHCPlayer> result = new ArrayList<>();

        for (UHCPlayer player : players.values()) {

            if (player.isOnline()) {
                result.add(player);
            }

        }

        return result;
    }

    public List<UHCPlayer> getPlayingOnlineUHCPlayers() {

        List<UHCPlayer> result = new ArrayList<>();
        for (UHCPlayer player : getOnlineUHCPlayers()) {

            if (player.isPlaying()) {

                result.add(player);

            }

        }

        return result;
    }

    public UHCPlayer getPlayer(Player player) {
        return players.get(player.getUniqueId());
    }



    public void connect(Player player) {

        UHCPlayer uhcPlayer = new UHCPlayer(player);

        players.putIfAbsent(player.getUniqueId(), uhcPlayer);

        uhcPlayer = getPlayer(player);

        uhcPlayer.connect(player);

    }

    public void disconnect(Player player) {

        UHCPlayer uhcPlayer = getPlayer(player);

        uhcPlayer.disconnect(player);

        // players.remove(player.getUniqueId()); enlever de la normal si status de jeu est lobby

    }

}
