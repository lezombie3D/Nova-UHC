package net.novaproject.novauhc.scenario.special.legend.legends;

import net.novaproject.novauhc.scenario.special.legend.core.LegendClass;
import net.novaproject.novauhc.scenario.special.legend.utils.LegendEffects;
import net.novaproject.novauhc.scenario.special.legend.utils.LegendItems;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Légende de l'Assassin
 * <p>
 * Capacités :
 * - Reçoit une Lame Secrète
 * - Passif : Force I quand il a moins de 2 épées et possède la Lame Secrète
 * - Pas de pouvoir activable
 *
 * @author NovaProject
 * @version 3.0
 */
public class Assassin extends LegendClass {

    public Assassin() {
        super(3, "Assassin", "Maître des lames avec une épée secrète", Material.IRON_SWORD);
    }

    @Override
    public void onChoose(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§6[Assassin] §aVous êtes maintenant un Assassin !");
        player.sendMessage("§7Vous avez reçu la Lame Secrète");
        player.sendMessage("§7Force I quand vous avez moins de 2 épées");

        // Donner la Lame Secrète
        player.getInventory().addItem(LegendItems.createSecretBlade());
    }

    @Override
    public boolean onPower(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§c[Assassin] Vous n'avez pas de pouvoir activable !");
        return false;
    }

    @Override
    public void onTick(Player player, UHCPlayer uhcPlayer) {
        int swordCount = LegendItems.countSwords(player);
        boolean hasSecretBlade = LegendItems.hasSecretBlade(player);

        if (hasSecretBlade && swordCount < 2) {
            PotionEffect strength = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, 0, false, false);
            LegendEffects.applyEffect(player, strength);
        }
    }

    @Override
    public void onDeath(Player player, UHCPlayer uhcPlayer, UHCPlayer killer) {
    }

    @Override
    public boolean hasPower() {
        return false;
    }
}
