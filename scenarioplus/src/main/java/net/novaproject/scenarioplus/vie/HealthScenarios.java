package net.novaproject.scenarioplus.vie;

import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.player.utils.PlayerUtils;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class HealthScenarios {

    private HealthScenarios() {
    }

    public static class HundredHeartsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.100hearts.desc",
                "§7Tout le monde démarre avec §c%coeurs% cœurs §7au lieu de 10.");

        @Var(name = "Cœurs de départ", desc = "Nombre de cœurs maximum donné à chaque joueur au début de la partie.", type = VariableType.INTEGER, min = 1, max = 1024)
        private int coeurs = 100;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "100 Hearts"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%coeurs%", coeurs));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SPECKLED_MELON); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            double max = coeurs * 2.0;
            player.setMaxHealth(max);
            player.setHealth(max);
        }
    }

    public static class NineLivesScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.9lives.desc",
                "§7Un coup mortel ne tue pas : il coûte §c%perte% cœur(s) §7de vie maximum, jusqu'à §c%minimum% cœur(s)§7.");

        private static final DynamicLang VIE_PERDUE = DynamicLang.of("scenario.9lives.vie-perdue",
                "§c%joueur% §7a survécu de justesse et tombe à §c%coeurs% cœur(s) §7maximum.");

        @Var(name = "Cœurs perdus", desc = "Cœurs de vie maximum retirés à chaque coup mortel encaissé.", type = VariableType.INTEGER, min = 1)
        private int coeursPerdusParMort = 1;

        @Var(name = "Cœurs minimum", desc = "Plancher de vie maximum sous lequel le joueur meurt vraiment.", type = VariableType.INTEGER, min = 1)
        private int coeursMinimum = 1;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "9 Lives"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%perte%", coeursPerdusParMort, "%minimum%", coeursMinimum));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.CAKE); }

        @Override
        public void onDamage(Player player, EntityDamageEvent event) {
            if (!isActive()) return;
            if (event.isCancelled()) return;
            if (event.getCause() == DamageCause.VOID) return;
            if (event.getFinalDamage() < player.getHealth()) return;

            double restant = player.getMaxHealth() - coeursPerdusParMort * 2.0;
            if (restant < coeursMinimum * 2.0) return;

            event.setCancelled(true);
            player.setMaxHealth(restant);
            player.setHealth(restant);
            player.setFireTicks(0);
            player.setFallDistance(0f);
            LangManager.get().sendAll(VIE_PERDUE, Map.of(
                    "%joueur%", player.getName(),
                    "%coeurs%", (int) (restant / 2.0)));
        }
    }

    public static class AbsorptionSwitchScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.absorptionswitch.desc",
                "§7Clic gauche avec une pomme d'or pour choisir : §e%absorption% cœur(s) d'absorption §7ou §c%soin% cœur(s) de soin§7.");

        private static final DynamicLang MODE_ABSORPTION = DynamicLang.of("scenario.absorptionswitch.mode-absorption",
                "§eMode absorption §7: la pomme d'or donnera des cœurs jaunes.");

        private static final DynamicLang MODE_SOIN = DynamicLang.of("scenario.absorptionswitch.mode-soin",
                "§cMode soin §7: la pomme d'or rendra de la vie directement.");

        private final Map<UUID, Boolean> modeSoin = new HashMap<>();

        @Var(name = "Cœurs d'absorption", desc = "Cœurs d'absorption donnés par la pomme d'or en mode absorption.", type = VariableType.INTEGER, min = 1)
        private int coeursAbsorption = 2;

        @Var(name = "Cœurs de soin", desc = "Cœurs de vie rendus par la pomme d'or en mode soin.", type = VariableType.INTEGER, min = 1)
        private int coeursSoin = 2;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Absorption Switch"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%absorption%", coeursAbsorption, "%soin%", coeursSoin));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLDEN_APPLE); }

        @Override
        public void onPlayerInteract(Player player, PlayerInteractEvent event) {
            if (!isActive()) return;
            Action action = event.getAction();
            if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) return;

            ItemStack hand = player.getItemInHand();
            if (hand == null || hand.getType() != Material.GOLDEN_APPLE) return;

            boolean soin = !modeSoin.getOrDefault(player.getUniqueId(), false);
            modeSoin.put(player.getUniqueId(), soin);
            DisplayService.actionBar(player, t(soin ? MODE_SOIN : MODE_ABSORPTION, player));
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 1f);
        }

        @Override
        public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
            if (!isActive()) return;
            if (item == null || item.getType() != Material.GOLDEN_APPLE) return;

            boolean soin = modeSoin.getOrDefault(player.getUniqueId(), false);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || player.isDead()) return;
                    player.removePotionEffect(PotionEffectType.REGENERATION);
                    player.removePotionEffect(PotionEffectType.ABSORPTION);
                    PlayerUtils.setAbsorptionHearts(player, 0.0);
                    if (soin) {
                        player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + coeursSoin * 2.0));
                    } else {
                        PlayerUtils.setAbsorptionHearts(player, coeursAbsorption * 2.0);
                    }
                }
            }.runTaskLater(Main.get(), 1L);
        }

        @Override
        public void onStop() {
            modeSoin.clear();
        }
    }

    public static class AquaphobiaScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.aquaphobia.desc",
                "§7L'eau monte inexorablement de §bY=%depart% §7à §bY=%fin%§7. Les casques gagnent Respiration %respiration%.");

        @Var(name = "Altitude de départ", desc = "Première couche remplie d'eau quand la montée commence.", type = VariableType.INTEGER, min = 1, max = 255)
        private int yDepart = 32;

        @Var(name = "Altitude finale", desc = "Dernière couche remplie d'eau avant l'arrêt de la montée.", type = VariableType.INTEGER, min = 1, max = 255)
        private int yFin = 80;

        @Var(name = "Délai avant la montée", desc = "Temps de jeu avant que l'eau ne commence à monter.", type = VariableType.TIME, min = 0)
        private int delaiAvantMonteeSec = 300;

        @Var(name = "Blocs par tick", desc = "Nombre de blocs remplis d'eau à chaque tick : règle la vitesse de montée et la charge serveur.", type = VariableType.INTEGER, min = 1)
        private int blocsParTick = 400;

        @Var(name = "Niveau de Respiration", desc = "Niveau de Respiration appliqué automatiquement aux casques portés.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauRespiration = 3;

        private BukkitRunnable task;
        private int coucheY;
        private int curseurX;
        private int curseurZ;
        private int minX;
        private int maxX;
        private int minZ;
        private int maxZ;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Aquaphobia"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of(
                    "%depart%", yDepart,
                    "%fin%", yFin,
                    "%respiration%", niveauRespiration));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.WATER_BUCKET); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            World world = Common.get().getArena();
            if (world == null) return;

            coucheY = Math.min(yDepart, yFin);
            cadrerCouche(world);
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    monter();
                }
            };
            task.runTaskTimer(Main.get(), delaiAvantMonteeSec * 20L, 1L);
        }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            ItemStack casque = player.getInventory().getHelmet();
            if (casque == null || casque.getType() == Material.AIR) return;
            if (casque.getEnchantmentLevel(Enchantment.OXYGEN) >= niveauRespiration) return;
            casque.addUnsafeEnchantment(Enchantment.OXYGEN, niveauRespiration);
            player.getInventory().setHelmet(casque);
        }

        @Override
        public void onStop() {
            arreter();
        }

        private void cadrerCouche(World world) {
            WorldBorder border = world.getWorldBorder();
            Location centre = border.getCenter();
            int demi = (int) Math.floor(border.getSize() / 2.0);
            minX = centre.getBlockX() - demi;
            maxX = centre.getBlockX() + demi;
            minZ = centre.getBlockZ() - demi;
            maxZ = centre.getBlockZ() + demi;
            curseurX = minX;
            curseurZ = minZ;
        }

        private void monter() {
            World world = Common.get().getArena();
            if (world == null) return;
            if (coucheY > yFin) {
                arreter();
                return;
            }

            int eau = Material.STATIONARY_WATER.getId();
            for (int i = 0; i < blocsParTick; i++) {
                Block block = world.getBlockAt(curseurX, coucheY, curseurZ);
                if (block.getType() == Material.AIR) {
                    block.setTypeIdAndData(eau, (byte) 0, false);
                }
                if (++curseurX <= maxX) continue;
                curseurX = minX;
                if (++curseurZ <= maxZ) continue;
                coucheY++;
                if (coucheY > yFin) {
                    arreter();
                    return;
                }
                cadrerCouche(world);
                return;
            }
        }

        private void arreter() {
            if (task == null) return;
            task.cancel();
            task = null;
        }
    }

    public static class AstrophobiaScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.astrophobia.desc",
                "§7Nuit permanente, §e%charge%% §7des creepers naissent chargés et un météore tombe toutes les §e%intervalle% §7secondes.");

        private static final DynamicLang METEORE = DynamicLang.of("scenario.astrophobia.meteore",
                "§4Un météore fonce droit sur toi !");

        private final Random random = new Random();

        @Var(name = "Heure de la nuit", desc = "Heure du monde maintenue en permanence (18000 = minuit).", type = VariableType.INTEGER, min = 0, max = 24000)
        private int heureNuit = 18000;

        @Var(name = "Intervalle des météores", desc = "Temps entre deux chutes de météore.", type = VariableType.TIME, min = 1)
        private int intervalleMeteoreSec = 90;

        @Var(name = "Dispersion", desc = "Écart horizontal maximum entre le joueur visé et le point d'impact.", type = VariableType.INTEGER, min = 0)
        private int dispersion = 12;

        @Var(name = "Hauteur de chute", desc = "Hauteur à laquelle le météore apparaît au-dessus du point d'impact.", type = VariableType.INTEGER, min = 5)
        private int hauteurChute = 60;

        @Var(name = "Puissance du météore", desc = "Puissance de l'explosion creusant le cratère.", type = VariableType.DOUBLE, min = 0.5)
        private double puissance = 4.0;

        @Var(name = "Météore incendiaire", desc = "Le cratère du météore prend feu.", type = VariableType.BOOLEAN)
        private boolean incendiaire = true;

        @Var(name = "Chance de creeper chargé", desc = "Chance qu'un creeper apparaisse déjà chargé.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceCreeperCharge = 25;

        private BukkitRunnable task;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Astrophobia"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of(
                    "%charge%", chanceCreeperCharge,
                    "%intervalle%", intervalleMeteoreSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.FIREBALL); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            World world = Common.get().getArena();
            if (world == null) return;

            world.setGameRuleValue("doDaylightCycle", "false");
            world.setTime(heureNuit);
            task = new BukkitRunnable() {
                private int secondes = 0;

                @Override
                public void run() {
                    World arene = Common.get().getArena();
                    if (arene == null) return;
                    arene.setTime(heureNuit);
                    if (++secondes < intervalleMeteoreSec) return;
                    secondes = 0;
                    frapper(arene);
                }
            };
            task.runTaskTimer(Main.get(), 20L, 20L);
        }

        @EventHandler
        public void onCreatureSpawn(CreatureSpawnEvent event) {
            if (!isRunning()) return;
            if (!(event.getEntity() instanceof Creeper creeper)) return;
            if (!UHCUtils.Rng.chance(chanceCreeperCharge)) return;
            creeper.setPowered(true);
        }

        @Override
        public void onStop() {
            if (task != null) {
                task.cancel();
                task = null;
            }
        }

        private void frapper(World world) {
            List<UHCPlayer> vivants = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();
            if (vivants.isEmpty()) return;

            Player cible = vivants.get(random.nextInt(vivants.size())).getPlayer();
            if (cible == null || !cible.getWorld().equals(world)) return;

            int ecartX = dispersion == 0 ? 0 : random.nextInt(dispersion * 2 + 1) - dispersion;
            int ecartZ = dispersion == 0 ? 0 : random.nextInt(dispersion * 2 + 1) - dispersion;
            Location depart = cible.getLocation().clone().add(ecartX, hauteurChute, ecartZ);
            if (depart.getY() > world.getMaxHeight() - 1) depart.setY(world.getMaxHeight() - 1);

            LargeFireball meteore = world.spawn(depart, LargeFireball.class);
            meteore.setYield((float) puissance);
            meteore.setIsIncendiary(incendiaire);
            meteore.setDirection(new Vector(0, -1, 0));
            DisplayService.actionBar(cible, t(METEORE, cible));
        }
    }

    public static class AurophobiaScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.aurophobia.desc",
                "§7Miner de l'or expose à quatre maux, chacun avec §e%chance%% §7de chances pendant §e%duree% §7secondes.");

        private static final PotionEffectType[] MALUS = {
                PotionEffectType.BLINDNESS,
                PotionEffectType.CONFUSION,
                PotionEffectType.SLOW,
                PotionEffectType.POISON
        };

        private final Random random = new Random();

        @Var(name = "Chance par effet", desc = "Chance que chacun des quatre effets négatifs se déclenche.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chance = 10;

        @Var(name = "Durée des effets", desc = "Durée de chaque effet négatif déclenché.", type = VariableType.TIME, min = 1)
        private int dureeSec = 15;

        @Var(name = "Niveau des effets", desc = "Niveau appliqué à chaque effet négatif déclenché.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveau = 1;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Aurophobia"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%chance%", chance, "%duree%", dureeSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLD_ORE); }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            if (block.getType() != Material.GOLD_ORE) return;

            for (PotionEffectType type : MALUS) {
                if (!UHCUtils.Rng.chance(chance)) continue;
                player.addPotionEffect(new PotionEffect(type, dureeSec * 20, niveau - 1, false, true), true);
            }
        }
    }

    public static class BirdsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.birds.desc",
                "§7Tout le monde peut voler et les dégâts de chute sont supprimés.");

        @Var(name = "Vitesse de vol", desc = "Vitesse de vol accordée aux joueurs (0.1 = vitesse créative normale).", type = VariableType.DOUBLE, min = 0.01, max = 1.0)
        private double vitesseVol = 0.1;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Birds"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.FEATHER); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            player.setAllowFlight(true);
            player.setFlySpeed((float) vitesseVol);
        }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            if (player.getAllowFlight()) return;
            player.setAllowFlight(true);
            player.setFlySpeed((float) vitesseVol);
        }

        @Override
        public void onDamage(Player player, EntityDamageEvent event) {
            if (!isActive()) return;
            if (event.getCause() != DamageCause.FALL) return;
            event.setCancelled(true);
        }
    }

    public static class BlackPlagueScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.blackplague.desc",
                "§7Une zone morte s'étend de §e%rayon% §7blocs et grandit : les bêtes y crèvent et les soins n'y valent que §e%soin%%§7.");

        private final Random random = new Random();

        @Var(name = "Rayon initial", desc = "Rayon de la zone morte au début de la partie.", type = VariableType.INTEGER, min = 1)
        private int rayonInitial = 50;

        @Var(name = "Expansion par seconde", desc = "Nombre de blocs gagnés par la zone morte chaque seconde.", type = VariableType.DOUBLE, min = 0.0)
        private double expansionParSeconde = 0.5;

        @Var(name = "Colonnes par tick", desc = "Nombre de colonnes de terrain flétries à chaque tick : règle la vitesse du pourrissement et la charge serveur.", type = VariableType.INTEGER, min = 1)
        private int colonnesParTick = 20;

        @Var(name = "Soin dans la zone", desc = "Pourcentage du soin conservé quand on se régénère dans la zone morte.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int pourcentageSoin = 50;

        @Var(name = "Dégâts aux bêtes", desc = "Dégâts infligés chaque seconde aux animaux pris dans la zone morte.", type = VariableType.DOUBLE, min = 0.5)
        private double degatsMobs = 4.0;

        private BukkitRunnable task;
        private double rayon;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Black Plague"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%rayon%", rayonInitial, "%soin%", pourcentageSoin));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.DEAD_BUSH); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            World world = Common.get().getArena();
            if (world == null) return;

            rayon = rayonInitial;
            task = new BukkitRunnable() {
                private int ticks = 0;

                @Override
                public void run() {
                    World arene = Common.get().getArena();
                    if (arene == null) return;
                    Location centre = arene.getWorldBorder().getCenter();
                    rayon = Math.min(rayon + expansionParSeconde / 20.0, arene.getWorldBorder().getSize() / 2.0);
                    for (int i = 0; i < colonnesParTick; i++) {
                        pourrir(arene, centre);
                    }
                    if (++ticks < 20) return;
                    ticks = 0;
                    tuerLesBetes(arene, centre);
                }
            };
            task.runTaskTimer(Main.get(), 1L, 1L);
        }

        @EventHandler
        public void onRegain(EntityRegainHealthEvent event) {
            if (!isRunning()) return;
            if (!(event.getEntity() instanceof Player player)) return;
            World world = Common.get().getArena();
            if (world == null || !player.getWorld().equals(world)) return;
            if (player.getLocation().distanceSquared(world.getWorldBorder().getCenter()) > rayon * rayon) return;
            event.setAmount(event.getAmount() * pourcentageSoin / 100.0);
        }

        @Override
        public void onStop() {
            rayon = 0;
            if (task != null) {
                task.cancel();
                task = null;
            }
        }

        private void pourrir(World world, Location centre) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = Math.sqrt(random.nextDouble()) * rayon;
            int x = centre.getBlockX() + (int) (Math.cos(angle) * distance);
            int z = centre.getBlockZ() + (int) (Math.sin(angle) * distance);
            int y = world.getHighestBlockYAt(x, z) - 1;
            if (y < 1) return;

            Block block = world.getBlockAt(x, y, z);
            switch (block.getType()) {
                case GRASS, MYCEL -> block.setTypeIdAndData(Material.DIRT.getId(), (byte) 0, false);
                case LONG_GRASS, RED_ROSE, YELLOW_FLOWER, DOUBLE_PLANT, LEAVES, LEAVES_2, VINE, WATER_LILY ->
                        block.setTypeIdAndData(Material.AIR.getId(), (byte) 0, false);
                default -> {
                }
            }
        }

        private void tuerLesBetes(World world, Location centre) {
            for (Animals animal : world.getEntitiesByClass(Animals.class)) {
                if (animal.getLocation().distanceSquared(centre) > rayon * rayon) continue;
                animal.damage(degatsMobs);
            }
        }
    }

    public static class BlitzScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.blitz.desc",
                "§7Tout le monde démarre à §c%coeurs% cœurs §7et retrouve toute sa vie au deathmatch.");

        private static final DynamicLang SOIN_DEATHMATCH = DynamicLang.of("scenario.blitz.soin-deathmatch",
                "§7Le deathmatch commence : tout le monde retrouve toute sa vie.");

        private final Set<UUID> soignes = new HashSet<>();

        @Var(name = "Cœurs de départ", desc = "Vie de chaque joueur au début de la partie.", type = VariableType.DOUBLE, min = 0.5)
        private double coeursDepart = 2.5;

        private boolean annonceFaite = false;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Blitz"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%coeurs%", coeursDepart));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.REDSTONE); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            player.setHealth(Math.min(player.getMaxHealth(), coeursDepart * 2.0));
        }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            UHCManager uhc = UHCManager.get();
            if (uhc.getTimer() < uhc.getBorderTimer()) return;
            if (!soignes.add(player.getUniqueId())) return;

            player.setHealth(player.getMaxHealth());
            if (annonceFaite) return;
            annonceFaite = true;
            LangManager.get().sendAll(SOIN_DEATHMATCH);
        }

        @Override
        public void onStop() {
            soignes.clear();
            annonceFaite = false;
        }
    }

    public static class BloodBrothersScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.bloodbrothers.desc",
                "§7Pommes d'or, têtes et potions soignent ton coéquipier à ta place (§e%part%%§7).");

        private static final DynamicLang DONNE = DynamicLang.of("scenario.bloodbrothers.donne",
                "§7Ton soin est parti à §c%joueur%§7.");

        private static final DynamicLang RECU = DynamicLang.of("scenario.bloodbrothers.recu",
                "§c%joueur% §7t'a soigné à sa place.");

        @Var(name = "Part transférée", desc = "Pourcentage du soin redirigé vers le coéquipier.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int pourcentageTransfere = 100;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Blood Brothers"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%part%", pourcentageTransfere));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SKULL_ITEM); }

        @EventHandler
        public void onRegain(EntityRegainHealthEvent event) {
            if (!isRunning()) return;
            RegainReason reason = event.getRegainReason();
            if (reason != RegainReason.MAGIC && reason != RegainReason.MAGIC_REGEN) return;
            if (!(event.getEntity() instanceof Player player)) return;

            UHCPlayer self = UHCPlayerManager.get().getPlayer(player);
            if (self == null || self.getTeam().isEmpty()) return;

            Player receveur = null;
            for (UHCPlayer mate : self.getTeam().get().getPlayers()) {
                if (mate == null || mate.getUniqueId().equals(self.getUniqueId())) continue;
                if (!mate.isPlaying() || !mate.isOnline()) continue;
                Player candidat = mate.getPlayer();
                if (candidat == null || candidat.isDead()) continue;
                if (receveur == null || candidat.getHealth() < receveur.getHealth()) receveur = candidat;
            }
            if (receveur == null) return;

            event.setCancelled(true);
            double montant = event.getAmount() * pourcentageTransfere / 100.0;
            if (montant <= 0) return;

            receveur.setHealth(Math.min(receveur.getMaxHealth(), receveur.getHealth() + montant));
            DisplayService.actionBar(player, t(DONNE, player, Map.of("%joueur%", receveur.getName())));
            DisplayService.actionBar(receveur, t(RECU, receveur, Map.of("%joueur%", player.getName())));
        }
    }

    public static class BloodFusionScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.bloodfusion.desc",
                "§7Chaque objet sorti d'un four coûte de la vie au mineur le plus proche.");

        private static final DynamicLang COUT = DynamicLang.of("scenario.bloodfusion.cout",
                "§cLa fusion t'a coûté de la vie.");

        @Var(name = "Dégâts par fusion", desc = "Dégâts infligés à chaque objet sorti d'un four.", type = VariableType.DOUBLE, min = 0.5)
        private double degats = 1.0;

        @Var(name = "Portée du four", desc = "Distance maximale entre le four et le joueur qui paie la fusion.", type = VariableType.INTEGER, min = 1)
        private int portee = 8;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Blood Fusion"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.FURNACE); }

        @Override
        public void onFurnace(ItemStack result, FurnaceSmeltEvent event) {
            if (!isActive()) return;
            Location four = event.getBlock().getLocation().add(0.5D, 0.5D, 0.5D);

            Player plusProche = null;
            double meilleure = portee * (double) portee;
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player candidat = uhcPlayer.getPlayer();
                if (candidat == null || !candidat.getWorld().equals(four.getWorld())) continue;
                double distance = candidat.getLocation().distanceSquared(four);
                if (distance > meilleure) continue;
                meilleure = distance;
                plusProche = candidat;
            }
            if (plusProche == null) return;

            plusProche.damage(degats);
            DisplayService.actionBar(plusProche, t(COUT, plusProche));
        }
    }

    public static class DoubleHealingScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.doublehealing.desc",
                "§7Pommes d'or et têtes soignent §e×%facteur%§7, et l'absorption monte à §e%absorption% cœurs§7.");

        @Var(name = "Multiplicateur de soin", desc = "Facteur appliqué aux soins des pommes d'or, têtes et potions.", type = VariableType.DOUBLE, min = 1.0)
        private double multiplicateur = 2.0;

        @Var(name = "Cœurs d'absorption", desc = "Cœurs d'absorption donnés par une pomme d'or.", type = VariableType.INTEGER, min = 1)
        private int coeursAbsorption = 3;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Double Healing"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%facteur%", multiplicateur, "%absorption%", coeursAbsorption));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLDEN_APPLE); }

        @EventHandler
        public void onRegain(EntityRegainHealthEvent event) {
            if (!isRunning()) return;
            RegainReason reason = event.getRegainReason();
            if (reason != RegainReason.MAGIC && reason != RegainReason.MAGIC_REGEN) return;
            if (!(event.getEntity() instanceof Player)) return;
            event.setAmount(event.getAmount() * multiplicateur);
        }

        @Override
        public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
            if (!isActive()) return;
            if (item == null || item.getType() != Material.GOLDEN_APPLE) return;

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || player.isDead()) return;
                    PlayerUtils.setAbsorptionHearts(player, coeursAbsorption * 2.0);
                }
            }.runTaskLater(Main.get(), 1L);
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new HundredHeartsScenario(),
                new NineLivesScenario(),
                new AbsorptionSwitchScenario(),
                new AquaphobiaScenario(),
                new AstrophobiaScenario(),
                new AurophobiaScenario(),
                new BirdsScenario(),
                new BlackPlagueScenario(),
                new BlitzScenario(),
                new BloodBrothersScenario(),
                new BloodFusionScenario(),
                new DoubleHealingScenario()
        );
    }
}
