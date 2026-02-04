package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.entity.PlayerDeathEvent;

public class GoldenDrop extends Scenario {

    @ScenarioVariable(
            name = "Quantité de Golden Apple",
            description = "Nombre de Golden Apple droppées à la mort d'un joueur",
            type = VariableType.INTEGER
    )
    private int goldenAppleAmount = 1;

    @Override
    public String getName() {
        return "Golden Drop";
    }

    @Override
    public String getDescription() {
        return "Les joueurs morts drop automatiquement une pomme dorée.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.SPIDER_EYE);
    }

    @Override
    public void toggleActive() {
        super.toggleActive();
        GoldenHead goldenHeadScenario = ScenarioManager.get().getScenario(GoldenHead.class);
        if (isActive()) {
            if (goldenHeadScenario != null && !goldenHeadScenario.isActive()) {
                goldenHeadScenario.toggleActive();
            }
        } else {
            if (goldenHeadScenario != null && goldenHeadScenario.isActive()) {
                goldenHeadScenario.toggleActive();
            }
        }
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        ItemCreator golden = new ItemCreator(Material.GOLDEN_APPLE)
                .setName(ChatColor.GOLD + "Golden Head")
                .setAmount(goldenAppleAmount);
        uhcPlayer.getPlayer().getWorld().dropItemNaturally(uhcPlayer.getPlayer().getLocation(), golden.getItemstack());
    }
}
