package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class FinalHeal extends Scenario {

    @Override
    public String getName() {
        return "FinalHeal";
    }

    @Override
    public String getDescription() {
        return "Heal tout les joueur un peu avant le PVP";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.POTION);
    }

    @Override
    public void onSec(Player p) {
        int timer = UHCManager.get().getTimer();
        int pvp = UHCManager.get().getTimerpvp();

        if (timer == pvp - 5) {
            for (UHCPlayer player : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                player.getPlayer().setHealth(player.getPlayer().getMaxHealth());
            }
            Bukkit.broadcastMessage("§aFinal Heal effectuer !");
        }
    }
}
