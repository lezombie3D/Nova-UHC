package net.novaproject.novauhc.utils;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CommonLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ReconnectionManager {

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
        
        Map<String, ItemStack[]> savedInventory = new HashMap<>();
        savedInventory.put("contents", player.getInventory().getContents());
        savedInventory.put("armor", player.getInventory().getArmorContents());

        uhcPlayer.setPlaying(false);

        long disconnectTime = System.currentTimeMillis();

        BukkitTask task = Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
            onTimeout(uuid, disconnectLocation, savedInventory);
        }, RECONNECTION_TIMEOUT * 20L);

        ReconnectionTask reconnectionTask = new ReconnectionTask(
            uuid,
            disconnectLocation,
            savedInventory,
            task,
            disconnectTime
        );

        pendingReconnections.put(uuid, reconnectionTask);

        Bukkit.broadcastMessage(
            LangManager.get().get(CommonLang.PLAYER_DISCONNECTED_TIMER, null,
                Map.of("%player%", player.getName(), "%time%", "15"))
        );

        Bukkit.getLogger().info("[ReconnectionManager] Timer démarré pour " + player.getName() + " (15 minutes)");

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

        player.teleport(task.disconnectLocation);

        if (task.savedInventory != null) {
            if (task.savedInventory.containsKey("contents")) {
                player.getInventory().setContents(task.savedInventory.get("contents"));
            }
            if (task.savedInventory.containsKey("armor")) {
                player.getInventory().setArmorContents(task.savedInventory.get("armor"));
            }
        }

        Bukkit.broadcastMessage(
            LangManager.get().get(CommonLang.PLAYER_RECONNECTED_TIMER, null,
                Map.of("%player%", player.getName(), "%time%", String.valueOf(remainingMinutes)))
        );

        Bukkit.getLogger().info("[ReconnectionManager] " + player.getName() + " s'est reconnecté avec " + remainingMinutes + " minutes restantes");
    }

    private void onTimeout(UUID uuid, Location disconnectLocation, Map<String, ItemStack[]> savedInventory) {
        pendingReconnections.remove(uuid);
        Player player = Bukkit.getPlayer(uuid);
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null) return;

        uhcPlayer.setPlaying(false);

        if (savedInventory != null && disconnectLocation != null) {
            if (savedInventory.containsKey("contents")) {
                for (ItemStack item : savedInventory.get("contents")) {
                    if (item != null && item.getType() != Material.AIR) {
                        disconnectLocation.getWorld().dropItemNaturally(disconnectLocation, item);
                    }
                }
            }
            if (savedInventory.containsKey("armor")) {
                for (ItemStack item : savedInventory.get("armor")) {
                    if (item != null && item.getType() != Material.AIR) {
                        disconnectLocation.getWorld().dropItemNaturally(disconnectLocation, item);
                    }
                }
            }
        }

        Bukkit.broadcastMessage(
            LangManager.get().get(CommonLang.PLAYER_TIMEOUT_DEATH, null,
                Map.of("%player%", Bukkit.getOfflinePlayer(uuid).getName()))
        );

        UHCManager.get().getStatsTracker().addDeath(uuid);

        Bukkit.getLogger().info("[ReconnectionManager] " + Bukkit.getOfflinePlayer(uuid).getName() + " est mort (timeout de reconnexion)");

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

    private static class ReconnectionTask {
        private final UUID uuid;
        private final Location disconnectLocation;
        private final Map<String, ItemStack[]> savedInventory;
        private final BukkitTask task;
        private final long disconnectTime;

        public ReconnectionTask(UUID uuid, Location disconnectLocation, Map<String, ItemStack[]> savedInventory, BukkitTask task, long disconnectTime) {
            this.uuid = uuid;
            this.disconnectLocation = disconnectLocation;
            this.savedInventory = savedInventory;
            this.task = task;
            this.disconnectTime = disconnectTime;
        }

        public void cancel() {
            if (task != null) {
                task.cancel();
            }
        }
    }
}