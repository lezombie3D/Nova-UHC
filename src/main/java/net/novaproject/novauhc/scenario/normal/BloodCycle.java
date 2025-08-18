package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.scenario.ScenarioLangManager;
import net.novaproject.novauhc.scenario.lang.BloodCycleLang;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class BloodCycle extends Scenario {
    private final Material[] cache = new Material[6];
    int i = 0;

    @Override
    public String getName() {
        return "BloodCycle";
    }

    @Override
    public String getDescription() {
        return "Un type de minerai change périodiquement et inflige des dégâts si miné.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BONE);
    }

    @Override
    public ScenarioLang[] getLang() {
        return BloodCycleLang.values();
    }

    @Override
    public String getPath() {
        return "bloodcycle";
    }

    @Override
    public void onStart(Player player) {
        cache[0] = Material.DIAMOND_ORE;
        cache[1] = Material.GOLD_ORE;
        cache[2] = Material.IRON_ORE;
        cache[3] = Material.COAL_ORE;
        cache[4] = Material.LAPIS_ORE;
        cache[5] = Material.REDSTONE_ORE;
        switch (cache[i]) {
            case DIAMOND_ORE:
                ScenarioLangManager.send(player, BloodCycleLang.DIAMOND);
                break;
            case GOLD_ORE:
                ScenarioLangManager.send(player, BloodCycleLang.GOLD);
                break;
            case IRON_ORE:
                ScenarioLangManager.send(player, BloodCycleLang.IRON);
                break;
            case COAL_ORE:
                ScenarioLangManager.send(player, BloodCycleLang.COAL);
                break;
            case LAPIS_ORE:
                ScenarioLangManager.send(player, BloodCycleLang.LAPIS);
                break;
            case REDSTONE_ORE:
                ScenarioLangManager.send(player, BloodCycleLang.REDSTONE);
                break;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                i++;
                if (i == 6)
                    i = 0;
                switch (cache[i]) {
                    case DIAMOND_ORE:
                        ScenarioLangManager.send(player, BloodCycleLang.DIAMOND);
                        break;
                    case GOLD_ORE:
                        ScenarioLangManager.send(player, BloodCycleLang.GOLD);
                        break;
                    case IRON_ORE:
                        ScenarioLangManager.send(player, BloodCycleLang.IRON);
                        break;
                    case COAL_ORE:
                        ScenarioLangManager.send(player, BloodCycleLang.COAL);
                        break;
                    case LAPIS_ORE:
                        ScenarioLangManager.send(player, BloodCycleLang.LAPIS);
                        break;
                    case REDSTONE_ORE:
                        ScenarioLangManager.send(player, BloodCycleLang.REDSTONE);
                        break;
                }

            }
        }.runTaskTimerAsynchronously(Main.get(), 20L * getConfig().getInt("timer"), 20L * getConfig().getInt("timer"));
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (block.getType().equals(cache[i])) {
            player.damage(1);
            ScenarioLangManager.send(player, BloodCycleLang.TAKE_DAMAGE);
        }
    }
}
