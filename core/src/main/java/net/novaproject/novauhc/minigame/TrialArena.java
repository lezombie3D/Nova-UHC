package net.novaproject.novauhc.minigame;

import lombok.Getter;
import net.novaproject.novauhc.utils.schematic.SchematicStructure;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.function.Consumer;

@Getter
public final class TrialArena {

    private final String id;
    private String schematicName;
    private boolean allowPvp;
    private boolean allowMobs;
    private boolean allowBuild;
    private boolean suspendPowers = true;
    private boolean virtualDeath = true;
    private Vector startOffset = new Vector(0, 1, 0);
    private Consumer<Location> fallbackBuilder = TrialArena::buildDefaultPlatform;
    private File baseDir;

    private Location anchor;
    private SchematicStructure structure;

    private TrialArena(String id) {
        this.id = id;
    }

    public static TrialArena of(String id) {
        return new TrialArena(id);
    }

    public TrialArena schematic(String schematicName) {
        this.schematicName = schematicName;
        return this;
    }

    public TrialArena allowPvp(boolean allowPvp) {
        this.allowPvp = allowPvp;
        return this;
    }

    public TrialArena allowMobs(boolean allowMobs) {
        this.allowMobs = allowMobs;
        return this;
    }

    public TrialArena allowBuild(boolean allowBuild) {
        this.allowBuild = allowBuild;
        return this;
    }

    public TrialArena suspendPowers(boolean suspendPowers) {
        this.suspendPowers = suspendPowers;
        return this;
    }

    public TrialArena virtualDeath(boolean virtualDeath) {
        this.virtualDeath = virtualDeath;
        return this;
    }

    public TrialArena startOffset(double dx, double dy, double dz) {
        this.startOffset = new Vector(dx, dy, dz);
        return this;
    }

    public TrialArena fallback(Consumer<Location> fallbackBuilder) {
        this.fallbackBuilder = fallbackBuilder;
        return this;
    }

    public TrialArena baseDir(File baseDir) {
        this.baseDir = baseDir;
        return this;
    }

    public boolean isPlaced() {
        return anchor != null;
    }

    public Location getStart() {
        if (anchor == null) return null;
        return anchor.clone().add(startOffset);
    }

    Location ensurePlaced(World world, int slot) {
        if (anchor != null) return getStart();
        int x = slot * 512;
        int z = 0;

        if (schematicName != null && SchematicStructure.schematicFile(baseDir, schematicName).exists()) {
            SchematicStructure s = SchematicStructure.named(baseDir, schematicName).requireGrass(false);
            Location spot = s.resolveGroundSpot(world, x, z);
            if (spot == null) spot = new Location(world, x, world.getHighestBlockYAt(x, z), z);
            if (s.placeExact(spot)) {
                this.structure = s;
                this.anchor = spot;
                return getStart();
            }
        }

        Location spot = new Location(world, x, world.getHighestBlockYAt(x, z), z);
        fallbackBuilder.accept(spot);
        this.anchor = spot;
        return getStart();
    }

    void reset() {
        if (structure != null) structure.reset();
        structure = null;
        anchor = null;
    }

    public boolean contains(Location loc) {
        if (anchor == null || loc == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().equals(anchor.getWorld())) return false;
        if (structure != null) return structure.isInBounds(loc, 16);
        return loc.distanceSquared(anchor) <= 128 * 128;
    }

    private static void buildDefaultPlatform(Location center) {
        World w = center.getWorld();
        int bx = center.getBlockX();
        int by = center.getBlockY();
        int bz = center.getBlockZ();
        for (int x = -12; x <= 12; x++) {
            for (int z = -12; z <= 12; z++) {
                w.getBlockAt(bx + x, by - 1, bz + z).setType(Material.SMOOTH_BRICK);
                for (int y = 0; y <= 4; y++) {
                    Block b = w.getBlockAt(bx + x, by + y, bz + z);
                    boolean rim = Math.abs(x) == 12 || Math.abs(z) == 12;
                    if (rim && y <= 2) {
                        b.setType(Material.SMOOTH_BRICK);
                    } else if (b.getType() != Material.AIR) {
                        b.setType(Material.AIR);
                    }
                }
            }
        }
        for (int x = -8; x <= 8; x += 8) {
            for (int z = -8; z <= 8; z += 8) {
                w.getBlockAt(bx + x, by, bz + z).setType(Material.TORCH);
            }
        }
    }
}

