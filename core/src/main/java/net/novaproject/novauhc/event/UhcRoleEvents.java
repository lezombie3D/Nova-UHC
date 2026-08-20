package net.novaproject.novauhc.event;

import java.util.List;
import java.util.Map;
import net.novaproject.novauhc.event.UHCPlayerEvent;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.team.UHCTeam;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class UhcRoleEvents {



    public static class UhcRoleAssignEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();

        private final UHCPlayer player;
        private final Role role;

        public UhcRoleAssignEvent(UHCPlayer player, Role role) {
            this.player = player;
            this.role = role;
        }

        public UHCPlayer getPlayer() {
            return player;
        }

        public Role getRole() {
            return role;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcRoleDistributionEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();

        private final List<UHCPlayer> players;
        private final List<Role> pool;

        public UhcRoleDistributionEvent(List<UHCPlayer> players, List<Role> pool) {
            this.players = players;
            this.pool = pool;
        }

        public List<UHCPlayer> getPlayers() {
            return players;
        }

        public List<Role> getPool() {
            return pool;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcRolesDistributedEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();

        private final Map<UHCPlayer, Role> roles;

        public UhcRolesDistributedEvent(Map<UHCPlayer, Role> roles) {
            this.roles = roles;
        }

        public Map<UHCPlayer, Role> getRoles() {
            return roles;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcCampChangeEvent extends UHCPlayerEvent {

        private static final HandlerList HANDLERS = new HandlerList();

        private final Role role;
        private final Camps oldCamp;
        private final Camps newCamp;

        public UhcCampChangeEvent(UHCPlayer player, Role role, Camps oldCamp, Camps newCamp) {
            super(player);

            this.role = role;
            this.oldCamp = oldCamp;
            this.newCamp = newCamp;
        }

        public UHCPlayer getPlayer() {
            return getUhcPlayer();
        }

        public Role getRole() {
            return role;
        }

        public Camps getOldCamp() {
            return oldCamp;
        }

        public Camps getNewCamp() {
            return newCamp;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class UhcTeamEliminatedEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();

        private final UHCTeam team;

        public UhcTeamEliminatedEvent(UHCTeam team) {
            this.team = team;
        }

        public UHCTeam getTeam() {
            return team;
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
