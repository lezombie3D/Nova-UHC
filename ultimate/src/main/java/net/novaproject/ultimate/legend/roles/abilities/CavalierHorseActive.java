package net.novaproject.ultimate.legend.roles.abilities;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.UseAbility;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CavalierHorseActive extends UseAbility {

    @Var(name = "Cavalier Horse HP", desc = "Horse HP.", type = VariableType.INTEGER)
    private int horseHealth = 40;

    public CavalierHorseActive() { setCooldown(300); setMaxUse(-1); }

    @Override public String getName() { return "Cheval Royal"; }

    @Override
    public boolean onEnable(Player player) {
        Horse horse = player.getWorld().spawn(player.getLocation(), Horse.class);
        horse.setCustomName("§6Cheval Royal"); horse.setCustomNameVisible(true);
        horse.setTamed(true); horse.setOwner(player); horse.setAdult();
        horse.setColor(Horse.Color.WHITE); horse.setJumpStrength(1.0);
        horse.setMaxHealth(Math.max(horseHealth, 2)); horse.setHealth(Math.max(horseHealth, 2));
        horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
        horse.setPassenger(player);
        return true;
    }
}

