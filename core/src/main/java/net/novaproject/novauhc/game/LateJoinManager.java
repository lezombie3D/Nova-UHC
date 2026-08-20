package net.novaproject.novauhc.game;

import net.novaproject.novauhc.display.TabFreezeService;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcLateJoinEvent;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.task.ScatterTask;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LateJoinManager implements Listener {

    private static final int NO_FALL_SECONDS = 8;
    private static final long VANISH_TICKS = 40L;
    private static final long INVENTORY_RETRY_TICKS = 5L;

    private static final Set<UUID> NO_FALL = new HashSet<>();
    private static final Set<UUID> TEMP_VANISHED = new HashSet<>();

    public static boolean handleLate(Player host, Player target) {
        String prefix = Common.get().getInfoTag();

        if (UHCManager.get().getGameState() != UHCManager.GameState.INGAME) {
            host.sendMessage(prefix + "§cLe late join n'est possible qu'en partie.");
            return false;
        }

        if (target == null || !target.isOnline()) {
            host.sendMessage(prefix + "§cJoueur introuvable.");
            return false;
        }

        UHCPlayer uhcTarget = UHCPlayerManager.get().getPlayer(target);
        if (uhcTarget == null) {
            host.sendMessage(prefix + "§cJoueur UHC introuvable.");
            return false;
        }

        if (uhcTarget.isPlaying()) {
            host.sendMessage(prefix + "§c" + target.getName() + " est déjà en jeu.");
            return false;
        }

        Location spawn = host.getLocation().clone();
        if (spawn.getWorld() == null) {
            host.sendMessage(prefix + "§cImpossible de trouver la position du host.");
            return false;
        }

        uhcTarget.setPlaying(true);

        if (UHCManager.get().getStatsTracker().getStats(uhcTarget.getUuid()) == null) {
            UHCManager.get().getStatsTracker().addPlayer(uhcTarget.getUuid(), target.getName());
        }

        preparePlayer(target);
        applyTemporaryVanish(target);

        target.teleport(spawn);
        target.setGameMode(GameMode.SURVIVAL);
        target.setAllowFlight(false);
        target.setFlying(false);
        target.setVelocity(new Vector(0.0D, 0.0D, 0.0D));
        target.setFallDistance(0.0F);
        target.setFireTicks(0);

        giveStartInventory(target);
        applyNoFall(target, NO_FALL_SECONDS);

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> scenario.onStart(target));

        ScenarioManager.get().getActiveSpecialScenarios().forEach(scenario -> {
            if (scenario instanceof ScenarioRole<?> scenarioRole) {
                scenarioRole.ensureFillerRole(uhcTarget);
            }
        });

        target.playSound(target.getLocation(), Sound.ENDERMAN_TELEPORT, 0.8F, 1.2F);
        target.sendMessage(prefix + "§aVous avez rejoint la partie en late.");

        host.sendMessage(prefix + "§a" + target.getName() + " §fest maintenant intégré à la partie.");
        Bukkit.broadcastMessage(prefix + "§6" + target.getName() + " §fa rejoint la partie.");

        Bukkit.getPluginManager().callEvent(new UhcLateJoinEvent(uhcTarget, host));
        return true;
    }

    public static boolean isNoFall(Player player) {
        return player != null && NO_FALL.contains(player.getUniqueId());
    }

    public static boolean isTempVanished(Player player) {
        return player != null && TEMP_VANISHED.contains(player.getUniqueId());
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (NO_FALL.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    private static void preparePlayer(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(false);
        player.setFlying(false);

        player.setMaxHealth(20.0D);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20.0F);
        player.setExp(0.0F);
        player.setLevel(0);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.updateInventory();
    }

    private static void giveStartInventory(final Player target) {
        giveStartInventoryNow(target);

        Bukkit.getScheduler().runTaskLater(Main.get(), () -> giveStartInventoryNow(target), INVENTORY_RETRY_TICKS);
    }

    private static void giveStartInventoryNow(Player target) {
        if (target == null || !target.isOnline()) return;
        if (UHCManager.get().start.isEmpty()) {
            target.updateInventory();
            return;
        }
        target.getInventory().clear();
        target.getInventory().setArmorContents(null);
        ScatterTask.giveStartInv(target, UHCManager.get().start);
        target.updateInventory();
    }

    private static void applyTemporaryVanish(final Player player) {
        if (player == null || !player.isOnline()) return;

        TEMP_VANISHED.add(player.getUniqueId());

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online == null || online.equals(player)) continue;
            online.hidePlayer(player);
            player.hidePlayer(online);
        }

        Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
            TEMP_VANISHED.remove(player.getUniqueId());

            if (!player.isOnline()) return;

            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online == null || online.equals(player)) continue;
                online.showPlayer(player);
                player.showPlayer(online);
            }
            TabFreezeService.applyTo(player);

            player.setGameMode(GameMode.SURVIVAL);
            player.setAllowFlight(false);
            player.setFlying(false);
            player.updateInventory();
        }, VANISH_TICKS);
    }

    private static void applyNoFall(final Player player, final int seconds) {
        if (player == null) return;

        NO_FALL.add(player.getUniqueId());

        new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    NO_FALL.remove(player.getUniqueId());
                    cancel();
                    return;
                }

                player.setFallDistance(0.0F);

                ticks += 2;

                if (ticks >= seconds * 20) {
                    player.setFallDistance(0.0F);
                    NO_FALL.remove(player.getUniqueId());
                    cancel();
                }
            }
        }.runTaskTimer(Main.get(), 0L, 2L);
    }
}