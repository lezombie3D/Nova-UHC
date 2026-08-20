package net.novaproject.novauhc.ability.toolbox;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.Color;

public final class Particles {


    public static void spark(Location loc) {
        new ParticleBuilder(ParticleEffect.FIREWORKS_SPARK).setLocation(loc).display();
    }

    public static void redstone(Location loc, Color color) {
        new ParticleBuilder(ParticleEffect.REDSTONE).setColor(color).setLocation(loc).display();
    }

    public static void redstone(Location loc, Color color, Player viewer) {
        new ParticleBuilder(ParticleEffect.REDSTONE).setColor(color).setLocation(loc).display(viewer);
    }

    public static void flame(Location loc) {
        new ParticleBuilder(ParticleEffect.FLAME).setLocation(loc).display();
    }

    public static void portal(Location loc) {
        new ParticleBuilder(ParticleEffect.PORTAL).setLocation(loc).display();
    }
}

