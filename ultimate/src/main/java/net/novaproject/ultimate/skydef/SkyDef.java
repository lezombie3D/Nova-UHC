package net.novaproject.ultimate.skydef;

import net.novaproject.novauhc.utils.schematic.SchematicUtils;
import net.novaproject.novauhc.utils.UHCUtils.GeoUtils;
import net.novaproject.novauhc.utils.item.ItemCreator.Heads;
import net.novaproject.novauhc.utils.variable.Var;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.cooldown.CooldownService;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.ui.CustomInventory;
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

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

@Getter
@Setter
public class SkyDef extends Scenario {

    private static final String BANNIERE_WAYPOINT = "Bannière";
    private static final Color BANNIERE_COLOR = new Color(255, 85, 85);

    private final Pattern[] pattern = {new Pattern(DyeColor.BLACK, PatternType.FLOWER)};

    @Var(name = "Var Cooldown Time", type = VariableType.INTEGER)
    private int cooldownTime = 5;

    @Var(name = "Var Team Size", type = VariableType.INTEGER)
    private int team_size = 3;

    @Var(name = "Var Tp Radius", type = VariableType.INTEGER)
    private int tpRadius = 1;

    @Var(name = "Var Banner Place Radius", type = VariableType.INTEGER)
    private int bannerPlaceRadius = 5;

    @Var(name = "Var Armor Enchant Level", type = VariableType.INTEGER)
    private int armorEnchantLevel = 2;

    @Var(name = "Var Golden Carrot Amount", type = VariableType.INTEGER)
    private int goldenCarrotAmount = 64;

    @Var(name = "Var Book Amount", type = VariableType.INTEGER)
    private int bookAmount = 7;

    @Var(name = "Var Start Inv", type = VariableType.BOOLEAN)
    private boolean startinv = true;

    private UHCTeam defTeam;
    private World world;
    private boolean bannerBreak = false;
    private Location tpLoc;

    @Override
    public String getName() {
        return LangManager.get().get(SkyDefLang.SKYDEF_NAME);
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(SkyDefLang.SKYDEF_DESC, player);
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
            System.out.println(schemLoc);
            if (schemLoc != null) {
                SchematicUtils.loadSchematic(
                        Main.get(),
                        net.novaproject.novauhc.utils.schematic.SchematicStructure.schematicFile("skydef.schematic"),
                        schemLoc
                );
            }

            tpLoc = ConfigUtils.getLocation(getConfig(), "tp_bas");
            if (tpLoc != null) {
                int x = tpLoc.getBlockX();
                int z = tpLoc.getBlockZ();
                int y = tpLoc.getWorld().getHighestBlockYAt(x, z);
                tpLoc = new Location(world, x, y, z);

                SchematicUtils.loadSchematic(
                        Main.get(),
                        net.novaproject.novauhc.utils.schematic.SchematicStructure.schematicFile("tp.schematic"),
                        tpLoc
                        ,false
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
        if (defTeam != null) {
            if (!defTeam.getPlayers().isEmpty()) {
                for (UHCPlayer player : defTeam.getPlayers()) {
                    player.setTeam(Optional.empty());
                }
            }
            UHCTeamManager.get().removeTeam(defTeam);
        }
        this.defTeam = new UHCTeam(
                DyeColor.BLUE,
                "§9§lDEF §r",
                "defender",
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
            LangManager.get().sendAll(CoreLang.COMMON_TEAM_REDFINIED_AUTO);
        }
    }

    @Override
    public void onMove(Player player, PlayerMoveEvent event) {
        if (!isActive() || world == null) return;
        if (!player.getWorld().equals(world)) return;

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null) return;
        if(!defTeam.getPlayers().contains(uhcPlayer)) return;
        Location tpLowerArea = tpLoc;
        Location tpUpperArea = ConfigUtils.getLocation(getConfig(), "tp_haut");
        if (tpLowerArea == null || tpUpperArea == null) return;

        if (CooldownService.get(player, "tp") > 0) return;

        Location playerLoc = player.getLocation();
        boolean teleported = false;

        if (GeoUtils.isInTeleportArea(playerLoc, tpLowerArea, tpRadius)) {
            player.teleport(tpUpperArea.clone().add(0, 1, 0));
            teleported = true;
        } else if (GeoUtils.isInTeleportArea(playerLoc, tpUpperArea, tpRadius)) {
            player.teleport(tpLowerArea.clone().add(0, 1, 0));
            teleported = true;
        }

        if (teleported) {
            CooldownService.put(player, "tp", 1000L * cooldownTime);
        }
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
                LangManager.get().send(SkyDefLang.BANNER_ZONE_PROTECTED, player);
                return;
            }
        }

