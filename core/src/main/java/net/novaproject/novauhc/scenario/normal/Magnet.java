package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.utils.VariableType;
import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.scenario.lang.lang.MagnetLang;
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

    @ScenarioVariable(
            name = "Magnet Radius",
            description = "Rayon en blocs dans lequel les minerais sont attirés.",
            type = VariableType.INTEGER
    )
    private int magnetRadius = 5;

    @Override
    public String getName() {
        return "Magnet";
    }

    @Override
    public String getDescription() {
        return "Les minerais dans un rayon de " + magnetRadius + " blocs viennent automatiquement à vous.";
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
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                    net.novaproject.novauhc.Main.get(),
                    () -> attractNearbyOres(player, block.getLocation()),
                    1L
            );
        }
    }

    private void attractNearbyOres(Player player, Location centerLocation) {
        int oresAttracted = 0;
        for (int x = -magnetRadius; x <= magnetRadius; x++) {
            for (int y = -magnetRadius; y <= magnetRadius; y++) {
                for (int z = -magnetRadius; z <= magnetRadius; z++) {
                    Location checkLocation = centerLocation.clone().add(x, y, z);
                    Block checkBlock = checkLocation.getBlock();

                    if (oreTypes.contains(checkBlock.getType())) {
                        Material oreType = checkBlock.getType();
                        ItemStack[] drops = getOreDrops(oreType);
                        checkBlock.setType(Material.AIR);

                        for (ItemStack drop : drops) {
                            if (player.getInventory().firstEmpty() != -1) {
                                player.getInventory().addItem(drop);
                            } else {
                                player.getWorld().dropItemNaturally(player.getLocation(), drop);
                            }
                        }

                        oresAttracted++;
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
            case COAL_ORE -> { return new ItemStack[]{new ItemStack(Material.COAL, 1)}; }
            case IRON_ORE -> { return new ItemStack[]{new ItemStack(Material.IRON_INGOT, 1)}; }
            case GOLD_ORE -> { return new ItemStack[]{new ItemStack(Material.GOLD_INGOT, 1)}; }
            case DIAMOND_ORE -> { return new ItemStack[]{new ItemStack(Material.DIAMOND, 1)}; }
            case EMERALD_ORE -> { return new ItemStack[]{new ItemStack(Material.EMERALD, 1)}; }
            case LAPIS_ORE -> {
                ItemStack lapis = new ItemStack(Material.INK_SACK, 4 + (int)(Math.random() * 5));
                lapis.setDurability((short) 4);
                return new ItemStack[]{lapis};
            }
            case REDSTONE_ORE -> { return new ItemStack[]{new ItemStack(Material.REDSTONE, 4 + (int)(Math.random() * 2))}; }
            default -> { return new ItemStack[]{new ItemStack(Material.COBBLESTONE, 1)}; }
        }
    }

    private void createMagnetEffect(Location from, Location to) {
        if (to.getWorld() != null) {
            to.getWorld().playSound(to, org.bukkit.Sound.ITEM_PICKUP, 0.3f, 1.5f);
        }
    }

    public boolean isWithinMagnetRange(Location center, Location target) {
        return center.distance(target) <= magnetRadius;
    }

    public List<Block> getOresInRange(Location center) {
        List<Block> oresInRange = new java.util.ArrayList<>();
        for (int x = -magnetRadius; x <= magnetRadius; x++) {
            for (int y = -magnetRadius; y <= magnetRadius; y++) {
                for (int z = -magnetRadius; z <= magnetRadius; z++) {
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

}
