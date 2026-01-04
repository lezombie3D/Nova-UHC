package net.novaproject.novauhc.utils.nms;

import org.bukkit.Bukkit;

public class NMSUtils {

    public static String getVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    public static Class<?> getNMSClass(String name) {
        try {
            return Class.forName("net.minecraft.server." + getVersion() + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Class<?> getClass(String className) throws ClassNotFoundException {
        return Class.forName(this + "." + className);
    }


}
