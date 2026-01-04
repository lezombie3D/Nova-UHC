package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CatEye extends Scenario {
    @Override
    public String getName() {
        return "CatEye";
    }

    @Override
    public String getDescription() {
        return "Donne une vision nocturne permanente à tous les joueurs.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.EYE_OF_ENDER);
    }

    @Override
    public void onSec(Player player) {

        boolean hasNightVision = false;

        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            if (potionEffect.getType().equals(PotionEffectType.NIGHT_VISION)) {
                hasNightVision = true;
                break;
            }
        }

        if (hasNightVision) return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));

    }

}
