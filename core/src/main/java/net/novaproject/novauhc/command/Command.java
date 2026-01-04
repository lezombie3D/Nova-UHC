//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.novaproject.novauhc.command;

import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Command {
    public static List<String> getOnlinePlayers(CommandArguments commandArguments) {
        String lastArg = commandArguments.getLastArgument();
        return Bukkit.getOnlinePlayers().stream().map(OfflinePlayer::getName).filter((name) -> name.startsWith(lastArg)).collect(Collectors.toList());
    }

    public static List<String> getNumbers(CommandArguments commandArguments, int... numbers) {
        String lastArg = commandArguments.getLastArgument();
        return Arrays.stream(numbers).mapToObj(String::valueOf).filter((number) -> number.startsWith(lastArg)).collect(Collectors.toList());
    }

    public static List<String> getBooleans(CommandArguments commandArguments) {
        String lastArg = commandArguments.getLastArgument();
        return Arrays.asList("true", "false").stream().filter((bool) -> bool.startsWith(lastArg)).collect(Collectors.toList());
    }

    public static List<String> getStrings(CommandArguments commandArguments, String... strings) {
        String lastArg = commandArguments.getLastArgument();
        return Arrays.asList(strings).stream().filter((string) -> string.startsWith(lastArg)).collect(Collectors.toList());
    }

    public static List<String> getTeams(CommandArguments commandArguments) {
        String lastArg = commandArguments.getLastArgument();
        return UHCTeamManager.get()
                .getTeams()
                .stream()
                .map(UHCTeam::getTeam)
                .map(Team::getName)
                .map(name -> name.replace('§', '&').replace(" ", "_"))
                .filter(name -> name.startsWith(lastArg))
                .collect(Collectors.toList());
    }



    public abstract void execute(CommandArguments args);

    public List<String> tabComplete(CommandArguments commandArguments) {
        return getOnlinePlayers(commandArguments);
    }
}
