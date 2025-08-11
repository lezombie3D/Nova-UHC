package net.novaproject.novauhc.world.utils;

import org.bukkit.WorldBorder;

public class AbstractBorder {
    private final WorldBorder worldBorder;

    public AbstractBorder(WorldBorder worldBorder) {
        this.worldBorder = worldBorder;
    }

    public void init(double defaultSize, int x, int z) {
        this.worldBorder.setSize(defaultSize);
        this.worldBorder.setCenter(x, z);
    }

    public WorldBorder getWorldBorder() {
        return this.worldBorder;
    }
}
