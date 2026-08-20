package net.novaproject.novauhc.command.cmd.host;

import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class HostSub extends Command.PlayerCommand {

    public static HostSub of(BiConsumer<Player, CommandArguments> action) {
        return new HostSub() {
            @Override
            protected void run(Player player, CommandArguments args) {
                action.accept(player, args);
            }
        };
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        return Collections.emptyList();
    }
}

