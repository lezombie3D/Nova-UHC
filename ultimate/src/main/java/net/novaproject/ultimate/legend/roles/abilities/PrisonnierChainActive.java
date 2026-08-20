package net.novaproject.ultimate.legend.roles.abilities;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.ability.template.UseAbility;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.awt.Color;

public class PrisonnierChainActive extends UseAbility {

    private static final Color CHAINE_GLOW = new Color(120, 120, 130);

    @Var(name = "Chain Radius", desc = "Chain search radius.", type = VariableType.DOUBLE)
    private double chainRadius = 8.0;

    @Var(name = "Chain Duration (s)", desc = "Chain duration.", type = VariableType.TIME)
    private int chainDuration = 8;

    public PrisonnierChainActive() { setCooldown(300); setMaxUse(-1); }

    @Override public String getName() { return "Chaînes"; }

    @Override
    public boolean onEnable(Player player) {
        UHCPlayer owner = getUHCPlayer(player);
        Player target = null; double minDist = chainRadius;
        for (Entity e : player.getNearbyEntities(chainRadius, chainRadius, chainRadius)) {
            if (!(e instanceof Player t) || t == player) continue;
            UHCPlayer tu = UHCPlayerManager.get().getPlayer(t);
            if (tu == null) continue;
            if (owner.getTeam().isPresent() && tu.getTeam().isPresent()
                    && owner.getTeam().get().equals(tu.getTeam().get())) continue;
            double d = player.getLocation().distance(t.getLocation());
            if (d < minDist) { minDist = d; target = t; }
        }
        if (target == null) return false;
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * chainDuration, 1));
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20 * chainDuration, 0));

        Player chained = target;
        DisplayService.glow(player, chained, CHAINE_GLOW);
        Bukkit.getScheduler().runTaskLater(Main.get(),
                () -> DisplayService.resetGlow(player, chained), 20L * chainDuration);
        return true;
    }
}

