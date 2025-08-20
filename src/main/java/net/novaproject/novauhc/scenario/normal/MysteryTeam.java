package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MysteryTeam extends Scenario {

    private final Map<UUID, UHCTeam> playerMysteryTeams = new HashMap<>();
    private final Map<UHCTeam, List<UUID>> mysteryTeamMembers = new HashMap<>();
    private final int REVEAL_TIME = 30 * 60; // 30 minutes in seconds
    private boolean teamsRevealed = false;

    @Override
    public String getName() {
        return "MysteryTeam";
    }

    @Override
    public String getDescription() {
        return "Les équipes sont inconnues jusqu'à se rencontrer et comparer les bannières !";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BANNER);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }


    @Override
    public void toggleActive() {
        super.toggleActive();
        if (isActive()) {
            setupMysteryTeams();
        } else {
            revealAllTeams();
        }
    }

    @Override
    public void onStart(Player player) {
        if (!isActive()) return;

        // Give player their mystery team banner
        UHCTeam mysteryTeam = playerMysteryTeams.get(player.getUniqueId());
        if (mysteryTeam != null) {
            ItemStack mysteryBanner = mysteryTeam.getItem();
            mysteryBanner.setAmount(1);
            player.getInventory().addItem(mysteryBanner);

            player.sendMessage("§5[MysteryTeam] §fVous avez reçu votre bannière mystère !");
            player.sendMessage("§5[MysteryTeam] §fTrouvez d'autres joueurs avec la même bannière !");
        }
    }

    @Override
    public void onSec(Player p) {
        if (!isActive()) return;

        int currentTime = UHCManager.get().getTimer();

        if (!teamsRevealed && currentTime >= REVEAL_TIME) {
            revealAllTeams();
        }

        sendRevealWarnings(currentTime);
    }

    private void setupMysteryTeams() {
        List<UHCPlayer> players = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();

        if (players.isEmpty()) {
            Bukkit.broadcastMessage("§5[MysteryTeam] §cAucun joueur trouvé !");
            return;
        }

        // Clear existing data
        playerMysteryTeams.clear();
        mysteryTeamMembers.clear();

        List<UHCTeam> availableTeams = new ArrayList<>(UHCTeamManager.get().getTeams());

        if (availableTeams.isEmpty()) {
            Bukkit.broadcastMessage("§5[MysteryTeam] §cAucune équipe configurée ! Créez d'abord des équipes.");
            return;
        }

        int teamSize = UHCManager.get().getTeam_size();
        if (teamSize <= 1) teamSize = 2; // Minimum team size for mystery teams

        Collections.shuffle(players);

        // Assign teams to players
        int teamIndex = 0;
        for (int i = 0; i < players.size(); i++) {
            UHCPlayer uhcPlayer = players.get(i);
            UUID playerUuid = uhcPlayer.getPlayer().getUniqueId();

            UHCTeam mysteryTeam = availableTeams.get(teamIndex % availableTeams.size());

            playerMysteryTeams.put(playerUuid, mysteryTeam);

            mysteryTeamMembers.computeIfAbsent(mysteryTeam, k -> new ArrayList<>()).add(playerUuid);

            if ((i + 1) % teamSize == 0) {
                teamIndex++;
            }
        }

        Bukkit.broadcastMessage("§5§l[MysteryTeam] §fLes équipes mystères ont été assignées !");
        Bukkit.broadcastMessage("§5[MysteryTeam] §fTrouvez vos coéquipiers en comparant vos bannières !");

        // Give mystery banners to all players
        for (UHCPlayer uhcPlayer : players) {
            onStart(uhcPlayer.getPlayer());
        }

        startMysteryTask();
    }

    private void startMysteryTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }
                checkNearbyTeammates();
            }
        }.runTaskTimer(Main.get(), 0, 100); // Check every 5 seconds
    }

    private void checkNearbyTeammates() {
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            UUID playerUuid = player.getUniqueId();
            UHCTeam playerTeam = playerMysteryTeams.get(playerUuid);

            if (playerTeam == null) continue;

            for (UHCPlayer otherUhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player otherPlayer = otherUhcPlayer.getPlayer();
                UUID otherUuid = otherPlayer.getUniqueId();

                if (playerUuid.equals(otherUuid)) continue;

                UHCTeam otherTeam = playerMysteryTeams.get(otherUuid);
                if (otherTeam != null && otherTeam.equals(playerTeam)) {
                    if (player.getLocation().distance(otherPlayer.getLocation()) <= 10) {
                        revealTeammate(player, otherPlayer, playerTeam);
                    }
                }
            }
        }
    }

    private void revealTeammate(Player player1, Player player2, UHCTeam mysteryTeam) {
        String teamName = mysteryTeam.getName();

        player1.sendMessage("§5[MysteryTeam] §f" + player2.getName() +
                " §fest votre coéquipier ! (Équipe " + teamName + ")");
        player2.sendMessage("§5[MysteryTeam] §f" + player1.getName() +
                " §fest votre coéquipier ! (Équipe " + teamName + ")");

        // Play discovery sound
        player1.getWorld().playSound(player1.getLocation(), org.bukkit.Sound.ORB_PICKUP, 1.0f, 1.5f);
        player2.getWorld().playSound(player2.getLocation(), org.bukkit.Sound.ORB_PICKUP, 1.0f, 1.5f);
    }

    private void sendRevealWarnings(int currentTime) {
        int timeUntilReveal = REVEAL_TIME - currentTime;

        if (timeUntilReveal == 300) {
            Bukkit.broadcastMessage("§5[MysteryTeam] §fRévélation des équipes dans 5 minutes !");
        } else if (timeUntilReveal == 60) {
            Bukkit.broadcastMessage("§5[MysteryTeam] §fRévélation des équipes dans 1 minute !");
        } else if (timeUntilReveal == 10) {
            Bukkit.broadcastMessage("§5[MysteryTeam] §fRévélation dans 10 secondes !");
        }
    }

    private void revealAllTeams() {
        if (teamsRevealed) return;

        teamsRevealed = true;

        Bukkit.broadcastMessage("§5§l[MysteryTeam] §fTOUTES LES ÉQUIPES SONT RÉVÉLÉES !");

        // Announce all teams
        for (Map.Entry<UHCTeam, List<UUID>> entry : mysteryTeamMembers.entrySet()) {
            UHCTeam team = entry.getKey();
            List<UUID> teamMembers = entry.getValue();

            if (teamMembers.size() > 1) {
                StringBuilder teamMessage = new StringBuilder();
                teamMessage.append("§5[MysteryTeam] §fÉquipe ").append(team.getName()).append(": ");

                for (int i = 0; i < teamMembers.size(); i++) {
                    Player player = Bukkit.getPlayer(teamMembers.get(i));
                    if (player != null) {
                        if (i > 0) teamMessage.append(", ");
                        teamMessage.append("§f").append(player.getName());
                    }
                }

                Bukkit.broadcastMessage(teamMessage.toString());
            }
        }

        updateUHCTeams();
    }

    private void updateUHCTeams() {
        for (Map.Entry<UHCTeam, List<UUID>> entry : mysteryTeamMembers.entrySet()) {
            UHCTeam mysteryTeam = entry.getKey();
            List<UUID> teamMembers = entry.getValue();

            if (teamMembers.size() > 1) {
                for (UUID memberUuid : teamMembers) {
                    UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(Bukkit.getPlayer(memberUuid));
                    if (uhcPlayer != null) {
                        uhcPlayer.setTeam(java.util.Optional.of(mysteryTeam));
                    }
                }
            }
        }
    }


    public UHCTeam getPlayerMysteryTeam(Player player) {
        return playerMysteryTeams.get(player.getUniqueId());
    }

    public List<Player> getTeammates(Player player) {
        UHCTeam playerTeam = playerMysteryTeams.get(player.getUniqueId());
        if (playerTeam == null) return new ArrayList<>();

        List<Player> teammates = new ArrayList<>();
        List<UUID> teamMembers = mysteryTeamMembers.get(playerTeam);

        if (teamMembers != null) {
            for (UUID memberUuid : teamMembers) {
                if (!memberUuid.equals(player.getUniqueId())) {
                    Player teammate = Bukkit.getPlayer(memberUuid);
                    if (teammate != null) {
                        teammates.add(teammate);
                    }
                }
            }
        }

        return teammates;
    }

    public boolean areTeamsRevealed() {
        return teamsRevealed;
    }

    public void forceRevealTeams() {
        if (isActive() && !teamsRevealed) {
            revealAllTeams();
            Bukkit.broadcastMessage("§5[MysteryTeam] §fÉquipes révélées par un administrateur !");
        }
    }

    public int getTimeUntilReveal() {
        if (teamsRevealed) return 0;

        int currentTime = UHCManager.get().getTimer();
        return Math.max(0, REVEAL_TIME - currentTime);
    }

    public boolean areSameTeam(Player player1, Player player2) {
        UHCTeam team1 = playerMysteryTeams.get(player1.getUniqueId());
        UHCTeam team2 = playerMysteryTeams.get(player2.getUniqueId());

        return team1 != null && team1.equals(team2);
    }
}
