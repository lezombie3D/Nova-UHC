package net.novaproject.novauhc.display.scoreboard;

import net.minecraft.server.v1_8_R3.IScoreboardCriteria;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardObjective;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardScore;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_8_R3.Scoreboard;
import net.minecraft.server.v1_8_R3.ScoreboardObjective;
import net.minecraft.server.v1_8_R3.ScoreboardScore;
import net.minecraft.server.v1_8_R3.ScoreboardTeam;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class NovaBoard {

    public static final int MAX_LINES = 15;
    private static final int MAX_TITLE_LENGTH = 32;
    private static final int PREFIX_LENGTH = 16;
    private static final int ENTRY_LENGTH = 40;
    private static final int SUFFIX_LENGTH = 16;
    private static final String OBJECTIVE_NAME = "nova_sb";
    private static final char[] UNIQUE = "0123456789abcdef".toCharArray();
    private String baseTitle = "";
    private final Player player;
    private final Scoreboard backing = new Scoreboard();
    private final ScoreboardObjective objective;
    private final List<String> currentLines = new ArrayList<>();
    private final List<String[]> currentSplits = new ArrayList<>();
    private String currentTitle = "";
    private boolean deleted = false;

    private BlinkEffect titleBlink = null;

    public NovaBoard(Player player) {
        this.player = player;
        this.objective = new ScoreboardObjective(backing, OBJECTIVE_NAME, IScoreboardCriteria.b);
        this.objective.setDisplayName("");
        sendPacket(new PacketPlayOutScoreboardObjective(objective, 0));
        sendPacket(new PacketPlayOutScoreboardDisplayObjective(1, objective));
    }

    public Player getPlayer() {
        return player;
    }

    public void setTitle(String coloredTitle) {

        if (deleted) {
            return;
        }

        coloredTitle = coloredTitle == null ? "" : coloredTitle;

        if (coloredTitle.equals(baseTitle)) {
            return;
        }

        baseTitle = coloredTitle;

        titleBlink = new BlinkEffect(coloredTitle);

        updateTitle(titleBlink.getText());
    }

    public void tick() {
        if (deleted || titleBlink == null) return;
        titleBlink.next();
        updateTitle(titleBlink.getText());
    }

    public void updateTitle(String title) {
        if (deleted) return;
        String t = cut(title == null ? "" : title, MAX_TITLE_LENGTH);
        if (t.equals(currentTitle)) return;
        currentTitle = t;
        objective.setDisplayName(t);
        sendPacket(new PacketPlayOutScoreboardObjective(objective, 2));
    }

    public void updateLines(List<String> lines) {
        if (deleted) return;
        List<String> target = new ArrayList<>(lines == null ? Collections.emptyList() : lines);
        if (target.size() > MAX_LINES) target = new ArrayList<>(target.subList(0, MAX_LINES));
        for (int i = 0; i < target.size(); i++) {
            if (target.get(i) == null) target.set(i, "");
        }

        if (target.size() != currentLines.size()) {
            clearAllLines();
            currentLines.clear();
            for (int i = 0; i < target.size(); i++) {
                createLine(i, target.get(i), target.size() - i);
                currentLines.add(target.get(i));
            }
            return;
        }

        for (int i = 0; i < target.size(); i++) {
            if (target.get(i).equals(currentLines.get(i))) continue;
            updateLine(i, target.get(i), target.size() - i);
            currentLines.set(i, target.get(i));
        }
    }

    public void delete() {
        if (deleted) return;
        clearAllLines();
        currentLines.clear();
        titleBlink = null;
        sendPacket(new PacketPlayOutScoreboardObjective(objective, 1));
        deleted = true;
    }

    private void createLine(int index, String text, int score) {
        String[] split = splitLine(text, index);
        while (currentSplits.size() <= index) currentSplits.add(null);
        currentSplits.set(index, split);

        ScoreboardTeam team = new ScoreboardTeam(backing, teamName(index));
        team.setPrefix(split[0]);
        team.setSuffix(split[2]);
        team.getPlayerNameSet().add(split[1]);
        sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
        sendScore(split[1], score);
    }

    private void updateLine(int index, String text, int score) {
        String[] old = currentSplits.get(index);
        String[] neu = splitLine(text, index);
        currentSplits.set(index, neu);

        ScoreboardTeam team = new ScoreboardTeam(backing, teamName(index));
        team.setPrefix(neu[0]);
        team.setSuffix(neu[2]);

        if (old != null && old[1].equals(neu[1])) {
            sendPacket(new PacketPlayOutScoreboardTeam(team, 2));
            return;
        }

        if (old != null) {
            sendPacket(new PacketPlayOutScoreboardTeam(new ScoreboardTeam(backing, teamName(index)), 1));
            removeScore(old[1]);
        }
        team.getPlayerNameSet().add(neu[1]);
        sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
        sendScore(neu[1], score);
    }

    private void clearAllLines() {
        for (int i = 0; i < currentSplits.size(); i++) {
            String[] split = currentSplits.get(i);
            if (split == null) continue;
            sendPacket(new PacketPlayOutScoreboardTeam(new ScoreboardTeam(backing, teamName(i)), 1));
            removeScore(split[1]);
        }
        currentSplits.clear();
    }

    private void sendScore(String entry, int value) {
        ScoreboardScore score = new ScoreboardScore(backing, objective, entry);
        score.setScore(value);
        sendPacket(new PacketPlayOutScoreboardScore(score));
    }

    private void removeScore(String entry) {
        sendPacket(new PacketPlayOutScoreboardScore(entry));
    }

    private void sendPacket(Packet<?> packet) {
        if (player == null || !player.isOnline()) return;
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    private String teamName(int index) {
        return "nova_l" + index;
    }

    public static String[] splitLine(String text, int index) {
        String raw = text == null ? "" : text;
        String prefix = cut(raw, PREFIX_LENGTH);
        String rest = raw.substring(prefix.length());

        String tag = "§" + UNIQUE[index % UNIQUE.length];
        String carryEntry = prefix.isEmpty() ? "" : ChatColor.getLastColors(prefix);
        int entryBudget = Math.max(ENTRY_LENGTH - tag.length() - carryEntry.length(), 0);
        String entryVisible = cut(rest, entryBudget);
        String entry = carryEntry + entryVisible + tag;
        rest = rest.substring(entryVisible.length());

        String suffix = "";
        if (!rest.isEmpty()) {
            String carrySuffix = ChatColor.getLastColors(prefix + entryVisible);
            suffix = cut(carrySuffix + rest, SUFFIX_LENGTH);
        }
        return new String[]{prefix, entry, suffix};
    }

    private static String cut(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        String out = s.substring(0, max);
        if (!out.isEmpty() && out.charAt(out.length() - 1) == '§') {
            out = out.substring(0, out.length() - 1);
        }
        return out;
    }

    public static final class BlinkEffect {

        private static final Map<Character, Character> HIGHLIGHT_MAP = new HashMap<>();
        static {
            HIGHLIGHT_MAP.put('0', '8');
            HIGHLIGHT_MAP.put('1', '9');
            HIGHLIGHT_MAP.put('2', 'a');
            HIGHLIGHT_MAP.put('3', 'b');
            HIGHLIGHT_MAP.put('4', 'c');
            HIGHLIGHT_MAP.put('5', 'd');
            HIGHLIGHT_MAP.put('6', 'e');
            HIGHLIGHT_MAP.put('7', 'f');
            HIGHLIGHT_MAP.put('8', '7');
            HIGHLIGHT_MAP.put('9', 'f');
            HIGHLIGHT_MAP.put('a', 'f');
            HIGHLIGHT_MAP.put('b', 'f');
            HIGHLIGHT_MAP.put('c', 'f');
            HIGHLIGHT_MAP.put('d', 'f');
            HIGHLIGHT_MAP.put('e', 'f');
            HIGHLIGHT_MAP.put('f', '7');
        }

        private int count = 0;
        private boolean back = false;
        private final List<Segment> segments;
        private final List<String> frames;

        public BlinkEffect(String coloredText) {
            this.segments = parse(coloredText == null ? "" : coloredText);
            this.frames = generateFrames(this.segments);
        }

        private record Segment(String color, char ch) {
        }

        private static List<Segment> parse(String text) {
            List<Segment> list = new ArrayList<>();
            String currentColor = "";
            int i = 0;
            while (i < text.length()) {
                if (text.charAt(i) == '§' && i + 1 < text.length()) {
                    char code = Character.toLowerCase(text.charAt(i + 1));
                    if ("0123456789abcdef".indexOf(code) >= 0) {
                        currentColor = "§" + code;
                    } else if ("lonmk".indexOf(code) >= 0) {
                        currentColor += "§" + code;
                    } else if (code == 'r') {
                        currentColor = "";
                    }
                    i += 2;
                } else {
                    list.add(new Segment(currentColor, text.charAt(i)));
                    i++;
                }
            }
            return list;
        }

        private static List<String> generateFrames(List<Segment> segs) {
            List<String> frames = new ArrayList<>();
            int len = segs.size();
            if (len == 0 || len > 30) {
                frames.add(rebuild(segs, -1));
                return frames;
            }
            for (int i = 0; i < len; i++) {
                frames.add(rebuild(segs, i));
            }
            frames.add(rebuild(segs, -1));
            return frames;
        }

        private static String rebuild(List<Segment> segs, int highlightIndex) {
            StringBuilder sb = new StringBuilder();
            String lastColor = null;
            for (int i = 0; i < segs.size(); i++) {
                Segment s = segs.get(i);
                String color = (i == highlightIndex) ? deriveHighlight(s.color) : s.color;
                if (!color.equals(lastColor)) {
                    sb.append(color);
                    lastColor = color;
                }
                sb.append(s.ch);
            }
            return sb.toString();
        }

        private static String deriveHighlight(String colorCode) {
            if (colorCode == null || colorCode.isEmpty()) return "§f";
            char colorChar = '6';
            StringBuilder formats = new StringBuilder();
            for (int i = 0; i + 1 < colorCode.length(); i++) {
                if (colorCode.charAt(i) == '§') {
                    char code = Character.toLowerCase(colorCode.charAt(++i));
                    if ("0123456789abcdef".indexOf(code) >= 0) {
                        colorChar = code;
                    } else if ("lonmk".indexOf(code) >= 0) {
                        formats.append('§').append(code);
                    }
                }
            }
            char hl = HIGHLIGHT_MAP.getOrDefault(colorChar, 'f');
            return "§" + hl + formats;
        }

        public void next() {
            if (frames.size() <= 1) return;
            if (count >= frames.size() - 1) back = true;
            if (count <= 0) back = false;
            if (!back) count++; else count--;
        }

        public String getText() {
            if (frames.isEmpty()) return "";
            return frames.get(Math.min(count, frames.size() - 1));
        }
    }
}

