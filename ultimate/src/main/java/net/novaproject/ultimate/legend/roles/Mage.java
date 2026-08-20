package net.novaproject.ultimate.legend.roles;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.Lang;

import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.ultimate.legend.LegendLang;
import net.novaproject.ultimate.legend.LegendRole;
import net.novaproject.ultimate.legend.roles.abilities.MagePotionPassive;
import org.bukkit.Material;

public class Mage extends LegendRole {

    @Var(name = "Potion", type = VariableType.ABILITY)
    public Ability potionPassive;

    public Mage() {
        this.potionPassive = new MagePotionPassive();
    }

    @Override public int getId() { return 2; }
    @Override public String getName() { return "Mage"; }
    @Override public Material getIconMaterial() { return Material.BLAZE_ROD; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.BLAZE_ROD); }
    @Override public Lang getDescriptionLang() { return LegendLang.ROLE_DESC_MAGE; }
}

