package net.novaproject.ultimate.legend.roles;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.ultimate.legend.LegendLang;
import net.novaproject.ultimate.legend.LegendRole;
import net.novaproject.ultimate.legend.roles.abilities.TankResistancePassive;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Tank extends LegendRole {

    @Var(name = "Tank Extra Hearts", desc = "Extra hearts (half-hearts).", type = VariableType.INTEGER)
    private int extraHearts = 6;

    @Var(name = "Resistance", type = VariableType.ABILITY)
    public Ability resistancePassive;

    public Tank() {
        this.resistancePassive = new TankResistancePassive();
    }

    @Override public int getId() { return 4; }
    @Override public String getName() { return "Tank"; }
    @Override public Material getIconMaterial() { return Material.DIAMOND_CHESTPLATE; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.DIAMOND_CHESTPLATE); }
    @Override public Lang getDescriptionLang() { return LegendLang.ROLE_DESC_TANK; }
    @Override protected String applyPlaceholders(String d) { return d.replace("%extra_hearts%", String.valueOf(extraHearts / 2)); }

    @Override
    public void onGive(UHCPlayer u) {
        super.onGive(u); Player p = u.getPlayer(); if (p == null) return;
        p.setMaxHealth(20 + extraHearts); p.setHealth(20 + extraHearts);
    }
}

