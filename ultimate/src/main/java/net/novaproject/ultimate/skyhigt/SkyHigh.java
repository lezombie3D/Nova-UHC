package net.novaproject.ultimate.skyhigt;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.ultimate.skyhigt.SkyHighLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.UHCManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class SkyHigh extends Scenario {

    @Var(name = "§eFirst layer", desc = "§7Altitude under which player takes damage (first layer)", type = VariableType.INTEGER)
    private int firstLevel = 120;

    @Var(name = "§eSecond layer", desc = "§7Altitude under which player takes damage (second layer)", type = VariableType.INTEGER)
    private int secondLevel = 80;

    @Var(name = "§eThird layer", desc = "§7Altitude under which player takes damage (third layer)", type = VariableType.INTEGER)
    private int thirdLevel = 40;

    @Var(name = "§eFirst layer damage", desc = "§7Damage dealt below first layer", type = VariableType.INTEGER)
    private int firstDamage = 5;

    @Var(name = "§eSecond layer damage", desc = "§7Damage dealt below second layer", type = VariableType.INTEGER)
    private int secondDamage = 3;

    @Var(name = "§eThird layer damage", desc = "§7Damage dealt below third layer", type = VariableType.INTEGER)
    private int thirdDamage = 1;

    @Override
    public String getName() {
        return "SkyHigh";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(SkyHighLang.WARNING_SKY_HIGH, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.PAPER);
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        if(UHCManager.get().getTeam_size() > 1){
            UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
            return;
        }
        uhcPlayer.getPlayer().teleport(location);
    }

    @Override
    public void onStart(Player player) {
        player.getInventory().addItem(new ItemStack(Material.DIRT));
    }

    @Override
    public void onSec(Player player) {
        int timer = UHCManager.get().getTimer();

        if (timer == UHCManager.get().getBorderTimer() - 120) {
            LangManager.get().send(SkyHighLang.WARNING_SKY_HIGH, player);
        }
        int y = player.getLocation().getBlockY();
        if (timer >= UHCManager.get().getBorderTimer()) {
            if (y < thirdLevel) {
                LangManager.get().send(SkyHighLang.DAMAGE_THIRD_LAYER, player);
                player.damage(thirdDamage);
            } else if (y < secondLevel) {
                LangManager.get().send(SkyHighLang.DAMAGE_SECOND_LAYER, player);
                player.damage(secondDamage);
            } else if (y < firstLevel) {
                LangManager.get().send(SkyHighLang.DAMAGE_FIRST_LAYER, player);
                player.damage(firstDamage);
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

