package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class FastFurnace extends Scenario {
    @Override
    public String getName() {
        return "FastFurnace";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.FURNACE);
    }

    @Override
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        startUpdate((Furnace) event.getBlock().getState());
    }

    private void startUpdate(final Furnace block) {

        new BukkitRunnable() {

            public void run() {

                if ((block.getCookTime() > 0) || (block.getBurnTime() > 0)) {

                    block.setCookTime((short) (block.getCookTime() + 4));
                    block.update();

                } else {
                    cancel();
                }
            }
        }.runTaskTimer(Main.get(), 1L, 1L);
    }
}
