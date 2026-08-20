package net.novaproject.novauhc.event;

import java.util.UUID;
import net.novaproject.novauhc.event.UHCPlayerEvent;
import net.novaproject.novauhc.player.UHCPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class UhcPlayerEvents {

    private UhcPlayerEvents() {
    }

    public static class UhcPlayerDeathEvent extends UHCPlayerEvent {

        private static final HandlerList HANDLERS = new HandlerList();

        private final UHCPlayer killer;
        private final PlayerDeathEvent bukkitEvent;

        public UhcPlayerDeathEvent(UHCPlayer victim, UHCPlayer killer, PlayerDeathEvent bukkitEvent) {
            super(victim);
            this.killer = killer;
            this.bukkitEvent = bukkitEvent;
        }

        public UHCPlayer getVictim() {
            return getUhcPlayer();
        }

        public UHCPlayer getKiller() {
            return killer;
        }

        public PlayerDeathEvent getBukkitEvent() {
            return bukkitEvent;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcPlayerDeathWaitingEvent extends UHCPlayerEvent {

        private static final HandlerList HANDLERS = new HandlerList();

        private final UHCPlayer killer;
        private final long waitTicks;

        public UhcPlayerDeathWaitingEvent(UHCPlayer victim, UHCPlayer killer, long waitTicks) {
            super(victim);
            this.killer = killer;
            this.waitTicks = waitTicks;
        }

        public UHCPlayer getVictim() {
            return getUhcPlayer();
        }

        public UHCPlayer getKiller() {
            return killer;
        }

        public long getWaitTicks() {
            return waitTicks;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcPlayerDeathFinalizedEvent extends UHCPlayerEvent {

        private static final HandlerList HANDLERS = new HandlerList();

        private final UHCPlayer killer;
        private final Location deathLocation;

        public UhcPlayerDeathFinalizedEvent(UHCPlayer victim, UHCPlayer killer, Location deathLocation) {
            super(victim);
            this.killer = killer;
            this.deathLocation = deathLocation;
        }

        public UHCPlayer getVictim() {
            return getUhcPlayer();
        }

        public UHCPlayer getKiller() {
            return killer;
        }

        public Location getDeathLocation() {
            return deathLocation;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcPlayerKillEvent extends UHCPlayerEvent {

        private static final HandlerList HANDLERS = new HandlerList();

        private final UHCPlayer victim;

        public UhcPlayerKillEvent(UHCPlayer killer, UHCPlayer victim) {
            super(killer);
            this.victim = victim;
        }

        public UHCPlayer getKiller() {
            return getUhcPlayer();
        }

        public UHCPlayer getVictim() {
            return victim;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcPlayerReconnectEvent extends UHCPlayerEvent {

        private static final HandlerList HANDLERS = new HandlerList();

        private final long remainingSeconds;

        public UhcPlayerReconnectEvent(UHCPlayer player, long remainingSeconds) {
            super(player);
            this.remainingSeconds = remainingSeconds;
        }

        public UHCPlayer getPlayer() {
            return getUhcPlayer();
        }

        public long getRemainingSeconds() {
            return remainingSeconds;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcPlayerResurrectEvent extends UHCPlayerEvent {

        private static final HandlerList HANDLERS = new HandlerList();

        private final int priority;

        public UhcPlayerResurrectEvent(UHCPlayer victim, int priority) {
            super(victim);
            this.priority = priority;
        }

        public UHCPlayer getVictim() {
            return getUhcPlayer();
        }

        public int getPriority() {
            return priority;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcPlayerTimeoutEvent extends UHCPlayerEvent {

        private static final HandlerList HANDLERS = new HandlerList();

        private final UUID uuid;

        public UhcPlayerTimeoutEvent(UUID uuid, UHCPlayer player) {
            super(player);
            this.uuid = uuid;
        }

        public UUID getUuid() {
            return uuid;
        }

        public UHCPlayer getPlayer() {
            return getUhcPlayer();
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcCombatTagEvent extends UHCPlayerEvent {

        private static final HandlerList HANDLERS = new HandlerList();


        private final UHCPlayer damager;
        private final double damage;

        public UhcCombatTagEvent(UHCPlayer victim, UHCPlayer damager, double damage) {
            super(victim);
            this.damager = damager;
            this.damage = damage;
        }

        public UHCPlayer getVictim() {
            return getUhcPlayer();
        }

        public UHCPlayer getDamager() {
            return damager;
        }

        public double getDamage() {
            return damage;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcLateJoinEvent extends UHCPlayerEvent {

        private static final HandlerList HANDLERS = new HandlerList();

        private final Player host;

        public UhcLateJoinEvent(UHCPlayer player, Player host) {
            super(player);
            this.host = host;
        }

        public UHCPlayer getPlayer() {
            return getUhcPlayer();
        }

        public Player getHost() {
            return host;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }
}
