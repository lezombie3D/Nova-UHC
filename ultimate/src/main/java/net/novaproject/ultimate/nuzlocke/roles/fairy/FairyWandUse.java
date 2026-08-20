package net.novaproject.ultimate.nuzlocke.roles.fairy;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.UseAbility;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FairyWandUse extends UseAbility {

    @Var(name = "Nuzlocke Fairy Wand Cd", type = VariableType.TIME)
    private int cdSeconds = 45;

    public FairyWandUse() {
        setCooldown(cdSeconds);
        setMaxUse(-1);
    }

    @Override public String getName() { return "Wand"; }
    @Override public Material getMaterial() { return Material.BLAZE_ROD; }

    @Override
    protected String itemDisplayName() {
        return "Wand de la Fée";
    }

    @Override
    public boolean onEnable(Player player) {
        EnderPearl pearl = player.launchProjectile(EnderPearl.class);
        pearl.setShooter(player);
        return true;
    }
}

