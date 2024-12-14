package net.novaproject.novauhc.scenario;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public abstract class Scenario {

    public abstract String getName();

    public abstract String getDescription();

    private boolean active = false;

    public void toggleActive() {
        active = !active;
    }

    public boolean isActive() {
        return active;
    }

    public void onBreak(Player player, Block block, BlockBreakEvent event) {

    }

    public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {

    }
}
