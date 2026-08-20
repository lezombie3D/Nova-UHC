package net.novaproject.ultimate.legend.roles;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.Lang;

import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.ultimate.legend.LegendLang;
import net.novaproject.ultimate.legend.LegendRole;
import net.novaproject.ultimate.legend.roles.abilities.*;
import org.bukkit.Material;

public class Corne extends LegendRole {

    @Var(name = "Weakness", type = VariableType.ABILITY)
    public Ability weaknessPassive;

    @Var(name = "Fire Melody", type = VariableType.ABILITY)
    public Ability melodieFeu;

    @Var(name = "Heal Melody", type = VariableType.ABILITY)
    public Ability melodieHeal;

    @Var(name = "Metal Melody", type = VariableType.ABILITY)
    public Ability melodieMetal;

    @Var(name = "Air Melody", type = VariableType.ABILITY)
    public Ability melodieAir;

    public Corne() {
        this.weaknessPassive = new CorneWeaknessPassive();
        this.melodieFeu      = new CorneMelodieFeu();
        this.melodieHeal     = new CorneMelodieHeal();
        this.melodieMetal    = new CorneMelodieMetal();
        this.melodieAir      = new CorneMelodieAir();
    }

    @Override public int getId() { return 16; }
    @Override public String getName() { return "Corne"; }
    @Override public Material getIconMaterial() { return Material.NOTE_BLOCK; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.NOTE_BLOCK); }
    @Override public Lang getDescriptionLang() { return LegendLang.ROLE_DESC_CORNE; }
}

