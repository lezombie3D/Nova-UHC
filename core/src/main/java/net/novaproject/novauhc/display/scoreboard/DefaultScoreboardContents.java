package net.novaproject.novauhc.display.scoreboard;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;

public class DefaultScoreboardContents implements ScoreboardService.ScoreboardContents {

    private int cycle = 0;

    @Override
    public void tick() {
        cycle += 20;
    }

    @Override
    public String title(Player player) {
        LangManager lm = LangManager.get();
        UHCManager uhc = UHCManager.get();
        if (showKills(uhc)) return lm.get(CoreLang.SCOREBOARD_SB_KILLS_TITLE, player);
        if (uhc.isLobby()) return lm.get(CoreLang.SCOREBOARD_SB_LOBBY_TITLE, player);
        if (uhc.isGame()) {
            if (isSpectator(player)) return lm.get(CoreLang.SCOREBOARD_SB_SPEC_TITLE, player);
            if (compositionPageIndex() > 0) return lm.get(CoreLang.SCOREBOARD_SB_COMPO_TITLE, player);
            return lm.get(isRoleBoard() ? CoreLang.SCOREBOARD_SB_GAME_ROLE_TITLE : CoreLang.SCOREBOARD_SB_GAME_TITLE, player);
        }
        return lm.get(CoreLang.SCOREBOARD_SB_END_TITLE, player);
    }

    @Override
    public List<String> lines(Player player) {
        UHCManager uhc = UHCManager.get();
        if (showKills(uhc)) return killsLines(player);

        LangManager lm = LangManager.get();
        String ip = Common.get().getServerIp();

        if (uhc.isLobby()) {
            return langLines(lm, player, ip, CoreLang.SCOREBOARD_SB_LOBBY_LINES);
        }

        if (uhc.isGame()) {
            if (isSpectator(player)) return spectatorLines(lm, player);
            int compoPage = compositionPageIndex();
            if (compoPage > 0) {
                List<List<String>> pages = compositionPages();
                if (compoPage <= pages.size()) return pages.get(compoPage - 1);
            }
            List<String> out;
            if (isRoleBoard()) {
                out = langLines(lm, player, ip, CoreLang.SCOREBOARD_SB_GAME_ROLE_LINES);
            } else {
                out = langLines(lm, player, ip, CoreLang.SCOREBOARD_SB_GAME_LINES);
            }
            List<String> extra = ScoreboardService.collectExtraLines(player);
            if (!extra.isEmpty()) {
                int insertAt = Math.max(0, out.size() - 2);
                out.addAll(insertAt, extra);
                out.add(insertAt, "");
            }
            return out;
        }

        return langLines(lm, player, ip, CoreLang.SCOREBOARD_SB_END_LINES);
    }

    @Override
    public String tabHeader(Player player) {
        if (!showTab()) return null;
        LangManager lm = LangManager.get();
        UHCManager uhc = UHCManager.get();
        if (uhc.isLobby()) return lm.get(CoreLang.SCOREBOARD_TAB_LOBBY_HEADER, player);
        if (uhc.isGame()) return lm.get(CoreLang.SCOREBOARD_TAB_GAME_HEADER, player);
        return lm.get(CoreLang.SCOREBOARD_TAB_END_HEADER, player);
    }

    @Override
    public String tabFooter(Player player) {
        if (!showTab()) return null;
        LangManager lm = LangManager.get();
        UHCManager uhc = UHCManager.get();
        if (uhc.isLobby()) return lm.get(CoreLang.SCOREBOARD_TAB_LOBBY_FOOTER, player);
        if (uhc.isGame()) {
            String footer = lm.get(CoreLang.SCOREBOARD_TAB_GAME_FOOTER, player);
            UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
            return uhcPlayer == null ? footer : footer + buildEffectFooter(uhcPlayer);
        }
        return lm.get(CoreLang.SCOREBOARD_TAB_END_FOOTER, player);
    }

    private boolean showKills(UHCManager uhc) {
        if (!uhc.isShowKillScoreboard()) return false;
        if (!uhc.isLobby() && !uhc.isGame()) return true;
        return uhc.isGame() && cycle % 200 >= 100;
    }

    private boolean showTab() {
        return UHCManager.get().isShowTabInfo();
    }

    private ScenarioRole<?> openCompositionScenario() {
        return ScenarioManager.get().getActiveSpecialScenarios().stream()
                .filter(s -> s instanceof ScenarioRole)
                .map(s -> (ScenarioRole<?>) s)
                .filter(ScenarioRole::isCompositionOpen)
                .filter(s -> !s.getPlayersRoles().isEmpty())
                .findFirst()
                .orElse(null);
    }

    private int compositionPageIndex() {
        ScenarioRole<?> scenario = openCompositionScenario();
        if (scenario == null) return 0;
        int pages = compositionPages().size();
        if (pages == 0) return 0;
        return (cycle / 100) % (pages + 1);
    }

