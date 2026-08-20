package net.novaproject.novauhc.scenario.role;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoleRegistration {

    private boolean unique = false;
    private int min = 0;
    private int max = Integer.MAX_VALUE;
    private Class<? extends Role> pairedWith = null;
    private boolean pairedSelf = false;

    private boolean filler = false;
    private int minPlayers = 0;
    private final Set<Class<? extends Role>> requires = new HashSet<>();
    private final Set<Class<? extends Role>> requiresOneOf = new HashSet<>();
    private final Set<Class<? extends Role>> excludes = new HashSet<>();

    public RoleRegistration unique() {
        this.unique = true;
        return this;
    }

    public RoleRegistration min(int min) {
        this.min = Math.max(min, 0);
        return this;
    }

    public RoleRegistration max(int max) {
        this.max = Math.max(max, 0);
        return this;
    }

    public RoleRegistration pairWith(Class<? extends Role> other) {
        this.pairedWith = other;
        return this;
    }

    public RoleRegistration paired() {
        this.pairedSelf = true;
        return this;
    }

    private int groupSize = 0;

    public RoleRegistration group(int size) {
        this.groupSize = Math.max(0, size);
        return this;
    }

    public int getGroupSize() {
        return groupSize;
    }

    private final List<Class<? extends Role>> groupedWith = new ArrayList<>();

    @SafeVarargs
    public final RoleRegistration groupWith(Class<? extends Role>... others) {
        for (Class<? extends Role> o : others) {
            if (o != null && !groupedWith.contains(o)) groupedWith.add(o);
        }
        return this;
    }

    public List<Class<? extends Role>> getGroupedWith() {
        return groupedWith;
    }

    private int weight = 0;

    public RoleRegistration weight(int weight) {
        this.weight = Math.max(0, weight);
        return this;
    }

    public int getWeight() {
        return weight;
    }

    public RoleRegistration fillerRole() {
        this.filler = true;
        return this;
    }

    public RoleRegistration minPlayers(int minPlayers) {
        this.minPlayers = Math.max(0, minPlayers);
        return this;
    }

    @SafeVarargs
    public final RoleRegistration requires(Class<? extends Role>... others) {
        for (Class<? extends Role> o : others) if (o != null) requires.add(o);
        return this;
    }

    @SafeVarargs
    public final RoleRegistration requiresOneOf(Class<? extends Role>... others) {
        for (Class<? extends Role> o : others) if (o != null) requiresOneOf.add(o);
        return this;
    }

    @SafeVarargs
    public final RoleRegistration excludes(Class<? extends Role>... others) {
        for (Class<? extends Role> o : others) if (o != null) excludes.add(o);
        return this;
    }

    public boolean isFiller() {
        return filler;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public Set<Class<? extends Role>> getRequires() {
        return requires;
    }

    public Set<Class<? extends Role>> getRequiresOneOf() {
        return requiresOneOf;
    }

    public Set<Class<? extends Role>> getExcludes() {
        return excludes;
    }

    public boolean isUnique() {
        return unique;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public Class<? extends Role> getPairedWith() {
        return pairedWith;
    }

    public boolean isPairedSelf() {
        return pairedSelf;
    }
}

