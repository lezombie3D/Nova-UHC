package net.novaproject.novauhc.utils.chat;

import com.google.common.base.Strings;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class TextUtils {

    public static final int LORE_WRAP_WIDTH = 40;


    public static String getFormattedTime(int seconds) {
        if (seconds < 0) return "Désactivé";
        return seconds >= 3600
                ? String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60)
                : String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    public static String formatValue(int value, int min) {
        if (value == min) {
            return ChatColor.RED + "Désactivé";
        }
        return ChatColor.GREEN + String.valueOf(value);
    }

    public static String calculPourcentage(double valeur, double total) {
        if (total <= 0) return "0.0%";
        double pourcentage = (valeur / total) * 100;
        return String.format("%.1f%%", pourcentage);
    }

    public static String getProgressBar(int current, int max, int totalBars, String symbol, ChatColor completedColor, ChatColor notCompletedColor) {
        if (max <= 0) max = 1;
        float percent = (float) current / max;
        int progressBars = Math.round(totalBars * percent);
        return Strings.repeat(completedColor + symbol, progressBars)
                + Strings.repeat(notCompletedColor + symbol, totalBars - progressBars);
    }

    public static void sendHoverLine(Player p, String text, String hover) {
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(text));
        component.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(hover).create()
        ));
        p.spigot().sendMessage(component);
    }

    public static List<String> wrap(String text, String prefix) {
        return wrap(text, prefix, LORE_WRAP_WIDTH);
    }

    public static List<String> wrap(String text, String prefix, int width) {
        List<String> out = new ArrayList<>();
        if (prefix == null) prefix = "";
        if (text == null || text.isEmpty()) { out.add(prefix); return out; }

        int usable = Math.max(8, width - visibleLength(prefix));
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        String carryColor = "";

        for (String word : words) {
            if (word.isEmpty()) continue;
            int currentLen = visibleLength(line.toString());
            int addedLen = (currentLen == 0 ? 0 : 1) + visibleLength(word);
            if (currentLen + addedLen > usable && currentLen > 0) {
                out.add(prefix + carryColor + line.toString().trim());
                carryColor = lastColor(carryColor + line);
                line.setLength(0);
            }
            if (line.length() > 0) line.append(' ');
            line.append(word);
        }
        if (line.length() > 0) out.add(prefix + carryColor + line.toString().trim());
        return out;
    }

    private static int visibleLength(String s) {
        if (s == null) return 0;
        int n = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '§' && i + 1 < s.length()) { i++; continue; }
            n++;
        }
        return n;
    }

    private static String lastColor(String s) {
        if (s == null) return "";
        for (int i = s.length() - 2; i >= 0; i--) {
            if (s.charAt(i) == '§') return s.substring(i, i + 2);
        }
        return "";
    }
}

