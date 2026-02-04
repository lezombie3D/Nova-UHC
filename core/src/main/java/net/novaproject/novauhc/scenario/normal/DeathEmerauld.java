package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.utils.VariableType;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;
import java.util.Random;

public class DeathEmerauld extends Scenario {

    private final Random random = new Random();

    @ScenarioVariable(
            name = "damage_amount",
            description = "Quantité de dégâts infligés au joueur aléatoire",
            type = VariableType.DOUBLE
    )
    private double damageAmount = 4.0;

    @ScenarioVariable(
            name = "target_block",
            description = "Type de bloc qui déclenche l'effet",
            type = VariableType.STRING
    )
    private String targetBlock = "EMERALD_ORE";

    @Override
    public String getName() {
        return "DeathEmerauld";
    }

    @Override
    public String getDescription() {
        return "Miner une " + targetBlock + " inflige " + damageAmount + " dégâts à un joueur aléatoire.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.EMERALD);
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (block.getType() != Material.getMaterial(targetBlock)) return;

        List<UHCPlayer> players = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();
        if (players.isEmpty()) return;

        UHCPlayer chosen = players.get(random.nextInt(players.size()));
        chosen.getPlayer().damage(damageAmount);
    }
}
