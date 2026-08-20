package net.novaproject.novauhc.ability.template;

import java.util.concurrent.ThreadLocalRandom;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.utils.variable.Var;
import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Attacking;
import net.novaproject.novauhc.player.UHCPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@Getter
@Setter
public abstract class MeleeAbility extends Ability implements Attacking {

    private UHCPlayer target;

    @Override public Material getMaterial() { return null; }

    @Var(name = "Chance de déclenchement",
            desc = "Probabilité (0-1) que le coup déclenche l'effet.",
            type = VariableType.PERCENTAGE, min = 0, max = 1)
    private double procChance = 1.0;


    @Override
    public void onAttack(UHCPlayer victimP, EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (getOwner() == null || !player.equals(getOwner().getPlayer())) return;
        if (procChance < 1.0
                && ThreadLocalRandom.current().nextDouble() >= procChance) return;
        setTarget(victimP);
        tryUse(player);
    }
}