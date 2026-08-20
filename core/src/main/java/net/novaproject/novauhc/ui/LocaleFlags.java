package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class LocaleFlags {

    private static final Map<String, Pattern[]> FLAGS = new LinkedHashMap<>();
    private static final Map<String, DyeColor> BASES = new LinkedHashMap<>();

    static {
        flag("fr", DyeColor.WHITE,
                new Pattern(DyeColor.BLUE, PatternType.STRIPE_LEFT),
                new Pattern(DyeColor.RED, PatternType.STRIPE_RIGHT));

        flag("en", DyeColor.BLUE,
                new Pattern(DyeColor.WHITE, PatternType.CROSS),
                new Pattern(DyeColor.WHITE, PatternType.STRAIGHT_CROSS),
                new Pattern(DyeColor.RED, PatternType.STRIPE_MIDDLE),
                new Pattern(DyeColor.RED, PatternType.STRIPE_CENTER));

        flag("de", DyeColor.RED,
                new Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP),
                new Pattern(DyeColor.YELLOW, PatternType.STRIPE_BOTTOM));

        flag("es", DyeColor.YELLOW,
                new Pattern(DyeColor.RED, PatternType.STRIPE_TOP),
                new Pattern(DyeColor.RED, PatternType.STRIPE_BOTTOM));

        flag("it", DyeColor.WHITE,
                new Pattern(DyeColor.GREEN, PatternType.STRIPE_LEFT),
                new Pattern(DyeColor.RED, PatternType.STRIPE_RIGHT));

        flag("pt", DyeColor.GREEN,
                new Pattern(DyeColor.YELLOW, PatternType.RHOMBUS_MIDDLE),
                new Pattern(DyeColor.BLUE, PatternType.CIRCLE_MIDDLE));

        flag("nl", DyeColor.WHITE,
                new Pattern(DyeColor.RED, PatternType.STRIPE_TOP),
                new Pattern(DyeColor.BLUE, PatternType.STRIPE_BOTTOM));

        flag("ru", DyeColor.WHITE,
                new Pattern(DyeColor.BLUE, PatternType.STRIPE_MIDDLE),
                new Pattern(DyeColor.RED, PatternType.STRIPE_BOTTOM));

        flag("pl", DyeColor.WHITE,
                new Pattern(DyeColor.RED, PatternType.STRIPE_BOTTOM));

        flag("ja", DyeColor.WHITE,
                new Pattern(DyeColor.RED, PatternType.CIRCLE_MIDDLE));

        flag("zh", DyeColor.RED,
                new Pattern(DyeColor.YELLOW, PatternType.FLOWER));

        flag("tr", DyeColor.RED,
                new Pattern(DyeColor.WHITE, PatternType.CIRCLE_MIDDLE),
                new Pattern(DyeColor.RED, PatternType.RHOMBUS_MIDDLE));
    }

    private LocaleFlags() {
    }

    private static void flag(String language, DyeColor base, Pattern... patterns) {
        BASES.put(language, base);
        FLAGS.put(language, patterns);
    }

    public static boolean has(String locale) {
        return FLAGS.containsKey(language(locale));
    }

    public static ItemCreator icon(String locale) {
        String language = language(locale);
        Pattern[] patterns = FLAGS.get(language);
        if (patterns == null) return new ItemCreator(Material.PAPER);

        ItemCreator banner = new ItemCreator(Material.BANNER);
        banner.setDurability((short) BASES.get(language).getDyeData());

        List<Pattern> all = new ArrayList<>(List.of(patterns));
        banner.setPatterns(new ArrayList<>(all));
        return banner;
    }

    private static String language(String locale) {
        if (locale == null) return "";
        String trimmed = locale.trim().toLowerCase(Locale.ROOT);
        int separator = trimmed.indexOf('_');
        return separator > 0 ? trimmed.substring(0, separator) : trimmed;
    }
}
