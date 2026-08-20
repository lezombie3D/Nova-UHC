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
import net.novaproject.ultimate.legend.roles.abilities.PrincesseNoFallPassive;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Princesse extends LegendRole {

    @Var(name = "Princess Extra Hearts", desc = "Extra hearts.", type = VariableType.INTEGER)
    private int extraHearts = 4;

    @Var(name = "Royal Grace", type = VariableType.ABILITY)
    public Ability noFallPassive;

    public Princesse() {
        this.noFallPassive = new PrincesseNoFallPassive();
    }

    @Override public int getId() { return 10; }
    @Override public String getName() { return "Princesse"; }
    @Override public Material getIconMaterial() { return Material.GOLD_HELMET; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLD_HELMET); }
    @Override public Lang getDescriptionLang() { return LegendLang.ROLE_DESC_PRINCESSE; }
    @Override protected String applyPlaceholders(String d) { return d.replace("%extra_hearts%", String.valueOf(extraHearts / 2)); }
    public boolean hasNoFall() { return true; }

    @Override
    public void onGive(UHCPlayer u) {
        super.onGive(u); Player p = u.getPlayer(); if (p == null) return;
        p.setMaxHealth(20 + extraHearts); p.setHealth(20 + extraHearts);
    }

    @Override
    public void onSec(Player p) {
        super.onSec(p);
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;
        UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                new PotionEffect(PotionEffectType.SPEED, 80, 0, false, false)
        }, owner);
    }
}

