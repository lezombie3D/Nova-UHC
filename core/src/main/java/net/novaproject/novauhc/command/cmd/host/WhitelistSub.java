package net.novaproject.novauhc.command.cmd.host;

import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class WhitelistSub extends HostSub {

    @Override
    protected void run(Player player, CommandArguments args) {
        if (args.size() < 1) {
            LangManager.get().send(CoreLang.CMD_WHITELIST_USAGE, player);
            LangManager.get().send(CoreLang.CMD_WHITELIST_LIST_USAGE, player);
            return;
        }
        String arg = args.get(0, "");
        if (args.size() == 1) {
            switch (arg) {
                case "clear":
                    Bukkit.getWhitelistedPlayers().forEach(offlinePlayer -> offlinePlayer.setWhitelisted(false));
                    LangManager.get().send(CoreLang.CMD_WHITELIST_CLEARED, player);
                    break;
                case "list":
                    StringBuilder sb = new StringBuilder();
                    for (OfflinePlayer offlinePlayer : Bukkit.getWhitelistedPlayers()) {
                        sb.append(offlinePlayer.getName()).append(" ");
                    }
                    LangManager.get().send(CoreLang.CMD_WHITELIST_LIST, player, Map.of("%players%", sb.toString()));
                    break;
                case "on":
                    if (Bukkit.hasWhitelist()) {
                        LangManager.get().send(CoreLang.CMD_WHITELIST_ALREADY_ON, player);
                    } else {
                        Bukkit.setWhitelist(true);
                        LangManager.get().send(CoreLang.CMD_WHITELIST_ENABLED, player);
                    }
                    break;
                case "off":
                    if (Bukkit.hasWhitelist()) {
                        LangManager.get().send(CoreLang.CMD_WHITELIST_DISABLED, player);
                        Bukkit.setWhitelist(false);
                    } else {
                        LangManager.get().send(CoreLang.CMD_WHITELIST_ALREADY_OFF, player);
                    }
                    break;
                default:
                    break;
            }
        } else {
            boolean add = arg.equalsIgnoreCase("add");
            if (!add && !arg.equalsIgnoreCase("remove")) return;
            for (int i = 1; i < args.size(); i++) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args.get(i, ""));
                if (offlinePlayer == null) {
                    LangManager.get().send(CoreLang.CMD_WHITELIST_UNKNOWN_PLAYER, player);
                    continue;
                }
                if (add) {
                    if (offlinePlayer.isWhitelisted()) {
                        LangManager.get().send(CoreLang.CMD_WHITELIST_ALREADY_IN, player);
                        continue;
                    }
                    offlinePlayer.setWhitelisted(true);
                    LangManager.get().send(CoreLang.CMD_WHITELIST_ADDED, player, Map.of("%player%", String.valueOf(offlinePlayer.getName())));
                } else {
                    if (!offlinePlayer.isWhitelisted()) {
                        LangManager.get().send(CoreLang.CMD_WHITELIST_NOT_IN, player);
                        continue;
                    }
                    offlinePlayer.setWhitelisted(false);
                    LangManager.get().send(CoreLang.CMD_WHITELIST_REMOVED, player, Map.of("%player%", String.valueOf(offlinePlayer.getName())));
                }
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        if (args.size() == 1) return getStrings(args, "add", "remove", "list", "clear", "on", "off");
        if (args.size() >= 2 && (args.get(0, "").equalsIgnoreCase("add") || args.get(0, "").equalsIgnoreCase("remove"))) {
            return getOnlinePlayers(args);
        }
        return List.of();
    }
}

