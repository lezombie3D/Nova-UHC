package net.novaproject.novauhc.display.apollo;

import net.novaproject.novauhc.display.apollo.VisualFeedback.ApolloConfig;
import net.novaproject.novauhc.display.apollo.VisualFeedback.ApolloConfig.Capability;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.lang.UiLang;
import net.novaproject.novauhc.ui.CustomInventory;
import net.novaproject.novauhc.ui.DefaultUi;
import net.novaproject.novauhc.ui.UiItems;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.Map;

public class ApolloConfigUi extends CustomInventory {

    private static final String BACK_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDIyNDZjOGFiZTdmMzczMmNlZjc1ODM3OTM1M2EyODgxNWE5YzM3MDdiMzIxMjk1NzMxZTIyMTVhYmFmNmZhZiJ9fX0=";
    private static final String MOD_ALLOWED_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzExMDFjNDk4YWFhYzkzZTc2YjhmMTZkMjljYmZhNDczZWQyNGQ5YzZjNzU1NjNjMjdlZWRkNDljOTYzZTk4YiJ9fX0=";
    private static final String MOD_BLOCKED_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWQ3OGNjMzkxYWZmYjgwYjJiMzVlYjczNjRmZjc2MmQzODQyNGMwN2U3MjRiOTkzOTZkZWU5MjFmYmJjOWNmIn19fQ==";

    private static final String MOD_ALLOWED_LORE = "§7» §f§aAutorisé§f §8┃ §fClique pour §cdésactiver";
    private static final String MOD_BLOCKED_LORE = "§7» §f§cDésactivé§f §8┃ §fClique pour §aautoriser§f";

    public ApolloConfigUi(Player player) {
        super(player);
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu.modules-lunar.title", "» Modules Lunar✓"));
    }

    @Override
    public int getLines() {
        return 6;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }

    @Override
    public void setup() {
        UiItems.panes(this, DyeColor.LIGHT_BLUE, 0, 8, 53);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 36, 44, 46, 52);

        ItemCreator lunar = new ItemCreator(Material.BEACON)
                .setName(t(DynamicLang.of("menu.modules-lunar.btn-4.name", "§8┃ §bLunar")));
        lunar.setLores(Arrays.asList(t(DynamicLang.of("menu.modules-lunar.btn-4.lore",
                "§7» Accès §bHost\n"
                        + "\n"
                        + " §b┃ §fPermet §ad'activé §f/ §cdesactivé\n"
                        + " §b┃ §fles visuels §bLunar.\n"
                        + "\n"
                        + " §b☰ §fStatus Lunar : %lunar_active%\n"
                        + " §b☰ §fAppolo Present : %appolo_active%"),
                Map.of("%lunar_active%", UiItems.onOff(getPlayer(), VisualFeedback.isEnabled()),
                        "%appolo_active%", t(VisualFeedback.isApolloPresent() ? UiLang.LUNAR_YES : UiLang.LUNAR_NO)))
                .split("\n", -1)));

        addItem(new ActionItem(4, lunar) {
            @Override
            public void onClick(InventoryClickEvent e) {
                VisualFeedback.setEnabled(!VisualFeedback.isEnabled());
                openAll();
            }
        });

        capability(10, "btn-10", Capability.COOLDOWN, "§8┃ §f§lCooldowns",
                "§7» Accès §bHost\n"
                        + "\n"
                        + " §f§l┃ §fAfficher le cooldown des pouvoirs avec l'overlay Lunar.\n"
                        + "\n"
                        + " §f§l☰ §fStatus : %actif%\n"
                        + " \n"
                        + "§8» §fClique pour §cdésactiver§f");
        capability(11, "btn-11", Capability.FIRE, "§8┃ §f§lFeu coloré",
                "§7» Accès §bHost\n"
                        + "\n"
                        + " §f§l┃ §fChange la couleur du feu lorsqu'il provient de pouvoirs.\n"
                        + "\n"
                        + " §f§l☰ §fStatus : %actif%\n"
                        + "\n"
                        + "§8» §fClique pour §cdésactiver§f");
        capability(12, "btn-12", Capability.GLOW, "§8┃ §f§lSurbrillance",
                "§7» Accès §bHost\n"
                        + "\n"
                        + " §f§l┃ §fSurbrillance des joueurs ciblés avec des pouvoirs.\n"
                        + "\n"
                        + " §f§l☰ §fStatus : %actif%\n"
                        + "\n"
                        + "§8» §fClique pour §cdésactiver§f");
        capability(13, "btn-13", Capability.TEAM_MARKERS, "§8┃ §f§lMarkers d'équipe",
                "§7» Accès §bHost\n"
                        + "\n"
                        + " §f§l┃ §fIndique la position des alliés (TeamView Lunar).\n"
                        + "\n"
                        + " §f§l☰ §fStatus : %actif%\n"
                        + "\n"
                        + "§8» §fClique pour §cdésactiver§f");
        capability(14, "btn-14", Capability.NOTIFICATION, "§8┃ §f§lNotifications",
                "§7» Accès §bHost\n"
                        + "\n"
                        + " §f§l┃ §fNotifications Lunar (détections, modération).\n"
                        + "\n"
                        + " §f§l☰ §fStatus : %actif%\n"
                        + "\n"
                        + "§8» §fClique pour §cdésactiver§f");
        capability(15, "btn-15", Capability.WAYPOINT, "§8┃ §f§lWaypoints",
                "§7» Accès §bHost\n"
                        + "\n"
                        + " §f§l┃ §fWaypoints Lunar (cibles marquées).\n"
                        + "\n"
                        + " §f§l☰ §fStatus : %actif%\n"
                        + "\n"
                        + "§8» §fClique pour §cdésactiver§f");
        capability(16, "btn-16", Capability.VIGNETTE, "§8┃ §f§lVignette",
                "§7» Accès §bHost\n"
                        + "\n"
                        + " §f§l┃ §fFiltre d'écran (gel modération, debuffs).\n"
                        + "\n"
                        + " §f§l☰ §fStatus : %actif%\n"
                        + "\n"
                        + "§8» §fClique pour §aactivez§f");
        capability(17, "btn-17", Capability.STAFF_MOD, "§8┃ §f§lMods Staff",
                "§7» Accès §bHost\n"
                        + "\n"
                        + " §f§l┃ §fMods staff Lunar pour les modérateurs mis en §f/h spec§f.\n"
                        + "\n"
                        + " §f§l☰ §fStatus : %actif%\n"
                        + "\n"
                        + "§8» §fClique pour §cdésactiver§f");

