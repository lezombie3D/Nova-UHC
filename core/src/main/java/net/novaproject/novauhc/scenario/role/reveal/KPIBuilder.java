package net.novaproject.novauhc.scenario.role.reveal;

import java.util.function.Supplier;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.player.UHCPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import org.bukkit.entity.Player;

public class KPIBuilder {

    private final List<Class<? extends Role>> roleClasses = new ArrayList<>();
    private final List<Class<? extends Role>> excludeClasses = new ArrayList<>();
    private final List<Camps> camps = new ArrayList<>();
    private boolean matchAll = false;
    private boolean includeSelf = false;
    private boolean showRole = false;
    private int max = -1;
    private boolean randomOrder = false;
    private Predicate<UHCPlayer> filter;
    private String name;


    @SafeVarargs
    public static KPIBuilder ofRoles(Class<? extends Role>... classes) {
        KPIBuilder builder = new KPIBuilder();
        builder.roleClasses.addAll(Arrays.asList(classes));
        return builder;
    }

    public static KPIBuilder ofCamp(Camps camp) {
        KPIBuilder builder = new KPIBuilder();
        if (camp != null) builder.camps.add(camp);
        return builder;
    }

    public static KPIBuilder ofCamps(Camps... camps) {
        KPIBuilder builder = new KPIBuilder();
        for (Camps camp : camps) {
            if (camp != null) builder.camps.add(camp);
        }
        return builder;
    }

    public static KPIBuilder all() {
        KPIBuilder builder = new KPIBuilder();
        builder.matchAll = true;
        return builder;
    }

    @SafeVarargs
    public final KPIBuilder exclude(Class<? extends Role>... classes) {
        excludeClasses.addAll(Arrays.asList(classes));
        return this;
    }

    public KPIBuilder includeSelf() {
        this.includeSelf = true;
        return this;
    }

    public KPIBuilder showRole() {
        this.showRole = true;
        return this;
    }

    public KPIBuilder max(int max) {
        this.max = max;
        return this;
    }

    public KPIBuilder random() {
        this.randomOrder = true;
        return this;
    }

    public KPIBuilder filter(Predicate<UHCPlayer> filter) {
        this.filter = filter;
        return this;
    }

    public KPIBuilder name(String name) {
        this.name = name;
        return this;
    }

    public KnowPlayerInfo build(Role viewer) {
        return new KnowPlayerInfo(() -> resolve(viewer), showRole, name);
    }

    public static ScenarioRole<?> activeScenarioRole() {
        for (Scenario s : ScenarioManager.get().getActiveSpecialScenarios()) {
            if (s instanceof ScenarioRole<?> scenarioRole) return scenarioRole;
        }
        return null;
    }

    public static Role roleOf(UHCPlayer uhcPlayer) {
        ScenarioRole<?> scenarioRole = activeScenarioRole();
        return scenarioRole == null ? null : scenarioRole.getRoleByUHCPlayer(uhcPlayer);
    }

    private List<UHCPlayer> resolve(Role viewer) {
        ScenarioRole<?> scenarioRole = activeScenarioRole();
        if (scenarioRole == null) return List.of();

        final List<UHCPlayer> collected = new ArrayList<>();
        scenarioRole.getPlayersRoles().forEach((uhcPlayer, role) -> {
            if (!includeSelf && viewer != null && viewer.getOwner() != null
                    && uhcPlayer.getUuid().equals(viewer.getOwner().getUuid())) return;
            if (!uhcPlayer.isPlaying()) return;

            boolean match = matchAll;
            if (!match) {
                for (Class<? extends Role> clazz : roleClasses) {
                    if (clazz.isInstance(role)) {
                        match = true;
                        break;
                    }
                }
            }
            if (!match && !camps.isEmpty() && role.getCamp() != null) {
                for (Camps c : camps) {
                    if (role.getCamp().isOrHasParent(c)) {
                        match = true;
                        break;
                    }
                }
            }
            if (match) {
                for (Class<? extends Role> excluded : excludeClasses) {
                    if (excluded.isInstance(role)) {
                        match = false;
                        break;
                    }
                }
            }
            if (!match) return;
            if (filter != null && !filter.test(uhcPlayer)) return;
            collected.add(uhcPlayer);
        });

        if (randomOrder) {
            Collections.shuffle(collected);
        } else {
            collected.sort(Comparator.comparing(p -> {
                Player bp = p.getPlayer();
                return bp != null ? bp.getName() : p.getUuid().toString();
            }, String.CASE_INSENSITIVE_ORDER));
        }
        if (max > 0 && collected.size() > max) {
            return new ArrayList<>(collected.subList(0, max));
        }
        return collected;
    }

    public static class KnowPlayerInfo {

        private final Supplier<List<UHCPlayer>> targets;
        private final boolean showRole;
        private final String customName;

        public KnowPlayerInfo(Supplier<List<UHCPlayer>> targets, boolean showRole, String customName) {
            this.targets = targets;
            this.showRole = showRole;
            this.customName = customName;
        }

        public List<UHCPlayer> resolve() {
            try {
                List<UHCPlayer> resolved = targets.get();
                return resolved == null ? List.of() : resolved;
            } catch (Throwable t) {
                return List.of();
            }
        }

        public boolean isShowRole() {
            return showRole;
        }

        public String getCustomName() {
            return customName;
        }
    }
}
