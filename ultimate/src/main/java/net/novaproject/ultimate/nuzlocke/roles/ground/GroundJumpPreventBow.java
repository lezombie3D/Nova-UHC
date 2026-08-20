package net.novaproject.ultimate.nuzlocke.roles.ground;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.ProjectileImpacting;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.ultimate.nuzlocke.Nuzlocke;
import net.novaproject.ultimate.nuzlocke.roles.flying.Flying;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GroundJumpPreventBow extends Ability implements ProjectileImpacting {

    @Var(name = "Nuzlocke Ground Jump Radius", type = VariableType.INTEGER)
    private int radius = 3;

    @Var(name = "Nuzlocke Ground Jump Dur", type = VariableType.INTEGER)
    private int durationTicks = 40;

    @Override public String getName() { return "Sismique"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player shooter)) return;
        if (getOwner() == null || !shooter.equals(getOwner().getPlayer())) return;

        for (Player target : arrow.getWorld().getPlayers()) {
            if (target.equals(shooter)) continue;
            if (target.getLocation().distance(arrow.getLocation()) > radius) continue;
            UHCPlayer up = UHCPlayerManager.get().getPlayer(target);
            if (up == null) continue;
            Nuzlocke nz = Nuzlocke.get();
            if (nz != null) {
                Role r = nz.getRoleByUHCPlayer(up);
                if (r instanceof Flying) continue;
            }
            target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, durationTicks, 200, true, false), true);
        }
    }
}

