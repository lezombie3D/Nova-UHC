package net.novaproject.ultimate.random;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.*;

public class RandomDrop extends Scenario {

    private static final Material[]    ALL_MATERIALS    = Material.values();
    private static final Enchantment[] ALL_ENCHANTMENTS = Enchantment.values();

    private static final EnumSet<Material> DIRECTIONABLE = EnumSet.of(
            Material.FENCE_GATE, Material.FURNACE, Material.TRAP_DOOR,
            Material.TRAPPED_CHEST, Material.CHEST, Material.DROPPER,
            Material.HOPPER, Material.SIGN
    );

    private static final EnumSet<Material> ALWAYS_BLOCKED = EnumSet.of(
            Material.AIR, Material.BEDROCK, Material.COMMAND,
            Material.PORTAL, Material.ENDER_PORTAL, Material.ENDER_PORTAL_FRAME,
            Material.BARRIER, Material.MOB_SPAWNER,
            Material.WATER, Material.LAVA,
            Material.STATIONARY_WATER, Material.STATIONARY_LAVA
    );

    private static final EnumSet<Material> RARE_MATERIALS = EnumSet.of(
            Material.DIAMOND, Material.DIAMOND_BLOCK,
            Material.GOLDEN_APPLE, Material.ENCHANTED_BOOK,
            Material.BEACON, Material.DRAGON_EGG
    );

    @Var(name = "§eGive starter kit", desc = "§7Give crafting table and furnace at start.", type = VariableType.BOOLEAN)
    private boolean giveStarter = true;

    @Var(name = "§eAllow rare items", desc = "§7Allow rare items in drops.", type = VariableType.BOOLEAN)
    private boolean allowRare = true;

    @Var(name = "§eMax ingot amount", desc = "§7Amount given if ingot generated.", type = VariableType.INTEGER)
    private int maxIngotAmount = 16;

    @Var(name = "§eGolden apple amount", desc = "§7Amount of golden apples if generated.", type = VariableType.INTEGER)
    private int gappleAmount = 3;

    @Var(name = "§eMax enchant level", desc = "§7Maximum level for enchanted books.", type = VariableType.INTEGER)
    private int maxEnchantLevel = 3;

    @Var(name = "§eAllow duplicates", desc = "§7Allow the same item to be generated multiple times.", type = VariableType.BOOLEAN)
    private boolean allowDuplicate = false;

    private final Map<String, ItemStack> cacheBlock = new HashMap<>();
    private final Random random = new Random();

    private List<Material> materialPool = null;

    @Override
    public String getName() { return "RandomDrop"; }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.RANDOMDROP, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.ROTTEN_FLESH);
    }

    @Override
    public void onStart(Player player) {
        if (!giveStarter) return;
        player.getInventory().addItem(new ItemStack(Material.WORKBENCH, 64));
        player.getInventory().addItem(new ItemStack(Material.FURNACE, 64));
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        if(UHCManager.get().getTeam_size() > 1){
            UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
            return;
        }
        uhcPlayer.getPlayer().teleport(location);
    }

    @Override
    public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
        if (event.getDrops().isEmpty()) return;

        Location loc = entity.getLocation();

        for (ItemStack drop : event.getDrops()) {
            String key = drop.getType().name() + ':' + drop.getDurability();
            ItemStack result = cacheBlock.computeIfAbsent(key, k -> generateDrop());
            if (result != null) entity.getWorld().dropItem(loc, result.clone());
        }

        event.getDrops().clear();
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (block.getDrops().isEmpty()) return;

        event.setCancelled(true);

        String key = block.getType().name() + ':' + (DIRECTIONABLE.contains(block.getType()) ? 0 : block.getData());
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);

        ItemStack result = cacheBlock.computeIfAbsent(key, k -> generateDrop());
        if (result != null) block.getWorld().dropItem(loc, result.clone());

        block.setType(Material.AIR);
    }

    private void initPoolIfNeeded() {
        if (materialPool != null) return;

        materialPool = new ArrayList<>();
        for (Material m : ALL_MATERIALS) {
            if (isAcceptedMaterial(m)) materialPool.add(m);
        }
        Collections.shuffle(materialPool, random);
    }

    private ItemStack generateDrop() {
        initPoolIfNeeded();

        if (materialPool.isEmpty()) return null;

        Material material;
        if (!allowDuplicate) {

            int idx = materialPool.size() - 1;
            material = materialPool.remove(idx);
        } else {
            material = materialPool.get(random.nextInt(materialPool.size()));
        }

        return buildItem(material);
    }

    private ItemStack buildItem(Material material) {
        int amount = 1;
        if (material == Material.GOLDEN_APPLE)                                  amount = gappleAmount;
        if (material == Material.IRON_INGOT || material == Material.GOLD_INGOT) amount = maxIngotAmount;

        ItemStack item = new ItemStack(material, Math.max(1, amount));

        if (material == Material.ENCHANTED_BOOK && allowRare) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            Enchantment enchant = ALL_ENCHANTMENTS[random.nextInt(ALL_ENCHANTMENTS.length)];
            int level = random.nextInt(Math.max(1, maxEnchantLevel)) + 1;
            meta.addStoredEnchant(enchant, level, true);
            item.setItemMeta(meta);
        }

        return item;
    }

    private boolean isAcceptedMaterial(Material type) {
        if (type == null) return false;
        if (ALWAYS_BLOCKED.contains(type)) return false;
        if (!allowRare && RARE_MATERIALS.contains(type)) return false;
        return true;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}

