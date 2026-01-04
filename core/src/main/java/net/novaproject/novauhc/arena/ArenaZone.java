package net.novaproject.novauhc.arena;

import org.bukkit.Location;

public record ArenaZone(Location base, double radius, double height, Location exit) {

    public boolean contains(Location loc) {
        if (loc.getY() < base.getY() || loc.getY() > base.getY() + height) return false;
        double dx = loc.getX() - base.getX();
        double dz = loc.getZ() - base.getZ();
        return (dx * dx + dz * dz) <= (radius * radius);
    }
}
