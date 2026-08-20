package net.novaproject.scenarioplus.timers;

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
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public final class TimerScenarios {

    private static final Random RANDOM = new Random();

    private static List<Scenario> swappable(Scenario self) {
        List<Scenario> result = new ArrayList<>();
        for (Scenario scenario : ScenarioManager.get().getScenarios()) {
            if (scenario == self) continue;
            if (scenario.isSpecial()) continue;
            if (scenario instanceof ScenarioStackup || scenario instanceof ScenarioSwitcher) continue;
            if (scenario.isActive()) continue;
            result.add(scenario);
        }
        return result;
    }

    public static class CreativeRush extends Scenario {

        @Override
        public Family getFamily() { return Family.TIMERS; }

        @Override public String getName() { return "Creative Rush"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%seconds%", creativeSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.COMMAND); }

        @Var(name = "Durée du créatif", desc = "Secondes de mode créatif accordées à tous au démarrage.", type = VariableType.TIME, min = 1)
        private int creativeSec = 20;

        private BukkitRunnable endTask;

        @Override
        public void onGameStart() {
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player player = uhcPlayer.getPlayer();
                if (player == null) continue;
                player.setGameMode(GameMode.CREATIVE);
                DisplayService.title(player, t(TITLE, player), t(SUBTITLE, player, Map.of("%seconds%", creativeSec)), 60);
            }
            LangManager.get().sendAll(START, Map.of("%seconds%", creativeSec));

            if (endTask != null) endTask.cancel();
            endTask = new BukkitRunnable() {
                @Override
                public void run() {
                    for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                        Player player = uhcPlayer.getPlayer();
                        if (player == null) continue;
                        player.setGameMode(GameMode.SURVIVAL);
                        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.4f);
                    }
                    LangManager.get().sendAll(END);
                }
            };
            endTask.runTaskLater(Main.get(), Math.max(1, creativeSec) * 20L);
        }

        @Override
        public void onCraft(ItemStack result, CraftItemEvent event) {
            if (!isActive() || result == null) return;
            if (result.getType() == Material.GOLDEN_APPLE && result.getDurability() == 1) {
                event.setCancelled(true);
                if (event.getWhoClicked() instanceof Player player) {
                    LangManager.get().send(NOTCH_BANNED, player);
                }
            }
        }

        @Override
        public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
            if (!isActive() || item == null) return;
            if (item.getType() == Material.GOLDEN_APPLE && item.getDurability() == 1) {
                event.setCancelled(true);
                LangManager.get().send(NOTCH_BANNED, player);
            }
        }

        @Override
        public void onStop() {
            if (endTask != null) {
                endTask.cancel();
                endTask = null;
            }
        }

        private static final DynamicLang DESC = DynamicLang.of("scenario.creativerush.desc",
                "§7Tout le monde passe en créatif pendant §e%seconds%s §7au début de la partie, et les pommes de Notch sont bannies.");
        private static final DynamicLang TITLE = DynamicLang.of("scenario.creativerush.title",
                "§c§lCREATIVE RUSH");
        private static final DynamicLang SUBTITLE = DynamicLang.of("scenario.creativerush.subtitle",
                "§7Créatif pendant §e%seconds%s");
        private static final DynamicLang START = DynamicLang.of("scenario.creativerush.start",
                "§a● §7Créatif pour tous pendant §e%seconds% secondes§7 !");
        private static final DynamicLang END = DynamicLang.of("scenario.creativerush.end",
                "§c● §7Le créatif est terminé, retour en survie.");
        private static final DynamicLang NOTCH_BANNED = DynamicLang.of("scenario.creativerush.notch-banned",
                "§c● §7Les pommes de Notch sont bannies.");
    }

    public static class DoomsdayClock extends Scenario {

        @Override
        public Family getFamily() { return Family.TIMERS; }

        @Override public String getName() { return "Doomsday Clock"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%deaths%", deathsBeforeStrike));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.TNT); }

        private static final long NIGHT_TIME = 18000L;

        @Var(name = "Morts avant l'apocalypse", desc = "Nombre de morts qui font sonner l'horloge.", type = VariableType.INTEGER, min = 1)
        private int deathsBeforeStrike = 5;

        @Var(name = "Intervalle des vagues", desc = "Secondes entre deux pluies de calamars explosifs.", type = VariableType.TIME, min = 1)
        private int waveIntervalSec = 30;

        @Var(name = "Calamars par vague", desc = "Nombre de calamars lâchés à chaque vague.", type = VariableType.INTEGER, min = 1)
        private int squidsPerWave = 3;

        @Var(name = "Hauteur de largage", desc = "Blocs au-dessus des joueurs où apparaissent les calamars.", type = VariableType.INTEGER, min = 1)
        private int dropHeight = 30;

        @Var(name = "Dispersion", desc = "Rayon en blocs autour du joueur visé pour le largage.", type = VariableType.INTEGER, min = 0)
        private int dropSpread = 6;

        @Var(name = "Puissance d'explosion", desc = "Puissance de l'explosion du calamar à l'atterrissage.", type = VariableType.DOUBLE, min = 0)
        private double explosionPower = 3.0;

        @Var(name = "Casse les blocs", desc = "Les explosions détruisent-elles le terrain.", type = VariableType.BOOLEAN)
        private boolean breakBlocks = false;

        private int deaths = 0;
        private boolean struck = false;
        private int waveCounter = 0;
        private final List<Entity> falling = new ArrayList<>();
        private BukkitRunnable task;

        @Override
        public void onGameStart() {
            deaths = 0;
            struck = false;
            waveCounter = 0;
        }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive() || struck) return;
            deaths++;
            int remaining = deathsBeforeStrike - deaths;
            if (remaining > 0) {
                LangManager.get().sendAll(TICK, Map.of("%deaths%", remaining));
                return;
            }
            strike();
        }

        private void strike() {
            struck = true;
            World world = Common.get().getArena();
            if (world != null) {
                world.setGameRuleValue("doDaylightCycle", "false");
                world.setTime(NIGHT_TIME);
            }
            LangManager.get().sendAll(STRIKE);
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player player = uhcPlayer.getPlayer();
                if (player == null) continue;
                player.playSound(player.getLocation(), Sound.WITHER_SPAWN, 1.0f, 0.6f);
            }

            if (task != null) task.cancel();
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isActive()) { cancel(); return; }
                    World arena = Common.get().getArena();
                    if (arena != null && arena.getTime() != NIGHT_TIME) arena.setTime(NIGHT_TIME);

                    waveCounter++;
                    if (waveCounter >= Math.max(1, waveIntervalSec) * 4) {
                        waveCounter = 0;
                        dropWave();
                    }
                    detonateLanded();
                }
            };
            task.runTaskTimer(Main.get(), 5L, 5L);
        }

        private void dropWave() {
            List<UHCPlayer> targets = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();
            if (targets.isEmpty()) return;
            for (int i = 0; i < squidsPerWave; i++) {
                Player target = targets.get(RANDOM.nextInt(targets.size())).getPlayer();
                if (target == null) continue;
                Location base = target.getLocation();
                double offsetX = dropSpread == 0 ? 0 : RANDOM.nextInt(dropSpread * 2 + 1) - dropSpread;
                double offsetZ = dropSpread == 0 ? 0 : RANDOM.nextInt(dropSpread * 2 + 1) - dropSpread;
                Location spawn = base.clone().add(offsetX, dropHeight, offsetZ);
                falling.add(spawn.getWorld().spawnEntity(spawn, EntityType.SQUID));
            }
        }

        private void detonateLanded() {
            List<Entity> landed = new ArrayList<>();
            for (Entity squid : falling) {
                if (squid.isDead() || squid.isOnGround()
                        || squid.getLocation().getBlock().getRelative(0, -1, 0).getType() != Material.AIR) {
                    landed.add(squid);
                }
            }
            for (Entity squid : landed) {
                falling.remove(squid);
                Location loc = squid.getLocation();
                squid.remove();
                loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(),
                        (float) explosionPower, false, breakBlocks);
            }
        }

        @Override
        public void onStop() {
            if (task != null) {
                task.cancel();
                task = null;
            }
            for (Entity squid : falling) squid.remove();
            falling.clear();
        }

        private static final DynamicLang DESC = DynamicLang.of("scenario.doomsdayclock.desc",
                "§7L'horloge de l'apocalypse avance à chaque mort. Après §e%deaths% morts§7, la nuit devient éternelle et des calamars explosifs tombent du ciel.");
        private static final DynamicLang TICK = DynamicLang.of("scenario.doomsdayclock.tick",
                "§c● §7L'horloge de l'apocalypse avance : encore §e%deaths% mort(s)§7 avant minuit.");
        private static final DynamicLang STRIKE = DynamicLang.of("scenario.doomsdayclock.strike",
                "§4● §7Minuit a sonné : la nuit est éternelle et le ciel se déchire !");
    }

    public static class EndMeetup extends Scenario {

        @Override
        public Family getFamily() { return Family.TIMERS; }

        @Override public String getName() { return "End Meetup"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%minutes%", meetupSec / 60));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ENDER_PORTAL_FRAME); }

        @Var(name = "Heure du meetup", desc = "Secondes de jeu avant l'envoi de tout le monde dans l'End.", type = VariableType.TIME, min = 1)
        private int meetupSec = 3600;

        @Var(name = "Durée du gel", desc = "Secondes de gel et d'invulnérabilité à l'arrivée.", type = VariableType.TIME, min = 1)
        private int freezeSec = 12;

        @Var(name = "Délai du dragon", desc = "Secondes après l'arrivée avant l'apparition du dragon.", type = VariableType.TIME, min = 1)
        private int dragonDelaySec = 600;

        @Var(name = "Rayon d'arrivée", desc = "Rayon en blocs autour du centre où les joueurs sont répartis.", type = VariableType.INTEGER, min = 1)
        private int spreadRadius = 30;

        @Var(name = "Hauteur du dragon", desc = "Blocs au-dessus du sol où le dragon apparaît.", type = VariableType.INTEGER, min = 1)
        private int dragonHeight = 40;

        private boolean sent = false;
        private boolean frozen = false;
        private BukkitRunnable releaseTask;
        private BukkitRunnable dragonTask;

        @Override
        public void onGameStart() {
            sent = false;
            frozen = false;
        }

        @Override
        public void onSec(Player p) {
            if (!isActive() || sent) return;
            if (UHCManager.get().getTimer() < meetupSec) return;
            sent = true;
            frozen = true;

            World world = Common.get().getEnd() != null ? Common.get().getEnd() : Common.get().getArena();
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player player = uhcPlayer.getPlayer();
                if (player == null) continue;
                player.teleport(arrival(world));
                player.setNoDamageTicks(freezeSec * 20);
                DisplayService.title(player, t(TITLE, player), t(SUBTITLE, player, Map.of("%seconds%", freezeSec)), 80);
            }
            LangManager.get().sendAll(SENT, Map.of("%seconds%", freezeSec));

            if (releaseTask != null) releaseTask.cancel();
            releaseTask = new BukkitRunnable() {
                @Override
                public void run() {
                    frozen = false;
                    LangManager.get().sendAll(RELEASED);
                }
            };
            releaseTask.runTaskLater(Main.get(), Math.max(1, freezeSec) * 20L);

            if (dragonTask != null) dragonTask.cancel();
            dragonTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isActive()) return;
                    World target = Common.get().getEnd() != null ? Common.get().getEnd() : Common.get().getArena();
                    Location loc = new Location(target, 0.5, target.getHighestBlockYAt(0, 0) + dragonHeight, 0.5);
                    target.spawnEntity(loc, EntityType.ENDER_DRAGON);
                    LangManager.get().sendAll(DRAGON);
                }
            };
            dragonTask.runTaskLater(Main.get(), Math.max(1, dragonDelaySec) * 20L);
        }

        private Location arrival(World world) {
            int x = RANDOM.nextInt(spreadRadius * 2 + 1) - spreadRadius;
            int z = RANDOM.nextInt(spreadRadius * 2 + 1) - spreadRadius;
            return new Location(world, x + 0.5, world.getHighestBlockYAt(x, z) + 1, z + 0.5);
        }

        @Override
        public void onMove(Player player, PlayerMoveEvent event) {
            if (!isActive() || !frozen) return;
            Location from = event.getFrom();
            Location to = event.getTo();
            if (to == null) return;
            if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) return;
            Location held = from.clone();
            held.setYaw(to.getYaw());
            held.setPitch(to.getPitch());
            event.setTo(held);
        }

        @Override
        public void onDamage(Player player, EntityDamageEvent event) {
            if (!isActive() || !frozen) return;
            event.setCancelled(true);
        }

        @Override
        public void onStop() {
            frozen = false;
            if (releaseTask != null) {
                releaseTask.cancel();
                releaseTask = null;
            }
            if (dragonTask != null) {
                dragonTask.cancel();
                dragonTask = null;
            }
        }

        private static final DynamicLang DESC = DynamicLang.of("scenario.endmeetup.desc",
                "§7Au meetup (§e%minutes% min§7), tout le monde est gelé et envoyé dans l'End sans PvP, puis le dragon arrive.");
        private static final DynamicLang TITLE = DynamicLang.of("scenario.endmeetup.title",
                "§5§lEND MEETUP");
        private static final DynamicLang SUBTITLE = DynamicLang.of("scenario.endmeetup.subtitle",
                "§7Gelé pendant §e%seconds%s");
        private static final DynamicLang SENT = DynamicLang.of("scenario.endmeetup.sent",
                "§5● §7Meetup dans l'End : tout le monde est gelé pendant §e%seconds% secondes§7.");
        private static final DynamicLang RELEASED = DynamicLang.of("scenario.endmeetup.released",
                "§a● §7Le gel est levé, le PvP reprend !");
        private static final DynamicLang DRAGON = DynamicLang.of("scenario.endmeetup.dragon",
                "§5● §7Le dragon de l'End vient d'apparaître !");
    }

    public static class Homework extends Scenario {

        @Override
        public Family getFamily() { return Family.TIMERS; }

        @Override public String getName() { return "Homework"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%minutes%", intervalSec / 60));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BOOK); }

        private static final Map<Material, DynamicLang> POOL = new LinkedHashMap<>();
        private static final List<Material> POOL_KEYS;

        static {
            pool(Material.WHEAT, "du blé");
            pool(Material.APPLE, "une pomme");
            pool(Material.IRON_INGOT, "un lingot de fer");
            pool(Material.COAL, "du charbon");
            pool(Material.LEATHER, "du cuir");
            pool(Material.FEATHER, "une plume");
            pool(Material.STRING, "de la ficelle");
            pool(Material.BONE, "un os");
            pool(Material.ARROW, "une flèche");
            pool(Material.GOLD_INGOT, "un lingot d'or");
            pool(Material.FLINT, "un silex");
            pool(Material.SEEDS, "des graines de blé");
            pool(Material.ROTTEN_FLESH, "de la chair putréfiée");
            pool(Material.EGG, "un œuf");
            pool(Material.COBBLESTONE, "de la pierre");
            pool(Material.SAND, "du sable");
            pool(Material.GRAVEL, "du gravier");
            pool(Material.CACTUS, "un cactus");
            pool(Material.SUGAR_CANE, "de la canne à sucre");
            pool(Material.MELON, "une tranche de pastèque");
            pool(Material.PUMPKIN, "une citrouille");
            pool(Material.RED_ROSE, "un coquelicot");
            pool(Material.INK_SACK, "de la teinture");
            pool(Material.SPIDER_EYE, "un œil d'araignée");
            pool(Material.SULPHUR, "de la poudre à canon");
            pool(Material.CARROT_ITEM, "une carotte");
            pool(Material.POTATO_ITEM, "une pomme de terre");
            POOL_KEYS = List.copyOf(POOL.keySet());
        }

        private static void pool(Material material, String frenchName) {
            POOL.put(material, DynamicLang.of("scenario.homework.item." + material.name().toLowerCase(), frenchName));
        }

        @Var(name = "Intervalle", desc = "Secondes entre deux devoirs.", type = VariableType.TIME, min = 1)
        private int intervalSec = 600;

        @Var(name = "Absorption gagnée", desc = "Demi-cœurs d'absorption gagnés si le devoir est fait.", type = VariableType.INTEGER, min = 0)
        private int rewardHalfHearts = 2;

        @Var(name = "Dégâts subis", desc = "Dégâts infligés au joueur qui n'a pas l'objet demandé.", type = VariableType.DOUBLE, min = 0)
        private double penaltyDamage = 2.0;

        private Material current;
        private BukkitRunnable task;

        @Override
        public void onGameStart() {
            assign();
            if (task != null) task.cancel();
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isActive()) { cancel(); return; }
                    grade();
                    assign();
                }
            };
            long period = Math.max(1, intervalSec) * 20L;
            task.runTaskTimer(Main.get(), period, period);
        }

        private void assign() {
            current = POOL_KEYS.get(RANDOM.nextInt(POOL_KEYS.size()));
            LangManager.get().sendAll(ASSIGNED, Map.of("%item%", t(POOL.get(current)), "%minutes%", intervalSec / 60));
        }

        private void grade() {
            if (current == null) return;
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player player = uhcPlayer.getPlayer();
                if (player == null) continue;
                if (player.getInventory().contains(current)) {
                    PlayerUtils.setAbsorptionHearts(player, PlayerUtils.getAbsorptionHearts(player) + rewardHalfHearts);
                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.6f);
                    LangManager.get().send(DONE, player, Map.of("%item%", t(POOL.get(current), player)));
                } else {
                    player.damage(penaltyDamage);
                    player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 0.5f);
                    LangManager.get().send(FAILED, player, Map.of("%item%", t(POOL.get(current), player)));
                }
            }
        }

        @Override
        public void onStop() {
            if (task != null) {
                task.cancel();
                task = null;
            }
            current = null;
        }

        private static final DynamicLang DESC = DynamicLang.of("scenario.homework.desc",
                "§7Toutes les §e%minutes% min§7, un objet est imposé : l'avoir sur soi rapporte de l'absorption, ne pas l'avoir coûte un cœur.");
        private static final DynamicLang ASSIGNED = DynamicLang.of("scenario.homework.assigned",
                "§e● §7Nouveau devoir : ayez §e%item%§7 sur vous d'ici §e%minutes% min§7.");
        private static final DynamicLang DONE = DynamicLang.of("scenario.homework.done",
                "§a● §7Devoir rendu (§e%item%§7) : absorption gagnée.");
        private static final DynamicLang FAILED = DynamicLang.of("scenario.homework.failed",
                "§c● §7Devoir non rendu (§e%item%§7) : vous perdez un cœur.");
    }

    public static class ProgressiveSpeedV1 extends Scenario {

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Progressive Speed V1"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%level%", baseLevel, "%max%", maxLevel));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SUGAR); }

        @Var(name = "Niveau de départ", desc = "Niveau de Vitesse accordé au démarrage.", type = VariableType.INTEGER, min = 1)
        private int baseLevel = 1;

        @Var(name = "Gain par kill", desc = "Niveaux de Vitesse gagnés à chaque kill.", type = VariableType.INTEGER, min = 1)
        private int levelPerKill = 1;

        @Var(name = "Niveau maximum", desc = "Plafond du niveau de Vitesse.", type = VariableType.INTEGER, min = 1)
        private int maxLevel = 5;

        private final Map<UUID, Integer> bonus = new HashMap<>();

        @Override
        public void onKill(UHCPlayer killer, UHCPlayer victim) {
            if (!isActive() || killer == null) return;
            UUID uuid = killer.getUniqueId();
            bonus.put(uuid, bonus.getOrDefault(uuid, 0) + levelPerKill);
            Player player = killer.getPlayer();
            if (player == null) return;
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.8f);
            LangManager.get().send(GAIN, player, Map.of("%level%", levelOf(uuid)));
        }

        private int levelOf(UUID uuid) {
            return Math.min(maxLevel, baseLevel + bonus.getOrDefault(uuid, 0));
        }

        @Override
        public void onSec(Player p) {
            if (!isActive()) return;
            UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                    new PotionEffect(PotionEffectType.SPEED, UHCUtils.LOOPED_EFFECT_DURATION_TICKS, levelOf(p.getUniqueId()) - 1)
            }, p);
        }

        @Override
        public void onStop() {
            bonus.clear();
        }

        private static final DynamicLang DESC = DynamicLang.of("scenario.progressivespeed.v1.desc",
                "§7Tout le monde démarre avec Vitesse §e%level%§7 ; chaque kill fait monter votre niveau d'un cran, jusqu'à §e%max%§7.");
        private static final DynamicLang GAIN = DynamicLang.of("scenario.progressivespeed.v1.gain",
                "§b● §7Votre Vitesse passe au niveau §e%level%§7.");
    }

    public static class ProgressiveSpeedV2 extends Scenario {

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Progressive Speed V2"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%level%", baseLevel, "%max%", maxLevel));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.FEATHER); }

        @Var(name = "Niveau de départ", desc = "Niveau de Vitesse accordé à tous au démarrage.", type = VariableType.INTEGER, min = 1)
        private int baseLevel = 1;

        @Var(name = "Gain par mort", desc = "Niveaux de Vitesse gagnés par tous à chaque mort.", type = VariableType.INTEGER, min = 1)
        private int levelPerDeath = 1;

        @Var(name = "Niveau maximum", desc = "Plafond du niveau de Vitesse.", type = VariableType.INTEGER, min = 1)
        private int maxLevel = 5;

        private int bonus = 0;

        @Override
        public void onGameStart() {
            bonus = 0;
        }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            int before = currentLevel();
            bonus += levelPerDeath;
            int after = currentLevel();
            if (after > before) LangManager.get().sendAll(GAIN, Map.of("%level%", after));
        }

        private int currentLevel() {
            return Math.min(maxLevel, baseLevel + bonus);
        }

        @Override
        public void onSec(Player p) {
            if (!isActive()) return;
            UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                    new PotionEffect(PotionEffectType.SPEED, UHCUtils.LOOPED_EFFECT_DURATION_TICKS, currentLevel() - 1)
            }, p);
        }

        private static final DynamicLang DESC = DynamicLang.of("scenario.progressivespeed.v2.desc",
                "§7Tout le monde démarre avec Vitesse §e%level%§7 ; chaque mort fait monter la Vitesse de tous d'un cran, jusqu'à §e%max%§7.");
        private static final DynamicLang GAIN = DynamicLang.of("scenario.progressivespeed.v2.gain",
                "§b● §7La Vitesse de tous passe au niveau §e%level%§7.");
    }

    public static class ScenarioStackup extends Scenario {

        @Override
        public Family getFamily() { return Family.TIMERS; }

        @Override public String getName() { return "Scenario Stackup"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%minutes%", intervalSec / 60));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BOOKSHELF); }

        @Var(name = "Intervalle", desc = "Secondes entre deux activations de scénario.", type = VariableType.TIME, min = 1)
        private int intervalSec = 600;

        @Var(name = "Activer au départ", desc = "Activer un premier scénario dès le début de la partie.", type = VariableType.BOOLEAN)
        private boolean activateAtStart = true;

        private BukkitRunnable task;

        @Override
        public void onGameStart() {
            if (activateAtStart) activateRandom();
            if (task != null) task.cancel();
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isActive()) { cancel(); return; }
                    activateRandom();
                }
            };
            long period = Math.max(1, intervalSec) * 20L;
            task.runTaskTimer(Main.get(), period, period);
        }

        private void activateRandom() {
            List<Scenario> pool = swappable(this);
            if (pool.isEmpty()) {
                LangManager.get().sendAll(EMPTY);
                return;
            }
            Scenario picked = pool.get(RANDOM.nextInt(pool.size()));
            picked.setActive(true);
            picked.onGameStart();
            LangManager.get().sendAll(ADDED, Map.of("%scenario%", picked.getName()));
        }

        @Override
        public void onStop() {
            if (task != null) {
                task.cancel();
                task = null;
            }
        }

        private static final DynamicLang DESC = DynamicLang.of("scenario.scenariostackup.desc",
                "§7Un scénario supplémentaire s'active toutes les §e%minutes% min§7, et aucun ne s'éteint.");
        private static final DynamicLang ADDED = DynamicLang.of("scenario.scenariostackup.added",
                "§a● §7Nouveau scénario actif : §e%scenario%§7 !");
        private static final DynamicLang EMPTY = DynamicLang.of("scenario.scenariostackup.empty",
                "§7Plus aucun scénario à activer.");
    }

    public static class ScenarioSwitcher extends Scenario {

        @Override
        public Family getFamily() { return Family.TIMERS; }

        @Override public String getName() { return "Scenario Switcher"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%minutes%", intervalSec / 60));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.LEVER); }

        @Var(name = "Intervalle", desc = "Secondes entre deux changements de scénario.", type = VariableType.TIME, min = 1)
        private int intervalSec = 600;

        @Var(name = "Activer au départ", desc = "Activer un premier scénario dès le début de la partie.", type = VariableType.BOOLEAN)
        private boolean activateAtStart = true;

        private Scenario current;
        private BukkitRunnable task;

        @Override
        public void onGameStart() {
            current = null;
            if (activateAtStart) switchScenario();
            if (task != null) task.cancel();
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isActive()) { cancel(); return; }
                    switchScenario();
                }
            };
            long period = Math.max(1, intervalSec) * 20L;
            task.runTaskTimer(Main.get(), period, period);
        }

        private void switchScenario() {
            List<Scenario> pool = swappable(this);
            if (pool.isEmpty()) {
                LangManager.get().sendAll(EMPTY);
                return;
            }
            Scenario picked = pool.get(RANDOM.nextInt(pool.size()));
            if (current != null) {
                current.setActive(false);
                current.onStop();
                LangManager.get().sendAll(REMOVED, Map.of("%scenario%", current.getName()));
            }
            current = picked;
            picked.setActive(true);
            picked.onGameStart();
            LangManager.get().sendAll(ADDED, Map.of("%scenario%", picked.getName()));
        }

        @Override
        public void onStop() {
            if (task != null) {
                task.cancel();
                task = null;
            }
            current = null;
        }

        private static final DynamicLang DESC = DynamicLang.of("scenario.scenarioswitcher.desc",
                "§7Toutes les §e%minutes% min§7, un nouveau scénario s'active et le précédent s'éteint.");
        private static final DynamicLang ADDED = DynamicLang.of("scenario.scenarioswitcher.added",
                "§a● §7Scénario en cours : §e%scenario%§7 !");
        private static final DynamicLang REMOVED = DynamicLang.of("scenario.scenarioswitcher.removed",
                "§c● §7Le scénario §e%scenario%§7 est désactivé.");
        private static final DynamicLang EMPTY = DynamicLang.of("scenario.scenarioswitcher.empty",
                "§7Plus aucun scénario à activer.");
    }

    public static class SuddenDeath extends Scenario {

        @Override
        public Family getFamily() { return Family.TIMERS; }

        @Override public String getName() { return "Sudden Death"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%minutes%", meetupSec / 60, "%size%", areaSize));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SKULL_ITEM); }

        @Var(name = "Heure du meetup", desc = "Secondes de jeu avant le regroupement final.", type = VariableType.TIME, min = 1)
        private int meetupSec = 3600;

        @Var(name = "Taille de la zone", desc = "Côté en blocs de la zone finale centrée sur 0,0.", type = VariableType.INTEGER, min = 1)
        private int areaSize = 300;

        @Var(name = "Invulnérabilité", desc = "Secondes d'invulnérabilité à l'arrivée dans la zone.", type = VariableType.TIME, min = 0)
        private int invulnerabilitySec = 5;

        private boolean done = false;

        @Override
        public void onGameStart() {
            done = false;
        }

        @Override
        public void onSec(Player p) {
            if (!isActive() || done) return;
            if (UHCManager.get().getTimer() < meetupSec) return;
            done = true;

            World world = Common.get().getArena();
            int half = Math.max(1, areaSize / 2);
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player player = uhcPlayer.getPlayer();
                if (player == null) continue;
                int x = RANDOM.nextInt(half * 2 + 1) - half;
                int z = RANDOM.nextInt(half * 2 + 1) - half;
                player.teleport(new Location(world, x + 0.5, world.getHighestBlockYAt(x, z) + 1, z + 0.5));
                player.setNoDamageTicks(invulnerabilitySec * 20);
                DisplayService.title(player, t(TITLE, player), t(SUBTITLE, player, Map.of("%size%", areaSize)), 80);
                player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0f, 1.0f);
            }
            LangManager.get().sendAll(BROADCAST, Map.of("%size%", areaSize));
        }

        private static final DynamicLang DESC = DynamicLang.of("scenario.suddendeath.desc",
                "§7À §e%minutes% min§7, tout le monde est téléporté dans une zone de §e%size%x%size%§7 au centre pour le combat final.");
        private static final DynamicLang TITLE = DynamicLang.of("scenario.suddendeath.title",
                "§4§lSUDDEN DEATH");
        private static final DynamicLang SUBTITLE = DynamicLang.of("scenario.suddendeath.subtitle",
                "§7Zone finale de §e%size% §7blocs");
        private static final DynamicLang BROADCAST = DynamicLang.of("scenario.suddendeath.broadcast",
                "§4● §7Combat final : tout le monde est réuni dans une zone de §e%size% §7blocs au centre !");
    }

    public static class Take5 extends Scenario implements Listener {

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Take5"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%count%", maxItems));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ENDER_CHEST); }

        @Var(name = "Objets récupérables", desc = "Nombre d'objets qu'un joueur peut prendre dans un coffre de mort.", type = VariableType.INTEGER, min = 1)
        private int maxItems = 5;

        private final Map<Location, Map<UUID, Integer>> withdrawals = new HashMap<>();

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            Player victim = uhcPlayer.getPlayer();
            if (victim == null) return;
            List<ItemStack> loot = new ArrayList<>(event.getDrops());
            if (loot.isEmpty()) return;
            event.getDrops().clear();

            Block block = victim.getLocation().getBlock();
            block.setType(Material.CHEST);
            if (!(block.getState() instanceof Chest chest)) {
                for (ItemStack drop : loot) block.getWorld().dropItemNaturally(block.getLocation(), drop);
                return;
            }
            for (ItemStack drop : loot) {
                if (drop == null) continue;
                for (ItemStack overflow : chest.getBlockInventory().addItem(drop).values()) {
                    block.getWorld().dropItemNaturally(block.getLocation(), overflow);
                }
            }
            withdrawals.put(block.getLocation(), new HashMap<>());
            LangManager.get().sendAll(SPAWNED, Map.of("%player%", victim.getName(), "%count%", maxItems));
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!isRunning()) return;
            Inventory inventory = event.getInventory();
            if (!(inventory.getHolder() instanceof Chest chest)) return;
            Map<UUID, Integer> taken = withdrawals.get(chest.getLocation());
            if (taken == null) return;
            if (!(event.getWhoClicked() instanceof Player player)) return;

            int raw = event.getRawSlot();
            if (raw < 0 || raw >= inventory.getSize()) return;
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            UUID uuid = player.getUniqueId();
            int count = taken.getOrDefault(uuid, 0);
            if (count >= maxItems) {
                event.setCancelled(true);
                LangManager.get().send(LIMIT, player, Map.of("%count%", maxItems));
                return;
            }
            taken.put(uuid, count + 1);
            LangManager.get().send(REMAINING, player, Map.of("%count%", maxItems - count - 1));
        }

        @EventHandler
        public void onInventoryDrag(InventoryDragEvent event) {
            if (!isRunning()) return;
            Inventory inventory = event.getInventory();
            if (!(inventory.getHolder() instanceof Chest chest)) return;
            if (!withdrawals.containsKey(chest.getLocation())) return;
            for (int slot : event.getRawSlots()) {
                if (slot < inventory.getSize()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        @Override
        public void onStop() {
            withdrawals.clear();
        }

        private static final DynamicLang DESC = DynamicLang.of("scenario.take5.desc",
                "§7Les morts laissent un coffre contenant tout leur stuff, mais chaque joueur ne peut y prendre que §e%count% objets§7.");
        private static final DynamicLang SPAWNED = DynamicLang.of("scenario.take5.spawned",
                "§6● §7Le stuff de §e%player%§7 repose dans un coffre : §e%count% objets§7 maximum par joueur.");
        private static final DynamicLang LIMIT = DynamicLang.of("scenario.take5.limit",
                "§c● §7Vous avez déjà pris vos §e%count% objets§7 dans ce coffre.");
        private static final DynamicLang REMAINING = DynamicLang.of("scenario.take5.remaining",
                "§7Il vous reste §e%count% objet(s)§7 à prendre dans ce coffre.");
    }

    public static class TimedTeams extends Scenario {

        @Override
        public Family getFamily() { return Family.TIMERS; }

        @Override public String getName() { return "Timed Teams"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%min%", minSec / 60, "%max%", maxSec / 60, "%size%", teamSize));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BANNER); }

        @Var(name = "Formation au plus tôt", desc = "Secondes de jeu avant lesquelles les équipes ne peuvent pas se former.", type = VariableType.TIME, min = 1)
        private int minSec = 600;

        @Var(name = "Formation au plus tard", desc = "Secondes de jeu après lesquelles les équipes sont formées.", type = VariableType.TIME, min = 1)
        private int maxSec = 1800;

        @Var(name = "Taille des équipes", desc = "Nombre de joueurs par équipe.", type = VariableType.INTEGER, min = 2)
        private int teamSize = 2;

        private boolean formed = false;
        private int formAt = -1;

        @Override
        public void onGameStart() {
            formed = false;
            int floor = Math.min(minSec, maxSec);
            int ceiling = Math.max(minSec, maxSec);
            formAt = floor + RANDOM.nextInt(ceiling - floor + 1);
        }

        @Override
        public void onSec(Player p) {
            if (!isActive() || formed || formAt < 0) return;
            if (UHCManager.get().getTimer() < formAt) return;
            formed = true;
            form();
        }

        private void form() {
            UHCTeamManager manager = UHCTeamManager.get();
            manager.clearAssignments();
            manager.deleteTeams();

            List<UHCPlayer> players = new ArrayList<>(UHCPlayerManager.get().getPlayingOnlineUHCPlayers());
            Collections.shuffle(players, RANDOM);
            List<UHCTeam> teams = manager.getTeams();

            int index = 0;
            while (index < players.size()) {
                int before = teams.size();
                manager.createTeam(teamSize);
                if (teams.size() == before) break;
                UHCTeam team = teams.get(teams.size() - 1);
                for (int i = 0; i < teamSize && index < players.size(); i++, index++) {
                    UHCPlayer member = players.get(index);
                    member.forceSetTeam(Optional.of(team));
                    Player player = member.getPlayer();
                    if (player == null) continue;
                    DisplayService.title(player, t(TITLE, player), team.prefix() + team.name(), 80);
                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.2f);
                }
            }
            LangManager.get().sendAll(FORMED, Map.of("%size%", teamSize));
        }

        private static final DynamicLang DESC = DynamicLang.of("scenario.timedteams.desc",
                "§7Les équipes de §e%size%§7 se forment à un moment aléatoire entre §e%min% min§7 et §e%max% min§7 de jeu.");
        private static final DynamicLang TITLE = DynamicLang.of("scenario.timedteams.title",
                "§a§lNOUVELLE ÉQUIPE");
        private static final DynamicLang FORMED = DynamicLang.of("scenario.timedteams.formed",
                "§a● §7Les équipes de §e%size%§7 viennent d'être formées au hasard !");
    }

    public static List<Scenario> all() {
        return List.of(
                new CreativeRush(),
                new DoomsdayClock(),
                new EndMeetup(),
                new Homework(),
                new ProgressiveSpeedV1(),
                new ProgressiveSpeedV2(),
                new ScenarioStackup(),
                new ScenarioSwitcher(),
                new SuddenDeath(),
                new Take5(),
                new TimedTeams()
        );
    }
}
