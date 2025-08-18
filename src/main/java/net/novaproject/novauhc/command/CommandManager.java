package net.novaproject.novauhc.command;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.command.cmd.*;
import org.bukkit.command.CommandExecutor;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {

    private static final Main PLUGIN = Main.get();

    private static final Map<String, CommandExecutor> COMMANDS = new HashMap<>();

    static {
        COMMANDS.put("p", new PCMD());
        COMMANDS.put("tc", new TeamCordCMD());
        COMMANDS.put("ti", new TeamInventoryCMD());
        COMMANDS.put("t", new TaupeCMD());
        COMMANDS.put("c", new CromagnonCMD());
        COMMANDS.put("ld", new LdCMD());
        COMMANDS.put("mumble", new MumbleCMD());
        COMMANDS.put("config", new ConfigCMD());
        COMMANDS.put("ff", new FireForceCMD());
        COMMANDS.put("h", new HCMD());
        COMMANDS.put("msg", new MsgCMD());
        COMMANDS.put("r", new RepCMD());
    }

    public static void setup() {
        for (Map.Entry<String, CommandExecutor> entry : COMMANDS.entrySet()) {
            PLUGIN.getCommand(entry.getKey()).setExecutor(entry.getValue());
        }
    }

}
