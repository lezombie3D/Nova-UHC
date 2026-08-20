package net.novaproject.novauhc.command.cmd.host;

import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.random.RandomEventScheduler;
import net.novaproject.novauhc.scenario.random.RandomGameEvent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ForceEventSub extends HostSub {

    @Override
    protected void run(Player player, CommandArguments args) {
        if (args.size() < 1) {
            LangManager.get().send(CoreLang.CMD_FORCEEVENT_USAGE, player);
            return;
        }
        String want = args.join(0).toLowerCase().replace('_', ' ').trim();
        for (Scenario s : ScenarioManager.get().getActiveScenarios()) {
            RandomEventScheduler sched = s.getEventScheduler();
            if (sched == null) continue;
            for (RandomGameEvent<?> e : sched.getEvents()) {
                String name = e.getName().toLowerCase();
                if (name.equals(want) || name.startsWith(want)) {
                    try {
                        e.execute();
                        LangManager.get().send(CoreLang.CMD_FORCEEVENT_TRIGGERED, player, Map.of("%event%", e.getName()));
                    } catch (Throwable t) {
                        LangManager.get().send(CoreLang.CMD_FORCEEVENT_ERROR, player, Map.of("%error%", String.valueOf(t.getMessage())));
                    }
                    return;
                }
            }
        }
        LangManager.get().send(CoreLang.CMD_FORCEEVENT_NOT_FOUND, player, Map.of("%event%", want));
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        if (args.size() != 1) return List.of();
        List<String> names = new ArrayList<>();
        for (Scenario s : ScenarioManager.get().getActiveScenarios()) {
            RandomEventScheduler sched = s.getEventScheduler();
            if (sched == null) continue;
            for (RandomGameEvent<?> e : sched.getEvents()) {
                names.add(e.getName().replace(' ', '_'));
            }
        }
        return getStrings(args, names.toArray(new String[0]));
    }
}

