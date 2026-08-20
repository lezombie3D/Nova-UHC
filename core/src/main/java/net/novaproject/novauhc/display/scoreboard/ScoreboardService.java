package net.novaproject.novauhc.display.scoreboard;

import net.novaproject.novauhc.display.DisplaySpi;

import java.util.List;

import net.novaproject.novauhc.utils.UHCUtils.ServerMonitor;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.display.TeamsTagsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import org.bukkit.scoreboard.Team;
import org.bukkit.ChatColor;

public class ScoreboardService implements DisplaySpi.IScoreboardService {

    private static final ScoreboardService INSTANCE = new ScoreboardService();

    private static final Map<String, Function<Player, List<String>>> EXTRA_LINE_PROVIDERS = new ConcurrentHashMap<>();
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private static final int LINE_WRAP_WIDTH = 60;

    private final Map<UUID, NovaBoard> boards = new ConcurrentHashMap<>();
    private final Map<UUID, String> lastTabContent = new ConcurrentHashMap<>();

    private ScoreboardContents contents = new DefaultScoreboardContents();

    private BukkitTask updateTask;
    private BukkitTask healthTask;
    private int tickCounter = 0;


    public static ScoreboardService get() {
        return INSTANCE;
    }

    public void setContents(ScoreboardContents contents) {
        if (contents != null) {
            this.contents = contents;
        }
    }

    public ScoreboardContents getContents() {
        return contents;
    }

    public void addPlayer(Player player) {
        NovaBoard previous = boards.remove(player.getUniqueId());

        if (previous != null) {
            previous.delete();
        }

        boards.put(player.getUniqueId(), new NovaBoard(player));

        ensureTasks();
    }

    public void removePlayer(UUID uuid) {
        NovaBoard board = boards.remove(uuid);
        lastTabContent.remove(uuid);

        if (board != null) {
            board.delete();
        }
    }

    public static void setExtraLines(String key, Function<Player, List<String>> provider) {
        if (provider == null) {
            EXTRA_LINE_PROVIDERS.remove(key);
        } else {
            EXTRA_LINE_PROVIDERS.put(key, provider);
        }
    }

    public static List<String> collectExtraLines(Player player) {
        if (EXTRA_LINE_PROVIDERS.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> out = new ArrayList<>();

        for (Function<Player, List<String>> provider : EXTRA_LINE_PROVIDERS.values()) {
            try {
                List<String> lines = provider.apply(player);

                if (lines != null) {
                    out.addAll(lines);
                }
            } catch (Throwable t) {
                warnOnce("provider de lignes scoreboard en échec : " + t);
            }
        }

        return out;
    }

    private void ensureTasks() {

        if (updateTask == null) {
            updateTask = Bukkit.getScheduler().runTaskTimer(
                    Main.get(),
                    this::updateBoards,
                    1L,
                    1L
            );
        }

        if (healthTask == null) {
            healthTask = Bukkit.getScheduler().runTaskTimer(
                    Main.get(),
                    this::updateHealthSuffixes,
                    20L,
                    20L
            );
        }
    }

    private void updateBoards() {

        tickCounter++;

        if (tickCounter % 20 == 0) {
            contents.tick();
        }

        boolean animate = tickCounter % 2 == 0;

        for (NovaBoard board : boards.values()) {

            Player player = board.getPlayer();

            if (player == null || !player.isOnline()) {
                continue;
            }

            try {

                board.setTitle(contents.title(player));

                board.updateLines(
                        wrapLines(
                                contents.lines(player),
                                LINE_WRAP_WIDTH
                        )
                );

                if (animate) {
                    board.tick();
                }

                String header = contents.tabHeader(player);
                String footer = contents.tabFooter(player);

                {
                    String tabContent = (header == null ? "" : header) + "\0" + (footer == null ? "" : footer);
                    String previous = lastTabContent.put(player.getUniqueId(), tabContent);
                    if (!tabContent.equals(previous)) {
                        ServerMonitor.sendTab(
                                player,
                                header == null ? "" : header,
                                footer == null ? "" : footer
                        );
                    }
                }

            } catch (Throwable t) {
                warnOnce("update scoreboard en échec pour "
                        + player.getName() + " : " + t);
            }
        }
    }

    private void updateHealthSuffixes() {

        if (TeamsTagsManager.scoreboard == null) {
            return;
        }

        boolean show =
                UHCManager.get().isShowHealthPercent()
                        && UHCManager.get().isGame();

        for (UHCPlayer uhcTarget :
                UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {

            Player tp = uhcTarget.getPlayer();

            if (tp == null) {
                continue;
            }

            Team team =
                    TeamsTagsManager.scoreboard.getPlayerTeam(tp);

            if (team == null) {
                continue;
            }

            if (show) {

                double pct =
                        (tp.getHealth() / tp.getMaxHealth()) * 100;

                String color =
                        pct > 75 ? "§a"
                                : pct > 50 ? "§e"
                                  : pct > 25 ? "§6"
                                    : "§c";

                team.setSuffix(
                        " " + color + String.format("%.0f%%", pct)
                );

            } else {
                team.setSuffix("");
            }
        }
    }

    public static List<String> wrapLines(List<String> input, int width) {

        if (input == null) {
            return Collections.emptyList();
        }

        List<String> out = new ArrayList<>(input.size());

        for (String line : input) {

            if (line == null) {
                out.add("");
                continue;
            }

            String stripped =
                    ChatColor.stripColor(line);

            if (stripped == null || stripped.length() <= width) {
                out.add(line);
                continue;
            }

            int split = line.lastIndexOf(' ', width);

            if (split < 0 || split < width / 2) {
                split = width;
            }

            String head = line.substring(0, split);

            String lastColor =
                    ChatColor.getLastColors(head);

            String tail =
                    "  "
                            + lastColor
                            + line.substring(split).trim();

            out.add(head);
            out.add(tail);
        }

        return out;
    }

    private static void warnOnce(String message) {

        if (WARNED.add(message)) {
            Bukkit.getLogger().log(
                    Level.WARNING,
                    "[Scoreboard] " + message
            );
        }
    }

    public interface ScoreboardContents {

        default void tick() {
        }

        String title(Player player);

        List<String> lines(Player player);

        default String tabHeader(Player player) {
            return null;
        }

        default String tabFooter(Player player) {
            return null;
        }
    }
}
