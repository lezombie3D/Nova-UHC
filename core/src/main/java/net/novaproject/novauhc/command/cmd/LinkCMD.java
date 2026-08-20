package net.novaproject.novauhc.command.cmd;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.lang.lang.CoreLang;
import org.bukkit.entity.Player;

public final class LinkCMD {

    private LinkCMD() {
    }

    private abstract static class LinkBase extends Command {

        protected abstract Lang message();

        protected abstract Lang hover();

        protected abstract String label();

        protected abstract String configKey();

        protected abstract String officialUrl();

        @Override
        public void execute(CommandArguments args) {
            if (!(args.getSender() instanceof Player player)) return;

            TextComponent base = new TextComponent(LangManager.get().get(message(), player));
            base.addExtra(link(player, "  §8│ " + Common.get().getMainColor() + label() + "§r",
                    Main.get().getConfig().getString(configKey())));
            base.addExtra(" ");
            base.addExtra(link(player, "  §8│ §3§lNova Code§r", officialUrl()));

            player.spigot().sendMessage(base);
        }

        private TextComponent link(Player player, String text, String url) {
            TextComponent component = new TextComponent(text);
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(LangManager.get().get(hover(), player)).create()));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
            return component;
        }
    }

    public static class DiscordCMD extends LinkBase {
        @Override protected Lang message() { return CoreLang.COMMON_DISCORD_MESSAGE; }
        @Override protected Lang hover() { return CoreLang.COMMON_DISCORD_MESSAGE_HOVER; }
        @Override protected String label() { return "Discord"; }
        @Override protected String configKey() { return "discord"; }
        @Override protected String officialUrl() { return "https://discord.gg/TGMM7bPwTb"; }
    }

    public static class DocumentCMD extends LinkBase {
        @Override protected Lang message() { return CoreLang.COMMON_DOCUMENT_MESSAGE; }
        @Override protected Lang hover() { return CoreLang.COMMON_DOCUMENT_MESSAGE_HOVER; }
        @Override protected String label() { return "Document"; }
        @Override protected String configKey() { return "doc"; }
        @Override protected String officialUrl() { return "https://nova-code.fr"; }
    }
}
