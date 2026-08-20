package net.novaproject.ultimate.legend.roles.abilities;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Attacking;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class SuccubeLifestealPassive extends Ability implements Attacking {

    @Var(name = "Succube Lifesteal", desc = "HP stolen on melee hit.", type = VariableType.DOUBLE)
    private double lifestealAmount = 1.0;

    private Player pendingAttacker;

    public SuccubeLifestealPassive() { setCooldown(0); }

    @Override public String getName() { return "Vol de Vie"; }
    @Override public Material getMaterial() { return null; }

    @Override
    public void onAttack(UHCPlayer victimP, EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (getOwner() == null || !attacker.equals(getOwner().getPlayer())) return;
        pendingAttacker = attacker;
        tryUse(attacker);
    }

    @Override
    public boolean onEnable(Player player) {
        if (pendingAttacker == null) return false;
        pendingAttacker.setHealth(
                Math.min(pendingAttacker.getHealth() + lifestealAmount, pendingAttacker.getMaxHealth()));
        pendingAttacker = null;
        return true;
    }
}

