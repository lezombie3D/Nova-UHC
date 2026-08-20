package net.novaproject.novauhc.minigame;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.ui.CustomInventory;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public abstract class RealTimeGame extends CustomInventory implements MiniGames.MiniGame {

    protected BukkitTask task;
    protected boolean over;

    protected RealTimeGame(Player player) {
        super(player);
    }

    protected abstract void onStart();

    protected abstract void tick();

    protected abstract long tickPeriod();

    protected long startDelay() {
        return 20L;
    }

    protected void onFinish() {}

    @Override
    public final void start() {
        stop();
        over = false;
        onStart();
        MiniGames.MiniGameRegistry.register(this);
        open();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (over || !getPlayer().isOnline()
                        || CustomInventory.cache.get(getPlayer().getUniqueId()) != RealTimeGame.this) {
                    stop();
                    return;
                }
                tick();
            }
        }.runTaskTimer(Main.get(), startDelay(), tickPeriod());
    }

    @Override
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        MiniGames.MiniGameRegistry.unregister(this);
    }

    protected void finish() {
        over = true;
        stop();
        onFinish();
    }

    @Override
    public void onPlayerQuit(UUID uuid) {
        stop();
    }

    @Override
    public Collection<UUID> participants() {
        return Collections.singletonList(getPlayer().getUniqueId());
    }

    @Override
    public void onClose() {
        stop();
    }
}

