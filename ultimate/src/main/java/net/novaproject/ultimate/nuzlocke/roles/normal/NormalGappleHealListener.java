package net.novaproject.ultimate.nuzlocke.roles.normal;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Consuming;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NormalGappleHealListener extends Ability implements Consuming {

    @Var(name = "Nuzlocke Normal Gapple Heal", type = VariableType.DOUBLE)
    private double extraHealHearts = 0.5;

    @Var(name = "Nuzlocke Normal Gapple Regen Ticks", type = VariableType.INTEGER)
    private int regenTicks = 20;

    @Override public String getName() { return "Gapple Buffé"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onConsume(PlayerItemConsumeEvent event) {
        if (getOwner() == null || !event.getPlayer().equals(getOwner().getPlayer())) return;
        if (event.getItem() == null) return;
        if (event.getItem().getType() != Material.GOLDEN_APPLE) return;
        Player p = event.getPlayer();
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regenTicks, 1, true, false), true);
        org.bukkit.Bukkit.getScheduler().runTaskLater(
                net.novaproject.novauhc.Main.get(),
                () -> p.setHealth(Math.min(p.getHealth() + 2 * extraHealHearts, p.getMaxHealth())),
                1L
        );
    }
}

