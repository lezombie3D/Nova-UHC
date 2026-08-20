package net.novaproject.ultimate.legend.roles.abilities;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Attacking;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public class ArcherBowPassive extends Ability implements Attacking {

    @Var(name = "Bow Bonus Damage", desc = "Bonus arrow damage.", type = VariableType.DOUBLE)
    private double bowBonusDamage = 1.5;

    @Var(name = "Arrow Slow Chance", desc = "Chance to apply Slowness.", type = VariableType.PERCENTAGE)
    private double slowChance = 0.25;

    @Var(name = "Arrow Slow Duration (s)", desc = "Slowness duration.", type = VariableType.TIME)
    private int slowDuration = 5;

    private Player pendingVictim;
    private EntityDamageByEntityEvent pendingEvent;

    @Override public String getName() { return "Maîtrise de l'Arc"; }
    @Override public Material getMaterial() { return null; }

    @Override
    public void onAttack(UHCPlayer victimP, EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player shooter)) return;
        if (getOwner() == null || !shooter.equals(getOwner().getPlayer())) return;

        Player victim = victimP.getPlayer();
        if (victim == null) return;

        pendingVictim = victim;
        pendingEvent  = event;
        tryUse(shooter);
    }

    @Override
    public boolean onEnable(Player player) {
        if (pendingEvent == null || pendingVictim == null) return false;

        pendingEvent.setDamage(pendingEvent.getDamage() + bowBonusDamage);

        if (ThreadLocalRandom.current().nextDouble() < slowChance) {
            pendingVictim.addPotionEffect(
                    new PotionEffect(PotionEffectType.SLOW, 20 * slowDuration, 0));
        }

        pendingVictim = null;
        pendingEvent  = null;
        return true;
    }
}

