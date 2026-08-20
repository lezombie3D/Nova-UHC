package net.novaproject.ultimate.legend.roles.abilities;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.UseAbility;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PaladinBlessingActive extends UseAbility {

    @Var(name = "Blessing Duration (s)", desc = "Blessing duration.", type = VariableType.TIME)
    private int duration = 30;

    public PaladinBlessingActive() { setCooldown(480); setMaxUse(-1); }

    @Override public String getName() { return "Bénédiction"; }

    @Override
    public boolean onEnable(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * duration, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * duration, 0));
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.5f);
        UHCPlayer owner = getUHCPlayer(player);
        if (owner != null && owner.getTeam().isPresent()) {
            PotionEffect allyRegen = new PotionEffect(PotionEffectType.REGENERATION, 200, 0);
            for (UHCPlayer t : owner.getTeam().get().getPlayers()) {
                if (t.equals(owner)) continue;
                Player tp = t.getPlayer();
                if (tp != null && tp.isOnline() && tp.getLocation().distance(player.getLocation()) <= 6.0)
                    tp.addPotionEffect(allyRegen);
            }
        }
        return true;
    }
}

