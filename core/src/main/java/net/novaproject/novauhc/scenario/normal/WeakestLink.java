package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WeakestLink extends Scenario {

    @Override
    public Family getFamily() { return Family.TIMERS; }

    @Var(name = "Intervalle", desc = "Secondes entre deux éliminations du joueur le plus faible.", type = VariableType.TIME, min = 1)
    private int intervalSeconds = 600;

    private BukkitRunnable updateTask;

    @Override
    public String getName() {
        return "WeakestLink";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.WEAKEST_LINK, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.IRON_CHESTPLATE);
    }

    @Override
    public void onGameStart() {
        if (updateTask != null) updateTask.cancel();
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) { cancel(); return; }
                eliminateWeakest();
            }
        };
        long period = 20L * Math.max(1, intervalSeconds);
        updateTask.runTaskTimer(Main.get(), period, period);
    }

    private void eliminateWeakest() {
        List<UHCPlayer> playing = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();
        if (playing.size() <= 1) return;

        double lowest = Double.MAX_VALUE;
        double highest = -1;
        for (UHCPlayer uhcPlayer : playing) {
            double health = uhcPlayer.getPlayer().getHealth();
            if (health < lowest) lowest = health;
            if (health > highest) highest = health;
        }
        if (lowest == highest) {
            Bukkit.broadcastMessage(LangManager.get().get(NOBODY));
            return;
        }

        List<Player> doomed = new ArrayList<>();
        for (UHCPlayer uhcPlayer : playing) {
            if (uhcPlayer.getPlayer().getHealth() == lowest) doomed.add(uhcPlayer.getPlayer());
        }

        Bukkit.broadcastMessage(LangManager.get().get(ELIMINATED, Map.of(
                "%players%", doomed.stream().map(Player::getName).collect(Collectors.joining(", ")))));
        for (Player player : doomed) player.setHealth(0);
    }

    @Override
    public void onStop() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    private static final DynamicLang ELIMINATED = DynamicLang.of("scenario.weakestlink.eliminated",
            "§c☠ §7Maillon faible §8: §f%players% §7n'avait pas assez de vie.");

    private static final DynamicLang NOBODY = DynamicLang.of("scenario.weakestlink.nobody",
            "§e⚠ §7Maillon faible §8: §7tout le monde est à égalité, personne n'est éliminé.");
}
