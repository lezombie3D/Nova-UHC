package net.novaproject.ultimate.nuzlocke.roles.normal;
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

public class Normal extends NuzlockeRole {

    @Var(name = "Nuzlocke Normal Substitute", type = VariableType.ABILITY)
    private Ability substitute;

    @Var(name = "Nuzlocke Normal Gapple", type = VariableType.ABILITY)
    private Ability gapple;

    public Normal() {
        this.substitute = new NormalSubstituteCommand();
        this.gapple = new NormalGappleHealListener();
    }

    @Override public int getId() { return 4; }
    @Override public String getName() { return "Normal"; }
    @Override public String getTypeColor() { return "§f"; }
    @Override public Material getIconMaterial() { return Material.WHEAT; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.WHEAT).setName("§f§lNormal"); }
    @Override public Lang getDescriptionLang() { return NuzlockeLang.ROLE_DESC_NORMAL; }

    @Override
    public void onSec(Player p) {
        super.onSec(p);
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;
        UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 0, true, false)
        }, owner);
    }
}

