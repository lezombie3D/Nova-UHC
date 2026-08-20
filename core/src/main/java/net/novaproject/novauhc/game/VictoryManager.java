package net.novaproject.novauhc.game;

import net.novaproject.novauhc.scenario.role.Role;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.api.ApiManager;
import net.novaproject.novauhc.event.UhcGameEvents.UhcGameWinEvent;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.player.utils.ReconnectionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class VictoryManager implements GameSpi.IVictoryManager {

    private static void stopScenarioSafely(Scenario scenario) {
        try {
            scenario.onStop();
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.WARNING, "[Victory] onStop en échec pour " + scenario.getName(), t);
        }
    }

    private static Function<List<UHCPlayer>, String> winLabelResolver;

    public static void setWinLabelResolver(Function<List<UHCPlayer>, String> resolver) {
        winLabelResolver = resolver;
    }

    private boolean checkQueued = false;
    private boolean victoryEnabled = true;

    public boolean isVictoryEnabled() {
        return victoryEnabled;
    }

    public void setVictoryEnabled(boolean enabled) {
        this.victoryEnabled = enabled;
        if (enabled) requestCheck();
    }

    public void requestCheck() {
        if (!victoryEnabled) return;
        if (UHCManager.get().getGameState() != UHCManager.GameState.INGAME) {
            return;
        }
        if (checkQueued) return;
        checkQueued = true;
        Bukkit.getScheduler().runTask(Main.get(), () -> {
            checkQueued = false;
            check();
        });
    }

    private void check() {
        UHCManager uhc = UHCManager.get();
        if (!victoryEnabled) return;
        if (uhc.getGameState() != UHCManager.GameState.INGAME) {
            return;
        }
        if (PendingDeathManager.get().hasAnyPending()) {
            return;
        }
        if (ReconnectionManager.get().hasAnyPendingReconnection()) {
            return;
        }

        for (Scenario scenario : ScenarioManager.get().getActiveScenarios()) {
            if (scenario.overridesVictory()) {
                if (scenario.isWin()) {
                    RoleWinners roleWinners = resolveRoleWinners();
                    endGame(roleWinners.players, roleWinners.camp);
                }
                return;
            }
        }

        boolean win = false;
        List<UHCPlayer> winners = new ArrayList<>();
        Camps winningCamp = null;

        if (uhc.getTeam_size() == 1) {
            List<UHCPlayer> alivePlayers = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();

            if (alivePlayers.size() <= 1) {
                if (alivePlayers.size() == 1) {
                    UHCPlayer player = alivePlayers.get(0);
                    Player soloPlayer = player.getPlayer();
                    String soloName = soloPlayer != null ? soloPlayer.getName() : Bukkit.getOfflinePlayer(player.getUuid()).getName();
                    LangManager.get().sendAll(CoreLang.COMMON_SOLO_WIN, Map.of("%player%", soloName));
                    winners.addAll(alivePlayers);
                }
                win = true;
            }
        } else {
            List<UHCTeam> aliveTeams = uhc.getUhcTeamManager().getAliveTeams();
            List<UHCPlayer> soloPlayers = UHCPlayerManager.get().getPlayingOnlineUHCPlayers().stream()
                    .filter(p -> p.getTeam().isEmpty())
                    .collect(Collectors.toList());

            if (aliveTeams.size() == 1 && soloPlayers.isEmpty()) {
                UHCTeam team = aliveTeams.get(0);

                List<UHCPlayer> sorted = new ArrayList<>(team.getPlayers());
                sorted.sort((a, b) -> Integer.compare(b.getKill(), a.getKill()));

                LangManager.get().sendAll(CoreLang.COMMON_TEAM_WIN, Map.of("%team%", team.name()));
                int rank = 1;
                int total = 0;

                for (UHCPlayer member : sorted) {
                    String prefix = rank == 1 ? "§6⭐ " : "  ";
                    Player memberPlayer = member.getPlayer();
                    String memberName = memberPlayer != null ? memberPlayer.getName() : Bukkit.getOfflinePlayer(member.getUuid()).getName();
                    Bukkit.broadcastMessage(
                            LangManager.get().get(
                                    CoreLang.COMMON_TEAM_RANK_LINE,
                                    null,
                                    Map.of(
                                            "%prefix%", prefix,
                                            "%rank%", String.valueOf(rank),
                                            "%player%", memberName,
                                            "%kills%", String.valueOf(member.getKill())
                                    )
                            )
                    );
                    total += member.getKill();
                    rank++;
                }

                LangManager.get().sendAll(CoreLang.COMMON_TEAM_TOTAL_KILLS, Map.of("%total%", String.valueOf(total)));
                winners.addAll(team.getPlayers());
                win = true;
            }
        }

        for (Scenario scenario : ScenarioManager.get().getActiveScenarios()) {
            if (!scenario.overridesVictory() && scenario.isWin()) {
                win = true;
            }
        }

        if (win) {
            if (winners.isEmpty()) {
                RoleWinners roleWinners = resolveRoleWinners();
                winners = roleWinners.players;
                winningCamp = roleWinners.camp;
            }
            endGame(winners, winningCamp);
        }
    }

    private record RoleWinners(List<UHCPlayer> players, Camps camp) {
    }

    private RoleWinners resolveRoleWinners() {
        List<UHCPlayer> alive = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();
        Camps camp = null;

        for (Scenario s : ScenarioManager.get().getActiveSpecialScenarios()) {
            if (s instanceof ScenarioRole<?> scenarioRole) {
                Map<Camps, Integer> aliveCamps = new HashMap<>();
                for (UHCPlayer uhcPlayer : alive) {
                    Role role = scenarioRole.getRoleByUHCPlayer(uhcPlayer);
                    if (role == null || role.getCamp() == null) continue;
                    aliveCamps.merge(role.getCamp(), 1, Integer::sum);
                }
                if (aliveCamps.size() == 1) {
                    camp = aliveCamps.keySet().iterator().next();
                } else {
                    camp = scenarioRole.getWinningCamp();
                }
                break;
            }
        }

        return new RoleWinners(new ArrayList<>(alive), camp);
    }

    private static String resolveWinLabel(List<UHCPlayer> winners) {
        if (winLabelResolver == null) return null;
        try {
            String label = winLabelResolver.apply(winners);
            return label == null || label.isEmpty() ? null : label;
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.WARNING, "[Victory] winLabelResolver en échec", t);
            return null;
        }
    }

    private void endGame(List<UHCPlayer> winnerPlayers, Camps winningCamp) {
        UHCManager uhc = UHCManager.get();
        StringBuilder killmessage = new StringBuilder();
        uhc.setGameState(UHCManager.GameState.ENDING);

        List<UHCPlayer> uniqueWinners = new ArrayList<>();
        Set<UUID> seen = new HashSet<>();
        if (winnerPlayers != null) {
            for (UHCPlayer w : winnerPlayers) {
                if (w != null && seen.add(w.getUuid())) {
                    uniqueWinners.add(w);
                }
            }
        }

        if (uniqueWinners.isEmpty()) {
            Bukkit.broadcastMessage(LangManager.get().get(CoreLang.COMMON_DRAW_NO_WINNER));
            ScenarioManager.get().getActiveScenarios().forEach(VictoryManager::stopScenarioSafely);
            Lifecycles.stopAll();
            for (UHCPlayer loser : UHCPlayerManager.get().getOnlineUHCPlayers()) {
                if (loser.getPlayer() != null) {
                    loser.getPlayer().teleport(Common.get().getLobbySpawn());
                }
            }
            ReconnectionManager.get().cleanup();
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.getServer().getPluginManager().disablePlugin(Main.get());
                    Bukkit.getServer().shutdown();
                }
            }.runTaskLater(Main.get(), 20 * 60);
            return;
        }

        List<ApiManager.WinnerInfo> winners = new ArrayList<>();

        for (UHCPlayer w : uniqueWinners) {
            Player winnerPlayer = w.getPlayer();
            String name = winnerPlayer != null ? winnerPlayer.getName() : Bukkit.getOfflinePlayer(w.getUuid()).getName();

            killmessage.append(winnerPlayer != null ? winnerPlayer.getDisplayName() : name)
                    .append(" : ")
                    .append(w.getKill())
                    .append(" kills\n");

            winners.add(new ApiManager.WinnerInfo(
                    "player",
                    w.getUuid().toString(),
                    name,
                    w.getKill(),
                    null
            ));

            if (winnerPlayer != null && winnerPlayer.isOnline()) {
                winnerPlayer.teleport(Common.get().getLobbySpawn());
                fireWork(winnerPlayer);
            }
        }

        boolean campHomogeneous = winningCamp != null && allInCamp(uniqueWinners, winningCamp);
        String customLabel = resolveWinLabel(uniqueWinners);
        if (campHomogeneous) {
            LangManager.get().sendAll(CoreLang.COMMON_WIN_CAMP_BROADCAST,
                    Map.of("%camp%", winningCamp.getColor() + winningCamp.getName()));
        } else if (customLabel != null) {
            LangManager.get().sendAll(CoreLang.COMMON_WIN_CAMP_BROADCAST, Map.of("%camp%", customLabel));
        }
        String winTitle = campHomogeneous
                ? winningCamp.getColor() + "✦ " + winningCamp.getName() + " ✦"
                : customLabel != null
                        ? customLabel
                        : LangManager.get().get(CoreLang.COMMON_WIN_TITLE_GENERIC);
        for (Player online : Bukkit.getOnlinePlayers()) {
            DisplayService.title(online, winTitle,
                    LangManager.get().get(CoreLang.COMMON_WIN_TITLE_SUBTITLE, online), 100);
        }

        Bukkit.broadcastMessage(ChatColor.AQUA + killmessage.toString());

        broadcastEndStats(uhc);

        Bukkit.getPluginManager().callEvent(new UhcGameWinEvent(uniqueWinners, winningCamp));

        List<ApiManager.PlayerStats> playerStats = uhc.getStatsTracker().getPlayerStats();
        int duration = uhc.getStatsTracker().getGameDuration();

        String mode = uhc.getTeam_size() == 1 ? "Solo" : "Team " + uhc.getTeam_size();
        String scenario = null;
        String winCondition = mode.equals("Solo") ? "solo" : "team";

        if (!ScenarioManager.get().getActiveSpecialScenarios().isEmpty()) {
            mode = "Special";
            scenario = ScenarioManager.get().getActiveSpecialScenarios().get(0).getName();
            winCondition = "custom";
        }

        ApiManager.get().gameEnd(mode, scenario, winCondition, winners, playerStats, duration, null);

        ScenarioManager.get().getActiveScenarios().forEach(VictoryManager::stopScenarioSafely);
        Lifecycles.stopAll();

        for (UHCPlayer loser : UHCPlayerManager.get().getOnlineUHCPlayers()) {
            loser.getPlayer().teleport(Common.get().getLobbySpawn());
        }
        ReconnectionManager.get().cleanup();

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getServer().getPluginManager().disablePlugin(Main.get());
                Bukkit.getServer().shutdown();
            }
        }.runTaskLater(Main.get(), 20 * 60);
    }

    private void broadcastEndStats(UHCManager uhc) {
        List<ApiManager.PlayerStats> allStats = uhc.getStatsTracker().getPlayerStats();
        if (allStats.isEmpty()) return;

        Bukkit.broadcastMessage(LangManager.get().get(CoreLang.COMMON_END_STATS_HEADER));

        List<ApiManager.PlayerStats> topKills = allStats.stream()
                .filter(s -> s.kills() > 0)
                .sorted((a, b) -> Integer.compare(b.kills(), a.kills()))
                .limit(3)
                .collect(Collectors.toList());
        String[] medals = {"§6⭐", "§7✦", "§c✦"};
        for (int i = 0; i < topKills.size(); i++) {
            ApiManager.PlayerStats s = topKills.get(i);
            Bukkit.broadcastMessage(LangManager.get().get(CoreLang.COMMON_END_STATS_KILL_LINE, null, Map.of(
                    "%medal%", medals[i],
                    "%player%", s.name(),
                    "%kills%", s.kills(),
                    "%assists%", s.assists())));
        }

        allStats.stream()
                .max((a, b) -> Double.compare(a.damageDealt(), b.damageDealt()))
                .filter(s -> s.damageDealt() > 0)
                .ifPresent(s -> Bukkit.broadcastMessage(LangManager.get().get(CoreLang.COMMON_END_STATS_DAMAGE_LINE, null, Map.of(
                        "%player%", s.name(),
                        "%damage%", String.format("%.1f", s.damageDealt())))));
    }

    private boolean allInCamp(List<UHCPlayer> winners, Camps camp) {
        ScenarioRole<?> scenario = KPIBuilder.activeScenarioRole();
        if (scenario == null) return true;
        for (UHCPlayer winner : winners) {
            Role role = scenario.getRoleByUHCPlayer(winner);
            if (role == null || role.getCamp() == null) return false;
            if (!role.getCamp().isOrHasParent(camp)) return false;
        }
        return true;
    }

    private void fireWork(Player p) {
        Firework f = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
        f.detonate();

        FireworkMeta fM = f.getFireworkMeta();

        FireworkEffect effect = FireworkEffect.builder()
                .flicker(true)
                .withColor(Color.PURPLE)
                .withFade(Color.ORANGE)
                .with(FireworkEffect.Type.BALL)
                .trail(true)
                .build();

        fM.addEffect(effect);
        fM.setPower(1);
        f.setFireworkMeta(fM);
    }
}