package net.novaproject.novauhc.utils.schematic;

import java.util.logging.Level;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.ChunkSection;
import net.minecraft.server.v1_8_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;

/**
 * Moteur de paste bas niveau d'un {@code .schematic} (format MCEdit/WorldEdit, Minecraft 1.8).
 * Ecrit directement dans les {@code ChunkSection} NMS, par tranches de 20000 blocs
 * toutes les 2 ticks.
 *
 * <p>La plupart du code doit passer par {@link SchematicStructure}, qui ajoute la recherche de
 * sol, le nettoyage du terrain, la memorisation de l'ancre et les tests d'appartenance. Cette
 * classe n'est l'entree directe que pour un paste a position deja connue.
 *
 * <h2>Ou atterrit le build</h2>
 *
 * Le bloc de coordonnee locale {@code (x, y, z)} est ecrit a :
 *
 * <pre>
 *   monde.x = location.x + x - largeur / 2
 *   monde.y = location.y + y - (centerY ? hauteur / 2 : 0)
 *   monde.z = location.z + z - longueur / 2
 * </pre>
 *
 * La {@code location} passee est donc toujours le <b>centre horizontal</b> du build, jamais son
 * coin. En Y, elle est soit la couche la plus basse, soit le milieu, selon la surcharge appelee.
 *
 * <h2>Ce que le paste ne fait pas</h2>
 *
 * <ul>
 *   <li>Les tags NBT {@code WEOffsetX/Y/Z} ecrits par WorldEdit sont <b>ignores</b>. Une
 *       coordonnee relevee dans l'espace d'offset de WorldEdit est donc inutilisable telle
 *       quelle — voir {@link SchematicPoints}.</li>
 *   <li>Les TileEntities du schematic ne sont pas restaurees : un coffre est pose vide, une
 *       pancarte sans texte. Le contenu doit etre rempli par le code appelant.</li>
 *   <li>Aucune rotation ni miroir.</li>
 *   <li>Un fichier vide, illisible ou de dimensions nulles n'ecrit rien : un warning est logue
 *       et la methode retourne sans exception.</li>
 * </ul>
 *
 * @see SchematicStructure pour l'API a utiliser par defaut
 * @see SchematicPoints pour declarer une position a l'interieur du build
 */
public final class SchematicUtils {

    private static final int SCHEMATIC_BLOCKS_PER_TICK = 20000;


    /**
     * Pose le build <b>centre en Y</b> : {@code location} devient le milieu de la hauteur, et
     * la moitie basse du build descend sous ce point.
     * <p>
     * Convention differente de {@link SchematicStructure}, qui pose toujours la couche la plus
     * basse sur son ancre. Un point calcule via {@link SchematicPoints} est faux de
     * {@code hauteur / 2} en Y sur un build pose par cette surcharge — preferer
     * {@link #loadSchematic(JavaPlugin, File, Location, boolean)}.
     */
    public static void loadSchematic(JavaPlugin plugin, File file, final Location location) {
        placeSchematic(plugin, file, location, true);
    }

    /**
     * Pose le build <b>base sur {@code location}</b> : la couche la plus basse est ecrite a
     * {@code location.y}, et le build monte depuis la. C'est la convention utilisee par
     * {@link SchematicStructure}, donc celle avec laquelle {@link SchematicPoints} est coherent.
     *
     * @param offset parametre <b>non lu</b> : la methode force le mode base-en-bas quelle que
     *               soit la valeur passee. Ne pas s'attendre a ce que {@code true} centre en Y.
     */
    public static void loadSchematic(JavaPlugin plugin, File file, final Location location, boolean offset) {
        placeSchematic(plugin, file, location, false);
    }

    /**
     * @param centerY {@code true} : {@code location} est le milieu de la hauteur.
     *                {@code false} : {@code location} est la couche la plus basse.
     */
    private static void placeSchematic(JavaPlugin plugin, File file, final Location location, boolean centerY) {
        final NBTTagCompound schematic;
        try (FileInputStream fis = new FileInputStream(file)) {
            schematic = NBTCompressedStreamTools.a(fis);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "[Schematic] échec de lecture de " + file.getName(), e);
            return;
        }

        final short width = schematic.getShort("Width");
        final short height = schematic.getShort("Height");
        final short length = schematic.getShort("Length");
        final byte[] blocks = schematic.getByteArray("Blocks");
        final byte[] data = schematic.getByteArray("Data");

        if (width <= 0 || length <= 0 || blocks.length == 0) {
            Bukkit.getLogger().warning("[Schematic] " + file.getName() + " vide ou dimensions invalides ("
                    + width + "x" + height + "x" + length + ", " + blocks.length + " blocs)");
            return;
        }

        final int offsetX = width / 2;
        final int offsetY = centerY ? height / 2 : 0;
        final int offsetZ = length / 2;
        final long start = System.currentTimeMillis();

        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                final var world = location.getWorld();
                if (world == null) {
                    cancel();
                    return;
                }
                final var nmsWorld = ((CraftWorld) world).getHandle();
                int processed = 0;

                while (index < blocks.length && processed++ < SCHEMATIC_BLOCKS_PER_TICK) {
                    int x = index % width;
                    int y = (index / (width * length));
                    int z = (index / width) % length;

                    int blockX = location.getBlockX() + x - offsetX;
                    int blockY = location.getBlockY() + y - offsetY;
                    int blockZ = location.getBlockZ() + z - offsetZ;

                    int blockId = blocks[index] & 0xFF;
                    byte blockData = index < data.length ? data[index] : 0;
                    index++;

                    if (blockY < 0 || blockY > 255) continue;

                    Material material = Material.getMaterial(blockId);
                    if (material == null) continue;

                    final var blockAt = world.getBlockAt(blockX, blockY, blockZ);
                    if (blockAt.getTypeId() == blockId && blockAt.getData() == blockData) continue;

                    final var nmsBlock = CraftMagicNumbers.getBlock(material);
                    if (nmsBlock == null) continue;

                    final var blockDataFinal = nmsBlock.fromLegacyData(blockData);
                    final var pos = new BlockPosition(blockX, blockY, blockZ);
                    final var nmsChunk = nmsWorld.getChunkAt(blockX >> 4, blockZ >> 4);

                    int sectionIndex = blockY >> 4;
                    if (sectionIndex < 0 || sectionIndex >= nmsChunk.getSections().length) continue;

                    var cs = nmsChunk.getSections()[sectionIndex];
                    if (cs == null) {
                        cs = new ChunkSection(sectionIndex << 4, !nmsWorld.worldProvider.o());
                        nmsChunk.getSections()[sectionIndex] = cs;
                    }

                    cs.setType(blockX & 15, blockY & 15, blockZ & 15, blockDataFinal);
                    nmsChunk.tileEntities.remove(pos);
                    nmsWorld.notify(pos);
                }

                if (index >= blocks.length) {
                    Bukkit.getLogger().info("[Schematic] " + file.getName() + " posé en "
                            + (System.currentTimeMillis() - start) + "ms à " + location.getBlockX()
                            + "," + location.getBlockY() + "," + location.getBlockZ());
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}