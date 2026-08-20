package net.novaproject.novauhc.world;

import lombok.Getter;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;

@Getter

public class SimpleBorder {
    private final WorldBorder worldBorder;
    private boolean isStart = false;
    private boolean isPause = true;
    private double finalSize;
    private double blocksSecond;

    public SimpleBorder(WorldBorder worldBorder) {
        this.worldBorder = worldBorder;
    }

    public void startReduce(double finalSize, double blocksSecond) {
        if (!this.isStart) {
            this.isStart = true;
            this.finalSize = finalSize;
            this.blocksSecond = blocksSecond;
            play();
            Bukkit.broadcastMessage(LangManager.get().get(CoreLang.COMMON_MEETUP_START));
            World endWorld = Bukkit.getWorld(Common.get().getArenaName() + "_the_end");
            if (endWorld != null) {
                WorldBorder endWorldBorder = endWorld.getWorldBorder();
                endWorldBorder.setSize(Math.max(endWorldBorder.getSize(), getWorldBorder().getSize() * 4));
            }
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
            worldBorder.setDamageAmount(UHCManager.get().getBorderDamageAmount());
            worldBorder.setDamageBuffer(UHCManager.get().getBorderDamageBuffer());
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

