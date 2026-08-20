package net.novaproject.ultimate.legend.roles;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.ultimate.legend.LegendLang;
import net.novaproject.ultimate.legend.LegendRole;
import net.novaproject.ultimate.legend.roles.abilities.NainArmorActive;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Nain extends LegendRole {

    @Var(name = "Armor", type = VariableType.ABILITY)
    public Ability armorActive;

    public Nain() {
        this.armorActive = new NainArmorActive();
    }

    @Override public int getId() { return 5; }
    @Override public String getName() { return "Nain"; }
    @Override public Material getIconMaterial() { return Material.GOLD_PICKAXE; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLD_PICKAXE); }
    @Override public Lang getDescriptionLang() { return LegendLang.ROLE_DESC_NAIN; }

    @Override
    public void onSec(Player p) {
        super.onSec(p);
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;
        UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                new PotionEffect(PotionEffectType.FAST_DIGGING, 80, 0, false, false)
        }, owner);
    }
}

