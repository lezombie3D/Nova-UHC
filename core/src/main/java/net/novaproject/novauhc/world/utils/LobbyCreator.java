package net.novaproject.novauhc.world.utils;

import net.novaproject.novauhc.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LobbyCreator {
    public static List<String> worldsBeingTasked = new ArrayList<>();

    private static File getWorldsDirectory() {
        return Bukkit.getServer().getWorldContainer();
    }

    public static void cloneWorld(String cloneOfName, String newWorldName) {
        File cloneOf = new File(getWorldsDirectory(), cloneOfName);
        File worldFolder = new File(getWorldsDirectory(), newWorldName);

        System.out.println("Cloning world '" + cloneOf.getName() + "' into new world '" + newWorldName + "'");
        worldsBeingTasked.add(newWorldName.toLowerCase());
        long start = System.currentTimeMillis();

        Bukkit.getScheduler().runTaskAsynchronously(Main.get(), () -> {
            copyFileStructure(cloneOf, worldFolder);
            WorldCreator worldCreator = new WorldCreator(newWorldName);
            worldCreator.generateStructures(false);
            worldCreator.type(WorldType.FLAT);
            System.out.println("Phase 1 successfully finished in " + (System.currentTimeMillis() - start) + " ms (Asynchronously)");

            Bukkit.getScheduler().runTask(Main.get(), () -> {
                long start2 = System.currentTimeMillis();
                worldCreator.createWorld();
                long current = System.currentTimeMillis();
                System.out.println("Phase 2 successfully finished in " + (current - start2) + " ms (Synchronously)");
                System.out.println("Successfully cloned world '" + cloneOf.getName() + "' into new world '" + newWorldName + "' in " + (current - start) + " ms.");
                worldsBeingTasked.remove(newWorldName.toLowerCase());
            });
        });
    }

    public static void createWorld(String worldName, World.Environment environment) {
        File worldFolder = new File(getWorldsDirectory(), worldName);

        System.out.println("Creating world '" + worldName + "'...");
        worldsBeingTasked.add(worldName.toLowerCase());
        long start = System.currentTimeMillis();

        Bukkit.getScheduler().runTaskAsynchronously(Main.get(), () -> {
            WorldCreator worldCreator = new WorldCreator(worldName);
            worldCreator.environment(environment);
            worldCreator.generateStructures(false);
            worldCreator.type(WorldType.FLAT);
            System.out.println("Phase 1 successfully finished in " + (System.currentTimeMillis() - start) + " ms (Asynchronously)");

            Bukkit.getScheduler().runTask(Main.get(), () -> {
                long start2 = System.currentTimeMillis();
                Block block = new Location(worldCreator.createWorld(), 0.0, 99.0, 0.0).getBlock();
                block.setType(Material.BEDROCK);
                long current = System.currentTimeMillis();
                System.out.println("Phase 2 successfully finished in " + (current - start2) + " ms (Synchronously)");
                System.out.println("Successfully created new world '" + worldName + "' in " + (current - start) + " ms.");
                worldsBeingTasked.remove(worldName.toLowerCase());
            });
        });
    }

    public static void deleteWorld(String worldName, Location fallBackLocation) {
        File worldFolder = new File(getWorldsDirectory(), worldName);
        System.out.println("Deleting world '" + worldName + "'...");
        worldsBeingTasked.add(worldName.toLowerCase());
        long start = System.currentTimeMillis();
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            for (Player player : world.getPlayers()) {
                player.teleport(fallBackLocation);
            }
            Bukkit.unloadWorld(world, false);
        }
        System.out.println("Phase 1 successfully finished in " + (System.currentTimeMillis() - start) + " ms (Synchronously)");

        Bukkit.getScheduler().runTaskAsynchronously(Main.get(), () -> {
            long start2 = System.currentTimeMillis();
            deleteDirectory(worldFolder);
            long current = System.currentTimeMillis();
            System.out.println("Phase 2 successfully finished in " + (current - start2) + " ms (Asynchronously)");
            System.out.println("Successfully deleted world '" + worldName + "' in " + (current - start) + " ms.");
            worldsBeingTasked.remove(worldName.toLowerCase());
        });
    }

    private static void copyFileStructure(File source, File target) {
        try {
            List<String> ignore = Arrays.asList("uid.dat", "session.lock");
            if (!ignore.contains(source.getName())) {
                if (source.isDirectory()) {
                    if (!target.exists() && !target.mkdirs()) {
                        throw new IOException("Couldn't create world directory!");
                    }
                    String[] files = source.list();
                    if (files != null) {
                        for (String file : files) {
                            File srcFile = new File(source, file);
                            File destFile = new File(target, file);
                            copyFileStructure(srcFile, destFile);
                        }
                    }
                } else {
                    try (InputStream in = Files.newInputStream(source.toPath());
                         OutputStream out = Files.newOutputStream(target.toPath())) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = in.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void deleteDirectory(File directory) {
        if (!directory.exists()) {
            return;
        }
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}