package net.novaproject.novauhc.ability.template;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Shooting;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;

@Setter
@Getter
public abstract class BowAbility extends Ability implements Shooting {
    private UHCPlayer target;

    public BowAbility() {
    }

    @Override
    public void onBow(Entity shooter, Player target, EntityShootBowEvent event) {
        if (!(event.getProjectile() instanceof Arrow)) return;
        if (!(shooter instanceof Player player)) return;
        if (getOwner() == null || !player.equals(getOwner().getPlayer())) return;
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(target);
        setTarget(uhcPlayer);
        tryUse(player);
    }
}

