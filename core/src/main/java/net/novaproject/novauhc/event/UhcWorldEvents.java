package net.novaproject.novauhc.event;

import java.util.Map;
import net.novaproject.novauhc.event.UHCPlayerEvent;
import net.novaproject.novauhc.player.UHCPlayer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class UhcWorldEvents {

    private UhcWorldEvents() {
    }

    public static class UhcBorderStartEvent extends UhcCancellableEvent {

        private static final HandlerList HANDLERS = new HandlerList();

        private double targetSize;
        private double blocksPerSecond;

        public UhcBorderStartEvent(double targetSize, double blocksPerSecond) {
            this.targetSize = targetSize;
            this.blocksPerSecond = blocksPerSecond;
        }

        public double getTargetSize() {
            return targetSize;
        }

        public void setTargetSize(double targetSize) {
            this.targetSize = targetSize;
        }

        public double getBlocksPerSecond() {
            return blocksPerSecond;
        }

        public void setBlocksPerSecond(double blocksPerSecond) {
            this.blocksPerSecond = blocksPerSecond;
        }



        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcBorderShrinkEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();

        private final int phaseId;
        private final double fromSize;
        private final double toSize;
        private final long durationSeconds;

        public UhcBorderShrinkEvent(int phaseId, double fromSize, double toSize, long durationSeconds) {
            this.phaseId = phaseId;
            this.fromSize = fromSize;
            this.toSize = toSize;
            this.durationSeconds = durationSeconds;
        }

        public int getPhaseId() {
            return phaseId;
        }

        public double getFromSize() {
            return fromSize;
        }

        public double getToSize() {
            return toSize;
        }

        public long getDurationSeconds() {
            return durationSeconds;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcDiamondMinedEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();

        private final UHCPlayer player;
        private final int totalMined;

        public UhcDiamondMinedEvent(UHCPlayer player, int totalMined) {
            this.player = player;
            this.totalMined = totalMined;
        }

        public UHCPlayer getPlayer() {
            return player;
        }

        public int getTotalMined() {
            return totalMined;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcDiamondLimitReachedEvent extends UHCPlayerEvent {

        private static final HandlerList HANDLERS = new HandlerList();

        public UhcDiamondLimitReachedEvent(UHCPlayer player) {
            super(player);
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

    public static class UhcItemMaxLevelEvent extends UhcCancellableEvent {

        private static final HandlerList HANDLERS = new HandlerList();

        private final Player player;
        private final Map<Enchantment, Integer> enchants;

        public UhcItemMaxLevelEvent(Player player, Map<Enchantment, Integer> enchants) {
            this.player = player;
            this.enchants = enchants;
        }

        public Player getPlayer() {
            return player;
        }

        public Map<Enchantment, Integer> getEnchants() {
            return enchants;
        }



        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcSchematicPlaceEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();

        private final String fileName;
        private final Location anchor;

        public UhcSchematicPlaceEvent(String fileName, Location anchor) {
            this.fileName = fileName;
            this.anchor = anchor;
        }

        public String getFileName() {
            return fileName;
        }

        public Location getAnchor() {
            return anchor == null ? null : anchor.clone();
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class ArenaRegeneratedEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();
        private final World newArena;

        public ArenaRegeneratedEvent(World newArena) {
            this.newArena = newArena;
        }

        public World getNewArena() { return newArena; }

        @Override public HandlerList getHandlers() { return HANDLERS; }
        public static HandlerList getHandlerList() { return HANDLERS; }
    }
}
