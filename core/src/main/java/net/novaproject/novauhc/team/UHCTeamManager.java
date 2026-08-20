package net.novaproject.novauhc.team;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.display.PlayerColorManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class UHCTeamManager implements TeamSpi.IUHCTeamManager {

    private final List<TeamTemplate> templates = Arrays.asList(
            new TeamTemplate("RED",     'c', DyeColor.RED),
            new TeamTemplate("BLUE",    '9', DyeColor.BLUE),
            new TeamTemplate("GREEN",   '2', DyeColor.GREEN),
            new TeamTemplate("YELLOW",  'e', DyeColor.YELLOW),
            new TeamTemplate("PURPLE",  '5', DyeColor.PURPLE),
            new TeamTemplate("PINK",    'd', DyeColor.PINK),
            new TeamTemplate("WHITE",   'f', DyeColor.WHITE),
            new TeamTemplate("GRAY",    '7', DyeColor.GRAY),
            new TeamTemplate("AQUA",    'b', DyeColor.LIGHT_BLUE),
            new TeamTemplate("ORANGE",  '6', DyeColor.ORANGE),
            new TeamTemplate("CYAN",    '3', DyeColor.CYAN),
            new TeamTemplate("LIME",    'a', DyeColor.LIME),
            new TeamTemplate("MAGENTA", 'd', DyeColor.MAGENTA)
    );

    private final List<UHCTeam> teams = new ArrayList<>();
    private final Map<String, String> pastilles = new HashMap<>();
    private final String[] symbols = {
            "", "❤ ", "♣ ", "☼ ", "☠ ", "☆ ", "⚡ ", "★ ", "✪ ", "☯ ", "☢ ",
            "✧ ", "☘ ", "☀ ", "☁ ", "⚔ ", "❄ ", "♛ ", "♞ ", "✝ ", "☣ ", "♠ ", "♤ ", "⚙ ", "⚛ "
    };
    private final Pattern[][] patternTypes = {
            {},
            {new Pattern(DyeColor.WHITE, PatternType.RHOMBUS_MIDDLE), new Pattern(DyeColor.BLACK, PatternType.HALF_HORIZONTAL), new Pattern(DyeColor.WHITE, PatternType.CIRCLE_MIDDLE), new Pattern(DyeColor.BLACK, PatternType.TRIANGLE_TOP)},
            {new Pattern(DyeColor.WHITE, PatternType.RHOMBUS_MIDDLE), new Pattern(DyeColor.BLACK, PatternType.HALF_HORIZONTAL_MIRROR), new Pattern(DyeColor.WHITE, PatternType.CIRCLE_MIDDLE), new Pattern(DyeColor.BLACK, PatternType.TRIANGLE_BOTTOM), new Pattern(DyeColor.WHITE, PatternType.TRIANGLE_BOTTOM), new Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM)},
            {new Pattern(DyeColor.WHITE, PatternType.FLOWER)},
            {new Pattern(DyeColor.WHITE, PatternType.SKULL)},
            {new Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE), new Pattern(DyeColor.BLACK, PatternType.FLOWER), new Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP), new Pattern(DyeColor.WHITE, PatternType.RHOMBUS_MIDDLE), new Pattern(DyeColor.BLACK, PatternType.TRIANGLE_BOTTOM), new Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM)},
            {new Pattern(DyeColor.BLUE, PatternType.STRIPE_TOP), new Pattern(DyeColor.LIGHT_BLUE, PatternType.STRIPE_MIDDLE), new Pattern(DyeColor.CYAN, PatternType.STRIPE_BOTTOM)},
            {new Pattern(DyeColor.YELLOW, PatternType.BORDER), new Pattern(DyeColor.RED, PatternType.CROSS), new Pattern(DyeColor.ORANGE, PatternType.TRIANGLES_TOP)},
            {new Pattern(DyeColor.GREEN, PatternType.HALF_VERTICAL), new Pattern(DyeColor.LIME, PatternType.HALF_VERTICAL_MIRROR), new Pattern(DyeColor.BROWN, PatternType.DIAGONAL_LEFT_MIRROR)},
            {new Pattern(DyeColor.MAGENTA, PatternType.STRIPE_CENTER), new Pattern(DyeColor.PINK, PatternType.BORDER)},
            {new Pattern(DyeColor.GRAY, PatternType.GRADIENT_UP), new Pattern(DyeColor.WHITE, PatternType.GRADIENT)}
    };
    private int colort = 0;
    private int symbolt = 0;
    private TeamSelectionMode selectionMode = TeamSelectionMode.CHOSEN;

    public static UHCTeamManager get() {
        return UHCManager.get().getUhcTeamManager();
    }

    public TeamSelectionMode getSelectionMode() {
        return selectionMode;
    }

    public void setSelectionMode(TeamSelectionMode mode) {
        if (mode != null) this.selectionMode = mode;
    }

    public boolean isRandomMode() {
        return selectionMode == TeamSelectionMode.RANDOM;
    }

    public void clearAssignments() {
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            uhcPlayer.setTeam(Optional.empty());
        }
    }

    public void createTeams() {
        deleteTeams();
        int teamSize = UHCManager.get().getTeam_size();
        int slot = UHCManager.get().getSlot();
        double teamCount = Math.ceil((double) slot / teamSize);

        synchronized (this) {
            for (int i = 0; i < teamCount; i++) {
                createTeam(teamSize);
            }
        }
    }

    public void removeTeam(UHCTeam team) {
        teams.remove(team);
        pastilles.remove(team.name());
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        if (scoreboard.getTeam(team.name()) != null) {
            scoreboard.getTeam(team.name()).unregister();
        }
    }

    public String pastilleOf(UHCTeam team) {
        if (team == null) return "";
        String known = pastilles.get(team.name());
        if (known != null) return known;
        return firstColorCode(team.prefix()) + PlayerColorManager.PASTILLE_GLYPH + " §f";
    }

    private static String firstColorCode(String prefix) {
        if (prefix == null) return "§f";
        for (int i = 0; i + 1 < prefix.length(); i++) {
            if (prefix.charAt(i) != '§') continue;
            ChatColor color = ChatColor.getByChar(prefix.charAt(i + 1));
            if (color != null && color.isColor()) return color.toString();
        }
        return "§f";
    }

    public void deleteTeams() {
        List<UHCTeam> teamsToRemove = new ArrayList<>(teams);
        for (UHCTeam team : teamsToRemove) {
            if (!team.isCustom()) {
                removeTeam(team);
            }
        }
        colort = 0;
        symbolt = 0;
    }

    public void createTeam(int teamSize) {
        if (symbolt >= symbols.length || colort >= templates.size()) {
            Bukkit.getLogger().info("Impossible de créer plus de teams");
            return;
        }

        TeamTemplate template = templates.get(colort);

        UHCTeam team = new UHCTeam(
                template.dyeColor,
                template.prefix() + symbols[symbolt],
                symbols[symbolt] + template.name,
                patternTypes[symbolt % patternTypes.length],
                teamSize,
                false
        );
        teams.add(team);
        pastilles.put(team.name(), template.pastille(symbols[symbolt]));

        colort++;
        if (colort == templates.size()) {
            colort = 0;
            symbolt++;
        }
    }

    public List<UHCTeam> getTeams() {
        return teams;
    }

    public List<UHCTeam> getAliveTeams() {
        List<UHCTeam> alives = new ArrayList<>();
        for (UHCTeam team : teams) {
            if (team.isAlive()) {
                alives.add(team);
            }
        }
        return alives;
    }

    public void addTeams(UHCTeam team) {
        teams.add(team);
    }

    public void fillTeams() {
        if(teams.isEmpty()) return;
        List<UHCTeam> fillableTeams = new ArrayList<>();
        for (UHCTeam team : teams) {
            if (team.getPlayers().size() < team.teamSize()) {
                fillableTeams.add(team);
            }
        }

        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            if (!uhcPlayer.getTeam().isPresent() && !fillableTeams.isEmpty()) {
                UHCTeam team = fillableTeams.get(ThreadLocalRandom.current().nextInt(fillableTeams.size()));
                uhcPlayer.setTeam(Optional.of(team));
                if (team.getPlayers().size() == team.teamSize()) {
                    fillableTeams.remove(team);
                }
            }
        }
    }

    public void scatterTeam(UHCPlayer player, HashMap<UHCTeam, Location> teamloc) {
        player.getTeam().ifPresent(team -> {
            if (teamloc.containsKey(team)) {
                player.getPlayer().teleport(teamloc.get(team));
            }
        });
    }

    private record TeamTemplate(String name, char colorCode, DyeColor dyeColor) {

        String prefix() {
            return "§" + colorCode + "§l" + name + " §" + colorCode;
        }

        String pastille(String symbol) {
            return "§" + colorCode
                    + (symbol == null || symbol.trim().isEmpty() ? PlayerColorManager.PASTILLE_GLYPH : symbol.trim())
                    + " §f";
        }
    }

    public enum TeamSelectionMode {
        CHOSEN,
        RANDOM;

        public TeamSelectionMode next() {
            return this == CHOSEN ? RANDOM : CHOSEN;
        }
    }
}

