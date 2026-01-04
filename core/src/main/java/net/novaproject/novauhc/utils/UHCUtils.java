package net.novaproject.novauhc.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.*;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.*;

public class UHCUtils {

    public static ItemStack createCustomHead(String base64Texture) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", base64Texture));

        try {
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        head.setItemMeta(meta);
        return head;
    }

    public static Map<String, ItemStack[]> savePlayerInventory(Player player) {
        PlayerInventory inv = player.getInventory();
        Map<String, ItemStack[]> savedInventory = new HashMap<>();
        savedInventory.put("armor", inv.getArmorContents());
        savedInventory.put("inventory", inv.getContents());

        return savedInventory;
    }

    public static String formatValue(int value,int min) {
        if (value == min) {
            return ChatColor.RED + "Désactivé";
        }
        return ChatColor.GREEN + String.valueOf(value);
    }
    public static void applyInfiniteEffects(PotionEffect[] effects, Player player) {
        for (PotionEffect activeEffect : effects) {
            player.getPlayer().removePotionEffect(activeEffect.getType());
        }
        for (PotionEffect effect : effects) {
            effect.apply(player);
        }
    }

    public static String getInventoryContentsAsString(Map<String, ItemStack[]> savedInventory) {
        if (savedInventory == null) return "Aucun inventaire sauvegardé.";

        StringBuilder content = new StringBuilder("Inventaire sauvegardé :\n");

        content.append(" - Armure : ");
        if (savedInventory.containsKey("armor")) {
            for (ItemStack item : savedInventory.get("armor")) {
                if (item != null && item.getType() != Material.AIR) {
                    content.append(item.getType().name()).append(", ");
                }
            }
        }
        content.append("\n");
        content.append(" - Contenu : ");
        if (savedInventory.containsKey("inventory")) {
            for (ItemStack item : savedInventory.get("inventory")) {
                if (item != null && item.getType() != Material.AIR) {
                    content.append(item.getAmount()).append("x ").append(item.getType().toString()).append(", ");
                }
            }
        }
        return content.toString();
    }

    public static void giveLobbyItems(Player player) {
        player.getInventory().setItem(0, Common.get().getTeamItem().getItemstack());
        player.getInventory().setItem(2, Common.get().getActiveRole().getItemstack());
        player.getInventory().setItem(6, Common.get().getActiveScenario().getItemstack());
        ItemStack item = new ItemStack(Material.APPLE);
        ItemMeta meta = item.getItemMeta();
        item.setItemMeta(meta);
        if (player.hasPermission("novauhc.host") || player.hasPermission("novauhc.cohost")) {
            player.getInventory().setItem(4, Common.get().getConfigItem().getItemstack());
            player.getInventory().setItem(8, Common.get().getReglesItem().getItemstack());
        }


    }

    public static void restorePlayerInventory(Player player, Map<String, ItemStack[]> savedInventory) {
        if (savedInventory == null) return;

        PlayerInventory inv = player.getInventory();

        if (savedInventory.containsKey("armor")) {
            inv.setArmorContents(savedInventory.get("armor"));
        }

        if (savedInventory.containsKey("inventory")) {
            inv.setContents(savedInventory.get("inventory"));
        }
    }

    public static void spawnFloatingDamage(Player viewer, String text) {
        Location eyeLocation = viewer.getEyeLocation();
        Vector direction = eyeLocation.getDirection().normalize();

        Location spawnLoc = eyeLocation.add(direction.multiply(2));

        WorldServer nmsWorld = ((CraftWorld) spawnLoc.getWorld()).getHandle();
        EntityArmorStand armorStand = new EntityArmorStand(nmsWorld, spawnLoc.getX(), spawnLoc.getY(), spawnLoc.getZ());

        armorStand.setCustomName(text);
        armorStand.setCustomNameVisible(true);
        armorStand.setInvisible(true);
        armorStand.setSmall(true);
        armorStand.setBasePlate(false);
        armorStand.setArms(false);
        armorStand.setHealth(0.5f);
        armorStand.n(true);

        PacketPlayOutSpawnEntityLiving spawnPacket = new PacketPlayOutSpawnEntityLiving(armorStand);
        ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(spawnPacket);

        int entityId = armorStand.getId();

        new BukkitRunnable() {
            @Override
            public void run() {
                PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(entityId);
                ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(destroyPacket);
            }
        }.runTaskLater(Main.get(), 10L);
    }

    public static void setRealHealth(int maxHealth, int currentHealth, Player player, int abso) {
        if (maxHealth <= 0) return;

        double heal = (20.0 * currentHealth) / maxHealth;
        heal = Math.max(1.0, Math.min(20.0, heal));

        player.setHealth(heal);

        double ratio = 250.0;
        int absorptionHP = (int) (abso / ratio * 2);

        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        nmsPlayer.setAbsorptionHearts(absorptionHP);
    }

    public static void clearPlayerInventory(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

    }

    public static ItemCreator createCustomButon(String texture, String name, List<String> lore) {
        ItemCreator item = new ItemCreator(createCustomHead(texture));
        item.setName(name);
        if (lore != null) item.setLores(lore);
        return item;
    }

