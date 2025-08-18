package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Rodless extends Scenario {
    @Override
    public String getName() {
        return "Rodless";
    }

    @Override
    public String getDescription() {
        return "Empêche l'utilisation et la fabrication de cannes à pêche.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.FISHING_ROD);

    }

    @Override
    public void onPlayerInteract(Player player, PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.FISHING_ROD) {
            event.setCancelled(true);
            player.sendMessage("§cLes cannes à pêche sont désactivées !");
        }
    }

    @Override
    public void onCraft(ItemStack result, CraftItemEvent event) {
        ItemStack item = event.getRecipe().getResult();
        if (item.getType() == Material.FISHING_ROD) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage("§cLa fabrication de cannes à pêche est désactivée dans ce scénario !");
        }
    }
}
