package net.novaproject.ultimate.nuzlocke.roles.grass;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.CommandAbility;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GrassNearCommand extends CommandAbility {

    @Var(name = "Nuzlocke Grass Near Radius", type = VariableType.INTEGER)
    private int radius = 100;

    @Var(name = "Nuzlocke Grass Near Cd", type = VariableType.TIME)
    private int cdSeconds = 300;

    public GrassNearCommand() {
        setCooldown(cdSeconds);
        setMaxUse(-1);
    }

    @Override public String getCommandKey() { return "nearme"; }
    @Override public String getName() { return "Nearby Detect"; }

    @Override
    public boolean onCommand(Player player, String[] args) {
        List<String> nearby = new ArrayList<>();
        for (UHCPlayer other : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player op = other.getPlayer();
            if (op == null || op.equals(player) || !op.getWorld().equals(player.getWorld())) continue;
            if (op.getLocation().distance(player.getLocation()) <= radius) nearby.add(op.getName());
        }
        Collections.shuffle(nearby);
        if (nearby.isEmpty()) {
            player.sendMessage("§a§lNearMe §8│ §7Aucun joueur dans " + radius + " blocs.");
        } else {
            player.sendMessage("§a§lNearMe §8│ §7" + nearby.size() + " joueur(s) proche(s) : §a"
                    + String.join("§7, §a", nearby));
        }
        return true;
    }
}

