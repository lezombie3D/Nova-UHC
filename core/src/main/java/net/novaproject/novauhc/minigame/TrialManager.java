package net.novaproject.novauhc.minigame;

import java.util.function.BiConsumer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.player.utils.InventorySnapshots;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;
import net.novaproject.novauhc.world.LobbyCreator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class TrialManager implements Listener {

    public static final String WORLD_NAME = "uhc_trials";

    private static TrialManager instance;

    public static TrialManager get() {
        if (instance == null) instance = new TrialManager();
        return instance;
    }

    private final Map<UUID, TrialSession> sessions = new HashMap<>();
    private final Map<String, Integer> slots = new HashMap<>();
    private int nextSlot;
    private boolean initialized;


    public void init() {
        if (initialized) return;
        initialized = true;
        Bukkit.getPluginManager().registerEvents(this, Main.get());
        if (Bukkit.getWorld(WORLD_NAME) == null
                && !LobbyCreator.worldsBeingTasked.contains(WORLD_NAME.toLowerCase())) {
            LobbyCreator.createWorld(WORLD_NAME, World.Environment.NORMAL);
        }
    }

    public boolean isReady() {
        return Bukkit.getWorld(WORLD_NAME) != null;
    }

    public boolean isInTrial(Player player) {
        return player != null && sessions.containsKey(player.getUniqueId());
    }

    public TrialSession getSession(Player player) {
        return player == null ? null : sessions.get(player.getUniqueId());
    }

    public TrialSession begin(Player player, TrialArena arena, TrialRequest request) {
        if (player == null || arena == null || !initialized) return null;
        World world = Bukkit.getWorld(WORLD_NAME);
        if (world == null) return null;
        if (sessions.containsKey(player.getUniqueId())) return null;

        int slot = slots.computeIfAbsent(arena.getId(), id -> nextSlot++);
        Location start = arena.ensurePlaced(world, slot);
        if (start == null) return null;

        Map<String, ItemStack[]> inventory = null;
        if (request.isSaveInventory()) {
            inventory = InventorySnapshots.savePlayerInventory(player);
        }

        TrialSession session = new TrialSession(player.getUniqueId(), arena, request.getGroupId(),
                player.getLocation().clone(), inventory, request.getOnEnd());
        sessions.put(player.getUniqueId(), session);

        if (arena.isSuspendPowers()) {
            suspendPowers(player, session);
        }

        player.teleport(start);

        if (request.getTimeLimitSeconds() > 0) {
            session.setTimeoutTask(Bukkit.getScheduler().runTaskLater(Main.get(),
                    () -> end(player, false), request.getTimeLimitSeconds() * 20L));
        }
        return session;
    }

    public void succeed(Player player) {
        end(player, true);
    }

    public void fail(Player player) {
        end(player, false);
    }

    public void end(Player player, boolean success) {
        if (player == null) return;
        TrialSession session = sessions.remove(player.getUniqueId());
        if (session == null || session.isEnded()) return;
        session.setEnded(true);

        if (session.getTimeoutTask() != null) session.getTimeoutTask().cancel();

        restorePowers(player, session);

        if (session.getSavedInventory() != null) {
            InventorySnapshots.restorePlayerInventory(player, session.getSavedInventory());
        }

        if (player.isOnline()) {
            session.setRestoring(true);
            player.teleport(session.getSavedLocation());
        }

        if (session.getOnEnd() != null) {
            session.getOnEnd().accept(player, success);
        }
    }

    public void endAll() {
        for (UUID uuid : new HashMap<>(sessions).keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) end(p, false);
            else sessions.remove(uuid);
        }
    }

    public void shutdown() {
        endAll();
        HandlerList.unregisterAll(this);
        slots.clear();
        nextSlot = 0;
        initialized = false;
        if (Bukkit.getWorld(WORLD_NAME) != null) {
            LobbyCreator.deleteWorld(WORLD_NAME, Common.get().getLobbySpawn());
        }
    }

    private void suspendPowers(Player player, TrialSession session) {
        Role role = resolveRole(player);
        if (role == null) return;
        for (Ability ability : role.getAbilities()) {
            session.getSuspendedAbilities().put(ability, ability.isDisabled());
            ability.setDisabled(true);
        }
    }

    private void restorePowers(Player player, TrialSession session) {
        for (Map.Entry<Ability, Boolean> entry : session.getSuspendedAbilities().entrySet()) {
            entry.getKey().setDisabled(entry.getValue());
        }
        session.getSuspendedAbilities().clear();
    }

    private Role resolveRole(Player player) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null) return null;
        return KPIBuilder.roleOf(uhcPlayer);
    }

    private boolean inTrialsWorld(Location loc) {
        return loc != null && loc.getWorld() != null && WORLD_NAME.equals(loc.getWorld().getName());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!inTrialsWorld(e.getBlock().getLocation())) return;
        TrialSession session = sessions.get(e.getPlayer().getUniqueId());
        if (session == null || !session.getArena().isAllowBuild()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!inTrialsWorld(e.getBlock().getLocation())) return;
        TrialSession session = sessions.get(e.getPlayer().getUniqueId());
        if (session == null || !session.getArena().isAllowBuild()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onLiquidFlow(BlockFromToEvent e) {
        if (inTrialsWorld(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (!inTrialsWorld(e.getLocation())) return;
        if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPvp(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        if (!inTrialsWorld(victim.getLocation())) return;
        Player damager = resolveDamager(e);
        if (damager == null) return;

        TrialSession victimSession = sessions.get(victim.getUniqueId());
        TrialSession damagerSession = sessions.get(damager.getUniqueId());
        if (victimSession == null || damagerSession == null) {
            e.setCancelled(true);
            return;
        }
        if (!victimSession.getArena().isAllowPvp()
                || !Objects.equals(victimSession.getGroupId(), damagerSession.getGroupId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLethalDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        TrialSession session = sessions.get(player.getUniqueId());
        if (session == null || !session.getArena().isVirtualDeath()) return;
        if (!inTrialsWorld(player.getLocation())) return;
        if (player.getHealth() - e.getFinalDamage() > 0) return;
        e.setCancelled(true);
        end(player, false);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        end(e.getPlayer(), false);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        if (!WORLD_NAME.equals(e.getFrom().getName())) return;
        TrialSession session = sessions.get(e.getPlayer().getUniqueId());
        if (session == null || session.isRestoring()) return;
        end(e.getPlayer(), false);
    }

    private Player resolveDamager(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player p) return p;
        if (e.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player shooter) return shooter;
        return null;
    }

    @Getter
    public static final class TrialRequest {

        private int timeLimitSeconds;
        private boolean saveInventory;
        private String groupId;
        private BiConsumer<Player, Boolean> onEnd;


        public static TrialRequest create() {
            return new TrialRequest();
        }

        public TrialRequest timeLimit(int seconds) {
            this.timeLimitSeconds = seconds;
            return this;
        }

        public TrialRequest saveInventory(boolean saveInventory) {
            this.saveInventory = saveInventory;
            return this;
        }

        public TrialRequest group(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public TrialRequest onEnd(BiConsumer<Player, Boolean> onEnd) {
            this.onEnd = onEnd;
            return this;
        }
    }

    @Getter
    public static final class TrialSession {

        private final UUID playerId;
        private final TrialArena arena;
        private final String groupId;
        private final Location savedLocation;
        private final Map<String, ItemStack[]> savedInventory;
        private final Map<Ability, Boolean> suspendedAbilities = new HashMap<>();
        private final BiConsumer<Player, Boolean> onEnd;

        @Setter private BukkitTask timeoutTask;
        @Setter private boolean ended;
        @Setter private boolean restoring;

        TrialSession(UUID playerId, TrialArena arena, String groupId,
                     Location savedLocation, Map<String, ItemStack[]> savedInventory,
                     BiConsumer<Player, Boolean> onEnd) {
            this.playerId = playerId;
            this.arena = arena;
            this.groupId = groupId;
            this.savedLocation = savedLocation;
            this.savedInventory = savedInventory;
            this.onEnd = onEnd;
        }
    }
}
