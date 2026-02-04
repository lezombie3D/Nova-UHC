package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.List;

public class WebCage extends Scenario {
    @Override
    public String getName() {
        return "WebCages";
    }
    @ScenarioVariable(name = "Taille de la cage",description = "Permet de definir la taille de WebCage",type = VariableType.INTEGER)
    public int size = 5;
    
    @Override
    public String getDescription() {
        return "À la mort, le joueur est entouré d'une cage de toiles d'araignée.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.WEB);
    }


    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (UHCManager.get().getTimer() > UHCManager.get().getTimerpvp()) {
            for (Location block : getSphere(uhcPlayer.getPlayer().getLocation())) {
                Block b = block.getBlock();
                if (b.getType() == Material.AIR || b.getType() == Material.LONG_GRASS) {
                    b.setType(Material.WEB);
                }

            }
        }
    }

    private List<Location> getSphere(Location centerBlock) {

        List<Location> circleBlocks = new ArrayList<>();

        int bX = centerBlock.getBlockX();
        int bY = centerBlock.getBlockY();
        int bZ = centerBlock.getBlockZ();

        for (int x = bX - size; x <= bX + size; x++) {
            for (int y = bY - size; y <= bY + size; y++) {
                for (int z = bZ - size; z <= bZ + size; z++) {

                    double distance = ((bX - x) * (bX - x) + ((bZ - z) * (bZ - z)) + ((bY - y) * (bY - y)));

                    if (distance < size * size && !(distance < ((size - 1) * (size - 1)))) {

                        Location block = new Location(centerBlock.getWorld(), x, y, z);
                        circleBlocks.add(block);
                    }
                }
            }
        }
        return circleBlocks;
    }
}
