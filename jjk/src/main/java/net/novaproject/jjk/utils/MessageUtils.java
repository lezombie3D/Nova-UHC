package net.novaproject.jjk.utils;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class MessageUtils {

    public static void sendHoverLine(Player p, String text, String hover) {
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(text));
        component.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(hover).create()
        ));
        p.spigot().sendMessage(component);
    }
}