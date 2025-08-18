package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Random;

public class DeathEmerauld extends Scenario {
    @Override
    public String getName() {
        return "DeathEmerauld";
    }

    @Override
    public String getDescription() {
        return "Miner une émeraude inflige des dégâts à un joueur aléatoire.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.EMERALD);
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        Random random = new Random();
        if (block.getType() == Material.EMERALD_ORE) {
            UHCPlayer choosen = UHCPlayerManager.get().getPlayingOnlineUHCPlayers().get(random.nextInt(UHCPlayerManager.get().getPlayingOnlineUHCPlayers().size()));
            choosen.getPlayer().damage(4);
        }
    }
}
