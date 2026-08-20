package net.novaproject.novauhc.utils.schematic;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.File;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public final class StructureSpawner implements Listener {

    @Getter
    public static final class Spawned {
        private final int index;
        private final Location anchor;
        private final Location chest;
        private final SchematicStructure structure;
        private final List<BlockState> snapshot;
        private boolean removed;
        @Setter private Object payload;

        private Spawned(int index, Location anchor, Location chest, SchematicStructure structure, List<BlockState> snapshot) {
            this.index = index;
            this.anchor = anchor;
            this.chest = chest;
            this.structure = structure;
            this.snapshot = snapshot;
        }

        public void remove() {
            if (removed) return;
            removed = true;
            for (BlockState state : snapshot) {
                state.restore();
            }
            if (structure != null) structure.reset();
        }
    }

    private record BlockState(World world, int x, int y, int z, int typeId, byte data) {
        @SuppressWarnings("deprecation")
        static BlockState capture(Block block) {
            return new BlockState(block.getWorld(), block.getX(), block.getY(), block.getZ(),
                    block.getTypeId(), block.getData());
        }

        @SuppressWarnings("deprecation")
        void restore() {
            world.getBlockAt(x, y, z).setTypeIdAndData(typeId, data, false);
        }
    }

    public static final class Builder {
        private final World world;
        private int count = 1;
        private int windowMinSeconds = 0;
        private int windowMaxSeconds = 0;
        private int radiusMin = 0;
        private int radiusMax = 0;
        private IntFunction<String> schematicPicker = i -> null;
        private BiConsumer<Player, Spawned> onChestClick;
        private Consumer<Spawned> onSpawn;
        private boolean protectBlocks = true;
        private int snapshotMargin = 10;
        private File baseDir;

        private Builder(World world) {
            this.world = world;
        }

        public Builder baseDir(File baseDir) {
            this.baseDir = baseDir;
            return this;
        }

        public Builder count(int count) {
            this.count = count;
            return this;
        }

        public Builder windowSeconds(int min, int max) {
            this.windowMinSeconds = min;
            this.windowMaxSeconds = Math.max(min, max);
            return this;
        }

        public Builder radiusRange(int min, int max) {
            this.radiusMin = min;
            this.radiusMax = Math.max(min, max);
            return this;
        }

        public Builder schematic(String fileName) {
            this.schematicPicker = i -> fileName;
            return this;
        }

        public Builder schematicPicker(IntFunction<String> picker) {
            this.schematicPicker = picker;
            return this;
        }

        public Builder onChestClick(BiConsumer<Player, Spawned> onChestClick) {
            this.onChestClick = onChestClick;
            return this;
        }

        public Builder onSpawn(Consumer<Spawned> onSpawn) {
            this.onSpawn = onSpawn;
            return this;
        }

        public Builder protectBlocks(boolean protect) {
            this.protectBlocks = protect;
            return this;
        }

        public Builder snapshotMargin(int margin) {
            this.snapshotMargin = Math.max(0, margin);
            return this;
        }

        public StructureSpawner build() {
            return new StructureSpawner(this);
        }
    }

    public static Builder builder(World world) {
        return new Builder(world);
    }

    private final World world;
    private final int count;
    private final int windowMinSeconds;
    private final int windowMaxSeconds;
    private final int radiusMin;
    private final int radiusMax;
    private final IntFunction<String> schematicPicker;
    private final BiConsumer<Player, Spawned> onChestClick;
    private final Consumer<Spawned> onSpawn;
    private final boolean protectBlocks;
    private final int snapshotMargin;
    private final File baseDir;

    private final List<Spawned> spawned = new ArrayList<>();
    private final List<BukkitTask> tasks = new ArrayList<>();
    private final Random random = new Random();
    private boolean started;

    private StructureSpawner(Builder b) {
        this.world = b.world;
        this.count = b.count;
        this.windowMinSeconds = b.windowMinSeconds;
        this.windowMaxSeconds = b.windowMaxSeconds;
        this.radiusMin = b.radiusMin;
        this.radiusMax = b.radiusMax;
        this.schematicPicker = b.schematicPicker;
        this.onChestClick = b.onChestClick;
        this.onSpawn = b.onSpawn;
        this.protectBlocks = b.protectBlocks;
        this.snapshotMargin = b.snapshotMargin;
        this.baseDir = b.baseDir;
    }

    public void start() {
        if (started) return;
        started = true;
        Bukkit.getPluginManager().registerEvents(this, Main.get());
        for (int i = 0; i < count; i++) {
            final int index = i;
            long delayTicks = (windowMinSeconds
                    + (long) (random.nextDouble() * (windowMaxSeconds - windowMinSeconds))) * 20L;
            tasks.add(Bukkit.getScheduler().runTaskLater(Main.get(), () -> spawnOne(index), delayTicks));
        }
    }

    public void stop() {
        started = false;
        tasks.forEach(BukkitTask::cancel);
        tasks.clear();
        HandlerList.unregisterAll(this);
        spawned.forEach(Spawned::remove);
        spawned.clear();
    }

    public List<Spawned> getSpawned() {
        return new ArrayList<>(spawned);
    }

    public List<Spawned> getActiveSpawned() {
        List<Spawned> active = new ArrayList<>();
        for (Spawned s : spawned) {
            if (!s.removed) active.add(s);
        }
        return active;
    }

    private void spawnOne(int index) {
        String schematicName = schematicPicker.apply(index);
        for (int attempt = 0; attempt < 8; attempt++) {
            Location anchor = tryResolveAnchor(schematicName);
            if (anchor == null) continue;
            Spawned result = placeAt(index, anchor, schematicName);
            if (result == null) continue;
            spawned.add(result);
            if (onSpawn != null) onSpawn.accept(result);
            return;
        }
        Bukkit.getLogger().warning("[StructureSpawner] aucun emplacement valide trouvé pour la structure #" + index);
    }

    private Location tryResolveAnchor(String schematicName) {
        double angle = random.nextDouble() * 2 * Math.PI;
        double dist = radiusMin + random.nextDouble() * (radiusMax - radiusMin);
        int x = (int) (Math.cos(angle) * dist);
        int z = (int) (Math.sin(angle) * dist);

        if (schematicName != null && SchematicStructure.schematicFile(baseDir, schematicName).exists()) {
            SchematicStructure probe = SchematicStructure.named(baseDir, schematicName).requireGrass(false);
            return probe.resolveGroundSpot(world, x, z);
        }
        return new Location(world, x, world.getHighestBlockYAt(x, z), z);
    }

    private Spawned placeAt(int index, Location anchor, String schematicName) {
        if (schematicName != null && SchematicStructure.schematicFile(baseDir, schematicName).exists()) {
            SchematicStructure structure = SchematicStructure.named(baseDir, schematicName).requireGrass(false);
            SchematicStructure.Dimensions dims = structure.getDimensions();
            if (dims == null) return null;
            List<BlockState> snapshot = captureRegion(anchor, dims.width(), dims.height(), dims.length(), snapshotMargin);
            if (!structure.placeExact(anchor)) return null;
            Location chest = findChest(anchor, dims);
            if (chest == null) {
                chest = anchor.clone();
                chest.getBlock().setType(Material.CHEST);
            }
            return new Spawned(index, anchor, chest, structure, snapshot);
        }

        List<BlockState> snapshot = captureRegion(anchor, 5, 4, 5, 2);
        Location chest = buildFallback(anchor);
        return new Spawned(index, anchor, chest, null, snapshot);
    }

    private List<BlockState> captureRegion(Location at, int width, int height, int length, int margin) {
        List<BlockState> states = new ArrayList<>();
        int halfW = width / 2;
        int halfL = length / 2;
        int x0 = at.getBlockX() - halfW - margin;
        int x1 = at.getBlockX() + (width - halfW) + margin;
        int z0 = at.getBlockZ() - halfL - margin;
        int z1 = at.getBlockZ() + (length - halfL) + margin;
        int y0 = Math.max(0, at.getBlockY() - 1);
        int y1 = Math.min(255, at.getBlockY() + height + 4);
        World w = at.getWorld();
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                for (int y = y0; y <= y1; y++) {
                    states.add(BlockState.capture(w.getBlockAt(x, y, z)));
                }
            }
        }
        return states;
    }

    private Location findChest(Location anchor, SchematicStructure.Dimensions dims) {
        int halfW = dims.width() / 2;
        int halfL = dims.length() / 2;
        World w = anchor.getWorld();
        for (int x = anchor.getBlockX() - halfW; x <= anchor.getBlockX() + (dims.width() - halfW); x++) {
            for (int z = anchor.getBlockZ() - halfL; z <= anchor.getBlockZ() + (dims.length() - halfL); z++) {
                for (int y = anchor.getBlockY(); y <= Math.min(255, anchor.getBlockY() + dims.height()); y++) {
                    if (w.getBlockAt(x, y, z).getType() == Material.CHEST) {
                        return new Location(w, x, y, z);
                    }
                }
            }
        }
        return null;
    }

    private Location buildFallback(Location anchor) {
        World w = anchor.getWorld();
        int bx = anchor.getBlockX();
        int by = anchor.getBlockY();
        int bz = anchor.getBlockZ();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                w.getBlockAt(bx + x, by - 1, bz + z).setType(Material.SMOOTH_BRICK);
                for (int y = 0; y <= 3; y++) {
                    boolean corner = Math.abs(x) == 2 && Math.abs(z) == 2;
                    Block block = w.getBlockAt(bx + x, by + y, bz + z);
                    if (corner && y < 3) {
                        block.setType(Material.COBBLE_WALL);
                    } else if (corner) {
                        block.setType(Material.GLOWSTONE);
                    } else if (block.getType() != Material.AIR) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }
        Block chestBlock = w.getBlockAt(bx, by, bz);
        chestBlock.setType(Material.CHEST);
        return chestBlock.getLocation();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (onChestClick == null) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null) return;
        Spawned target = spawnedAtChest(e.getClickedBlock().getLocation());
        if (target == null) return;
        e.setCancelled(true);
        onChestClick.accept(e.getPlayer(), target);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (!protectBlocks) return;
        Location loc = e.getBlock().getLocation();
        for (Spawned s : spawned) {
            if (s.removed) continue;
            if (s.structure != null && s.structure.isInBounds(loc, 0)) {
                e.setCancelled(true);
                return;
            }
            if (s.structure == null && s.anchor.getWorld().equals(loc.getWorld())
                    && s.anchor.distanceSquared(loc) <= 25) {
                e.setCancelled(true);
                return;
            }
        }
    }

    private Spawned spawnedAtChest(Location loc) {
        for (Spawned s : spawned) {
            if (s.removed) continue;
            if (s.chest.getWorld().equals(loc.getWorld())
                    && s.chest.getBlockX() == loc.getBlockX()
                    && s.chest.getBlockY() == loc.getBlockY()
                    && s.chest.getBlockZ() == loc.getBlockZ()) {
                return s;
            }
        }
        return null;
    }
}

