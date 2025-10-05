package net.novaproject.novauhc.arena;

import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import org.bukkit.entity.Player;

public class ArenaCommand extends Command {

    @Override
    public void execute(CommandArguments commandArguments) {

        if (commandArguments.getSender() instanceof Player) {
            Player player = (Player) commandArguments.getSender();
            ArenaUHC arenaUHC = ArenaUHC.get();

            if (arenaUHC == null) {
                return;
            }

            arenaUHC.removePlayer(player);

        }

    }
}
