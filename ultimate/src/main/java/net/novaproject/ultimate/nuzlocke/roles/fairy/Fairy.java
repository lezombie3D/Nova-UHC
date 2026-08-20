package net.novaproject.ultimate.nuzlocke.roles.fairy;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.ultimate.nuzlocke.NuzlockeLang;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.ultimate.nuzlocke.NuzlockeRole;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Fairy extends NuzlockeRole {

    @Var(name = "Nuzlocke Fairy Wand", type = VariableType.ABILITY)
    private Ability wand;

    @Var(name = "Nuzlocke Fairy Kiss", type = VariableType.ABILITY)
    private Ability kiss;

    @Var(name = "Nuzlocke Fairy Flower", type = VariableType.ABILITY)
    private Ability flower;

    public Fairy() {
        this.wand = new FairyWandUse();
        this.kiss = new FairySweetKissBow();
        this.flower = new FairyFlowerEatListener();
    }

    @Override public int getId() { return 13; }
    @Override public String getName() { return "Fairy"; }
    @Override public String getTypeColor() { return "§d"; }
    @Override public Material getIconMaterial() { return Material.RED_ROSE; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.RED_ROSE).setName("§d§lFairy"); }
    @Override public Lang getDescriptionLang() { return NuzlockeLang.ROLE_DESC_FAIRY; }

    @Override
    public void onSec(Player p) {
        super.onSec(p);
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;
        UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                new PotionEffect(PotionEffectType.WEAKNESS, 80, 0, true, false)
        }, owner);
        boolean sunExposed = net.novaproject.ultimate.nuzlocke.roles.dark.Dark.isExposedToSun(owner);
        if (sunExposed) {
            owner.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, true, false), true);
            owner.removePotionEffect(PotionEffectType.SLOW);
        } else {
            owner.removePotionEffect(PotionEffectType.SPEED);
            owner.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0, true, false), true);
        }
    }
}

