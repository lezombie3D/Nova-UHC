package net.novaproject.scenarioplus.minage;

import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.normal.MiningScenarios;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class OreScenarios {

    private static final Set<Material> ORES = EnumSet.of(
            Material.COAL_ORE,
            Material.IRON_ORE,
            Material.GOLD_ORE,
            Material.DIAMOND_ORE,
            Material.EMERALD_ORE,
            Material.LAPIS_ORE,
            Material.REDSTONE_ORE,
            Material.GLOWING_REDSTONE_ORE
    );

    private OreScenarios() {
    }

    private static boolean isOre(Material material) {
        return ORES.contains(material);
    }

    private static Location dropCenter(Block block) {
        return block.getLocation().add(0.5D, 0.5D, 0.5D);
    }

    public static class AutobreakScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.autobreak.desc",
                "§7Les blocs que tu poses se cassent d'eux-mêmes au bout de §e%duree% §7secondes.");

        @Var(name = "Délai avant chute", desc = "Temps avant qu'un bloc posé ne se casse tout seul.", type = VariableType.TIME, min = 1)
        private int delaiSec = 30;

        @Override
        public Family getFamily() { return Family.MINAGE; }

        @Override public String getName() { return "Autobreak"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%duree%", delaiSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SAND); }

        @Override
        public void onPlace(Player player, Block block, BlockPlaceEvent event) {
            if (!isActive()) return;
            Material placed = block.getType();
            if (placed == Material.AIR) return;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (block.getType() == placed) block.breakNaturally();
                }
            }.runTaskLater(Main.get(), delaiSec * 20L);
        }
    }

    public static class BlastMiningScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.blastmining.desc",
                "§7Miner un minerai a §e%creeper%%% §7de chances d'invoquer un creeper ralenti et §e%tnt%%% §7d'amorcer de la TNT.");

        private final Random random = new Random();

        @Var(name = "Chance creeper", desc = "Chance qu'un creeper apparaisse en minant un minerai.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int creeperChance = 5;

        @Var(name = "Chance TNT", desc = "Chance qu'une TNT amorcée apparaisse en minant un minerai.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int tntChance = 3;

        @Var(name = "Lenteur du creeper", desc = "Durée de la lenteur appliquée au creeper invoqué.", type = VariableType.TIME, min = 0)
        private int slownessSec = 10;

        @Var(name = "Niveau de lenteur", desc = "Niveau de l'effet Lenteur appliqué au creeper.", type = VariableType.INTEGER, min = 1, max = 5)
        private int slownessLevel = 2;

        @Var(name = "Mèche de la TNT", desc = "Durée de la mèche de la TNT amorcée, en ticks.", type = VariableType.INTEGER, min = 1)
        private int fuseTicks = 40;

        @Override
        public Family getFamily() { return Family.MINAGE; }

        @Override public String getName() { return "Blast Mining"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%creeper%", creeperChance, "%tnt%", tntChance));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SULPHUR); }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            if (!isOre(block.getType())) return;

            Location loc = dropCenter(block);
            World world = loc.getWorld();

            if (UHCUtils.Rng.chance(creeperChance)) {
                Creeper creeper = world.spawn(loc, Creeper.class);
                if (slownessSec > 0) {
                    creeper.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, slownessSec * 20, slownessLevel - 1));
                }
            }

            if (UHCUtils.Rng.chance(tntChance)) {
                TNTPrimed tnt = world.spawn(loc, TNTPrimed.class);
                tnt.setFuseTicks(fuseTicks);
            }
        }
    }

    public static class DemolitionScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.demolition.desc",
                "§7Les minerais lâchent de la TNT dans §e%chance%%% §7des cas, et toute TNT posée s'amorce seule.");

        private final Random random = new Random();

        @Var(name = "Chance de TNT", desc = "Chance qu'un minerai miné lâche de la TNT au lieu de son butin.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int tntChance = 50;

        @Var(name = "TNT lâchée", desc = "Nombre de TNT lâchées à la place du butin du minerai.", type = VariableType.INTEGER, min = 1)
        private int tntAmount = 1;

        @Var(name = "Mèche à la pose", desc = "Durée de la mèche d'une TNT posée, en ticks.", type = VariableType.INTEGER, min = 1)
        private int fuseTicks = 60;

        @Override
        public Family getFamily() { return Family.MINAGE; }

        @Override public String getName() { return "Demolition"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%chance%", tntChance));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.TNT); }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            if (!isOre(block.getType())) return;
            if (!UHCUtils.Rng.chance(tntChance)) return;

            event.setCancelled(true);
            Location loc = dropCenter(block);
            block.setType(Material.AIR);
            MiningScenarios.damageTool(player, player.getItemInHand());
            loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.TNT, tntAmount));
        }

        @Override
        public void onPlace(Player player, Block block, BlockPlaceEvent event) {
            if (!isActive()) return;
            if (block.getType() != Material.TNT) return;
            int fuse = fuseTicks;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (block.getType() != Material.TNT) return;
                    Location loc = dropCenter(block);
                    block.setType(Material.AIR);
                    TNTPrimed tnt = loc.getWorld().spawn(loc, TNTPrimed.class);
                    tnt.setFuseTicks(fuse);
                }
            }.runTaskLater(Main.get(), 1L);
        }
    }

    public static class DiamondHunterScenario extends Scenario {

        private static final DynamicLang BOUNTY_SELF = DynamicLang.of("scenario.diamondhunter.bounty-self",
                "§7Une prime de §b%prime% diamant(s) §7repose sur ta tête.");

        private static final DynamicLang BOUNTY_CLAIMED = DynamicLang.of("scenario.diamondhunter.bounty-claimed",
                "§b%victime% §7portait une prime de §b%prime% diamant(s)§7.");

        private static final DynamicLang DESC = DynamicLang.of("scenario.diamondhunter.desc",
                "§7Chaque joueur porte une prime de §b%min% §7à §b%max% diamants §7lâchée à sa mort.");

        private final Map<UUID, Integer> bounties = new HashMap<>();

        private final Random random = new Random();

        @Var(name = "Prime minimum", desc = "Nombre minimum de diamants placés sur la tête d'un joueur.", type = VariableType.INTEGER, min = 0)
        private int primeMin = 1;

        @Var(name = "Prime maximum", desc = "Nombre maximum de diamants placés sur la tête d'un joueur.", type = VariableType.INTEGER, min = 1)
        private int primeMax = 5;

        @Override
        public Family getFamily() { return Family.MINAGE; }

        @Override public String getName() { return "Diamond Hunter"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%min%", primeMin, "%max%", primeMax));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.DIAMOND); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            int low = Math.min(primeMin, primeMax);
            int high = Math.max(primeMin, primeMax);
            int bounty = low + random.nextInt(high - low + 1);
            bounties.put(player.getUniqueId(), bounty);
            LangManager.get().send(BOUNTY_SELF, player, Map.of("%prime%", bounty));
        }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            Integer bounty = bounties.remove(uhcPlayer.getUniqueId());
            if (bounty == null || bounty <= 0) return;
            event.getDrops().add(new ItemStack(Material.DIAMOND, bounty));
            LangManager.get().sendAll(BOUNTY_CLAIMED, Map.of(
                    "%victime%", event.getEntity().getName(),
                    "%prime%", bounty));
        }

        @Override
        public void onStop() {
            bounties.clear();
        }
    }

    public static class DiamondlessScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.diamondless.desc",
                "§7Le minerai de diamant lâche §f%fer% lingot(s) de fer§7, et aucun diamant ne tombe à la mort.");

        @Var(name = "Fer lâché", desc = "Nombre de lingots de fer lâchés à la place du diamant.", type = VariableType.INTEGER, min = 1)
        private int ironAmount = 1;

        @Override
        public Family getFamily() { return Family.MINAGE; }

        @Override public String getName() { return "Diamondless"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%fer%", ironAmount));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.IRON_INGOT); }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            if (block.getType() != Material.DIAMOND_ORE) return;

            event.setCancelled(true);
            Location loc = dropCenter(block);
            block.setType(Material.AIR);
            MiningScenarios.damageTool(player, player.getItemInHand());
            loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.IRON_INGOT, ironAmount));
        }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            event.getDrops().removeIf(drop -> drop != null
                    && (drop.getType() == Material.DIAMOND || drop.getType() == Material.DIAMOND_BLOCK));
        }
    }

    public static class DirtTrickScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.dirttrick.desc",
                "§7Sous §eY=%hauteur%§7, une pelle retire la terre instantanément.");

        private static final Set<Material> SPADES = EnumSet.of(
                Material.WOOD_SPADE,
                Material.STONE_SPADE,
                Material.IRON_SPADE,
                Material.GOLD_SPADE,
                Material.DIAMOND_SPADE
        );

        private static final Set<Material> DIGGABLE = EnumSet.of(
                Material.DIRT,
                Material.GRASS
        );

        @Var(name = "Hauteur maximum", desc = "Altitude sous laquelle la pelle retire la terre instantanément.", type = VariableType.INTEGER, min = 0, max = 256)
        private int hauteurMax = 32;

        @Override
        public Family getFamily() { return Family.MINAGE; }

        @Override public String getName() { return "Dirt Trick"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%hauteur%", hauteurMax));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.DIRT); }

        @Override
        public void onPlayerInteract(Player player, PlayerInteractEvent event) {
            if (!isActive()) return;
            Action action = event.getAction();
            if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) return;

            ItemStack tool = event.getItem();
            if (tool == null || !SPADES.contains(tool.getType())) return;

            Block block = event.getClickedBlock();
            if (block == null || !DIGGABLE.contains(block.getType())) return;
            if (block.getY() >= hauteurMax) return;

            event.setCancelled(true);
            block.breakNaturally();
            MiningScenarios.damageTool(player, tool);
        }
    }

    public static class DoubleOrNothingScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.doubleornothing.desc",
                "§7Chaque minerai a §e%chance%%% §7de chances de lâcher §e×%facteur% §7son butin, sinon rien du tout.");

        private final Random random = new Random();

        @Var(name = "Chance de double", desc = "Chance que le minerai lâche un butin multiplié plutôt que rien.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int doubleChance = 50;

        @Var(name = "Multiplicateur", desc = "Facteur appliqué au butin du minerai quand le tirage est gagnant.", type = VariableType.INTEGER, min = 1)
        private int multiplier = 2;

        @Override
        public Family getFamily() { return Family.MINAGE; }

        @Override public String getName() { return "Double or Nothing"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%chance%", doubleChance, "%facteur%", multiplier));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLD_ORE); }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            if (!isOre(block.getType())) return;

            ItemStack tool = player.getItemInHand();
            boolean win = UHCUtils.Rng.chance(doubleChance);
            Location loc = dropCenter(block);
            List<ItemStack> drops = List.copyOf(block.getDrops(tool));

            event.setCancelled(true);
            block.setType(Material.AIR);
            MiningScenarios.damageTool(player, tool);
            if (!win) return;

            for (ItemStack drop : drops) {
                ItemStack multiplied = drop.clone();
                multiplied.setAmount(drop.getAmount() * multiplier);
                loc.getWorld().dropItemNaturally(loc, multiplied);
            }
        }
    }

    public static class EnoughCobblestoneScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.enoughcobblestone.desc",
                "§7Une fois §e%seuil% §7blocs de pierre du même type en poche, les suivants partent en fumée.");

        private static final Set<Material> STONE_TYPES = EnumSet.of(
                Material.COBBLESTONE,
                Material.MOSSY_COBBLESTONE,
                Material.STONE
        );

        @Var(name = "Seuil", desc = "Quantité détenue à partir de laquelle les ramassages du même bloc sont annulés.", type = VariableType.INTEGER, min = 1)
        private int seuil = 64;

        @Override
        public Family getFamily() { return Family.MINAGE; }

        @Override public String getName() { return "Enough Cobblestone"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%seuil%", seuil));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.COBBLESTONE); }

        @Override
        public void onPickUp(Player player, Item item, PlayerPickupItemEvent event) {
            if (!isActive()) return;
            ItemStack stack = item.getItemStack();
            if (stack == null || !STONE_TYPES.contains(stack.getType())) return;
            if (held(player, stack.getType()) < seuil) return;

            event.setCancelled(true);
            item.remove();
        }

        private int held(Player player, Material material) {
            int total = 0;
            for (ItemStack content : player.getInventory().getContents()) {
                if (content != null && content.getType() == material) total += content.getAmount();
            }
            return total;
        }
    }

    public static class EverlastingDiamondScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.everlastingdiamond.desc",
                "§7L'équipement en diamant ne s'use jamais.");

        private static final Set<Material> DIAMOND_GEAR = EnumSet.of(
                Material.DIAMOND_SWORD,
                Material.DIAMOND_PICKAXE,
                Material.DIAMOND_AXE,
                Material.DIAMOND_SPADE,
                Material.DIAMOND_HOE,
                Material.DIAMOND_HELMET,
                Material.DIAMOND_CHESTPLATE,
                Material.DIAMOND_LEGGINGS,
                Material.DIAMOND_BOOTS
        );

        @Override
        public Family getFamily() { return Family.MINAGE; }

        @Override public String getName() { return "Everlasting Diamond"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.DIAMOND_CHESTPLATE); }

        @EventHandler
        public void onItemDamage(PlayerItemDamageEvent event) {
            if (!isRunning()) return;
            ItemStack item = event.getItem();
            if (item == null || !DIAMOND_GEAR.contains(item.getType())) return;
            event.setCancelled(true);
            item.setDurability((short) 0);
        }
    }

    public static class FortuneBoysScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.fortuneboys.desc",
                "§7Tout outil crafté sort avec §eFortune %niveau%§7.");

        private static final Set<Material> TOOLS = EnumSet.of(
                Material.WOOD_PICKAXE,
                Material.STONE_PICKAXE,
                Material.IRON_PICKAXE,
                Material.GOLD_PICKAXE,
                Material.DIAMOND_PICKAXE,
                Material.WOOD_SPADE,
                Material.STONE_SPADE,
                Material.IRON_SPADE,
                Material.GOLD_SPADE,
                Material.DIAMOND_SPADE,
                Material.WOOD_AXE,
                Material.STONE_AXE,
                Material.IRON_AXE,
                Material.GOLD_AXE,
                Material.DIAMOND_AXE
        );

        @Var(name = "Niveau de Fortune", desc = "Niveau de Fortune appliqué à chaque outil crafté.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveau = 2;

        @Override
        public Family getFamily() { return Family.MINAGE; }

        @Override public String getName() { return "Fortune Boys"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%niveau%", niveau));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.DIAMOND_PICKAXE); }

        @Override
        public void onCraft(ItemStack result, CraftItemEvent event) {
            if (!isActive()) return;
            if (result == null || !TOOLS.contains(result.getType())) return;
            if (result.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) >= niveau) return;

            result.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, niveau);
            event.getInventory().setResult(result);
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new AutobreakScenario(),
                new BlastMiningScenario(),
                new DemolitionScenario(),
                new DiamondHunterScenario(),
                new DiamondlessScenario(),
                new DirtTrickScenario(),
                new DoubleOrNothingScenario(),
                new EnoughCobblestoneScenario(),
                new EverlastingDiamondScenario(),
                new FortuneBoysScenario()
        );
    }
}
