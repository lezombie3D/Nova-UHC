package net.novaproject.novauhc.ability;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;
import net.novaproject.novauhc.ui.CustomInventory;
import net.novaproject.novauhc.ui.MenuGrid;
import net.novaproject.novauhc.ui.UiItems;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.utils.chat.TextUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.item.PendingItemsManager;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.ToDoubleFunction;

public final class FormSet<F extends Enum<F>> {

    private static final List<FormSet<?>> REGISTRY = new ArrayList<>();

    private final String name;
    private final Map<F, FormDef<F>> defs = new LinkedHashMap<>();
    private F defaultForm;
    private boolean definitive;
    private IntSupplier autoPickSeconds = () -> 0;
    private IntSupplier switchCooldownSeconds = () -> 0;

    private final Map<UUID, F> current = new HashMap<>();
    private final Map<UUID, Long> lastSwitch = new HashMap<>();
    private final Set<UUID> chosen = new HashSet<>();
    private final Map<UUID, Map<F, Set<Class<? extends Ability>>>> pickedPools = new HashMap<>();

    private FormSet(String name) {
        this.name = name;
    }

    public static <F extends Enum<F>> FormSet<F> of(String name, Class<F> formType) {
        FormSet<F> set = new FormSet<>(name);
        REGISTRY.add(set);
        return set;
    }

    public static void clearAllStates() {
        REGISTRY.forEach(FormSet::clearState);
    }

    public static boolean isFormDisabled(UUID uuid, Ability ability) {
        for (FormSet<?> set : REGISTRY) {
            if (set.isDisabledByForm(uuid, ability)) return true;
        }
        return false;
    }

    private boolean isDisabledByForm(UUID uuid, Ability ability) {
        F owner = formOf(ability);
        if (owner == null) return false;
        if (owner != current(uuid)) return true;
        FormDef<F> def = defs.get(owner);
        if (def == null || def.pool.isEmpty() || !def.pool.contains(ability.getClass())) return false;
        Map<F, Set<Class<? extends Ability>>> pools = pickedPools.get(uuid);
        Set<Class<? extends Ability>> kept = pools == null ? null : pools.get(owner);
        return kept != null && !kept.contains(ability.getClass());
    }

    public String getName() {
        return name;
    }

    public FormSet<F> defaultForm(F form) {
        this.defaultForm = form;
        return this;
    }

    public FormSet<F> definitive() {
        this.definitive = true;
        return this;
    }

    public FormSet<F> autoPickAfter(IntSupplier seconds) {
        if (seconds != null) this.autoPickSeconds = seconds;
        return this;
    }

    public FormSet<F> switchCooldown(IntSupplier seconds) {
        if (seconds != null) this.switchCooldownSeconds = seconds;
        return this;
    }

    public FormSet<F> form(F form, Consumer<FormDef<F>> config) {
        FormDef<F> def = defs.computeIfAbsent(form, FormDef::new);
        config.accept(def);
        if (defaultForm == null) defaultForm = form;
        return this;
    }

    public F current(UUID uuid) {
        return current.getOrDefault(uuid, defaultForm);
    }

    public boolean hasChosen(UUID uuid) {
        return chosen.contains(uuid);
    }

    public boolean isActive(Player player, F form) {
        return player != null && current(player.getUniqueId()) == form
                && (!definitive || hasChosen(player.getUniqueId()));
    }

    public boolean requireForm(Player player, F form) {
        if (isActive(player, form)) return true;
        if (player != null) {
            LangManager.get().send(CoreLang.COMMON_FORM_WRONG, player,
                    Map.of("%form%", displayOf(form)));
        }
        return false;
    }

    public double speedBonus(UUID uuid) {
        return bonus(uuid, FormDef::speedValue);
    }

    public double strengthBonus(UUID uuid) {
        return bonus(uuid, FormDef::strengthValue);
    }

    public double resistanceBonus(UUID uuid) {
        return bonus(uuid, FormDef::resistanceValue);
    }

