package net.novaproject.novauhc.ability;

import net.novaproject.novauhc.ability.template.PassiveAbility;
import net.novaproject.novauhc.debug.DebugLog.AbilityDebug;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.event.UhcAbilityEvents.UhcAbilityUseEvent;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;
import net.novaproject.novauhc.utils.chat.TextUtils;
import net.novaproject.novauhc.utils.cooldown.CooldownService;
import net.novaproject.novauhc.utils.item.ItemCreator.AbilityNbt;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class AbilityManager implements AbilitySpi.IAbilityManager {

    private static final AbilityManager INSTANCE = new AbilityManager();

    public static AbilityManager get() {
        return INSTANCE;
    }



    public Set<Ability> abilitiesOf(Player owner) {
        UHCPlayer up = owner == null ? null : UHCPlayerManager.get().getPlayer(owner);
        Role role = up == null ? null : KPIBuilder.roleOf(up);
        return role == null ? Collections.emptySet() : role.getAbilities();
    }

    public <H> void dispatch(Iterable<Ability> abilities, Class<H> hook, Predicate<Ability> gate, Consumer<H> call) {
        List<Ability> snapshot = new ArrayList<>();
        for (Ability a : abilities) snapshot.add(a);
        for (Ability a : snapshot) {
            if (gate != null && !gate.test(a)) continue;
            if (hook.isInstance(a)) call.accept(hook.cast(a));
        }
    }

    public <H> void fire(Player owner, Class<H> hook, Consumer<H> call) {
        dispatch(abilitiesOf(owner), hook, Ability::isUsable, call);
    }

    public <H> void fireGlobal(Class<H> hook, Consumer<H> call) {
        for (UHCPlayer up : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Role role = KPIBuilder.roleOf(up);
            if (role != null) dispatch(role.getAbilities(), hook, Ability::isUsable, call);
        }
    }

    public boolean isAbilityItem(ItemStack item) {
        return AbilityNbt.isAbilityItem(item);
    }

    public boolean tryUse(Ability ability, Player player) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        long remainingMs = CooldownService.get(player, ability.getName() + "Cooldown");
        if (remainingMs != -1) {
            deny(ability, player, CoreLang.COMMON_ABILITY_ON_COOLDOWN,
                    Map.of("%ability%", ability.getName(), "%time%", String.valueOf((remainingMs + 999) / 1000)));
            AbilityDebug.tryUse(ability, player.getName(), "BLOCKED cooldown=" + remainingMs + "ms");
            return false;
        }
        if (!uhcPlayer.isPlaying()) {
            AbilityDebug.tryUse(ability, player.getName(), "BLOCKED not playing");
            return false;
        }
        if (!ability.active() || ability.isDisabled() || ability.isStolen()) {
            deny(ability, player, CoreLang.COMMON_ABILITY_DISABLED, Map.of("%ability%", ability.getName()));
            AbilityDebug.tryUse(ability, player.getName(),
                    "BLOCKED " + (!ability.active() ? "inactive" : ability.isStolen() ? "stolen" : "disabled"));
            return false;
        }

        if (!(ability instanceof PassiveAbility)) {
            UhcAbilityUseEvent useEvent = new UhcAbilityUseEvent(player, ability);
            Bukkit.getPluginManager().callEvent(useEvent);
            if (useEvent.isCancelled()) {
                AbilityDebug.tryUse(ability, player.getName(), "BLOCKED event cancelled");
                return false;
            }
        }

        if (ability.getMaxUse() == 0) {
            deny(ability, player, CoreLang.COMMON_ABILITY_MAX_USE_REACHED, Map.of("%ability%", ability.getName()));
            AbilityDebug.tryUse(ability, player.getName(), "BLOCKED maxUse=0 exhausted");
            return false;
        }

        boolean enabled = ability.onEnable(player);
        if (enabled || !(ability instanceof PassiveAbility)) {
            AbilityDebug.onEnable(ability, player.getName(), enabled);
        }
        if (!enabled) return false;

        int cd = Math.max(0, ability.getCooldown());
        if (ability.getMaxUse() > -1) ability.decrementMaxUse();
        if (cd > 0) {
            CooldownService.put(player, ability.getName() + "Cooldown", cd * 1000L);
            ability.startCooldownDisplay(player, cd);
        }
        AbilityDebug.tryUse(ability, player.getName(),
                ability.getMaxUse() < 0 ? "OK maxUse=∞ cd=" + cd + "s"
                        : "OK maxUse left=" + ability.getMaxUse() + " cd=" + cd + "s");
        ability.fireUsed(player);
        return true;
    }

    private void deny(Ability ability, Player player, CoreLang key, Map<String, Object> placeholders) {
        if (ability.isManualAbility()) {
            LangManager.get().send(key, player, placeholders);
        }
    }

    public static final class CooldownDisplayService {

        private static final long ONE_SHOT_THRESHOLD_MS = 24L * 3600L * 1000L;
        private static final Map<UUID, Map<String, String>> activeCooldowns = new HashMap<>();

        private static BukkitTask task;

        public static void start(JavaPlugin plugin) {
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    for (UHCPlayer up : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                        Player player = up.getPlayer();
                        if (player == null || !player.isOnline()) continue;
                        tick(player, up);
                    }
                }
            }.runTaskTimer(plugin, 1L, 1L);
        }

        public static void stop() {
            if (task != null) {
                task.cancel();
                task = null;
            }
            activeCooldowns.clear();
        }

        private static void tick(Player player, UHCPlayer up) {
            Role role = KPIBuilder.roleOf(up);
            if (role == null) {
                activeCooldowns.remove(player.getUniqueId());
                return;
            }

            Map<String, String> current = new HashMap<>();
            for (Ability ability : role.getAbilities()) {
                if (!ability.isManualAbility()) continue;
                String key = ability.activeCooldownKey(player);
                if (key == null) continue;
                long leftMs = CooldownService.get(player, key);
                if (leftMs > 0 && leftMs <= ONE_SHOT_THRESHOLD_MS) {
                    current.put(key, ability.activeCooldownDisplayName(player));
                }
            }

            Map<String, String> previous = activeCooldowns.getOrDefault(player.getUniqueId(), Collections.emptyMap());
            for (Map.Entry<String, String> e : previous.entrySet()) {
                if (!current.containsKey(e.getKey())) {
                    sendReady(player, e.getValue());
                }
            }
            if (current.isEmpty()) activeCooldowns.remove(player.getUniqueId());
            else activeCooldowns.put(player.getUniqueId(), current);

            ItemStack hand = player.getItemInHand();
            if (hand == null) return;
            for (Ability ability : role.getAbilities()) {
                if (!ability.isAbilityItem(hand)) continue;
                String key = ability.activeCooldownKey(player);
                if (key == null) return;
                long leftMs = CooldownService.get(player, key);
                if (leftMs <= 0 || leftMs > ONE_SHOT_THRESHOLD_MS) return;
                int leftSec = (int) ((leftMs + 999) / 1000);
                int total = Math.max(ability.activeCooldownTotalSeconds(player), leftSec);
                sendBar(player, ability.activeCooldownDisplayName(player), ability.activeCooldownMaxUse(player), total, leftSec);
                return;
            }
        }

        private static void sendReady(Player player, String displayName) {
            DisplayService.actionBar(player,
                    LangManager.get().get(CoreLang.COMMON_ABILITY_READY, player,
                            Map.of("%ability%", displayName)));
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 0.6f, 1.4f);
        }

        private static void sendBar(Player player, String displayName, int maxUse, int total, int left) {
            int percent = total <= 0 ? 100 : (int) Math.round((total - left) * 100.0 / total);
            percent = Math.max(0, Math.min(100, percent));
            int filled = Math.min(10, percent / 10);
            StringBuilder bar = new StringBuilder("§a");
            for (int i = 0; i < filled; i++) bar.append("|");
            bar.append("§8");
            for (int i = filled; i < 10; i++) bar.append("|");

            String uses = maxUse < 0 ? "∞" : String.valueOf(maxUse);
            DisplayService.actionBar(player,
                    LangManager.get().get(CoreLang.COMMON_ABILITY_COOLDOWN_BAR, player,
                            Map.of("%ability%", displayName,
                                    "%time%", TextUtils.getFormattedTime(left),
                                    "%bar%", bar.toString(),
                                    "%percent%", String.valueOf(percent),
                                    "%uses%", uses)));
        }
    }
}
