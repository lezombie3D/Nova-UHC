package net.novaproject.novauhc.command.cmd.host;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EffectSub extends HostSub {

    private static final String[] EFFECT_NAMES = {
            "speed", "slow", "increase_damage", "weakness", "regeneration", "poison",
            "wither", "fire_resistance", "water_breathing", "invisibility", "night_vision",
            "jump", "damage_resistance", "absorption", "blindness", "confusion", "hunger",
            "fast_digging", "slow_digging", "health_boost", "saturation"
    };

    @Override
    protected void run(Player player, CommandArguments args) {
        if (!UHCManager.get().isGame()) {
            LangManager.get().send(CoreLang.COMMON_DISABLE_ACTION, player);
            return;
        }
        if (args.size() < 2) {
            sendUsage(player);
            return;
        }
        Player target = Bukkit.getPlayer(args.get(0, ""));
        if (target == null) {
            LangManager.get().send(CoreLang.CMD_INVALID_PLAYER, player);
            return;
        }
        UHCPlayer targetUp = UHCPlayerManager.get().getPlayer(target);
        if (targetUp != null && !targetUp.isPlaying()) {
            LangManager.get().send(CoreLang.CMD_EFFECT_NOT_PLAYING, player, Map.of("%player%", target.getName()));
        }
        if (args.get(1, "").equalsIgnoreCase("clear")) {
            clear(player, target, args);
            return;
        }
        apply(player, target, args);
    }

    private void clear(Player player, Player target, CommandArguments args) {
        if (args.has(2)) {
            PotionEffectType type = PotionEffectType.getByName(args.get(2, "").toUpperCase(Locale.ROOT));
            if (type == null) {
                LangManager.get().send(CoreLang.CMD_EFFECT_UNKNOWN, player, Map.of("%effect%", args.get(2, "")));
                return;
            }
            if (!target.hasPotionEffect(type)) {
                LangManager.get().send(CoreLang.CMD_EFFECT_TARGET_HASNT, player, Map.of("%player%", target.getName(), "%effect%", type.getName()));
                return;
            }
            target.removePotionEffect(type);
            LangManager.get().send(CoreLang.CMD_EFFECT_REMOVED, player, Map.of("%effect%", type.getName(), "%player%", target.getName()));
            LangManager.get().send(CoreLang.COMMON_HOST_EFFECT_REMOVED, target, Map.of("%effect%", type.getName()));
            return;
        }
        for (PotionEffect effect : new ArrayList<>(target.getActivePotionEffects())) {
            target.removePotionEffect(effect.getType());
        }
        LangManager.get().send(CoreLang.CMD_EFFECT_CLEARED, player, Map.of("%player%", target.getName()));
        LangManager.get().send(CoreLang.COMMON_HOST_EFFECT_CLEARED, target);
    }

    private void apply(Player player, Player target, CommandArguments args) {
        PotionEffectType type = PotionEffectType.getByName(args.get(1, "").toUpperCase(Locale.ROOT));
        if (type == null) {
            LangManager.get().send(CoreLang.CMD_EFFECT_UNKNOWN, player, Map.of("%effect%", args.get(1, "")));
            return;
        }
        int seconds = args.getInt(2, 0);
        if (seconds <= 0) {
            LangManager.get().send(CoreLang.CMD_EFFECT_INVALID_DURATION, player);
            return;
        }
        int level = args.getInt(3, 1);
        if (level < 1) {
            LangManager.get().send(CoreLang.CMD_EFFECT_INVALID_LEVEL, player);
            return;
        }
        target.addPotionEffect(new PotionEffect(type, seconds * 20, level - 1), true);
        LangManager.get().send(CoreLang.CMD_EFFECT_APPLIED, player,
                Map.of("%effect%", type.getName(), "%level%", level, "%player%", target.getName(), "%seconds%", seconds));
        LangManager.get().send(CoreLang.COMMON_HOST_EFFECT_APPLIED, target, Map.of(
                "%effect%", type.getName(),
                "%level%", String.valueOf(level),
                "%seconds%", String.valueOf(seconds)));
    }

    private void sendUsage(Player player) {
        LangManager.get().send(CoreLang.CMD_EFFECT_USAGE, player);
        LangManager.get().send(CoreLang.CMD_EFFECT_USAGE_CLEAR, player);
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        if (args.size() == 1) return getOnlinePlayers(args);
        if (args.size() == 2) {
            List<String> options = new ArrayList<>();
            options.add("clear");
            options.addAll(Arrays.asList(EFFECT_NAMES));
            return getStrings(args, options.toArray(new String[0]));
        }
        if (args.size() == 3) {
            if (args.get(1, "").equalsIgnoreCase("clear")) return getStrings(args, EFFECT_NAMES);
            return getNumbers(args, 10, 30, 60, 120);
        }
        if (args.size() == 4 && !args.get(1, "").equalsIgnoreCase("clear")) {
            return getNumbers(args, 1, 2, 3);
        }
        return List.of();
    }
}
