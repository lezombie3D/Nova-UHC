package net.novaproject.novauhc.ability.template;

import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.utils.variable.Variables;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.event.UhcAbilityEvents.UhcAbilityUseEvent;
import net.novaproject.novauhc.debug.DebugLog.AbilityDebug;
import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Given;
import net.novaproject.novauhc.ability.AbilityHooks.Clicking;
import net.novaproject.novauhc.ability.AbilityHooks.Ticking;
import net.novaproject.novauhc.ability.AbilityHooks.Moving;
import net.novaproject.novauhc.ability.AbilityHooks.Attacking;
import net.novaproject.novauhc.ability.AbilityHooks.Hurtable;
import net.novaproject.novauhc.ability.AbilityHooks.Dropping;
import net.novaproject.novauhc.ability.AbilityHooks.Dying;
import net.novaproject.novauhc.ability.AbilityHooks.Killing;
import net.novaproject.novauhc.ability.AbilityHooks.Consuming;
import net.novaproject.novauhc.ability.AbilityHooks.Shooting;
import net.novaproject.novauhc.ability.AbilityHooks.Mining;
import net.novaproject.novauhc.ability.AbilityHooks.Building;
import net.novaproject.novauhc.ability.AbilityHooks.ProjectileImpacting;
import net.novaproject.novauhc.ability.AbilityHooks.EntityInteracting;
import net.novaproject.novauhc.ability.AbilityHooks.MobKilling;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.cooldown.CooldownService;
import net.novaproject.novauhc.utils.variable.Variables.VariableSerializer;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.novaproject.novauhc.lang.lang.CoreLang;
import org.bukkit.Bukkit;

public abstract class SwitchAbility extends Ability implements Given, Clicking, Ticking, Moving, Attacking, Hurtable, Dropping, Dying, Killing, Consuming, Shooting, Mining, Building, ProjectileImpacting, EntityInteracting, MobKilling {

    @Var(name = "CD de switch", desc = "§7Cooldown entre deux changements de forme (s). 0 = aucun.", type = VariableType.TIME, min = 0)
    private int switchCooldownSec = 0;

    private Class<? extends UseAbility> current;

    private LinkedHashMap<Class<? extends UseAbility>, UseAbility> instances;

    public void setSwitchCooldownSec(int switchCooldownSec) { this.switchCooldownSec = switchCooldownSec; }

    public abstract List<Class<? extends UseAbility>> getSwitchedAbilities();

    public abstract String getSwitchName();

    public void onSwitch(Player player, Class<? extends UseAbility> newAbility) {}

    public boolean cancel() { return true; }

    public boolean alert(Player player) { return false; }

    public final Class<? extends UseAbility> getCurrent() {
        if (current == null) {
            List<Class<? extends UseAbility>> list = getSwitchedAbilities();
            if (list != null && !list.isEmpty()) current = list.get(0);
        }
        return current;
    }

    public final void setCurrent(Class<? extends UseAbility> current) {
        this.current = current;
    }

    public final void next(Player player) {
        List<Class<? extends UseAbility>> list = getSwitchedAbilities();
        if (list == null || list.isEmpty()) return;
        ItemStack oldItem = getItemStack();
        int idx = list.indexOf(getCurrent());
        int nextIdx = ((idx < 0 ? 0 : idx) + 1) % list.size();
        setCurrent(list.get(nextIdx));
        if (player != null) replaceInInventory(player, oldItem, getItemStack());
        onSwitch(player, getCurrent());
    }

    public final Collection<UseAbility> getChildAbilities() {
        ensureInstances();
        return Collections.unmodifiableCollection(instances.values());
    }

    private void ensureInstances() {
        if (instances == null) instances = new LinkedHashMap<>();
        List<Class<? extends UseAbility>> list = getSwitchedAbilities();
        if (list == null) return;
        for (Class<? extends UseAbility> c : list) {
            if (instances.containsKey(c)) continue;
            try {
                UseAbility a = c.getDeclaredConstructor().newInstance();
                instances.put(c, a);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(
                        "SwitchAbility: " + c.getName() + " doit avoir un constructeur public vide", e);
            }
        }
    }

    private UseAbility getCurrentInstance() {
        Class<? extends UseAbility> c = getCurrent();
        if (c == null) return null;
        ensureInstances();
        UseAbility a = instances.get(c);
        if (a != null) a.setOwner(getOwner());
        return a;
    }

    public final void updateAll(Player player, List<Ability> abilities) {
        updateAll(player, abilities, getCooldown());
    }

    public final void updateAll(Player player, List<Ability> abilities, int cooldown) {
        if (cooldown == -1 || player == null || abilities == null) return;
        long cdMs = cooldown * 1000L;
        List<Class<? extends UseAbility>> switched = getSwitchedAbilities();
        for (Ability a : abilities) {
            if (switched.contains(a.getClass())) {
                CooldownService.put(player, a.getName() + "Cooldown", cdMs);
            }
        }
    }

    @Override
    public final String getName() {
        return getSwitchName();
    }

