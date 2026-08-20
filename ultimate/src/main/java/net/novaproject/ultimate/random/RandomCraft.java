package net.novaproject.ultimate.random;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.*;

public class RandomCraft extends Scenario {

    private static final Material[]    ALL_MATERIALS    = Material.values();
    private static final Enchantment[] ALL_ENCHANTMENTS = Enchantment.values();

    private static final EnumSet<Material> ALWAYS_BLOCKED = EnumSet.of(
            Material.AIR, Material.BEDROCK, Material.COMMAND,
            Material.BARRIER, Material.PORTAL,
            Material.ENDER_PORTAL, Material.ENDER_PORTAL_FRAME,
            Material.WATER, Material.LAVA,
            Material.STATIONARY_WATER, Material.STATIONARY_LAVA
    );

    private static final EnumSet<Material> RARE_MATERIALS = EnumSet.of(
            Material.DIAMOND, Material.DIAMOND_BLOCK,
            Material.GOLDEN_APPLE, Material.ENCHANTED_BOOK,
            Material.BEACON, Material.DRAGON_EGG
    );

    @Var(name = "§eGive starter kit", desc = "§7Give crafting table, furnace and tools at start.", type = VariableType.BOOLEAN)
    private boolean giveStarterKit = true;

    @Var(name = "§eAllow rare items", desc = "§7Allow rare items like enchanted book.", type = VariableType.BOOLEAN)
    private boolean allowRareItems = true;

    @Var(name = "§eMax ingot amount", desc = "§7Amount given if ingot generated.", type = VariableType.INTEGER)
    private int maxIngotAmount = 16;

    @Var(name = "§eGolden apple amount", desc = "§7Amount of golden apples if generated.", type = VariableType.INTEGER)
    private int goldenAppleAmount = 3;

    @Var(name = "§eMax enchant level", desc = "§7Maximum level for enchanted books.", type = VariableType.INTEGER)
    private int maxEnchantLevel = 3;

    @Var(name = "§eAllow duplicates", desc = "§7Allow the same item to be generated multiple times.", type = VariableType.BOOLEAN)
    private boolean allowDuplicateDrops = false;

    private final Map<Material, ItemStack> cache = new HashMap<>();
    private final Random random = new Random();

    private List<Material> materialPool = null;

    @Override
    public String getName() { return "RandomCraft"; }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.RANDOMCRAFT, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.WORKBENCH);
    }

    @Override
    public void onStart(Player player) {
        if (!giveStarterKit) return;
        player.getInventory().addItem(new ItemStack(Material.WORKBENCH, 64));
        player.getInventory().addItem(new ItemStack(Material.FURNACE, 64));
        player.getInventory().addItem(new ItemStack(Material.IRON_PICKAXE));
        player.getInventory().addItem(new ItemStack(Material.IRON_AXE));
        player.getInventory().addItem(new ItemStack(Material.IRON_SPADE));
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
    public void onFurnace(ItemStack result, FurnaceSmeltEvent event) {
        if (result == null) return;
        event.setResult(getRandomized(result.getType()));
    }

    @Override
    public void onCraft(ItemStack result, CraftItemEvent event) {
        if (result == null) return;
        event.getInventory().setResult(getRandomized(result.getType()));
    }

    private ItemStack getRandomized(Material original) {
        return cache.computeIfAbsent(original, k -> generateRandomItem());
    }

    private void initPoolIfNeeded() {
        if (materialPool != null) return;

        materialPool = new ArrayList<>();
        for (Material m : ALL_MATERIALS) {
            if (isAcceptedMaterial(m)) materialPool.add(m);
        }
        Collections.shuffle(materialPool, random);
    }

    private ItemStack generateRandomItem() {
        initPoolIfNeeded();

        if (materialPool.isEmpty()) return null;

        Material chosen;
        if (!allowDuplicateDrops) {
            chosen = materialPool.remove(materialPool.size() - 1);
        } else {
            chosen = materialPool.get(random.nextInt(materialPool.size()));
        }

        return buildItem(chosen);
    }

    private ItemStack buildItem(Material chosen) {
        int amount = 1;
        if (chosen == Material.GOLDEN_APPLE)                                    amount = goldenAppleAmount;
        if (chosen == Material.IRON_INGOT || chosen == Material.GOLD_INGOT)     amount = maxIngotAmount;

        ItemStack item = new ItemStack(chosen, Math.max(1, amount));

        if (chosen == Material.ENCHANTED_BOOK && allowRareItems) {
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
        if (!allowRareItems && RARE_MATERIALS.contains(type)) return false;
        return true;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

}

