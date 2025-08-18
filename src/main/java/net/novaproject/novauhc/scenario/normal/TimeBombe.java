package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class TimeBombe extends Scenario {
    @Override
    public String getName() {
        return "TimeBombe";
    }

    @Override
    public String getDescription() {
        return "Les joueurs morts explosent après un délai, créant un cratère.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.TNT);
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {

        if (UHCManager.get().getTimer() > UHCManager.get().getTimerpvp()) {
            event.getDrops().clear();
            Location loc = uhcPlayer.getPlayer().getLocation().clone();
            Block block = uhcPlayer.getPlayer().getLocation().getBlock();

            block = block.getRelative(BlockFace.DOWN);
            block.setType(Material.CHEST);

            Chest chest = (Chest) block.getState();

            block = block.getRelative(BlockFace.NORTH);
            block.setType(Material.CHEST);
            chest.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
            for (ItemStack item : uhcPlayer.getPlayer().getInventory().getContents()) {
                if (item == null || item.getType() == Material.AIR) {
                    continue;
                }

                chest.getInventory().addItem(item);
            }


            for (ItemStack item : uhcPlayer.getPlayer().getInventory().getArmorContents()) {
                if (item == null || item.getType() == Material.AIR) {
                    continue;
                }

                chest.getInventory().addItem(item);
            }

            final ArmorStand stand = uhcPlayer.getPlayer().getWorld().spawn(chest.getLocation().clone().add(0.5, 1, 0), ArmorStand.class);

            stand.setCustomNameVisible(true);
            stand.setSmall(true);

            stand.setGravity(false);
            stand.setVisible(false);

            stand.setMarker(true);

            new BukkitRunnable() {
                private int time = 30 + 1; // add one for countdown.

                public void run() {
                    time--;

                    if (time == 0) {
                        Bukkit.broadcastMessage("§a" + uhcPlayer.getPlayer().getName() + "'s §fcorpse has exploded!");

                        loc.getBlock().setType(Material.AIR);

                        loc.getWorld().createExplosion(loc.getBlockX() + 0.5, loc.getBlockY() + 0.5, loc.getBlockZ() + 0.5, 2, false, true);
                        loc.getWorld().strikeLightning(loc); // Using actual lightning to kill the items.

                        stand.remove();
                        cancel();
                    } else if (time == 1) {
                        stand.setCustomName("§4" + time + "s");
                    } else if (time == 2) {
                        stand.setCustomName("§c" + time + "s");
                    } else if (time == 3) {
                        stand.setCustomName("§6" + time + "s");
                    } else if (time <= 15) {
                        stand.setCustomName("§e" + time + "s");
                    } else {
                        stand.setCustomName("§a" + time + "s");
                    }
                }
            }.runTaskTimer(Main.get(), 0, 20);
        }
    }

}

