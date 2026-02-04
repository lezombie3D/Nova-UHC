package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerPortalEvent;

import java.util.Optional;

public class NoNether extends Scenario {
    @Override
    public String getName() {
        return "NetherLess";
    }

    @Override
    public String getDescription() {
        return "Désactive complètement l'accès au Nether.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.NETHERRACK);
    }

    @Override
    public void onPortal(PlayerPortalEvent event) {

        Optional<Scenario> netheriBus = ScenarioManager.get().getScenarioByName("NetheriBus");

        if (netheriBus.isPresent() && ScenarioManager.get().getActiveScenarios().contains(netheriBus.get())) {

        } else {
            if (event.getCause() == PlayerPortalEvent.TeleportCause.NETHER_PORTAL) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cL'accès au Nether est désactivé !");
            }
        }

    }
}
