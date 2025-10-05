package net.novaproject.novauhc.arena;

import org.bukkit.Location;

public class ArenaZone {
    private final Location base;
    private final double radius;
    private final double height;
    private final Location exit;

    public ArenaZone(Location base, double radius, double height, Location exit) {
        this.base = base;
        this.radius = radius;
        this.height = height;
        this.exit = exit;
    }

    public Location getBase() {
        return base;
    }

    public double getRadius() {
        return radius;
    }

    public double getHeight() {
        return height;
    }

    public Location getExit() {
        return exit;
    }

    public boolean contains(Location loc) {
        if (loc.getY() < base.getY() || loc.getY() > base.getY() + height) return false;
        double dx = loc.getX() - base.getX();
        double dz = loc.getZ() - base.getZ();
        return (dx * dx + dz * dz) <= (radius * radius);
    }
}
