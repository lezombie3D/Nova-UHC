package net.novaproject.scenarioplus.monde;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.scenarioplus.util.RisingFluid;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class FloodScenarios {

    private FloodScenarios() {
    }

    public static class DungeoneeringScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.dungeoneering.desc",
                "§7Les donjons pullulent (§e%donjons% §7tentatives par chunk), le charbon se raréfie et la redstone abonde.");

        @Var(name = "Fréquence des donjons", desc = "Nombre de tentatives de génération de donjon par chunk.", type = VariableType.INTEGER, min = 1, max = 200)
        private int frequenceDonjons = 32;

        @Var(name = "Filons de charbon", desc = "Nombre de filons de charbon générés par chunk.", type = VariableType.INTEGER, min = 0, max = 60)
        private int filonsCharbon = 5;

        @Var(name = "Filons de redstone", desc = "Nombre de filons de redstone générés par chunk.", type = VariableType.INTEGER, min = 0, max = 60)
        private int filonsRedstone = 24;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Dungeoneering"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%donjons%", frequenceDonjons));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.MOB_SPAWNER); }

        @Override
        public Map<String, String> getGeneratorOverrides() {
            if (!isActive()) return null;
            return Map.of(
                    "useDungeons", "true",
                    "dungeonChance", String.valueOf(frequenceDonjons),
                    "coalCount", String.valueOf(filonsCharbon),
                    "redstoneCount", String.valueOf(filonsRedstone));
        }
    }

    public static class FissuresScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.fissures.desc",
                "§7D'immenses failles de lave larges de §e%largeur% §7blocs déchirent la carte tous les §e%espacement% §7blocs.");

        @Var(name = "Espacement des failles", desc = "Distance en blocs entre deux failles sur un même axe.", type = VariableType.INTEGER, min = 16, max = 2000)
        private int espacement = 200;

        @Var(name = "Largeur d'une faille", desc = "Largeur en blocs d'une faille.", type = VariableType.INTEGER, min = 1, max = 64)
        private int largeur = 5;

        @Var(name = "Fond de la faille", desc = "Altitude du fond de la faille, où stagne la lave.", type = VariableType.INTEGER, min = 1, max = 255)
        private int fondY = 12;

        @Var(name = "Épaisseur de lave", desc = "Nombre de couches de lave déposées au fond de la faille.", type = VariableType.INTEGER, min = 0, max = 64)
        private int epaisseurLave = 4;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Fissures"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%largeur%", largeur, "%espacement%", espacement));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.MAGMA_CREAM); }

        @Override
        public BlockPopulator getPopulator(World world) {
            if (!isActive()) return null;
            if (!world.getName().equals(Common.get().getArenaName())) return null;

            int pas = espacement;
            int bande = largeur;
            int fond = fondY;
            int lave = epaisseurLave;

            return new BlockPopulator() {
                @Override
                public void populate(World w, Random random, Chunk chunk) {
                    int baseX = chunk.getX() << 4;
                    int baseZ = chunk.getZ() << 4;
                    int laveId = Material.STATIONARY_LAVA.getId();
                    int basLave = Math.max(1, fond - lave + 1);

                    for (int dx = 0; dx < 16; dx++) {
                        for (int dz = 0; dz < 16; dz++) {
                            boolean surAxeX = Math.floorMod(baseX + dx, pas) < bande;
                            boolean surAxeZ = Math.floorMod(baseZ + dz, pas) < bande;
                            if (!surAxeX && !surAxeZ) continue;

                            for (int y = w.getHighestBlockYAt(baseX + dx, baseZ + dz); y > fond; y--) {
                                chunk.getBlock(dx, y, dz).setTypeIdAndData(0, (byte) 0, false);
                            }
                            for (int y = fond; y >= basLave; y--) {
                                chunk.getBlock(dx, y, dz).setTypeIdAndData(laveId, (byte) 0, false);
                            }
                        }
                    }
                }
            };
        }
    }

    public static class FlatWorldScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.flatworld.desc",
                "§7L'arène est un superflat : aucun relief, mais aussi §caucun minerai, aucun arbre,"
                        + " §caucune grotte et aucune eau§7. Le boost de minerai ne s'applique pas.");

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Flat World"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GRASS); }

        @Override
        public WorldType getWorldType() {
            if (!isActive()) return null;
            return WorldType.FLAT;
        }
    }

    public static class FlightScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.flight.desc",
                "§7Tout le monde peut voler tant que le PvP n'est pas activé.");

        private static final DynamicLang FIN = DynamicLang.of("scenario.flight.fin",
                "§7Le PvP est actif : le vol vient d'être coupé.");

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Flight"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.FEATHER); }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            if (player.getGameMode() == GameMode.CREATIVE) return;
            if (UHCManager.get().getTimer() >= UHCManager.get().getPvpTimer()) return;
            if (!player.getAllowFlight()) player.setAllowFlight(true);
        }

        @Override
        public void onPvP() {
            if (!isActive()) return;
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player player = uhcPlayer.getPlayer();
                if (player == null || player.getGameMode() == GameMode.CREATIVE) continue;
                player.setFlying(false);
                player.setAllowFlight(false);
            }
            LangManager.get().sendAll(FIN);
        }
    }

    public static class FloodedScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.flooded.desc",
                "§7Le niveau de la mer monte à §eY=%niveau% §7: le monde est noyé. Respiration aquatique permanente et livres de nage au départ.");

        @Var(name = "Niveau de la mer", desc = "Altitude Y jusqu'à laquelle le monde est rempli d'eau.", type = VariableType.INTEGER, min = 1, max = 255)
        private int niveauMer = 70;

        @Var(name = "Niveau de Pieds Légers", desc = "Niveau de l'enchantement Pieds Légers du livre offert au départ.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauNage = 3;

        @Var(name = "Niveau d'Affinité Aquatique", desc = "Niveau de l'enchantement Affinité Aquatique du livre offert au départ.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauAffinite = 1;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Flooded"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%niveau%", niveauMer));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.WATER_BUCKET); }

        @Override
        public Map<String, String> getGeneratorOverrides() {
            if (!isActive()) return null;
            return Map.of("seaLevel", String.valueOf(niveauMer));
        }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            player.getInventory().addItem(
                    new ItemCreator(Material.ENCHANTED_BOOK)
                            .addStoredEnchantment(Enchantment.DEPTH_STRIDER, niveauNage).getItemstack(),
                    new ItemCreator(Material.ENCHANTED_BOOK)
                            .addStoredEnchantment(Enchantment.WATER_WORKER, niveauAffinite).getItemstack());
        }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            UHCUtils.applyInfiniteEffects(
                    new PotionEffect[]{new PotionEffect(PotionEffectType.WATER_BREATHING, 80, 0)}, player);
        }
    }

    public static class FloorIsLavaScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.floorislava.desc",
                "§7La lave monte d'un bloc toutes les §e%interval% §7secondes à partir de §eY=%depart%§7,"
                        + " §7dans un rayon de §e%rayon% §7blocs autour de chaque joueur.");

        private static final DynamicLang DEBUT = DynamicLang.of("scenario.floorislava.debut",
                "§cLe sol devient de la lave : elle monte d'un bloc toutes les §e%interval% §csecondes.");

        @Var(name = "Altitude de départ", desc = "Altitude Y à laquelle la lave commence à monter.", type = VariableType.INTEGER, min = 1, max = 255)
        private int niveauDepart = 1;

        @Var(name = "Intervalle de montée", desc = "Secondes entre deux montées d'un bloc du niveau de lave.", type = VariableType.TIME, min = 1)
        private int intervalSec = 30;

        @Var(name = "Rayon d'inondation", desc = "Rayon en blocs, autour de chaque joueur, où la couche de lave est posée.", type = VariableType.INTEGER, min = 8, max = 128)
        private int rayon = 48;

        @Var(name = "Chunks par passe", desc = "Nombre de chunks traités à chaque seconde.", type = VariableType.INTEGER, min = 1)
        private int chunksParPasse = 8;

        private final RisingFluid lave = new RisingFluid(Material.STATIONARY_LAVA);

        private BukkitRunnable task;

        private int compteur;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Floor is Lava"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%interval%", intervalSec, "%depart%", niveauDepart, "%rayon%", rayon));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.LAVA_BUCKET); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            lave.demarrer(niveauDepart - 1);
            compteur = 0;
            if (task != null) task.cancel();
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isActive()) { cancel(); return; }
                    World arena = Common.get().getArena();
                    if (arena == null) return;
                    compteur++;
                    if (compteur >= Math.max(1, intervalSec)) {
                        compteur = 0;
                        if (lave.monter(1, arena.getMaxHeight() - 1)) {
                            lave.empilerAutourDesJoueurs(arena, rayon);
                        }
                    }
                    lave.traiter(arena, chunksParPasse);
                }
            };
            task.runTaskTimer(Main.get(), 20L, 20L);
            LangManager.get().sendAll(DEBUT, Map.of("%interval%", intervalSec));
        }

        @Override
        public void onStop() {
            if (task != null) {
                task.cancel();
                task = null;
            }
            lave.vider();
            compteur = 0;
        }
    }

    public static class FrozenInTimeScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.frozenintime.desc",
                "§7L'eau et la lave ne coulent plus, les blocs soumis à la gravité restent en l'air et les feuilles ne se dégradent jamais.");

        private static final Set<Material> FIGES = EnumSet.of(
                Material.SAND,
                Material.GRAVEL,
                Material.ANVIL,
                Material.DRAGON_EGG,
                Material.CACTUS,
                Material.SUGAR_CANE_BLOCK,
                Material.WATER,
                Material.STATIONARY_WATER,
                Material.LAVA,
                Material.STATIONARY_LAVA
        );

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Frozen in Time"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.PACKED_ICE); }

        @EventHandler
        public void onFlow(BlockFromToEvent event) {
            if (!isRunning()) return;
            event.setCancelled(true);
        }

        @EventHandler
        public void onPhysics(BlockPhysicsEvent event) {
            if (!isRunning()) return;
            if (!FIGES.contains(event.getBlock().getType())) return;
            event.setCancelled(true);
        }

        @EventHandler
        public void onLeavesDecay(LeavesDecayEvent event) {
            if (!isRunning()) return;
            event.setCancelled(true);
        }
    }

    public static class IslandsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.islands.desc",
                "§7Des failles de §e%largeur% §7blocs plongent dans le vide tous les §e%espacement% §7blocs sur X et sur Z.");

        @Var(name = "Espacement des failles", desc = "Distance en blocs entre deux failles sur un même axe.", type = VariableType.INTEGER, min = 16, max = 2000)
        private int espacement = 100;

        @Var(name = "Largeur d'une faille", desc = "Largeur en blocs d'une faille.", type = VariableType.INTEGER, min = 1, max = 64)
        private int largeur = 10;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Islands"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%largeur%", largeur, "%espacement%", espacement));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SAND); }

        @Override
        public BlockPopulator getPopulator(World world) {
            if (!isActive()) return null;
            if (!world.getName().equals(Common.get().getArenaName())) return null;

            int pas = espacement;
            int bande = largeur;

            return new BlockPopulator() {
                @Override
                public void populate(World w, Random random, Chunk chunk) {
                    int baseX = chunk.getX() << 4;
                    int baseZ = chunk.getZ() << 4;

                    for (int dx = 0; dx < 16; dx++) {
                        for (int dz = 0; dz < 16; dz++) {
                            boolean surAxeX = Math.floorMod(baseX + dx, pas) < bande;
                            boolean surAxeZ = Math.floorMod(baseZ + dz, pas) < bande;
                            if (!surAxeX && !surAxeZ) continue;

                            for (int y = w.getHighestBlockYAt(baseX + dx, baseZ + dz); y >= 0; y--) {
                                chunk.getBlock(dx, y, dz).setTypeIdAndData(0, (byte) 0, false);
                            }
                        }
                    }
                }
            };
        }
    }

    public static class KillShrinkScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.killshrink.desc",
                "§7Chaque mort réduit la bordure de §e%blocs% §7blocs, jusqu'à un plancher de §e%min% §7blocs.");

        private static final DynamicLang REDUCTION = DynamicLang.of("scenario.killshrink.reduction",
                "§7Une mort de plus : la bordure se referme à §e%taille% §7blocs.");

        @Var(name = "Réduction par mort", desc = "Nombre de blocs retirés au diamètre de la bordure à chaque mort.", type = VariableType.INTEGER, min = 1, max = 2000)
        private int reductionBlocs = 25;

        @Var(name = "Durée de la réduction", desc = "Secondes que met la bordure à atteindre sa nouvelle taille.", type = VariableType.TIME, min = 1)
        private int dureeSec = 10;

        @Var(name = "Taille minimale", desc = "Diamètre en dessous duquel la bordure ne se réduit plus.", type = VariableType.INTEGER, min = 10, max = 10000)
        private int tailleMin = 100;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Kill Shrink"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%blocs%", reductionBlocs, "%min%", tailleMin));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BARRIER); }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            World arena = Common.get().getArena();
            if (arena == null) return;

            WorldBorder border = arena.getWorldBorder();
            double cible = Math.max(tailleMin, border.getSize() - reductionBlocs);
            if (cible >= border.getSize()) return;

            border.setSize(cible, dureeSec);
            LangManager.get().sendAll(REDUCTION, Map.of("%taille%", (int) cible));
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new DungeoneeringScenario(),
                new FissuresScenario(),
                new FlatWorldScenario(),
                new FlightScenario(),
                new FloodedScenario(),
                new FloorIsLavaScenario(),
                new FrozenInTimeScenario(),
                new IslandsScenario(),
                new KillShrinkScenario()
        );
    }
}
