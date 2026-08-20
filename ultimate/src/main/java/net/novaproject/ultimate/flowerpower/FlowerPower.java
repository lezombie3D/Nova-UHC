package net.novaproject.ultimate.flowerpower;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.ultimate.flowerpower.FlowerPowerLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.HashMap;
import java.util.Random;

public class FlowerPower extends Scenario {

    private static final Random RANDOM = new Random();

    @Var(name = "Var Golden Apple Amount", type = VariableType.INTEGER)
    private int goldenAppleAmount = 3;

    @Var(name = "Var Ingot Amount", type = VariableType.INTEGER)
    private int ingotAmount = 16;

    @Var(name = "Var Normal Drop Amount", type = VariableType.INTEGER)
    private int normalDropAmount = 16;

    @Var(name = "Var Max Enchant Level", type = VariableType.INTEGER)
    private int maxEnchantLevel = 3;

    @Override
    public String getName() {
        return LangManager.get().get(FlowerPowerLang.FLOWER_POWER_NAME);
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(FlowerPowerLang.FLOWER_POWER_DESC, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.YELLOW_FLOWER);
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (block.getType() == Material.YELLOW_FLOWER || block.getType() == Material.RED_ROSE) {
            event.setCancelled(true);
            checkDrop(block);
            block.setType(Material.AIR);
        }
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    private void checkDrop(Block b) {
        Material mat;
        int attempts = 0;
        do {
            mat = Material.values()[RANDOM.nextInt(Material.values().length)];
            attempts++;
            if (attempts > 1000) return;
        } while (!isAcceptedMaterial(mat));

        int amount;
        ItemStack item;

        if (isRarity(mat)) {
            if (mat == Material.GOLDEN_APPLE) {
                amount = goldenAppleAmount;
            } else if (mat == Material.IRON_INGOT || mat == Material.GOLD_INGOT) {
                amount = ingotAmount;
            } else {
                amount = 1;
            }
            item = new ItemStack(mat, amount);

            if (mat == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
                Enchantment randomEnchant = Enchantment.values()[RANDOM.nextInt(Enchantment.values().length)];
                int level = RANDOM.nextInt(maxEnchantLevel) + 1;
                meta.addStoredEnchant(randomEnchant, level, true);
                item.setItemMeta(meta);
            }
        } else {
            item = new ItemStack(mat, normalDropAmount);
        }

        b.getWorld().dropItem(b.getLocation(), item);
    }

    private boolean isAcceptedMaterial(Material type) {
        return type != Material.AIR && type != Material.ARMOR_STAND && type != Material.BEDROCK && type != Material.BED_BLOCK && type != Material.ACACIA_DOOR && type != Material.BIRCH_DOOR_ITEM && type != Material.BREWING_STAND
                && type != Material.BURNING_FURNACE && type != Material.CAKE_BLOCK && type != Material.CAULDRON && type != Material.COMMAND && type != Material.COMMAND_MINECART && type != Material.DARK_OAK_DOOR
                && type != Material.DIODE_BLOCK_OFF && type != Material.DIODE_BLOCK_ON && type != Material.ENDER_PORTAL && type != Material.ENDER_PORTAL_FRAME && type != Material.FLOWER_POT && type != Material.GLOWING_REDSTONE_ORE
                && type != Material.POWERED_MINECART && type != Material.HOPPER_MINECART && type != Material.PISTON_EXTENSION && type != Material.PISTON_MOVING_PIECE && type != Material.PORTAL && type != Material.SKULL && type != Material.STATIONARY_LAVA
                && type != Material.STATIONARY_WATER && type != Material.SUGAR_CANE_BLOCK && type != Material.WATER && type != Material.LAVA && type != Material.BARRIER && type != Material.BED && type != Material.BED_BLOCK && type != Material.MOB_SPAWNER
                && type != Material.MONSTER_EGG && type != Material.MONSTER_EGGS && type != Material.WRITTEN_BOOK && type != Material.EXPLOSIVE_MINECART && type != Material.SOIL;
    }

    private boolean isRarity(Material type) {
        return type == Material.ANVIL || type == Material.BEACON || type == Material.BREWING_STAND_ITEM || type == Material.CHAINMAIL_BOOTS || type == Material.CHAINMAIL_CHESTPLATE || type == Material.CHAINMAIL_LEGGINGS || type == Material.CHAINMAIL_HELMET || type == Material.LEATHER_BOOTS || type == Material.LEATHER_CHESTPLATE || type == Material.LEATHER_LEGGINGS || type == Material.LEATHER_HELMET
                || type == Material.DIAMOND || type == Material.DIAMOND_AXE || type == Material.DIAMOND_BARDING || type == Material.DIAMOND_BLOCK || type == Material.DIAMOND_BOOTS || type == Material.DIAMOND_CHESTPLATE || type == Material.DIAMOND_HELMET || type == Material.DIAMOND_HOE || type == Material.DIAMOND_LEGGINGS || type == Material.DIAMOND_ORE || type == Material.DIAMOND_PICKAXE || type == Material.DIAMOND_SPADE || type == Material.DIAMOND_SWORD
                || type == Material.IRON_INGOT || type == Material.IRON_AXE || type == Material.IRON_BARDING || type == Material.IRON_BLOCK || type == Material.IRON_BOOTS || type == Material.IRON_CHESTPLATE || type == Material.IRON_HELMET || type == Material.IRON_HOE || type == Material.IRON_LEGGINGS || type == Material.IRON_ORE || type == Material.IRON_PICKAXE || type == Material.IRON_SPADE || type == Material.IRON_SWORD
                || type == Material.GOLD_INGOT || type == Material.GOLD_AXE || type == Material.GOLD_BARDING || type == Material.GOLD_BLOCK || type == Material.GOLD_BOOTS || type == Material.GOLD_CHESTPLATE || type == Material.GOLD_HELMET || type == Material.GOLD_HOE || type == Material.GOLD_LEGGINGS || type == Material.GOLD_ORE || type == Material.GOLD_PICKAXE || type == Material.GOLD_SPADE || type == Material.GOLD_SWORD
                || type == Material.DRAGON_EGG || type == Material.ENDER_PEARL || type == Material.FISHING_ROD || type == Material.LAVA_BUCKET || type == Material.WATER_BUCKET || type == Material.ENCHANTED_BOOK || type == Material.ENCHANTMENT_TABLE || type == Material.RECORD_10 || type == Material.RECORD_11 || type == Material.RECORD_12 || type == Material.RECORD_3 || type == Material.RECORD_4 || type == Material.RECORD_5 || type == Material.RECORD_6 || type == Material.RECORD_7 || type == Material.RECORD_8
                || type == Material.RECORD_9 || type == Material.FLINT_AND_STEEL || type == Material.CAKE || type == Material.STONE_PICKAXE || type == Material.STONE_SPADE || type == Material.STONE_SWORD || type == Material.STONE_HOE || type == Material.STONE_AXE || type == Material.WOOD_PICKAXE || type == Material.WOOD_SPADE || type == Material.WOOD_SWORD || type == Material.WOOD_HOE || type == Material.WOOD_AXE || type == Material.RABBIT_STEW || type == Material.MUSHROOM_SOUP || type == Material.CARROT_STICK
                || type == Material.MILK_BUCKET || type == Material.POTION || type == Material.GOLDEN_APPLE || type == Material.SADDLE || type == Material.BONE || type == Material.INK_SACK || type == Material.BOW || type == Material.BOWL;
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        if(UHCManager.get().getTeam_size() > 1){
            UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
            return;
        }
        uhcPlayer.getPlayer().teleport(location);
    }
}