    public String displayOf(F form) {
        FormDef<F> def = defs.get(form);
        return def != null ? def.getDisplay() : String.valueOf(form);
    }

    public int switchRemainingSeconds(UUID uuid) {
        if (definitive) return -1;
        int cooldown = Math.max(0, switchCooldownSeconds.getAsInt());
        if (cooldown <= 0) return 0;
        Long last = lastSwitch.get(uuid);
        if (last == null) return 0;
        long elapsed = (System.currentTimeMillis() - last) / 1000L;
        return (int) Math.max(0, cooldown - elapsed);
    }

    public void bootstrap(UHCPlayer uhcPlayer) {
        if (uhcPlayer == null) return;
        UUID uuid = uhcPlayer.getUuid();
        Player player = uhcPlayer.getPlayer();
        if (definitive) {
            if (player != null && player.isOnline()) {
                applyLoadout(player, null, false, false);
                openSelector(player);
            }
            scheduleAutoPick(uuid);
            return;
        }
        if (player != null && player.isOnline() && defaultForm != null) {
            applyLoadout(player, defaultForm, false, false);
        }
    }

    public void openSelector(Player player) {
        if (player == null || defs.isEmpty()) return;
        new FormSelectionUi<>(player, this).open();
    }

    public boolean select(Player player, F form) {
        if (player == null || form == null || !defs.containsKey(form)) return false;
        UUID uuid = player.getUniqueId();
        if (definitive && hasChosen(uuid)) {
            LangManager.get().send(CoreLang.COMMON_FORM_ALREADY_CHOSEN, player);
            return false;
        }
        if (!definitive && current.containsKey(uuid) && current(uuid) == form) {
            LangManager.get().send(CoreLang.COMMON_FORM_ALREADY, player);
            return false;
        }
        int remaining = switchRemainingSeconds(uuid);
        if (!definitive && remaining > 0 && current.containsKey(uuid)) {
            LangManager.get().send(CoreLang.COMMON_FORM_SWITCH_CD, player,
                    Map.of("%time%", TextUtils.getFormattedTime(remaining)));
            return false;
        }
        applyLoadout(player, form, !definitive, true);
        if (definitive) chosen.add(uuid);
        return true;
    }

    public void applyLoadout(Player player, F form, boolean recordSwitch, boolean announce) {
        if (player == null) return;
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        Role role = uhcPlayer == null ? null : KPIBuilder.roleOf(uhcPlayer);
        if (role == null) return;

        UUID uuid = player.getUniqueId();
        if (form != null) current.put(uuid, form);
        if (recordSwitch) lastSwitch.put(uuid, System.currentTimeMillis());

        Set<Class<? extends Ability>> keptPool = resolvePool(uuid, form);

        for (Ability ability : role.getAbilities()) {
            F owner = formOf(ability);
            if (owner == null) continue;
            stripItems(player, ability);
            boolean active = owner == form
                    && (!isPooled(owner, ability) || keptPool.contains(ability.getClass()));
            if (active) {
                ability.setDisabled(false);
                if (ability.getMaterial() != null) {
                    PendingItemsManager.give(player, ability.getItemStack());
                }
            } else {
                ability.setDisabled(true);
            }
        }

        if (announce) {
            LangManager.get().send(CoreLang.COMMON_FORM_SWITCHED, player,
                    Map.of("%form%", displayOf(form)));
        }
    }

    public F formOf(Ability ability) {
        if (ability == null) return null;
        for (FormDef<F> def : defs.values()) {
            if (def.fixed.contains(ability.getClass()) || def.pool.contains(ability.getClass())) {
                return def.form;
            }
        }
        return null;
    }

    public List<F> forms() {
        return List.copyOf(defs.keySet());
    }

    FormDef<F> defOf(F form) {
        return defs.get(form);
    }

    boolean isDefinitive() {
        return definitive;
    }

    public void clearState() {
        current.clear();
        lastSwitch.clear();
        chosen.clear();
        pickedPools.clear();
    }

