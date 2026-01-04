package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class HeathCharity extends Scenario {
    private boolean active = false;

    @Override
    public String getName() {
        return "Heath Charity";
    }

    @Override
    public String getDescription() {
        return "Partage automatiquement la vie entre les membres d'une équipe.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.RED_ROSE);
    }

    @Override
    public void onStart(Player player) {
        if (!active) {
            active = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    Player lowestHealthPlayer = null;
                    double lowestHealth = Double.MAX_VALUE;

                    for (UHCPlayer player : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                        double health = player.getPlayer().getHealth();
                        if (health < lowestHealth) {
                            lowestHealth = health;
                            lowestHealthPlayer = player.getPlayer();
                        }
                    }
                    if (lowestHealthPlayer != null) {
                        lowestHealthPlayer.setHealth(lowestHealthPlayer.getMaxHealth());
                    }

                }
            }.runTaskTimer(Main.get(), 0, 20 * 300 * 2);
        }
    }
}
