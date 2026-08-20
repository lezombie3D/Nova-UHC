package net.novaproject.ultimate.nuzlocke.roles.fighting;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.MeleeAbility;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class FightingCloseCombatMelee extends MeleeAbility {

    @Var(name = "Nuzlocke Fighting Cc Hunger", type = VariableType.INTEGER)
    private int maxHungerBars = 5;

    @Var(name = "Nuzlocke Fighting Cc Radius", type = VariableType.INTEGER)
    private int radius = 4;

    @Var(name = "Nuzlocke Fighting Cc Damage", type = VariableType.DOUBLE)
    private double areaDamage = 4.0;

    @Override public String getName() { return "Close Combat"; }
    @Override public Material getMaterial() { return null; }

    @Override
    public boolean onEnable(Player player) {
        int hungerBars = player.getFoodLevel() / 2;
        if (hungerBars > maxHungerBars) return false;
        Player owner = player;
        for (LivingEntity e : owner.getWorld().getLivingEntities()) {
            if (e.equals(owner)) continue;
            if (e.getLocation().distance(owner.getLocation()) > radius) continue;
            e.damage(areaDamage, owner);
        }
        return true;
    }
}

