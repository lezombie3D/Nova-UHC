package net.novaproject.novauhc.command.cmd;

import net.novaproject.novauhc.ui.LanguageUi;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LangCMD extends Command {

    @Override
    public void execute(CommandArguments args) {
        if (!(args.getSender() instanceof Player player)) return;
        if (args.getArguments().length < 1) {
            new LanguageUi(player).open();
            return;
        }
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null) return;
        String input = args.getArgument(0);

        String locale = resolve(input);
        if (locale == null) {
            LangManager.get().send(CoreLang.COMMON_ERROR_INVALID_ARGUMENT, player, Map.of("argument", input));
            return;
        }

        uhcPlayer.setLocale(locale);
        LangManager.get().send(CoreLang.COMMON_SUCCESSFUL_MODIFICATION, player);
    }

    private String resolve(String input) {
        for (String locale : LangManager.get().getAvailableLocales()) {
            if (locale.equalsIgnoreCase(input) || locale.split("_")[0].equalsIgnoreCase(input)) {
                return locale;
            }
        }
        return null;
    }

    @Override
    public List<String> tabComplete(CommandArguments commandArguments) {
        if (commandArguments.getArguments().length == 1) {
            List<String> out = new ArrayList<>();
            for (String locale : LangManager.get().getAvailableLocales()) {
                if (!out.contains(locale)) out.add(locale);
                String shortCode = locale.split("_")[0];
                if (!out.contains(shortCode)) out.add(shortCode);
            }
            return out;
        }
        return List.of();
    }
}
