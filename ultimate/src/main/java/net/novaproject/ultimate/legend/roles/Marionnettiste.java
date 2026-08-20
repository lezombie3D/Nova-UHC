package net.novaproject.ultimate.legend.roles;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.Lang;

import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.ultimate.legend.LegendLang;
import net.novaproject.ultimate.legend.LegendRole;
import net.novaproject.ultimate.legend.roles.abilities.MarionnettistePuppetPassive;
import org.bukkit.Material;
import java.util.UUID;

public class Marionnettiste extends LegendRole {

    @Var(name = "Puppet", type = VariableType.ABILITY)
    public Ability puppetPassive;

    public Marionnettiste() {
        this.puppetPassive = new MarionnettistePuppetPassive();
    }

    @Override public int getId() { return 17; }
    @Override public String getName() { return "Marionnettiste"; }
    @Override public Material getIconMaterial() { return Material.STRING; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.STRING); }
    @Override public Lang getDescriptionLang() { return LegendLang.ROLE_DESC_MARIONNETTISTE; }

    @Override
    public void onKill(UHCPlayer killer, UHCPlayer victim) {
        super.onKill(killer, victim);
        if (getOwner() != null && getOwner().equals(killer)) {
            ((MarionnettistePuppetPassive) puppetPassive).createPuppet(victim, killer);
        }
    }

    public boolean isPuppet(UUID uuid) {
        return ((MarionnettistePuppetPassive) puppetPassive).isPuppet(uuid);
    }
}

