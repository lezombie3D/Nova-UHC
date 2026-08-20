package net.novaproject.ultimate.nuzlocke.roles.dragon;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.ultimate.nuzlocke.NuzlockeLang;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.ultimate.nuzlocke.NuzlockeRole;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Dragon extends NuzlockeRole {

    @Var(name = "Nuzlocke Dragon Breath", type = VariableType.ABILITY)
    private Ability breath;

    @Var(name = "Nuzlocke Dragon Dance", type = VariableType.ABILITY)
    private Ability dance;

    @Var(name = "Nuzlocke Dragon Lowhp Hearts", type = VariableType.DOUBLE)
    private double lowHpHearts = 5.0;

    @Var(name = "Nuzlocke Dragon Extreme Hearts", type = VariableType.DOUBLE)
    private double extremeHillsHearts = 9.0;

    public Dragon() {
        this.breath = new DragonBreathBow();
        this.dance = new DragonDanceListener();
    }

    @Override public int getId() { return 16; }
    @Override public String getName() { return "Dragon"; }
    @Override public String getTypeColor() { return "§5"; }
    @Override public Material getIconMaterial() { return Material.DRAGON_EGG; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.DRAGON_EGG).setName("§5§lDragon"); }
    @Override public Lang getDescriptionLang() { return NuzlockeLang.ROLE_DESC_DRAGON; }

    @Override
    public void onSec(Player p) {
        super.onSec(p);
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;

        double hearts = owner.getHealth() / 2.0;
        Biome biome = owner.getLocation().getBlock().getBiome();
        boolean isExtremeHills = biome == Biome.EXTREME_HILLS || biome == Biome.EXTREME_HILLS_PLUS
                || biome == Biome.EXTREME_HILLS_MOUNTAINS || biome == Biome.EXTREME_HILLS_PLUS_MOUNTAINS
                || biome == Biome.SMALL_MOUNTAINS;
        int resAmp = (isExtremeHills && hearts > extremeHillsHearts) ? 2 : 0;

        UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, resAmp, true, false)
        }, owner);

        if (hearts < lowHpHearts) {
            owner.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 0, true, false), true);
            owner.removePotionEffect(PotionEffectType.SLOW);
        } else {
            owner.removePotionEffect(PotionEffectType.SLOW_DIGGING);
            owner.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0, true, false), true);
        }
    }
}

