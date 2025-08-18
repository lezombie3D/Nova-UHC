package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
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

        for (int x = bX - 5; x <= bX + 5; x++) {
            for (int y = bY - 5; y <= bY + 5; y++) {
                for (int z = bZ - 5; z <= bZ + 5; z++) {

                    double distance = ((bX - x) * (bX - x) + ((bZ - z) * (bZ - z)) + ((bY - y) * (bY - y)));

                    if (distance < 5 * 5 && !(distance < ((5 - 1) * (5 - 1)))) {

                        Location block = new Location(centerBlock.getWorld(), x, y, z);
                        circleBlocks.add(block);
                    }
                }
            }
        }
        return circleBlocks;
    }
}
