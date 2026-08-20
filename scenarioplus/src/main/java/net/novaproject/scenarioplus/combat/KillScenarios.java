package net.novaproject.scenarioplus.combat;

import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.player.utils.PlayerUtils;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.utils.cooldown.CooldownService;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class KillScenarios {

    private KillScenarios() {
    }

    private static Player shooterOf(Entity damager) {
        if (damager instanceof Player player) return player;
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) return player;
        return null;
    }

    private static double distanceBetween(Player first, Player second) {
        if (!first.getWorld().equals(second.getWorld())) return -1;
        return first.getLocation().distance(second.getLocation());
    }

    public static class RiskyFallScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.riskyfall.desc",
                "§7Les dégâts de chute sont soit totalement annulés (§e%chance%%§7), soit multipliés par §e×%facteur%§7.");


        @Var(name = "Chance d'annulation", desc = "Chance que les dégâts de chute soient totalement annulés.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceAnnulation = 50;

        @Var(name = "Multiplicateur", desc = "Facteur appliqué aux dégâts de chute quand ils ne sont pas annulés.", type = VariableType.DOUBLE, min = 1)
        private double multiplicateur = 2.0;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Risky Fall"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%chance%", chanceAnnulation, "%facteur%", multiplicateur));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.FEATHER); }

        @Override
        public void onDamage(Player player, EntityDamageEvent event) {
            if (!isActive()) return;
            if (event.getCause() != DamageCause.FALL) return;

            if (UHCUtils.Rng.chance(chanceAnnulation)) {
                event.setCancelled(true);
                return;
            }
            event.setDamage(event.getDamage() * multiplicateur);
        }
    }

    public static class SelectFireScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.selectfire.desc",
                "§7Clic gauche avec un arc pour changer de munition : explosion, creeper, flamme ou TNT.");

        private static final DynamicLang MODE_EXPLOSION = DynamicLang.of("scenario.selectfire.mode-explosion", "§cExplosion");
        private static final DynamicLang MODE_CREEPER = DynamicLang.of("scenario.selectfire.mode-creeper", "§aCreeper");
        private static final DynamicLang MODE_FLAMME = DynamicLang.of("scenario.selectfire.mode-flamme", "§6Flamme");
        private static final DynamicLang MODE_TNT = DynamicLang.of("scenario.selectfire.mode-tnt", "§4TNT");

        private static final DynamicLang SWITCHED = DynamicLang.of("scenario.selectfire.switched",
                "§7Munition sélectionnée : %mode%");

        private static final int MODE_COUNT = 4;

        private final Map<UUID, Integer> modes = new HashMap<>();

        @Var(name = "Puissance de l'explosion", desc = "Puissance de l'explosion en mode Explosion.", type = VariableType.DOUBLE, min = 0)
        private double puissanceExplosion = 2.0;

        @Var(name = "Mèche de la TNT", desc = "Durée de la mèche de la TNT amorcée à l'impact, en ticks.", type = VariableType.INTEGER, min = 1)
        private int mecheTicks = 40;

        @Var(name = "Durée du feu", desc = "Durée pendant laquelle les cibles brûlent en mode Flamme.", type = VariableType.TIME, min = 1)
        private int dureeFeuSec = 5;

        @Var(name = "Rayon de la flamme", desc = "Rayon d'embrasement autour de l'impact en mode Flamme.", type = VariableType.DOUBLE, min = 1)
        private double rayonFeu = 2.0;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Select Fire"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.FIREBALL); }

        @Override
        public void onPlayerInteract(Player player, PlayerInteractEvent event) {
            if (!isActive()) return;
            Action action = event.getAction();
            if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) return;

            ItemStack item = player.getItemInHand();
            if (item == null || item.getType() != Material.BOW) return;

            UUID uuid = player.getUniqueId();
            int next = (modes.getOrDefault(uuid, 0) + 1) % MODE_COUNT;
            modes.put(uuid, next);

            DisplayService.actionBar(player, t(SWITCHED, player, Map.of("%mode%", t(modeLabel(next), player))));
            player.playSound(player.getLocation(), Sound.CLICK, 1f, 1.6f);
        }

        @Override
        public void onProjectileHit(ProjectileHitEvent event) {
            if (!isActive()) return;
            if (!(event.getEntity() instanceof Arrow arrow)) return;
            if (!(arrow.getShooter() instanceof Player shooter)) return;

            Location loc = arrow.getLocation();
            World world = loc.getWorld();
            int mode = modes.getOrDefault(shooter.getUniqueId(), 0);
            arrow.remove();

            switch (mode) {
                case 0 -> world.createExplosion(loc.getX(), loc.getY(), loc.getZ(), (float) puissanceExplosion, false, false);
                case 1 -> world.spawn(loc, Creeper.class);
                case 2 -> ignite(loc);
                default -> world.spawn(loc, TNTPrimed.class).setFuseTicks(mecheTicks);
            }
        }

        private void ignite(Location loc) {
            Block block = loc.getBlock();
            if (block.getType() == Material.AIR) block.setType(Material.FIRE);

            for (Entity nearby : loc.getWorld().getNearbyEntities(loc, rayonFeu, rayonFeu, rayonFeu)) {
                if (nearby instanceof LivingEntity living) living.setFireTicks(dureeFeuSec * 20);
            }
        }

        private DynamicLang modeLabel(int mode) {
            return switch (mode) {
                case 0 -> MODE_EXPLOSION;
                case 1 -> MODE_CREEPER;
                case 2 -> MODE_FLAMME;
                default -> MODE_TNT;
            };
        }

        @Override
        public void onStop() {
            modes.clear();
        }
    }

    public static class SlenderSniperScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.slendersniper.desc",
                "§7Tu démarres avec §e%fleches% flèches§7 : elles ne se craftent pas et les squelettes n'en lâchent plus. Un kill te remet à §e%fleches%§7.");

        private static final DynamicLang REFILL = DynamicLang.of("scenario.slendersniper.refill",
                "§7Ton carquois est rechargé à §e%fleches% flèches§7.");

        private static final DynamicLang CRAFT_DENIED = DynamicLang.of("scenario.slendersniper.craft-denied",
                "§cLes flèches ne peuvent pas être craftées.");

        @Var(name = "Flèches de départ", desc = "Nombre de flèches données au départ et rendues à chaque kill.", type = VariableType.INTEGER, min = 1, max = 64)
        private int flechesDepart = 16;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Slender Sniper"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%fleches%", flechesDepart));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ARROW); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            refill(player);
        }

        @Override
        public void onKill(UHCPlayer killer, UHCPlayer victim) {
            if (!isActive()) return;
            if (killer == null) return;

            Player player = killer.getPlayer();
            if (player == null) return;

            refill(player);
            LangManager.get().send(REFILL, player, Map.of("%fleches%", flechesDepart));
        }

        @Override
        public void onCraft(ItemStack result, CraftItemEvent event) {
            if (!isActive()) return;
            if (result == null || result.getType() != Material.ARROW) return;

            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                LangManager.get().send(CRAFT_DENIED, player);
            }
        }

        @Override
        public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
            if (!isActive()) return;
            if (!(entity instanceof Skeleton)) return;

            event.getDrops().removeIf(drop -> drop != null && drop.getType() == Material.ARROW);
        }

        private void refill(Player player) {
            player.getInventory().remove(Material.ARROW);
            player.getInventory().addItem(new ItemStack(Material.ARROW, flechesDepart));
        }
    }

    public static class SnowballFightersScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.snowballfighters.desc",
                "§7Les boules de neige infligent de §e%min% §7à §e%max% cœurs §7selon la distance. Arcs désactivés, mêlée limitée à la hache en pierre et à l'épée en bois.");

        private static final DynamicLang BOW_DENIED = DynamicLang.of("scenario.snowballfighters.bow-denied",
                "§cLes arcs sont désactivés.");

        private static final DynamicLang WEAPON_DENIED = DynamicLang.of("scenario.snowballfighters.weapon-denied",
                "§cCette arme est trop puissante pour ce scénario.");

        private static final Set<Material> FORBIDDEN_WEAPONS = EnumSet.of(
                Material.STONE_SWORD,
                Material.IRON_SWORD,
                Material.GOLD_SWORD,
                Material.DIAMOND_SWORD,
                Material.IRON_AXE,
                Material.GOLD_AXE,
                Material.DIAMOND_AXE
        );

        @Var(name = "Dégâts minimum", desc = "Dégâts en cœurs d'une boule de neige tirée à bout portant.", type = VariableType.DOUBLE, min = 0)
        private double degatsMin = 1.75;

        @Var(name = "Dégâts maximum", desc = "Dégâts en cœurs d'une boule de neige tirée à la distance maximale.", type = VariableType.DOUBLE, min = 0)
        private double degatsMax = 2.75;

        @Var(name = "Distance de plein dégât", desc = "Distance à partir de laquelle une boule de neige inflige ses dégâts maximum.", type = VariableType.DOUBLE, min = 1)
        private double distancePlein = 40;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Snowball Fighters"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%min%", degatsMin, "%max%", degatsMax));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SNOW_BALL); }

        @Override
        public void onBow(Entity entity, Player player, EntityShootBowEvent event) {
            if (!isActive()) return;
            event.setCancelled(true);
            DisplayService.actionBar(player, t(BOW_DENIED, player));
        }

        @Override
        public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (!(entity instanceof Player victim)) return;

            if (damager instanceof Snowball snowball) {
                if (!(snowball.getShooter() instanceof Player shooter)) return;
                double distance = distanceBetween(shooter, victim);
                if (distance < 0) return;

                double ratio = Math.min(1.0, distance / distancePlein);
                event.setDamage((degatsMin + (degatsMax - degatsMin) * ratio) * 2.0);
                return;
            }

            if (!(damager instanceof Player attacker)) return;
            ItemStack weapon = attacker.getItemInHand();
            if (weapon == null || !FORBIDDEN_WEAPONS.contains(weapon.getType())) return;

            event.setCancelled(true);
            DisplayService.actionBar(attacker, t(WEAPON_DENIED, attacker));
        }
    }

    public static class SnowballFlightScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.snowballflight.desc",
                "§7Clic droit avec une boule de neige pour t'envoler sur §e%distance% blocs§7, sans dégâts de chute. Recharge : §e%cooldown% s§7.");

        private static final DynamicLang ON_COOLDOWN = DynamicLang.of("scenario.snowballflight.cooldown",
                "§cEncore §e%temps% s §cavant de pouvoir redécoller.");

        private static final String COOLDOWN_KEY = "SnowballFlightCooldown";

        private final Set<UUID> enVol = new HashSet<>();

        @Var(name = "Distance du vol", desc = "Distance parcourue en blocs par le vol.", type = VariableType.DOUBLE, min = 1)
        private double distanceVol = 14;

        @Var(name = "Élan vertical", desc = "Poussée verticale appliquée au décollage.", type = VariableType.DOUBLE, min = 0)
        private double elanVertical = 0.8;

        @Var(name = "Recharge", desc = "Temps d'attente entre deux vols.", type = VariableType.TIME, min = 0)
        private int cooldownSec = 10;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Snowball Flight"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%distance%", distanceVol, "%cooldown%", cooldownSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SNOW_BLOCK); }

        @Override
        public void onPlayerInteract(Player player, PlayerInteractEvent event) {
            if (!isActive()) return;
            Action action = event.getAction();
            if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

            ItemStack item = event.getItem();
            if (item == null || item.getType() != Material.SNOW_BALL) return;

            event.setCancelled(true);

            if (!CooldownService.put(player, COOLDOWN_KEY, cooldownSec * 1000L)) {
                long remaining = (CooldownService.get(player, COOLDOWN_KEY) + 999L) / 1000L;
                DisplayService.actionBar(player, t(ON_COOLDOWN, player, Map.of("%temps%", remaining)));
                return;
            }

            ItemCreator.consumeOne(player, item);
            enVol.add(player.getUniqueId());
            PlayerUtils.pushBlocks(player, player.getLocation().getDirection(), distanceVol, elanVertical);
            player.playSound(player.getLocation(), Sound.SHOOT_ARROW, 1f, 1.4f);
        }

        @Override
        public void onDamage(Player player, EntityDamageEvent event) {
            if (!isActive()) return;
            if (event.getCause() != DamageCause.FALL) return;
            if (!enVol.remove(player.getUniqueId())) return;

            event.setCancelled(true);
        }

        @Override
        public void onStop() {
            enVol.clear();
        }
    }

    public static class SpeedShotScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.speedshot.desc",
                "§7Toucher un joueur à l'arc à §e%distance% blocs §7ou plus t'accorde §eVitesse %niveau% §7pendant §e%duree% s§7.");

        private static final DynamicLang REWARD = DynamicLang.of("scenario.speedshot.reward",
                "§7Tir longue portée : §eVitesse %niveau% §7pendant §e%duree% s§7.");

        @Var(name = "Distance minimum", desc = "Distance minimale du tir pour déclencher la récompense.", type = VariableType.INTEGER, min = 1)
        private int distanceMin = 50;

        @Var(name = "Durée de la vitesse", desc = "Durée de l'effet Vitesse accordé au tireur.", type = VariableType.TIME, min = 1)
        private int dureeSec = 10;

        @Var(name = "Niveau de vitesse", desc = "Niveau de l'effet Vitesse accordé au tireur.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveau = 2;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Speed Shot"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%distance%", distanceMin, "%niveau%", niveau, "%duree%", dureeSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BOW); }

        @Override
        public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (!(entity instanceof Player victim)) return;
            if (!(damager instanceof Arrow arrow)) return;
            if (!(arrow.getShooter() instanceof Player shooter)) return;
            if (shooter.equals(victim)) return;

            double distance = distanceBetween(shooter, victim);
            if (distance < distanceMin) return;

            shooter.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, dureeSec * 20, niveau - 1), true);
            LangManager.get().send(REWARD, shooter, Map.of("%niveau%", niveau, "%duree%", dureeSec));
        }
    }

    public static class SurfacePvpScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.surfacepvp.desc",
                "§7Le PvP est désactivé sous la couche §eY=%altitude%§7.");

        private static final DynamicLang DENIED = DynamicLang.of("scenario.surfacepvp.denied",
                "§cPas de PvP sous la couche §eY=%altitude%§c.");

        @Var(name = "Altitude minimum", desc = "Couche sous laquelle le PvP est interdit.", type = VariableType.INTEGER, min = 0, max = 256)
        private int altitudeMin = 55;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Surface PvP"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%altitude%", altitudeMin));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GRASS); }

        @Override
        public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (!(entity instanceof Player victim)) return;

            Player attacker = shooterOf(damager);
            if (attacker == null || attacker.equals(victim)) return;
            if (victim.getLocation().getY() >= altitudeMin && attacker.getLocation().getY() >= altitudeMin) return;

            event.setCancelled(true);
            DisplayService.actionBar(attacker, t(DENIED, attacker, Map.of("%altitude%", altitudeMin)));
        }
    }

    public static class SwingersScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.swingers.desc",
                "§7Fais un clic droit sur le premier joueur d'une autre équipe : vos deux équipes fusionnent. Limite : §e%fusions% §7par joueur.");

        private static final DynamicLang MERGED = DynamicLang.of("scenario.swingers.merged",
                "§e%joueur% §7et §e%cible% §7ont fusionné leurs équipes.");

        private final Map<UUID, Integer> fusions = new HashMap<>();

        @Var(name = "Fusions par joueur", desc = "Nombre de fusions d'équipe qu'un même joueur peut déclencher.", type = VariableType.INTEGER, min = 1)
        private int fusionsMax = 1;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Swingers"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%fusions%", fusionsMax));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BANNER); }

        @Override
        public void onPlayerInteractEntity(Player player, PlayerInteractEntityEvent event) {
            if (!isActive()) return;
            if (UHCManager.get().isLobby()) return;
            if (!(event.getRightClicked() instanceof Player target)) return;

            UHCPlayer actor = UHCPlayerManager.get().getPlayer(player);
            UHCPlayer other = UHCPlayerManager.get().getPlayer(target);
            if (actor == null || other == null) return;

            Optional<UHCTeam> actorTeam = actor.getTeam();
            Optional<UHCTeam> otherTeam = other.getTeam();
            if (actorTeam.isEmpty() || otherTeam.isEmpty()) return;
            if (actorTeam.get().equals(otherTeam.get())) return;

            UUID uuid = player.getUniqueId();
            int used = fusions.getOrDefault(uuid, 0);
            if (used >= fusionsMax) return;
            fusions.put(uuid, used + 1);

            for (UHCPlayer member : otherTeam.get().getPlayers()) {
                member.forceSetTeam(actorTeam);
            }

            LangManager.get().sendAll(MERGED, Map.of("%joueur%", player.getName(), "%cible%", target.getName()));
        }

        @Override
        public void onStop() {
            fusions.clear();
        }
    }

    public static class ZoomiesScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.zoomies.desc",
                "§7Toutes les créatures bénéficient en permanence de §eVitesse %niveau%§7.");

        private BukkitRunnable tache;

        @Var(name = "Niveau de vitesse", desc = "Niveau de l'effet Vitesse appliqué aux créatures.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveau = 3;

        @Var(name = "Rythme d'application", desc = "Intervalle entre deux applications de l'effet aux créatures.", type = VariableType.TIME, min = 1)
        private int periodeSec = 3;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Zoomies"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%niveau%", niveau));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SUGAR); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;

            int duration = periodeSec * 20 + 40;
            int amplifier = niveau - 1;

            tache = new BukkitRunnable() {
                @Override
                public void run() {
                    for (World world : Bukkit.getWorlds()) {
                        for (LivingEntity living : world.getLivingEntities()) {
                            if (living instanceof Player || living instanceof ArmorStand) continue;
                            living.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, amplifier), true);
                        }
                    }
                }
            };
            tache.runTaskTimer(Main.get(), 0L, periodeSec * 20L);
        }

        @Override
        public void onStop() {
            if (tache != null) {
                tache.cancel();
                tache = null;
            }
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new RiskyFallScenario(),
                new SelectFireScenario(),
                new SlenderSniperScenario(),
                new SnowballFightersScenario(),
                new SnowballFlightScenario(),
                new SpeedShotScenario(),
                new SurfacePvpScenario(),
                new SwingersScenario(),
                new ZoomiesScenario()
        );
    }
}
