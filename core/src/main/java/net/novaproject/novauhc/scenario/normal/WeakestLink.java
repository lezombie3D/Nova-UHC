package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.scenario.lang.ScenarioLangManager;
import net.novaproject.novauhc.scenario.lang.lang.WeakestLinkLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
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
    @ScenarioVariable(name = "multiplier", description = "Le multiplicateur de dégâts pour le maillon faible", type = VariableType.PERCENTAGE)
    private double multiplier = 2.0;

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
    public void onGameStart() {
        startUpdateTask();
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (!isActive()) return;

        if (killer != null) {
            UUID killerUuid = killer.getPlayer().getUniqueId();
            playerKills.put(killerUuid, playerKills.getOrDefault(killerUuid, 0) + 1);
        }

        playerKills.remove(uhcPlayer.getPlayer().getUniqueId());

        updateWeakestLink();
    }

    @Override
    public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
        if (!isActive()) return;

        if (!(entity instanceof Player victim) || !(damager instanceof Player)) return;

        if (isWeakestLink(victim)) {
            double originalDamage = event.getDamage();

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

        updateTask.runTaskTimerAsynchronously(Main.get(), 0, 600);
    }


    private void updateWeakestLink() {
        List<UHCPlayer> playingPlayers = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();

        if (playingPlayers.size() <= 1) {
            return;
        }

        for (UHCPlayer uhcPlayer : playingPlayers) {
            UUID playerUuid = uhcPlayer.getPlayer().getUniqueId();
            if (!playerKills.containsKey(playerUuid)) {
                playerKills.put(playerUuid, 0);
            }
        }

        int minKills = Integer.MAX_VALUE;
        for (UHCPlayer uhcPlayer : playingPlayers) {
            UUID playerUuid = uhcPlayer.getPlayer().getUniqueId();
            int kills = playerKills.getOrDefault(playerUuid, 0);
            if (kills < minKills) {
                minKills = kills;
            }
        }

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

                ScenarioLangManager.send(uhcPlayer, WeakestLinkLang.WEAKEST_PLAYER, Map.of("%multiplier%", multiplier));
            }
        }

        if (weakestCount > 0) {
            ScenarioLangManager.sendAll(WeakestLinkLang.WEAKEST_PLAYER, Map.of("%players%", weakestLinks.toString()));
        }
    }

    private boolean isWeakestLink(Player player) {
        List<UHCPlayer> playingPlayers = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();

        if (playingPlayers.size() <= 1) {
            return false;
        }

        UUID playerUuid = player.getUniqueId();
        int playerKillCount = playerKills.getOrDefault(playerUuid, 0);

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
}
