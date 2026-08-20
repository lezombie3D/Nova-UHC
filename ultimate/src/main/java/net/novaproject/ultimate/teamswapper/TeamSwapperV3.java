package net.novaproject.ultimate.teamswapper;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.ui.CustomInventory;
import net.novaproject.novauhc.ui.item.ActionItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;
import net.novaproject.novauhc.lang.lang.ScenarioLang;

public class TeamSwapperV3 extends Scenario {

    @Var(name = "Maximum lives", desc = "Maximum number of lives a player can have.", type = VariableType.INTEGER)
    private int maxLives = 3;

    @Var(name = "Initial lives", desc = "Number of lives each player starts with.", type = VariableType.INTEGER)
    private int initialLives = 2;

    @Var(name = "Number of Anciens", desc = "Number of Ancien players per team (oldest members excluding Maître).", type = VariableType.INTEGER)
    private int nombreAnciens = 2;

    @Var(name = "Coup d'État start", desc = "Game time before the first Coup d'État.", type = VariableType.TIME)
    private int coupDetatStartTime = 1800;

    @Var(name = "Coup d'État interval", desc = "Time between each Coup d'État.", type = VariableType.TIME)
    private int coupDetatInterval = 900;

    @Var(name = "Cargaison interval", desc = "Time between each Cargaison spawn.", type = VariableType.TIME)
    private int cargaisonInterval = 300;

    @Var(name = "Maître protection (%)", desc = "Chance the Maître survives a killing blow.", type = VariableType.PERCENTAGE)
    private double maitreProtectionChance = 0.6;

    @Var(name = "Ancien proximity radius", desc = "Block distance at which a Nouveau loses HP near an Ancien.", type = VariableType.INTEGER)
    private int proximityRadius = 16;

    @Var(name = "HP loss interval", desc = "Time between each Nouveau HP-loss check.", type = VariableType.TIME)
    private int proximityInterval = 180;

    @Var(name = "Cargaison effect duration", desc = "Duration in seconds of cargaison effects.", type = VariableType.TIME)
    private int cargaisonEffectDuration = 300;

    @Var(name = "Number of Winners", desc = "Number of players (by seniority) who win at the end, among the surviving team.", type = VariableType.INTEGER)
    private int numberOfWinners = 3;

    private boolean winnersPrinted = false;

    private final Map<UUID, Integer> playerLives      = new HashMap<>();
    private final Map<UUID, TSClass> playerClasses    = new HashMap<>();
    private final Map<UUID, Long>    teamJoinTime     = new HashMap<>();
    private final Map<UUID, Double>  permanentHpLoss  = new HashMap<>();
    private final Set<Entity>        activeCargaisons = new HashSet<>();
    private final Random             random           = new Random();

    private BukkitTask proximityTask;
    private BukkitTask coupDetatTask;
    private BukkitTask cargaisonTask;

