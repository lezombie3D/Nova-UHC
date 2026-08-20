package net.novaproject.ultimate.legend.roles;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.ultimate.legend.LegendLang;
import net.novaproject.ultimate.legend.LegendRole;
import net.novaproject.ultimate.legend.roles.abilities.OgrePassive;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Ogre extends LegendRole {

    @Var(name = "Ogre Extra Hearts", desc = "Extra hearts.", type = VariableType.INTEGER)
    private int extraHearts = 10;
    @Var(name = "Ogre Starting Gapples", desc = "Starting golden apples.", type = VariableType.INTEGER)
    private int startGapples = 8;

    @Var(name = "Brute Force", type = VariableType.ABILITY)
    public Ability passive;

    public Ogre() {
        this.passive = new OgrePassive();
    }

    @Override public int getId() { return 12; }
    @Override public String getName() { return "Ogre"; }
    @Override public Material getIconMaterial() { return Material.GOLDEN_APPLE; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLDEN_APPLE); }
    @Override public Lang getDescriptionLang() { return LegendLang.ROLE_DESC_OGRE; }
    @Override protected String applyPlaceholders(String d) {
        return d.replace("%extra_hearts%", String.valueOf(extraHearts / 2)).replace("%start_gapples%", String.valueOf(startGapples));
    }

    @Override
    public void onGive(UHCPlayer u) {
        super.onGive(u); Player p = u.getPlayer(); if (p == null) return;
        p.setMaxHealth(20 + extraHearts); p.setHealth(20 + extraHearts);
        p.getInventory().addItem(new ItemCreator(Material.GOLDEN_APPLE).setAmount(startGapples).getItemstack());
    }
}

