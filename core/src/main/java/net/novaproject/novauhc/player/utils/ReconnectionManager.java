package net.novaproject.novauhc.player.utils;

import net.novaproject.novauhc.utils.UHCUtils.ServerMonitor;
import net.novaproject.novauhc.player.PlayerSpi;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.Bonds.VictoryLinks;
import net.novaproject.novauhc.event.UhcRoleEvents.UhcTeamEliminatedEvent;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerTimeoutEvent;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerReconnectEvent;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.game.SpectatorNotifier;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ReconnectionManager implements PlayerSpi.IReconnectionManager {

    private static ReconnectionManager instance;

    private static final long RECONNECTION_TIMEOUT = TimeUnit.MINUTES.toSeconds(15);

    private final Map<UUID, ReconnectionTask> pendingReconnections = new HashMap<>();

    public ReconnectionManager() {
        instance = this;
    }

    public static ReconnectionManager get() {
        return instance;
    }

    public void startReconnectionTimer(UHCPlayer uhcPlayer, Player player) {
        UUID uuid = uhcPlayer.getUniqueId();

        if (pendingReconnections.containsKey(uuid)) {
            pendingReconnections.get(uuid).cancel();
        }

        Location disconnectLocation = player.getLocation().clone();

        Map<String, ItemStack[]> savedInventory = InventorySnapshots.savePlayerInventory(player);

        uhcPlayer.setPlaying(false);

        long disconnectTime = System.currentTimeMillis();

        BukkitTask task = Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
            onTimeout(uuid, disconnectLocation, savedInventory, true);
        }, RECONNECTION_TIMEOUT * 20L);

        ReconnectionTask reconnectionTask = new ReconnectionTask(
            uuid,
            disconnectLocation,
            savedInventory,
            task,
            disconnectTime
        );

        pendingReconnections.put(uuid, reconnectionTask);

        long timeoutMinutes = TimeUnit.SECONDS.toMinutes(RECONNECTION_TIMEOUT);

        SpectatorNotifier.notifySpectators(CoreLang.COMMON_PLAYER_DISCONNECTED_TIMER,
                Map.of("%player%", player.getName(), "%time%", timeoutMinutes), null);

        Bukkit.getLogger().info("[ReconnectionManager] Timer démarré pour " + player.getName()
                + " (" + timeoutMinutes + " minutes)");

        UHCManager.get().checkVictory();
    }

    public void handleReconnection(UHCPlayer uhcPlayer) {
        UUID uuid = uhcPlayer.getUniqueId();

        if (!pendingReconnections.containsKey(uuid)) {
            return;
        }

        ReconnectionTask task = pendingReconnections.remove(uuid);
        task.cancel();

        long elapsedSeconds = (System.currentTimeMillis() - task.disconnectTime) / 1000;
        long remainingSeconds = RECONNECTION_TIMEOUT - elapsedSeconds;
        long remainingMinutes = remainingSeconds / 60;

        Player player = uhcPlayer.getPlayer();

        uhcPlayer.setPlaying(true);

        if (UHCManager.get().getStatsTracker().getStats(uhcPlayer.getUuid()) == null) {
            UHCManager.get().getStatsTracker().addPlayer(uhcPlayer.getUuid(),
                    player != null ? player.getName() : uhcPlayer.getUuid().toString());
        }

        player.teleport(task.disconnectLocation);

        InventorySnapshots.restorePlayerInventory(player, task.savedInventory);

        for (Scenario s : ScenarioManager.get().getActiveSpecialScenarios()) {
            if (s instanceof ScenarioRole<?> scenarioRole) {
                scenarioRole.ensureFillerRole(uhcPlayer);
                break;
            }
        }

        SpectatorNotifier.notifySpectators(CoreLang.COMMON_PLAYER_RECONNECTED_TIMER,
                Map.of("%player%", player.getName(), "%time%", String.valueOf(remainingMinutes)), player);

        Bukkit.getLogger().info("[ReconnectionManager] " + player.getName() + " s'est reconnecté avec " + remainingMinutes + " minutes restantes");

        Bukkit.getPluginManager().callEvent(new UhcPlayerReconnectEvent(uhcPlayer, remainingSeconds));
    }

    public void eliminateNow(UUID uuid, Location dropLocation) {
        ReconnectionTask task = pendingReconnections.get(uuid);
        if (task == null) return;
        task.cancel();
        onTimeout(uuid, dropLocation != null ? dropLocation : task.disconnectLocation, task.savedInventory, false);
    }

    private void onTimeout(UUID uuid, Location disconnectLocation, Map<String, ItemStack[]> savedInventory, boolean announce) {
        pendingReconnections.remove(uuid);
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(uuid);
        if (uhcPlayer != null) {
            uhcPlayer.setPlaying(false);
            VictoryLinks.onDeath(uuid);
            uhcPlayer.getTeam().filter(t -> !t.isAlive()).ifPresent(t ->
                    Bukkit.getPluginManager().callEvent(
                            new UhcTeamEliminatedEvent(t)));
        }

        InventorySnapshots.dropAll(savedInventory, disconnectLocation);

        if (announce && !ServerMonitor.isTabFige()) {
            Bukkit.broadcastMessage(
                LangManager.get().get(CoreLang.COMMON_PLAYER_TIMEOUT_DEATH, null,
                    Map.of("%player%", Bukkit.getOfflinePlayer(uuid).getName()))
            );
        }

        UHCManager.get().getStatsTracker().addDeath(uuid);

        Bukkit.getLogger().info("[ReconnectionManager] " + Bukkit.getOfflinePlayer(uuid).getName() + " est éliminé (" + (announce ? "timeout de reconnexion" : "combat-log") + ")");

        Bukkit.getPluginManager().callEvent(new UhcPlayerTimeoutEvent(uuid, uhcPlayer));

        UHCManager.get().checkVictory();
    }

    public void cancelReconnectionTimer(UUID uuid) {
        ReconnectionTask task = pendingReconnections.remove(uuid);
        if (task != null) {
            task.cancel();
            Bukkit.getLogger().info("[ReconnectionManager] Timer annulé pour " + Bukkit.getOfflinePlayer(uuid).getName());
        }
    }

    public void cleanup() {
        pendingReconnections.values().forEach(ReconnectionTask::cancel);
        pendingReconnections.clear();
        Bukkit.getLogger().info("[ReconnectionManager] Tous les timers de reconnexion ont été annulés");
    }

    public boolean hasPendingReconnection(UUID uuid) {
        return pendingReconnections.containsKey(uuid);
    }

    public boolean hasAnyPendingReconnection() {
        return !pendingReconnections.isEmpty();
    }

    private record ReconnectionTask(UUID uuid, Location disconnectLocation, Map<String, ItemStack[]> savedInventory,
                                    BukkitTask task, long disconnectTime) {

        public void cancel() {
                if (task != null) {
                    task.cancel();
                }
            }
        }
}