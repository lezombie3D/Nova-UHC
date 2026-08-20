package net.novaproject.ultimate.legend.roles;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.ultimate.legend.LegendLang;
import net.novaproject.ultimate.legend.LegendRole;
import net.novaproject.ultimate.legend.roles.abilities.SuccubeAbsorptionActive;
import net.novaproject.ultimate.legend.roles.abilities.SuccubeLifestealPassive;
import org.bukkit.Material;

public class Succube extends LegendRole {

    @Var(name = "Succube Lifesteal", type = VariableType.ABILITY)
    public Ability lifestealPassive;

    @Var(name = "Absorption", type = VariableType.ABILITY)
    public Ability absorptionActive;

    public Succube() {
        this.lifestealPassive = new SuccubeLifestealPassive();
        this.absorptionActive = new SuccubeAbsorptionActive();
    }

    @Override public int getId() { return 8; }
    @Override public String getName() { return "Succube"; }
    @Override public Material getIconMaterial() { return Material.REDSTONE; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.REDSTONE); }
    @Override public Lang getDescriptionLang() { return LegendLang.ROLE_DESC_SUCCUBE; }
}