        if (Math.abs(block.getX() - bannerLoc.getBlockX()) <= radius
                && Math.abs(block.getY() - bannerLoc.getBlockY()) <= radius
                && Math.abs(block.getZ() - bannerLoc.getBlockZ()) <= radius
                && block.getLocation().equals(bannerBlock.getLocation())) {

            Material checkType = bannerBlock.getType();

            if (isDefTeamAlive()) {
                if (Heads.isBannerMaterial(checkType)) {
                    LangManager.get().send(SkyDefLang.DEFENDERS_NOT_DEAD, player);
                    event.setCancelled(true);
                }
            } else {
                if (Heads.isBannerMaterial(checkType)) {
                    StringJoiner joiner = new StringJoiner(", ");
                    uhcPlayerTeam(player).ifPresent(team ->
                            team.getPlayers().forEach(p -> joiner.add(p.getPlayer().getDisplayName()))
                    );

                    String teamName = uhcPlayerTeam(player).map(UHCTeam::name).orElse("???");
                    Bukkit.broadcastMessage(LangManager.get().get(SkyDefLang.BANNER_BROKEN_BROADCAST, Map.of(
                            "%team%", teamName,
                            "%players%", joiner.toString()
                    )));
                    bannerBreak = true;
                    for (UHCPlayer pl : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                        Player viewer = pl.getPlayer();
                        if (viewer != null) DisplayService.removeWaypoint(viewer, BANNIERE_WAYPOINT);
                    }
                    UHCManager.get().checkVictory();
                }
            }
        }
    }

    @Override
    public void onPlace(Player player, Block block, BlockPlaceEvent event) {
        Location bannerLoc = ConfigUtils.getLocation(getConfig(), "banner_loc");
        if (bannerLoc == null || world == null) return;

        if (block.getLocation().distance(bannerLoc) <= bannerPlaceRadius) {
            event.setCancelled(true);
            LangManager.get().send(SkyDefLang.BANNER_PLACE_FORBIDDEN, player, Map.of("%radius%", bannerPlaceRadius));
        }
    }

    private Optional<UHCTeam> uhcPlayerTeam(Player player) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null) return Optional.empty();
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
    public void onGameStart() {
        Location bannerLoc = ConfigUtils.getLocation(getConfig(), "banner_loc");
        if (bannerLoc != null && bannerLoc.getWorld() != null) {
            Block bannerBlock = bannerLoc.getBlock();
            bannerBlock.setType(Material.WALL_BANNER);
            bannerBlock.setData((byte) getConfig().getInt("banner_data"));

            for (UHCPlayer pl : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player p = pl.getPlayer();
                if (p != null) DisplayService.waypoint(p, BANNIERE_WAYPOINT, bannerLoc, BANNIERE_COLOR, true);
            }
        }

        ItemCreator[] items = {
                new ItemCreator(Material.IRON_BOOTS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, armorEnchantLevel),
                new ItemCreator(Material.IRON_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, armorEnchantLevel),
                new ItemCreator(Material.IRON_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, armorEnchantLevel),
                new ItemCreator(Material.IRON_HELMET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, armorEnchantLevel),
                new ItemCreator(Material.GOLDEN_CARROT).setAmount(goldenCarrotAmount),
                new ItemCreator(Material.BOOK).setAmount(bookAmount)
        };

        for (UHCPlayer pl : defTeam.getPlayers()) {
            Player p = pl.getPlayer();
            if (p == null) continue;
            if(!startinv) continue;
            p.getInventory().setBoots(items[0].getItemstack());
            p.getInventory().setLeggings(items[1].getItemstack());
            p.getInventory().setChestplate(items[2].getItemstack());
            p.getInventory().setHelmet(items[3].getItemstack());
            p.getInventory().addItem(items[4].getItemstack(), items[5].getItemstack());
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

