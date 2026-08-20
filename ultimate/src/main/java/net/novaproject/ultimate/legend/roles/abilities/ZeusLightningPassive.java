package net.novaproject.ultimate.legend.roles.abilities;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Attacking;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public class ZeusLightningPassive extends Ability implements Attacking {

    @Var(name = "Zeus Lightning Chance", desc = "Lightning chance on hit.", type = VariableType.PERCENTAGE)
    private double lightningChance = 0.10;

    @Var(name = "Zeus Speed Chance", desc = "Speed I 10s chance.", type = VariableType.PERCENTAGE)
    private double speedChance = 0.20;

    private Player pendingAttacker;
    private Player pendingVictim;

    public ZeusLightningPassive() { setCooldown(0); }

    @Override public String getName() { return "Foudre de Zeus"; }
    @Override public Material getMaterial() { return null; }

    @Override
    public void onAttack(UHCPlayer victimP, EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (getOwner() == null || !attacker.equals(getOwner().getPlayer())) return;
        Player victim = victimP.getPlayer();
        if (victim == null) return;
        pendingAttacker = attacker;
        pendingVictim   = victim;
        tryUse(attacker);
    }

    @Override
    public boolean onEnable(Player player) {
        if (pendingAttacker == null || pendingVictim == null) return false;

        boolean triggered = false;

        if (ThreadLocalRandom.current().nextDouble() < lightningChance) {
            pendingVictim.getWorld().strikeLightningEffect(pendingVictim.getLocation());
            pendingVictim.damage(3.0, pendingAttacker);
            triggered = true;
        }

        if (ThreadLocalRandom.current().nextDouble() < speedChance) {
            pendingAttacker.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0));
            triggered = true;
        }

        pendingAttacker = null;
        pendingVictim   = null;
        return triggered;
    }
}

