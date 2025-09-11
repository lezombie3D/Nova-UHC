package net.novaproject.novauhc.ability;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@Getter
@Setter
public abstract class MeleeAbility extends Ability {

    private UHCPlayer target;

    public MeleeAbility(UHCPlayer target) {
        this.target = target;
    }

    @Override
    public void onAttack(UHCPlayer victimP, EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        setTarget(victimP);
        tryUse(player);
    }


}
