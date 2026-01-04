package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GapRoulette extends Scenario {

    private final List<PotionEffectType> effects = new ArrayList<>();
    private final Random random = new Random();

    public GapRoulette() {
        effects.add(PotionEffectType.SPEED);
        effects.add(PotionEffectType.INCREASE_DAMAGE);
        effects.add(PotionEffectType.DAMAGE_RESISTANCE);
        effects.add(PotionEffectType.JUMP);
        effects.add(PotionEffectType.FIRE_RESISTANCE);
        effects.add(PotionEffectType.WEAKNESS);
        effects.add(PotionEffectType.POISON);
        effects.add(PotionEffectType.SLOW);
        effects.add(PotionEffectType.HUNGER);
        effects.add(PotionEffectType.WITHER);
    }

    @Override
    public String getName() {
        return "GapRoulette";
    }

    @Override
    public String getDescription() {
        return "Les pommes dorées donnent des effets aléatoires au lieu de leurs effets normaux.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.GOLDEN_APPLE);
    }

    @Override
    public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {

        if (item.getType() == Material.GOLDEN_APPLE) {

            PotionEffectType randomEffect = effects.get(random.nextInt(effects.size()));

            int duration = 20 * (random.nextInt(10) + 5);
            int amplifier = random.nextInt(2);
            player.addPotionEffect(new PotionEffect(randomEffect, duration, amplifier));

            player.sendMessage(ChatColor.GOLD + "Vous avez reçu l'effet : " + ChatColor.AQUA + randomEffect.getName());
        }
    }
}
