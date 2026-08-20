package net.novaproject.scenarioplus.regles;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.utils.GameStatsTracker;
import net.novaproject.novauhc.player.utils.PlayerUtils;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.normal.MiningScenarios;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.cooldown.CooldownService;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class RegleScenarios {

    private static final double BUKKIT_MAX_HEALTH = 2048.0D;

    private static final short PLAYER_SKULL_DATA = 3;

    private RegleScenarios() {
    }

    private static String blockKey(Block block) {
        return block.getWorld().getName() + ':' + block.getX() + ':' + block.getY() + ':' + block.getZ();
    }

    public static class BlockedV1 extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.blockedv1.desc",
                "§7Tu ne peux pas casser les blocs que §ftu §7as posés. Les autres joueurs le peuvent.");
        private static final DynamicLang OWN_BLOCK = DynamicLang.of("scenario.blockedv1.own_block",
                "§cTu ne peux pas casser un bloc que tu as posé.");

        private final Map<String, UUID> placedBy = new HashMap<>();

        @Override
        public Family getFamily() { return Family.RESTRICTIONS; }

        @Override public String getName() { return "Blocked V1"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BARRIER); }

        @Override
        public void onPlace(Player player, Block block, BlockPlaceEvent event) {
            if (!isActive()) return;
            placedBy.put(blockKey(block), player.getUniqueId());
        }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            String key = blockKey(block);
            UUID owner = placedBy.get(key);
            if (owner == null) return;
            if (owner.equals(player.getUniqueId())) {
                event.setCancelled(true);
                DisplayService.actionBar(player, t(OWN_BLOCK, player));
                return;
            }
            placedBy.remove(key);
        }

        @Override
        public void onStop() {
            placedBy.clear();
        }
    }

    public static class BlockedV2 extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.blockedv2.desc",
                "§7Aucun bloc ne peut être cassé par les joueurs.");
        private static final DynamicLang NO_BREAK = DynamicLang.of("scenario.blockedv2.no_break",
                "§cCasser des blocs est impossible dans cette partie.");

        @Override
        public Family getFamily() { return Family.RESTRICTIONS; }

        @Override public String getName() { return "Blocked V2"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BEDROCK); }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            event.setCancelled(true);
            DisplayService.actionBar(player, t(NO_BREAK, player));
        }
    }

    public static class Drought extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.drought.desc",
                "§7Sécheresse : l'eau ne peut être ni ramassée ni posée.");
        private static final DynamicLang NO_WATER = DynamicLang.of("scenario.drought.no_water",
                "§cL'eau est inutilisable pendant la sécheresse.");

        @Override
        public Family getFamily() { return Family.RESTRICTIONS; }

        @Override public String getName() { return "Drought"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.WATER_BUCKET); }

        @EventHandler
        public void onBucketFill(PlayerBucketFillEvent event) {
            if (!isRunning()) return;
            Block clicked = event.getBlockClicked();
            if (clicked == null) return;
            Material type = clicked.getType();
            if (type != Material.WATER && type != Material.STATIONARY_WATER) return;
            event.setCancelled(true);
            DisplayService.actionBar(event.getPlayer(), t(NO_WATER, event.getPlayer()));
        }

        @EventHandler
        public void onBucketEmpty(PlayerBucketEmptyEvent event) {
            if (!isRunning()) return;
            if (event.getBucket() != Material.WATER_BUCKET) return;
            event.setCancelled(true);
            DisplayService.actionBar(event.getPlayer(), t(NO_WATER, event.getPlayer()));
        }
    }

    public static class NoTrading extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.notrading.desc",
                "§7Le commerce avec les villageois est désactivé.");
        private static final DynamicLang BLOCKED = DynamicLang.of("scenario.notrading.blocked",
                "§cLe commerce avec les villageois est désactivé.");

        @Override
        public Family getFamily() { return Family.RESTRICTIONS; }

        @Override public String getName() { return "No Trading"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.EMERALD); }

        @Override
        public void onPlayerInteractEntity(Player player, PlayerInteractEntityEvent event) {
            if (!isActive()) return;
            if (event.getRightClicked() == null) return;
            if (event.getRightClicked().getType() != EntityType.VILLAGER) return;
            event.setCancelled(true);
            DisplayService.actionBar(player, t(BLOCKED, player));
        }
    }

    public static class Statless extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.statless.desc",
                "§7Aucune statistique de partie n'est enregistrée.");

        @Override
        public Family getFamily() { return Family.RESTRICTIONS; }

        @Override public String getName() { return "Statless"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BOOK); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            UHCManager.get().setStatsTracker(new SilentStatsTracker());
        }

        private static final class SilentStatsTracker extends GameStatsTracker {

            @Override
            public void addPlayer(UUID uuid, String name) {
            }
        }
    }

    public static class AppleFamine extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.applefamine.desc",
                "§7Les feuilles ne donnent plus jamais de pommes.");

        private final Set<String> leafBlocks = new HashSet<>();

        @Override
        public Family getFamily() { return Family.NOURRITURE; }

        @Override public String getName() { return "Apple Famine"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.APPLE); }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            markLeaf(block);
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onLeavesDecay(LeavesDecayEvent event) {
            if (!isRunning()) return;
            markLeaf(event.getBlock());
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onItemSpawn(ItemSpawnEvent event) {
            if (!isRunning()) return;
            ItemStack stack = event.getEntity().getItemStack();
            if (stack == null || stack.getType() != Material.APPLE) return;
            if (!leafBlocks.contains(blockKey(event.getEntity().getLocation().getBlock()))) return;
            event.setCancelled(true);
        }

        private void markLeaf(Block block) {
            Material type = block.getType();
            if (type != Material.LEAVES && type != Material.LEAVES_2) return;
            if (leafBlocks.isEmpty()) {
                Bukkit.getScheduler().runTask(Main.get(), leafBlocks::clear);
            }
            leafBlocks.add(blockKey(block));
        }

        @Override
        public void onStop() {
            leafBlocks.clear();
        }
    }

    public static class Cannibalism extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.cannibalism.desc",
                "§7Consommer une tête de joueur (clic droit) donne des cœurs vides permanents.");
        private static final DynamicLang EATEN = DynamicLang.of("scenario.cannibalism.eaten",
                "§cTu dévores la tête et gagnes §f%hearts% §ccœur(s) vide(s).");

        @Var(name = "Cœurs par tête", desc = "Nombre de cœurs vides permanents gagnés par tête consommée.",
                type = VariableType.INTEGER, min = 1)
        private int heartsPerHead = 1;

        @Override
        public Family getFamily() { return Family.NOURRITURE; }

        @Override public String getName() { return "Cannibalism"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override
        public ItemCreator getItem() {
            return new ItemCreator(Material.SKULL_ITEM).setDurability(PLAYER_SKULL_DATA);
        }

        @Override
        public void onPlayerInteract(Player player, PlayerInteractEvent event) {
            if (!isActive()) return;
            Action action = event.getAction();
            if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
            ItemStack item = event.getItem();
            if (item == null || item.getType() != Material.SKULL_ITEM) return;
            if (item.getDurability() != PLAYER_SKULL_DATA) return;
            event.setCancelled(true);
            ItemCreator.consumeOne(player, item);
            player.setMaxHealth(Math.min(BUKKIT_MAX_HEALTH, player.getMaxHealth() + heartsPerHead * 2.0D));
            DisplayService.actionBar(player, t(EATEN, player, Map.of("%hearts%", heartsPerHead)));
        }
    }

    public static class Choke extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.choke.desc",
                "§7Chaque aliment consommé peut te faire t'étouffer : Wither définitif.");
        private static final DynamicLang CHOKED = DynamicLang.of("scenario.choke.choked",
                "§4Tu t'étouffes ! Le Wither ne te quittera plus.");

        @Var(name = "Chance d'étouffement", desc = "Probabilité de s'étouffer à chaque aliment consommé.",
                type = VariableType.PERCENTAGE, min = 0, max = 1)
        private double chokeChance = 0.01D;

        @Var(name = "Niveau de Wither", desc = "Niveau de l'effet Wither appliqué en permanence.",
                type = VariableType.INTEGER, min = 1)
        private int witherLevel = 2;

        private final Set<UUID> choked = new HashSet<>();

        private final Random random = new Random();

        @Override
        public Family getFamily() { return Family.NOURRITURE; }

        @Override public String getName() { return "Choke"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ROTTEN_FLESH); }

        @Override
        public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
            if (!isActive()) return;
            if (item == null || !item.getType().isEdible()) return;
            if (choked.contains(player.getUniqueId())) return;
            if (random.nextDouble() >= chokeChance) return;
            choked.add(player.getUniqueId());
            player.sendMessage(t(CHOKED, player));
        }

        @Override
        public void onSec(Player p) {
            if (!isActive()) return;
            if (!choked.contains(p.getUniqueId())) return;
            UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                    new PotionEffect(PotionEffectType.WITHER, UHCUtils.LOOPED_EFFECT_DURATION_TICKS, witherLevel - 1)
            }, p);
        }

        @Override
        public void onStop() {
            choked.clear();
        }
    }

    public static class LapisConsumption extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.lapisconsumption.desc",
                "§7Miner du lapis-lazuli donne un aliment aléatoire à la place du minerai.");

        private static final List<Material> FOODS = List.of(
                Material.APPLE,
                Material.BREAD,
                Material.COOKED_BEEF,
                Material.COOKED_CHICKEN,
                Material.COOKED_FISH,
                Material.GRILLED_PORK,
                Material.BAKED_POTATO,
                Material.CARROT_ITEM,
                Material.MELON,
                Material.COOKIE,
                Material.PUMPKIN_PIE,
                Material.MUSHROOM_SOUP
        );

        @Var(name = "Quantité d'aliments", desc = "Nombre d'aliments droppés par minerai de lapis.",
                type = VariableType.INTEGER, min = 1)
        private int foodAmount = 1;

        private final Random random = new Random();

        @Override
        public Family getFamily() { return Family.NOURRITURE; }

        @Override public String getName() { return "Lapis Consumption"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.LAPIS_ORE); }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            if (block.getType() != Material.LAPIS_ORE) return;
            event.setCancelled(true);
            block.setType(Material.AIR);
            MiningScenarios.damageTool(player, player.getItemInHand());
            Location drop = block.getLocation().clone().add(0.5D, 0.5D, 0.5D);
            drop.getWorld().dropItemNaturally(drop,
                    new ItemStack(FOODS.get(random.nextInt(FOODS.size())), foodAmount));
        }
    }

    public static class Refill extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.refill.desc",
                "§7Un kill rapporte de l'or : plus tu es bas en vie, plus la récompense est grosse.");
        private static final DynamicLang REWARD = DynamicLang.of("scenario.refill.reward",
                "§6+%amount% lingot(s) d'or §7pour ce kill.");

        @Var(name = "Or maximum", desc = "Lingots d'or donnés au tueur lorsqu'il est au plus bas en vie.",
                type = VariableType.INTEGER, min = 0)
        private int maxGold = 10;

        @Override
        public Family getFamily() { return Family.NOURRITURE; }

        @Override public String getName() { return "Refill"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLD_INGOT); }

        @Override
        public void onKill(UHCPlayer killer, UHCPlayer victim) {
            if (!isActive()) return;
            if (killer == null) return;
            Player player = killer.getPlayer();
            if (player == null) return;
            double maxHealth = player.getMaxHealth();
            double missing = maxHealth <= 0 ? 1.0D : 1.0D - (player.getHealth() / maxHealth);
            int amount = (int) Math.round(maxGold * missing);
            if (amount <= 0) return;
            PlayerUtils.giveOrDrop(player, new ItemStack(Material.GOLD_INGOT, amount));
            DisplayService.actionBar(player, t(REWARD, player, Map.of("%amount%", amount)));
        }
    }

    public static class StrongRecovery extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.strongrecovery.desc",
                "§7Après un kill, le tueur obtient la Force pendant quelques secondes.");
        private static final DynamicLang GRANTED = DynamicLang.of("scenario.strongrecovery.granted",
                "§cForce §7accordée pendant §f%seconds%s§7.");

        @Var(name = "Durée de la Force", desc = "Durée de l'effet Force accordé au tueur.",
                type = VariableType.TIME, min = 1)
        private int strengthDurationSec = 15;

        @Var(name = "Niveau de Force", desc = "Niveau de l'effet Force accordé au tueur.",
                type = VariableType.INTEGER, min = 1)
        private int strengthLevel = 1;

        @Override
        public Family getFamily() { return Family.NOURRITURE; }

        @Override public String getName() { return "Strong Recovery"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BLAZE_POWDER); }

        @Override
        public void onKill(UHCPlayer killer, UHCPlayer victim) {
            if (!isActive()) return;
            if (killer == null) return;
            Player player = killer.getPlayer();
            if (player == null) return;
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,
                    strengthDurationSec * 20, strengthLevel - 1), true);
            DisplayService.actionBar(player, t(GRANTED, player, Map.of("%seconds%", strengthDurationSec)));
        }
    }

    public static class SugarRush extends Scenario {

        private static final String COOLDOWN_KEY = "SugarRushCooldown";

        private static final DynamicLang DESC = DynamicLang.of("scenario.sugarrush.desc",
                "§7Le sucre se consomme (clic droit) et accorde un boost de Vitesse.");
        private static final DynamicLang ON_COOLDOWN = DynamicLang.of("scenario.sugarrush.cooldown",
                "§cEncore §f%seconds%s §cavant de reprendre du sucre.");

        @Var(name = "Durée du boost", desc = "Durée de l'effet Vitesse accordé par le sucre.",
                type = VariableType.TIME, min = 1)
        private int speedDurationSec = 4;

        @Var(name = "Niveau de Vitesse", desc = "Niveau de l'effet Vitesse accordé par le sucre.",
                type = VariableType.INTEGER, min = 1)
        private int speedLevel = 2;

        @Var(name = "Recharge", desc = "Temps d'attente avant de pouvoir reconsommer du sucre.",
                type = VariableType.TIME, min = 0)
        private int cooldownSec = 15;

        @Override
        public Family getFamily() { return Family.NOURRITURE; }

        @Override public String getName() { return "Sugar Rush"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SUGAR); }

        @Override
        public void onPlayerInteract(Player player, PlayerInteractEvent event) {
            if (!isActive()) return;
            Action action = event.getAction();
            if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
            ItemStack item = event.getItem();
            if (item == null || item.getType() != Material.SUGAR) return;
            event.setCancelled(true);
            long remaining = CooldownService.get(player, COOLDOWN_KEY);
            if (remaining > 0) {
                DisplayService.actionBar(player,
                        t(ON_COOLDOWN, player, Map.of("%seconds%", (remaining + 999L) / 1000L)));
                return;
            }
            ItemCreator.consumeOne(player, item);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
                    speedDurationSec * 20, speedLevel - 1), true);
            CooldownService.put(player, COOLDOWN_KEY, cooldownSec * 1000L);
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new BlockedV1(),
                new BlockedV2(),
                new Drought(),
                new NoTrading(),
                new Statless(),
                new AppleFamine(),
                new Cannibalism(),
                new Choke(),
                new LapisConsumption(),
                new Refill(),
                new StrongRecovery(),
                new SugarRush()
        );
    }
}
