package net.novaproject.ultimate.nuzlocke.roles.poison;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Hurtable;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

public class PoisonImmunityListener extends Ability implements Hurtable {

    @Var(name = "Nuzlocke Poison Heal", type = VariableType.DOUBLE)
    private double poisonHealHearts = 0.5;

    @Override public String getName() { return "Immunité Poison"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onTakeDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        if (getOwner() == null || !p.equals(getOwner().getPlayer())) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.POISON
                && event.getCause() != EntityDamageEvent.DamageCause.MAGIC) return;
        if (p.hasPotionEffect(PotionEffectType.POISON)) {
            event.setCancelled(true);
            p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + 2 * poisonHealHearts));
        }
    }

}

