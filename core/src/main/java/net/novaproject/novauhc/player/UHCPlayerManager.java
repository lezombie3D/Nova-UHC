package net.novaproject.novauhc.player;

import java.util.function.Supplier;
import java.util.function.Function;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.display.scoreboard.ScoreboardService;
import net.novaproject.novauhc.display.PlayerColorManager;
import net.novaproject.novauhc.display.TabFreezeService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class UHCPlayerManager implements PlayerSpi.IUHCPlayerManager {

    public static void setCustomScoreboardLines(String key, Function<Player, List<String>> provider) {
        ScoreboardService.setExtraLines(key, provider);
    }

    public static void setCustomRoleScoreboardLines(Supplier<List<String>> supplier) {
        ScoreboardService.setExtraLines("__legacy_role__", supplier == null ? null : p -> supplier.get());
    }

    public static UHCPlayerManager get() {
        return UHCManager.get().getUhcPlayerManager();
    }

    private final Map<UUID, UHCPlayer> players = new HashMap<>();

    public List<UHCPlayer> getOnlineUHCPlayers() {
        return players.values().stream().filter(UHCPlayer::isOnline).collect(Collectors.toList());
    }

    public List<UHCPlayer> getPlayingOnlineUHCPlayers() {
        return players.values().stream()
                .filter(UHCPlayer::isPlaying)
                .filter(UHCPlayer::isOnline)
                .collect(Collectors.toList());
    }

    public UHCPlayer getPlayer(Player player) {
        return player == null ? null : players.get(player.getUniqueId());
    }

    public UHCPlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public void connect(Player player) {
        UHCPlayer uhcPlayer = new UHCPlayer(player);
        players.putIfAbsent(player.getUniqueId(), uhcPlayer);
        uhcPlayer = getPlayer(player);
        uhcPlayer.connect(player);

        ScoreboardService.get().addPlayer(player);

        Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
            PlayerColorManager.reapplyColorsForViewer(player);
            PlayerColorManager.reapplyColorsForTarget(player);
            TabFreezeService.onJoin(player);
        }, 5L);
    }

    public void disconnect(Player player) {
        UHCPlayer uhcPlayer = getPlayer(player);
        uhcPlayer.disconnect(player);

        PlayerColorManager.removeTeamForTarget(player);
        PlayerColorManager.cleanupViewer(player.getUniqueId());

        if (UHCManager.get().isLobby()) {
            players.remove(player.getUniqueId());
        }
        ScoreboardService.get().removePlayer(player.getUniqueId());
    }
}