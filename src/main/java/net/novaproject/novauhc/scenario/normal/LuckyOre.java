package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
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

public class LuckyOre extends Scenario {

    private final List<Material> oreTypes = Arrays.asList(
            Material.COAL_ORE,
            Material.IRON_ORE,
            Material.GOLD_ORE,
            Material.DIAMOND_ORE,
            Material.EMERALD_ORE,
            Material.LAPIS_ORE,
            Material.REDSTONE_ORE
    );

    private final Random random = new Random();
    private final int LUCKY_CHANCE = 10; // 10% chance

    @Override
    public String getName() {
        return "LuckyOre";
    }

    @Override
    public String getDescription() {
        return "10% de chance d'obtenir un objet légendaire en minant des minerais.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.NETHER_STAR);
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (!isActive()) return;

        Material blockType = block.getType();

        if (oreTypes.contains(blockType)) {
            // Check for lucky chance
            if (random.nextInt(100) < LUCKY_CHANCE) {
                // Player got lucky!
                LuckyReward reward = getLuckyReward();

                if (reward != null) {
                    // Give the reward
                    giveReward(player, reward);

                    // Announce the lucky find
                    String oreName = getOreName(blockType);
                    Bukkit.broadcastMessage("§6§l[LuckyOre] §f" + player.getName() +
                            " §fa trouvé " + reward.getDescription() +
                            " §fen minant du " + oreName + " !");

                    // Special effects
                    player.getWorld().strikeLightning(player.getLocation());
                    player.sendMessage("§6§l[LuckyOre] §fVOUS AVEZ EU DE LA CHANCE !");
                }
            }
        }
    }

    private LuckyReward getLuckyReward() {
        List<LuckyReward> rewards = Arrays.asList(
                // Weapons and Tools
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

                // Armor
                new LuckyReward(RewardType.ITEM, "un casque en diamant enchanté",
                        createEnchantedItem(Material.DIAMOND_HELMET,
                                Arrays.asList(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.DURABILITY),
                                Arrays.asList(3, 3))),

                new LuckyReward(RewardType.ITEM, "une plastron en diamant enchanté",
                        createEnchantedItem(Material.DIAMOND_CHESTPLATE,
                                Arrays.asList(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.DURABILITY),
                                Arrays.asList(3, 3))),

                // Resources
                new LuckyReward(RewardType.ITEM, "un stack de diamants",
                        new ItemStack(Material.DIAMOND, 64)),

                new LuckyReward(RewardType.ITEM, "des pommes d'or enchantées",
                        new ItemStack(Material.GOLDEN_APPLE, 5, (short) 1)),

                new LuckyReward(RewardType.ITEM, "des perles d'ender",
                        new ItemStack(Material.ENDER_PEARL, 16)),

                new LuckyReward(RewardType.ITEM, "des flèches",
                        new ItemStack(Material.ARROW, 64)),

                // Effects
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
        switch (reward.getType()) {
            case ITEM:
                ItemStack item = reward.getItem();
                if (item != null) {
                    if (player.getInventory().firstEmpty() != -1) {
                        player.getInventory().addItem(item);
                    } else {
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                        player.sendMessage("§6[LuckyOre] §fVotre inventaire est plein ! L'objet a été jeté au sol.");
                    }
                }
                break;

            case EFFECT:
                @SuppressWarnings("unchecked")
                List<PotionEffect> effects = (List<PotionEffect>) reward.getData();
                if (effects != null) {
                    for (PotionEffect effect : effects) {
                        player.addPotionEffect(effect);
                    }
                }
                break;
        }
    }

    private String getOreName(Material ore) {
        switch (ore) {
            case COAL_ORE:
                return "Charbon";
            case IRON_ORE:
                return "Fer";
            case GOLD_ORE:
                return "Or";
            case DIAMOND_ORE:
                return "Diamant";
            case EMERALD_ORE:
                return "Émeraude";
            case LAPIS_ORE:
                return "Lapis";
            case REDSTONE_ORE:
                return "Redstone";
            default:
                return ore.name();
        }
    }

    public int getLuckyChance() {
        return LUCKY_CHANCE;
    }

    // Admin methods
    public void setLuckyChance(int newChance) {
        // This would require making LUCKY_CHANCE non-final
        // For now, it's fixed at 10%
    }

    // Inner classes for reward system
    private enum RewardType {
        ITEM, EFFECT
    }

    private static class LuckyReward {
        private final RewardType type;
        private final String description;
        private final Object data;

        public LuckyReward(RewardType type, String description, Object data) {
            this.type = type;
            this.description = description;
            this.data = data;
        }

        public RewardType getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public Object getData() {
            return data;
        }

        public ItemStack getItem() {
            return type == RewardType.ITEM ? (ItemStack) data : null;
        }
    }
}
