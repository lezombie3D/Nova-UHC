package net.novaproject.ultimate.nuzlocke.roles.psychic;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.ultimate.nuzlocke.NuzlockeLang;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.ultimate.nuzlocke.NuzlockeRole;
import org.bukkit.Material;

public class Psychic extends NuzlockeRole {

    @Var(name = "Nuzlocke Psychic Mindread", type = VariableType.ABILITY)
    private Ability mindread;

    @Var(name = "Nuzlocke Psychic Buffs", type = VariableType.ABILITY)
    private Ability buffs;

    public Psychic() {
        this.mindread = new PsychicMindreadCommand();
        this.buffs = new PsychicBuffsListener();
    }

    @Override public int getId() { return 15; }
    @Override public String getName() { return "Psychic"; }
    @Override public String getTypeColor() { return "§d"; }
    @Override public Material getIconMaterial() { return Material.ENDER_PEARL; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.ENDER_PEARL).setName("§d§lPsychic"); }
    @Override public Lang getDescriptionLang() { return NuzlockeLang.ROLE_DESC_PSYCHIC; }
}

