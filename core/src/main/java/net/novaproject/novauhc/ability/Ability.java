package net.novaproject.novauhc.ability;

import net.novaproject.novauhc.event.UhcAbilityEvents.UhcAbilityUsedEvent;
import net.novaproject.novauhc.ability.template.UseAbility;
import net.novaproject.novauhc.ability.template.SwitchAbility;
import net.novaproject.novauhc.ability.template.PassiveAbility;
import net.novaproject.novauhc.ability.template.CommandAbility;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.utils.variable.Variables;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.debug.DebugLog.AbilityDebug;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.item.ItemCreator.AbilityNbt;
import net.novaproject.novauhc.utils.item.PendingItemsManager;
import net.novaproject.novauhc.utils.variable.Variables.VariableSerializer;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public abstract class Ability implements Cloneable, AbilitySpi.IAbility {

    @Var(lang = ScenarioVarLang.class, nameKey = "ABILITY_VAR_MAX_USE_NAME", descKey = "ABILITY_VAR_MAX_USE_DESC", type = VariableType.INTEGER, min = -1)
    private int maxUse = -1;
    @Var(lang = ScenarioVarLang.class, nameKey = "ABILITY_VAR_COOLDOWN_NAME", descKey = "ABILITY_VAR_COOLDOWN_DESC", type = VariableType.TIME, min = 0)
    private int cooldown = 0;
    @Var(lang = ScenarioVarLang.class, nameKey = "ABILITY_VAR_ACTIVE_NAME", descKey = "ABILITY_VAR_ACTIVE_DESC", type = VariableType.BOOLEAN)
    private boolean active = true;
    @Var(name = "Droppé à la mort", desc = "§7Si activé, l'item tombe au sol à la mort au lieu d'être retiré. Le retrait manuel (drop, coffre, craft) reste bloqué.", type = VariableType.BOOLEAN)
    private boolean droppedOnDeath = false;

    private UHCPlayer owner;
    private boolean disabled = false;
    private boolean stolen = false;

    public abstract String getName();

    public void decrementMaxUse() {
        if (maxUse > 0) {
            maxUse--;
        }
    }

    public boolean active(){
        return active;
    }

    public boolean canBeDisabled() {
        return true;
    }

    public String getDescription(Player player) {
        return null;
    }

    public boolean isUsable() {
        return active() && !disabled && !stolen;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled && canBeDisabled();
    }

    public Material getMaterial() {
        return Material.NETHER_STAR;
    }

    public String abilityId() {
        return getName();
    }

    protected String itemDisplayName() {
        return getName();
    }

    public static String itemName(String name) {
        return ItemCreator.abilityItemName(name);
    }

    public ItemStack getItemStack() {
        if (getMaterial() == null) return new ItemStack(Material.AIR);
        ItemStack it = new ItemCreator(getMaterial()).setName(itemName(itemDisplayName())).getItemstack();
        return AbilityNbt.setDroppedOnDeath(AbilityNbt.stamp(it, abilityId()), droppedOnDeath);
    }

    protected void giveAbilityItem(UHCPlayer uhcPlayer) {
        Player p = uhcPlayer == null ? null : uhcPlayer.getPlayer();
        if (p == null) return;
        if (getMaterial() == null) return;
        ItemStack stack = getItemStack();
        PendingItemsManager.give(p, stack);
        AbilityDebug.onGive(this, uhcPlayer, stack);
    }

    public boolean isAbilityItem(ItemStack item) {
        if (item == null) return false;
        String id = AbilityNbt.idOf(item);
        if (id != null) return abilityId().equals(id);
        Material mat = getMaterial();
        if (mat == null || item.getType() != mat) return false;
        ItemStack expected = getItemStack();
        if (!item.hasItemMeta() || !expected.hasItemMeta()) return false;
        String want = expected.getItemMeta().getDisplayName();
        return want != null && want.equals(item.getItemMeta().getDisplayName());
    }

    public abstract boolean onEnable(Player player);

    @Override
    public boolean equals(Object o) {
        return o != null && this.getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

    public boolean isManualAbility() {
        return this instanceof UseAbility
                || this instanceof SwitchAbility
                || this instanceof CommandAbility;
    }

    public boolean tryUse(Player player) {
        return AbilityManager.get().tryUse(this, player);
    }

    public void fireUsed(Player player) {
        if (this instanceof PassiveAbility) return;
        Bukkit.getPluginManager().callEvent(new UhcAbilityUsedEvent(player, this));
    }

    protected void startCooldownDisplay(Player player, int seconds) {
        if (!isManualAbility() || seconds <= 0 || getMaterial() == null) return;
        DisplayService.cooldown(player, activeCooldownDisplayName(player), seconds, getMaterial());
    }

    public String activeCooldownKey(Player player) {
        return getName() + "Cooldown";
    }

    public String activeCooldownDisplayName(Player player) {
        return getName();
    }

    public int activeCooldownTotalSeconds(Player player) {
        return getCooldown();
    }

    public int activeCooldownMaxUse(Player player) {
        return getMaxUse();
    }

    public UHCPlayer getUHCPlayer(Player player) {
        return UHCPlayerManager.get().getPlayer(player);
    }

    protected Player livingOwner() {
        if (getOwner() == null) return null;
        Player p = getOwner().getPlayer();
        return p == null || p.isDead() ? null : p;
    }

    protected Player actingOwner(Player clicker) {
        if (getOwner() == null) return null;
        Player p = getOwner().getPlayer();
        return p == null || !p.equals(clicker) ? null : p;
    }

    @Override
    public Ability clone() {
        AbilityDebug.warnSharedState(this);
        try {
            return (Ability) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public Document abilityToDoc() {
        return VariableSerializer.toDoc(this, Variables.of(this));
    }

    public void docToAbility(Document doc) {
        VariableSerializer.fromDoc(this, doc, Variables.of(this));
    }

}