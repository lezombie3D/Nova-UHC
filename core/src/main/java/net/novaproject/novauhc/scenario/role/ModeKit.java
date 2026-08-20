package net.novaproject.novauhc.scenario.role;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.event.UhcRoleEvents.UhcRolesDistributedEvent;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;
import net.novaproject.novauhc.player.UHCPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;

public final class ModeKit<T extends Role> {

    private final ScenarioRole<T> scenario;
    private final List<Runnable> steps = new ArrayList<>();
    private final List<BondSpec> bonds = new ArrayList<>();
    private final List<PickSpec> picks = new ArrayList<>();
    private final List<RevealSpec> reveals = new ArrayList<>();
    private final List<Consumer<Map<UHCPlayer, Role>>> distributedCallbacks = new ArrayList<>();
    private boolean applied = false;

    private ModeKit(ScenarioRole<T> scenario) {
        this.scenario = scenario;
    }

    public static <T extends Role> ModeKit<T> of(ScenarioRole<T> scenario) {
        return new ModeKit<>(scenario);
    }

    public ModeKit<T> rolesTimer(int seconds) {
        steps.add(() -> scenario.setRolesTimer(seconds));
        return this;
    }

    public ModeKit<T> camp(Camps camp, Consumer<CampWinPolicy> wins) {
        steps.add(() -> wins.accept(scenario.winPolicy(camp)));
        return this;
    }

    public ModeKit<T> winCondition(CampWinPolicy.WinCondition<T> condition) {
        steps.add(() -> scenario.setWinCondition(condition));
        return this;
    }

    public ModeKit<T> onDistributed(Runnable callback) {
        distributedCallbacks.add(roles -> callback.run());
        return this;
    }

    public ModeKit<T> onDistributed(Consumer<Map<UHCPlayer, Role>> callback) {
        distributedCallbacks.add(callback);
        return this;
    }

    public ModeKit<T> pickRandom(int count, Predicate<Role> roleFilter, Consumer<List<UHCPlayer>> action) {
        picks.add(new PickSpec(count, roleFilter, action));
        return this;
    }

    public ModeKit<T> revealWithin(Camps camp) {
        return revealTo(camp, camp);
    }

    public ModeKit<T> revealTo(Camps viewers, Camps targets) {
        if (viewers != null && targets != null) {
            reveals.add(new RevealSpec(viewers, targets));
        }
        return this;
    }

    public ModeKit<T> role(Class<? extends T> roleClass) {
        steps.add(() -> scenario.addRole(roleClass));
        return this;
    }

    public ModeKit<T> role(Class<? extends T> roleClass, Consumer<RoleRegistration> config) {
        steps.add(() -> config.accept(scenario.addRole(roleClass)));
        return this;
    }

    @SafeVarargs
    public final ModeKit<T> uniqueRoles(Class<? extends T>... roleClasses) {
        for (Class<? extends T> roleClass : roleClasses) {
            steps.add(() -> scenario.addRole(roleClass).unique());
        }
        return this;
    }

    public ModeKit<T> filler(Class<? extends T> roleClass) {
        steps.add(() -> {
            scenario.addRole(roleClass).fillerRole();
            scenario.setFillerRoleClass(roleClass);
        });
        return this;
    }

    public ModeKit<T> groupAnnounce(int divisor, int min, int max) {
        steps.add(() -> {
            scenario.setGroupSizeAnnounce(true);
            scenario.setGroupSizeDivisor(divisor);
            scenario.setGroupSizeMin(min);
            scenario.setGroupSizeMax(max);
        });
        return this;
    }

    public ModeKit<T> duo(Class<? extends T> first, Class<? extends T> second) {
        return duo(first, second, r -> {}, r -> {});
    }

    public ModeKit<T> duo(Class<? extends T> first, Class<? extends T> second,
                          Consumer<RoleRegistration> firstConfig, Consumer<RoleRegistration> secondConfig) {
        steps.add(() -> {
            firstConfig.accept(scenario.addRole(first).pairWith(second));
            secondConfig.accept(scenario.addRole(second));
        });
        return this;
    }

    public ModeKit<T> couple(Class<? extends T> first, Class<? extends T> second) {
        return couple(first, second, r -> {}, r -> {});
    }

    public ModeKit<T> couple(Class<? extends T> first, Class<? extends T> second,
                             Consumer<RoleRegistration> firstConfig, Consumer<RoleRegistration> secondConfig) {
        duo(first, second, firstConfig, secondConfig);
        bonds.add(new BondSpec(List.of(first, second),
                EnumSet.of(Bonds.VictoryLinks.BondType.DIE_TOGETHER, Bonds.VictoryLinks.BondType.WIN_TOGETHER),
                null, Bonds.AnnounceMode.NONE, 0.0, false, true));
        return this;
    }

