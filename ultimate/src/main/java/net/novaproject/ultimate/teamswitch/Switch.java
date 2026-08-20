package net.novaproject.ultimate.teamswitch;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;
import net.novaproject.novauhc.lang.lang.ScenarioLang;

public class Switch extends Scenario {

    @Var(name = "Swap interval", desc = "Time between each player exchange between teams.", type = VariableType.TIME)
    private int swapInterval = 300;

    @Var(name = "Swap start time", desc = "Game time before the first team exchange.", type = VariableType.TIME)
    private int swapStartTime = 600;

    private BukkitRunnable swapTask;
    private long nextSwapAt = 0L;
    private final Random random = new Random();

    @Override
    public String getName() { return "Switch"; }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.SWITCH, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.ENDER_PEARL);
    }

    @Override
    public void onGameStart() {
        nextSwapAt = System.currentTimeMillis() + swapStartTime * 1000L;
        swapTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) { cancel(); return; }
                performSwap();
                nextSwapAt = System.currentTimeMillis() + swapInterval * 1000L;
            }
        };
        swapTask.runTaskTimer(Main.get(), swapStartTime * 20L, swapInterval * 20L);

        UHCPlayerManager.setCustomScoreboardLines("switch", p -> {
            long diff = nextSwapAt - System.currentTimeMillis();
            if (diff < 0) diff = 0;
            int sec = (int) (diff / 1000L);
            int m = sec / 60, s = sec % 60;
            return Collections.singletonList("§f  §8● §fProchain Switch: §6" + String.format("%d:%02d", m, s));
        });
    }

    @Override
    public void onStop() {
        UHCPlayerManager.setCustomScoreboardLines("switch", null);
        if (swapTask != null) {
            swapTask.cancel();
            swapTask = null;
        }
    }
    @Override
    public boolean isSpecial() {
        return true;
    }
    private void performSwap() {
        List<UHCTeam> aliveTeams = UHCTeamManager.get().getAliveTeams();
        if (aliveTeams.size() < 2) return;

        int teamSize = UHCManager.get().getTeam_size();

        List<UHCTeam> soloTeams = new ArrayList<>();
        List<UHCTeam> nonSoloTeams = new ArrayList<>();
        for (UHCTeam team : aliveTeams) {
            long aliveCount = team.getPlayers().stream().filter(UHCPlayer::isPlaying).count();
            if (aliveCount == 1) soloTeams.add(team);
            else nonSoloTeams.add(team);
        }

        boolean didMerge = false;
        if (teamSize > 1 && soloTeams.size() >= 2) {
            Collections.shuffle(soloTeams, random);
            int i = 0;
            while (i + 1 < soloTeams.size()) {
                UHCTeam teamA = soloTeams.get(i);
                UHCTeam teamB = soloTeams.get(i + 1);

                UHCPlayer pA = teamA.getPlayers().stream().filter(UHCPlayer::isPlaying).findFirst().orElse(null);
                UHCPlayer pB = teamB.getPlayers().stream().filter(UHCPlayer::isPlaying).findFirst().orElse(null);
                if (pA == null || pB == null) { i += 2; continue; }

                boolean intoA = random.nextBoolean();
                UHCTeam target = intoA ? teamA : teamB;
                UHCPlayer mover = intoA ? pB : pA;
                UHCPlayer anchor = intoA ? pA : pB;

                mover.forceSetTeam(Optional.of(target));
                Player moverPlayer = mover.getPlayer();
                Player anchorPlayer = anchor.getPlayer();
                if (moverPlayer != null && anchorPlayer != null) {
                    moverPlayer.teleport(anchorPlayer.getLocation());
                }

                String teamDisplay = target.prefix() + target.name();
                if (moverPlayer != null) {
                    LangManager.get().send(ScenarioLang.SWITCH_MERGE_PLAYER_INFO, moverPlayer, Map.of("%team%", teamDisplay));
                }
                if (anchorPlayer != null) {
                    LangManager.get().send(ScenarioLang.SWITCH_MERGE_PLAYER_INFO, anchorPlayer, Map.of("%team%", teamDisplay));
                }
                didMerge = true;
                i += 2;
            }
            if (i < soloTeams.size()) {
                nonSoloTeams.add(soloTeams.get(i));
            }
        } else {
            nonSoloTeams.addAll(soloTeams);
        }

        if (didMerge) {
            LangManager.get().sendAll(ScenarioLang.SWITCH_MERGE_ANNOUNCE);
        }

        List<UHCPlayer> toSwap = new ArrayList<>();
        for (UHCTeam team : nonSoloTeams) {
            List<UHCPlayer> members = team.getPlayers().stream()
                    .filter(UHCPlayer::isPlaying)
                    .collect(Collectors.toList());
            if (!members.isEmpty()) {
                toSwap.add(members.get(random.nextInt(members.size())));
            }
        }

        if (toSwap.size() < 2) return;

        List<Optional<UHCTeam>> teams = toSwap.stream()
                .map(UHCPlayer::getTeam)
                .collect(Collectors.toList());
        List<Location> locations = toSwap.stream()
                .map(p -> p.getPlayer() != null ? p.getPlayer().getLocation() : null)
                .collect(Collectors.toList());

        int n = toSwap.size();
        for (int i = 0; i < n; i++) {
            int next = (i + 1) % n;
            toSwap.get(i).forceSetTeam(teams.get(next));
            Player player = toSwap.get(i).getPlayer();
            if (player != null && locations.get(next) != null) {
                player.teleport(locations.get(next));
            }
        }

        LangManager.get().sendAll(ScenarioLang.SWITCH_SWAP_ANNOUNCE);
        for (UHCPlayer uhcPlayer : toSwap) {
            Player player = uhcPlayer.getPlayer();
            if (player == null) continue;
            String teamDisplay = uhcPlayer.getTeam().map(t -> t.prefix() + t.name()).orElse("?");
            LangManager.get().send(ScenarioLang.SWITCH_SWAP_PLAYER_INFO, player, Map.of("%team%", teamDisplay));
        }
    }

    @Override
    public void onTeamUpdate() {
        if (UHCManager.get().getTeam_size() == 1) {
            UHCManager.get().setTeam_size(2);
            LangManager.get().sendAll(CoreLang.COMMON_TEAM_REDFINIED_AUTO);
        }
    }
    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
    }
}

