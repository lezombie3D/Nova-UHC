package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class XpSansue extends Scenario {
    private boolean active = false;

    @Override
    public String getName() {
        return "XpSansue";
    }

    @Override
    public String getDescription() {
        return "Modifie le système d'expérience pour le rendre plus équilibré.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.EXP_BOTTLE);
    }

    @Override
    public void onStart(Player player) {
        if (!active) {
            active = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (UHCPlayer p : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                        if (p.getPlayer().getLevel() > 0) {
                            p.getPlayer().setLevel(p.getPlayer().getLevel() - 1);
                        } else {
                            p.getPlayer().damage(2);
                        }
                    }
                }
            }.runTaskTimer(Main.get(), 20 * 300 * 2, 20 * 300 * 2);
        }
    }
}
