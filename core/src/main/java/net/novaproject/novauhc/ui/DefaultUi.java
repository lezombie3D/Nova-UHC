package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.lang.CommonLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.ui.DefaultUiLang;
import net.novaproject.novauhc.lang.ui.LunarApolloLang;
import net.novaproject.novauhc.lang.ui.UiTitleLang;
import net.novaproject.novauhc.lunar.LunarApolloManager;
import net.novaproject.novauhc.ui.config.ScenariosUi;
import net.novaproject.novauhc.ui.config.TeamConfigUi;
import net.novaproject.novauhc.ui.world.BorderConfig;
import net.novaproject.novauhc.ui.world.WorldUi;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.Titles;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.Map;

public class DefaultUi extends CustomInventory {

    public DefaultUi(Player player) {
        super(player);
    }

    private String t(DefaultUiLang key) {
        return LangManager.get().get(key, getPlayer());
    }

    private String t(DefaultUiLang key, Map<String, Object> params) {
        return LangManager.get().get(key, getPlayer(), params);
    }

    @Override
    public void setup() {
        fillCorner(getConfig().getInt("menu.main.color"));

        String accessHost  = LangManager.get().get(CommonLang.ACCESS_HOST,          getPlayer());
        String clickAccess = LangManager.get().get(CommonLang.CLICK_HERE_TO_ACCESS,  getPlayer());
        String clickModify = LangManager.get().get(CommonLang.CLICK_HERE_TO_MODIFY,  getPlayer());
        String clickToggle = LangManager.get().get(CommonLang.CLICK_HERE_TO_ACTIVATE, getPlayer());

        ItemCreator border = new ItemCreator(Material.STAINED_GLASS).setDurability((short) 9)
                .setName(t(DefaultUiLang.BORDER_ITEM_NAME))
                .addLore("").addLore(accessHost).addLore("")
                .addLore(t(DefaultUiLang.BORDER_ITEM_DESC))
                .addLore("").addLore(clickAccess).addLore("");

        ItemCreator scenarui = new ItemCreator(Material.BOOK)
                .setName(t(DefaultUiLang.SCENARIOS_ITEM_NAME))
                .addLore("").addLore(accessHost).addLore("")
                .addLore(t(DefaultUiLang.SCENARIOS_ITEM_DESC))
                .addLore("").addLore(clickAccess).addLore("");

        ItemCreator special = new ItemCreator(Material.PRISMARINE_SHARD)
                .setName(t(DefaultUiLang.SPECIAL_ITEM_NAME))
                .addLore("").addLore(accessHost).addLore("")
                .addLore(t(DefaultUiLang.SPECIAL_ITEM_DESC))
                .addLore("").addLore(clickAccess).addLore("");

        ItemCreator team = new ItemCreator(Material.BANNER).setDurability((short) 15)
                .setName(t(DefaultUiLang.TEAM_ITEM_NAME))
                .addLore("").addLore(accessHost).addLore("")
                .addLore(t(DefaultUiLang.TEAM_ITEM_DESC))
                .addLore("").addLore(clickAccess).addLore("");

        ItemCreator stop = new ItemCreator(Material.BARRIER)
                .setName(t(DefaultUiLang.STOP_ITEM_NAME))
                .addLore("").addLore(accessHost).addLore("")
                .addLore(t(DefaultUiLang.STOP_ITEM_DESC))
                .addLore("").addLore(clickToggle).addLore("");

        String specStatus = UHCManager.get().isSpectator()
                ? t(DefaultUiLang.SPEC_ENABLED)
                : t(DefaultUiLang.SPEC_DISABLED);
        ItemCreator spec = new ItemCreator(Material.EYE_OF_ENDER)
                .setName(t(DefaultUiLang.SPEC_ITEM_NAME))
                .addLore("").addLore(accessHost)
                .addLore(t(DefaultUiLang.SPEC_ITEM_STATUS) + specStatus)
                .addLore("")
                .addLore(t(DefaultUiLang.SPEC_ITEM_DESC))
                .addLore("").addLore(clickModify).addLore("");

        ItemCreator option = new ItemCreator(Material.ITEM_FRAME)
                .setName(t(DefaultUiLang.OPTION_ITEM_NAME))
                .addLore("").addLore(accessHost).addLore("")
                .addLore(t(DefaultUiLang.OPTION_ITEM_DESC))
                .addLore("").addLore(clickModify).addLore("");

        ItemCreator preconf = new ItemCreator(Material.PAPER)
                .setName(t(DefaultUiLang.PRECONF_ITEM_NAME))
                .addLore("").addLore(accessHost).addLore("")
                .addLore(t(DefaultUiLang.PRECONF_ITEM_DESC))
                .addLore("").addLore(clickAccess).addLore("");

        String whiteStatus = Bukkit.hasWhitelist()
                ? t(DefaultUiLang.SPEC_ENABLED)
                : t(DefaultUiLang.SPEC_DISABLED);
        ItemCreator white = new ItemCreator(Material.WATCH)
                .setName(t(DefaultUiLang.WHITE_ITEM_NAME))
                .addLore("").addLore(accessHost)
                .addLore(t(DefaultUiLang.SPEC_ITEM_STATUS) + whiteStatus)
                .addLore("")
                .addLore(t(DefaultUiLang.WHITE_ITEM_DESC))
                .addLore("").addLore(clickModify).addLore("");

        ItemCreator world = UHCUtils.createCustomButon(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmM2MjExMGQ4MTg4NDQxZDIxNzk0NDM0ZjY3ZDEyYTAyMWI3NDAyYzhkYWE0MmQ0ZmVhMzIzZTdlMTllMGJiNyJ9fX0=",
                t(DefaultUiLang.WORLD_ITEM_NAME), null)
                .addLore("").addLore(accessHost).addLore("")
                .addLore(t(DefaultUiLang.WORLD_ITEM_DESC))
                .addLore("").addLore(clickAccess).addLore("");

        boolean isWait = UHCManager.get().getWaitState().equals(UHCManager.WaitState.WAIT_STATE);
        Material reglesMat = isWait ? Material.EYE_OF_ENDER : Material.ENDER_PEARL;
        String reglesName  = isWait ? t(DefaultUiLang.RULES_LOBBY_NAME) : t(DefaultUiLang.RULES_RULES_NAME);
        String reglesDesc  = isWait ? t(DefaultUiLang.RULES_LOBBY_DESC) : t(DefaultUiLang.RULES_RULES_DESC);

        ItemCreator regles = new ItemCreator(reglesMat)
                .setName(reglesName)
                .addLore("").addLore(accessHost).addLore("")
                .addLore(reglesDesc)
                .addLore("").addLore(clickAccess).addLore("");

        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta  = (SkullMeta) skull.getItemMeta();
        meta.setOwner(getPlayer().getName());
        skull.setItemMeta(meta);

        ItemCreator slot = new ItemCreator(skull)
                .setName(t(DefaultUiLang.SLOT_ITEM_NAME))
                .setLores(Arrays.asList(
                        t(DefaultUiLang.SLOT_ITEM_DESC),
                        t(DefaultUiLang.SLOT_ITEM_DESC2),
                        ""
                ));

        boolean lunarAvailable = LunarApolloManager.get().isAvailable();
        boolean lunarEnabled = UHCManager.get().isLunarApollo();
        String lunarStatus = lunarEnabled
                ? LangManager.get().get(LunarApolloLang.ITEM_ENABLED, getPlayer())
                : LangManager.get().get(LunarApolloLang.ITEM_DISABLED, getPlayer());

        ItemCreator lunar = new ItemCreator(Material.EMERALD)
                .setName(LangManager.get().get(LunarApolloLang.ITEM_NAME, getPlayer()))
                .addLore("").addLore(accessHost)
                .addLore(LangManager.get().get(LunarApolloLang.ITEM_STATUS, getPlayer()) + lunarStatus)
                .addLore("")
                .addLore(LangManager.get().get(LunarApolloLang.ITEM_DESC, getPlayer()));

        if (!lunarAvailable) {
            lunar.addLore("").addLore(LangManager.get().get(LunarApolloLang.ITEM_NOT_AVAILABLE, getPlayer()));
        } else {
            lunar.addLore("").addLore(clickToggle);
        }
        lunar.addLore("");

        addItem(new ActionItem(4, lunar) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (!LunarApolloManager.get().isAvailable()) return;
                UHCManager.get().setLunarApollo(!UHCManager.get().isLunarApollo());
                openAll();
            }
        });

        addMenu(16, world, new WorldUi(getPlayer()));
        addItem(new ActionItem(6, regles) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (UHCManager.get().getWaitState().equals(UHCManager.WaitState.WAIT_STATE)) {
                    UHCManager.get().setWaitState(UHCManager.WaitState.LOBBY_STATE);
                    Bukkit.getOnlinePlayers().forEach(p -> p.teleport(Common.get().getLobbySpawn()));
                } else {
                    UHCManager.get().setWaitState(UHCManager.WaitState.WAIT_STATE);
                    Bukkit.getOnlinePlayers().forEach(p -> p.teleport(Common.get().getRulesSpawn()));
                }
            }
        });
        addMenu(2, team, new TeamConfigUi(getPlayer()));
        addMenu(10, slot, new ConfigVarUi(getPlayer(), 10, 5, 1, 10, 5, 1, UHCManager.get().getSlot(), 1, 100, this) {
            @Override public void onChange(Number newValue) { UHCManager.get().setSlot((int) newValue); }
        });
        addMenu(11, stop, new ConfirmMenu(getPlayer(),
                t(DefaultUiLang.STOP_CONFIRM),
                () -> Bukkit.shutdown(),
                () -> {},
                this));
        addItem(new ActionItem(15, spec) {
            @Override
            public void onClick(InventoryClickEvent e) {
                UHCManager.get().setSpectator(!UHCManager.get().isSpectator());
                openAll();
            }
        });
        addMenu(22, option,   new GameUi(getPlayer()));
        addMenu(31, special,  new ScenariosUi(getPlayer(), true));
        addMenu(37, border,   new BorderConfig(getPlayer()));
        addMenu(43, scenarui, new ScenariosUi(getPlayer()));
        addMenu(47, white,    new WhiteListUi(getPlayer()));
        addMenu(51, preconf,  new PreconfigUi(getPlayer(), this));

        addItem(new ActionItem(49, getWool(UHCManager.get().isStarted())) {
            @Override
            public void onClick(InventoryClickEvent e) {
                boolean started = UHCManager.get().isStarted();
                if (!started) {
                    UHCManager.get().setCanceled(false);
                    UHCManager.get().onStart();
                    getPlayer().closeInventory();
                    UHCManager.get().setStarted(true);
                } else {
                    UHCManager.get().setCanceled(true);
                    getPlayer().closeInventory();
                    UHCManager.get().setStarted(false);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        new Titles().sendTitle(p,
                                ConfigUtils.getLangConfig().getString("start_canceled.title"),
                                ConfigUtils.getLangConfig().getString("start_canceled.subtitle"),
                                ConfigUtils.getLangConfig().getInt("start_canceled.duration"));
                        p.setLevel(0);
                        p.setExp(0f);
                    }
                }
            }
        });
    }

    private ItemCreator getWool(boolean started) {
        ItemCreator dyec = new ItemCreator(Material.INK_SACK).setDurability((short) (started ? 8 : 10));
        if (started) {
            dyec.setName(t(DefaultUiLang.START_CANCEL_NAME));
            dyec.setLores(Arrays.asList(
                    t(DefaultUiLang.START_CANCEL_LORE1),
                    t(DefaultUiLang.START_ACCESS),
                    "",
                    t(DefaultUiLang.START_CANCEL_LORE2),
                    "",
                    t(DefaultUiLang.START_CLICK),
                    ""
            ));
        } else {
            dyec.setName(t(DefaultUiLang.START_READY_NAME));
            dyec.setLores(Arrays.asList(
                    t(DefaultUiLang.START_READY_LORE1),
                    t(DefaultUiLang.START_ACCESS),
                    "",
                    t(DefaultUiLang.START_READY_LORE2),
                    "",
                    t(DefaultUiLang.START_CLICK),
                    ""
            ));
        }
        return dyec;
    }

    @Override
    public String getTitle() {
        return LangManager.get().get(UiTitleLang.MAIN_TITLE, getPlayer());
    }

    @Override
    public int getLines() { return 6; }

    @Override
    public boolean isRefreshAuto() { return false; }
}
