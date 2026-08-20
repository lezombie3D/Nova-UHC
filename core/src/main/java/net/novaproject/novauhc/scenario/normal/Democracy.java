package net.novaproject.novauhc.scenario.normal;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.command.CommandManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioLang;

public class Democracy extends Scenario {

    @Override
    public Family getFamily() { return Family.LOOT; }

    private final Map<UUID, UUID> playerVotes = new HashMap<>();
    private final Map<UUID, Integer> voteCount = new HashMap<>();
    private final Set<UUID> hasVoted = new HashSet<>();

    private BukkitRunnable voteTask;
    private boolean voteActive = false;

    @Var(lang = ScenarioVarLang.class, nameKey = "DEMOCRACY_VAR_VOTE_INTERVAL_NAME", descKey = "DEMOCRACY_VAR_VOTE_INTERVAL_DESC", type = VariableType.TIME)
    private int voteInterval = 1800;

    @Var(lang = ScenarioVarLang.class, nameKey = "DEMOCRACY_VAR_VOTE_DURATION_NAME", descKey = "DEMOCRACY_VAR_VOTE_DURATION_DESC", type = VariableType.TIME)
    private int voteDuration = 120;

    @Var(lang = ScenarioVarLang.class, nameKey = "DEMOCRACY_VAR_WARNING5MIN_NAME", descKey = "DEMOCRACY_VAR_WARNING5MIN_DESC", type = VariableType.BOOLEAN)
    private boolean warning5Min = true;

    @Var(lang = ScenarioVarLang.class, nameKey = "DEMOCRACY_VAR_WARNING1MIN_NAME", descKey = "DEMOCRACY_VAR_WARNING1MIN_DESC", type = VariableType.BOOLEAN)
    private boolean warning1Min = true;

    @Var(lang = ScenarioVarLang.class, nameKey = "DEMOCRACY_VAR_WARNING1MIN_VOTE_NAME", descKey = "DEMOCRACY_VAR_WARNING1MIN_VOTE_DESC", type = VariableType.BOOLEAN)
    private boolean warning1MinVote = true;

    @Var(lang = ScenarioVarLang.class, nameKey = "DEMOCRACY_VAR_WARNING10SEC_VOTE_NAME", descKey = "DEMOCRACY_VAR_WARNING10SEC_VOTE_DESC", type = VariableType.BOOLEAN)
    private boolean warning10SecVote = true;

    @Var(lang = ScenarioVarLang.class, nameKey = "DEMOCRACY_VAR_MIN_PLAYERS_NAME", descKey = "DEMOCRACY_VAR_MIN_PLAYERS_DESC", type = VariableType.INTEGER)
    private int minPlayers = 3;

