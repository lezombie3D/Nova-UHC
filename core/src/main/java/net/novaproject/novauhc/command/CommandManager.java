package net.novaproject.novauhc.command;

import java.util.logging.Level;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.debug.DebugLog;
import net.novaproject.novauhc.minigame.ArenaUHC.ArenaCommand;
import net.novaproject.novauhc.command.cmd.*;
import net.novaproject.novauhc.ability.craft.CraftConsultGUI;
import net.novaproject.novauhc.debug.DevCMD;
import net.novaproject.novauhc.ui.EnchantItemUi;
import net.novaproject.novauhc.ui.MumbleUi;
import net.novaproject.novauhc.ui.RulesUi;
import net.novaproject.novauhc.ui.TopLuckUi;
import net.novaproject.novauhc.lobby.RankManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.UiLang;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
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
import org.bukkit.Bukkit;

public class CommandManager implements CommandExecutor, TabExecutor {

    private final JavaPlugin javaPlugin;
    private final Map<String, Command> commands = new HashMap();
    private CommandMap commandMap;

    public CommandManager(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
        if (javaPlugin.getServer().getPluginManager() instanceof SimplePluginManager manager) {

            try {
                Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);
                this.commandMap = (CommandMap) field.get(manager);
            } catch (SecurityException | IllegalAccessException | NoSuchFieldException | IllegalArgumentException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Erreur inattendue", e);
            }
        }

    }

    public static CommandManager get() {
        return Main.get().getCommandManager();
    }

    public void setup() {
        register("teamco", new TeamCordCMD(), "tc", "tcoo");
        register("teaminventory", new TeamInventoryCMD(), "ti", "to", "tinv", "bp");
        register("helpop", new HelpOp(), "");
        register("h", new HCMD(), "host");
        register("doc", new LinkCMD.DocumentCMD(), "");
        register("discord", new LinkCMD.DiscordCMD(), "");
        register("arena", new ArenaCommand(), "leave");
        register("lang",new LangCMD(),"l");
        register("lag", new LagCMD(), "tps", "ping");
        register("color", new ColorCMD(), "couleur");
        register("rules", Command.ofPlayer(player -> new RulesUi(player).open()), "regles", "regle");
        if(DebugLog.devMode()){
            register("dev", new DevCMD(), "debug");
        }
        register("role", new RoleCMD(), "monrole", "myrole");
        register("spec", new SpecCMD(), "spectate");
        register("msg", new MsgCMD(), "m", "w", "tell");
        register("rep", new RepCMD(), "r", "reply");
        register("compo", new CompoCMD(), "composition");
        register("mumble", Command.ofPlayer(player -> new MumbleUi(player).open()), "vocal");
        register("novalink", new NovaLinkCMD(), "linknova");
        register("vote", new VoteCMD(), "sondage");
        register("craft", Command.ofPlayer(player -> new CraftConsultGUI(player).open()));
        register("enchant", Command.ofPlayer(player -> {
            if (!RankManager.isStaffOrOp(player)) {
                LangManager.get().send(CoreLang.COMMON_NO_PERMISSION, player);
                return;
            }
            ItemStack inHand = player.getItemInHand();
            if (inHand == null || inHand.getType() == Material.AIR) {
                LangManager.get().send(UiLang.ENCHANT_ITEM_NO_ITEM, player);
                return;
            }
            new EnchantItemUi(player, inHand).open();
        }), "ench");
        register("full", new FullCMD(), "claim");
        register("ss", new StaffCMD.FreezeCMD());
        register("v", new StaffCMD.VanishCMD());
        register("invsee", new StaffCMD.InvseeCMD(), "inv");
        register("topluck", Command.ofPlayer(player -> {
            if (!RankManager.isStaff(player) && !StaffCMD.isSpectating(player)) {
                LangManager.get().send(CoreLang.COMMON_NO_PERMISSION, player);
                return;
            }
            new TopLuckUi(player).open();
        }), "luck");
        register("mg", new MgCMD(),"minigame");
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
            Bukkit.getLogger().log(Level.SEVERE, "Erreur inattendue", exception);
        }

    }

    public org.bukkit.command.Command resolve(String label) {
        return commandMap == null ? null : commandMap.getCommand(label);
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
