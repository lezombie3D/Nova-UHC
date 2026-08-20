package net.novaproject.ultimate.nuzlocke.roles.ghost;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.ultimate.nuzlocke.NuzlockeLang;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.ultimate.nuzlocke.NuzlockeRole;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Ghost extends NuzlockeRole {

    @Var(name = "Nuzlocke Ghost Dodge", type = VariableType.ABILITY)
    private Ability dodge;

    @Var(name = "Nuzlocke Ghost Pve", type = VariableType.ABILITY)
    private Ability pveImmunity;

    @Var(name = "Nuzlocke Ghost Invisible Y", type = VariableType.INTEGER)
    private int invisibleYThreshold = 70;

    @Var(name = "Nuzlocke Ghost Dirt Range", type = VariableType.INTEGER)
    private int dirtRange = 2;

    public Ghost() {
        this.dodge = new GhostArrowDodgeListener();
        this.pveImmunity = new GhostPveImmunityListener();
    }

    @Override public int getId() { return 14; }
    @Override public String getName() { return "Ghost"; }
    @Override public String getTypeColor() { return "§5"; }
    @Override public Material getIconMaterial() { return Material.SKULL_ITEM; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.SKULL_ITEM).setName("§5§lGhost"); }
    @Override public Lang getDescriptionLang() { return NuzlockeLang.ROLE_DESC_GHOST; }

    @Override
    public void onSec(Player p) {
        super.onSec(p);
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;
        UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                new PotionEffect(PotionEffectType.SLOW, 80, 0, true, false)
        }, owner);
        if (owner.getLocation().getBlockY() < invisibleYThreshold) {
            owner.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 0, true, false), true);
        } else {
            owner.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
        if (net.novaproject.ultimate.nuzlocke.roles.dark.Dark.isExposedToSun(owner)) {
            owner.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0, true, false), true);
        }
        if (isNearDirt(owner.getLocation().getBlock())) {
            owner.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 0, true, false), true);
        }
    }

    private boolean isNearDirt(Block c) {
        for (int dx = -dirtRange; dx <= dirtRange; dx++) {
            for (int dy = -dirtRange; dy <= dirtRange; dy++) {
                for (int dz = -dirtRange; dz <= dirtRange; dz++) {
                    if (c.getRelative(dx, dy, dz).getType() == Material.DIRT) return true;
                }
            }
        }
        return false;
    }
}

