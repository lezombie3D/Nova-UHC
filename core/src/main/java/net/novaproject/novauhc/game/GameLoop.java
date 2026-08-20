package net.novaproject.novauhc.game;

import net.novaproject.novauhc.task.ScatterTask;
import org.bson.Document;

import com.google.gson.JsonObject;
import net.novaproject.novauhc.api.ApiManager;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.minigame.DuelRequestManager;
import net.novaproject.novauhc.minigame.MiniGames.MiniGameRegistry;
import net.novaproject.novauhc.player.utils.ReconnectionManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.event.UhcGameEvents.UhcScatterStartEvent;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.stream.Collectors;

import net.novaproject.novauhc.event.UhcGameEvents.UhcGameStartEvent;

import net.novaproject.novauhc.world.BorderPhase;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.event.UhcGameEvents.UhcFinalHealEvent;
import net.novaproject.novauhc.event.UhcWorldEvents.UhcBorderShrinkEvent;
import net.novaproject.novauhc.utils.chat.TextUtils;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.event.UhcWorldEvents.UhcBorderStartEvent;
import net.novaproject.novauhc.event.UhcGameEvents.UhcGameSecondEvent;
import net.novaproject.novauhc.event.UhcGameEvents.UhcPvpEnableEvent;
import net.novaproject.novauhc.lang.LangManager;
import org.bukkit.Bukkit;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.world.ArenaMirrorManager;
import net.novaproject.novauhc.world.SimpleBorder;
import org.bukkit.WorldBorder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GameLoop {

    private static final List<Integer> DEFAULT_PVP_WARNINGS = Arrays.asList(300, 60);
    private static final List<Integer> DEFAULT_MEETUP_WARNINGS = Arrays.asList(6000, 60);

    public void tick() {
        UHCManager uhc = UHCManager.get();
        uhc.setTimer(uhc.getTimer() + 1);
        int timer = uhc.getTimer();

        Bukkit.getPluginManager().callEvent(new UhcGameSecondEvent(timer));

        EpisodeManager.get().tick(timer);
        DayNightCycle.get().tick();

        if (timer == uhc.getInvulnerabilityDuration()) {
            LangManager.get().sendAll(CoreLang.COMMON_INVULNERABLE_OFF);
        }

        for (int healAt : ConfigUtils.getGeneralConfig().getIntegerList("final-heals")) {
            if (timer == healAt && healAt > 0) {
                UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(uhcPlayer -> {
                    Player p = uhcPlayer.getPlayer();
                    if (p != null && p.isOnline()) {
                        p.setHealth(p.getMaxHealth());
                    }
                });
                LangManager.get().sendAll(CoreLang.COMMON_FINAL_HEAL);
                Bukkit.getPluginManager().callEvent(new UhcFinalHealEvent(timer));
            }
        }

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player -> {
                scenario.onSec(player.getPlayer());
            });
        });

        for (int before : warningPaliers("announcements.pvp-warnings", DEFAULT_PVP_WARNINGS)) {
            if (timer == uhc.getPvpTimer() - before && before > 0) {
                LangManager.get().sendAll(CoreLang.COMMON_PVP_START_IN, Map.of("%time_before%", TextUtils.getFormattedTime(before)));
            }
        }
        if (timer == uhc.getPvpTimer()) {
            ArenaMirrorManager.get().propagatePVP(true);
            LangManager.get().sendAll(CoreLang.COMMON_PVP_START);
            ScenarioManager.get().getActiveScenarios().forEach(Scenario::onPvP);
            Bukkit.getPluginManager().callEvent(new UhcPvpEnableEvent());
        }
        for (int before : warningPaliers("announcements.meetup-warnings", DEFAULT_MEETUP_WARNINGS)) {
            if (timer == uhc.getBorderTimer() - before && before > 0) {
                LangManager.get().sendAll(CoreLang.COMMON_MEETUP_START_IN, Map.of("%time_before%", TextUtils.getFormattedTime(before)));
            }
        }
        if (timer == uhc.getBorderTimer()) {
            UhcBorderStartEvent borderEvent = new UhcBorderStartEvent(uhc.getTargetSize(), uhc.getReducSpeed());
            Bukkit.getPluginManager().callEvent(borderEvent);
            if (!borderEvent.isCancelled()) {
                WorldBorder border = Common.get().getArena().getWorldBorder();
                SimpleBorder activeBorder = new SimpleBorder(border);
                uhc.setActiveBorder(activeBorder);
                activeBorder.startReduce(borderEvent.getTargetSize(), borderEvent.getBlocksPerSecond());
            }
        }

        tickBorderPhases(uhc, timer);
    }

    private void tickBorderPhases(UHCManager uhc, int timer) {
        for (BorderPhase phase : uhc.getBorderPhases()) {
            if (phase.isExecuted()) continue;

            if (timer == phase.getTimer() - 60 || timer == phase.getTimer() - 10) {
                LangManager.get().sendAll(CoreLang.COMMON_BORDER_PHASE_WARNING, Map.of(
                        "%id%", phase.getId(),
                        "%time%", TextUtils.getFormattedTime(phase.getTimer() - timer),
                        "%size%", (int) phase.getSize()));
                continue;
            }

            if (timer == phase.getTimer()) {
                phase.setExecuted(true);
                executeBorderPhase(uhc, phase);
            }
        }
    }

    private void executeBorderPhase(UHCManager uhc, BorderPhase phase) {
        World world = Common.get().getArena();
        if (world == null) return;
        WorldBorder border = world.getWorldBorder();

        LangManager.get().sendAll(CoreLang.COMMON_BORDER_PHASE_START, Map.of(
                "%id%", phase.getId(),
                "%size%", (int) phase.getSize()));

        if (phase.hasCenter()) {
            double oldX = border.getCenter().getX();
            double oldZ = border.getCenter().getZ();
            if (oldX != phase.getCenterX() || oldZ != phase.getCenterZ()) {
                LangManager.get().sendAll(CoreLang.COMMON_BORDER_CENTER_MOVED);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1f, 0.7f);
                }
                animateCenterMove(border, oldX, oldZ, phase.getCenterX(), phase.getCenterZ(), 10);
            }
        }

        double current = border.getSize();
        double dif = Math.abs(current - phase.getSize());
        long speed = Math.max(uhc.getReducSpeed(), 1);
        long durationSeconds = (long) (dif / speed);
        Bukkit.getPluginManager().callEvent(
                new UhcBorderShrinkEvent(phase.getId(), current, phase.getSize(), durationSeconds));
        border.setSize(phase.getSize(), durationSeconds);
        border.setDamageAmount(uhc.getBorderDamageAmount());
        border.setDamageBuffer(uhc.getBorderDamageBuffer());
    }

    private void animateCenterMove(WorldBorder border, double fromX, double fromZ, double toX, double toZ, int durationSeconds) {
        int steps = Math.max(durationSeconds * 4, 1);
        new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                step++;
                double t = Math.min(1.0, step / (double) steps);
                border.setCenter(fromX + (toX - fromX) * t, fromZ + (toZ - fromZ) * t);
                if (step >= steps) {
                    cancel();
                }
            }
        }.runTaskTimer(Main.get(), 5L, 5L);
    }

    private List<Integer> warningPaliers(String path, List<Integer> defaults) {
        List<Integer> configured = ConfigUtils.getGeneralConfig().getIntegerList(path);
        return configured == null || configured.isEmpty() ? defaults : configured;
    }

    public static class GameStartSequence {

        public void start() {
            start(false);
        }

        public void start(boolean force) {
            UHCManager uhc = UHCManager.get();

            UhcGameStartEvent startEvent = new UhcGameStartEvent(force);
            Bukkit.getPluginManager().callEvent(startEvent);
            if (startEvent.isCancelled()) return;

            MiniGameRegistry.stopAll();
            DuelRequestManager.clearAll();

            uhc.setGameState(UHCManager.GameState.SCATTERING);
            ArenaMirrorManager.get().propagatePVP(false);
            if (uhc.getTeam_size() > 1 && uhc.getUhcTeamManager().isRandomMode()) {
                uhc.getUhcTeamManager().clearAssignments();
            }
            uhc.getUhcTeamManager().fillTeams();

            new BukkitRunnable() {
                private int countdown = UHCManager.get().getStartCountdownSeconds();

                @Override
                public void run() {
                    if (uhc.isCanceled()) {
                        cancel();
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.setLevel(0);
                        }
                        uhc.setGameState(UHCManager.GameState.LOBBY);
                        return;
                    }

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        String title = "";
                        String subtitle = "§cAttention..";
                        switch (countdown) {
                            case 10:
                                title = "§e" + countdown;
                                DisplayService.title(p, title, subtitle, 60);
                                p.playSound(p.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
                                break;
                            case 4:
                            case 5:
                                title = "§c" + countdown;
                                DisplayService.title(p, title, subtitle, 60);
                                p.playSound(p.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
                                break;
                            case 3:
                                title = "§6" + countdown;
                                subtitle = "§ePlugin par lezombie3D";
                                DisplayService.title(p, title, subtitle, 60);
                                p.playSound(p.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
                                break;
                            case 2:
                                title = "§e" + countdown;
                                subtitle = "§ePlugin par lezombie3D";
                                DisplayService.title(p, title, subtitle, 60);
                                p.playSound(p.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
                                break;
                            case 1:
                                title = "§2" + countdown;
                                subtitle = "§ePlugin par lezombie3D";
                                DisplayService.title(p, title, subtitle, 60);
                                p.playSound(p.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
                                break;
                            default:
                                break;
                        }
                        long remaining = countdown;
                        float ratio = Math.min(1f, (float) remaining / UHCManager.get().getStartCountdownSeconds());
                        p.setExp(ratio);
                        p.setLevel((int) (remaining));
                        DisplayService.actionBar(p, LangManager.get().get(CoreLang.TASK_START_ACTION_BAR, Map.of("%countdown%", countdown)));
                    }

                    countdown--;

                    if (countdown < 0) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
                        }
                        cancel();
                        uhc.getUhcTeamManager().fillTeams();
                        Bukkit.getPluginManager().callEvent(new UhcScatterStartEvent());
                        new ScatterTask().runTaskTimer(Main.get(), 0, 5);
                        World world = Common.get().getArena();
                        world.setTime(1200);
                        world.setThundering(false);
                        world.setWeatherDuration(Integer.MAX_VALUE);

                        uhc.getStatsTracker().startGame();
                        EpisodeManager.get().reset();
                        PendingDeathManager.get().clearAll();
                        ReconnectionManager.get().cleanup();
                        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                            uhc.getStatsTracker().addPlayer(uhcPlayer.getUuid(), uhcPlayer.getPlayer().getName());
                        }

                        String mode = uhc.getTeam_size() == 1 ? "Solo" : "Team " + uhc.getTeam_size();
                        String scenario = null;
                        if (!ScenarioManager.get().getActiveSpecialScenarios().isEmpty()) {
                            mode = "Special";
                            scenario = ScenarioManager.get().getActiveSpecialScenarios().get(0).getName();
                        }

                        Map<String, Document> scenarioConfig = ScenarioManager.get().getActiveScenarios()
                                .stream()
                                .collect(Collectors.toMap(
                                        Scenario::getName,
                                        Scenario::scenarioToDoc
                                ));

                        JsonObject scenarioJson = Main.getConfigManager().documentMapToJson(scenarioConfig);
                        int borderSize = (int) Common.get().getArena().getWorldBorder().getSize();

                        List<ApiManager.PlayerInfo> apiPlayers = UHCPlayerManager.get().getPlayingOnlineUHCPlayers().stream()
                                .map(p -> new ApiManager.PlayerInfo(p.getUuid().toString(), p.getPlayer().getName()))
                                .toList();

                        ApiManager.get().gameStart(
                                mode,
                                scenario,
                                ScenarioManager.get().getActiveScenarios().stream()
                                        .map(Scenario::getName)
                                        .collect(Collectors.toList()),
                                scenarioJson,
                                borderSize,
                                apiPlayers
                        ).thenAccept(gameUuid -> {
                            uhc.setCurrentGameUuid(gameUuid);
                            Main.get().getLogger().info("Game started with UUID: " + gameUuid);
                        });
                    }
                }
            }.runTaskTimer(Main.get(), 0, 20);
        }
    }

    public static class GameTask extends BukkitRunnable {

        @Override
        public void run() {
            UHCManager uhcManager = UHCManager.get();
            if (uhcManager.isGame()) {
                uhcManager.onSec();
            } else {
                cancel();
            }
        }
    }
}
