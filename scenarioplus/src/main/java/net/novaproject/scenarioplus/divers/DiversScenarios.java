package net.novaproject.scenarioplus.divers;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.nms.MobDisguiseService;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class DiversScenarios {

    private DiversScenarios() {
    }

    public static class TierProgressionScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.tierprogression.desc",
                "§7Trois mondes liés : ici ni or ni diamant et PvP à §e%minutes% min§7, le Nether débloque l'or, l'End débloque tous les minerais — et le PvP s'ouvre dès qu'un joueur y entre.");

        private static final DynamicLang MONDE_DEUX = DynamicLang.of("scenario.tierprogression.monde-deux",
                "§6Monde 2 §8» §7L'or t'est désormais accessible, et le PvP vient de s'ouvrir.");

        private static final DynamicLang MONDE_TROIS = DynamicLang.of("scenario.tierprogression.monde-trois",
                "§6Monde 3 §8» §7Tous les minerais te sont désormais accessibles, et le PvP vient de s'ouvrir.");

        private static final DynamicLang OR_BLOQUE = DynamicLang.of("scenario.tierprogression.or-bloque",
                "§cL'or reste hors de portée tant que tu n'as pas rejoint le monde 2 par un portail du Nether.");

        private static final DynamicLang DIAMANT_BLOQUE = DynamicLang.of("scenario.tierprogression.diamant-bloque",
                "§cLe diamant reste hors de portée tant que tu n'as pas rejoint le monde 3 par un portail de l'End.");

        private final Map<UUID, Integer> paliers = new HashMap<>();

        @Var(name = "PvP du monde 1", desc = "Temps de jeu au bout duquel le PvP s'active dans le monde de départ.", type = VariableType.TIME, min = 1)
        private int pvpMonde1Sec = 900;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Tier Progression"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%minutes%", pvpMonde1Sec / 60));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ENDER_PORTAL_FRAME); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            UHCManager.get().setPvpTimer(pvpMonde1Sec);
        }

        @Override
        public void onPortal(PlayerPortalEvent event) {
            if (!isActive()) return;
            int palier;
            DynamicLang annonce;
            if (event.getCause() == PlayerPortalEvent.TeleportCause.NETHER_PORTAL) {
                palier = 2;
                annonce = MONDE_DEUX;
            } else if (event.getCause() == PlayerPortalEvent.TeleportCause.END_PORTAL) {
                palier = 3;
                annonce = MONDE_TROIS;
            } else {
                return;
            }

            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            if (paliers.getOrDefault(uuid, 1) >= palier) return;

            paliers.put(uuid, palier);
            LangManager.get().send(annonce, player);

            UHCManager uhc = UHCManager.get();
            if (uhc.getTimer() < uhc.getPvpTimer()) uhc.setPvpTimer(uhc.getTimer() + 1);
        }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            Material type = block.getType();
            if (type != Material.GOLD_ORE && type != Material.DIAMOND_ORE) return;

            int palier = paliers.getOrDefault(player.getUniqueId(), 1);
            if (type == Material.GOLD_ORE && palier < 2) {
                event.setCancelled(true);
                LangManager.get().send(OR_BLOQUE, player);
                return;
            }
            if (type == Material.DIAMOND_ORE && palier < 3) {
                event.setCancelled(true);
                LangManager.get().send(DIAMANT_BLOQUE, player);
            }
        }

        @Override
        public void onStop() {
            paliers.clear();
        }
    }

    public static class EnderDanceScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.enderdance.desc",
                "§7Course au disque : détruis §e%piliers% §7cristal(aux) de soin du dragon puis passe un disque sur un jukebox pour remporter la partie.");

        private static final DynamicLang PILIER = DynamicLang.of("scenario.enderdance.pilier",
                "§5Ender Dance §8» §7Cristal détruit §8(§e%actuel%§7/§e%total%§8)§7.");

        private static final DynamicLang PAS_PRET = DynamicLang.of("scenario.enderdance.pas-pret",
                "§5Ender Dance §8» §7Détruis d'abord §e%total% §7cristal(aux) de soin du dragon.");

        private static final DynamicLang VICTOIRE = DynamicLang.of("scenario.enderdance.victoire",
                "§5Ender Dance §8» §e%joueur% §7a fait tourner son disque : la danse est finie.");

        private final Map<UUID, Integer> piliers = new HashMap<>();

        @Var(name = "Cristaux à détruire", desc = "Nombre de cristaux de soin du dragon à détruire avant de pouvoir gagner.", type = VariableType.INTEGER, min = 1)
        private int piliersRequis = 1;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Ender Dance"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%piliers%", piliersRequis));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.JUKEBOX); }

        @Override
        public void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (!(entity instanceof EnderCrystal)) return;

            Player casseur = null;
            if (dammager instanceof Player direct) casseur = direct;
            else if (dammager instanceof Projectile tir && tir.getShooter() instanceof Player tireur) casseur = tireur;
            if (casseur == null) return;

            int actuel = piliers.getOrDefault(casseur.getUniqueId(), 0) + 1;
            piliers.put(casseur.getUniqueId(), actuel);
            LangManager.get().send(PILIER, casseur, Map.of("%actuel%", Math.min(actuel, piliersRequis), "%total%", piliersRequis));
        }

        @Override
        public void onPlayerInteract(Player player, PlayerInteractEvent event) {
            if (!isActive()) return;
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

            Block block = event.getClickedBlock();
            if (block == null || block.getType() != Material.JUKEBOX) return;

            ItemStack disque = event.getItem();
            if (disque == null || !disque.getType().isRecord()) return;

            if (piliers.getOrDefault(player.getUniqueId(), 0) < piliersRequis) {
                LangManager.get().send(PAS_PRET, player, Map.of("%total%", piliersRequis));
                return;
            }

            LangManager.get().sendAll(VICTOIRE, Map.of("%joueur%", player.getName()));
            for (UHCPlayer autre : new ArrayList<>(UHCPlayerManager.get().getPlayingOnlineUHCPlayers())) {
                Player cible = autre.getPlayer();
                if (cible == null || cible.getUniqueId().equals(player.getUniqueId())) continue;
                cible.setHealth(0.0D);
            }
        }

        @Override
        public void onStop() {
            piliers.clear();
        }
    }

    public static class XenophobiaScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.xenophobia.desc",
                "§7Toute créature et tout joueur, passif ou hostile, prend l'apparence d'un villageois. §8Comportement, dégâts et butin inchangés.");

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Xenophobia"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.EMERALD); }

        @Override
        public void onGameStart() {
            MobDisguiseService.get().disguiseAllMobs(EntityType.VILLAGER);
        }

        @Override
        public void onStart(Player player) {
            MobDisguiseService.get().disguiseAllMobs(EntityType.VILLAGER);
            MobDisguiseService.get().disguise(player, EntityType.VILLAGER);
        }

        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            if (!isRunning()) return;
            UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(event.getPlayer());
            if (uhcPlayer == null || !uhcPlayer.isPlaying()) return;
            MobDisguiseService.get().disguise(event.getPlayer(), EntityType.VILLAGER);
        }

        @Override
        public void onStop() {
            MobDisguiseService.get().undisguiseAllMobs();
            for (Player online : Bukkit.getOnlinePlayers()) {
                MobDisguiseService.get().undisguise(online);
            }
        }
    }

    public static class PopchorusScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.popchorus.desc",
                "§7Manger une nourriture non dorée vous téléporte à quelques blocs de là, comme un fruit du chorus.");

        private static final DynamicLang TELEPORTE = DynamicLang.of("scenario.popchorus.teleporte",
                "§d✦ §7La nourriture vous a §ddéplacé§7.");

        @Var(name = "Portée", desc = "Rayon maximal de la téléportation, en blocs.", type = VariableType.INTEGER, min = 1, max = 64)
        private int portee = 8;

        @Var(name = "Essais", desc = "Nombre de positions testées avant d'abandonner la téléportation.", type = VariableType.INTEGER, min = 1, max = 64)
        private int essais = 16;

        private final Random random = new Random();

        @Override public Family getFamily() { return Family.NOURRITURE; }

        @Override public String getName() { return "Popchorus"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.PORK); }

        @Override
        public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
            if (!isActive()) return;
            if (item == null) return;
            if (item.getType() == Material.GOLDEN_APPLE || item.getType() == Material.GOLDEN_CARROT) return;

            Location destination = positionSure(player.getLocation());
            if (destination == null) return;
            Bukkit.getScheduler().runTask(Main.get(), () -> {
                if (!player.isOnline()) return;
                player.teleport(destination);
                player.getWorld().playSound(destination, Sound.ENDERMAN_TELEPORT, 1.0f, 1.0f);
                LangManager.get().send(TELEPORTE, player);
            });
        }

        private Location positionSure(Location depart) {
            World monde = depart.getWorld();
            double limite = monde.getWorldBorder().getSize() / 2.0;
            Location centre = monde.getWorldBorder().getCenter();
            for (int essai = 0; essai < essais; essai++) {
                int x = depart.getBlockX() + random.nextInt(portee * 2 + 1) - portee;
                int z = depart.getBlockZ() + random.nextInt(portee * 2 + 1) - portee;
                if (Math.abs(x - centre.getBlockX()) > limite - 2 || Math.abs(z - centre.getBlockZ()) > limite - 2) continue;
                int y = monde.getHighestBlockYAt(x, z);
                if (y <= 0) continue;
                Block sol = monde.getBlockAt(x, y - 1, z);
                if (sol.getType() == Material.AIR || sol.isLiquid()) continue;
                if (monde.getBlockAt(x, y, z).getType() != Material.AIR) continue;
                if (monde.getBlockAt(x, y + 1, z).getType() != Material.AIR) continue;
                return new Location(monde, x + 0.5D, y, z + 0.5D, depart.getYaw(), depart.getPitch());
            }
            return null;
        }
    }

    public static class PermaGlowScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.permaglow.desc",
                "§7Chaque mort bascule la surbrillance de tous les joueurs. §8(visible uniquement pour les joueurs sous Lunar Client)");

        private static final DynamicLang BASCULE_ON = DynamicLang.of("scenario.permaglow.on",
                "§b✦ §7Une mort a §ballumé §7la surbrillance de tout le monde.");

        private static final DynamicLang BASCULE_OFF = DynamicLang.of("scenario.permaglow.off",
                "§8✦ §7Une mort a §8éteint §7la surbrillance de tout le monde.");

        private boolean allume = false;

        @Override public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Perma Glow"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GLOWSTONE_DUST); }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            allume = !allume;
            appliquer();
            LangManager.get().sendAll(allume ? BASCULE_ON : BASCULE_OFF);
        }

        private void appliquer() {
            for (UHCPlayer viewerUhc : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player viewer = viewerUhc.getPlayer();
                if (viewer == null) continue;
                for (UHCPlayer cibleUhc : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                    Player cible = cibleUhc.getPlayer();
                    if (cible == null || cible.equals(viewer)) continue;
                    if (allume) DisplayService.glow(viewer, cible, Color.WHITE);
                    else DisplayService.resetGlow(viewer, cible);
                }
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            allume = false;
        }
    }

    public static class InvertedParallelScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.invertedparallel.desc",
                "§7Sous la couche §e%altitude%§7, le monde est fait de terrain du Nether. L'eau ne peut plus être posée.");

        private static final DynamicLang EAU_INTERDITE = DynamicLang.of("scenario.invertedparallel.eau",
                "§c✦ §7L'eau s'évapore instantanément dans ce monde.");

        @Var(name = "Altitude de bascule", desc = "Couche sous laquelle le terrain devient celui du Nether.", type = VariableType.INTEGER, min = 1, max = 200)
        private int altitude = 42;

        @Var(name = "Chance de sable des âmes", desc = "Chance qu'un bloc converti devienne du sable des âmes.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceSoulSand = 6;

        @Var(name = "Chance de pierre lumineuse", desc = "Chance qu'un bloc converti devienne de la pierre lumineuse.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceGlowstone = 2;

        @Override public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Inverted Parallel"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%altitude%", altitude));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.NETHERRACK); }

        @Override
        public void onPlace(Player player, Block block, BlockPlaceEvent event) {
            if (!isActive()) return;
            if (block.getType() != Material.WATER && block.getType() != Material.STATIONARY_WATER) return;
            event.setCancelled(true);
            LangManager.get().send(EAU_INTERDITE, player);
        }

        @Override
        public BlockPopulator getPopulator(World world) {
            if (!isActive()) return null;
            final int plafond = altitude;
            final int soulSand = chanceSoulSand;
            final int glowstone = chanceGlowstone;
            return new BlockPopulator() {
                @Override
                public void populate(World monde, Random alea, Chunk chunk) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            for (int y = 1; y < plafond; y++) {
                                Block bloc = chunk.getBlock(x, y, z);
                                Material type = bloc.getType();
                                if (type == Material.AIR || type == Material.BEDROCK) continue;
                                if (type == Material.WATER || type == Material.STATIONARY_WATER) {
                                    bloc.setTypeIdAndData(Material.STATIONARY_LAVA.getId(), (byte) 0, false);
                                    continue;
                                }
                                if (type != Material.STONE && type != Material.DIRT && type != Material.GRAVEL) continue;
                                int tirage = alea.nextInt(100);
                                if (tirage < glowstone) {
                                    bloc.setTypeIdAndData(Material.GLOWSTONE.getId(), (byte) 0, false);
                                } else if (tirage < glowstone + soulSand) {
                                    bloc.setTypeIdAndData(Material.SOUL_SAND.getId(), (byte) 0, false);
                                } else {
                                    bloc.setTypeIdAndData(Material.NETHERRACK.getId(), (byte) 0, false);
                                }
                            }
                        }
                    }
                }
            };
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new TierProgressionScenario(),
                new EnderDanceScenario(),
                new XenophobiaScenario(),
                new PopchorusScenario(),
                new PermaGlowScenario(),
                new InvertedParallelScenario()
        );
    }
}
