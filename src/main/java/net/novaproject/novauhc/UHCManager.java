package net.novaproject.novauhc;


import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.command.CommandManager;
import net.novaproject.novauhc.listener.ListenerManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.task.ScatterTask;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.utils.Titles;
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
import java.util.Optional;

@Getter
@Setter
public class UHCManager {

    public static UHCManager get() {
        return Main.getUHCManager();
    }

    public Map<String, ItemStack[]> start = new HashMap<>();
    private WaitState waitState = WaitState.LOBBY_STATE;
    private UHCTeamManager uhcTeamManager;
    private UHCPlayerManager uhcPlayerManager;
    private GameState gameState = GameState.LOBBY;
    private ScenarioManager scenarioManager;
    private boolean spectator = false;
    private int timer = -1;
    private int slot = 25, team_size = 1;
    private int diamondArmor = 4;
    private int protectionMax = 2;
    private int protection = 4;
    private int fireProtection = 4;
    private int featherFalling = 4;
    private int blastProtection = 4;
    private int projectileProtection = 4;
    private int respiration = 3;
    private int aquaAffinity = 1;
    private int thorns = 4;
    private int depthStrider = 4;
    private int sharpness = 5;
    private int smite = 5;
    private int baneOfArthropods = 5;
    private int knockback = 2;
    private int fireAspect = 1;
    private int looting = 3;
    private int efficiency = 5;
    private int silkTouch = 1;
    private int unbreaking = 3;
    private int fortune = 3;
    private int power = 5;
    private int punch = 2;
    private int flame = 1;
    private int infinity = 1;
    private int luckOfTheSea = 3;
    private int lure = 3;
    private int dimamondLimit = 22;
    private int timerpvp = 60;
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
        ListenerManager.setup();
        CommandManager.setup();
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
                            break;
                        case 5:
                            title = "§c" + countdown;
                            new Titles().sendTitle(p, title, subtitle, 60);
                            break;
                        case 3:
                            title = "§6" + countdown;
                            subtitle = "§ePlugin par lezombie3D";
                            new Titles().sendTitle(p, title, subtitle, 60);
                            break;
                        case 2:
                            title = "§e" + countdown;
                            subtitle = "§ePlugin par lezombie3D";
                            new Titles().sendTitle(p, title, subtitle, 60);
                            break;
                        case 1:
                            title = "§2" + countdown;
                            subtitle = "§ePlugin par lezombie3D";
                            new Titles().sendTitle(p, title, subtitle, 60);
                            break;
                        default:
                            break;
                    }
                    p.setLevel(countdown);
                    new Titles().sendActionText(p, "§8●§fDébut de la partie dans §c" + countdown + "secondes§8●");
                    p.playSound(p.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
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
            Bukkit.broadcastMessage("§aVous prenez désormais des dégâts !");
        }

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player -> {
                scenario.onSec(player.getPlayer());
            });
        });

        if (timer == timerpvp - 300) {
            Bukkit.broadcastMessage("§aLe PvP seras activé dans 5min");

        }
        if (timer == timerpvp - 60) {
            Bukkit.broadcastMessage("§aLe PvP seras activé dans 1min");
        }
        if (timer == timerpvp) {

            Common.get().getArena().setPVP(true);
            Bukkit.broadcastMessage("§aLe PvP est désormais activer");
            ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
                scenario.onPvP();
            });

        }
        if (timer == timerborder - 6000) {
            Bukkit.broadcastMessage("§aLa bordure se mettra en mouvement dans 10min");
        }
        if (timer == timerborder - 60) {
            Bukkit.broadcastMessage("§aLa bordure se mettra en mouvement dans 1min");
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

        if (uhcTeamManager.getAliveTeams().size() == 1) {

            StringBuilder winner = new StringBuilder();
            StringBuilder teamMemeber = new StringBuilder();

            for (UHCPlayer player : uhcPlayerManager.getPlayingOnlineUHCPlayers()) {
                Optional<UHCTeam> team = player.getTeam();
                if (team.isPresent()) {
                    winner.append(team.get().getName()).append(" ");
                    for (UHCPlayer teamM : team.get().getPlayers()) {
                        teamMemeber.append(teamM.getPlayer().getName()).append(" ");
                    }
                    break;
                }

            }
            Bukkit.broadcastMessage(ChatColor.GOLD + " Félicitations à l'équipe " + winner + " : " + teamMemeber);
            win = true;
        } else if (uhcPlayerManager.getPlayingOnlineUHCPlayers().size() == 1) {

            String winner = "";
            for (UHCPlayer player : uhcPlayerManager.getPlayingOnlineUHCPlayers()) {
                winner = player.getPlayer().getDisplayName();
            }
            Bukkit.broadcastMessage(ChatColor.GOLD + " Félicitations au joueur : " + winner);
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

    public void setProtection(int protection) {
        this.protection = protection;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setProtection(protection)
        );
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

    public void setFireProtection(int fireProtection) {
        this.fireProtection = fireProtection;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setFireProtection(fireProtection)
        );
    }

    public void setFeatherFalling(int featherFalling) {
        this.featherFalling = featherFalling;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setFeatherFalling(featherFalling)
        );
    }

    public void setBlastProtection(int blastProtection) {
        this.blastProtection = blastProtection;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setBlastProtection(blastProtection)
        );
    }

    public void setProjectileProtection(int projectileProtection) {
        this.projectileProtection = projectileProtection;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setProjectileProtection(projectileProtection)
        );
    }

    public void setRespiration(int respiration) {
        this.respiration = respiration;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setRespiration(respiration)
        );
    }

    public void setAquaAffinity(int aquaAffinity) {
        this.aquaAffinity = aquaAffinity;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setAquaAffinity(aquaAffinity)
        );
    }

    public void setThorns(int thorns) {
        this.thorns = thorns;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setThorns(thorns)
        );
    }

    public void setDepthStrider(int depthStrider) {
        this.depthStrider = depthStrider;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setDepthStrider(depthStrider)
        );
    }

    public void setSharpness(int sharpness) {
        this.sharpness = sharpness;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setSharpness(sharpness)
        );
    }

    public void setSmite(int smite) {
        this.smite = smite;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setSmite(smite)
        );
    }

    public void setBaneOfArthropods(int baneOfArthropods) {
        this.baneOfArthropods = baneOfArthropods;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setBaneOfArthropods(baneOfArthropods)
        );
    }

    public void setKnockback(int knockback) {
        this.knockback = knockback;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setKnockback(knockback)
        );
    }

    public void setFireAspect(int fireAspect) {
        this.fireAspect = fireAspect;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setFireAspect(fireAspect)
        );
    }

    public void setLooting(int looting) {
        this.looting = looting;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setLooting(looting)
        );
    }

    public void setEfficiency(int efficiency) {
        this.efficiency = efficiency;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setEfficiency(efficiency)
        );
    }

    public void setSilkTouch(int silkTouch) {
        this.silkTouch = silkTouch;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setSilkTouch(silkTouch)
        );
    }

    public void setUnbreaking(int unbreaking) {
        this.unbreaking = unbreaking;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setUnbreaking(unbreaking)
        );
    }

    public void setFortune(int fortune) {
        this.fortune = fortune;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setFortune(fortune)
        );
    }

    public void setPower(int power) {
        this.power = power;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setPower(power)
        );
    }

    public void setPunch(int punch) {
        this.punch = punch;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setPunch(punch)
        );
    }

    public void setFlame(int flame) {
        this.flame = flame;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setFlame(flame)
        );
    }

    public void setInfinity(int infinity) {
        this.infinity = infinity;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setInfinity(infinity)
        );
    }

    public void setLuckOfTheSea(int luckOfTheSea) {
        this.luckOfTheSea = luckOfTheSea;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setLuckOfTheSea(luckOfTheSea)
        );
    }

    public void setLure(int lure) {
        this.lure = lure;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player ->
                player.setLure(lure)
        );
    }

    public void applyLimitsFromList(List<Integer> limites) {
        if (limites == null || limites.size() < 30) return;

        setProtection(limites.get(0));
        setFireProtection(limites.get(1));
        setFeatherFalling(limites.get(2));
        setBlastProtection(limites.get(3));
        setProjectileProtection(limites.get(4));
        setRespiration(limites.get(5));
        setAquaAffinity(limites.get(6));
        setThorns(limites.get(7));
        setDepthStrider(limites.get(8));
        setSharpness(limites.get(9));
        setSmite(limites.get(10));
        setBaneOfArthropods(limites.get(11));
        setKnockback(limites.get(12));
        setFireAspect(limites.get(13));
        setLooting(limites.get(14));
        setEfficiency(limites.get(15));
        setSilkTouch(limites.get(16));
        setUnbreaking(limites.get(17));
        setFortune(limites.get(18));
        setPower(limites.get(19));
        setPunch(limites.get(20));
        setFlame(limites.get(21));
        setInfinity(limites.get(22));
        setLuckOfTheSea(limites.get(23));
        setLure(limites.get(24));
    }

    public enum GameState {
        LOBBY, SCATTERING, INGAME, ENDING
    }

    public enum WaitState {
        WAIT_STATE, LOBBY_STATE
    }

}
