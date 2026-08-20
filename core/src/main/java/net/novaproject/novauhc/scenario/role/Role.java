package net.novaproject.novauhc.scenario.role;

import net.novaproject.novauhc.scenario.ScenarioSpi;

import net.novaproject.novauhc.utils.variable.VariableDescriptor;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder.KnowPlayerInfo;
import net.novaproject.novauhc.event.UhcRoleEvents.UhcCampChangeEvent;
import java.util.UUID;
import java.util.Map;
import java.util.logging.Level;
import java.util.List;
import java.util.function.Function;
import java.util.function.Consumer;
import java.util.ArrayList;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.utils.variable.Variables;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;
import net.novaproject.novauhc.lang.LangManager;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityManager;
import net.novaproject.novauhc.ability.AbilityHooks.Given;
import net.novaproject.novauhc.ability.AbilityHooks.Ticking;
import net.novaproject.novauhc.ability.AbilityHooks.Moving;
import net.novaproject.novauhc.ability.AbilityHooks.Attacking;
import net.novaproject.novauhc.ability.AbilityHooks.Hurtable;
import net.novaproject.novauhc.ability.AbilityHooks.Dropping;
import net.novaproject.novauhc.ability.AbilityHooks.Dying;
import net.novaproject.novauhc.ability.AbilityHooks.Killing;
import net.novaproject.novauhc.ability.AbilityHooks.Clicking;
import net.novaproject.novauhc.ability.AbilityHooks.Consuming;
import net.novaproject.novauhc.ability.AbilityHooks.Shooting;
import net.novaproject.novauhc.ability.AbilityHooks.Mining;
import net.novaproject.novauhc.ability.AbilityHooks.Building;
import net.novaproject.novauhc.ability.AbilityHooks.ProjectileImpacting;
import net.novaproject.novauhc.ability.AbilityHooks.EntityInteracting;
import net.novaproject.novauhc.ability.AbilityHooks.MobKilling;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bson.Document;
import org.bukkit.Sound;
import org.bukkit.Bukkit;
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

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;

@Getter
@Setter
public abstract class Role implements Cloneable, ScenarioSpi.IRole {
    @Getter(AccessLevel.NONE)
    @Var(lang = CoreLang.class, nameKey = "COMMON_ROLE_VAR_STRENGTH_NAME", descKey = "COMMON_ROLE_VAR_STRENGTH_DESC", type = VariableType.PERCENTAGE)
    private double strength = 0.20;
    @Getter(AccessLevel.NONE)
    @Var(lang = CoreLang.class, nameKey = "COMMON_ROLE_VAR_RESISTANCE_NAME", descKey = "COMMON_ROLE_VAR_RESISTANCE_DESC", type = VariableType.PERCENTAGE)
    private double resistance = 0.20;
    @Getter(AccessLevel.NONE)
    @Var(lang = CoreLang.class, nameKey = "COMMON_ROLE_VAR_STRENGTH_CRITIC_NAME", descKey = "COMMON_ROLE_VAR_STRENGTH_CRITIC_DESC", type = VariableType.PERCENTAGE)
    private double strengthCritic = 0.50;

    private Set<Ability> abilities = new HashSet<>();

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private List<KnowPlayerInfo> knowPlayers = new ArrayList<>();

    public abstract String getName();

    public List<String> getDescriptionLines(Player player) {
        return List.of();
    }

    public void appendFeatures(RoleDescription features, Player player) {
    }

    public String describeAbility(Ability ability, Player player) {
        return null;
    }

