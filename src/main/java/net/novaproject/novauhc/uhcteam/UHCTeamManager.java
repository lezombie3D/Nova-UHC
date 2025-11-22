package net.novaproject.novauhc.uhcteam;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class UHCTeamManager {

    private final List<TeamTemplate> templates = Arrays.asList(
            new TeamTemplate("Red", "§4", DyeColor.RED),
            new TeamTemplate("Blue", "§1", DyeColor.BLUE),
            new TeamTemplate("Green", "§2", DyeColor.GREEN),
            new TeamTemplate("Yellow", "§e", DyeColor.YELLOW),
            new TeamTemplate("Purple", "§5", DyeColor.PURPLE),
            new TeamTemplate("Pink", "§d", DyeColor.PINK),
            new TeamTemplate("White", "§f", DyeColor.WHITE),
            new TeamTemplate("Gray", "§7", DyeColor.GRAY),
            new TeamTemplate("Aqua", "§b", DyeColor.LIGHT_BLUE),
            new TeamTemplate("Orange", "§6", DyeColor.ORANGE),
            new TeamTemplate("Cyan", "§3", DyeColor.CYAN),
            new TeamTemplate("Lime", "§a", DyeColor.LIME),
            new TeamTemplate("Brown", "§c", DyeColor.BROWN),
            new TeamTemplate("Magenta", "§d", DyeColor.MAGENTA),
            new TeamTemplate("Black", "§0", DyeColor.BLACK),
            new TeamTemplate("Dark Gray", "§8", DyeColor.GRAY)
    );

    private final List<UHCTeam> teams = new ArrayList<>();
    private final String[] symbols = {
            "", "❤ ", "♣ ", "\u263C ", "\u2620 ", "\u2606 ", "⚡ ", "★ ", "✪ ", "☯ ", "☢ ",
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

    public static UHCTeamManager get() {
        return UHCManager.get().getUhcTeamManager();
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
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        if (scoreboard.getTeam(team.name()) != null) {
            scoreboard.getTeam(team.name()).unregister();
        }
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
            System.out.println("Impossible de créer plus de teams");
            return;
        }

        TeamTemplate template = templates.get(colort);

        UHCTeam team = new UHCTeam(
                template.dyeColor,
                template.prefix + symbols[symbolt],
                template.prefix + symbols[symbolt] + template.name,
                patternTypes[symbolt % patternTypes.length],
                teamSize,
                false
        );
        teams.add(team);

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
        List<UHCTeam> fillableTeams = new ArrayList<>();
        for (UHCTeam team : teams) {
            if (team.getPlayers().size() < team.teamSize()) {
                fillableTeams.add(team);
            }
        }

        Random random = new Random();
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            if (!uhcPlayer.getTeam().isPresent() && !fillableTeams.isEmpty()) {
                UHCTeam team = fillableTeams.get(random.nextInt(fillableTeams.size()));
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

    private record TeamTemplate(String name, String prefix, DyeColor dyeColor) {
    }
}
