package net.novaproject.novauhc.scenario.role;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.craft.CraftAbilityManager;
import net.novaproject.novauhc.ability.craft.CraftableAbility;
import net.novaproject.novauhc.debug.DebugLog.AbilityDebug;
import net.novaproject.novauhc.utils.variable.VariableDescriptor;
import java.lang.reflect.Field;
import net.novaproject.novauhc.scenario.ScenarioSpi;

import net.novaproject.novauhc.ability.AbilitySuppression;
import net.novaproject.novauhc.ability.FormSet;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.event.UhcRoleEvents.UhcRolesDistributedEvent;
import net.novaproject.novauhc.event.UhcRoleEvents.UhcRoleDistributionEvent;
import net.novaproject.novauhc.event.UhcRoleEvents.UhcRoleAssignEvent;
import java.util.UUID;
import java.util.Set;
import java.util.Map;
import java.util.logging.Level;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Deque;
import java.util.Collections;
import java.util.ArrayDeque;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.utils.variable.Variables;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.game.PendingDeathManager.DeathAnnouncer;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.scenario.role.ui.ScenarioCampUi;
import net.novaproject.novauhc.scenario.role.ui.ScenarioRoleDispatcherUi;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.ui.CustomInventory;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public abstract class ScenarioRole<T extends Role> extends Scenario implements ScenarioSpi.IScenarioRole {

    private final Map<Class<? extends T>, Integer> default_roles = new HashMap<>();
    private final Map<Class<? extends T>, T> roleConfigs = new HashMap<>();
    private final Map<Class<? extends T>, RoleRegistration> registrations = new HashMap<>();

    private final Map<UHCPlayer, T> players_roles = new HashMap<>();
    protected boolean isgived = false;
    private CampWinPolicy.WinCondition<T> winCondition = null;
    private Class<? extends T> fillerRoleClass = null;

    @Var(lang = ScenarioVarLang.class, nameKey ="ROLE_COMPOSITION_OPEN_NAME", descKey = "ROLE_COMPOSITION_OPEN_DESC", type = VariableType.BOOLEAN)
    private boolean compositionOpen = false;

    @Var(lang = ScenarioVarLang.class, nameKey ="ROLE_GROUP_ANNOUNCE_NAME", descKey = "ROLE_GROUP_ANNOUNCE_DESC", type = VariableType.BOOLEAN)
    private boolean groupSizeAnnounce = false;

    @Var(lang = ScenarioVarLang.class, nameKey ="ROLE_GROUP_DIVISOR_NAME", descKey = "ROLE_GROUP_DIVISOR_DESC", type = VariableType.INTEGER, min = 1, max = 20)
    private int groupSizeDivisor = 5;

    @Var(lang = ScenarioVarLang.class, nameKey ="ROLE_GROUP_MIN_NAME", descKey = "ROLE_GROUP_MIN_DESC", type = VariableType.INTEGER, min = 1, max = 10)
    private int groupSizeMin = 2;

    @Var(lang = ScenarioVarLang.class, nameKey ="ROLE_GROUP_MAX_NAME", descKey = "ROLE_GROUP_MAX_DESC", type = VariableType.INTEGER, min = 1, max = 10)
    private int groupSizeMax = 5;

    @Var(lang = ScenarioVarLang.class, nameKey ="ROLE_GIVE_TIMER_NAME", descKey = "ROLE_GIVE_TIMER_DESC", type = VariableType.TIME, min = 0)
    private int rolesTimer = 0;

    private final Map<String, CampWinPolicy> winPolicies = new HashMap<>();
    private Camps customWinnerCamp = null;

    public int getRolesTimer() {
        return rolesTimer;
    }

    public boolean isRolesDistributed() {
        return isgived;
    }

    public void setRolesTimer(int rolesTimer) {
        this.rolesTimer = Math.max(0, rolesTimer);
    }

    public CampWinPolicy winPolicy(Camps camp) {
        return winPolicies.computeIfAbsent(camp.getName(), k -> new CampWinPolicy(camp));
    }

    public CampWinPolicy policyFor(Camps camp) {
        if (camp == null) return null;
        CampWinPolicy policy = winPolicies.get(camp.getName());
        if (policy != null) return policy;
        Camps parent = camp.getParent();
        if (parent != null) return winPolicies.get(parent.getName());
        return null;
    }

    public Collection<CampWinPolicy> getWinPolicies() {
        return Collections.unmodifiableCollection(winPolicies.values());
    }

    public String winGroupKey(UHCPlayer player, Role role) {

        Set<UUID> winBond = Bonds.VictoryLinks.winGroupOf(player.getUuid());
        if (!winBond.isEmpty()) {
            return "BOND:" + Collections.min(winBond);
        }
        Camps camp = role.getCamp();
        CampWinPolicy policy = policyFor(camp);
        if (policy != null && policy.getGrouping() == CampWinPolicy.Grouping.SOLO) {
            return "SOLO:" + player.getUuid();
        }
        return "CAMP:" + allianceCluster(baseGroupName(camp));
    }

    private String baseGroupName(Camps camp) {
        if (camp == null) return "";
        CampWinPolicy policy = policyFor(camp);
        if (policy != null && policy.getGrouping() == CampWinPolicy.Grouping.TOGETHER) {
            return camp.getName();
        }
        return camp.getParent() != null ? camp.getParent().getName() : camp.getName();
    }

    private String allianceCluster(String base) {
        Map<String, Set<String>> adjacency = new HashMap<>();
        for (CampWinPolicy policy : winPolicies.values()) {
            if (policy.getGrouping() != CampWinPolicy.Grouping.WINS_WITH) continue;
            String a = baseGroupName(policy.getCamp());
            for (Camps ally : policy.getAllies()) {
                String b = baseGroupName(ally);
                if (a.equals(b)) continue;
                adjacency.computeIfAbsent(a, k -> new HashSet<>()).add(b);
                adjacency.computeIfAbsent(b, k -> new HashSet<>()).add(a);
            }
        }
        if (!adjacency.containsKey(base)) return base;

        String representative = base;
        Set<String> visited = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        visited.add(base);
        queue.add(base);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.compareTo(representative) < 0) representative = current;
            for (String next : adjacency.getOrDefault(current, Collections.emptySet())) {
                if (visited.add(next)) queue.add(next);
            }
        }
        return representative;
    }

    private Camps representativeCamp(UHCPlayer player, Role role) {
        Camps camp = role.getCamp();
        CampWinPolicy policy = policyFor(camp);
        if (policy != null) {
            switch (policy.getGrouping()) {
                case WINS_WITH -> {
                    if (policy.getWinsWith() != null) return policy.getWinsWith();
                }
                case SOLO, TOGETHER -> {
                    return camp;
                }
                default -> { }
            }
        }
        return camp.getParent() != null ? camp.getParent() : camp;
    }

    public boolean isCompositionOpen() {
        return compositionOpen;
    }

    public boolean isGroupSizeAnnounce() {
        return groupSizeAnnounce;
    }

    public int getGroupSizeDivisor() {
        return groupSizeDivisor;
    }

    public int getGroupSizeMin() {
        return groupSizeMin;
    }

    public int getGroupSizeMax() {
        return groupSizeMax;
    }

    public void setGroupSizeAnnounce(boolean groupSizeAnnounce) {
        this.groupSizeAnnounce = groupSizeAnnounce;
    }

    public void setGroupSizeDivisor(int groupSizeDivisor) {
        this.groupSizeDivisor = Math.max(1, groupSizeDivisor);
    }

    public void setGroupSizeMin(int groupSizeMin) {
        this.groupSizeMin = Math.max(1, groupSizeMin);
    }

    public void setGroupSizeMax(int groupSizeMax) {
        this.groupSizeMax = Math.max(1, groupSizeMax);
    }

    public abstract Camps[] getCamps();

    public void setWinCondition(CampWinPolicy.WinCondition<T> condition) { this.winCondition = condition; }

    public boolean hasCustomWinCondition() { return winCondition != null; }

    public RoleRegistration addRole(Class<? extends T> roleClass) {
        RoleRegistration existing = registrations.get(roleClass);
        if (existing != null && roleConfigs.containsKey(roleClass)) {
            return existing;
        }
        try {
            T role = roleClass.getDeclaredConstructor().newInstance();
            roleConfigs.put(roleClass, role);
            default_roles.put(roleClass, 0);
            RoleRegistration registration = new RoleRegistration();
            registrations.put(roleClass, registration);
            return registration;
        } catch (Exception e) {
            throw new RuntimeException("Cannot register roles " + roleClass.getName(), e);
        }
    }

    public RoleRegistration getRegistration(Class<? extends T> roleClass) {
        return registrations.get(roleClass);
    }

    @SuppressWarnings("unchecked")
    public Class<? extends T> partnerClassOf(Class<? extends T> roleClass) {
        RoleRegistration own = registrations.get(roleClass);
        if (own != null && own.getPairedWith() != null && own.getPairedWith() != roleClass) {
            return (Class<? extends T>) own.getPairedWith();
        }
        for (Map.Entry<Class<? extends T>, RoleRegistration> e : registrations.entrySet()) {
            if (e.getValue().getPairedWith() == roleClass && e.getKey() != roleClass) {
                return e.getKey();
            }
        }
        return null;
    }

    public void setRoleAmout(Class<? extends T> roleClass,int amont){
        default_roles.replace(roleClass,amont);
        Class<? extends T> partner = partnerClassOf(roleClass);
        if (partner != null) default_roles.replace(partner, amont);
    }

    public void swapRoles(UHCPlayer player1, UHCPlayer player2) {
        T role1 = players_roles.get(player1);
        T role2 = players_roles.get(player2);
        if (role1 == null || role2 == null) return;
        players_roles.put(player1, role2);
        players_roles.put(player2, role1);
        role1.setOwner(player2);
        role2.setOwner(player1);
        role1.getAbilities().forEach(a -> a.setOwner(player2));
        role2.getAbilities().forEach(a -> a.setOwner(player1));
    }

    @SuppressWarnings("unchecked")
    public Map<Class<? extends T>, Class<? extends T>> pairedRoles() {
        Map<Class<? extends T>, Class<? extends T>> out = new LinkedHashMap<>();
        for (Map.Entry<Class<? extends T>, RoleRegistration> e : registrations.entrySet()) {
            Class<? extends Role> partner = e.getValue().getPairedWith();
            if (partner != null && partner != e.getKey()) {
                out.put(e.getKey(), (Class<? extends T>) partner);
            }
        }
        return out;
    }

    public int getRoleAmount(Class<? extends T> roleClass) {
        return default_roles.getOrDefault(roleClass, 0);
    }

    public void incrementRole(Class<? extends T> roleClass) {
        default_roles.put(roleClass, getRoleAmount(roleClass) + 1);
        Class<? extends T> partner = partnerClassOf(roleClass);
        if (partner != null) default_roles.put(partner, getRoleAmount(partner) + 1);
    }

    public void setRole(UHCPlayer player, T role) {
        if(role == null) return;
        players_roles.remove(player);
        players_roles.put(player, role);
        role.setOwner(player);
        RoleVariableProcessor.process(role,player,this);
        role.onGive(player);
        Bukkit.getPluginManager().callEvent(new UhcRoleAssignEvent(player, role));
    }

    public void decrementRole(Class<? extends T> roleClass) {
        int current = getRoleAmount(roleClass);
        if (current > 0) {
            default_roles.put(roleClass, current - 1);
        }
        Class<? extends T> partner = partnerClassOf(roleClass);
        if (partner != null) {
            int partnerCurrent = getRoleAmount(partner);
            if (partnerCurrent > 0) default_roles.put(partner, partnerCurrent - 1);
        }
    }

    public void setFillerRoleClass(Class<? extends T> roleClass) {
        this.fillerRoleClass = roleClass;
    }

    public Class<? extends T> getFillerRoleClass() {
        return fillerRoleClass;
    }

    public void ensureFillerRole(UHCPlayer player) {
        if (!isgived || player == null) return;
        if (players_roles.containsKey(player)) return;
        T filler = findFillerBase();
        if (filler == null) return;
        @SuppressWarnings("unchecked")
        T clone = (T) filler.clone();
        setRole(player, clone);
        try {
            clone.registerKnowPlayers();
            clone.sendKnowPlayers();
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.WARNING,
                    "[Reveal] reveals du filler tardif en échec pour " + clone.getName(), t);
        }
        Bukkit.getLogger().info("[Roles] Filler '" + clone.getName() + "' assigné à "
                + (player.getPlayer() != null ? player.getPlayer().getName() : player.getUuid())
                + " (reconnexion/late-join sans rôle)");
    }

    @SuppressWarnings("unchecked")
    public <R extends T> R getRoleConfig(Class<R> roleClass) {
        return (R) roleConfigs.get(roleClass);
    }

    public Map<Class<? extends T>, T> getRoleConfigs() {
        return Collections.unmodifiableMap(roleConfigs);
    }

    public Map<T, Integer> getDefault_roles() {
        Map<T, Integer> result = new TreeMap<>(Comparator.comparing(Role::getName));
        default_roles.forEach((clazz, amount) ->
                result.put(roleConfigs.get(clazz), amount)
        );
        return result;
    }

    public Document getRolesDocument() {
        Document doc = new Document();
        Map<String, Integer> rolesList = new HashMap<>();
        default_roles.forEach((clazz, amount) -> {
            T role = roleConfigs.get(clazz);
            if(amount > 0) rolesList.put(role.getName(),amount);

        });
        if(!rolesList.isEmpty()) doc.append("roles", rolesList);

        Map<String,Document> roleConfigsDoc = new HashMap<>();
        roleConfigs.forEach((clazz, role) -> {
            Document roleDoc = role.roleToDoc();
            if(!roleDoc.isEmpty()){
                roleConfigsDoc.put(role.getName(),roleDoc);
            }
        });
        if(!roleConfigsDoc.isEmpty()) doc.append("roleConfigs",roleConfigsDoc);

        if (fillerRoleClass != null) {
            T fillerRole = roleConfigs.get(fillerRoleClass);
            if (fillerRole != null) doc.append("filler", fillerRole.getName());
        }

        return doc;
    }

    public void loadRolesDocument(Document doc) {
        if (doc == null) return;

        if(doc.containsKey("roles")){
            Document rolesDoc = (Document) doc.get("roles");
            rolesDoc.forEach((roleName, amountObj) -> {
                int amount;
                if(amountObj instanceof Integer i) amount = i;
                else if(amountObj instanceof Double d) amount = d.intValue();
                else {
                    amount = 0;
                }

                roleConfigs.forEach((clazz, role) -> {
                    if(role.getName().equals(roleName)){
                        default_roles.put(clazz, amount);
                    }
                });
            });
        }

        if(doc.containsKey("roleConfigs")){
            Document roleConfigsDoc = (Document) doc.get("roleConfigs");
            roleConfigsDoc.forEach((roleName, roleDocObj) -> {
                if(roleDocObj instanceof Document roleDoc){
                    roleConfigs.forEach((clazz, role) -> {
                        if(role.getName().equals(roleName)){
                            role.docToRole(roleDoc);
                        }
                    });
                }
            });
        }

        if(doc.containsKey("filler")){
            String fillerName = doc.getString("filler");
            roleConfigs.forEach((clazz, role) -> {
                if(role.getName().equals(fillerName)) fillerRoleClass = clazz;
            });
        }
    }

    public void giveRoles() {

        Bukkit.broadcastMessage(LangManager.get().get(CoreLang.COMMON_GIVING_ROLES));

        customWinnerCamp = null;
        Bonds.clearAll();
        FormSet.clearAllStates();
        AbilitySuppression.clearAll();
        RoleStates.clearAll();

        int playerCount = UHCPlayerManager.get().getPlayingOnlineUHCPlayers().size();
        Map<Class<? extends T>, Integer> effectiveCounts = new HashMap<>();
        default_roles.forEach((clazz, amount) -> {
            int effective = amount;
            RoleRegistration registration = registrations.get(clazz);
            if (registration != null) {
                if (registration.getMinPlayers() > 0 && playerCount < registration.getMinPlayers()) {
                    Bukkit.getLogger().info("[Roles] " + clazz.getSimpleName() + " ignoré à la distribution : "
                            + playerCount + " joueurs < minPlayers=" + registration.getMinPlayers());
                    return;
                }
                int cap = registration.isUnique() ? 1 : registration.getMax();
                if (effective > cap) effective = cap;
                if (effective > 0 && effective < registration.getMin()) effective = registration.getMin();
            }
            if (effective > 0) effectiveCounts.put(clazz, effective);
        });

        applyCompositionConstraints(effectiveCounts);

        List<List<T>> groups = new ArrayList<>();
        Set<Class<? extends T>> pairHandled = new HashSet<>();
        for (Map.Entry<Class<? extends T>, RoleRegistration> entry : registrations.entrySet()) {
            Class<? extends T> clazz = entry.getKey();
            RoleRegistration registration = entry.getValue();
            T base = roleConfigs.get(clazz);
            if (base == null) continue;

            int groupSize = registration.isPairedSelf() ? 2 : registration.getGroupSize();
            if (groupSize >= 2) {
                int n = effectiveCounts.getOrDefault(clazz, 0) / groupSize;
                for (int i = 0; i < n; i++) {
                    List<T> group = new ArrayList<>();
                    for (int j = 0; j < groupSize; j++) group.add((T) base.clone());
                    groups.add(group);
                }
                effectiveCounts.merge(clazz, -n * groupSize, Integer::sum);
                continue;
            }

            if (!registration.getGroupedWith().isEmpty() && !pairHandled.contains(clazz)) {
                List<Class<? extends T>> chain = new ArrayList<>();
                chain.add(clazz);
                boolean resolvable = true;
                for (Class<? extends Role> memberRaw : registration.getGroupedWith()) {
                    @SuppressWarnings("unchecked")
                    Class<? extends T> member = (Class<? extends T>) memberRaw;
                    if (roleConfigs.get(member) == null) {
                        resolvable = false;
                        break;
                    }
                    if (!chain.contains(member)) chain.add(member);
                }
                if (resolvable && chain.size() >= 2) {
                    int n = Integer.MAX_VALUE;
                    for (Class<? extends T> member : chain) {
                        n = Math.min(n, effectiveCounts.getOrDefault(member, 0));
                    }
                    for (int i = 0; i < n; i++) {
                        List<T> group = new ArrayList<>();
                        for (Class<? extends T> member : chain) {
                            group.add((T) roleConfigs.get(member).clone());
                        }
                        groups.add(group);
                    }
                    for (Class<? extends T> member : chain) {
                        effectiveCounts.merge(member, -n, Integer::sum);
                        pairHandled.add(member);
                    }
                    continue;
                }
            }

            Class<? extends Role> otherRaw = registration.getPairedWith();
            if (otherRaw == null || pairHandled.contains(clazz)) continue;
            @SuppressWarnings("unchecked")
            Class<? extends T> other = (Class<? extends T>) otherRaw;
            T otherBase = roleConfigs.get(other);
            if (otherBase == null) continue;

            int n = Math.min(effectiveCounts.getOrDefault(clazz, 0), effectiveCounts.getOrDefault(other, 0));
            for (int i = 0; i < n; i++) {
                List<T> group = new ArrayList<>();
                group.add((T) base.clone());
                group.add((T) otherBase.clone());
                groups.add(group);
            }
            effectiveCounts.merge(clazz, -n, Integer::sum);
            effectiveCounts.merge(other, -n, Integer::sum);
            pairHandled.add(clazz);
            pairHandled.add(other);
        }
        Collections.shuffle(groups, new Random());

        List<T> pool = new ArrayList<>();
        effectiveCounts.forEach((clazz, amount) -> {
            T base = roleConfigs.get(clazz);
            for (int i = 0; i < amount; i++) {
                pool.add((T) base.clone());
            }
        });
        Collections.shuffle(pool, new Random());

        List<UHCPlayer> playersToAssign = new ArrayList<>(UHCPlayerManager.get().getPlayingOnlineUHCPlayers());
        Collections.shuffle(playersToAssign, new Random());

        @SuppressWarnings("unchecked")
        List<Role> eventPool = (List<Role>) pool;
        Bukkit.getPluginManager().callEvent(new UhcRoleDistributionEvent(playersToAssign, eventPool));

        List<UHCPlayer> unassigned = new ArrayList<>(playersToAssign);
        for (List<T> group : groups) {
            int size = group.size();
            if (unassigned.size() < size) {
                continue;
            }
            List<UHCPlayer> members = pickTeamGroup(unassigned, size);
            if (members == null) {
                members = new ArrayList<>(unassigned.subList(0, size));
                ensureGroupSharesTeam(members);
            }
            Collections.shuffle(members, new Random());
            unassigned.removeAll(members);

            for (int i = 0; i < size; i++) {
                assignOne(members.get(i), group.get(i));
            }

            for (int i = 0; i < size; i++) {
                T role = group.get(i);
                final Set<UUID> others = new HashSet<>();
                for (int j = 0; j < size; j++) {
                    if (j != i) others.add(members.get(j).getUuid());
                }
                others.forEach(role::addPartner);
                role.addKnowPlayer(KPIBuilder.ofRoles(Role.class)
                        .showRole().filter(up -> others.contains(up.getUuid())).build(role));
            }
        }

        Collections.shuffle(pool, new Random());
        T fillerBase = findFillerBase();
        for (UHCPlayer player : unassigned) {
            if (!pool.isEmpty()) {
                assignOne(player, pool.remove(0));
                continue;
            }
            T weighted = drawWeighted();
            if (weighted != null) {
                assignOne(player, weighted);
                continue;
            }
            if (fillerBase != null) {
                assignOne(player, (T) fillerBase.clone());
            } else {
                break;
            }
        }

        isgived = true;

        Bukkit.getPluginManager().callEvent(new UhcRolesDistributedEvent(
                Collections.unmodifiableMap(new HashMap<UHCPlayer, Role>(players_roles))));
    }

    private void assignOne(UHCPlayer player, T role) {
        players_roles.put(player, role);
        role.setOwner(player);
        RoleVariableProcessor.process(role, player, this);
        role.onGive(player);
        Bukkit.getPluginManager().callEvent(new UhcRoleAssignEvent(player, role));
    }

    private void applyCompositionConstraints(Map<Class<? extends T>, Integer> effectiveCounts) {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Class<? extends T> clazz : new ArrayList<>(effectiveCounts.keySet())) {
                RoleRegistration reg = registrations.get(clazz);
                if (reg == null) continue;
                if (reg.getRequires().isEmpty() && reg.getRequiresOneOf().isEmpty()
                        && reg.getExcludes().isEmpty()) continue;
                boolean violated = false;
                for (Class<? extends Role> req : reg.getRequires()) {
                    if (!effectiveCounts.containsKey(req)) { violated = true; break; }
                }
                if (!violated && !reg.getRequiresOneOf().isEmpty()) {
                    boolean anyPresent = false;
                    for (Class<? extends Role> req : reg.getRequiresOneOf()) {
                        if (effectiveCounts.containsKey(req)) { anyPresent = true; break; }
                    }
                    if (!anyPresent) violated = true;
                }
                if (!violated) {
                    for (Class<? extends Role> exc : reg.getExcludes()) {
                        if (effectiveCounts.containsKey(exc)) { violated = true; break; }
                    }
                }
                if (violated) {
                    effectiveCounts.remove(clazz);
                    changed = true;
                }
            }
        }
    }

    private T drawWeighted() {
        List<Class<? extends T>> candidates = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        int total = 0;
        for (Map.Entry<Class<? extends T>, RoleRegistration> entry : registrations.entrySet()) {
            RoleRegistration reg = entry.getValue();
            if (reg.getWeight() <= 0) continue;
            T base = roleConfigs.get(entry.getKey());
            if (base == null) continue;
            int cap = reg.isUnique() ? 1 : reg.getMax();
            if (assignedCount(entry.getKey()) >= cap) continue;
            candidates.add(entry.getKey());
            weights.add(reg.getWeight());
            total += reg.getWeight();
        }
        if (total <= 0) return null;
        int r = new Random().nextInt(total);
        for (int i = 0; i < candidates.size(); i++) {
            r -= weights.get(i);
            if (r < 0) return (T) roleConfigs.get(candidates.get(i)).clone();
        }
        return null;
    }

    private int assignedCount(Class<? extends T> clazz) {
        int count = 0;
        for (Role role : players_roles.values()) {
            if (clazz.isInstance(role)) count++;
        }
        return count;
    }

    private T findFillerBase() {
        if (fillerRoleClass != null) {
            T base = roleConfigs.get(fillerRoleClass);
            if (base != null) return base;
        }
        for (Map.Entry<Class<? extends T>, RoleRegistration> e : registrations.entrySet()) {
            if (e.getValue().isFiller()) {
                return roleConfigs.get(e.getKey());
            }
        }
        return null;
    }

    private List<UHCPlayer> pickTeamGroup(List<UHCPlayer> unassigned, int size) {
        for (UHCPlayer candidate : unassigned) {
            if (candidate.getTeam().isEmpty()) continue;
            List<UHCPlayer> members = new ArrayList<>();
            members.add(candidate);
            for (UHCPlayer mate : candidate.getTeam().get().getPlayers()) {
                if (mate != candidate && unassigned.contains(mate)) {
                    members.add(mate);
                    if (members.size() == size) return members;
                }
            }
        }
        return null;
    }

    private void ensureGroupSharesTeam(List<UHCPlayer> members) {
        if (members == null || members.size() < 2 || UHCManager.get().getTeam_size() <= 1) return;
        int teamSize = UHCManager.get().getTeam_size();

        for (UHCPlayer anchor : members) {
            if (anchor.getTeam().isPresent()
                    && anchor.getTeam().get().getPlayers().size() + members.size() - 1 <= teamSize) {
                for (UHCPlayer member : members) {
                    if (member != anchor) member.forceSetTeam(anchor.getTeam());
                }
                return;
            }
        }

        UHCTeamManager.get().createTeam(Math.max(teamSize, members.size()));
        List<UHCTeam> teams = UHCTeamManager.get().getTeams();
        if (teams.isEmpty()) return;
        UHCTeam team = teams.get(teams.size() - 1);
        for (UHCPlayer member : members) {
            member.forceSetTeam(Optional.ofNullable(team));
        }
    }

    public void giveRole(UHCPlayer player, T role) {
        players_roles.put(player, role);
        role.setOwner(player);

        RoleVariableProcessor.process(role,player,this);
        role.onGive(player);
        Bukkit.getPluginManager().callEvent(new UhcRoleAssignEvent(player, role));
    }

    public T getRoleByUHCPlayer(UHCPlayer player) {
        return players_roles.get(player);
    }

    public Map<UHCPlayer, T> getPlayersRoles() {
        return Collections.unmodifiableMap(players_roles);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public CustomInventory getMenu(Player player) {
        if (Variables.present(this.getClass())) {
            return new ScenarioRoleDispatcherUi<>(player, this);
        }
        return new ScenarioCampUi<>(player, this);
    }

    @Override
    public void onSec(Player p) {
        int timer = UHCManager.get().getTimer();

        UHCPlayer up = UHCPlayerManager.get().getPlayer(p);
        T role = up == null ? null : players_roles.get(up);
        if (role != null) role.onSec(p);

        int giveAt = rolesTimer > 0 ? rolesTimer : UHCManager.get().getPvpTimer();
        if (!isgived && timer > 0 && timer >= giveAt) {
            giveRoles();
        }
    }

    @Override
    public void setup() {
        super.setup();
        players_roles.forEach((player, role) -> role.onSetup());
    }

    @Override
    public void onStop() {
        super.onStop();
        Bonds.clearAll();
        FormSet.clearAllStates();
        AbilitySuppression.clearAll();
        RoleStates.clearAll();
    }

    @Override
    public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
        new HashMap<>(players_roles).forEach((p, role) -> role.onConsume(player, item, event));
    }

    @Override
    public void onDrop(PlayerDropItemEvent event) {
        players_roles.forEach((p, role) -> role.onDrop(p,event));
    }

    @Override
    public void onPlayerInteract(Player player, PlayerInteractEvent event) {
        new HashMap<>(players_roles).forEach((p, role) -> role.onInteract(player, event));
    }

    @Override
    public void onMove(Player player, PlayerMoveEvent event) {
        players_roles.forEach((p, role) -> role.onMove(p, event));
    }

    @Override
    public void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event) {
        players_roles.forEach((p, role) -> role.onHit(entity, dammager, event));
    }

    @Override
    public void onBow(Entity entity, Player player, EntityShootBowEvent event) {
        players_roles.forEach((p,role) -> role.onBow(entity,player,event));
    }

    @Override
    public void onKill(UHCPlayer killer, UHCPlayer victim) {
        new HashMap<>(players_roles).forEach((p, role) -> role.onKill(killer, victim));
    }

    @Override
    public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {
        players_roles.forEach((p, role) -> role.onTakeDamage(event));
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        new HashMap<>(players_roles).forEach((p, role) -> role.onBlockBreak(event));
    }

    @Override
    public void onProjectileHit(ProjectileHitEvent event) {
        new HashMap<>(players_roles).forEach((p, role) -> role.onProjectileHit(event));
    }

    @Override
    public void onPlayerInteractEntity(Player player, PlayerInteractEntityEvent event) {
        new HashMap<>(players_roles).forEach((p, role) -> role.onInteractEntity(event));
    }

    @Override
    public void onPlace(Player player, Block block, BlockPlaceEvent event) {
        new HashMap<>(players_roles).forEach((p, role) -> role.onBlockPlace(event));
    }

    @Override
    public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
        new HashMap<>(players_roles).forEach((p, role) -> role.onMobDeath(event));
    }

    public List<UHCPlayer> getPlayersByRoleName(String name) {
        List<UHCPlayer> list = new ArrayList<>();
        players_roles.forEach((player, role) -> {
            if (role.getName().equals(name)) list.add(player);
        });
        return list;
    }

    public Camps getWinningCamp() {
        if (customWinnerCamp != null) return customWinnerCamp;

        Map<String, Camps> groups = new HashMap<>();
        for (UHCPlayer player : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Role role = getRoleByUHCPlayer(player);
            if (role == null || role.getCamp() == null) continue;
            groups.put(winGroupKey(player, role), representativeCamp(player, role));
        }

        return groups.size() == 1 ? groups.values().iterator().next() : null;
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        new HashMap<>(players_roles).forEach((p, role) -> role.onDeath(uhcPlayer, killer, event));
    }

    @Override
    public boolean isWin() {
        if (winCondition != null) return isgived && winCondition.isWin(players_roles);

        for (CampWinPolicy policy : winPolicies.values()) {
            if (policy.getCondition() != null && policy.getCondition().test(players_roles)) {
                customWinnerCamp = policy.getCamp();
                return true;
            }
        }

        Set<String> groups = new HashSet<>();
        boolean requirementBlocked = false;
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Role role = getRoleByUHCPlayer(uhcPlayer);
            if (role == null || role.getCamp() == null) {
                groups.add("NOROLE:" + uhcPlayer.getUuid());
                continue;
            }
            groups.add(winGroupKey(uhcPlayer, role));
            CampWinPolicy policy = policyFor(role.getCamp());
            if (policy != null && policy.getRequirement() != null
                    && !policy.getRequirement().test(players_roles)) {
                requirementBlocked = true;
            }
        }

        return groups.size() <= 1 && !requirementBlocked;
    }

    @Override
    public boolean hascustomDeathMessage() {
        return isgived;
    }

    @Override
    public void sendCustomDeathMessage(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        broadcastRoleDeathMessage(uhcPlayer, killer);
        UHCManager.get().checkVictory();
    }

    public boolean showRoleScoreboard() { return true; }

    public void broadcastRoleDeathMessage(UHCPlayer uhcPlayer, UHCPlayer killer) {
        DeathAnnouncer.announceRole(uhcPlayer, getRoleByUHCPlayer(uhcPlayer));
    }

    public static class RoleVariableProcessor {

        public static void process(Object role, UHCPlayer player, ScenarioRole scenarioRole) {
            for (VariableDescriptor d : Variables.of(role)) {
                Field field = d.field();

                try {
                    switch (d.type()) {
                        case ABILITY -> handleAbility(field, role, player, scenarioRole);
                        case INTEGER, TIME -> {
                            Object value = field.get(role);
                            if (value instanceof Number n) field.set(role, n.intValue());
                        }
                        case DOUBLE, PERCENTAGE -> {
                            Object value = field.get(role);
                            if (value instanceof Number n) field.set(role, n.doubleValue());
                        }
                        case BOOLEAN -> {
                            Object value = field.get(role);
                            if (value instanceof Boolean b) field.set(role, b);
                        }
                        default -> {  }
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().log(Level.SEVERE,
                            "[@Var] échec sur le champ '" + field.getName() + "' du rôle "
                                    + role.getClass().getSimpleName() + " (type=" + d.type() + ")", e);
                }
            }
        }

        private static void handleAbility(Field field, Object role, UHCPlayer player, ScenarioRole scenarioRole)
                throws Exception {

            if (!Ability.class.isAssignableFrom(field.getType())) {
                throw new IllegalStateException(
                        "@Var(type=ABILITY) doit être Ability sur le champ " + field.getName());
            }

            Ability base = (Ability) field.get(role);
            if (base == null) {
                AbilityDebug.roleVariableSkip(null, field.getName(), "champ null");
                return;
            }
            if (!base.active()) {
                AbilityDebug.roleVariableSkip(base, field.getName(), "active()==false");
                return;
            }

            Ability instance = base.clone();
            Role playerRole = scenarioRole.getRoleByUHCPlayer(player);
            instance.setOwner(playerRole.getOwner());
            playerRole.getAbilities().add(instance);
            AbilityDebug.roleVariableClone(instance, playerRole.getOwner());

            if (instance instanceof CraftableAbility craftable) {
                CraftAbilityManager.register(craftable);
            }
        }
    }
}
