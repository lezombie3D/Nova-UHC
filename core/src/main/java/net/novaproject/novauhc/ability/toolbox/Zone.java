package net.novaproject.novauhc.ability.toolbox;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class Zone {

    protected Location center;
    protected double borderStep = 2.5;
    protected double borderYOffset = 1.0;
    protected Color borderColor = Color.CYAN;

    protected Zone(Location center) {
        this.center = center.clone();
    }

    public Location center() {
        return center.clone();
    }

    public void setCenter(Location center) {
        this.center = center.clone();
    }

    public Zone borderColor(Color color) {
        this.borderColor = color;
        return this;
    }

    public Zone borderStep(double step) {
        this.borderStep = step;
        return this;
    }

    public abstract boolean contains(Location loc);

    public abstract void renderBorder();

    public List<Player> playersInside(Player... exclude) {
        List<Player> out = new ArrayList<>();
        for (UHCPlayer up : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player p = up.getPlayer();
            if (p == null || isExcluded(p, exclude)) continue;
            if (contains(p.getLocation())) out.add(p);
        }
        return out;
    }

    private static boolean isExcluded(Player p, Player[] exclude) {
        for (Player e : exclude) if (e != null && e.equals(p)) return true;
        return false;
    }

    protected void renderBorderPoint(double x, double z) {
        Location l = new Location(center.getWorld(), x, center.getY() + borderYOffset, z);
        Particles.spark(l);
        Particles.redstone(l.clone().add(0, 0.5, 0), borderColor);
    }

    public static class CircleZone extends Zone {

        private double radius;

        public CircleZone(Location center, double radius) {
            super(center);
            this.radius = radius;
        }

        public double radius() {
            return radius;
        }

        public void setRadius(double radius) {
            this.radius = radius;
        }

        @Override
        public boolean contains(Location loc) {
            if (loc == null || loc.getWorld() == null || !loc.getWorld().equals(center.getWorld())) return false;
            double dx = loc.getX() - center.getX();
            double dz = loc.getZ() - center.getZ();
            return dx * dx + dz * dz <= radius * radius;
        }

        @Override
        public void renderBorder() {
            if (radius <= 0) return;

            double angleStep = borderStep / radius;
            for (double a = 0; a < Math.PI * 2; a += angleStep) {
                double x = center.getX() + radius * Math.cos(a);
                double z = center.getZ() + radius * Math.sin(a);
                renderBorderPoint(x, z);
            }
        }
    }

    public static class SquareZone extends Zone {

        private double half;

        public SquareZone(Location center, double half) {
            super(center);
            this.half = half;
        }

        public double half() {
            return half;
        }

        public void setHalf(double half) {
            this.half = half;
        }

        @Override
        public boolean contains(Location loc) {
            if (loc == null || loc.getWorld() == null || !loc.getWorld().equals(center.getWorld())) return false;
            return Math.abs(loc.getX() - center.getX()) <= half
                    && Math.abs(loc.getZ() - center.getZ()) <= half;
        }

        @Override
        public void renderBorder() {
            for (double t = -half; t <= half; t += borderStep) {
                renderBorderPoint(center.getX() + t, center.getZ() - half);
                renderBorderPoint(center.getX() + t, center.getZ() + half);
                renderBorderPoint(center.getX() - half, center.getZ() + t);
                renderBorderPoint(center.getX() + half, center.getZ() + t);
            }
        }
    }

    public static final class EffectField {

        private final Zone zone;
        private final int durationSeconds;
        private final Consumer<Player> perPlayer;
        private int periodTicks = 1;
        private Player follow;
        private int elapsedTicks = 0;
        private BukkitTask task;

        public EffectField(Zone zone, int durationSeconds, Consumer<Player> perPlayer) {
            this.zone = zone;
            this.durationSeconds = Math.max(0, durationSeconds);
            this.perPlayer = perPlayer;
        }

        public EffectField(Location center, double radius, int durationSeconds, Consumer<Player> perPlayer) {
            this(new CircleZone(center, radius), durationSeconds, perPlayer);
        }

        public EffectField period(int ticks) {
            this.periodTicks = Math.max(1, ticks);
            return this;
        }

        public EffectField follow(Player player) {
            this.follow = player;
            return this;
        }

        public Zone zone() {
            return zone;
        }

        public EffectField start() {
            if (task != null) return this;
            int totalTicks = durationSeconds * 20;
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (elapsedTicks >= totalTicks) {
                        stop();
                        return;
                    }
                    if (follow != null) {
                        if (!follow.isOnline() || follow.isDead()) {
                            stop();
                            return;
                        }
                        zone.setCenter(follow.getLocation());
                    }
                    elapsedTicks += periodTicks;
                    zone.renderBorder();
                    if (perPlayer != null) {
                        for (Player p : zone.playersInside()) {
                            perPlayer.accept(p);
                        }
                    }
                }
            }.runTaskTimer(Main.get(), 0L, periodTicks);
            return this;
        }

        public void stop() {
            if (task != null) {
                task.cancel();
                task = null;
            }
        }
    }
}
