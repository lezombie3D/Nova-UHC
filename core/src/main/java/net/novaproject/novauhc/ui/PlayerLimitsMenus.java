package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.Map;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

public final class PlayerLimitsMenus {

    private PlayerLimitsMenus() {
    }

    private abstract static class Targeted extends CustomInventory {

        protected final Player target;

        protected Targeted(Player viewer, Player target) {
            super(viewer);
            this.target = target;
        }

        protected UHCPlayer uhcTarget() {
            return UHCPlayerManager.get().getPlayer(target);
        }

        protected ItemCreator targetHead() {
            return new ItemCreator(Material.SKULL_ITEM)
                    .setDurability((short) 3)
                    .setOwner(target.getName());
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
        public String broadcastKey() {
            return getClass().getName() + ":" + target.getUniqueId();
        }
    }

    public static class Hub extends Targeted {

        public Hub(Player viewer, Player target) {
            super(viewer, target);
        }

        @Override
        public String getTitle() {
            return t(DynamicLang.of("menu.limites-du-joueur.title", "» Limites de : %player%"),
                    Map.of("%player%", target.getName()));
        }

        @Override
        public void setup() {
            UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17);

            addItem(new StaticItem(4, targetHead().setName("§8┃ §a" + target.getName())));

            UiItems.panes(this, DyeColor.GREEN, 0, 8, 18, 26);

            ItemCreator enchants = new ItemCreator(Material.ENCHANTED_BOOK)
                    .setName(t(DynamicLang.of("menu.limites-du-joueur.btn-11.name",
                            "§8┃ §5Limite d'enchantements")));
            enchants.setLores(Arrays.asList(t(DynamicLang.of("menu.limites-du-joueur.btn-11.lore",
                    "§7» Accès §5Host\n"
                            + "\n"
                            + " §5┃ §fDéfinir la limite de §5tous §fles §5enchantements\n"
                            + " §5┃ §fdu §5Joueur §fselectionner.\n"
                            + "\n"
                            + "§8» §fCliquez pour §5modifier§f.")).split("\n", -1)));

            addItem(new ActionItem(11, enchants) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new LimiteStuffbyPlayerUi(getPlayer(), target).open();
                }
            });

            UHCPlayer stats = uhcTarget();
            Map<String, Object> multipliers = Map.of(
                    "%player_force%", percent(stats.getForcePercent()),
                    "%player_resistance%", percent(stats.getResistancePercent()),
                    "%player_speed%", percent(stats.getSpeedPercent()),
                    "%player_critic%", percent(stats.getForceCriticPercent()));

            ItemCreator effects = new ItemCreator(Material.GLASS_BOTTLE)
                    .setName(t(DynamicLang.of("menu.limites-du-joueur.btn-13.name", "§8┃ §dEffets"), multipliers));
            effects.setLores(Arrays.asList(t(DynamicLang.of("menu.limites-du-joueur.btn-13.lore",
                    "§7» Accès §dHost\n"
                            + "\n"
                            + " §d┃ §fConfigurer les multiplicateurs du §dJoueur §fde§f: \n"
                            + "\n"
                            + " §d☰ §cForce§f: %player_force%§f%\n"
                            + " §d☰ §bRésistance§f: %player_resistance%§f%\n"
                            + " §d☰ §eSpeed§f: %player_speed%§f%\n"
                            + " §d☰ §6Critiques§f: %player_critic%§f%\n"
                            + "\n"
                            + "§8» §fCliquez pour y §daccéder§f."), multipliers).split("\n", -1)));

