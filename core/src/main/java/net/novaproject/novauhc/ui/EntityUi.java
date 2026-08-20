package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class EntityUi extends CustomInventory {

    private static final String COW_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGJkYTFkZjgwYmZhMzE0ODUzMzhkOTYzMzgzZDhkZTI2ZjA1ZjI5MzRmODc1Njk2ODYyNDU0ZTdjNzBmNDVmNiJ9fX0=";
    private static final String PIG_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzhlOTg0NTBhNGMwMTdhMjA3NGM5OGJiYzk2MjMxNDkwNDNiNjBhZTVkYTRiNWEwYzA5ZjkyYjg2MjgzMWFiMCJ9fX0=";
    private static final String SHEEP_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTcyMzg5M2RmNGNmYjljNzI0MGZjNDdiNTYwY2NmNmRkZWIxOWRhOTE4M2QzMzA4M2YyYzcxZjQ2ZGFkMjkwYSJ9fX0=";
    private static final String CHICKEN_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzY0ZDYyM2QzMGMyNzhlMmVjNDRjYmM2ZGNlNjc4MDI2OGQ1MDUyNmQ2YTQ5NmQyNjZiMjFlOTBlZjEwN2RjYSJ9fX0=";
    private static final String RABBIT_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2UxNjlhZjlmNzNlZjAwZGNlMGRlZGY3NTBlNDgyMTk3ZTRlZDVlNDVmMWFlZGVlMjVlOTExNTVhZmUwZTFiZCJ9fX0=";
    private static final String MUSHROOM_COW_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzY3YWM4NDJhOGQxMmMwMmQ4YTlmMGQ4MDNlZGE5MThkYzRkMGM4MGUwZjJlYTAyYjRiOWE3NTgxY2Q3YTRiNSJ9fX0=";
    private static final String VILLAGER_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTUxYTE4YjJkNDM3ZWFmNjc0ZTc1YmM0OGE2MDFjNDJmMGRhNDkwNjBlZWRlMjE4MmEwYmJjYzVjYmY3ZjQyZiJ9fX0=";
    private static final String HORSE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjI0ZGFkNGY3ZmM5NGYxNzU5N2YwNGRkMTFiYTg1NmUzMTU2MTRmMzk1MTc3ZmRjMGIzN2Y5MzFhNmNjMWVmIn19fQ==";
    private static final String WOLF_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGViNDFlMzA5ZDgyOTUyZjVmNGE5MDhiZjE2OGU4NDhjODE3NmU1ZjA0ODdlNTc2YmMzMjE5ZWQwNjQ0ZTZlNyJ9fX0=";
    private static final String OCELOT_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTI1MTAxZjg1MzFiMTIwN2Q2MDc1OTdkM2I4N2Q3YzY1Yjk5MGZiOTdkOTE4YzBkYmRkY2YxNmY1ZjcxM2Y3ZiJ9fX0=";
    private static final String SQUID_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGY5Nzk2ZjlmZjFjN2I5NzAzNjNlZTJmNTMyNmFiY2I3MzlkMjdjM2YzNmQ5NDBlOTE4N2YwODA2ZWY0OWY1MSJ9fX0=";
    private static final String BAT_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGUxY2VjY2NhMGY1MjhkOTk3ZTZiMTRhOGU2YzBiMzlkMDc0Yzc5MjAzODljZDJjNzgzZTNhYTVlYWZjMGJhZSJ9fX0=";

    private static final String ZOMBIE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTZmYzg1NGJiODRjZjRiNzY5NzI5Nzk3M2UwMmI3OWJjMTA2OTg0NjBiNTFhNjM5YzYwZTVlNDE3NzM0ZTExIn19fQ==";
    private static final String SKELETON_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDgyYjc4ZGE2ZWU3MTNkNWFjZmU1ZmNiMDc1NGVlNTY5MDA4MzFhNTA5ODMxMzA2NDEwOGRlNmU3ZTQwNjgzOSJ9fX0=";
    private static final String CREEPER_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjQyNTQ4MzhjMzNlYTIyN2ZmY2EyMjNkZGRhYWJmZTBiMDIxNWY3MGRhNjQ5ZTk0NDQ3N2Y0NDM3MGNhNjk1MiJ9fX0=";
    private static final String SPIDER_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzM4ODE3M2Y0ZjgyYTE0MTUzZTA4NmJmMTM3OTA3MjU2ZTUxMmIyMTczMWYwNDcwMDQ3YmYyZDQ1MzU0NWQyMSJ9fX0=";
    private static final String ENDERMAN_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzA5ZjFkZTYxMzVmNGJlYTc4MWM1YThlMGQ2MTA5NWY4MzNlZTI2ODVkODE1NGVjZWE4MTRlZTZkMzI4YTVjNiJ9fX0=";
    private static final String WITCH_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmNlNjYwNDE1N2ZjNGFiNTU5MWU0YmNmNTA3YTc0OTkxOGVlOWM0MWUzNTdkNDczNzZlMGVlNzM0MjA3NGM5MCJ9fX0=";
    private static final String SLIME_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTVhY2Q4YjI0ZjczODlhNDA0MDQzNDhmNDM0NGVlYzIyMzVkNGNhNzE4NDUzYmU5ODAzYjYwYjcxYTEyNTg5MSJ9fX0=";
    private static final String BLAZE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzM3NjIzZjc5ZjdlYjRmM2Y4MGRhNjViNjUyY2M0NGIyMTQ4ZWVhNDFmOWZmZTJlODZhMjNiZGY0OWFiNzdiMSJ9fX0=";
    private static final String GHAST_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2ZlZmNmNDUyYzQ0N2ExYzdjNTQxN2M0YjgzY2MzODFhOGIwZjNmYTVkM2MyYjhkMzdjMjg4ZDM3MjNhYTQwYyJ9fX0=";
    private static final String MAGMA_CUBE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzAyNzgzNDhlNjU3YjlkN2ExNGM4MjQ5ZmNlZjFiNGI5YmQ0ZTQ4YTY1OThkYzU2NDRhNzAxYmQ0OGI0MjcxMSJ9fX0=";
    private static final String PIG_ZOMBIE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzRlOWM2ZTk4NTgyZmZkOGZmOGZlYjMzMjJjZDE4NDljNDNmYjE2YjE1OGFiYjExY2E3YjQyZWRhNzc0M2ViIn19fQ==";
    private static final String GUARDIAN_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMThkMmE3ZmVhN2YyZTBkOTE2YzdjNmQ3OTE0OTM3YmI4ZGQzZmJmZDdmOTQ4M2E0YTM5MTJmNWEwZmM2M2QzIn19fQ==";
    private static final String CAVE_SPIDER_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjA0ZDVmY2IyODlmZTY1YjY3ODY2ODJlMWM3MzZjM2Y3YjE2ZjM5ZDk0MGUzZDJmNDFjZjAwNDA3MDRjNjI4MiJ9fX0=";
    private static final String SILVERFISH_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGE5MWRhYjgzOTFhZjVmZGE1NGFjZDJjMGIxOGZiZDgxOWI4NjVlMWE4ZjFkNjIzODEzZmE3NjFlOTI0NTQwIn19fQ==";
    private static final String ENDERMITE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWJjN2I5ZDM2ZmI5MmI2YmYyOTJiZTczZDMyYzZjNWIwZWNjMjViNDQzMjNhNTQxZmFlMWYxZTY3ZTM5M2EzZSJ9fX0=";

    private record Mob(String key, EntityType type, int slot, String texture,
                       String color, String display, String plural) {
    }

    private static final List<Mob> PASSIVE = List.of(
            new Mob("cow", EntityType.COW, 11, COW_TEXTURE, "§e", "Vaches", "vaches"),
            new Mob("pig", EntityType.PIG, 12, PIG_TEXTURE, "§d", "Cochons", "cochons"),
            new Mob("sheep", EntityType.SHEEP, 14, SHEEP_TEXTURE, "§6", "Moutons", "moutons"),
            new Mob("chicken", EntityType.CHICKEN, 15, CHICKEN_TEXTURE, "§2", "Poulets", "poulets"),
            new Mob("rabbit", EntityType.RABBIT, 20, RABBIT_TEXTURE, "§3", "Lapins", "lapins"),
            new Mob("mushroom-cow", EntityType.MUSHROOM_COW, 21, MUSHROOM_COW_TEXTURE, "§4", "Champimeuh", "champimeuh"),
            new Mob("villager", EntityType.VILLAGER, 23, VILLAGER_TEXTURE, "§5", "Villageois", "villageois"),
            new Mob("horse", EntityType.HORSE, 24, HORSE_TEXTURE, "§7", "Chevaux", "chevaux"),
            new Mob("wolf", EntityType.WOLF, 29, WOLF_TEXTURE, "§8", "Loups", "loups"),
            new Mob("ocelot", EntityType.OCELOT, 30, OCELOT_TEXTURE, "§9", "Ocelots", "ocelots"),
            new Mob("squid", EntityType.SQUID, 32, SQUID_TEXTURE, "§f", "Poulpes", "poulpes"),
            new Mob("bat", EntityType.BAT, 33, BAT_TEXTURE, "§c", "Chauves-souris", "chauves-souris"));

    private static final List<Mob> HOSTILE = List.of(
            new Mob("zombie", EntityType.ZOMBIE, 11, ZOMBIE_TEXTURE, "§e", "Zombies", "zombies"),
            new Mob("skeleton", EntityType.SKELETON, 12, SKELETON_TEXTURE, "§d", "Squelettes", "squelettes"),
            new Mob("creeper", EntityType.CREEPER, 13, CREEPER_TEXTURE, "§6", "Creepers", "creepers"),
            new Mob("spider", EntityType.SPIDER, 14, SPIDER_TEXTURE, "§2", "Araignées", "araignées"),
            new Mob("enderman", EntityType.ENDERMAN, 15, ENDERMAN_TEXTURE, "§3", "Endermen", "endermen"),
            new Mob("witch", EntityType.WITCH, 20, WITCH_TEXTURE, "§4", "Sorcières", "sorcières"),
            new Mob("slime", EntityType.SLIME, 21, SLIME_TEXTURE, "§5", "Slimes", "slimes"),
            new Mob("blaze", EntityType.BLAZE, 22, BLAZE_TEXTURE, "§7", "Blazes", "blazes"),
            new Mob("ghast", EntityType.GHAST, 23, GHAST_TEXTURE, "§8", "Ghasts", "ghasts"),
            new Mob("magma-cube", EntityType.MAGMA_CUBE, 24, MAGMA_CUBE_TEXTURE, "§9", "Cubes de magma", "cubes de magma"),
            new Mob("pig-zombie", EntityType.PIG_ZOMBIE, 29, PIG_ZOMBIE_TEXTURE, "§f", "Cochons-zombies", "cochons-zombies"),
            new Mob("guardian", EntityType.GUARDIAN, 30, GUARDIAN_TEXTURE, "§c", "Gardiens", "gardiens"),
            new Mob("cave-spider", EntityType.CAVE_SPIDER, 31, CAVE_SPIDER_TEXTURE, "§b", "Araignées venimeuses", "araignées venimeuses"),
            new Mob("silverfish", EntityType.SILVERFISH, 32, SILVERFISH_TEXTURE, "§a", "Poissons d’argent", "poissons d’argent"),
            new Mob("endermite", EntityType.ENDERMITE, 33, ENDERMITE_TEXTURE, "§e", "Endermites", "endermites"));

    private static final List<Mob> ALL = Stream.concat(PASSIVE.stream(), HOSTILE.stream()).toList();
    private static final Set<EntityType> HOSTILE_TYPES = EnumSet.copyOf(HOSTILE.stream().map(Mob::type).toList());
    private static final Set<EntityType> FOOD = EnumSet.of(EntityType.COW, EntityType.PIG,
            EntityType.SHEEP, EntityType.CHICKEN, EntityType.RABBIT, EntityType.MUSHROOM_COW);

    private enum Tab {
        PASSIVE("entites", "%passive_mob_"),
        HOSTILE("entites-monstres", "%hostile_mob_");

        private final String menuId;
        private final String prefix;

        Tab(String menuId, String prefix) {
            this.menuId = menuId;
            this.prefix = prefix;
        }

        private List<Mob> mobs() {
            return this == PASSIVE ? EntityUi.PASSIVE : EntityUi.HOSTILE;
        }

        private Tab other() {
            return this == PASSIVE ? HOSTILE : PASSIVE;
        }
    }

    private enum Preset {
        VANILLA("Vanilla"),
        SANS_MONSTRES("Sans monstres"),
        NOURRITURE_SEULE("Nourriture seule"),
        AUCUNE_CREATURE("Aucune créature");

        private final String label;

        Preset(String label) {
            this.label = label;
        }

        private boolean disables(EntityType type) {
            return switch (this) {
                case VANILLA -> false;
                case SANS_MONSTRES -> HOSTILE_TYPES.contains(type);
                case NOURRITURE_SEULE -> !FOOD.contains(type);
                case AUCUNE_CREATURE -> true;
            };
        }
    }

    private final Tab tab;

    public EntityUi(Player player) {
        this(player, Tab.PASSIVE);
    }

    private EntityUi(Player player, Tab tab) {
        super(player);
        this.tab = tab;
    }

    @Override
    public String broadcastKey() {
        return getClass().getName();
    }

    @Override
    public String getTitle() {
        return text("title", "» Entités");
    }

    @Override
    public int getLines() {
        return 5;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }

    @Override
    public void setup() {
        UHCManager uhc = UHCManager.get();

        UiItems.panes(this, DyeColor.RED, 0, 8, 44);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 27, 35, 37, 43);

        if (tab == Tab.PASSIVE) {
            tabButton(2, "tab-passive", Material.WHEAT, "§8┃ §cAnimaux — actif",
                    "§7» Accès §cHost\n"
                            + "\n"
                            + " §c┃ §fAffichez les créatures passives autorisées.\n", null);
            tabButton(6, "tab-hostile", Material.ROTTEN_FLESH, "§8┃ §bMonstres",
                    "§7» Accès §bHost\n"
                            + "\n"
                            + " §b┃ §fAffichez les créatures hostiles autorisées.\n"
                            + "\n"
                            + "§8» §fCliquez pour §butiliser§f.", Tab.HOSTILE);

            int rate = (int) Math.round(uhc.getPassiveSpawnPercent() * 100);
            rateButton("passive-rate", Material.MONSTER_EGG, "§8┃ §aApparition des animaux",
                    "§7» Accès §aHost\n"
                            + "\n"
                            + " §a┃ §fDéfinissez le pourcentage des apparitions\n"
                            + " §a┃ §fnaturelles d’animaux à la génération.\n"
                            + "\n"
                            + " §a☰ §fTaux : %passive_spawn_percent%%\n"
                            + "\n"
                            + "§8» §fCliquez pour §amodifier§f.",
                    "%passive_spawn_percent%", rate,
                    v -> uhc.setPassiveSpawnPercent(v.intValue() / 100.0));
        } else {
            tabButton(2, "tab-passive", Material.WHEAT, "§8┃ §cAnimaux",
                    "§7» Accès §cHost\n"
                            + "\n"
                            + " §c┃ §fAffichez les créatures passives autorisées.\n"
                            + "\n"
                            + "§8» §fCliquez pour §cutiliser§f.", Tab.PASSIVE);
            tabButton(6, "tab-hostile", Material.ROTTEN_FLESH, "§8┃ §bMonstres — actif",
                    "§7» Accès §bHost\n"
                            + "\n"
                            + " §b┃ §fAffichez les créatures hostiles autorisées.\n", null);

            int rate = (int) Math.round(uhc.getSurfaceHostileSpawnPercent() * 100);
            rateButton("surface-hostiles", Material.MOB_SPAWNER, "§8┃ §aMonstres en surface",
                    "§7» Accès §aHost\n"
                            + "\n"
                            + " §a┃ §fDéfinissez le pourcentage des apparitions\n"
                            + " §a┃ §fnaturelles de monstres à la surface.\n"
                            + "\n"
                            + " §a☰ §fTaux : %surface_hostile_spawn_percent%%\n"
                            + "\n"
                            + "§8» §fCliquez pour §amodifier§f.",
                    "%surface_hostile_spawn_percent%", rate,
                    v -> uhc.setSurfaceHostileSpawnPercent(v.intValue() / 100.0));
        }

        tab.mobs().forEach(mob -> mobButton(mob, uhc));

        ItemCreator allowAll = new ItemCreator(Material.EMERALD)
                .setName(text("allow-all.name", "§8┃ §aTout autoriser"))
                .setLores(Arrays.asList(text("allow-all.lore",
                        "§7» Accès §aHost\n"
                                + "\n"
                                + " §a┃ §fAutorisez toutes les créatures de cet onglet.\n"
                                + "\n"
                                + "§8» §fCliquez pour §autiliser§f.").split("\n", -1)));

        addItem(new ActionItem(38, allowAll) {
            @Override
            public void onClick(InventoryClickEvent e) {
                tab.mobs().forEach(mob -> uhc.setEntityDisabled(mob.type(), false));
                openAll();
            }
        });

        Preset current = currentPreset(uhc);
        String label = current == null ? "Personnalisé" : current.label;

        ItemCreator preset = new ItemCreator(Material.BOOK_AND_QUILL)
                .setName(text("preset.name", "§8┃ §ePréréglage : %entity_preset%")
                        .replace("%entity_preset%", label))
                .setLores(Arrays.asList(text("preset.lore",
                        "§7» Accès §eHost\n"
                                + "\n"
                                + " §e┃ §fFaites défiler Vanilla, Sans monstres,\n"
                                + " §e┃ §fNourriture seule et Aucune créature.\n"
                                + "\n"
                                + "§8» §fCliquez pour §eutiliser§f.")
                        .replace("%entity_preset%", label).split("\n", -1)));

        addItem(new ActionItem(40, preset) {
            @Override
            public void onClick(InventoryClickEvent e) {
                Preset next = current == null ? Preset.VANILLA
                        : Preset.values()[(current.ordinal() + 1) % Preset.values().length];
                ALL.forEach(mob -> uhc.setEntityDisabled(mob.type(), next.disables(mob.type())));
                openAll();
            }
        });

        ItemCreator blockAll = new ItemCreator(Material.BARRIER)
                .setName(text("block-all.name", "§8┃ §cTout interdire"))
                .setLores(Arrays.asList(text("block-all.lore",
                        "§7» Accès §cHost\n"
                                + "\n"
                                + " §c┃ §fInterdisez toutes les créatures de cet onglet.\n"
                                + "\n"
                                + "§8» §fCliquez pour §cutiliser§f.").split("\n", -1)));

        addItem(new ActionItem(42, blockAll) {
            @Override
            public void onClick(InventoryClickEvent e) {
                tab.mobs().forEach(mob -> uhc.setEntityDisabled(mob.type(), true));
                openAll();
            }
        });

        addItem(new ActionItem(36, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.RED),
                text("back.name", "§8┃ §7§lRetour"), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new WorldUi.WorldEntityUi(getPlayer()).open();
            }
        });
    }

    private String text(String key, String french) {
        return t(DynamicLang.of("menu." + tab.menuId + "." + key, french));
    }

    private void tabButton(int slot, String key, Material material, String name, String lore, Tab target) {
        ItemCreator icon = new ItemCreator(material)
                .setName(text(key + ".name", name))
                .setLores(Arrays.asList(text(key + ".lore", lore).split("\n", -1)));

        if (target == null) {
            addItem(new StaticItem(slot, icon));
            return;
        }

        addItem(new ActionItem(slot, icon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new EntityUi(getPlayer(), target).open();
            }
        });
    }

    private void rateButton(String key, Material material, String name, String lore,
                            String placeholder, int current, Consumer<Number> apply) {
        String value = String.valueOf(current);

        ItemCreator icon = new ItemCreator(material)
                .setName(text(key + ".name", name).replace(placeholder, value))
                .setLores(Arrays.asList(text(key + ".lore", lore)
                        .replace(placeholder, value).split("\n", -1)));

        addItem(new ActionItem(4, icon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ConfigVarUi(getPlayer(), 25, 10, 5, 25, 10, 5, current, 0, 100,
                        EntityUi.this, VariableType.INTEGER) {
                    @Override
                    public void onChange(Number newValue) {
                        apply.accept(newValue);
                    }
                }.open();
            }
        });
    }

    private void mobButton(Mob mob, UHCManager uhc) {
        String placeholder = tab.prefix + mob.key().replace('-', '_') + "_status%";
        String status = UiItems.onOff(getPlayer(), !uhc.isEntityDisabled(mob.type()));
        String color = mob.color();

        String name = text(mob.key() + ".name", "§8┃ " + color + mob.display())
                .replace(placeholder, status);

        String lore = text(mob.key() + ".lore",
                "§7» Accès " + color + "Host\n"
                        + "\n"
                        + " " + color + "┃ §fAutorisez ou interdisez les " + mob.plural() + "\n"
                        + " " + color + "┃ §fpendant la génération et la partie.\n"
                        + "\n"
                        + " " + color + "☰ §fStatut : " + placeholder + "\n"
                        + "\n"
                        + "§8» §fCliquez pour " + color + "activer ou désactiver§f.")
                .replace(placeholder, status);

        addItem(new ActionItem(mob.slot(), UiItems.createCustomButon(mob.texture(), name,
                Arrays.asList(lore.split("\n", -1))).setGlow(!uhc.isEntityDisabled(mob.type()))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                uhc.setEntityDisabled(mob.type(), !uhc.isEntityDisabled(mob.type()));
                openAll();
            }
        });
    }

    private static Preset currentPreset(UHCManager uhc) {
        for (Preset preset : Preset.values()) {
            boolean match = true;
            for (Mob mob : ALL) {
                if (uhc.isEntityDisabled(mob.type()) != preset.disables(mob.type())) {
                    match = false;
                    break;
                }
            }
            if (match) return preset;
        }
        return null;
    }
}
