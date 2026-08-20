package net.novaproject.ultimate.nuzlocke.roles.ice;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.BowAbility;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public class IceBlizzardBow extends BowAbility {

    @Var(name = "Nuzlocke Ice Bliz Slow Dur", type = VariableType.INTEGER)
    private int slowSeconds = 15;

    @Var(name = "Nuzlocke Ice Bliz Freeze", type = VariableType.PERCENTAGE)
    private double freezeChance = 0.20;

    @Var(name = "Nuzlocke Ice Bliz Freeze Dur", type = VariableType.INTEGER)
    private int freezeSeconds = 4;

    @Override public String getName() { return "Blizzard"; }
    @Override public Material getMaterial() { return null; }

    @Override
    public boolean onEnable(Player player) {
        if (getTarget() == null) return false;
        Player victim = getTarget().getPlayer();
        if (victim == null) return false;

        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * slowSeconds, 0, true, false), true);
        if (ThreadLocalRandom.current().nextDouble() < freezeChance) {
            freezeCage(victim);
        }
        return true;
    }

    private void freezeCage(Player victim) {
        Block b = victim.getLocation().getBlock();
        int[][] offs = { {1,0,0},{-1,0,0},{0,0,1},{0,0,-1},{0,1,0},{0,-1,0},
                {0,2,0},{1,1,0},{-1,1,0},{0,1,1},{0,1,-1} };
        for (int[] o : offs) {
            Block t = b.getRelative(o[0], o[1], o[2]);
            if (t.getType() == Material.AIR) {
                t.setType(Material.SNOW_BLOCK);
                org.bukkit.Bukkit.getScheduler().runTaskLater(net.novaproject.novauhc.Main.get(),
                        () -> { if (t.getType() == Material.SNOW_BLOCK) t.setType(Material.AIR); },
                        20L * freezeSeconds);
            }
        }
    }
}

