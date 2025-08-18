package net.novaproject.novauhc.scenario.special.legend.legends;

import net.novaproject.novauhc.scenario.special.legend.core.LegendClass;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

/**
 * Légende du Soldat
 */
public class Soldat extends LegendClass {

    public Soldat() {
        super(9, "Soldat", "Reçoit un équipement en diamant complet", Material.ANVIL);
    }

    @Override
    public void onChoose(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§6[Soldat] §aVous êtes maintenant un Soldat !");
        player.sendMessage("§7Vous avez reçu un équipement en diamant !");

        // Donner l'équipement en diamant
        player.getInventory().addItem(new ItemCreator(Material.DIAMOND_SWORD)
                .addEnchantment(Enchantment.DAMAGE_ALL, 1).setUnbreakable(true).getItemstack());
        player.getInventory().addItem(new ItemCreator(Material.DIAMOND_PICKAXE)
                .addEnchantment(Enchantment.DIG_SPEED, 1).setUnbreakable(true).getItemstack());
        player.getInventory().addItem(new ItemCreator(Material.DIAMOND_AXE)
                .addEnchantment(Enchantment.DIG_SPEED, 1).setUnbreakable(true).getItemstack());
        player.getInventory().addItem(new ItemCreator(Material.DIAMOND_SPADE)
                .addEnchantment(Enchantment.DIG_SPEED, 1).setUnbreakable(true).getItemstack());
    }

    @Override
    public boolean onPower(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§c[Soldat] Vous n'avez pas de pouvoir activable !");
        return false;
    }

    @Override
    public void onTick(Player player, UHCPlayer uhcPlayer) {
        // Pas d'effets passifs
    }

    @Override
    public void onDeath(Player player, UHCPlayer uhcPlayer, UHCPlayer killer) {
        // Pas d'effet spécial
    }

    @Override
    public boolean hasPower() {
        return false;
    }
}
