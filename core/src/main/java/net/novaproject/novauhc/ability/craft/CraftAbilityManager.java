package net.novaproject.novauhc.ability.craft;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public final class CraftAbilityManager {

    private static final char[] SLOTS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};
    private static final Set<String> REGISTERED_RECIPES = new HashSet<>();

    public static void register(CraftableAbility ability) {
        if (ability == null) return;

        Material[] pattern = ability.getCraftPattern();
        ItemStack result = ability.getCraftResult();
        validate(ability, pattern, result);

        CraftConsultRegistry.register(
                ability.getCraftId(),
                ability.getCraftPermissionAbilityName(),
                ability.getCraftName(),
                ability.getCraftDescription(),
                pattern,
                result
        );

        synchronized (REGISTERED_RECIPES) {
            if (!REGISTERED_RECIPES.add(ability.getCraftId())) return;
        }

        try {
            Bukkit.addRecipe(createRecipe(pattern, result.clone()));
        } catch (Exception e) {
            synchronized (REGISTERED_RECIPES) {
                REGISTERED_RECIPES.remove(ability.getCraftId());
            }
            Bukkit.getLogger().log(Level.WARNING,
                    "[CraftAbility] Recette non enregistree pour " + ability.getCraftId(), e);
        }
    }

    private static ShapedRecipe createRecipe(Material[] pattern, ItemStack result) {
        ShapedRecipe recipe = new ShapedRecipe(result);
        recipe.shape(row(pattern, 0), row(pattern, 3), row(pattern, 6));
        for (int i = 0; i < pattern.length; i++) {
            if (pattern[i] != null) recipe.setIngredient(SLOTS[i], pattern[i]);
        }
        return recipe;
    }

    private static String row(Material[] pattern, int start) {
        StringBuilder builder = new StringBuilder(3);
        for (int i = start; i < start + 3; i++) {
            builder.append(pattern[i] == null ? ' ' : SLOTS[i]);
        }
        return builder.toString();
    }

    private static void validate(CraftableAbility ability, Material[] pattern, ItemStack result) {
        if (ability.getCraftId() == null || ability.getCraftId().isBlank()) {
            throw new IllegalArgumentException("CraftAbility sans craftId");
        }
        if (ability.getCraftPermissionAbilityName() == null || ability.getCraftPermissionAbilityName().isBlank()) {
            throw new IllegalArgumentException("CraftAbility sans abilityName d'autorisation pour " + ability.getCraftId());
        }
        if (pattern == null || pattern.length != 9) {
            throw new IllegalArgumentException("Pattern invalide pour " + ability.getCraftId() + " (9 cases attendues)");
        }
        if (result == null || result.getType() == Material.AIR) {
            throw new IllegalArgumentException("Resultat invalide pour " + ability.getCraftId());
        }
    }

    public static final class CraftConsultRegistry {

        private static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();

        public static void register(String craftId, String abilityName, String displayName, String description, Material[] pattern, ItemStack result) {
            if (craftId == null || craftId.isBlank()) return;
            if (abilityName == null || abilityName.isBlank()) return;
            ENTRIES.put(craftId, new Entry(craftId, abilityName, displayName, description, pattern, result));
        }

        public static List<Entry> getByPermissionAbilityName(String abilityName) {
            if (abilityName == null) return Collections.emptyList();
            List<Entry> out = new ArrayList<>();
            for (Entry e : ENTRIES.values()) {
                if (abilityName.equals(e.abilityName)) out.add(e);
            }
            return out;
        }

        public static Entry getByResult(ItemStack result) {
            if (result == null) return null;
            for (Entry e : ENTRIES.values()) {
                if (e.result != null && e.result.isSimilar(result)) return e;
            }
            return null;
        }

        public static void clear() {
            ENTRIES.clear();
        }

        public record Entry(String craftId, String abilityName, String displayName, String description, Material[] pattern,
                            ItemStack result) {
            public Entry(String craftId, String abilityName, String displayName, String description, Material[] pattern, ItemStack result) {
                this.craftId = craftId;
                this.abilityName = abilityName;
                this.displayName = displayName;
                this.description = description;
                this.pattern = pattern == null ? null : pattern.clone();
                this.result = result == null ? null : result.clone();
            }
        }
    }
}

