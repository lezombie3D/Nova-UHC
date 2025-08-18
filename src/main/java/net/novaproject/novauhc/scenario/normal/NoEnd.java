package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerPortalEvent;

public class NoEnd extends Scenario {
    @Override
    public String getName() {
        return "EndLess";
    }

    @Override
    public String getDescription() {
        return "Désactive complètement l'accès à l'End.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.ENDER_STONE);
    }

    @Override
    public void onPortal(PlayerPortalEvent event) {
        if (event.getCause() == PlayerPortalEvent.TeleportCause.END_PORTAL) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cL'accès a l'End est désactivé !");
        }
    }
}
