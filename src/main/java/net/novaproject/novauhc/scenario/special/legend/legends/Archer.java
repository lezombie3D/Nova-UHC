package net.novaproject.novauhc.scenario.special.legend.legends;

import net.novaproject.novauhc.scenario.special.legend.core.LegendClass;
import net.novaproject.novauhc.scenario.special.legend.utils.LegendItems;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Légende de l'Archer
 * <p>
 * Capacités :
 * - Choix entre 2 arcs : Infinity + Power III ou Power IV
 * - Pas de pouvoir activable
 * - Pas d'effets passifs
 *
 * @author NovaProject
 * @version 3.0
 */
public class Archer extends LegendClass {

    public Archer() {
        super(2, "Archer", "Maître de l'arc avec des flèches puissantes", Material.BOW);
    }

    @Override
    public void onChoose(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§6[Archer] §aVous êtes maintenant un Archer !");
        player.sendMessage("§7Vous avez reçu un arc puissant");
        player.sendMessage("§7Le choix de l'arc a été fait lors de la sélection");
    }

    @Override
    public boolean onPower(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§c[Archer] Vous n'avez pas de pouvoir activable !");
        return false;
    }

    @Override
    public void onTick(Player player, UHCPlayer uhcPlayer) {
    }

    @Override
    public void onDeath(Player player, UHCPlayer uhcPlayer, UHCPlayer killer) {
    }

    @Override
    public boolean hasPower() {
        return false;
    }

    public void giveBow(Player player, int choice) {
        if (choice == 1) {
            player.getInventory().addItem(LegendItems.createArcherBowInfinity());
            player.sendMessage("§6[Archer] §aVous avez reçu l'Arc de Lumière (Infinity + Power III) !");
        } else {
            player.getInventory().addItem(LegendItems.createArcherBowPower());
            player.sendMessage("§6[Archer] §aVous avez reçu l'Arc de Lumière (Power IV) !");
        }
    }
}
