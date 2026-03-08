package net.novaproject.ultimate.slavemarket;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.special.SlaveMarketLang;
import net.novaproject.novauhc.listener.player.PlayerConnectionEvent;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.VariableType;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SlaveMarket extends Scenario {

    private static SlaveMarket slaveMarket;

    @ScenarioVariable(lang = SlaveMarketLang.class, nameKey = "VAR_NB_DIAMOND_NAME", descKey = "VAR_NB_DIAMOND_DESC", type = VariableType.INTEGER)
    private int nbDiamond = 48;

    @ScenarioVariable(lang = SlaveMarketLang.class, nameKey = "VAR_AUCTION_TIMER_NAME", descKey = "VAR_AUCTION_TIMER_DESC", type = VariableType.INTEGER)
    private int auctionTimerDuration = 10;

    @ScenarioVariable(lang = SlaveMarketLang.class, nameKey = "VAR_REBUY_TIMER_NAME", descKey = "VAR_REBUY_TIMER_DESC", type = VariableType.INTEGER)
    private int rebuyTimerDuration = 5;

    @ScenarioVariable(lang = SlaveMarketLang.class, nameKey = "VAR_BID_SMALL_NAME", descKey = "VAR_BID_SMALL_DESC", type = VariableType.INTEGER)
    private int bidSmallAmount = 1;

    @ScenarioVariable(lang = SlaveMarketLang.class, nameKey = "VAR_BID_LARGE_NAME", descKey = "VAR_BID_LARGE_DESC", type = VariableType.INTEGER)
    private int bidLargeAmount = 5;

    private final Random random = new Random();
    private final HashMap<UHCPlayer, Integer> diams = new HashMap<>();
    private final Map<UHCTeam, UHCPlayer> owners = new LinkedHashMap<>();
    private final Map<UHCTeam, Integer> teamDiamonds = new LinkedHashMap<>();
    private boolean isFinish = false;
    private Location center;
    private List<TeamPlace> team_place = new ArrayList<>();
    private UHCPlayer lastBuyer;
    private UHCPlayer choosen;
    private boolean canBuy = true;
    private int bid = 0;
    private boolean rebuy;
    private boolean beingBuy = false;
    private BukkitRunnable auctionTask;

    public static SlaveMarket get() {
        return slaveMarket;
    }

    @Override
    public String getName() {
        return LangManager.get().get(SlaveMarketLang.SLAVE_MARKET_NAME);
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(SlaveMarketLang.SLAVE_MARKET_DESC, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.DIAMOND);
    }

    @Override
    public String getPath() {
        return "special/slave";
    }

    @Override
    public void setup() {
        super.setup();
        slaveMarket = this;
    }

    private List<TeamPlace> loadTeamPlacesFromConfig() {
        List<TeamPlace> teamPlaces = new ArrayList<>();
        FileConfiguration config = getConfig();

        if (!config.contains("team_place")) {
            Bukkit.getLogger().warning("Aucune configuration 'team_place' trouvée pour SlaveMarket!");
            return teamPlaces;
        }

        ConfigurationSection teamPlaceSection = config.getConfigurationSection("team_place");
        if (teamPlaceSection == null) {
            Bukkit.getLogger().warning("Section 'team_place' invalide dans la configuration SlaveMarket!");
            return teamPlaces;
        }

        for (String teamIndex : teamPlaceSection.getKeys(false)) {
            String basePath = "team_place." + teamIndex;
            Location captainLocation = ConfigUtils.getLocation(config, basePath + ".captain");
            Location slaveLocation   = ConfigUtils.getLocation(config, basePath + ".slave");

            if (captainLocation != null && slaveLocation != null) {
                teamPlaces.add(new TeamPlace(captainLocation, slaveLocation));
            } else {
                Bukkit.getLogger().warning("Équipe " + teamIndex + " ignorée - positions captain ou slave manquantes");
            }
        }

        return teamPlaces;
    }

    private Location getSlaveLocationForOwner(UHCPlayer owner) {
        if (owner == null || !owner.getTeam().isPresent()) return null;

        List<UHCTeam> teams = UHCTeamManager.get().getTeams();
        int teamIndex = teams.indexOf(owner.getTeam().get());

        if (teamIndex >= 0 && teamIndex < team_place.size()) {
            TeamPlace teamPlace = team_place.get(teamIndex);
            if (teamPlace.isValid()) return teamPlace.slaveLocation();
        }

        return null;
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
    }

    @Override
    public void toggleActive() {
        super.toggleActive();

        this.team_place = loadTeamPlacesFromConfig();
        this.center = ConfigUtils.getLocation(getConfig(), "enchere_place");
        Location wait = ConfigUtils.getLocation(getConfig(), "wait_place");

        if (isActive()) {
            if (wait != null) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.teleport(wait);
                }
            }

            int totalSlots = team_place.isEmpty() ? 8 : team_place.size();
            if (team_place.isEmpty()) {
                Bukkit.getLogger().warning("Aucune équipe configurée pour SlaveMarket! Utilisation de 8 équipes par défaut.");
            }
            UHCTeamManager.get().deleteTeams();
            List<UHCTeam> existingTeams = UHCTeamManager.get().getTeams().stream().filter(UHCTeam::isCustom).toList();
            int toCreate = Math.max(0, totalSlots - existingTeams.size());
            for (int i = 0; i < toCreate; i++) {
                UHCTeamManager.get().createTeam(UHCManager.get().getSlot());
            }
        } else {
            if (auctionTask != null) {
                auctionTask.cancel();
                auctionTask = null;
            }
            owners.clear();
            teamDiamonds.clear();
            diams.clear();
            beingBuy = false;
            lastBuyer = null;
            bid = 0;
        }
    }

    @Override
    public void onTeamUpdate() {
        UHCManager.get().setTeam_size(1);
    }

    public boolean assignOwner(UHCTeam team, UHCPlayer player) {
        if (owners.containsValue(player)) return false;
        owners.put(team, player);
        Bukkit.broadcastMessage(LangManager.get().get(SlaveMarketLang.OWNER_ADDED,
                Map.of("%player%", player.getPlayer().getName())));
        return true;
    }

    public boolean removeOwner(UHCTeam team) {
        UHCPlayer player = owners.remove(team);
        if (player == null) return false;
        Bukkit.broadcastMessage(LangManager.get().get(SlaveMarketLang.OWNER_REMOVED,
                Map.of("%player%", player.getPlayer().getName())));
        return true;
    }

    public Map<UHCTeam, UHCPlayer> getOwners() {
        return owners;
    }

    public boolean isOwner(UHCPlayer player) {
        return owners.containsValue(player);
    }

    public int getDiamondsForTeam(UHCTeam team) {
        return teamDiamonds.getOrDefault(team, nbDiamond);
    }

    public void setDiamondsForTeam(UHCTeam team, int amount) {
        teamDiamonds.put(team, amount);
    }

    @Override
    public void onStart(Player player) {
        UHCPlayer p = UHCPlayerManager.get().getPlayer(player);
        if (isOwner(p)) {
            int remaining = diams.getOrDefault(p, 0);
            if (remaining > 0) {
                player.getInventory().addItem(new ItemStack(Material.DIAMOND, remaining));
            }
        }
    }

    public void startEnchere() {
        List<Location> captainLocations = new ArrayList<>();
        for (TeamPlace teamPlace : team_place) {
            if (teamPlace.isValid()) captainLocations.add(teamPlace.captainLocation());
        }

        if (captainLocations.isEmpty()) {
            Bukkit.getLogger().warning("Aucune position de capitaine configurée, utilisation des positions par défaut");
            World lob = Common.get().getLobby();
            captainLocations = Arrays.asList(
                    new Location(lob, 2, 136, 89),
                    new Location(lob, 0, 136, 91),
                    new Location(lob, 0, 136, 106),
                    new Location(lob, 2, 136, 108),
                    new Location(lob, 17, 136, 108),
                    new Location(lob, 19, 136, 106),
                    new Location(lob, 19, 136, 91),
                    new Location(lob, 17, 136, 89)
            );
        }

        if (owners.size() < 2) {
            canBuy = false;
            Bukkit.broadcastMessage(LangManager.get().get(SlaveMarketLang.NOT_ENOUGH_OWNERS));
            return;
        }

        if (UHCPlayerManager.get().getOnlineUHCPlayers().size() - owners.size() <= 0) {
            Bukkit.broadcastMessage(LangManager.get().get(SlaveMarketLang.NOT_ENOUGH_PLAYERS));
            return;
        }

        if (!canBuy) return;

        List<UHCPlayer> players = new ArrayList<>();
        for (UHCPlayer p : UHCPlayerManager.get().getOnlineUHCPlayers()) {
            p.setTeam(Optional.empty());
            p.getPlayer().getInventory().clear();
            players.add(p);
        }

        List<UHCTeam> allTeams = UHCTeamManager.get().getTeams();

        for (Map.Entry<UHCTeam, UHCPlayer> entry : owners.entrySet()) {
            UHCTeam team = entry.getKey();
            UHCPlayer p  = entry.getValue();

            int teamIdx = allTeams.indexOf(team);
            if (teamIdx < 0 || teamIdx >= captainLocations.size()) continue;

            int diamonds = getDiamondsForTeam(team);
            diams.put(p, diamonds);

            p.getPlayer().getInventory().setItem(4, new ItemCreator(Material.DIAMOND)
                    .setName(ChatColor.AQUA + "Diamants: " + diamonds)
                    .getItemstack());
            p.getPlayer().getInventory().setItem(0, new ItemCreator(Material.EMERALD)
                    .setName(ChatColor.GREEN + "Enchérir +" + bidSmallAmount)
                    .getItemstack());
            p.getPlayer().getInventory().setItem(1, new ItemCreator(Material.EMERALD_BLOCK)
                    .setName(ChatColor.GREEN + "Enchérir +" + bidLargeAmount)
                    .getItemstack());

            p.setTeam(Optional.of(team));
            p.getPlayer().teleport(captainLocations.get(teamIdx));
            players.remove(p);
        }

        beingBuy = false;
        lastBuyer = null;
        bid = 0;

        auctionTask = (BukkitRunnable) new BukkitRunnable() {
            int timer = auctionTimerDuration;

            @Override
            public void run() {
                if (!beingBuy) {
                    beingBuy = true;
                    bid = 0;
                    lastBuyer = null;

                    choosen = players.get(random.nextInt(players.size()));
                    players.remove(choosen);
                    choosen.getPlayer().teleport(center);

                    Bukkit.broadcastMessage(LangManager.get().get(SlaveMarketLang.AUCTION_START,
                            Map.of("%player%", choosen.getPlayer().getName())));

                    timer = auctionTimerDuration;
                } else {
                    timer--;

                    if (rebuy) {
                        timer = rebuyTimerDuration;
                        rebuy = false;
                    }

                    if (timer <= 5) {
                        String buyerSuffix = lastBuyer != null
                                ? LangManager.get().get(SlaveMarketLang.AUCTION_BUYER_SUFFIX,
                                Map.of("%buyer%", lastBuyer.getPlayer().getName()))
                                : "";
                        Bukkit.broadcastMessage(LangManager.get().get(SlaveMarketLang.AUCTION_TIMER_WARNING, Map.of(
                                "%timer%", String.valueOf(timer),
                                "%player%", choosen.getPlayer().getName(),
                                "%bid%", String.valueOf(bid),
                                "%buyer%", buyerSuffix
                        )));
                    }

                    if (timer == 0) {
                        if (lastBuyer != null && lastBuyer.getTeam().isPresent()) {
                            diams.replace(lastBuyer, diams.get(lastBuyer) - bid);
                            lastBuyer.getPlayer().getInventory().setItem(4, new ItemCreator(Material.DIAMOND)
                                    .setAmount(diams.get(lastBuyer))
                                    .setName(ChatColor.AQUA + "Diamants: " + diams.get(lastBuyer))
                                    .getItemstack());
                            choosen.forceSetTeam(Optional.of(lastBuyer.getTeam().get()));
                            Bukkit.broadcastMessage(LangManager.get().get(SlaveMarketLang.AUCTION_SOLD, Map.of(
                                    "%player%", choosen.getPlayer().getName(),
                                    "%buyer%", lastBuyer.getPlayer().getName(),
                                    "%bid%", String.valueOf(bid)
                            )));
                        } else {
                            List<UHCPlayer> ownerList = new ArrayList<>(owners.values());
                            UHCPlayer randomOwner = ownerList.get(random.nextInt(ownerList.size()));
                            choosen.forceSetTeam(Optional.of(randomOwner.getTeam().get()));
                            Bukkit.broadcastMessage(LangManager.get().get(SlaveMarketLang.AUCTION_NOT_SOLD, Map.of(
                                    "%player%", choosen.getPlayer().getName(),
                                    "%owner%", randomOwner.getPlayer().getName()
                            )));
                            lastBuyer = randomOwner;
                        }

                        Location slaveLocation = getSlaveLocationForOwner(lastBuyer);
                        if (slaveLocation != null) {
                            choosen.getPlayer().teleport(slaveLocation);
                        } else {
                            Location base = lastBuyer.getPlayer().getLocation();
                            choosen.getPlayer().teleport(new Location(base.getWorld(), base.getX(), base.getY() - 5, base.getZ()));
                        }

                        beingBuy = false;

                        if (players.isEmpty()) {
                            Bukkit.broadcastMessage(LangManager.get().get(SlaveMarketLang.AUCTION_FINISHED));
                            cancel();
                            for (UHCPlayer p : UHCPlayerManager.get().getOnlineUHCPlayers()) {
                                p.getPlayer().getInventory().clear();
                                p.getPlayer().teleport(Common.get().getLobbySpawn());
                                if (PlayerConnectionEvent.getHost() == p.getPlayer()) {
                                    UHCUtils.giveLobbyItems(p.getPlayer());
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(Main.get(), 0L, 20L);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public void placeBid(UHCPlayer bidder, int amount) {
        if (!beingBuy || !canBuy || !isOwner(bidder)) return;

        int currentDiamonds = diams.getOrDefault(bidder, 0);
        int newBid = bid + amount;

        if (currentDiamonds < newBid) {
            LangManager.get().send(SlaveMarketLang.BID_NOT_ENOUGH_DIAMONDS, bidder.getPlayer());
            return;
        }

        if (bidder == lastBuyer) {
            LangManager.get().send(SlaveMarketLang.BID_ALREADY_HIGHEST, bidder.getPlayer());
            return;
        }

        bid = newBid;
        lastBuyer = bidder;
        rebuy = true;

        Bukkit.broadcastMessage(LangManager.get().get(SlaveMarketLang.BID_PLACED, Map.of(
                "%bidder%", bidder.getPlayer().getName(),
                "%bid%", String.valueOf(bid),
                "%player%", choosen.getPlayer().getName()
        )));
    }

    @Override
    public void onPlayerInteract(Player player, PlayerInteractEvent event) {
        if (!canBuy || !beingBuy) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (!isOwner(uhcPlayer)) return;

        if (item.getType() == Material.EMERALD) {
            event.setCancelled(true);
            placeBid(uhcPlayer, bidSmallAmount);
        } else if (item.getType() == Material.EMERALD_BLOCK) {
            event.setCancelled(true);
            placeBid(uhcPlayer, bidLargeAmount);
        } else if (item.getType() == Material.DIAMOND) {
            event.setCancelled(true);
        }
    }

    public boolean isFinish() { return isFinish; }
    public int getNbDiamond() { return nbDiamond; }
    public void setNbDiamond(int nbDiamond) { this.nbDiamond = nbDiamond; }
    public boolean canBuy() { return canBuy; }
    public List<TeamPlace> getTeamPlaces() { return new ArrayList<>(team_place); }

    public void cancelAction() {
        if (auctionTask != null) {
            auctionTask.cancel();
            auctionTask = null;
        }
    }

    @Override
    public CustomInventory getMenu(Player player) {
        return new SlaveMarketUi(player);
    }

    @Override
    public boolean canOpenInGameTeamUi() {
        return false;
    }
}