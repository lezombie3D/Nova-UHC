package net.novaproject.scenarioplus.craft;

import net.novaproject.novauhc.utils.UHCUtils;
import net.minecraft.server.v1_8_R3.ContainerEnchantTable;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.normal.MiningScenarios;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Furnace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class EnchantScenarios {

    private EnchantScenarios() {
    }

    public static class ExperiencedCrafter extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.experiencedcrafter.desc",
                "§7Chaque craft coûte des niveaux d'expérience, selon l'objet fabriqué.");
        private static final DynamicLang PAID = DynamicLang.of("scenario.experiencedcrafter.paid",
                "§7Ce craft t'a coûté §e%cost% §7niveau(x).");
        private static final DynamicLang TOO_POOR = DynamicLang.of("scenario.experiencedcrafter.toopoor",
                "§cIl te faut §e%cost% §cniveau(x) pour fabriquer cet objet.");

        @Var(name = "Coût minimum", desc = "Coût minimum d'un craft en niveaux d'expérience.", type = VariableType.INTEGER, min = 0)
        private int minCost = 1;

        @Var(name = "Coût maximum", desc = "Coût maximum d'un craft en niveaux d'expérience.", type = VariableType.INTEGER, min = 0)
        private int maxCost = 10;

        @Override public String getName() { return "Experienced Crafter"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.EXP_BOTTLE); }

        @Override public Family getFamily() { return Family.CRAFT; }

        @Override
        public void onCraft(ItemStack result, CraftItemEvent event) {
            if (!isActive()) return;
            if (result == null) return;
            if (!(event.getWhoClicked() instanceof Player player)) return;

            int cost = costOf(result.getType());
            if (cost <= 0) return;

            if (player.getLevel() < cost) {
                event.setCancelled(true);
                player.updateInventory();
                player.sendMessage(t(TOO_POOR, player, Map.of("%cost%", cost)));
                return;
            }

            player.setLevel(player.getLevel() - cost);
            player.sendMessage(t(PAID, player, Map.of("%cost%", cost)));
        }

        private int costOf(Material material) {
            int low = Math.min(minCost, maxCost);
            int span = Math.abs(maxCost - minCost) + 1;
            return low + Math.floorMod(material.name().hashCode(), span);
        }
    }

    public static class FastPotion extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.fastpotion.desc",
                "§7Le brassage des potions est accéléré.");

        @Var(name = "Multiplicateur de vitesse", desc = "Multiplicateur de vitesse du brassage des potions.", type = VariableType.INTEGER, min = 1)
        private int speedMultiplier = 2;

        private final Set<Location> stands = new HashSet<>();
        private BukkitRunnable task;

        @Override public String getName() { return "FastPotion"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BREWING_STAND_ITEM); }

        @Override public Family getFamily() { return Family.CRAFT; }

        @Override
        public void onPlayerInteract(Player player, PlayerInteractEvent event) {
            if (!isActive()) return;
            Block block = event.getClickedBlock();
            if (block == null || block.getType() != Material.BREWING_STAND) return;
            stands.add(block.getLocation());
        }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    accelerate();
                }
            };
            task.runTaskTimer(Main.get(), 1L, 1L);
        }

        private void accelerate() {
            int gain = speedMultiplier - 1;
            if (gain <= 0) return;
            Iterator<Location> iterator = stands.iterator();
            while (iterator.hasNext()) {
                Block block = iterator.next().getBlock();
                if (block.getType() != Material.BREWING_STAND) {
                    iterator.remove();
                    continue;
                }
                BrewingStand stand = (BrewingStand) block.getState();
                int remaining = stand.getBrewingTime();
                if (remaining > 1) stand.setBrewingTime(Math.max(1, remaining - gain));
            }
        }

        @Override
        public void onStop() {
            if (task != null) {
                task.cancel();
                task = null;
            }
            stands.clear();
        }
    }

    public static class GoldenPearl extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.goldenpearl.desc",
                "§7Une perle d'ender entourée de 8 lingots d'or donne une perle dorée qui ne blesse pas.");
        private static final DynamicLang ITEM_NAME = DynamicLang.of("scenario.goldenpearl.item",
                "§6Perle dorée");

        private static final String THROWN_MARK = "novauhc-golden-pearl";

        @Var(name = "Immunité à la chute", desc = "Durée d'immunité aux dégâts de chute après l'atterrissage de la perle dorée, en ticks.", type = VariableType.INTEGER, min = 0)
        private int fallImmunityTicks = 5;

        private final Set<UUID> throwing = new HashSet<>();
        private final Set<UUID> immune = new HashSet<>();

        @Override public String getName() { return "Golden Pearl"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ENDER_PEARL); }

        @Override public Family getFamily() { return Family.CRAFT; }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            ShapedRecipe recipe = new ShapedRecipe(goldenPearl());
            recipe.shape("GGG", "GPG", "GGG");
            recipe.setIngredient('G', Material.GOLD_INGOT);
            recipe.setIngredient('P', Material.ENDER_PEARL);
            Bukkit.addRecipe(recipe);
        }

        @Override
        public void onPlayerInteract(Player player, PlayerInteractEvent event) {
            if (!isActive()) return;
            Action action = event.getAction();
            if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
            if (!isGoldenPearl(event.getItem())) return;
            UUID uuid = player.getUniqueId();
            throwing.add(uuid);
            Bukkit.getScheduler().runTask(Main.get(), () -> throwing.remove(uuid));
        }

        @EventHandler
        public void onLaunch(ProjectileLaunchEvent event) {
            if (!isRunning()) return;
            if (!(event.getEntity() instanceof EnderPearl pearl)) return;
            if (!(pearl.getShooter() instanceof Player shooter)) return;
            if (!throwing.remove(shooter.getUniqueId())) return;
            pearl.setMetadata(THROWN_MARK, new FixedMetadataValue(Main.get(), true));
        }

        @Override
        public void onProjectileHit(ProjectileHitEvent event) {
            if (!isActive()) return;
            if (!(event.getEntity() instanceof EnderPearl pearl)) return;
            if (!pearl.hasMetadata(THROWN_MARK)) return;
            if (!(pearl.getShooter() instanceof Player shooter)) return;
            UUID uuid = shooter.getUniqueId();
            immune.add(uuid);
            Bukkit.getScheduler().runTaskLater(Main.get(), () -> immune.remove(uuid), fallImmunityTicks);
        }

        @Override
        public void onDamage(Player player, EntityDamageEvent event) {
            if (!isActive()) return;
            if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
            if (immune.remove(player.getUniqueId())) event.setCancelled(true);
        }

        @Override
        public void onStop() {
            throwing.clear();
            immune.clear();
            Iterator<Recipe> iterator = Bukkit.recipeIterator();
            while (iterator.hasNext()) {
                if (isGoldenPearl(iterator.next().getResult())) iterator.remove();
            }
        }

        private ItemStack goldenPearl() {
            return new ItemCreator(Material.ENDER_PEARL).setName(t(ITEM_NAME)).getItemstack();
        }

        private boolean isGoldenPearl(ItemStack item) {
            if (item == null || item.getType() != Material.ENDER_PEARL || !item.hasItemMeta()) return false;
            ItemMeta meta = item.getItemMeta();
            return meta.hasDisplayName() && meta.getDisplayName().equals(t(ITEM_NAME));
        }
    }

    public static class MasterLevel extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.masterlevel.desc",
                "§7Tout le monde démarre avec un stock de niveaux et d'enclumes.");

        @Var(name = "Niveaux offerts", desc = "Niveaux d'expérience donnés au départ.", type = VariableType.INTEGER, min = 0)
        private int levels = 10000;

        @Var(name = "Enclumes", desc = "Nombre d'enclumes données au départ.", type = VariableType.INTEGER, min = 0)
        private int anvils = 64;

        @Override public String getName() { return "Master Level"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ANVIL); }

        @Override public Family getFamily() { return Family.CRAFT; }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            player.setLevel(levels);
            if (anvils <= 0) return;
            for (ItemStack overflow : player.getInventory().addItem(new ItemStack(Material.ANVIL, anvils)).values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), overflow);
            }
        }
    }

    public static class NoEnchantPreview extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.noenchantpreview.desc",
                "§7La table d'enchantement n'affiche plus l'aperçu de l'enchantement.");

        @Override public String getName() { return "No Enchant Preview"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ENCHANTMENT_TABLE); }

        @Override public Family getFamily() { return Family.CRAFT; }

        @EventHandler
        public void onPrepareEnchant(PrepareItemEnchantEvent event) {
            if (!isRunning()) return;
            Player player = event.getEnchanter();
            Bukkit.getScheduler().runTask(Main.get(), () -> hideHints(player));
        }

        private void hideHints(Player player) {
            if (!player.isOnline()) return;
            if (((CraftPlayer) player).getHandle().activeContainer instanceof ContainerEnchantTable table) {
                Arrays.fill(table.h, -1);
            }
        }
    }

    public static class Overcook extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.overcook.desc",
                "§7Un four explose dès qu'il a cuit un objet, et rend ce qu'il contenait.");

        @Var(name = "Puissance de l'explosion", desc = "Puissance de l'explosion du four.", type = VariableType.DOUBLE, min = 0)
        private double explosionPower = 4.0;

        @Override public String getName() { return "Overcook"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.FURNACE); }

        @Override public Family getFamily() { return Family.CRAFT; }

        @Override
        public void onFurnace(ItemStack result, FurnaceSmeltEvent event) {
            if (!isActive()) return;
            Block block = event.getFurnace();
            if (block == null) return;
            Bukkit.getScheduler().runTask(Main.get(), () -> blow(block));
        }

        private void blow(Block block) {
            if (block.getType() != Material.FURNACE && block.getType() != Material.BURNING_FURNACE) return;
            Furnace furnace = (Furnace) block.getState();
            Location loc = block.getLocation().add(0.5D, 0.5D, 0.5D);
            for (ItemStack content : furnace.getInventory().getContents()) {
                if (content != null && content.getType() != Material.AIR) {
                    loc.getWorld().dropItemNaturally(loc, content);
                }
            }
            furnace.getInventory().clear();
            block.setType(Material.AIR);
            loc.getWorld().createExplosion(loc, (float) explosionPower);
        }
    }

    public static class ReadingSweets extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.readingsweets.desc",
                "§7Un joueur mort laisse un livre, un livre et une plume, et un livre enchanté aléatoire.");

        private final Random random = new Random();

        @Var(name = "Livres", desc = "Nombre de livres vierges laissés à la mort.", type = VariableType.INTEGER, min = 0)
        private int books = 1;

        @Var(name = "Livres et plumes", desc = "Nombre de livres et plumes laissés à la mort.", type = VariableType.INTEGER, min = 0)
        private int quills = 1;

        @Var(name = "Niveau de l'enchantement", desc = "Niveau du livre enchanté laissé à la mort.", type = VariableType.INTEGER, min = 1)
        private int enchantLevel = 1;

        @Override public String getName() { return "Reading Sweets"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BOOK_AND_QUILL); }

        @Override public Family getFamily() { return Family.LOOT; }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            if (books > 0) event.getDrops().add(new ItemStack(Material.BOOK, books));
            if (quills > 0) event.getDrops().add(new ItemStack(Material.BOOK_AND_QUILL, quills));
            event.getDrops().add(randomBook());
        }

        private ItemStack randomBook() {
            Enchantment[] pool = Enchantment.values();
            Enchantment chosen = pool[random.nextInt(pool.length)];
            return new ItemCreator(Material.ENCHANTED_BOOK)
                    .addStoredEnchantment(chosen, Math.min(enchantLevel, chosen.getMaxLevel()))
                    .getItemstack();
        }
    }

    public static class Unbreakable extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.unbreakable.desc",
                "§7Les outils, armes et armures ne perdent plus de durabilité.");

        @Override public String getName() { return "Unbreakable"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.DIAMOND_CHESTPLATE); }

        @Override public Family getFamily() { return Family.CRAFT; }

        @EventHandler
        public void onItemDamage(PlayerItemDamageEvent event) {
            if (!isRunning()) return;
            event.setCancelled(true);
        }
    }

    public static class UpsideDownCrafting extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.upsidedowncrafting.desc",
                "§7Toutes les recettes de craft sont retournées verticalement.");

        @Override public String getName() { return "UpsideDown Crafting"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.WORKBENCH); }

        @Override public Family getFamily() { return Family.CRAFT; }

        private boolean flipped;

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            List<Recipe> recipes = new ArrayList<>();
            Iterator<Recipe> iterator = Bukkit.recipeIterator();
            while (iterator.hasNext()) recipes.add(iterator.next());

            Bukkit.clearRecipes();
            for (Recipe recipe : recipes) {
                Bukkit.addRecipe(recipe instanceof ShapedRecipe shaped ? flip(shaped) : recipe);
            }
            flipped = true;
        }

        @Override
        public void onStop() {
            if (!flipped) return;
            Bukkit.resetRecipes();
            flipped = false;
        }

        private Recipe flip(ShapedRecipe recipe) {
            try {
                String[] shape = recipe.getShape();
                String[] flipped = new String[shape.length];
                for (int i = 0; i < shape.length; i++) flipped[i] = shape[shape.length - 1 - i];

                ShapedRecipe result = new ShapedRecipe(recipe.getResult());
                result.shape(flipped);
                for (Map.Entry<Character, ItemStack> entry : recipe.getIngredientMap().entrySet()) {
                    ItemStack ingredient = entry.getValue();
                    if (ingredient == null) continue;
                    short data = ingredient.getDurability();
                    if (data < 0 || data == Short.MAX_VALUE) result.setIngredient(entry.getKey(), ingredient.getType());
                    else result.setIngredient(entry.getKey(), ingredient.getType(), data);
                }
                return result;
            } catch (Throwable ignored) {
                return recipe;
            }
        }
    }

    public static class VanillaPlus extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.vanillaplus.desc",
                "§7Vanilla, avec du silex et des pommes bien plus fréquents.");

        private final Random random = new Random();

        @Var(name = "Chance de silex", desc = "Chance qu'un bloc de gravier donne du silex.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int flintPercent = 100;

        @Var(name = "Chance de pomme", desc = "Chance qu'un bloc de feuilles donne une pomme.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int applePercent = 5;

        @Override public String getName() { return "Vanilla+"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.FLINT); }

        @Override public Family getFamily() { return Family.MONDE; }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            Material type = block.getType();
            if (type != Material.GRAVEL && type != Material.LEAVES && type != Material.LEAVES_2) return;

            ItemStack tool = player.getItemInHand();
            if (tool != null && tool.containsEnchantment(Enchantment.SILK_TOUCH)) return;

            Location loc = block.getLocation().add(0.5D, 0.5D, 0.5D);
            if (type == Material.GRAVEL) {
                event.setCancelled(true);
                block.setType(Material.AIR);
                MiningScenarios.damageTool(player, tool);
                loc.getWorld().dropItemNaturally(loc, new ItemStack(roll(flintPercent) ? Material.FLINT : Material.GRAVEL, 1));
                return;
            }

            if (tool != null && tool.getType() == Material.SHEARS) return;
            if (roll(applePercent)) loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.APPLE, 1));
        }

        private boolean roll(int percent) {
            return percent > 0 && UHCUtils.Rng.chance(percent);
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new ExperiencedCrafter(),
                new FastPotion(),
                new GoldenPearl(),
                new MasterLevel(),
                new NoEnchantPreview(),
                new Overcook(),
                new ReadingSweets(),
                new Unbreakable(),
                new UpsideDownCrafting(),
                new VanillaPlus()
        );
    }
}
