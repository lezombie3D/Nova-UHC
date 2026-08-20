package net.novaproject.ultimate.nuzlocke.roles.flying;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Attacking;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class FlyingKnockbackListener extends Ability implements Attacking {

    @Var(name = "Nuzlocke Flying Kb Mult", type = VariableType.DOUBLE)
    private double extraKb = 1.5;

    @Override public String getName() { return "Vulnérabilité aux KB"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onAttack(UHCPlayer victimP, EntityDamageByEntityEvent event) {
        if (getOwner() == null) return;
        Player p = getOwner().getPlayer();
        if (p == null) return;
        if (!event.getEntity().equals(p)) return;
        new BukkitRunnable() {
            @Override public void run() {
                if (!p.isOnline()) return;
                Vector v = p.getVelocity();
                p.setVelocity(new Vector(v.getX() * extraKb, v.getY() * extraKb, v.getZ() * extraKb));
            }
        }.runTaskLater(net.novaproject.novauhc.Main.get(), 1L);
    }
}

