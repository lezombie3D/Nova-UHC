package net.novaproject.novauhc.player;

import net.novaproject.novauhc.player.utils.InventorySnapshots;
import net.novaproject.novauhc.player.utils.ReconnectionManager;
import net.novaproject.novauhc.scenario.role.Bonds.VictoryLinks;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.lobby.RankManager;
import net.novaproject.novauhc.lobby.RankManager.Rank;
import net.novaproject.novauhc.event.UhcRoleEvents.UhcTeamEliminatedEvent;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerKillEvent;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerDeathEvent;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.display.TabFreezeService;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.game.PendingDeathManager;
import net.novaproject.novauhc.game.PendingDeathManager.DeathAnnouncer;
import net.novaproject.novauhc.lobby.HotbarManager;
import net.novaproject.novauhc.utils.UHCUtils.ServerMonitor;
import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.api.ApiManager;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.listener.PlayerConnectionEvent;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.ui.config.Enchants;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import org.bukkit.ChatColor;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class UHCPlayer implements PlayerSpi.IUHCPlayer {

    private final EnumMap<Enchants, Integer> enchantLimits;

    private final UUID uuid;
    private boolean playing = false;
    private boolean spec = false;
    private final String hostname = PlayerConnectionEvent.getHost().getName();
    private Player killer;
    public List<ItemStack> deathItem = new LinkedList<>();
    private Optional<UHCTeam> team = Optional.empty();
    private boolean bypassed = false;
    private final UHCManager uhcManager = UHCManager.get();
    private int diamondLimit = uhcManager.getDiamondLimit();
    private int diamondArmor = -1;
    private int protectionMax = uhcManager.getProtectionMax();
    private double forcePercent = 0.5;
    private double forceCriticPercent = 1;
    private double resistancePercent = 1;
    private double speedPercent = 0.20;
    private int minedDiamond = 0;
    private int minedStone = 0;
    private int minedIron = 0;
    private int minedGold = 0;
    private int goldenApplesEaten = 0;
    private int kill = 0;
    private volatile String locale = null;

    private ItemStack[] lastInventoryContents;
    private ItemStack[] lastArmorContents;
    private Location lastDeathLocation;
    private int lastXpLevel = 0;
    private double lastMaxHealth = 20;

    private final Map<UUID, ChatColor> tabColorPrefs = new HashMap<>();

    public UHCPlayer(Player player) {
        this.uuid = player.getUniqueId();
        this.enchantLimits = new EnumMap<>(Enchants.class);
        for (Enchants ench : Enchants.values()) {
            enchantLimits.put(ench, ench.getConfigValue());
        }
    }

    public int getDiamondArmor() {
        return diamondArmor < 0 ? uhcManager.getDiamondArmor() : diamondArmor;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public boolean isOnline() {
        return getPlayer() != null;
    }

    public String getLocale() {
        if (locale != null) return locale;
        try {
            return LangManager.get().getServerDefaultLocale();
        } catch (Exception e) {
            return "fr_FR";
        }
    }

    public void setLocale(String locale) {
        this.locale = locale;
        ApiManager api = ApiManager.get();
        if (api != null && locale != null) {
            api.setLocale(uuid.toString(), locale);
        }
    }

    public void applyLoadedLocale(String locale) {
        this.locale = locale;
    }

    public void setTeam(Optional<UHCTeam> team) {
        Player player = getPlayer();

        if (!team.isPresent()) {
            this.team.ifPresent(uhcTeam -> {
                LangManager.get().send(CoreLang.COMMON_SUCCESSFUL_MODIFICATION, getPlayer());
                if (player != null) DisplayService.nametag(player, "", "", "");
            });

            this.team = team;

        } else {
            int team_size = uhcManager.getTeam_size();
            UHCTeam next = team.get();

            if (next.getPlayers().size() == team_size && team_size != 1) {
                LangManager.get().send(CoreLang.COMMON_DISABLE_ACTION, getPlayer());
            } else {
                this.team = team;
                LangManager.get().send(CoreLang.COMMON_SUCCESSFUL_MODIFICATION, getPlayer());
                if (player != null) DisplayService.nametag(player, next.name(), next.prefix(), "");
            }
        }
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    public int getDiamondmined() {
        return minedDiamond;
    }

    public void forceSetTeam(Optional<UHCTeam> team) {
        Player player = getPlayer();

        if (!team.isPresent()) {
            this.team.ifPresent(uhcTeam -> {
                LangManager.get().send(CoreLang.COMMON_SUCCESSFUL_MODIFICATION, getPlayer());
                if (player != null) DisplayService.nametag(player, "", "", "");
            });

            this.team = team;

        } else {
            UHCTeam next = team.get();

            this.team = team;
            if (player != null) DisplayService.nametag(player, next.name(), next.prefix(), "");
        }
    }

    public void setEnchantLimit(Enchants ench, int value) {
        if (value < 0) value = 0;
        if (value > ench.getEnchantment().getMaxLevel()) value = ench.getEnchantment().getMaxLevel();

        enchantLimits.put(ench, value);
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
        if (!playing) return;
        TabFreezeService.release(uuid);
        TabFreezeService.applyTo(getPlayer());
    }

    public void connect(Player player) {

        if (uhcManager.isLobby()) {

            playing = !spec;

            player.setGameMode(spec ? GameMode.SPECTATOR : GameMode.ADVENTURE);
            player.getActivePotionEffects().clear();
            player.setMaxHealth(20);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(20);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        player.teleport(Common.get().getLobbySpawn());
                    }
                }
            }.runTaskLater(Main.get(), 1L);
            Location lobbySpawn = Common.get().getLobbySpawn();
            if (lobbySpawn != null && lobbySpawn.getWorld() != null) {
                lobbySpawn.getWorld().setDifficulty(Difficulty.PEACEFUL);
            }
            player.setAllowFlight(false);
            player.setFlying(false);
            InventorySnapshots.clearPlayerInventory(player);
            player.getActivePotionEffects().clear();
            player.setExp(0);
            player.setLevel(0);
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }

            DisplayService.nametag(player, "", "", "");
            PermissionAttachment attachment = player.addAttachment(Main.get());

            ApiManager.get().connectPlayer(player.getUniqueId(), player.getName());

            if (player.equals(PlayerConnectionEvent.getHost())) {
                if (!player.hasPermission("novauhc.host")) {
                    attachment.setPermission("novauhc.host", true);
                }

                LangManager.get().send(CoreLang.COMMON_WELCOME_HOST, player);
                RankManager.get().applyTag(player, Rank.HOST);
            } else {
                attachment.unsetPermission("novauhc.host");
                LangManager.get().send(CoreLang.COMMON_WELCOME, player);
            }

            HotbarManager.get().giveHotbar(getPlayer());
            for (Player player1 : Bukkit.getOnlinePlayers()) {
                if (player1 != null && player1.isOnline()) {
                    DisplayService.actionBar(player1, ChatColor.GREEN + player.getName() + " (" + Bukkit.getOnlinePlayers().size() + "/" + uhcManager.getSlot() + ")");
                }
            }

        } else {

            if (!playing && !ReconnectionManager.get().hasPendingReconnection(uuid)) {
                player.teleport(new Location(Common.get().getArena(), 0, 100, 0));
                for(UHCPlayer p : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                    Player onlinePlayer = p.getPlayer();
                    if (onlinePlayer != null && onlinePlayer.isOnline()) {
                        onlinePlayer.hidePlayer(player);
                    }
                }
                player.setGameMode(GameMode.SPECTATOR);
                if (!TabFreezeService.isFrozen(uuid)) {
                    RankManager.get().applyTag(player, Rank.SPECTATOR);
                }
                LangManager.get().send(CoreLang.COMMON_WELCOME_SPECTATOR, player);

            } else if (ReconnectionManager.get().hasPendingReconnection(uuid)) {
                ReconnectionManager.get().handleReconnection(this);

            } else {
                for (Player player1 : Bukkit.getOnlinePlayers()) {
                    if (player1 != null && player1.isOnline()) {
                        DisplayService.actionBar(player1, LangManager.get().get(CoreLang.COMMON_CONNECTION_GAME, getPlayer()));
                    }
                }
            }

        }

    }

    public boolean isStillInGame() {
        if (playing) return true;
        ReconnectionManager reconnection = ReconnectionManager.get();
        return reconnection != null && reconnection.hasPendingReconnection(uuid);
    }

    public <T extends Role> Optional<T> getRole(
            Class<? extends ScenarioRole<T>> scenarioClass) {
        ScenarioRole<T> scenario =
                ScenarioManager.get().getScenario(scenarioClass);
        if (scenario == null) return Optional.empty();
        return Optional.ofNullable(scenario.getRoleByUHCPlayer(this));
    }

    public String getDistanceToCenter(Location to) {
        double distance = getPlayer().getLocation().distance(to);
        return String.format("%.0f m", distance);
    }

    public void disconnect(Player player) {

        if (uhcManager.isLobby()) {
            for (Player player1 : Bukkit.getOnlinePlayers()) {
                if (player1 != null && player1.isOnline()) {
                    DisplayService.actionBar(player1, LangManager.get().get(CoreLang.COMMON_DECONNECTION_LOBBY, getPlayer()));
                }
            }
            setTeam(Optional.empty());

        } else {

            if (playing) {
                ReconnectionManager.get().startReconnectionTimer(this, player);
            }

        }

    }

    public void onDeath(UHCPlayer killer, PlayerDeathEvent event) {
        if (PendingDeathManager.get().tryAutoRevive(this, event)) return;

        deathItem.clear();
        Player snapshotPlayer = getPlayer();
        if (snapshotPlayer != null) {
            lastInventoryContents = snapshotPlayer.getInventory().getContents().clone();
            lastArmorContents = snapshotPlayer.getInventory().getArmorContents().clone();
            lastDeathLocation = snapshotPlayer.getLocation().clone();
            lastXpLevel = snapshotPlayer.getLevel();
            lastMaxHealth = snapshotPlayer.getMaxHealth();
            for (ItemStack item : snapshotPlayer.getInventory().getContents()) {
                if (item != null && item.getType() != Material.AIR) deathItem.add(item.clone());
            }
            for (ItemStack item : snapshotPlayer.getInventory().getArmorContents()) {
                if (item != null && item.getType() != Material.AIR) deathItem.add(item.clone());
            }
        }
        playing = false;

        getTeam().filter(t -> !t.isAlive()).ifPresent(t ->
                Bukkit.getPluginManager().callEvent(
                        new UhcTeamEliminatedEvent(t)));

        ReconnectionManager.get().cancelReconnectionTimer(uuid);

        Player player = getPlayer();
        Location location = player.getLocation();

        if (killer != null) {
            killer.setKill(killer.getKill() + 1);

            UHCManager.get().onPlayerKill(killer.getPlayer(), player);

            this.killer = killer.getPlayer();
            Bukkit.getPluginManager().callEvent(new UhcPlayerKillEvent(killer, this));
            ScenarioManager.get().getActiveScenarios()
                    .forEach(scenario -> scenario.onKill(killer, this));
        } else {
            UHCManager.get().getStatsTracker().addDeath(player.getUniqueId());
        }

        if (ServerMonitor.isTabFige() && UHCManager.get().isGame()) TabFreezeService.freeze(this);
        if (!TabFreezeService.isFrozen(uuid)) {
            DisplayService.nametag(player, "zzzzz", "§8§lSPEC §r§8", "");
        }

        Bukkit.getPluginManager().callEvent(new UhcPlayerDeathEvent(this, killer, event));
        ScenarioManager.get().getActiveScenarios()
                .forEach(scenario -> scenario.onDeath(this, killer, event));

        player.setGameMode(GameMode.SPECTATOR);
        player.spigot().respawn();
        player.teleport(location);
        event.setDeathMessage(null);

        if (killer != null && UHCManager.get().isSpectatorAutoTpKiller()) {
            final UUID victimUuid = uuid;
            final Player killerPlayer = killer.getPlayer();
            if (killerPlayer != null) {
                final UUID killerUuid = killerPlayer.getUniqueId();
                Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
                    Player victim = Bukkit.getPlayer(victimUuid);
                    Player target = Bukkit.getPlayer(killerUuid);
                    if (victim != null && victim.isOnline() && victim.getGameMode() == GameMode.SPECTATOR
                            && target != null && target.isOnline()) {
                        victim.teleport(target);
                    }
                }, 30L);
            }
        }

        ScenarioManager.get().getActiveScenarios()
                .forEach(scenario -> scenario.onAfterDeath(this, killer, event));

        if (!PendingDeathManager.get().isPending(uuid)) VictoryLinks.onDeath(uuid);

        List<Scenario> deathMsgScenarios = ScenarioManager.get().getActiveScenarios().stream()
                .filter(Scenario::hascustomDeathMessage)
                .collect(Collectors.toList());

        if (!deathMsgScenarios.isEmpty()) {
            deathMsgScenarios.forEach(scenario -> scenario.sendCustomDeathMessage(this, killer, event));
            return;
        }

        DeathAnnouncer.announceVanilla(this);
        uhcManager.checkVictory();
    }
}