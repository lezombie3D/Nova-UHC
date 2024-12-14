package net.novaproject.novauhc.scenario;

import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class Scenario {

    public abstract String getName();

    public abstract String getDescription();

    public abstract ItemCreator getItem();

    public Scenario(){
        ScenarioManager.get().addScenario(this);
        setup();
    }

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
    public void onCraft(Player player, ItemStack craft, Inventory inventory) {

    }

    public void setup(){

    }

    public void onStart(){

    }

    public void onSec(Player p){

    }

    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {

    }
}
