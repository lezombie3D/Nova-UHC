package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.task.LoadingChunkTask;
import net.novaproject.novauhc.ui.config.ScenariosUi;
import net.novaproject.novauhc.ui.config.TeamConfigUi;
import net.novaproject.novauhc.ui.world.BorderConfig;
import net.novaproject.novauhc.ui.world.WorldUi;
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

import java.util.ArrayList;
import java.util.List;

public class DefaultUi extends CustomInventory {


    public DefaultUi(Player player) {
        super(player);
    }


    @Override
    public void setup() {


        fillCorner(getConfig().getInt("menu.main.color"));
        ItemCreator border = (new ItemCreator(Material.STAINED_GLASS))
                .setDurability((short) 9)
                .setName(CommonString.MENU_BORDER_TITLE.getMessage())
                .addLore("")
                .addLore(CommonString.MENU_BORDER_ACCESS.getMessage())
                .addLore("")
                .addLore(CommonString.MENU_BORDER_DESCRIPTION_1.getMessage())
                .addLore(CommonString.MENU_BORDER_DESCRIPTION_2.getMessage())
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_ACCESS.getMessage())
                .addLore("");

        ItemCreator scenarui = (new ItemCreator(Material.BOOK))
                .setName(CommonString.MENU_SCENARIOS_TITLE.getMessage())
                .addLore("")
                .addLore(CommonString.MENU_SCENARIOS_ACCESS.getMessage())
                .addLore("")
                .addLore(CommonString.MENU_SCENARIOS_DESCRIPTION_1.getMessage())
                .addLore(CommonString.MENU_SCENARIOS_DESCRIPTION_2.getMessage())
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_ACCESS.getMessage())
                .addLore("");

        ItemCreator special = (new ItemCreator(Material.PRISMARINE_SHARD))
                .setName(CommonString.MENU_GAMEMODE_TITLE.getMessage())
                .addLore("")
                .addLore(CommonString.MENU_GAMEMODE_ACCESS.getMessage())
                .addLore("")
                .addLore(CommonString.MENU_GAMEMODE_DESCRIPTION_1.getMessage())
                .addLore(CommonString.MENU_GAMEMODE_DESCRIPTION_2.getMessage())
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_ACCESS.getMessage())
                .addLore("");

        ItemCreator team = new ItemCreator(Material.BANNER)
                .setName(CommonString.MENU_TEAMS_TITLE.getMessage())
                .setDurability((short) 15)
                .addLore("")
                .addLore(CommonString.MENU_TEAMS_ACCESS.getMessage())
                .addLore("")
                .addLore(CommonString.MENU_TEAMS_DESCRIPTION_1.getMessage())
                .addLore(CommonString.MENU_TEAMS_DESCRIPTION_2.getMessage())
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_ACCESS.getMessage())
                .addLore("");
        ItemCreator stop = new ItemCreator(Material.BARRIER)
                .setName(CommonString.MENU_STOP_TITLE.getMessage())
                .addLore("")
                .addLore(CommonString.MENU_STOP_ACCESS.getMessage())
                .addLore("")
                .addLore(CommonString.MENU_STOP_DESCRIPTION_1.getMessage())
                .addLore(CommonString.MENU_STOP_DESCRIPTION_2.getMessage())
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_ACTIVATE.getMessage())
                .addLore("");
        ItemCreator spec = new ItemCreator(Material.EYE_OF_ENDER)
                .setName("§8┃ §fSpectateurs")
                .addLore("")
                .addLore(" §8» §fAccès §f: §6§lHost")
                .addLore(" §8» §fStatut §f: " + (UHCManager.get().isSpectator() ? "§aActivé" : "§cDésactivé"))
                .addLore("")
                .addLore("  §8┃ §fPermet d'§aaccepter§f ou §cnon§f la présence")
                .addLore("  §8┃ §fdes spectateurs dans la §cpartie§f.")
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_MODIFY.getMessage())
                .addLore("");
        ItemCreator option = new ItemCreator(Material.ITEM_FRAME)
                .setName("§8┃ §fOptions de la " + Common.get().getMainColor() + "partie")
                .addLore("")
                .addLore(" §8» §fAccès §f: §6§lHost")
                .addLore("")
                .addLore("  §8┃ §fPermet d'accéder aux")
                .addLore("  §8┃ " + Common.get().getMainColor() + "options§f/" + Common.get().getMainColor() + "règles§f de la partie")
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_MODIFY.getMessage())
                .addLore("");
        ItemCreator preconf = (new ItemCreator(Material.PAPER))
                .setName("§8┃ §fPré-Config §f(" + Common.get().getMainColor() + "§lUHC§f)")
                .addLore("")
                .addLore(" §8» §fAccès §f: §6§lHost")
                .addLore("")
                .addLore("  §8┃ §fPermet d'accéder à")
                .addLore("  §8┃ §fvos " + Common.get().getMainColor() + "configurations§f.")
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_ACCESS.getMessage())
                .addLore("");


        ItemCreator white = (new ItemCreator(Material.WATCH))
                .setName("§8┃ §fAccessibilité de la " + Common.get().getMainColor() + "partie")
                .addLore("")
                .addLore(" §8» §fAccès §f: §6§lHost")
                .addLore(" §8» §fStatut §f: " + (Bukkit.hasWhitelist() ? "§aActivé" : "§cDésactivé"))
                .addLore("")
                .addLore("  §8┃ §fPermet de " + Common.get().getMainColor() + "modifier§f l'accessibilité")
                .addLore("  §8┃ §fà la partie pour les " + Common.get().getMainColor() + "joueurs§f.")
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_MODIFY.getMessage())
                .addLore("");

        ItemCreator world = UHCUtils.createCustomButon("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmM2MjExMGQ4MTg4NDQxZDIxNzk0NDM0ZjY3ZDEyYTAyMWI3NDAyYzhkYWE0MmQ0ZmVhMzIzZTdlMTllMGJiNyJ9fX0=", CommonString.MENU_WORLD_TITLE.getMessage(), null)
                .addLore("")
                .addLore(CommonString.MENU_WORLD_ACCESS.getMessage())
                .addLore("")
                .addLore(CommonString.MENU_WORLD_DESCRIPTION_1.getMessage())
                .addLore(CommonString.MENU_WORLD_DESCRIPTION_2.getMessage())
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_ACCESS.getMessage())
                .addLore("");
        Material material = UHCManager.get().getWaitState().equals(UHCManager.WaitState.WAIT_STATE)
                ? Material.EYE_OF_ENDER
                : Material.ENDER_PEARL;

        String name = UHCManager.get().getWaitState().equals(UHCManager.WaitState.WAIT_STATE)
                ? CommonString.MENU_TP_LOBBY_TITLE.getMessage()
                : CommonString.MENU_TP_RULES_TITLE.getMessage();

        String destination = UHCManager.get().getWaitState().equals(UHCManager.WaitState.WAIT_STATE)
                ? CommonString.MENU_TP_LOBBY_DESTINATION.getMessage()
                : CommonString.MENU_TP_RULES_DESTINATION.getMessage();

        String description2 = CommonString.MENU_TP_DESCRIPTION_2.getMessage().replace("%destination%", destination);

        ItemCreator regles = new ItemCreator(material)
                .setName(name)
                .addLore("")
                .addLore(" §8» §fAccès §f: §6§lHost")
                .addLore("")
                .addLore(CommonString.MENU_TP_DESCRIPTION_1.getMessage())
                .addLore(description2)
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_ACCESS.getMessage())
                .addLore("");
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(getPlayer().getName());
        skull.setItemMeta(meta);
        ItemCreator slot = new ItemCreator(skull).setName(CommonString.MENU_SLOTS_TITLE.getMessage())
                .addLore("")
                .addLore(CommonString.MENU_SLOTS_ACCESS.getMessage())
                .addLore("")
                .addLore(CommonString.MENU_SLOTS_DESCRIPTION_1.getMessage())
                .addLore(CommonString.MENU_SLOTS_DESCRIPTION_2.getMessage())
                .addLore("");

        addMenu(16, world, new WorldUi(getPlayer()));
        addItem(new ActionItem(6, regles) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (UHCManager.get().getWaitState().equals(UHCManager.WaitState.WAIT_STATE)) {
                    UHCManager.get().setWaitState(UHCManager.WaitState.LOBBY_STATE);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.teleport(Common.get().getLobbySpawn());
                    }
                } else {
                    UHCManager.get().setWaitState(UHCManager.WaitState.WAIT_STATE);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.teleport(Common.get().getRulesSpawn());
                    }
                }
            }
        });
        addMenu(2, team, new TeamConfigUi(getPlayer()));
        addMenu(10, slot, new ConfigVarUi(getPlayer(), 10, 5, 1, 10, 5, 1, UHCManager.get().getSlot(), 1, 100, this) {
            @Override
            public void onChange(int newValue) {
                UHCManager.get().setSlot(newValue);
            }
        });
        addMenu(11, stop, new ConfirmMenu(getPlayer(),
                CommonString.MENU_STOP_CONFIRM.getMessage(),
                () -> {
                    Bukkit.shutdown();
                },
                () -> {

                }, this));
        addItem(new ActionItem(15, spec) {
            @Override
            public void onClick(InventoryClickEvent e) {
                UHCManager.get().setSpectator(!UHCManager.get().isSpectator());
                openAll();
            }
        });
        addMenu(22, option, new GameUi(getPlayer()));
        addMenu(31, special, new ScenariosUi(getPlayer(), true));
        addMenu(37, border, new BorderConfig(getPlayer()));
        addMenu(43, scenarui, new ScenariosUi(getPlayer()));
        addMenu(47, white, new WhiteListUi(getPlayer()));
        addMenu(51, preconf, new PreconfigUi(getPlayer(), this));


        addItem(new ActionItem(49, getWool(UHCManager.get().isStarted())) {
            @Override
            public void onClick(InventoryClickEvent e) {

                if (!LoadingChunkTask.get().isFinished()) return;
                boolean started = UHCManager.get().isStarted();
                if (!started) {
                    UHCManager.get().setCanceled(false);
                    UHCManager.get().onStart();
                    getPlayer().closeInventory();
                    UHCManager.get().setStarted(true);
                    return;
                }
                if (started) {
                    UHCManager.get().setCanceled(true);
                    getPlayer().closeInventory();
                    UHCManager.get().setStarted(false);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        new Titles().sendTitle(player, getConfig().getString("start_canceled.title"), getConfig().getString("start_canceled.subtitle"), getConfig().getInt("start_canceled.duration"));
                        player.setLevel(0);
                        player.setExp(0f);
                    }
                }
            }
        });

    }

    private ItemCreator getWool(boolean started) {
        ItemCreator dyec = new ItemCreator(Material.INK_SACK).setDurability((short) (started ? 8 : 10));
        List<String> lore = new ArrayList<>();
        lore.add(CommonString.MENU_LAUNCH_READY_QUESTION.getMessage());
        lore.add(CommonString.MENU_LAUNCH_ACCESS.getMessage());
        lore.add("");
        lore.add(CommonString.MENU_LAUNCH_START_DESC_1.getMessage());
        lore.add(CommonString.MENU_LAUNCH_START_DESC_2.getMessage());
        lore.add("");
        lore.add(CommonString.MENU_LAUNCH_ACTION_START.getMessage());
        lore.add("");
        List<String> lore2 = new ArrayList<>();
        lore2.add(CommonString.MENU_LAUNCH_CANCEL_QUESTION.getMessage());
        lore2.add(CommonString.MENU_LAUNCH_ACCESS.getMessage());
        lore2.add("");
        lore2.add("§7Permet d’arrêter la " + Common.get().getMainColor() + "partie §7si");
        lore2.add(CommonString.MENU_LAUNCH_CANCEL_DESC_2.getMessage());
        lore2.add("");
        lore2.add(CommonString.MENU_LAUNCH_ACTION_START.getMessage());
        lore2.add("");
        dyec.setName(started ? CommonString.MENU_LAUNCH_TITLE_CANCEL.getMessage() : CommonString.MENU_LAUNCH_TITLE_START.getMessage());
        dyec.setLores(started ? lore2 : lore);
        return dyec;
    }


    @Override
    public String getTitle() {
        return getConfig().getString("menu.main.title");
    }

    @Override
    public int getLines() {
        return 6;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }

}