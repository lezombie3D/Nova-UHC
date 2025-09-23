package net.novaproject.novauhc.scenario.special.skydef;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.scenario.lang.SkyDefLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ShortCooldownManager;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.world.generation.WorldGenerator;
import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.io.File;
import java.util.HashMap;
import java.util.StringJoiner;

@Getter
@Setter
public class SkyDef extends Scenario {

    private final Pattern[] pattern = {new Pattern(DyeColor.BLACK, PatternType.FLOWER)};
    private UHCTeam defTeam;
    private Location tpbas_loc;
    private World world;
    private final int COOLDOWN_TIME = 2;
    private int team_size = 3;
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
    public String getPath() {
        return "special/skydef";
    }

    @Override
    public ScenarioLang[] getLang() {
        return SkyDefLang.values();
    }

    @Override
    public void toggleActive() {
        super.toggleActive();

        if (isActive()) {
            this.world = Common.get().getArena();
            if (world == null) {
                Bukkit.getLogger().severe("Impossible de récupérer l'arène !");
                return;
            }

            bannerBreak = false;
            createDefTeam();

            Location schemLoc = ConfigUtils.getLocation(getConfig(), "schem_loc");
            if (schemLoc != null) {
                UHCUtils.loadSchematic(
                        Main.get(),
                        new File(Main.get().getDataFolder(), "api/schem/skydef.schematic"),
                        schemLoc
                );
            }

            Location tpBas = ConfigUtils.getLocation(getConfig(), "tp_bas");
            if (tpBas != null) {

                int x = tpBas.getBlockX();
                int z = tpBas.getBlockZ();
                int highestY = tpBas.getWorld().getHighestBlockYAt(x, z) + 1;

                this.tpbas_loc = new Location(world, x, highestY, z);
                UHCUtils.loadSchematic(
                        Main.get(),
                        new File(Main.get().getDataFolder(), "api/schem/tp.schematic"),
                        tpbas_loc
                );
            }

        } else {
            if (defTeam != null) {
                UHCTeamManager.get().removeTeam(defTeam);
                UHCTeamManager.get().deleteTeams();
            }
            new WorldGenerator(Main.get(), Common.get().getArenaName());
        }
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        if (UHCManager.get().getTeam_size() == 1) {
            uhcPlayer.getPlayer().teleport(location);
        } else {
            UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
        }
        if (defTeam.getPlayers().contains(uhcPlayer)) {
            uhcPlayer.getPlayer().teleport(ConfigUtils.getLocation(getConfig(), "def_spawn"));
        }
    }

    public void createDefTeam() {
        if (defTeam != null) {
            UHCTeamManager.get().removeTeam(defTeam);
        }
        this.defTeam = new UHCTeam(
                DyeColor.BLUE,
                "§9[Défenseur] ",
                "§9Défenseur",
                pattern,
                team_size,
                true
        );
        UHCTeamManager.get().addTeams(defTeam);
        UHCTeamManager.get().deleteTeams();
    }

    @Override
    public CustomInventory getMenu(Player player) {
        return new SkyDefUi(player);
    }

    @Override
    public void onTeamUpdate() {
        if (UHCManager.get().getTeam_size() == 1) {
            UHCManager.get().setTeam_size(2);
            CommonString.TEAM_REDFINIED_AUTO.sendAll();
        }
    }

    @Override
    public void onMove(Player player, PlayerMoveEvent event) {
        if (!isActive() || world == null) return;
        if (!player.getWorld().equals(world)) return;

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null) return;

        Location tpLowerArea = tpbas_loc;
        Location tpUpperArea = ConfigUtils.getLocation(getConfig(), "tp_haut");
        if (tpLowerArea == null || tpUpperArea == null) return;

        Location playerLoc = player.getLocation();

        if (ShortCooldownManager.get(player, "tp") != -1) {
            return;
        }

        if (isInTeleportArea(playerLoc, tpLowerArea, 2)) {
            Location targetLocation = tpUpperArea.clone().add(0.5, 1, 0.5);
            targetLocation.setYaw(player.getLocation().getYaw());
            targetLocation.setPitch(player.getLocation().getPitch());

            player.teleport(targetLocation);
            player.sendMessage("§aTéléportation vers la zone haute !");
            ShortCooldownManager.put(player, "tp", 1000L * COOLDOWN_TIME);

            return;
        }

