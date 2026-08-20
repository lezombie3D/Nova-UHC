package net.novaproject.novauhc.event;

import java.util.Collections;
import java.util.List;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.game.DayNightCycle;
import net.novaproject.novauhc.game.EpisodeManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class UhcGameEvents {

    private UhcGameEvents() {
    }

    public static class UhcGameStartEvent extends UhcCancellableEvent {

        private static final HandlerList HANDLERS = new HandlerList();

        private final boolean forced;

        public UhcGameStartEvent() {
            this(false);
        }

        public UhcGameStartEvent(boolean forced) {
            this.forced = forced;
        }

        public boolean isForced() {
            return forced;
        }

        @Override
        protected boolean canBeCancelled() {
            return !forced;
        }




        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcGameSecondEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();

        private final int timer;

        public UhcGameSecondEvent(int timer) {
            this.timer = timer;
        }

        public int getTimer() {
            return timer;
        }

        public boolean isAt(int second) {
            return timer == second;
        }

        public boolean isMinuteBefore(int targetSecond, int minutes) {
            return timer == targetSecond - minutes * 60;
        }

        public boolean isSecondBefore(int targetSecond, int seconds) {
            return timer == targetSecond - seconds;
        }

        public boolean isEveryMinute() {
            return timer > 0 && timer % 60 == 0;
        }

        public boolean isEvery(int seconds) {
            return seconds > 0 && timer > 0 && timer % seconds == 0;
        }

        public int getEpisode() {
            return EpisodeManager.get().getEpisode();
        }

        public boolean isDay() {
            return DayNightCycle.get().isDay();
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcGameStateChangeEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();

        private final UHCManager.GameState oldState;
        private final UHCManager.GameState newState;

        public UhcGameStateChangeEvent(UHCManager.GameState oldState, UHCManager.GameState newState) {
            this.oldState = oldState;
            this.newState = newState;
        }

        public UHCManager.GameState getOldState() {
            return oldState;
        }

        public UHCManager.GameState getNewState() {
            return newState;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcGameWinEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();

        private final List<UHCPlayer> winners;
        private final Camps winningCamp;

        public UhcGameWinEvent(List<UHCPlayer> winners, Camps winningCamp) {
            this.winners = winners == null ? Collections.emptyList() : Collections.unmodifiableList(winners);
            this.winningCamp = winningCamp;
        }

        public List<UHCPlayer> getWinners() {
            return winners;
        }

        public Camps getWinningCamp() {
            return winningCamp;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcNewEpisodeEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();

        private final int episode;

        public UhcNewEpisodeEvent(int episode) {
            this.episode = episode;
        }

        public int getEpisode() {
            return episode;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcDayEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcNightEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcPvpEnableEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcFinalHealEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();

        private final int timer;

        public UhcFinalHealEvent(int timer) {
            this.timer = timer;
        }

        public int getTimer() {
            return timer;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcScatterStartEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcScatterEndEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }
}
