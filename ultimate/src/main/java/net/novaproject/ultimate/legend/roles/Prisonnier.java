package net.novaproject.ultimate.legend.roles;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.ultimate.legend.LegendLang;
import net.novaproject.ultimate.legend.LegendRole;
import net.novaproject.ultimate.legend.roles.abilities.PrisonnierChainActive;
import net.novaproject.ultimate.legend.roles.abilities.PrisonnierSpeedPassive;
import org.bukkit.Material;

public class Prisonnier extends LegendRole {

    @Var(name = "Speed", type = VariableType.ABILITY)
    public Ability speedPassive;

    @Var(name = "Chain", type = VariableType.ABILITY)
    public Ability chainActive;

    public Prisonnier() {
        this.speedPassive = new PrisonnierSpeedPassive();
        this.chainActive  = new PrisonnierChainActive();
    }

    @Override public int getId() { return 15; }
    @Override public String getName() { return "Prisonnier"; }
    @Override public Material getIconMaterial() { return Material.IRON_FENCE; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.IRON_FENCE); }
    @Override public Lang getDescriptionLang() { return LegendLang.ROLE_DESC_PRISONNIER; }
}

