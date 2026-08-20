package net.novaproject.novauhc.utils.variable;

import org.bson.Document;

import net.novaproject.novauhc.ability.template.SwitchAbility;
import java.util.logging.Level;
import java.util.IdentityHashMap;
import java.util.Collections;
import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.scenario.random.RandomGameEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;

public final class Variables {


    public static List<VariableDescriptor> of(Object obj) {
        return of(obj.getClass());
    }

    private static final Map<Class<?>, List<VariableDescriptor>> CACHE = new ConcurrentHashMap<>();

    public static List<VariableDescriptor> of(Class<?> clazz) {
        return new ArrayList<>(CACHE.computeIfAbsent(clazz, Variables::build));
    }

    private static List<VariableDescriptor> build(Class<?> clazz) {
        List<VariableDescriptor> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                Var var = field.getAnnotation(Var.class);
                if (var == null) continue;
                if (!seen.add(field.getName())) continue;
                out.add(fromVar(field, var));
            }
        }
        return List.copyOf(out);
    }

    public static void deepScan(Object root) {
        deepScan(root, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private static void deepScan(Object root, Set<Object> visited) {
        if (root == null || !visited.add(root)) return;
        for (VariableDescriptor d : of(root)) {
            if (d.type() != VariableType.ABILITY && d.type() != VariableType.RANDOM_EVENT) continue;
            try {
                Object value = d.field().get(root);
                if (value == null) continue;
                deepScan(value, visited);
                if (value instanceof SwitchAbility sw) {
                    sw.getChildAbilities().forEach(child -> deepScan(child, visited));
                }
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    public static boolean present(Class<?> clazz) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                if (field.isAnnotationPresent(Var.class)) return true;
            }
        }
        return false;
    }

    private static VariableDescriptor fromVar(Field field, Var v) {
        return new VariableDescriptor(field, resolveType(field, v.type()), v.min(), v.max(),
                v.lang(), v.nameKey(), v.descKey(), v.name(), v.desc(), v.common(), v.category());
    }

    public static VariableType resolveType(Field field, VariableType declared) {
        if (declared != null && declared != VariableType.INFER) return declared;
        return infer(field);
    }

    public static VariableType infer(Field field) {
        Class<?> t = field.getType();
        if (t == boolean.class || t == Boolean.class) return VariableType.BOOLEAN;
        if (t == int.class || t == Integer.class || t == long.class || t == Long.class
                || t == short.class || t == Short.class || t == byte.class || t == Byte.class)
            return VariableType.INTEGER;
        if (t == double.class || t == Double.class || t == float.class || t == Float.class)
            return VariableType.DOUBLE;
        if (t == String.class) return VariableType.STRING;
        if (t.isEnum()) return VariableType.ENUM;
        if (Ability.class.isAssignableFrom(t)) return VariableType.ABILITY;
        if (RandomGameEvent.class.isAssignableFrom(t)) return VariableType.RANDOM_EVENT;
        return VariableType.STRING;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void setEnumValue(Field field, Object target, String name) {
        Class<?> t = field.getType();
        if (!t.isEnum() || name == null) return;
        try {
            field.setAccessible(true);
            field.set(target, Enum.valueOf((Class<? extends Enum>) t.asSubclass(Enum.class), name));
        } catch (IllegalArgumentException | IllegalAccessException ignored) {

        }
    }

    public static void setNumber(Field field, Object target, Number value) {
        Class<?> t = field.getType();
        Object coerced = null;
        if (t == int.class || t == Integer.class) coerced = value.intValue();
        else if (t == double.class || t == Double.class) coerced = value.doubleValue();
        else if (t == float.class || t == Float.class) coerced = value.floatValue();
        else if (t == long.class || t == Long.class) coerced = value.longValue();
        if (coerced == null) return;
        write(field, target, coerced);
    }

    public static void write(Field field, Object target, Object value) {
        try {
            java.lang.reflect.Method setter = findSetter(target.getClass(), field);
            if (setter != null) {
                setter.invoke(target, value);
                return;
            }
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Erreur inattendue", e);
        }
    }

    private static java.lang.reflect.Method findSetter(Class<?> clazz, Field field) {
        String name = "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                if (!m.getName().equals(name) || m.getParameterCount() != 1) continue;
                Class<?> p = m.getParameterTypes()[0];
                if (p == field.getType()) return m;
            }
        }
        return null;
    }

    public static String humanize(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        boolean newWord = true;
        char[] chars = fieldName.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            if (ch == '_' || ch == '-') {
                newWord = true;
                continue;
            }
            boolean boundary = i > 0 && Character.isUpperCase(ch)
                    && (Character.isLowerCase(chars[i - 1]) || (i + 1 < chars.length && Character.isLowerCase(chars[i + 1])));
            if (boundary && sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
                sb.append(' ');
                newWord = false;
            }
            if (newWord) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(Character.toUpperCase(ch));
                newWord = false;
            } else {
                sb.append(ch);
            }
        }
        return sb.toString().trim();
    }

    public static final class VariableFormatter {


        public static String format(VariableType type, Number value) {
            if (type == null || value == null) return String.valueOf(value);
            switch (type) {
                case TIME:        return formatTime(value.longValue());
                case PERCENTAGE:  return formatPercentage(value);
                case INTEGER:     return String.valueOf(value.intValue());
                case DOUBLE:      return trimDouble(value.doubleValue());
                default:          return String.valueOf(value);
            }
        }

        public static String formatDelta(VariableType type, Number step, boolean positive) {
            String sign = positive ? "+" : "-";
            if (type == VariableType.TIME) return sign + formatTime(Math.abs(step.longValue()));
            if (type == VariableType.PERCENTAGE) return sign + formatPercentage(abs(step));
            if (type == VariableType.DOUBLE) return sign + trimDouble(Math.abs(step.doubleValue()));
            return sign + Math.abs(step.intValue());
        }

        public static int[] defaultTiers(VariableType type) {
            if (type == VariableType.TIME) return new int[]{3600, 60, 1};
            return new int[]{10, 5, 1};
        }

        public static double defaultMin(VariableType type) {
            switch (type) {
                case TIME, INTEGER, DOUBLE, PERCENTAGE: return 0;
                default: return Double.NaN;
            }
        }

        public static double defaultMax(VariableType type) {
            return Double.NaN;
        }

        private static String formatTime(long seconds) {
            if (seconds == 0) return "0s";
            boolean negative = seconds < 0;
            long s = Math.abs(seconds);
            long h = s / 3600;
            long m = (s % 3600) / 60;
            long sec = s % 60;
            StringBuilder sb = new StringBuilder();
            if (negative) sb.append('-');
            if (h > 0) sb.append(h).append('h');
            if (m > 0) { if (sb.length() > (negative ? 1 : 0)) sb.append(' '); sb.append(m).append('m'); }
            if (sec > 0) { if (sb.length() > (negative ? 1 : 0)) sb.append(' '); sb.append(sec).append('s'); }
            return sb.toString();
        }

        private static String formatPercentage(Number value) {
            double d = value.doubleValue();

            if (Math.abs(d) <= 1) {
                return trimDouble(d * 100) + "%";
            }
            return trimDouble(d) + "%";
        }

        private static String trimDouble(double d) {
            if (d == Math.floor(d) && !Double.isInfinite(d)) return String.valueOf((long) d);
            return String.valueOf(Math.round(d * 100.0) / 100.0);
        }

        private static Number abs(Number n) {
            double d = n.doubleValue();
            return d < 0 ? -d : d;
        }
    }

    public static class VariableSerializer {

        public static Document toDoc(Object obj, List<VariableDescriptor> descriptors) {
            Document doc = new Document();
            for (VariableDescriptor d : descriptors) {
                Field field = d.field();
                try {
                    Object value = field.get(obj);
                    if (value == null) continue;
                    switch (d.type()) {
                        case RANDOM_EVENT -> {
                            if (value instanceof RandomGameEvent<?> event)
                                doc.append(field.getName(), event.toDoc());
                        }
                        case ABILITY -> {
                            if (value instanceof Ability ability)
                                doc.append(field.getName(), ability.abilityToDoc());
                        }
                        case ENUM -> {
                            if (value instanceof Enum<?> en)
                                doc.append(field.getName(), en.name());
                        }
                        default -> doc.append(field.getName(), value);
                    }
                } catch (IllegalAccessException e) {
                    Bukkit.getLogger().log(Level.SEVERE, "Erreur inattendue", e);
                }
            }
            return doc;
        }

        public static void fromDoc(Object obj, Document doc, List<VariableDescriptor> descriptors) {
            if (doc == null) return;
            for (VariableDescriptor d : descriptors) {
                Field field = d.field();
                if (!doc.containsKey(field.getName())) continue;
                try {
                    Object value = doc.get(field.getName());
                    switch (d.type()) {
                        case RANDOM_EVENT -> {
                            if (value instanceof Document subDoc) {
                                Object existing = field.get(obj);
                                if (existing instanceof RandomGameEvent<?> event)
                                    event.fromDoc(subDoc);
                            }
                        }
                        case ABILITY -> {
                            if (value instanceof Document subDoc) {
                                Object existing = field.get(obj);
                                if (existing instanceof Ability ability)
                                    ability.docToAbility(subDoc);
                            }
                        }
                        case ENUM -> {
                            if (value instanceof String s) Variables.setEnumValue(field, obj, s);
                        }
                        default -> {
                            if (value instanceof Number n) {
                                Class<?> ft = field.getType();
                                if (ft == int.class || ft == Integer.class) field.set(obj, n.intValue());
                                else if (ft == double.class || ft == Double.class) field.set(obj, n.doubleValue());
                                else if (ft == float.class || ft == Float.class) field.set(obj, n.floatValue());
                                else if (ft == long.class || ft == Long.class) field.set(obj, n.longValue());
                                else field.set(obj, value);
                            } else {
                                field.set(obj, value);
                            }
                        }
                    }
                } catch (ReflectiveOperationException | RuntimeException e) {
                    Bukkit.getLogger().log(Level.SEVERE, "Champ ignoré: " + field.getName(), e);
                }
            }
        }
    }
}
