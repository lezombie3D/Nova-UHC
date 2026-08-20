package net.novaproject.novauhc.game;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerDeathEvent;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerResurrectEvent;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerTimeoutEvent;
import net.novaproject.novauhc.event.UhcRoleEvents.UhcCampChangeEvent;
import net.novaproject.novauhc.event.UhcRoleEvents.UhcRoleAssignEvent;
import net.novaproject.novauhc.event.UhcRoleEvents.UhcRolesDistributedEvent;

import net.novaproject.novauhc.game.GameSpi.IBountySystem;
import net.novaproject.novauhc.game.GameSpi.ISilenceSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.novaproject.novauhc.ability.craft.CraftAbilityManager.CraftConsultRegistry;
import net.novaproject.novauhc.ability.toolbox.AbilityStates;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.event.UhcGameEvents.UhcGameStartEvent;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerDeathFinalizedEvent;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerKillEvent;
import net.novaproject.novauhc.game.GameSpi.Clearable;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.utils.UHCUtils.LocationRegistry;
import net.novaproject.novauhc.utils.cooldown.CooldownService.CombatTracker;
import net.novaproject.novauhc.utils.cooldown.CooldownService.ExpiryTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.EventPriority;

public final class Systems {

    private Systems() {
    }

    public static final class BountySystem {

        private static final Impl IMPL = new Impl();

        public static IBountySystem instance() {
            return IMPL;
        }

        public static void setOnClaim(BiConsumer<Player, Integer> handler) {
            IMPL.setOnClaim(handler);
        }

        public static void add(UUID target, int amount) {
            IMPL.add(target, amount);
        }

        public static void set(UUID target, int amount) {
            IMPL.set(target, amount);
        }

        public static int get(UUID target) {
            return IMPL.get(target);
        }

        public static boolean has(UUID target) {
            return IMPL.has(target);
        }

        public static int claim(Player claimer, UUID target) {
            return IMPL.claim(claimer, target);
        }

        public static void clear(UUID target) {
            IMPL.clear(target);
        }

        public static void clearAll() {
            IMPL.clearAll();
        }

        static final class Impl implements IBountySystem {

            private final Map<UUID, Integer> bounties = new HashMap<>();
            private BiConsumer<Player, Integer> onClaim;

            @Override
            public void setOnClaim(BiConsumer<Player, Integer> handler) {
                onClaim = handler;
            }

            @Override
            public void add(UUID target, int amount) {
                if (target == null || amount <= 0) return;
                bounties.merge(target, amount, Integer::sum);
            }

            @Override
            public void set(UUID target, int amount) {
                if (target == null) return;
                if (amount <= 0) bounties.remove(target);
                else bounties.put(target, amount);
            }

            @Override
            public int get(UUID target) {
                return bounties.getOrDefault(target, 0);
            }

            @Override
            public boolean has(UUID target) {
                return bounties.getOrDefault(target, 0) > 0;
            }

            @Override
            public int claim(Player claimer, UUID target) {
                int amount = bounties.getOrDefault(target, 0);
                if (amount > 0) {
                    bounties.remove(target);
                    if (onClaim != null) onClaim.accept(claimer, amount);
                }
                return amount;
            }

            @Override
            public void clear(UUID target) {
                bounties.remove(target);
            }

            @Override
            public void clearAll() {
                bounties.clear();
            }
        }
    }

    public static final class SilenceSystem {

        private static final Impl IMPL = new Impl();

        public static ISilenceSystem instance() {
            return IMPL;
        }

        public static void silence(UUID player, int seconds) {
            IMPL.silence(player, seconds);
        }

        public static boolean isSilenced(UUID player) {
            return IMPL.isSilenced(player);
        }

        public static int remainingSeconds(UUID player) {
            return IMPL.remainingSeconds(player);
        }

        public static void unsilence(UUID player) {
            IMPL.unsilence(player);
        }

        public static void clearAll() {
            IMPL.clearAll();
        }

        static final class Impl implements ISilenceSystem {

            private final ExpiryTracker<UUID> silenced = new ExpiryTracker<>();

            @Override
            public void silence(UUID player, int seconds) {
                if (player == null || seconds <= 0) return;
                silenced.extend(player, seconds * 1000L);
            }

