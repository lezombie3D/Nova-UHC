package net.novaproject.novauhc.ui.world;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.task.LoadingChunkTask;
import net.novaproject.novauhc.ui.DefaultUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import net.novaproject.novauhc.world.generation.WorldGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class WorldUi extends CustomInventory {


    private final World arenaWorld = Common.get().getArena();
    private final World spawn = Common.get().getLobby();
    public WorldUi(Player player) {
        super(player);
    }

    @Override
    public void setup() {
        fillCorner(getConfig().getInt("menu.world.color"));

        ItemCreator orepop = new ItemCreator(Material.DIAMOND_PICKAXE)
                .setName("§8┃ §fBoost des Minerais")
                .addLore("")
                .addLore(" §8» §fAccès §f: §6§lHost")
                .addLore("")
                .addLore("  §8┃ §fPermet de configurer les")
                .addLore("  §8┃ §fparamètres des boosts des §2minerais§r.")
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_ACCESS.getMessage())
                .addLore("");
        ItemCreator world = new ItemCreator(Material.ENDER_PEARL)
                .setName("§8┃ §fRecréer l'Arena")
                .addLore("")
                .addLore(" §8» §fAccès §f: §6§lHost")
                .addLore("")
                .addLore("  §8┃ §fPermet de recréer l'arène.")
                .addLore("  §8┃ §c§lAttention, cela effacera l'arène existante.")
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_ACCESS.getMessage())
                .addLore("");

        ItemCreator pregen = new ItemCreator(Material.GRASS)
                .setName("§8┃ §fRedémarrer la Prégénération")
                .addLore("")
                .addLore(" §8» §fAccès §f: §6§lHost")
                .addLore("")
                .addLore("  §8┃ §fPermet de redémarrer la prégénération.")
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_ACCESS.getMessage())
                .addLore("");

        String name = getPlayer().getWorld().getName().equals(Common.get().getArenaName())
                ? "§8┃ §fTéléportation au §a§llobby"
                : "§8┃ §fTéléportation au monde " + Common.get().getMainColor() + "§lArena";

        String destination = getPlayer().getWorld().getName().equals(Common.get().getArenaName())
                ? "au §a§llobby"
                : "au monde " + Common.get().getMainColor() + "§lArena";

        ItemCreator prev = new ItemCreator(Material.WOOD_DOOR)
                .setName(name)
                .addLore("")
                .addLore(" §8» §fAccès §f: §6§lHost")
                .addLore("")
                .addLore("  §8┃ §fPermet de " + destination + ".")
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_ACCESS.getMessage())
                .addLore("");

        addItem(new ActionItem(22, prev) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (getPlayer().getWorld().equals(Common.get().getArena())) {
                    getPlayer().teleport(Common.get().getLobbySpawn());
                    getPlayer().getInventory().clear();
                    UHCUtils.giveLobbyItems(getPlayer());
                } else {
                    getPlayer().teleport(new Location(Common.get().getArena(), 0, 100, 0));
                    getPlayer().setFlying(true);
                    getPlayer().getInventory().clear();
                    getPlayer().getInventory().setItem(0,new ItemCreator(Material.GRASS).setName("§8┃ §fRecrée l' §a§lArena").getItemstack());
                    getPlayer().getInventory().setItem(8,new ItemCreator(Material.WOOD_DOOR).setName(name).getItemstack());
                }
                openAll();
            }
        });
        addItem(new ActionItem(12, pregen) {
            @Override
            public void onClick(InventoryClickEvent e) {
                openAll();
                LoadingChunkTask.create(Common.get().getArena(), Bukkit.getWorld(Common.get().getArenaName() + "_nether"), (int) Common.get().getArena().getWorldBorder().getSize() / 2);
            }
        });
        addItem(new ActionItem(13, world) {
            @Override
            public void onClick(InventoryClickEvent e) {
                openAll();
                new WorldGenerator(Main.get(), Common.get().getArenaName());
            }
        });

        addReturn(18, new DefaultUi(getPlayer()));
        addMenu(14, orepop, new OrePopulatorUi(getPlayer()));

    }


    @Override
    public String getTitle() {
        return getConfig().getString("menu.world.title");
    }

    @Override
    public int getLines() {
        return 3;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }

}
