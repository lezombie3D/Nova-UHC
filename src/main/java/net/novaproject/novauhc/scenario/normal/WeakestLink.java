package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.scenario.ScenarioLangManager;
import net.novaproject.novauhc.scenario.lang.WeakestLinkLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WeakestLink extends Scenario {

    private final Map<UUID, Integer> playerKills = new HashMap<>();
    private BukkitRunnable updateTask;

    @Override
    public String getName() {
        return "WeakestLink";
    }

    @Override
    public String getDescription() {
        return "Le joueur avec le moins de kills prend 2x plus de dégâts.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.IRON_CHESTPLATE);
    }

    @Override
    public String getPath() {
        return "weakestlink";
    }

    @Override
    public ScenarioLang[] getLang() {
        return WeakestLinkLang.values();
    }


    @Override
    public void toggleActive() {
        super.toggleActive();
        if (isActive()) {
            startUpdateTask();
        } else {
            stopUpdateTask();
        }
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (!isActive()) return;

        if (killer != null) {
            UUID killerUuid = killer.getPlayer().getUniqueId();
            playerKills.put(killerUuid, playerKills.getOrDefault(killerUuid, 0) + 1);

            killer.getPlayer().sendMessage("§c[WeakestLink] §fVous avez maintenant " +
                    playerKills.get(killerUuid) + " kill(s) !");
        }

        playerKills.remove(uhcPlayer.getPlayer().getUniqueId());

        updateWeakestLink();
    }

    @Override
    public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
        if (!isActive()) return;

        if (!(entity instanceof Player) || !(damager instanceof Player)) return;

        Player victim = (Player) entity;

        // Check if victim is the weakest link
        if (isWeakestLink(victim)) {
            // Apply damage multiplier from config
            double originalDamage = event.getDamage();
            double multiplier = getConfig().getDouble("damage_multiplier", 2.0);
            event.setDamage(originalDamage * multiplier);

            UHCPlayer uhcVictim = UHCPlayerManager.get().getPlayer(victim);
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("%multiplier%", String.valueOf(multiplier));
            ScenarioLangManager.send(uhcVictim, WeakestLinkLang.DAMAGE_TAKEN, placeholders);
        }
    }

    private void startUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
        }

        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }
                updateWeakestLink();
            }
        };

        // Update every 30 seconds
        updateTask.runTaskTimerAsynchronously(Main.get(), 0, 600);
    }

    private void stopUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    private void updateWeakestLink() {
        List<UHCPlayer> playingPlayers = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();

        if (playingPlayers.size() <= 1) {
            return; // No point in having a weakest link with 1 or 0 players
        }

        // Initialize kills for new players
        for (UHCPlayer uhcPlayer : playingPlayers) {
            UUID playerUuid = uhcPlayer.getPlayer().getUniqueId();
            if (!playerKills.containsKey(playerUuid)) {
                playerKills.put(playerUuid, 0);
            }
        }

        // Find minimum kills
        int minKills = Integer.MAX_VALUE;
        for (UHCPlayer uhcPlayer : playingPlayers) {
            UUID playerUuid = uhcPlayer.getPlayer().getUniqueId();
            int kills = playerKills.getOrDefault(playerUuid, 0);
            if (kills < minKills) {
                minKills = kills;
            }
        }

        // Find all players with minimum kills (weakest links)
        StringBuilder weakestLinks = new StringBuilder();
        int weakestCount = 0;

        for (UHCPlayer uhcPlayer : playingPlayers) {
            UUID playerUuid = uhcPlayer.getPlayer().getUniqueId();
            int kills = playerKills.getOrDefault(playerUuid, 0);

            if (kills == minKills) {
                if (weakestCount > 0) {
                    weakestLinks.append(", ");
                }
                weakestLinks.append(uhcPlayer.getPlayer().getName());
                weakestCount++;

                // Notify the weakest link
                uhcPlayer.getPlayer().sendMessage("§c[WeakestLink] §fVous êtes un maillon faible avec " +
                        kills + " kill(s) ! Vous prenez 2x plus de dégâts !");
            }
        }

        // Broadcast weakest link(s)
        if (weakestCount > 0) {
            String message = weakestCount == 1 ?
                    "§c[WeakestLink] §fLe maillon faible est : §c" + weakestLinks :
                    "§c[WeakestLink] §fLes maillons faibles sont : §c" + weakestLinks;

            Bukkit.broadcastMessage(message);
        }
    }

    private boolean isWeakestLink(Player player) {
        List<UHCPlayer> playingPlayers = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();

        if (playingPlayers.size() <= 1) {
            return false;
        }

        UUID playerUuid = player.getUniqueId();
        int playerKillCount = playerKills.getOrDefault(playerUuid, 0);

        // Find minimum kills among all players
        int minKills = Integer.MAX_VALUE;
        for (UHCPlayer uhcPlayer : playingPlayers) {
            UUID uuid = uhcPlayer.getPlayer().getUniqueId();
            int kills = playerKills.getOrDefault(uuid, 0);
            if (kills < minKills) {
                minKills = kills;
            }
        }

        return playerKillCount == minKills;
    }

    // Get kills for a player
    public int getKills(Player player) {
        return playerKills.getOrDefault(player.getUniqueId(), 0);
    }

    // Get all player kills (for debugging/admin commands)
    public Map<UUID, Integer> getAllKills() {
        return new HashMap<>(playerKills);
    }

    // Reset kills for a player (admin command)
    public void resetKills(Player player) {
        playerKills.put(player.getUniqueId(), 0);
        updateWeakestLink();
    }

    // Reset all kills (admin command)
    public void resetAllKills() {
        playerKills.clear();
        updateWeakestLink();
    }
}
