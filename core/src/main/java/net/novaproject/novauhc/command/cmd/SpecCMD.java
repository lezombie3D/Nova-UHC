package net.novaproject.novauhc.command.cmd;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class SpecCMD extends Command.PlayerCommand {

    @Override
    protected void run(Player player, CommandArguments args) {

        if (!UHCManager.get().isGame()) {
            LangManager.get().send(CoreLang.COMMON_SPEC_NOT_INGAME, player);
            return;
        }

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        boolean spectator = uhcPlayer == null || !uhcPlayer.isPlaying();
        boolean staff = isHost(player);
        if (!spectator && !staff) {
            LangManager.get().send(CoreLang.COMMON_SPEC_ONLY, player);
            return;
        }

        List<UHCPlayer> alive = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();

        if (args.size() == 0) {
            String names = alive.stream()
                    .map(up -> {
                        Player p = up.getPlayer();
                        return p != null ? p.getName() : null;
                    })
                    .filter(n -> n != null)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.joining("§7, §f"));
            LangManager.get().send(CoreLang.COMMON_SPEC_LIST_HEADER, player,
                    Map.of("%count%", alive.size(), "%players%", names.isEmpty() ? "§8-" : names));
            return;
        }

        String targetName = args.getArgument(0);
        Player target = alive.stream()
                .map(UHCPlayer::getPlayer)
                .filter(p -> p != null && p.isOnline() && p.getName().equalsIgnoreCase(targetName))
                .findFirst()
                .orElse(null);

        if (target == null) {
            LangManager.get().send(CoreLang.COMMON_SPEC_TARGET_NOT_FOUND, player);
            return;
        }

        player.teleport(target.getLocation());
        LangManager.get().send(CoreLang.COMMON_SPEC_TELEPORTED, player, Map.of("%player%", target.getName()));
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        if (args.size() != 1) return Collections.emptyList();
        if (!(args.getSender() instanceof Player sender)) return Collections.emptyList();
        UHCPlayer senderUhc = UHCPlayerManager.get().getPlayer(sender);
        if (UHCManager.get().isGame() && senderUhc != null && senderUhc.isPlaying() && !isHost(sender)) {
            return Collections.emptyList();
        }
        String prefix = args.getArgument(0).toLowerCase(Locale.ROOT);
        return UHCPlayerManager.get().getPlayingOnlineUHCPlayers().stream()
                .map(UHCPlayer::getPlayer)
                .filter(p -> p != null && p.isOnline())
                .map(Player::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }
}

