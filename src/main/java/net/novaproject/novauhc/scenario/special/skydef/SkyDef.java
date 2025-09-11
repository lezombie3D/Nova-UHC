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
import org.bukkit.event.player.PlayerMoveEvent;

import java.io.File;
import java.util.StringJoiner;

@Getter
@Setter
public class SkyDef extends Scenario {

    private final Pattern[] pattern = {new Pattern(DyeColor.BLACK, PatternType.FLOWER)};
    private UHCTeam defTeam;
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

            Location schemLoc = ConfigUtils.getLocation(getConfig(), "shem_loc");
            if (schemLoc != null) {
                UHCUtils.loadSchematic(
                        Main.get(),
                        new File(Main.get().getDataFolder(), "api/schem/skydef.schematic"),
                        schemLoc
                );
            }

            Location tpLoc = ConfigUtils.getLocation(getConfig(), "tp_bas");
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

        Location tpLowerArea = ConfigUtils.getLocation(getConfig(), "tp_bas");
        Location tpUpperArea = ConfigUtils.getLocation(getConfig(), "tp_haut");
        if (tpLowerArea == null || tpUpperArea == null) return;

        Location playerLoc = player.getLocation();

        if (ShortCooldownManager.get(player, "tp") != -1
                && !defTeam.getPlayers().contains(uhcPlayer)) return;

        if (isInTeleportArea(playerLoc, tpLowerArea, 1)) {
            player.teleport(tpUpperArea.clone().add(0, 1, 0));
            ShortCooldownManager.put(player, "tp", 1000L * COOLDOWN_TIME);
            return;
        }

        if (isInTeleportArea(playerLoc, tpUpperArea, 1)) {
            player.teleport(tpLowerArea.clone().add(0, 1, 0));
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
        if (bannerLoc == null || world == null) return;

        Block bannerBlock = world.getBlockAt(bannerLoc);
        if (bannerBlock.getType() != Material.AIR) {
            event.setCancelled(true);
        }

        Location checkLoc = new Location(world, 0, 100, 0);
        Material checkType = world.getBlockAt(checkLoc).getType();

        if (isDefTeamAlive()) {
            if (checkType == Material.BANNER || checkType == Material.WALL_BANNER) {
                player.sendMessage("La team des défenseurs n’est pas encore morte.");
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
    }

    @Override
    public boolean isWin() {
        return bannerBreak;
    }

    private boolean isDefTeamAlive() {
        return defTeam != null && defTeam.isAlive();
    }

}
