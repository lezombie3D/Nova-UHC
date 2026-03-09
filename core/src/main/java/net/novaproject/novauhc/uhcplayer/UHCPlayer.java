package net.novaproject.novauhc.uhcplayer;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.arena.ArenaUHC;
import net.novaproject.novauhc.lang.lang.CommonLang;
import net.novaproject.novauhc.listener.player.PlayerConnectionEvent;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.ui.config.Enchants;
import net.novaproject.novauhc.utils.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class UHCPlayer {

    private final EnumMap<Enchants, Integer> enchantLimits;

    private final UUID uuid;
    private boolean playing = false;
    private final String hostname = PlayerConnectionEvent.getHost().getName();
    private Player killer;
    public List<ItemStack> deathItem = new LinkedList<>();
    private Optional<UHCTeam> team = Optional.empty();
    private boolean bypassed = false;
    private final UHCManager uhcManager = UHCManager.get();
    private int dimamondLimit = uhcManager.getDimamondLimit();
    private int diamondArmor = uhcManager.getDiamondArmor();
    private int protectionMax = uhcManager.getProtectionMax();

    private int minedDiamond = 0;
    private int kill = 0;
    private String locale = null;

    public UHCPlayer(Player player) {
        this.uuid = player.getUniqueId();
        this.enchantLimits = new EnumMap<>(Enchants.class);
        for (Enchants ench : Enchants.values()) {
            enchantLimits.put(ench, ench.getConfigValue());
        }
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

    public void setTeam(Optional<UHCTeam> team) {
        Player player = getPlayer();

        if (!team.isPresent()) {
            this.team.ifPresent(uhcTeam -> {
                LangManager.get().send(CommonLang.SUCCESSFUL_MODIFICATION, getPlayer());
                uhcTeam.getTeam().removePlayer(getOfflinePlayer());
            });

            this.team = team;

        } else {
            int team_size = uhcManager.getTeam_size();
            UHCTeam next = team.get();

            if (next.getPlayers().size() == team_size && team_size != 1) {
                LangManager.get().send(CommonLang.DISABLE_ACTION, getPlayer());
            } else {
                this.team.ifPresent(uhcTeam -> {
                    uhcTeam.getTeam().removePlayer(getOfflinePlayer());
                });

                this.team = team;
                LangManager.get().send(CommonLang.SUCCESSFUL_MODIFICATION, getPlayer());
                next.getTeam().addPlayer(getOfflinePlayer());
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
                LangManager.get().send(CommonLang.SUCCESSFUL_MODIFICATION, getPlayer());
                uhcTeam.getTeam().removePlayer(getOfflinePlayer());
            });

            this.team = team;

        } else {
            UHCTeam next = team.get();

            this.team.ifPresent(uhcTeam -> {
                uhcTeam.getTeam().removePlayer(getOfflinePlayer());
            });

            this.team = team;
            next.getTeam().addPlayer(getOfflinePlayer());
        }
    }

    public void setEnchantLimit(Enchants ench, int value) {
        if (value < 0) value = 0;
        if (value > ench.getEnchantment().getMaxLevel()) value = ench.getEnchantment().getMaxLevel();

        enchantLimits.put(ench, value);
    }

    public void connect(Player player) {

        if (uhcManager.isLobby()) {

            playing = true;

            player.setGameMode(GameMode.ADVENTURE);
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
            player.getWorld().setDifficulty(Difficulty.PEACEFUL);
            player.setAllowFlight(false);
            player.setFlying(false);
            player.getInventory().clear();
            player.getActivePotionEffects().clear();
            player.setExp(0);
            player.setLevel(0);
            player.getInventory().setArmorContents(null);
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }

            if (ArenaUHC.get() == null) {
                new ArenaUHC();
            }
            TeamsTagsManager.setNameTag(player, "", "", "");
            PermissionAttachment attachment = player.addAttachment(Main.get());

            Main.getDatabaseManager().connectPlayer(player.getUniqueId(), player.getName());

            if (player.equals(PlayerConnectionEvent.getHost())) {
                if (!player.hasPermission("novauhc.host")) {
                    attachment.setPermission("novauhc.host", true);
                }

                LangManager.get().send(CommonLang.WELCOME_HOST, player);
                System.out.println(LangManager.get().get(CommonLang.WELCOME_HOST, player));
                TeamsTagsManager.setNameTag(player, "host", "§c§lHOST §r§c", "");
            } else {
                attachment.unsetPermission("novauhc.host");
                LangManager.get().send(CommonLang.WELCOME, player);
            }

            UHCUtils.giveLobbyItems(getPlayer());
            for (Player player1 : Bukkit.getOnlinePlayers()) {
                if (player1 != null && player1.isOnline()) {
                    new Titles().sendActionText(player1, ChatColor.GREEN + player.getName() + " (" + Bukkit.getOnlinePlayers().size() + "/" + uhcManager.getSlot() + ")");
                }
            }

        } else {

            if (!playing && !ReconnectionManager.get().hasPendingReconnection(uuid)) {
                player.teleport(new Location(Common.get().getArena(), 0, 100, 0));
                player.setGameMode(GameMode.SPECTATOR);
                for(UHCPlayer p : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                    Player onlinePlayer = p.getPlayer();
                    if (onlinePlayer != null && onlinePlayer.isOnline()) {
                        onlinePlayer.hidePlayer(player);
                    }
                }
                TeamsTagsManager.setNameTag(player, "zzzzz", "§8§lSPEC §r§8", "");
                LangManager.get().send(CommonLang.WELCOME_SPECTATOR, player);

            } else if (ReconnectionManager.get().hasPendingReconnection(uuid)) {
                ReconnectionManager.get().handleReconnection(this);

            } else {
                for (Player player1 : Bukkit.getOnlinePlayers()) {
                    if (player1 != null && player1.isOnline()) {
                        new Titles().sendActionText(player1, LangManager.get().get(CommonLang.CONNECTION_GAME, getPlayer()));
                    }
                }
            }

        }

    }

    public String getArrowDirection(Location from, Location to, float playerYaw) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();

        double angle = Math.toDegrees(Math.atan2(-dx, dz));
        angle = (angle + 360) % 360;

        float normalizedYaw = (playerYaw + 360) % 360;

        double relativeAngle = (angle - normalizedYaw + 360) % 360;

        if (relativeAngle >= 337.5 || relativeAngle < 22.5) return ChatColor.BOLD + "↑";
        if (relativeAngle >= 22.5 && relativeAngle < 67.5) return ChatColor.BOLD + "↗";
        if (relativeAngle >= 67.5 && relativeAngle < 112.5) return ChatColor.BOLD + "→";
        if (relativeAngle >= 112.5 && relativeAngle < 157.5) return ChatColor.BOLD + "↘";
        if (relativeAngle >= 157.5 && relativeAngle < 202.5) return ChatColor.BOLD + "↓";
        if (relativeAngle >= 202.5 && relativeAngle < 247.5) return ChatColor.BOLD + "↙";
        if (relativeAngle >= 247.5 && relativeAngle < 292.5) return ChatColor.BOLD + "←";
        if (relativeAngle >= 292.5 && relativeAngle < 337.5) return ChatColor.BOLD + "↖";

        return "?";
    }

    public String getDistanceToCenter(Location to) {
        double distance = getPlayer().getLocation().distance(to);
        return String.format("%.0f m", distance);
    }

    public void disconnect(Player player) {

        if (uhcManager.isLobby()) {
            for (Player player1 : Bukkit.getOnlinePlayers()) {
                if (player1 != null && player1.isOnline()) {
                    new Titles().sendActionText(player1, LangManager.get().get(CommonLang.DECONNECTION_LOBBY, getPlayer()));
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
        deathItem.clear();
        deathItem.addAll(event.getDrops());
        playing = false;

        ReconnectionManager.get().cancelReconnectionTimer(uuid);

        Player player = getPlayer();
        Location location = player.getLocation();
        uhcManager.checkVictory();

        if (killer != null) {
            killer.setKill(killer.getKill() + 1);

            UHCManager.get().onPlayerKill(killer.getPlayer(), player);

            this.killer = killer.getPlayer();
            ScenarioManager.get().getActiveScenarios()
                    .forEach(scenario -> scenario.onKill(killer, this));
        } else {
            UHCManager.get().getStatsTracker().addDeath(player.getUniqueId());
        }

        TeamsTagsManager.setNameTag(player, "zzzzz", "§8§lSPEC §r§8", "");

        ScenarioManager.get().getActiveScenarios()
                .forEach(scenario -> scenario.onDeath(this, killer, event));

        player.setGameMode(GameMode.SPECTATOR);
        player.spigot().respawn();
        player.teleport(location);
        event.setDeathMessage(null);

        ScenarioManager.get().getActiveScenarios()
                .forEach(scenario -> scenario.onAfterDeath(this, killer, event));

        List<Scenario> deathMsgScenarios = ScenarioManager.get().getActiveScenarios().stream()
                .filter(Scenario::hascustomDeathMessage)
                .collect(Collectors.toList());

        if (!deathMsgScenarios.isEmpty()) {
            deathMsgScenarios.forEach(scenario -> scenario.sendCustomDeathMessage(this, killer, event));
            return;
        }

        for (Player alive : Bukkit.getOnlinePlayers()) {
            if (alive != null && alive.isOnline()) {
                alive.playSound(alive.getLocation(), Sound.WITHER_SPAWN, 1, 1);
            }
        }

        if (getTeam().isPresent()) {
            Bukkit.broadcastMessage(LangManager.get().get(CommonLang.DEATH_MESSAGE_TEAM, getPlayer()));
            uhcManager.checkVictory();
            return;
        }
        Bukkit.broadcastMessage(LangManager.get().get(CommonLang.DEATH_MESSAGE, getPlayer()));

        uhcManager.checkVictory();
    }
}