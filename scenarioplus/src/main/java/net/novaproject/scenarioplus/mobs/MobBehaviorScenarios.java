package net.novaproject.scenarioplus.mobs;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class MobBehaviorScenarios {

    private static final Random RANDOM = new Random();

    private MobBehaviorScenarios() {
    }

    public static class PvEceptionScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.pveception.desc",
                "§7Reste §e%duree% §7secondes sans subir de dégâts de l'environnement pour recevoir un livre enchanté.");

        private static final DynamicLang RECOMPENSE = DynamicLang.of("scenario.pveception.recompense",
                "§7Ta prudence face à l'environnement te vaut un livre enchanté.");

        private static final Enchantment[] LIVRES = {
                Enchantment.DAMAGE_ALL,
                Enchantment.ARROW_DAMAGE,
                Enchantment.PROTECTION_ENVIRONMENTAL,
                Enchantment.PROTECTION_PROJECTILE,
                Enchantment.DIG_SPEED,
                Enchantment.DURABILITY,
                Enchantment.FIRE_ASPECT,
                Enchantment.KNOCKBACK,
                Enchantment.LOOT_BONUS_MOBS
        };

        private final Map<UUID, Integer> series = new HashMap<>();

        @Var(name = "Durée sans dégâts", desc = "Temps à tenir sans dégâts environnementaux pour gagner un livre.", type = VariableType.TIME, min = 1)
        private int dureeSec = 120;

        @Var(name = "Niveau du livre", desc = "Niveau de l'enchantement stocké dans le livre offert.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauLivre = 1;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "PvEception"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%duree%", dureeSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ENCHANTED_BOOK); }

        @Override
        public void onDamage(Player player, EntityDamageEvent event) {
            if (!isActive()) return;
            if (event.isCancelled()) return;
            if (event instanceof EntityDamageByEntityEvent) return;
            series.put(player.getUniqueId(), 0);
        }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            int serie = series.getOrDefault(player.getUniqueId(), 0) + 1;
            if (serie < dureeSec) {
                series.put(player.getUniqueId(), serie);
                return;
            }
            series.put(player.getUniqueId(), 0);

            ItemStack livre = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) livre.getItemMeta();
            meta.addStoredEnchant(LIVRES[RANDOM.nextInt(LIVRES.length)], niveauLivre, true);
            livre.setItemMeta(meta);
            player.getInventory().addItem(livre);
            LangManager.get().send(RECOMPENSE, player);
        }

        @Override
        public void onStop() {
            series.clear();
        }
    }

    public static class SlimyChunkScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.slimychunk.desc",
                "§7Chaque chunk a §e%chance%% §7de disparaître : il ne reste qu'une couche de blocs de slime au-dessus du vide.");

        @Var(name = "Chance de disparition", desc = "Chance qu'un chunk généré soit vidé jusqu'au vide.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chance = 10;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Slimy Chunk"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%chance%", chance));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SLIME_BLOCK); }

        @EventHandler
        public void onChunkLoad(ChunkLoadEvent event) {
            if (!isActive()) return;
            if (!event.isNewChunk()) return;
            World arena = Common.get().getArena();
            if (arena == null || !event.getWorld().equals(arena)) return;
            if (!UHCUtils.Rng.chance(chance)) return;

            Chunk chunk = event.getChunk();
            int baseX = chunk.getX() << 4;
            int baseZ = chunk.getZ() << 4;
            int slime = Material.SLIME_BLOCK.getId();

            for (int dx = 0; dx < 16; dx++) {
                for (int dz = 0; dz < 16; dz++) {
                    int x = baseX + dx;
                    int z = baseZ + dz;
                    int sommet = arena.getHighestBlockYAt(x, z) - 1;
                    if (sommet < 0) continue;
                    for (int y = sommet; y >= 0; y--) {
                        arena.getBlockAt(x, y, z).setTypeIdAndData(0, (byte) 0, false);
                    }
                    arena.getBlockAt(x, sommet, z).setTypeIdAndData(slime, (byte) 0, false);
                }
            }
        }
    }

    public static class SlimyCrackScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.slimycrack.desc",
                "§7Une faille de §e%largeur% §7blocs traverse la carte (§e%failles% §7faille(s)), avec des blocs de slime cassables au fond.");

        @Var(name = "Largeur de la faille", desc = "Largeur totale de la faille en blocs.", type = VariableType.INTEGER, min = 1)
        private int largeur = 50;

        @Var(name = "Fond de la faille", desc = "Altitude de la couche de blocs de slime servant de fond.", type = VariableType.INTEGER, min = 1, max = 255)
        private int fondY = 5;

        @Var(name = "Faille sur l'axe X", desc = "La faille est centrée sur X:0 ; sinon elle est centrée sur Z:0.", type = VariableType.BOOLEAN)
        private boolean axeX = true;

        @Var(name = "Nombre de failles", desc = "1 = une seule faille, 2 = une seconde faille perpendiculaire sur l'autre axe.", type = VariableType.INTEGER, min = 1, max = 2)
        private int nombreFailles = 1;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Slimy Crack"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%largeur%", largeur, "%failles%", nombreFailles));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SLIME_BALL); }

        @EventHandler
        public void onChunkLoad(ChunkLoadEvent event) {
            if (!isActive()) return;
            if (!event.isNewChunk()) return;
            World arena = Common.get().getArena();
            if (arena == null || !event.getWorld().equals(arena)) return;

            Location centre = arena.getWorldBorder().getCenter();
            int axePrincipal = axeX ? centre.getBlockX() : centre.getBlockZ();
            int axeSecondaire = axeX ? centre.getBlockZ() : centre.getBlockX();
            int demi = largeur / 2;
            Chunk chunk = event.getChunk();
            int baseX = chunk.getX() << 4;
            int baseZ = chunk.getZ() << 4;
            int slime = Material.SLIME_BLOCK.getId();

            for (int dx = 0; dx < 16; dx++) {
                for (int dz = 0; dz < 16; dz++) {
                    int x = baseX + dx;
                    int z = baseZ + dz;
                    boolean surPrincipal = Math.abs((axeX ? x : z) - axePrincipal) <= demi;
                    boolean surSecondaire = nombreFailles > 1 && Math.abs((axeX ? z : x) - axeSecondaire) <= demi;
                    if (!surPrincipal && !surSecondaire) continue;
                    int sommet = arena.getHighestBlockYAt(x, z) - 1;
                    for (int y = sommet; y > fondY; y--) {
                        arena.getBlockAt(x, y, z).setTypeIdAndData(0, (byte) 0, false);
                    }
                    arena.getBlockAt(x, fondY, z).setTypeIdAndData(slime, (byte) 0, false);
                }
            }
        }
    }

    public static class SpawnersGaloreScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.spawnersgalore.desc",
                "§7Les spawners crachent §e%mobs% §7monstres à la fois, de n'importe quelle espèce.");

        private static final EntityType[] MOBS = {
                EntityType.ZOMBIE,
                EntityType.SKELETON,
                EntityType.SPIDER,
                EntityType.CAVE_SPIDER,
                EntityType.CREEPER,
                EntityType.ENDERMAN,
                EntityType.WITCH,
                EntityType.SLIME,
                EntityType.SILVERFISH,
                EntityType.PIG_ZOMBIE,
                EntityType.BLAZE,
                EntityType.MAGMA_CUBE
        };

        @Var(name = "Monstres par déclenchement", desc = "Nombre de monstres générés à chaque déclenchement d'un spawner.", type = VariableType.INTEGER, min = 1)
        private int mobsParSpawn = 3;

        @Var(name = "Espèce aléatoire", desc = "Un spawner peut générer n'importe quelle espèce, pas seulement la sienne.", type = VariableType.BOOLEAN)
        private boolean especeAleatoire = true;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Spawners Galore"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%mobs%", mobsParSpawn));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.MOB_SPAWNER); }

        @EventHandler
        public void onCreatureSpawn(CreatureSpawnEvent event) {
            if (!isRunning()) return;
            if (event.getSpawnReason() != SpawnReason.SPAWNER) return;

            Location loc = event.getLocation();
            EntityType espece = event.getEntityType();
            int renforts = mobsParSpawn;
            if (especeAleatoire) {
                event.setCancelled(true);
            } else {
                renforts--;
            }
            for (int i = 0; i < renforts; i++) {
                loc.getWorld().spawnEntity(loc, especeAleatoire ? MOBS[RANDOM.nextInt(MOBS.length)] : espece);
            }
        }
    }

    public static class StrongerMobsScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.strongermobs.desc",
                "§7Zombies et squelettes portent une armure, §e%creeper%% §7des creepers sont chargés, les araignées sont dopées et les sorcières pullulent.");

        @Var(name = "Chance d'armure", desc = "Chance qu'un zombie ou un squelette apparaisse équipé de fer.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceArmure = 100;

        @Var(name = "Chance de creeper chargé", desc = "Chance qu'un creeper apparaisse déjà chargé.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceCreeperCharge = 25;

        @Var(name = "Chance de sorcière", desc = "Chance qu'un monstre naturel soit remplacé par une sorcière.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceSorciere = 15;

        @Var(name = "Durée des effets d'araignée", desc = "Durée des effets de vitesse et de force donnés aux araignées.", type = VariableType.TIME, min = 1)
        private int dureeEffetsAraigneeSec = 600;

        @Var(name = "Niveau des effets d'araignée", desc = "Niveau des effets de vitesse et de force donnés aux araignées.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauEffetsAraignee = 1;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Stronger Mobs"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%creeper%", chanceCreeperCharge));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.IRON_CHESTPLATE); }

        @EventHandler
        public void onCreatureSpawn(CreatureSpawnEvent event) {
            if (!isRunning()) return;
            Entity entity = event.getEntity();

            if (event.getSpawnReason() == SpawnReason.NATURAL
                    && entity instanceof Monster
                    && !(entity instanceof Witch)
                    && UHCUtils.Rng.chance(chanceSorciere)) {
                event.setCancelled(true);
                event.getLocation().getWorld().spawnEntity(event.getLocation(), EntityType.WITCH);
                return;
            }

            if (entity instanceof Creeper creeper) {
                if (UHCUtils.Rng.chance(chanceCreeperCharge)) creeper.setPowered(true);
                return;
            }

            if (entity instanceof Spider spider) {
                spider.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, dureeEffetsAraigneeSec * 20, niveauEffetsAraignee - 1));
                spider.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, dureeEffetsAraigneeSec * 20, niveauEffetsAraignee - 1));
                return;
            }

            if (!(entity instanceof Zombie) && !(entity instanceof Skeleton)) return;
            if (!UHCUtils.Rng.chance(chanceArmure)) return;

            EntityEquipment equipement = ((LivingEntity) entity).getEquipment();
            equipement.setHelmet(new ItemStack(Material.IRON_HELMET));
            equipement.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
            equipement.setHelmetDropChance(0f);
            equipement.setChestplateDropChance(0f);
        }
    }

    public static class TrainingRabbitsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.trainingrabbits.desc",
                "§7Tu démarres avec Saut §e%depart%§7, gagnes un niveau par kill jusqu'à §e%max%§7, et ne subis aucun dégât de chute.");

        private static final DynamicLang PROGRESSION = DynamicLang.of("scenario.trainingrabbits.progression",
                "§7Ton entraînement paie : Saut §e%niveau%§7.");

        private final Map<UUID, Integer> niveaux = new HashMap<>();

        @Var(name = "Niveau de départ", desc = "Niveau de Saut donné à chaque joueur au début de la partie.", type = VariableType.INTEGER, min = 1, max = 10)
        private int niveauDepart = 2;

        @Var(name = "Niveau maximum", desc = "Niveau de Saut maximum atteignable à force de kills.", type = VariableType.INTEGER, min = 1, max = 10)
        private int niveauMax = 6;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Training Rabbits"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%depart%", niveauDepart, "%max%", niveauMax));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.RABBIT_FOOT); }

        @Override
        public void onKill(UHCPlayer killer, UHCPlayer victim) {
            if (!isActive()) return;
            if (killer == null) return;
            Player tueur = killer.getPlayer();
            if (tueur == null) return;

            int niveau = Math.min(niveauMax, niveaux.getOrDefault(killer.getUniqueId(), niveauDepart) + 1);
            niveaux.put(killer.getUniqueId(), niveau);
            LangManager.get().send(PROGRESSION, tueur, Map.of("%niveau%", niveau));
        }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            int niveau = niveaux.getOrDefault(player.getUniqueId(), niveauDepart);
            UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                    new PotionEffect(PotionEffectType.JUMP, UHCUtils.LOOPED_EFFECT_DURATION_TICKS, niveau - 1)
            }, player);
        }

        @Override
        public void onDamage(Player player, EntityDamageEvent event) {
            if (!isActive()) return;
            if (event.getCause() != DamageCause.FALL) return;
            event.setCancelled(true);
        }

        @Override
        public void onStop() {
            niveaux.clear();
        }
    }

    public static class UndeadsRevengeScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.undeadsrevenge.desc",
                "§7Un mort se relève en mort-vivant portant tout son butin : tue-le pour le récupérer.");

        private static final DynamicLang APPARITION = DynamicLang.of("scenario.undeadsrevenge.apparition",
                "§c%joueur% §7se relève et garde son butin sur lui.");

        private final Map<UUID, List<ItemStack>> butins = new HashMap<>();

        @Var(name = "Vie du mort-vivant", desc = "Points de vie du mort-vivant qui garde le butin.", type = VariableType.DOUBLE, min = 1)
        private double vieDuGardien = 20.0;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Undead's Revenge"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SKULL_ITEM); }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            Player mort = event.getEntity();
            List<ItemStack> butin = new ArrayList<>(event.getDrops());
            if (butin.isEmpty()) return;
            event.getDrops().clear();

            Location loc = mort.getLocation();
            LivingEntity gardien = (LivingEntity) loc.getWorld().spawnEntity(loc,
                    RANDOM.nextBoolean() ? EntityType.ZOMBIE : EntityType.SKELETON);
            gardien.setMaxHealth(vieDuGardien);
            gardien.setHealth(vieDuGardien);
            gardien.setCustomName(mort.getName());
            gardien.setCustomNameVisible(true);

            ItemStack tete = new ItemCreator(Material.SKULL_ITEM).setDurability((short) 3)
                    .setOwner(mort.getName()).getItemstack();
            gardien.getEquipment().setHelmet(tete);
            gardien.getEquipment().setHelmetDropChance(0f);

            butins.put(gardien.getUniqueId(), butin);
            LangManager.get().sendAll(APPARITION, Map.of("%joueur%", mort.getName()));
        }

        @Override
        public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
            if (!isActive()) return;
            List<ItemStack> butin = butins.remove(entity.getUniqueId());
            if (butin == null) return;
            event.getDrops().addAll(butin);
        }

        @Override
        public void onStop() {
            butins.clear();
        }
    }

    public static class VillagerMadnessScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.villagermadness.desc",
                "§7Tout le monde démarre avec §a%emeraudes% blocs d'émeraude §7et §a%oeufs% œufs de villageois§7.");

        @Var(name = "Blocs d'émeraude", desc = "Nombre de blocs d'émeraude donnés au départ.", type = VariableType.INTEGER, min = 1, max = 64)
        private int blocsEmeraude = 64;

        @Var(name = "Œufs de villageois", desc = "Nombre d'œufs de villageois donnés au départ.", type = VariableType.INTEGER, min = 1, max = 64)
        private int oeufsVillageois = 64;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Villager Madness"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%emeraudes%", blocsEmeraude, "%oeufs%", oeufsVillageois));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.EMERALD_BLOCK); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            player.getInventory().addItem(new ItemStack(Material.EMERALD_BLOCK, blocsEmeraude));
            player.getInventory().addItem(new ItemStack(Material.MONSTER_EGG, oeufsVillageois,
                    EntityType.VILLAGER.getTypeId()));
        }
    }

    public static class ZetaBombiesScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.zetabombies.desc",
                "§7Les zombies lâchent du silex, du foin ou du slime au lieu de leur chair putréfiée.");

        private static final Material[] BUTINS = {
                Material.FLINT,
                Material.HAY_BLOCK,
                Material.SLIME_BLOCK
        };

        @Var(name = "Quantité lâchée", desc = "Nombre d'objets lâchés à la place de la chair putréfiée.", type = VariableType.INTEGER, min = 1)
        private int quantite = 1;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Zeta Bombies"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.HAY_BLOCK); }

        @Override
        public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
            if (!isActive()) return;
            if (entity.getType() != EntityType.ZOMBIE) return;
            event.getDrops().removeIf(drop -> drop != null && drop.getType() == Material.ROTTEN_FLESH);
            event.getDrops().add(new ItemStack(BUTINS[RANDOM.nextInt(BUTINS.length)], quantite));
        }
    }

    public static class Zombies20Scenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.zombies20.desc",
                "§7Mourir te transforme en zombie qui traque ton tueur.");

        private static final DynamicLang TRANSFORMATION = DynamicLang.of("scenario.zombies20.transformation",
                "§c%joueur% §7revient d'entre les morts sous forme de zombie.");

        @Var(name = "Vie du zombie", desc = "Points de vie du zombie né de la mort d'un joueur.", type = VariableType.DOUBLE, min = 1)
        private double vieDuZombie = 20.0;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Zombies 2.0"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ROTTEN_FLESH); }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            Player mort = event.getEntity();
            Zombie zombie = mort.getWorld().spawn(mort.getLocation(), Zombie.class);
            zombie.setMaxHealth(vieDuZombie);
            zombie.setHealth(vieDuZombie);
            zombie.setCustomName(mort.getName());
            zombie.setCustomNameVisible(true);
            if (killer != null && killer.getPlayer() != null) zombie.setTarget(killer.getPlayer());
            LangManager.get().sendAll(TRANSFORMATION, Map.of("%joueur%", mort.getName()));
        }
    }

    public static class ZombiesGaloreScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.zombiesgalore.desc",
                "§7Tous les monstres apparaissent en zombies et lâchent des os et de la ficelle.");

        @Var(name = "Os lâchés", desc = "Nombre d'os lâchés par un zombie tué.", type = VariableType.INTEGER, min = 1)
        private int osLaches = 1;

        @Var(name = "Ficelle lâchée", desc = "Nombre de ficelles lâchées par un zombie tué.", type = VariableType.INTEGER, min = 1)
        private int ficelleLachee = 1;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Zombies Galore"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.MONSTER_EGG); }

        @EventHandler
        public void onCreatureSpawn(CreatureSpawnEvent event) {
            if (!isRunning()) return;
            Entity entity = event.getEntity();
            if (!(entity instanceof Monster) || entity instanceof Zombie) return;
            event.setCancelled(true);
            event.getLocation().getWorld().spawnEntity(event.getLocation(), EntityType.ZOMBIE);
        }

        @Override
        public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
            if (!isActive()) return;
            if (!(entity instanceof Zombie)) return;
            event.getDrops().add(new ItemStack(Material.BONE, osLaches));
            event.getDrops().add(new ItemStack(Material.STRING, ficelleLachee));
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new PvEceptionScenario(),
                new SlimyChunkScenario(),
                new SlimyCrackScenario(),
                new SpawnersGaloreScenario(),
                new StrongerMobsScenario(),
                new TrainingRabbitsScenario(),
                new UndeadsRevengeScenario(),
                new VillagerMadnessScenario(),
                new ZetaBombiesScenario(),
                new Zombies20Scenario(),
                new ZombiesGaloreScenario()
        );
    }
}
