package net.novaproject.ultimate.teamswapper;
import net.novaproject.novauhc.utils.variable.Var;

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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.*;
import net.novaproject.novauhc.lang.lang.ScenarioLang;

public class TeamSwapperClassic extends Scenario {

    @Var(name = "Maximum lives", desc = "Maximum number of lives a player can have.", type = VariableType.INTEGER)
    private int maxLives = 3;

    @Var(name = "Initial lives", desc = "Number of lives each player starts with.", type = VariableType.INTEGER)
    private int initialLives = 2;

    @Var(name = "Number of Winners", desc = "Number of players (by seniority) who win at the end, among the surviving team.", type = VariableType.INTEGER)
    private int numberOfWinners = 3;

    private final Map<UUID, Integer> playerLives = new HashMap<>();
    private final Map<UUID, Long> teamJoinTime = new HashMap<>();
    private boolean winnersPrinted = false;

    @Override
    public String getName() { return "TeamSwapper Classic"; }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.TEAM_SWAPPER_CLASSIC, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.NAME_TAG);
    }

    @Override
    public void onGameStart() {
        Bukkit.broadcastMessage("Mode de jeu recréer a partir de la documentation publique d'Eterny");
        long now = System.currentTimeMillis();
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(uhcPlayer -> {
            playerLives.put(uhcPlayer.getUniqueId(), initialLives);
            teamJoinTime.put(uhcPlayer.getUniqueId(), now);
        });
        winnersPrinted = false;

        UHCPlayerManager.setCustomScoreboardLines("teamswapper_classic", p -> {
            UHCPlayer up = UHCPlayerManager.get().getPlayer(p);
            if (up == null || !up.isPlaying()) return Collections.emptyList();
            int lives = playerLives.getOrDefault(p.getUniqueId(), initialLives);
            return Collections.singletonList("§f  §8● §fVies: §c" + lives + " §7/ §c" + maxLives);
        });
    }

    @Override
    public void onStop() {
        UHCPlayerManager.setCustomScoreboardLines("teamswapper_classic", null);
        playerLives.clear();
        teamJoinTime.clear();
        winnersPrinted = false;
    }

    @Override
    public boolean overridesVictory() {
        return isActive();
    }

    @Override
    public boolean isWin() {
        List<UHCTeam> aliveTeams = UHCTeamManager.get().getAliveTeams();
        if (aliveTeams.size() != 1) return false;
        if (!winnersPrinted) {
            printVainqueurs(aliveTeams.get(0));
            winnersPrinted = true;
        }
        return true;
    }

    private void printVainqueurs(UHCTeam team) {
        List<UHCPlayer> survivors = new java.util.ArrayList<>(team.getPlayers().stream()
                .filter(UHCPlayer::isPlaying).toList());
        long now = System.currentTimeMillis();
        survivors.sort(java.util.Comparator.comparingLong(p -> teamJoinTime.getOrDefault(p.getUniqueId(), now)));

        Bukkit.broadcastMessage(LangManager.get().get(ScenarioLang.TEAMSWAPPERV3_VAINQUEURS_HEADER));
        int rank = 1;
        for (UHCPlayer p : survivors) {
            long ancienneSec = (now - teamJoinTime.getOrDefault(p.getUniqueId(), now)) / 1000;
            String prefix = rank <= numberOfWinners ? "§6⭐ " : "  ";
            Bukkit.broadcastMessage(LangManager.get().get(
                    ScenarioLang.TEAMSWAPPERV3_VAINQUEURS_LINE,
                    Map.of(
                            "%prefix%", prefix,
                            "%rank%", String.valueOf(rank),
                            "%player%", p.getPlayer() != null ? p.getPlayer().getName() : "?",
                            "%anc%", String.valueOf(ancienneSec)
                    )
            ));
            rank++;
        }
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event) {
        if (!isActive()) return;
        if (!(entity instanceof Player victim)) return;

        Player killer = null;
        if (dammager instanceof Player) {
            killer = (Player) dammager;
        } else if (dammager instanceof Projectile) {
            Object shooter = ((Projectile) dammager).getShooter();
            if (shooter instanceof Player) killer = (Player) shooter;
        }
        if (killer == null || killer.equals(victim)) return;

        if (victim.getHealth() - event.getFinalDamage() > 0) return;

        UHCPlayer uhcVictim = UHCPlayerManager.get().getPlayer(victim);
        UHCPlayer uhcKiller = UHCPlayerManager.get().getPlayer(killer);
        if (uhcVictim == null || !uhcVictim.isPlaying()) return;
        if (uhcKiller == null || !uhcKiller.isPlaying()) return;

        int lives = playerLives.getOrDefault(victim.getUniqueId(), initialLives);
        if (lives <= 1) return;

        event.setCancelled(true);
        playerLives.put(victim.getUniqueId(), lives - 1);
        victim.setHealth(victim.getMaxHealth());

        Optional<UHCTeam> killerTeam = uhcKiller.getTeam();
        uhcVictim.forceSetTeam(killerTeam);
        teamJoinTime.put(victim.getUniqueId(), System.currentTimeMillis());

        int killerLives = playerLives.getOrDefault(killer.getUniqueId(), initialLives);
        int newKillerLives = Math.min(killerLives + 1, maxLives);
        playerLives.put(killer.getUniqueId(), newKillerLives);

        String teamDisplay = killerTeam.map(t -> t.prefix() + t.name()).orElse("§f" + killer.getName());
        LangManager.get().sendAll(ScenarioLang.TEAMSWAPPERV3_TRANSFER,
                Map.of("%victim%", victim.getName(), "%killer%", killer.getName(), "%team%", teamDisplay));
        LangManager.get().send(ScenarioLang.TEAMSWAPPERV3_LIVES_LEFT, victim,
                Map.of("%lives%", String.valueOf(lives - 1), "%max%", String.valueOf(maxLives)));
        LangManager.get().send(ScenarioLang.TEAMSWAPPERV3_LIVES_GAINED, killer,
                Map.of("%lives%", String.valueOf(newKillerLives)));
        UHCManager.get().checkVictory();
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

