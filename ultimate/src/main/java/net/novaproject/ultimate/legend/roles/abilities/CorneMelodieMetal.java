package net.novaproject.ultimate.legend.roles.abilities;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.UseAbility;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CorneMelodieMetal extends UseAbility {
    @Var(name = "Metal Melody Duration (s)", desc = "Resistance duration.", type = VariableType.TIME)
    private int duration = 5;
    public CorneMelodieMetal() { setCooldown(60); setMaxUse(-1); }
    @Override public String getName() { return "Melodie : Metal"; }
    @Override
    public boolean onEnable(Player player) {
        UHCPlayer owner = getUHCPlayer(player);
        if (owner == null || !owner.getTeam().isPresent()) return false;
        PotionEffect e = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * duration, 1);
        for (UHCPlayer t : owner.getTeam().get().getPlayers()) {
            Player tp = t.getPlayer();
            if (tp != null && tp.isOnline() && tp.getLocation().distance(player.getLocation()) <= 31) tp.addPotionEffect(e);
        }
        return true;
    }
}

