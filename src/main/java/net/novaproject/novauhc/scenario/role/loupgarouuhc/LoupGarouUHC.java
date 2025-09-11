package net.novaproject.novauhc.scenario.role.loupgarouuhc;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.scenario.role.loupgarouuhc.roles.Assasin;
import net.novaproject.novauhc.scenario.role.loupgarouuhc.roles.LoupGarou;
import net.novaproject.novauhc.scenario.role.loupgarouuhc.roles.Renard;
import net.novaproject.novauhc.scenario.role.loupgarouuhc.roles.Villageois;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class LoupGarouUHC extends ScenarioRole<LoupGarouRole> {


    @Override
    public String getName() {
        return "LGUHC";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.NETHER_STAR);
    }

    @Override
    public Camps[] getCamps() {
        return LGCamps.values();
    }

    @Override
    public void setup() {
        addRole(Villageois.class);
        addRole(LoupGarou.class);
        addRole(Renard.class);
        addRole(Assasin.class);
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        uhcPlayer.getPlayer().setGameMode(GameMode.ADVENTURE);
        Player player = uhcPlayer.getPlayer();
        Location location = player.getLocation();
        World world = Common.get().getArena();

        ItemStack[] inventoryContents = player.getInventory().getContents();
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        event.getDrops().clear();
        player.teleport(Common.get().getLobbySpawn());
        player.sendMessage(ChatColor.RED + "Vous etes mort mais vous pouvez toujours être ressusciter");

        new BukkitRunnable() {
            @Override
            public void run() {

                for (ItemStack item : inventoryContents) {
                    if (item != null && item.getType() != Material.AIR) {
                        world.dropItemNaturally(location, item);
                    }
                }

                for (ItemStack item : armorContents) {
                    if (item != null && item.getType() != Material.AIR) {
                        world.dropItemNaturally(location, item);
                    }
                }
                String message = "§8§m---------" + ChatColor.MAGIC + "jdsqjlkmsqjsqjml§8§m----------§r\n" +
                        ChatColor.AQUA + "Le village a perdu un de ses membre §6" + uhcPlayer.getPlayer().getDisplayName() + "\n" +
                        "Il etait : " + getRoleByUHCPlayer(uhcPlayer).getName() + "\n" +
                        "§8§m---------" + ChatColor.MAGIC + "jdsqjlkmsqjsqjml§8§m----------§r";

                Bukkit.broadcastMessage(message);
                player.setGameMode(GameMode.SPECTATOR);
            }
        }.runTaskLater(Main.get(), 120L);

    }

    @Override
    public LoupGarouRole getRoleByUHCPlayer(UHCPlayer player) {
        return super.getRoleByUHCPlayer(player);
    }

    public boolean isWin() {
        Map<Camps, Integer> campCounts = new HashMap<>();

        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Role role = getRoleByUHCPlayer(uhcPlayer);
            if (role == null) continue;

            Camps camp = role.getCamp();
            campCounts.put(camp, campCounts.getOrDefault(camp, 0) + 1);
        }

        return campCounts.size() == 1;
    }

}
