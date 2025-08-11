package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.event.entity.PlayerDeathEvent;

public class NoCleanUp extends Scenario {
    @Override
    public String getName() {
        return "NoCleanUp";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.GOLD_NUGGET);
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (killer != null) {
            double nexthealth = killer.getPlayer().getHealth() + 8;
            if (nexthealth <= killer.getPlayer().getMaxHealth()) {
                killer.getPlayer().setHealth(killer.getPlayer().getHealth() + 8);
            } else killer.getPlayer().setHealth(killer.getPlayer().getMaxHealth());
        }
    }
}