    public String getDisplayName() {
        UseAbility inst = getCurrentInstance();
        return inst == null ? getSwitchName() : inst.getName();
    }


    @Override
    public Material getMaterial() {
        UseAbility inst = getCurrentInstance();
        return inst == null ? Material.NETHER_STAR : inst.getMaterial();
    }

    @Override
    protected String itemDisplayName() {
        return getDisplayName();
    }

    @Override
    public boolean onEnable(Player player) {
        UseAbility inst = getCurrentInstance();
        return inst != null && inst.onEnable(player);
    }

    @Override public void onAttack(UHCPlayer victimP, EntityDamageByEntityEvent event) {
        UseAbility inst = getCurrentInstance(); if (inst instanceof Attacking a) a.onAttack(victimP, event);
    }
    @Override public void onTakeDamage(EntityDamageEvent event) {
        UseAbility inst = getCurrentInstance(); if (inst instanceof Hurtable a) a.onTakeDamage(event);
    }
    @Override public void onConsume(PlayerItemConsumeEvent event) {
        UseAbility inst = getCurrentInstance(); if (inst instanceof Consuming a) a.onConsume(event);
    }
    @Override public void onDeath(UHCPlayer victim, UHCPlayer killer, PlayerDeathEvent event) {
        UseAbility inst = getCurrentInstance(); if (inst instanceof Dying a) a.onDeath(victim, killer, event);
    }
    @Override public void onKill(UHCPlayer killed) {
        UseAbility inst = getCurrentInstance(); if (inst instanceof Killing a) a.onKill(killed);
    }
    @Override public void onBow(Entity shooter, Player target, EntityShootBowEvent event) {
        UseAbility inst = getCurrentInstance(); if (inst instanceof Shooting a) a.onBow(shooter, target, event);
    }
    @Override public void onBlockBreak(BlockBreakEvent event) {
        UseAbility inst = getCurrentInstance(); if (inst instanceof Mining a) a.onBlockBreak(event);
    }
    @Override public void onBlockPlace(BlockPlaceEvent event) {
        UseAbility inst = getCurrentInstance(); if (inst instanceof Building a) a.onBlockPlace(event);
    }
    @Override public void onProjectileHit(ProjectileHitEvent event) {
        UseAbility inst = getCurrentInstance(); if (inst instanceof ProjectileImpacting a) a.onProjectileHit(event);
    }
    @Override public void onMove(PlayerMoveEvent event) {
        UseAbility inst = getCurrentInstance(); if (inst instanceof Moving a) a.onMove(event);
    }
    @Override public void onDrop(UHCPlayer uhcPlayer, PlayerDropItemEvent event) {
        UseAbility inst = getCurrentInstance(); if (inst instanceof Dropping a) a.onDrop(uhcPlayer, event);
    }
    @Override public void onInteractEntity(PlayerInteractEntityEvent event) {
        UseAbility inst = getCurrentInstance(); if (inst instanceof EntityInteracting a) a.onInteractEntity(event);
    }
    @Override public void onMobDeath(EntityDeathEvent event) {
        UseAbility inst = getCurrentInstance(); if (inst instanceof MobKilling a) a.onMobDeath(event);
    }
    @Override public void onSec(Player player) {
        UseAbility inst = getCurrentInstance(); if (inst instanceof Ticking a) a.onSec(player);
    }

    @Override
    public int getCooldown() {
        UseAbility inst = getCurrentInstance();
        if (inst != null && inst.getCooldown() > 0) return inst.getCooldown();
        return super.getCooldown();
    }

    @Override
    public int getMaxUse() {
        UseAbility inst = getCurrentInstance();
        if (inst != null && inst.getMaxUse() != -1) return inst.getMaxUse();
        return super.getMaxUse();
    }

    public boolean perChildCooldown() { return false; }

    @Override
    public boolean tryUse(Player player) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        UseAbility child = getCurrentInstance();
        if (child == null) return false;

        String cdKey = perChildCooldown() ? (child.getName() + "Cooldown") : (getSwitchName() + "Cooldown");

        long remainingMs = CooldownService.get(player, cdKey);
        if (remainingMs != -1) {
            LangManager.get().send(
                    CoreLang.COMMON_ABILITY_ON_COOLDOWN, player,
                    Map.of("%ability%", perChildCooldown() ? child.getName() : getSwitchName(),
                            "%time%", String.valueOf((remainingMs + 999) / 1000)));
            return false;
        }
        if (!uhcPlayer.isPlaying()) return false;
        if (!isUsable() || !child.isUsable()) {
            LangManager.get().send(
                    CoreLang.COMMON_ABILITY_DISABLED, player,
                    Map.of("%ability%", child.getName()));
            return false;
        }

        int childMaxUse = child.getMaxUse();
        boolean useChildMax = childMaxUse != -1;
        int effectiveMaxUse = useChildMax ? childMaxUse : super.getMaxUse();
        if (effectiveMaxUse == 0) return false;

        UhcAbilityUseEvent useEvent =
                new UhcAbilityUseEvent(player, child);
        Bukkit.getPluginManager().callEvent(useEvent);
        if (useEvent.isCancelled()) return false;

