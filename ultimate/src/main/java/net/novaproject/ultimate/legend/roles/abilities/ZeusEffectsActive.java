package net.novaproject.ultimate.legend.roles.abilities;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.UseAbility;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ZeusEffectsActive extends UseAbility {

    @Var(name = "Zeus Effect Duration (s)", desc = "Random effects duration.", type = VariableType.TIME)
    private int effectDuration = 120;

    public ZeusEffectsActive() { setCooldown(600); setMaxUse(-1); }

    @Override public String getName() { return "Pouvoir de Zeus"; }

    @Override
    public boolean onEnable(Player player) {
        List<PotionEffect> pool = Arrays.asList(
                new PotionEffect(PotionEffectType.SPEED, 20 * effectDuration, 0),
                new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * effectDuration, 0),
                new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * effectDuration, 0));
        Collections.shuffle(pool);
        player.addPotionEffect(pool.get(0)); player.addPotionEffect(pool.get(1));
        return true;
    }
}

