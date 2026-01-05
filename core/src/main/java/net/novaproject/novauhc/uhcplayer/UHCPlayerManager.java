package net.novaproject.novauhc.uhcplayer;

import fr.mrmicky.fastboard.FastBoard;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.ui.config.Enchants;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.utils.TabListManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class UHCPlayerManager {

    private boolean tasks = false;

    public static UHCPlayerManager get(){
        return UHCManager.get().getUhcPlayerManager();
    }


    private final Map<UUID, UHCPlayer> players = new HashMap<>();
    private final Map<UUID, FastBoard> boards = new HashMap<>();

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

        FastBoard board = new FastBoard(player);


        boards.put(player.getUniqueId(), board);

        if (!tasks) {
            tasks = true;
            player.getServer().getScheduler().runTaskTimerAsynchronously(
                    Main.get(),
                    () -> boards.values().forEach(this::updateBoard),
                    0, 1L
            );
        }

    }

    public void disconnect(Player player) {

        UHCPlayer uhcPlayer = getPlayer(player);

        uhcPlayer.disconnect(player);

        if (UHCManager.get().isLobby()) {
            players.remove(player.getUniqueId());
        }
        FastBoard board = boards.remove(player.getUniqueId());
        if (board != null) {
            board.delete();
        }

    }

    private void updateBoard(FastBoard board) {
        Player player = board.getPlayer();
        UHCManager uhcManager = UHCManager.get();


        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null) return;

        FileConfiguration config = ConfigUtils.getGeneralConfig();

        String phase = uhcManager.isLobby() ? "lobby" : (uhcManager.isGame() ? "game" : "end");
        boolean tabIsActive = config.getBoolean("message.tab.show", true);

        String header = CommonString.getMessage(config.getString("message.tab." + phase + ".header", ""), uhcPlayer);
        String footer = CommonString.getMessage(config.getString("message.tab." + phase + ".footer", ""), uhcPlayer);

        String title = config.getString("message.scoreboard." + phase + ".title", "§6NovaUHC");
        title = CommonString.getMessage(title, uhcPlayer);

        List<String> lines = config.getStringList("message.scoreboard." + phase + ".lines");
        if (ScenarioManager.get().getActiveSpecialScenarios().stream()
                .anyMatch(scenario -> scenario instanceof ScenarioRole)) {
            lines = config.getStringList("message.scoreboard." + phase + ".lines_role");
            title = config.getString("message.scoreboard." + phase + ".title_role", "§6NovaUHC");
            title = CommonString.getMessage(title, uhcPlayer);
        }
        List<String> processedLines = lines.stream()
                .map(line -> {
                    line = line.replace("<ip>", Common.get().getServerIp());
                    return CommonString.getMessage(line, uhcPlayer);
                })
                .collect(Collectors.toList());

        board.updateTitle(title);
        board.updateLines(processedLines);

        if (tabIsActive) {
            TabListManager.sendTab(player, header, footer);
        }
    }
}
