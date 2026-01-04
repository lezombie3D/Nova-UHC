
package net.novaproject.novauhc.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandArguments {
    private final CommandSender commandSender;
    private final Command command;
    private final String label;
    private final String[] arguments;

    public CommandArguments(CommandSender commandSender, Command command, String label, String... arguments) {
        this.commandSender = commandSender;
        this.command = command;
        this.label = label;
        this.arguments = arguments;
    }

    public CommandSender getSender() {
        return this.commandSender;
    }

    public Command getCommand() {
        return this.command;
    }

    public String getArgument(int index) {
        return this.arguments[index];
    }

    public String[] getArguments() {
        return this.arguments;
    }

    public String getLabel() {
        return this.label;
    }

    public String getLastArgument() {
        return this.arguments.length == 0 ? "" : this.arguments[this.arguments.length - 1];
    }
}
