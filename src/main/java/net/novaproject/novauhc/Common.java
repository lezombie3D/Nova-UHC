package net.novaproject.novauhc;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.world.generation.WorldGenerator;
import net.novaproject.novauhc.world.utils.LobbyCreator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

@Getter
@Setter
public class Common {

    private String servertag;
    private String infoTag;
    private String serverIp;
    private String servername;
    private String helpop;

    private String mainColor;
    private String mbip;
    private String mbport;

    private String arenaName;
    private String lobbyName;

    private ItemCreator configItem;
    private ItemCreator teamItem;
    private ItemCreator activeScenario;
    private ItemCreator activeRole;
    private ItemCreator reglesItem;


    public static Common get() {
        return Main.getCommon();
    }

    public void setup() {
        getServerInfo();
        loadItems();
        LobbyCreator.cloneWorld(ConfigUtils.getWorldConfig().getString("world_lobby.lobby_template"), ConfigUtils.getWorldConfig().getString("world_lobby.lobby_target"));
        new WorldGenerator(Main.get(), arenaName);

    }

    public void getServerInfo() {
        servername = Main.get().getConfig().getString("serverinfo.servername");
        servertag = Main.get().getConfig().getString("serverinfo.servertag");
        infoTag = Main.get().getConfig().getString("serverinfo.info");
        helpop = Main.get().getConfig().getString("serverinfo.helpop");
        serverIp = Main.get().getConfig().getString("serverinfo.ip");
        mbip = Main.get().getConfig().getString("mumble.ip");
        mbport = Main.get().getConfig().getString("mumble.port");
        arenaName = ConfigUtils.getWorldConfig().getString("arena_world.name");
        lobbyName = ConfigUtils.getWorldConfig().getString("world_lobby.lobby_target");
        mainColor = ConfigUtils.getLangConfig().getString("main_color");

    }

    public void loadItems() {
        configItem = (new ItemCreator(Material.REDSTONE_COMPARATOR))
                .setName(CommonString.ITEM_CONFIG_NAME.getMessage()).setGlow(true);
        teamItem = (new ItemCreator(Material.BANNER)).setName(CommonString.ITEM_TEAM_NAME.getMessage());
        activeScenario = (new ItemCreator(Material.BOOK)).setName(CommonString.ITEM_ACTIVE_SCENARIO_NAME.getMessage());
        activeRole = (new ItemCreator(Material.PAPER)).setName(CommonString.ITEM_ACTIVE_ROLE_NAME.getMessage());
        reglesItem = (new ItemCreator(Material.NETHER_STAR)).setName(CommonString.ITEM_TELEPORTATION_NAME.getMessage());
    }

    public World getArena() {
        return Bukkit.getWorld(arenaName);
    }

    public World getLobby() {
        return Bukkit.getWorld(lobbyName);
    }

    public World getNether() {
        return Bukkit.getWorld(arenaName + "_nether");
    }

    public World getEnd() {
        return Bukkit.getWorld(arenaName + "_the_end");
    }

    public Location getLobbySpawn() {
        return ConfigUtils.getLocation(ConfigUtils.getWorldConfig(), "world_lobby.location.spawn");
    }

    public Location getRulesSpawn() {
        return ConfigUtils.getLocation(ConfigUtils.getWorldConfig(), "world_lobby.location.rules");
    }
}
