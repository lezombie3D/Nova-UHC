package net.novaproject.novauhc.debug;

import net.novaproject.novauhc.lobby.RankManager;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.lang.DevLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class DebugLog {

    public static boolean devMode() {
        return Main.get().getConfig().getBoolean("dev", false);
    }

    public enum Channel {
        ABILITIES("abilities", "give/click/tryUse des abilities"),
        EVENTS("events", "events custom Uhc*"),
        SCHEM("schem", "placements de schematics");

        private final String key;
        private final String description;

        Channel(String key, String description) {
            this.key = key;
            this.description = description;
        }

        public String getKey() {
            return key;
        }

        public String getDescription() {
            return description;
        }

        public static Channel fromKey(String key) {
            if (key == null) return null;
            for (Channel channel : values()) {
                if (channel.key.equalsIgnoreCase(key)) return channel;
            }
            return null;
        }
    }

    private static final Set<Channel> ENABLED = EnumSet.noneOf(Channel.class);


    public static boolean isEnabled(Channel channel) {
        return ENABLED.contains(channel);
    }

    public static void setEnabled(Channel channel, boolean value) {
        if (value) ENABLED.add(channel);
        else ENABLED.remove(channel);
        Bukkit.getConsoleSender().sendMessage(LangManager.get().get(DevLang.LOG_CHANNEL_TOGGLED)
                .replace("%channel%", channel.getKey())
                .replace("%state%", LangManager.get().get(value ? DevLang.STATE_ON : DevLang.STATE_OFF)));
    }

    public static void setAll(boolean value) {
        for (Channel channel : Channel.values()) {
            setEnabled(channel, value);
        }
    }

    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    public static void warnOnce(String tag, String msg) {
        if (!WARNED.add(tag + '|' + msg)) return;
        Bukkit.getLogger().warning("[" + tag + "] " + msg);
    }

    public static void warnOnce(String tag, String msg, Throwable error) {
        warnOnce(tag, msg + " : " + error);
    }

    public static void log(Channel channel, String tag, String msg) {
        if (!isEnabled(channel)) return;
        String line = ChatColor.GOLD + "[Debug:" + channel.getKey() + "] " + ChatColor.YELLOW + tag
                + ChatColor.GRAY + " | " + ChatColor.WHITE + msg;
        Main.get().getLogger().info(ChatColor.stripColor(line));
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (RankManager.isStaffOrOp(p)) {
                p.sendMessage(line);
            }
        }
    }

    public static final class AbilityDebug {

        private static final Set<Class<?>> CLONE_CHECKED = ConcurrentHashMap.newKeySet();

        private static boolean isEnabled() {
            return DebugLog.isEnabled(Channel.ABILITIES);
        }

        private static void log(String tag, String msg) {
            DebugLog.log(Channel.ABILITIES, tag, msg);
        }

        public static void onGive(Ability ability, UHCPlayer to, ItemStack item) {
            if (!isEnabled()) return;
            log("onGive", describe(ability) + " → " + playerName(to) + " | item=" + describeItem(item));
        }

        public static void onClickAttempt(Ability ability, String playerName, ItemStack handItem, ItemStack expected, boolean matched) {
            if (!isEnabled()) return;
            log("onClick",
                    describe(ability) + " ← " + playerName
                            + " | hand=" + describeItem(handItem)
                            + " | expected=" + describeItem(expected)
                            + " | match=" + matched);
        }

        public static void onClickSkipped(Ability ability, String playerName, String reason) {
            if (!isEnabled()) return;
            log("onClick.skip", describe(ability) + " ← " + playerName + " | reason=" + reason);
        }

        public static void tryUse(Ability ability, String playerName, String outcome) {
            if (!isEnabled()) return;
            log("tryUse", describe(ability) + " ← " + playerName + " | " + outcome);
        }

        public static void onEnable(Ability ability, String playerName, boolean result) {
            if (!isEnabled()) return;
            log("onEnable", describe(ability) + " ← " + playerName + " | returned=" + result);
        }

        public static void roleVariableClone(Ability cloned, UHCPlayer owner) {
            if (!isEnabled()) return;
            log("roleVar", "cloned " + describe(cloned) + " owner=" + playerName(owner));
        }

        public static void roleVariableSkip(Ability base, String fieldName, String reason) {
            if (!isEnabled()) return;
            log("roleVar", "SKIPPED field '" + fieldName + "' (" + describe(base) + ") — " + reason);
        }

        public static void warnSharedState(Ability ability) {
            Class<?> type = ability.getClass();
            if (!CLONE_CHECKED.add(type)) return;
            for (Class<?> c = type; c != null && c != Ability.class; c = c.getSuperclass()) {
                try {
                    c.getDeclaredMethod("clone");
                    return;
                } catch (NoSuchMethodException ignored) {
                }
            }
            for (Class<?> c = type; c != null && c != Ability.class; c = c.getSuperclass()) {
                for (Field f : c.getDeclaredFields()) {
                    if (Modifier.isStatic(f.getModifiers())) continue;
                    Class<?> t = f.getType();
                    if (Map.class.isAssignableFrom(t) || Collection.class.isAssignableFrom(t)) {
                        String warning = type.getSimpleName()
                                + " : champ d'instance mutable '" + f.getName() + "' (" + t.getSimpleName()
                                + ") — clone() est shallow, cet état est PARTAGÉ entre les joueurs."
                                + " Utilise un static keyé UUID (cleanup onStop) ou override clone().";
                        Bukkit.getLogger().warning("[Ability] " + warning);
                        log("clone", warning);
                    }
                }
            }
        }

        private static String describe(Ability a) {
            if (a == null) return "null";
            return a.getClass().getSimpleName() + "[" + a.getName() + "]";
        }

        private static String playerName(UHCPlayer up) {
            if (up == null) return "null";
            return up.getPlayer() != null ? up.getPlayer().getName() : up.getUniqueId().toString();
        }

        private static String describeItem(ItemStack it) {
            if (it == null) return "null";
            String name = "?";
            if (it.hasItemMeta() && it.getItemMeta().getDisplayName() != null) {
                name = ChatColor.stripColor(it.getItemMeta().getDisplayName());
            }
            return it.getType() + "{name=\"" + name + "\",amt=" + it.getAmount() + "}";
        }
    }
}