    public void sendDescription(Player player) {
        Camps c = getCamp();
        ScenarioRole<?> scenario = KPIBuilder.activeScenarioRole();
        String color = scenario != null ? scenario.getColor() : "§6";
        String prefix = scenario != null ? scenario.getPrefix() : "";
        LangManager lang = LangManager.get();

        String title = lang.get(CoreLang.COMMON_ROLE_DESC_TITLE, player,
                Map.of("%role%", color + (c != null ? c.getColor() : "") + "§l" + getName()));

        RoleDescription desc = RoleDescription.of(player)
                .line(CoreLang.COMMON_ROLE_DESC_SEPARATOR)
                .space()
                .raw(prefix.isEmpty() ? title : prefix + " " + title)
                .space();

        List<String> objective = objectiveLines(player, c);
        if (!objective.isEmpty()) {
            desc.raw(color + lang.get(CoreLang.COMMON_ROLE_DESC_OBJECTIVE, player));
            objective.forEach(line -> desc.raw(" " + line));
            desc.space();
        }

        RoleDescription features = RoleDescription.of(player);
        getDescriptionLines(player).forEach(line -> features.raw(" " + line));
        appendFeatures(features, player);
        if (!features.isEmpty()) {
            desc.raw(color + lang.get(CoreLang.COMMON_ROLE_DESC_FEATURES, player))
                    .append(features)
                    .space();
        }

        desc.raw(color + lang.get(CoreLang.COMMON_ROLE_DESC_ABILITIES, player))
                .abilities(this, color)
                .space()
                .line(CoreLang.COMMON_ROLE_DESC_SEPARATOR)
                .send();
    }

    private List<String> objectiveLines(Player player, Camps c) {
        List<String> lines = new ArrayList<>();
        ScenarioRole<?> scenario = KPIBuilder.activeScenarioRole();
        if (scenario == null || c == null) return lines;
        if (getOwner() != null && !Bonds.VictoryLinks.winGroupOf(getOwner().getUuid()).isEmpty()) {
            lines.add(LangManager.get().get(CoreLang.COMMON_ROLE_DESC_WIN_BOND, player));
            return lines;
        }
        CampWinPolicy policy = scenario.policyFor(c);
        if (policy != null || !scenario.hasCustomWinCondition()) {
            CampWinPolicy.Grouping grouping = policy != null
                    ? policy.getGrouping()
                    : CampWinPolicy.Grouping.PARENT_GROUP;
            if (grouping == CampWinPolicy.Grouping.WINS_WITH && policy.getAllies().isEmpty()) {
                grouping = CampWinPolicy.Grouping.PARENT_GROUP;
            }
            switch (grouping) {
                case SOLO -> lines.add(LangManager.get().get(CoreLang.COMMON_ROLE_DESC_WIN_SOLO, player));
                case TOGETHER -> lines.add(LangManager.get().get(CoreLang.COMMON_ROLE_DESC_WIN_CAMP, player,
                        Map.of("%camp%", c.getColor() + c.getName())));
                case WINS_WITH -> {
                    List<String> allyNames = new ArrayList<>();
                    for (Camps ally : policy.getAllies()) {
                        allyNames.add(ally.getColor() + ally.getName());
                    }
                    lines.add(LangManager.get().get(CoreLang.COMMON_ROLE_DESC_WIN_WITH, player,
                            Map.of("%camp%", c.getColor() + c.getName(),
                                    "%allies%", String.join(", ", allyNames))));
                }
                default -> {
                    CampWinPolicy inverse = null;
                    for (CampWinPolicy other : scenario.getWinPolicies()) {
                        if (other.getGrouping() != CampWinPolicy.Grouping.WINS_WITH) continue;
                        for (Camps ally : other.getAllies()) {
                            if (c.is(ally) || c.isOrHasParent(ally)) {
                                inverse = other;
                                break;
                            }
                        }
                        if (inverse != null) break;
                    }
                    if (inverse != null) {
                        lines.add(LangManager.get().get(CoreLang.COMMON_ROLE_DESC_WIN_WITH, player,
                                Map.of("%camp%", c.getColor() + c.getName(),
                                        "%allies%", inverse.getCamp().getColor() + inverse.getCamp().getName())));
                    } else {
                        Camps group = c.getParent() != null ? c.getParent() : c;
                        lines.add(LangManager.get().get(CoreLang.COMMON_ROLE_DESC_WIN_CAMP, player,
                                Map.of("%camp%", group.getColor() + group.getName())));
                    }
                }
            }
        }
        return lines;
    }

    public void addKnowPlayer(KnowPlayerInfo info) {
        if (info != null) knowPlayers.add(info);
    }

    public List<KnowPlayerInfo> getKnowPlayers() {
        return knowPlayers;
    }

    public void registerKnowPlayers() {
    }

    public void sendKnowPlayers() {
        if (knowPlayers.isEmpty()) return;
        UHCPlayer ownerPlayer = getOwner();
        if (ownerPlayer == null) return;
        Player player = ownerPlayer.getPlayer();
        if (player == null || !player.isOnline()) return;

        player.sendMessage(" ");
        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1.0F, 1.2F);

