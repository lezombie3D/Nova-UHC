package net.novaproject.ultimate.legend.roles.abilities;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Consuming;
import net.novaproject.novauhc.ability.AbilityHooks.Ticking;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class OgrePassive extends Ability implements Ticking, Consuming {

    @Var(name = "Min Gapples Threshold", desc = "Below = debuff.", type = VariableType.INTEGER)
    private int minApples = 5;

    @Var(name = "Gapple Effect Duration (s)", desc = "Random effect duration.", type = VariableType.TIME)
    private int effectDuration = 5;

    private static final List<PotionEffectType> POOL = Arrays.asList(
            PotionEffectType.SPEED, PotionEffectType.INCREASE_DAMAGE,
            PotionEffectType.DAMAGE_RESISTANCE, PotionEffectType.FIRE_RESISTANCE,
            PotionEffectType.ABSORPTION, PotionEffectType.SATURATION);

    public OgrePassive() { setCooldown(0); }

    @Override public String getName() { return "Appétit d'Ogre"; }
    @Override public Material getMaterial() { return null; }

    @Override
    public void onSec(Player player) {
        tryUse(player);
    }

    @Override
    public boolean onEnable(Player player) {
        if (countGA(player) <= minApples) {
            player.removePotionEffect(PotionEffectType.SLOW);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 0, false, false));
            player.removePotionEffect(PotionEffectType.WEAKNESS);
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 0, false, false));
            return true;
        }

        player.removePotionEffect(PotionEffectType.SLOW);
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        return false;
    }

    @Override
    public void onConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() != Material.GOLDEN_APPLE) return;
        Player p = event.getPlayer();
        if (getOwner() == null || !p.equals(getOwner().getPlayer())) return;
        PotionEffectType type = POOL.get(ThreadLocalRandom.current().nextInt(POOL.size()));
        p.addPotionEffect(new PotionEffect(type, 20 * effectDuration, 0));
    }

    private int countGA(Player p) {
        int c = 0;
        for (ItemStack i : p.getInventory().getContents())
            if (i != null && i.getType() == Material.GOLDEN_APPLE) c += i.getAmount();
        return c;
    }
}

