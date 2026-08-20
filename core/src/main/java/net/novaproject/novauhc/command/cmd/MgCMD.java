package net.novaproject.novauhc.command.cmd;

import java.util.ArrayList;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.minigame.*;
import net.novaproject.novauhc.minigame.MiniGames.MiniGameRegistry;
import net.novaproject.novauhc.minigame.MiniGames.MiniGameType;
import net.novaproject.novauhc.minigame.DuelRequestManager.DuelRequest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class MgCMD extends Command {

    @Override
    public void execute(CommandArguments args) {
        if (!(args.getSender() instanceof Player player)) return;
        if (UHCManager.get().getGameState() != UHCManager.GameState.LOBBY) {
            LangManager.get().send(CoreLang.CMD_MG_LOBBY_ONLY, player);
            return;
        }
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null) return;

        String sub = args.size() > 0 ? args.getArgument(0).toLowerCase() : "";

        switch (sub) {

            case "duel" -> {
                if (args.size() < 3) { LangManager.get().send(CoreLang.CMD_MG_DUEL_USAGE, player); return; }
                Player opponent = Bukkit.getPlayer(args.getArgument(1));
                if (opponent == null || !opponent.isOnline()) { LangManager.get().send(CoreLang.COMMON_PLAYER_NOT_FOUND, player); return; }
                if (opponent.equals(player)) { LangManager.get().send(CoreLang.CMD_MG_SELF, player); return; }
                String game = args.getArgument(2).toLowerCase();
                MiniGameType type = MiniGameType.fromKey(game);
                if (type == null || !type.isTwoPlayer()) { LangManager.get().send(CoreLang.CMD_MG_UNKNOWN_GAME, player); return; }
                DuelRequestManager.send(player, opponent, game);
            }

            case "accept" -> {
                DuelRequest req = DuelRequestManager.accept(player);
                if (req == null) { LangManager.get().send(CoreLang.CMD_MG_NO_REQUEST, player); return; }
                Player challenger = Bukkit.getPlayer(req.getFrom());
                if (challenger == null || !challenger.isOnline()) {
                    LangManager.get().send(CoreLang.CMD_MG_CHALLENGER_OFFLINE, player);
                    return;
                }
                if (MiniGameRegistry.isPlaying(player.getUniqueId())
                        || MiniGameRegistry.isPlaying(challenger.getUniqueId())) {
                    LangManager.get().send(CoreLang.CMD_MG_ALREADY_IN_GAME, player);
                    return;
                }
                LangManager.get().send(CoreLang.CMD_MG_ACCEPTED, player);
                LangManager.get().send(CoreLang.CMD_MG_ACCEPTED_BY, challenger, Map.of("%player%", player.getName()));
                startGame(req.getGame(), challenger, player);
            }

            case "refuse" -> {
                DuelRequest req = DuelRequestManager.refuse(player);
                if (req == null) { LangManager.get().send(CoreLang.CMD_MG_NO_REQUEST, player); return; }
                Player challenger = Bukkit.getPlayer(req.getFrom());
                LangManager.get().send(CoreLang.CMD_MG_REFUSED, player);
                if (challenger != null) LangManager.get().send(CoreLang.CMD_MG_REFUSED_BY, challenger, Map.of("%player%", player.getName()));
            }

            case "snake" -> {
                if (MiniGameRegistry.isPlaying(player.getUniqueId())) {
                    LangManager.get().send(CoreLang.CMD_MG_SELF_IN_GAME, player);
                    return;
                }
                MiniGameType.SNAKE.create(player).start();
            }

            default -> LangManager.get().send(CoreLang.CMD_MG_USAGE, player);
        }
    }

    private void startGame(String game, Player p1, Player p2) {
        MiniGameType type = MiniGameType.fromKey(game);
        if (type != null && type.isTwoPlayer()) {
            type.create(p1, p2).start();
        }
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        return switch (args.getArguments().length) {
            case 1 -> {
                List<String> subs = new ArrayList<>(List.of("duel", "accept", "refuse"));
                subs.addAll(MiniGameType.soloKeys());
                yield subs;
            }
            case 2 -> getOnlinePlayers(args);
            case 3 -> MiniGameType.duelKeys();
            default -> null;
        };
    }
}