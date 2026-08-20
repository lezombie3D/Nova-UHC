package net.novaproject.ultimate.legend.roles.abilities;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.UseAbility;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;

public class DragonFireballActive extends UseAbility {

    @Var(name = "Fireball Yield", desc = "Explosion power.", type = VariableType.DOUBLE)
    private double yield = 1.5;

    public DragonFireballActive() { setCooldown(300); setMaxUse(-1); }

    @Override public String getName() { return "Boule de Feu"; }
    @Override public Material getMaterial() { return Material.FIREBALL; }

    @Override
    public boolean onEnable(Player player) {
        Fireball fb = player.launchProjectile(Fireball.class);
        fb.setYield((float) yield); fb.setIsIncendiary(true);
        return true;
    }
}

