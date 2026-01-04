package net.novaproject.ultimate.legend.legends;

import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.ultimate.legend.core.LegendClass;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Légende du DragonRole
 */
public class Dragon extends LegendClass {

    public Dragon() {
        super(13, "DragonRole", "Résistance au feu permanente", Material.FIREBALL);
    }

    @Override
    public void onChoose(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§6[DragonRole] §aVous êtes maintenant un DragonRole !");
        player.sendMessage("§7Résistance au feu permanente");
    }

    @Override
    public boolean onPower(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§c[DragonRole] Vous n'avez pas de pouvoir activable !");
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
            attacker.sendMessage("§6[DragonRole] §eVotre attaque enflamme votre ennemi !");
            victim.sendMessage("§c[DragonRole] §eVous êtes enflammé par le DragonRole !");
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
