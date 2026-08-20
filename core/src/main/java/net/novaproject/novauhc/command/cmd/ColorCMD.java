package net.novaproject.novauhc.command.cmd;

import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.ui.ColorPickerUi;
import net.novaproject.novauhc.ui.ColorPickerUi.ColorPlayerListUi;
import net.novaproject.novauhc.ui.ColorPickerUi.ColorRoleUi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ColorCMD extends Command {

    @Override
    public List<String> tabComplete(CommandArguments args) {
        if (args.size() == 1) {
            List<String> suggestions = new ArrayList<>(getStrings(args, "role"));
            suggestions.addAll(getOnlinePlayers(args));
            return suggestions;
        }
        return getOnlinePlayers(args);
    }

    @Override
    public void execute(CommandArguments args) {
        if (!(args.getSender() instanceof Player player)) return;

        if (args.size() == 0) {
            new ColorPlayerListUi(player).open();
            return;
        }

        if (args.getArguments()[0].equalsIgnoreCase("role")) {
            if (!ColorRoleUi.hasRoleMode()) {
                LangManager.get().send(ColorRoleUi.noRoleModeMessage(), player);
                return;
            }
            if (args.size() < 2) {
                LangManager.get().send(CoreLang.COMMON_PLAYER_NOT_FOUND, player);
                return;
            }
            Player roleTarget = Bukkit.getPlayer(args.getArguments()[1]);
            if (roleTarget == null || !roleTarget.isOnline()) {
                LangManager.get().send(CoreLang.COMMON_PLAYER_NOT_FOUND, player);
                return;
            }
            new ColorRoleUi(player, roleTarget).open();
            return;
        }

        List<Player> targets = new ArrayList<>();
        for (String targetName : args.getArguments()) {
            Player target = Bukkit.getPlayer(targetName);
            if (target == null || !target.isOnline()) {
                LangManager.get().send(CoreLang.COMMON_PLAYER_NOT_FOUND, player);
                return;
            }
            targets.add(target);
        }
        new ColorPickerUi(player, targets).open();
    }
}

