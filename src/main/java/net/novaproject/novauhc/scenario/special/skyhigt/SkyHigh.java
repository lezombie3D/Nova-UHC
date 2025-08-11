package net.novaproject.novauhc.scenario.special.skyhigt;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;


public class SkyHigh extends Scenario {
    @Override
    public String getName() {
        return "SkyHigh";
    }

    @Override
    public String getDescription() {
        return "ça va haut";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.PAPER);

    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        if (UHCManager.get().getTeam_size() != 1) {
            UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
        } else {
            uhcPlayer.getPlayer().teleport(location);
        }
    }

    @Override
    public void onStart(Player player) {
        player.getInventory().addItem(new ItemStack(Material.DIRT));
    }

    @Override
    public void onSec(Player p) {
        int timer = UHCManager.get().getTimer();
        if (timer == UHCManager.get().getTimerborder() - 120) {
            p.sendMessage(Common.get().getInfoTag() + ChatColor.RED + "Vous avez 2 minutes pour vous élever au ciel !");
        }
        if (timer >= UHCManager.get().getTimerborder()) {
            if (p.getLocation().getBlockY() < 160) {
                p.damage(1);
            } else if (p.getLocation().getBlockY() < 100) {
                p.damage(2);
            } else if (p.getLocation().getBlockY() < 70) {
                p.damage(4);
            }
        }
    }

    @Override
    public void onPlace(Player player, Block block, BlockPlaceEvent event) {
        if (block.getType() == Material.DIRT) {
            int dirtCount = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == Material.DIRT) {
                    dirtCount += item.getAmount();
                }
            }
            if (dirtCount - 2 < 64) {
                player.getInventory().addItem(new ItemStack(Material.DIRT, 2));
            }
        }
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

}
