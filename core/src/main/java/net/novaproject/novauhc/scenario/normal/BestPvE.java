package net.novaproject.novauhc.scenario.normal;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.lang.LangManager;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioLang;

public class BestPvE extends Scenario {

    @Override
    public Family getFamily() { return Family.COMBAT; }
    private final List<Player> listPve = new ArrayList<>();
    private final List<Player> listOutPve = new ArrayList<>();
    private final List<BukkitRunnable> tasks = new ArrayList<>();
    private final Map<UUID, Double> baseMaxHealth = new HashMap<>();
    @Var(lang = ScenarioVarLang.class, nameKey = "BESTPVE_VAR_TIMER_NAME", descKey = "BESTPVE_VAR_TIMER_DESC", type = VariableType.TIME)
    private int timer = 600;

    @Var(lang = ScenarioVarLang.class, nameKey = "BESTPVE_VAR_HEART_GAIN_NAME", descKey = "BESTPVE_VAR_HEART_GAIN_DESC", type = VariableType.INTEGER)
    private int heartGain = 2;

    @Override
    public String getName() {
        return "Best PvE";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.BEST_PVE, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.CACTUS);
    }

    @Override
    public void onStart(Player player) {
        listPve.add(player);
        baseMaxHealth.putIfAbsent(player.getUniqueId(), player.getMaxHealth());
        bestPvE(player);
    }

    private void bestPvE(Player player) {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }
                if (listPve.contains(player)) {
                    player.setMaxHealth(player.getMaxHealth() + heartGain);
                    LangManager.get().send(ScenarioLang.BESTPVE_GAIN_MESSAGE, player);
                }
            }
        };
        tasks.add(task);
        long period = 20L * Math.max(1, timer);
        task.runTaskTimer(Main.get(), period, period);
    }

    @Override
    public void onKill(UHCPlayer killer, UHCPlayer victim) {
        if (killer == null || killer.getPlayer() == null) return;
        Player player = killer.getPlayer();
        if (!listOutPve.remove(player)) return;
        listPve.add(player);
        LangManager.get().send(ScenarioLang.BESTPVE_LIST_JOIN, player);
    }

    @Override
    public void onStop() {
        for (BukkitRunnable task : tasks) task.cancel();
        tasks.clear();
        for (Map.Entry<UUID, Double> entry : baseMaxHealth.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) player.setMaxHealth(entry.getValue());
        }
        baseMaxHealth.clear();
        listPve.clear();
        listOutPve.clear();
    }

    @Override
    public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {
        if (entity instanceof Player player) {
            if (listPve.contains(player)) {
                listPve.remove(player);
                if (!listOutPve.contains(player)) {
                    listOutPve.add(player);
                    LangManager.get().send(ScenarioLang.BESTPVE_LIST_QUIT, player);
                }
            }
        }
    }

}

