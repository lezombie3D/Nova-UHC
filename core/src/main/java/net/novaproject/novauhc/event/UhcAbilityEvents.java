package net.novaproject.novauhc.event;

import java.util.List;
import java.util.Map;
import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lobby.HotbarManager.HotbarItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class UhcAbilityEvents {

    private UhcAbilityEvents() {
    }

    public static class UhcAbilityUseEvent extends UhcCancellableEvent {

        private static final HandlerList HANDLERS = new HandlerList();

        private final Player player;
        private final Ability ability;

        public UhcAbilityUseEvent(Player player, Ability ability) {
            this.player = player;
            this.ability = ability;
        }

        public Player getPlayer() {
            return player;
        }

        public Ability getAbility() {
            return ability;
        }



        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcAbilityUsedEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();

        private final Player player;
        private final Ability ability;

        public UhcAbilityUsedEvent(Player player, Ability ability) {
            this.player = player;
            this.ability = ability;
        }

        public Player getPlayer() {
            return player;
        }

        public Ability getAbility() {
            return ability;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcGiveHotbarEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();

        private final Player player;
        private final List<HotbarItem> items;

        public UhcGiveHotbarEvent(Player player, List<HotbarItem> items) {
            this.player = player;
            this.items = items;
        }

        public Player getPlayer() {
            return player;
        }

        public List<HotbarItem> getItems() {
            return items;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcVoteEndEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();

        private final String voteId;
        private final String question;
        private final Map<String, Integer> tally;
        private final String winner;

        public UhcVoteEndEvent(String voteId, String question, Map<String, Integer> tally, String winner) {
            this.voteId = voteId;
            this.question = question;
            this.tally = tally;
            this.winner = winner;
        }

        public String getVoteId() {
            return voteId;
        }

        public String getQuestion() {
            return question;
        }

        public Map<String, Integer> getTally() {
            return tally;
        }

        public String getWinner() {
            return winner;
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
