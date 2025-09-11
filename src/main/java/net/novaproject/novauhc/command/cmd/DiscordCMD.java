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

public class DiscordCMD implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return false;
        Player player = (Player) commandSender;

        TextComponent base = new TextComponent(CommonString.DISCORD_MESSAGE.getMessage(player));

        TextComponent linkConfig = new TextComponent(" §f[" + Common.get().getMainColor() + "Discord§f]§r");
        linkConfig.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(CommonString.DISCORD_MESSAGE_HOVER.getMessage(player)).create()
        ));
        linkConfig.setClickEvent(new ClickEvent(
                ClickEvent.Action.OPEN_URL,
                Main.get().getConfig().getString("discord")
        ));

        TextComponent linkOfficial = new TextComponent(" §f[§3§lNova Code§f]§r");
        linkOfficial.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(CommonString.DISCORD_MESSAGE_HOVER.getMessage(player)).create()
        ));
        linkOfficial.setClickEvent(new ClickEvent(
                ClickEvent.Action.OPEN_URL,
                "https://discord.gg/TGMM7bPwTb"
        ));
        base.addExtra(linkConfig);
        base.addExtra(" ");
        base.addExtra(linkOfficial);

        player.spigot().sendMessage(base);
        return true;
    }

}
