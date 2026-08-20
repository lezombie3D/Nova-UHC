package net.novaproject.novauhc;

import net.novaproject.novauhc.player.utils.CombatLogManager.CombatActionRegistry;
import net.novaproject.novauhc.player.utils.ZombieCombat;
import net.novaproject.novauhc.world.BorderPhase;
import net.novaproject.novauhc.display.TabFreezeService;
import net.novaproject.novauhc.display.apollo.TeamMarkerService;
import net.novaproject.novauhc.display.PerViewerHologramManager;
import net.novaproject.novauhc.display.InvisibilityTagService;
import net.novaproject.novauhc.display.SpectatorNametagService;
import net.novaproject.novauhc.scenario.role.reveal.FacadePerceptionService;
import net.novaproject.novauhc.event.UhcGameEvents.UhcGameStateChangeEvent;
import net.novaproject.novauhc.ability.AbilityManager.CooldownDisplayService;
import java.util.ArrayList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.command.CommandManager;
import net.novaproject.novauhc.debug.DebugLog;
import net.novaproject.novauhc.game.GameLoop;
import net.novaproject.novauhc.game.GameLoop.GameStartSequence;
import net.novaproject.novauhc.game.Lifecycles;
import net.novaproject.novauhc.game.VictoryManager;
import net.novaproject.novauhc.listener.ListenerManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.lobby.RankManager;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.ui.config.Enchants;
import net.novaproject.novauhc.player.utils.GameStatsTracker;
import net.novaproject.novauhc.world.SimpleBorder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class UHCManager {

    public static UHCManager get() {
        return Main.getUHCManager();
    }

    private GameStatsTracker statsTracker = new GameStatsTracker();
    private String currentGameUuid = null;

    public Map<String, ItemStack[]> start = new HashMap<>();
    public Map<String, ItemStack[]> death = new HashMap<>();
    private WaitState waitState = WaitState.LOBBY_STATE;
    private UHCTeamManager uhcTeamManager;
    private UHCPlayerManager uhcPlayerManager;
    private GameState gameState = GameState.LOBBY;
    private ScenarioManager scenarioManager;
    private int timer = -1;
    private boolean started = false;
    private boolean canceled = false;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @lombok.experimental.Delegate
    private final GameSettings settings = new GameSettings();

    public GameSettings settings() {
        return settings;
    }

    private SimpleBorder activeBorder = null;
    private final List<BorderPhase> borderPhases = new ArrayList<>();

    private final VictoryManager victoryManager = new VictoryManager();
    private final GameLoop gameLoop = new GameLoop();
    private final GameStartSequence startSequence = new GameStartSequence();

    public void setup() {
        uhcPlayerManager = new UHCPlayerManager();
        uhcTeamManager = new UHCTeamManager();
        (scenarioManager = new ScenarioManager()).setup();
        ListenerManager.setup();
        CommandManager.get().setup();
        if (DebugLog.devMode()) {
            Bukkit.setWhitelist(false);
        } else {
            Bukkit.setWhitelist(true);
            Bukkit.getWhitelistedPlayers().forEach(wl -> {
                wl.setWhitelisted(false);
            });
        }

        Lifecycles.register(null, () -> PerViewerHologramManager.get().start(), () -> PerViewerHologramManager.get().stop());
        Lifecycles.register(null, () -> CooldownDisplayService.start(Main.get()), CooldownDisplayService::stop);
        Lifecycles.register(null, () -> InvisibilityTagService.start(Main.get()), InvisibilityTagService::stop);
        Lifecycles.register(null, () -> FacadePerceptionService.start(Main.get()), FacadePerceptionService::stop);
        Lifecycles.register(null, () -> SpectatorNametagService.start(Main.get()), SpectatorNametagService::stop);
        Lifecycles.register(null, () -> TeamMarkerService.start(Main.get()), TeamMarkerService::stop);
        Lifecycles.startAll();

        CombatActionRegistry.INSTANCE.register(ZombieCombat.INSTANCE);
    }

    public String getTimerFormatted() {
        int seconds = Math.max(timer, 0);
        return seconds >= 3600
                ? String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60)
                : String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    public void onStart() {
        startSequence.start();
    }

    public void onStart(boolean force) {
        startSequence.start(force);
    }

    public void onSec() {
        gameLoop.tick();
    }

    public boolean isLobby() {
        return gameState == GameState.LOBBY || gameState == GameState.SCATTERING;
    }

    public boolean isGame() {
        return gameState == GameState.INGAME;
    }

    public void setGameState(GameState gameState) {
        GameState old = this.gameState;
        this.gameState = gameState;
        if (old != gameState) {
            if (old == GameState.INGAME) TabFreezeService.releaseAll();
            Bukkit.getPluginManager().callEvent(new UhcGameStateChangeEvent(old, gameState));
            RankManager.get().refreshTags();
        }
    }

    public void onPlayerKill(Player killer, Player victim) {
        statsTracker.addKill(killer.getUniqueId());
        statsTracker.addDeath(victim.getUniqueId());
    }

    public void checkVictory() {
        victoryManager.requestCheck();
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