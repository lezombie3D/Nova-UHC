package net.novaproject.scenarioplus.mobs;

import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.normal.MiningScenarios;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class MobSpawnScenarios {

    private MobSpawnScenarios() {
    }

    public static class DogInTheFamilyScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.adoginthefamily.desc",
                "§7Chaque joueur démarre avec un chien apprivoisé : si le chien meurt, son maître meurt avec lui.");

        private static final DynamicLang DOG_DEAD = DynamicLang.of("scenario.adoginthefamily.dog-dead",
                "§e%joueur% §7n'a pas survécu à la mort de son chien.");

        private final Map<UUID, UUID> dogs = new HashMap<>();

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "A Dog in the Family"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BONE); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            Wolf wolf = player.getWorld().spawn(player.getLocation(), Wolf.class);
            wolf.setTamed(true);
            wolf.setOwner(player);
            dogs.put(wolf.getUniqueId(), player.getUniqueId());
        }

        @Override
        public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
            if (!isActive()) return;
            UUID ownerId = dogs.remove(entity.getUniqueId());
            if (ownerId == null) return;

            UHCPlayer owner = UHCPlayerManager.get().getPlayer(ownerId);
            if (owner == null || !owner.isPlaying()) return;

            Player player = owner.getPlayer();
            if (player == null || !player.isOnline()) return;

            LangManager.get().sendAll(DOG_DEAD, Map.of("%joueur%", player.getName()));
            player.setHealth(0.0D);
        }

        @Override
        public void onStop() {
            dogs.clear();
        }
    }

    public static class AnimalLeatherScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.animalleather.desc",
                "§7Cochons et moutons lâchent du cuir dans §e%betail%% §7des cas, les poules dans §e%poule%% §7des cas.");

        private final Random random = new Random();

        @Var(name = "Chance cochon/mouton", desc = "Chance qu'un cochon ou un mouton lâche du cuir à sa mort.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int betailChance = 20;

        @Var(name = "Chance poule", desc = "Chance qu'une poule lâche du cuir à sa mort.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int pouleChance = 10;

        @Var(name = "Cuir lâché", desc = "Nombre de cuirs lâchés quand le tirage est gagnant.", type = VariableType.INTEGER, min = 1)
        private int quantite = 1;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Animal Leather"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%betail%", betailChance, "%poule%", pouleChance));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.LEATHER); }

        @Override
        public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
            if (!isActive()) return;

            int chance;
            if (entity instanceof Pig || entity instanceof Sheep) chance = betailChance;
            else if (entity instanceof Chicken) chance = pouleChance;
            else return;

            if (!UHCUtils.Rng.chance(chance)) return;
            event.getDrops().add(new ItemStack(Material.LEATHER, quantite));
        }
    }

    public static class BaldChickenScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.baldchicken.desc",
                "§7Les poules ne lâchent plus de plumes ; les squelettes lâchent §e%min% §7à §e%max% flèches§7.");

        private final Random random = new Random();

        @Var(name = "Flèches minimum", desc = "Nombre minimum de flèches lâchées par un squelette.", type = VariableType.INTEGER, min = 0)
        private int flechesMin = 3;

        @Var(name = "Flèches maximum", desc = "Nombre maximum de flèches lâchées par un squelette.", type = VariableType.INTEGER, min = 1)
        private int flechesMax = 5;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Bald Chicken"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%min%", flechesMin, "%max%", flechesMax));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.FEATHER); }

        @Override
        public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
            if (!isActive()) return;

            if (entity instanceof Chicken) {
                event.getDrops().removeIf(drop -> drop != null && drop.getType() == Material.FEATHER);
                return;
            }
            if (!(entity instanceof Skeleton)) return;

            event.getDrops().removeIf(drop -> drop != null && drop.getType() == Material.ARROW);
            int low = Math.min(flechesMin, flechesMax);
            int high = Math.max(flechesMin, flechesMax);
            int amount = low + random.nextInt(high - low + 1);
            if (amount <= 0) return;
            event.getDrops().add(new ItemStack(Material.ARROW, amount));
        }
    }

    public static class BarebonesV1Scenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.barebonesv1.desc",
                "§7Le fer est le palier maximum : or et diamant ne se minent plus, enclume et table d'enchantement sont interdites, et chaque mort lâche §b%diamants% diamant(s)§7.");

        @Var(name = "Diamants à la mort", desc = "Nombre de diamants lâchés en plus par un joueur qui meurt.", type = VariableType.INTEGER, min = 0)
        private int diamants = 1;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Barebones V1"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%diamants%", diamants));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.IRON_INGOT); }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            if (block.getType() != Material.GOLD_ORE && block.getType() != Material.DIAMOND_ORE) return;

            event.setCancelled(true);
            block.setType(Material.AIR);
            MiningScenarios.damageTool(player, player.getItemInHand());
        }

        @Override
        public void onCraft(ItemStack result, CraftItemEvent event) {
            if (!isActive()) return;
            if (result == null) return;
            if (result.getType() != Material.ANVIL && result.getType() != Material.ENCHANTMENT_TABLE) return;
            event.setCancelled(true);
        }

        @Override
        public void onPlayerInteract(Player player, PlayerInteractEvent event) {
            if (!isActive()) return;
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

            Block block = event.getClickedBlock();
            if (block == null) return;
            if (block.getType() != Material.ANVIL && block.getType() != Material.ENCHANTMENT_TABLE) return;
            event.setCancelled(true);
        }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            if (diamants <= 0) return;
            event.getDrops().add(new ItemStack(Material.DIAMOND, diamants));
        }
    }

    public static class BarebonesV2Scenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.barebonesv2.desc",
                "§7L'or se craft avec §f%fer% lingots de fer §7et le diamant avec §6%or% lingots d'or§7. Chaque mort lâche en plus diamant, flèches et fil.");

        @Var(name = "Fer par lingot d'or", desc = "Nombre de lingots de fer nécessaires pour crafter un lingot d'or.", type = VariableType.INTEGER, min = 1, max = 9)
        private int ferParOr = 4;

        @Var(name = "Or par diamant", desc = "Nombre de lingots d'or nécessaires pour crafter un diamant.", type = VariableType.INTEGER, min = 1, max = 9)
        private int orParDiamant = 4;

        @Var(name = "Diamants à la mort", desc = "Nombre de diamants lâchés en plus par un joueur qui meurt.", type = VariableType.INTEGER, min = 0)
        private int diamants = 1;

        @Var(name = "Flèches à la mort", desc = "Nombre de flèches lâchées en plus par un joueur qui meurt.", type = VariableType.INTEGER, min = 0)
        private int fleches = 16;

        @Var(name = "Fil à la mort", desc = "Nombre de fils lâchés en plus par un joueur qui meurt.", type = VariableType.INTEGER, min = 0)
        private int fil = 8;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Barebones V2"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%fer%", ferParOr, "%or%", orParDiamant));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLD_INGOT); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;

            ShapelessRecipe or = new ShapelessRecipe(new ItemStack(Material.GOLD_INGOT));
            or.addIngredient(ferParOr, Material.IRON_INGOT);
            Bukkit.addRecipe(or);

            ShapelessRecipe diamant = new ShapelessRecipe(new ItemStack(Material.DIAMOND));
            diamant.addIngredient(orParDiamant, Material.GOLD_INGOT);
            Bukkit.addRecipe(diamant);
        }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            if (diamants > 0) event.getDrops().add(new ItemStack(Material.DIAMOND, diamants));
            if (fleches > 0) event.getDrops().add(new ItemStack(Material.ARROW, fleches));
            if (fil > 0) event.getDrops().add(new ItemStack(Material.STRING, fil));
        }
    }

    public static class BatceptionScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.batception.desc",
                "§7Tuer une chauve-souris donne un livre enchanté, avec §c%chance%% §7de chances de te tuer sur le coup.");

        private final Random random = new Random();

        @Var(name = "Chance de mort", desc = "Chance que tuer une chauve-souris tue le joueur au lieu de lâcher un livre.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int mortChance = 5;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Batception"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%chance%", mortChance));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ENCHANTED_BOOK); }

        @Override
        public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
            if (!isActive()) return;
            if (!(entity instanceof Bat) || killer == null) return;

            if (UHCUtils.Rng.chance(mortChance)) {
                killer.setHealth(0.0D);
                return;
            }

            Enchantment[] pool = Enchantment.values();
            Enchantment enchantment = pool[random.nextInt(pool.length)];
            int level = 1 + random.nextInt(Math.max(1, enchantment.getMaxLevel()));
            Location loc = entity.getLocation();
            loc.getWorld().dropItemNaturally(loc, new ItemCreator(Material.ENCHANTED_BOOK)
                    .addStoredEnchantment(enchantment, level)
                    .getItemstack());
        }
    }

    public static class BombersScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.bombers.desc",
                "§7Tout le monde démarre avec un briquet incassable et les monstres lâchent de la TNT dans §e%chance%% §7des cas.");

        private final Random random = new Random();

        @Var(name = "Chance de TNT", desc = "Chance qu'un monstre tué lâche de la TNT.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int tntChance = 50;

        @Var(name = "TNT lâchée", desc = "Nombre de TNT lâchées quand le tirage est gagnant.", type = VariableType.INTEGER, min = 1)
        private int tntQuantite = 1;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Bombers"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%chance%", tntChance));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.FLINT_AND_STEEL); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            player.getInventory().addItem(new ItemCreator(Material.FLINT_AND_STEEL).getItemstack());
        }

        @Override
        public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
            if (!isActive()) return;
            if (!(entity instanceof Monster) || killer == null) return;
            if (!UHCUtils.Rng.chance(tntChance)) return;
            event.getDrops().add(new ItemStack(Material.TNT, tntQuantite));
        }

        @EventHandler
        public void onItemDamage(PlayerItemDamageEvent event) {
            if (!isRunning()) return;
            ItemStack item = event.getItem();
            if (item == null || item.getType() != Material.FLINT_AND_STEEL) return;
            event.setCancelled(true);
        }
    }

    public static class CircusScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.circus.desc",
                "§7Un mob tué lâche son propre œuf d'apparition dans §e%chance%% §7des cas.");

        private final Random random = new Random();

        @Var(name = "Chance d'œuf", desc = "Chance qu'un mob tué lâche son œuf d'apparition.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chance = 25;

        @Var(name = "Œufs lâchés", desc = "Nombre d'œufs d'apparition lâchés quand le tirage est gagnant.", type = VariableType.INTEGER, min = 1)
        private int quantite = 1;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Circus"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%chance%", chance));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.MONSTER_EGG); }

        @Override
        public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
            if (!isActive()) return;
            if (entity instanceof Player || killer == null) return;
            if (!UHCUtils.Rng.chance(chance)) return;

            short typeId = entity.getType().getTypeId();
            if (typeId <= 0) return;
            event.getDrops().add(new ItemStack(Material.MONSTER_EGG, quantite, typeId));
        }
    }

    public static class CryophobiaScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.cryophobia.desc",
                "§7Les squelettes troquent leurs flèches pour des boules de neige qui infligent §c%degats% §7de dégâts.");

        @Var(name = "Dégâts de boule de neige", desc = "Dégâts infligés par une boule de neige de squelette.", type = VariableType.DOUBLE, min = 0, max = 40)
        private double degats = 4.0D;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Cryophobia"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%degats%", degats));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SNOW_BALL); }

        @EventHandler
        public void onSkeletonShoot(EntityShootBowEvent event) {
            if (!isRunning()) return;
            if (!(event.getEntity() instanceof Skeleton skeleton)) return;

            event.setCancelled(true);
            Snowball ball = skeleton.launchProjectile(Snowball.class);
            ball.setVelocity(event.getProjectile().getVelocity());
        }

        @Override
        public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (!(damager instanceof Snowball ball)) return;
            if (!(ball.getShooter() instanceof Skeleton)) return;
            event.setDamage(degats);
        }
    }

    public static class DepthsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.depths.desc",
                "§7Les monstres frappent plus fort en profondeur : §c×%m1% §7sous §eY=%y1%§7, §c×%m2% §7sous §eY=%y2%§7, §c×%m3% §7sous §eY=%y3% §7et §c×%m4% §7sous §eY=%y4%§7.");

        @Var(name = "Palier 1 (Y)", desc = "Altitude sous laquelle le premier multiplicateur de dégâts s'applique.", type = VariableType.INTEGER, min = 0, max = 256)
        private int palier1 = 60;

        @Var(name = "Multiplicateur palier 1", desc = "Multiplicateur de dégâts des monstres sous le palier 1.", type = VariableType.DOUBLE, min = 1, max = 20)
        private double multiplicateur1 = 1.5D;

        @Var(name = "Palier 2 (Y)", desc = "Altitude sous laquelle le deuxième multiplicateur de dégâts s'applique.", type = VariableType.INTEGER, min = 0, max = 256)
        private int palier2 = 45;

        @Var(name = "Multiplicateur palier 2", desc = "Multiplicateur de dégâts des monstres sous le palier 2.", type = VariableType.DOUBLE, min = 1, max = 20)
        private double multiplicateur2 = 2.0D;

        @Var(name = "Palier 3 (Y)", desc = "Altitude sous laquelle le troisième multiplicateur de dégâts s'applique.", type = VariableType.INTEGER, min = 0, max = 256)
        private int palier3 = 30;

        @Var(name = "Multiplicateur palier 3", desc = "Multiplicateur de dégâts des monstres sous le palier 3.", type = VariableType.DOUBLE, min = 1, max = 20)
        private double multiplicateur3 = 3.0D;

        @Var(name = "Palier 4 (Y)", desc = "Altitude sous laquelle le quatrième multiplicateur de dégâts s'applique.", type = VariableType.INTEGER, min = 0, max = 256)
        private int palier4 = 15;

        @Var(name = "Multiplicateur palier 4", desc = "Multiplicateur de dégâts des monstres sous le palier 4.", type = VariableType.DOUBLE, min = 1, max = 20)
        private double multiplicateur4 = 5.0D;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Depths"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of(
                    "%y1%", palier1, "%m1%", multiplicateur1,
                    "%y2%", palier2, "%m2%", multiplicateur2,
                    "%y3%", palier3, "%m3%", multiplicateur3,
                    "%y4%", palier4, "%m4%", multiplicateur4));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BEDROCK); }

        @Override
        public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (!(entity instanceof Player player)) return;

            boolean fromMob = damager instanceof Monster
                    || (damager instanceof Projectile projectile && projectile.getShooter() instanceof Monster);
            if (!fromMob) return;

            int y = player.getLocation().getBlockY();
            double multiplier = 1.0D;
            if (y < palier1) multiplier = multiplicateur1;
            if (y < palier2) multiplier = multiplicateur2;
            if (y < palier3) multiplier = multiplicateur3;
            if (y < palier4) multiplier = multiplicateur4;
            if (multiplier <= 1.0D) return;

            event.setDamage(event.getDamage() * multiplier);
        }
    }

    public static class DoNotDisturbScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.donotdisturb.desc",
                "§7Frapper un joueur vous enferme tous les deux en duel pendant §e%duel% s §7dans un rayon de §e%rayon% blocs§7 : personne d'autre ne peut s'en mêler, et le butin du perdant est réservé au vainqueur pendant §e%butin% s§7.");

        private static final DynamicLang INTERFERENCE = DynamicLang.of("scenario.donotdisturb.interference",
                "§cCe duel ne te concerne pas.");

        private static final DynamicLang LOOT_LOCKED = DynamicLang.of("scenario.donotdisturb.loot-locked",
                "§cCe butin est réservé au vainqueur du duel.");

        private final Map<UUID, UUID> duel = new HashMap<>();

        private final Map<UUID, Long> duelUntil = new HashMap<>();

        private final Map<UUID, UUID> lootOwner = new HashMap<>();

        private final Map<UUID, Location> lootZone = new HashMap<>();

        @Var(name = "Rayon du duel", desc = "Distance maximale, en blocs, à laquelle le duel bloque les intrus.", type = VariableType.INTEGER, min = 1)
        private int rayon = 100;

        @Var(name = "Durée du duel", desc = "Durée pendant laquelle deux combattants restent enfermés en duel.", type = VariableType.TIME, min = 1)
        private int duelSec = 15;

        @Var(name = "Protection du butin", desc = "Durée pendant laquelle seul le vainqueur peut ramasser le butin du perdant.", type = VariableType.TIME, min = 1)
        private int butinSec = 60;

        @Var(name = "Rayon du butin", desc = "Rayon, en blocs, de la zone de butin protégée autour du corps.", type = VariableType.INTEGER, min = 1)
        private int butinRayon = 10;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "DoNotDisturb"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%duel%", duelSec, "%rayon%", rayon, "%butin%", butinSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.IRON_FENCE); }

        @Override
        public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (!(entity instanceof Player victim)) return;

            Player attacker = null;
            if (damager instanceof Player direct) attacker = direct;
            else if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) attacker = shooter;
            if (attacker == null || attacker == victim) return;

            UUID attackerId = attacker.getUniqueId();
            UUID victimId = victim.getUniqueId();
            UUID attackerPartner = partner(attackerId);
            UUID victimPartner = partner(victimId);

            boolean intrusion = (attackerPartner != null && !attackerPartner.equals(victimId))
                    || (victimPartner != null && !victimPartner.equals(attackerId));

            if (intrusion) {
                if (!attacker.getWorld().equals(victim.getWorld())) return;
                if (attacker.getLocation().distanceSquared(victim.getLocation()) > (double) rayon * rayon) return;
                event.setCancelled(true);
                DisplayService.actionBar(attacker, t(INTERFERENCE, attacker));
                return;
            }

            long until = System.currentTimeMillis() + duelSec * 1000L;
            duel.put(attackerId, victimId);
            duel.put(victimId, attackerId);
            duelUntil.put(attackerId, until);
            duelUntil.put(victimId, until);
        }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;

            UUID victimId = uhcPlayer.getUniqueId();
            UUID adversaire = duel.remove(victimId);
            duelUntil.remove(victimId);
            if (adversaire != null) {
                duel.remove(adversaire);
                duelUntil.remove(adversaire);
            }

            if (killer == null) return;
            lootOwner.put(victimId, killer.getUniqueId());
            lootZone.put(victimId, event.getEntity().getLocation());

            new BukkitRunnable() {
                @Override
                public void run() {
                    lootOwner.remove(victimId);
                    lootZone.remove(victimId);
                }
            }.runTaskLater(Main.get(), butinSec * 20L);
        }

        @Override
        public void onPickUp(Player player, Item item, PlayerPickupItemEvent event) {
            if (!isActive()) return;

            Location loc = item.getLocation();
            for (Map.Entry<UUID, Location> entry : lootZone.entrySet()) {
                Location zone = entry.getValue();
                if (!zone.getWorld().equals(loc.getWorld())) continue;
                if (zone.distanceSquared(loc) > (double) butinRayon * butinRayon) continue;

                UUID owner = lootOwner.get(entry.getKey());
                if (owner == null || owner.equals(player.getUniqueId())) continue;

                event.setCancelled(true);
                DisplayService.actionBar(player, t(LOOT_LOCKED, player));
                return;
            }
        }

        @Override
        public void onStop() {
            duel.clear();
            duelUntil.clear();
            lootOwner.clear();
            lootZone.clear();
        }

        private UUID partner(UUID uuid) {
            Long until = duelUntil.get(uuid);
            if (until == null || until < System.currentTimeMillis()) return null;
            return duel.get(uuid);
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new DogInTheFamilyScenario(),
                new AnimalLeatherScenario(),
                new BaldChickenScenario(),
                new BarebonesV1Scenario(),
                new BarebonesV2Scenario(),
                new BatceptionScenario(),
                new BombersScenario(),
                new CircusScenario(),
                new CryophobiaScenario(),
                new DepthsScenario(),
                new DoNotDisturbScenario()
        );
    }
}
