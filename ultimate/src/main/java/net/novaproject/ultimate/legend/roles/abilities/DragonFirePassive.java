package net.novaproject.ultimate.legend.roles.abilities;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Attacking;
import net.novaproject.novauhc.ability.AbilityHooks.Ticking;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public class DragonFirePassive extends Ability implements Ticking, Attacking {

    @Var(name = "Dragon Fire Chance", desc = "Ignite chance on melee.", type = VariableType.PERCENTAGE)
    private double fireChance = 0.30;

    @Var(name = "Dragon Fire Duration (s)", desc = "Fire duration.", type = VariableType.INTEGER)
    private int fireDuration = 5;

    private Player pendingVictim;

    public DragonFirePassive() { setCooldown(0); }

    @Override public String getName() { return "Souffle du Dragon"; }
    @Override public Material getMaterial() { return null; }

    @Override
    public void onSec(Player player) {
        player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, 0, false, false));
    }

    @Override
    public void onAttack(UHCPlayer victimP, EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (getOwner() == null || !attacker.equals(getOwner().getPlayer())) return;
        Player victim = victimP.getPlayer();
        if (victim == null) return;
        pendingVictim = victim;
        tryUse(attacker);
    }

    @Override
    public boolean onEnable(Player player) {
        if (pendingVictim == null) return false;
        boolean ignited = false;
        if (ThreadLocalRandom.current().nextDouble() < fireChance) {
            pendingVictim.setFireTicks(20 * fireDuration);
            ignited = true;
        }
        pendingVictim = null;
        return ignited;
    }
}

