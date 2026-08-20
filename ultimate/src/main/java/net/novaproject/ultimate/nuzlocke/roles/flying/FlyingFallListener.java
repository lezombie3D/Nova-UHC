package net.novaproject.ultimate.nuzlocke.roles.flying;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Hurtable;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FlyingFallListener extends Ability implements Hurtable {

    @Var(name = "Nuzlocke Flying Fall Boost Level", type = VariableType.INTEGER)
    private int boostLevel = 5;

    @Var(name = "Nuzlocke Flying Fall Boost Ticks", type = VariableType.INTEGER)
    private int boostTicks = 20;

    @Override public String getName() { return "Plumes"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onTakeDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        if (getOwner() == null || !p.equals(getOwner().getPlayer())) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        event.setCancelled(true);
        boolean jumpOn = Flying.JUMP_ON.getOrDefault(p.getUniqueId(), true);
        if (jumpOn) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, boostTicks, boostLevel, true, false), true);
        }
    }
}

