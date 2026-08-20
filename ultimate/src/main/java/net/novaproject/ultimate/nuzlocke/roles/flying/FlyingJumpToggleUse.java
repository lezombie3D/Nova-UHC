package net.novaproject.ultimate.nuzlocke.roles.flying;

import net.novaproject.novauhc.ability.template.UseAbility;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class FlyingJumpToggleUse extends UseAbility {

    public FlyingJumpToggleUse() {
        setCooldown(1);
        setMaxUse(-1);
    }

    @Override public String getName() { return "Toggle Jump"; }
    @Override public Material getMaterial() { return Material.FEATHER; }

    @Override
    public boolean onEnable(Player player) {
        boolean now = !Flying.JUMP_ON.getOrDefault(player.getUniqueId(), true);
        Flying.JUMP_ON.put(player.getUniqueId(), now);
        player.sendMessage("§b§lFlying §8│ §7Jump boost : §" + (now ? "a§lON" : "c§lOFF"));
        return true;
    }
}

