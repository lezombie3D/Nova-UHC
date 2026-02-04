package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;

import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.scenario.lang.lang.DemocracyLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Democracy extends Scenario {

    private final Map<UUID, UUID> playerVotes = new HashMap<>();
    private final Map<UUID, Integer> voteCount = new HashMap<>();
    private final Set<UUID> hasVoted = new HashSet<>();

    private BukkitRunnable voteTask;
    private boolean voteActive = false;

    // --- Scenario Variables ---
    @ScenarioVariable(
            name = "vote_interval",
            description = "Intervalle entre chaque vote en secondes",
            type = VariableType.TIME
    )
    private int voteInterval = 1800; // 30 minutes

    @ScenarioVariable(
            name = "vote_duration",
            description = "Durée d'un vote actif en secondes",
            type = VariableType.TIME
    )
    private int voteDuration = 120; // 2 minutes

    @ScenarioVariable(
            name = "warning_5min",
            description = "Activer avertissement 5 minutes avant vote",
            type = VariableType.BOOLEAN
    )
    private boolean warning5Min = true;

    @ScenarioVariable(
            name = "warning_1min",
            description = "Activer avertissement 1 minute avant vote",
            type = VariableType.BOOLEAN
    )
    private boolean warning1Min = true;

    @ScenarioVariable(
            name = "warning_1min_vote",
            description = "Activer avertissement 1 minute pendant le vote",
            type = VariableType.BOOLEAN
    )
    private boolean warning1MinVote = true;

    @ScenarioVariable(
            name = "warning_10sec_vote",
            description = "Activer avertissement 10 secondes pendant le vote",
            type = VariableType.BOOLEAN
    )
    private boolean warning10SecVote = true;

    @ScenarioVariable(
            name = "min_players",
            description = "Nombre minimum de joueurs pour que le vote commence",
            type = VariableType.INTEGER
    )
    private int minPlayers = 3;

    @Override
    public String getName() {
        return "Democracy";
    }

    @Override
    public String getDescription() {
        return "Votez pour éliminer un joueur toutes les " + (voteInterval / 60) + " minutes !";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.PAPER);
    }

    @Override
    public String getPath() {
        return "democracy";
    }

    @Override
    public ScenarioLang[] getLang() {
        return DemocracyLang.values();
    }

    @Override
    public void onGameStart() {
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
                            Bukkit.broadcastMessage("§9[Democracy] §fVote démocratique dans 5 minutes !");
                        } else if (warning1Min && timeUntilVote == 60) {
                            Bukkit.broadcastMessage("§9[Democracy] §fVote démocratique dans 1 minute !");
                        }
                    }
                } else {
                    voteTimer--;

                    if (voteTimer <= 0) {
                        endVoting();
                    } else {
                        if (warning1MinVote && voteTimer == 60) {
                            Bukkit.broadcastMessage("§9[Democracy] §fPlus qu'1 minute pour voter !");
                        } else if (warning10SecVote && voteTimer == 10) {
                            Bukkit.broadcastMessage("§9[Democracy] §fPlus que 10 secondes pour voter !");
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
            Bukkit.broadcastMessage("§9[Democracy] §fPas assez de joueurs pour un vote !");
            voteActive = false;
            return;
        }

        Bukkit.broadcastMessage("§9§l[Democracy] §fLE VOTE DÉMOCRATIQUE COMMENCE !");
        Bukkit.broadcastMessage("§9[Democracy] §fUtilisez /vote <joueur> pour voter !");
        Bukkit.broadcastMessage("§9[Democracy] §fVous avez " + (voteDuration / 60) + " minutes pour voter !");

        for (UHCPlayer uhcPlayer : playingPlayers) {
            voteCount.put(uhcPlayer.getPlayer().getUniqueId(), 0);
        }

        for (UHCPlayer uhcPlayer : playingPlayers) {
            Player player = uhcPlayer.getPlayer();
            player.sendMessage("§9[Democracy] §fJoueurs disponibles :");
            for (UHCPlayer target : playingPlayers) {
                if (!target.equals(uhcPlayer)) {
                    player.sendMessage("§9[Democracy] §f- " + target.getPlayer().getName());
                }
            }
        }
    }

    private void endVoting() {
        voteActive = false;
        List<UHCPlayer> playingPlayers = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();

        int totalVotes = hasVoted.size();
        if (totalVotes == 0) {
            Bukkit.broadcastMessage("§9[Democracy] §fAucun vote ! Personne n'est éliminé.");
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
            Bukkit.broadcastMessage("§9[Democracy] §fÉgalité ! Sélection aléatoire...");
        }

        Bukkit.broadcastMessage("§9§l[Democracy] §fRÉSULTATS DU VOTE :");
        for (Map.Entry<UUID, Integer> entry : voteCount.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                int votes = entry.getValue();
                Bukkit.broadcastMessage("§9[Democracy] §f" + player.getName() + ": " + votes + " vote(s)");
            }
        }

        if (targetToEliminate != null && maxVotes > 0) {
            Player eliminatedPlayer = Bukkit.getPlayer(targetToEliminate);
            if (eliminatedPlayer != null) {
                Bukkit.broadcastMessage("§9§l[Democracy] §f" + eliminatedPlayer.getName() +
                        " §fa été éliminé par vote démocratique !");
                eliminatedPlayer.setHealth(0);
                eliminatedPlayer.sendMessage("§9[Democracy] §cVous avez été éliminé par le vote du peuple !");
            }
        } else {
            Bukkit.broadcastMessage("§9[Democracy] §fAucun joueur n'a reçu assez de votes pour être éliminé !");
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
            voter.sendMessage("§9[Democracy] §cAucun vote en cours !");
            return false;
        }

        UUID voterUuid = voter.getUniqueId();

        if (hasVoted.contains(voterUuid)) {
            voter.sendMessage("§9[Democracy] §cVous avez déjà voté !");
            return false;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            voter.sendMessage("§9[Democracy] §cJoueur introuvable !");
            return false;
        }

        UUID targetUuid = target.getUniqueId();
        UHCPlayer uhcTarget = UHCPlayerManager.get().getPlayer(target);
        if (uhcTarget == null || !uhcTarget.isPlaying()) {
            voter.sendMessage("§9[Democracy] §cCe joueur ne participe pas !");
            return false;
        }

        if (voterUuid.equals(targetUuid)) {
            voter.sendMessage("§9[Democracy] §cVous ne pouvez pas voter pour vous-même !");
            return false;
        }

        playerVotes.put(voterUuid, targetUuid);
        hasVoted.add(voterUuid);
        voteCount.put(targetUuid, voteCount.getOrDefault(targetUuid, 0) + 1);

        voter.sendMessage("§9[Democracy] §fVous avez voté pour " + target.getName() + " !");
        int totalVoters = UHCPlayerManager.get().getPlayingOnlineUHCPlayers().size();
        int votesReceived = hasVoted.size();
        Bukkit.broadcastMessage("§9[Democracy] §f" + voter.getName() + " a voté ! (" +
                votesReceived + "/" + totalVoters + " votes reçus)");

        return true;
    }

    public String getVoteStatus() {
        if (!voteActive) {
            return "§9[Democracy] §fAucun vote en cours.";
        }

        StringBuilder status = new StringBuilder();
        status.append("§9[Democracy] §fVotes actuels :\n");

        for (Map.Entry<UUID, Integer> entry : voteCount.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                status.append("§9[Democracy] §f").append(player.getName())
                        .append(": ").append(entry.getValue()).append(" vote(s)\n");
            }
        }

        return status.toString();
    }

}
