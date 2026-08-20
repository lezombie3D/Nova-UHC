package net.novaproject.novauhc.utils.cooldown;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Cooldown {

    public interface OnTick {
        void tick(int secondsLeft);
    }

    public interface OnEnd {
        void end();
    }

    private final int duration;
    private int timeLeft;
    private boolean running;
    private OnTick onTick;
    private OnEnd onEnd;
    private BukkitTask task;

    public Cooldown(int durationSeconds) {
        this.duration = Math.max(0, durationSeconds);
        this.timeLeft = this.duration;
    }

    public Cooldown onTick(OnTick onTick) {
        this.onTick = onTick;
        return this;
    }

    public Cooldown onEnd(OnEnd onEnd) {
        this.onEnd = onEnd;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public boolean isRunning() {
        return running;
    }

    public Cooldown start() {
        timeLeft = duration;
        running = duration > 0;
        if (!running && onEnd != null) onEnd.end();
        return this;
    }

    public void step() {
        if (!running) return;
        timeLeft--;
        if (onTick != null) onTick.tick(timeLeft);
        if (timeLeft <= 0) {
            running = false;
            if (onEnd != null) onEnd.end();
        }
    }

    public void reduce(int seconds) {
        if (!running) return;
        timeLeft = Math.max(0, timeLeft - Math.max(0, seconds));
        if (timeLeft == 0) {
            running = false;
            if (onEnd != null) onEnd.end();
        }
    }

    public void launch(Plugin plugin) {
        if (running) return;
        start();
        if (!running) return;
        task = new BukkitRunnable() {
            @Override
            public void run() {
                step();
                if (!running) cancel();
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void cancel() {
        running = false;
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}

