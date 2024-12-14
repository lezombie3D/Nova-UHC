package net.novaproject.novauhc.command;

import net.novaproject.novauhc.Main;
import org.bukkit.command.CommandExecutor;

public class CommandManager {

    public static void setup(){

        Main.get().getCommand("hconfig").setExecutor(new HConfig());

    }

}
