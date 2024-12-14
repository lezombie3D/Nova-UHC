package net.novaproject.novauhc;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {


    private static Main instance;
    private static UHCManager uhcManager;

    @Override
    public void onLoad() {
        instance = this;
        uhcManager = new UHCManager();
    }

    @Override
    public void onEnable() {
        uhcManager.setup();


    }

    public static Main get() {
        return instance;
    }

    public static UHCManager getUHCManager() {
        return uhcManager;
    }


}
