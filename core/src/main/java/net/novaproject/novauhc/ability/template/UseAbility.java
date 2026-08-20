package net.novaproject.novauhc.ability.template;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Clicking;
import net.novaproject.novauhc.ability.AbilityHooks.Given;
import net.novaproject.novauhc.debug.DebugLog.AbilityDebug;
import net.novaproject.novauhc.player.UHCPlayer;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public abstract class UseAbility extends Ability implements Clicking, Given {

    @Override
    public void onClick(PlayerInteractEvent event, ItemStack item) {
        if (item == null) return;
        if (getOwner() == null) {
            AbilityDebug.onClickSkipped(this, event.getPlayer().getName(), "owner=null (ability not bound to a player)");
            return;
        }
        if (!event.getPlayer().equals(getOwner().getPlayer())) {
            return;
        }
        boolean matched = isAbilityItem(item);
        AbilityDebug.onClickAttempt(this, event.getPlayer().getName(), item, getItemStack(), matched);
        if (!matched) return;

        tryUse(event.getPlayer());
    }

    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        giveAbilityItem(uhcPlayer);
    }
}

