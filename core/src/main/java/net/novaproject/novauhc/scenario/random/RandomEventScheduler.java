package net.novaproject.novauhc.scenario.random;

import net.novaproject.novauhc.scenario.Scenario;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RandomEventScheduler {

    private final List<RandomGameEvent<?>> events = new ArrayList<>();
    private final Map<RandomGameEvent<?>, Integer> scheduledTasks = new HashMap<>();
    private final Random random = new Random();
    private volatile boolean running = false;
    private JavaPlugin plugin;
    private Scenario scenario;

    public void register(RandomGameEvent<?>... events) {
        this.events.addAll(Arrays.asList(events));
    }

    public List<RandomGameEvent<?>> getEvents() {
        return events;
    }

    @SuppressWarnings("unchecked")
    public <S extends Scenario> void start(S scenario, JavaPlugin plugin) {
        running = true;
        this.plugin = plugin;
        this.scenario = scenario;
        for (RandomGameEvent<?> event : events) {
            ((RandomGameEvent<S>) event).setScenario(scenario);
            if (!event.isEnabled()) continue;
            if (event.isTriggered()) continue;
            scheduleOne(event);
        }
    }

    public boolean arm(Class<? extends RandomGameEvent<?>> clazz) {
        if (!running || plugin == null) return false;
        for (RandomGameEvent<?> event : events) {
            if (!clazz.isInstance(event)) continue;
            if (!event.isEnabled()) return false;
            if (scheduledTasks.containsKey(event)) return false;
            scheduleOne(event);
            return true;
        }
        return false;
    }

    public boolean disarm(Class<? extends RandomGameEvent<?>> clazz) {
        for (RandomGameEvent<?> event : events) {
            if (!clazz.isInstance(event)) continue;
            Integer taskId = scheduledTasks.remove(event);
            if (taskId != null) {
                Bukkit.getScheduler().cancelTask(taskId);
                return true;
            }
        }
        return false;
    }

    private void scheduleOne(RandomGameEvent<?> event) {
        int min = event.getMinGameTime();
        int max = event.getMaxGameTime();
        int range = Math.max(0, max - min);
        long delayTicks = (long) (min + (range > 0 ? random.nextInt(range + 1) : 0)) * 20L;

        int id = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            scheduledTasks.remove(event);
            if (event.canFire() && Math.random() < event.getChance()) {
                event.execute();
            }
            if (event.isRepeating() && event.isEnabled() && running && !event.isTriggered()) {
                scheduleOne(event);
            }
        }, delayTicks).getTaskId();

        scheduledTasks.put(event, id);
    }

    public void stop() {
        running = false;
        scheduledTasks.values().forEach(Bukkit.getScheduler()::cancelTask);
        scheduledTasks.clear();
        plugin = null;
        scenario = null;
    }
}