        if (isInTeleportArea(playerLoc, tpUpperArea, 2)) {
            Location targetLocation = tpLowerArea.clone().add(0.5, 1, 0.5);
            targetLocation.setYaw(player.getLocation().getYaw());
            targetLocation.setPitch(player.getLocation().getPitch());

            player.teleport(targetLocation);
            player.sendMessage("§aTéléportation vers la zone basse !");
            ShortCooldownManager.put(player, "tp", 1000L * COOLDOWN_TIME);

        }
    }

    private boolean isInTeleportArea(Location location, Location center, int radius) {
        if (location == null || center == null) return false;
        if (!location.getWorld().equals(center.getWorld())) return false;

        double deltaX = Math.abs(location.getX() - center.getX());
        double deltaY = Math.abs(location.getY() - center.getY());
        double deltaZ = Math.abs(location.getZ() - center.getZ());

        boolean inRadius = deltaX <= radius && deltaZ <= radius && deltaY <= 3;

        if (inRadius) {
            location.getWorld().playSound(location, Sound.NOTE_PLING, 1.0f, 1.0f);
        }

        return inRadius;
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        Location bannerLoc = ConfigUtils.getLocation(getConfig(), "banner_loc");
        if (bannerLoc == null || world == null) return;

        Location blockLoc = block.getLocation();

        if (bannerLoc.distance(blockLoc) <= 2.0) {
            if (blockLoc.equals(bannerLoc)) {
                Material blockType = block.getType();
                if (blockType != Material.BANNER && blockType != Material.WALL_BANNER) {
                    return;
                }

                if (isDefTeamAlive()) {
                    player.sendMessage("La team des défenseurs n'est pas encore morte.");
                    event.setCancelled(true);
                } else {
                    StringJoiner joiner = new StringJoiner(", ");
                    uhcPlayerTeam(player).ifPresent(team ->
                            team.getPlayers().forEach(p -> joiner.add(p.getPlayer().getDisplayName()))
                    );

                    bannerBreak = true;
                    UHCManager.get().checkVictory();
                }
            } else {
                player.sendMessage("Vous ne pouvez pas casser de blocs près de la bannière !");
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onPlace(Player player, Block block, BlockPlaceEvent event) {
        Location bannerLoc = ConfigUtils.getLocation(getConfig(), "banner_loc");
        if (bannerLoc == null) return;

        Location blockLoc = event.getBlock().getLocation();

        if (bannerLoc.distance(blockLoc) <= 2.0) {
            event.getPlayer().sendMessage("Vous ne pouvez pas placer de blocs près de la bannière !");
            event.setCancelled(true);
        }
    }

    private java.util.Optional<UHCTeam> uhcPlayerTeam(Player player) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null) return java.util.Optional.empty();
        return uhcPlayer.getTeam();
    }

    @Override
    public void onStart(Player player) {
        ItemCreator[] items = {
                new ItemCreator(Material.IRON_BOOTS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2),
                new ItemCreator(Material.IRON_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2),
                new ItemCreator(Material.IRON_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2),
                new ItemCreator(Material.IRON_HELMET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2),
                new ItemCreator(Material.GOLDEN_CARROT).setAmount(64),
                new ItemCreator(Material.BOOK).setAmount(7)
        };

        for (UHCPlayer pl : defTeam.getPlayers()) {
            Player p = pl.getPlayer();
            if (p == null) continue;

            p.getInventory().setBoots(items[0].getItemstack());
            p.getInventory().setLeggings(items[1].getItemstack());
            p.getInventory().setChestplate(items[2].getItemstack());
            p.getInventory().setHelmet(items[3].getItemstack());
            p.getInventory().addItem(items[4].getItemstack(), items[5].getItemstack());
        }

        Location bannerLoc = ConfigUtils.getLocation(getConfig(), "banner_loc");
        if (bannerLoc != null && bannerLoc.getWorld() != null) {
            Block bannerBlock = bannerLoc.getWorld().getBlockAt(bannerLoc);

            bannerBlock.setType(Material.WALL_BANNER);

            // 2 = Nord, 3 = Sud, 4 = Ouest, 5 = Est
            bannerBlock.setData((byte) 5); // 5 = Est (dos face à l'est)

            if (bannerBlock.getState() instanceof Banner) {
                Banner banner = (Banner) bannerBlock.getState();
                banner.setBaseColor(DyeColor.WHITE);
                banner.update();
            }
        }
    }

    @Override
    public boolean isWin() {
        return bannerBreak;
    }

    private boolean isDefTeamAlive() {
        return defTeam != null && defTeam.isAlive();
    }

}