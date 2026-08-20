package net.novaproject.novauhc.scenario.role;

import net.novaproject.novauhc.game.PendingDeathManager;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.utils.chat.ChatManager;
import org.bukkit.Sound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public final class Bonds {

    public enum AnnounceMode { NONE, REVEALED, HIDDEN, PROXIMITY }

    private static final String CHAT_PREFIX = ".";
    private static final long PROXIMITY_CHECK_PERIOD_TICKS = 40L;

    private static final List<PendingReveal> pendingReveals = new ArrayList<>();
    private static final Map<UUID, List<Runnable>> pendingAnnounces = new HashMap<>();
    private static final Set<String> channelIds = new HashSet<>();
    private static BukkitTask proximityWatcher;

    public static BondLink link(UUID first, UUID second) {
        return new BondLink(first, second);
    }

    public static void unlink(UUID first, UUID second) {
        if (first == null || second == null) return;
        VictoryLinks.unbond(first, second);
        pendingReveals.removeIf(pending -> pending.members.contains(first) && pending.members.contains(second));
        String channelId = pairChannelId(first, second);
        if (channelIds.remove(channelId)) {
            ChatManager.get().unregisterChannel(channelId);
        }
        stopWatcherIfIdle();
    }

    public static void clearAll() {
        pendingReveals.clear();
        pendingAnnounces.clear();
        channelIds.forEach(id -> ChatManager.get().unregisterChannel(id));
        channelIds.clear();
        stopWatcherIfIdle();
        VictoryLinks.clear();
    }

    public static void onJoin(Player player) {
        if (player == null) return;
        List<Runnable> queued = pendingAnnounces.remove(player.getUniqueId());
        if (queued != null) queued.forEach(Runnable::run);
    }

    static void applyToGroup(List<UUID> members, Set<VictoryLinks.BondType> types, Camps swapCamp,
                             AnnounceMode announceMode, double revealRadius, boolean chat) {
        if (members == null || members.size() < 2) return;

        for (VictoryLinks.BondType type : types) {
            VictoryLinks.bond(members, type, swapCamp);
        }

        switch (announceMode) {
            case REVEALED -> announceRevealed(members);
            case HIDDEN -> announceHidden(members);
            case PROXIMITY -> {
                announceHidden(members);
                registerProximityReveals(members, revealRadius);
            }
            default -> { }
        }

        if (chat) {
            openGroupChannel(members);
        }
    }

    private static void announceRevealed(List<UUID> members) {
        for (UUID uuid : members) {
            List<UUID> others = new ArrayList<>(members);
            others.remove(uuid);
            deliver(uuid, player -> {
                for (UUID other : others) {
                    LangManager.get().send(CoreLang.COMMON_BOND_LINKED, player,
                            Map.of("%player%", resolveName(other)));
                }
            });
        }
    }

    private static void announceHidden(List<UUID> members) {
        for (UUID uuid : members) {
            deliver(uuid, player -> LangManager.get().send(CoreLang.COMMON_BOND_LINKED_HIDDEN, player));
        }
    }

    private static void deliver(UUID uuid, Consumer<Player> action) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            highlight(player, action);
            return;
        }
        pendingAnnounces.computeIfAbsent(uuid, k -> new ArrayList<>()).add(() -> {
            Player joined = Bukkit.getPlayer(uuid);
            if (joined != null && joined.isOnline()) highlight(joined, action);
        });
    }

    private static void highlight(Player player, Consumer<Player> action) {
        player.sendMessage(" ");
        action.accept(player);
        player.sendMessage(" ");
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0F, 1.6F);
    }

    private static String resolveName(UUID uuid) {
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) return online.getName();
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        return name != null ? name : "??";
    }

    private static void registerProximityReveals(List<UUID> members, double radius) {
        double effective = Math.max(1.0, radius);
        for (int i = 0; i < members.size(); i++) {
            for (int j = i + 1; j < members.size(); j++) {
                pendingReveals.add(new PendingReveal(members.get(i), members.get(j), effective * effective));
            }
        }
        ensureWatcher();
    }

    private static void openGroupChannel(List<UUID> members) {
        String channelId = groupChannelId(members);
        if (!channelIds.add(channelId)) return;
        ChatManager.get().createGroupChannel(channelId, members, CHAT_PREFIX);
        for (UUID uuid : members) {
            deliver(uuid, player -> LangManager.get().send(CoreLang.COMMON_BOND_CHAT_HINT, player,
                    Map.of("%prefix%", CHAT_PREFIX)));
        }
    }

    private static void ensureWatcher() {
        if (proximityWatcher != null) return;
        proximityWatcher = Bukkit.getScheduler().runTaskTimer(Main.get(), Bonds::tickProximity,
                PROXIMITY_CHECK_PERIOD_TICKS, PROXIMITY_CHECK_PERIOD_TICKS);
    }

    private static void stopWatcherIfIdle() {
        if (!pendingReveals.isEmpty() || proximityWatcher == null) return;
        proximityWatcher.cancel();
        proximityWatcher = null;
    }

    private static void tickProximity() {
        pendingReveals.removeIf(pending -> eliminated(pending.first) || eliminated(pending.second));
        pendingReveals.removeIf(Bonds::tryReveal);
        stopWatcherIfIdle();
    }

    private static boolean eliminated(UUID uuid) {
        if (!UHCManager.get().isGame()) return false;
        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) return false;
        UHCPlayer up = UHCPlayerManager.get().getPlayer(player);
        return up != null && !up.isPlaying();
    }

    private static boolean tryReveal(PendingReveal pending) {
        Player first = Bukkit.getPlayer(pending.first);
        Player second = Bukkit.getPlayer(pending.second);
        if (first == null || second == null || !first.isOnline() || !second.isOnline()) return false;
        if (!playing(first) || !playing(second)) return false;
        if (!first.getWorld().equals(second.getWorld())) return false;
        if (first.getLocation().distanceSquared(second.getLocation()) > pending.radiusSquared) return false;
        LangManager.get().send(CoreLang.COMMON_BOND_REVEALED, first, Map.of("%player%", second.getName()));
        LangManager.get().send(CoreLang.COMMON_BOND_REVEALED, second, Map.of("%player%", first.getName()));
        return true;
    }

    private static boolean playing(Player player) {
        UHCPlayer up = UHCPlayerManager.get().getPlayer(player);
        return up != null && up.isPlaying();
    }

    private static String pairChannelId(UUID a, UUID b) {
        return groupChannelId(List.of(a, b));
    }

    private static String groupChannelId(List<UUID> members) {
        List<String> sorted = new ArrayList<>();
        members.forEach(uuid -> sorted.add(uuid.toString()));
        Collections.sort(sorted);
        return "bond:" + String.join("|", sorted);
    }

    private static final class PendingReveal {

        private final UUID first;
        private final UUID second;
        private final double radiusSquared;
        private final Set<UUID> members;

        private PendingReveal(UUID first, UUID second, double radiusSquared) {
            this.first = first;
            this.second = second;
            this.radiusSquared = radiusSquared;
            this.members = Set.of(first, second);
        }
    }

    public static final class BondLink {

        private final UUID first;
        private final UUID second;
        private final EnumSet<VictoryLinks.BondType> types = EnumSet.noneOf(VictoryLinks.BondType.class);
        private Camps swapCamp;
        private AnnounceMode announceMode = AnnounceMode.NONE;
        private double revealRadius;
        private boolean chat;

        private BondLink(UUID first, UUID second) {
            this.first = first;
            this.second = second;
        }

        public BondLink dieTogether() {
            types.add(VictoryLinks.BondType.DIE_TOGETHER);
            return this;
        }

        public BondLink winTogether() {
            types.add(VictoryLinks.BondType.WIN_TOGETHER);
            return this;
        }

        public BondLink sharedPerception() {
            types.add(VictoryLinks.BondType.SHARED_PERCEPTION);
            return this;
        }

        public BondLink swapOnDeath(Camps camp) {
            types.add(VictoryLinks.BondType.CAMP_SWAP_ON_DEATH);
            this.swapCamp = camp;
            return this;
        }

        public BondLink announce() {
            this.announceMode = AnnounceMode.REVEALED;
            return this;
        }

        public BondLink announceHidden() {
            this.announceMode = AnnounceMode.HIDDEN;
            return this;
        }

        public BondLink revealOnProximity(double radius) {
            this.announceMode = AnnounceMode.PROXIMITY;
            this.revealRadius = radius;
            return this;
        }

        public BondLink chat() {
            this.chat = true;
            return this;
        }

        public void apply() {
            if (first == null || second == null || first.equals(second)) return;
            if (types.isEmpty() && announceMode == AnnounceMode.NONE && !chat) return;
            applyToGroup(List.of(first, second), types, swapCamp, announceMode, revealRadius, chat);
        }
    }

    public static final class VictoryLinks {

        public enum BondType { DIE_TOGETHER, WIN_TOGETHER, CAMP_SWAP_ON_DEATH, SHARED_PERCEPTION }

        public static final class Bond {
            public final List<UUID> members;
            public final BondType type;
            public final Camps swapCamp;

            Bond(List<UUID> members, BondType type, Camps swapCamp) {
                this.members = members;
                this.type = type;
                this.swapCamp = swapCamp;
            }
        }

        private static final List<Bond> bonds = new ArrayList<>();
        private static final Set<UUID> dying = new HashSet<>();


        public static void bond(List<UUID> members, BondType type) {
            bond(members, type, null);
        }

        public static void bond(List<UUID> members, BondType type, Camps swapCamp) {
            if (members == null || members.size() < 2) return;
            bonds.add(new Bond(new ArrayList<>(members), type, swapCamp));
        }

        public static void clear() {
            bonds.clear();
            dying.clear();
        }

        public static void unbond(UUID a, UUID b) {
            if (a == null || b == null) return;
            bonds.removeIf(bond -> bond.members.size() == 2
                    && bond.members.contains(a) && bond.members.contains(b));
        }

        public static void onDeath(UUID dead) {
            if (dead == null || dying.contains(dead)) return;
            if (PendingDeathManager.get().isPending(dead)) return;
            for (Bond b : new ArrayList<>(bonds)) {
                if (!b.members.contains(dead)) continue;
                for (UUID other : b.members) {
                    if (other.equals(dead) || dying.contains(other)) continue;
                    Player p = Bukkit.getPlayer(other);
                    if (p == null || !p.isOnline()) continue;
                    if (b.type == BondType.DIE_TOGETHER) {
                        dying.add(dead);
                        dying.add(other);
                        try {
                            p.setHealth(0);
                        } finally {
                            dying.remove(dead);
                            dying.remove(other);
                        }
                    } else if (b.type == BondType.CAMP_SWAP_ON_DEATH && b.swapCamp != null) {
                        UHCPlayer up = UHCPlayerManager.get().getPlayer(p);
                        Role role = up != null ? KPIBuilder.roleOf(up) : null;
                        if (role != null) role.setCamp(b.swapCamp);
                    }
                }
            }
        }

        public static Set<UUID> winGroupOf(UUID member) {
            Set<UUID> group = new HashSet<>();
            for (Bond b : bonds) {
                if (b.type == BondType.WIN_TOGETHER && b.members.contains(member)) {
                    group.addAll(b.members);
                }
            }
            return group;
        }

        public static boolean sharesPerception(UUID viewer, UUID target) {
            if (viewer == null || target == null) return false;
            for (Bond b : bonds) {
                if (b.type == BondType.SHARED_PERCEPTION
                        && b.members.contains(viewer) && b.members.contains(target)) {
                    return true;
                }
            }
            return false;
        }
    }
}
