package net.novaproject.novauhc.command.cmd;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.template.CommandAbility;
import net.novaproject.novauhc.ability.template.SwitchAbility;
import net.novaproject.novauhc.ability.template.UseAbility;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;

public class RoleCMD extends Command {

    @Override
    public void execute(CommandArguments args) {
        if (!(args.getSender() instanceof Player player)) return;

        ScenarioRole<?> scenarioRole = KPIBuilder.activeScenarioRole();
        if (scenarioRole == null) {
            LangManager.get().send(CoreLang.COMMON_ROLE_CMD_NOT_ROLEMODE, player);
            return;
        }

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null) return;

        String sub = args.size() > 0 ? args.getArgument(0).toLowerCase() : "";

        switch (sub) {
            case "", "me" -> {
                Role role = KPIBuilder.roleOf(uhcPlayer);
                if (role == null) {
                    LangManager.get().send(CoreLang.COMMON_ROLE_CMD_NO_ROLE, player);
                    return;
                }
                role.sendDescription(player);
            }
            case "mates" -> {
                Role role = KPIBuilder.roleOf(uhcPlayer);
                if (role == null) {
                    LangManager.get().send(CoreLang.COMMON_ROLE_CMD_NO_ROLE, player);
                    return;
                }
                if (role.getKnowPlayers().isEmpty()) {
                    LangManager.get().send(CoreLang.COMMON_ROLE_CMD_MATES_NONE, player);
                    return;
                }
                LangManager.get().send(CoreLang.COMMON_ROLE_CMD_MATES_HEADER, player);
                role.sendKnowPlayers();
            }
            case "usage", "help" -> LangManager.get().send(CoreLang.COMMON_ROLE_CMD_USAGE, player);
            case "alter" -> {
                if (!isHost(player)) {
                    LangManager.get().send(CoreLang.COMMON_ROLE_CMD_UNKNOWN, player);
                    return;
                }
                String target = args.size() > 1 ? args.getArgument(1) : "";
                if (target.isEmpty() || target.equalsIgnoreCase("all")) {
                    for (UHCPlayer up : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                        Role role = KPIBuilder.roleOf(up);
                        if (role != null) giveMissingAbilityItems(up, role);
                    }
                    LangManager.get().send(CoreLang.COMMON_ROLE_CMD_ALTER_GIVEN_ALL, player);
                } else {
                    Player targetPlayer = Bukkit.getPlayer(target);
                    UHCPlayer targetUhc = targetPlayer == null ? null : UHCPlayerManager.get().getPlayer(targetPlayer);
                    Role role = targetUhc == null ? null : KPIBuilder.roleOf(targetUhc);
                    if (targetUhc == null || role == null || !targetUhc.isPlaying()) {
                        LangManager.get().send(CoreLang.COMMON_ROLE_CMD_PLAYER_NOT_FOUND, player);
                        return;
                    }
                    giveMissingAbilityItems(targetUhc, role);
                    LangManager.get().send(CoreLang.COMMON_ROLE_CMD_ALTER_GIVEN, player,
                            Map.of("%player%", targetPlayer.getName()));
                }
            }
            case "force" -> {
                if (!isHost(player)) {
                    LangManager.get().send(CoreLang.COMMON_ROLE_CMD_UNKNOWN, player);
                    return;
                }
                if (scenarioRole.isRolesDistributed()
                        || UHCManager.get().getTimer() + 5 > effectiveRolesTimer(scenarioRole)) {
                    LangManager.get().send(CoreLang.COMMON_ROLE_CMD_ROLES_ALREADY, player);
                } else {
                    scenarioRole.setRolesTimer(UHCManager.get().getTimer() + 5);
                    LangManager.get().send(CoreLang.COMMON_ROLE_CMD_ROLES_FORCED, player);
                }
            }
            default -> {
                Role role = KPIBuilder.roleOf(uhcPlayer);
                if (role == null) {
                    LangManager.get().send(CoreLang.COMMON_ROLE_CMD_NO_ROLE, player);
                    return;
                }
                String key = matchKey(role, args.getArguments());
                if (key == null) {
                    LangManager.get().send(CoreLang.COMMON_ROLE_CMD_UNKNOWN, player);
                    LangManager.get().send(CoreLang.COMMON_ROLE_CMD_USAGE, player);
                    return;
                }
                int consumed = key.split(" ").length;
                String[] rest = Arrays.copyOfRange(args.getArguments(), consumed, args.getArguments().length);
                final String resolvedKey = key;
                role.getAbilities().stream()
                        .filter(a -> a instanceof CommandAbility)
                        .map(a -> (CommandAbility) a)
                        .filter(a -> a.getCommandKey().equalsIgnoreCase(resolvedKey))
                        .findFirst()
                        .ifPresent(a -> a.tryCommandUse(player, rest));
            }
        }
    }

    private String matchKey(Role role, String[] arguments) {
        String full = String.join(" ", arguments).toLowerCase();
        String best = null;
        for (Ability a : role.getAbilities()) {
            if (!(a instanceof CommandAbility ca)) continue;
            String ck = ca.getCommandKey().toLowerCase();
            if ((full + " ").startsWith(ck + " ") || full.equals(ck)) {
                if (best == null || ck.length() > best.length()) best = ca.getCommandKey();
            }
        }
        return best;
    }

    private int effectiveRolesTimer(ScenarioRole<?> scenarioRole) {
        return scenarioRole.getRolesTimer() > 0 ? scenarioRole.getRolesTimer() : UHCManager.get().getPvpTimer();
    }

    private void giveMissingAbilityItems(UHCPlayer uhcPlayer, Role role) {
        Player player = uhcPlayer.getPlayer();
        if (player == null || !uhcPlayer.isPlaying()) return;
        for (Ability ability : role.getAbilities()) {
            if (!(ability instanceof UseAbility || ability instanceof SwitchAbility || ability instanceof CommandAbility)) continue;
            if (ability.getMaterial() == null) continue;
            boolean has = false;
            for (ItemStack item : player.getInventory().getContents()) {
                if (ability.isAbilityItem(item)) {
                    has = true;
                    break;
                }
            }
            if (!has && ability.getItemStack().getType() != Material.AIR) {
                player.getInventory().addItem(ability.getItemStack());
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        Role role = null;
        boolean host = false;
        if (args.getSender() instanceof Player player) {
            host = isHost(player);
            UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
            if (KPIBuilder.activeScenarioRole() != null && uhcPlayer != null) {
                role = KPIBuilder.roleOf(uhcPlayer);
            }
        }
        if (args.size() <= 1) {
            List<String> subs = new ArrayList<>(List.of("me", "mates", "usage"));
            if (host) {
                subs.add("alter");
                subs.add("force");
            }
            if (role != null) {
                role.getAbilities().stream()
                        .filter(a -> a instanceof CommandAbility)
                        .map(a -> ((CommandAbility) a).getCommandKey().split(" ")[0])
                        .forEach(subs::add);
            }
            return getStrings(args, subs.toArray(new String[0]));
        }
        String sub = args.getArgument(0).toLowerCase();
        if (sub.equals("alter") && host) {
            List<String> list = getOnlinePlayers(args);
            list.add("all");
            return list;
        }
        if (role != null && args.getSender() instanceof Player player) {
            for (Ability a : role.getAbilities()) {
                if (a instanceof CommandAbility ca && ca.getCommandKey().equalsIgnoreCase(sub)) {
                    String[] rest = Arrays.copyOfRange(args.getArguments(), 1, args.getArguments().length);
                    return ca.onTabComplete(player, rest);
                }
            }
        }
        return List.of();
    }
}

