package net.novaproject.novauhc.scenario.special.legend.legends;

import net.novaproject.novauhc.scenario.special.legend.core.LegendClass;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.UHCUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Légende du Dragon
 */
public class Dragon extends LegendClass {

    public Dragon() {
        super(13, "Dragon", "Résistance au feu permanente", Material.FIREBALL);
    }

    @Override
    public void onChoose(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§6[Dragon] §aVous êtes maintenant un Dragon !");
        player.sendMessage("§7Résistance au feu permanente");
    }

    @Override
    public boolean onPower(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§c[Dragon] Vous n'avez pas de pouvoir activable !");
        return false;
    }

    @Override
    public void onTick(Player player, UHCPlayer uhcPlayer) {
        // Résistance au feu permanente
        UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, 2, false, false)
        }, player);
    }

    @Override
    public void onHit(Player attacker, Player victim, UHCPlayer uhcAttacker, UHCPlayer uhcVictim) {
        // 20% de chance d'enflammer la victime
        if (Math.random() < 0.20) {
            victim.setFireTicks(20 * 3); // 3 secondes de feu
            attacker.sendMessage("§6[Dragon] §eVotre attaque enflamme votre ennemi !");
            victim.sendMessage("§c[Dragon] §eVous êtes enflammé par le Dragon !");
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