    @Override public String getName() { return "TeamSwapper v3"; }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.TEAM_SWAPPER_V3, player);
    }

    @Override
    public ItemCreator getItem() { return new ItemCreator(Material.SKULL_ITEM); }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean overridesVictory() {
        return isActive();
    }

    @Override
    public boolean isWin() {
        List<UHCTeam> aliveTeams = UHCTeamManager.get().getAliveTeams();
        if (aliveTeams.size() != 1) return false;
        if (!winnersPrinted) {
            printVainqueurs(aliveTeams.get(0));
            winnersPrinted = true;
        }
        return true;
    }

    private void printVainqueurs(UHCTeam team) {
        List<UHCPlayer> survivors = team.getPlayers().stream()
                .filter(UHCPlayer::isPlaying)
                .collect(Collectors.toList());
        long now = System.currentTimeMillis();
        survivors.sort(Comparator.comparingLong(p -> teamJoinTime.getOrDefault(p.getUniqueId(), now)));

        Bukkit.broadcastMessage(LangManager.get().get(ScenarioLang.TEAMSWAPPERV3_VAINQUEURS_HEADER));
        int rank = 1;
        for (UHCPlayer p : survivors) {
            long ancienneSec = (now - teamJoinTime.getOrDefault(p.getUniqueId(), now)) / 1000;
            String prefix = rank <= numberOfWinners ? "§6⭐ " : "  ";
            Bukkit.broadcastMessage(LangManager.get().get(
                    ScenarioLang.TEAMSWAPPERV3_VAINQUEURS_LINE,
                    Map.of(
                            "%prefix%", prefix,
                            "%rank%", String.valueOf(rank),
                            "%player%", p.getPlayer() != null ? p.getPlayer().getName() : "?",
                            "%anc%", String.valueOf(ancienneSec)
                    )
            ));
            rank++;
        }
    }

    @Override
    public void onGameStart() {
        Bukkit.broadcastMessage("Mode de jeu recréer a partir de la documentation publique d'Eterny");
        winnersPrinted = false;
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(p ->
                playerLives.put(p.getUniqueId(), initialLives));

        initClasses();

        scheduleProximity();
        scheduleCoupDetat(coupDetatStartTime);
        scheduleCargaison(cargaisonInterval);

        UHCPlayerManager.setCustomScoreboardLines("teamswapper_v3", p -> {
            UHCPlayer up = UHCPlayerManager.get().getPlayer(p);
            if (up == null || !up.isPlaying()) return Collections.emptyList();
            int lives = playerLives.getOrDefault(p.getUniqueId(), initialLives);
            TSClass cls = playerClasses.getOrDefault(p.getUniqueId(), TSClass.NOUVEAU);
            return Arrays.asList(
                    "§f  §8● §fVies: §c" + lives + " §7/ §c" + maxLives,
                    "§f  §8● §fClasse: §6" + cls.name()
            );
        });
    }

    private void scheduleProximity() {
        proximityTask = new BukkitRunnable() {
            @Override public void run() {
                if (!isActive()) return;
                checkProximityHpLoss();
                scheduleProximity();
            }
        }.runTaskLater(Main.get(), proximityInterval * 20L);
    }

    private void scheduleCoupDetat(int delaySeconds) {
        coupDetatTask = new BukkitRunnable() {
            @Override public void run() {
                if (!isActive()) return;
                performCoupDetat();
                scheduleCoupDetat(coupDetatInterval);
            }
        }.runTaskLater(Main.get(), delaySeconds * 20L);
    }

    private void scheduleCargaison(int delaySeconds) {
        cargaisonTask = new BukkitRunnable() {
            @Override public void run() {
                if (!isActive()) return;
                spawnCargaison();
                scheduleCargaison(cargaisonInterval);
            }
        }.runTaskLater(Main.get(), delaySeconds * 20L);
    }

    @Override
    public void onStop() {
        UHCPlayerManager.setCustomScoreboardLines("teamswapper_v3", null);
        if (proximityTask != null) { proximityTask.cancel(); proximityTask = null; }
        if (coupDetatTask != null) { coupDetatTask.cancel(); coupDetatTask = null; }
        if (cargaisonTask != null) { cargaisonTask.cancel(); cargaisonTask = null; }

        activeCargaisons.forEach(e -> { if (!e.isDead()) e.remove(); });
        activeCargaisons.clear();

        playerLives.clear();
        playerClasses.clear();
        teamJoinTime.clear();
        permanentHpLoss.clear();
        winnersPrinted = false;
    }

    @Override
    public void onSec(Player p) {
        if (!isActive()) return;
        TSClass cls = playerClasses.get(p.getUniqueId());
        if (cls == null) return;

        if (cls == TSClass.MAITRE) {
            UHCPlayer uhc = UHCPlayerManager.get().getPlayer(p);
            boolean isLast = uhc != null && uhc.getTeam().isPresent() &&
                    uhc.getTeam().get().getPlayers().stream().filter(UHCPlayer::isPlaying).count() <= 1;
            if (isLast) {
                UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                        new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 40, 0, false, false),
                        new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 0, false, false)
                }, p);
            } else {
                p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
                UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                        new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 40, 0, false, false)
                }, p);
            }
        } else if (cls == TSClass.ANCIEN) {
            UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                    new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 0, false, false)
            }, p);
            updateCompass(p);
        }
    }

    @Override
    public void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event) {
        if (!isActive()) return;
        if (!(entity instanceof Player victim)) return;

        Player killer = null;
        if (dammager instanceof Player) {
            killer = (Player) dammager;
        } else if (dammager instanceof Projectile) {
            Object shooter = ((Projectile) dammager).getShooter();
            if (shooter instanceof Player) killer = (Player) shooter;
        }
        if (killer == null || killer.equals(victim)) return;
        if (victim.getHealth() - event.getFinalDamage() > 0) return;

        UHCPlayer uhcVictim = UHCPlayerManager.get().getPlayer(victim);
        UHCPlayer uhcKiller = UHCPlayerManager.get().getPlayer(killer);
        if (uhcVictim == null || !uhcVictim.isPlaying()) return;
        if (uhcKiller == null || !uhcKiller.isPlaying()) return;

        int lives = playerLives.getOrDefault(victim.getUniqueId(), initialLives);
        TSClass victimClass = playerClasses.getOrDefault(victim.getUniqueId(), TSClass.NOUVEAU);

        if (victimClass == TSClass.MAITRE) {
            if (random.nextDouble() < maitreProtectionChance) {
                event.setCancelled(true);
                victim.setHealth(1.0);
                LangManager.get().send(ScenarioLang.TEAMSWAPPERV3_MAITRE_PROTECTED, victim, Map.of());
                LangManager.get().send(ScenarioLang.TEAMSWAPPERV3_MAITRE_PROTECTED_KILLER, killer,
                        Map.of("%player%", victim.getName()));
                return;
            }
            if (lives <= 1) return;
            event.setCancelled(true);
            playerLives.put(victim.getUniqueId(), lives - 1);
        } else {
            if (lives <= 1) return;
            event.setCancelled(true);
            playerLives.put(victim.getUniqueId(), lives - 1);
        }

        victim.setHealth(victim.getMaxHealth());
        Optional<UHCTeam> oldTeam    = uhcVictim.getTeam();
        Optional<UHCTeam> killerTeam = uhcKiller.getTeam();

        if (victimClass == TSClass.NOUVEAU) {
            double loss = permanentHpLoss.getOrDefault(victim.getUniqueId(), 0.0);
            if (loss > 0) {
                victim.setMaxHealth(Math.min(20.0, victim.getMaxHealth() + loss));
                victim.setHealth(victim.getMaxHealth());
                permanentHpLoss.put(victim.getUniqueId(), 0.0);
            }
        }
        if (victimClass == TSClass.ANCIEN) removeTrackerCompass(uhcVictim);

        playerClasses.put(victim.getUniqueId(), TSClass.NOUVEAU);
        uhcVictim.forceSetTeam(killerTeam);
        teamJoinTime.put(victim.getUniqueId(), System.currentTimeMillis());
        victim.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1200, 0));

        oldTeam.ifPresent(this::updateClasses);
        killerTeam.ifPresent(this::updateClasses);

        int newKillerLives = Math.min(playerLives.getOrDefault(killer.getUniqueId(), initialLives) + 1, maxLives);
        playerLives.put(killer.getUniqueId(), newKillerLives);

        String teamDisplay = killerTeam.map(t -> t.prefix() + t.name()).orElse("§f" + killer.getName());
        LangManager.get().sendAll(ScenarioLang.TEAMSWAPPERV3_TRANSFER,
                Map.of("%victim%", victim.getName(), "%killer%", killer.getName(), "%team%", teamDisplay));
        LangManager.get().send(ScenarioLang.TEAMSWAPPERV3_LIVES_LEFT, victim,
                Map.of("%lives%", String.valueOf(lives - 1), "%max%", String.valueOf(maxLives)));
        LangManager.get().send(ScenarioLang.TEAMSWAPPERV3_LIVES_GAINED, killer,
                Map.of("%lives%", String.valueOf(newKillerLives)));
        UHCManager.get().checkVictory();
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
    }

    @Override
    public void onTeamUpdate() {
        if (UHCManager.get().getTeam_size() == 1) {
            UHCManager.get().setTeam_size(2);
            LangManager.get().sendAll(CoreLang.COMMON_TEAM_REDFINIED_AUTO);
        }
    }

    @Override
    public void onPlayerInteractEntity(Player player, PlayerInteractEntityEvent event) {
        if (!isActive()) return;
        if (!activeCargaisons.contains(event.getRightClicked())) return;
        event.setCancelled(true);
        Entity cargaison = event.getRightClicked();
        activeCargaisons.remove(cargaison);
        cargaison.remove();
        new CargaisonMenu(player).open();
    }

    private void initClasses() {
        long now = System.currentTimeMillis();
        for (UHCTeam team : UHCTeamManager.get().getAliveTeams()) {
            List<UHCPlayer> members = team.getPlayers().stream()
                    .filter(UHCPlayer::isPlaying).collect(Collectors.toList());
            if (members.isEmpty()) continue;

            for (int i = 0; i < members.size(); i++)
                teamJoinTime.put(members.get(i).getUniqueId(), now + i);

            UHCPlayer maitre = members.get(random.nextInt(members.size()));
            playerClasses.put(maitre.getUniqueId(), TSClass.MAITRE);
            Optional.ofNullable(maitre.getPlayer()).ifPresent(p ->
                    LangManager.get().send(ScenarioLang.TEAMSWAPPERV3_CLASSE_MAITRE_ASSIGNED, p, Map.of()));

            List<UHCPlayer> rest = members.stream()
                    .filter(p -> !p.getUniqueId().equals(maitre.getUniqueId()))
                    .collect(Collectors.toList());
            for (int i = 0; i < rest.size(); i++) {
                UHCPlayer uhcp = rest.get(i);
                TSClass cls = (i < nombreAnciens) ? TSClass.ANCIEN : TSClass.NOUVEAU;
                playerClasses.put(uhcp.getUniqueId(), cls);
                Player p = uhcp.getPlayer();
                if (p == null) continue;
                if (cls == TSClass.ANCIEN) {
                    giveTrackerCompass(uhcp);
                    LangManager.get().send(ScenarioLang.TEAMSWAPPERV3_CLASSE_ANCIEN_ASSIGNED, p, Map.of());
                } else {
                    LangManager.get().send(ScenarioLang.TEAMSWAPPERV3_CLASSE_NOUVEAU_ASSIGNED, p, Map.of());
                }
            }
        }
    }

    private void updateClasses(UHCTeam team) {
        List<UHCPlayer> members = team.getPlayers().stream()
                .filter(UHCPlayer::isPlaying).collect(Collectors.toList());
        if (members.isEmpty()) return;

        UUID currentMaitre = members.stream().map(UHCPlayer::getUniqueId)
                .filter(uuid -> playerClasses.getOrDefault(uuid, TSClass.NOUVEAU) == TSClass.MAITRE)
                .findFirst().orElse(null);

        if (currentMaitre == null) {
            currentMaitre = members.stream()
                    .min(Comparator.comparingLong(p -> teamJoinTime.getOrDefault(p.getUniqueId(), Long.MAX_VALUE)))
                    .map(UHCPlayer::getUniqueId).orElse(null);
        }

        final UUID maitre = currentMaitre;
        if (maitre != null) {
            TSClass prev = playerClasses.put(maitre, TSClass.MAITRE);
            if (prev != TSClass.MAITRE) {
                members.stream().filter(p -> p.getUniqueId().equals(maitre))
                        .findFirst().ifPresent(uhcp -> {
                            removeTrackerCompass(uhcp);
                            Optional.ofNullable(uhcp.getPlayer()).ifPresent(p ->
                                    LangManager.get().send(ScenarioLang.TEAMSWAPPERV3_CLASSE_MAITRE_ASSIGNED, p, Map.of()));
                        });
            }
        }

        List<UHCPlayer> nonMaitres = members.stream()
                .filter(p -> !p.getUniqueId().equals(maitre))
                .sorted(Comparator.comparingLong(p -> teamJoinTime.getOrDefault(p.getUniqueId(), Long.MAX_VALUE)))
                .collect(Collectors.toList());

        for (int i = 0; i < nonMaitres.size(); i++) {
            UHCPlayer uhcp = nonMaitres.get(i);
            UUID uuid = uhcp.getUniqueId();
            TSClass oldCls = playerClasses.getOrDefault(uuid, TSClass.NOUVEAU);
            TSClass newCls = (i < nombreAnciens) ? TSClass.ANCIEN : TSClass.NOUVEAU;
            playerClasses.put(uuid, newCls);
            if (oldCls == newCls) continue;

            Player p = uhcp.getPlayer();
            if (oldCls == TSClass.NOUVEAU && newCls == TSClass.ANCIEN) {
                double loss = permanentHpLoss.getOrDefault(uuid, 0.0);
                if (loss > 0 && p != null) {
                    p.setMaxHealth(Math.min(20.0, p.getMaxHealth() + loss));
                    p.setHealth(Math.min(p.getHealth() + loss, p.getMaxHealth()));
                    permanentHpLoss.put(uuid, 0.0);
                }
                giveTrackerCompass(uhcp);
                if (p != null) LangManager.get().send(ScenarioLang.TEAMSWAPPERV3_CLASSE_ANCIEN_ASSIGNED, p, Map.of());
            } else if (oldCls == TSClass.ANCIEN && newCls == TSClass.NOUVEAU) {
                removeTrackerCompass(uhcp);
                if (p != null) LangManager.get().send(ScenarioLang.TEAMSWAPPERV3_CLASSE_NOUVEAU_DEMOTED, p, Map.of());
            }
        }
    }

    private void giveTrackerCompass(UHCPlayer uhcp) {
        Player p = uhcp.getPlayer();
        if (p == null) return;
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        meta.setDisplayName(LangManager.get().get(ScenarioLang.TEAMSWAPPERV3_TRACKER_NAME, p));
        compass.setItemMeta(meta);
        p.getInventory().addItem(compass);
    }

    private void removeTrackerCompass(UHCPlayer uhcp) {
        Player p = uhcp.getPlayer();
        if (p == null) return;
        String trackerName = LangManager.get().get(ScenarioLang.TEAMSWAPPERV3_TRACKER_NAME, p);
        ItemStack[] contents = p.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.hasItemMeta() &&
                    trackerName.equals(item.getItemMeta().getDisplayName())) {
                p.getInventory().setItem(i, null);
                break;
            }
        }
    }

    private void updateCompass(Player p) {
        UHCPlayer uhc = UHCPlayerManager.get().getPlayer(p);
        if (uhc == null || !uhc.getTeam().isPresent()) return;
        Location pLoc = p.getLocation();
        uhc.getTeam().get().getPlayers().stream()
                .filter(UHCPlayer::isPlaying)
                .filter(ally -> !ally.getUniqueId().equals(p.getUniqueId()))
                .filter(ally -> {
                    TSClass cls = playerClasses.get(ally.getUniqueId());
                    return cls == TSClass.ANCIEN || cls == TSClass.MAITRE;
                })
                .filter(ally -> ally.getPlayer() != null)
                .min(Comparator.comparingDouble(ally ->
                        ally.getPlayer().getLocation().distanceSquared(pLoc)))
                .ifPresent(nearest -> p.setCompassTarget(nearest.getPlayer().getLocation()));
    }

    private void checkProximityHpLoss() {
        for (UHCPlayer uhcp : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player p = uhcp.getPlayer();
            if (p == null) continue;
            UUID uuid = p.getUniqueId();
            if (playerClasses.getOrDefault(uuid, TSClass.NOUVEAU) != TSClass.NOUVEAU) continue;
            if (!uhcp.getTeam().isPresent()) continue;

            boolean nearAncien = uhcp.getTeam().get().getPlayers().stream()
                    .filter(UHCPlayer::isPlaying)
                    .filter(ally -> !ally.getUniqueId().equals(uuid))
                    .filter(ally -> playerClasses.getOrDefault(ally.getUniqueId(), TSClass.NOUVEAU) == TSClass.ANCIEN)
                    .anyMatch(ally -> {
                        Player ap = ally.getPlayer();
                        return ap != null && ap.getWorld().equals(p.getWorld()) &&
                                ap.getLocation().distanceSquared(p.getLocation()) <= proximityRadius * proximityRadius;
                    });

            if (!nearAncien) continue;

            double newMax = Math.max(2.0, p.getMaxHealth() - 1.0);
            p.setMaxHealth(newMax);
            if (p.getHealth() > newMax) p.setHealth(newMax);
            double totalLoss = permanentHpLoss.merge(uuid, 1.0, Double::sum);
            LangManager.get().send(ScenarioLang.TEAMSWAPPERV3_HP_LOSS, p,
                    Map.of("%hearts%", String.valueOf((int) (totalLoss / 2))));
        }
    }

    private void performCoupDetat() {
        List<UHCTeam> eligibles = UHCTeamManager.get().getAliveTeams().stream()
                .filter(team -> team.getPlayers().stream().filter(UHCPlayer::isPlaying)
                        .anyMatch(p -> playerClasses.getOrDefault(p.getUniqueId(), TSClass.NOUVEAU) == TSClass.NOUVEAU))
                .collect(Collectors.toList());
        if (eligibles.isEmpty()) return;

        UHCTeam sourceTeam = eligibles.get(random.nextInt(eligibles.size()));
        List<UHCPlayer> nouveaux = sourceTeam.getPlayers().stream()
                .filter(UHCPlayer::isPlaying)
                .filter(p -> playerClasses.getOrDefault(p.getUniqueId(), TSClass.NOUVEAU) == TSClass.NOUVEAU)
                .collect(Collectors.toList());
        if (nouveaux.isEmpty()) return;

        UHCPlayer chosen = nouveaux.get(random.nextInt(nouveaux.size()));
        Player chosenPlayer = chosen.getPlayer();
        if (chosenPlayer == null) return;

        double loss = permanentHpLoss.getOrDefault(chosen.getUniqueId(), 0.0);
        if (loss > 0) {
            chosenPlayer.setMaxHealth(Math.min(20.0, chosenPlayer.getMaxHealth() + loss));
            chosenPlayer.setHealth(chosenPlayer.getMaxHealth());
            permanentHpLoss.put(chosen.getUniqueId(), 0.0);
        }

        int sizeBefore = UHCTeamManager.get().getTeams().size();
        UHCTeamManager.get().createTeam(100);
        List<UHCTeam> allTeams = UHCTeamManager.get().getTeams();
        if (allTeams.size() <= sizeBefore) return;
        UHCTeam newTeam = allTeams.get(allTeams.size() - 1);
        chosen.forceSetTeam(Optional.of(newTeam));
        teamJoinTime.put(chosen.getUniqueId(), System.currentTimeMillis());
        playerClasses.put(chosen.getUniqueId(), TSClass.MAITRE);

        updateClasses(sourceTeam);

        String teamDisplay = newTeam.prefix() + newTeam.name();
        LangManager.get().sendAll(ScenarioLang.TEAMSWAPPERV3_COUP_DETAT_BROADCAST,
                Map.of("%player%", chosenPlayer.getName(), "%team%", teamDisplay));
        LangManager.get().send(ScenarioLang.TEAMSWAPPERV3_COUP_DETAT_PLAYER, chosenPlayer,
                Map.of("%team%", teamDisplay));
    }

    private void spawnCargaison() {
        World world = Common.get().getArena();
        double borderRadius = world.getWorldBorder().getSize() / 2.0 * 0.8;
        Location center = world.getWorldBorder().getCenter();
        double x = center.getX() + (random.nextDouble() * 2 - 1) * borderRadius;
        double z = center.getZ() + (random.nextDouble() * 2 - 1) * borderRadius;
        int y = world.getHighestBlockYAt((int) x, (int) z);
        Location loc = new Location(world, x + 0.5, y, z + 0.5);

        ArmorStand stand = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setCustomName("§6✦ §lCargaison §6✦");
        stand.setCustomNameVisible(true);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setSmall(true);
        activeCargaisons.add(stand);

        LangManager.get().sendAll(ScenarioLang.TEAMSWAPPERV3_CARGAISON_SPAWN,
                Map.of("%x%", String.valueOf((int) x),
                       "%y%", String.valueOf(y),
                       "%z%", String.valueOf((int) z)));
    }

    private void giveReward(Player player, int slot) {
        UUID uuid = player.getUniqueId();
        TSClass cls = playerClasses.getOrDefault(uuid, TSClass.NOUVEAU);

        if (slot == 1) {
            PotionEffectType type;
            if (cls == TSClass.MAITRE) {
                type = PotionEffectType.SPEED;
            } else if (cls == TSClass.ANCIEN) {
                type = random.nextBoolean() ? PotionEffectType.INCREASE_DAMAGE : PotionEffectType.SPEED;
            } else {
                PotionEffectType[] pool = {PotionEffectType.SPEED, PotionEffectType.INCREASE_DAMAGE, PotionEffectType.DAMAGE_RESISTANCE};
                type = pool[random.nextInt(pool.length)];
            }
            player.addPotionEffect(new PotionEffect(type, cargaisonEffectDuration * 20, 0));
            LangManager.get().send(ScenarioLang.TEAMSWAPPERV3_CARGAISON_REWARD_EFFECT, player,
                    Map.of("%effect%", getEffectName(type, player)));

        } else if (slot == 4) {
            int updated = Math.min(playerLives.getOrDefault(uuid, initialLives) + 1, maxLives);
            playerLives.put(uuid, updated);
            LangManager.get().send(ScenarioLang.TEAMSWAPPERV3_CARGAISON_REWARD_LIFE, player,
                    Map.of("%lives%", String.valueOf(updated), "%max%", String.valueOf(maxLives)));

        } else if (slot == 7) {
            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (armor == null || armor.getType() == Material.AIR) continue;
                short maxDur = armor.getType().getMaxDurability();
                armor.setDurability((short) Math.max(0, armor.getDurability() - (short) (maxDur * 0.5)));
            }
            player.updateInventory();
            LangManager.get().send(ScenarioLang.TEAMSWAPPERV3_CARGAISON_REWARD_ARMOR, player, Map.of());
        }
    }

    private String getEffectName(PotionEffectType type, Player viewer) {
        if (type == PotionEffectType.SPEED)             return LangManager.get().get(ScenarioLang.TEAMSWAPPERV3_EFFECT_SPEED, viewer);
        if (type == PotionEffectType.INCREASE_DAMAGE)   return LangManager.get().get(ScenarioLang.TEAMSWAPPERV3_EFFECT_STRENGTH, viewer);
        if (type == PotionEffectType.DAMAGE_RESISTANCE) return LangManager.get().get(ScenarioLang.TEAMSWAPPERV3_EFFECT_RESISTANCE, viewer);
        return type.getName();
    }

    private class CargaisonMenu extends CustomInventory {
        CargaisonMenu(Player player) { super(player); }

        @Override public String getTitle() { return LangManager.get().get(ScenarioLang.TEAMSWAPPERV3_CARGAISON_MENU_TITLE, getPlayer()); }
        @Override public int getLines() { return 1; }
        @Override public boolean isRefreshAuto() { return false; }

        @Override
        public void setup() {
            Player p = getPlayer();
            TSClass cls = playerClasses.getOrDefault(p.getUniqueId(), TSClass.NOUVEAU);

            String effectLore;
            if      (cls == TSClass.MAITRE) effectLore = LangManager.get().get(ScenarioLang.TEAMSWAPPERV3_CARGAISON_SLOT_EFFECT_LORE_MAITRE, p);
            else if (cls == TSClass.ANCIEN) effectLore = LangManager.get().get(ScenarioLang.TEAMSWAPPERV3_CARGAISON_SLOT_EFFECT_LORE_ANCIEN, p);
            else                            effectLore = LangManager.get().get(ScenarioLang.TEAMSWAPPERV3_CARGAISON_SLOT_EFFECT_LORE_NOUVEAU, p);

            ItemCreator effectItem = new ItemCreator(Material.NETHER_STAR)
                    .setName(LangManager.get().get(ScenarioLang.TEAMSWAPPERV3_CARGAISON_SLOT_EFFECT_NAME, p))
                    .setLores(Collections.singletonList(effectLore));
            ItemCreator lifeItem = new ItemCreator(Material.GOLDEN_APPLE)
                    .setName(LangManager.get().get(ScenarioLang.TEAMSWAPPERV3_CARGAISON_SLOT_LIFE_NAME, p))
                    .setLores(Collections.singletonList(LangManager.get().get(ScenarioLang.TEAMSWAPPERV3_CARGAISON_SLOT_LIFE_LORE, p)));
            ItemCreator armorItem = new ItemCreator(Material.IRON_CHESTPLATE)
                    .setName(LangManager.get().get(ScenarioLang.TEAMSWAPPERV3_CARGAISON_SLOT_ARMOR_NAME, p))
                    .setLores(Collections.singletonList(LangManager.get().get(ScenarioLang.TEAMSWAPPERV3_CARGAISON_SLOT_ARMOR_LORE, p)));

            addItem(new ActionItem(1, effectItem) {
                @Override public void onClick(InventoryClickEvent e) { giveReward(p, 1); p.closeInventory(); }
            });
            addItem(new ActionItem(4, lifeItem) {
                @Override public void onClick(InventoryClickEvent e) { giveReward(p, 4); p.closeInventory(); }
            });
            addItem(new ActionItem(7, armorItem) {
                @Override public void onClick(InventoryClickEvent e) { giveReward(p, 7); p.closeInventory(); }
            });
        }
    }
}

