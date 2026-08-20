package net.novaproject.novauhc.utils.variable;

import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.LangResolver;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Locale;

public final class VariableDescriptor {

    private final Field field;
    private final VariableType type;
    private final double min;
    private final double max;
    private final Class<? extends Lang> lang;
    private final String nameKey;
    private final String descKey;
    private final String inlineName;
    private final String inlineDesc;
    private final boolean common;
    private final String category;
    private final DynamicLang autoName;
    private final DynamicLang autoDesc;

    public VariableDescriptor(Field field, VariableType type, double min, double max,
                              Class<? extends Lang> lang, String nameKey, String descKey,
                              String inlineName, String inlineDesc, boolean common) {
        this(field, type, min, max, lang, nameKey, descKey, inlineName, inlineDesc, common, "");
    }

    public VariableDescriptor(Field field, VariableType type, double min, double max,
                              Class<? extends Lang> lang, String nameKey, String descKey,
                              String inlineName, String inlineDesc, boolean common, String category) {
        this.field = field;
        this.type = type;
        this.min = min;
        this.max = max;
        this.lang = lang;
        this.nameKey = nameKey;
        this.descKey = descKey;
        this.inlineName = inlineName;
        this.inlineDesc = inlineDesc;
        this.common = common;
        this.category = category != null ? category : "";
        field.setAccessible(true);

        boolean explicitKey = lang != null && lang != Lang.class;
        String base = "var." + field.getDeclaringClass().getSimpleName() + "."
                + field.getName().toLowerCase(Locale.ROOT);
        this.autoName = (!explicitKey && inlineName != null && !inlineName.isEmpty())
                ? DynamicLang.of(base + ".name", inlineName) : null;
        this.autoDesc = (!explicitKey && inlineDesc != null && !inlineDesc.isEmpty())
                ? DynamicLang.of(base + ".desc", inlineDesc) : null;
    }

    public Field field() {
        return field;
    }

    public VariableType type() {
        return type;
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    public boolean common() {
        return common;
    }

    public String category() {
        return category;
    }

    public String name(Player player) {
        if (autoName != null && LangManager.get() != null) {
            return LangManager.get().get(autoName, player);
        }
        return LangResolver.resolveVar(lang, nameKey, inlineName, Variables.humanize(field.getName()), player);
    }

    public String name() {
        if (autoName != null && LangManager.get() != null) {
            return LangManager.get().get(autoName);
        }
        return LangResolver.resolveVar(lang, nameKey, inlineName, Variables.humanize(field.getName()), null);
    }

    public String desc(Player player) {
        if (autoDesc != null && LangManager.get() != null) {
            return LangManager.get().get(autoDesc, player);
        }
        return LangResolver.resolveVar(lang, descKey, inlineDesc, "", player);
    }

    public String desc() {
        if (autoDesc != null && LangManager.get() != null) {
            return LangManager.get().get(autoDesc);
        }
        return LangResolver.resolveVar(lang, descKey, inlineDesc, "", null);
    }
}

