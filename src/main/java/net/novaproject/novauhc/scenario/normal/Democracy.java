package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Democracy extends Scenario {

    private final Map<UUID, UUID> playerVotes = new HashMap<>(); // voter -> target
    private final Map<UUID, Integer> voteCount = new HashMap<>(); // target -> vote count
    private final Set<UUID> hasVoted = new HashSet<>();
    private final int VOTE_INTERVAL = 30 * 60; // 30 minutes in seconds
    private final int VOTE_DURATION = 120; // 2 minutes to vote
    private BukkitRunnable voteTask;
    private boolean voteActive = false;

    @Override
    public String getName() {
        return "Democracy";
    }

    @Override
    public String getDescription() {
        return "Votez pour éliminer un joueur toutes les 30 minutes !";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.PAPER);
    }

    @Override
    public void enable() {
        super.enable();
        if (isActive()) {
            startVoteTask();
        }
    }

    @Override
    public void toggleActive() {
        super.toggleActive();
        if (isActive()) {
            startVoteTask();
        } else {
            stopVoteTask();
        }
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
                    // Waiting for next vote
                    if (timer >= VOTE_INTERVAL) {
                        startVoting();
                        timer = 0;
                        voteTimer = VOTE_DURATION;
                    } else {
                        // Send warnings
                        int timeUntilVote = VOTE_INTERVAL - timer;
                        if (timeUntilVote == 300) { // 5 minutes before
                            Bukkit.broadcastMessage("§9[Democracy] §fVote démocratique dans 5 minutes !");
                        } else if (timeUntilVote == 60) { // 1 minute before
                            Bukkit.broadcastMessage("§9[Democracy] §fVote démocratique dans 1 minute !");
                        }
                    }
                } else {
                    // Vote is active
                    voteTimer--;

                    if (voteTimer <= 0) {
                        endVoting();
                    } else {
                        // Send countdown
                        if (voteTimer == 60) {
                            Bukkit.broadcastMessage("§9[Democracy] §fPlus qu'1 minute pour voter !");
                        } else if (voteTimer == 10) {
                            Bukkit.broadcastMessage("§9[Democracy] §fPlus que 10 secondes pour voter !");
                        }
                    }
                }
            }
        };

        // Run every second
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

        if (playingPlayers.size() <= 2) {
            Bukkit.broadcastMessage("§9[Democracy] §fPas assez de joueurs pour un vote !");
            voteActive = false;
            return;
        }

        Bukkit.broadcastMessage("§9§l[Democracy] §fLE VOTE DÉMOCRATIQUE COMMENCE !");
        Bukkit.broadcastMessage("§9[Democracy] §fUtilisez /vote <joueur> pour voter !");
        Bukkit.broadcastMessage("§9[Democracy] §fVous avez 2 minutes pour voter !");

        // Initialize vote counts
        for (UHCPlayer uhcPlayer : playingPlayers) {
            voteCount.put(uhcPlayer.getPlayer().getUniqueId(), 0);
        }

        // Send individual messages
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

        // Count total votes
        int totalVotes = hasVoted.size();

        if (totalVotes == 0) {
            Bukkit.broadcastMessage("§9[Democracy] §fAucun vote ! Personne n'est éliminé.");
            clearVoteData();
            return;
        }

        // Find player with most votes
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

        // Handle ties
        if (tiedPlayers.size() > 1) {
            // Random selection in case of tie
            targetToEliminate = tiedPlayers.get(new Random().nextInt(tiedPlayers.size()));
            Bukkit.broadcastMessage("§9[Democracy] §fÉgalité ! Sélection aléatoire...");
        }

        // Announce results
        Bukkit.broadcastMessage("§9§l[Democracy] §fRÉSULTATS DU VOTE :");
        for (Map.Entry<UUID, Integer> entry : voteCount.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                int votes = entry.getValue();
                Bukkit.broadcastMessage("§9[Democracy] §f" + player.getName() + ": " + votes + " vote(s)");
            }
        }

        // Eliminate the target
        if (targetToEliminate != null && maxVotes > 0) {
            Player eliminatedPlayer = Bukkit.getPlayer(targetToEliminate);
            if (eliminatedPlayer != null) {
                Bukkit.broadcastMessage("§9§l[Democracy] §f" + eliminatedPlayer.getName() +
                        " §fa été éliminé par vote démocratique !");

                // Kill the player
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

    // Method to handle vote commands (would be called from a command)
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

        // Find target player
        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            voter.sendMessage("§9[Democracy] §cJoueur introuvable !");
            return false;
        }

        UUID targetUuid = target.getUniqueId();

        // Check if target is playing
        UHCPlayer uhcTarget = UHCPlayerManager.get().getPlayer(target);
        if (uhcTarget == null || !uhcTarget.isPlaying()) {
            voter.sendMessage("§9[Democracy] §cCe joueur ne participe pas !");
            return false;
        }

        // Can't vote for yourself
        if (voterUuid.equals(targetUuid)) {
            voter.sendMessage("§9[Democracy] §cVous ne pouvez pas voter pour vous-même !");
            return false;
        }

        // Record vote
        playerVotes.put(voterUuid, targetUuid);
        hasVoted.add(voterUuid);
        voteCount.put(targetUuid, voteCount.getOrDefault(targetUuid, 0) + 1);

        voter.sendMessage("§9[Democracy] §fVous avez voté pour " + target.getName() + " !");

        // Announce vote count
        int totalVoters = UHCPlayerManager.get().getPlayingOnlineUHCPlayers().size();
        int votesReceived = hasVoted.size();

        Bukkit.broadcastMessage("§9[Democracy] §f" + voter.getName() + " a voté ! (" +
                votesReceived + "/" + totalVoters + " votes reçus)");

        return true;
    }

    // Get current vote status
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

    // Check if voting is active
    public boolean isVotingActive() {
        return voteActive;
    }

    // Force start vote (admin command)
    public void forceStartVote() {
        if (isActive() && !voteActive) {
            startVoting();
            Bukkit.broadcastMessage("§9[Democracy] §fVote forcé par un administrateur !");
        }
    }

    // Get who a player voted for
    public Player getPlayerVote(Player voter) {
        UUID targetUuid = playerVotes.get(voter.getUniqueId());
        return targetUuid != null ? Bukkit.getPlayer(targetUuid) : null;
    }
}
