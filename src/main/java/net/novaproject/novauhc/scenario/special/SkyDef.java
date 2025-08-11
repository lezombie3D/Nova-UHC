package net.novaproject.novauhc.scenario.special;

/*import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;*/

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.world.generation.WorldGenerator;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.UUID;

public class SkyDef extends Scenario {

    private final World world = Common.get().getArena();
    private final Pattern[] pattern = new Pattern[]{new Pattern(DyeColor.BLACK, PatternType.FLOWER)};
    private final UHCTeam defTeam = new UHCTeam(DyeColor.BLUE, "§9[Défenseur] ", "§9Défenseur", pattern, UHCManager.get().getTeam_size() + 1, true);
    private final String schematicName = "sky.schem";
    private final HashMap<UUID, Long> teleportCooldown = new HashMap<>();
    private final int COOLDOWN_TIME = 2;
    private boolean bannerBreak = false;

    @Override
    public String getName() {
        return "SkyDef";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BANNER);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public void toggleActive() {
        super.toggleActive();

        if (isActive()) {
            bannerBreak = false;
            UHCTeamManager.get().addTeams(defTeam);
            UHCTeamManager.get().deleteTeams();

            //placeStructureAtLocation(new Location(world, 0, 180, 0),schematicName);

        } else {
            UHCTeamManager.get().removeTeam(defTeam);
            UHCTeamManager.get().deleteTeams();
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.teleport(Common.get().getLobbySpawn());
            }
            new WorldGenerator(Main.get(), Common.get().getArenaName());
        }
    }

    @Override
    public void onTeamUpdate() {
        if (UHCManager.get().getTeam_size() == 1) {
            UHCManager.get().setTeam_size(2);
            Bukkit.broadcastMessage(ChatColor.YELLOW + "La taille des équipes a été automatiquement définie à 2 pour le mode FallenKingdom.");
        }

    }

    /*private void placeStructureAtLocation(Location location,String filename) {
        try {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("wd");
            if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
                Bukkit.getLogger().severe("WorldEdit plugin not found or not compatible!");
                return;
            }

            WorldEditPlugin worldEditPlugin = (WorldEditPlugin) plugin;

            File schematicFile = new File(Bukkit.getPluginManager().getPlugin("NovaUHC").getDataFolder(),
                    "schematics" + File.separator + filename);

            if (!schematicFile.exists()) {
                Bukkit.getLogger().severe("Schematic file not found: " + schematicFile.getAbsolutePath());
                return;
            }

            CuboidClipboard clipboard = MCEditSchematicFormat.getFormat(schematicFile).load(schematicFile);

            EditSession editSession = worldEditPlugin.getWorldEdit().getEditSessionFactory()
                    .getEditSession(new BukkitWorld(location.getWorld()), 10000);

            clipboard.paste(editSession, new Vector(location.getX(), location.getY(), location.getZ()), false);

            Bukkit.broadcastMessage(ChatColor.GREEN + "Structure "+schematicName+" placée avec succès en " +
                    location.getX() + ", " + location.getY() + ", " + location.getZ() + "!");

        } catch (Exception e) {
            Bukkit.getLogger().severe("Error placing structure: " + e.getMessage());
            e.printStackTrace();
        }
    }*/

    @Override
    public void onMove(Player player, PlayerMoveEvent event) {
        if (!isActive()) return;

        World world = Common.get().getArena();
        if (world == null) return;

        if (!player.getWorld().equals(world)) return;

        Location tpLowerArea = new Location(world, 0, 70, 0);
        Location tpUpperArea = new Location(world, 0, 170, 0);

        Location playerLoc = player.getLocation();

        if (teleportCooldown.containsKey(player.getUniqueId())) {
            long lastTeleportTime = teleportCooldown.get(player.getUniqueId());
            if ((System.currentTimeMillis() / 1000) - lastTeleportTime < COOLDOWN_TIME) {
                return;
            }
        }

        if (isInTeleportArea(playerLoc, tpLowerArea, 1)) {
            // Teleport to upper area
            Location destination = tpUpperArea.clone().add(0, 1, 0);
            player.teleport(destination);
            player.sendMessage(ChatColor.GREEN + "TP en haut !");
            teleportCooldown.put(player.getUniqueId(), System.currentTimeMillis() / 1000);
            return;
        }

        if (isInTeleportArea(playerLoc, tpUpperArea, 1)) {
            // Teleport to lower area
            Location destination = tpLowerArea.clone().add(0, 1, 0);
            player.teleport(destination);
            player.sendMessage(ChatColor.GREEN + "TP en bas !");
            teleportCooldown.put(player.getUniqueId(), System.currentTimeMillis() / 1000);
        }
    }

    private boolean isInTeleportArea(Location location, Location center, int radius) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();

        return Math.abs(x - centerX) <= radius &&
                Math.abs(y - centerY) <= 1 &&
                Math.abs(z - centerZ) <= radius;
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {

        if (world.getBlockAt(new Location(world, 1, 100, 0)).getType() != Material.AIR) {
            event.setCancelled(true);
        }

        if (isDefTeamAlive()) {
            if (world.getBlockAt(new Location(world, 0, 100, 0)).getType() == Material.BANNER || world.getBlockAt(new Location(world, 0, 100, 0)).getType() == Material.WALL_BANNER) {
                player.sendMessage("La team des défenseur n est pas encore morte");
                event.setCancelled(true);
            }
        } else {
            if (world.getBlockAt(new Location(world, 0, 100, 0)).getType() == Material.AIR || world.getBlockAt(new Location(world, 0, 100, 0)).getType() == Material.AIR) {
                String message = "";
                for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayer(player).getTeam().get().getPlayers()) {
                    message = message + " " + uhcPlayer.getPlayer().getDisplayName();
                }
                Bukkit.broadcastMessage(ChatColor.YELLOW + " Bien jouer a l'équipe " + UHCPlayerManager.get().getPlayer(player).getTeam().get().getName() + "composer de : " + message);
                bannerBreak = true;
            }
        }

    }


    @Override
    public void onStart(Player player) {

        ItemCreator boot = new ItemCreator(Material.IRON_BOOTS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        ItemCreator legging = new ItemCreator(Material.IRON_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        ItemCreator chest = new ItemCreator(Material.IRON_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        ItemCreator helmet = new ItemCreator(Material.IRON_HELMET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        ItemCreator food = new ItemCreator(Material.GOLDEN_CARROT).setAmount(64);
        ItemCreator book = new ItemCreator(Material.BOOK).setAmount(7);

        for (UHCPlayer pl : defTeam.getPlayers()) {
            pl.getPlayer().getInventory().setBoots(boot.getItemstack());
            pl.getPlayer().getInventory().setLeggings(legging.getItemstack());
            pl.getPlayer().getInventory().setChestplate(chest.getItemstack());
            pl.getPlayer().getInventory().setHelmet(helmet.getItemstack());
            pl.getPlayer().getInventory().addItem(food.getItemstack());
            pl.getPlayer().getInventory().addItem(book.getItemstack());
        }

    }

    @Override
    public boolean isWin() {
        return bannerBreak;
    }

    private boolean isDefTeamAlive() {
        return defTeam.isAlive();
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        Player player = uhcPlayer.getPlayer();
        Location location = player.getLocation();
        player.setGameMode(GameMode.SPECTATOR);
        player.spigot().respawn();
        player.teleport(location);
        event.setDeathMessage(null);
        for (Player alive : Bukkit.getOnlinePlayers()) {
            alive.playSound(alive.getLocation(), Sound.WITHER_SPAWN, 1, 1);
        }
    }


    @Override
    public void onChatSpeek(Player player, String message, AsyncPlayerChatEvent event) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        if (UHCManager.get().getGameState() != UHCManager.GameState.INGAME) {
            for (UHCPlayer lobby : UHCPlayerManager.get().getOnlineUHCPlayers()) {
                lobby.getPlayer().sendMessage(ChatColor.DARK_GRAY + "❯ "
                        + player.getName() + " » "
                        + ChatColor.WHITE + message);
            }
            return;
        }

        if (!uhcPlayer.isPlaying()) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas le droit de parler !");
            return;
        }

        if (UHCManager.get().isChatdisbale()) return;

        if (UHCManager.get().getTeam_size() == 1) {
            for (UHCPlayer lobby : UHCPlayerManager.get().getOnlineUHCPlayers()) {
                lobby.getPlayer().sendMessage(ChatColor.DARK_GRAY + "❯ "
                        + player.getName() + " » "
                        + ChatColor.WHITE + message);
            }
            return;
        }

        if (message.startsWith("!")) {
            for (UHCPlayer lobby : UHCPlayerManager.get().getOnlineUHCPlayers()) {
                lobby.getPlayer().sendMessage(ChatColor.GREEN + "✦ Global ✦ "
                        + ChatColor.DARK_GRAY + player.getName() + " » "
                        + ChatColor.WHITE + message.substring(1));
            }
            return;
        }

        if (!uhcPlayer.getTeam().isPresent()) return;

        UHCTeam team = uhcPlayer.getTeam().get();
        for (UHCPlayer teamPlayer : team.getPlayers()) {
            teamPlayer.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "❖ Team ❖ "
                    + ChatColor.DARK_GRAY + player.getName() + " » "
                    + ChatColor.WHITE + message);
        }
    }
}