        for (KnowPlayerInfo info : knowPlayers) {
            List<UHCPlayer> targets = info.resolve();
            if (targets.isEmpty()) continue;

            if (targets.size() == 1) {
                UHCPlayer target = targets.get(0);
                Player targetPlayer = target.getPlayer();
                String targetName = targetPlayer != null ? targetPlayer.getName() : "?";
                Role targetRole = KPIBuilder.roleOf(target);

                Camps perceivedCamp = targetRole != null ? targetRole.getPerceivedCamp(player) : null;
                if (info.isShowRole() && targetRole != null) {
                    LangManager.get().send(CoreLang.COMMON_KPI_SHOW_ROLE, player,
                            Map.of("%player%", targetName,
                                    "%role%", targetRole.getPerceivedDisplay(player)));
                } else if (perceivedCamp != null) {
                    LangManager.get().send(CoreLang.COMMON_KPI_IN_CAMP, player,
                            Map.of("%player%", targetName,
                                    "%camp%", perceivedCamp.getColor() + perceivedCamp.getName()));
                } else {
                    LangManager.get().send(CoreLang.COMMON_KPI_KNOWN, player,
                            Map.of("%player%", targetName));
                }
                continue;
            }

            String header = info.getCustomName() != null
                    ? info.getCustomName()
                    : LangManager.get().get(CoreLang.COMMON_KPI_LIST_DEFAULT_NAME, player);
            LangManager.get().send(CoreLang.COMMON_KPI_LIST_HEADER, player,
                    Map.of("%name%", header, "%count%", targets.size()));

            for (UHCPlayer target : targets) {
                Player targetPlayer = target.getPlayer();
                String targetName = targetPlayer != null ? targetPlayer.getName() : "?";
                Role targetRole = KPIBuilder.roleOf(target);
                String suffix = info.isShowRole() && targetRole != null
                        ? " §8(" + targetRole.getPerceivedDisplay(player) + "§8)"
                        : "";
                player.sendMessage(LangManager.get().get(CoreLang.COMMON_KPI_LIST_LINE, player,
                        Map.of("%player%", targetName)) + suffix);
            }
        }
    }

    private Camps camp;
    @Getter(AccessLevel.NONE)
    @Var(lang = CoreLang.class, nameKey = "COMMON_ROLE_VAR_SPEED_NAME", descKey = "COMMON_ROLE_VAR_SPEED_DESC", type = VariableType.PERCENTAGE)
    private double speed = 0.20;

    public double getSpeedPercent(){
        return speed;
    }
    public boolean overridesSpeedPercent(){
        return getSpeedPercent() != 0.20;
    }
    public double getStrengthPercent(){
        return strength;
    }
    public double getResistancePercent(){
        return resistance;
    }
    public double getStrengthCriticPercent(){
        return strengthCritic;
    }

    @Getter(AccessLevel.NONE)
    @Var(name = "Absorption pomme d'or", desc = "Demi-coeurs d'absorption donnés par une pomme d'or (4 = vanilla).", type = VariableType.INTEGER, min = 0, max = 40)
    private int goldenAppleAbsorptionHearts = 4;

    @Getter(AccessLevel.NONE)
    @Var(name = "Niveau régén pomme d'or", desc = "Niveau de l'effet Régénération donné par une pomme d'or (2 = vanilla, 0 = aucune régén).", type = VariableType.INTEGER, min = 0, max = 5)
    private int goldenAppleRegenLevel = 2;

    @Getter(AccessLevel.NONE)
    @Var(name = "Régén pomme d'or", desc = "Demi-coeurs rendus par la régénération d'une pomme d'or (4 = vanilla) — la durée de l'effet est calculée automatiquement.", type = VariableType.INTEGER, min = 0, max = 40)
    private int goldenAppleRegenHearts = 4;

    public int getGoldenAppleAbsorptionHearts() {
        return goldenAppleAbsorptionHearts;
    }

    public int getGoldenAppleRegenLevel() {
        return goldenAppleRegenLevel;
    }

    public int getGoldenAppleRegenHearts() {
        return goldenAppleRegenHearts;
    }

    private UHCPlayer owner;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private List<UUID> partners = new ArrayList<>();

    public List<UUID> getPartners() {
        return partners;
    }

    public void addPartner(UUID partner) {
        if (partner != null) partners.add(partner);
    }

    public UUID getPartnerUuid() {
        return partners.isEmpty() ? null : partners.get(0);
    }

    public boolean isCompositionAlive() {
        if (owner != null && owner.isStillInGame()) return true;
        for (UUID partner : partners) {
            UHCPlayer other = UHCPlayerManager.get().getPlayer(partner);
            if (other != null && other.isStillInGame()) return true;
        }
        return false;
    }

    public void setPartnerUuid(UUID partner) {
        partners.clear();
        if (partner != null) partners.add(partner);
    }

    public String getColor() {
        return camp.getColor();
    }

    public void setCamp(Camps camp) {
        Camps old = this.camp;
        this.camp = camp;
        if (old != camp && getOwner() != null) {
            Bukkit.getPluginManager().callEvent(
                    new UhcCampChangeEvent(getOwner(), this, old, camp));
        }
    }

    public static final class FakeIdentity {
        public final String display;
        public final Camps camp;
        public FakeIdentity(String display, Camps camp) {
            this.display = display;
            this.camp = camp;
        }
    }

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private List<Function<Player, FakeIdentity>> facades = new ArrayList<>();

    public void addFacade(Function<Player, FakeIdentity> facade) {
        if (facade != null) facades.add(facade);
    }

    public void clearFacades() {
        facades.clear();
    }

    public boolean hasFacades() {
        return !facades.isEmpty();
    }

    public boolean hasFacadeFor(Player viewer) {
        if (viewer == null) return false;
        for (Function<Player, FakeIdentity> f : facades) {
            if (f.apply(viewer) != null) return true;
        }
        return false;
    }

    public FakeIdentity perceivedIdentity(Player viewer) {
        if (viewer != null) {

            if (getOwner() != null
                    && Bonds.VictoryLinks.sharesPerception(viewer.getUniqueId(), getOwner().getUuid())) {
                Camps real = getCamp();
                return new FakeIdentity((real != null ? real.getColor() : "") + getName(), real);
            }
            for (Function<Player, FakeIdentity> f : facades) {
                FakeIdentity id = f.apply(viewer);
                if (id != null) return id;
            }
        }
        Camps c = getCamp();
        String display = (c != null ? c.getColor() : "") + getName();
        return new FakeIdentity(display, c);
    }

    public String getPerceivedDisplay(Player viewer) {
        return perceivedIdentity(viewer).display;
    }

    public Camps getPerceivedCamp(Player viewer) {
        FakeIdentity id = perceivedIdentity(viewer);
        return id.camp != null ? id.camp : getCamp();
    }

    public ItemCreator getItem() {
        Camps c = getCamp();
        return new ItemCreator(getIconMaterial()).setName((c != null ? c.getColor() : "§f") + getName());
    }

    public Material getIconMaterial() {
        return Material.NETHER_STAR;
    }

    private enum Gate { NONE, USABLE, ACTIVE }

    private <H> void dispatch(Gate gate, Class<H> hook, Consumer<H> call) {
        if (getAbilities().isEmpty()) return;
        java.util.function.Predicate<Ability> filter =
                gate == Gate.USABLE ? Ability::isUsable : gate == Gate.ACTIVE ? Ability::active : null;
        AbilityManager.get().dispatch(getAbilities(), hook, filter, call);
    }

    public void onGive(UHCPlayer uhcPlayer) {
        Player player = uhcPlayer.getPlayer();
        if (player != null && player.isOnline()) {
            try {
                sendDescription(player);
            } catch (Throwable t) {
                Bukkit.getLogger().log(Level.WARNING, "[Role] briefing en échec pour " + getName(), t);
            }
        }
        dispatch(Gate.ACTIVE, Given.class, ability -> ability.onGive(uhcPlayer));
    }

    public void onDrop(UHCPlayer uhcPlayer, PlayerDropItemEvent event) {
        dispatch(Gate.NONE, Dropping.class, ability -> ability.onDrop(uhcPlayer, event));
    }

    public void onSec(Player player) {
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;
        dispatch(Gate.USABLE, Ticking.class, ability -> ability.onSec(owner));
    }

    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        dispatch(Gate.NONE, Dying.class, ability -> ability.onDeath(uhcPlayer, killer, event));
    }

    public void onBow(Entity shooter, Player target, EntityShootBowEvent event) {
        dispatch(Gate.USABLE, Shooting.class, ability -> ability.onBow(shooter, target, event));
    }

    public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
        dispatch(Gate.USABLE, Consuming.class, ability -> ability.onConsume(event));
    }

    public void onInteract(Player player1, PlayerInteractEvent event) {
        dispatch(Gate.NONE, Clicking.class, ability -> ability.onClick(event, event.getItem()));
    }

    public void onMove(UHCPlayer player1, PlayerMoveEvent event) {
        dispatch(Gate.USABLE, Moving.class, ability -> ability.onMove(event));
    }

    public void onSetup() {

    }

    public void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event) {
        if (!(entity instanceof Player)) return;
        dispatch(Gate.USABLE, Attacking.class, ability -> ability.onAttack(UHCPlayerManager.get().getPlayer((Player) entity), event));
    }

    public void onKill(UHCPlayer killer, UHCPlayer victim) {
        dispatch(Gate.USABLE, Killing.class, ability -> ability.onKill(victim));
    }

    public void onTakeDamage(EntityDamageEvent event) {
        dispatch(Gate.USABLE, Hurtable.class, ability -> ability.onTakeDamage(event));
    }

    public void onBlockBreak(BlockBreakEvent event) {
        dispatch(Gate.USABLE, Mining.class, ability -> ability.onBlockBreak(event));
    }

    public void onProjectileHit(ProjectileHitEvent event) {
        dispatch(Gate.USABLE, ProjectileImpacting.class, ability -> ability.onProjectileHit(event));
    }

    public void onMobDeath(EntityDeathEvent event) {
        dispatch(Gate.USABLE, MobKilling.class, ability -> ability.onMobDeath(event));
    }

    public void onInteractEntity(PlayerInteractEntityEvent event) {
        dispatch(Gate.USABLE, EntityInteracting.class, ability -> ability.onInteractEntity(event));
    }

    public void onBlockPlace(BlockPlaceEvent event) {
        dispatch(Gate.USABLE, Building.class, ability -> ability.onBlockPlace(event));
    }

    @Override
    public Role clone() {
        try {
            Role clone = (Role) super.clone();

            Set<Ability> clonedAbilities = new HashSet<>();
            for (Ability ability : this.abilities) {
                clonedAbilities.add(ability.clone());
            }

            clone.setAbilities(clonedAbilities);
            clone.knowPlayers = new ArrayList<>();
            clone.facades = new ArrayList<>();
            clone.partners = new ArrayList<>();
            return clone;

        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Field> roleVariableFields(Class<?> roleClass) {
        List<Field> fields = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Class<?> clazz = roleClass; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Var.class)) continue;
                if (!seen.add(field.getName())) continue;
                fields.add(field);
            }
        }
        return fields;
    }

    public Document roleToDoc() {
        Document doc = new Document();
        for (VariableDescriptor d
                : Variables.of(this)) {
            Field field = d.field();
            try {
                Object value = field.get(this);
                if (value instanceof Ability ability) {
                    value = ability.abilityToDoc();
                } else if (value instanceof Enum<?> en) {
                    value = en.name();
                }
                doc.append(field.getName(), value);
            } catch (IllegalAccessException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Erreur inattendue", e);
            }
        }
        return doc;
    }

    public void docToRole(Document doc) {
        if (doc == null) return;
        for (VariableDescriptor d
                : Variables.of(this)) {
            Field field = d.field();
            if (!doc.containsKey(field.getName())) continue;
            try {
                Object value = doc.get(field.getName());
                if (Ability.class.isAssignableFrom(field.getType()) && value instanceof Document abilityDoc) {
                    Ability ability = ((Ability) field.get(this)).clone();
                    ability.docToAbility(abilityDoc);
                    field.set(this, ability);
                } else if (field.getType().isEnum() && value instanceof String s) {
                    Variables.setEnumValue(field, this, s);
                } else {
                    field.set(this, value);
                }
            } catch (IllegalAccessException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Erreur inattendue", e);
            }
        }
    }
}