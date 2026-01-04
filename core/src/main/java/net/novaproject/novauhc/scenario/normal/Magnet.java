package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.scenario.lang.MagnetLang;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class Magnet extends Scenario {

    private final List<Material> oreTypes = Arrays.asList(
            Material.COAL_ORE,
            Material.IRON_ORE,
            Material.GOLD_ORE,
            Material.DIAMOND_ORE,
            Material.EMERALD_ORE,
            Material.LAPIS_ORE,
            Material.REDSTONE_ORE
    );

    private final int MAGNET_RADIUS = 5;

    @Override
    public String getName() {
        return "Magnet";
    }

    @Override
    public String getDescription() {
        return "Les minerais dans un rayon de 5 blocs viennent automatiquement à vous.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.IRON_INGOT);
    }

    @Override
    public String getPath() {
        return "magnet";
    }

    @Override
    public ScenarioLang[] getLang() {
        return MagnetLang.values();
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (!isActive()) return;

        Material blockType = block.getType();

        if (oreTypes.contains(blockType)) {
            // Let the original break happen first
            // Then find and attract nearby ores

            // Use a delayed task to ensure the block is broken first
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                    net.novaproject.novauhc.Main.get(),
                    () -> attractNearbyOres(player, block.getLocation()),
                    1L
            );
        }
    }

    private void attractNearbyOres(Player player, Location centerLocation) {
        int oresAttracted = 0;

        // Search in a cube around the broken block
        int magnetRadius = (int) getConfig().getDouble("magnet_radius", 5.0);
        for (int x = -magnetRadius; x <= magnetRadius; x++) {
            for (int y = -magnetRadius; y <= magnetRadius; y++) {
                for (int z = -magnetRadius; z <= magnetRadius; z++) {
                    Location checkLocation = centerLocation.clone().add(x, y, z);
                    Block checkBlock = checkLocation.getBlock();

                    if (oreTypes.contains(checkBlock.getType())) {
                        // Break this ore and give items to player
                        Material oreType = checkBlock.getType();

                        // Get the drops that would normally occur
                        ItemStack[] drops = getOreDrops(oreType);

                        // Break the block
                        checkBlock.setType(Material.AIR);

                        // Add items directly to player inventory or drop at their feet
                        for (ItemStack drop : drops) {
                            if (player.getInventory().firstEmpty() != -1) {
                                player.getInventory().addItem(drop);
                            } else {
                                player.getWorld().dropItemNaturally(player.getLocation(), drop);
                            }
                        }

                        oresAttracted++;

                        // Create visual effect (optional)
                        createMagnetEffect(checkLocation, player.getLocation());
                    }
                }
            }
        }

        if (oresAttracted > 0) {
            player.sendMessage("§b[Magnet] §f" + oresAttracted + " minerai(s) attiré(s) !");
        }
    }

    private ItemStack[] getOreDrops(Material oreType) {
        switch (oreType) {
            case COAL_ORE:
                return new ItemStack[]{new ItemStack(Material.COAL, 1)};
            case IRON_ORE:
                return new ItemStack[]{new ItemStack(Material.IRON_INGOT, 1)};
            case GOLD_ORE:
                return new ItemStack[]{new ItemStack(Material.GOLD_INGOT, 1)};
            case DIAMOND_ORE:
                return new ItemStack[]{new ItemStack(Material.DIAMOND, 1)};
            case EMERALD_ORE:
                return new ItemStack[]{new ItemStack(Material.EMERALD, 1)};
            case LAPIS_ORE:
                ItemStack lapis = new ItemStack(Material.INK_SACK, 4 + (int) (Math.random() * 5));
                lapis.setDurability((short) 4); // Lapis Lazuli data value
                return new ItemStack[]{lapis};
            case REDSTONE_ORE:
                return new ItemStack[]{new ItemStack(Material.REDSTONE, 4 + (int) (Math.random() * 2))};
            default:
                return new ItemStack[]{new ItemStack(Material.COBBLESTONE, 1)};
        }
    }

    private void createMagnetEffect(Location from, Location to) {
        // Create a visual effect showing the ore being attracted
        // This could be particles, but for simplicity we'll just use a message

        // Calculate distance for effect intensity
        double distance = from.distance(to);

        // You could add particle effects here if desired
        // For now, we'll just create a subtle sound effect
        if (to.getWorld() != null) {
            to.getWorld().playSound(to, org.bukkit.Sound.ITEM_PICKUP, 0.3f, 1.5f);
        }
    }

    // Check if a location is within magnet range of another location
    public boolean isWithinMagnetRange(Location center, Location target) {
        return center.distance(target) <= MAGNET_RADIUS;
    }

    // Get all ore blocks within magnet range of a location
    public List<Block> getOresInRange(Location center) {
        List<Block> oresInRange = new java.util.ArrayList<>();

        for (int x = -MAGNET_RADIUS; x <= MAGNET_RADIUS; x++) {
            for (int y = -MAGNET_RADIUS; y <= MAGNET_RADIUS; y++) {
                for (int z = -MAGNET_RADIUS; z <= MAGNET_RADIUS; z++) {
                    Location checkLocation = center.clone().add(x, y, z);
                    Block checkBlock = checkLocation.getBlock();

                    if (oreTypes.contains(checkBlock.getType())) {
                        oresInRange.add(checkBlock);
                    }
                }
            }
        }

        return oresInRange;
    }

    // Get current magnet radius
    public int getMagnetRadius() {
        return MAGNET_RADIUS;
    }

    // Admin command to change magnet radius
    public void setMagnetRadius(int newRadius) {
        // This would require making MAGNET_RADIUS non-final and adding proper validation
        // For now, it's fixed at 5 blocks
    }
}
