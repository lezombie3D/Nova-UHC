package net.novaproject.novauhc.command.cmd.host;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.game.GroupSizeManager;
import net.novaproject.novauhc.game.Systems.SilenceSystem;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.listener.PlayerConnectionEvent;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;
import net.novaproject.novauhc.ui.PlayerLimitsMenus;
import net.novaproject.novauhc.utils.item.PendingItemsManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public final class HostSubs {

    private HostSubs() {
    }

    public static class BypassSub extends HostSub {

        @Override
        protected void run(Player player, CommandArguments args) {
            UHCPlayer host = UHCPlayerManager.get().getPlayer(player);
            host.setBypassed(!host.isBypassed());
            if (host.isBypassed()) {
                LangManager.get().send(CoreLang.COMMON_HOST_BYPASS_ENABLED, player);
                player.setGameMode(GameMode.CREATIVE);
            } else {
                LangManager.get().send(CoreLang.COMMON_HOST_BYPASS_DISABLED, player);
                player.setGameMode(UHCManager.get().isGame() ? GameMode.SURVIVAL : GameMode.ADVENTURE);
            }
        }
    }

    public static class ForceRolesSub extends HostSub {

        @Override
        protected void run(Player player, CommandArguments args) {
            if (!UHCManager.get().isGame()) {
                LangManager.get().send(CoreLang.CMD_FORCEROLES_INGAME_ONLY, player);
                return;
            }
            ScenarioRole<?> scenarioRole = KPIBuilder.activeScenarioRole();
            if (scenarioRole == null) {
                LangManager.get().send(CoreLang.COMMON_ROLE_CMD_NOT_ROLEMODE, player);
                return;
            }
            if (scenarioRole.isRolesDistributed()) {
                LangManager.get().send(CoreLang.COMMON_ROLE_CMD_ROLES_ALREADY, player);
                return;
            }
            scenarioRole.giveRoles();
            Bukkit.broadcastMessage(LangManager.get().get(CoreLang.CMD_FORCEROLES_BROADCAST));
        }
    }

    public static class GiveSub extends HostSub {

        @Override
        protected void run(Player player, CommandArguments args) {
            if (!UHCManager.get().isGame()) {
                LangManager.get().send(CoreLang.COMMON_DISABLE_ACTION, player);
                return;
            }
            ItemStack item = player.getItemInHand().clone();
            if (args.size() == 1) {
                item.setAmount(args.getInt(0, item.getAmount()));
            }
            UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(p -> {
                Player target = p.getPlayer();
                if (target == null || !target.isOnline()) return;
                PendingItemsManager.give(target, item.clone());
                String lang = LangManager.get().get(CoreLang.COMMON_HOST_GIVE)
                        .replace("%item%", item.getType().name())
                        .replace("%amont%", "" + item.getAmount());
                target.sendMessage(lang);
            });
        }
    }

    public static class LimiteSub extends HostSub {

        @Override
        protected void run(Player player, CommandArguments args) {
            if (args.size() < 1) return;
            Player bukkitTarget = Bukkit.getPlayer(args.get(0, ""));
            if (bukkitTarget == null) {
                LangManager.get().send(CoreLang.CMD_INVALID_PLAYER, player);
                return;
            }
            UHCPlayer target = UHCPlayerManager.get().getPlayer(bukkitTarget);
            if (target == null) {
                LangManager.get().send(CoreLang.CMD_INVALID_PLAYER, player);
                return;
            }
            new PlayerLimitsMenus.Hub(player, target.getPlayer()).open();
        }

        @Override
        public List<String> tabComplete(CommandArguments args) {
            return playersFirstArg(args);
        }
    }

    public static class SilenceSub extends HostSub {

        @Override
        protected void run(Player player, CommandArguments args) {
            if (args.size() < 2) {
                LangManager.get().send(CoreLang.CMD_SILENCE_USAGE, player);
                return;
            }
            Player target = Bukkit.getPlayerExact(args.get(0, ""));
            if (target == null) {
                LangManager.get().send(CoreLang.CMD_PLAYER_NOT_FOUND, player);
                return;
            }
            try {
                int seconds = Integer.parseInt(args.get(1, ""));
                SilenceSystem.silence(target.getUniqueId(), seconds);
                LangManager.get().send(CoreLang.COMMON_SILENCE_APPLIED, player,
                        Map.of("%player%", target.getName(), "%time%", seconds));
                LangManager.get().send(CoreLang.COMMON_SILENCED, target, Map.of("%time%", seconds));
            } catch (NumberFormatException e) {
                LangManager.get().send(CoreLang.CMD_SILENCE_USAGE, player);
            }
        }

        @Override
        public List<String> tabComplete(CommandArguments args) {
            return playersFirstArg(args);
        }
    }

    public static class UnsilenceSub extends HostSub {

        @Override
        protected void run(Player player, CommandArguments args) {
            if (args.size() < 1) {
                LangManager.get().send(CoreLang.CMD_UNSILENCE_USAGE, player);
                return;
            }
            Player target = Bukkit.getPlayerExact(args.get(0, ""));
            if (target == null) {
                LangManager.get().send(CoreLang.CMD_PLAYER_NOT_FOUND, player);
                return;
            }
            SilenceSystem.unsilence(target.getUniqueId());
            LangManager.get().send(CoreLang.COMMON_SILENCE_REMOVED, player, Map.of("%player%", target.getName()));
        }

        @Override
        public List<String> tabComplete(CommandArguments args) {
            return playersFirstArg(args);
        }
    }

    public static class SetHostSub extends HostSub {

        @Override
        protected void run(Player player, CommandArguments args) {
            if (!player.hasPermission("novauhc.host")) {
                LangManager.get().send(CoreLang.CMD_SETHOST_ONLY_HOST, player);
                return;
            }
            if (args.size() < 1) {
                LangManager.get().send(CoreLang.CMD_SETHOST_USAGE, player);
                return;
            }
            Player target = Bukkit.getPlayer(args.get(0, ""));
            if (target == null || !target.isOnline()) {
                LangManager.get().send(CoreLang.COMMON_PLAYER_NOT_FOUND, player);
                return;
            }
            if (target.equals(player)) {
                LangManager.get().send(CoreLang.CMD_SETHOST_ALREADY, player);
                return;
            }
            PlayerConnectionEvent.transferHost(target);
            LangManager.get().send(CoreLang.CMD_SETHOST_SUCCESS, player, Map.of("%player%", target.getName()));
        }

        @Override
        public List<String> tabComplete(CommandArguments args) {
            return playersFirstArg(args);
        }
    }

    public static class GroupesSub extends HostSub {

        @Override
        protected void run(Player player, CommandArguments args) {
            GroupSizeManager groups = GroupSizeManager.get();
            if (groups == null) {
                LangManager.get().send(CoreLang.CMD_GROUPES_UNAVAILABLE, player);
                return;
            }
            if (args.size() < 1) {
                LangManager.get().send(CoreLang.CMD_GROUPES_STATUS, player, Map.of(
                        "%size%", groups.getCurrentSize(), "%auto%", groups.isEnabled() ? "on" : "off"));
                LangManager.get().send(CoreLang.CMD_GROUPES_USAGE, player);
                return;
            }
            if (args.get(0, "").equalsIgnoreCase("off")) {
                groups.setForcedSize(-1);
                LangManager.get().send(CoreLang.CMD_GROUPES_CLEARED, player);
                return;
            }
            try {
                int size = Integer.parseInt(args.get(0, ""));
                if (size < 1) throw new NumberFormatException();
                groups.setForcedSize(size);
                LangManager.get().send(CoreLang.CMD_GROUPES_SET, player, Map.of("%size%", size));
            } catch (NumberFormatException e) {
                LangManager.get().send(CoreLang.CMD_GROUPES_USAGE, player);
            }
        }

        @Override
        public List<String> tabComplete(CommandArguments args) {
            if (args.size() == 1) return getStrings(args, "off", "2", "3", "4");
            return List.of();
        }
    }
}
