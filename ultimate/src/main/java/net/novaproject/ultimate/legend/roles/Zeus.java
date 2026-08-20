package net.novaproject.ultimate.legend.roles;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.ultimate.legend.LegendLang;
import net.novaproject.ultimate.legend.LegendRole;
import net.novaproject.ultimate.legend.roles.abilities.ZeusEffectsActive;
import net.novaproject.ultimate.legend.roles.abilities.ZeusLightningPassive;
import org.bukkit.Material;

public class Zeus extends LegendRole {

    @Var(name = "Lightning", type = VariableType.ABILITY)
    public Ability lightningPassive;

    @Var(name = "Effects", type = VariableType.ABILITY)
    public Ability effectsActive;

    public Zeus() {
        this.lightningPassive = new ZeusLightningPassive();
        this.effectsActive    = new ZeusEffectsActive();
    }

    @Override public int getId() { return 6; }
    @Override public String getName() { return "Zeus"; }
    @Override public Material getIconMaterial() { return Material.GOLD_AXE; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLD_AXE); }
    @Override public Lang getDescriptionLang() { return LegendLang.ROLE_DESC_ZEUS; }
}