    private boolean isPooled(F form, Ability ability) {
        FormDef<F> def = defs.get(form);
        return def != null && def.pool.contains(ability.getClass());
    }

    private Set<Class<? extends Ability>> resolvePool(UUID uuid, F form) {
        FormDef<F> def = defs.get(form);
        if (def == null || def.pool.isEmpty()) return Set.of();
        return pickedPools
                .computeIfAbsent(uuid, k -> new HashMap<>())
                .computeIfAbsent(form, f -> {
                    List<Class<? extends Ability>> shuffled = new ArrayList<>(def.pool);
                    Collections.shuffle(shuffled);
                    int count = Math.max(0, Math.min(def.poolCount, shuffled.size()));
                    return new HashSet<>(shuffled.subList(0, count));
                });
    }

    private double bonus(UUID uuid, ToDoubleFunction<FormDef<F>> getter) {
        F form = current(uuid);
        if (form == null || (definitive && !hasChosen(uuid))) return 0.0;
        FormDef<F> def = defs.get(form);
        if (def == null) return 0.0;
        return getter.applyAsDouble(def);
    }

    private void scheduleAutoPick(UUID uuid) {
        int seconds = Math.max(0, autoPickSeconds.getAsInt());
        if (seconds <= 0) return;
        Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
            if (hasChosen(uuid)) return;
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) return;
            UHCPlayer up = UHCPlayerManager.get().getPlayer(player);
            if (up == null || !up.isPlaying()) return;
            List<F> available = forms();
            if (available.isEmpty()) return;
            F picked = available.get(new Random().nextInt(available.size()));
            if (select(player, picked)) {
                LangManager.get().send(CoreLang.COMMON_FORM_AUTO_PICKED, player,
                        Map.of("%form%", displayOf(picked)));
            }
        }, seconds * 20L);
    }

    private void stripItems(Player player, Ability ability) {
        if (ability.getMaterial() == null) return;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (ItemCreator.isAbilityItem(contents[i]) && ability.isAbilityItem(contents[i])) {
                player.getInventory().setItem(i, null);
            }
        }
        PendingItemsManager.drop(player.getUniqueId(), ability::isAbilityItem);
    }

    public static final class FormDef<F extends Enum<F>> {

        private final F form;
        private Material icon = Material.NETHER_STAR;
        private String display;
        private final List<String> lore = new ArrayList<>();
        private final Set<Class<? extends Ability>> fixed = new HashSet<>();
        private final Set<Class<? extends Ability>> pool = new HashSet<>();
        private int poolCount;
        private DoubleSupplier speed;
        private DoubleSupplier strength;
        private DoubleSupplier resistance;

        private FormDef(F form) {
            this.form = form;
            this.display = String.valueOf(form);
        }

        public FormDef<F> icon(Material icon) {
            if (icon != null) this.icon = icon;
            return this;
        }

        public FormDef<F> display(String display) {
            if (display != null) this.display = display;
            return this;
        }

        public FormDef<F> lore(String... lines) {
            for (String line : lines) {
                if (line != null) lore.add(line);
            }
            return this;
        }

        @SafeVarargs
        public final FormDef<F> abilities(Class<? extends Ability>... classes) {
            Collections.addAll(fixed, classes);
            return this;
        }

        @SafeVarargs
        public final FormDef<F> pool(int count, Class<? extends Ability>... classes) {
            this.poolCount = Math.max(0, count);
            Collections.addAll(pool, classes);
            return this;
        }

        public FormDef<F> speed(DoubleSupplier percent) {
            this.speed = percent;
            return this;
        }

        public FormDef<F> strength(DoubleSupplier percent) {
            this.strength = percent;
            return this;
        }

        public FormDef<F> resistance(DoubleSupplier percent) {
            this.resistance = percent;
            return this;
        }

        Material getIcon() {
            return icon;
        }

        String getDisplay() {
            return display;
        }

        List<String> getLore() {
            return lore;
        }

        double speedValue() {
            return speed == null ? 0.0 : speed.getAsDouble();
        }

        double strengthValue() {
            return strength == null ? 0.0 : strength.getAsDouble();
        }

        double resistanceValue() {
            return resistance == null ? 0.0 : resistance.getAsDouble();
        }
    }

    public static final class FormSelectionUi<F extends Enum<F>> extends CustomInventory {

        private static final int ROW_FROM = 1;
        private static final int ROW_TO = 2;
        private static final int COL_FROM = 1;
        private static final int COL_TO = 7;
        private static final int PER_PAGE = MenuGrid.spacedCapacity(ROW_FROM, ROW_TO, COL_FROM, COL_TO);

        private final FormSet<F> formSet;
        private int pages = 1;

        public FormSelectionUi(Player player, FormSet<F> formSet) {
            super(player);
            this.formSet = formSet;
        }

        @Override
        public String getTitle() {
            return t(DynamicLang.of("menu.formes.title", "» Formes — %name%"),
                    Map.of("%name%", formSet.getName()));
        }

        @Override
        public int getLines() {
            return 3;
        }

        @Override
        public boolean isRefreshAuto() {
            return false;
        }

        @Override
        public int getCategories() {
            return pages;
        }

        @Override
        public void setup() {
            UiItems.panes(this, DyeColor.PURPLE, 0, 8, 18, 26);
            UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 19, 25);

            pages = paginate(formSet.forms(), PER_PAGE,
                    n -> MenuGrid.spaced(ROW_FROM, ROW_TO, COL_FROM, COL_TO, n),
                    (form, slot) -> addItem(new ActionItem(slot, render(form)) {
                        @Override
                        public void onClick(InventoryClickEvent e) {
                            getPlayer().closeInventory();
                            formSet.select(getPlayer(), form);
                        }
                    }));
        }

        private ItemCreator render(F form) {
            FormDef<F> def = formSet.defOf(form);
            ItemCreator icon = new ItemCreator(def.getIcon())
                    .setName(t(DynamicLang.of("menu.formes.form-dummy.name", "§8┃ §a%nom_de_la_forme%"))
                            .replace("%nom_de_la_forme%", def.getDisplay()))
                    .setLores(Arrays.asList(t(DynamicLang.of("menu.formes.form-dummy.lore",
                            "§7» Élément §adynamique\n\n §a┃ §fUne entrée est générée pour chaque forme disponible.\n §a┃ §fCliquez pour adopter cette forme.\n\n§8» §fCliquez pour §autiliser§f."))
                            .split("\n", -1)));
            icon.addLore("");

            List<String> abilityNames = abilityNamesFor(form);
            if (!abilityNames.isEmpty()) {
                icon.addLore(t(CoreLang.COMMON_ROLE_DESC_ABILITIES));
                for (String abilityName : abilityNames) {
                    icon.addLore("§8✦ §f" + abilityName);
                }
            }

            addBonusLore(icon, CoreLang.COMMON_FORM_UI_SPEED, def.speedValue());
            addBonusLore(icon, CoreLang.COMMON_FORM_UI_STRENGTH, def.strengthValue());
            addBonusLore(icon, CoreLang.COMMON_FORM_UI_RESISTANCE, def.resistanceValue());

            for (String line : def.getLore()) {
                icon.addLore(line);
            }

            if (formSet.isDefinitive()) {
                icon.addLore("");
                icon.addLore(t(CoreLang.COMMON_FORM_DEFINITIVE));
            }
            icon.addLore("");
            return icon;
        }

        private List<String> abilityNamesFor(F form) {
            List<String> names = new ArrayList<>();
            for (Ability ability : AbilityManager.get().abilitiesOf(getPlayer())) {
                if (formSet.formOf(ability) == form && ability.getMaterial() != null) {
                    names.add(ability.getName());
                }
            }
            return names;
        }

        private void addBonusLore(ItemCreator creator, CoreLang key, double value) {
            if (value <= 0) return;
            creator.addLore(t(key, Map.of("%value%", (int) Math.round(value * 100))));
        }
    }
}
