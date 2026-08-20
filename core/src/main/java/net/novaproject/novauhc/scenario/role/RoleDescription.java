package net.novaproject.novauhc.scenario.role;

import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.utils.variable.Variables;
import net.novaproject.novauhc.utils.variable.VariableDescriptor;
import net.novaproject.novauhc.ability.template.UseAbility;
import net.novaproject.novauhc.ability.template.SwitchAbility;
import net.novaproject.novauhc.ability.Ability;
import java.lang.reflect.Field;
import java.util.Map;
import net.novaproject.novauhc.utils.chat.TextUtils;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.lang.LangManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import net.novaproject.novauhc.lang.lang.CoreLang;

public class RoleDescription {

    private final Player player;
    private final List<Runnable> lines = new ArrayList<>();

    private RoleDescription(Player player) {
        this.player = player;
    }

    public static RoleDescription of(Player player) {
        return new RoleDescription(player);
    }

    public RoleDescription separator(Lang key) {
        lines.add(() -> player.sendMessage(LangManager.get().get(key, player)));
        return this;
    }

    public RoleDescription space() {
        lines.add(() -> player.sendMessage(" "));
        return this;
    }

    public RoleDescription line(Lang key) {
        lines.add(() -> player.sendMessage(LangManager.get().get(key, player)));
        return this;
    }

    public RoleDescription line(Lang key, Map<String, Object> vars) {
        lines.add(() -> player.sendMessage(LangManager.get().get(key, player, vars)));
        return this;
    }

    public RoleDescription line(Lang prefix, Lang suffix) {
        lines.add(() -> player.sendMessage(
                LangManager.get().get(prefix, player) + LangManager.get().get(suffix, player)));
        return this;
    }

    public RoleDescription hover(Lang text, Lang hover) {
        lines.add(() -> TextUtils.sendHoverLine(
                player,
                LangManager.get().get(text, player),
                LangManager.get().get(hover, player)));
        return this;
    }

    public RoleDescription hover(Lang text, Lang hover, Map<String, Object> vars) {
        lines.add(() -> TextUtils.sendHoverLine(
                player,
                LangManager.get().get(text, player, vars),
                LangManager.get().get(hover, player, vars)));
        return this;
    }

    public RoleDescription raw(String message) {
        lines.add(() -> player.sendMessage(message));
        return this;
    }

    public RoleDescription abilities(Role role) {
        return abilities(role, "§b");
    }

    public RoleDescription abilities(Role role, String color) {
        lines.add(() -> {
            for (VariableDescriptor d
                    : Variables.of(role)) {
                if (d.type() != VariableType.ABILITY) continue;
                Field field = d.field();
                try {
                    Object value = field.get(role);
                    if (!(value instanceof Ability ability)) continue;
                    if (!ability.active()) continue;
                    sendAbilityLine(ability, "", fallback(role, ability, d.desc(player)), color);
                    if (ability instanceof SwitchAbility switchAbility) {
                        for (UseAbility child : switchAbility.getChildAbilities()) {
                            if (!child.active()) continue;
                            sendAbilityLine(child, "  ", fallback(role, child, null), color);
                        }
                    }
                } catch (IllegalAccessException ignored) {
                }
            }
        });
        return this;
    }

    private String fallback(Role role, Ability ability, String variableDescription) {
        String fromRole = role.describeAbility(ability, player);
        return fromRole != null && !fromRole.trim().isEmpty() ? fromRole : variableDescription;
    }

    private void sendAbilityLine(Ability ability, String indent, String fallbackDescription, String color) {
        String text = indent + " §f[" + color + "➲ " + ability.getName() + "§f]";

        String description = ability.getDescription(player);
        if (description == null || description.trim().isEmpty()) {
            description = fallbackDescription;
        }

        StringBuilder hover = new StringBuilder();
        if (description == null || description.trim().isEmpty()) {
            hover.append(LangManager.get().get(CoreLang.COMMON_ROLE_DESC_POWER_NO_DESC, player));
        } else {
            boolean first = true;
            for (String segment : description.split("\n")) {
                for (String wrappedLine : TextUtils.wrap(segment, "§f")) {
                    if (!first) hover.append("\n");
                    hover.append("§8❘ ").append(wrappedLine);
                    first = false;
                }
            }
            if (first) {
                hover.append(LangManager.get().get(CoreLang.COMMON_ROLE_DESC_POWER_NO_DESC, player));
            }
        }
        appendTunables(hover, ability);

        if (ability.getMaxUse() > 0) {
            hover.append("\n§8❘ ").append(LangManager.get().get(CoreLang.COMMON_ROLE_ABILITY_USES, player,
                    Map.of("%uses%", ability.getMaxUse())));
        }
        if (ability.getCooldown() > 0) {
            hover.append("\n§8❘ ").append(LangManager.get().get(CoreLang.COMMON_ROLE_ABILITY_CD, player,
                    Map.of("%time%", TextUtils.getFormattedTime(ability.getCooldown()))));
        }

        TextUtils.sendHoverLine(player, text, hover.toString());
    }

    private void appendTunables(StringBuilder hover, Ability ability) {
        for (VariableDescriptor descriptor : Variables.of(ability)) {
            if (descriptor.type() == VariableType.ABILITY) continue;
            String value = tunableValue(descriptor, ability);
            if (value == null || value.isEmpty()) continue;
            hover.append("\n§8❘ ").append(LangManager.get().get(CoreLang.COMMON_ROLE_ABILITY_TUNABLE, player,
                    Map.of("%name%", descriptor.name(player), "%value%", value)));
        }
    }

    private String tunableValue(VariableDescriptor descriptor, Ability ability) {
        try {
            Object raw = descriptor.field().get(ability);
            if (raw == null) return null;
            if (raw instanceof Number number) {
                return Variables.VariableFormatter.format(descriptor.type(), number);
            }
            if (raw instanceof Boolean flag) {
                return LangManager.get().get(flag
                        ? CoreLang.COMMON_ROLE_ABILITY_TUNABLE_ON
                        : CoreLang.COMMON_ROLE_ABILITY_TUNABLE_OFF, player);
            }
            if (raw instanceof Enum<?> constant) return Variables.humanize(constant.name());
            return String.valueOf(raw);
        } catch (IllegalAccessException ignored) {
            return null;
        }
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public RoleDescription append(RoleDescription other) {
        lines.addAll(other.lines);
        return this;
    }

    public void send() {
        lines.forEach(Runnable::run);
    }
}