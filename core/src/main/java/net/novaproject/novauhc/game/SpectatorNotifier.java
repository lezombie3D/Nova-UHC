package net.novaproject.novauhc.game;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.event.UhcAbilityEvents.UhcAbilityUsedEvent;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lobby.RankManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SpectatorNotifier implements Listener {

    private static final long FIGHT_THROTTLE_MS = 30_000L;
    private static final long GROUP_THROTTLE_MS = 60_000L;
    private static final long COMMAND_THROTTLE_MS = 3_000L;
    private static final long ABILITY_THROTTLE_MS = 10_000L;
    private static final long GROUP_CHECK_PERIOD_TICKS = 100L;
    private static final double GROUP_CLUSTER_RADIUS = 25.0;

    private static final Set<String> PRIVATE_COMMANDS = Set.of(
            "/msg", "/w", "/tell", "/r", "/t", "/h", "/dev", "/helpop", "/mg");

    private static final Map<String, Long> FIGHT_THROTTLE = new HashMap<>();
    private static final Map<String, Long> FREE_PUNCH_THROTTLE = new HashMap<>();
    private static final Map<String, Long> GROUP_THROTTLE = new HashMap<>();
    private static final Map<String, Long> COMMAND_THROTTLE = new HashMap<>();
    private static final Map<String, Long> ABILITY_THROTTLE = new HashMap<>();

    private static final Lang TP_BUTTON = DynamicLang.of("ui.specnotify.tp_button", "§8[§b➜ TP§8]");
    private static final Lang TP_HOVER = DynamicLang.of("ui.specnotify.tp_hover",
            "§7Téléportation vers §f%player%\n§7Position : §f%x%, %y%, %z%");
    private static final Lang FREE_PUNCH = DynamicLang.of("ui.specnotify.free_punch",
            "§e▸ §6FREE PUNCH §8│ §f%a% §7a frappé §f%b% §7avant le PvP");

    private static boolean taskStarted = false;

    public SpectatorNotifier() {
        if (taskStarted) return;
        taskStarted = true;
        Bukkit.getScheduler().runTaskTimer(Main.get(), this::tick, GROUP_CHECK_PERIOD_TICKS, GROUP_CHECK_PERIOD_TICKS);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAbilityUsed(UhcAbilityUsedEvent event) {
        if (!canNotify()) return;
        Player player = event.getPlayer();
        if (player == null || event.getAbility() == null) return;
        String abilityKey = player.getUniqueId() + "|" + event.getAbility().getName();
        if (isThrottled(ABILITY_THROTTLE, abilityKey, ABILITY_THROTTLE_MS)) return;
        notifySpectators(CoreLang.COMMON_SPECNOTIFY_ABILITY, Map.of(
                "%player%", player.getName(),
                "%ability%", event.getAbility().getName()), player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFight(EntityDamageByEntityEvent event) {
        if (!canNotify()) return;
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;
        Player damager = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        if (damager.getUniqueId().equals(victim.getUniqueId())) return;
        if (!isAlive(damager) || !isAlive(victim)) return;
        if (isThrottled(FIGHT_THROTTLE, pairKey(damager.getUniqueId(), victim.getUniqueId()), FIGHT_THROTTLE_MS)) return;
        notifySpectators(CoreLang.COMMON_SPECNOTIFY_FIGHT, Map.of(
                "%a%", damager.getName(),
                "%b%", victim.getName()), damager);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFreePunch(EntityDamageByEntityEvent event) {
        if (!canNotify() || !event.isCancelled()) return;
        if (!(event.getDamager() instanceof Player damager) || !(event.getEntity() instanceof Player victim)) return;
        if (damager.getUniqueId().equals(victim.getUniqueId())) return;
        if (damager.getWorld().getPVP()) return;
        if (!isAlive(damager) || !isAlive(victim)) return;
        if (isThrottled(FREE_PUNCH_THROTTLE, pairKey(damager.getUniqueId(), victim.getUniqueId()), FIGHT_THROTTLE_MS)) return;
        notifySpectators(FREE_PUNCH, Map.of(
                "%a%", damager.getName(),
                "%b%", victim.getName()), damager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!canNotify()) return;
        if (!isAlive(event.getPlayer())) return;
        String root = event.getMessage().split(" ")[0].toLowerCase(Locale.ROOT);
        if (PRIVATE_COMMANDS.contains(root)) return;
        if (isThrottled(COMMAND_THROTTLE, event.getPlayer().getUniqueId().toString(), COMMAND_THROTTLE_MS)) return;
        notifySpectators(CoreLang.COMMON_SPECNOTIFY_COMMAND, Map.of(
                "%player%", event.getPlayer().getName(),
                "%command%", event.getMessage()), event.getPlayer());
    }

    private void tick() {
        if (!UHCManager.get().isGame()) {
            FIGHT_THROTTLE.clear();
            GROUP_THROTTLE.clear();
            COMMAND_THROTTLE.clear();
            ABILITY_THROTTLE.clear();
            return;
        }
        checkGroupSizes();
    }

    private void checkGroupSizes() {
        if (!UHCManager.get().isSpectatorNotifications()) return;
        GroupSizeManager groupSizeManager = GroupSizeManager.get();
        if (groupSizeManager == null || !groupSizeManager.isEnabled()) return;
        int maxSize = groupSizeManager.getCurrentSize();
        if (maxSize <= 0) return;
        List<Player> alive = new ArrayList<>();
        for (UHCPlayer up : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = up.getPlayer();
            if (player != null && player.isOnline()) alive.add(player);
        }
        for (List<Player> cluster : clusterize(alive)) {
            if (cluster.size() <= maxSize) continue;
            long now = System.currentTimeMillis();
            boolean anyFresh = false;
            for (Player member : cluster) {
                Long last = GROUP_THROTTLE.get(member.getUniqueId().toString());
                if (last == null || now - last >= GROUP_THROTTLE_MS) {
                    anyFresh = true;
                    break;
                }
            }
            if (!anyFresh) continue;
            List<String> names = new ArrayList<>();
            for (Player member : cluster) {
                names.add(member.getName());
                GROUP_THROTTLE.put(member.getUniqueId().toString(), now);
            }
            names.sort(String::compareTo);
            notifySpectators(CoreLang.COMMON_SPECNOTIFY_GROUP, Map.of(
                    "%players%", String.join("§7, §f", names),
                    "%count%", cluster.size(),
                    "%size%", maxSize), cluster.get(0));
        }
    }

    private List<List<Player>> clusterize(List<Player> players) {
        List<List<Player>> clusters = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();
        double radiusSquared = GROUP_CLUSTER_RADIUS * GROUP_CLUSTER_RADIUS;
        for (Player start : players) {
            if (!visited.add(start.getUniqueId())) continue;
            List<Player> cluster = new ArrayList<>();
            Deque<Player> queue = new ArrayDeque<>();
            queue.add(start);
            while (!queue.isEmpty()) {
                Player current = queue.poll();
                cluster.add(current);
                for (Player other : players) {
                    if (visited.contains(other.getUniqueId())) continue;
                    if (!current.getWorld().equals(other.getWorld())) continue;
                    if (current.getLocation().distanceSquared(other.getLocation()) > radiusSquared) continue;
                    visited.add(other.getUniqueId());
                    queue.add(other);
                }
            }
            clusters.add(cluster);
        }
        return clusters;
    }

    private String pairKey(UUID a, UUID b) {
        return a.compareTo(b) < 0 ? a + "|" + b : b + "|" + a;
    }

    private boolean isThrottled(Map<String, Long> throttle, String key, long windowMs) {
        long now = System.currentTimeMillis();
        Long last = throttle.get(key);
        if (last != null && now - last < windowMs) return true;
        throttle.put(key, now);
        return false;
    }

    private boolean canNotify() {
        return UHCManager.get().isGame() && UHCManager.get().isSpectatorNotifications();
    }

    private boolean isAlive(Player player) {
        UHCPlayer up = UHCPlayerManager.get().getPlayer(player);
        return up != null && up.isPlaying() && !up.isSpec();
    }

    private static boolean isModerator(Player player, UHCPlayer up) {
        return up.isSpec() || (RankManager.isStaff(player) && !up.isPlaying());
    }

    public static void notifySpectators(Lang key, Map<String, Object> placeholders, Player teleportTarget) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            UHCPlayer up = UHCPlayerManager.get().getPlayer(online);
            if (up == null) continue;
            if (!isModerator(online, up)) continue;
            if (PendingDeathManager.get().isPending(online.getUniqueId())) continue;
            if (teleportTarget == null || !teleportTarget.isOnline()) {
                LangManager.get().send(key, online, placeholders);
                continue;
            }
            online.spigot().sendMessage(actionable(online, key, placeholders, teleportTarget));
        }
    }

    private static BaseComponent[] actionable(Player viewer, Lang key, Map<String, Object> placeholders,
                                              Player teleportTarget) {
        Location location = teleportTarget.getLocation();
        TextComponent button = new TextComponent(" "
                + LangManager.get().get(TP_BUTTON, viewer, placeholders));
        button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(LangManager.get().get(TP_HOVER, viewer, Map.of(
                        "%player%", teleportTarget.getName(),
                        "%x%", location.getBlockX(),
                        "%y%", location.getBlockY(),
                        "%z%", location.getBlockZ()))).create()));
        button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/spec " + teleportTarget.getName()));
        return new BaseComponent[]{
                new TextComponent(LangManager.get().get(key, viewer, placeholders)),
                button};
    }
}
