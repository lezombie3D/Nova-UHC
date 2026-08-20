package net.novaproject.novauhc.command.cmd;

import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.game.VoteSystem;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class VoteCMD extends Command.PlayerCommand {

    @Override
    protected void run(Player player, CommandArguments args) {
        String choice = args.get(0, "").toLowerCase(Locale.ROOT);
        switch (choice) {
            case "yes", "oui" -> VoteSystem.castCurrent(player, 0);
            case "no", "non" -> VoteSystem.castCurrent(player, 1);
            default -> {
                try {
                    VoteSystem.castCurrent(player, Integer.parseInt(choice) - 1);
                } catch (NumberFormatException e) {
                    LangManager.get().send(CoreLang.CMD_VOTE_PLAYER_USAGE, player);
                }
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        if (args.size() != 1) return Collections.emptyList();
        List<String> options = VoteSystem.currentOptions();
        if (options.size() == 2 && "Oui".equals(options.get(0))) {
            return List.of("oui", "non");
        }
        List<String> out = new ArrayList<>();
        for (int i = 1; i <= options.size(); i++) out.add(String.valueOf(i));
        return out.isEmpty() ? List.of("oui", "non") : out;
    }
}

