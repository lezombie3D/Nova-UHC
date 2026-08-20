package net.novaproject.ultimate.nuzlocke.roles.electric;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.AbilityHooks.Shooting;
import net.novaproject.novauhc.ability.template.UseAbility;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ElectricMotorDriveUse extends UseAbility implements Shooting {

    @Var(name = "Nuzlocke Electric Motor Mult", type = VariableType.DOUBLE)
    private double speedMultiplier = 1.75;

    public static final Map<UUID, Boolean> MOTOR_ON = new HashMap<>();

    public ElectricMotorDriveUse() {
        setCooldown(1);
        setMaxUse(-1);
    }

    @Override public String getName() { return "Motor Drive"; }
    @Override public Material getMaterial() { return Material.REDSTONE_TORCH_ON; }

    @Override
    public boolean onEnable(Player player) {
        boolean now = !MOTOR_ON.getOrDefault(player.getUniqueId(), false);
        MOTOR_ON.put(player.getUniqueId(), now);
        player.sendMessage("§e§lMotor Drive §8│ §7Flèches accélérées : §" + (now ? "a§lON" : "c§lOFF"));
        return true;
    }

    @Override
    public void onBow(Entity shooter, Player target, EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        if (getOwner() == null || !p.equals(getOwner().getPlayer())) return;
        if (!MOTOR_ON.getOrDefault(p.getUniqueId(), false)) return;
        if (event.getProjectile() != null) {
            event.getProjectile().setVelocity(event.getProjectile().getVelocity().multiply(speedMultiplier));
        }
    }
}