    @Override
    public String getName() {
        return "Democracy";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.DEMOCRACY, player)
                .replace("%minutes%", String.valueOf(voteInterval / 60));
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.PAPER);
    }

    @Override
    public void onGameStart() {
        CommandManager.get().register("vote",new VoteCMD(),"v");
        startVoteTask();
    }

    private void startVoteTask() {
        if (voteTask != null) {
            voteTask.cancel();
        }

        voteTask = new BukkitRunnable() {
            private int timer = 0;
            private int voteTimer = 0;

            @Override
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }

                timer++;

                if (!voteActive) {
                    if (timer >= voteInterval) {
                        startVoting();
                        timer = 0;
                        voteTimer = voteDuration;
                    } else {
                        int timeUntilVote = voteInterval - timer;
                        if (warning5Min && timeUntilVote == 300) {
                            LangManager.get().sendAll(ScenarioLang.DEMOCRACY_VOTE_IN_FIVE_MINUTES);
                        } else if (warning1Min && timeUntilVote == 60) {
                            LangManager.get().sendAll(ScenarioLang.DEMOCRACY_VOTE_IN_ONE_MINUTE);
                        }
                    }
                } else {
                    voteTimer--;

                    if (voteTimer <= 0) {
                        endVoting();
                    } else {
                        if (warning1MinVote && voteTimer == 60) {
                            LangManager.get().sendAll(ScenarioLang.DEMOCRACY_ONE_MINUTE_LEFT);
                        } else if (warning10SecVote && voteTimer == 10) {
                            LangManager.get().sendAll(ScenarioLang.DEMOCRACY_TEN_SECONDS_LEFT);
                        }
                    }
                }
            }
        };

        voteTask.runTaskTimer(Main.get(), 0, 20);
    }

    private void stopVoteTask() {
        if (voteTask != null) {
            voteTask.cancel();
            voteTask = null;
        }
        voteActive = false;
        clearVoteData();
    }

    private void startVoting() {
        voteActive = true;
        clearVoteData();

        List<UHCPlayer> playingPlayers = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();

        if (playingPlayers.size() < minPlayers) {
            LangManager.get().sendAll(ScenarioLang.DEMOCRACY_NOT_ENOUGH_PLAYERS);
            voteActive = false;
            return;
        }

        LangManager.get().sendAll(ScenarioLang.DEMOCRACY_VOTE_STARTS);
        LangManager.get().sendAll(ScenarioLang.DEMOCRACY_USE_VOTE_COMMAND);
        LangManager.get().sendAll(ScenarioLang.DEMOCRACY_VOTE_DURATION, Map.of("%minutes%", String.valueOf(voteDuration / 60)));

        for (UHCPlayer uhcPlayer : playingPlayers) {
            voteCount.put(uhcPlayer.getPlayer().getUniqueId(), 0);
        }

        for (UHCPlayer uhcPlayer : playingPlayers) {
            Player player = uhcPlayer.getPlayer();
            LangManager.get().send(ScenarioLang.DEMOCRACY_AVAILABLE_PLAYERS, player);
            for (UHCPlayer target : playingPlayers) {
                if (!target.equals(uhcPlayer)) {
                    LangManager.get().send(ScenarioLang.DEMOCRACY_PLAYER_ENTRY, player, Map.of("%player%", target.getPlayer().getName()));
                }
            }
        }
    }

    private void endVoting() {
        voteActive = false;
        List<UHCPlayer> playingPlayers = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();

        int totalVotes = hasVoted.size();
        if (totalVotes == 0) {
            LangManager.get().sendAll(ScenarioLang.DEMOCRACY_NO_VOTES);
            clearVoteData();
            return;
        }

        UUID targetToEliminate = null;
        int maxVotes = 0;
        List<UUID> tiedPlayers = new ArrayList<>();

        for (Map.Entry<UUID, Integer> entry : voteCount.entrySet()) {
            int votes = entry.getValue();
            if (votes > maxVotes) {
                maxVotes = votes;
                targetToEliminate = entry.getKey();
                tiedPlayers.clear();
                tiedPlayers.add(targetToEliminate);
            } else if (votes == maxVotes && votes > 0) {
                tiedPlayers.add(entry.getKey());
            }
        }

        if (tiedPlayers.size() > 1) {
            targetToEliminate = tiedPlayers.get(new Random().nextInt(tiedPlayers.size()));
            LangManager.get().sendAll(ScenarioLang.DEMOCRACY_TIE_RANDOM);
        }

        LangManager.get().sendAll(ScenarioLang.DEMOCRACY_VOTE_RESULTS);
        for (Map.Entry<UUID, Integer> entry : voteCount.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                int votes = entry.getValue();
                LangManager.get().sendAll(ScenarioLang.DEMOCRACY_VOTE_RESULT_LINE, Map.of("%player%", player.getName(), "%votes%", String.valueOf(votes)));
            }
        }

        if (targetToEliminate != null && maxVotes > 0) {
            Player eliminatedPlayer = Bukkit.getPlayer(targetToEliminate);
            if (eliminatedPlayer != null) {
                LangManager.get().sendAll(ScenarioLang.DEMOCRACY_PLAYER_ELIMINATED, Map.of("%player%", eliminatedPlayer.getName()));
                eliminatedPlayer.setHealth(0);
                LangManager.get().send(ScenarioLang.DEMOCRACY_YOU_ELIMINATED, eliminatedPlayer);
            }
        } else {
            LangManager.get().sendAll(ScenarioLang.DEMOCRACY_NOT_ENOUGH_VOTES);
        }

        clearVoteData();
    }

    private void clearVoteData() {
        playerVotes.clear();
        voteCount.clear();
        hasVoted.clear();
    }

    public boolean vote(Player voter, String targetName) {
        if (!isActive() || !voteActive) {
            LangManager.get().send(ScenarioLang.DEMOCRACY_NO_VOTE_ACTIVE, voter);
            return false;
        }

        UUID voterUuid = voter.getUniqueId();

        if (hasVoted.contains(voterUuid)) {
            LangManager.get().send(ScenarioLang.DEMOCRACY_ALREADY_VOTED, voter);
            return false;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            LangManager.get().send(ScenarioLang.DEMOCRACY_PLAYER_NOT_FOUND, voter);
            return false;
        }

        UUID targetUuid = target.getUniqueId();
        UHCPlayer uhcTarget = UHCPlayerManager.get().getPlayer(target);
        if (uhcTarget == null || !uhcTarget.isPlaying()) {
            LangManager.get().send(ScenarioLang.DEMOCRACY_NOT_PARTICIPATING, voter);
            return false;
        }

        if (voterUuid.equals(targetUuid)) {
            LangManager.get().send(ScenarioLang.DEMOCRACY_CANNOT_VOTE_SELF, voter);
            return false;
        }

        playerVotes.put(voterUuid, targetUuid);
        hasVoted.add(voterUuid);
        voteCount.put(targetUuid, voteCount.getOrDefault(targetUuid, 0) + 1);

        LangManager.get().send(ScenarioLang.DEMOCRACY_VOTE_CAST, voter, Map.of("%target%", target.getName()));
        int totalVoters = UHCPlayerManager.get().getPlayingOnlineUHCPlayers().size();
        int votesReceived = hasVoted.size();
        LangManager.get().sendAll(ScenarioLang.DEMOCRACY_VOTE_BROADCAST, Map.of("%voter%", voter.getName(), "%current%", String.valueOf(votesReceived), "%total%", String.valueOf(totalVoters)));

        return true;
    }


    public static class VoteCMD extends Command {

        @Override
        public void execute(CommandArguments arguments) {
            if (!(arguments.getSender() instanceof Player player)) return;
            if (arguments.size() < 2) {
                LangManager.get().send(CoreLang.COMMON_MSG_USAGE, player);
                return;
            }

            Player target = Bukkit.getPlayerExact(arguments.getArgument(0));
            if (target == null || !target.isOnline() || target == player) return;

            Democracy democracy = UHCManager.get().getScenarioManager().getScenario(Democracy.class);
            if (democracy == null || !democracy.isActive()) return;

            democracy.vote(player, target.getName());
        }
    }
}
