package net.novaproject.novauhc.scenario.role.scenario.mhdragonfall;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.utils.Titles;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class DragonStatsDisplay {

    private final Player player;
    private final DragonRole dragon;
    private BukkitRunnable task;

    public DragonStatsDisplay(Player player, DragonRole dragon) {
        this.player = player;
        this.dragon = dragon;
    }


    public void start() {
        if (task != null) task.cancel();

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    return;
                }

                String message = formatStats(dragon);

                new Titles().sendActionText(player, message);
            }
        };
        task.runTaskTimer(Main.get(), 0L, 2L);
    }


    public void stop() {
        if (task != null) task.cancel();
    }

    private String formatStats(DragonRole dragon) {
        int hp = dragon.getCurrentHP();
        int maxHp = dragon.getMaxHP();
        double resistance = dragon.getCurrentResistance();
        double strength = dragon.getCurrentStrength();
        int abso = dragon.getAbsortion();

        return "§c❤ §f" + hp + "/" + maxHp + "   " +
                "§b✦ §f" + String.format("%.1f", resistance) + "   " +
                "§6⚔ §f" + String.format("%.1f", strength) + "   " +
                "§a✚ §f" + abso;
    }
}
