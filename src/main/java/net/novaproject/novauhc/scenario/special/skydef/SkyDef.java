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
    private World world;
    private final int COOLDOWN_TIME = 5;
    private int team_size = 3;
    private boolean bannerBreak = false;
    private Location tpLoc;

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

            tpLoc = ConfigUtils.getLocation(getConfig(), "tp_bas");
            if (tpLoc != null) {
                int x = tpLoc.getBlockX();
                int z = tpLoc.getBlockZ();
                int y = tpLoc.getWorld().getHighestBlockYAt(x, z);
                tpLoc = new Location(world, x, y, z);

                UHCUtils.loadSchematic(
                        Main.get(),
                        new File(Main.get().getDataFolder(), "api/schem/tp.schematic"),
                        tpLoc
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

    public void createDefTeam() {
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

        Location tpLowerArea = tpLoc;
        Location tpUpperArea = ConfigUtils.getLocation(getConfig(), "tp_haut");
        if (tpLowerArea == null || tpUpperArea == null) return;

        long cooldownLeft = ShortCooldownManager.get(player, "tp");
        if (cooldownLeft > 0) return;

        Location playerLoc = player.getLocation();
        boolean teleported = false;

        if (isInTeleportArea(playerLoc, tpLowerArea, 1)) {
            player.teleport(tpUpperArea.clone().add(0, 1, 0));
            teleported = true;
        } else if (isInTeleportArea(playerLoc, tpUpperArea, 1)) {
            player.teleport(tpLowerArea.clone().add(0, 1, 0));
            teleported = true;
        }

        if (teleported) {
            ShortCooldownManager.put(player, "tp", 1000L * COOLDOWN_TIME);
        }
    }


    private boolean isInTeleportArea(Location location, Location center, int radius) {
        if (location == null || center == null) return false;

        return Math.abs(location.getBlockX() - center.getBlockX()) <= radius &&
                Math.abs(location.getBlockY() - center.getBlockY()) <= 1 &&
                Math.abs(location.getBlockZ() - center.getBlockZ()) <= radius;
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        Location bannerLoc = ConfigUtils.getLocation(getConfig(), "banner_loc");
        if (bannerLoc == null || bannerLoc.getWorld() == null) return;

        World world = bannerLoc.getWorld();
        Block bannerBlock = world.getBlockAt(bannerLoc);

        int radius = 1;
        if (Math.abs(block.getX() - bannerLoc.getBlockX()) <= radius
                && Math.abs(block.getY() - bannerLoc.getBlockY()) <= radius
                && Math.abs(block.getZ() - bannerLoc.getBlockZ()) <= radius) {

            if (!block.getLocation().equals(bannerBlock.getLocation())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Cette zone autour de la bannière est protégée !");
                return;
            }
        }

        // Ne vérifier la bannière que si on est dans la zone ET que c'est le bloc de la bannière
        if (Math.abs(block.getX() - bannerLoc.getBlockX()) <= radius
                && Math.abs(block.getY() - bannerLoc.getBlockY()) <= radius
                && Math.abs(block.getZ() - bannerLoc.getBlockZ()) <= radius
                && block.getLocation().equals(bannerBlock.getLocation())) {

            Material checkType = bannerBlock.getType();

            if (isDefTeamAlive()) {
                if (isBannerMaterial(checkType)) {
                    player.sendMessage("La team des défenseurs n'est pas encore morte.");
                    event.setCancelled(true);
                }
            } else {
                if (checkType == Material.AIR) {
                    StringJoiner joiner = new StringJoiner(", ");
                    uhcPlayerTeam(player).ifPresent(team ->
                            team.getPlayers().forEach(p -> joiner.add(p.getPlayer().getDisplayName()))
                    );

                    Bukkit.broadcastMessage(ChatColor.YELLOW +
                            " Bien joué à l'équipe " +
                            uhcPlayerTeam(player).map(UHCTeam::getName).orElse("???") +
                            " composée de : " + joiner
                    );
                    bannerBreak = true;
                }
            }
        }
    }

    private boolean isBannerMaterial(Material material) {
        return material == Material.BANNER || material == Material.WALL_BANNER ||
                material.name().endsWith("_BANNER") || material.name().endsWith("_WALL_BANNER");
    }

    @Override
    public void onPlace(Player player, Block block, BlockPlaceEvent event) {
        Location bannerLoc = ConfigUtils.getLocation(getConfig(), "banner_loc");
        if (bannerLoc == null || world == null) return;

        int radius = 5;
        double distance = block.getLocation().distance(bannerLoc);

        if (distance <= radius) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Tu ne peux rien poser dans une sphère de 5 blocs autour de la bannière !");
        }
    }



    private java.util.Optional<UHCTeam> uhcPlayerTeam(Player player) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null) return java.util.Optional.empty();
        return uhcPlayer.getTeam();
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        Player player = uhcPlayer.getPlayer();
        if (player == null) return;

        if (defTeam.getPlayers().contains(uhcPlayer)) {
            Location loc = ConfigUtils.getLocation(getConfig(), "def_spawn");
            if (loc != null) player.teleport(loc);
            return;
        }
        if (UHCManager.get().getTeam_size() > 1) {
            UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
        } else {
            player.teleport(location);
        }
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
            Block bannerBlock = bannerLoc.getBlock();
            bannerBlock.setType(Material.WALL_BANNER);

            bannerBlock.setData((byte) 5);
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
