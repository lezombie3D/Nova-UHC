package net.novaproject.novauhc.command.cmd;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HelpOp extends Command {

    private static final String IGNORE_KEYWORD = "ignorer";

    private static final Set<UUID> IGNORING = ConcurrentHashMap.newKeySet();

    @Override
    public void execute(CommandArguments args) {
        if (!(args.getSender() instanceof Player player)) return;
        if (args.size() < 1) {
            LangManager.get().send(CoreLang.COMMON_HELPOP_USAGE, player);
            return;
        }

        if (args.size() == 1 && IGNORE_KEYWORD.equalsIgnoreCase(args.get(0, "")) && isHost(player)) {
            boolean ignoring = IGNORING.add(player.getUniqueId());
            if (!ignoring) IGNORING.remove(player.getUniqueId());
            LangManager.get().send(ignoring ? CoreLang.COMMON_HELPOP_IGNORE_ON : CoreLang.COMMON_HELPOP_IGNORE_OFF, player);
            return;
        }

        String message = args.join(0);
        String target = player.getName();
        Map<String, Object> placeholders = Map.of("%player%", target);

        int reached = 0;
        for (UHCPlayer host : UHCPlayerManager.get().getOnlineUHCPlayers()) {
            Player hostPlayer = host.getPlayer();
            if (hostPlayer == null || !hostPlayer.isOnline() || !isHost(hostPlayer)) continue;
            if (IGNORING.contains(hostPlayer.getUniqueId())) continue;

            hostPlayer.sendMessage(LangManager.get().get(CoreLang.COMMON_HELPOP_MESSAGE, hostPlayer, placeholders) + message);
            hostPlayer.spigot().sendMessage(actions(hostPlayer, target, placeholders));
            reached++;
        }

        LangManager.get().send(reached == 0 ? CoreLang.COMMON_HELPOP_NO_HOST : CoreLang.COMMON_HELPOP_SENT, player,
                Map.of("%count%", reached));
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        if (args.size() > 1 || !(args.getSender() instanceof Player player) || !isHost(player)) return Collections.emptyList();
        return getStrings(args, IGNORE_KEYWORD);
    }

    private BaseComponent[] actions(Player host, String target, Map<String, Object> placeholders) {
        List<BaseComponent> line = new ArrayList<>();
        line.add(new TextComponent(LangManager.get().get(CoreLang.COMMON_HELPOP_ACTIONS, host, placeholders)));
        appendButton(line, host, CoreLang.COMMON_HELPOP_BTN_INVSEE, CoreLang.COMMON_HELPOP_BTN_INVSEE_HOVER,
                ClickEvent.Action.RUN_COMMAND, "/h invsee " + target, placeholders);
        appendButton(line, host, CoreLang.COMMON_HELPOP_BTN_TP, CoreLang.COMMON_HELPOP_BTN_TP_HOVER,
                ClickEvent.Action.RUN_COMMAND, "/spec " + target, placeholders);
        appendButton(line, host, CoreLang.COMMON_HELPOP_BTN_REPLY, CoreLang.COMMON_HELPOP_BTN_REPLY_HOVER,
                ClickEvent.Action.SUGGEST_COMMAND, "/msg " + target + " ", placeholders);
        appendButton(line, host, CoreLang.COMMON_HELPOP_BTN_FREEZE, CoreLang.COMMON_HELPOP_BTN_FREEZE_HOVER,
                ClickEvent.Action.RUN_COMMAND, "/h freeze " + target, placeholders);
        return line.toArray(new BaseComponent[0]);
    }

    private void appendButton(List<BaseComponent> line, Player host, CoreLang label, CoreLang hover,
                              ClickEvent.Action action, String command, Map<String, Object> placeholders) {
        TextComponent button = new TextComponent(" " + LangManager.get().get(label, host, placeholders));
        button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(LangManager.get().get(hover, host, placeholders)).create()));
        button.setClickEvent(new ClickEvent(action, command));
        line.add(button);
    }
}
