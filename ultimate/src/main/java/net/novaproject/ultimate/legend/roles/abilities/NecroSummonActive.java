package net.novaproject.ultimate.legend.roles.abilities;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.ability.template.UseAbility;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.awt.Color;

public class NecroSummonActive extends UseAbility {

    private static final Color PROIE_GLOW = new Color(60, 140, 60);

    @Var(name = "Skeleton Count", desc = "Summoned skeletons count.", type = VariableType.INTEGER)
    private int skeletonCount = 2;

    @Var(name = "Zombie Count", desc = "Baby zombies count.", type = VariableType.INTEGER)
    private int zombieCount = 3;

    @Var(name = "Enemy Search Radius", desc = "Search radius.", type = VariableType.DOUBLE)
    private double searchRadius = 30.0;

    @Var(name = "Durée de marquage de la proie", desc = "Durée pendant laquelle le nécromancien voit la cible de ses morts-vivants.", type = VariableType.TIME)
    private int preyMarkDuration = 30;

    public NecroSummonActive() { setCooldown(600); setMaxUse(-1); }

    @Override public String getName() { return "Nécromancie"; }

    @Override
    public boolean onEnable(Player player) {
        UHCPlayer owner = getUHCPlayer(player);
        Player target = findNearestEnemy(player, owner);
        if (target == null) return false;
        for (int i = 0; i < skeletonCount; i++) {
            Skeleton s = player.getWorld().spawn(player.getLocation(), Skeleton.class);
            s.setTarget(target); s.setMaxHealth(40); s.setHealth(40);
            s.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2400, 0));
        }
        for (int i = 0; i < zombieCount; i++) {
            Zombie z = player.getWorld().spawn(target.getLocation().add(0, 2, 0), Zombie.class);
            z.setTarget(target); z.setMaxHealth(40); z.setHealth(40); z.setBaby(true);
            z.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2400, 0));
        }
        DisplayService.glow(player, target, PROIE_GLOW);
        Bukkit.getScheduler().runTaskLater(Main.get(),
                () -> DisplayService.resetGlow(player, target), 20L * preyMarkDuration);
        return true;
    }

    private Player findNearestEnemy(Player player, UHCPlayer owner) {
        Player nearest = null; double min = searchRadius;
        for (Entity e : player.getNearbyEntities(searchRadius, searchRadius, searchRadius)) {
            if (!(e instanceof Player t) || t == player) continue;
            UHCPlayer tu = UHCPlayerManager.get().getPlayer(t);
            if (tu == null) continue;
            if (owner.getTeam().isPresent() && tu.getTeam().isPresent()
                    && owner.getTeam().get().equals(tu.getTeam().get())) continue;
            double d = player.getLocation().distance(t.getLocation());
            if (d < min) { min = d; nearest = t; }
        }
        return nearest;
    }
}

