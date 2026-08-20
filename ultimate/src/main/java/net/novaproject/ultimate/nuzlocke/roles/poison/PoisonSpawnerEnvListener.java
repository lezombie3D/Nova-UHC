package net.novaproject.ultimate.nuzlocke.roles.poison;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Mining;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.SpawnEgg;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.util.concurrent.ThreadLocalRandom;

public class PoisonSpawnerEnvListener extends Ability implements Mining {

    @Var(name = "Nuzlocke Poison Spawner Drop", type = VariableType.PERCENTAGE)
    private double potionDropChance = 0.25;

    @Override public String getName() { return "Spawner Toxique"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (getOwner() == null || !event.getPlayer().equals(getOwner().getPlayer())) return;
        if (event.getBlock().getType() != Material.MOB_SPAWNER) return;
        Location loc = event.getBlock().getLocation().add(0.5, 0.5, 0.5);
        SpawnEgg egg = new SpawnEgg(EntityType.CAVE_SPIDER);
        event.getBlock().getWorld().dropItemNaturally(loc, egg.toItemStack(1));
        if (ThreadLocalRandom.current().nextDouble() < potionDropChance) {
            event.getBlock().getWorld().dropItemNaturally(loc, makePoisonPotion());
        }
    }

    private ItemStack makePoisonPotion() {
        Potion p = new Potion(PotionType.POISON);
        p.setSplash(true);
        return p.toItemStack(1);
    }
}

