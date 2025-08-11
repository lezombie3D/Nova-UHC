package net.novaproject.novauhc.command;

import net.novaproject.novauhc.Main;

public class CommandManager {

    public static void setup(){

        Main.get().getCommand("h").setExecutor(new HCMD());
        Main.get().getCommand("p").setExecutor(new PCMD());
        Main.get().getCommand("tc").setExecutor(new TeamCordCMD());
        Main.get().getCommand("ti").setExecutor(new TeamInventoryCMD());
        Main.get().getCommand("t").setExecutor(new TaupeCMD());
        Main.get().getCommand("c").setExecutor(new CromagnonCMD());
        Main.get().getCommand("ld").setExecutor(new LdCMD());
        Main.get().getCommand("mumble").setExecutor(new MumbleCMD());
        Main.get().getCommand("config").setExecutor(new ConfigCMD());
        Main.get().getCommand("ff").setExecutor(new FireForceCMD());
    }

}
