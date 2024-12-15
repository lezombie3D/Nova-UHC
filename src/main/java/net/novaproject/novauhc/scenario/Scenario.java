package net.novaproject.novauhc.scenario;

import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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

    public void setup(){

    }

    public void onStart(){

    }

    public void onSec(Player p){

    }

    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {

    }
    public void onCraft(ItemStack result, CraftItemEvent event){

    }
    public void onPlayerInteract(Player player,PlayerInteractEvent event){

    }
    public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {

    }
    public void onPlace(Player player, Block block, BlockPlaceEvent event){

    }
}
