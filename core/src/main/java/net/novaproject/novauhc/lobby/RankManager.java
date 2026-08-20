package net.novaproject.novauhc.lobby;

import net.novaproject.novauhc.display.TabFreezeService;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.listener.PlayerConnectionEvent;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;

public class RankManager {

    private static final RankManager INSTANCE = new RankManager();


    public static RankManager get() {
        return INSTANCE;
    }

    public static boolean isStaff(Player player) {
        return player != null
                && (player.hasPermission("novauhc.host") || player.hasPermission("novauhc.cohost"));
    }

    public static boolean isStaffOrOp(Player player) {
        return player != null && (player.isOp() || isStaff(player));
    }

    public Rank getRank(Player player) {
        if (player == null) return Rank.PLAYER;

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        boolean inGame = uhcPlayer != null && hasStarted(uhcPlayer.getUhcManager());

        if (uhcPlayer != null && (uhcPlayer.isSpec() || (!uhcPlayer.isPlaying() && inGame))) {
            return Rank.SPECTATOR;
        }

        if (!inGame) {
            OfflinePlayer host = PlayerConnectionEvent.getHost();
            if (host != null && player.getUniqueId().equals(host.getUniqueId())) {
                return Rank.HOST;
            }
            if (player.hasPermission("novauhc.cohost")) {
                return Rank.COHOST;
            }
        }
        return Rank.PLAYER;
    }

    public void applyTag(Player player) {
        if (player == null) return;
        applyTag(player, getRank(player));
    }

    public void applyTag(Player player, Rank rank) {
        if (player == null || rank == null) return;
        if (rank == Rank.SPECTATOR && TabFreezeService.isFrozen(player.getUniqueId())) return;
        if ((rank == Rank.HOST || rank == Rank.COHOST) && hasStarted(UHCManager.get())) {
            rank = Rank.PLAYER;
        }
        if (rank == Rank.PLAYER) {
            Optional<UHCTeam> team = playingTeamOf(player);
            if (team.isPresent()) {
                UHCTeam uhcTeam = team.get();
                String prefix = isPastilleMode()
                        ? UHCTeamManager.get().pastilleOf(uhcTeam)
                        : uhcTeam.prefix();
                DisplayService.nametag(player, uhcTeam.name(), prefix, "");
                return;
            }
        }
        DisplayService.nametag(player, rank.getSortKey(), rank.getPrefix(), "");
    }

    private static boolean isPastilleMode() {
        ScenarioRole<?> scenarioRole = KPIBuilder.activeScenarioRole();
        return scenarioRole != null && scenarioRole.isRolesDistributed();
    }

    private static Optional<UHCTeam> playingTeamOf(Player player) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null || !uhcPlayer.isPlaying()) return Optional.empty();
        return uhcPlayer.getTeam();
    }

    private static boolean hasStarted(UHCManager uhcManager) {
        return !uhcManager.isLobby();
    }

    public void refreshTags() {
        for (Player online : Bukkit.getOnlinePlayers()) applyTag(online);
    }

    public enum Rank {
        HOST("host", "§c§lHOST §r§c"),
        COHOST("cohost", "§5§lCOHOST §r§5"),
        SPECTATOR("zzzzz", "§8§lSPEC §r§8"),
        PLAYER("", "");

        private final String sortKey;
        private final String prefix;

        Rank(String sortKey, String prefix) {
            this.sortKey = sortKey;
            this.prefix = prefix;
        }

        public String getSortKey() {
            return sortKey;
        }

        public String getPrefix() {
            return prefix;
        }
    }
}
