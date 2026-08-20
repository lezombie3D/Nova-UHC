package net.novaproject.ultimate.soulbrother;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.ultimate.soulbrother.SoulBrotherLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.world.LobbyCreator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.awt.Color;
import java.util.*;

public class SoulBrother extends Scenario {

    private static final Color AME_GLOW = new Color(85, 255, 255);

    private static final DynamicLang NOTIF_REUNION_TITLE =
            DynamicLang.of("mode.soulbrother.notif.reunion_title", "§bRéunion des âmes");
    private static final DynamicLang NOTIF_REUNION_BODY =
            DynamicLang.of("mode.soulbrother.notif.reunion_body", "§7Ton frère d'âme est surligné, ne le perds pas.");
    private static final DynamicLang NOTIF_BROTHER_DEAD_TITLE =
            DynamicLang.of("mode.soulbrother.notif.dead_title", "§4Ton frère d'âme est mort");
    private static final DynamicLang NOTIF_BROTHER_DEAD_BODY =
            DynamicLang.of("mode.soulbrother.notif.dead_body", "§7Votre lien est rompu.");

    private static final String WORLD_ALPHA = "world1";
    private static final String WORLD_BETA = "world2";
    private static final String DISPLAY_ALPHA = "Alpha";
    private static final String DISPLAY_BETA = "Beta";

    @Var(name = "Var Reunion Time", type = VariableType.TIME)
    private int reunionTime = 3600;

    @Var(name = "Var Update Interval", type = VariableType.TIME)
    private int updateInterval = 300;

    @Var(name = "Var Death Backlash", type = VariableType.BOOLEAN)
    private boolean deathBacklash = true;

    @Var(name = "Var Death Backlash Damage", type = VariableType.DOUBLE)
    private double deathBacklashDamage = 6.0;

    @Var(name = "Var Death Backlash Duration", type = VariableType.INTEGER)
    private int deathBacklashDuration = 200;

    @Var(name = "Var Reunion Scatter Radius", type = VariableType.INTEGER)
    private int reunionScatterRadius = 500;

    private final Map<UUID, World> playerWorlds = new HashMap<>();
    private final Map<UUID, UUID> soulBrothers = new HashMap<>();
    private final Map<UHCTeam, TeamSplit> teamSplits = new HashMap<>();
    private final Set<UUID> updatedThisCycle = new HashSet<>();

    private boolean reunionHappened = false;
    private BukkitTask countdownTask;

    private static class TeamSplit {
        final Map<UUID, World> assignments;
        final Map<UUID, UUID> pairs;

        TeamSplit(List<UHCPlayer> members, World worldAlpha, World worldBeta) {
            assignments = new HashMap<>();
            pairs = new HashMap<>();

            int half = members.size() / 2;
            List<UHCPlayer> alphaGroup = members.subList(0, half);
            List<UHCPlayer> betaGroup = members.subList(half, members.size());

            for (UHCPlayer m : alphaGroup) {
                if (m.getPlayer() != null) assignments.put(m.getPlayer().getUniqueId(), worldAlpha);
            }
            for (UHCPlayer m : betaGroup) {
                if (m.getPlayer() != null) assignments.put(m.getPlayer().getUniqueId(), worldBeta);
            }

            int pairCount = Math.min(alphaGroup.size(), betaGroup.size());
            for (int i = 0; i < pairCount; i++) {
                Player a = alphaGroup.get(i).getPlayer();
                Player b = betaGroup.get(i).getPlayer();
                if (a != null && b != null) {
                    pairs.put(a.getUniqueId(), b.getUniqueId());
                    pairs.put(b.getUniqueId(), a.getUniqueId());
                }
            }
        }

        World getWorld(UUID uuid) { return assignments.get(uuid); }
        UUID getBrother(UUID uuid) { return pairs.get(uuid); }
    }

    @Override
    public String getName() {
        return LangManager.get().get(SoulBrotherLang.SOUL_BROTHER_NAME);
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(SoulBrotherLang.SOUL_BROTHER_DESC, player,
                Map.of("%minutes%", String.valueOf(reunionTime / 60)));
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.SOUL_SAND);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public void toggleActive() {
        super.toggleActive();
        if (isActive()) {
            setup();
            if (UHCManager.get().getTeam_size() < 2) {
                UHCManager.get().setTeam_size(2);
            }
            String arenaName = Common.get().getArenaName();
            LobbyCreator.cloneWorld(arenaName, WORLD_ALPHA);
            LobbyCreator.cloneWorld(arenaName, WORLD_BETA);
        } else {
            cleanup();
        }
    }