    public ModeKit<T> squad(int size, Class<? extends T> roleClass) {
        return squad(size, roleClass, r -> {});
    }

    public ModeKit<T> squad(int size, Class<? extends T> roleClass, Consumer<RoleRegistration> config) {
        steps.add(() -> config.accept(scenario.addRole(roleClass).group(size)));
        return this;
    }

    @SafeVarargs
    public final ModeKit<T> group(Class<? extends T> first, Class<? extends T>... others) {
        steps.add(() -> {
            RoleRegistration firstReg = scenario.addRole(first);
            for (Class<? extends T> other : others) {
                scenario.addRole(other);
            }
            firstReg.groupWith(others);
        });
        return this;
    }

    public ModeKit<T> bond(Class<? extends T> roleClass, Consumer<BondOptions> config) {
        return bond(List.of(roleClass), config);
    }

    public ModeKit<T> bond(Class<? extends T> first, Class<? extends T> second, Consumer<BondOptions> config) {
        return bond(List.of(first, second), config);
    }

    public ModeKit<T> bond(List<Class<? extends T>> roleClasses, Consumer<BondOptions> config) {
        BondOptions options = new BondOptions();
        config.accept(options);
        boolean effectful = !options.types.isEmpty()
                || options.announceMode != Bonds.AnnounceMode.NONE
                || options.chat;
        if (effectful && !roleClasses.isEmpty()) {
            bonds.add(new BondSpec(List.copyOf(roleClasses),
                    options.types.isEmpty()
                            ? EnumSet.noneOf(Bonds.VictoryLinks.BondType.class)
                            : EnumSet.copyOf(options.types),
                    options.swapCamp, options.announceMode, options.revealRadius, options.chat, false));
        }
        return this;
    }

    public void apply() {
        if (applied) return;
        applied = true;
        steps.forEach(Runnable::run);
        if (!bonds.isEmpty() || !picks.isEmpty() || !reveals.isEmpty() || !distributedCallbacks.isEmpty()) {
            Bukkit.getPluginManager().registerEvents(
                    new DistributionListener(scenario, List.copyOf(bonds), List.copyOf(picks),
                            List.copyOf(reveals), List.copyOf(distributedCallbacks)),
                    Main.get());
        }
    }

    public static final class BondOptions {

        private final EnumSet<Bonds.VictoryLinks.BondType> types = EnumSet.noneOf(Bonds.VictoryLinks.BondType.class);
        private Camps swapCamp;
        private Bonds.AnnounceMode announceMode = Bonds.AnnounceMode.NONE;
        private double revealRadius;
        private boolean chat;

        public BondOptions dieTogether() {
            types.add(Bonds.VictoryLinks.BondType.DIE_TOGETHER);
            return this;
        }

        public BondOptions winTogether() {
            types.add(Bonds.VictoryLinks.BondType.WIN_TOGETHER);
            return this;
        }

        public BondOptions sharedPerception() {
            types.add(Bonds.VictoryLinks.BondType.SHARED_PERCEPTION);
            return this;
        }

        public BondOptions swapOnDeath(Camps camp) {
            types.add(Bonds.VictoryLinks.BondType.CAMP_SWAP_ON_DEATH);
            this.swapCamp = camp;
            return this;
        }

        public BondOptions announce() {
            this.announceMode = Bonds.AnnounceMode.REVEALED;
            return this;
        }

        public BondOptions announceHidden() {
            this.announceMode = Bonds.AnnounceMode.HIDDEN;
            return this;
        }

        public BondOptions revealOnProximity(double radius) {
            this.announceMode = Bonds.AnnounceMode.PROXIMITY;
            this.revealRadius = radius;
            return this;
        }

        public BondOptions chat() {
            this.chat = true;
            return this;
        }
    }

    private record BondSpec(List<Class<? extends Role>> roleClasses,
                            Set<Bonds.VictoryLinks.BondType> types,
                            Camps swapCamp,
                            Bonds.AnnounceMode announceMode,
                            double revealRadius,
                            boolean chat,
                            boolean pairwiseOnly) {
    }

    private record PickSpec(int count,
                            Predicate<Role> roleFilter,
                            Consumer<List<UHCPlayer>> action) {
    }

    private record RevealSpec(Camps viewers, Camps targets) {
    }

    private static final class DistributionListener implements Listener {

        private final ScenarioRole<?> scenario;
        private final List<BondSpec> bonds;
        private final List<PickSpec> picks;
        private final List<RevealSpec> reveals;
        private final List<Consumer<Map<UHCPlayer, Role>>> callbacks;

