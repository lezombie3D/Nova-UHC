package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.player.utils.PlayerUtils;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioLang;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LootCrate extends Scenario {

    @Override
    public Family getFamily() { return Family.LOOT; }

    private final Random random = new Random();
    private BukkitRunnable lootCrateTask;

    @Var(lang = ScenarioVarLang.class, nameKey = "LOOTCRATE_VAR_SPAWN_INTERVAL_NAME", descKey = "LOOTCRATE_VAR_SPAWN_INTERVAL_DESC", type = VariableType.TIME)
    private int spawnInterval = 600;

    @Var(name = "Chance de tier 2", desc = "Chance en pourcentage de recevoir une caisse de tier 2 au lieu de tier 1.", type = VariableType.PERCENTAGE, min = 0, max = 100)
    private int tier2Chance = 25;

    @Var(name = "Objets tier 1", desc = "Nombre d'objets dans une caisse de tier 1.", type = VariableType.INTEGER, min = 1)
    private int tier1Items = 3;

    @Var(name = "Objets tier 2", desc = "Nombre d'objets dans une caisse de tier 2.", type = VariableType.INTEGER, min = 1)
    private int tier2Items = 4;

    @Var(lang = ScenarioVarLang.class, nameKey = "LOOTCRATE_VAR_ENABLE_DIAMONDS_NAME", descKey = "LOOTCRATE_VAR_ENABLE_DIAMONDS_DESC", type = VariableType.BOOLEAN)
    private boolean enableDiamonds = true;

    @Var(lang = ScenarioVarLang.class, nameKey = "LOOTCRATE_VAR_ENABLE_GOLDEN_APPLES_NAME", descKey = "LOOTCRATE_VAR_ENABLE_GOLDEN_APPLES_DESC", type = VariableType.BOOLEAN)
    private boolean enableGoldenApples = true;

    @Var(lang = ScenarioVarLang.class, nameKey = "LOOTCRATE_VAR_ENABLE_ENCHANTED_ITEMS_NAME", descKey = "LOOTCRATE_VAR_ENABLE_ENCHANTED_ITEMS_DESC", type = VariableType.BOOLEAN)
    private boolean enableEnchantedItems = true;

    @Var(lang = ScenarioVarLang.class, nameKey = "LOOTCRATE_VAR_ENABLE_POTIONS_NAME", descKey = "LOOTCRATE_VAR_ENABLE_POTIONS_DESC", type = VariableType.BOOLEAN)
    private boolean enablePotions = true;

    @Var(lang = ScenarioVarLang.class, nameKey = "LOOTCRATE_VAR_ENABLE_FOOD_NAME", descKey = "LOOTCRATE_VAR_ENABLE_FOOD_DESC", type = VariableType.BOOLEAN)
    private boolean enableFood = true;

    @Override
    public String getName() {
        return "LootCrate";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.LOOT_CRATE, player)
                .replace("%minutes%", String.valueOf(spawnInterval / 60));
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.CHEST);
    }

    @Override
    public void onGameStart() {
        if (lootCrateTask != null) lootCrateTask.cancel();

        lootCrateTask = new BukkitRunnable() {
            private int timer = 0;

            @Override
            public void run() {
                if (!isActive()) { cancel(); return; }

                timer++;
                if (timer >= spawnInterval) {
                    deliverCrates();
                    timer = 0;
                    return;
                }

                int remaining = spawnInterval - timer;
                if (remaining == 60) LangManager.get().sendAll(ScenarioLang.LOOTCRATE_CRATES_WARNING_1MIN);
                else if (remaining == 10) LangManager.get().sendAll(ScenarioLang.LOOTCRATE_CRATES_WARNING_10SEC);
            }
        };
        lootCrateTask.runTaskTimer(Main.get(), 20L, 20L);
    }

    private void deliverCrates() {
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            if (player == null) continue;

            boolean elite = random.nextInt(100) < tier2Chance;
            List<ItemStack> pool = elite ? tier2Pool() : tier1Pool();
            if (pool.isEmpty()) pool = elite ? tier1Pool() : tier2Pool();
            if (pool.isEmpty()) continue;

            int count = elite ? tier2Items : tier1Items;
            for (int i = 0; i < count; i++) {
                PlayerUtils.giveOrDrop(player, pool.get(random.nextInt(pool.size())).clone());
            }

            player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0f, elite ? 1.8f : 1.2f);
            player.sendMessage(t(elite ? TIER2 : TIER1, player, Map.of("%count%", count)));
        }
    }

    private List<ItemStack> tier1Pool() {
        List<ItemStack> pool = new ArrayList<>();
        if (enableFood) pool.add(new ItemStack(Material.COOKED_BEEF, 8 + random.nextInt(8)));
        pool.add(new ItemStack(Material.IRON_INGOT, 2 + random.nextInt(4)));
        pool.add(new ItemStack(Material.ARROW, 16 + random.nextInt(17)));
        pool.add(new ItemStack(Material.GOLD_INGOT, 2 + random.nextInt(4)));
        return pool;
    }

    private List<ItemStack> tier2Pool() {
        List<ItemStack> pool = new ArrayList<>();
        if (enableDiamonds) pool.add(new ItemStack(Material.DIAMOND, 3 + random.nextInt(5)));
        if (enableGoldenApples) pool.add(new ItemStack(Material.GOLDEN_APPLE, 2 + random.nextInt(3)));
        if (enablePotions) pool.add(createPotion(8201));
        if (enableEnchantedItems) {
            pool.add(createEnchantedItem(Material.DIAMOND_SWORD, Enchantment.DAMAGE_ALL, 2));
            pool.add(createEnchantedItem(Material.DIAMOND_PICKAXE, Enchantment.DIG_SPEED, 3));
            pool.add(createEnchantedItem(Material.BOW, Enchantment.ARROW_DAMAGE, 3));
        }
        return pool;
    }

    private ItemStack createEnchantedItem(Material mat, Enchantment ench, int lvl) {
        ItemStack item = new ItemStack(mat);
        item.addUnsafeEnchantment(ench, lvl);
        return item;
    }

    private ItemStack createPotion(int data) {
        ItemStack potion = new ItemStack(Material.POTION, 1);
        potion.setDurability((short) data);
        return potion;
    }

    @Override
    public void onStop() {
        if (lootCrateTask != null) {
            lootCrateTask.cancel();
            lootCrateTask = null;
        }
    }

    private static final DynamicLang TIER1 = DynamicLang.of("scenario.lootcrate.tier1",
            "§a✦ §7Caisse §fTier 1 §7reçue §8: §f%count% §7objets.");

    private static final DynamicLang TIER2 = DynamicLang.of("scenario.lootcrate.tier2",
            "§6✦ §7Caisse §6§lTier 2 §7reçue §8: §f%count% §7objets !");
}
