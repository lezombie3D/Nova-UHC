package net.novaproject.scenarioplus.monde;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.schematic.SchematicStructure;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class TerrainScenarios {

    private static final Random RANDOM = new Random();

    private static final int CHUNK_SIZE = 16;

    private static final List<Biome> BIOMES = List.of(
            Biome.PLAINS,
            Biome.SUNFLOWER_PLAINS,
            Biome.FOREST,
            Biome.FLOWER_FOREST,
            Biome.BIRCH_FOREST,
            Biome.ROOFED_FOREST,
            Biome.TAIGA,
            Biome.MEGA_TAIGA,
            Biome.COLD_TAIGA,
            Biome.SWAMPLAND,
            Biome.JUNGLE,
            Biome.DESERT,
            Biome.SAVANNA,
            Biome.MESA,
            Biome.EXTREME_HILLS,
            Biome.EXTREME_HILLS_PLUS,
            Biome.ICE_PLAINS,
            Biome.MUSHROOM_ISLAND);

    private TerrainScenarios() {
    }

    public static class AltitudeAnvilsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.altitudeanvils.desc",
                "§7Les enclumes ne fonctionnent qu'au-dessus de §eY=%hauteur%§7.");

        private static final DynamicLang TOO_LOW = DynamicLang.of("scenario.altitudeanvils.too-low",
                "§cCette enclume est trop basse : il faut §e%hauteur% §cblocs d'altitude minimum.");

        @Var(name = "Altitude minimale", desc = "Altitude sous laquelle une enclume refuse de s'ouvrir.", type = VariableType.INTEGER, min = 0, max = 255)
        private int hauteurMin = 60;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Altitude Anvils"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%hauteur%", hauteurMin));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ANVIL); }

        @Override
        public void onPlayerInteract(Player player, PlayerInteractEvent event) {
            if (!isActive()) return;
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

            Block block = event.getClickedBlock();
            if (block == null || block.getType() != Material.ANVIL) return;
            if (block.getY() >= hauteurMin) return;

            event.setCancelled(true);
            LangManager.get().send(TOO_LOW, player, Map.of("%hauteur%", hauteurMin));
        }
    }

    public static class AmplifiedTerrainScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.amplifiedterrain.desc",
                "§7Le monde est généré en relief amplifié : montagnes géantes et surplombs partout.");

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Amplified Terrain"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.STONE); }

        @Override
        public WorldType getWorldType() {
            if (!isActive()) return null;
            return WorldType.AMPLIFIED;
        }
    }

    public static class ArmageddonScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.armageddon.desc",
                "§7Toutes les §e%minutes% min§7, gravier, or, potions splash et lave tombent du ciel dans un rayon de §e%rayon% §7blocs.");

        private static final DynamicLang WAVE = DynamicLang.of("scenario.armageddon.wave",
                "§cLe ciel se déchire, mettez-vous à l'abri !");

        @Var(name = "Intervalle", desc = "Temps entre deux vagues de pluie destructrice.", type = VariableType.TIME, min = 5)
        private int intervalSec = 120;

        @Var(name = "Rayon", desc = "Rayon autour de chaque joueur dans lequel tombent les projectiles.", type = VariableType.INTEGER, min = 1, max = 100)
        private int rayon = 25;

        @Var(name = "Hauteur de chute", desc = "Hauteur au-dessus du sol à laquelle apparaissent les projectiles.", type = VariableType.INTEGER, min = 1, max = 100)
        private int hauteur = 25;

        @Var(name = "Projectiles par vague", desc = "Nombre de projectiles tombant autour de chaque joueur à chaque vague.", type = VariableType.INTEGER, min = 1, max = 50)
        private int projectilesParVague = 5;

        private BukkitRunnable task;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Armageddon"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%minutes%", Math.max(1, intervalSec / 60), "%rayon%", rayon));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.FIREBALL); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            if (task != null) task.cancel();
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    rain();
                }
            };
            long period = Math.max(1, intervalSec) * 20L;
            task.runTaskTimer(Main.get(), period, period);
        }

        @Override
        public void onStop() {
            if (task != null) task.cancel();
            task = null;
        }

        private void rain() {
            List<UHCPlayer> targets = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();
            if (targets.isEmpty()) return;

            for (UHCPlayer uhcPlayer : targets) {
                Player player = uhcPlayer.getPlayer();
                if (player == null) continue;

                World world = player.getWorld();
                Location center = player.getLocation();
                for (int i = 0; i < projectilesParVague; i++) {
                    int x = center.getBlockX() + RANDOM.nextInt(rayon * 2 + 1) - rayon;
                    int z = center.getBlockZ() + RANDOM.nextInt(rayon * 2 + 1) - rayon;
                    int surface = world.getHighestBlockYAt(x, z);
                    int y = Math.min(world.getMaxHeight() - 1, surface + hauteur);
                    Location sky = new Location(world, x + 0.5D, y, z + 0.5D);

                    int kind = RANDOM.nextInt(4);
                    if (kind == 0) {
                        world.spawnFallingBlock(sky, Material.GRAVEL, (byte) 0);
                    } else if (kind == 1) {
                        world.dropItemNaturally(sky, new ItemStack(Material.GOLD_INGOT));
                    } else if (kind == 2) {
                        world.spawn(sky, ThrownPotion.class)
                                .setItem(new Potion(PotionType.INSTANT_DAMAGE).splash().toItemStack(1));
                    } else {
                        world.getBlockAt(x, surface, z).setType(Material.STATIONARY_LAVA);
                    }
                }
            }
            LangManager.get().sendAll(WAVE);
        }
    }

    public static class BedrocklessScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.bedrockless.desc",
                "§7Les §e%couches% §7couches de bedrock du fond du monde sont remplacées par de la pierre.");

        private static final int STONE_ID = Material.STONE.getId();

        @Var(name = "Couches traitées", desc = "Nombre de couches, depuis Y=0, où la bedrock est remplacée par de la pierre.", type = VariableType.INTEGER, min = 1, max = 16)
        private int couches = 5;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Bedrockless"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%couches%", couches));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BEDROCK); }

        @Override
        public BlockPopulator getPopulator(World world) {
            return new BlockPopulator() {
                @Override
                public void populate(World w, Random random, Chunk chunk) {
                    if (!isActive()) return;
                    int max = Math.min(couches, w.getMaxHeight());
                    for (int x = 0; x < CHUNK_SIZE; x++) {
                        for (int z = 0; z < CHUNK_SIZE; z++) {
                            for (int y = 0; y < max; y++) {
                                Block block = chunk.getBlock(x, y, z);
                                if (block.getType() != Material.BEDROCK) continue;
                                block.setTypeIdAndData(STONE_ID, (byte) 0, false);
                            }
                        }
                    }
                }
            };
        }
    }

    public static class BigCrackScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.bigcrack.desc",
                "§7Une faille de §e%largeur% §7blocs de large coupe le monde jusqu'au vide (§e%failles% §7faille(s)).");

        @Var(name = "Rayon de la faille", desc = "Distance de part et d'autre de l'axe vidée jusqu'au vide.", type = VariableType.INTEGER, min = 1, max = 100)
        private int rayon = 25;

        @Var(name = "Nombre de failles", desc = "1 = une faille le long de X=0, 2 = une seconde faille le long de Z=0.", type = VariableType.INTEGER, min = 1, max = 2)
        private int nombreFailles = 1;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Big Crack"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%largeur%", rayon * 2, "%failles%", nombreFailles));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.OBSIDIAN); }

        @Override
        public BlockPopulator getPopulator(World world) {
            return new BlockPopulator() {
                @Override
                public void populate(World w, Random random, Chunk chunk) {
                    if (!isActive()) return;
                    int baseX = chunk.getX() * CHUNK_SIZE;
                    int baseZ = chunk.getZ() * CHUNK_SIZE;
                    for (int x = 0; x < CHUNK_SIZE; x++) {
                        boolean surAxeX = Math.abs(baseX + x) <= rayon;
                        for (int z = 0; z < CHUNK_SIZE; z++) {
                            boolean surAxeZ = nombreFailles > 1 && Math.abs(baseZ + z) <= rayon;
                            if (!surAxeX && !surAxeZ) continue;
                            int top = w.getHighestBlockYAt(baseX + x, baseZ + z);
                            for (int y = 0; y < top; y++) {
                                chunk.getBlock(x, y, z).setTypeIdAndData(0, (byte) 0, false);
                            }
                        }
                    }
                }
            };
        }
    }

    public static class BiomeEffectScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.biomeeffect.desc",
                "§7Chaque biome applique son propre effet de potion, niveau §e%niveau%§7, tant que tu y restes.");

        private static final Map<Biome, PotionEffectType> EFFECTS = Map.ofEntries(
                Map.entry(Biome.DESERT, PotionEffectType.FIRE_RESISTANCE),
                Map.entry(Biome.DESERT_HILLS, PotionEffectType.FIRE_RESISTANCE),
                Map.entry(Biome.DESERT_MOUNTAINS, PotionEffectType.FIRE_RESISTANCE),
                Map.entry(Biome.MESA, PotionEffectType.FIRE_RESISTANCE),
                Map.entry(Biome.MESA_PLATEAU, PotionEffectType.FIRE_RESISTANCE),
                Map.entry(Biome.SAVANNA, PotionEffectType.INCREASE_DAMAGE),
                Map.entry(Biome.SAVANNA_PLATEAU, PotionEffectType.INCREASE_DAMAGE),
                Map.entry(Biome.PLAINS, PotionEffectType.SPEED),
                Map.entry(Biome.SUNFLOWER_PLAINS, PotionEffectType.SPEED),
                Map.entry(Biome.JUNGLE, PotionEffectType.JUMP),
                Map.entry(Biome.JUNGLE_HILLS, PotionEffectType.JUMP),
                Map.entry(Biome.JUNGLE_EDGE, PotionEffectType.JUMP),
                Map.entry(Biome.FOREST, PotionEffectType.FAST_DIGGING),
                Map.entry(Biome.BIRCH_FOREST, PotionEffectType.FAST_DIGGING),
                Map.entry(Biome.ROOFED_FOREST, PotionEffectType.NIGHT_VISION),
                Map.entry(Biome.SWAMPLAND, PotionEffectType.WATER_BREATHING),
                Map.entry(Biome.OCEAN, PotionEffectType.WATER_BREATHING),
                Map.entry(Biome.DEEP_OCEAN, PotionEffectType.WATER_BREATHING),
                Map.entry(Biome.RIVER, PotionEffectType.WATER_BREATHING),
                Map.entry(Biome.ICE_PLAINS, PotionEffectType.SLOW),
                Map.entry(Biome.COLD_TAIGA, PotionEffectType.SLOW),
                Map.entry(Biome.FROZEN_RIVER, PotionEffectType.SLOW),
                Map.entry(Biome.FROZEN_OCEAN, PotionEffectType.SLOW),
                Map.entry(Biome.TAIGA, PotionEffectType.DAMAGE_RESISTANCE),
                Map.entry(Biome.MEGA_TAIGA, PotionEffectType.DAMAGE_RESISTANCE),
                Map.entry(Biome.EXTREME_HILLS, PotionEffectType.JUMP),
                Map.entry(Biome.EXTREME_HILLS_PLUS, PotionEffectType.JUMP),
                Map.entry(Biome.MUSHROOM_ISLAND, PotionEffectType.INVISIBILITY));

        @Var(name = "Niveau de l'effet", desc = "Niveau de l'effet de potion accordé par le biome.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveau = 1;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Biome Effect"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%niveau%", niveau));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.LEAVES); }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            PotionEffectType type = EFFECTS.get(player.getLocation().getBlock().getBiome());
            if (type == null) return;
            UHCUtils.applyInfiniteEffects(new PotionEffect[]{new PotionEffect(type, 1, niveau - 1)}, player);
        }
    }

    public static class CatacombsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.catacombs.desc",
                "§e%etages% §7étages souterrains reproduisent le relief de la surface, le premier à §e%profondeur% §7blocs sous vos pieds.");

        private static final int FLOOR_ID = Material.SMOOTH_BRICK.getId();

        @Var(name = "Nombre d'étages", desc = "Nombre d'étages souterrains qui reproduisent la surface.", type = VariableType.INTEGER, min = 1, max = 5)
        private int etages = 3;

        @Var(name = "Profondeur du premier étage", desc = "Distance en blocs entre la surface et le sol du premier étage.", type = VariableType.INTEGER, min = 4, max = 100)
        private int profondeur = 18;

        @Var(name = "Espacement des étages", desc = "Distance en blocs entre le sol de deux étages consécutifs.", type = VariableType.INTEGER, min = 3, max = 60)
        private int espacement = 12;

        @Var(name = "Hauteur sous plafond", desc = "Hauteur creusée au-dessus du sol de chaque étage.", type = VariableType.INTEGER, min = 2, max = 12)
        private int hauteurPlafond = 4;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Catacombs"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%etages%", etages, "%profondeur%", profondeur));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SMOOTH_BRICK); }

        @Override
        public BlockPopulator getPopulator(World world) {
            return new BlockPopulator() {
                @Override
                public void populate(World w, Random random, Chunk chunk) {
                    if (!isActive()) return;
                    int baseX = chunk.getX() * CHUNK_SIZE;
                    int baseZ = chunk.getZ() * CHUNK_SIZE;
                    int top = w.getMaxHeight();
                    for (int x = 0; x < CHUNK_SIZE; x++) {
                        for (int z = 0; z < CHUNK_SIZE; z++) {
                            int surface = w.getHighestBlockYAt(baseX + x, baseZ + z) - 1;
                            for (int etage = 0; etage < etages; etage++) {
                                int sol = surface - profondeur - etage * espacement;
                                if (sol <= 1) continue;
                                chunk.getBlock(x, sol, z).setTypeIdAndData(FLOOR_ID, (byte) 0, false);
                                int plafond = Math.min(top - 1, sol + hauteurPlafond);
                                for (int y = sol + 1; y <= plafond; y++) {
                                    chunk.getBlock(x, y, z).setTypeIdAndData(0, (byte) 0, false);
                                }
                            }
                        }
                    }
                }
            };
        }
    }

    public static class ChunkApocalypseScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.chunkapocalypse.desc",
                "§e%chance%% §7des chunks sont entièrement vides : un pas de trop et c'est le vide.");

        @Var(name = "Chance de chunk vide", desc = "Chance qu'un chunk soit entièrement remplacé par du vide.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chance = 10;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Chunk Apocalypse"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%chance%", chance));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BARRIER); }

        @Override
        public BlockPopulator getPopulator(World world) {
            return new BlockPopulator() {
                @Override
                public void populate(World w, Random random, Chunk chunk) {
                    if (!isActive()) return;
                    if (!UHCUtils.Rng.chance(chance)) return;
                    int baseX = chunk.getX() * CHUNK_SIZE;
                    int baseZ = chunk.getZ() * CHUNK_SIZE;
                    for (int x = 0; x < CHUNK_SIZE; x++) {
                        for (int z = 0; z < CHUNK_SIZE; z++) {
                            int top = w.getHighestBlockYAt(baseX + x, baseZ + z);
                            for (int y = 0; y < top; y++) {
                                chunk.getBlock(x, y, z).setTypeIdAndData(0, (byte) 0, false);
                            }
                        }
                    }
                }
            };
        }
    }

    public static class ChunkBiomesScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.chunkbiomes.desc",
                "§7Chaque chunk se voit réassigner un biome : teinte, météo et mobs changent,"
                        + " §8mais le relief déjà généré reste le même. §7Les frontières sont marquées par du slime.");

        private static final int SLIME_ID = Material.SLIME_BLOCK.getId();

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Chunk Biomes"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SLIME_BLOCK); }

        @Override
        public BlockPopulator getPopulator(World world) {
            return new BlockPopulator() {
                @Override
                public void populate(World w, Random random, Chunk chunk) {
                    if (!isActive()) return;
                    Biome biome = BIOMES.get(random.nextInt(BIOMES.size()));
                    int baseX = chunk.getX() * CHUNK_SIZE;
                    int baseZ = chunk.getZ() * CHUNK_SIZE;
                    for (int x = 0; x < CHUNK_SIZE; x++) {
                        for (int z = 0; z < CHUNK_SIZE; z++) {
                            chunk.getBlock(x, 0, z).setBiome(biome);
                            if (x != 0 && x != CHUNK_SIZE - 1 && z != 0 && z != CHUNK_SIZE - 1) continue;
                            int surface = w.getHighestBlockYAt(baseX + x, baseZ + z) - 1;
                            if (surface < 1) continue;
                            chunk.getBlock(x, surface, z).setTypeIdAndData(SLIME_ID, (byte) 0, false);
                        }
                    }
                }
            };
        }
    }

    public static class Custom00Scenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.custom00.desc",
                "§7Un build choisi par l'host est posé au centre de la carte, en 0,0.");

        @Var(name = "Fichier du build", desc = "Nom du fichier schematic posé au centre de la carte.", type = VariableType.STRING)
        private String fichier = "custom00.schematic";

        private SchematicStructure structure;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Custom 0,0"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BEACON); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            World world = Common.get().getArena();
            if (world == null || fichier == null || fichier.isEmpty()) return;
            structure = SchematicStructure.named(fichier);
            structure.placeAt(world, 0, 0);
        }

        @Override
        public void onStop() {
            if (structure != null) structure.reset();
            structure = null;
        }
    }

    public static class DeadBiomesScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.deadbiomes.desc",
                "§7Toutes les §e%minutes% min§7, un biome est déclaré mort : lenteur, faiblesse et §e%degats% §7dégâts par palier à l'intérieur.");

        private static final DynamicLang DECLARED = DynamicLang.of("scenario.deadbiomes.declared",
                "§4Un nouveau biome vient d'être déclaré mort. Fuyez si la terre vous ronge.");

        private static final DynamicLang INSIDE = DynamicLang.of("scenario.deadbiomes.inside",
                "§cLe biome où tu te trouves est mort : il te dévore.");

        @Var(name = "Intervalle de déclaration", desc = "Temps entre deux biomes déclarés morts.", type = VariableType.TIME, min = 10)
        private int intervalleDeclaration = 600;

        @Var(name = "Intervalle des dégâts", desc = "Temps passé dans un biome mort entre deux pertes de vie.", type = VariableType.TIME, min = 1)
        private int intervalleDegats = 60;

        @Var(name = "Dégâts", desc = "Dégâts infligés à chaque palier passé dans un biome mort.", type = VariableType.DOUBLE, min = 0, max = 20)
        private double degats = 2.0D;

        @Var(name = "Niveau de lenteur", desc = "Niveau de Lenteur appliqué dans un biome mort.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauLenteur = 1;

        @Var(name = "Niveau de faiblesse", desc = "Niveau de Faiblesse appliqué dans un biome mort.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauFaiblesse = 1;

        private final Set<Biome> morts = EnumSet.noneOf(Biome.class);

        private final Map<UUID, Integer> expositions = new HashMap<>();

        private BukkitRunnable task;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Dead Biomes"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of(
                    "%minutes%", Math.max(1, intervalleDeclaration / 60),
                    "%degats%", degats));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.DEAD_BUSH); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            if (task != null) task.cancel();
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    declare();
                }
            };
            long period = Math.max(1, intervalleDeclaration) * 20L;
            task.runTaskTimer(Main.get(), period, period);
        }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            UUID uuid = player.getUniqueId();
            if (!morts.contains(player.getLocation().getBlock().getBiome())) {
                expositions.remove(uuid);
                return;
            }

            UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                    new PotionEffect(PotionEffectType.SLOW, 1, niveauLenteur - 1),
                    new PotionEffect(PotionEffectType.WEAKNESS, 1, niveauFaiblesse - 1)
            }, player);

            int expose = expositions.merge(uuid, 1, Integer::sum);
            if (expose < intervalleDegats) return;
            expositions.put(uuid, 0);
            if (degats > 0) player.damage(degats);
            LangManager.get().send(INSIDE, player);
        }

        @Override
        public void onStop() {
            if (task != null) task.cancel();
            task = null;
            morts.clear();
            expositions.clear();
        }

        private void declare() {
            if (morts.size() >= BIOMES.size()) return;
            Biome picked = BIOMES.get(RANDOM.nextInt(BIOMES.size()));
            while (morts.contains(picked)) {
                picked = BIOMES.get(RANDOM.nextInt(BIOMES.size()));
            }
            morts.add(picked);
            LangManager.get().sendAll(DECLARED);
        }
    }

    public static class DesertFlatWorldScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.desertflatworld.desc",
                "§7Le monde est un désert entièrement plat, avec §e%couches% §7couches de sable sur du grès.");

        private static final Set<Material> FLAT_GROUND = EnumSet.of(
                Material.GRASS,
                Material.DIRT,
                Material.STONE);

        private static final int SAND_ID = Material.SAND.getId();

        private static final int SANDSTONE_ID = Material.SANDSTONE.getId();

        @Var(name = "Couches de sable", desc = "Nombre de couches de sable posées au-dessus du grès.", type = VariableType.INTEGER, min = 1, max = 10)
        private int couchesSable = 3;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Desert Flat World"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%couches%", couchesSable));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SAND); }

        @Override
        public WorldType getWorldType() {
            if (!isActive()) return null;
            return WorldType.FLAT;
        }

        @Override
        public BlockPopulator getPopulator(World world) {
            return new BlockPopulator() {
                @Override
                public void populate(World w, Random random, Chunk chunk) {
                    if (!isActive()) return;
                    int baseX = chunk.getX() * CHUNK_SIZE;
                    int baseZ = chunk.getZ() * CHUNK_SIZE;
                    for (int x = 0; x < CHUNK_SIZE; x++) {
                        for (int z = 0; z < CHUNK_SIZE; z++) {
                            chunk.getBlock(x, 0, z).setBiome(Biome.DESERT);
                            int sol = w.getHighestBlockYAt(baseX + x, baseZ + z) - 1;
                            while (sol > 0 && !FLAT_GROUND.contains(chunk.getBlock(x, sol, z).getType())) sol--;
                            for (int y = 1; y <= sol; y++) {
                                Block block = chunk.getBlock(x, y, z);
                                if (!FLAT_GROUND.contains(block.getType())) continue;
                                int sable = Math.min(couchesSable, Math.max(1, sol - 1));
                                int id = y > sol - sable ? SAND_ID : SANDSTONE_ID;
                                block.setTypeIdAndData(id, (byte) 0, false);
                            }
                        }
                    }
                }
            };
        }
    }

    public static class DolphinsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.dolphins.desc",
                "§7Tu ne te noies plus jamais et tu nages avec §eVitesse %niveau%§7.");

        @Var(name = "Niveau de vitesse", desc = "Niveau de Vitesse accordé tant que le joueur est dans l'eau.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauVitesse = 2;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Dolphins"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%niveau%", niveauVitesse));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.RAW_FISH); }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            Material feet = player.getLocation().getBlock().getType();
            boolean dansEau = feet == Material.WATER || feet == Material.STATIONARY_WATER;
            if (!dansEau) {
                UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                        new PotionEffect(PotionEffectType.WATER_BREATHING, 1, 0)
                }, player);
                return;
            }
            UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                    new PotionEffect(PotionEffectType.WATER_BREATHING, 1, 0),
                    new PotionEffect(PotionEffectType.SPEED, 1, niveauVitesse - 1)
            }, player);
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new AltitudeAnvilsScenario(),
                new AmplifiedTerrainScenario(),
                new ArmageddonScenario(),
                new BedrocklessScenario(),
                new BigCrackScenario(),
                new BiomeEffectScenario(),
                new CatacombsScenario(),
                new ChunkApocalypseScenario(),
                new ChunkBiomesScenario(),
                new Custom00Scenario(),
                new DeadBiomesScenario(),
                new DesertFlatWorldScenario(),
                new DolphinsScenario()
        );
    }
}
