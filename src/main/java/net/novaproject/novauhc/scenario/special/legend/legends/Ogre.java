package net.novaproject.novauhc.scenario.special.legend.legends;

import net.novaproject.novauhc.scenario.special.legend.core.LegendClass;
import net.novaproject.novauhc.scenario.special.legend.utils.LegendEffects;
import net.novaproject.novauhc.scenario.special.legend.utils.LegendItems;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Légende de l'Ogre
 */
public class Ogre extends LegendClass {

    private final List<PotionEffectType> effects = Arrays.asList(
            PotionEffectType.SPEED, PotionEffectType.INCREASE_DAMAGE, PotionEffectType.DAMAGE_RESISTANCE,
            PotionEffectType.FIRE_RESISTANCE, PotionEffectType.WATER_BREATHING, PotionEffectType.NIGHT_VISION,
            PotionEffectType.HEALTH_BOOST, PotionEffectType.ABSORPTION, PotionEffectType.SATURATION
    );
    private final Random random = new Random();

    public Ogre() {
        super(12, "Ogre", "Effets aléatoires en mangeant des pommes dorées", Material.COOKED_BEEF);
    }

    @Override
    public void onChoose(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§6[Ogre] §aVous êtes maintenant un Ogre !");
        player.sendMessage("§7Mangez des pommes dorées pour des effets aléatoires");
        player.sendMessage("§7Malus si vous avez 5 pommes dorées ou moins");
    }

    @Override
    public boolean onPower(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§c[Ogre] Vous n'avez pas de pouvoir activable !");
        return false;
    }

    @Override
    public void onTick(Player player, UHCPlayer uhcPlayer) {
        // Malus si peu de pommes dorées
        int goldenAppleCount = LegendItems.countItems(player, Material.GOLDEN_APPLE);
        if (goldenAppleCount <= 5) {
            PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 80, 0, false, false);
            PotionEffect weakness = new PotionEffect(PotionEffectType.WEAKNESS, 80, 0, false, false);
            LegendEffects.applyEffects(player, slow, weakness);
        }
    }

    @Override
    public void onConsume(Player player, UHCPlayer uhcPlayer, ItemStack item) {
        if (item.getType() == Material.GOLDEN_APPLE) {
            // Donner un effet aléatoire
            PotionEffectType randomEffect = effects.get(random.nextInt(effects.size()));
            PotionEffect effect = new PotionEffect(randomEffect, 20 * 5, 0);
            LegendEffects.applyEffect(player, effect);

            player.sendMessage("§6[Ogre] §aVous avez reçu l'effet : §b" + randomEffect.getName());
        }
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
