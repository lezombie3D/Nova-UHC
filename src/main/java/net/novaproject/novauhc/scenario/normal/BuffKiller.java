package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BuffKiller extends Scenario {

    private final List<PotionEffectType> effects = new ArrayList<>();
    private final Random random = new Random();

    public BuffKiller() {
        effects.add(PotionEffectType.SPEED);
        effects.add(PotionEffectType.INCREASE_DAMAGE);
        effects.add(PotionEffectType.DAMAGE_RESISTANCE);
        effects.add(PotionEffectType.FIRE_RESISTANCE);
    }

    @Override
    public String getName() {
        return "Buff Killer";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.WOOD_SWORD);
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        PotionEffectType randomEffect = effects.get(random.nextInt(effects.size()));

        int duration = 20 * (random.nextInt(10) + 5);
        killer.getPlayer().addPotionEffect(new PotionEffect(randomEffect, duration, 0));
    }
}
