package net.novaproject.novauhc.command;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.command.cmd.*;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager implements CommandExecutor, TabExecutor {

    private final JavaPlugin javaPlugin;
    private final Map<String, Command> commands = new HashMap();
    private CommandMap commandMap;

    public CommandManager(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
        if (javaPlugin.getServer().getPluginManager() instanceof SimplePluginManager) {
            SimplePluginManager manager = (SimplePluginManager) javaPlugin.getServer().getPluginManager();

            try {
                Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);
                this.commandMap = (CommandMap) field.get(manager);
            } catch (SecurityException | IllegalAccessException | NoSuchFieldException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

    }

    public static CommandManager get() {
        return Main.get().getCommandManager();
    }

    public void setup() {
        register("teamco", new TeamCordCMD(), "tc", "tcoo");
        register("teaminventory", new TeamInventoryCMD(), "ti", "to", "tinv");
        register("helpop", new HelpOp(), "");
        register("h", new HCMD(), "host");
        register("doc", new DocumentCMD(), "");
        register("discord", new DiscordCMD(), "");
        register("config", new ConfigCMD(), "preconfig");

    }

    public void register(String name, Command command, String... aliases) {
        this.commands.put(name, command);

        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            PluginCommand pluginCommand = constructor.newInstance(name, this.javaPlugin);
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
            pluginCommand.setAliases(Arrays.asList(aliases));
            this.commandMap.register(name, pluginCommand);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
        for (String command_name : this.commands.keySet()) {
            if (command.getName().equalsIgnoreCase(command_name)) {
                this.commands.get(command_name).execute(new CommandArguments(commandSender, command, command_name, strings));
                return true;
            }
        }

        return false;
    }


    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        for (String commandName : this.commands.keySet()) {
            if (command.getName().equalsIgnoreCase(commandName)) {
                return this.commands.get(commandName).tabComplete(new CommandArguments(sender, command, commandName, args));
            }
        }

        return null;
    }

}
