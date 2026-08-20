package net.novaproject.ultimate.nuzlocke.roles.steel;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Mining;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class SteelIronOreReliefListener extends Ability implements Mining {

    @Var(name = "Nuzlocke Steel Relief Dur", type = VariableType.TIME)
    private int reliefSeconds = 60;

    @Override public String getName() { return "Iron Relief"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (getOwner() == null || !event.getPlayer().equals(getOwner().getPlayer())) return;
        if (event.getBlock().getType() != Material.IRON_ORE) return;
        Steel.RELIEF_UNTIL.put(event.getPlayer().getUniqueId(),
                System.currentTimeMillis() + reliefSeconds * 1000L);
        event.getPlayer().sendMessage("§7§lSteel §8│ §7Le métal vous ragaillardit (Slowness 1 pendant " + reliefSeconds + "s).");
    }
}

