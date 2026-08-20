package net.novaproject.novauhc.game;

import org.bukkit.Sound;

import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.scenario.ScenarioSpi.IRole;

import net.novaproject.novauhc.player.PlayerSpi.IUHCPlayer;

import net.novaproject.novauhc.scenario.role.Role;

import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.chat.TextUtils;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerDeathFinalizedEvent;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerResurrectEvent;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.display.TabFreezeService;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerDeathWaitingEvent;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.scenario.role.Bonds.VictoryLinks;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.normal.ScheduledScenarios;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PendingDeathManager implements GameSpi.IPendingDeathManager {

    private static final PendingDeathManager INSTANCE = new PendingDeathManager();

    private final Map<UUID, Long> pendingDeaths = new HashMap<>();
    private final Map<UUID, Long> deadlines = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedArmors = new HashMap<>();
    private final Map<UUID, Location> deathLocations = new HashMap<>();
    private final Map<UUID, UUID> killers = new HashMap<>();
    private final Map<UUID, TreeMap<Integer, Runnable>> resurrections = new HashMap<>();
    private final Set<UUID> noRevive = new HashSet<>();
    private BukkitTask countdownTask;


    public static PendingDeathManager get() {
        return INSTANCE;
    }

    public boolean isPending(UUID uuid) {
        return pendingDeaths.containsKey(uuid);
    }

    public boolean hasResurrection(UUID uuid) {
        TreeMap<Integer, Runnable> map = resurrections.get(uuid);
        return map != null && !map.isEmpty() && !noRevive.contains(uuid);
    }

    public boolean hasAnyPending() {
        return !pendingDeaths.isEmpty();
    }

    public void clearAll() {
        pendingDeaths.clear();
        deadlines.clear();
        savedInventories.clear();
        savedArmors.clear();
        deathLocations.clear();
        killers.clear();
        resurrections.clear();
        noRevive.clear();
        stopCountdownIfIdle();
    }

    public Set<UUID> getPendingUuids() {
        return new HashSet<>(pendingDeaths.keySet());
    }

    public int getRemainingSeconds(UUID uuid) {
        Long deadline = deadlines.get(uuid);
        if (deadline == null) return -1;
        return (int) Math.max(0, (deadline - System.currentTimeMillis() + 999) / 1000);
    }

    public ItemStack[] getSavedInventory(UUID uuid) {
        ItemStack[] saved = savedInventories.get(uuid);
        return saved == null ? null : saved.clone();
    }

    public ItemStack[] getSavedArmor(UUID uuid) {
        ItemStack[] saved = savedArmors.get(uuid);
        return saved == null ? null : saved.clone();
    }

    public void markNoRevive(UUID uuid) {
        noRevive.add(uuid);
    }

    public boolean consumeNoRevive(UUID uuid) {
        return noRevive.remove(uuid);
    }

    public boolean isNoRevive(UUID uuid) {
        return noRevive.contains(uuid);
    }

    public UUID getKiller(UUID victimUuid) {
        return killers.get(victimUuid);
    }

    public Location getDeathLocation(UUID victimUuid) {
        return deathLocations.get(victimUuid);
    }

    public void beginPendingDeath(UHCPlayer victim, UHCPlayer killer, PlayerDeathEvent event, long requestedWaitTicks) {
        if (victim == null || victim.getPlayer() == null) return;
        Player player = victim.getPlayer();
        UUID uuid = player.getUniqueId();
        if (pendingDeaths.containsKey(uuid)) return;
        final long waitTicks = isWaitingDeathAllowed() ? requestedWaitTicks : 1L;

        deathLocations.put(uuid, player.getLocation().clone());
        savedInventories.put(uuid, player.getInventory().getContents().clone());
        savedArmors.put(uuid, player.getInventory().getArmorContents().clone());
        if (killer != null && killer.getPlayer() != null) {
            killers.put(uuid, killer.getPlayer().getUniqueId());
        }
        pendingDeaths.put(uuid, System.currentTimeMillis());
        deadlines.put(uuid, System.currentTimeMillis() + Math.max(waitTicks, 1L) * 50L);
        if (event != null) event.getDrops().clear();

        if (isAutoReviveWindow()) registerResurrection(victim, Integer.MAX_VALUE, () -> cancelPendingDeath(victim));

        if (!TabFreezeService.isFrozen(uuid)) {
            victim.getTeam().ifPresentOrElse(
                    team -> DisplayService.nametag(player, team.name(), team.prefix(), ""),
                    () -> DisplayService.nametag(player, "", "", ""));
        }

        LangManager.get().send(CoreLang.COMMON_DEATH_PENDING, player);
        Bukkit.getPluginManager().callEvent(new UhcPlayerDeathWaitingEvent(victim, killer, waitTicks));

        ensureCountdown();

        Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
            if (pendingDeaths.containsKey(uuid)) finalizeDeath(victim);
        }, Math.max(waitTicks, 1L));

        Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
            if (pendingDeaths.containsKey(uuid) && player.isOnline()) {
                double maxHealth = player.getMaxHealth();
                player.spigot().respawn();
                player.setMaxHealth(maxHealth);
                player.setGameMode(GameMode.SURVIVAL);
                player.teleport(Common.get().getLobbySpawn());
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
                applyWaitingEffects(player, waitTicks);
            }
        }, 2L);
    }

    private void applyWaitingEffects(Player player, long waitTicks) {
        int dur = (int) Math.max(waitTicks, 1L) + 40;
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.BLINDNESS, dur, 0, false, false), true);
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOW, dur, 128, false, false), true);
    }

    private void clearWaitingEffects(Player player) {
        if (player == null) return;
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.SLOW);
    }

    private void ensureCountdown() {
        if (countdownTask != null) return;
        countdownTask = Bukkit.getScheduler().runTaskTimer(Main.get(), this::tickCountdown, 20L, 20L);
    }

    private void stopCountdownIfIdle() {
        if (!pendingDeaths.isEmpty() || countdownTask == null) return;
        countdownTask.cancel();
        countdownTask = null;
    }

    private void tickCountdown() {
        if (pendingDeaths.isEmpty()) {
            stopCountdownIfIdle();
            return;
        }
        for (UUID uuid : pendingDeaths.keySet()) {
            int remaining = Math.max(1, getRemainingSeconds(uuid));
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            DisplayService.actionBar(player, LangManager.get().get(CoreLang.COMMON_DEATH_PENDING_BAR, player,
                    Map.of("%time%", TextUtils.getFormattedTime(remaining))));
        }
    }

    private boolean isWaitingDeathAllowed() {
        ScenarioRole<?> role = KPIBuilder.activeScenarioRole();
        return role != null && role.isRolesDistributed();
    }

    public boolean tryAutoRevive(UHCPlayer victim, PlayerDeathEvent event) {
        if (victim == null || !isAutoReviveWindow()) return false;
        Player player = victim.getPlayer();
        if (player == null || !player.isOnline()) return false;

        ItemStack[] contents = player.getInventory().getContents().clone();
        ItemStack[] armor = player.getInventory().getArmorContents().clone();
        Location location = player.getLocation().clone();
        int level = player.getLevel();
        double maxHealth = player.getMaxHealth();

        if (event != null) {
            event.getDrops().clear();
            event.setDroppedExp(0);
            event.setDeathMessage(null);
        }

        Bukkit.getScheduler().runTask(Main.get(), () -> {
            if (!player.isOnline()) return;
            player.spigot().respawn();
            player.setMaxHealth(maxHealth);
            player.setGameMode(GameMode.SURVIVAL);
            player.teleport(location);
            player.getInventory().setContents(contents);
            player.getInventory().setArmorContents(armor);
            player.setLevel(level);
            player.setHealth(Math.max(2.0, maxHealth * UHCManager.get().getResurrectionHealthPercent()));
            player.setFoodLevel(20);
            player.setFireTicks(0);
            player.setNoDamageTicks(60);
            LangManager.get().send(CoreLang.COMMON_AUTO_REVIVE, player);
        });
        return true;
    }

    private boolean isAutoReviveWindow() {
        UHCManager uhc = UHCManager.get();
        int now = uhc.getTimer();

        if (uhc.isAutoRevivePvp() && now < uhc.getPvpTimer()) return true;

        if (uhc.isAutoReviveRoles()) {
            ScenarioRole<?> role = KPIBuilder.activeScenarioRole();
            if (role != null && !role.isRolesDistributed()) return true;
        }

        if (uhc.isAutoReviveFinalHeal()) {
            ScheduledScenarios.FinalHeal finalHeal =
                    ScenarioManager.get().getScenario(ScheduledScenarios.FinalHeal.class);
            if (finalHeal != null && finalHeal.isActive() && now < finalHeal.getHealTime1()) return true;
        }
        return false;
    }

    public void registerResurrection(UHCPlayer victim, int priority, Runnable onResurrect) {
        if (victim == null || victim.getPlayer() == null) return;
        UUID uuid = victim.getPlayer().getUniqueId();
        if (!pendingDeaths.containsKey(uuid)) return;
        if (noRevive.contains(uuid)) return;
        resurrections.computeIfAbsent(uuid, k -> new TreeMap<>()).putIfAbsent(priority, onResurrect);
    }

    public void unregisterResurrection(UHCPlayer victim, int priority) {
        if (victim == null || victim.getPlayer() == null) return;
        UUID uuid = victim.getPlayer().getUniqueId();
        TreeMap<Integer, Runnable> map = resurrections.get(uuid);
        if (map == null) return;
        map.remove(priority);
        if (map.isEmpty()) resurrections.remove(uuid);
    }

    public void cancelPendingDeath(UHCPlayer uhcPlayer) {
        if (uhcPlayer == null || uhcPlayer.getPlayer() == null) return;
        UUID uuid = uhcPlayer.getPlayer().getUniqueId();
        if (!pendingDeaths.containsKey(uuid)) return;
        pendingDeaths.remove(uuid);
        deadlines.remove(uuid);
        resurrections.remove(uuid);

        Player player = uhcPlayer.getPlayer();
        ItemStack[] contents = savedInventories.remove(uuid);
        ItemStack[] armor = savedArmors.remove(uuid);
        Location deathLoc = deathLocations.remove(uuid);
        killers.remove(uuid);
        noRevive.remove(uuid);
        stopCountdownIfIdle();

        clearWaitingEffects(player);
        uhcPlayer.setPlaying(true);
        if (!TabFreezeService.isFrozen(uuid)) {
            uhcPlayer.getTeam().ifPresentOrElse(
                    team -> DisplayService.nametag(player, team.name(), team.prefix(), ""),
                    () -> DisplayService.nametag(player, "", "", ""));
        }
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(Math.max(2.0, player.getMaxHealth() * UHCManager.get().getResurrectionHealthPercent()));
        if (contents != null) player.getInventory().setContents(contents);
        if (armor != null) player.getInventory().setArmorContents(armor);
        if (deathLoc != null) player.teleport(deathLoc);
    }

    public void forceImmediateDeath(UHCPlayer victim) {
        if (victim == null || victim.getPlayer() == null) return;
        UUID uuid = victim.getPlayer().getUniqueId();
        noRevive.add(uuid);
        if (pendingDeaths.containsKey(uuid)) {
            resurrections.remove(uuid);
            finalizeDeath(victim);
        }
    }

    public void finalizeDeath(UHCPlayer victim) {
        if (victim == null) return;
        UUID uuid = victim.getUniqueId();
        Player player = victim.getPlayer();

        TreeMap<Integer, Runnable> map = resurrections.remove(uuid);
        if (player != null && player.isOnline()
                && map != null && !map.isEmpty() && !noRevive.contains(uuid)) {
            int priority = map.lastKey();
            Runnable best = map.get(priority);
            if (best != null) {
                Bukkit.getPluginManager().callEvent(
                        new UhcPlayerResurrectEvent(victim, priority));
                best.run();
                Bukkit.getScheduler().runTask(Main.get(), () -> {
                    if (pendingDeaths.containsKey(uuid)) {
                        Bukkit.getLogger().warning("[PendingDeath] la résurrection de " + uuid
                                + " n'a pas appelé cancelPendingDeath — victoire bloquée tant que le pending existe.");
                    }
                });
                return;
            }
        }

        pendingDeaths.remove(uuid);
        deadlines.remove(uuid);

        Location deathLoc = deathLocations.remove(uuid);
        ItemStack[] contents = savedInventories.remove(uuid);
        ItemStack[] armor = savedArmors.remove(uuid);
        UUID killerUuid = killers.remove(uuid);
        noRevive.remove(uuid);
        stopCountdownIfIdle();

        if (deathLoc != null && deathLoc.getWorld() != null) {
            if (contents != null) Arrays.stream(contents)
                    .filter(i -> i != null && i.getType() != Material.AIR)
                    .filter(i -> !ItemCreator.isAbilityItem(i) || ItemCreator.AbilityNbt.isDroppedOnDeath(i))
                    .forEach(i -> deathLoc.getWorld().dropItemNaturally(deathLoc, i));
            if (armor != null) Arrays.stream(armor)
                    .filter(i -> i != null && i.getType() != Material.AIR)
                    .filter(i -> !ItemCreator.isAbilityItem(i) || ItemCreator.AbilityNbt.isDroppedOnDeath(i))
                    .forEach(i -> deathLoc.getWorld().dropItemNaturally(deathLoc, i));
        }

        if (player != null && player.isOnline()) {
            clearWaitingEffects(player);
            player.setGameMode(GameMode.SPECTATOR);
            if (!TabFreezeService.isFrozen(uuid)) {
                DisplayService.nametag(player, "zzzzz", "§8§lSPEC §r§8", "");
            }
        }
        victim.setPlaying(false);

        VictoryLinks.onDeath(uuid);

        Bukkit.getPluginManager().callEvent(new UhcPlayerDeathFinalizedEvent(victim, UHCPlayerManager.get().getPlayer(killerUuid), deathLoc));

        UHCManager.get().checkVictory();
    }

    public static final class DeathAnnouncer {

        private static final GameSpi.IDeathAnnouncer INSTANCE = new GameSpi.IDeathAnnouncer() {
            @Override public void announceVanilla(IUHCPlayer victim) { DeathAnnouncer.announceVanilla((UHCPlayer) victim); }
            @Override public void announceRole(IUHCPlayer victim, IRole deadRole) { DeathAnnouncer.announceRole((UHCPlayer) victim, (Role) deadRole); }
        };

        public static GameSpi.IDeathAnnouncer instance() { return INSTANCE; }


        public static void announceVanilla(UHCPlayer victim) {
            for (Player alive : Bukkit.getOnlinePlayers()) {
                if (alive != null && alive.isOnline()) {
                    alive.playSound(alive.getLocation(), Sound.WITHER_SPAWN, 1, 1);
                }
            }
            if (victim.getTeam().isPresent()) {
                Bukkit.broadcastMessage(LangManager.get().get(CoreLang.COMMON_DEATH_MESSAGE_TEAM, victim.getPlayer()));
                return;
            }
            Bukkit.broadcastMessage(LangManager.get().get(CoreLang.COMMON_DEATH_MESSAGE, victim.getPlayer()));
        }

        public static void announceRole(UHCPlayer victim, Role deadRole) {
            if (victim == null || victim.getPlayer() == null) {
                return;
            }

            String victimName = victim.getPlayer().getDisplayName();

            for (Player viewer : Bukkit.getOnlinePlayers()) {
                String perceivedRole = deadRole != null
                        ? deadRole.getPerceivedDisplay(viewer)
                        : LangManager.get().get(CoreLang.COMMON_ROLE_UNKNOWN, viewer);

                viewer.sendMessage(LangManager.get().get(CoreLang.COMMON_DEATH_ROLE_MESSAGE, viewer,
                        Map.of("%player%", victimName, "%role%", perceivedRole)));
            }
        }
    }
}
