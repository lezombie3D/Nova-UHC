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
import net.novaproject.ultimate.legend.roles.abilities.NecroSummonActive;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Necromancien extends LegendRole {

    @Var(name = "Summon", type = VariableType.ABILITY)
    public Ability summonActive;

    public Necromancien() {
        this.summonActive = new NecroSummonActive();
    }

    @Override public int getId() { return 7; }
    @Override public String getName() { return "Nécromancien"; }
    @Override public Material getIconMaterial() { return Material.SKULL_ITEM; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.SKULL_ITEM); }
    @Override public Lang getDescriptionLang() { return LegendLang.ROLE_DESC_NECROMANCIEN; }

    @Override
    public void onSec(Player p) {
        super.onSec(p);
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;
        UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                new PotionEffect(PotionEffectType.NIGHT_VISION, 80, 0, false, false)
        }, owner);
    }
}

