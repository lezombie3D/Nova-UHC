package net.novaproject.novauhc.task;

import net.novaproject.novauhc.UHCManager;
import org.bukkit.scheduler.BukkitRunnable;

public class GameTask extends BukkitRunnable {

    private static GameTask instance;

    public static GameTask getInstance() {
        return instance;
    }

    @Override
    public void run() {
        instance = this;
        UHCManager uhcManager = UHCManager.get();
        if (uhcManager.isGame()) {
            uhcManager.onSec();
        } else {
            cancel();
        }


    }

}
