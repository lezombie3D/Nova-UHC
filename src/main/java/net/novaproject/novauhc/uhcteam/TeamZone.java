package net.novaproject.novauhc.uhcteam;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class TeamZone {
    private final UHCTeam team;
    private final int minX;
    private final int minZ;
    private final int maxX;
    private final int maxZ;
    private final String worldName;

    public TeamZone(UHCTeam team, int centerX, int centerZ, int size, String worldName) {
        this.team = team;
        this.minX = centerX - (size / 2);
        this.minZ = centerZ - (size / 2);
        this.maxX = centerX + (size / 2);
        this.maxZ = centerZ + (size / 2);
        this.worldName = worldName;
    }

    public boolean isInZone(Location location) {
        if (!location.getWorld().getName().equals(worldName)) {
            return false;
        }

        int x = location.getBlockX();
        int z = location.getBlockZ();

        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public UHCTeam getTeam() {
        return team;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public String getWorldName() {
        return worldName;
    }

    public Location getSpawn() {
        int centerX = (minX + maxX) / 2;
        int centerZ = (minZ + maxZ) / 2;
        return new Location(Bukkit.getWorld(worldName), centerX, 0, centerZ);
    }
}
