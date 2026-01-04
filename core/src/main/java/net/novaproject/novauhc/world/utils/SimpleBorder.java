package net.novaproject.novauhc.world.utils;

import lombok.Getter;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.CommonString;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;

@Getter

public class SimpleBorder extends AbstractBorder {
    private boolean isStart = false;
    private boolean isPause = true;
    private double finalSize;
    private double blocksSecond;

    public SimpleBorder(WorldBorder worldBorder) {
        super(worldBorder);
    }

    public void startReduce(double finalSize, double blocksSecond) {
        if (!this.isStart) {
            this.isStart = true;
            this.finalSize = finalSize;
            this.blocksSecond = blocksSecond;
            play();
            Bukkit.broadcastMessage(CommonString.MEETUP_START.getMessage());
            World endWorld = Bukkit.getWorld(Common.get().getArenaName() + "_the_end");
            WorldBorder endWorldBorder = endWorld.getWorldBorder();
            endWorldBorder.setSize(1000000.0D);
        }
    }

    public void play() {
        if (this.isStart && this.isPause) {
            this.isPause = false;
            WorldBorder worldBorder = getWorldBorder();
            double size = worldBorder.getSize();
            double dif = Math.abs(size - this.finalSize);
            double time = dif / this.blocksSecond;
            worldBorder.setSize(this.finalSize, (long) time);
            worldBorder.setDamageBuffer(0.0D);
        }
    }

    public void pause() {
        if (this.isStart && !this.isPause) {
            this.isPause = true;
            WorldBorder worldBorder = getWorldBorder();
            worldBorder.setSize(worldBorder.getSize());
        }
    }

}
