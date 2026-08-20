package net.novaproject.ultimate.nuzlocke.roles.electric;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.PassiveAbility;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;

public class ElectricStormEnv extends PassiveAbility {

    @Var(name = "Nuzlocke Electric Storm Range", type = VariableType.INTEGER)
    private int stormRange = 50;

    @Override public String getName() { return "Tempête"; }
    @Override public Material getMaterial() { return null; }

    @Override
    public boolean onEnable(Player player) {
        if (!player.getWorld().hasStorm()) return false;
        for (Entity e : player.getNearbyEntities(stormRange, stormRange, stormRange)) {
            if (e instanceof Creeper creeper && !creeper.isPowered()) {
                creeper.setPowered(true);
            } else if (e instanceof Pig pig && !(pig instanceof PigZombie)) {
                pig.getWorld().spawn(pig.getLocation(), PigZombie.class);
                pig.remove();
            }
        }
        return true;
    }
}