            @Override
            public boolean isSilenced(UUID player) {
                return player != null && silenced.isActive(player);
            }

            @Override
            public int remainingSeconds(UUID player) {
                if (player == null) return 0;
                long ms = silenced.remaining(player);
                return ms <= 0 ? 0 : (int) (ms / 1000L);
            }

            @Override
            public void unsilence(UUID player) {
                silenced.remove(player);
            }

            @Override
            public void clearAll() {
                silenced.clear();
            }
        }
    }

    public static final class Clearables {

        private static final List<Clearable> GAME_START = new ArrayList<>();

        static {
            register(CombatTracker::clearAll);
            register(VoteSystem::clearAll);
            register(BountySystem::clearAll);
            register(SilenceSystem::clearAll);
            register(CraftConsultRegistry::clear);
            register(LocationRegistry::clear);
            register(AbilityStates::clearAll);
        }

        public static void register(Clearable clearable) {
            if (clearable != null && !GAME_START.contains(clearable)) GAME_START.add(clearable);
        }

        public static void clearGameStart() {
            for (Clearable clearable : GAME_START) clearable.clearAll();
        }

    }

    public static class SystemsListener implements Listener {

        @EventHandler
        public void onKill(UhcPlayerKillEvent event) {
            if (event.getKiller() == null || event.getVictim() == null) return;
            UUID victimId = event.getVictim().getUuid();
            if (!BountySystem.has(victimId)) return;

            Player killer = event.getKiller().getPlayer();
            String victimName = event.getVictim().getPlayer() != null
                    ? event.getVictim().getPlayer().getName() : "?";
            int amount = BountySystem.claim(killer, victimId);
            if (amount <= 0) return;

            String killerName = killer != null ? killer.getName() : "?";
            LangManager.get().sendAll(CoreLang.COMMON_BOUNTY_CLAIMED, Map.of(
                    "%killer%", killerName,
                    "%victim%", victimName,
                    "%amount%", amount));
            if (killer != null) {
                DisplayService.notification(killer, "Prime réclamée",
                        victimName + " §7(" + amount + ")", 5);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onGameStart(UhcGameStartEvent event) {
            Clearables.clearGameStart();
        }

        @EventHandler
        public void onDeathFinalized(UhcPlayerDeathFinalizedEvent event) {
            if (event.getVictim() != null) {
                AbilityStates.clearPlayer(event.getVictim().getUuid());
            }
        }
    }

    public static class StatsRoleListener implements Listener {

        @EventHandler
        public void onRolesDistributed(UhcRolesDistributedEvent event) {
            event.getRoles().forEach((player, role) -> {
                String camp = role.getCamp() != null ? role.getCamp().getName() : role.getName();
                UHCManager.get().getStatsTracker().setCamp(player.getUuid(), camp);
            });
        }

        @EventHandler
        public void onRoleAssign(UhcRoleAssignEvent event) {
            if (event.getRole() == null || event.getPlayer() == null) return;
            String camp = event.getRole().getCamp() != null
                    ? event.getRole().getCamp().getName()
                    : event.getRole().getName();
            UHCManager.get().getStatsTracker().setCamp(event.getPlayer().getUuid(), camp);
        }

        @EventHandler
        public void onCampChange(UhcCampChangeEvent event) {
            if (event.getNewCamp() == null || event.getPlayer() == null) return;
            UHCManager.get().getStatsTracker().setCamp(event.getPlayer().getUuid(), event.getNewCamp().getName());
        }

        @EventHandler
        public void onPlayerDeath(UhcPlayerDeathEvent event) {
            if (event.getVictim() == null) return;
            UUID killer = event.getKiller() != null ? event.getKiller().getUuid() : null;
            UHCManager.get().getStatsTracker().creditAssists(event.getVictim().getUuid(), killer);
        }

        @EventHandler
        public void onPlayerTimeout(UhcPlayerTimeoutEvent event) {
            if (event.getUuid() == null) return;
            UHCManager.get().getStatsTracker().creditAssists(event.getUuid(), null);
        }

        @EventHandler
        public void onPlayerResurrect(UhcPlayerResurrectEvent event) {
            if (event.getVictim() == null) return;
            UHCManager.get().getStatsTracker().revive(event.getVictim().getUuid());
        }
    }
}
