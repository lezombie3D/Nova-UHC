package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class BestPvE extends Scenario {
    private final List<Player> listPve = new ArrayList<>();
    private final List<Player> listOutPve = new ArrayList<>();


    @Override
    public String getName() {
        return "Best PvE";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.CACTUS);
    }

    @Override
    public void onStart(Player player) {
        listPve.add(player);
        bestPvE(player);
    }

    private void bestPvE(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (listPve.contains(player)) {
                    player.setMaxHealth(player.getMaxHealth() + 1);
                } else if (listOutPve.contains(player)) {
                    listOutPve.remove(player);
                    listPve.add(player);
                }

            }
        }.runTaskTimer(Main.get(), 0, 20 * 300 * 2);
    }

    @Override
    public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (listPve.contains(player)) {
                listPve.remove(player);
                if (!listOutPve.contains(player)) {
                    listOutPve.add(player);
                }
            }
        }
    }
}
