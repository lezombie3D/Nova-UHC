package net.novaproject.novauhc.utils.schematic;

import net.novaproject.novauhc.Main;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Positions nommees a l'interieur d'un build pose ({@code spawn}, {@code chest}, cibles, POI…),
 * declarees dans un {@code .yml} <b>frere du {@code .schematic}</b> : meme dossier, meme nom de
 * base. Evite de coder en dur des coordonnees monde qui ne survivent pas a un deplacement du
 * build.
 *
 * <h2>Format du fichier</h2>
 *
 * <pre>
 *   chest:  [6, 1, 17]                # dx, dy, dz
 *   spawn:  [0, 2, -4, 90.0, 0.0]     # dx, dy, dz, yaw, pitch
 *   mobs:                             # liste de points, lue par points()
 *     - [3, 0, 5]
 *     - [-3, 0, 5]
 * </pre>
 *
 * <h2>Comment donner la position d'un bloc — la seule methode correcte</h2>
 *
 * Les valeurs sont <b>relatives a l'ancre</b>, et l'ancre est le <b>centre horizontal, couche
 * la plus basse</b> du build (voir {@link SchematicStructure}). Elles ne sont donc <b>pas</b>
 * les coordonnees locales brutes du schematic.
 *
 * <p><b>Methode A — depuis l'editeur.</b> Releve les coordonnees locales du bloc
 * ({@code 0 <= x < largeur}, {@code 0 <= y < hauteur}, {@code 0 <= z < longueur}, comptees
 * depuis le coin d'origine), puis convertis :
 *
 * <pre>
 *   dx = x - largeur / 2        (division entiere)
 *   dy = y
 *   dz = z - longueur / 2       (division entiere)
 * </pre>
 *
 * <p><b>Methode B — en jeu, sans calcul.</b> Active {@code /dev log schem on} et pose la
 * structure : l'ancre est marquee en BEDROCK 5 s apres le paste. Va sur le bloc voulu, releve
 * ses coordonnees monde, et soustrais celles du BEDROCK. Le resultat est directement le triplet
 * a ecrire.
 *
 * <h2>Verification — une coordonnee hors de ces bornes est fausse</h2>
 *
 * <pre>
 *   dx dans [ -largeur / 2 , largeur - largeur / 2 - 1 ]
 *   dy dans [ 0 , hauteur - 1 ]
 *   dz dans [ -longueur / 2 , longueur - longueur / 2 - 1 ]
 * </pre>
 *
 * Exemple pour un build 77x75x88 : {@code dx} dans {@code [-38, 38]}, {@code dy} dans
 * {@code [0, 74]}, {@code dz} dans {@code [-44, 43]}.
 *
 * <h2>Piege — les coordonnees relatives de WorldEdit</h2>
 *
 * WorldEdit affiche des coordonnees relatives qui incluent les tags NBT {@code WEOffsetX/Y/Z}
 * (position du joueur au moment du {@code //copy}). <b>Le paste du core ignore totalement ces
 * tags.</b> Recopier ces valeurs telles quelles produit un decalage de {@code WEOffset +
 * dimension / 2} — soit plusieurs dizaines de blocs, bien au-dela de toute recherche de
 * proximite. Toujours repasser par les coordonnees locales (methode A) ou par le marqueur
 * BEDROCK (methode B).
 *
 * <h2>Piege — le dossier de chargement</h2>
 *
 * {@link #load(String)} cherche toujours dans {@code plugins/NovaUHC/api/schem/} et
 * <b>ignore le dossier du module</b>. Un module qui pose ses builds depuis son propre
 * {@code baseDir} doit utiliser {@link #load(File)} en lui passant
 * {@code structure.getFile()}, sinon son yml n'est jamais lu et l'absence est silencieuse.
 *
 * @see SchematicStructure#getAnchor()
 */
public final class SchematicPoints {

    private final YamlConfiguration yml;

    private SchematicPoints(YamlConfiguration yml) {
        this.yml = yml;
    }

    /**
     * Charge le yml depuis le dossier partage {@code plugins/NovaUHC/api/schem/}, quel que soit
     * l'endroit d'ou vient reellement le schematic. Pour un build pose depuis le dossier d'un
     * module, utiliser {@link #load(File)} — sinon le yml du module n'est jamais lu.
     */
    public static SchematicPoints load(String schematicName) {
        String base = schematicName == null ? "" : schematicName;
        int dot = base.lastIndexOf('.');
        if (dot > 0) base = base.substring(0, dot);
        return fromFile(new File(Main.get().getDataFolder(), "api/schem/" + base + ".yml"));
    }

    /**
     * Charge le yml frere du fichier donne (meme dossier, extension remplacee par
     * {@code .yml}). Variante a utiliser par defaut : elle suit le dossier reel du schematic.
     * Un fichier absent donne un {@code SchematicPoints} vide, jamais une exception.
     */
    public static SchematicPoints load(File schematicFile) {
        if (schematicFile == null) return new SchematicPoints(new YamlConfiguration());
        String name = schematicFile.getName();
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        File parent = schematicFile.getParentFile();
        return fromFile(new File(parent, base + ".yml"));
    }

    private static SchematicPoints fromFile(File file) {
        YamlConfiguration cfg = file != null && file.exists()
                ? YamlConfiguration.loadConfiguration(file)
                : new YamlConfiguration();
        return new SchematicPoints(cfg);
    }

    /** Decalage manuel depuis l'ancre, sans passer par un yml et sans centrage sur le bloc. */
    public static Location relative(Location anchor, double dx, double dy, double dz) {
        return anchor == null ? null : anchor.clone().add(dx, dy, dz);
    }

    public boolean has(String key) {
        return yml.contains(key);
    }

    /**
     * Resout une cle en Location monde. La Location renvoyee est <b>centree sur le bloc</b>
     * ({@code +0.5} en X et Z), donc directement utilisable comme cible de teleportation ;
     * passer par {@code getBlockX/Y/Z} pour retrouver le bloc lui-meme.
     * Un yaw/pitch est applique si la liste compte 5 elements.
     *
     * @return {@code null} si l'ancre est nulle ou si la cle est absente du yml
     */
    public Location point(Location anchor, String key) {
        if (anchor == null || !yml.contains(key)) return null;
        return fromCoords(anchor, yml.getList(key));
    }

    /**
     * Resout une cle contenant plusieurs positions. Accepte aussi bien une liste de triplets
     * qu'un triplet seul, qui est alors renvoye comme liste d'un element. Liste vide si la cle
     * est absente — jamais {@code null}.
     */
    public List<Location> points(Location anchor, String key) {
        List<Location> result = new ArrayList<>();
        if (anchor == null || !yml.contains(key)) return result;
        List<?> list = yml.getList(key);
        if (list == null || list.isEmpty()) return result;
        if (list.get(0) instanceof List) {
            for (Object entry : list) {
                if (entry instanceof List<?> coords) {
                    Location loc = fromCoords(anchor, coords);
                    if (loc != null) result.add(loc);
                }
            }
        } else {
            Location loc = fromCoords(anchor, list);
            if (loc != null) result.add(loc);
        }
        return result;
    }

    private Location fromCoords(Location anchor, List<?> coords) {
        if (coords == null || coords.size() < 3) return null;
        Location loc = anchor.clone().add(toDouble(coords.get(0)) + 0.5,
                toDouble(coords.get(1)), toDouble(coords.get(2)) + 0.5);
        if (coords.size() >= 5) {
            loc.setYaw((float) toDouble(coords.get(3)));
            loc.setPitch((float) toDouble(coords.get(4)));
        }
        return loc;
    }

    private double toDouble(Object o) {
        return o instanceof Number number ? number.doubleValue() : 0.0;
    }
}

