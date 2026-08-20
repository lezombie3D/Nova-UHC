package net.novaproject.scenarioplus.combat;

import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.ability.toolbox.Particles;
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
import org.bukkit.Bukkit;
import net.novaproject.novauhc.Common;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class DamageScenarios {

    private static final Random RANDOM = new Random();

    private DamageScenarios() {
    }

    private static boolean sameWorld(Location first, Location second) {
        return first.getWorld() != null && second.getWorld() != null
                && first.getWorld().equals(second.getWorld());
    }

    public static class OneshotScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.oneshot.desc",
                "§7Toute flèche qui touche un joueur le tue sur le coup.");

        private static final DynamicLang PIERCED = DynamicLang.of("scenario.oneshot.pierced",
                "§4Une flèche t'a transpercé.");

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Oneshot"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ARROW); }

        @Override
        public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (!(entity instanceof Player victim)) return;
            if (!(damager instanceof Arrow)) return;

            event.setDamage(DamageModifier.BASE, victim.getHealth());
            for (DamageModifier modifier : DamageModifier.values()) {
                if (modifier == DamageModifier.BASE) continue;
                if (!event.isApplicable(modifier)) continue;
                event.setDamage(modifier, 0.0D);
            }
            DisplayService.actionBar(victim, t(PIERCED, victim));
        }
    }

    public static class OutbreakScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.outbreak.desc",
                "§7Rester à moins de §e%distance% §7blocs d'un autre joueur risque de te contaminer : Wither pendant §e%duree%s§7.");

        private static final DynamicLang INFECTED = DynamicLang.of("scenario.outbreak.infected",
                "§8Tu as été contaminé par un joueur trop proche.");

        @Var(name = "Distance de sécurité", desc = "Distance en blocs sous laquelle un joueur proche peut te contaminer.", type = VariableType.INTEGER, min = 1)
        private int distanceMin = 6;

        @Var(name = "Risque par seconde", desc = "Chance de contamination par seconde passée trop près d'un joueur.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int risque = 10;

        @Var(name = "Sur-risque si contaminé", desc = "Augmentation du risque quand la victime est déjà sous Wither.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int surRisque = 50;

        @Var(name = "Durée du Wither", desc = "Durée de l'effet Wither appliqué à la contamination.", type = VariableType.TIME, min = 1)
        private int dureeSec = 10;

        @Var(name = "Niveau du Wither", desc = "Niveau de l'effet Wither appliqué à la contamination.", type = VariableType.INTEGER, min = 1)
        private int niveauWither = 2;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Outbreak"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%distance%", distanceMin, "%duree%", dureeSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ROTTEN_FLESH); }

        @Override
        public void onSec(Player p) {
            if (!isActive()) return;
            UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(p);
            if (uhcPlayer == null || !uhcPlayer.isPlaying()) return;
            if (!hasNeighbour(p)) return;

            double chance = risque;
            if (p.hasPotionEffect(PotionEffectType.WITHER)) chance += chance * surRisque / 100.0D;
            if (RANDOM.nextDouble() * 100.0D >= chance) return;

            p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER,
                    dureeSec * 20, niveauWither - 1), true);
            DisplayService.actionBar(p, t(INFECTED, p));
        }

        private boolean hasNeighbour(Player player) {
            double range = distanceMin * (double) distanceMin;
            for (UHCPlayer other : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player target = other.getPlayer();
                if (target == null || target.equals(player)) continue;
                if (!sameWorld(target.getLocation(), player.getLocation())) continue;
                if (target.getLocation().distanceSquared(player.getLocation()) <= range) return true;
            }
            return false;
        }
    }

    public static class PeriodOfResistanceScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.periodofresistance.desc",
                "§7Toutes les §e%minutes% §7minutes, un type de dégâts est tiré au sort et devient inoffensif.");

        private static final DynamicLang ROTATED = DynamicLang.of("scenario.periodofresistance.rotated",
                "§6Période de résistance : §f%type% §6ne fait plus aucun dégât.");

        private static final Map<DamageCause, DynamicLang> CAUSE_LABELS = Map.ofEntries(
                Map.entry(DamageCause.FALL, DynamicLang.of("scenario.periodofresistance.cause.fall", "la chute")),
                Map.entry(DamageCause.FIRE, DynamicLang.of("scenario.periodofresistance.cause.fire", "le feu")),
                Map.entry(DamageCause.FIRE_TICK, DynamicLang.of("scenario.periodofresistance.cause.burning", "les brûlures")),
                Map.entry(DamageCause.LAVA, DynamicLang.of("scenario.periodofresistance.cause.lava", "la lave")),
                Map.entry(DamageCause.DROWNING, DynamicLang.of("scenario.periodofresistance.cause.drowning", "la noyade")),
                Map.entry(DamageCause.SUFFOCATION, DynamicLang.of("scenario.periodofresistance.cause.suffocation", "l'étouffement")),
                Map.entry(DamageCause.ENTITY_EXPLOSION, DynamicLang.of("scenario.periodofresistance.cause.explosion", "les explosions")),
                Map.entry(DamageCause.PROJECTILE, DynamicLang.of("scenario.periodofresistance.cause.projectile", "les projectiles")),
                Map.entry(DamageCause.MAGIC, DynamicLang.of("scenario.periodofresistance.cause.magic", "la magie")),
                Map.entry(DamageCause.POISON, DynamicLang.of("scenario.periodofresistance.cause.poison", "le poison")),
                Map.entry(DamageCause.WITHER, DynamicLang.of("scenario.periodofresistance.cause.wither", "le Wither")),
                Map.entry(DamageCause.STARVATION, DynamicLang.of("scenario.periodofresistance.cause.starvation", "la faim")),
                Map.entry(DamageCause.CONTACT, DynamicLang.of("scenario.periodofresistance.cause.contact", "les cactus")),
                Map.entry(DamageCause.LIGHTNING, DynamicLang.of("scenario.periodofresistance.cause.lightning", "la foudre"))
        );

        private static final List<DamageCause> CAUSE_POOL = List.copyOf(CAUSE_LABELS.keySet());

        @Var(name = "Intervalle", desc = "Temps entre deux tirages du type de dégâts inoffensif.", type = VariableType.TIME, min = 1)
        private int intervalSec = 600;

        private DamageCause immune;

        private BukkitRunnable rotationTask;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Period of Resistance"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%minutes%", Math.max(1, intervalSec / 60)));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.POTION); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            rotate();
            if (rotationTask != null) rotationTask.cancel();
            rotationTask = new BukkitRunnable() {
                @Override
                public void run() {
                    rotate();
                }
            };
            long period = Math.max(1, intervalSec) * 20L;
            rotationTask.runTaskTimer(Main.get(), period, period);
        }

        @Override
        public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {
            if (!isActive()) return;
            if (immune == null) return;
            if (event.getCause() != immune) return;
            event.setCancelled(true);
        }

        @Override
        public void onStop() {
            if (rotationTask != null) rotationTask.cancel();
            rotationTask = null;
            immune = null;
        }

        private void rotate() {
            DamageCause picked = CAUSE_POOL.get(RANDOM.nextInt(CAUSE_POOL.size()));
            if (picked == immune && CAUSE_POOL.size() > 1) {
                picked = CAUSE_POOL.get((CAUSE_POOL.indexOf(picked) + 1) % CAUSE_POOL.size());
            }
            immune = picked;
            LangManager.get().sendAll(ROTATED, Map.of("%type%", t(CAUSE_LABELS.get(picked))));
        }
    }

    public static class PyrophobiaScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.pyrophobia.desc",
                "§7L'eau et la glace deviennent de la lave, §e%chance%% §7du lapis et de la redstone devient de l'obsidienne, et les mobs sont ignifugés.");

        private static final Set<Material> LIQUEFIED = EnumSet.of(
                Material.WATER,
                Material.STATIONARY_WATER,
                Material.ICE,
                Material.PACKED_ICE
        );

        private static final Set<Material> PETRIFIED = EnumSet.of(
                Material.LAPIS_ORE,
                Material.REDSTONE_ORE,
                Material.GLOWING_REDSTONE_ORE
        );

        private static final int LAVA_ID = Material.STATIONARY_LAVA.getId();

        private static final int OBSIDIAN_ID = Material.OBSIDIAN.getId();

        private static final int CHUNKS_PER_TICK = 2;

        private final Deque<Chunk> pending = new ArrayDeque<>();

        private final Set<String> converted = new HashSet<>();

        private BukkitRunnable conversionTask;

        @Var(name = "Hauteur analysée", desc = "Altitude maximale à laquelle les blocs sont convertis.", type = VariableType.INTEGER, min = 1, max = 256)
        private int hauteurMax = 128;

        @Var(name = "Chance d'obsidienne", desc = "Chance qu'un minerai de lapis ou de redstone devienne de l'obsidienne.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceObsidienne = 50;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Pyrophobia"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%chance%", chanceObsidienne));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.LAVA_BUCKET); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            for (World world : Bukkit.getWorlds()) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    enqueue(chunk);
                }
            }
            if (conversionTask != null) conversionTask.cancel();
            conversionTask = new BukkitRunnable() {
                @Override
                public void run() {
                    for (int i = 0; i < CHUNKS_PER_TICK; i++) {
                        Chunk chunk = pending.poll();
                        if (chunk == null) return;
                        convert(chunk);
                    }
                }
            };
            conversionTask.runTaskTimer(Main.get(), 1L, 1L);
        }

        @EventHandler
        public void onChunkLoad(ChunkLoadEvent event) {
            if (!isActive()) return;
            World arena = Common.get().getArena();
            if (arena == null || !event.getWorld().equals(arena)) return;
            enqueue(event.getChunk());
        }

        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) {
            if (!isRunning()) return;
            Entity entity = event.getEntity();
            if (entity instanceof Player) return;
            if (!(entity instanceof LivingEntity)) return;
            DamageCause cause = event.getCause();
            if (cause != DamageCause.FIRE && cause != DamageCause.FIRE_TICK && cause != DamageCause.LAVA) return;
            event.setCancelled(true);
            entity.setFireTicks(0);
        }

        @Override
        public void onStop() {
            if (conversionTask != null) conversionTask.cancel();
            conversionTask = null;
            pending.clear();
            converted.clear();
        }

        private void enqueue(Chunk chunk) {
            String key = chunk.getWorld().getName() + ':' + chunk.getX() + ':' + chunk.getZ();
            if (!converted.add(key)) return;
            pending.add(chunk);
        }

        private void convert(Chunk chunk) {
            int maxY = Math.min(hauteurMax, chunk.getWorld().getMaxHeight() - 1);
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y <= maxY; y++) {
                        Block block = chunk.getBlock(x, y, z);
                        Material type = block.getType();
                        if (LIQUEFIED.contains(type)) {
                            block.setTypeIdAndData(LAVA_ID, (byte) 0, false);
                        } else if (PETRIFIED.contains(type) && UHCUtils.Rng.chance(chanceObsidienne)) {
                            block.setTypeIdAndData(OBSIDIAN_ID, (byte) 0, false);
                        }
                    }
                }
            }
        }
    }

    public static class RagDollsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.ragdolls.desc",
                "§7Chacun démarre avec un livre §eRecul %recul% §7et un livre §eCoup de feu %feu%§7.");

        private static final DynamicLang RECEIVED = DynamicLang.of("scenario.ragdolls.received",
                "§7Tu reçois un livre §eRecul %recul% §7et un livre §eCoup de feu %feu%§7.");

        @Var(name = "Niveau de Recul", desc = "Niveau du livre Recul distribué au démarrage.", type = VariableType.INTEGER, min = 1)
        private int niveauRecul = 2;

        @Var(name = "Niveau de Coup de feu", desc = "Niveau du livre Coup de feu distribué au démarrage.", type = VariableType.INTEGER, min = 1)
        private int niveauFeu = 2;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Rag Dolls"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%recul%", niveauRecul, "%feu%", niveauFeu));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ENCHANTED_BOOK); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            PlayerUtils.giveOrDrop(player, new ItemCreator(Material.ENCHANTED_BOOK)
                    .addStoredEnchantment(Enchantment.KNOCKBACK, niveauRecul)
                    .getItemstack());
            PlayerUtils.giveOrDrop(player, new ItemCreator(Material.ENCHANTED_BOOK)
                    .addStoredEnchantment(Enchantment.ARROW_FIRE, niveauFeu)
                    .getItemstack());
            LangManager.get().send(RECEIVED, player, Map.of("%recul%", niveauRecul, "%feu%", niveauFeu));
        }
    }

    public static class RedArrowsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.redarrows.desc",
                "§7Une flèche rouge apparaît dans le ciel au-dessus de chaque lieu de mort pendant §e%duree%s§7.");

        private static final DynamicLang MARKED = DynamicLang.of("scenario.redarrows.marked",
                "§cUne flèche rouge marque le dernier lieu de mort.");

        private static final int REFRESH_TICKS = 20;

        private final Map<Location, Long> markers = new HashMap<>();

        private BukkitRunnable drawTask;

        @Var(name = "Altitude du marqueur", desc = "Hauteur à laquelle la flèche apparaît au-dessus du lieu de mort.", type = VariableType.INTEGER, min = 1, max = 256)
        private int altitude = 40;

        @Var(name = "Taille de la flèche", desc = "Longueur en blocs du trait de la flèche.", type = VariableType.INTEGER, min = 1)
        private int taille = 10;

        @Var(name = "Taille de la pointe", desc = "Longueur en blocs des barbes de la pointe de la flèche.", type = VariableType.INTEGER, min = 1)
        private int taillePointe = 2;

        @Var(name = "Durée du marqueur", desc = "Temps pendant lequel la flèche reste visible dans le ciel.", type = VariableType.TIME, min = 1)
        private int dureeSec = 300;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Red Arrows"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%duree%", dureeSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.REDSTONE); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            if (drawTask != null) drawTask.cancel();
            drawTask = new BukkitRunnable() {
                @Override
                public void run() {
                    long now = System.currentTimeMillis();
                    Iterator<Map.Entry<Location, Long>> iterator = markers.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<Location, Long> entry = iterator.next();
                        if (entry.getValue() <= now) {
                            iterator.remove();
                            continue;
                        }
                        draw(entry.getKey());
                    }
                }
            };
            drawTask.runTaskTimer(Main.get(), REFRESH_TICKS, REFRESH_TICKS);
        }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            Player victim = event.getEntity();
            if (victim == null || victim.getWorld() == null) return;
            markers.put(victim.getLocation().clone(), System.currentTimeMillis() + dureeSec * 1000L);
            LangManager.get().sendAll(MARKED);
        }

        @Override
        public void onStop() {
            if (drawTask != null) drawTask.cancel();
            drawTask = null;
            markers.clear();
        }

        private void draw(Location base) {
            World world = base.getWorld();
            if (world == null) return;
            double x = base.getX();
            double z = base.getZ();
            double startY = Math.min(world.getMaxHeight() - taille - 1.0D, base.getY() + altitude);
            if (startY <= base.getY()) startY = base.getY() + 1.0D;

            for (int i = 0; i < taille; i++) {
                Particles.redstone(new Location(world, x, startY + i, z), Color.RED);
            }
            for (int i = 1; i <= taillePointe; i++) {
                Particles.redstone(new Location(world, x + i, startY + i, z), Color.RED);
                Particles.redstone(new Location(world, x - i, startY + i, z), Color.RED);
                Particles.redstone(new Location(world, x, startY + i, z + i), Color.RED);
                Particles.redstone(new Location(world, x, startY + i, z - i), Color.RED);
            }
        }
    }

    public static class RewardingKillScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.rewardingkill.desc",
                "§7Un kill rapporte un lot d'autant meilleur que la vie qu'il te reste est haute.");

        private static final DynamicLang REWARD_HIGH = DynamicLang.of("scenario.rewardingkill.reward-high",
                "§bKill propre : §f%amount% diamant(s)§b.");

        private static final DynamicLang REWARD_MID = DynamicLang.of("scenario.rewardingkill.reward-mid",
                "§6Kill disputé : §f%amount% lingot(s) d'or§6.");

        private static final DynamicLang REWARD_LOW = DynamicLang.of("scenario.rewardingkill.reward-low",
                "§7Kill arraché : §f%amount% lingot(s) de fer§7.");

        @Var(name = "Seuil de vie haut", desc = "Part de vie restante à partir de laquelle le tueur touche le meilleur lot.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int seuilHaut = 75;

        @Var(name = "Seuil de vie moyen", desc = "Part de vie restante à partir de laquelle le tueur touche le lot intermédiaire.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int seuilMoyen = 40;

        @Var(name = "Diamants du lot haut", desc = "Nombre de diamants donnés au tueur presque intact.", type = VariableType.INTEGER, min = 0)
        private int diamants = 2;

        @Var(name = "Or du lot moyen", desc = "Nombre de lingots d'or donnés au tueur à mi-vie.", type = VariableType.INTEGER, min = 0)
        private int lingotsOr = 3;

        @Var(name = "Fer du lot bas", desc = "Nombre de lingots de fer donnés au tueur au bord de la mort.", type = VariableType.INTEGER, min = 0)
        private int lingotsFer = 3;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Rewarding Kill"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.DIAMOND); }

        @Override
        public void onKill(UHCPlayer killer, UHCPlayer victim) {
            if (!isActive()) return;
            if (killer == null) return;
            Player player = killer.getPlayer();
            if (player == null) return;

            double maxHealth = player.getMaxHealth();
            double ratio = maxHealth <= 0 ? 0.0D : player.getHealth() / maxHealth * 100.0D;

            if (ratio >= seuilHaut) {
                PlayerUtils.giveOrDrop(player, new ItemStack(Material.DIAMOND, diamants));
                DisplayService.actionBar(player, t(REWARD_HIGH, player, Map.of("%amount%", diamants)));
                return;
            }
            if (ratio >= seuilMoyen) {
                PlayerUtils.giveOrDrop(player, new ItemStack(Material.GOLD_INGOT, lingotsOr));
                DisplayService.actionBar(player, t(REWARD_MID, player, Map.of("%amount%", lingotsOr)));
                return;
            }
            PlayerUtils.giveOrDrop(player, new ItemStack(Material.IRON_INGOT, lingotsFer));
            DisplayService.actionBar(player, t(REWARD_LOW, player, Map.of("%amount%", lingotsFer)));
        }
    }

    public static class RewardingNsScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.rewardingns.desc",
                "§7Après un tir à l'arc réussi à §e%distance% §7blocs ou plus, le premier à écrire §e%mot% §7empoche la récompense.");

        private static final DynamicLang CALL = DynamicLang.of("scenario.rewardingns.call",
                "§e%tireur% §7touche à §e%distance% §7blocs : premier à écrire §e%mot% §7!");

        private static final DynamicLang WON = DynamicLang.of("scenario.rewardingns.won",
                "§e%joueur% §7rafle la récompense du beau tir.");

        @Var(name = "Distance minimale", desc = "Distance minimale du tir à l'arc pour ouvrir la course à la récompense.", type = VariableType.INTEGER, min = 1)
        private int distanceMin = 30;

        @Var(name = "Fenêtre de réponse", desc = "Temps laissé aux joueurs pour écrire le mot après un beau tir.", type = VariableType.TIME, min = 1)
        private int fenetreSec = 10;

        @Var(name = "Mot à écrire", desc = "Mot à écrire dans le chat pour rafler la récompense.", type = VariableType.STRING)
        private String mot = "ns";

        @Var(name = "Pommes dorées", desc = "Nombre de pommes dorées données au gagnant.", type = VariableType.INTEGER, min = 1)
        private int recompense = 1;

        private long fenetreFin;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Rewarding NS's"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%distance%", distanceMin, "%mot%", mot));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BOW); }

        @Override
        public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (!(entity instanceof Player victim)) return;
            if (!(damager instanceof Arrow arrow)) return;
            if (!(arrow.getShooter() instanceof Player shooter)) return;
            if (shooter.equals(victim)) return;
            if (!sameWorld(shooter.getLocation(), victim.getLocation())) return;

            int distance = (int) shooter.getLocation().distance(victim.getLocation());
            if (distance < distanceMin) return;

            fenetreFin = System.currentTimeMillis() + fenetreSec * 1000L;
            LangManager.get().sendAll(CALL, Map.of(
                    "%tireur%", shooter.getName(),
                    "%distance%", distance,
                    "%mot%", mot));
        }

        @EventHandler(ignoreCancelled = true)
        public void onChat(AsyncPlayerChatEvent event) {
            if (!isRunning()) return;
            if (System.currentTimeMillis() > fenetreFin) return;
            if (!event.getMessage().trim().equalsIgnoreCase(mot)) return;
            Player player = event.getPlayer();
            Bukkit.getScheduler().runTask(Main.get(), () -> award(player));
        }

        @Override
        public void onStop() {
            fenetreFin = 0L;
        }

        private void award(Player player) {
            if (!isActive()) return;
            if (System.currentTimeMillis() > fenetreFin) return;
            UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
            if (uhcPlayer == null || !uhcPlayer.isPlaying()) return;
            fenetreFin = 0L;
            PlayerUtils.giveOrDrop(player, new ItemStack(Material.GOLDEN_APPLE, recompense));
            LangManager.get().sendAll(WON, Map.of("%joueur%", player.getName()));
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new OneshotScenario(),
                new OutbreakScenario(),
                new PeriodOfResistanceScenario(),
                new PyrophobiaScenario(),
                new RagDollsScenario(),
                new RedArrowsScenario(),
                new RewardingKillScenario(),
                new RewardingNsScenario()
        );
    }
}
