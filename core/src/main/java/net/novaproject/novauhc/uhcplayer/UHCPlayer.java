package net.novaproject.novauhc.uhcplayer;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.arena.ArenaUHC;
import net.novaproject.novauhc.listener.player.PlayerConnectionEvent;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.ui.config.Enchants;
import net.novaproject.novauhc.utils.*;
import net.novaproject.novauhc.utils.fastboard.FastBoard;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
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


    private int minedDiamond = 0;
    private int kill = 0;


    public void setTeam(Optional<UHCTeam> team) {

        Player player = getPlayer();

        if (!team.isPresent()) {

            this.team.ifPresent(uhcTeam -> {
                CommonString.SUCCESSFUL_MODIFICATION.send(getPlayer());
                uhcTeam.getTeam().removePlayer(getOfflinePlayer());
            });

            this.team = team;

        } else {

            int team_size = uhcManager.getTeam_size();

            UHCTeam next = team.get();

            if (next.getPlayers().size() == team_size && team_size != 1) {
                CommonString.DISABLE_ACTION.send(getPlayer());
            } else {

                this.team.ifPresent(uhcTeam -> {
                    uhcTeam.getTeam().removePlayer(getOfflinePlayer());
                });

                this.team = team;
                CommonString.SUCCESSFUL_MODIFICATION.send(getPlayer());
                next.getTeam().addPlayer(getOfflinePlayer());

            }

        }

    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    public List<ItemStack> getDeathItem() {
        return deathItem;
    }

    public int getDiamondmined() {
        return minedDiamond;
    }

    public void setMinedDiamond(int minedDiamond) {
        this.minedDiamond = minedDiamond;
    }

    public int getKill() {
        return kill;
    }

    public void setKill(int kill) {
        this.kill = kill;
    }

    public boolean isBypassed() {
        return bypassed;
    }

    public void setBypassed(boolean bypassed) {
        this.bypassed = bypassed;
    }

    public void forceSetTeam(Optional<UHCTeam> team) {
        Player player = getPlayer();

        if (!team.isPresent()) {

            this.team.ifPresent(uhcTeam -> {
                CommonString.SUCCESSFUL_MODIFICATION.send(getPlayer());
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
                    player.teleport(Common.get().getLobbySpawn());
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
            Main.getDatabaseManager().connectPlayer(player.getUniqueId());
            if (player.equals(PlayerConnectionEvent.getHost())) {
                if (!player.hasPermission("novauhc.host")) {
                    attachment.setPermission("novauhc.host", true);
                }

                CommonString.WELCOME_HOST.send(player);
                System.out.println(CommonString.WELCOME_HOST.getMessage(player));
                TeamsTagsManager.setNameTag(player, "host", "§c§lHOST §r§c", "");
            } else {
                attachment.unsetPermission("novauhc.host");
                CommonString.WELCOME.send(player);
            }

            UHCUtils.giveLobbyItems(getPlayer());
            for (Player player1 : Bukkit.getOnlinePlayers()) {
                new Titles().sendActionText(player1, ChatColor.GREEN + player.getName() + " (" + Bukkit.getOnlinePlayers().size() + "/" + uhcManager.getSlot() + ")");
            }

        } else {

            if (!playing) {
                player.teleport(new Location(Common.get().getArena(), 0, 100, 0));
                player.setGameMode(GameMode.SPECTATOR);
                TeamsTagsManager.setNameTag(player, "zzzzz", "§8§lSPEC §r§8", "");
                CommonString.WELCOME_SPECTATOR.send(player);


            } else {

                for (Player player1 : Bukkit.getOnlinePlayers()) {
                    new Titles().sendActionText(player1, CommonString.CONNECTION_GAME.getMessage(getPlayer()));
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
                new Titles().sendActionText(player1, CommonString.DECONNECTION_LOBBY.getMessage(getPlayer()));
            }
            setTeam(Optional.empty());

        } else {

            if (playing) {
                for (Player player1 : Bukkit.getOnlinePlayers()) {
                    new Titles().sendActionText(player1, CommonString.DECONNECTION_GAME.getMessage(getPlayer()));
                }

                uhcManager.checkVictory();
            }

        }

    }

    public void onDeath(UHCPlayer killer, PlayerDeathEvent event) {
        deathItem.clear();
        deathItem.addAll(event.getDrops());
        playing = false;

        Player player = getPlayer();
        Location location = player.getLocation();
        uhcManager.checkVictory();
        if (killer != null) {
            killer.setKill(killer.getKill() + 1);
            Main.getDatabaseManager().addKills(killer.getUniqueId(), 1);
            this.killer = killer.getPlayer();
            ScenarioManager.get().getActiveScenarios()
                    .forEach(scenario -> scenario.onKill(killer, this));
        }

        Main.getDatabaseManager().addDeath(player.getUniqueId(), 1);

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
            alive.playSound(alive.getLocation(), Sound.WITHER_SPAWN, 1, 1);
        }

        if (getTeam().isPresent()) {
            Bukkit.broadcastMessage(CommonString.DEATH_MESSAGE_TEAM.getMessage(getPlayer()));
            uhcManager.checkVictory();
            return;
        }
        Bukkit.broadcastMessage(CommonString.DEATH_MESSAGE.getMessage(getPlayer()));

        uhcManager.checkVictory();
    }

}

