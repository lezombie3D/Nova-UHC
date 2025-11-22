package net.novaproject.novauhc;


import eu.cloudnetservice.driver.document.property.DocProperty;
import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.ability.AbilityManager;
import net.novaproject.novauhc.command.CommandManager;
import net.novaproject.novauhc.listener.ListenerManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.task.ScatterTask;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.ui.config.Enchants;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.utils.Titles;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.world.utils.SimpleBorder;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class UHCManager {

    public static UHCManager get() {
        return Main.getUHCManager();
    }

    public Map<String, ItemStack[]> start = new HashMap<>();
    public static final DocProperty<String> NOVA = DocProperty.property("NovaUHC", String.class);
    private WaitState waitState = WaitState.LOBBY_STATE;
    private UHCTeamManager uhcTeamManager;
    private AbilityManager abilityManager;
    private UHCPlayerManager uhcPlayerManager;
    private GameState gameState = GameState.LOBBY;
    private ScenarioManager scenarioManager;
    private boolean spectator = false;
    private int timer = -1;
    private int slot = 25, team_size = 1;
    private int diamondArmor = 2;
    private int protectionMax = 2;
    private int dimamondLimit = 22;
    private int timerpvp = 60 * 20;
    private int timerborder = 3600;
    private double targetSize = 200.0;
    private long reducSpeed = 2;
    private double boost_Diamond = 0;
    private double boost_Gold = 0;
    private double boost_Iron = 0;
    private double boost_Lapis = 0;
    private boolean started = false;
    private boolean chatdisbale = false;
    private boolean canceled = false;

    public void setup() {

        uhcPlayerManager = new UHCPlayerManager();
        uhcTeamManager = new UHCTeamManager();
        (scenarioManager = new ScenarioManager()).setup();
        (abilityManager = new AbilityManager()).setup();
        ListenerManager.setup();
        CommandManager.get().setup();
        Bukkit.setWhitelist(true);
        Bukkit.getWhitelistedPlayers().forEach(wl -> {
            wl.setWhitelisted(false);
        });


    }

    public String getTimerFormatted() {
        int seconds = Math.max(timer, 0);
        return seconds >= 3600
                ? String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60)
                : String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    public void reset() {
        Bukkit.getServer().shutdown();
    }

    public void onStart() {
        gameState = GameState.SCATTERING;

        if (team_size != 1) {
            uhcTeamManager.fillTeams();
        }

        new BukkitRunnable() {
            private int countdown = ConfigUtils.getGeneralConfig().getInt("timer.timer_before_start");

            @Override
            public void run() {
                if (canceled) {
                    cancel();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.setLevel(0);
                    }
                    gameState = GameState.LOBBY;

                }
                for (Player p : Bukkit.getOnlinePlayers()) {
                    String title = "";
                    String subtitle = "§cAttention..";
                    switch (countdown) {
                        case 10:
                            title = "§e" + countdown;
                            new Titles().sendTitle(p, title, subtitle, 60);
                            p.playSound(p.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
                            break;
                        case 4:
                        case 5:
                            title = "§c" + countdown;
                            new Titles().sendTitle(p, title, subtitle, 60);
                            p.playSound(p.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
                            break;
                        case 3:
                            title = "§6" + countdown;
                            subtitle = "§ePlugin par lezombie3D";
                            new Titles().sendTitle(p, title, subtitle, 60);
                            p.playSound(p.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
                            break;
                        case 2:
                            title = "§e" + countdown;
                            subtitle = "§ePlugin par lezombie3D";
                            new Titles().sendTitle(p, title, subtitle, 60);
                            p.playSound(p.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
                            break;
                        case 1:
                            title = "§2" + countdown;
                            subtitle = "§ePlugin par lezombie3D";
                            new Titles().sendTitle(p, title, subtitle, 60);
                            p.playSound(p.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
                            break;
                        default:
                            break;
                    }
                    long remaining = countdown;
                    float ratio = Math.min(1f, (float) remaining / ConfigUtils.getGeneralConfig().getInt("timer.timer_before_start"));
                    p.setExp(ratio);
                    p.setLevel((int) (remaining));
                    new Titles().sendActionText(p, "§8●§fDébut de la partie dans §c" + countdown + "secondes§8●");

                }
                countdown--;
                if (countdown < 0) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        new Titles().sendTitle(p, "§6Bonne chance !", "", 60);
                        p.playSound(p.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
                    }
                    cancel();
                    new ScatterTask().runTaskTimer(Main.get(), 0, 5);
                    World world = Common.get().getArena();
                    world.setTime(1200);
                    world.setThundering(false);
                    world.setWeatherDuration(Integer.MAX_VALUE);
                }
            }
        }.runTaskTimer(Main.get(), 0, 20);
    }

    public void onSec(){

        timer++;

        if (timer == 30) {
            Bukkit.broadcastMessage(CommonString.INVULNERABLE_OFF.getMessage());
        }

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player -> {
                scenario.onSec(player.getPlayer());
            });
        });

        if (timer == timerpvp - 300) {
            Bukkit.broadcastMessage(CommonString.PVP_START_IN.getMessage().replace("%time_before%", UHCUtils.getFormattedTime(300)));

        }
        if (timer == timerpvp - 60) {
            Bukkit.broadcastMessage(CommonString.PVP_START_IN.getMessage().replace("%time_before%", UHCUtils.getFormattedTime(60)));
        }
        if (timer == timerpvp) {

            Common.get().getArena().setPVP(true);
            Bukkit.broadcastMessage(CommonString.PVP_START.getMessage());
            ScenarioManager.get().getActiveScenarios().forEach(Scenario::onPvP);

        }
        if (timer == timerborder - 6000) {
            Bukkit.broadcastMessage(CommonString.MEETUP_START_IN.getMessage().replace("%time_before%", UHCUtils.getFormattedTime(6000)));
        }
        if (timer == timerborder - 60) {
            Bukkit.broadcastMessage(CommonString.MEETUP_START_IN.getMessage().replace("%time_before%", UHCUtils.getFormattedTime(60)));
        }
        if (timer == timerborder) {
            WorldBorder border = Common.get().getArena().getWorldBorder();
            new SimpleBorder(border).startReduce(targetSize, reducSpeed);
        }

    }


    public boolean isLobby() {
        return gameState == GameState.LOBBY || gameState == GameState.SCATTERING;
    }

    public boolean isGame() {
        return gameState == GameState.INGAME;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    private void endGame() {

        StringBuilder killmessage = new StringBuilder();
        UHCManager.get().setGameState(GameState.ENDING);
        UHCPlayer player = null;
        for (UHCPlayer uhcPlayer : uhcPlayerManager.getPlayingOnlineUHCPlayers()) {
            player = uhcPlayer;
            break;
        }
        player.getPlayer().teleport(Common.get().getLobbySpawn());
        fireWork(player.getPlayer());

        if (player.getTeam().isPresent()) {
            UHCTeam team = player.getTeam().get();
            for (UHCPlayer teamMember : team.getPlayers()) {
                if (teamMember.getPlayer() != null) {

                    killmessage.append(teamMember.getPlayer().getDisplayName())
                            .append(" : ")
                            .append(teamMember.getKill())
                            .append(" kills\n");
                    Main.getDatabaseManager().addWins(teamMember.getUniqueId(), 1);
                    Main.getDatabaseManager().addCoins(teamMember.getUniqueId(), 300);
                    Main.getDatabaseManager().addPartie(player.getUniqueId(), 1);
                }
            }
        } else {
            killmessage.append(player.getPlayer().getDisplayName())
                    .append(" : ")
                    .append(player.getKill())
                    .append(" kills\n");
            Main.getDatabaseManager().addWins(player.getUniqueId(), 1);
            Main.getDatabaseManager().addCoins(player.getUniqueId(), 300);
            Main.getDatabaseManager().addPartie(player.getUniqueId(), 1);

        }

        Bukkit.broadcastMessage(ChatColor.AQUA + killmessage.toString());

        for (UHCPlayer loser : UHCPlayerManager.get().getOnlineUHCPlayers()) {
            loser.getPlayer().teleport(Common.get().getLobbySpawn());
            if (loser != player.getPlayer() && !loser.isPlaying()) {
                if (player.getTeam().isPresent()) {
                    if (player.getTeam().get().getPlayers().contains(loser)) {
                        return;
                    }
                }
                Main.getDatabaseManager().addPartie(loser.getUniqueId(), 1);
                Main.getDatabaseManager().addLose(loser.getUniqueId(), 1);
                Main.getDatabaseManager().removeCoins(loser.getUniqueId(), 50);
            }
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getServer().getPluginManager().disablePlugin(Main.get());
                Bukkit.getServer().shutdown();
            }
        }.runTaskLater(Main.get(), 20 * 60);
    }

    public void checkVictory() {
        if (gameState != GameState.INGAME) {
            return;
        }

        boolean win = false;

        List<UHCTeam> aliveTeams = uhcTeamManager.getAliveTeams();
        List<UHCPlayer> alivePlayers = uhcPlayerManager.getPlayingOnlineUHCPlayers();

        List<UHCPlayer> soloPlayers = alivePlayers.stream()
                .filter(p -> !p.getTeam().isPresent())
                .collect(Collectors.toList());

        if (aliveTeams.size() == 1 && soloPlayers.isEmpty()) {

            UHCTeam team = aliveTeams.get(0);
            String winner = team.name();
            String teamMembers = team.getPlayers().stream()
                    .map(p -> p.getPlayer().getName())
                    .collect(Collectors.joining(", "));

            Bukkit.broadcastMessage(ChatColor.GOLD + " Félicitations à l'équipe " + winner + " : " + teamMembers);
            win = true;

        } else if (aliveTeams.isEmpty() && soloPlayers.size() == 1) {

            UHCPlayer player = soloPlayers.get(0);
            Bukkit.broadcastMessage(ChatColor.GOLD + " Félicitations au joueur : " + player.getPlayer().getDisplayName());
            win = true;
        }

        for (Scenario scenario : ScenarioManager.get().getActiveScenarios()) {
            if (scenario.isWin()) {
                win = true;
            }
        }

        if (win) {
            endGame();
        }
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

    public void setSlot(int slot) {
        if (slot < 1)
            slot = 1;
        this.slot = slot;
    }

    public void setTeam_size(int team_size) {
        this.team_size = team_size;

        if (team_size < 1)
            team_size = 1;

        if (team_size == 1) {
            uhcTeamManager.deleteTeams();
        } else {
            uhcTeamManager.createTeams();
        }
    }



    public void applyLimitsFromList(List<Integer> limites) {
        if (limites == null || limites.size() < Enchants.values().length) return;
        for (int i = 0; i < Enchants.values().length; i++) {
            Enchants.getEnchant(i).setConfigValue(limites.get(i));
        }
    }

    public enum GameState {
        LOBBY, SCATTERING, INGAME, ENDING
    }

    public enum WaitState {
        WAIT_STATE, LOBBY_STATE
    }

}