        if (!child.onEnable(player)) return false;

        int effectiveCd = child.getCooldown() > 0 ? child.getCooldown() : Math.max(0, super.getCooldown());

        if (effectiveMaxUse > 0) {
            if (useChildMax) child.decrementMaxUse();
            else decrementMaxUse();
        }

        if (effectiveCd > 0) {
            CooldownService.put(player, cdKey, effectiveCd * 1000L);
            startCooldownDisplay(player, effectiveCd);
        }
        child.fireUsed(player);
        return true;
    }

    @Override
    public String activeCooldownKey(Player player) {
        if (!perChildCooldown()) return getSwitchName() + "Cooldown";
        UseAbility child = getCurrentInstance();
        return child != null ? child.getName() + "Cooldown" : getSwitchName() + "Cooldown";
    }

    @Override
    public String activeCooldownDisplayName(Player player) {
        if (!perChildCooldown()) return getSwitchName();
        UseAbility child = getCurrentInstance();
        return child != null ? child.getName() : getSwitchName();
    }

    @Override
    public int activeCooldownTotalSeconds(Player player) {
        UseAbility child = getCurrentInstance();
        if (child != null && child.getCooldown() > 0) return child.getCooldown();
        return Math.max(0, super.getCooldown());
    }

    @Override
    public int activeCooldownMaxUse(Player player) {
        UseAbility child = getCurrentInstance();
        if (child != null && child.getMaxUse() != -1) return child.getMaxUse();
        return super.getMaxUse();
    }

    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        giveAbilityItem(uhcPlayer);
    }

    @Override
    public void onClick(PlayerInteractEvent event, ItemStack item) {
        if (item == null) return;
        if (getOwner() == null) {
            AbilityDebug.onClickSkipped(this, event.getPlayer().getName(), "owner=null (ability not bound to a player)");
            return;
        }
        if (!event.getPlayer().equals(getOwner().getPlayer())) return;
        boolean matched = isAbilityItem(item);
        AbilityDebug.onClickAttempt(this, event.getPlayer().getName(), item, getItemStack(), matched);
        if (!matched) return;

        Player player = event.getPlayer();
        Action action = event.getAction();

        if (cancel()) event.setCancelled(true);
        if (alert(player)) return;

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            if (switchCooldownSec > 0) {
                String switchKey = getSwitchName() + "SwitchCooldown";
                long remainingMs = CooldownService.get(player, switchKey);
                if (remainingMs != -1) {
                    LangManager.get().send(
                            CoreLang.COMMON_ABILITY_ON_COOLDOWN, player,
                            Map.of("%ability%", getSwitchName(),
                                    "%time%", String.valueOf((remainingMs + 999) / 1000)));
                    return;
                }
                CooldownService.put(player, switchKey, switchCooldownSec * 1000L);
            }
            next(player);
        } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            tryUse(player);
        }
    }

    private void replaceInInventory(Player player, ItemStack oldItem, ItemStack newItem) {
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack it = player.getInventory().getItem(slot);
            if (it != null && it.isSimilar(oldItem)) {
                player.getInventory().setItem(slot, newItem);
                return;
            }
        }
    }

    private static final String CHILDREN_KEY = "__children";
    private static final String CURRENT_KEY  = "__current";

    @Override
    public Document abilityToDoc() {
        Document doc = VariableSerializer.toDoc(this, Variables.of(this));
        ensureInstances();
        Document children = new Document();
        for (Map.Entry<Class<? extends UseAbility>, UseAbility> e : instances.entrySet()) {
            children.append(e.getKey().getName(), e.getValue().abilityToDoc());
        }
        doc.append(CHILDREN_KEY, children);
        if (current != null) doc.append(CURRENT_KEY, current.getName());
        return doc;
    }

    @Override
    public void docToAbility(Document doc) {
        if (doc == null) return;
        VariableSerializer.fromDoc(this, doc, Variables.of(this));
        ensureInstances();
        Object childrenRaw = doc.get(CHILDREN_KEY);
        if (childrenRaw instanceof Document children) {
            for (Map.Entry<Class<? extends UseAbility>, UseAbility> e : instances.entrySet()) {
                Object sub = children.get(e.getKey().getName());
                if (sub instanceof Document subDoc) e.getValue().docToAbility(subDoc);
            }
        }
        Object cur = doc.get(CURRENT_KEY);
        if (cur instanceof String name) {
            for (Class<? extends UseAbility> c : getSwitchedAbilities()) {
                if (c.getName().equals(name)) { setCurrent(c); break; }
            }
        }
    }

    @Override
    public SwitchAbility clone() {
        SwitchAbility c = (SwitchAbility) super.clone();
        c.instances = new LinkedHashMap<>();
        if (this.instances != null) {
            for (Map.Entry<Class<? extends UseAbility>, UseAbility> e : this.instances.entrySet()) {
                c.instances.put(e.getKey(), (UseAbility) e.getValue().clone());
            }
        }
        c.current = this.current;
        return c;
    }
}