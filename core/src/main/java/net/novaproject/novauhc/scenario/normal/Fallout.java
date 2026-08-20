package net.novaproject.novauhc.scenario.normal;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioLang;
import org.bukkit.Sound;

public class Fallout extends Scenario {

    @Override
    public Family getFamily() { return Family.MONDE; }

    private int lastDamageAt = 0;
    private BukkitRunnable falloutTask;
    private boolean falloutStarted = false;

    @Var(lang = ScenarioVarLang.class, nameKey = "FALLOUT_VAR_FALLOUT_START_TIME_NAME", descKey = "FALLOUT_VAR_FALLOUT_START_TIME_DESC", type = VariableType.TIME)
    private int falloutStartTime = 45 * 60;

    @Var(lang = ScenarioVarLang.class, nameKey = "FALLOUT_VAR_SAFE_YLEVEL_NAME", descKey = "FALLOUT_VAR_SAFE_YLEVEL_DESC", type = VariableType.INTEGER)
    private int safeYLevel = 60;

    @Var(lang = ScenarioVarLang.class, nameKey = "FALLOUT_VAR_BASE_DAMAGE_NAME", descKey = "FALLOUT_VAR_BASE_DAMAGE_DESC", type = VariableType.DOUBLE)
    private double baseDamage = 1.0;

    @Var(name = "Intervalle des dégâts", desc = "Secondes entre deux ticks de radiation.", type = VariableType.TIME, min = 1)
    private int damageIntervalSeconds = 30;

    @Override
    public String getName() {
        return "Fallout";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.FALLOUT, player)
                .replace("%level%", String.valueOf(safeYLevel));
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.SKULL_ITEM).setDurability((short) 1);
    }

    @Override
    public void onGameStart() {
        startFalloutTask();
    }

    private void startFalloutTask() {
        if (falloutTask != null) falloutTask.cancel();

        falloutTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }

                int currentTime = UHCManager.get().getTimer();

                if (!falloutStarted) {
                    sendFalloutWarnings(currentTime);
                    if (currentTime >= falloutStartTime) startFallout();
                    return;
                }
                applyRadiationToExposedPlayers();
            }
        };

        falloutTask.runTaskTimer(Main.get(), 20L, 20L);
    }

    @Override
    public void onStop() {
        if (falloutTask != null) {
            falloutTask.cancel();
            falloutTask = null;
        }
        falloutStarted = false;
    }

    private void startFallout() {
        falloutStarted = true;
        lastDamageAt = UHCManager.get().getTimer();

        LangManager.get().sendAll(ScenarioLang.FALLOUT_RADIATION_START);
        LangManager.get().sendAll(ScenarioLang.FALLOUT_GO_UNDERGROUND, Map.of("%level%", String.valueOf(safeYLevel)));

        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            player.getWorld().playSound(player.getLocation(),
                    Sound.WITHER_SPAWN, 1.0f, 0.5f);
        }
    }

    private void sendFalloutWarnings(int currentTime) {
        int timeUntilFallout = falloutStartTime - currentTime;
        if (timeUntilFallout == 300)
            LangManager.get().sendAll(ScenarioLang.FALLOUT_WARNING_FIVE_MINUTES);
        else if (timeUntilFallout == 60)
            LangManager.get().sendAll(ScenarioLang.FALLOUT_WARNING_ONE_MINUTE, Map.of("%level%", String.valueOf(safeYLevel)));
        else if (timeUntilFallout == 10)
            LangManager.get().sendAll(ScenarioLang.FALLOUT_WARNING_TEN_SECONDS);
    }

    private void applyRadiationToExposedPlayers() {
        int now = UHCManager.get().getTimer();
        if (now - lastDamageAt < Math.max(1, damageIntervalSeconds)) return;
        lastDamageAt = now;

        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            if (player.getLocation().getY() <= safeYLevel) continue;
            player.damage(baseDamage);
            LangManager.get().send(ScenarioLang.FALLOUT_EXPOSED, player);
        }
    }
}

