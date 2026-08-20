package net.novaproject.ultimate.legend.roles.abilities;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.UseAbility;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SuccubeAbsorptionActive extends UseAbility {

    @Var(name = "Absorption Radius", desc = "Absorption buff radius.", type = VariableType.DOUBLE)
    private double radius = 11.0;

    @Var(name = "Absorption Level", desc = "Absorption level.", type = VariableType.INTEGER)
    private int absorptionLevel = 3;

    @Var(name = "Absorption Duration (s)", desc = "Absorption duration.", type = VariableType.TIME)
    private int duration = 60;

    public SuccubeAbsorptionActive() { setCooldown(360); setMaxUse(-1); }

    @Override public String getName() { return "Charme Succube"; }

    @Override
    public boolean onEnable(Player player) {
        if (absorptionLevel <= 0) return false;
        UHCPlayer owner = getUHCPlayer(player);
        if (owner == null || !owner.getTeam().isPresent()) return false;
        PotionEffect effect = new PotionEffect(PotionEffectType.ABSORPTION, 20 * duration, absorptionLevel - 1);
        for (UHCPlayer t : owner.getTeam().get().getPlayers()) {
            Player tp = t.getPlayer();
            if (tp != null && tp.isOnline() && tp.getLocation().distance(player.getLocation()) <= radius)
                tp.addPotionEffect(effect);
        }
        return true;
    }
}

