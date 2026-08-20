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
import net.novaproject.ultimate.legend.roles.abilities.MedecinHealPassive;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Medecin extends LegendRole {

    @Var(name = "Heal", type = VariableType.ABILITY)
    public Ability healPassive;

    public Medecin() {
        this.healPassive = new MedecinHealPassive();
    }

    @Override public int getId() { return 14; }
    @Override public String getName() { return "Médecin"; }
    @Override public Material getIconMaterial() { return Material.POTION; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.POTION); }
    @Override public Lang getDescriptionLang() { return LegendLang.ROLE_DESC_MEDECIN; }

    @Override
    public void onGive(UHCPlayer u) {
        super.onGive(u); Player p = u.getPlayer(); if (p == null) return;
        p.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 5));
    }

    @Override
    public void onSec(Player p) {
        super.onSec(p);
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;
        UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                new PotionEffect(PotionEffectType.REGENERATION, 80, 0, false, false)
        }, owner);
    }
}

