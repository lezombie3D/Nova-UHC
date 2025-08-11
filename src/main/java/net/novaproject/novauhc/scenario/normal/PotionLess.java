package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PotionLess extends Scenario {
    @Override
    public String getName() {
        return "Potionless";
    }

    @Override
    public String getDescription() {
        return "Aucune potion n'est utilisable";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.POTION);
    }

    @Override
    public void onPlayerInteract(Player player, PlayerInteractEvent event) {
        if (!isActive()) return;

        ItemStack item = event.getItem();
        if (item != null && item.getType().toString().contains("POTION")) {
            event.setCancelled(true);
            }
        }
    }

