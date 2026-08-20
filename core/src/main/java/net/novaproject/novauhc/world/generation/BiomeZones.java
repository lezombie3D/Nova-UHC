package net.novaproject.novauhc.world.generation;

import net.minecraft.server.v1_8_R3.BiomeBase;
import net.novaproject.novauhc.UHCManager;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public final class BiomeZones {

    public enum Mode {
        AUCUN, EXCLURE, SEULEMENT;

        public Mode next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    public enum Zone {
        INNER, OUTER, GLOBAL;

        public String mode() {
            UHCManager uhc = UHCManager.get();
            return switch (this) {
                case INNER -> uhc.getBiomeInnerMode();
                case OUTER -> uhc.getBiomeOuterMode();
                case GLOBAL -> uhc.getBiomeGlobalMode();
            };
        }

        public void setMode(String value) {
            UHCManager uhc = UHCManager.get();
            switch (this) {
                case INNER -> uhc.setBiomeInnerMode(value);
                case OUTER -> uhc.setBiomeOuterMode(value);
                case GLOBAL -> uhc.setBiomeGlobalMode(value);
            }
        }

        public String list() {
            UHCManager uhc = UHCManager.get();
            return switch (this) {
                case INNER -> uhc.getBiomeInnerList();
                case OUTER -> uhc.getBiomeOuterList();
                case GLOBAL -> uhc.getBiomeGlobalList();
            };
        }

        public void setList(String value) {
            UHCManager uhc = UHCManager.get();
            switch (this) {
                case INNER -> uhc.setBiomeInnerList(value);
                case OUTER -> uhc.setBiomeOuterList(value);
                case GLOBAL -> uhc.setBiomeGlobalList(value);
            }
        }

        public String replacement() {
            UHCManager uhc = UHCManager.get();
            return switch (this) {
                case INNER -> uhc.getBiomeInnerReplacement();
                case OUTER -> uhc.getBiomeOuterReplacement();
                case GLOBAL -> uhc.getBiomeGlobalReplacement();
            };
        }

        public void setReplacement(String value) {
            UHCManager uhc = UHCManager.get();
            switch (this) {
                case INNER -> uhc.setBiomeInnerReplacement(value);
                case OUTER -> uhc.setBiomeOuterReplacement(value);
                case GLOBAL -> uhc.setBiomeGlobalReplacement(value);
            }
        }

        public boolean contains(Biome biome) {
            for (String token : list().split(",")) {
                if (token.trim().equalsIgnoreCase(biome.name())) return true;
            }
            return false;
        }

        public void toggle(Biome biome) {
            Set<String> names = new LinkedHashSet<>();
            for (String token : list().split(",")) {
                String name = token.trim().toUpperCase(Locale.ROOT);
                if (!name.isEmpty()) names.add(name);
            }
            if (!names.remove(biome.name())) names.add(biome.name());
            setList(String.join(",", names));
        }
    }

    private final Rule global;
    private final Rule inner;
    private final Rule outer;
    private final int innerRadius;

    private BiomeZones(Rule global, Rule inner, Rule outer, int innerRadius) {
        this.global = global;
        this.inner = inner;
        this.outer = outer;
        this.innerRadius = innerRadius;
    }

    public static BiomeZones snapshot() {
        UHCManager uhc = UHCManager.get();
        return new BiomeZones(
                Rule.of(uhc.getBiomeGlobalMode(), uhc.getBiomeGlobalList(), uhc.getBiomeGlobalReplacement()),
                Rule.of(uhc.getBiomeInnerMode(), uhc.getBiomeInnerList(), uhc.getBiomeInnerReplacement()),
                Rule.of(uhc.getBiomeOuterMode(), uhc.getBiomeOuterList(), uhc.getBiomeOuterReplacement()),
                Math.max(0, uhc.getBiomeInnerRadius()));
    }

    public boolean isActive() {
        return global.active() || inner.active() || outer.active();
    }

    public BiomeBase resolve(int blockX, int blockZ, BiomeBase original) {
        BiomeBase result = global.apply(original);
        if (innerRadius <= 0) return result;

        int distance = Math.max(Math.abs(blockX), Math.abs(blockZ));
        return distance <= innerRadius ? inner.apply(result) : outer.apply(result);
    }

    private static final class Rule {

        private final Mode mode;
        private final Set<Biome> biomes;
        private final BiomeBase replacement;

        private Rule(Mode mode, Set<Biome> biomes, BiomeBase replacement) {
            this.mode = mode;
            this.biomes = biomes;
            this.replacement = replacement;
        }

        private static Rule of(String rawMode, String rawList, String rawReplacement) {
            Mode mode = parseMode(rawMode);
            Set<Biome> biomes = parseBiomes(rawList);
            BiomeBase replacement = parseBiome(rawReplacement);
            if (replacement == null || biomes.isEmpty()) mode = Mode.AUCUN;
            return new Rule(mode, biomes, replacement);
        }

        private boolean active() {
            return mode != Mode.AUCUN;
        }

        private BiomeBase apply(BiomeBase original) {
            if (mode == Mode.AUCUN || original == null) return original;

            boolean listed = biomes.contains(CraftBlock.biomeBaseToBiome(original));
            if (mode == Mode.EXCLURE) return listed ? replacement : original;
            return listed ? original : replacement;
        }

        private static Mode parseMode(String raw) {
            if (raw == null) return Mode.AUCUN;
            try {
                return Mode.valueOf(raw.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return Mode.AUCUN;
            }
        }

        private static Set<Biome> parseBiomes(String raw) {
            Set<Biome> out = EnumSet.noneOf(Biome.class);
            if (raw == null) return out;
            for (String token : raw.split(",")) {
                String name = token.trim().toUpperCase(Locale.ROOT);
                if (name.isEmpty()) continue;
                try {
                    out.add(Biome.valueOf(name));
                } catch (IllegalArgumentException ignored) {
                    continue;
                }
            }
            return out;
        }

        private static BiomeBase parseBiome(String raw) {
            if (raw == null || raw.trim().isEmpty()) return null;
            try {
                return CraftBlock.biomeToBiomeBase(Biome.valueOf(raw.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}
