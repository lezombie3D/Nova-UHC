package net.novaproject.scenarioplus.mobs;

import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.Common;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Giant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Spider;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class MobDropScenarios {

    private MobDropScenarios() {
    }

    public static class EggsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.eggs.desc",
                "§7Tu démarres avec §e%oeufs% œuf(s)§7 : chaque œuf lancé fait naître un mob au hasard. Les poules en lâchent dans §e%chance%%% §7des cas.");

        private static final EntityType[] MOBS = {
                EntityType.ZOMBIE,
                EntityType.SKELETON,
                EntityType.SPIDER,
                EntityType.CAVE_SPIDER,
                EntityType.CREEPER,
                EntityType.ENDERMAN,
                EntityType.WITCH,
                EntityType.SILVERFISH,
                EntityType.SLIME,
                EntityType.MAGMA_CUBE,
                EntityType.BLAZE,
                EntityType.PIG_ZOMBIE,
                EntityType.CHICKEN,
                EntityType.COW,
                EntityType.PIG,
                EntityType.SHEEP,
                EntityType.WOLF,
                EntityType.RABBIT,
                EntityType.MUSHROOM_COW,
                EntityType.HORSE,
                EntityType.BAT
        };

        private final Random random = new Random();

        @Var(name = "Œufs de départ", desc = "Nombre d'œufs donnés à chaque joueur au début de la partie.", type = VariableType.INTEGER, min = 0)
        private int oeufsDepart = 3;

        @Var(name = "Chance de la poule", desc = "Chance qu'une poule tuée lâche des œufs.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chancePoule = 20;

        @Var(name = "Œufs par poule", desc = "Nombre d'œufs lâchés par une poule tuée.", type = VariableType.INTEGER, min = 1)
        private int oeufsParPoule = 1;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Eggs"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%oeufs%", oeufsDepart, "%chance%", chancePoule));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.EGG); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            if (oeufsDepart <= 0) return;
            player.getInventory().addItem(new ItemStack(Material.EGG, oeufsDepart));
        }

        @Override
        public void onProjectileHit(ProjectileHitEvent event) {
            if (!isActive()) return;
            if (!(event.getEntity() instanceof Egg egg)) return;
            Location loc = egg.getLocation();
            loc.getWorld().spawnEntity(loc, MOBS[random.nextInt(MOBS.length)]);
        }

        @Override
        public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
            if (!isActive()) return;
            if (!(entity instanceof Chicken)) return;
            if (!UHCUtils.Rng.chance(chancePoule)) return;
            event.getDrops().add(new ItemStack(Material.EGG, oeufsParPoule));
        }
    }

    public static class EightLeggedFreaksScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.eightleggedfreaks.desc",
                "§7Tous les monstres sont remplacés par des araignées, dont §e%grotte%%% §7d'araignées de grotte, chacune dotée d'un pouvoir.");

        private static final PotionEffectType[] POUVOIRS = {
                PotionEffectType.SPEED,
                PotionEffectType.INCREASE_DAMAGE,
                PotionEffectType.JUMP,
                PotionEffectType.INVISIBILITY
        };

        private final Random random = new Random();

        @Var(name = "Chance d'araignée de grotte", desc = "Chance que l'araignée de remplacement soit une araignée de grotte.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceGrotte = 30;

        @Var(name = "Durée du pouvoir", desc = "Durée de l'effet accordé à chaque araignée.", type = VariableType.TIME, min = 1)
        private int dureePouvoirSec = 3600;

        @Var(name = "Niveau du pouvoir", desc = "Niveau de l'effet accordé à chaque araignée.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauPouvoir = 2;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Eight Legged Freaks"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%grotte%", chanceGrotte));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SPIDER_EYE); }

        @EventHandler
        public void onSpawn(CreatureSpawnEvent event) {
            if (!isRunning()) return;
            if (event.getSpawnReason() == SpawnReason.CUSTOM) return;
            LivingEntity entity = event.getEntity();
            if (entity instanceof Spider) return;
            if (!(entity instanceof Monster)) return;

            event.setCancelled(true);
            Location loc = entity.getLocation();
            boolean grotte = UHCUtils.Rng.chance(chanceGrotte);
            LivingEntity araignee = (LivingEntity) loc.getWorld().spawnEntity(loc,
                    grotte ? EntityType.CAVE_SPIDER : EntityType.SPIDER);
            araignee.addPotionEffect(new PotionEffect(POUVOIRS[random.nextInt(POUVOIRS.length)],
                    dureePouvoirSec * 20, niveauPouvoir - 1), true);
        }
    }

    public static class ExplosiveMobsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.explosivemobs.desc",
                "§7Un mob tué a §e%chance%%% §7de chances de lâcher de la TNT déjà amorcée.");

        private final Random random = new Random();

        @Var(name = "Chance de TNT", desc = "Chance qu'un mob tué lâche de la TNT amorcée.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chance = 100;

        @Var(name = "TNT lâchées", desc = "Nombre de TNT amorcées apparaissant sur le cadavre.", type = VariableType.INTEGER, min = 1)
        private int nbTnt = 1;

        @Var(name = "Mèche", desc = "Durée de la mèche de la TNT amorcée, en ticks.", type = VariableType.INTEGER, min = 1)
        private int fuseTicks = 60;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Explosive Mobs"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%chance%", chance));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.TNT); }

        @Override
        public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
            if (!isActive()) return;
            if (entity instanceof Player) return;
            if (!UHCUtils.Rng.chance(chance)) return;

            Location loc = entity.getLocation();
            for (int i = 0; i < nbTnt; i++) {
                TNTPrimed tnt = loc.getWorld().spawn(loc, TNTPrimed.class);
                tnt.setFuseTicks(fuseTicks);
            }
        }
    }

    public static class ExtremeMobsScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.extrememobs.desc",
                "§7§e%geant%%% §7de zombies géants, §e%jockey%%% §7de creepers jockeys, §e%feu%%% §7de creepers incendiaires et §e%silverfish%%% §7de creepers à poissons d'argent.");

        private static final String META_FEU = "extrememobs-feu";

        private static final String META_SILVERFISH = "extrememobs-silverfish";

        private final Random random = new Random();

        @Var(name = "Chance de géant", desc = "Chance qu'un zombie soit remplacé par un zombie géant.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceGeant = 1;

        @Var(name = "Or du géant", desc = "Lingots d'or lâchés par un zombie géant.", type = VariableType.INTEGER, min = 1)
        private int orGeant = 4;

        @Var(name = "Chance de jockey", desc = "Chance qu'un creeper apparaisse monté sur une poule.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceJockey = 5;

        @Var(name = "Chance de creeper incendiaire", desc = "Chance que l'explosion d'un creeper mette le feu.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceFeu = 10;

        @Var(name = "Chance de creeper infesté", desc = "Chance que l'explosion d'un creeper libère des poissons d'argent.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceSilverfish = 10;

        @Var(name = "Poissons d'argent", desc = "Nombre de poissons d'argent libérés par un creeper infesté.", type = VariableType.INTEGER, min = 1)
        private int nbSilverfish = 3;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Extreme Mobs"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of(
                    "%geant%", chanceGeant,
                    "%jockey%", chanceJockey,
                    "%feu%", chanceFeu,
                    "%silverfish%", chanceSilverfish));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLD_INGOT); }

        @EventHandler
        public void onSpawn(CreatureSpawnEvent event) {
            if (!isRunning()) return;
            LivingEntity entity = event.getEntity();
            Location loc = entity.getLocation();

            if (entity instanceof Zombie && !(entity instanceof PigZombie)) {
                if (!UHCUtils.Rng.chance(chanceGeant)) return;
                event.setCancelled(true);
                loc.getWorld().spawn(loc, Giant.class);
                return;
            }

            if (!(entity instanceof Creeper creeper)) return;

            if (UHCUtils.Rng.chance(chanceFeu)) {
                creeper.setMetadata(META_FEU, new FixedMetadataValue(Main.get(), true));
            }
            if (UHCUtils.Rng.chance(chanceSilverfish)) {
                creeper.setMetadata(META_SILVERFISH, new FixedMetadataValue(Main.get(), true));
            }
            if (!UHCUtils.Rng.chance(chanceJockey)) return;

            Chicken monture = loc.getWorld().spawn(loc, Chicken.class);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (creeper.isDead() || monture.isDead()) return;
                    monture.setPassenger(creeper);
                }
            }.runTaskLater(Main.get(), 1L);
        }

        @EventHandler
        public void onPrime(ExplosionPrimeEvent event) {
            if (!isRunning()) return;
            if (!event.getEntity().hasMetadata(META_FEU)) return;
            event.setFire(true);
        }

        @Override
        public void onEntityExplode(Entity entity, EntityExplodeEvent event) {
            if (!isActive()) return;
            if (!entity.hasMetadata(META_SILVERFISH)) return;
            Location loc = entity.getLocation();
            for (int i = 0; i < nbSilverfish; i++) {
                loc.getWorld().spawn(loc, Silverfish.class);
            }
        }

        @Override
        public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
            if (!isActive()) return;
            if (!(entity instanceof Giant)) return;
            event.getDrops().add(new ItemStack(Material.GOLD_INGOT, orGeant));
        }
    }

    public static class FortressMobsScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.fortressmobs.desc",
                "§7Dans le Nether, §e%blaze%%% §7des monstres naissent blazes et §e%wither%%% §7squelettes wither, partout.");

        private final Random random = new Random();

        @Var(name = "Chance de blaze", desc = "Chance qu'un monstre du Nether apparaisse sous forme de blaze.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceBlaze = 20;

        @Var(name = "Chance de squelette wither", desc = "Chance qu'un monstre du Nether apparaisse sous forme de squelette wither.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceWither = 20;

        @Var(name = "Inclure la surface", desc = "Applique aussi la règle aux monstres du monde normal.", type = VariableType.BOOLEAN)
        private boolean inclureSurface = false;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Fortress Mobs"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%blaze%", chanceBlaze, "%wither%", chanceWither));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BLAZE_ROD); }

        @EventHandler
        public void onSpawn(CreatureSpawnEvent event) {
            if (!isRunning()) return;
            if (event.getSpawnReason() != SpawnReason.NATURAL) return;

            LivingEntity entity = event.getEntity();
            if (entity instanceof Blaze || entity instanceof Skeleton) return;
            if (!(entity instanceof Monster)) return;

            Location loc = entity.getLocation();
            World.Environment environnement = loc.getWorld().getEnvironment();
            if (environnement != World.Environment.NETHER
                    && !(inclureSurface && environnement == World.Environment.NORMAL)) return;

            int tirage = random.nextInt(100);
            if (tirage < chanceBlaze) {
                event.setCancelled(true);
                loc.getWorld().spawn(loc, Blaze.class);
                return;
            }
            if (tirage >= chanceBlaze + chanceWither) return;

            event.setCancelled(true);
            Skeleton squelette = loc.getWorld().spawn(loc, Skeleton.class);
            squelette.setSkeletonType(Skeleton.SkeletonType.WITHER);
            squelette.getEquipment().setItemInHand(new ItemStack(Material.STONE_SWORD));
        }
    }

    public static class FragileSpiritsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.fragilespirits.desc",
                "§7À la mort d'un joueur, son esprit reste sur place : un ocelot en surface, une chauve-souris sous terre. Le tuer rend sa tête.");

        private final Map<UUID, String> esprits = new HashMap<>();

        @Var(name = "Durée des effets", desc = "Durée des effets portés par l'esprit du mort.", type = VariableType.TIME, min = 1)
        private int dureeEffetSec = 3600;

        @Var(name = "Vitesse de l'ocelot", desc = "Niveau de Vitesse de l'esprit de surface.", type = VariableType.INTEGER, min = 1, max = 5)
        private int vitesseOcelot = 1;

        @Var(name = "Résistance de l'ocelot", desc = "Niveau de Résistance de l'esprit de surface.", type = VariableType.INTEGER, min = 1, max = 5)
        private int resistanceOcelot = 1;

        @Var(name = "Résistance de la chauve-souris", desc = "Niveau de Résistance de l'esprit souterrain.", type = VariableType.INTEGER, min = 1, max = 5)
        private int resistanceChauveSouris = 2;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Fragile Spirits"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GHAST_TEAR); }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            Player mort = event.getEntity();
            Location loc = mort.getLocation();
            boolean surface = loc.getWorld().getHighestBlockYAt(loc) <= loc.getBlockY();

            LivingEntity esprit;
            if (surface) {
                esprit = loc.getWorld().spawn(loc, Ocelot.class);
                esprit.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, dureeEffetSec * 20, vitesseOcelot - 1));
                esprit.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, dureeEffetSec * 20, resistanceOcelot - 1));
            } else {
                esprit = loc.getWorld().spawn(loc, Bat.class);
                esprit.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, dureeEffetSec * 20, resistanceChauveSouris - 1));
            }
            esprits.put(esprit.getUniqueId(), mort.getName());
        }

        @Override
        public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
            if (!isActive()) return;
            String proprietaire = esprits.remove(entity.getUniqueId());
            if (proprietaire == null) return;

            ItemStack tete = new ItemCreator(Material.SKULL_ITEM).setDurability((short) 3)
                    .setOwner(proprietaire).getItemstack();
            event.getDrops().add(tete);
        }

        @Override
        public void onStop() {
            esprits.clear();
        }
    }

    public static class HairySheepScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.hairysheep.desc",
                "§7Les moutons lâchent de la ficelle quand on les tond comme quand on les tue.");

        @Var(name = "Ficelle à la mort", desc = "Ficelles lâchées par un mouton tué.", type = VariableType.INTEGER, min = 1)
        private int ficelleMort = 2;

        @Var(name = "Ficelle à la tonte", desc = "Ficelles lâchées par un mouton tondu.", type = VariableType.INTEGER, min = 1)
        private int ficelleTonte = 2;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Hairy Sheep"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.STRING); }

        @Override
        public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
            if (!isActive()) return;
            if (!(entity instanceof Sheep)) return;
            event.getDrops().add(new ItemStack(Material.STRING, ficelleMort));
        }

        @EventHandler
        public void onShear(PlayerShearEntityEvent event) {
            if (!isRunning()) return;
            if (!(event.getEntity() instanceof Sheep mouton)) return;
            Location loc = mouton.getLocation();
            loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.STRING, ficelleTonte));
        }
    }

    public static class InfestationScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.infestation.desc",
                "§7Un monstre tué a §e%chance%%% §7de chances de renaître sur place.");

        private final Random random = new Random();

        @Var(name = "Chance de renaissance", desc = "Chance qu'un monstre tué réapparaisse à l'endroit de sa mort.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chance = 40;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Infestation"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%chance%", chance));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.MONSTER_EGG); }

        @Override
        public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
            if (!isActive()) return;
            if (!(entity instanceof Monster)) return;
            if (!UHCUtils.Rng.chance(chance)) return;

            Location loc = entity.getLocation();
            EntityType type = entity.getType();
            new BukkitRunnable() {
                @Override
                public void run() {
                    loc.getWorld().spawnEntity(loc, type);
                }
            }.runTaskLater(Main.get(), 1L);
        }
    }

    public static class NecrophobiaScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.necrophobia.desc",
                "§7Toute bête et tout monstre naît zombie, et lâche cuir, ficelle, plume et chair humaine.");

        private final Random random = new Random();

        @Var(name = "Chance de cuir", desc = "Chance qu'un zombie lâche du cuir.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceCuir = 50;

        @Var(name = "Chance de ficelle", desc = "Chance qu'un zombie lâche de la ficelle.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceFicelle = 50;

        @Var(name = "Chance de plume", desc = "Chance qu'un zombie lâche une plume.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chancePlume = 50;

        @Var(name = "Quantité par drop", desc = "Quantité lâchée pour chaque ressource tirée au sort.", type = VariableType.INTEGER, min = 1)
        private int quantite = 1;

        @Var(name = "Chair humaine", desc = "Chair putréfiée supplémentaire lâchée par chaque zombie.", type = VariableType.INTEGER, min = 1)
        private int chairHumaine = 1;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Necrophobia"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ROTTEN_FLESH); }

        @EventHandler
        public void onSpawn(CreatureSpawnEvent event) {
            if (!isActive()) return;
            if (event.getLocation().getWorld().equals(Common.get().getLobby())) return;
            if (event.getSpawnReason() == SpawnReason.CUSTOM) return;
            LivingEntity entity = event.getEntity();
            if (entity instanceof Zombie) return;
            if (!(entity instanceof Monster) && !(entity instanceof Animals)) return;

            event.setCancelled(true);
            Location loc = entity.getLocation();
            loc.getWorld().spawn(loc, Zombie.class);
        }

        @Override
        public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
            if (!isActive()) return;
            if (!(entity instanceof Zombie)) return;

            List<ItemStack> drops = event.getDrops();
            if (UHCUtils.Rng.chance(chanceCuir)) drops.add(new ItemStack(Material.LEATHER, quantite));
            if (UHCUtils.Rng.chance(chanceFicelle)) drops.add(new ItemStack(Material.STRING, quantite));
            if (UHCUtils.Rng.chance(chancePlume)) drops.add(new ItemStack(Material.FEATHER, quantite));
            drops.add(new ItemStack(Material.ROTTEN_FLESH, chairHumaine));
        }
    }

    public static class NeutralMobsScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.neutralmobs.desc",
                "§7Les monstres t'ignorent tant que tu ne les frappes pas. Une fois provoqué, le mob te vise et frappe §e×%facteur%§7.");

        private final Map<UUID, UUID> provoques = new HashMap<>();

        @Var(name = "Multiplicateur de dégâts", desc = "Facteur appliqué aux dégâts d'un monstre provoqué.", type = VariableType.DOUBLE, min = 1.0)
        private double multiplicateur = 3.0;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Neutral Mobs"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%facteur%", multiplicateur));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GRILLED_PORK); }

        @EventHandler
        public void onTarget(EntityTargetEvent event) {
            if (!isRunning()) return;
            if (!(event.getTarget() instanceof Player cible)) return;
            if (!(event.getEntity() instanceof Monster)) return;
            if (cible.getUniqueId().equals(provoques.get(event.getEntity().getUniqueId()))) return;
            event.setCancelled(true);
        }

        @Override
        public void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;

            Player agresseur = null;
            if (dammager instanceof Player direct) agresseur = direct;
            else if (dammager instanceof Projectile tir && tir.getShooter() instanceof Player tireur) agresseur = tireur;

            if (agresseur != null && entity instanceof Monster) {
                provoques.put(entity.getUniqueId(), agresseur.getUniqueId());
                return;
            }
            if (!(entity instanceof Player)) return;
            if (!(dammager instanceof Monster)) return;

            if (!provoques.containsKey(dammager.getUniqueId())) {
                event.setCancelled(true);
                return;
            }
            event.setDamage(event.getDamage() * multiplicateur);
        }

        @Override
        public void onStop() {
            provoques.clear();
        }
    }

    public static class NightmareModeScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.nightmaremode.desc",
                "§7Creepers rapides crachant des poissons d'argent, zombies lents mais puissants qui laissent des squelettes, flèches empoisonnées, araignées tisseuses, endermen porteurs de blazes et sorcières survitaminées.");

        @Var(name = "Durée des renforts", desc = "Durée des effets appliqués aux monstres à leur apparition.", type = VariableType.TIME, min = 1)
        private int dureeBuffSec = 3600;

        @Var(name = "Vitesse des creepers", desc = "Niveau de Vitesse des creepers.", type = VariableType.INTEGER, min = 1, max = 5)
        private int vitesseCreeper = 2;

        @Var(name = "Force des zombies", desc = "Niveau de Force des zombies.", type = VariableType.INTEGER, min = 1, max = 5)
        private int forceZombie = 2;

        @Var(name = "Lenteur des zombies", desc = "Niveau de Lenteur des zombies.", type = VariableType.INTEGER, min = 1, max = 5)
        private int lenteurZombie = 1;

        @Var(name = "Vitesse des araignées", desc = "Niveau de Vitesse des araignées.", type = VariableType.INTEGER, min = 1, max = 5)
        private int vitesseAraignee = 2;

        @Var(name = "Vitesse des sorcières", desc = "Niveau de Vitesse des sorcières.", type = VariableType.INTEGER, min = 1, max = 5)
        private int vitesseSorciere = 5;

        @Var(name = "Durée du poison", desc = "Durée du poison infligé par une flèche de squelette.", type = VariableType.TIME, min = 1)
        private int dureePoisonSec = 5;

        @Var(name = "Niveau du poison", desc = "Niveau du poison infligé par une flèche de squelette.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauPoison = 1;

        @Var(name = "Poissons d'argent", desc = "Poissons d'argent libérés à la mort d'un creeper.", type = VariableType.INTEGER, min = 1)
        private int nbSilverfish = 2;

        @Var(name = "Squelettes", desc = "Squelettes libérés à la mort d'un zombie.", type = VariableType.INTEGER, min = 1)
        private int nbSquelettes = 1;

        @Var(name = "Blazes", desc = "Blazes libérés à la mort d'un enderman.", type = VariableType.INTEGER, min = 1)
        private int nbBlazes = 1;

        @Var(name = "Toiles", desc = "Toiles d'araignée posées à la mort d'une araignée.", type = VariableType.INTEGER, min = 1)
        private int nbToiles = 3;

        @Var(name = "Rayon des toiles", desc = "Rayon, en blocs, de dispersion des toiles autour du cadavre de l'araignée.", type = VariableType.INTEGER, min = 0, max = 3)
        private int rayonToiles = 1;

        private final Random random = new Random();

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Nightmare Mode"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BED); }

        @EventHandler
        public void onSpawn(CreatureSpawnEvent event) {
            if (!isRunning()) return;
            LivingEntity entity = event.getEntity();
            int duree = dureeBuffSec * 20;

            if (entity instanceof Creeper) {
                entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duree, vitesseCreeper - 1));
            } else if (entity instanceof Zombie) {
                entity.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, duree, forceZombie - 1));
                entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duree, lenteurZombie - 1));
            } else if (entity instanceof Spider) {
                entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duree, vitesseAraignee - 1));
            } else if (entity instanceof Witch) {
                entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duree, vitesseSorciere - 1));
            }
        }

        @Override
        public void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (!(dammager instanceof Arrow fleche)) return;
            if (!(fleche.getShooter() instanceof Skeleton)) return;
            if (!(entity instanceof LivingEntity victime)) return;
            victime.addPotionEffect(new PotionEffect(PotionEffectType.POISON, dureePoisonSec * 20, niveauPoison - 1));
        }

        @Override
        public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
            if (!isActive()) return;
            Location loc = entity.getLocation();
            World world = loc.getWorld();

            if (entity instanceof Creeper) {
                for (int i = 0; i < nbSilverfish; i++) world.spawn(loc, Silverfish.class);
            } else if (entity instanceof Zombie) {
                for (int i = 0; i < nbSquelettes; i++) world.spawn(loc, Skeleton.class);
            } else if (entity instanceof Enderman) {
                for (int i = 0; i < nbBlazes; i++) world.spawn(loc, Blaze.class);
            } else if (entity instanceof Spider) {
                for (int i = 0; i < nbToiles; i++) {
                    int diametre = rayonToiles * 2 + 1;
                    Block block = world.getBlockAt(
                            loc.getBlockX() + random.nextInt(diametre) - rayonToiles,
                            loc.getBlockY(),
                            loc.getBlockZ() + random.nextInt(diametre) - rayonToiles);
                    if (block.getType() != Material.AIR) continue;
                    block.setTypeIdAndData(Material.WEB.getId(), (byte) 0, false);
                }
            }
        }
    }

    public static class PassiveMobsScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.passivemobs.desc",
                "§7Les monstres ne s'en prennent jamais aux joueurs, même provoqués.");

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Passive Mobs"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.FEATHER); }

        @EventHandler
        public void onTarget(EntityTargetEvent event) {
            if (!isRunning()) return;
            if (!(event.getTarget() instanceof Player)) return;
            if (!(event.getEntity() instanceof Monster)) return;
            event.setCancelled(true);
        }

        @Override
        public void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (!(entity instanceof Player)) return;
            if (!(dammager instanceof Monster)) return;
            event.setCancelled(true);
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new EggsScenario(),
                new EightLeggedFreaksScenario(),
                new ExplosiveMobsScenario(),
                new ExtremeMobsScenario(),
                new FortressMobsScenario(),
                new FragileSpiritsScenario(),
                new HairySheepScenario(),
                new InfestationScenario(),
                new NecrophobiaScenario(),
                new NeutralMobsScenario(),
                new NightmareModeScenario(),
                new PassiveMobsScenario()
        );
    }
}
