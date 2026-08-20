package net.novaproject.novauhc.ability.toolbox;

import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class TargetSelector {


    public static List<Player> playersInRadius(Location center, double radius, Player... exclude) {
        Set<UUID> ex = new HashSet<>();
        for (Player p : exclude) if (p != null) ex.add(p.getUniqueId());
        List<Player> out = new ArrayList<>();
        double r2 = radius * radius;
        for (UHCPlayer up : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player p = up.getPlayer();
            if (p == null || ex.contains(p.getUniqueId())) continue;
            if (p.getWorld() == null || !p.getWorld().equals(center.getWorld())) continue;
            if (p.getLocation().distanceSquared(center) <= r2) out.add(p);
        }
        return out;
    }

    public static List<Player> playersInRadius(Player around, double radius) {
        if (around == null) return new ArrayList<>();
        return playersInRadius(around.getLocation(), radius, around);
    }

    public static List<Player> playersInRadius(Player around, double radius, Predicate<Player> filter) {
        List<Player> out = new ArrayList<>();
        if (around == null) return out;
        for (Player p : playersInRadius(around.getLocation(), radius, around)) {
            if (filter == null || filter.test(p)) out.add(p);
        }
        return out;
    }

    public static Player nearest(Player around, double maxRadius) {
        return nearest(around, maxRadius, null);
    }

    public static Player nearest(Player around, double maxRadius, Predicate<Player> filter) {
        if (around == null) return null;
        Player best = null;
        double bestDist = maxRadius * maxRadius;
        Location from = around.getLocation();
        for (Player p : playersInRadius(from, maxRadius, around)) {
            if (filter != null && !filter.test(p)) continue;
            double d = p.getLocation().distanceSquared(from);
            if (d <= bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }

    public static Player nearest(Location center, double radius, Player... exclude) {
        Player best = null;
        double bestDist = radius * radius;
        for (Player p : playersInRadius(center, radius, exclude)) {
            double d = p.getLocation().distanceSquared(center);
            if (d <= bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }

    public static Player lookedAt(Player viewer, double maxDistance) {
        if (viewer == null) return null;
        Location eye = viewer.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        Player best = null;
        double bestDist = maxDistance;
        for (Player p : playersInRadius(eye, maxDistance, viewer)) {
            Vector toTarget = p.getLocation().add(0, 1, 0).toVector().subtract(eye.toVector());
            double dist = toTarget.length();
            if (dist > maxDistance || dist < 1.0E-4) continue;
            double dot = toTarget.normalize().dot(dir);

            if (dot > 0.95 && dist < bestDist) {
                bestDist = dist;
                best = p;
            }
        }
        return best;
    }

    public static void forPlayersInRadius(Location center, double radius, Predicate<Player> filter, Consumer<Player> action) {
        for (Player p : playersInRadius(center, radius)) {
            if (filter == null || filter.test(p)) action.accept(p);
        }
    }

    public static void forPlayersInRadius(Player around, double radius, Predicate<Player> filter, Consumer<Player> action) {
        if (around == null) return;
        for (Player p : playersInRadius(around.getLocation(), radius, around)) {
            if (filter == null || filter.test(p)) action.accept(p);
        }
    }

    public static Player lookedAtOrWarn(Player owner, double maxDistance) {
        if (owner == null) return null;
        Player target = lookedAt(owner, maxDistance);
        if (target == null) {
            owner.sendMessage("§cAucune cible dans votre ligne de vue.");
            Fx.Sounds.fail(owner);
        }
        return target;
    }
}

