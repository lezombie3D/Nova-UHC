package net.novaproject.ultimate.nuzlocke;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.template.CommandAbility;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.entity.Player;

import java.util.List;

public class NuzlockeRootCommand extends Command {

    private final Class<? extends CommandAbility> abilityClass;

    public NuzlockeRootCommand(Class<? extends CommandAbility> abilityClass) {
        this.abilityClass = abilityClass;
    }

    @Override
    public void execute(CommandArguments args) {
        if (!(args.getSender() instanceof Player p)) return;
        UHCPlayer up = UHCPlayerManager.get().getPlayer(p);
        if (up == null) return;
        Nuzlocke nz = Nuzlocke.get();
        if (nz == null) return;
        NuzlockeRole role = nz.getRoleByUHCPlayer(up);
        if (role == null) return;
        for (Ability a : role.getAbilities()) {
            if (abilityClass.isInstance(a)) {
                ((CommandAbility) a).tryCommandUse(p, args.getArguments());
                return;
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        if (!(args.getSender() instanceof Player p)) return List.of();
        UHCPlayer up = UHCPlayerManager.get().getPlayer(p);
        if (up == null) return List.of();
        Nuzlocke nz = Nuzlocke.get();
        if (nz == null) return List.of();
        NuzlockeRole role = nz.getRoleByUHCPlayer(up);
        if (role == null) return List.of();
        for (Ability a : role.getAbilities()) {
            if (abilityClass.isInstance(a)) {
                return ((CommandAbility) a).onTabComplete(p, args.getArguments());
            }
        }
        return List.of();
    }
}

