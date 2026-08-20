package net.novaproject.ultimate.legend.roles.abilities;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.PassiveAbility;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MagePotionPassive extends PassiveAbility {

    @Var(name = "Mage Potion Interval (s)", desc = "Interval between regenerations.", type = VariableType.TIME)
    private int regenInterval = 600;

    public MagePotionPassive() { setCooldown(regenInterval); }

    @Override public String getName() { return "Potions Magiques"; }
    @Override public Material getMaterial() { return null; }

    @Override
    public boolean onEnable(Player player) {

        setCooldown(regenInterval);
        dropPotions(player);
        player.sendMessage("§6§lMage §8│ §7Nouvelles potions reçues.");
        return true;
    }

    private void dropPotions(Player p) {
        p.getWorld().dropItemNaturally(p.getLocation(),
                new ItemCreator(Material.POTION)
                        .addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 600, 0), true)
                        .setName("§cPotion de Force").getItemstack()).setPickupDelay(0);
        p.getWorld().dropItemNaturally(p.getLocation(),
                new ItemCreator(Material.POTION)
                        .addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 600, 0), true)
                        .setName("§7Potion de Résistance").getItemstack()).setPickupDelay(0);
        p.getWorld().dropItemNaturally(p.getLocation(),
                new ItemCreator(Material.POTION)
                        .addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 0), true)
                        .setName("§bPotion de Vitesse").getItemstack()).setPickupDelay(0);
    }
}