        private DistributionListener(ScenarioRole<?> scenario, List<BondSpec> bonds, List<PickSpec> picks,
                                     List<RevealSpec> reveals, List<Consumer<Map<UHCPlayer, Role>>> callbacks) {
            this.scenario = scenario;
            this.bonds = bonds;
            this.picks = picks;
            this.reveals = reveals;
            this.callbacks = callbacks;
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onRolesDistributed(UhcRolesDistributedEvent event) {
            if (!scenario.isActive()) return;
            Map<UHCPlayer, Role> roles = event.getRoles();
            reveals.forEach(spec -> runSafely(() -> applyReveal(spec, roles)));
            bonds.forEach(spec -> runSafely(() -> applyBond(spec, roles)));
            picks.forEach(spec -> runSafely(() -> applyPick(spec, roles)));
            callbacks.forEach(callback -> runSafely(() -> callback.accept(roles)));
        }

        private void applyReveal(RevealSpec spec, Map<UHCPlayer, Role> roles) {
            for (Map.Entry<UHCPlayer, Role> entry : roles.entrySet()) {
                Role role = entry.getValue();
                if (role == null || role.getCamp() == null) continue;
                if (!role.getCamp().isOrHasParent(spec.viewers())) continue;
                role.addKnowPlayer(KPIBuilder.ofCamp(spec.targets()).showRole().build(role));
            }
        }

        private void runSafely(Runnable action) {
            try {
                action.run();
            } catch (Throwable t) {
                Bukkit.getLogger().log(Level.WARNING, "[ModeKit] étape post-distribution en échec", t);
            }
        }

        private void applyBond(BondSpec spec, Map<UHCPlayer, Role> roles) {
            Map<UUID, Role> matched = new LinkedHashMap<>();
            for (Map.Entry<UHCPlayer, Role> entry : roles.entrySet()) {
                UHCPlayer up = entry.getKey();
                Role role = entry.getValue();
                if (up == null || role == null || !up.isPlaying() || up.getPlayer() == null) continue;
                for (Class<? extends Role> roleClass : spec.roleClasses()) {
                    if (roleClass.isInstance(role)) {
                        matched.put(up.getUuid(), role);
                        break;
                    }
                }
            }
            if (matched.size() < 2) return;

            List<List<UUID>> bondGroups = new ArrayList<>();
            Set<UUID> grouped = new HashSet<>();
            for (Map.Entry<UUID, Role> entry : matched.entrySet()) {
                if (grouped.contains(entry.getKey())) continue;
                List<UUID> members = new ArrayList<>();
                members.add(entry.getKey());
                for (UUID partner : entry.getValue().getPartners()) {
                    if (matched.containsKey(partner) && !grouped.contains(partner)) {
                        members.add(partner);
                    }
                }
                if (members.size() >= 2) {
                    grouped.addAll(members);
                    bondGroups.add(members);
                }
            }
            if (bondGroups.isEmpty()) {
                if (spec.pairwiseOnly()) {
                    Bukkit.getLogger().info("[ModeKit] bond de couple ignoré : aucun appariement par partners parmi "
                            + matched.size() + " porteurs.");
                    return;
                }
                bondGroups.add(new ArrayList<>(matched.keySet()));
            } else if (grouped.size() < matched.size()) {
                Bukkit.getLogger().info("[ModeKit] bond : " + (matched.size() - grouped.size())
                        + " porteur(s) sans partner apparié, non lié(s).");
            }

            for (List<UUID> members : bondGroups) {
                Bonds.applyToGroup(members, spec.types(), spec.swapCamp(),
                        spec.announceMode(), spec.revealRadius(), spec.chat());
            }
        }

        private void applyPick(PickSpec spec, Map<UHCPlayer, Role> roles) {
            if (spec.count() <= 0) return;
            List<UHCPlayer> candidates = new ArrayList<>();
            for (Map.Entry<UHCPlayer, Role> entry : roles.entrySet()) {
                UHCPlayer up = entry.getKey();
                Role role = entry.getValue();
                if (up == null || role == null || !up.isPlaying() || up.getPlayer() == null) continue;
                if (spec.roleFilter() != null && !spec.roleFilter().test(role)) continue;
                candidates.add(up);
            }
            if (candidates.isEmpty()) return;
            Collections.shuffle(candidates, new Random());
            List<UHCPlayer> chosen = new ArrayList<>(
                    candidates.subList(0, Math.min(spec.count(), candidates.size())));
            spec.action().accept(chosen);
        }
    }
}
