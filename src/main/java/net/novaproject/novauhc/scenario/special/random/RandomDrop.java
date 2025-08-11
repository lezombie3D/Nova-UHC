package net.novaproject.novauhc.scenario.special.random;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.*;

public class RandomDrop extends Scenario {
    private final Map<String, ItemStack> cacheblock = new HashMap<>();
    private final List<ItemStack> alreadyused = new ArrayList<>();

    @Override
    public String getName() {
        return "RandomDrop";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.ROTTEN_FLESH);
    }

    @Override
    public void onStart(Player player) {
        player.getInventory().addItem(new ItemStack(Material.WORKBENCH, 64));
        player.getInventory().addItem(new ItemStack(Material.FURNACE, 64));
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        if (UHCManager.get().getTeam_size() != 1) {
            UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
        } else {
            uhcPlayer.getPlayer().teleport(location);
        }
    }

    @Override
    public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
        if (event.getDrops().isEmpty())
            return;
        for (ItemStack itemStack : event.getDrops()) {
            String itemtostring = itemStack.getType() + ":" + itemStack.getData();
            if (cacheblock.get(itemtostring) == null) {
                checkDrop(itemtostring);
                event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), cacheblock.get(itemtostring));
            } else {
                event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), cacheblock.get(itemtostring));
            }
            itemStack.setType(Material.AIR);
        }
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (block.getDrops().isEmpty())
            return;
        event.setCancelled(true);
        String itemtostring = block.getType() + ":" + block.getData();
        if (isDirectionable(block.getType())) {
            itemtostring = block.getType() + ":0";
        }
        Location loc = new Location(block.getWorld(), block.getLocation().getBlockX() + 0.5D, block.getLocation().getBlockY() + 0.5D, block.getLocation().getBlockZ() + 0.5D);

        if (cacheblock.get(itemtostring) == null) {
            checkDrop(itemtostring);
            block.getWorld().dropItem(loc, cacheblock.get(itemtostring));
        } else {
            block.getWorld().dropItem(loc, cacheblock.get(itemtostring));
        }
        block.setType(Material.AIR);
    }

    private void checkDrop(String b) {

        Material randomMaterial = Material.values()[new Random().nextInt(Material.values().length)];
        if (isAcceptedMaterial(randomMaterial)) {

            if (isRarity(randomMaterial)) {
                int amount = 1;
                if (randomMaterial == Material.GOLDEN_APPLE)
                    amount = 3;
                if (randomMaterial == Material.IRON_INGOT || randomMaterial == Material.GOLD_INGOT)
                    amount = 16;
                ItemStack item = new ItemStack(randomMaterial, amount);


                if (randomMaterial == Material.ENCHANTED_BOOK) {
                    EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
                    Enchantment randomEnchant = Enchantment.values()[new Random().nextInt(Enchantment.values().length)];
                    int randomLevel = new Random().nextInt(3);
                    meta.addStoredEnchant(randomEnchant, randomLevel + 1, true);
                    item.setItemMeta(meta);
                }
                alreadyused.add(item);
                cacheblock.put(b, item);
            } else {
                ItemStack item = new ItemCreator(randomMaterial).getItemstack();
                alreadyused.add(item);
                cacheblock.put(b, item);
            }
        } else {
            checkDrop(b);
        }
    }

    private boolean isDirectionable(Material type) {
        if (type == null)
            return false;

        return type == Material.FENCE_GATE || type == Material.FURNACE || type == Material.TRAP_DOOR || type == Material.TRAPPED_CHEST || type == Material.CHEST || type == Material.DROPPER || type == Material.HOPPER || type == Material.SIGN;
    }

    private boolean isAcceptedMaterial(Material type) {
        if (type == null || alreadyused.contains(type)) {
            return false;
        }
        return type != Material.AIR && type != Material.ARMOR_STAND && type != Material.BEDROCK && type != Material.ACACIA_DOOR && type != Material.BIRCH_DOOR_ITEM && type != Material.BREWING_STAND
                && type != Material.BURNING_FURNACE && type != Material.CAKE_BLOCK && type != Material.CAULDRON && type != Material.COMMAND && type != Material.COMMAND_MINECART && type != Material.DARK_OAK_DOOR
                && type != Material.DIODE_BLOCK_OFF && type != Material.DIODE_BLOCK_ON && type != Material.ENDER_PORTAL && type != Material.ENDER_PORTAL_FRAME && type != Material.FLOWER_POT && type != Material.GLOWING_REDSTONE_ORE
                && type != Material.POWERED_MINECART && type != Material.HOPPER_MINECART && type != Material.PISTON_EXTENSION && type != Material.PISTON_MOVING_PIECE && type != Material.PORTAL && type != Material.SKULL && type != Material.STATIONARY_LAVA
                && type != Material.STATIONARY_WATER && type != Material.SUGAR_CANE_BLOCK && type != Material.WATER && type != Material.LAVA && type != Material.BARRIER && type != Material.BED && type != Material.BED_BLOCK && type != Material.MOB_SPAWNER
                && type != Material.MONSTER_EGG && type != Material.MONSTER_EGGS && type != Material.WRITTEN_BOOK && type != Material.EXPLOSIVE_MINECART && type != Material.SOIL;
    }

    private boolean isRarity(Material type) {
        if (alreadyused.contains(type)) {
            return false;
        }
        return type == Material.ANVIL || type == Material.BEACON || type == Material.BREWING_STAND_ITEM || type == Material.CHAINMAIL_BOOTS || type == Material.CHAINMAIL_CHESTPLATE || type == Material.CHAINMAIL_LEGGINGS || type == Material.CHAINMAIL_HELMET || type == Material.LEATHER_BOOTS || type == Material.LEATHER_CHESTPLATE || type == Material.LEATHER_LEGGINGS || type == Material.LEATHER_HELMET || type == Material.DIAMOND || type == Material.DIAMOND_AXE || type == Material.DIAMOND_BARDING || type == Material.DIAMOND_BLOCK || type == Material.DIAMOND_BOOTS || type == Material.DIAMOND_CHESTPLATE || type == Material.DIAMOND_HELMET || type == Material.DIAMOND_HOE || type == Material.DIAMOND_LEGGINGS || type == Material.DIAMOND_ORE || type == Material.DIAMOND_PICKAXE || type == Material.DIAMOND_SPADE || type == Material.DIAMOND_SWORD || type == Material.IRON_INGOT || type == Material.IRON_AXE || type == Material.IRON_BARDING || type == Material.IRON_BLOCK || type == Material.IRON_BOOTS || type == Material.IRON_CHESTPLATE || type == Material.IRON_HELMET || type == Material.IRON_HOE || type == Material.IRON_LEGGINGS || type == Material.IRON_ORE || type == Material.IRON_PICKAXE || type == Material.IRON_SPADE || type == Material.IRON_SWORD || type == Material.GOLD_INGOT || type == Material.GOLD_AXE || type == Material.GOLD_BARDING || type == Material.GOLD_BLOCK || type == Material.GOLD_BOOTS || type == Material.GOLD_CHESTPLATE || type == Material.GOLD_HELMET || type == Material.GOLD_HOE || type == Material.GOLD_LEGGINGS || type == Material.GOLD_ORE || type == Material.GOLD_PICKAXE || type == Material.GOLD_SPADE || type == Material.GOLD_SWORD || type == Material.DRAGON_EGG || type == Material.ENDER_PEARL || type == Material.FISHING_ROD || type == Material.LAVA_BUCKET || type == Material.WATER_BUCKET || type == Material.ENCHANTED_BOOK || type == Material.ENCHANTMENT_TABLE || type == Material.RECORD_10 || type == Material.RECORD_11 || type == Material.RECORD_12 || type == Material.RECORD_3 || type == Material.RECORD_4 || type == Material.RECORD_5 || type == Material.RECORD_6 || type == Material.RECORD_7 || type == Material.RECORD_8 || type == Material.RECORD_9 || type == Material.FLINT_AND_STEEL || type == Material.CAKE || type == Material.STONE_PICKAXE || type == Material.STONE_SPADE || type == Material.STONE_SWORD || type == Material.STONE_HOE || type == Material.STONE_AXE || type == Material.WOOD_PICKAXE || type == Material.WOOD_SPADE || type == Material.WOOD_SWORD || type == Material.WOOD_HOE || type == Material.WOOD_AXE || type == Material.RABBIT_STEW || type == Material.MUSHROOM_SOUP || type == Material.CARROT_STICK || type == Material.MILK_BUCKET || type == Material.POTION || type == Material.GOLDEN_APPLE || type == Material.SADDLE || type == Material.BONE || type == Material.INK_SACK || type == Material.BOW || type == Material.BOWL;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}
