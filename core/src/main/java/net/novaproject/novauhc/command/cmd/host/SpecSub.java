package net.novaproject.novauhc.command.cmd.host;

import net.novaproject.novauhc.display.TabFreezeService;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.game.SpectatorNotifier;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lobby.HotbarManager;
import net.novaproject.novauhc.lobby.RankManager.Rank;
import net.novaproject.novauhc.lobby.RankManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.display.DisplayService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpecSub extends HostSub {

    private static final Lang MOD_SPEC_ON = DynamicLang.of("ui.specnotify.spec_on",
            "§8▸ §7%staff% §7a mis §f%player% §7en spectateur");
    private static final Lang MOD_SPEC_OFF = DynamicLang.of("ui.specnotify.spec_off",
            "§8▸ §7%staff% §7a retiré §f%player% §7des spectateurs");

    public static boolean setSpectator(Player staff, UHCPlayer target, boolean spec) {
        Player targetPlayer = target == null ? null : target.getPlayer();
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            LangManager.get().send(CoreLang.COMMON_PLAYER_NOT_FOUND, staff);
            return false;
        }

        if (spec) {
            if (target.isSpec()) {
                LangManager.get().send(CoreLang.CMD_SPEC_ALREADY, staff, Map.of("%player%", targetPlayer.getName()));
                return false;
            }
            boolean wasPlaying = target.isPlaying();
            target.setSpec(true);
            TabFreezeService.release(target.getUuid());
            target.setPlaying(false);
            target.setTeam(Optional.empty());
            targetPlayer.setGameMode(GameMode.SPECTATOR);
            for (PotionEffect e : targetPlayer.getActivePotionEffects()) {
                targetPlayer.removePotionEffect(e.getType());
            }
            targetPlayer.setFireTicks(0);
            targetPlayer.setHealth(targetPlayer.getMaxHealth());
            targetPlayer.setFoodLevel(20);
            RankManager.get().applyTag(targetPlayer, Rank.SPECTATOR);
            DisplayService.staffMod(targetPlayer, true);
            for (UHCPlayer up : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player op = up.getPlayer();
                if (op != null && op.isOnline()) op.hidePlayer(targetPlayer);
            }
            LangManager.get().send(CoreLang.CMD_SPEC_NOW, targetPlayer);
            announceSpec(targetPlayer, staff, wasPlaying, true);
            return true;
        }

        if (!target.isSpec()) {
            LangManager.get().send(CoreLang.CMD_SPEC_NOT_SPEC, staff, Map.of("%player%", targetPlayer.getName()));
            return false;
        }
        target.setSpec(false);
        DisplayService.staffMod(targetPlayer, false);
        if (UHCManager.get().isLobby()) {
            target.setPlaying(true);
            targetPlayer.setGameMode(GameMode.ADVENTURE);
            targetPlayer.teleport(Common.get().getLobbySpawn());
            DisplayService.nametag(targetPlayer, "", "", "");
            HotbarManager.get().giveHotbar(targetPlayer);
        }
        for (Player op : Bukkit.getOnlinePlayers()) op.showPlayer(targetPlayer);
        LangManager.get().send(CoreLang.CMD_SPEC_NO_MORE, targetPlayer);
        announceSpec(targetPlayer, staff, UHCManager.get().isLobby(), false);
        return true;
    }

    private static void announceSpec(Player target, Player staff, boolean publicly, boolean spec) {
        if (publicly) {
            Bukkit.broadcastMessage(Common.get().getInfoTag() + ChatColor.GOLD + target.getName()
                    + (spec ? " est désormais spectateur." : " n'est plus spectateur."));
            return;
        }
        SpectatorNotifier.notifySpectators(spec ? MOD_SPEC_ON : MOD_SPEC_OFF, Map.of(
                "%staff%", staff == null ? "?" : staff.getName(),
                "%player%", target.getName()), target);
    }

    @Override
    protected void run(Player player, CommandArguments args) {
        if (args.size() < 1) {
            LangManager.get().send(CoreLang.CMD_SPEC_USAGE, player);
            return;
        }
        String action = args.get(0, "").toLowerCase();
        switch (action) {
            case "list": {
                List<UHCPlayer> specs = UHCPlayerManager.get().getOnlineUHCPlayers().stream()
                        .filter(UHCPlayer::isSpec)
                        .collect(Collectors.toList());
                if (specs.isEmpty()) {
                    LangManager.get().send(CoreLang.CMD_SPEC_NONE, player);
                    return;
                }
                StringBuilder sb = new StringBuilder(ChatColor.GOLD + "Spectateurs (" + specs.size() + ") : " + ChatColor.WHITE);
                for (int i = 0; i < specs.size(); i++) {
                    Player p = specs.get(i).getPlayer();
                    sb.append(p == null ? "?" : p.getName());
                    if (i < specs.size() - 1) sb.append(", ");
                }
                player.sendMessage(sb.toString());
                return;
            }
            case "add":
            case "remove": {
                if (args.size() < 2) {
                    LangManager.get().send(CoreLang.CMD_SPEC_ACTION_USAGE, player, Map.of("%action%", action));
                    return;
                }
                Player targetPlayer = Bukkit.getPlayer(args.get(1, ""));
                if (targetPlayer == null) {
                    LangManager.get().send(CoreLang.COMMON_PLAYER_NOT_FOUND, player);
                    return;
                }
                UHCPlayer target = UHCPlayerManager.get().getPlayer(targetPlayer);
                if (target == null) {
                    LangManager.get().send(CoreLang.CMD_SPEC_NOT_REGISTERED, player);
                    return;
                }
                setSpectator(player, target, action.equals("add"));
                return;
            }
            default:
                LangManager.get().send(CoreLang.CMD_SPEC_USAGE, player);
        }
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        if (args.size() == 1) return getStrings(args, "add", "remove", "list");
        if (args.size() == 2 && (args.get(0, "").equalsIgnoreCase("add") || args.get(0, "").equalsIgnoreCase("remove"))) {
            return getOnlinePlayers(args);
        }
        return List.of();
    }
}
