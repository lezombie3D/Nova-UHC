package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FastMiner extends Scenario {
    @Override
    public String getName() {
        return "FastMiner";
    }

    @Override
    public String getDescription() {
        return "Augmente la vitesse de minage de tous les joueurs.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.IRON_PICKAXE);
    }

    @Override
    public void onSec(Player p) {
        int timer = UHCManager.get().getTimer();
        if (timer < UHCManager.get().getTimerpvp()) {
            PotionEffect[] getEff = new PotionEffect[]{
                    new PotionEffect(PotionEffectType.SATURATION, 80, 0, false, false),
                    new PotionEffect(PotionEffectType.FAST_DIGGING, 80, 1, false, false),
                    new PotionEffect(PotionEffectType.SPEED, 80, 1, false, false),
            };

            for (PotionEffect activeEffect : getEff) {
                p.removePotionEffect(activeEffect.getType());
            }
            for (PotionEffect effect : getEff) {
                effect.apply(p);
            }
        }
    }

}
