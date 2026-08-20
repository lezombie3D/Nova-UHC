package net.novaproject.novauhc.command.cmd;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.api.ApiManager;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletionException;
import java.util.regex.Pattern;

public class NovaLinkCMD extends Command.PlayerCommand {

    private static final Pattern LINK_CODE = Pattern.compile("^[A-HJ-NP-Z2-9]{8}$");

    @Override
    protected void run(Player player, CommandArguments args) {

        if (args.size() != 1) {
            LangManager.get().send(CoreLang.COMMON_NOVALINK_USAGE, player);
            return;
        }

        String code = args.getArgument(0)
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace("-", "");
        if (!LINK_CODE.matcher(code).matches()) {
            LangManager.get().send(CoreLang.COMMON_NOVALINK_INVALID, player);
            return;
        }

        ApiManager api = ApiManager.get();
        if (api == null) {
            LangManager.get().send(CoreLang.COMMON_NOVALINK_UNAVAILABLE, player);
            return;
        }

        LangManager.get().send(CoreLang.COMMON_NOVALINK_PROCESSING, player);
        api.verifyMinecraftLink(code, player.getUniqueId(), player.getName())
                .whenComplete((response, throwable) ->
                        Bukkit.getScheduler().runTask(Main.get(), () -> {
                            if (!player.isOnline()) return;

                            if (throwable == null) {
                                LangManager.get().send(CoreLang.COMMON_NOVALINK_SUCCESS, player);
                                return;
                            }

                            Throwable root = unwrap(throwable);
                            Main.get().getLogger().warning(
                                    "Échec /novalink pour " + player.getName() + ": " + root.getMessage()
                            );
                            LangManager.get().send(CoreLang.COMMON_NOVALINK_FAILURE, player);
                        })
                );
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        return Collections.emptyList();
    }

    private Throwable unwrap(Throwable throwable) {
        Throwable current = throwable;
        while ((current instanceof CompletionException || current.getCause() != null)
                && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}