    public static String getFormattedTime(int seconds) {
        return seconds >= 3600
                ? String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60)
                : String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    public static ItemCreator redMinus(List<String> lore, String name) {

        String text = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGU0YjhiOGQyMzYyYzg2NGUwNjIzMDE0ODdkOTRkMzI3MmE2YjU3MGFmYmY4MGMyYzViMTQ4Yzk1NDU3OWQ0NiJ9fX0=;";
        return createCustomButon(text, name, lore);
    }

    public static ItemCreator greenPlus(List<String> lore, String name) {
        String text = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzkwZjYyZWM1ZmEyZTkzZTY3Y2YxZTAwZGI4YWY0YjQ3YWM3YWM3NjlhYTA5YTIwM2ExZjU3NWExMjcxMGIxMCJ9fX0=";
        return createCustomButon(text, name, lore);
    }

    public static ItemCreator arrowDroite(List<String> lore, String name) {
        String text = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzkwZjYyZWM1ZmEyZTkzZTY3Y2YxZTAwZGI4YWY0YjQ3YWM3YWM3NjlhYTA5YTIwM2ExZjU3NWExMjcxMGIxMCJ9fX0=";
        return createCustomButon(text, name, lore);
    }

    public static ItemCreator arrowGauche(List<String> lore, String name) {
        String text = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RjOWU0ZGNmYTQyMjFhMWZhZGMxYjViMmIxMWQ4YmVlYjU3ODc5YWYxYzQyMzYyMTQyYmFlMWVkZDUifX19";
        return createCustomButon(text, name, lore);
    }

    public static String calculPourcentage(double valeur, double total) {
        if (total <= 0) return "0.0%";
        double pourcentage = (valeur / total) * 100;
        return String.format("%.1f%%", pourcentage);
    }

    public static ItemCreator getReset() {
        return new ItemCreator(Material.REDSTONE).setName(ChatColor.RED + "Réinitialisée les valeur.");
    }

    public static List<String> getLoreForCLique(String left, String rigth) {
        return Arrays.asList("", ChatColor.YELLOW + "► Clic gauche pour " + ChatColor.GREEN + left, ChatColor.YELLOW + "► Clic droit pour " + ChatColor.RED + rigth, "");

    }

    public static void loadSchematic(JavaPlugin plugin, File file, final Location location) {
        try {
            FileInputStream fis = new FileInputStream(file);
            NBTTagCompound schematic = NBTCompressedStreamTools.a(fis);

            short width = schematic.getShort("Width");
            short height = schematic.getShort("Height");
            short length = schematic.getShort("Length");

            byte[] blocks = schematic.getByteArray("Blocks");
            byte[] data = schematic.getByteArray("Data");

            int offsetX = width / 2;
            int offsetY = height / 2;
            int offsetZ = length / 2;

            final long start = System.currentTimeMillis();

            new BukkitRunnable() {
                int index = 0;

                @Override
                public void run() {
                    int limit = 10000000;
                    int placed = 0;

                    while (index < blocks.length && placed++ < limit) {
                        int x = index % width;
                        int y = (index / (width * length));
                        int z = (index / width) % length;

                        int blockX = location.getBlockX() + x - offsetX;
                        int blockY = location.getBlockY() + y - offsetY;
                        int blockZ = location.getBlockZ() + z - offsetZ;

                        int blockId = blocks[index] & 0xFF;
                        byte blockData = data[index];

                        Material material = Material.getMaterial(blockId);
                        if (material == null) {
                            index++;
                            continue;
                        }

                        final var world = location.getWorld();
                        final var blockAt = world.getBlockAt(blockX, blockY, blockZ);

                        if (blockAt.getTypeId() != blockId || blockAt.getData() != blockData) {
                            final var nmsBlock = CraftMagicNumbers.getBlock(material);
                            if (nmsBlock == null) {
                                index++;
                                continue;
                            }

                            final var blockDataFinal = nmsBlock.fromLegacyData(blockData);

                            final var pos = new BlockPosition(blockX, blockY, blockZ);
                            final var nmsWorld = ((CraftWorld) world).getHandle();
                            final var nmsChunk = nmsWorld.getChunkAt(blockX >> 4, blockZ >> 4);
                            var cs = nmsChunk.getSections()[blockY >> 4];

                            if (cs == null) {
                                cs = new ChunkSection((blockY >> 4) << 4, !nmsWorld.worldProvider.o());
                                nmsChunk.getSections()[blockY >> 4] = cs;
                            }

                            cs.setType(blockX & 15, blockY & 15, blockZ & 15, blockDataFinal);
                            nmsChunk.tileEntities.remove(pos);

                            final var packet = new PacketPlayOutBlockChange(nmsWorld, pos);
                            for (final var p : Bukkit.getServer().getOnlinePlayers()) {
                                if(location.getWorld().equals(p.getWorld())) ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
                            }
                        }

                        index++;
                    }


                    if (index >= blocks.length) {
                        Bukkit.broadcastMessage("§aSchematic " + file.getName() + " loaded in " + (System.currentTimeMillis() - start) + "ms");
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 2L);
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.broadcastMessage("§cAn error has occured while loading the schematic " + file.getName() + ". (" + e.getMessage() + ")");
        }
    }

}
