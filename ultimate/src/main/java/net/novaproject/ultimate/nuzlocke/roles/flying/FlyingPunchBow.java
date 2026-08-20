package net.novaproject.ultimate.nuzlocke.roles.flying;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Shooting;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;

public class FlyingPunchBow extends Ability implements Shooting {

    @Var(name = "Nuzlocke Flying Punch Level", type = VariableType.INTEGER)
    private int punchLevel = 1;

    @Override public String getName() { return "Punch Auto"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onBow(Entity shooter, Player target, EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        if (getOwner() == null || !p.equals(getOwner().getPlayer())) return;
        if (!(event.getProjectile() instanceof Arrow arrow)) return;
        arrow.setKnockbackStrength(arrow.getKnockbackStrength() + punchLevel);
    }
}

