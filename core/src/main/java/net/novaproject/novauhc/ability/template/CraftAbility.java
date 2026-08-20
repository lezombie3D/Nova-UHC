package net.novaproject.novauhc.ability.template;

import net.novaproject.novauhc.ability.craft.CraftableAbility;
import net.novaproject.novauhc.player.UHCPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;


public abstract class CraftAbility extends UseAbility implements CraftableAbility {

    public abstract Material[] getCraftPattern();

    public abstract ItemStack getCraftResult();

    public abstract String getCraftName();

    public abstract String getCraftDescription();


    public boolean isCraftGated() {
        return true;
    }

    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        registerCraft();
        if (isCraftGated()) this.setActive(false);
    }

    @Override
    public void onCrafted(Player player, CraftItemEvent event) {
        setActive(true);
    }
}

