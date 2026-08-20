package net.novaproject.novauhc.command.cmd;

import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CompoCMD extends Command.PlayerCommand {

    @Override
    protected void run(Player player, CommandArguments args) {

        ScenarioRole<?> scenarioRole = KPIBuilder.activeScenarioRole();
        if (scenarioRole == null) {
            LangManager.get().send(CoreLang.COMMON_ROLE_CMD_NOT_ROLEMODE, player);
            return;
        }

        boolean staff = isHost(player);
        if (!staff && !scenarioRole.isCompositionOpen()) {
            LangManager.get().send(CoreLang.CMD_COMPO_NOT_PUBLIC, player);
            return;
        }

        campOrder.clear();
        campAlive.clear();
        Map<String, String> campColors = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> campRoles = new LinkedHashMap<>();
        int total = 0;

        if (scenarioRole.getPlayersRoles().isEmpty()) {
            for (Map.Entry<?, ? extends Role> entry : scenarioRole.getRoleConfigs().entrySet()) {
                Integer amount = scenarioRole.getDefault_roles().get(entry.getKey());
                if (amount == null || amount <= 0) continue;
                total += count(campColors, campRoles, entry.getValue(), amount);
            }
            LangManager.get().send(CoreLang.CMD_COMPO_HEADER_PLANNED, player, Map.of("%total%", total));
        } else {
            for (Map.Entry<UHCPlayer, ? extends Role> entry : scenarioRole.getPlayersRoles().entrySet()) {
                Role role = entry.getValue();
                boolean alive = entry.getKey().isStillInGame();
                register(campColors, campRoles, role, role.isCompositionAlive() ? role.getName() : struck(role));
                campAlive.merge(campNameOf(role), alive ? 1 : 0, Integer::sum);
                if (alive) total++;
            }
            LangManager.get().send(CoreLang.CMD_COMPO_HEADER_ALIVE, player, Map.of("%total%", total));
        }

        if (campRoles.isEmpty()) {
            LangManager.get().send(CoreLang.CMD_COMPO_EMPTY, player);
            return;
        }

        campRoles.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> campOrder.getOrDefault(e.getKey(), Integer.MAX_VALUE)))
                .forEach(entry -> {
            String camp = entry.getKey();
            Map<String, Integer> roles = entry.getValue();
            int campTotal = campAlive.containsKey(camp)
                    ? campAlive.get(camp)
                    : roles.values().stream().mapToInt(Integer::intValue).sum();
            LangManager.get().send(CoreLang.CMD_COMPO_CAMP_LINE, player, Map.of("%camp%", campColors.get(camp) + camp, "%count%", campTotal));
            roles.forEach((role, count) ->
                    LangManager.get().send(CoreLang.CMD_COMPO_ROLE_LINE, player, Map.of("%role%", role, "%extra%", count > 1 ? " §7x" + count : "")));
        });
    }

    private final Map<String, Integer> campOrder = new LinkedHashMap<>();
    private final Map<String, Integer> campAlive = new LinkedHashMap<>();

    private String campNameOf(Role role) {
        Camps camp = role.getCamp();
        return camp != null ? camp.getName() : "Sans camp";
    }

    private String struck(Role role) {
        return "§7§m" + ChatColor.stripColor(role.getName());
    }

    private void register(Map<String, String> campColors, Map<String, Map<String, Integer>> campRoles,
                          Role role, String label) {
        Camps camp = role.getCamp();
        String campName = campNameOf(role);
        campColors.putIfAbsent(campName, camp != null ? camp.getColor() : "§7");
        campOrder.putIfAbsent(campName, camp instanceof Enum<?> constant ? constant.ordinal() : Integer.MAX_VALUE);
        campRoles.computeIfAbsent(campName, key -> new LinkedHashMap<>()).merge(label, 1, Integer::sum);
    }

    private int count(Map<String, String> campColors, Map<String, Map<String, Integer>> campRoles,
                      Role role, int amount) {
        Camps camp = role.getCamp();
        String campName = camp != null ? camp.getName() : "Sans camp";
        campColors.putIfAbsent(campName, camp != null ? camp.getColor() : "§7");
        campOrder.putIfAbsent(campName, camp instanceof Enum<?> constant ? constant.ordinal() : Integer.MAX_VALUE);
        campRoles.computeIfAbsent(campName, key -> new LinkedHashMap<>())
                .merge(role.getName(), amount, Integer::sum);
        return amount;
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        return Collections.emptyList();
    }
}

