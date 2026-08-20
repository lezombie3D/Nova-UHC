package net.novaproject.scenarioplus.monde;

import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.event.UhcGameEvents.UhcDayEvent;
import net.novaproject.novauhc.event.UhcGameEvents.UhcNightEvent;
import net.novaproject.novauhc.event.UhcGameEvents.UhcScatterStartEvent;
import net.novaproject.novauhc.game.DayNightCycle;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class BiomeScenarios {

    private static final short OEUF_VILLAGEOIS = 120;

    private static final int TENTATIVES = 40;

    private static final int BIOME_TAIGA_ENNEIGEE = 30;

    private static final String[] COULEURS_QUADRANT = {"§a", "§b", "§e", "§d"};


    private static final Set<Material> VEGETATION = EnumSet.of(
            Material.LEAVES,
            Material.LEAVES_2,
            Material.LONG_GRASS,
            Material.DEAD_BUSH,
            Material.RED_ROSE,
            Material.YELLOW_FLOWER,
            Material.DOUBLE_PLANT,
            Material.VINE,
            Material.BROWN_MUSHROOM,
            Material.RED_MUSHROOM,
            Material.HUGE_MUSHROOM_1,
            Material.HUGE_MUSHROOM_2,
            Material.CACTUS,
            Material.SUGAR_CANE_BLOCK,
            Material.PUMPKIN,
            Material.MELON_BLOCK,
            Material.WATER_LILY
    );

    private static final Set<Material> BOIS = EnumSet.of(
            Material.LOG,
            Material.LOG_2,
            Material.LEAVES,
            Material.LEAVES_2
    );

    private static final Set<Material> SOURCES_WACKY = EnumSet.of(
            Material.STONE,
            Material.DIRT,
            Material.GRASS,
            Material.SAND,
            Material.SANDSTONE,
            Material.GRAVEL,
            Material.CLAY,
            Material.HARD_CLAY,
            Material.STAINED_CLAY,
            Material.MYCEL
    );

    private static final List<Material> CIBLES_WACKY = List.of(
            Material.STONE,
            Material.COBBLESTONE,
            Material.MOSSY_COBBLESTONE,
            Material.SMOOTH_BRICK,
            Material.BRICK,
            Material.NETHER_BRICK,
            Material.NETHERRACK,
            Material.SOUL_SAND,
            Material.SAND,
            Material.SANDSTONE,
            Material.GRAVEL,
            Material.CLAY,
            Material.HARD_CLAY,
            Material.OBSIDIAN,
            Material.GLOWSTONE,
            Material.QUARTZ_BLOCK,
            Material.ENDER_STONE,
            Material.PACKED_ICE,
            Material.SNOW_BLOCK,
            Material.BOOKSHELF,
            Material.HAY_BLOCK,
            Material.WOOL,
            Material.PRISMARINE,
            Material.SPONGE
    );

    private BiomeScenarios() {
    }

    private static int quadrant(Location loc) {
        Location centre = loc.getWorld().getWorldBorder().getCenter();
        int est = loc.getX() >= centre.getX() ? 1 : 0;
        int sud = loc.getZ() >= centre.getZ() ? 2 : 0;
        return est + sud;
    }

    private static boolean memeEquipe(UHCPlayer premier, UHCPlayer second) {
        if (premier.getTeam().isEmpty() || second.getTeam().isEmpty()) return false;
        UHCTeam equipe = premier.getTeam().get();
        return equipe.equals(second.getTeam().get());
    }

    private static int sommet(World world, Chunk chunk, int x, int z) {
        int wx = (chunk.getX() << 4) + x;
        int wz = (chunk.getZ() << 4) + z;
        int y = Math.min(world.getHighestBlockYAt(wx, wz), 254);
        while (y > 0 && chunk.getBlock(x, y, z).getType() == Material.AIR) y--;
        return y;
    }

    public static class QuadrantParanoiaScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.quadrantparanoia.desc",
                "§7La map est coupée en 4 quadrants : tu es prévenu dès qu'un adversaire entre dans le tien.");
        private static final DynamicLang ALERTE = DynamicLang.of("scenario.quadrantparanoia.alerte",
                "§c%player% §7vient d'entrer dans ton quadrant.");

        @Var(name = "Délai entre deux alertes", desc = "Temps minimal entre deux alertes provoquées par le même joueur.", type = VariableType.TIME, min = 0, max = 600)
        private int delaiAlerteSec = 20;

        private final Map<UUID, Integer> dernierQuadrant = new HashMap<>();
        private final Map<UUID, Long> derniereAlerte = new HashMap<>();

        @Override public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Quadrant Paranoia"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.COMPASS); }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            if (player == null) return;
            UHCPlayer entrant = UHCPlayerManager.get().getPlayer(player);
            if (entrant == null) return;

            int actuel = quadrant(player.getLocation());
            Integer precedent = dernierQuadrant.put(player.getUniqueId(), actuel);
            if (precedent == null || precedent == actuel) return;

            long maintenant = System.currentTimeMillis();
            Long derniere = derniereAlerte.get(player.getUniqueId());
            if (derniere != null && maintenant - derniere < delaiAlerteSec * 1000L) return;
            derniereAlerte.put(player.getUniqueId(), maintenant);

            for (UHCPlayer autre : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player voisin = autre.getPlayer();
                if (voisin == null || voisin.getUniqueId().equals(player.getUniqueId())) continue;
                if (memeEquipe(entrant, autre)) continue;
                if (quadrant(voisin.getLocation()) != actuel) continue;
                voisin.sendMessage(t(ALERTE, voisin, Map.of("%player%", player.getName())));
            }
        }

        @Override
        public void onStop() {
            dernierQuadrant.clear();
            derniereAlerte.clear();
        }
    }

    public static class QuadrantParanoiaTabScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.quadrantparanoiav2.desc",
                "§7La couleur du pseudo dans le tab indique le quadrant de map où se trouve chaque joueur.");

        private final Set<UUID> renommes = new HashSet<>();

        @Override public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Quadrant Paranoia V2"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.MAP); }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            if (player == null) return;
            String couleur = COULEURS_QUADRANT[quadrant(player.getLocation())];
            String pseudo = player.getName();
            int place = 16 - couleur.length();
            if (pseudo.length() > place) pseudo = pseudo.substring(0, place);
            player.setPlayerListName(couleur + pseudo);
            renommes.add(player.getUniqueId());
        }

        @Override
        public void onStop() {
            for (UUID uuid : renommes) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) player.setPlayerListName(player.getName());
            }
            renommes.clear();
        }
    }

    public static class ScatterlessScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.scatterless.desc",
                "§7Aucun spread : tout le monde démarre au centre de la map, dans un rayon de §e%rayon% §7blocs.");

        @Var(name = "Dispersion au centre", desc = "Rayon, en blocs, dans lequel les joueurs sont posés autour du centre.", type = VariableType.INTEGER, min = 0, max = 100)
        private int rayonDispersion = 5;

        private final Random random = new Random();

        @Override public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Scatterless"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%rayon%", rayonDispersion));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ENDER_PEARL); }

        @Override public boolean isSpecial() { return true; }

        @Override
        public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
            if (!isActive()) return;
            Player player = uhcPlayer.getPlayer();
            if (player == null || location == null) return;
            World world = location.getWorld();
            Location centre = world.getWorldBorder().getCenter();
            int etendue = rayonDispersion * 2 + 1;
            int x = centre.getBlockX() + random.nextInt(etendue) - rayonDispersion;
            int z = centre.getBlockZ() + random.nextInt(etendue) - rayonDispersion;
            int y = world.getHighestBlockYAt(x, z);
            player.teleport(new Location(world, x + 0.5, y, z + 0.5));
        }
    }

    public static class ScenarioManiaScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.scenariomania.desc",
                "§7Au démarrage, §e%count% §7scénarios supplémentaires sont tirés au hasard et activés.");
        private static final DynamicLang ANNONCE = DynamicLang.of("scenario.scenariomania.annonce",
                "§6Scenario Mania §8» §e%count% §7scénarios viennent d'être tirés au hasard.");

        @Var(name = "Scénarios tirés", desc = "Nombre de scénarios activés au hasard au démarrage.", type = VariableType.INTEGER, min = 1, max = 30)
        private int nombreScenarios = 5;

        private final Random random = new Random();

        @Override public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Scenario Mania"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%count%", nombreScenarios));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ENCHANTMENT_TABLE); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            List<Scenario> candidats = new ArrayList<>();
            for (Scenario autre : ScenarioManager.get().getScenarios()) {
                if (autre == this || autre.isActive() || autre.isSpecial()) continue;
                if (autre instanceof ScenarioRole<?>) continue;
                if (autre.overridesVictory() || autre.isWin()) continue;
                if (autre.touchesGeneration()) continue;
                candidats.add(autre);
            }
            Collections.shuffle(candidats, random);

            int tires = Math.min(nombreScenarios, candidats.size());
            for (int index = 0; index < tires; index++) {
                Scenario tire = candidats.get(index);
                tire.enable();
                for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                    Player player = uhcPlayer.getPlayer();
                    if (player != null) tire.onStart(player);
                }
                tire.onGameStart();
            }
            LangManager.get().sendAll(ANNONCE, Map.of("%count%", tires));
        }
    }

    public static class SilentNightScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.silentnight.desc",
                "§7La nuit, le chat, les pseudos au-dessus des têtes et les messages de mort sont coupés.");
        private static final DynamicLang NUIT = DynamicLang.of("scenario.silentnight.nuit",
                "§8La nuit tombe : plus un bruit, plus un nom, plus une annonce.");
        private static final DynamicLang JOUR = DynamicLang.of("scenario.silentnight.jour",
                "§eLe jour se lève : le chat et les pseudos reviennent.");

        @Override public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Silent Night"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.WATCH); }

        @EventHandler
        public void onNuit(UhcNightEvent event) {
            if (!isRunning()) return;
            UHCManager.get().setChatDisabled(true);
            visibilitePseudos(false);
            LangManager.get().sendAll(NUIT);
        }

        @EventHandler
        public void onJour(UhcDayEvent event) {
            if (!isRunning()) return;
            UHCManager.get().setChatDisabled(false);
            visibilitePseudos(true);
            LangManager.get().sendAll(JOUR);
        }

        @Override
        public boolean hascustomDeathMessage() {
            if (!isActive()) return false;
            return !DayNightCycle.get().isDay();
        }

        @Override
        public void sendCustomDeathMessage(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            UHCManager.get().checkVictory();
        }

        private void visibilitePseudos(boolean visible) {
            if (visible && ScenarioManager.get().isScenarioActive("NoNameTag")) return;
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            if (manager == null) return;
            for (Team equipe : manager.getMainScoreboard().getTeams()) {
                equipe.setNameTagVisibility(visible ? NameTagVisibility.ALWAYS : NameTagVisibility.NEVER);
            }
        }

        @Override
        public void onStop() {
            UHCManager.get().setChatDisabled(false);
            visibilitePseudos(true);
        }
    }

    public static class SnowdayScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.snowday.desc",
                "§7La map entière est une taiga enneigée, et casser un arbre donne §e%chance%% §7de canne à sucre.");

        @Var(name = "Chance de canne à sucre", desc = "Chance qu'un bloc d'arbre cassé lâche une canne à sucre.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceCanne = 2;

        private final Random random = new Random();

        @Override public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Snowday"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%chance%", chanceCanne));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SNOW_BLOCK); }

        @Override
        public WorldType getWorldType() {
            if (!isActive()) return null;
            return WorldType.CUSTOMIZED;
        }

        @Override
        public Map<String, String> getGeneratorOverrides() {
            if (!isActive()) return null;
            return Map.of("fixedBiome", String.valueOf(BIOME_TAIGA_ENNEIGEE));
        }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            if (!BOIS.contains(block.getType())) return;
            if (!UHCUtils.Rng.chance(chanceCanne)) return;
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5D, 0.5D, 0.5D),
                    new ItemStack(Material.SUGAR_CANE));
        }
    }

    public static class SplitSpawnScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.splitspawn.desc",
                "§7Les coéquipiers sont spread séparément, en visant §e%distance% §7blocs d'écart."
                        + " §8Sur une carte encombrée, l'écart obtenu peut être plus faible.");

        @Var(name = "Distance minimale", desc = "Distance minimale entre deux points de spawn individuels.", type = VariableType.INTEGER, min = 0, max = 2000)
        private int distanceMinimale = 200;

        @Var(name = "Marge de bordure", desc = "Distance conservée entre le spawn et la bordure du monde.", type = VariableType.INTEGER, min = 0, max = 500)
        private int margeBordure = 30;

        private final List<Location> attribuees = new ArrayList<>();
        private final Random random = new Random();

        @Override public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Split Spawn"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%distance%", distanceMinimale));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.FEATHER); }

        @Override public boolean isSpecial() { return true; }

        @Override
        public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
            if (!isActive()) return;
            Player player = uhcPlayer.getPlayer();
            if (player == null || location == null) return;
            Location individuelle = positionIndividuelle(location.getWorld());
            if (individuelle == null) individuelle = location;
            attribuees.add(individuelle);
            player.teleport(individuelle);
        }

        private Location positionIndividuelle(World world) {
            WorldBorder bordure = world.getWorldBorder();
            double rayon = bordure.getSize() / 2.0 - margeBordure;
            if (rayon < 10.0) rayon = 10.0;
            double centreX = bordure.getCenter().getX();
            double centreZ = bordure.getCenter().getZ();
            double minimumCarre = (double) distanceMinimale * distanceMinimale;

            Location repli = null;
            for (int essai = 0; essai < TENTATIVES; essai++) {
                int x = (int) Math.floor(centreX + (random.nextDouble() * 2 - 1) * rayon);
                int z = (int) Math.floor(centreZ + (random.nextDouble() * 2 - 1) * rayon);
                int y = world.getHighestBlockYAt(x, z) - 1;
                if (y <= 0) continue;
                Block sol = world.getBlockAt(x, y, z);
                if (sol.isLiquid() || sol.getType() == Material.CACTUS || sol.getType() == Material.FIRE) continue;

                Location candidate = new Location(world, x + 0.5, y + 1.0, z + 0.5);
                if (repli == null) repli = candidate;

                boolean tropProche = false;
                for (Location prise : attribuees) {
                    if (prise.getWorld() != world) continue;
                    if (prise.distanceSquared(candidate) < minimumCarre) {
                        tropProche = true;
                        break;
                    }
                }
                if (tropProche) continue;
                return candidate;
            }
            return repli;
        }

        @Override
        public void onStop() {
            attribuees.clear();
        }
    }

    public static class UndergroundParallelScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.undergroundparallel.desc",
                "§7La surface est recopiée sous terre, autour de la couche §e%altitude%§7.");

        @Var(name = "Altitude de la copie", desc = "Couche Y à laquelle se retrouve le niveau de la mer recopié.", type = VariableType.INTEGER, min = 10, max = 120)
        private int altitudeCopie = 42;

        @Var(name = "Épaisseur du sol copié", desc = "Nombre de couches de terrain recopiées sous la surface parallèle.", type = VariableType.INTEGER, min = 1, max = 30)
        private int epaisseurCopie = 6;

        @Var(name = "Hauteur d'air", desc = "Hauteur d'air dégagée au-dessus de la surface parallèle.", type = VariableType.INTEGER, min = 1, max = 40)
        private int hauteurAir = 8;

        @Override public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Underground Parallel"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%altitude%", altitudeCopie));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.STONE); }

        @Override
        public BlockPopulator getPopulator(World world) {
            if (!isActive()) return null;
            final int altitude = altitudeCopie;
            final int epaisseur = epaisseurCopie;
            final int air = hauteurAir;
            return new BlockPopulator() {
                @Override
                public void populate(World monde, Random alea, Chunk chunk) {
                    int mer = monde.getSeaLevel();
                    ChunkSnapshot snapshot = chunk.getChunkSnapshot();
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            int source = sommet(monde, chunk, x, z);
                            if (source <= 0) continue;
                            int cible = altitude + (source - mer);
                            if (cible - epaisseur < 1) continue;
                            if (cible + air >= source) continue;

                            for (int profondeur = 0; profondeur < epaisseur; profondeur++) {
                                int origine = source - profondeur;
                                chunk.getBlock(x, cible - profondeur, z).setTypeIdAndData(
                                        snapshot.getBlockTypeId(x, origine, z),
                                        (byte) snapshot.getBlockData(x, origine, z), false);
                            }
                            for (int hauteur = 1; hauteur <= air; hauteur++) {
                                chunk.getBlock(x, cible + hauteur, z).setTypeIdAndData(0, (byte) 0, false);
                            }
                        }
                    }
                }
            };
        }
    }

    public static class UrbanSprawlScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.urbansprawl.desc",
                "§7Les villages sont partout : tu démarres avec §e%oeufs% §7œufs de villageois, ils ne spawnent plus tout seuls.");

        @Var(name = "Œufs de villageois", desc = "Nombre d'œufs de villageois donnés au démarrage.", type = VariableType.INTEGER, min = 0, max = 64)
        private int oeufs = 20;

        @Var(name = "Biome imposé", desc = "Identifiant du biome imposé à toute la map (1 = plaines).", type = VariableType.INTEGER, min = 0, max = 39)
        private int biomeImpose = 1;

        @Override public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Urban Sprawl"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%oeufs%", oeufs));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.MONSTER_EGG); }

        @Override
        public WorldType getWorldType() {
            if (!isActive()) return null;
            return WorldType.CUSTOMIZED;
        }

        @Override
        public Map<String, String> getGeneratorOverrides() {
            if (!isActive()) return null;
            Map<String, String> reglages = new HashMap<>();
            reglages.put("useVillages", "true");
            reglages.put("fixedBiome", String.valueOf(biomeImpose));
            return reglages;
        }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            if (oeufs <= 0) return;
            player.getInventory().addItem(new ItemStack(Material.MONSTER_EGG, oeufs, OEUF_VILLAGEOIS));
        }

        @EventHandler
        public void onSpawn(CreatureSpawnEvent event) {
            if (!isActive()) return;
            if (event.getLocation().getWorld().equals(Common.get().getLobby())) return;
            if (event.getEntityType() != EntityType.VILLAGER) return;
            SpawnReason raison = event.getSpawnReason();
            if (raison == SpawnReason.SPAWNER_EGG) return;
            if (raison == SpawnReason.CUSTOM) return;
            if (raison == SpawnReason.CURED) return;
            event.setCancelled(true);
        }
    }

    public static class VoidMineshaftScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.voidmineshaft.desc",
                "§7Le monde est vide : seules des mines abandonnées vanilla flottent dans le néant,"
                        + " avec leurs rails, leurs toiles et leurs coffres d'origine.");

        @Var(name = "Densité des mines", desc = "Chance qu'un chunk fasse naître un réseau de mines. Au-delà de 50% les réseaux se croisent en permanence.",
                type = VariableType.PERCENTAGE, min = 1, max = 100)
        private int densiteMines = 60;

        private VoidMineshaftWorld monde;

        @Override public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Void Mineshaft"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.RAILS); }

        @Override
        public ChunkGenerator getChunkGenerator() {
            if (!isActive()) return null;
            monde = new VoidMineshaftWorld(densiteMines / 100.0);
            return monde.generator();
        }

        @Override
        public BlockPopulator getPopulator(World world) {
            if (!isActive() || monde == null) return null;
            return monde.populator();
        }

        @Override
        public Location resolveSpawn(World world) {
            if (!isActive() || monde == null) return null;
            return monde.spawn(world);
        }
    }

    public static class WackyWorldScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.wackyworld.desc",
                "§7Les §e%profondeur% §7premières couches de terrain sont repeintes au hasard, sauf les arbres et les minerais.");

        @Var(name = "Profondeur repeinte", desc = "Couches de terrain repeintes sous la surface de chaque colonne.", type = VariableType.INTEGER, min = 1, max = 16)
        private int profondeurPeau = 3;

        @Override public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Wacky World"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%profondeur%", profondeurPeau));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SLIME_BALL); }

        @Override
        public BlockPopulator getPopulator(World world) {
            if (!isActive()) return null;
            final int peau = profondeurPeau;
            final Map<Material, Material> substitution = new EnumMap<>(Material.class);
            Random tirage = new Random();
            for (Material source : SOURCES_WACKY) {
                substitution.put(source, CIBLES_WACKY.get(tirage.nextInt(CIBLES_WACKY.size())));
            }
            return new BlockPopulator() {
                @Override
                public void populate(World monde, Random alea, Chunk chunk) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            int haut = sommet(monde, chunk, x, z);
                            int bas = Math.max(1, haut - peau + 1);
                            for (int y = haut; y >= bas; y--) {
                                Block bloc = chunk.getBlock(x, y, z);
                                Material remplacant = substitution.get(bloc.getType());
                                if (remplacant == null) continue;
                                bloc.setTypeIdAndData(remplacant.getId(), (byte) 0, false);
                            }
                        }
                    }
                }
            };
        }
    }

    public static class WastelandScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.wasteland.desc",
                "§7La surface est morte : plus une herbe, plus une feuille, rien que de la terre aride et des troncs nus.");

        @Var(name = "Altitude minimale", desc = "Couche Y à partir de laquelle la végétation est supprimée.", type = VariableType.INTEGER, min = 1, max = 128)
        private int altitudeMin = 40;

        @Override public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Wasteland"; }

        @Override public String getDescription(Player player) { return t(DESC, player); }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.DEAD_BUSH); }

        @Override
        public BlockPopulator getPopulator(World world) {
            if (!isActive()) return null;
            final int minimum = altitudeMin;
            return new BlockPopulator() {
                @Override
                public void populate(World monde, Random alea, Chunk chunk) {
                    ChunkSnapshot snapshot = chunk.getChunkSnapshot();
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            int haut = Math.min(snapshot.getHighestBlockYAt(x, z), 255);
                            for (int y = minimum; y <= haut; y++) {
                                Material type = Material.getMaterial(snapshot.getBlockTypeId(x, y, z));
                                if (type == null) continue;
                                if (VEGETATION.contains(type)) {
                                    chunk.getBlock(x, y, z).setTypeIdAndData(0, (byte) 0, false);
                                } else if (type == Material.GRASS || type == Material.MYCEL) {
                                    chunk.getBlock(x, y, z).setTypeIdAndData(Material.DIRT.getId(), (byte) 0, false);
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
                new QuadrantParanoiaScenario(),
                new QuadrantParanoiaTabScenario(),
                new ScatterlessScenario(),
                new ScenarioManiaScenario(),
                new SilentNightScenario(),
                new SnowdayScenario(),
                new SplitSpawnScenario(),
                new UndergroundParallelScenario(),
                new UrbanSprawlScenario(),
                new VoidMineshaftScenario(),
                new WackyWorldScenario(),
                new WastelandScenario()
        );
    }
}
