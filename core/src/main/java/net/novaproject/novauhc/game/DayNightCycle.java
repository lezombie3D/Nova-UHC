package net.novaproject.novauhc.game;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.event.UhcGameEvents.UhcDayEvent;
import net.novaproject.novauhc.event.UhcGameEvents.UhcNightEvent;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class DayNightCycle implements GameSpi.IDayNightCycle {

    private static final DayNightCycle INSTANCE = new DayNightCycle();

    private static final long DAY_END = 12000L;
    private static final long FULL_DAY = 24000L;

    private Boolean lastIsDay = null;
    private double timeAccumulator = 0;
    private boolean customRuleApplied = false;


    public static DayNightCycle get() {
        return INSTANCE;
    }

    public boolean isDay() {
        World world = Common.get().getArena();
        if (world == null) return true;
        return world.getTime() < DAY_END;
    }

    public void tick() {
        World world = Common.get().getArena();
        if (world == null) return;

        UHCManager uhc = UHCManager.get();

        if (uhc.isCustomDayCycle()) {
            if (!customRuleApplied) {
                world.setGameRuleValue("doDaylightCycle", "false");
                customRuleApplied = true;
            }
            advance(world, uhc);
        } else if (customRuleApplied) {
            world.setGameRuleValue("doDaylightCycle", "true");
            customRuleApplied = false;
        }

        boolean day = world.getTime() < DAY_END;
        if (lastIsDay == null) {
            lastIsDay = day;
            return;
        }
        if (day == lastIsDay) return;
        lastIsDay = day;

        if (day) {
            Bukkit.getPluginManager().callEvent(new UhcDayEvent());
            if (uhc.isAnnounceDayNight()) {
                LangManager.get().sendAll(CoreLang.COMMON_DAY_START);
            }
        } else {
            Bukkit.getPluginManager().callEvent(new UhcNightEvent());
            if (uhc.isAnnounceDayNight()) {
                LangManager.get().sendAll(CoreLang.COMMON_NIGHT_START);
            }
        }
    }

    private void advance(World world, UHCManager uhc) {
        boolean day = world.getTime() < DAY_END;
        int phaseDuration = day ? uhc.getDayDuration() : uhc.getNightDuration();
        if (phaseDuration <= 0) phaseDuration = 600;

        timeAccumulator += DAY_END / (double) phaseDuration;
        long increment = (long) timeAccumulator;
        if (increment <= 0) return;
        timeAccumulator -= increment;

        world.setTime((world.getTime() + increment) % FULL_DAY);
    }
}

