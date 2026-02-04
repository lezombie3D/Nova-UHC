package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
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

import java.util.HashMap;
import java.util.Map;

public class TimeBombe extends Scenario {

    @ScenarioVariable(
            name = "Explosion Delay",
            description = "Temps en secondes avant l'explosion après la mort du joueur.",
            type = VariableType.INTEGER
    )
    private int explosion_delay = 30;

    @ScenarioVariable(
            name = "Explosion Power",
            description = "Puissance de l'explosion créée par la TimeBombe.",
            type = VariableType.INTEGER
    )
    private int explosion_power = 2;

    @ScenarioVariable(
            name = "Give Golden Apple",
            description = "Donner une pomme d'or supplémentaire dans le coffre.",
            type = VariableType.BOOLEAN
    )
    private boolean give_golden_apple = true;

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

        if (UHCManager.get().getTimer() <= UHCManager.get().getTimerpvp()) return;

        event.getDrops().clear();

        Location loc = uhcPlayer.getPlayer().getLocation().clone();
        Block block = loc.getBlock().getRelative(BlockFace.DOWN);

        block.setType(Material.CHEST);
        Chest chest = (Chest) block.getState();

        block.getRelative(BlockFace.NORTH).setType(Material.CHEST);

        if (give_golden_apple) {
            chest.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
        }

        for (ItemStack item : uhcPlayer.getPlayer().getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                chest.getInventory().addItem(item);
            }
        }

        for (ItemStack item : uhcPlayer.getPlayer().getInventory().getArmorContents()) {
            if (item != null && item.getType() != Material.AIR) {
                chest.getInventory().addItem(item);
            }
        }

        final ArmorStand stand = loc.getWorld().spawn(
                chest.getLocation().clone().add(0.5, 1, 0),
                ArmorStand.class
        );

        stand.setCustomNameVisible(true);
        stand.setSmall(true);
        stand.setGravity(false);
        stand.setVisible(false);
        stand.setMarker(true);

        new BukkitRunnable() {
            private int time = explosion_delay + 1;

            @Override
            public void run() {
                time--;

                if (time <= 0) {
                    Bukkit.broadcastMessage(
                            CommonString.getMessage(
                                    CommonString.TIMEBOMB_EXPLOSION.getRawMessage(),
                                    uhcPlayer
                            )
                    );

                    loc.getBlock().setType(Material.AIR);
                    loc.getWorld().createExplosion(
                            loc.getX() + 0.5,
                            loc.getY() + 0.5,
                            loc.getZ() + 0.5,
                            explosion_power,
                            false,
                            true
                    );

                    loc.getWorld().strikeLightning(loc);
                    stand.remove();
                    cancel();
                    return;
                }

                if (time == 1) stand.setCustomName("§4" + time + "s");
                else if (time == 2) stand.setCustomName("§c" + time + "s");
                else if (time == 3) stand.setCustomName("§6" + time + "s");
                else if (time <= 15) stand.setCustomName("§e" + time + "s");
                else stand.setCustomName("§a" + time + "s");
            }
        }.runTaskTimer(Main.get(), 0, 20);
    }
}
