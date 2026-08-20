package net.novaproject.novauhc.team;

import java.util.List;
import net.novaproject.novauhc.player.PlayerSpi.IUHCPlayer;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

public final class TeamSpi {

    private TeamSpi() {
    }

    public interface IUHCTeam {

        DyeColor dyeColor();

        String prefix();

        String name();

        Pattern[] patterns();

        int teamSize();

        boolean isCustom();

        Team getTeam();

        ItemStack getItem();

        List<? extends IUHCPlayer> getPlayers();

        boolean isAlive();
    }

    public interface IUHCTeamManager {

        boolean isRandomMode();

        void clearAssignments();

        void createTeams();

        void deleteTeams();

        void createTeam(int teamSize);

        List<? extends IUHCTeam> getTeams();

        List<? extends IUHCTeam> getAliveTeams();

        void fillTeams();
    }
}