            addItem(new ActionItem(13, effects) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new Effects(getPlayer(), target).open();
                }
            });

            ItemCreator limits = new ItemCreator(Material.DIAMOND_CHESTPLATE)
                    .setName(t(DynamicLang.of("menu.limites-du-joueur.btn-15.name", "§8┃ §bLimite")));
            limits.setLores(Arrays.asList(t(DynamicLang.of("menu.limites-du-joueur.btn-15.lore",
                    "§7» Accès §bHost\n"
                            + "\n"
                            + " §b┃ §fVous permet de §blimiter \n"
                            + " §b┃ §fcertain §bitem §fou §bobtention §fd'§bitem§f.\n"
                            + "\n"
                            + "§8» §fCliquez pour y §baccéder§f.")).split("\n", -1)));

            addItem(new ActionItem(15, limits) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new Technical(getPlayer(), target).open();
                }
            });

            UiItems.panes(this, DyeColor.WHITE, 19, 25);
        }
    }

    public static class Effects extends Targeted {

        public Effects(Player viewer, Player target) {
            super(viewer, target);
        }

        @Override
        public String getTitle() {
            return t(DynamicLang.of("menu.effet-du-joueur.title", "» Effet : %player%"),
                    Map.of("%player%", target.getName()));
        }

        @Override
        public void setup() {
            UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 19, 25);

            addItem(new ActionItem(18, UiItems.createCustomButon(
                    MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.MAGENTA),
                    t(DynamicLang.of("menu.effet-du-joueur.btn-18.name", "§8┃ §7§lRetour")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new Hub(getPlayer(), target).open();
                }
            });

            UiItems.panes(this, DyeColor.MAGENTA, 0, 8, 26);

            UHCPlayer uhc = uhcTarget();

            stat(12, "btn-12", "§8┃ §3Resistance : %res_player_percent%", "%res_player_percent%",
                    DyeColor.CYAN, uhc.getResistancePercent(), uhc::setResistancePercent);
            stat(11, "btn-11", "§8┃ §cForce : %player_force_percent%", "%player_force_percent%",
                    DyeColor.RED, uhc.getForcePercent(), uhc::setForcePercent);
            stat(15, "btn-15", "§8┃ §6Critique : %player_crit_percent%", "%player_crit_percent%",
                    DyeColor.ORANGE, uhc.getForceCriticPercent(), uhc::setForceCriticPercent);
            stat(14, "btn-14", "§8┃ §eSpeed : %player_speed_percent%", "%player_speed_percent%",
                    DyeColor.YELLOW, uhc.getSpeedPercent(), uhc::setSpeedPercent);
        }

        private void stat(int slot, String key, String defaultName, String placeholder,
                          DyeColor color, double current, DoubleConsumer setter) {
            int shown = (int) Math.round(current * 100);

            ItemCreator icon = new ItemCreator(Material.INK_SACK)
                    .setDurability((short) color.getDyeData())
                    .setName(t(DynamicLang.of("menu.effet-du-joueur." + key + ".name", defaultName),
                            Map.of(placeholder, String.valueOf(shown))));

            addItem(new ActionItem(slot, icon) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new ConfigVarUi(getPlayer(), 25, 10, 5, 25, 10, 5, shown, 0, 500,
                            Effects.this, VariableType.INTEGER) {
                        @Override
                        public void onChange(Number newValue) {
                            setter.accept(newValue.intValue() / 100.0);
                        }
                    }.open();
                }
            });
        }
    }

    public static class Technical extends Targeted {

        public Technical(Player viewer, Player target) {
            super(viewer, target);
        }

        @Override
        public String getTitle() {
            return t(DynamicLang.of("menu.limites-techniques-joueur.title", "» Limites de : %player%"),
                    Map.of("%player%", target.getName()));
        }

        @Override
        public void setup() {
            UiItems.panes(this, DyeColor.LIGHT_BLUE, 0, 8, 26);
            UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 19, 25);

            Map<String, Object> name = Map.of("%player%", target.getName());
            ItemCreator head = targetHead()
                    .setName(t(DynamicLang.of("menu.limites-techniques-joueur.player.name",
                            "§8┃ §a%player%"), name));
            head.setLores(Arrays.asList(t(DynamicLang.of("menu.limites-techniques-joueur.player.lore",
                    "§7» Élément §adynamique\n"
                            + "\n"
                            + " §a┃ §fLimites techniques appliquées à ce joueur.\n"), name).split("\n", -1)));
            addItem(new StaticItem(4, head));

            UHCPlayer uhc = uhcTarget();

            number(12, "diamond-armor", new ItemCreator(Material.DIAMOND_CHESTPLATE),
                    "§8┃ §cArmure en diamant",
                    "§7» Accès §cHost\n"
                            + "\n"
                            + " §c┃ §fDéfinissez le nombre de pièces en diamant\n"
                            + " §c┃ §fautorisées pour cette cible.\n"
                            + "\n"
                            + " §c☰ §fPièces : %player_diamond_armor%\n"
                            + "\n"
                            + "§8» §fCliquez pour §cmodifier§f.",
                    "%player_diamond_armor%", uhc.getDiamondArmor(), 4, 2, 1, 0, 4,
                    uhc::setDiamondArmor);

            number(13, "protection", new ItemCreator(Material.ENCHANTED_BOOK),
                    "§8┃ §eProtection maximale",
                    "§7» Accès §eHost\n"
                            + "\n"
                            + " §e┃ §fDéfinissez sa limite globale de Protection,\n"
                            + " §e┃ §fdistincte de l’enchantement du même nom.\n"
                            + "\n"
                            + " §e☰ §fNiveau : %player_protection_max%\n"
                            + "\n"
                            + "§8» §fCliquez pour §emodifier§f.",
                    "%player_protection_max%", uhc.getProtectionMax(), 4, 2, 1, 0, 4,
                    uhc::setProtectionMax);

            number(14, "diamond-limit", new ItemCreator(Material.DIAMOND),
                    "§8┃ §bLimite de diamants",
                    "§7» Accès §bHost\n"
                            + "\n"
                            + " §b┃ §fDéfinissez sa limite personnelle\n"
                            + " §b┃ §fde diamants minables.\n"
                            + "\n"
                            + " §b☰ §fLimite : %player_diamond_limit%\n"
                            + "\n"
                            + "§8» §fCliquez pour §bmodifier§f.",
                    "%player_diamond_limit%", uhc.getDiamondLimit(), 10, 5, 1, 0, Double.NaN,
                    uhc::setDiamondLimit);

            addItem(new ActionItem(18, UiItems.createCustomButon(
                    MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.LIGHT_BLUE),
                    t(DynamicLang.of("menu.limites-techniques-joueur.back.name", "§8┃ §7§lRetour")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new Hub(getPlayer(), target).open();
                }
            });
        }

        private void number(int slot, String key, ItemCreator base, String defaultName, String defaultLore,
                            String placeholder, int current, Number big, Number mid, Number small,
                            double min, double max, IntConsumer apply) {
            Map<String, Object> value = Map.of(placeholder, String.valueOf(current));

            ItemCreator icon = base
                    .setName(t(DynamicLang.of("menu.limites-techniques-joueur." + key + ".name",
                            defaultName), value));
            icon.setLores(Arrays.asList(t(DynamicLang.of("menu.limites-techniques-joueur." + key + ".lore",
                    defaultLore), value).split("\n", -1)));

            addItem(new ActionItem(slot, icon) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new ConfigVarUi(getPlayer(), big, mid, small, big, mid, small, current, min, max,
                            Technical.this, VariableType.INTEGER) {
                        @Override
                        public void onChange(Number newValue) {
                            apply.accept(newValue.intValue());
                        }
                    }.open();
                }
            });
        }
    }

    private static String percent(double value) {
        return String.valueOf((int) Math.round(value * 100));
    }
}
