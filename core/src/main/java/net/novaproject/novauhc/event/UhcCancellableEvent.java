package net.novaproject.novauhc.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class UhcCancellableEvent extends Event implements Cancellable {

    private boolean cancelled = false;

    @Override
    public final boolean isCancelled() {
        return cancelled;
    }

    @Override
    public final void setCancelled(boolean cancelled) {
        if (cancelled && !canBeCancelled()) return;
        this.cancelled = cancelled;
    }

    protected boolean canBeCancelled() {
        return true;
    }
}
