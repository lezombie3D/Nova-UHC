package net.novaproject.ultimate.nuzlocke.roles.poison;
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

public class Poison extends NuzlockeRole {

    @Var(name = "Nuzlocke Poison Thorns", type = VariableType.ABILITY)
    private Ability thorns;

    @Var(name = "Nuzlocke Poison Immunity", type = VariableType.ABILITY)
    private Ability immunity;

    @Var(name = "Nuzlocke Poison Spawner", type = VariableType.ABILITY)
    private Ability spawner;

    @Var(name = "Nuzlocke Poison Dirt Range", type = VariableType.INTEGER)
    private int dirtRange = 2;

    public Poison() {
        this.thorns = new PoisonSpikesMelee();
        this.immunity = new PoisonImmunityListener();
        this.spawner = new PoisonSpawnerEnvListener();
    }

    @Override public int getId() { return 12; }
    @Override public String getName() { return "Poison"; }
    @Override public String getTypeColor() { return "§5"; }
    @Override public Material getIconMaterial() { return Material.SPIDER_EYE; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.SPIDER_EYE).setName("§5§lPoison"); }
    @Override public Lang getDescriptionLang() { return NuzlockeLang.ROLE_DESC_POISON; }

    @Override
    public void onSec(Player p) {
        super.onSec(p);
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;

        UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                new PotionEffect(PotionEffectType.SLOW, 80, 0, true, false)
        }, owner);

        Block at = owner.getLocation().getBlock();
        if (isNear(at, Material.DIRT, dirtRange)) {
            owner.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 0, true, false), true);
        }
    }

    private boolean isNear(Block c, Material m, int r) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (c.getRelative(dx, dy, dz).getType() == m) return true;
                }
            }
        }
        return false;
    }
}

