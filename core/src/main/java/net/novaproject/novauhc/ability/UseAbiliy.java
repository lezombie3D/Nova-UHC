package net.novaproject.novauhc.ability;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public abstract class UseAbiliy extends Ability {

    private final ItemStack item;

    public UseAbiliy() {
        this.item = getItemStack();
    }

    @Override
    public void onClick(PlayerInteractEvent event, ItemStack item) {

        if (item == null) return;
        if (item.isSimilar(this.item)) {
            tryUse(event.getPlayer());
        }
    }
}
