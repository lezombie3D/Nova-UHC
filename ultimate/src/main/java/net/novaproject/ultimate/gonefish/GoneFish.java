package net.novaproject.ultimate.gonefish;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.LangManager;

import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.scenario.Scenario;

import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class GoneFish extends Scenario {

    @Var(name = "Luck of the Sea Level", desc = "Luck of the Sea enchantment level on the rod.", type = VariableType.INTEGER)
    private int luckLevel = 250;

    @Var(name = "Lure Level", desc = "Lure enchantment level on the rod.", type = VariableType.INTEGER)
    private int lureLevel = 250;

    @Var(name = "Anvil Amount", desc = "Number of anvils given at the start of the game.", type = VariableType.INTEGER)
    private int anvilAmount = 20;

    @Override
    public String getName() {
        return "GoneFish";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.GONE_FISH, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.FISHING_ROD);
    }

    @Override
    public void onStart(Player player) {
        player.getInventory().addItem(
                new ItemCreator(Material.ANVIL)
                        .setAmount(anvilAmount)
                        .getItemstack()
        );
        player.getInventory().addItem(
                new ItemCreator(Material.FISHING_ROD)
                        .setUnbreakable(true)
                        .addEnchantment(Enchantment.LUCK, luckLevel)
                        .addEnchantment(Enchantment.LURE, lureLevel)
                        .getItemstack()
        );
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        if(UHCManager.get().getTeam_size() > 1){
            UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
            return;
        }
        uhcPlayer.getPlayer().teleport(location);
    }
}

