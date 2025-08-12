package net.novaproject.novauhc.uhcplayer;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.ui.config.Enchants;
import org.bukkit.entity.Player;

import java.util.*;

public class UHCPlayerManager {

    public static UHCPlayerManager get(){
        return UHCManager.get().getUhcPlayerManager();
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

    public void updateAllPlayersEnchant(Enchants enchant, int delta) {
        getOnlineUHCPlayers().forEach(uhcPlayer -> {
            int current = uhcPlayer.getEnchantLimits().get(enchant);
            int newValue = Math.max(
                    enchant.getMin(),
                    Math.min(enchant.getMax(), current + delta)
            );
            uhcPlayer.setEnchantLimit(enchant, newValue);
        });
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

        if (UHCManager.get().isLobby()) {
            players.remove(player.getUniqueId());
        }

    }


}
