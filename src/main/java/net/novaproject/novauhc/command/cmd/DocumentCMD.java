package net.novaproject.novauhc.command.cmd;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DocumentCMD implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return false;
        Player player = (Player) commandSender;

        // Message principal
        TextComponent base = new TextComponent(CommonString.DOCUMENT_MESSAGE.getMessage(player));

        TextComponent linkConfig = new TextComponent(" §f[" + Common.get().getMainColor() + "Document§f]§r");
        linkConfig.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(CommonString.DOCUMENT_MESSAGE_HOVER.getMessage(player)).create()
        ));
        linkConfig.setClickEvent(new ClickEvent(
                ClickEvent.Action.OPEN_URL,
                Main.get().getConfig().getString("doc")
        ));

        TextComponent linkOfficial = new TextComponent(" §f[§3§lNova Code§f]§r");
        linkOfficial.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(CommonString.DOCUMENT_MESSAGE_HOVER.getMessage(player)).create()
        ));
        linkOfficial.setClickEvent(new ClickEvent(
                ClickEvent.Action.OPEN_URL,
                "https://mystouille.gitbook.io/nova-code"
        ));
        base.addExtra(linkConfig);
        base.addExtra(" ");
        base.addExtra(linkOfficial);

        player.spigot().sendMessage(base);
        return true;
    }
}
