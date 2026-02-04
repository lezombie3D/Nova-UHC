package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.utils.VariableType;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.event.entity.PlayerDeathEvent;

public class NoCleanUp extends Scenario {

    @ScenarioVariable(
            name = "Health Restore",
            description = "Nombre de cœurs restaurés au tueur lors d'une élimination.",
            type = VariableType.DOUBLE
    )
    private double healthRestore = 8.0;

    @Override
    public String getName() {
        return "NoCleanUp";
    }

    @Override
    public String getDescription() {
        return "Tuer un joueur restaure " + (healthRestore / 2) + " cœurs de vie au tueur.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.GOLD_NUGGET);
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (killer != null) {
            double nextHealth = killer.getPlayer().getHealth() + healthRestore;
            killer.getPlayer().setHealth(Math.min(nextHealth, killer.getPlayer().getMaxHealth()));
        }
    }
}