        ItemCreator legend = new ItemCreator(Material.REDSTONE_COMPARATOR)
                .setName(t(DynamicLang.of("menu.modules-lunar.btn-22.name", "§8┃ §5Mods Lunar (Global)")));
        legend.setLores(Arrays.asList(t(DynamicLang.of("menu.modules-lunar.btn-22.lore",
                "§8┃ §f§a§lVert§f = Autorisé\n"
                        + " §8┃ §f§c§lRouge§f = Désactivé")).split("\n", -1)));
        addItem(new StaticItem(22, legend));

        mod(27, "btn-27", "minimap", "§8┃ §f§lMinimap", MOD_BLOCKED_LORE);
        mod(28, "btn-28", "waypoints", "§8┃ §f§lWaypoints", MOD_ALLOWED_LORE);
        mod(29, "btn-29", "coordinates", "§8┃ §f§lCoordonnées", MOD_ALLOWED_LORE);
        mod(30, "btn-30", "directionhud", "§8┃ §f§lDirection HUD", MOD_ALLOWED_LORE);
        mod(31, "btn-31", "freelook", "§8┃ §f§lFreelook", MOD_ALLOWED_LORE);
        mod(32, "btn-32", "snaplook", "§8┃ §f§lSnaplook", MOD_ALLOWED_LORE);
        mod(33, "btn-33", "zoom", "§8┃ §f§lZoom", MOD_ALLOWED_LORE);
        mod(34, "btn-34", "chunkborders", "§8┃ §f§lChunk Borders", MOD_BLOCKED_LORE);
        mod(35, "btn-35", "f3display", "§8┃ §f§lF3 Display", MOD_ALLOWED_LORE);
        mod(37, "btn-37", "teamview", "§8┃ §f§lTeamView", MOD_ALLOWED_LORE);
        mod(38, "btn-38", "replaymod", "§8┃ §f§lReplayMod", MOD_ALLOWED_LORE);
        mod(39, "btn-39", "rewind", "§8┃ §f§lRewind", MOD_ALLOWED_LORE);
        mod(40, "btn-40", "hitbox", "§8┃ §f§lHitbox", MOD_ALLOWED_LORE);
        mod(41, "btn-41", "reachdisplay", "§8┃ §f§lReach Display", MOD_ALLOWED_LORE);
        mod(42, "btn-42", "pvpinfo", "§8┃ §f§lPvP Info", MOD_ALLOWED_LORE);
        mod(43, "btn-43", "uhcoverlay", "§8┃ §f§lUHC Overlay", MOD_ALLOWED_LORE);

        addItem(new ActionItem(45, UiItems.createCustomButon(BACK_TEXTURE,
                t(DynamicLang.of("menu.modules-lunar.btn-45.name", "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new DefaultUi(getPlayer()).open();
            }
        });
    }

    private void capability(int slot, String key, Capability cap, String name, String lore) {
        boolean enabled = ApolloConfig.isCapabilityEnabled(cap);

        ItemCreator icon = new ItemCreator(Material.INK_SACK)
                .setDurability((short) (enabled ? DyeColor.LIME.getDyeData() : DyeColor.RED.getDyeData()))
                .setName(t(DynamicLang.of("menu.modules-lunar." + key + ".name", name)));
        icon.setLores(Arrays.asList(t(DynamicLang.of("menu.modules-lunar." + key + ".lore", lore),
                Map.of("%actif%", UiItems.onOff(getPlayer(), enabled))).split("\n", -1)));

        addItem(new ActionItem(slot, icon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                ApolloConfig.setCapabilityEnabled(cap, !ApolloConfig.isCapabilityEnabled(cap));
                openAll();
            }
        });
    }

    private void mod(int slot, String key, String modId, String name, String lore) {
        boolean allowed = ApolloConfig.isModAllowed(modId);

        ItemCreator icon = UiItems.createCustomButon(allowed ? MOD_ALLOWED_TEXTURE : MOD_BLOCKED_TEXTURE,
                t(DynamicLang.of("menu.modules-lunar." + key + ".name", name)),
                Arrays.asList(t(DynamicLang.of("menu.modules-lunar." + key + ".lore", lore)).split("\n", -1)));

        addItem(new ActionItem(slot, icon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                ApolloConfig.setModAllowed(modId, !ApolloConfig.isModAllowed(modId));
                openAll();
            }
        });
    }
}
