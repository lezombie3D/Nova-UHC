package net.novaproject.scenarioplus.combat;

import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.command.CommandManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.ui.InvseeUi;
import net.novaproject.novauhc.utils.chat.TextUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.nms.EquipmentHider;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class ArrowScenarios {

    private static final Random RANDOM = new Random();

    private ArrowScenarios() {
    }

    private static Player meleeOrArrowShooter(Entity damager) {
        if (damager instanceof Player player) return player;
        if (damager instanceof Arrow arrow && arrow.getShooter() instanceof Player shooter) return shooter;
        return null;
    }

    public static class CreeperPongScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.creeperpong.desc",
                "§7Chacun démarre avec §a%oeufs% œufs de creeper chargé§7, un bâton §eKnockback %knockback% §7et un briquet §eSolidité %solidite%§7.");

        private static final short CREEPER_EGG = 50;

        @Var(name = "Œufs de creeper", desc = "Nombre d'œufs de creeper donnés au départ.", type = VariableType.INTEGER, min = 1)
        private int oeufs = 64;

        @Var(name = "Niveau de Knockback", desc = "Niveau de Knockback du bâton donné au départ.", type = VariableType.INTEGER, min = 1, max = 20)
        private int knockback = 10;

        @Var(name = "Niveau de Solidité", desc = "Niveau de Solidité du briquet donné au départ.", type = VariableType.INTEGER, min = 1, max = 20)
        private int solidite = 10;

        @Var(name = "Creepers chargés", desc = "Les creepers issus d'un œuf apparaissent chargés en électricité.", type = VariableType.BOOLEAN)
        private boolean charges = true;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Creeper Pong"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%oeufs%", oeufs, "%knockback%", knockback, "%solidite%", solidite));
        }

        @Override
        public ItemCreator getItem() {
            return new ItemCreator(Material.MONSTER_EGG).setDurability(CREEPER_EGG);
        }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            player.getInventory().addItem(new ItemCreator(Material.MONSTER_EGG)
                    .setDurability(CREEPER_EGG)
                    .setAmount(oeufs)
                    .getItemstack());
            player.getInventory().addItem(new ItemCreator(Material.STICK)
                    .addEnchantment(Enchantment.KNOCKBACK, knockback)
                    .getItemstack());
            player.getInventory().addItem(new ItemCreator(Material.FLINT_AND_STEEL)
                    .addEnchantment(Enchantment.DURABILITY, solidite)
                    .getItemstack());
        }

        @EventHandler
        public void onEggSpawn(CreatureSpawnEvent event) {
            if (!isRunning()) return;
            if (!charges) return;
            if (event.getSpawnReason() != SpawnReason.SPAWNER_EGG) return;
            if (event.getEntity() instanceof Creeper creeper) creeper.setPowered(true);
        }
    }

    public static class DamageCycleScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.damagecycle.desc",
                "§7Toutes les §e%duree%§7, un type de dégâts est tiré au sort : le subir inflige §c×%multiplicateur% §7dégâts.");

        private static final DynamicLang ANNOUNCE = DynamicLang.of("scenario.damagecycle.announce",
                "§c☠ §7Dégâts maudits pour §e%duree% §7: §c%type% §7(§c×%multiplicateur%§7).");

        private static final Map<DamageCause, DynamicLang> CAUSES = new LinkedHashMap<>();
        private static final List<DamageCause> CAUSE_KEYS;

        static {
            cause(DamageCause.ENTITY_ATTACK, "les coups au corps à corps");
            cause(DamageCause.PROJECTILE, "les projectiles");
            cause(DamageCause.FALL, "les chutes");
            cause(DamageCause.LAVA, "la lave");
            cause(DamageCause.FIRE, "le feu");
            cause(DamageCause.FIRE_TICK, "les brûlures");
            cause(DamageCause.DROWNING, "la noyade");
            cause(DamageCause.SUFFOCATION, "l'étouffement");
            cause(DamageCause.CONTACT, "les cactus");
            cause(DamageCause.POISON, "le poison");
            cause(DamageCause.BLOCK_EXPLOSION, "les explosions de blocs");
            cause(DamageCause.ENTITY_EXPLOSION, "les explosions de créatures");
            CAUSE_KEYS = List.copyOf(CAUSES.keySet());
        }

        private static void cause(DamageCause cause, String frenchName) {
            CAUSES.put(cause, DynamicLang.of("scenario.damagecycle.cause." + cause.name().toLowerCase(), frenchName));
        }

        @Var(name = "Intervalle", desc = "Secondes entre deux tirages du type de dégâts maudit.", type = VariableType.TIME, min = 1)
        private int intervalSec = 600;

        @Var(name = "Multiplicateur", desc = "Facteur appliqué aux dégâts du type maudit.", type = VariableType.DOUBLE, min = 1)
        private double multiplicateur = 3.0;

        private DamageCause current;
        private BukkitRunnable task;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Damage Cycle"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%duree%", TextUtils.getFormattedTime(intervalSec), "%multiplicateur%", multiplicateur));
        }

        @Override
        public ItemCreator getItem() { return new ItemCreator(Material.REDSTONE_TORCH_ON); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            draw();
            if (task != null) task.cancel();
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isActive()) { cancel(); return; }
                    draw();
                }
            };
            long period = Math.max(1, intervalSec) * 20L;
            task.runTaskTimer(Main.get(), period, period);
        }

        private void draw() {
            current = CAUSE_KEYS.get(RANDOM.nextInt(CAUSE_KEYS.size()));
            LangManager.get().sendAll(ANNOUNCE, Map.of(
                    "%type%", t(CAUSES.get(current)),
                    "%duree%", TextUtils.getFormattedTime(intervalSec),
                    "%multiplicateur%", multiplicateur));
        }

        @Override
        public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {
            if (!isActive()) return;
            if (current == null) return;
            if (event.getCause() != current) return;
            event.setDamage(event.getDamage() * multiplicateur);
        }

        @Override
        public void onStop() {
            if (task != null) {
                task.cancel();
                task = null;
            }
            current = null;
        }
    }

    public static class EnemyReconScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.enemyrecon.desc",
                "§7Chaque kill donne §e%gain% §7reconnaissance(s) : §e/er <joueur> §7ouvre son inventaire, avec §c%chance%% §7de chances d'être annoncé à tous.");

        private static final DynamicLang EARNED = DynamicLang.of("scenario.enemyrecon.earned",
                "§7Reconnaissance gagnée : §e%restant% §7utilisation(s) disponible(s) — §e/er <joueur>§7.");

        private static final DynamicLang NO_USE = DynamicLang.of("scenario.enemyrecon.no-use",
                "§cTu n'as plus aucune reconnaissance : élimine un joueur pour en gagner.");

        private static final DynamicLang USAGE = DynamicLang.of("scenario.enemyrecon.usage",
                "§7Utilisation : §e/er <joueur>§7.");

        private static final DynamicLang UNKNOWN = DynamicLang.of("scenario.enemyrecon.unknown",
                "§cCe joueur est introuvable.");

        private static final DynamicLang SELF = DynamicLang.of("scenario.enemyrecon.self",
                "§cTu ne peux pas t'espionner toi-même.");

        private static final DynamicLang SPIED = DynamicLang.of("scenario.enemyrecon.spied",
                "§7Inventaire de §e%cible% §7consulté — §e%restant% §7utilisation(s) restante(s).");

        private static final DynamicLang BROADCAST = DynamicLang.of("scenario.enemyrecon.broadcast",
                "§c☠ §e%espion% §7vient d'espionner l'inventaire de §e%cible%§7.");

        private final Map<UUID, Integer> uses = new HashMap<>();

        @Var(name = "Reconnaissances par kill", desc = "Utilisations de /er gagnées à chaque élimination.", type = VariableType.INTEGER, min = 0)
        private int gainParKill = 1;

        @Var(name = "Reconnaissances de départ", desc = "Utilisations de /er données au début de la partie.", type = VariableType.INTEGER, min = 0)
        private int gainDepart = 0;

        @Var(name = "Chance d'être repéré", desc = "Chance que l'espionnage soit annoncé à toute la partie.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceAnnonce = 30;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Enemy Recon"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%gain%", gainParKill, "%chance%", chanceAnnonce));
        }

        @Override
        public ItemCreator getItem() { return new ItemCreator(Material.COMPASS); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            uses.put(player.getUniqueId(), gainDepart);
        }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            CommandManager.get().register("er", new Command.PlayerCommand() {
                @Override
                protected void run(Player player, CommandArguments args) {
                    spy(player, args);
                }
            });
        }

        @Override
        public void onKill(UHCPlayer killer, UHCPlayer victim) {
            if (!isActive()) return;
            if (killer == null || gainParKill <= 0) return;
            Player player = killer.getPlayer();
            if (player == null) return;
            int left = uses.getOrDefault(player.getUniqueId(), 0) + gainParKill;
            uses.put(player.getUniqueId(), left);
            LangManager.get().send(EARNED, player, Map.of("%restant%", left));
        }

        private void spy(Player player, CommandArguments args) {
            if (!isActive()) return;
            int left = uses.getOrDefault(player.getUniqueId(), 0);
            if (left <= 0) {
                LangManager.get().send(NO_USE, player);
                return;
            }
            if (args.size() < 1) {
                LangManager.get().send(USAGE, player);
                return;
            }
            Player target = args.getPlayer(0);
            if (target == null) {
                LangManager.get().send(UNKNOWN, player);
                return;
            }
            if (target.equals(player)) {
                LangManager.get().send(SELF, player);
                return;
            }
            uses.put(player.getUniqueId(), left - 1);
            new InvseeUi(player, target).open();
            LangManager.get().send(SPIED, player, Map.of("%cible%", target.getName(), "%restant%", left - 1));
            if (UHCUtils.Rng.chance(chanceAnnonce)) {
                LangManager.get().sendAll(BROADCAST, Map.of("%espion%", player.getName(), "%cible%", target.getName()));
            }
        }

        @Override
        public void onStop() {
            uses.clear();
        }
    }

    public static class ExplosiveArrowsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.explosivearrows.desc",
                "§7Une flèche tirée à pleine tension §cexplose §7à l'impact (puissance §c%puissance%§7).");

        private static final String EXPLOSIVE = "scenarioplus_explosive_arrow";

        @Var(name = "Puissance", desc = "Puissance de l'explosion provoquée par la flèche.", type = VariableType.DOUBLE, min = 0)
        private double puissance = 2.0;

        @Var(name = "Tension minimale", desc = "Tension de l'arc nécessaire pour armer une flèche explosive.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int tensionMin = 100;

        @Var(name = "Met le feu", desc = "L'explosion enflamme les blocs autour du point d'impact.", type = VariableType.BOOLEAN)
        private boolean feu = false;

        @Var(name = "Casse les blocs", desc = "L'explosion détruit les blocs autour du point d'impact.", type = VariableType.BOOLEAN)
        private boolean casseBlocs = false;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Explosive Arrows"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%puissance%", puissance));
        }

        @Override
        public ItemCreator getItem() { return new ItemCreator(Material.ARROW); }

        @Override
        public void onBow(Entity entity, Player player, EntityShootBowEvent event) {
            if (!isActive()) return;
            if (event.getForce() * 100.0F < tensionMin) return;
            Entity projectile = event.getProjectile();
            if (!(projectile instanceof Arrow)) return;
            projectile.setMetadata(EXPLOSIVE, new FixedMetadataValue(Main.get(), true));
        }

        @Override
        public void onProjectileHit(ProjectileHitEvent event) {
            if (!isActive()) return;
            if (!(event.getEntity() instanceof Arrow arrow)) return;
            if (!arrow.hasMetadata(EXPLOSIVE)) return;
            Location loc = arrow.getLocation();
            arrow.remove();
            loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), (float) puissance, feu, casseBlocs);
        }
    }

    public static class FastGetawayScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.fastgetaway.desc",
                "§7Un kill donne §bVitesse %niveau% §7pendant §e%duree%§7, coupée §e%annulation% §7après avoir frappé un adversaire.");

        private static final DynamicLang GRANTED = DynamicLang.of("scenario.fastgetaway.granted",
                "§bFuite rapide §7: §bVitesse %niveau% §7pendant §e%duree%§7.");

        private static final DynamicLang FADING = DynamicLang.of("scenario.fastgetaway.fading",
                "§7Tu as repris le combat : ta vitesse s'éteint dans §e%duree%§7.");

        private final Set<UUID> buffed = new HashSet<>();
        private final Set<UUID> pending = new HashSet<>();

        @Var(name = "Durée", desc = "Durée de la vitesse offerte par un kill.", type = VariableType.TIME, min = 1)
        private int dureeSec = 45;

        @Var(name = "Niveau de Vitesse", desc = "Niveau de l'effet Vitesse offert par un kill.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveau = 2;

        @Var(name = "Délai d'annulation", desc = "Secondes entre un coup porté et la perte de la vitesse.", type = VariableType.TIME, min = 1)
        private int annulationSec = 5;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Fast Getaway"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of(
                    "%niveau%", niveau,
                    "%duree%", TextUtils.getFormattedTime(dureeSec),
                    "%annulation%", TextUtils.getFormattedTime(annulationSec)));
        }

        @Override
        public ItemCreator getItem() { return new ItemCreator(Material.FEATHER); }

        @Override
        public void onKill(UHCPlayer killer, UHCPlayer victim) {
            if (!isActive()) return;
            if (killer == null) return;
            Player player = killer.getPlayer();
            if (player == null) return;
            player.removePotionEffect(PotionEffectType.SPEED);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, dureeSec * 20, niveau - 1));
            buffed.add(player.getUniqueId());
            LangManager.get().send(GRANTED, player, Map.of(
                    "%niveau%", niveau,
                    "%duree%", TextUtils.getFormattedTime(dureeSec)));
        }

        @Override
        public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (!(entity instanceof Player)) return;
            Player attacker = meleeOrArrowShooter(damager);
            if (attacker == null || attacker.equals(entity)) return;
            UUID uuid = attacker.getUniqueId();
            if (!buffed.contains(uuid)) return;
            if (!pending.add(uuid)) return;
            LangManager.get().send(FADING, attacker, Map.of("%duree%", TextUtils.getFormattedTime(annulationSec)));
            new BukkitRunnable() {
                @Override
                public void run() {
                    pending.remove(uuid);
                    if (!buffed.remove(uuid)) return;
                    Player online = Bukkit.getPlayer(uuid);
                    if (online != null) online.removePotionEffect(PotionEffectType.SPEED);
                }
            }.runTaskLater(Main.get(), annulationSec * 20L);
        }

        @Override
        public void onStop() {
            buffed.clear();
            pending.clear();
        }
    }

    public static class LuckyBuffScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.luckybuff.desc",
                "§7Chaque joueur porte un bonus aléatoire, retiré au sort toutes les §e%duree%§7.");

        private static final DynamicLang ROLLED = DynamicLang.of("scenario.luckybuff.rolled",
                "§aNouveau bonus §7: §a%effet% %niveau% §7pendant §e%duree%§7.");

        private static final Map<PotionEffectType, DynamicLang> BUFFS = new LinkedHashMap<>();
        private static final List<PotionEffectType> BUFF_KEYS;

        static {
            buff(PotionEffectType.REGENERATION, "Régénération");
            buff(PotionEffectType.DAMAGE_RESISTANCE, "Résistance");
            buff(PotionEffectType.FAST_DIGGING, "Célérité");
            buff(PotionEffectType.INCREASE_DAMAGE, "Force");
            buff(PotionEffectType.JUMP, "Détente");
            buff(PotionEffectType.FIRE_RESISTANCE, "Résistance au feu");
            buff(PotionEffectType.WATER_BREATHING, "Apnée");
            BUFF_KEYS = List.copyOf(BUFFS.keySet());
        }

        private static void buff(PotionEffectType type, String frenchName) {
            BUFFS.put(type, DynamicLang.of("scenario.luckybuff.effect." + type.getName().toLowerCase(), frenchName));
        }

        private final Map<UUID, PotionEffectType> current = new HashMap<>();

        @Var(name = "Intervalle", desc = "Secondes entre deux tirages du bonus aléatoire.", type = VariableType.TIME, min = 1)
        private int intervalSec = 600;

        @Var(name = "Niveau du bonus", desc = "Niveau de l'effet tiré au sort.", type = VariableType.INTEGER, min = 1, max = 3)
        private int niveau = 1;

        private BukkitRunnable task;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "LuckyBuff"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%duree%", TextUtils.getFormattedTime(intervalSec)));
        }

        @Override
        public ItemCreator getItem() { return new ItemCreator(Material.POTION); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            roll();
            if (task != null) task.cancel();
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isActive()) { cancel(); return; }
                    roll();
                }
            };
            long period = Math.max(1, intervalSec) * 20L;
            task.runTaskTimer(Main.get(), period, period);
        }

        private void roll() {
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player player = uhcPlayer.getPlayer();
                if (player == null) continue;
                PotionEffectType previous = current.remove(player.getUniqueId());
                if (previous != null) player.removePotionEffect(previous);
                PotionEffectType picked = BUFF_KEYS.get(RANDOM.nextInt(BUFF_KEYS.size()));
                current.put(player.getUniqueId(), picked);
                player.addPotionEffect(new PotionEffect(picked, Math.max(1, intervalSec) * 20, niveau - 1));
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0F, 1.6F);
                LangManager.get().send(ROLLED, player, Map.of(
                        "%effet%", t(BUFFS.get(picked), player),
                        "%niveau%", niveau,
                        "%duree%", TextUtils.getFormattedTime(intervalSec)));
            }
        }

        @Override
        public void onStop() {
            if (task != null) {
                task.cancel();
                task = null;
            }
            current.clear();
        }
    }

    public static class MeleeFunScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.meleefun.desc",
                "§7Le délai d'invulnérabilité entre deux coups tombe à §e%ticks% tick(s) §7: les combos n'ont plus de limite.");

        @Var(name = "Ticks d'invulnérabilité", desc = "Ticks d'invulnérabilité laissés après un coup reçu.", type = VariableType.INTEGER, min = 0, max = 20)
        private int noDamageTicks = 0;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Melee Fun"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%ticks%", noDamageTicks));
        }

        @Override
        public ItemCreator getItem() { return new ItemCreator(Material.IRON_SWORD); }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            if (player == null) return;
            if (player.getMaximumNoDamageTicks() == noDamageTicks) return;
            player.setMaximumNoDamageTicks(noDamageTicks);
        }
    }

    public static class NakedAndAfraidScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.nakedandafraid.desc",
                "§7L'armure des autres joueurs est invisible : tout le monde paraît nu.");

        private final Map<UUID, Integer> hidden = new HashMap<>();

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Naked and Afraid"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override
        public ItemCreator getItem() { return new ItemCreator(Material.LEATHER_CHESTPLATE); }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            if (player == null) return;
            Integer known = hidden.get(player.getUniqueId());
            if (known != null && known == player.getEntityId()) return;
            hidden.put(player.getUniqueId(), player.getEntityId());
            EquipmentHider.setArmorHidden(player, true);
        }

        @Override
        public void onStop() {
            hidden.clear();
            EquipmentHider.clearArmorHidden();
        }
    }

    public static class NintendoSwitchScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.nintendoswitch.desc",
                "§7Toucher une créature à l'arc échange ta position avec la sienne.");

        @Var(name = "Échanger avec les joueurs", desc = "Une flèche touchant un joueur échange aussi les positions.", type = VariableType.BOOLEAN)
        private boolean joueursInclus = false;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Nintendo Switch"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override
        public ItemCreator getItem() { return new ItemCreator(Material.BOW); }

        @Override
        public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (!(damager instanceof Arrow arrow)) return;
            if (!(arrow.getShooter() instanceof Player shooter)) return;
            if (!(entity instanceof LivingEntity)) return;
            if (entity.equals(shooter)) return;
            if (entity instanceof Player && !joueursInclus) return;

            Location shooterLoc = shooter.getLocation();
            Location targetLoc = entity.getLocation();
            if (!shooterLoc.getWorld().equals(targetLoc.getWorld())) return;

            shooter.teleport(new Location(targetLoc.getWorld(), targetLoc.getX(), targetLoc.getY(), targetLoc.getZ(),
                    shooterLoc.getYaw(), shooterLoc.getPitch()));
            entity.teleport(shooterLoc);
            shooter.playSound(shooter.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new CreeperPongScenario(),
                new DamageCycleScenario(),
                new EnemyReconScenario(),
                new ExplosiveArrowsScenario(),
                new FastGetawayScenario(),
                new LuckyBuffScenario(),
                new MeleeFunScenario(),
                new NakedAndAfraidScenario(),
                new NintendoSwitchScenario()
        );
    }
}
