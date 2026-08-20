package net.novaproject.novauhc.command.cmd.host;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.game.PendingDeathManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;
import net.novaproject.novauhc.utils.item.PendingItemsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class RegiveSub extends HostSub {

    @Override
    protected void run(Player player, CommandArguments args) {
        if (!UHCManager.get().isGame()) {
            LangManager.get().send(CoreLang.COMMON_DISABLE_ACTION, player);
            return;
        }
        if (args.size() < 1) {
            LangManager.get().send(CoreLang.CMD_REGIVE_USAGE, player);
            return;
        }
        Player targetPlayer = Bukkit.getPlayer(args.get(0, ""));
        if (targetPlayer == null) {
            LangManager.get().send(CoreLang.CMD_INVALID_PLAYER, player);
            return;
        }
        UHCPlayer target = UHCPlayerManager.get().getPlayer(targetPlayer);
        if (target == null) {
            LangManager.get().send(CoreLang.CMD_INVALID_PLAYER, player);
            return;
        }
        if (PendingDeathManager.get().isPending(targetPlayer.getUniqueId())) {
            player.sendMessage(ChatColor.RED + targetPlayer.getName()
                    + " est en mort différée — attends la résolution avant de regive.");
            return;
        }
        Role role = KPIBuilder.roleOf(target);
        if (role == null) {
            LangManager.get().send(CoreLang.CMD_REGIVE_NO_ROLE, player, Map.of("%player%", targetPlayer.getName()));
            return;
        }
        int given = 0;
        int pendingBefore = PendingItemsManager.getPendingCount(targetPlayer.getUniqueId());
        for (Ability ability : role.getAbilities()) {
            if (!ability.isManualAbility() || !ability.active()) continue;
            if (ability.getMaterial() == null) continue;
            if (hasAbilityItem(targetPlayer, ability)) continue;
            PendingItemsManager.give(targetPlayer, ability.getItemStack());
            given++;
        }
        int stored = PendingItemsManager.getPendingCount(targetPlayer.getUniqueId()) - pendingBefore;
        if (given == 0) {
            LangManager.get().send(CoreLang.CMD_REGIVE_NOTHING, player, Map.of("%player%", targetPlayer.getName()));
        } else if (stored > 0) {
            LangManager.get().send(CoreLang.CMD_REGIVE_DONE_PENDING, player,
                    Map.of("%count%", given, "%player%", targetPlayer.getName(), "%stored%", stored));
        } else {
            LangManager.get().send(CoreLang.CMD_REGIVE_DONE, player,
                    Map.of("%count%", given, "%player%", targetPlayer.getName()));
        }
        if (given > 0) {
            LangManager.get().send(CoreLang.COMMON_HOST_REGIVE, targetPlayer, Map.of("%count%", String.valueOf(given)));
        }
    }

    private boolean hasAbilityItem(Player targetPlayer, Ability ability) {
        for (ItemStack item : targetPlayer.getInventory().getContents()) {
            if (ability.isAbilityItem(item)) return true;
        }
        return false;
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        return playersFirstArg(args);
    }
}
