package net.novaproject.novauhc.utils.schematic;

import net.novaproject.novauhc.event.UhcWorldEvents.UhcSchematicPlaceEvent;
import net.novaproject.novauhc.debug.DebugLog;
import java.util.List;
import java.util.ArrayList;
import net.minecraft.server.v1_8_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.novaproject.novauhc.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.File;
import java.io.FileInputStream;
import java.util.function.Consumer;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Pose un fichier {@code .schematic} (format MCEdit/WorldEdit, Minecraft 1.8) dans le monde,
 * puis expose la zone occupee : ancre, dimensions, appartenance d'une Location.
 *
 * <h2>Convention d'ancre — a lire avant d'ecrire la moindre coordonnee</h2>
 *
 * Le paste ne part <b>pas</b> du coin du build mais de son <b>centre horizontal</b>.
 * {@link SchematicUtils} pose le bloc de coordonnee locale {@code (x, y, z)} a :
 *
 * <pre>
 *   monde.x = ancre.x + x - largeur / 2
 *   monde.y = ancre.y + y
 *   monde.z = ancre.z + z - longueur / 2
 * </pre>
 *
 * L'ancre ({@link #getAnchor()}) vaut donc exactement la coordonnee locale
 * {@code (largeur / 2, 0, longueur / 2)} : <b>centre en X et Z, couche la plus basse en Y</b>.
 * Le build s'etend de {@code ancre.y} a {@code ancre.y + hauteur - 1} : rien n'est jamais
 * pose sous l'ancre.
 *
 * <h2>Y et yOffset</h2>
 *
 * {@link #placeAt(World, int, int, int, Consumer)} applique son {@code yOffset} a l'ancre
 * <b>avant</b> le paste : l'ancre et le build montent ensemble. Un point declare dans le
 * {@code .yml} frere reste donc valable quel que soit le {@code yOffset} — ne jamais
 * l'incorporer aux coordonnees du yml.
 *
 * <h2>Exemple</h2>
 *
 * <pre>
 *   SchematicStructure maison = SchematicStructure.named("minnie_house.schematic");
 *   if (!maison.placeAt(arena, 250, -250)) return;
 *   Location ancre = maison.getAnchor();
 *   Location coffre = SchematicPoints.load(maison.getFile()).point(ancre, "chest");
 *   boolean dedans = maison.isInBounds(joueur.getLocation(), 2);
 * </pre>
 *
 * <h2>Pieges</h2>
 *
 * <ul>
 *   <li>{@link #placeAt} renvoie {@code false} quand aucun sol valide n'est trouve. Ce retour
 *       doit toujours etre teste : l'ignorer fait consommer items et cooldown pour un build
 *       qui n'existe pas.</li>
 *   <li>{@code requireGrass} vaut {@code true} par defaut : en desert, sur sable, sur neige,
 *       en grotte ou au Nether le placement echoue. Utiliser {@link #requireGrass(boolean)}
 *       avec {@code false} pour accepter tout sol solide.</li>
 *   <li>{@link #isInZone(Location, double)} ne teste <b>que</b> la distance horizontale :
 *       un joueur 40 blocs sous le build est « dans la zone ». Pour une detection bornee en
 *       Y, utiliser {@link #isInBounds(Location, double)}.</li>
 *   <li>Un rayon de zone code en dur doit etre confronte aux dimensions reelles : un rayon
 *       de 15 sur un build de 74 de large ne couvre que le sixieme de son emprise.</li>
 *   <li>{@link #reset()} doit etre appele au {@code onStop} du module, sinon la structure
 *       fuit dans la liste statique des structures posees.</li>
 * </ul>
 *
 * @see SchematicPoints pour declarer une position <b>a l'interieur</b> du build
 * @see SchematicUtils pour le moteur de paste lui-meme
 */
public final class SchematicStructure {

    /** Dimensions lues dans le NBT du fichier. {@code width} = axe X, {@code length} = axe Z. */
    public record Dimensions(int width, int height, int length) {}

    private static final List<SchematicStructure> PLACED = new ArrayList<>();

    public static List<SchematicStructure> getPlaced() {
        synchronized (PLACED) {
            return new ArrayList<>(PLACED);
        }
    }

    private final File file;
    private int groundSearchRadius = 16;
    private int clearRing = 5;
    private boolean clearAround = true;
    private boolean requireGrass = true;
    private double zoneRadius = -1.0D;

    private Location anchor;
    private Dimensions dims;

    private SchematicStructure(File file) {
        this.file = file;
    }

    public static SchematicStructure of(File schemFile) {
        return new SchematicStructure(schemFile);
    }

    /** Resout un nom vers le dossier partage {@code plugins/NovaUHC/api/schem/}. */
    public static File schematicFile(String fileName) {
        return new File(Main.get().getDataFolder(), "api/schem/" + fileName);
    }

    /**
     * Resout un nom vers le dossier d'un module (ex. {@code plugins/FairyTail/schem/}).
     * Le {@code .yml} frere sera cherche dans ce meme dossier — c'est la seule variante a
     * utiliser quand un module embarque ses propres builds.
     */
    public static File schematicFile(File baseDir, String fileName) {
        return baseDir == null ? schematicFile(fileName) : new File(baseDir, fileName);
    }

    /** Structure lue depuis le dossier partage {@code plugins/NovaUHC/api/schem/}. */
    public static SchematicStructure named(String fileName) {
        return new SchematicStructure(schematicFile(fileName));
    }

    /** Structure lue depuis le dossier d'un module. */
    public static SchematicStructure named(File baseDir, String fileName) {
        return new SchematicStructure(schematicFile(baseDir, fileName));
    }

    public File getFile() {
        return file;
    }

    public SchematicStructure groundSearchRadius(int radius) {
        this.groundSearchRadius = Math.max(0, radius);
        return this;
    }

    /** Blocs d'air ajoutes autour de l'emprise avant le paste (defaut 10). */
    public SchematicStructure clearRing(int ring) {
        this.clearRing = ring;
        return this;
    }

    public SchematicStructure clearAround(boolean clear) {
        this.clearAround = clear;
        return this;
    }

    /** Pose le build sans raser le terrain autour — a utiliser pour un build qui s'integre. */
    public SchematicStructure noClear() {
        return clearAround(false);
    }

    public SchematicStructure zoneRadius(double radius) {
        this.zoneRadius = radius;
        return this;
    }

    public double getZoneRadius() {
        if (zoneRadius >= 0) return zoneRadius;
        readDims();
        if (dims == null) return 0;
        return Math.max(dims.width(), dims.length()) / 2.0D;
    }

    /**
     * {@code true} (defaut) : le placement n'accepte qu'un sol en GRASS, et echoue en desert,
     * sur sable, sur neige, en grotte et au Nether. {@code false} : tout sol solide convient.
     */
    public SchematicStructure requireGrass(boolean require) {
        this.requireGrass = require;
        return this;
    }

    public boolean placeAt(World world, int x, int z) {
        return placeAt(world, x, z, 0, null);
    }

    public boolean placeAt(World world, int x, int z, int yOffset) {
        return placeAt(world, x, z, yOffset, null);
    }

    public boolean placeRandomInRadius(World world, int radius, Consumer<Location> after) {
        int x = ThreadLocalRandom.current().nextInt(radius * 2 + 1) - radius;
        int z = ThreadLocalRandom.current().nextInt(radius * 2 + 1) - radius;
        return placeAt(world, x, z, 0, after);
    }

    /**
     * Cherche un sol valide autour de {@code (x, z)}, y pose le build et memorise l'ancre.
     *
     * @param yOffset decale l'ancre <b>et</b> le build vers le haut avant le paste. Les points
     *                du {@code .yml} frere restent valables : ne pas les recalculer.
     * @param after   execute avec l'ancre une fois le paste lance (pas termine : le paste
     *                s'etale sur plusieurs ticks, 20000 blocs toutes les 2 ticks).
     * @return {@code false} si le fichier est illisible, le monde nul, ou si aucun sol valide
     *         n'a ete trouve dans {@code groundSearchRadius}. <b>Toujours tester ce retour</b>
     *         avant de consommer des items ou de poser un cooldown.
     */
    public boolean placeAt(World world, int x, int z, int yOffset, Consumer<Location> after) {
        if (!readDims()) return false;
        if (world == null) {
            warn("monde null — paste annulé pour " + file.getName());
            return false;
        }
        Location spot = findGroundSpot(world, x, z);
        if (spot == null) {
            warn("aucun sol valide près de " + x + "," + z + " pour " + file.getName());
            return false;
        }
        spot.add(0, yOffset, 0);
        return pasteAt(spot, after);
    }

    public Location resolveGroundSpot(World world, int x, int z) {
        if (!readDims() || world == null) return null;
        return findGroundSpot(world, x, z);
    }

    /**
     * Pose le build sans recherche de sol : {@code exact} devient directement l'ancre, donc le
     * centre horizontal et la couche la plus basse. Le build monte depuis ce point.
     */
    public boolean placeExact(Location exact) {
        if (!readDims()) return false;
        if (exact == null || exact.getWorld() == null) {
            warn("location exacte invalide pour " + file.getName());
            return false;
        }
        return pasteAt(exact.clone(), null);
    }

    private boolean pasteAt(Location spot, Consumer<Location> after) {
        if (clearAround && clearRing >= 0) clearArea(spot.getWorld(), spot, dims, clearRing);
        SchematicUtils.loadSchematic(Main.get(), file, spot, false);
        this.anchor = spot;
        synchronized (PLACED) {
            if (!PLACED.contains(this)) PLACED.add(this);
        }
        Bukkit.getLogger().info("[SchematicStructure] " + file.getName()
                + " collé à " + spot.getBlockX() + "," + spot.getBlockY() + "," + spot.getBlockZ()
                + " (dims " + dims.width() + "x" + dims.height() + "x" + dims.length()
                + ") monde=" + spot.getWorld().getName());
        DebugLog.log(DebugLog.Channel.SCHEM,
                "place", file.getName() + " @ " + spot.getBlockX() + "," + spot.getBlockY() + "," + spot.getBlockZ()
                        + " | dims=" + dims.width() + "x" + dims.height() + "x" + dims.length()
                        + " | monde=" + spot.getWorld().getName()
                        + " | clear=" + (clearAround && clearRing >= 0 ? "ring " + clearRing : "non"));
        Bukkit.getPluginManager().callEvent(
                new UhcSchematicPlaceEvent(file.getName(), spot.clone()));
        if (DebugLog.isEnabled(DebugLog.Channel.SCHEM)) {
            final Location markerLoc = spot.clone();
            Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
                markerLoc.getWorld().getBlockAt(markerLoc.getBlockX(), markerLoc.getBlockY(), markerLoc.getBlockZ())
                        .setType(Material.BEDROCK);
                DebugLog.log(DebugLog.Channel.SCHEM,
                        "marker", file.getName() + " | anchor marqué BEDROCK à " + markerLoc.getBlockX() + ","
                                + markerLoc.getBlockY() + "," + markerLoc.getBlockZ()
                                + " | monde=" + markerLoc.getWorld().getName());
            }, 100L);
        }
        if (after != null) after.accept(spot);
        return true;
    }

    public String getFileName() {
        return file == null ? "?" : file.getName();
    }

    public boolean isPlaced() {
        return anchor != null;
    }

    /**
     * Centre horizontal du build, sur sa couche la plus basse — coordonnee locale
     * {@code (largeur / 2, 0, longueur / 2)}. C'est l'origine de tous les points declares
     * dans le {@code .yml} frere. {@code null} tant que le build n'est pas pose.
     * <p>
     * En jeu, {@code /dev log schem on} marque cette position en BEDROCK 5 s apres le paste,
     * et {@code /dev schem} liste les ancres de toutes les structures posees.
     */
    public Location getAnchor() {
        return anchor == null ? null : anchor.clone();
    }

    public World getWorld() {
        return anchor == null ? null : anchor.getWorld();
    }

    public Dimensions getDimensions() {
        readDims();
        return dims;
    }

    public boolean isInZone(Location loc) {
        return isInZone(loc, getZoneRadius());
    }

    /**
     * Distance <b>horizontale</b> a l'ancre uniquement : aucune borne en Y. Un joueur en grotte
     * sous le build, ou en plein ciel au-dessus, est considere dans la zone. Pour une detection
     * qui suit reellement l'emprise du build, preferer {@link #isInBounds(Location, double)}.
     * <p>
     * Le rayon doit etre choisi d'apres les dimensions reelles : l'ancre etant au centre, un
     * build de largeur {@code W} deborde deja de {@code W / 2} dans chaque direction.
     */
    public boolean isInZone(Location loc, double radius) {
        if (anchor == null || loc == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().equals(anchor.getWorld())) return false;
        double dx = loc.getX() - anchor.getX();
        double dz = loc.getZ() - anchor.getZ();
        return (dx * dx + dz * dz) <= radius * radius;
    }

    /**
     * Boite englobante reelle du build, bornee sur les trois axes : X et Z autour de l'ancre
     * selon largeur/longueur, Y de {@code ancre.y} a {@code ancre.y + hauteur}.
     * A privilegier sur {@link #isInZone} des que la verticale compte.
     */
    public boolean isInBounds(Location loc, double margin) {
        if (anchor == null || dims == null || loc == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().equals(anchor.getWorld())) return false;
        int halfW = dims.width() / 2;
        int halfL = dims.length() / 2;
        double x0 = anchor.getBlockX() - halfW - margin;
        double x1 = anchor.getBlockX() + (dims.width() - halfW) + margin;
        double z0 = anchor.getBlockZ() - halfL - margin;
        double z1 = anchor.getBlockZ() + (dims.length() - halfL) + margin;
        double y0 = anchor.getBlockY() - margin;
        double y1 = anchor.getBlockY() + dims.height() + margin;
        return loc.getX() >= x0 && loc.getX() <= x1
                && loc.getZ() >= z0 && loc.getZ() <= z1
                && loc.getY() >= y0 && loc.getY() <= y1;
    }

    /**
     * Oublie l'ancre et retire la structure de la liste statique des structures posees.
     * A appeler au {@code onStop} du module, sinon l'entree survit a la partie et pointe sur
     * un monde potentiellement supprime.
     */
    public void reset() {
        anchor = null;
        synchronized (PLACED) {
            PLACED.remove(this);
        }
    }

    private boolean readDims() {
        if (dims != null) return true;
        if (file == null || !file.exists()) {
            warn("schematic absent : " + (file == null ? "null" : file.getName()));
            return false;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            NBTTagCompound nbt = NBTCompressedStreamTools.a(fis);
            dims = new Dimensions(nbt.getShort("Width"), nbt.getShort("Height"), nbt.getShort("Length"));
            if (dims.width() <= 0 || dims.length() <= 0) {
                warn("dimensions invalides pour " + file.getName());
                dims = null;
                return false;
            }
            return true;
        } catch (Exception e) {
            warn("lecture NBT échouée pour " + file.getName() + " : " + e.getMessage());
            return false;
        }
    }

    private Location findGroundSpot(World w, int x, int z) {
        if (isValidGround(w, x, z)) return atSurface(w, x, z);
        for (int r = 1; r <= groundSearchRadius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                    if (isValidGround(w, x + dx, z + dz)) return atSurface(w, x + dx, z + dz);
                }
            }
        }
        return requireGrass ? null : atSurface(w, x, z);
    }

    private boolean isValidGround(World w, int x, int z) {
        int y = w.getHighestBlockYAt(x, z) - 1;
        if (y < 0) return false;
        Material m = w.getBlockAt(x, y, z).getType();
        if (requireGrass) return m == Material.GRASS;
        return m.isSolid();
    }

    private Location atSurface(World w, int x, int z) {
        return new Location(w, x, w.getHighestBlockYAt(x, z), z);
    }

    private void clearArea(World w, Location at, Dimensions d, int ringExtra) {
        int halfW = d.width() / 2;
        int halfL = d.length() / 2;
        int x0 = at.getBlockX() - halfW - ringExtra;
        int x1 = at.getBlockX() + (d.width() - halfW) + ringExtra;
        int z0 = at.getBlockZ() - halfL - ringExtra;
        int z1 = at.getBlockZ() + (d.length() - halfL) + ringExtra;
        int y0 = at.getBlockY();
        int y1 = Math.min(255, at.getBlockY() + d.height() + 4);

        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                for (int y = y0; y <= y1; y++) {
                    Block b = w.getBlockAt(x, y, z);
                    if (b.getType() != Material.AIR) b.setType(Material.AIR, false);
                }
            }
        }
    }

    private void warn(String msg) {
        Bukkit.getLogger().warning("[SchematicStructure] " + msg);
    }
}