package net.novaproject.novauhc.scenario.normal;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioLang;

public class BloodCycle extends Scenario {

    @Override
    public Family getFamily() { return Family.MINAGE; }

    private static final Material[] CYCLE = {
            Material.DIAMOND_ORE, Material.GOLD_ORE, Material.IRON_ORE, Material.COAL_ORE,
            Material.LAPIS_ORE, Material.REDSTONE_ORE, Material.EMERALD_ORE, null
    };

    private static final Random RANDOM = new Random();

    private Material current = CYCLE[0];
    private BukkitRunnable cycleTask;

    @Var(lang = ScenarioVarLang.class, nameKey = "BLOODCYCLE_VAR_BETWEEN_NAME", descKey = "BLOODCYCLE_VAR_BETWEEN_DESC", type = VariableType.TIME)
    private int between = 600;

    @Var(name = "Dégâts", desc = "Dégâts subis en minant le minerai maudit.", type = VariableType.DOUBLE, min = 0)
    private double damage = 1.0;

    @Override
    public String getName() {
        return "BloodCycle";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.BLOOD_CYCLE, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BONE);
    }

    @Override
    public void onGameStart() {
        rollNextOre();

        if (cycleTask != null) cycleTask.cancel();
        cycleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) { cancel(); return; }
                rollNextOre();
                for (UHCPlayer p : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                    sendOreMessage(p.getPlayer(), current);
                }
            }
        };
        long period = 20L * Math.max(1, between);
        cycleTask.runTaskTimer(Main.get(), period, period);
    }

    private void rollNextOre() {
        Material next;
        do {
            next = CYCLE[RANDOM.nextInt(CYCLE.length)];
        } while (next == current && CYCLE.length > 1);
        current = next;
    }

    @Override
    public void onStart(Player player) {
        sendOreMessage(player, current);
    }

    private void sendOreMessage(Player player, Material ore) {
        if (ore == null) {
            LangManager.get().send(NO_ORE, player);
            return;
        }
        switch (ore) {
            case DIAMOND_ORE:
                LangManager.get().send(ScenarioLang.BLOODCYCLE_DIAMOND, player);
                break;
            case GOLD_ORE:
                LangManager.get().send(ScenarioLang.BLOODCYCLE_GOLD, player);
                break;
            case IRON_ORE:
                LangManager.get().send(ScenarioLang.BLOODCYCLE_IRON, player);
                break;
            case COAL_ORE:
                LangManager.get().send(ScenarioLang.BLOODCYCLE_COAL, player);
                break;
            case LAPIS_ORE:
                LangManager.get().send(ScenarioLang.BLOODCYCLE_LAPIS, player);
                break;
            case REDSTONE_ORE:
                LangManager.get().send(ScenarioLang.BLOODCYCLE_REDSTONE, player);
                break;
            case EMERALD_ORE:
                LangManager.get().send(EMERALD, player);
                break;
        }
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (current == null || block.getType() != current) return;
        player.damage(damage);
        LangManager.get().send(ScenarioLang.BLOODCYCLE_TAKE_DAMAGE, player);
    }

    private static final DynamicLang NO_ORE = DynamicLang.of("scenario.bloodcycle.no-ore",
            "§a✦ §7Aucun minerai n'est maudit pour le moment.");

    private static final DynamicLang EMERALD = DynamicLang.of("scenario.bloodcycle.emerald",
            "Vous ne devez pas miner §a§lÉmeraude§f.");

    @Override
    public void onStop() {
        if (cycleTask != null) {
            cycleTask.cancel();
            cycleTask = null;
        }
    }
}

