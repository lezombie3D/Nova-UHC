package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.utils.VariableType;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class FinalHeal extends Scenario {

    @ScenarioVariable(
            name = "Final Heal 1 Time",
            description = "Temps (en secondes) du premier heal",
            type = VariableType.TIME
    )
    private int healTime1 = 600;

    @ScenarioVariable(
            name = "Final Heal 2 Time",
            description = "Temps (en secondes) du deuxième heal",
            type = VariableType.TIME
    )
    private int healTime2 = 1200;

    @ScenarioVariable(
            name = "Heal Food",
            description = "Si vrai, restaure aussi la nourriture",
            type = VariableType.BOOLEAN
    )
    private boolean healFood = true;

    @Override
    public String getName() {
        return "FinalHeal";
    }

    @Override
    public String getDescription() {
        return "Heal tous les joueurs à des moments précis avant le PVP.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.POTION);
    }

    @Override
    public void onSec(Player p) {
        int timer = UHCManager.get().getTimer();

        if (!isActive()) return;

        if (timer == healTime1 || timer == healTime2) {
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player player = uhcPlayer.getPlayer();
                player.setHealth(player.getMaxHealth());
                if (healFood) {
                    player.setFoodLevel(20);
                    player.setSaturation(20f);
                }
            }
            Bukkit.broadcastMessage(CommonString.FINAL_HEAL_BROADCAST.getMessage());
        }
    }
}
