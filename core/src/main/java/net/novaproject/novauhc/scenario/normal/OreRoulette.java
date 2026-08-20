package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioLang;

public class OreRoulette extends Scenario {

    @Override
    public Family getFamily() { return Family.MINAGE; }

    private final List<Material> oreTypes = Arrays.asList(
            Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE,
            Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.LAPIS_ORE, Material.REDSTONE_ORE);
    private final Map<Material, RouletteItem> oreRewards = new HashMap<>();
    private final Random random = new Random();

    public OreRoulette() { initializeRouletteRewards(); }


    @Override public String getName() { return "OreRoulette"; }
    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.ORE_ROULETTE, player);
    }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.EMERALD_ORE); }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (!isActive() || !oreTypes.contains(block.getType())) return;
        event.setCancelled(true);
        block.setType(Material.AIR);
        MiningScenarios.damageTool(player, player.getItemInHand());
        RouletteItem reward = getRandomReward();
        if (reward == null) return;
        ItemStack drop = new ItemStack(reward.material, reward.amount);
        if (reward.material == Material.INK_SACK) drop.setDurability((short) 4);
        block.getWorld().dropItemNaturally(block.getLocation().add(0.5D, 0.5D, 0.5D), drop);
        String from = getOreName(block.getType()); String to = getItemName(reward.material);
        player.sendMessage(t(ScenarioLang.OREROULETTE_ORE_CHANGED, player, Map.of("%from%", from, "%to%", reward.amount + "x " + to)));
        if (reward.material == Material.DIAMOND) player.sendMessage(t(ScenarioLang.OREROULETTE_JACKPOT_DIAMOND, player, Map.of()));
        else if (reward.material == Material.EMERALD) player.sendMessage(t(ScenarioLang.OREROULETTE_JACKPOT_EMERALD, player, Map.of()));
    }

    private void initializeRouletteRewards() {
        oreRewards.put(Material.COAL, new RouletteItem(Material.COAL, 1, 35));
        oreRewards.put(Material.IRON_INGOT, new RouletteItem(Material.IRON_INGOT, 1, 25));
        oreRewards.put(Material.GOLD_INGOT, new RouletteItem(Material.GOLD_INGOT, 1, 15));
        oreRewards.put(Material.INK_SACK, new RouletteItem(Material.INK_SACK, 4 + random.nextInt(5), 10));
        oreRewards.put(Material.REDSTONE, new RouletteItem(Material.REDSTONE, 4 + random.nextInt(2), 8));
        oreRewards.put(Material.EMERALD, new RouletteItem(Material.EMERALD, 1, 4));
        oreRewards.put(Material.DIAMOND, new RouletteItem(Material.DIAMOND, 1, 3));
    }

    private RouletteItem getRandomReward() {
        int total = oreRewards.values().stream().mapToInt(i -> i.weight).sum();
        int val = random.nextInt(total); int cur = 0;
        for (RouletteItem item : oreRewards.values()) { cur += item.weight; if (val < cur) return new RouletteItem(item.material, calculateAmount(item.material), item.weight); }
        return new RouletteItem(Material.COAL, 1, 35);
    }

    private int calculateAmount(Material m) {
        return switch (m) { case INK_SACK -> 4 + random.nextInt(5); case REDSTONE -> 4 + random.nextInt(2); default -> 1; };
    }

    private String getOreName(Material m) {
        return switch (m) { case COAL_ORE -> "Charbon"; case IRON_ORE -> "Fer"; case GOLD_ORE -> "Or";
            case DIAMOND_ORE -> "Diamant"; case EMERALD_ORE -> "Émeraude"; case LAPIS_ORE -> "Lapis";
            case REDSTONE_ORE -> "Redstone"; default -> m.name(); };
    }

    private String getItemName(Material m) {
        return switch (m) { case COAL -> "Charbon"; case IRON_INGOT -> "Lingot de Fer"; case GOLD_INGOT -> "Lingot d'Or";
            case DIAMOND -> "Diamant"; case EMERALD -> "Émeraude"; case INK_SACK -> "Lapis Lazuli";
            case REDSTONE -> "Redstone"; default -> m.name(); };
    }

    private record RouletteItem(Material material, int amount, int weight) {}
}

