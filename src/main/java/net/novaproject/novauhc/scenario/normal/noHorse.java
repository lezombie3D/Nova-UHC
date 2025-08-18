package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class noHorse extends Scenario {
    @Override
    public String getName() {
        return "HorseLess";
    }

    @Override
    public String getDescription() {
        return "Interdit de monter sur les chevaux.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.SADDLE);
    }

    @Override
    public void onPlayerInteractonEntity(Player player, PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() == EntityType.HORSE) {
            player.sendMessage("§cTu ne peux pas monter sur un cheval !");
            event.setCancelled(true);
        }
    }

}
//TODO
