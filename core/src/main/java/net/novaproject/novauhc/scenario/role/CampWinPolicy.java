package net.novaproject.novauhc.scenario.role;

import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.player.UHCPlayer;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class CampWinPolicy {

    public enum Grouping { PARENT_GROUP, SOLO, TOGETHER, WINS_WITH }

    private final Camps camp;
    private Grouping grouping = Grouping.PARENT_GROUP;
    private final Set<Camps> allies = new LinkedHashSet<>();
    private Predicate<Map<UHCPlayer, ? extends Role>> condition;
    private Predicate<Map<UHCPlayer, ? extends Role>> requirement;

    CampWinPolicy(Camps camp) {
        this.camp = camp;
    }

    public CampWinPolicy solo() {
        this.grouping = Grouping.SOLO;
        return this;
    }

    public CampWinPolicy together() {
        this.grouping = Grouping.TOGETHER;
        return this;
    }

    public CampWinPolicy winsWith(Camps... others) {
        this.grouping = Grouping.WINS_WITH;
        for (Camps other : others) {
            if (other != null) allies.add(other);
        }
        return this;
    }

    public CampWinPolicy custom(Predicate<Map<UHCPlayer, ? extends Role>> condition) {
        this.condition = condition;
        return this;
    }

    public CampWinPolicy requires(Predicate<Map<UHCPlayer, ? extends Role>> requirement) {
        this.requirement = requirement;
        return this;
    }

    public Camps getCamp() {
        return camp;
    }

    public Grouping getGrouping() {
        return grouping;
    }

    public Set<Camps> getAllies() {
        return allies;
    }

    public Camps getWinsWith() {
        return allies.isEmpty() ? null : allies.iterator().next();
    }

    public Predicate<Map<UHCPlayer, ? extends Role>> getCondition() {
        return condition;
    }

    public Predicate<Map<UHCPlayer, ? extends Role>> getRequirement() {
        return requirement;
    }

    @FunctionalInterface
    public interface WinCondition<T extends Role> {
        boolean isWin(Map<UHCPlayer, T> playerRoles);
    }
}
