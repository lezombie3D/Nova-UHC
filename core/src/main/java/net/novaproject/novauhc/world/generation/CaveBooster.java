package net.novaproject.novauhc.world.generation;

import java.util.logging.Level;
import net.minecraft.server.v1_8_R3.ChunkProviderGenerate;
import net.minecraft.server.v1_8_R3.ChunkProviderServer;
import net.minecraft.server.v1_8_R3.IChunkProvider;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import java.lang.reflect.Field;

public class CaveBooster {

    public static void install(World world, int caveMultiplier, int ravineMultiplier) {
        if (world == null) return;
        try {
            WorldServer nms = ((CraftWorld) world).getHandle();
            ChunkProviderServer cps = nms.chunkProviderServer;
            IChunkProvider raw = cps.chunkProvider;

            ChunkProviderGenerate cpg = unwrap(raw);
            if (cpg == null) {
                Bukkit.getLogger().warning("[CaveBooster] " + world.getName()
                        + " : impossible d'unwrap " + raw.getClass().getName() + " — skip");
                return;
            }

            setField(cpg, "u", new BoostedCaveGenerator(caveMultiplier));
            setField(cpg, "z", new BoostedRavineGenerator(ravineMultiplier));

            Bukkit.getLogger().info("[CaveBooster] " + world.getName()
                    + " caves x" + caveMultiplier
                    + " ravines x" + ravineMultiplier
                    + " (wrapper: " + raw.getClass().getSimpleName() + ")");
        } catch (Throwable e) {
            Bukkit.getLogger().warning("[CaveBooster] install failed on " + world.getName() + ": " + e);
            Bukkit.getLogger().log(Level.SEVERE, "Erreur inattendue", e);
        }
    }

    private static ChunkProviderGenerate unwrap(Object raw) throws ReflectiveOperationException {
        Object cur = raw;
        for (int depth = 0; depth < 5 && cur != null; depth++) {
            if (cur instanceof ChunkProviderGenerate cpg) return cpg;
            Field providerField = findField(cur.getClass(), IChunkProvider.class, "provider");
            if (providerField == null) return null;
            providerField.setAccessible(true);
            cur = providerField.get(cur);
        }
        return null;
    }

    private static Field findField(Class<?> type, Class<?> fieldType, String preferredName) {
        Class<?> c = type;
        while (c != null && c != Object.class) {
            try {
                Field f = c.getDeclaredField(preferredName);
                if (fieldType.isAssignableFrom(f.getType())) return f;
            } catch (NoSuchFieldException ignored) {}
            for (Field f : c.getDeclaredFields()) {
                if (fieldType.isAssignableFrom(f.getType())) return f;
            }
            c = c.getSuperclass();
        }
        return null;
    }

    private static void setField(ChunkProviderGenerate target, String fieldName, Object value) throws ReflectiveOperationException {
        Field f = ChunkProviderGenerate.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}