    @Override
    public void onGameStart() {
        if (!isActive()) return;
        startCountdownTask();
        LangManager.get().sendAll(SoulBrotherLang.SOULS_SEPARATED);
    }

    @Override
    public void onTeamUpdate() {
        if (UHCManager.get().getTeam_size() < 2) {
            UHCManager.get().setTeam_size(2);
            LangManager.get().sendAll(SoulBrotherLang.TEAM_SIZE_MINIMUM);
        }
    }

    @Override
    public void onSec(Player p) {
        if (!isActive() || reunionHappened) return;

        int currentTime = UHCManager.get().getTimer();

        if (currentTime >= reunionTime) {
            startReunion();
            return;
        }

        if (updateInterval > 0 && currentTime % updateInterval == 0) {
            if (updatedThisCycle.add(p.getUniqueId())) {
                sendSoulBrotherUpdate(p);
            }
        } else {
            updatedThisCycle.clear();
        }
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        World world1 = Bukkit.getWorld(WORLD_ALPHA);
        World world2 = Bukkit.getWorld(WORLD_BETA);

        if (world1 == null || world2 == null) {
            LangManager.get().sendAll(SoulBrotherLang.WORLDS_NOT_AVAILABLE);
            return;
        }

        Player player = uhcPlayer.getPlayer();
        if (player == null) return;

        UHCTeam team = uhcPlayer.getTeam().orElse(null);
        if (team == null || team.getPlayers().size() < 2) {
            playerWorlds.put(player.getUniqueId(), world1);
            teleportToWorld(player, world1, location);
            return;
        }

        TeamSplit split = teamSplits.computeIfAbsent(team,
                t -> new TeamSplit(t.getPlayers(), world1, world2));

        Location teamLocation = teamloc.getOrDefault(team, location);
        UUID uuid = player.getUniqueId();

        World targetWorld = split.getWorld(uuid);
        if (targetWorld == null) targetWorld = world1;

        playerWorlds.put(uuid, targetWorld);
        teleportToWorld(player, targetWorld, teamLocation);

        UUID brotherUuid = split.getBrother(uuid);
        if (brotherUuid != null) {
            soulBrothers.put(uuid, brotherUuid);
            soulBrothers.put(brotherUuid, uuid);

            Player brother = Bukkit.getPlayer(brotherUuid);
            if (brother != null && brother.isOnline()) {
                LangManager.get().send(SoulBrotherLang.SOUL_BROTHER_LINKED, player, Map.of("%brother%", brother.getName()));
                LangManager.get().send(SoulBrotherLang.SOUL_BROTHER_LINKED, brother, Map.of("%brother%", player.getName()));
            }
        } else {
            LangManager.get().send(SoulBrotherLang.NO_SOUL_BROTHER, player);
        }
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (!isActive()) return;

        Player deadPlayer = uhcPlayer.getPlayer();
        if (deadPlayer == null) return;

        UUID deadUuid = deadPlayer.getUniqueId();
        UUID brotherUuid = soulBrothers.get(deadUuid);
        if (brotherUuid == null) return;

        Player brother = Bukkit.getPlayer(brotherUuid);
        String deadName = deadPlayer.getName();
        String brotherName = (brother != null) ? brother.getName() : "?";

        LangManager.get().sendAll(SoulBrotherLang.BROTHER_DIED_BROADCAST, Map.of(
                "%player%", deadName,
                "%brother%", brotherName
        ));

        if (brother != null && brother.isOnline()) {
            DisplayService.resetGlow(brother, deadPlayer);
            DisplayService.notification(brother,
                    t(NOTIF_BROTHER_DEAD_TITLE, brother), t(NOTIF_BROTHER_DEAD_BODY, brother), 6);
        }

        if (brother != null && brother.isOnline() && deathBacklash) {
            LangManager.get().send(SoulBrotherLang.BROTHER_DIED, brother, Map.of("%brother%", deadName));
            brother.damage(deathBacklashDamage);
            brother.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, deathBacklashDuration, 1));
            brother.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, deathBacklashDuration / 2, 0));
            brother.playSound(brother.getLocation(), Sound.WITHER_DEATH, 1.0f, 0.5f);
        }

        soulBrothers.remove(deadUuid);
        soulBrothers.remove(brotherUuid);
        playerWorlds.remove(deadUuid);
    }

    private void teleportToWorld(Player player, World targetWorld, Location reference) {
        player.teleport(new Location(targetWorld,
                reference.getX(), reference.getY(), reference.getZ(),
                reference.getYaw(), reference.getPitch()));

        String worldDisplay = targetWorld.getName().equals(WORLD_ALPHA) ? DISPLAY_ALPHA : DISPLAY_BETA;
        LangManager.get().send(SoulBrotherLang.ASSIGNED_TO_WORLD, player, Map.of("%world%", worldDisplay));
    }

    private void startCountdownTask() {
        if (countdownTask != null) return;

        countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive() || reunionHappened) {
                    cancel();
                    return;
                }

                int timeLeft = reunionTime - UHCManager.get().getTimer();

                if (timeLeft == 300) {
                    LangManager.get().sendAll(SoulBrotherLang.REUNION_WARNING_MINUTES, Map.of("%minutes%", "5"));
                } else if (timeLeft == 60) {
                    LangManager.get().sendAll(SoulBrotherLang.REUNION_WARNING_ONE_MINUTE);
                } else if (timeLeft == 10) {
                    LangManager.get().sendAll(SoulBrotherLang.REUNION_WARNING_SECONDS, Map.of("%seconds%", "10"));
                }
            }
        }.runTaskTimer(Main.get(), 0L, 20L);
    }

    private void startReunion() {
        if (reunionHappened) return;
        reunionHappened = true;

        LangManager.get().sendAll(SoulBrotherLang.REUNION_BROADCAST);

        World reunionWorld = Common.get().getArena();
        Set<UUID> alreadyTeleported = new HashSet<>();

        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            if (player == null || alreadyTeleported.contains(player.getUniqueId())) continue;

            UUID brotherUuid = soulBrothers.get(player.getUniqueId());
            Player brother = (brotherUuid != null) ? Bukkit.getPlayer(brotherUuid) : null;

            Location reunionLoc = findSafeLocation(reunionWorld);

            player.teleport(reunionLoc);
            LangManager.get().send(SoulBrotherLang.REUNION_PERSONAL, player);
            alreadyTeleported.add(player.getUniqueId());

            if (brother != null && brother.isOnline()) {
                brother.teleport(reunionLoc);
                LangManager.get().send(SoulBrotherLang.REUNION_PERSONAL, brother);
                alreadyTeleported.add(brother.getUniqueId());

                LangManager.get().send(SoulBrotherLang.REUNION_BROTHER_NAME, player, Map.of("%brother%", brother.getName()));
                LangManager.get().send(SoulBrotherLang.REUNION_BROTHER_NAME, brother, Map.of("%brother%", player.getName()));

                DisplayService.glow(player, brother, AME_GLOW);
                DisplayService.glow(brother, player, AME_GLOW);
                DisplayService.notification(player,
                        t(NOTIF_REUNION_TITLE, player), t(NOTIF_REUNION_BODY, player), 6);
                DisplayService.notification(brother,
                        t(NOTIF_REUNION_TITLE, brother), t(NOTIF_REUNION_BODY, brother), 6);

                giveReunionBonus(player);
                giveReunionBonus(brother);
            }
        }

        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            if (player != null) {
                player.playSound(player.getLocation(), Sound.ENDERDRAGON_DEATH, 1.0f, 1.5f);
            }
        }
    }

    private Location findSafeLocation(World world) {
        Location spawn = world.getSpawnLocation();
        int x = spawn.getBlockX() + (int) ((Math.random() - 0.5) * reunionScatterRadius * 2);
        int z = spawn.getBlockZ() + (int) ((Math.random() - 0.5) * reunionScatterRadius * 2);
        int y = world.getHighestBlockYAt(x, z) + 1;
        return new Location(world, x + 0.5, y, z + 0.5);
    }

    private void giveReunionBonus(Player player) {
        player.getInventory().addItem(
                new ItemStack(Material.GOLDEN_APPLE, 2),
                new ItemStack(Material.DIAMOND, 1),
                new ItemStack(Material.EXP_BOTTLE, 5)
        );
        LangManager.get().send(SoulBrotherLang.REUNION_BONUS, player);
    }

    private void sendSoulBrotherUpdate(Player player) {
        UUID brotherUuid = soulBrothers.get(player.getUniqueId());
        if (brotherUuid == null) return;

        Player brother = Bukkit.getPlayer(brotherUuid);
        if (brother == null || !brother.isOnline()) return;

        Location brotherLoc = brother.getLocation();
        LangManager.get().send(SoulBrotherLang.SOUL_UPDATE, player, Map.of(
                "%brother%", brother.getName(),
                "%x%", String.valueOf(brotherLoc.getBlockX()),
                "%z%", String.valueOf(brotherLoc.getBlockZ()),
                "%health%", String.valueOf((int) brother.getHealth())
        ));
    }

    private void cleanup() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        playerWorlds.clear();
        soulBrothers.clear();
        teamSplits.clear();
        updatedThisCycle.clear();
        reunionHappened = false;
    }
}

