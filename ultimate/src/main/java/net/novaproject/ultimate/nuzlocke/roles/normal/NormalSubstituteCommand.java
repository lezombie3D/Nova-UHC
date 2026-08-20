package net.novaproject.ultimate.nuzlocke.roles.normal;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.CommandAbility;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class NormalSubstituteCommand extends CommandAbility {

    @Var(name = "Nuzlocke Normal Sub Radius", type = VariableType.INTEGER)
    private int radius = 20;

    @Var(name = "Nuzlocke Normal Sub Cd", type = VariableType.TIME)
    private int cdSeconds = 300;

    public NormalSubstituteCommand() {
        setCooldown(cdSeconds);
        setMaxUse(-1);
    }

    @Override public String getCommandKey() { return "substitute"; }
    @Override public String getName() { return "Substitute"; }

    @Override
    public boolean onCommand(Player player, String[] args) {
        Location origin = player.getLocation().clone();

        double angle = ThreadLocalRandom.current().nextDouble(2 * Math.PI);
        double dist = ThreadLocalRandom.current().nextDouble(radius);
        double dx = Math.cos(angle) * dist;
        double dz = Math.sin(angle) * dist;
        Location target = origin.clone().add(dx, 0, dz);
        target.setY(player.getWorld().getHighestBlockYAt(target));
        player.teleport(target);

        Zombie zombie = origin.getWorld().spawn(origin, Zombie.class);
        zombie.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
        zombie.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        zombie.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        zombie.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
        zombie.getEquipment().setItemInHand(new ItemStack(Material.IRON_SWORD));
        zombie.setCustomName(player.getName() + " §7(Substitute)");
        zombie.setCustomNameVisible(true);
        return true;
    }
}

