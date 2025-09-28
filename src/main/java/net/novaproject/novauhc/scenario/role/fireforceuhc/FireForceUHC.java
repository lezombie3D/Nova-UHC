package net.novaproject.novauhc.scenario.role.fireforceuhc;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.scenario.role.fireforceuhc.role.Sho;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.TeamsTagsManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FireForceUHC extends ScenarioRole<FireForceRole> {
    private int fragment = 0;
    private boolean canAssemble = false;

    @Override
    public String getName() {
        return "Fire Force UHC";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.FIREBALL);
    }

    @Override
    public void setup() {
        addRole(Sho.class);
    }

    @Override
    public void onAfterDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        waitDeath(event, uhcPlayer);
    }

    @Override
    public void giveRoles() {
        super.giveRoles();
        List<UHCPlayer> players = getPlayersByCamps("homme");
        for (UHCPlayer uhcPlayer : players) {
            sendListByCamps("homme", uhcPlayer.getPlayer());
        }
    }

    public void sendListByCamps(String camps, Player player) {
        List<UHCPlayer> players = getPlayersByCamps(camps);
        StringBuilder list = new StringBuilder();
        for (UHCPlayer uhcPlayer : players) {
            list.append(uhcPlayer.getPlayer().getDisplayName()).append(" ");
        }
        player.sendMessage(ChatColor.RED + "vos allier : " + list);
    }

    /*@Override
    public void onFfCMD(Player player, String subCommand, String[] args) {
        super.onFfCMD(player, subCommand, args);
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        switch (subCommand) {
            case "desc":
                Role role = getRoleByUHCPlayer(uhcPlayer);
                player.sendMessage(role.getDescription());
                if (getPlayersByCamps("homme").contains(uhcPlayer)) {
                    sendListByCamps("homme", player);
                }
                break;
            default:
                break;
        }
    }*/

    @Override
    public boolean hascustomDeathMessage() {
        return true;
    }

    @Override
    public FireForceRole getRoleByUHCPlayer(UHCPlayer player) {
        return super.getRoleByUHCPlayer(player);
    }

    @Override
    public List<UHCPlayer> getPlayersByRoleName(String name) {
        return super.getPlayersByRoleName(name);
    }

    @Override
    public Camps[] getCamps() {
        return new Camps[0];
    }

    @Override
    public void onSec(Player p) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(p);
        List<UHCPlayer> hommes = getPlayersByCamps("homme");
        if (hommes.contains(uhcPlayer)) {
            FireForceRole role = getRoleByUHCPlayer(uhcPlayer);
            if (role.hasFragment()) {
                fragment++;
            }
        }
        if (fragment <= 8 && !canAssemble) {
            canAssemble = true;
            for (UHCPlayer homme : hommes) {
                homme.getPlayer().sendMessage("Les fragments ont été réunis, vous commencez le rituel");
            }
        } else {
            canAssemble = false;
        }
    }

    public boolean isWin() {
        Map<String, Integer> campCounts = new HashMap<>();

        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Role role = getRoleByUHCPlayer(uhcPlayer);
            String playerCamp = role.getCamp().toString();
            campCounts.put(playerCamp, campCounts.getOrDefault(playerCamp, 0) + 1);
        }
        if (campCounts.size() == 1) {
            String remainingCamp = campCounts.keySet().iterator().next();

            if (isDuoCamp(remainingCamp)) {
                return campCounts.get(remainingCamp) == 2;
            }
            return true;
        }

        for (String camp : campCounts.keySet()) {
            if (isSoloCamp(camp) && campCounts.get(camp) == 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        List<UHCPlayer> hommes = getPlayersByCamps("homme");
        FireForceRole role = getRoleByUHCPlayer(uhcPlayer);
        if (hommes.contains(killer)) {
            if (role.hasFragment()) {
                fragment++;
            }
        }
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        uhcPlayer.getPlayer().teleport(location);
    }


    private boolean isSoloCamp(String camp) {
        return camp.startsWith("Solo");
    }

    private boolean isDuoCamp(String camp) {
        return camp.startsWith("Duo");
    }

    private void waitDeath(PlayerDeathEvent event, UHCPlayer uhcPlayer) {
        TeamsTagsManager.setNameTag(uhcPlayer.getPlayer(), "", "", "");
        uhcPlayer.getPlayer().setGameMode(GameMode.ADVENTURE);
        Player player = uhcPlayer.getPlayer();
        Location location = player.getLocation();
        World world = Common.get().getArena();

        ItemStack[] inventoryContents = player.getInventory().getContents();
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        event.getDrops().clear();
        player.teleport(Common.get().getLobbySpawn());
        player.sendMessage(ChatColor.RED + "Vous êtes mort mais vous pouvez toujours être ressusciter");

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
                        ChatColor.AQUA + " Quelqu'un est mort : §6" + uhcPlayer.getPlayer().getDisplayName() + "\n" +
                        "Il etait : " + getRoleByUHCPlayer(uhcPlayer).getColor() + getRoleByUHCPlayer(uhcPlayer).getName() + "\n" +
                        "§8§m---------" + ChatColor.MAGIC + "jdsqjlkmsqjsqjml§8§m----------§r";

                Bukkit.broadcastMessage(message);
                player.setGameMode(GameMode.SPECTATOR);
                TeamsTagsManager.setNameTag(player, "zzzzz", "§8§lSPEC §r§8", "");
                UHCManager.get().checkVictory();
            }
        }.runTaskLater(Main.get(), 120L);
    }

    public List<UHCPlayer> getPlayersByCamps(String camps) {
        List<UHCPlayer> players = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();
        List<UHCPlayer> playersByCamps = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();
        for (UHCPlayer player : players) {
            if (!getRoleByUHCPlayer(player).getCamp().equals(camps)) {
                playersByCamps.remove(player);
            }
        }
        return playersByCamps;
    }

}
