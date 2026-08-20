package net.novaproject.novauhc.game;

import net.novaproject.novauhc.scenario.Scenario;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public final class Lifecycles {

    private static final List<Registration> REGISTERED = new CopyOnWriteArrayList<>();
    private static final Set<Lifecycle> ACTIVE = ConcurrentHashMap.newKeySet();

    private static boolean running = false;

    private static final GameSpi.ILifecycles INSTANCE = new GameSpi.ILifecycles() {
        @Override public void startAll() { Lifecycles.startAll(); }
        @Override public void stopAll() { Lifecycles.stopAll(); }
    };

    public static GameSpi.ILifecycles instance() {
        return INSTANCE;
    }

    public static void register(Scenario owner, Lifecycle lifecycle) {
        if (lifecycle == null || isRegistered(lifecycle)) return;
        Registration registration = new Registration(owner, lifecycle);
        REGISTERED.add(registration);
        if (running) {
            startOne(registration);
        }
    }

    public static void register(Lifecycle lifecycle) {
        register(null, lifecycle);
    }

    public static void register(Scenario owner, Runnable start, Runnable stop) {
        register(owner, new Lifecycle() {
            @Override
            public void start() {
                start.run();
            }

            @Override
            public void stop() {
                stop.run();
            }
        });
    }

    public static void startAll() {
        running = true;
        REGISTERED.forEach(Lifecycles::startOne);
    }

    public static void stopAll() {
        running = false;
        for (int i = REGISTERED.size() - 1; i >= 0; i--) {
            Lifecycle lifecycle = REGISTERED.get(i).lifecycle();
            if (!ACTIVE.remove(lifecycle)) continue;
            try {
                lifecycle.stop();
            } catch (Throwable t) {
                Bukkit.getLogger().log(Level.WARNING, "[Lifecycles] stop en échec", t);
            }
        }
    }

    private static boolean isRegistered(Lifecycle lifecycle) {
        for (Registration registration : REGISTERED) {
            if (registration.lifecycle() == lifecycle) return true;
        }
        return false;
    }

    private static void startOne(Registration registration) {
        if (registration.owner() != null && !registration.owner().isActive()) return;
        Lifecycle lifecycle = registration.lifecycle();
        if (ACTIVE.contains(lifecycle)) return;
        try {
            lifecycle.start();
            ACTIVE.add(lifecycle);
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.WARNING, "[Lifecycles] start en échec", t);
        }
    }

    private record Registration(Scenario owner, Lifecycle lifecycle) {
    }

    public interface Lifecycle {

        void start();

        void stop();
    }
}