    private List<List<String>> compositionPages() {
        ScenarioRole<?> scenario = openCompositionScenario();
        if (scenario == null) return new ArrayList<>();

        List<String> alive = new ArrayList<>();
        List<String> dead = new ArrayList<>();
        int alivePlayers = 0;
        for (Map.Entry<UHCPlayer, ? extends Role> entry : scenario.getPlayersRoles().entrySet()) {
            Role role = entry.getValue();
            String color = role.getCamp() != null ? role.getCamp().getColor() : "§f";
            if (entry.getKey().isStillInGame()) alivePlayers++;
            if (role.isCompositionAlive()) {
                alive.add(color + role.getName());
            } else {
                dead.add("§8§m" + ChatColor.stripColor(color + role.getName()));
            }
        }
        alive.sort(String.CASE_INSENSITIVE_ORDER);
        dead.sort(String.CASE_INSENSITIVE_ORDER);

        List<String> entries = new ArrayList<>(alive);
        entries.addAll(dead);
        if (entries.isEmpty()) return new ArrayList<>();

        int perPage = 10;
        int totalPages = (entries.size() + perPage - 1) / perPage;
        List<List<String>> pages = new ArrayList<>();
        for (int p = 0; p < totalPages; p++) {
            List<String> page = new ArrayList<>();
            page.add("");
            int from = p * perPage;
            int to = Math.min(from + perPage, entries.size());
            page.addAll(entries.subList(from, to));
            page.add("");
            page.add("§7Vivants : §a" + alivePlayers + " §8| §7Page §f" + (p + 1) + "§8/§f" + totalPages);
            pages.add(page);
        }
        return pages;
    }

    private boolean isSpectator(Player player) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        return uhcPlayer != null && !uhcPlayer.isPlaying();
    }

    private List<String> spectatorLines(LangManager lm, Player player) {
        List<String> lines = new ArrayList<>();
        lines.add("");
        lines.add(lm.get(CoreLang.SCOREBOARD_SB_SPEC_ALIVE, player));
        lines.add(lm.get(CoreLang.SCOREBOARD_SB_SPEC_TIMER, player));
        lines.add(lm.get(CoreLang.SCOREBOARD_SB_SPEC_BORDER, player));
        lines.add("");
        lines.add(lm.get(CoreLang.SCOREBOARD_SB_SPEC_HINT, player));
        lines.add("");
        return lines;
    }

    private boolean isRoleBoard() {
        return ScenarioManager.get().getActiveSpecialScenarios().stream()
                .anyMatch(s -> s instanceof ScenarioRole && ((ScenarioRole<?>) s).showRoleScoreboard());
    }

    private List<String> killsLines(Player player) {
        LangManager lm = LangManager.get();
        List<UHCPlayer> sorted = UHCPlayerManager.get().getOnlineUHCPlayers().stream()
                .sorted(Comparator.comparingInt(UHCPlayer::getKill).reversed())
                .limit(10)
                .collect(Collectors.toList());

        List<String> lines = new ArrayList<>();
        lines.add("");
        if (sorted.isEmpty()) {
            lines.add(lm.get(CoreLang.SCOREBOARD_SB_KILLS_EMPTY, player));
        } else {
            String[] medals = {"§6⭐", "§7✦", "§c✦"};
            for (int rank = 0; rank < sorted.size(); rank++) {
                UHCPlayer p = sorted.get(rank);
                Player target = p.getPlayer();
                if (target == null) continue;
                String prefix = rank < medals.length ? medals[rank] : "§8" + (rank + 1) + ".";
                lines.add(prefix + " §f" + target.getName() + " §8- §e" + p.getKill() + " kill" + (p.getKill() > 1 ? "s" : ""));
            }
        }
        lines.add("");
        return lines;
    }

    private List<String> langLines(LangManager lm, Player player, String ip, CoreLang key) {
        return lm.getLines(key, player).stream()
                .map(line -> line.replace("<ip>", ip))
                .collect(Collectors.toList());
    }

    private String buildEffectFooter(UHCPlayer uhcPlayer) {
        double strength;
        double speed;
        double resistance;
        String source;

        ScenarioRole<?> activeScenarioRole = ScenarioManager.get()
                .getActiveSpecialScenarios()
                .stream()
                .filter(s -> s instanceof ScenarioRole)
                .map(s -> (ScenarioRole<?>) s)
                .findFirst()
                .orElse(null);

        Role role = activeScenarioRole == null ? null : activeScenarioRole.getRoleByUHCPlayer(uhcPlayer);

        if (role != null) {
            strength   = role.getStrengthPercent();
            speed      = role.getSpeedPercent();
            resistance = role.getResistancePercent();
            source = "§7Rôle";
        } else {
            strength   = uhcPlayer.getForcePercent();
            speed      = uhcPlayer.getSpeedPercent();
            resistance = uhcPlayer.getResistancePercent();
            source = "§7Perso";
        }

        Player player = uhcPlayer.getPlayer();
        boolean hasStrength   = player != null && player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE);
        boolean hasSpeed      = player != null && player.hasPotionEffect(PotionEffectType.SPEED);
        boolean hasResistance = player != null && player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);

        int strengthPct   = hasStrength   ? (int) Math.round(strength   * 100) : 0;
        int speedPct      = hasSpeed      ? (int) Math.round(speed      * 100) : 0;
        int resistancePct = hasResistance ? (int) Math.round(resistance * 100) : 0;

        List<String> parts = new ArrayList<>();
        if (strengthPct > 0)   parts.add("§c⚔ Force " + strengthPct + "%");
        if (speedPct > 0)      parts.add("§b➤ Vitesse " + speedPct + "%");
        if (resistancePct > 0) parts.add("§a✦ Résistance " + resistancePct + "%");
        if (parts.isEmpty()) return "";

        return "\n  §8§l" + source
                + "\n  §8│ " + String.join(" §8│ ", parts);
    }

}

