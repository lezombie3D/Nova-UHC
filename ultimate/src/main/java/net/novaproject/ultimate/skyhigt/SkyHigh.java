package net.novaproject.ultimate.skyhigt;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.scenario.ScenarioLangManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
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
        return "Force les joueurs à rester en hauteur, les dégâts augmentent en dessous d'une certaine altitude.";
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
            ScenarioLangManager.send(p, SkyHigthLang.WARNING_SKY_HIGH);
        }
        if (timer >= UHCManager.get().getTimerborder()) {
            if (p.getLocation().getBlockY() < getConfig().getInt("third_level")) {
                ScenarioLangManager.send(p, SkyHigthLang.DAMAGE_THIRD_LAYER);
                p.damage(getConfig().getInt("third_damage"));
            } else if (p.getLocation().getBlockY() < getConfig().getInt("second_level")) {
                ScenarioLangManager.send(p, SkyHigthLang.DAMAGE_SECOND_LAYER);
                p.damage(getConfig().getInt("second_damage"));
            } else if (p.getLocation().getBlockY() < getConfig().getInt("first_level")) {
                ScenarioLangManager.send(p, SkyHigthLang.DAMAGE_FIRST_LAYER);
                p.damage(getConfig().getInt("firth_damage"));
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

    @Override
    public String getPath() {
        return "special/skyhigh";
    }


    @Override
    public ScenarioLang[] getLang() {
        return SkyHigthLang.values();
    }
}
