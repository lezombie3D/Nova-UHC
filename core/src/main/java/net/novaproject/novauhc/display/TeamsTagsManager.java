package net.novaproject.novauhc.display;

import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import net.novaproject.novauhc.scenario.ScenarioManager;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Collection;
import java.util.UUID;

public class TeamsTagsManager {

    private String prefix;
    private String suffix;
    private Team team;
    public static Scoreboard scoreboard;

    public TeamsTagsManager(String name, String prefix, String suffix, Scoreboard current) throws Exception {
        this.prefix = prefix;
        this.suffix = suffix;
        this.team = ScoreboardTeams.getOrCreate(current, name);
        scoreboard = current;

        int prefixLength = 0;
        int suffixLength = 0;
        if (prefix != null) {
            prefixLength = prefix.length();
        }
        if (suffix != null) {
            suffixLength = suffix.length();
        }
        if (prefixLength + suffixLength >= 32) {
            throw new Exception("prefix and suffix lenghts are greater than 16");
        }
        if (suffix != null) {
            this.team.setSuffix(ChatColor.translateAlternateColorCodes('&', suffix));
        }
        if (prefix != null) {
            this.team.setPrefix(ChatColor.translateAlternateColorCodes('&', prefix));
        }
    }

    public TeamsTagsManager(String name, String prefix, String suffix) throws Exception {
        this(name, prefix, suffix, Bukkit.getScoreboardManager().getMainScoreboard());
    }

    @SuppressWarnings("deprecation")
    public void set(Player player){
        this.team.addPlayer(player);
        player.setScoreboard(scoreboard);
        PlayerColorManager.reapplyColorsForTarget(player);
    }

    @SuppressWarnings("deprecation")
    public void remove(Player player){
        this.team.removePlayer(player);
    }

    public void setAll(Collection<Player> players) {
        for (Player player : players) {
            set(player);
        }
    }

    public void setAll(Player[] players) {
        Player[] arrayOfPlayer;
        int j = (arrayOfPlayer = players).length;
        for (int i = 0; i < j; i++) {
            Player player = arrayOfPlayer[i];
            set(player);
        }
    }

    public void setPrefix(String prefix) {
        this.prefix = ChatColor.translateAlternateColorCodes('&', prefix);
        this.team.setPrefix(this.prefix);
    }

    public void setSuffix(String suffix) {
        this.suffix = ChatColor.translateAlternateColorCodes('&', suffix);
        this.team.setSuffix(this.suffix);
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getSuffix() {
        return this.suffix;
    }

    public Team getTeam() {
        return this.team;
    }

    public void removeTeam() {
        this.team.unregister();
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public static void setNameTag(Player player, String name, String prefix, String suffix){
        try{
            TeamsTagsManager tagplayer = new TeamsTagsManager(perPlayerTeamName(name, player), prefix, suffix);
            tagplayer.set(player);
        }catch (Exception e){
            Bukkit.getLogger().log(Level.SEVERE, "Erreur inattendue", e);
        }
    }

    public static String perPlayerTeamName(String sortKey, Player player) {
        String key = sortKey == null ? "" : sortKey;
        if (key.length() > 11) key = key.substring(0, 11);
        String id = Integer.toHexString(player.getUniqueId().hashCode());
        if (id.length() > 4) id = id.substring(id.length() - 4);
        return key + "#" + id;
    }

    public static final class ScoreboardTeams {

        @SuppressWarnings("deprecation")
        public static Team getOrCreate(Scoreboard scoreboard, String name) {
            Team team = scoreboard.getTeam(name);
            if (team == null) team = scoreboard.registerNewTeam(name);
            team.setCanSeeFriendlyInvisibles(false);
            team.setAllowFriendlyFire(true);
            if (ScenarioManager.get().isScenarioActive("NoNameTag")) {
                team.setNameTagVisibility(NameTagVisibility.NEVER);
            }
            return team;
        }

    }
}
