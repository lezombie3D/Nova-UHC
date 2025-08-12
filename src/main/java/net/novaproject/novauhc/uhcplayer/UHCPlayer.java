package net.novaproject.novauhc.uhcplayer;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.listener.player.PlayerConnectionEvent;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
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
    public List<ItemStack> deathIteam = new LinkedList<>();
    private Optional<UHCTeam> team = Optional.empty();
    private boolean bypassed = false;
    private final UHCManager uhcManager = UHCManager.get();
    private int limite = uhcManager.getDimamondLimit();
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

    public Player getPlayer(){
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
                player.sendMessage(Common.get().getServertag() + "Vous avez quitté l'équipe " + uhcTeam.getName());
                uhcTeam.getTeam().removePlayer(getOfflinePlayer());
            });

            this.team = team;

        } else {

            int team_size = uhcManager.getTeam_size();

            UHCTeam next = team.get();

            if (next.getPlayers().size() == team_size && team_size != 1) {
                player.sendMessage(Common.get().getServertag() + "sorry mais c'est full");
            } else {

                this.team.ifPresent(uhcTeam -> {
                    uhcTeam.getTeam().removePlayer(getOfflinePlayer());
                });

                this.team = team;
                player.sendMessage(Common.get().getServertag() + "vous avez rejoins l'équipe " + next.getName());
                next.getTeam().addPlayer(getOfflinePlayer());

            }

        }

    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    public List<ItemStack> getDeathIteam() {
        return deathIteam;
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

    public void forecSetTeam(Optional<UHCTeam> team) {
        Player player = getPlayer();

        if (!team.isPresent()) {

            this.team.ifPresent(uhcTeam -> {
                player.sendMessage(Common.get().getServertag() + "Vous avez quitté l'équipe " + uhcTeam.getName());
                uhcTeam.getTeam().removePlayer(getOfflinePlayer());
            });

            this.team = team;

        } else {

            UHCTeam next = team.get();

            this.team.ifPresent(uhcTeam -> {
                uhcTeam.getTeam().removePlayer(getOfflinePlayer());
            });

            this.team = team;
            player.sendMessage(Common.get().getServertag() + "vous avez rejoins l'équipe " + next.getName());
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
            player.teleport(Common.get().getLobbySpawn());
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
            PermissionAttachment attachment = player.addAttachment(Main.get());
            Main.getDatabaseManager().connectPlayer(player.getUniqueId());
            if (player == PlayerConnectionEvent.getHost()) {
                if (!player.hasPermission("novauhc.host")) {
                    attachment.setPermission("novauhc.host", true);
                }


                TeamsTagsManager.setNameTag(player, "host", "[§cHost§r] ", "");
            } else {
                attachment.unsetPermission("novauhc.host");
            }

            player.getInventory().setItem(0, Common.get().getTeamItem().getItemstack());
            player.getInventory().setItem(2, Common.get().getActiveRole().getItemstack());
            player.getInventory().setItem(4, Common.get().getConfigItem().getItemstack());
            player.getInventory().setItem(6, Common.get().getActiveScenario().getItemstack());
            player.getInventory().setItem(8, Common.get().getReglesItem().getItemstack());
            updateScoreboard(player);

            for (Player player1 : Bukkit.getOnlinePlayers()) {
                new Titles().sendActionText(player1, ChatColor.GREEN + player.getName() + " (" + Bukkit.getOnlinePlayers().size() + "/" + uhcManager.getSlot() + ")");
            }
            String grade = Main.getDatabaseManager().getGrade(player.getUniqueId());
            switch (grade) {
                case "default":
                    TeamsTagsManager.setNameTag(player, "default", "[§fJoueur§r] ", "");
                    Bukkit.broadcastMessage("§f[§a+§f] " + player.getName() + " §fa rejoint le serveur");
                    break;
                case "vip":
                    TeamsTagsManager.setNameTag(player, "vip", "[§6VIP§r] ", "");
                    Bukkit.broadcastMessage("§f[§a+§f] Le §6Vip §f" + player.getName() + " §fa rejoint le serveur");
                    player.setAllowFlight(true);

                    break;
                case "host":
                    TeamsTagsManager.setNameTag(player, "host", "[§dHost§r] ", "");
                    Bukkit.broadcastMessage("§f[§a+§f] L' §dHost §f" + player.getName() + " §fa rejoint le serveur");

                    break;
                case "build":
                    TeamsTagsManager.setNameTag(player, "build", "[§bBuild§r] ", "");
                    Bukkit.broadcastMessage("§f[§a+§f] Le §aBuilder §f" + player.getName() + " §fa rejoint le serveur !");
                    player.setAllowFlight(true);
                    break;
                case "modo":
                    TeamsTagsManager.setNameTag(player, "modo", "[§5Modo§r] ", "");
                    Bukkit.broadcastMessage("§f[§a+§f] Le §5Modérateur §f" + player.getName() + " §fa rejoint le serveur !");
                    player.setAllowFlight(true);
                    break;
                case "dev":
                    TeamsTagsManager.setNameTag(player, "dev", "[§2Dev§r] ", "");
                    Bukkit.broadcastMessage("§f[§a+§f] Le §2Développer §f" + player.getName() + " §fa rejoint le serveur !");
                    player.setAllowFlight(true);
                    player.setOp(true);
                    break;
                case "admin":
                    TeamsTagsManager.setNameTag(player, "admin", "[§cAdmin§r] ", "");
                    Bukkit.broadcastMessage("§f[§a+§f] L' §cAdministateur §f" + player.getName() + " §fa rejoint le serveur !");
                    player.setAllowFlight(true);
                    player.setOp(true);
                    break;
                default:
                    break;
            }

        } else {

            if (!playing) {
                player.teleport(new Location(Common.get().getArena(), 0, 100, 0));
                player.setGameMode(GameMode.SPECTATOR);
                TeamsTagsManager.setNameTag(player, "zzzzz", "§8§o[Spec] ", "");
                updateScoreboard(player);

            } else {

                for (Player player1 : Bukkit.getOnlinePlayers()) {
                    new Titles().sendActionText(player1, ChatColor.GREEN + "§c>> " + player.getName() + " (" + Bukkit.getOnlinePlayers().size() + "/" + uhcManager.getSlot() + ")");
                }

                updateScoreboard(player);
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

    public void disconnect(Player player) {

        if (uhcManager.isLobby()) {
            for (Player player1 : Bukkit.getOnlinePlayers()) {
                new Titles().sendActionText(player1, ChatColor.RED + "§c<< " + player.getName() + " (" + (Bukkit.getOnlinePlayers().size() - 1) + "/" + uhcManager.getSlot() + ")");
            }
            setTeam(Optional.empty());

        } else {

            if (playing) {
                for (Player player1 : Bukkit.getOnlinePlayers()) {
                    new Titles().sendActionText(player1, ChatColor.RED + "§c<< " + player.getName() + " (" + (Bukkit.getOnlinePlayers().size() - 1) + "/" + uhcManager.getSlot() + ")");
                }

                uhcManager.checkVictory();
            }

        }

    }

    public void onDeath(UHCPlayer killer, PlayerDeathEvent event) {
        deathIteam.clear();
        deathIteam.addAll(event.getDrops());
        playing = false;

        Player player = getPlayer();
        Location location = player.getLocation();

        if (killer != null) {
            killer.setKill(killer.getKill() + 1);
            Main.getDatabaseManager().addKills(killer.getUniqueId(), 1);
        }
        Main.getDatabaseManager().addDeath(player.getUniqueId(), 1);

        TeamsTagsManager.setNameTag(player, "zzzzz", "§8§o[Spec] ", "");

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

        Bukkit.broadcastMessage(Common.get().getServertag() + ChatColor.RED + "Le joueur " + player.getName() + ChatColor.RED + " est mort !");

        uhcManager.checkVictory();
    }

    private void updateScoreboard(Player player) {
        FastBoard scoreboard = new FastBoard(player);
        String string_ip = Common.get().getServerIp();
        if (string_ip.length() + 2 > 30) {
            string_ip = string_ip.substring(0, 30);
        }
        BlinkEffect ip = new BlinkEffect(string_ip);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (getPlayer() == null) cancel();
                ip.next();
                String ips = ip.getText();

                UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
                if (uhcPlayer == null) return;

                String header = "", footer = "";
                FileConfiguration config = ConfigUtils.getGeneralConfig();

                String phase = uhcManager.isLobby() ? "lobby" : (uhcManager.isGame() ? "game" : "end");

                header = config.getString("message.tab." + phase + ".header", "");
                footer = config.getString("message.tab." + phase + ".footer", "");

                header = UHCUtils.translateGamePlaceholders(
                        UHCUtils.applyPlayerPlaceholders(header, uhcPlayer), null);
                footer = UHCUtils.translateGamePlaceholders(
                        UHCUtils.applyPlayerPlaceholders(footer, uhcPlayer), null);

                String title = config.getString("message.scoreboard." + phase + ".title", "§6NovaUHC");
                List<String> lines = config.getStringList("message.scoreboard." + phase + ".lines");

                List<String> processedLines = lines.stream()
                        .map(line -> {
                            line = line.replace("<ip>", ips);
                            return UHCUtils.translateGamePlaceholders(
                                    UHCUtils.applyPlayerPlaceholders(line, uhcPlayer), null);
                        })
                        .collect(Collectors.toList());

                scoreboard.updateTitle(title);
                scoreboard.updateLines(processedLines);
                TabListManager.sendTab(player, header, footer);

                if (!uhcManager.isLobby() && !uhcManager.isGame()) cancel();
            }
        }.runTaskTimerAsynchronously(Main.get(), 0L, 2L);

    }

}
