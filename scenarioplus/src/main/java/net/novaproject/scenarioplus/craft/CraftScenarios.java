package net.novaproject.scenarioplus.craft;

import net.minecraft.server.v1_8_R3.Container;
import net.minecraft.server.v1_8_R3.ContainerEnchantTable;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.player.utils.PlayerUtils;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class CraftScenarios {

    private CraftScenarios() {
    }

    private static void hideEnchantHints(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player == null || !player.isOnline()) return;
                Container container = ((CraftPlayer) player).getHandle().activeContainer;
                if (!(container instanceof ContainerEnchantTable table)) return;
                table.h[0] = -1;
                table.h[1] = -1;
                table.h[2] = -1;
            }
        }.runTask(Main.get());
    }

    public static class LegacyEnchantsScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.legacyenchants.desc",
                "§7L'enchantement repasse en règles 1.7 : le niveau affiché est entièrement consommé, "
                        + "le lapis n'est pas dépensé et aucun aperçu d'enchantement n'est visible.");

        @Var(name = "Coût en niveaux complet", desc = "Le niveau affiché est entièrement consommé.", type = VariableType.BOOLEAN)
        private boolean fullLevelCost = true;

        @Var(name = "Lapis conservé", desc = "Le lapis-lazuli n'est jamais consommé par la table.", type = VariableType.BOOLEAN)
        private boolean keepLapis = true;

        @Var(name = "Aperçu masqué", desc = "Masque l'enchantement suggéré par la table.", type = VariableType.BOOLEAN)
        private boolean hidePreview = true;

        @Override
        public Family getFamily() { return Family.CRAFT; }

        @Override public String getName() { return "1.7 Enchants"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ENCHANTMENT_TABLE); }

        @EventHandler
        public void onPrepareEnchant(PrepareItemEnchantEvent event) {
            if (!isRunning()) return;
            if (!hidePreview) return;
            hideEnchantHints(event.getEnchanter());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onEnchant(EnchantItemEvent event) {
            if (!isRunning()) return;
            Player player = event.getEnchanter();
            int cost = event.getExpLevelCost();
            int levelBefore = player.getLevel();
            EnchantingInventory inventory = event.getInventory() instanceof EnchantingInventory ench ? ench : null;
            ItemStack lapis = inventory != null && inventory.getSecondary() != null
                    ? inventory.getSecondary().clone()
                    : null;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) return;
                    if (fullLevelCost) player.setLevel(Math.max(0, levelBefore - cost));
                    if (keepLapis && inventory != null && lapis != null) inventory.setSecondary(lapis);
                    player.updateInventory();
                }
            }.runTask(Main.get());
        }
    }

    public static class AnvilProgressionScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.anvilprogression.desc",
                "§7L'enclume ne fonctionne que si tu as des utilisations en réserve : chaque kill t'en offre.");

        private static final DynamicLang NO_USE = DynamicLang.of("scenario.anvilprogression.no_use",
                "§cTu dois tuer un joueur pour gagner une utilisation d'enclume.");

        private static final DynamicLang USED = DynamicLang.of("scenario.anvilprogression.used",
                "§7Utilisations d'enclume restantes : §e%uses%");

        private static final DynamicLang GAINED = DynamicLang.of("scenario.anvilprogression.gained",
                "§a+%amount% utilisation(s) d'enclume §7(total : §e%uses%§7)");

        @Var(name = "Utilisations par kill", desc = "Utilisations d'enclume gagnées par kill.", type = VariableType.INTEGER, min = 0)
        private int usesPerKill = 1;

        @Var(name = "Utilisations de départ", desc = "Utilisations d'enclume disponibles au début de la partie.", type = VariableType.INTEGER, min = 0)
        private int startingUses = 0;

        private final Map<UUID, Integer> uses = new HashMap<>();

        @Override
        public Family getFamily() { return Family.CRAFT; }

        @Override public String getName() { return "Anvil Progression"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ANVIL); }

        @Override
        public void onKill(UHCPlayer killer, UHCPlayer victim) {
            if (!isActive()) return;
            if (killer == null || usesPerKill <= 0) return;
            int total = uses.getOrDefault(killer.getUniqueId(), startingUses) + usesPerKill;
            uses.put(killer.getUniqueId(), total);
            Player player = killer.getPlayer();
            if (player == null) return;
            LangManager.get().send(GAINED, player, Map.of("%amount%", usesPerKill, "%uses%", total));
        }

        @EventHandler(ignoreCancelled = true)
        public void onAnvilTake(InventoryClickEvent event) {
            if (!isRunning()) return;
            if (!(event.getWhoClicked() instanceof Player player)) return;
            if (!(event.getClickedInventory() instanceof AnvilInventory anvil)) return;
            if (event.getRawSlot() != 2 || anvil.getItem(2) == null) return;
            int left = uses.getOrDefault(player.getUniqueId(), startingUses);
            if (left <= 0) {
                event.setCancelled(true);
                LangManager.get().send(NO_USE, player);
                return;
            }
            uses.put(player.getUniqueId(), left - 1);
            LangManager.get().send(USED, player, Map.of("%uses%", left - 1));
        }

        @Override
        public void onStop() {
            uses.clear();
        }
    }

    public static class BenchBlitzScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.benchblitz.desc",
                "§7Chaque joueur démarre avec une table de craft, et les tables de craft sont incraftables.");

        private static final DynamicLang BLOCKED = DynamicLang.of("scenario.benchblitz.blocked",
                "§cLa table de craft ne peut pas être fabriquée.");

        @Var(name = "Tables de craft au départ", desc = "Nombre de tables de craft données au début de la partie.", type = VariableType.INTEGER, min = 0)
        private int benches = 1;

        @Override
        public Family getFamily() { return Family.CRAFT; }

        @Override public String getName() { return "Bench Blitz"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.WORKBENCH); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            if (benches <= 0) return;
            PlayerUtils.giveOrDrop(player, new ItemStack(Material.WORKBENCH, benches));
        }

        @Override
        public void onCraft(ItemStack result, CraftItemEvent event) {
            if (!isActive()) return;
            if (result == null || result.getType() != Material.WORKBENCH) return;
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                LangManager.get().send(BLOCKED, player);
                player.updateInventory();
            }
        }
    }

    public static class BiomeEnchantersScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.biomeenchanters.desc",
                "§7Tu ne peux enchanter qu'un nombre limité d'objets par biome : change de biome pour enchanter à nouveau.");

        private static final DynamicLang EXHAUSTED = DynamicLang.of("scenario.biomeenchanters.exhausted",
                "§cCe biome est épuisé, va enchanter ailleurs.");

        private static final DynamicLang LEFT = DynamicLang.of("scenario.biomeenchanters.left",
                "§7Enchantements restants dans ce biome : §e%left%");

        @Var(name = "Enchantements par biome", desc = "Nombre d'objets enchantables dans un même biome.", type = VariableType.INTEGER, min = 1)
        private int enchantsPerBiome = 1;

        private final Map<UUID, Map<Biome, Integer>> used = new HashMap<>();

        @Override
        public Family getFamily() { return Family.CRAFT; }

        @Override public String getName() { return "Biome Enchanters"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SAPLING); }

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onEnchant(EnchantItemEvent event) {
            if (!isRunning()) return;
            Player player = event.getEnchanter();
            Biome biome = player.getLocation().getBlock().getBiome();
            Map<Biome, Integer> counts = used.computeIfAbsent(player.getUniqueId(), key -> new EnumMap<>(Biome.class));
            int done = counts.getOrDefault(biome, 0);
            if (done >= enchantsPerBiome) {
                event.setCancelled(true);
                LangManager.get().send(EXHAUSTED, player);
                return;
            }
            counts.put(biome, done + 1);
            LangManager.get().send(LEFT, player, Map.of("%left%", enchantsPerBiome - done - 1));
        }

        @Override
        public void onStop() {
            used.clear();
        }
    }

    public static class BloodEnchantsScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.bloodenchants.desc",
                "§7Chaque enchantement appliqué te coûte de la vie.");

        @Var(name = "Dégâts par enchantement", desc = "Dégâts subis par enchantement appliqué (2 = un coeur).", type = VariableType.DOUBLE, min = 0)
        private double damagePerEnchant = 1.0;

        @Var(name = "Coût par niveau d'XP", desc = "Compter les dégâts par niveau d'expérience dépensé au lieu de par enchantement.", type = VariableType.BOOLEAN)
        private boolean perExpLevel = false;

        @Override
        public Family getFamily() { return Family.CRAFT; }

        @Override public String getName() { return "Blood Enchants"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.REDSTONE); }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onEnchant(EnchantItemEvent event) {
            if (!isRunning()) return;
            int units = perExpLevel ? event.getExpLevelCost() : Math.max(1, event.getEnchantsToAdd().size());
            double damage = damagePerEnchant * units;
            if (damage <= 0) return;
            event.getEnchanter().damage(damage);
        }
    }

    public static class CarrotComboScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.carrotcombo.desc",
                "§7Crafter une épée donne une carotte enchantée d'une puissance équivalente.");

        @Var(name = "Tranchant épée en bois", desc = "Niveau de Tranchant de la carotte issue d'une épée en bois.", type = VariableType.INTEGER, min = 0)
        private int woodSharpness = 2;

        @Var(name = "Tranchant épée en or", desc = "Niveau de Tranchant de la carotte issue d'une épée en or.", type = VariableType.INTEGER, min = 0)
        private int goldSharpness = 2;

        @Var(name = "Tranchant épée en pierre", desc = "Niveau de Tranchant de la carotte issue d'une épée en pierre.", type = VariableType.INTEGER, min = 0)
        private int stoneSharpness = 3;

        @Var(name = "Tranchant épée en fer", desc = "Niveau de Tranchant de la carotte issue d'une épée en fer.", type = VariableType.INTEGER, min = 0)
        private int ironSharpness = 4;

        @Var(name = "Tranchant épée en diamant", desc = "Niveau de Tranchant de la carotte issue d'une épée en diamant.", type = VariableType.INTEGER, min = 0)
        private int diamondSharpness = 5;

        @Override
        public Family getFamily() { return Family.CRAFT; }

        @Override public String getName() { return "Carrot Combo"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.CARROT_ITEM); }

        @Override
        public void onCraft(ItemStack result, CraftItemEvent event) {
            if (!isActive()) return;
            if (result == null) return;
            int sharpness = switch (result.getType()) {
                case WOOD_SWORD -> woodSharpness;
                case GOLD_SWORD -> goldSharpness;
                case STONE_SWORD -> stoneSharpness;
                case IRON_SWORD -> ironSharpness;
                case DIAMOND_SWORD -> diamondSharpness;
                default -> -1;
            };
            if (sharpness < 0) return;
            ItemStack carrot = new ItemStack(Material.CARROT_ITEM, Math.max(1, result.getAmount()));
            if (sharpness > 0) carrot.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, sharpness);
            event.getInventory().setResult(carrot);
        }
    }

    public static class CompanionBenchScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.companionbench.desc",
                "§7Tu démarres avec une unique table de craft incraftable : sa destruction est annoncée à tous.");

        private static final DynamicLang BLOCKED = DynamicLang.of("scenario.companionbench.blocked",
                "§cLa table de craft ne peut pas être fabriquée.");

        private static final DynamicLang BROKEN = DynamicLang.of("scenario.companionbench.broken",
                "§e%player% §7vient de casser une table de craft.");

        private static final DynamicLang BURNED = DynamicLang.of("scenario.companionbench.burned",
                "§7Une table de craft vient de partir en fumée.");

        @Var(name = "Tables de craft au départ", desc = "Nombre de tables de craft données au début de la partie.", type = VariableType.INTEGER, min = 0)
        private int benches = 1;

        @Override
        public Family getFamily() { return Family.CRAFT; }

        @Override public String getName() { return "Companion Bench"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.WOOD); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            if (benches <= 0) return;
            PlayerUtils.giveOrDrop(player, new ItemStack(Material.WORKBENCH, benches));
        }

        @Override
        public void onCraft(ItemStack result, CraftItemEvent event) {
            if (!isActive()) return;
            if (result == null || result.getType() != Material.WORKBENCH) return;
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                LangManager.get().send(BLOCKED, player);
                player.updateInventory();
            }
        }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            if (block.getType() != Material.WORKBENCH) return;
            LangManager.get().sendAll(BROKEN, Map.of("%player%", player.getName()));
        }

        @EventHandler(ignoreCancelled = true)
        public void onBurn(BlockBurnEvent event) {
            if (!isRunning()) return;
            if (event.getBlock().getType() != Material.WORKBENCH) return;
            LangManager.get().sendAll(BURNED);
        }
    }

    public static class CraftableAbsorptionScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.craftableabsorption.desc",
                "§7Les pommes d'or ne donnent plus d'absorption : entoure une pomme d'or de 4 lingots pour obtenir une pomme d'absorption.");

        private static final DynamicLang ITEM_NAME = DynamicLang.of("scenario.craftableabsorption.item",
                "§6Pomme d'absorption");

        @Var(name = "Niveau d'absorption", desc = "Niveau d'absorption donné par la pomme d'absorption.", type = VariableType.INTEGER, min = 1)
        private int absorptionLevel = 1;

        @Var(name = "Durée d'absorption", desc = "Durée de l'absorption donnée par la pomme d'absorption.", type = VariableType.TIME, min = 1)
        private int absorptionDurationSec = 120;

        @Override
        public Family getFamily() { return Family.CRAFT; }

        @Override public String getName() { return "Craftable Absorption"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLDEN_APPLE); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            ShapedRecipe recipe = new ShapedRecipe(absorptionApple());
            recipe.shape(" G ", "GAG", " G ");
            recipe.setIngredient('G', Material.GOLD_INGOT);
            recipe.setIngredient('A', Material.GOLDEN_APPLE);
            Bukkit.addRecipe(recipe);
        }

        @Override
        public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
            if (!isActive()) return;
            if (item == null || item.getType() != Material.GOLDEN_APPLE) return;
            boolean crafted = isAbsorptionApple(item);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) return;
                    player.removePotionEffect(PotionEffectType.ABSORPTION);
                    if (!crafted) return;
                    player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,
                            absorptionDurationSec * 20, Math.max(0, absorptionLevel - 1)), true);
                }
            }.runTask(Main.get());
        }

        private ItemStack absorptionApple() {
            return new ItemCreator(Material.GOLDEN_APPLE)
                    .setName(LangManager.get().get(ITEM_NAME))
                    .getItemstack();
        }

        private boolean isAbsorptionApple(ItemStack item) {
            return item.hasItemMeta()
                    && item.getItemMeta().hasDisplayName()
                    && item.getItemMeta().getDisplayName().equals(LangManager.get().get(ITEM_NAME));
        }
    }

    public static class CraftableTpScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.craftabletp.desc",
                "§7Tu démarres avec une perle de l'Ender : renomme-la au pseudo d'un joueur vivant pour être téléporté près de lui.");

        private static final DynamicLang UNKNOWN = DynamicLang.of("scenario.craftabletp.unknown",
                "§cAucun joueur vivant ne porte ce nom.");

        private static final DynamicLang SENT = DynamicLang.of("scenario.craftabletp.sent",
                "§7Téléporté à §e%blocks% §7blocs de §e%player%§7.");

        @Var(name = "Perles au départ", desc = "Nombre de perles de l'Ender données au début de la partie.", type = VariableType.INTEGER, min = 0)
        private int pearls = 1;

        @Var(name = "Distance d'arrivée", desc = "Distance en blocs entre le point d'arrivée et la cible.", type = VariableType.INTEGER, min = 1)
        private int distanceBlocks = 50;

        private final Random random = new Random();

        @Override
        public Family getFamily() { return Family.CRAFT; }

        @Override public String getName() { return "Craftable TP"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ENDER_PEARL); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            if (pearls <= 0) return;
            PlayerUtils.giveOrDrop(player, new ItemStack(Material.ENDER_PEARL, pearls));
        }

        @Override
        public void onPlayerInteract(Player player, PlayerInteractEvent event) {
            if (!isActive()) return;
            ItemStack item = event.getItem();
            if (item == null || item.getType() != Material.ENDER_PEARL) return;
            if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

            String wanted = ChatColor.stripColor(item.getItemMeta().getDisplayName()).trim();
            event.setCancelled(true);

            Player target = null;
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player online = uhcPlayer.getPlayer();
                if (online == null || online.equals(player)) continue;
                if (online.getName().equalsIgnoreCase(wanted)) {
                    target = online;
                    break;
                }
            }
            if (target == null) {
                LangManager.get().send(UNKNOWN, player);
                return;
            }

            ItemCreator.consumeOne(player, item);
            player.updateInventory();

            double angle = random.nextDouble() * Math.PI * 2;
            Location base = target.getLocation();
            World world = base.getWorld();
            double x = base.getX() + Math.cos(angle) * distanceBlocks;
            double z = base.getZ() + Math.sin(angle) * distanceBlocks;
            int y = world.getHighestBlockYAt((int) Math.floor(x), (int) Math.floor(z)) + 1;

            player.teleport(new Location(world, x, y, z, player.getLocation().getYaw(), player.getLocation().getPitch()));
            world.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 1f);
            LangManager.get().send(SENT, player, Map.of("%blocks%", distanceBlocks, "%player%", target.getName()));
        }
    }

    public static class CraftCleanScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.craftclean.desc",
                "§7Entourer du charbon de 8 objets cuisinables dans la table de craft rend directement le résultat cuit.");

        @Var(name = "Résultats par objet", desc = "Multiplicateur du nombre d'objets cuits rendus par craft.", type = VariableType.INTEGER, min = 1)
        private int resultPerItem = 1;

        @Override
        public Family getFamily() { return Family.CRAFT; }

        @Override public String getName() { return "CraftClean"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.COAL); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            List<FurnaceRecipe> smelting = new ArrayList<>();
            Iterator<Recipe> iterator = Bukkit.recipeIterator();
            while (iterator.hasNext()) {
                Recipe recipe = iterator.next();
                if (recipe instanceof FurnaceRecipe furnace) smelting.add(furnace);
            }

            Set<String> seen = new HashSet<>();
            for (FurnaceRecipe furnace : smelting) {
                ItemStack input = furnace.getInput();
                ItemStack smelted = furnace.getResult();
                if (input == null || smelted == null) continue;
                if (input.getType() == Material.COAL || input.getType() == Material.AIR) continue;
                if (smelted.getType() == Material.AIR) continue;

                int data = input.getDurability();
                if (!seen.add(input.getType().name() + ":" + data)) continue;

                ItemStack result = smelted.clone();
                result.setAmount(Math.min(64, Math.max(1, smelted.getAmount()) * 8 * resultPerItem));

                ShapedRecipe recipe = new ShapedRecipe(result);
                recipe.shape("SSS", "SCS", "SSS");
                recipe.setIngredient('C', Material.COAL, -1);
                if (data < 0 || data == Short.MAX_VALUE) {
                    recipe.setIngredient('S', input.getType(), -1);
                } else {
                    recipe.setIngredient('S', input.getType(), data);
                }
                Bukkit.addRecipe(recipe);
            }
        }
    }

    public static class EnchantParanoiaScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.enchantparanoia.desc",
                "§7La table d'enchantement n'affiche plus aucun aperçu : tu enchantes à l'aveugle.");

        @Override
        public Family getFamily() { return Family.CRAFT; }

        @Override public String getName() { return "Enchant Paranoia"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.EYE_OF_ENDER); }

        @EventHandler
        public void onPrepareEnchant(PrepareItemEnchantEvent event) {
            if (!isRunning()) return;
            hideEnchantHints(event.getEnchanter());
        }
    }

    public static class EnchantProgressionScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.enchantprogression.desc",
                "§7La table d'enchantement ne fonctionne que si tu as des utilisations en réserve : chaque kill t'en offre.");

        private static final DynamicLang NO_USE = DynamicLang.of("scenario.enchantprogression.no_use",
                "§cTu dois tuer un joueur pour gagner une utilisation de table d'enchantement.");

        private static final DynamicLang USED = DynamicLang.of("scenario.enchantprogression.used",
                "§7Utilisations de table restantes : §e%uses%");

        private static final DynamicLang GAINED = DynamicLang.of("scenario.enchantprogression.gained",
                "§a+%amount% utilisation(s) de table §7(total : §e%uses%§7)");

        @Var(name = "Utilisations par kill", desc = "Utilisations de table d'enchantement gagnées par kill.", type = VariableType.INTEGER, min = 0)
        private int usesPerKill = 1;

        @Var(name = "Utilisations de départ", desc = "Utilisations de table disponibles au début de la partie.", type = VariableType.INTEGER, min = 0)
        private int startingUses = 0;

        private final Map<UUID, Integer> uses = new HashMap<>();

        @Override
        public Family getFamily() { return Family.CRAFT; }

        @Override public String getName() { return "Enchant Progression"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BOOK); }

        @Override
        public void onKill(UHCPlayer killer, UHCPlayer victim) {
            if (!isActive()) return;
            if (killer == null || usesPerKill <= 0) return;
            int total = uses.getOrDefault(killer.getUniqueId(), startingUses) + usesPerKill;
            uses.put(killer.getUniqueId(), total);
            Player player = killer.getPlayer();
            if (player == null) return;
            LangManager.get().send(GAINED, player, Map.of("%amount%", usesPerKill, "%uses%", total));
        }

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onEnchant(EnchantItemEvent event) {
            if (!isRunning()) return;
            Player player = event.getEnchanter();
            int left = uses.getOrDefault(player.getUniqueId(), startingUses);
            if (left <= 0) {
                event.setCancelled(true);
                LangManager.get().send(NO_USE, player);
                return;
            }
            uses.put(player.getUniqueId(), left - 1);
            LangManager.get().send(USED, player, Map.of("%uses%", left - 1));
        }

        @Override
        public void onStop() {
            uses.clear();
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new LegacyEnchantsScenario(),
                new AnvilProgressionScenario(),
                new BenchBlitzScenario(),
                new BiomeEnchantersScenario(),
                new BloodEnchantsScenario(),
                new CarrotComboScenario(),
                new CompanionBenchScenario(),
                new CraftableAbsorptionScenario(),
                new CraftableTpScenario(),
                new CraftCleanScenario(),
                new EnchantParanoiaScenario(),
                new EnchantProgressionScenario()
        );
    }
}
