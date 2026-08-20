package net.novaproject.ultimate.taupedefender;

import net.novaproject.novauhc.utils.schematic.SchematicUtils;
import net.novaproject.novauhc.utils.UHCUtils.GeoUtils;
import net.novaproject.novauhc.utils.item.ItemCreator.Heads;
import net.novaproject.novauhc.utils.variable.Var;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.CommandManager;
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
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.utils.variable.VariableType;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class TaupeDefender extends Scenario {

    private static TaupeDefender instance;

    private final Pattern[] pattern = {new Pattern(DyeColor.BLACK, PatternType.FLOWER)};

    @Var(name = "Var Mole Delay", type = VariableType.INTEGER)
    private int moleDelay = 20;

    @Var(name = "Var Team Size", type = VariableType.INTEGER)
    private int defTeamSize = 3;

    @Var(name = "Var Cooldown Time", type = VariableType.INTEGER)
    private int cooldownTime = 5;

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
    private boolean startInv = true;

    @Var(name = "Var Group Check Interval", type = VariableType.INTEGER)
    private int groupCheckInterval = 5;

    private UHCTeam defTeam;
    private World world;
    private Location tpLoc;
    private Location cachedBannerLoc;
    private Location cachedTpUpperArea;
    private int cachedBannerData;
    private boolean taupeAssigned = false;
    private boolean bannerCaptured = false;

    private final Set<UHCPlayer> revealedDefenders = new HashSet<>();
    private final HashMap<UHCPlayer, UHCTeam> oldTeam = new HashMap<>();
    private final List<UHCPlayer> claimedKit = new ArrayList<>();
    private final HashMap<UHCPlayer, Integer> kit = new HashMap<>();

    private UHCPlayer capturingPlayer = null;
    private int captureTimeLeft = 0;
    private BukkitTask captureTask = null;
    private BukkitTask groupCheckTask = null;

    public static TaupeDefender getInstance() {
        return instance;
    }

    @Override
    public String getName() {
        return LangManager.get().get(TaupeDefenderLang.TAUPE_DEFENDER_NAME);
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(TaupeDefenderLang.TAUPE_DEFENDER_DESC, player);
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
        return "special/taupedefender";
    }

    @Override
    public void setup() {
        super.setup();
        instance = this;
        taupeAssigned = false;
        bannerCaptured = false;
        revealedDefenders.clear();
        oldTeam.clear();
        claimedKit.clear();
        kit.clear();
        capturingPlayer = null;
        captureTimeLeft = 0;
        if (captureTask != null) { captureTask.cancel(); captureTask = null; }
    }

    @Override
    public void toggleActive() {
        super.toggleActive();

        if (isActive()) {
            this.world = Common.get().getArena();
            if (world == null) {
                Bukkit.getLogger().severe("[TaupeDefender] Impossible de récupérer l'arène !");
                return;
            }

            taupeAssigned = false;
            bannerCaptured = false;
            revealedDefenders.clear();
            oldTeam.clear();
            claimedKit.clear();
            kit.clear();
            defTeam = null;

            cachedBannerLoc = ConfigUtils.getLocation(getConfig(), "banner_loc");
            cachedTpUpperArea = ConfigUtils.getLocation(getConfig(), "tp_haut");
            cachedBannerData = getConfig().getInt("banner_data");

            Location schemLoc = ConfigUtils.getLocation(getConfig(), "schem_loc");
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
                        tpLoc,
                        false
                );
            }
        } else {
            if (captureTask != null) { captureTask.cancel(); captureTask = null; }
            if (groupCheckTask != null) { groupCheckTask.cancel(); groupCheckTask = null; }
            cachedBannerLoc = null;
            cachedTpUpperArea = null;
            if (defTeam != null) {
                UHCTeamManager.get().removeTeam(defTeam);
                UHCTeamManager.get().deleteTeams();
                defTeam = null;
            }
            new WorldGenerator(Main.get(), Common.get().getArenaName());
        }
    }

    @Override
    public void onStop() {
        if (captureTask != null) { captureTask.cancel(); captureTask = null; }
        if (groupCheckTask != null) { groupCheckTask.cancel(); groupCheckTask = null; }
    }

    @Override
    public void onTeamUpdate() {
        if (UHCManager.get().getTeam_size() == 1) {
            UHCManager.get().setTeam_size(2);
            LangManager.get().sendAll(CoreLang.COMMON_TEAM_REDFINIED_AUTO);
        }
    }

    @Override
    public void onGameStart() {
        Bukkit.broadcastMessage("Mode de jeu recréer a partir de la documentation publique d'Eterny");
        if (cachedBannerLoc == null) cachedBannerLoc = ConfigUtils.getLocation(getConfig(), "banner_loc");
        if (cachedTpUpperArea == null) cachedTpUpperArea = ConfigUtils.getLocation(getConfig(), "tp_haut");
        if (cachedBannerLoc != null && cachedBannerLoc.getWorld() != null) {
            Block bannerBlock = cachedBannerLoc.getBlock();
            bannerBlock.setType(Material.WALL_BANNER);
            bannerBlock.setData((byte) cachedBannerData);
        }
        CommandManager.get().register("taupedefender", new TaupeDefenderCMD(), "td");
    }

    @Override
    public void onSec(Player p) {
        if (!isActive()) return;
        if (taupeAssigned) return;
        int timer = UHCManager.get().getTimer();
        int pvp = UHCManager.get().getPvpTimer();
        if (timer == pvp + moleDelay * 60) {
            taupeAssigned = true;
            assignDefenders();
            startGroupCheckTask();
        }
    }

    private void startGroupCheckTask() {
        if (groupCheckTask != null) groupCheckTask.cancel();
        long period = groupCheckInterval * 60L * 20L;
        groupCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) { cancel(); groupCheckTask = null; return; }
                checkGroupLimit();
            }
        }.runTaskTimer(Main.get(), period, period);
    }

    private void assignDefenders() {
        defTeam = new UHCTeam(DyeColor.BLUE, "§9§lDEFENDER", "defender", pattern, defTeamSize, true);
        UHCTeamManager.get().addTeams(defTeam);
        UHCTeamManager.get().deleteTeams();

        Random random = new Random();
        Set<UHCTeam> visited = new HashSet<>();

        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            if (!uhcPlayer.getTeam().isPresent()) continue;
            UHCTeam team = uhcPlayer.getTeam().get();
            if (team.equals(defTeam)) continue;
            if (visited.contains(team)) continue;
            visited.add(team);

            List<UHCPlayer> available = team.getPlayers().stream()
                    .filter(UHCPlayer::isPlaying)
                    .collect(Collectors.toList());
            if (available.isEmpty()) continue;

            UHCPlayer chosen = available.get(random.nextInt(available.size()));
            oldTeam.put(chosen, team);
            kit.put(chosen, random.nextInt(7));
            chosen.setTeam(Optional.of(defTeam));

            Player p = chosen.getPlayer();
            if (p == null) continue;

            DisplayService.title(
                    p,
                    LangManager.get().get(TaupeDefenderLang.DEFENDER_ASSIGNED_TITLE, p),
                    LangManager.get().get(TaupeDefenderLang.DEFENDER_ASSIGNED_SUBTITLE, p),
                    10
            );

            if (startInv) {
                p.getInventory().setBoots(new ItemCreator(Material.IRON_BOOTS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, armorEnchantLevel).getItemstack());
                p.getInventory().setLeggings(new ItemCreator(Material.IRON_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, armorEnchantLevel).getItemstack());
                p.getInventory().setChestplate(new ItemCreator(Material.IRON_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, armorEnchantLevel).getItemstack());
                p.getInventory().setHelmet(new ItemCreator(Material.IRON_HELMET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, armorEnchantLevel).getItemstack());
                p.getInventory().addItem(
                        new ItemCreator(Material.GOLDEN_CARROT).setAmount(goldenCarrotAmount).getItemstack(),
                        new ItemCreator(Material.BOOK).setAmount(bookAmount).getItemstack()
                );
            }
        }
    }

    @Override
    public void onMove(Player player, PlayerMoveEvent event) {
        if (!isActive() || world == null || tpLoc == null || cachedTpUpperArea == null) return;
        if (!player.getWorld().equals(world)) return;

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null) return;
        if (defTeam == null || !defTeam.getPlayers().contains(uhcPlayer)) return;

        Location playerLoc = player.getLocation();

        if (!revealedDefenders.contains(uhcPlayer)) {
            if (GeoUtils.isInTeleportArea(playerLoc, tpLoc, tpRadius)
                    || GeoUtils.isInTeleportArea(playerLoc, cachedTpUpperArea, tpRadius)) {
                LangManager.get().send(TaupeDefenderLang.TP_MUST_REVEAL, player);
            }
            return;
        }

        if (CooldownService.get(player, "tdtp") > 0) return;

        boolean teleported = false;

        if (GeoUtils.isInTeleportArea(playerLoc, tpLoc, tpRadius)) {
            player.teleport(cachedTpUpperArea.clone().add(0, 1, 0));
            teleported = true;
        } else if (GeoUtils.isInTeleportArea(playerLoc, cachedTpUpperArea, tpRadius)) {
            player.teleport(tpLoc.clone().add(0, 1, 0));
            teleported = true;
        }

        if (teleported) {
            CooldownService.put(player, "tdtp", 1000L * cooldownTime);
        }
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (cachedBannerLoc == null || cachedBannerLoc.getWorld() == null) return;

        Block bannerBlock = cachedBannerLoc.getWorld().getBlockAt(cachedBannerLoc);

        int radius = 1;
        boolean nearBanner = Math.abs(block.getX() - cachedBannerLoc.getBlockX()) <= radius
                && Math.abs(block.getY() - cachedBannerLoc.getBlockY()) <= radius
                && Math.abs(block.getZ() - cachedBannerLoc.getBlockZ()) <= radius;

        if (!nearBanner) return;

        if (!block.getLocation().equals(bannerBlock.getLocation())) {
            event.setCancelled(true);
            LangManager.get().send(TaupeDefenderLang.BANNER_ZONE_PROTECTED, player);
            return;
        }

        if (!Heads.isBannerMaterial(block.getType())) return;

        event.setCancelled(true);

        if (!taupeAssigned || defTeam == null) return;

        if (isDefTeamAlive()) {
            UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
            if (uhcPlayer == null) return;
            if (defTeam.getPlayers().contains(uhcPlayer)) return;

            if (capturingPlayer != null) return;

            startCapture(player);
        } else {
            StringJoiner joiner = new StringJoiner(", ");
            UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
            if (uhcPlayer != null) {
                uhcPlayer.getTeam().ifPresent(team ->
                        team.getPlayers().forEach(p -> joiner.add(p.getPlayer().getDisplayName()))
                );
                String teamName = uhcPlayer.getTeam().map(UHCTeam::name).orElse("???");
                Bukkit.broadcastMessage(LangManager.get().get(TaupeDefenderLang.BANNER_CAPTURED_BROADCAST,
                        Map.of("%team%", teamName, "%players%", joiner.toString())));
            }
            bannerCaptured = true;
            UHCManager.get().checkVictory();
        }
    }

    private void startCapture(Player player) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null) return;

        capturingPlayer = uhcPlayer;
        captureTimeLeft = getCaptureTime();

        Bukkit.broadcastMessage(LangManager.get().get(TaupeDefenderLang.CAPTURE_STARTED_BROADCAST,
                Map.of("%player%", player.getName())));

        captureTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) { cancelCapture(false); cancel(); return; }

                Player p = capturingPlayer == null ? null : capturingPlayer.getPlayer();
                if (p == null || !p.isOnline() || !capturingPlayer.isPlaying()) {
                    cancelCapture(true); cancel(); return;
                }

                if (cachedBannerLoc == null || !isNearBanner(p.getLocation(), cachedBannerLoc, 2)) {
                    cancelCapture(true); cancel(); return;
                }

                captureTimeLeft--;
                if (captureTimeLeft <= 0) {

                    StringJoiner joiner = new StringJoiner(", ");
                    capturingPlayer.getTeam().ifPresent(team ->
                            team.getPlayers().forEach(tp -> joiner.add(tp.getPlayer().getDisplayName()))
                    );
                    String teamName = capturingPlayer.getTeam().map(UHCTeam::name).orElse("???");
                    Bukkit.broadcastMessage(LangManager.get().get(TaupeDefenderLang.BANNER_CAPTURED_BROADCAST,
                            Map.of("%team%", teamName, "%players%", joiner.toString())));
                    bannerCaptured = true;
                    capturingPlayer = null;
                    captureTask = null;
                    UHCManager.get().checkVictory();
                    cancel();
                }
            }
        }.runTaskTimer(net.novaproject.novauhc.Main.get(), 20L, 20L);
    }

    private void cancelCapture(boolean broadcast) {
        capturingPlayer = null;
        captureTimeLeft = 0;
        if (captureTask != null) { captureTask.cancel(); captureTask = null; }
        if (broadcast) {
            Bukkit.broadcastMessage(LangManager.get().get(TaupeDefenderLang.CAPTURE_CANCELLED_BROADCAST));
        }
    }

    private boolean isNearBanner(Location loc, Location bannerLoc, int radius) {
        if (loc == null || bannerLoc == null) return false;
        return Math.abs(loc.getBlockX() - bannerLoc.getBlockX()) <= radius
                && Math.abs(loc.getBlockZ() - bannerLoc.getBlockZ()) <= radius
                && Math.abs(loc.getBlockY() - bannerLoc.getBlockY()) <= 2;
    }

    private int getCaptureTime() {
        int defenders = defTeam == null ? 0 :
                (int) defTeam.getPlayers().stream().filter(UHCPlayer::isPlaying).count();
        if (defenders >= 7) return 15 * 60;
        if (defenders == 6) return 10 * 60;
        if (defenders == 5) return  8 * 60;
        if (defenders == 4) return  5 * 60;
        if (defenders == 3) return  3 * 60;
        if (defenders == 2) return  2 * 60;
        if (defenders == 1) return  1 * 60;
        return 30;
    }

    private void checkGroupLimit() {
        if (defTeam == null) return;
        int maxGroup = largestAttackerTeamSize() + 2;

        List<Player> defenders = defTeam.getPlayers().stream()
                .filter(UHCPlayer::isPlaying)
                .map(UHCPlayer::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        final double castleRadiusSq = 30 * 30;
        final double nearCastleRadiusSq = 10 * 10;
        final double farRadiusSq = 30 * 30;

        Set<Player> checked = new HashSet<>();
        for (Player d : defenders) {
            if (checked.contains(d)) continue;
            List<Player> group = new ArrayList<>();
            group.add(d);
            boolean nearCastle = cachedBannerLoc != null
                    && d.getLocation().distanceSquared(cachedBannerLoc) <= castleRadiusSq;
            double radiusSq = nearCastle ? nearCastleRadiusSq : farRadiusSq;

            for (Player other : defenders) {
                if (other.equals(d) || checked.contains(other)) continue;
                if (d.getLocation().distanceSquared(other.getLocation()) <= radiusSq) {
                    group.add(other);
                }
            }
            checked.addAll(group);

            if (group.size() > maxGroup) {
                for (Player gp : group) {
                    gp.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20 * 20, 0, false, false));
                    LangManager.get().send(TaupeDefenderLang.GROUP_LIMIT_EXCEEDED, gp);
                }
            }
        }
    }

    private int largestAttackerTeamSize() {
        int max = 0;
        for (UHCTeam team : UHCTeamManager.get().getTeams()) {
            if (defTeam != null && team.equals(defTeam)) continue;
            long alive = team.getPlayers().stream().filter(UHCPlayer::isPlaying).count();
            if (alive > max) max = (int) alive;
        }
        return max;
    }

    private boolean isDefTeamAlive() {
        return defTeam != null && defTeam.isAlive();
    }

    @Override
    public void onPlace(Player player, Block block, BlockPlaceEvent event) {
        if (cachedBannerLoc == null || world == null) return;
        if (block.getLocation().distanceSquared(cachedBannerLoc) <= bannerPlaceRadius * bannerPlaceRadius) {
            event.setCancelled(true);
            LangManager.get().send(TaupeDefenderLang.BANNER_PLACE_FORBIDDEN, player,
                    Map.of("%radius%", bannerPlaceRadius));
        }
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
    }

    @Override
    public boolean isWin() {
        return bannerCaptured;
    }

    public void saveOldTeam(UHCPlayer player, UHCTeam team) {
        oldTeam.put(player, team);
    }
}

