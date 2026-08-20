package net.novaproject.novauhc.utils.nms;

import net.novaproject.novauhc.debug.DebugLog;
import java.lang.reflect.Modifier;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class NmsAccessor {

    public static final class NmsException extends RuntimeException {
        public NmsException(String message) { super(message); }
        public NmsException(String message, Throwable cause) { super(message, cause); }
    }

    private static final Logger LOG = Logger.getLogger("Minecraft");
    private static final ConcurrentHashMap<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private static volatile String version;


    public static String version() {
        String v = version;
        if (v == null) {
            v = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            version = v;
        }
        return v;
    }

    private static Class<?> resolve(String basePackage, String[] candidates) {
        String cacheKey = basePackage + "|" + String.join(",", candidates);
        Class<?> cached = CLASS_CACHE.get(cacheKey);
        if (cached != null) return cached;
        ClassNotFoundException last = null;
        for (String name : candidates) {
            try {
                Class<?> c = Class.forName(basePackage + "." + version() + "." + name);
                CLASS_CACHE.put(cacheKey, c);
                return c;
            } catch (ClassNotFoundException e) {
                last = e;
            }
        }
        throw new NmsException("classe introuvable : " + basePackage + "." + version() + ".[" + String.join("|", candidates) + "]", last);
    }

    public static Class<?> nmsClass(String... candidates) {
        return resolve("net.minecraft.server", candidates);
    }

    public static Class<?> obcClass(String... candidates) {
        return resolve("org.bukkit.craftbukkit", candidates);
    }

    public static Class<?> nmsClassOrNull(String... candidates) {
        try {
            return nmsClass(candidates);
        } catch (NmsException e) {
            warnOnce(e.getMessage());
            return null;
        }
    }

    public static Class<?> obcClassOrNull(String... candidates) {
        try {
            return obcClass(candidates);
        } catch (NmsException e) {
            warnOnce(e.getMessage());
            return null;
        }
    }

    public static Object getHandle(Object bukkitObject) {
        try {
            Method m = bukkitObject.getClass().getMethod("getHandle");
            return m.invoke(bukkitObject);
        } catch (ReflectiveOperationException e) {
            throw new NmsException("getHandle() impossible sur " + bukkitObject.getClass().getName(), e);
        }
    }

    public static Field field(Class<?> clazz, String... names) {
        for (String name : names) {
            try {
                Field f = clazz.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NmsException("champ introuvable dans " + clazz.getName() + " : [" + String.join("|", names) + "]");
    }

    public static Object getField(Object target, Class<?> clazz, String... names) {
        try {
            return field(clazz, names).get(target);
        } catch (IllegalAccessException e) {
            throw new NmsException("lecture de champ impossible dans " + clazz.getName(), e);
        }
    }

    public static void setField(Object target, Class<?> clazz, Object value, String... names) {
        try {
            field(clazz, names).set(target, value);
        } catch (IllegalAccessException e) {
            throw new NmsException("écriture de champ impossible dans " + clazz.getName(), e);
        }
    }

    public static void sendPacket(Player player, Object packet) {
        try {
            Object handle = getHandle(player);
            Object connection = handle.getClass().getField("playerConnection").get(handle);
            Class<?> packetClass = nmsClass("Packet");
            Method send = connection.getClass().getMethod("sendPacket", packetClass);
            send.invoke(connection, packet);
        } catch (ReflectiveOperationException | NmsException e) {
            warnOnce("sendPacket impossible : " + e.getMessage());
        }
    }

    private static void warnOnce(String message) {
        if (WARNED.add(message)) {
            LOG.log(Level.WARNING, "[NMS] " + message);
        }
    }

    public static class Reflection {

        private static final sun.misc.Unsafe unsafe;

        static {
            sun.misc.Unsafe instance = null;
            try {
                Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
                theUnsafe.setAccessible(true);
                instance = (sun.misc.Unsafe) theUnsafe.get(null);
            } catch (Throwable error) {
                DebugLog.warnOnce("Nms", "echec silencieux", error);
            }
            unsafe = instance;
        }

        public static Class<?> getNMSClass(String name) throws ClassNotFoundException {
            return Class.forName("net.minecraft.server.v1_8_R3." + name);
        }

        public static Class<?> getOBCClass(String name) throws ClassNotFoundException {
            return Class.forName("org.bukkit.craftbukkit.v1_8_R3." + name);
        }

        public static void sendPacket(Player player, Object packet) {
            try {
                Object handle = player.getClass().getMethod("getHandle").invoke(player);
                Object connection = handle.getClass().getField("playerConnection").get(handle);
                connection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(connection, packet);
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING,
                        "[Reflection] sendPacket a échoué : " + e.getMessage(), e);
            }
        }

        public static void setFinalStatic(Field field, Object value) throws ReflectiveOperationException {
            field.setAccessible(true);

            try {
                Field mf = Field.class.getDeclaredField("modifiers");
                mf.setAccessible(true);
                mf.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                field.set(null, value);
                return;
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }

            if (unsafe != null) {
                try {
                    Object staticFieldBase = unsafe.staticFieldBase(field);
                    long staticFieldOffset = unsafe.staticFieldOffset(field);
                    unsafe.putObject(staticFieldBase, staticFieldOffset, value);
                    return;
                } catch (Exception error) {
                    DebugLog.warnOnce("Nms", "echec silencieux", error);
                }
            }

            try {
                field.set(null, value);
            } catch (IllegalAccessException e) {
                throw new ReflectiveOperationException("Unable to set final static field: " + field.getName()
                        + ". This may be due to Java security restrictions. Consider using --add-opens java.base/java.lang=ALL-UNNAMED", e);
            }
        }
    }
}
