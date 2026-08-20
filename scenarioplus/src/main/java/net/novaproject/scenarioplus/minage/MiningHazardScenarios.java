package net.novaproject.scenarioplus.minage;

import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.player.utils.PlayerUtils;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.normal.MiningScenarios;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import net.novaproject.novauhc.Common;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class MiningHazardScenarios {

    private static final Set<Material> PICKAXES = EnumSet.of(
            Material.WOOD_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE,
            Material.GOLD_PICKAXE, Material.DIAMOND_PICKAXE);

    private static final Set<Material> SPADES = EnumSet.of(
            Material.WOOD_SPADE, Material.STONE_SPADE, Material.IRON_SPADE,
            Material.GOLD_SPADE, Material.DIAMOND_SPADE);

    private static final Set<Material> CRAFTED_TOOLS = EnumSet.of(
            Material.WOOD_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE,
            Material.GOLD_PICKAXE, Material.DIAMOND_PICKAXE,
            Material.WOOD_AXE, Material.STONE_AXE, Material.IRON_AXE,
            Material.GOLD_AXE, Material.DIAMOND_AXE,
            Material.WOOD_SPADE, Material.STONE_SPADE, Material.IRON_SPADE,
            Material.GOLD_SPADE, Material.DIAMOND_SPADE);

    private static final Set<Material> ORES = EnumSet.of(
            Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE,
            Material.EMERALD_ORE, Material.LAPIS_ORE, Material.REDSTONE_ORE,
            Material.GLOWING_REDSTONE_ORE, Material.QUARTZ_ORE);

    private static final Set<Material> GOLD_INGREDIENTS = EnumSet.of(
            Material.GOLD_INGOT, Material.GOLD_NUGGET, Material.GOLD_BLOCK, Material.GOLD_ORE);

    private MiningHazardScenarios() {
    }

    private static Material normalizeOre(Material material) {
        return material == Material.GLOWING_REDSTONE_ORE ? Material.REDSTONE_ORE : material;
    }

    private static Material resourceOf(Material ore) {
        return switch (normalizeOre(ore)) {
            case COAL_ORE -> Material.COAL;
            case IRON_ORE -> Material.IRON_INGOT;
            case GOLD_ORE -> Material.GOLD_INGOT;
            case DIAMOND_ORE -> Material.DIAMOND;
            case EMERALD_ORE -> Material.EMERALD;
            case LAPIS_ORE -> Material.INK_SACK;
            case REDSTONE_ORE -> Material.REDSTONE;
            case QUARTZ_ORE -> Material.QUARTZ;
            default -> null;
        };
    }

    public static class GigaDrillScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.gigadrill.desc",
                "§7Tous les outils craftés sortent avec §eEfficacité §7et §eSolidité §7à un niveau extrême.");

        @Var(name = "Niveau d'Efficacité", desc = "Niveau d'Efficacité posé sur tout outil crafté.", type = VariableType.INTEGER, min = 0)
        private int digSpeedLevel = 10;

        @Var(name = "Niveau de Solidité", desc = "Niveau de Solidité posé sur tout outil crafté.", type = VariableType.INTEGER, min = 0)
        private int unbreakingLevel = 10;

        @Override public Family getFamily() { return Family.MINAGE; }

        @Override public String getName() { return "Giga Drill"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.DIAMOND_PICKAXE); }

        @Override
        public void onCraft(ItemStack result, CraftItemEvent event) {
            if (!isActive()) return;
            if (result == null || !CRAFTED_TOOLS.contains(result.getType())) return;
            if (digSpeedLevel > 0) result.addUnsafeEnchantment(Enchantment.DIG_SPEED, digSpeedLevel);
            if (unbreakingLevel > 0) result.addUnsafeEnchantment(Enchantment.DURABILITY, unbreakingLevel);
            event.getInventory().setResult(result);
        }
    }

    public static class GoldHunterScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.goldhunter.desc",
                "§7Chaque joueur porte une §6prime en or §7sur sa tête, versée à son tueur.");

        private static final DynamicLang OWN_BOUNTY = DynamicLang.of("scenario.goldhunter.own",
                "§6Gold Hunter §8» §7Ta tête vaut §e%gold% §7lingot(s) d'or.");

        private static final DynamicLang CLAIMED = DynamicLang.of("scenario.goldhunter.claimed",
                "§6Gold Hunter §8» §e%killer% §7empoche la prime de §e%victim% §8(§6%gold% §7or§8)§7.");

        private final Map<UUID, Integer> bounties = new HashMap<>();

        private final Random random = new Random();

        @Var(name = "Prime minimum", desc = "Nombre minimum de lingots d'or sur la tête d'un joueur.", type = VariableType.INTEGER, min = 0)
        private int minBounty = 1;

        @Var(name = "Prime maximum", desc = "Nombre maximum de lingots d'or sur la tête d'un joueur.", type = VariableType.INTEGER, min = 0)
        private int maxBounty = 5;

        @Override public Family getFamily() { return Family.MINAGE; }

        @Override public String getName() { return "Gold Hunter"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLD_INGOT); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            int low = Math.min(minBounty, maxBounty);
            int high = Math.max(minBounty, maxBounty);
            int bounty = low + random.nextInt(high - low + 1);
            bounties.put(player.getUniqueId(), bounty);
            LangManager.get().send(OWN_BOUNTY, player, Map.of("%gold%", bounty));
        }

        @Override
        public void onKill(UHCPlayer killer, UHCPlayer victim) {
            if (!isActive()) return;
            if (killer == null || victim == null) return;
            Integer bounty = bounties.remove(victim.getUuid());
            if (bounty == null || bounty <= 0) return;
            Player shooter = killer.getPlayer();
            if (shooter == null) return;
            Player dead = victim.getPlayer();
            String victimName = dead != null ? dead.getName() : Bukkit.getOfflinePlayer(victim.getUuid()).getName();
            PlayerUtils.giveOrDrop(shooter, new ItemStack(Material.GOLD_INGOT, bounty));
            LangManager.get().sendAll(CLAIMED, Map.of(
                    "%killer%", shooter.getName(),
                    "%victim%", String.valueOf(victimName),
                    "%gold%", bounty));
        }

        @Override
        public void onStop() {
            if (!isActive()) return;
            bounties.clear();
        }
    }

    public static class GoldlessV1Scenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.goldlessv1.desc",
                "§7L'or miné donne du §ffer§7. À la mort, tu lâches une §6tête dorée §7et de l'or.");

        private static final DynamicLang HEAD_NAME = DynamicLang.of("scenario.goldlessv1.headname",
                "§6Tête dorée de %player%");

        @Var(name = "Fer donné", desc = "Nombre de minerais de fer lâchés à la place du minerai d'or.", type = VariableType.INTEGER, min = 0)
        private int ironDrop = 1;

        @Var(name = "Or à la mort", desc = "Nombre de lingots d'or lâchés à la mort d'un joueur.", type = VariableType.INTEGER, min = 0)
        private int goldOnDeath = 8;

        @Override public Family getFamily() { return Family.MINAGE; }

        @Override public String getName() { return "Goldless V1"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLD_ORE); }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            if (block.getType() != Material.GOLD_ORE) return;

            ItemStack tool = player.getItemInHand();
            event.setCancelled(true);
            block.setType(Material.AIR);
            MiningScenarios.damageTool(player, tool);

            if (ironDrop <= 0) return;
            Location loc = block.getLocation().clone().add(0.5D, 0.5D, 0.5D);
            loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.IRON_ORE, ironDrop));
        }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            Player dead = uhcPlayer.getPlayer();
            if (dead == null) return;

            Location loc = dead.getLocation();
            ItemStack skull = new ItemCreator(Material.SKULL_ITEM).setDurability((short) 3)
                    .setOwner(dead.getName())
                    .setName(t(HEAD_NAME, dead, Map.of("%player%", dead.getName())))
                    .getItemstack();
            loc.getWorld().dropItemNaturally(loc, skull);

            if (goldOnDeath > 0) {
                loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.GOLD_INGOT, goldOnDeath));
            }
        }
    }

    public static class GoldlessV2Scenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.goldlessv2.desc",
                "§7L'or est §cinutilisable §7: aucun craft à base d'or, aucune consommation dorée.");

        private static final DynamicLang BLOCKED = DynamicLang.of("scenario.goldlessv2.blocked",
                "§cL'or est inutilisable dans cette partie.");

        @Override public Family getFamily() { return Family.MINAGE; }

        @Override public String getName() { return "Goldless V2"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLD_BLOCK); }

        @Override
        public void onCraft(ItemStack result, CraftItemEvent event) {
            if (!isActive()) return;
            for (ItemStack ingredient : event.getInventory().getMatrix()) {
                if (ingredient == null || !GOLD_INGREDIENTS.contains(ingredient.getType())) continue;
                event.setCancelled(true);
                if (event.getWhoClicked() instanceof Player player) {
                    LangManager.get().send(BLOCKED, player);
                    player.updateInventory();
                }
                return;
            }
        }

        @Override
        public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
            if (!isActive()) return;
            if (item == null) return;
            if (item.getType() != Material.GOLDEN_APPLE && item.getType() != Material.GOLDEN_CARROT) return;
            event.setCancelled(true);
            LangManager.get().send(BLOCKED, player);
        }
    }

    public static class MobDiamondsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.mobdiamonds.desc",
                "§7Miner un §bdiamant §7fait apparaître un monstre hostile aléatoire.");

        private static final List<EntityType> HOSTILES = List.of(
                EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.CREEPER,
                EntityType.ENDERMAN, EntityType.SILVERFISH, EntityType.SLIME,
                EntityType.PIG_ZOMBIE, EntityType.MAGMA_CUBE, EntityType.BLAZE);

        private final Random random = new Random();

        @Var(name = "Chance d'apparition", desc = "Chance qu'un monstre apparaisse en minant un diamant.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int spawnChance = 100;

        @Override public Family getFamily() { return Family.MINAGE; }

        @Override public String getName() { return "Mob Diamonds"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.MONSTER_EGG); }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            if (block.getType() != Material.DIAMOND_ORE) return;
            if (!UHCUtils.Rng.chance(spawnChance)) return;

            Location loc = block.getLocation().clone().add(0.5D, 0.5D, 0.5D);
            loc.getWorld().spawnEntity(loc, HOSTILES.get(random.nextInt(HOSTILES.size())));
        }
    }

    public static class OreJackpotScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.orejackpot.desc",
                "§7Un minerai est tiré au sort régulièrement : celui qui en possède le plus rafle la cagnotte.");

        private static final DynamicLang DRAWN = DynamicLang.of("scenario.orejackpot.drawn",
                "§6Ore Jackpot §8» §7Minerai tiré : §e%ore%§7. Le plus riche rafle la cagnotte au prochain tirage !");

        private static final DynamicLang WINNER = DynamicLang.of("scenario.orejackpot.winner",
                "§6Ore Jackpot §8» §e%player% §7remporte la cagnotte avec §e%amount% §7x §e%ore%§7.");

        private static final DynamicLang NO_WINNER = DynamicLang.of("scenario.orejackpot.nowinner",
                "§6Ore Jackpot §8» §7Personne ne possédait de §e%ore%§7, la cagnotte est perdue.");

        private static final DynamicLang ORE_COAL = DynamicLang.of("scenario.orejackpot.ore.coal", "Charbon");

        private static final DynamicLang ORE_IRON = DynamicLang.of("scenario.orejackpot.ore.iron", "Fer");

        private static final DynamicLang ORE_GOLD = DynamicLang.of("scenario.orejackpot.ore.gold", "Or");

        private static final DynamicLang ORE_DIAMOND = DynamicLang.of("scenario.orejackpot.ore.diamond", "Diamant");

        private static final DynamicLang ORE_EMERALD = DynamicLang.of("scenario.orejackpot.ore.emerald", "Émeraude");

        private static final DynamicLang ORE_LAPIS = DynamicLang.of("scenario.orejackpot.ore.lapis", "Lapis-lazuli");

        private static final DynamicLang ORE_REDSTONE = DynamicLang.of("scenario.orejackpot.ore.redstone", "Redstone");

        private static final DynamicLang ORE_QUARTZ = DynamicLang.of("scenario.orejackpot.ore.quartz", "Quartz");

        private final Random random = new Random();

        private BukkitRunnable task;

        private Material drawnOre;

        @Var(name = "Intervalle de tirage", desc = "Temps entre deux tirages de minerai.", type = VariableType.TIME, min = 30)
        private int intervalSec = 600;

        @Var(name = "Diamants de la cagnotte", desc = "Diamants remis au gagnant du tirage.", type = VariableType.INTEGER, min = 0)
        private int rewardDiamonds = 3;

        @Var(name = "Or de la cagnotte", desc = "Lingots d'or remis au gagnant du tirage.", type = VariableType.INTEGER, min = 0)
        private int rewardGold = 8;

        @Override public Family getFamily() { return Family.MINAGE; }

        @Override public String getName() { return "Ore Jackpot"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.EMERALD_ORE); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            long period = Math.max(1L, intervalSec) * 20L;
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isActive()) return;
                    resolveDraw();
                    startDraw();
                }
            };
            task.runTaskTimer(Main.get(), period, period);
        }

        private void startDraw() {
            List<Material> pool = MiningScenarios.ORE_TYPES;
            drawnOre = pool.get(random.nextInt(pool.size()));
            LangManager.get().sendAll(DRAWN, Map.of("%ore%", oreLabel(drawnOre)));
        }

        private String oreLabel(Material ore) {
            DynamicLang label = switch (normalizeOre(ore)) {
                case COAL_ORE -> ORE_COAL;
                case IRON_ORE -> ORE_IRON;
                case GOLD_ORE -> ORE_GOLD;
                case DIAMOND_ORE -> ORE_DIAMOND;
                case EMERALD_ORE -> ORE_EMERALD;
                case LAPIS_ORE -> ORE_LAPIS;
                case REDSTONE_ORE -> ORE_REDSTONE;
                case QUARTZ_ORE -> ORE_QUARTZ;
                default -> null;
            };
            return label == null ? ore.name() : t(label);
        }

        private void resolveDraw() {
            if (drawnOre == null) return;

            UHCPlayer best = null;
            int bestAmount = 0;
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player player = uhcPlayer.getPlayer();
                if (player == null) continue;
                int held = count(player, drawnOre);
                if (held > bestAmount) {
                    bestAmount = held;
                    best = uhcPlayer;
                }
            }

            if (best == null) {
                LangManager.get().sendAll(NO_WINNER, Map.of("%ore%", oreLabel(drawnOre)));
                drawnOre = null;
                return;
            }

            Player winner = best.getPlayer();
            if (rewardDiamonds > 0) PlayerUtils.giveOrDrop(winner, new ItemStack(Material.DIAMOND, rewardDiamonds));
            if (rewardGold > 0) PlayerUtils.giveOrDrop(winner, new ItemStack(Material.GOLD_INGOT, rewardGold));
            winner.getWorld().playSound(winner.getLocation(), Sound.LEVEL_UP, 1f, 1f);
            LangManager.get().sendAll(WINNER, Map.of(
                    "%player%", winner.getName(),
                    "%amount%", bestAmount,
                    "%ore%", oreLabel(drawnOre)));
            drawnOre = null;
        }

        private int count(Player player, Material ore) {
            Material resource = resourceOf(ore);
            int total = 0;
            for (ItemStack stack : player.getInventory().getContents()) {
                if (stack == null) continue;
                Material type = stack.getType();
                if (normalizeOre(type) == normalizeOre(ore)) {
                    total += stack.getAmount();
                    continue;
                }
                if (resource == null || type != resource) continue;
                if (resource == Material.INK_SACK && stack.getDurability() != 4) continue;
                total += stack.getAmount();
            }
            return total;
        }

        @Override
        public void onStop() {
            if (!isActive()) return;
            if (task != null) {
                task.cancel();
                task = null;
            }
            drawnOre = null;
        }
    }

    public static class OreBookScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.orebook.desc",
                "§7Crafter un §flivre §7donne un §dlivre enchanté aléatoire§7.");

        private final Random random = new Random();

        @Var(name = "Niveau minimum", desc = "Niveau minimum de l'enchantement stocké dans le livre.", type = VariableType.INTEGER, min = 1)
        private int minLevel = 1;

        @Var(name = "Niveau maximum", desc = "Niveau maximum de l'enchantement stocké dans le livre.", type = VariableType.INTEGER, min = 1)
        private int maxLevel = 3;

        @Override public Family getFamily() { return Family.MINAGE; }

        @Override public String getName() { return "OreBook"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ENCHANTED_BOOK); }

        @Override
        public void onCraft(ItemStack result, CraftItemEvent event) {
            if (!isActive()) return;
            if (result == null || result.getType() != Material.BOOK) return;

            Enchantment[] pool = Enchantment.values();
            if (pool.length == 0) return;
            Enchantment picked = pool[random.nextInt(pool.length)];

            int low = Math.min(minLevel, maxLevel);
            int high = Math.max(minLevel, maxLevel);
            int level = low + random.nextInt(high - low + 1);

            ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
            book = new ItemCreator(book).addStoredEnchantment(picked, level).getItemstack();
            event.getInventory().setResult(book);
        }
    }

    public static class OreLessScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.oreless.desc",
                "§7Aucun minerai ne génère naturellement dans le monde.");

        @Var(name = "Hauteur maximale", desc = "Hauteur jusqu'à laquelle les minerais générés sont effacés.", type = VariableType.INTEGER, min = 1, max = 255)
        private int maxY = 128;

        @Override public Family getFamily() { return Family.MINAGE; }

        @Override public String getName() { return "OreLess"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.STONE); }

        @EventHandler
        public void onChunkPopulate(ChunkPopulateEvent event) {
            if (!isActive()) return;

            Chunk chunk = event.getChunk();
            World world = chunk.getWorld();
            if (world.equals(Common.get().getLobby())) return;
            Material filler = world.getEnvironment() == Environment.NETHER ? Material.NETHERRACK : Material.STONE;
            ChunkSnapshot snapshot = chunk.getChunkSnapshot();
            int ceiling = Math.min(maxY, world.getMaxHeight() - 1);

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 1; y <= ceiling; y++) {
                        Material type = Material.getMaterial(snapshot.getBlockTypeId(x, y, z));
                        if (type == null || !ORES.contains(type)) continue;
                        chunk.getBlock(x, y, z).setTypeIdAndData(filler.getId(), (byte) 0, false);
                    }
                }
            }
        }
    }

    public static class StoneCrusherScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.stonecrusher.desc",
                "§7Miner de la pierre à la pioche broie un cube autour du bloc. §8(sneak + clic droit pour basculer)");

        private static final DynamicLang TOGGLE_ON = DynamicLang.of("scenario.stonecrusher.on",
                "§aStone Crusher activé.");

        private static final DynamicLang TOGGLE_OFF = DynamicLang.of("scenario.stonecrusher.off",
                "§cStone Crusher désactivé.");

        private final Set<UUID> disabled = new HashSet<>();

        @Var(name = "Rayon de broyage", desc = "Rayon du cube de pierre brisé autour du bloc miné.", type = VariableType.INTEGER, min = 1, max = 3)
        private int crushRadius = 1;

        @Override public Family getFamily() { return Family.MINAGE; }

        @Override public String getName() { return "Stone Crusher"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.STONE_PICKAXE); }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            if (block.getType() != Material.STONE) return;
            if (disabled.contains(player.getUniqueId())) return;

            ItemStack tool = player.getItemInHand();
            if (tool == null || !PICKAXES.contains(tool.getType())) return;

            for (int dx = -crushRadius; dx <= crushRadius; dx++) {
                for (int dy = -crushRadius; dy <= crushRadius; dy++) {
                    for (int dz = -crushRadius; dz <= crushRadius; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        Block around = block.getRelative(dx, dy, dz);
                        if (around.getType() != Material.STONE) continue;
                        around.breakNaturally();
                    }
                }
            }
        }

        @Override
        public void onPlayerInteract(Player player, PlayerInteractEvent event) {
            if (!isActive()) return;
            Action action = event.getAction();
            if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
            if (!player.isSneaking()) return;

            ItemStack item = event.getItem();
            if (item == null || !PICKAXES.contains(item.getType())) return;

            if (disabled.remove(player.getUniqueId())) {
                LangManager.get().send(TOGGLE_ON, player);
                return;
            }
            disabled.add(player.getUniqueId());
            LangManager.get().send(TOGGLE_OFF, player);
        }

        @Override
        public void onStop() {
            if (!isActive()) return;
            disabled.clear();
        }
    }

    public static class VeinMinerScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.veinminer.desc",
                "§7Casser un minerai mine la §eveine entière§7.");

        @Var(name = "Blocs maximum", desc = "Nombre maximum de blocs minés d'un coup dans une veine.", type = VariableType.INTEGER, min = 1)
        private int maxBlocks = 64;

        @Var(name = "Sneak obligatoire", desc = "Exiger que le joueur soit accroupi pour miner la veine.", type = VariableType.BOOLEAN)
        private boolean requireSneak = true;

        @Var(name = "Gravier inclus", desc = "Étendre la veine au gravier miné à la pelle.", type = VariableType.BOOLEAN)
        private boolean includeGravel = true;

        @Override public Family getFamily() { return Family.MINAGE; }

        @Override public String getName() { return "VeinMiner"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.IRON_ORE); }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            if (requireSneak && !player.isSneaking()) return;

            ItemStack tool = player.getItemInHand();
            if (tool == null) return;

            Material type = block.getType();
            boolean gravel = includeGravel && type == Material.GRAVEL && SPADES.contains(tool.getType());
            boolean ore = ORES.contains(type) && PICKAXES.contains(tool.getType());
            if (!gravel && !ore) return;

            for (Block vein : collectVein(block)) {
                vein.breakNaturally();
            }
        }

        private Set<Block> collectVein(Block origin) {
            Material target = normalizeOre(origin.getType());
            Set<Location> visited = new HashSet<>();
            Set<Block> found = new HashSet<>();
            Deque<Block> queue = new ArrayDeque<>();
            queue.add(origin);
            visited.add(origin.getLocation());

            while (!queue.isEmpty() && found.size() < maxBlocks) {
                Block current = queue.poll();
                if (normalizeOre(current.getType()) != target) continue;
                if (current != origin) found.add(current);

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;
                            Block next = current.getRelative(dx, dy, dz);
                            if (visited.add(next.getLocation())) queue.add(next);
                        }
                    }
                }
            }
            return found;
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new GigaDrillScenario(),
                new GoldHunterScenario(),
                new GoldlessV1Scenario(),
                new GoldlessV2Scenario(),
                new MobDiamondsScenario(),
                new OreJackpotScenario(),
                new OreBookScenario(),
                new OreLessScenario(),
                new StoneCrusherScenario(),
                new VeinMinerScenario());
    }
}
