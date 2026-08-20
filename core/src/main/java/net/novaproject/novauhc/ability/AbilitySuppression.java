package net.novaproject.novauhc.ability;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.PlayerSpi.IUHCPlayer;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;
import net.novaproject.novauhc.utils.chat.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class AbilitySuppression {

    private static final Map<UUID, Suppression> ACTIVE = new HashMap<>();

    private static final AbilitySpi.IAbilitySuppression INSTANCE = new AbilitySpi.IAbilitySuppression() {
        @Override public int suppress(IUHCPlayer target, int seconds) { return AbilitySuppression.suppress((UHCPlayer) target, seconds); }
        @Override public int suppress(IUHCPlayer target, int seconds, boolean announce) { return AbilitySuppression.suppress((UHCPlayer) target, seconds, announce); }
        @Override public void restore(UUID uuid) { AbilitySuppression.restore(uuid); }
        @Override public boolean isSuppressed(UUID uuid) { return AbilitySuppression.isSuppressed(uuid); }
        @Override public int remainingSeconds(UUID uuid) { return AbilitySuppression.remainingSeconds(uuid); }
        @Override public void clearAll() { AbilitySuppression.clearAll(); }
    };

    public static AbilitySpi.IAbilitySuppression instance() { return INSTANCE; }

    public static int suppress(UHCPlayer target, int seconds) {
        return suppress(target, seconds, true);
    }

    public static int suppress(UHCPlayer target, int seconds, boolean announce) {
        if (target == null || seconds <= 0) return 0;
        Role role = KPIBuilder.roleOf(target);
        if (role == null) return 0;
        UUID uuid = target.getUuid();

        Suppression suppression = ACTIVE.get(uuid);
        if (suppression == null) {
            suppression = new Suppression();
            ACTIVE.put(uuid, suppression);
        }

        int suppressed = 0;
        for (Ability ability : role.getAbilities()) {
            if (!ability.canBeDisabled()) continue;
            if (ability.isDisabled()) continue;
            ability.setDisabled(true);
            suppression.abilities.add(ability);
            suppressed++;
        }

        long until = System.currentTimeMillis() + seconds * 1000L;
        if (until > suppression.untilMs) {
            suppression.untilMs = until;
            if (suppression.task != null) suppression.task.cancel();
            suppression.task = Bukkit.getScheduler().runTaskLater(Main.get(),
                    () -> restore(uuid, announce), seconds * 20L);
        }

        Player player = target.getPlayer();
        if (announce && player != null && player.isOnline()) {
            int remaining = (int) Math.max(1, (suppression.untilMs - System.currentTimeMillis()) / 1000);
            LangManager.get().send(CoreLang.COMMON_SUPPRESSED, player,
                    Map.of("%time%", TextUtils.getFormattedTime(remaining)));
        }
        return suppressed;
    }

    public static void restore(UUID uuid) {
        restore(uuid, false);
    }

    public static boolean isSuppressed(UUID uuid) {
        Suppression suppression = ACTIVE.get(uuid);
        return suppression != null && suppression.untilMs > System.currentTimeMillis();
    }

    public static int remainingSeconds(UUID uuid) {
        Suppression suppression = ACTIVE.get(uuid);
        if (suppression == null) return 0;
        return (int) Math.max(0, (suppression.untilMs - System.currentTimeMillis() + 999) / 1000);
    }

    public static void clearAll() {
        for (UUID uuid : new HashSet<>(ACTIVE.keySet())) {
            restore(uuid, false);
        }
        ACTIVE.clear();
    }

    private static void restore(UUID uuid, boolean announce) {
        Suppression suppression = ACTIVE.remove(uuid);
        if (suppression == null) return;
        if (suppression.task != null) suppression.task.cancel();

        for (Ability ability : suppression.abilities) {
            if (FormSet.isFormDisabled(uuid, ability)) continue;
            ability.setDisabled(false);
        }

        if (announce) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                LangManager.get().send(CoreLang.COMMON_SUPPRESSION_END, player);
            }
        }
    }

    private static final class Suppression {

        private final Set<Ability> abilities = new HashSet<>();
        private long untilMs;
        private BukkitTask task;
    }
}
