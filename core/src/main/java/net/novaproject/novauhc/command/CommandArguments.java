package net.novaproject.novauhc.command;

import java.util.Locale;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

    public int size() {
        return this.arguments.length;
    }

    public boolean has(int index) {
        return index >= 0 && index < this.arguments.length;
    }

    public String get(int index, String def) {
        return has(index) ? this.arguments[index] : def;
    }

    public int getInt(int index, int def) {
        if (!has(index)) return def;
        try {
            return Integer.parseInt(this.arguments[index]);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public double getDouble(int index, double def) {
        if (!has(index)) return def;
        try {
            return Double.parseDouble(this.arguments[index].replace(',', '.'));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public boolean getBoolean(int index, boolean def) {
        if (!has(index)) return def;
        String v = this.arguments[index].toLowerCase(Locale.ROOT);
        if (v.equals("true") || v.equals("on") || v.equals("oui") || v.equals("yes")) return true;
        if (v.equals("false") || v.equals("off") || v.equals("non") || v.equals("no")) return false;
        return def;
    }

    public Player getPlayer(int index) {
        return has(index) ? Bukkit.getPlayerExact(this.arguments[index]) : null;
    }

    public String join(int fromIndex) {
        if (!has(fromIndex)) return "";
        return String.join(" ", Arrays.copyOfRange(this.arguments, fromIndex, this.arguments.length));
    }
}