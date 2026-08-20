package net.novaproject.novauhc.command.cmd;

import net.novaproject.novauhc.api.BlacklistManager;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.ui.BlackListUi;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BLCMD extends Command {

    @Override
    public void execute(CommandArguments args) {
        CommandSender sender = args.getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LangManager.get().get(CoreLang.CMD_PLAYERS_ONLY));
            return;
        }

        if (!isHost(player)) {
            LangManager.get().send(CoreLang.COMMON_NO_PERMISSION, player);
            return;
        }

        String[] arguments = args.getArguments();
        if (arguments.length == 0) {
            LangManager.get().send(CoreLang.COMMON_BL_USAGE, player);
            return;
        }

        switch (arguments[0].toLowerCase()) {
            case "menu":
                new BlackListUi(player).open();
                break;
            case "list":
                listBlacklist(player);
                break;
            case "add":
                if (arguments.length < 2) {
                    LangManager.get().send(CoreLang.COMMON_BL_USAGE, player);
                    return;
                }
                addTargets(player, arguments[1]);
                break;
            case "remove":
                if (arguments.length < 2) {
                    LangManager.get().send(CoreLang.COMMON_BL_USAGE, player);
                    return;
                }
                removeTargets(player, arguments[1]);
                break;
            default:
                LangManager.get().send(CoreLang.COMMON_BL_USAGE, player);
        }
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        String[] arguments = args.getArguments();
        if (arguments.length == 1) {
            return getStrings(args, "add", "remove", "list", "menu");
        }
        if (arguments.length == 2 && arguments[0].equalsIgnoreCase("add")) {
            return getOnlinePlayers(args);
        }
        if (arguments.length == 2 && arguments[0].equalsIgnoreCase("remove")) {
            String last = args.getLastArgument();
            return BlacklistManager.get().getEntries().values().stream()
                    .filter(n -> n != null && n.toLowerCase().startsWith(last.toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }

    private void listBlacklist(Player player) {
        Map<UUID, String> entries = BlacklistManager.get().getEntries();
        if (entries.isEmpty()) {
            LangManager.get().send(CoreLang.COMMON_BL_LIST_EMPTY, player);
            return;
        }
        LangManager.get().send(CoreLang.COMMON_BL_LIST_HEADER, player,
                Map.of("%count%", String.valueOf(entries.size())));
        StringBuilder sb = new StringBuilder("§c");
        boolean first = true;
        for (String name : entries.values()) {
            if (!first) sb.append("§7, §c");
            sb.append(name != null ? name : "?");
            first = false;
        }
        player.sendMessage(sb.toString());
    }

    private void addTargets(Player player, String raw) {
        for (String name : splitNames(raw)) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(name);
            if (target == null || target.getUniqueId() == null) {
                LangManager.get().send(CoreLang.COMMON_BL_INVALID_PLAYER, player, Map.of("%player%", name));
                continue;
            }
            String displayName = target.getName() != null ? target.getName() : name;
            if (BlacklistManager.get().add(target)) {
                LangManager.get().send(CoreLang.COMMON_BL_ADDED, player, Map.of("%player%", displayName));
                if (target.isOnline() && target.getPlayer() != null) {
                    Player online = target.getPlayer();
                    online.kickPlayer(LangManager.get().get(CoreLang.COMMON_KICK_BLACKLIST, online));
                    Bukkit.broadcastMessage(LangManager.get().get(CoreLang.COMMON_BL_KICKED_BROADCAST)
                            .replace("%player%", displayName));
                }
            } else {
                LangManager.get().send(CoreLang.COMMON_BL_ALREADY_IN, player, Map.of("%player%", displayName));
            }
        }
    }

    private void removeTargets(Player player, String raw) {
        for (String name : splitNames(raw)) {
            UUID matched = null;
            String displayName = name;
            for (Map.Entry<UUID, String> e : BlacklistManager.get().getEntries().entrySet()) {
                if (e.getValue() != null && e.getValue().equalsIgnoreCase(name)) {
                    matched = e.getKey();
                    displayName = e.getValue();
                    break;
                }
            }
            if (matched == null) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(name);
                if (target != null && target.getUniqueId() != null
                        && BlacklistManager.get().isBlacklisted(target.getUniqueId())) {
                    matched = target.getUniqueId();
                    displayName = target.getName() != null ? target.getName() : name;
                }
            }
            if (matched == null) {
                LangManager.get().send(CoreLang.COMMON_BL_NOT_IN, player, Map.of("%player%", name));
                continue;
            }
            if (BlacklistManager.get().remove(matched)) {
                LangManager.get().send(CoreLang.COMMON_BL_REMOVED, player, Map.of("%player%", displayName));
            } else {
                LangManager.get().send(CoreLang.COMMON_BL_NOT_IN, player, Map.of("%player%", displayName));
            }
        }
    }

    private List<String> splitNames(String raw) {
        return Arrays.stream(raw.split("[,;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}

