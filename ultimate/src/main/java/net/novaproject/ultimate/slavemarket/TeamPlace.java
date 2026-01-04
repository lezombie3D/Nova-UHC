package net.novaproject.ultimate.slavemarket;

import org.bukkit.Location;


public record TeamPlace(Location captainLocation, Location slaveLocation) {

    public boolean isValid() {
        return captainLocation != null && slaveLocation != null;
    }
}
