package net.novaproject.novauhc.world.generation;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.generator.ChunkGenerator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class UHCWorldSettings {

    public static final double DIAMOND_BASE = 1.375;
    public static final double GOLD_BASE = 1.75;
    public static final double IRON_BASE = 1.5;
    public static final double LAPIS_BASE = 1.5;

    private static final int VANILLA_DIAMOND_COUNT = 1, VANILLA_DIAMOND_SIZE = 8;
    private static final int VANILLA_GOLD_COUNT = 2, VANILLA_GOLD_SIZE = 9;
    private static final int VANILLA_IRON_COUNT = 20, VANILLA_IRON_SIZE = 9;
    private static final int VANILLA_LAPIS_COUNT = 1, VANILLA_LAPIS_SIZE = 7;

    public static double diamondMultiplier() { return DIAMOND_BASE + UHCManager.get().getBoost_Diamond(); }
    public static double goldMultiplier()    { return GOLD_BASE + UHCManager.get().getBoost_Gold(); }
    public static double ironMultiplier()    { return IRON_BASE + UHCManager.get().getBoost_Iron(); }
    public static double lapisMultiplier()   { return LAPIS_BASE + UHCManager.get().getBoost_Lapis(); }

    static int veinCount(int vanillaCount, double multiplier) {
        return Math.max(1, (int) Math.round(vanillaCount * multiplier));
    }

    static int veinSize(int vanillaCount, int vanillaSize, double multiplier, int count) {
        return Math.max(1, (int) Math.round(vanillaCount * vanillaSize * multiplier / (double) count));
    }

    public static int diamondCount() { return veinCount(VANILLA_DIAMOND_COUNT, diamondMultiplier()); }
    public static int goldCount()    { return veinCount(VANILLA_GOLD_COUNT, goldMultiplier()); }
    public static int ironCount()    { return veinCount(VANILLA_IRON_COUNT, ironMultiplier()); }
    public static int lapisCount()   { return veinCount(VANILLA_LAPIS_COUNT, lapisMultiplier()); }

    public static int diamondSize() { return veinSize(VANILLA_DIAMOND_COUNT, VANILLA_DIAMOND_SIZE, diamondMultiplier(), diamondCount()); }
    public static int goldSize()    { return veinSize(VANILLA_GOLD_COUNT, VANILLA_GOLD_SIZE, goldMultiplier(), goldCount()); }
    public static int ironSize()    { return veinSize(VANILLA_IRON_COUNT, VANILLA_IRON_SIZE, ironMultiplier(), ironCount()); }
    public static int lapisSize()   { return veinSize(VANILLA_LAPIS_COUNT, VANILLA_LAPIS_SIZE, lapisMultiplier(), lapisCount()); }

    private static final Set<String> PROTECTED_KEYS = Set.of(
            "diamondSize", "diamondCount", "diamondMinHeight", "diamondMaxHeight",
            "goldSize", "goldCount", "goldMinHeight", "goldMaxHeight",
            "ironSize", "ironCount", "ironMinHeight", "ironMaxHeight",
            "lapisSize", "lapisCount", "lapisCenterHeight", "lapisSpread");

    public static String generateWorldSettings() {
        String str = "{" + "\"diamondSize\":" + diamondSize() + "," +
                "\"diamondCount\":" + diamondCount() + "," +
                "\"diamondMinHeight\":1," +
                "\"diamondMaxHeight\":16," +
                "\"goldSize\":" + goldSize() + "," +
                "\"goldCount\":" + goldCount() + "," +
                "\"goldMinHeight\":1," +
                "\"goldMaxHeight\":32," +
                "\"ironSize\":" + ironSize() + "," +
                "\"ironCount\":" + ironCount() + "," +
                "\"ironMinHeight\":1," +
                "\"ironMaxHeight\":64," +
                "\"lapisSize\":" + lapisSize() + "," +
                "\"lapisCount\":" + lapisCount() + "," +
                "\"lapisCenterHeight\":16," +
                "\"lapisSpread\":16," +
                "\"useCaves\":true," +
                "\"useRavines\":true," +
                "\"useMineShafts\":" + UHCManager.get().isGenerateMineshafts() + "," +
                "\"useStrongholds\":" + UHCManager.get().isGenerateStrongholds() + "," +
                "\"useVillages\":" + UHCManager.get().isGenerateVillages() + "," +
                "\"useTemples\":" + UHCManager.get().isGenerateTemples() + "," +
                "\"useMonuments\":" + UHCManager.get().isGenerateMonuments() + "," +
                "\"useDungeons\":" + UHCManager.get().isGenerateDungeons() + "," +
                "\"useWaterLakes\":" + UHCManager.get().isGenerateWaterLakes() + "," +
                "\"useLavaLakes\":" + UHCManager.get().isGenerateLavaLakes() +
                "}";
        return str;
    }

    private static String oreReport(String ore, int count, int size, int vanillaCount, int vanillaSize) {
        int total = count * size;
        int vanillaTotal = vanillaCount * vanillaSize;
        return ore + "=" + count + "x" + size + "=" + total
                + " (vanilla " + vanillaTotal + ", x" + String.format("%.2f", total / (double) vanillaTotal) + ") ";
    }

    private static String applyOverrides(String json, Map<String, String> overrides) {
        String out = json;
        for (Map.Entry<String, String> entry : overrides.entrySet()) {
            String marker = "\"" + entry.getKey() + "\":";
            int start = out.indexOf(marker);
            if (start < 0) {
                String body = out.substring(1, out.length() - 1);
                out = "{" + (body.isEmpty() ? "" : body + ",") + marker + entry.getValue() + "}";
                continue;
            }
            int from = start + marker.length();
            int end = from;
            while (end < out.length() && out.charAt(end) != ',' && out.charAt(end) != '}') end++;
            out = out.substring(0, from) + entry.getValue() + out.substring(end);
        }
        return out;
    }

    public static Map<String, String> resolveOverrides() {
        Map<String, String> overrides = new LinkedHashMap<>();
        for (Scenario scenario : ScenarioManager.get().getActiveScenarios()) {
            Map<String, String> wanted = scenario.getGeneratorOverrides();
            if (wanted == null) continue;
            for (Map.Entry<String, String> entry : wanted.entrySet()) {
                if (PROTECTED_KEYS.contains(entry.getKey())) {
                    Bukkit.getLogger().warning("[WorldGen] " + scenario.getName() + " tente de modifier "
                            + entry.getKey() + " (boost de minerai) — refuse");
                    continue;
                }
                overrides.put(entry.getKey(), entry.getValue());
            }
        }
        return overrides;
    }

    public static WorldType resolveWorldType() {
        WorldType type = null;
        for (Scenario scenario : ScenarioManager.get().getActiveScenarios()) {
            WorldType wanted = scenario.getWorldType();
            if (wanted == null) continue;
            if (type != null && type != wanted) {
                Bukkit.getLogger().warning("[WorldGen] " + scenario.getName() + " demande " + wanted
                        + " mais " + type + " est deja impose — ignore");
                continue;
            }
            type = wanted;
        }
        return type;
    }

    private static ChunkGenerator appliedGenerator;

    public static ChunkGenerator appliedChunkGenerator() {
        return appliedGenerator;
    }

    public static ChunkGenerator resolveChunkGenerator() {
        ChunkGenerator generator = null;
        String owner = null;
        for (Scenario scenario : ScenarioManager.get().getActiveScenarios()) {
            ChunkGenerator wanted = scenario.getChunkGenerator();
            if (wanted == null) continue;
            if (generator != null) {
                Bukkit.getLogger().warning("[WorldGen] " + scenario.getName()
                        + " demande un ChunkGenerator mais celui de " + owner + " est deja impose — ignore");
                continue;
            }
            generator = wanted;
            owner = scenario.getName();
        }
        return generator;
    }

    public static World createUHCWorld(String worldName) {
        WorldType type = resolveWorldType();
        ChunkGenerator generator = resolveChunkGenerator();
        appliedGenerator = generator;

        if (generator != null) {
            if (type != null) {
                Bukkit.getLogger().warning("[WorldGen] WorldType " + type
                        + " ignore — un scenario impose un ChunkGenerator");
            }
            Bukkit.getLogger().warning("[OreBoost] ChunkGenerator custom impose sur " + worldName
                    + " — ni boost de minerai, ni overrides de generation, ni structures vanilla");
            WorldCreator custom = new WorldCreator(worldName);
            custom.environment(World.Environment.NORMAL);
            custom.generateStructures(true);
            custom.generator(generator);
            return custom.createWorld();
        }

        String json = applyOverrides(generateWorldSettings(), resolveOverrides());
        Bukkit.getLogger().info("[OreBoost] generatorSettings for " + worldName + " = " + json);
        Bukkit.getLogger().info("[OreBoost] "
                + oreReport("diamond", diamondCount(), diamondSize(), VANILLA_DIAMOND_COUNT, VANILLA_DIAMOND_SIZE)
                + oreReport("gold", goldCount(), goldSize(), VANILLA_GOLD_COUNT, VANILLA_GOLD_SIZE)
                + oreReport("iron", ironCount(), ironSize(), VANILLA_IRON_COUNT, VANILLA_IRON_SIZE)
                + oreReport("lapis", lapisCount(), lapisSize(), VANILLA_LAPIS_COUNT, VANILLA_LAPIS_SIZE));

        WorldCreator creator = new WorldCreator(worldName);
        if (type == WorldType.FLAT) {
            Bukkit.getLogger().warning("[OreBoost] WorldType FLAT impose par un scenario"
                    + " — le boost de minerai ne s'applique pas a un superflat");
        } else {
            creator.generatorSettings(json);
        }
        creator.environment(World.Environment.NORMAL);
        creator.generateStructures(true);
        if (type != null) creator.type(type);

        return creator.createWorld();
    }
}

