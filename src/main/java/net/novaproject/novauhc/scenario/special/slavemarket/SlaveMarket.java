package net.novaproject.novauhc.scenario.special.slavemarket;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.listener.player.PlayerConnectionEvent;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.scenario.ScenarioLangManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
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
    private Location center;
    private final Random random = new Random();
    private final HashMap<UHCPlayer, Integer> diams = new HashMap<>();
    private List<UHCPlayer> owners = new ArrayList<>();
    private List<TeamPlace> team_place = new ArrayList<>();
    private UHCPlayer lastBuyer;
    private UHCPlayer choosen;
    private boolean canBuy = true;
    private int nbDiamond = 48;
    private int bid = 0;
    private boolean rebuy;
    private boolean beingBuy = false;
    private BukkitRunnable auctionTask;
    private final boolean isFinish = false;

    public static SlaveMarket get() {
        return slaveMarket;
    }

    @Override
    public String getName() {
        return "Slave Market";
    }

    @Override
    public String getDescription() {
        return "Système d'enchères où les joueurs peuvent être achetés par d'autres équipes.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.DIAMOND);
    }

    @Override
    public void setup() {
        super.setup();
        owners = new ArrayList<>();
        slaveMarket = this;

    }

    @Override
    public String getPath() {
        return "special/slave";
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
            Location slaveLocation = ConfigUtils.getLocation(config, basePath + ".slave");

            if (captainLocation != null && slaveLocation != null) {
                TeamPlace teamPlace = new TeamPlace(captainLocation, slaveLocation);
                teamPlaces.add(teamPlace);
                Bukkit.getLogger().info("Équipe " + teamIndex + " chargée avec succès pour SlaveMarket");
            } else {
                Bukkit.getLogger().warning("Équipe " + teamIndex + " ignorée - positions captain ou slave manquantes");
            }
        }

        Bukkit.getLogger().info("SlaveMarket: " + teamPlaces.size() + " équipes chargées depuis la configuration");
        return teamPlaces;
    }


    private Location getSlaveLocationForOwner(UHCPlayer owner) {
        if (owner == null || !owner.getTeam().isPresent()) {
            return null;
        }

        List<UHCTeam> teams = UHCTeamManager.get().getTeams();
        int teamIndex = teams.indexOf(owner.getTeam().get());

        if (teamIndex >= 0 && teamIndex < team_place.size()) {
            TeamPlace teamPlace = team_place.get(teamIndex);
            if (teamPlace.isValid()) {
                return teamPlace.getSlaveLocation();
            }
        }

        return null;
    }

    @Override
    public ScenarioLang[] getLang() {
        return SlaveMarketLang.values();
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
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.teleport(wait);
            }

            int teamCount = team_place.size();
            if (teamCount == 0) {
                Bukkit.getLogger().warning("Aucune équipe configurée pour SlaveMarket! Utilisation de 8 équipes par défaut.");
                teamCount = 8;
            }
            for (int i = 0; i < teamCount; i++) {
                UHCTeamManager.get().createTeam(UHCManager.get().getSlot());
            }
            Bukkit.getLogger().info("SlaveMarket: " + teamCount + " équipes créées");
        } else {
            if (auctionTask != null) {
                auctionTask.cancel();
                auctionTask = null;
            }
        }
    }

    @Override
    public void onTeamUpdate() {
        UHCManager.get().setTeam_size(1);
    }

    public boolean addOwner(UHCPlayer player) {
        if (!owners.contains(player)) {
            owners.add(player);
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("%player%", player.getPlayer().getName());
            Bukkit.broadcastMessage(ScenarioLangManager.get(SlaveMarketLang.OWNER_ADDED, player, placeholders));
            return true;
        }
        return false;
    }

    public boolean removeOwner(UHCPlayer player) {
        if (owners.contains(player)) {
            owners.remove(player);
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("%player%", player.getPlayer().getName());
            Bukkit.broadcastMessage(ScenarioLangManager.get(SlaveMarketLang.OWNER_REMOVED, player, placeholders));
            return true;
        }
        return false;
    }

    public List<UHCPlayer> getOwners() {
        return owners;
    }

    @Override
    public void onStart(Player player) {
        UHCPlayer p = UHCPlayerManager.get().getPlayer(player);
        if (owners.contains(p)) {
            int diamsrestant = diams.get(p);
            if (diamsrestant != 0) {
                player.getInventory().addItem(new ItemStack(Material.DIAMOND, diamsrestant));
            }
        }
    }

    public void startEnchere() {
        List<Location> captainLocations = new ArrayList<>();
        for (TeamPlace teamPlace : team_place) {
            if (teamPlace.isValid()) {
                captainLocations.add(teamPlace.getCaptainLocation());
            }
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
                    new Location(lob, 17, 136, 89));
        }

        List<UHCPlayer> players = new ArrayList<>();

        if (owners.size() < 2) {
            canBuy = false;
            Bukkit.broadcastMessage(ChatColor.RED + "Il n'y a pas assez de propriétaires pour commencer l'enchère!");
            return;
        }

        if (!canBuy) {
            return;
        }

        for (UHCPlayer p : UHCPlayerManager.get().getOnlineUHCPlayers()) {
            p.setTeam(Optional.empty());
            p.getPlayer().getInventory().clear();
            players.add(p);
        }

        int i = 0;
        for (UHCPlayer p : owners) {
            if (i >= captainLocations.size() || i >= UHCTeamManager.get().getTeams().size()) break;

            diams.put(p, nbDiamond);
            p.getPlayer().getInventory().setItem(4, new ItemCreator(Material.DIAMOND)
                    .setAmount(nbDiamond)
                    .setName(ChatColor.AQUA + "Diamants: " + nbDiamond)
                    .getItemstack());

            p.getPlayer().getInventory().setItem(0, new ItemCreator(Material.EMERALD)
                    .setName(ChatColor.GREEN + "Enchérir +1")
                    .getItemstack());

            p.getPlayer().getInventory().setItem(1, new ItemCreator(Material.EMERALD_BLOCK)
                    .setName(ChatColor.GREEN + "Enchérir +5")
                    .getItemstack());

            p.setTeam(Optional.ofNullable(UHCTeamManager.get().getTeams().get(i)));
            p.getPlayer().teleport(captainLocations.get(i));

            players.remove(p);
            i++;
        }

        beingBuy = false;
        lastBuyer = null;
        bid = 0;

        auctionTask = (BukkitRunnable) new BukkitRunnable() {
            int timer = 10;

            @Override
            public void run() {


                if (!beingBuy) {
                    beingBuy = true;
                    bid = 0;
                    lastBuyer = null;

                    choosen = players.get(random.nextInt(players.size()));
                    players.remove(choosen);
                    choosen.getPlayer().teleport(center);

                    Bukkit.broadcastMessage(ChatColor.YELLOW + choosen.getPlayer().getName() +
                            ChatColor.GOLD + " a été mis en vente ! Enchère de départ: " +
                            ChatColor.AQUA + "0 diamants");

                    timer = 10;
                } else {
                    timer--;

                    if (rebuy) {
                        timer = 5;
                        rebuy = false;
                    }

                    if (timer <= 5) {
                        Bukkit.broadcastMessage(ChatColor.YELLOW + "Il reste " + timer +
                                " secondes avant la fin de l'enchère pour " +
                                ChatColor.GREEN + choosen.getPlayer().getName() +
                                ChatColor.YELLOW + "! Offre actuelle: " +
                                ChatColor.AQUA + bid + " diamants" +
                                (lastBuyer != null ? ChatColor.YELLOW + " par " +
                                        ChatColor.GOLD + lastBuyer.getPlayer().getName() : ""));
                    }

                    if (timer == 0) {
                        if (lastBuyer != null && lastBuyer.getTeam().isPresent()) {
                            diams.replace(lastBuyer, diams.get(lastBuyer) - bid);
                            lastBuyer.getPlayer().getInventory().setItem(4, new ItemCreator(Material.DIAMOND)
                                    .setAmount(diams.get(lastBuyer))
                                    .setName(ChatColor.AQUA + "Diamants: " + diams.get(lastBuyer))
                                    .getItemstack());
                            UHCTeam team = lastBuyer.getTeam().get();
                            System.out.println(team);
                            choosen.forecSetTeam(Optional.of(team));
                            Bukkit.broadcastMessage(ChatColor.GREEN + choosen.getPlayer().getName() +
                                    ChatColor.YELLOW + " a été acheté par " +
                                    ChatColor.GOLD + lastBuyer.getPlayer().getName() +
                                    ChatColor.YELLOW + " pour " +
                                    ChatColor.AQUA + bid + " diamants");
                        } else {
                            UHCPlayer randomOwner = owners.get(random.nextInt(owners.size()));
                            choosen.forecSetTeam(Optional.of(owners.get(random.nextInt(owners.size())).getTeam().get()));
                            Bukkit.broadcastMessage(ChatColor.GREEN + choosen.getPlayer().getName() +
                                    ChatColor.YELLOW + " n'a pas été acheté et a été assigné à " +
                                    ChatColor.GOLD + randomOwner.getPlayer().getName());
                            lastBuyer = randomOwner;
                        }

                        Location slaveLocation = getSlaveLocationForOwner(lastBuyer);
                        if (slaveLocation != null) {
                            choosen.getPlayer().teleport(slaveLocation);
                        } else {
                            Location teamateLoc = lastBuyer.getPlayer().getLocation();
                            Location loc = new Location(teamateLoc.getWorld(), teamateLoc.getX(), teamateLoc.getY() - 5, teamateLoc.getZ());
                            choosen.getPlayer().teleport(loc);
                        }

                        beingBuy = false;
                        if (players.isEmpty()) {
                            Bukkit.broadcastMessage(Common.get().getServertag() + ChatColor.GOLD + "Enchére Terminée ! Attente d l'host pour le lancement de la patrie...");
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
        if (!beingBuy || !canBuy || !owners.contains(bidder)) {
            return;
        }

        int currentDiamonds = diams.getOrDefault(bidder, 0);
        int newBid = bid + amount;

        if (currentDiamonds < newBid) {
            bidder.getPlayer().sendMessage(ChatColor.RED + "Vous n'avez pas assez de diamants!");
            return;
        }

        if (bidder == lastBuyer) {
            bidder.getPlayer().sendMessage(ChatColor.RED + "Vous êtes déjà le plus offrant!");
            return;
        }

        bid = newBid;
        lastBuyer = bidder;
        rebuy = true;

        Bukkit.broadcastMessage(ChatColor.GOLD + bidder.getPlayer().getName() +
                ChatColor.YELLOW + " a enchéri " +
                ChatColor.AQUA + bid + " diamants" +
                ChatColor.YELLOW + " pour " +
                ChatColor.GREEN + choosen.getPlayer().getName());
    }

    @Override
    public void onPlayerInteract(Player player, PlayerInteractEvent event) {
        if (!canBuy || !beingBuy) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (!owners.contains(uhcPlayer)) return;

        if (item.getType() == Material.EMERALD) {
            event.setCancelled(true);
            placeBid(uhcPlayer, 1);
        } else if (item.getType() == Material.EMERALD_BLOCK) {
            event.setCancelled(true);
            placeBid(uhcPlayer, 5);
        } else if (item.getType() == Material.DIAMOND) {
            event.setCancelled(true);
        }
    }

    public boolean isFinish() {
        return isFinish;
    }

    public int getNbDiamond() {
        return nbDiamond;
    }

    public void setNbDiamond(int nbDiamond) {
        this.nbDiamond = nbDiamond;
    }

    public void cancelAction() {
        if (auctionTask != null) {
            auctionTask.cancel();
            auctionTask = null;
        }
    }

    public boolean canBuy() {
        return canBuy;
    }


    public List<TeamPlace> getTeamPlaces() {
        return new ArrayList<>(team_place);
    }

    @Override
    public CustomInventory getMenu(Player player) {
        return new SlaveMarketUi(player);
    }

}
