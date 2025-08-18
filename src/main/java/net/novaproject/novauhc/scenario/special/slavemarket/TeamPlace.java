package net.novaproject.novauhc.scenario.special.slavemarket;

import org.bukkit.Location;

/**
 * Représente les positions d'une équipe dans le scénario SlaveMarket
 */
public class TeamPlace {
    private final Location captainLocation;
    private final Location slaveLocation;

    public TeamPlace(Location captainLocation, Location slaveLocation) {
        this.captainLocation = captainLocation;
        this.slaveLocation = slaveLocation;
    }

    public Location getCaptainLocation() {
        return captainLocation;
    }

    public Location getSlaveLocation() {
        return slaveLocation;
    }

    public boolean isValid() {
        return captainLocation != null && slaveLocation != null;
    }
}
