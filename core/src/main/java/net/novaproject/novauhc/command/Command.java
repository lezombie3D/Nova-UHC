package net.novaproject.novauhc.command;

import net.novaproject.novauhc.lobby.RankManager;
import java.util.Set;
import java.util.Map;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Collections;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class Command {

    public abstract static class PlayerCommand extends Command {

        @Override
        public final void execute(CommandArguments args) {
            if (!(args.getSender() instanceof Player player)) {
                args.getSender().sendMessage(LangManager.get().get(CoreLang.CMD_PLAYERS_ONLY));
                return;
            }
            run(player, args);
        }

        protected abstract void run(Player player, CommandArguments args);
    }

    public static boolean isHost(Player player) {
        return RankManager.isStaff(player);
    }

    public static Command ofPlayer(Consumer<Player> action) {
        return new PlayerCommand() {
            @Override
            protected void run(Player player, CommandArguments args) {
                action.accept(player);
            }

            @Override
            public List<String> tabComplete(CommandArguments args) {
                return Collections.emptyList();
            }
        };
    }

    public static List<String> getOnlinePlayers(CommandArguments commandArguments) {
        String lastArg = commandArguments.getLastArgument();
        return Bukkit.getOnlinePlayers().stream().map(OfflinePlayer::getName).filter((name) -> name.startsWith(lastArg)).collect(Collectors.toList());
    }

    public static List<String> getNumbers(CommandArguments commandArguments, int... numbers) {
        String lastArg = commandArguments.getLastArgument();
        return Arrays.stream(numbers).mapToObj(String::valueOf).filter((number) -> number.startsWith(lastArg)).collect(Collectors.toList());
    }

    public static List<String> playersFirstArg(CommandArguments commandArguments) {
        return commandArguments.size() == 1 ? getOnlinePlayers(commandArguments) : List.of();
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

    private final Map<String, Command> subCommands = new LinkedHashMap<>();

    protected void sub(String name, Command subCommand) {
        if (name == null || subCommand == null) return;
        subCommands.put(name.toLowerCase(Locale.ROOT), subCommand);
    }

    protected Set<String> subNames() {
        return subCommands.keySet();
    }

    protected Command subOf(String name) {
        return name == null ? null : subCommands.get(name.toLowerCase(Locale.ROOT));
    }

    protected boolean dispatchSub(CommandArguments args) {
        if (args.size() == 0) return false;
        Command subCommand = subOf(args.getArgument(0));
        if (subCommand == null) return false;
        subCommand.execute(shift(args));
        return true;
    }

    protected List<String> tabCompleteSub(CommandArguments args) {
        if (args.size() <= 1) {
            return getStrings(args, subNames().toArray(new String[0]));
        }
        Command subCommand = subOf(args.getArgument(0));
        return subCommand == null ? Collections.emptyList() : subCommand.tabComplete(shift(args));
    }

    protected static CommandArguments shift(CommandArguments args) {
        String[] arguments = args.getArguments();
        String[] rest = arguments.length <= 1 ? new String[0]
                : Arrays.copyOfRange(arguments, 1, arguments.length);
        return new CommandArguments(args.getSender(), args.getCommand(), args.getLabel(), rest);
    }
}