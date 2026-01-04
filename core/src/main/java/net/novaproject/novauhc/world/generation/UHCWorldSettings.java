package net.novaproject.novauhc.world.generation;

import net.novaproject.novauhc.UHCManager;
import org.bukkit.World;
import org.bukkit.WorldCreator;

public class UHCWorldSettings {
    private static final double DIAMOND_MULTIPLIER = 2.3 + UHCManager.get().getBoost_Diamond();
    private static final double GOLD_MULTIPLIER = 2.55 + UHCManager.get().getBoost_Gold();
    private static final double IRON_MULTIPLIER = 2 + UHCManager.get().getBoost_Iron();
    private static final double LAPIS_MULTIPLIER = 2.22 + UHCManager.get().getBoost_Lapis();

    public static String generateWorldSettings() {

        String str = "{" + "\"diamondSize\":7," +
                "\"diamondCount\":" + (int) (1 * DIAMOND_MULTIPLIER) + "," +
                "\"diamondMinHeight\":1," +
                "\"diamondMaxHeight\":16," +
                "\"goldSize\":9," +
                "\"goldCount\":" + (int) (2 * GOLD_MULTIPLIER) + "," +
                "\"goldMinHeight\":1," +
                "\"goldMaxHeight\":32," +
                "\"ironSize\":9," +
                "\"ironCount\":" + (int) (20 * IRON_MULTIPLIER) + "," +
                "\"ironMinHeight\":1," +
                "\"ironMaxHeight\":64," +
                "\"lapisSize\":7," +
                "\"lapisCount\":" + (int) (1 * LAPIS_MULTIPLIER) + "," +
                "\"lapisCenterHeight\":16," +
                "\"lapisSpread\":16" +
                "}";
        return str;
    }

    public static World createUHCWorld(String worldName) {
        WorldCreator creator = new WorldCreator(worldName);
        creator.generatorSettings(generateWorldSettings());
        creator.environment(World.Environment.NORMAL);
        creator.generateStructures(false);

        return creator.createWorld();
    }
}