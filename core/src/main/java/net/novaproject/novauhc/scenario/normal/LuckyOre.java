package net.novaproject.novauhc.scenario.normal;

import java.util.Map;import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioLang;

public class LuckyOre extends Scenario {

    @Override
    public Family getFamily() { return Family.MINAGE; }


    private final Random random = new Random();

    @Var(lang = ScenarioVarLang.class, nameKey = "LUCKYORE_VAR_LUCKY_CHANCE_NAME", descKey = "LUCKYORE_VAR_LUCKY_CHANCE_DESC", type = VariableType.PERCENTAGE)
    private int luckyChance = 10;

    @Override
    public String getName() {
        return "LuckyOre";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.LUCKY_ORE, player)
                .replace("%chance%", String.valueOf(luckyChance));
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.NETHER_STAR);
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (!isActive()) return;

        Material blockType = block.getType();
        if (MiningScenarios.ORE_TYPES.contains(blockType) && random.nextInt(100) < luckyChance) {
            LuckyReward reward = getLuckyReward();
            if (reward != null) {
                giveReward(player, reward);
                LangManager.get().sendAll(ScenarioLang.LUCKYORE_LUCKY_BROADCAST, Map.of("%player%", player.getName(), "%reward%", reward.description(), "%ore%", getOreName(blockType)));
                player.getWorld().strikeLightning(player.getLocation());
                LangManager.get().send(ScenarioLang.LUCKYORE_LUCKY_PERSONAL, player);
            }
        }
    }

    private LuckyReward getLuckyReward() {
        List<LuckyReward> rewards = Arrays.asList(
                new LuckyReward(RewardType.ITEM, "une épée en diamant enchantée",
                        createEnchantedItem(Material.DIAMOND_SWORD,
                                Arrays.asList(Enchantment.DAMAGE_ALL, Enchantment.FIRE_ASPECT),
                                Arrays.asList(3, 2))),
                new LuckyReward(RewardType.ITEM, "une pioche en diamant enchantée",
                        createEnchantedItem(Material.DIAMOND_PICKAXE,
                                Arrays.asList(Enchantment.DIG_SPEED, Enchantment.DURABILITY),
                                Arrays.asList(4, 3))),
                new LuckyReward(RewardType.ITEM, "un arc enchanté",
                        createEnchantedItem(Material.BOW,
                                Arrays.asList(Enchantment.ARROW_DAMAGE, Enchantment.ARROW_INFINITE),
                                Arrays.asList(4, 1))),
                new LuckyReward(RewardType.ITEM, "un casque en diamant enchanté",
                        createEnchantedItem(Material.DIAMOND_HELMET,
                                Arrays.asList(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.DURABILITY),
                                Arrays.asList(3, 3))),
                new LuckyReward(RewardType.ITEM, "un plastron en diamant enchanté",
                        createEnchantedItem(Material.DIAMOND_CHESTPLATE,
                                Arrays.asList(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.DURABILITY),
                                Arrays.asList(3, 3))),
                new LuckyReward(RewardType.ITEM, "un stack de diamants",
                        new ItemStack(Material.DIAMOND, 64)),
                new LuckyReward(RewardType.ITEM, "des pommes d'or enchantées",
                        new ItemStack(Material.GOLDEN_APPLE, 5, (short) 1)),
                new LuckyReward(RewardType.ITEM, "des perles d'ender",
                        new ItemStack(Material.ENDER_PEARL, 16)),
                new LuckyReward(RewardType.ITEM, "des flèches",
                        new ItemStack(Material.ARROW, 64)),
                new LuckyReward(RewardType.EFFECT, "des effets de chance",
                        Arrays.asList(
                                new PotionEffect(PotionEffectType.SPEED, 6000, 1),
                                new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 6000, 0),
                                new PotionEffect(PotionEffectType.REGENERATION, 1200, 1)
                        )),
                new LuckyReward(RewardType.EFFECT, "des effets de protection",
                        Arrays.asList(
                                new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 6000, 0),
                                new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 6000, 0),
                                new PotionEffect(PotionEffectType.WATER_BREATHING, 6000, 0)
                        ))
        );
        return rewards.get(random.nextInt(rewards.size()));
    }

    private ItemStack createEnchantedItem(Material material, List<Enchantment> enchantments, List<Integer> levels) {
        ItemStack item = new ItemStack(material);
        for (int i = 0; i < enchantments.size() && i < levels.size(); i++) {
            item.addUnsafeEnchantment(enchantments.get(i), levels.get(i));
        }
        return item;
    }

    private void giveReward(Player player, LuckyReward reward) {
        switch (reward.type()) {
            case ITEM -> {
                ItemStack item = reward.getItem();
                if (item != null) {
                    if (player.getInventory().firstEmpty() != -1) {
                        player.getInventory().addItem(item);
                    } else {
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                        LangManager.get().send(ScenarioLang.LUCKYORE_INVENTORY_FULL, player);
                    }
                }
            }
            case EFFECT -> {
                @SuppressWarnings("unchecked")
                List<PotionEffect> effects = (List<PotionEffect>) reward.data();
                if (effects != null) effects.forEach(player::addPotionEffect);
            }
        }
    }

    private String getOreName(Material ore) {
        return switch (ore) {
            case COAL_ORE -> "Charbon";
            case IRON_ORE -> "Fer";
            case GOLD_ORE -> "Or";
            case DIAMOND_ORE -> "Diamant";
            case EMERALD_ORE -> "Émeraude";
            case LAPIS_ORE -> "Lapis";
            case REDSTONE_ORE -> "Redstone";
            default -> ore.name();
        };
    }

    private enum RewardType { ITEM, EFFECT }

    private record LuckyReward(RewardType type, String description, Object data) {
        public ItemStack getItem() {
            return type == RewardType.ITEM ? (ItemStack) data : null;
        }
    }
}
