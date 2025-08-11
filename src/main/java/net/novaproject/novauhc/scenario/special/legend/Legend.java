package net.novaproject.novauhc.scenario.special.legend;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.LongCooldownManager;
import net.novaproject.novauhc.utils.ShortCooldownManager;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static net.novaproject.novauhc.scenario.special.legend.LegendsUtils.*;

public class Legend extends Scenario {

    private static Legend instance;
    private final List<PotionEffectType> effects = new ArrayList<>();
    private final Random random = new Random();
    public Map<UHCPlayer, Integer> classe = new HashMap<>();
    public Map<UHCTeam, Map<UHCPlayer, Integer>> classTeam = new HashMap<>();
    public Map<UHCTeam, UHCPlayer> princesse = new HashMap<>();
    public Map<UHCPlayer, List<UHCPlayer>> marionettes = new HashMap<>();
    public Map<UHCPlayer, Integer> marionetteClass = new HashMap<>();
    private final Map<UHCPlayer, UHCPlayer> puppetToMaster = new HashMap<>();
    private final Map<UHCPlayer, UHCPlayer> necro = new HashMap<>();
    private final Map<UHCPlayer, List<Entity>> NecrotoSumonded = new HashMap<>();
    private boolean canchoosen = true;

    public Legend() {
        effects.add(PotionEffectType.SPEED);
        effects.add(PotionEffectType.INCREASE_DAMAGE);
        effects.add(PotionEffectType.DAMAGE_RESISTANCE);
        effects.add(PotionEffectType.FIRE_RESISTANCE);
    }

    public static Legend get() {
        return instance;
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
    }

    @Override
    public String getName() {
        return "UHC Legends";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.DRAGON_EGG);
    }

    @Override
    public void setup() {
        instance = this;
        canchoosen = true;
        classe.clear();
        classTeam.clear();
        marionettes.clear();
        marionetteClass.clear();
        puppetToMaster.clear();
        princesse.clear();
        necro.clear();
        NecrotoSumonded.clear();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public void onStart(Player player) {
        UHCPlayer p = UHCPlayerManager.get().getPlayer(player);
        if (p.getTeam().isPresent()) {
            UHCTeam team = p.getTeam().get();

            classTeam.putIfAbsent(team, new HashMap<>());
        }

        player.sendMessage(Common.get().getServertag() + ChatColor.GOLD +
                " Vous avez 3 min pour choisir vos classes : /ld choose");

        new BukkitRunnable() {
            @Override
            public void run() {
                canchoosen = false;
                cannotchoose(player);
            }
        }.runTaskLater(Main.get(), 20 * 60 * 3L);
    }

    private void removePotion(Player player, PotionEffect[] addPotion) {
        for (PotionEffect activeEffect : addPotion) {
            player.removePotionEffect(activeEffect.getType());
        }
        for (PotionEffect effect : addPotion) {
            effect.apply(player);
        }
    }

    private void cannotchoose(Player player) {
        UHCPlayer uplayer = UHCPlayerManager.get().getPlayer(player);
        if (!canchoosen) {
            if (uplayer.getTeam().isPresent()) {
                UHCTeam team = uplayer.getTeam().get();

                if (classe.containsKey(uplayer)) {
                    player.getPlayer().sendMessage(ChatColor.RED + "Vous avez déjà une classe");
                    return;
                }
                Random random = new Random();
                classTeam.putIfAbsent(team, new HashMap<>());
                int idclass;
                List<Integer> list = getClassesDisponibles(team);
                idclass = list.get(random.nextInt(list.size()));

                classe.put(uplayer, idclass);
                classTeam.get(team).put(uplayer, idclass);
                switch (idclass) {
                    case 0:
                        marionettes.putIfAbsent(uplayer, new ArrayList<>());
                        System.out.println("Marionnette");
                        setMarionnette(player);
                        break;
                    case 1:
                        setMage(player);
                        break;
                    case 2:
                        setArcher(player);
                        break;
                    case 3:
                        setAssasin(player);
                        break;
                    case 4:
                        setTank(player);
                        break;
                    case 5:
                        setNain(player);
                        break;
                    case 6:
                        setZeus(player);
                        break;
                    case 7:
                        NecrotoSumonded.putIfAbsent(uplayer, new ArrayList<>());
                        setNecro(player);
                        break;
                    case 8:
                        setSucube(player);
                        break;
                    case 9:
                        setSoldat(player);
                        break;
                    case 10:
                        princesse.put(team, team.getPlayers().get(random.nextInt(team.getTeamSize())));
                        setPrincesse(player);
                        break;
                    case 11:
                        setCavalier(player);
                        break;
                    case 12:
                        setOgre(player);
                        break;
                    case 13:
                        setDragon(player);
                        break;
                    case 14:
                        setMedecin(player);
                        break;
                    case 15:
                        setPrisonier(player);
                    case 16:
                        setCorne(player);
                        break;
                    default:
                        break;

                }

            }

        }
    }

    @Override
    public void toggleActive() {
        super.toggleActive();
        if (isActive()) {
            UHCManager.get().setTeam_size(2);
        }
    }

    @Override
    public void onPlayerInteract(Player player, PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        int idclass = classe.getOrDefault(uhcPlayer, -1);
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) return;

            if (item.getType() == Material.NETHER_STAR && meta.getDisplayName().equals(ChatColor.GOLD + "Pouvoir")) {
                player.sendMessage(Common.get().getServertag() + ChatColor.GREEN + "Vous avez activée votre pouvoir !");
                switch (idclass) {
                    case 5:
                        if (LongCooldownManager.get(player.getUniqueId(), "CDNain") == -1) {
                            LongCooldownManager.put(player.getUniqueId(), "CDNain", 10 * 60 * 1000);
                            giveArmor(player, 20 * 30);

                        } else {
                            player.sendMessage(ChatColor.RED + "Veuillez attendre encore : " + getTimerFormatted((int) (LongCooldownManager.get(player.getUniqueId(), "CDNain") / 1000)));
                        }
                        break;
                    case 6:
                        if (LongCooldownManager.get(player.getUniqueId(), "CDpala") == -1) {
                            LongCooldownManager.put(player.getUniqueId(), "CDpala", 10 * 60 * 1000);
                            List<PotionEffect> effects = Arrays.asList(
                                    new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 2, 0, false, false),
                                    new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 60 * 2, 0, false, false),
                                    new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 60 * 2, 0, false, false)
                            );
                            Collections.shuffle(effects);
                            for (int i = 0; i < 2; i++) {
                                player.addPotionEffect(effects.get(i));
                            }

                        } else {
                            player.sendMessage(ChatColor.RED + "Veuillez attendre encore : " + getTimerFormatted((int) (LongCooldownManager.get(player.getUniqueId(), "CDpala") / 1000)));
                        }
                        break;
                    case 8:
                        if (LongCooldownManager.get(player.getUniqueId(), "CDdemon") == -1) {
                            LongCooldownManager.put(player.getUniqueId(), "CDdemon", 6 * 60 * 1000);
                            PotionEffect[] potionEffects = new PotionEffect[]{
                                    new PotionEffect(PotionEffectType.ABSORPTION, 20 * 60, 2, false, false)
                            };

                            if (uhcPlayer.getTeam().isPresent()) {
                                for (UHCPlayer p : uhcPlayer.getTeam().get().getPlayers()) {
                                    if (p != uhcPlayer) {
                                        if (p.getPlayer().getLocation().distance(uhcPlayer.getPlayer().getLocation()) < 11) {
                                            removePotion(player, potionEffects);
                                        }
                                    }

                                }
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Veuillez attendre encore : " + LongCooldownManager.get(player.getUniqueId(), "CDdemon") / 1000 + "s");
                        }
                        break;
                    case 11:
                        if (LongCooldownManager.get(player.getUniqueId(), "CDcava") == -1) {
                            LongCooldownManager.put(player.getUniqueId(), "CDcava", 5 * 60 * 1000);
                            spawnHorse(player);

                        } else {
                            player.sendMessage(ChatColor.RED + "Veuillez attendre encore : " + getTimerFormatted((int) (LongCooldownManager.get(player.getUniqueId(), "CDcava") / 1000)));
                        }
                        break;
                    case 7:
                        if (LongCooldownManager.get(player.getUniqueId(), "CDnecro") == -1) {
                            LongCooldownManager.put(player.getUniqueId(), "CDnecro", 10 * 60 * 1000);
                            Player target = findNearestPlayer(player);

                            if (target == null
                                    || target == player
                                    || (uhcPlayer.getTeam().isPresent() && uhcPlayer.getTeam().get().getPlayers().contains(target))) {
                                player.sendMessage(Common.get().getInfoTag() + ChatColor.RED + "Aucun joueur ennemi valide trouvé à proximité. Ressayez !.");
                                LongCooldownManager.remove(player.getUniqueId(), "CDnecro");
                                return;
                            }
                            UHCPlayer uhcTarget = UHCPlayerManager.get().getPlayer(target);
                            necro.putIfAbsent(uhcPlayer, uhcTarget);
                            List<Entity> entities = NecrotoSumonded.get(uhcTarget);
                            summonMobs(player, target, entities);
                        } else {
                            player.sendMessage(ChatColor.RED + "Veuillez attendre encore : " + getTimerFormatted((int) (LongCooldownManager.get(player.getUniqueId(), "CDnecro") / 1000)));
                        }
                        break;
                    default:
                        break;
                }
            }
            if (idclass == 16) {

                if (meta.getDisplayName().equals(ChatColor.GOLD + "Melodie : Feu")) {
                    if (LongCooldownManager.get(player.getUniqueId(), "CDCfeu") == -1) {
                        LongCooldownManager.put(player.getUniqueId(), "CDCfeu", 60 * 1000);

                        if (uhcPlayer.getTeam().isPresent()) {

                            for (UHCPlayer p : uhcPlayer.getTeam().get().getPlayers()) {
                                if (p.getPlayer().getLocation().distance(uhcPlayer.getPlayer().getLocation()) < 31) {
                                    p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 12, 2, false, false));
                                    player.sendMessage(Common.get().getInfoTag() + ChatColor.GREEN + "Vous avez reçu les pouvoir d'une melodie !");
                                }

                            }
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Veuillez attendre encore : " + getTimerFormatted((int) (LongCooldownManager.get(player.getUniqueId(), "CDCfeu") / 1000)));
                    }
                }
                if (meta.getDisplayName().equals(ChatColor.GOLD + "Melodie : Heal")) {
                    if (LongCooldownManager.get(player.getUniqueId(), "CDCheal") == -1) {
                        LongCooldownManager.put(player.getUniqueId(), "CDCheal", 10 * 60 * 1000);

                        if (uhcPlayer.getTeam().isPresent()) {

                            for (UHCPlayer p : uhcPlayer.getTeam().get().getPlayers()) {
                                if (p.getPlayer().getLocation().distance(uhcPlayer.getPlayer().getLocation()) < 31) {
                                    p.getPlayer().setHealth(p.getPlayer().getMaxHealth());
                                    player.sendMessage(Common.get().getInfoTag() + ChatColor.GREEN + "Vous avez reçu les pouvoir d'une melodie !");
                                }

                            }
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Veuillez attendre encore : " + getTimerFormatted((int) (LongCooldownManager.get(player.getUniqueId(), "CDCheal") / 1000)));
                    }
                }
                if (meta.getDisplayName().equals(ChatColor.GOLD + "Melodie : Metal")) {
                    if (LongCooldownManager.get(player.getUniqueId(), "CDTerre") == -1) {
                        LongCooldownManager.put(player.getUniqueId(), "CDTerre", 60 * 1000);

                        if (uhcPlayer.getTeam().isPresent()) {

                            for (UHCPlayer p : uhcPlayer.getTeam().get().getPlayers()) {
                                if (p.getPlayer().getLocation().distance(uhcPlayer.getPlayer().getLocation()) < 31) {
                                    p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 5, 1, false, false));
                                    player.sendMessage(Common.get().getInfoTag() + ChatColor.GREEN + "Vous avez reçu les pouvoir d'une melodie !");
                                }

                            }
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Veuillez attendre encore : " + getTimerFormatted((int) (LongCooldownManager.get(player.getUniqueId(), "CDTerre") / 1000)));
                    }
                }
                if (meta.getDisplayName().equals(ChatColor.GOLD + "Melodie : Air")) {
                    if (LongCooldownManager.get(player.getUniqueId(), "CDair") == -1) {
                        LongCooldownManager.put(player.getUniqueId(), "CDair", 3 * 60 * 1000);
                        if (uhcPlayer.getTeam().isPresent()) {

                            for (UHCPlayer p : uhcPlayer.getTeam().get().getPlayers()) {
                                if (p.getPlayer().getLocation().distance(uhcPlayer.getPlayer().getLocation()) < 31) {
                                    p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 8, 1, false, false));
                                    player.sendMessage(Common.get().getInfoTag() + ChatColor.GREEN + "Vous avez reçu les pouvoir d'une melodie !");
                                }

                            }
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Veuillez attendre encore : " + getTimerFormatted((int) (LongCooldownManager.get(player.getUniqueId(), "CDair") / 1000)));
                    }
                }
            }
        }
    }

    @Override
    public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
        if (!(entity instanceof Player)) return;

        Player victim = (Player) entity;
        Player attacker = null;

        if (damager instanceof Player) {
            attacker = (Player) damager;
        } else if (damager instanceof Arrow) {
            Arrow arrow = (Arrow) damager;
            if (arrow.getShooter() instanceof Player) {
                attacker = (Player) arrow.getShooter();
            }
        }

        if (attacker == null) return;

        UHCPlayer uhcAttacker = UHCPlayerManager.get().getPlayer(attacker);
        UHCPlayer uhcVictim = UHCPlayerManager.get().getPlayer(victim);

        UHCPlayer master = puppetToMaster.get(uhcAttacker);
        if (master != null && master.equals(uhcVictim)) {
            event.setCancelled(true);
            return;
        }

        List<UHCPlayer> puppets = marionettes.get(uhcAttacker);
        if (puppets != null && puppets.contains(uhcVictim)) {
            event.setCancelled(true);
            return;
        }

        int classeId = classe.getOrDefault(uhcAttacker, -1);
        if (classeId == 13 && Math.random() < 0.5) {
            victim.setFireTicks(100);
        } else if (classeId == 6) {
            Random rand = new Random();
            if (rand.nextDouble() < 0.10) {
                victim.getWorld().strikeLightning(victim.getLocation());
                victim.damage(1.0);
            }

            if (rand.nextDouble() < 0.20) {
                attacker.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0, true, true));
            }
        }
    }

    @Override
    public void onAfterDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        Player player = uhcPlayer.getPlayer();
        Location location = player.getLocation();
        int idclassdead = classe.getOrDefault(uhcPlayer, -1);
        int idclasskiller = classe.getOrDefault(killer, -1);


        if (idclassdead == 0) {
            killMarionettes(uhcPlayer, location, event);
        }

        if (puppetToMaster.containsKey(uhcPlayer)) {
            UHCPlayer master = puppetToMaster.get(uhcPlayer);
            if (marionettes.containsKey(master)) {
                marionettes.get(master).remove(uhcPlayer);
            }
            puppetToMaster.remove(uhcPlayer);
            marionetteClass.remove(uhcPlayer);
        }

        if (uhcPlayer.getTeam().isPresent()) {
            UHCTeam team = uhcPlayer.getTeam().get();
            for (UHCPlayer p : team.getPlayers()) {
                int teamMateClass = classe.getOrDefault(p, -1);
                if (teamMateClass == 10) {
                    if (princesse.containsValue(uhcPlayer)) {
                        p.getPlayer().setMaxHealth(20);
                    }
                }
            }
        }
        if (necro.get(killer) == uhcPlayer) {
            List<Entity> sumo = NecrotoSumonded.get(killer);
            necro.remove(killer);
            Player target = findNearestPlayer(player);
            if (target == null
                    || target == player
                    || (uhcPlayer.getTeam().isPresent() && uhcPlayer.getTeam().get().getPlayers().contains(target))) {
                for (Entity entity : sumo) {
                    entity.remove();
                }
            } else {
                for (Entity entity : sumo) {
                    if (entity instanceof Skeleton) {
                        Skeleton skeleton = (Skeleton) entity;
                        skeleton.setTarget(target);
                    }
                    if (entity instanceof Zombie) {
                        Zombie zombie = (Zombie) entity;
                        zombie.setTarget(target);
                    }
                }
            }
        }
        if (killer != null && idclassdead == 0 && idclasskiller != 0) {
            return;
        }
        if (killer != null && idclasskiller == 0) {
            List<UHCPlayer> puppetList = marionettes.getOrDefault(killer, new ArrayList<>());
            int teamSize = UHCManager.get().getTeam_size();
            int currentPuppets = puppetList.size();

            Bukkit.getLogger().info("Current puppets: " + currentPuppets + ", Team size: " + teamSize);
            giveGoldenApplesToPuppets(killer, marionettes);

            if (currentPuppets < teamSize) {
                classe.remove(uhcPlayer);
                setMarionettes(uhcPlayer, killer);
                killer.getPlayer().sendMessage(Common.get().getInfoTag() + ChatColor.GREEN +
                        "Marionnette ajoutée (" + (currentPuppets + 1) + "/" + teamSize + ")");
            } else {
                uhcPlayer.setPlaying(false);
            }
        }

    }

    @Override
    public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {

        if (entity instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            Player player = (Player) entity;
            UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
            if (classe.getOrDefault(uhcPlayer, -1) == 10) {
                event.setCancelled(true);
            }

        }

    }

    @Override
    public void onTeamUpdate() {
        if (UHCManager.get().getTeam_size() == 1) {
            UHCManager.get().setTeam_size(2);
            Bukkit.broadcastMessage(ChatColor.YELLOW + "La taille des équipes a été automatiquement définie à 2.");
        }

    }

    @Override
    public void onSec(Player p) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(p);
        int idclass = classe.getOrDefault(uhcPlayer, -1);
        if (ShortCooldownManager.get(p, "pala") == -1) {
            p.getActivePotionEffects().clear();
        }
        switch (idclass) {
            case 3:
                int swordCount = 0;
                for (ItemStack item : p.getInventory().getContents()) {
                    if (item != null && isSword(item.getType())) {
                        swordCount += item.getAmount();
                    }
                }
                if (hasSecretBlade(p) && swordCount < 2) {
                    PotionEffect[] bladeEff = {
                            new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, 0, false, false)
                    };
                    removePotion(p, bladeEff);
                }
                break;
            case 1:
                if (LongCooldownManager.get(p, "mage") == -1) {
                    LongCooldownManager.put(p, "mage", 1000 * 60 * 10);
                    ItemCreator force = new ItemCreator(Material.POTION)
                            .addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 30, 0, false, false), true);
                    ItemCreator res = new ItemCreator(Material.POTION)
                            .addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 30, 0, false, false), true);
                    ItemCreator speed = new ItemCreator(Material.POTION)
                            .addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 30, 0, false, false), true);

                    p.getWorld().dropItemNaturally(p.getLocation(), force.getItemstack()).setPickupDelay(0);
                    p.getWorld().dropItemNaturally(p.getLocation(), res.getItemstack()).setPickupDelay(0);
                    p.getWorld().dropItemNaturally(p.getLocation(), speed.getItemstack()).setPickupDelay(0);
                }
                break;
            case 4:
                if (p.getHealth() < 11) {
                    PotionEffect[] eff = {
                            new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 0, false, false)
                    };
                    removePotion(p, eff);
                }
                break;

            case 15:
                PotionEffect[] speedEff = {
                        new PotionEffect(PotionEffectType.SPEED, 80, 0, false, false)
                };
                removePotion(p, speedEff);
                break;

            case 16:
                PotionEffect[] weakEff = {
                        new PotionEffect(PotionEffectType.WEAKNESS, 80, 0, false, false)
                };
                removePotion(p, weakEff);
                break;

            case 13:
                PotionEffect[] fireEff = {
                        new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, 1, false, false)
                };
                removePotion(p, fireEff);
                break;

            case 12:
                int gapCount = 0;
                for (ItemStack item : p.getInventory().getContents()) {
                    if (item != null && item.getType() == Material.GOLDEN_APPLE) {
                        gapCount += item.getAmount();
                    }
                }
                if (gapCount <= 5) {
                    PotionEffect[] lowGapEff = {
                            new PotionEffect(PotionEffectType.SLOW, 80, 0, false, false),
                            new PotionEffect(PotionEffectType.WEAKNESS, 80, 0, false, false)
                    };
                    removePotion(p, lowGapEff);
                }
                break;
            case 14:
                if (uhcPlayer.getTeam().isPresent()) {
                    spawnParticleCircle(p, p.getLocation(), Effect.HEART, 15, 50);
                    UHCTeam team = uhcPlayer.getTeam().get();
                    for (UHCPlayer uhcPlayer1 : team.getPlayers()) {
                        Player player1 = uhcPlayer1.getPlayer();
                        if (player1.getLocation().distance(p.getLocation()) < 16 && player1.getHealth() < 10) {
                            player1.setHealth(player1.getHealth() + 1);
                        }
                    }
                }
                break;
            default:
                break;
        }
        if (marionetteClass.containsKey(uhcPlayer)) {
            int marionetteID = marionetteClass.getOrDefault(uhcPlayer, -1);
            PotionEffect[] lowGapEff = {
                    new PotionEffect(PotionEffectType.POISON, 80, 0, false, false),
            };
            UHCPlayer pupetmaster = puppetToMaster.get(uhcPlayer);
            if (!(p.getLocation().distance(pupetmaster.getPlayer().getLocation()) < 16)) {
                removePotion(p, lowGapEff);
            }
            switch (marionetteID) {
                case 0:
                    PotionEffect[] eff = new PotionEffect[]{
                            new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, 0, false, false),
                    };
                    removePotion(p, eff);
                    break;
                case 1:
                    eff = new PotionEffect[]{
                            new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 0, false, false),
                    };
                    removePotion(p, eff);
                    break;

                case 2:
                    eff = new PotionEffect[]{
                            new PotionEffect(PotionEffectType.SPEED, 80, 0, false, false),
                    };
                    removePotion(p, eff);
                    break;
                default:
                    break;
            }
        }
    }

    private boolean isSword(Material material) {
        return material == Material.WOOD_SWORD ||
                material == Material.STONE_SWORD ||
                material == Material.IRON_SWORD ||
                material == Material.GOLD_SWORD ||
                material == Material.DIAMOND_SWORD;
    }


    @Override
    public void onLdCMD(Player player, String subCommand, String[] args) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        int idclass = classe.getOrDefault(uhcPlayer, -1);
        switch (subCommand) {
            case "choose":
                if (canchoosen && UHCManager.get().isGame()) {
                    new ChooseUi(player).open();
                } else {
                    player.sendMessage(ChatColor.RED + "vous ne pouvez plus choisir");
                }
                break;
            case "pouvoir":
                if (idclass == 8 || idclass == 6 || idclass == 13 || idclass == 11) {
                    ItemCreator pov = new ItemCreator(Material.NETHER_STAR).setName(ChatColor.GOLD + "Pouvoir");
                    player.getInventory().addItem(pov.getItemstack());
                }
                if (idclass == 16) {
                    ItemCreator pov25 = new ItemCreator(Material.NETHER_STAR).setName(ChatColor.GOLD + "Melodie : Feu");
                    ItemCreator pov3 = new ItemCreator(Material.NETHER_STAR).setName(ChatColor.GOLD + "Melodie : Heal");
                    ItemCreator pov4 = new ItemCreator(Material.NETHER_STAR).setName(ChatColor.GOLD + "Melodie : Metal");
                    ItemCreator pov5 = new ItemCreator(Material.NETHER_STAR).setName(ChatColor.GOLD + "Melodie : Air");

                    player.getInventory().addItem(pov25.getItemstack());
                    player.getInventory().addItem(pov3.getItemstack());
                    player.getInventory().addItem(pov4.getItemstack());
                    player.getInventory().addItem(pov5.getItemstack());
                }
                break;
            default:
                player.sendMessage(ChatColor.RED + "Commande inconnue. Essayez /ld pour plus d'informations.");
        }
    }

    private boolean hasSecretBlade(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isSecretBlade(item)) {
                return true;
            }
        }
        return false;
    }


    private boolean isSecretBlade(ItemStack item) {
        if (item == null) return false;
        if (item.getType() != Material.IRON_SWORD) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        String display = meta.getDisplayName();
        String expectedName = ChatColor.BOLD + "" + ChatColor.GOLD + "Lame Secréte";
        if (!expectedName.equals(display)) return false;
        if (!meta.hasEnchant(Enchantment.DAMAGE_ALL)) return false;
        return meta.getEnchantLevel(Enchantment.DAMAGE_ALL) == 4;
    }

    private void giveArmor(Player player, int delay) {
        ItemStack[] oldArmor = Arrays.stream(player.getInventory().getArmorContents())
                .map(item -> item != null ? item.clone() : null)
                .toArray(ItemStack[]::new);

        ItemStack[] newArmor = new ItemStack[]{
                new ItemCreator(Material.DIAMOND_BOOTS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3).getItemstack(),
                new ItemCreator(Material.DIAMOND_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3).getItemstack(),
                new ItemCreator(Material.DIAMOND_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3).getItemstack(),
                new ItemCreator(Material.DIAMOND_HELMET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3).getItemstack(),
        };

        player.getInventory().setArmorContents(newArmor);

        new BukkitRunnable() {
            @Override
            public void run() {
                player.getInventory().setArmorContents(oldArmor);
            }
        }.runTaskLater(Main.get(), delay);
    }

    @Override
    public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (classe.get(uhcPlayer) == 12) {
            if (item.getType() == Material.GOLDEN_APPLE) {

                PotionEffectType randomEffect = effects.get(random.nextInt(effects.size()));

                player.addPotionEffect(new PotionEffect(randomEffect, 20 * 5, 0));

                player.sendMessage(ChatColor.GOLD + "Vous avez reçu l'effet : " + ChatColor.AQUA + randomEffect.getName());
            }
        }
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType() == PotionEffectType.ABSORPTION) {
                player.removePotionEffect(PotionEffectType.ABSORPTION);
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 4, 0));
            }
        }
    }

    private List<Integer> getClassesDisponibles(UHCTeam team) {
        Set<Integer> classesUtilisées = new HashSet<>();

        if (classTeam.containsKey(team)) {
            for (UHCPlayer player : team.getPlayers()) {
                int playerClass = classTeam.get(team).getOrDefault(player, -1);
                if (playerClass >= 0) {
                    classesUtilisées.add(playerClass);
                }
            }
        }

        List<Integer> classesDisponibles = new ArrayList<>();
        for (int i = 0; i < 17; i++) {
            if (!classesUtilisées.contains(i)) {
                classesDisponibles.add(i);
            }
        }

        return classesDisponibles;
    }

    @Override
    public void onDrop(Player player, PlayerDropItemEvent event) {
        UHCPlayer p = UHCPlayerManager.get().getPlayer(player);
        if (marionetteClass.containsKey(p)) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onPickUp(Player player, Item item, PlayerPickupItemEvent event) {
        UHCPlayer p = UHCPlayerManager.get().getPlayer(player);
        if (marionetteClass.containsKey(p)) {
            Material type = item.getItemStack().getType();
            if (type != Material.COBBLESTONE) {
                event.setCancelled(true);
            }
        }
    }

    public void setMarionettes(UHCPlayer uhcPlayer, UHCPlayer killer) {
        Player player = uhcPlayer.getPlayer();
        Player killerPlayer = killer.getPlayer();

        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.setMaxHealth(20);
        player.setHealth(player.getMaxHealth());
        player.setGameMode(GameMode.SURVIVAL);

        if (!marionettes.containsKey(killer)) {
            marionettes.put(killer, new ArrayList<>());
        }
        marionettes.get(killer).add(uhcPlayer);

        uhcPlayer.setPlaying(true);
        if (killer.getTeam().isPresent()) {
            uhcPlayer.forecSetTeam(Optional.of(killer.getTeam().get()));
        }

        player.teleport(killerPlayer.getLocation());

        player.sendMessage(Common.get().getInfoTag() + ChatColor.GREEN + "Vous avez été transformé en marionnette !");
        killerPlayer.sendMessage(Common.get().getInfoTag() + ChatColor.GREEN + "Vous avez bien transformé une marionnette !");

        Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
            try {
                ItemStack boots = new ItemCreator(Material.IRON_BOOTS).setUnbreakable(true).getItemstack();
                ItemStack leggings = new ItemCreator(Material.IRON_LEGGINGS).setUnbreakable(true).getItemstack();
                ItemStack chestplate = new ItemCreator(Material.IRON_CHESTPLATE)
                        .addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
                        .setUnbreakable(true)
                        .getItemstack();
                ItemStack helmet = new ItemCreator(Material.IRON_HELMET).setUnbreakable(true).getItemstack();

                player.getInventory().setBoots(boots);
                player.getInventory().setLeggings(leggings);
                player.getInventory().setChestplate(chestplate);
                player.getInventory().setHelmet(helmet);

                player.getInventory().addItem(new ItemCreator(Material.WOOD_SWORD)
                        .addEnchantment(Enchantment.DAMAGE_ALL, 2)
                        .setUnbreakable(true)
                        .getItemstack());

                player.getInventory().addItem(new ItemCreator(Material.BOW)
                        .setUnbreakable(true)
                        .getItemstack());

                player.getInventory().addItem(new ItemCreator(Material.ARROW)
                        .setAmount(32)
                        .getItemstack());

                player.getInventory().addItem(new ItemCreator(Material.COOKED_BEEF)
                        .setAmount(64)
                        .getItemstack());

                player.getInventory().addItem(new ItemCreator(Material.GOLDEN_APPLE)
                        .setAmount(5)
                        .getItemstack());

                player.getInventory().addItem(new ItemCreator(Material.COBBLESTONE)
                        .setAmount(128)
                        .getItemstack());

                player.getInventory().addItem(new ItemCreator(Material.IRON_PICKAXE)
                        .addEnchantment(Enchantment.DIG_SPEED, 2)
                        .setUnbreakable(true)
                        .getItemstack());

                player.updateInventory();

                Random random = new Random();
                int id = random.nextInt(3);

                switch (id) {
                    case 0:
                        sendFeroce(player);
                        break;
                    case 1:
                        sendMassif(player);
                        break;
                    case 2:
                        sendTimide(player);
                        break;
                    default:
                        break;
                }

                marionetteClass.put(uhcPlayer, id);
                puppetToMaster.put(uhcPlayer, killer);
            } catch (Exception e) {
                Bukkit.getLogger().severe("Error giving equipment to marionette: " + e.getMessage());
                e.printStackTrace();
            }
        }, 5L);
    }


    public void killMarionettes(UHCPlayer marionnettiste, Location location, PlayerDeathEvent event) {
        if (!marionettes.containsKey(marionnettiste)) return;

        List<UHCPlayer> puppets = marionettes.get(marionnettiste);
        if (puppets == null || puppets.isEmpty()) return;

        List<UHCPlayer> puppetsToKill = new ArrayList<>(puppets);

        for (UHCPlayer puppet : puppetsToKill) {
            Player puppetPlayer = puppet.getPlayer();
            if (puppetPlayer != null && puppetPlayer.isOnline()) {
                puppet.setPlaying(false);

                marionetteClass.remove(puppet);
                puppetToMaster.remove(puppet);

                puppetPlayer.setHealth(0.0);
                puppetPlayer.setGameMode(GameMode.SPECTATOR);
                puppetPlayer.spigot().respawn();
                puppetPlayer.teleport(location);

                Bukkit.broadcastMessage(Common.get().getServertag() + ChatColor.RED +
                        "La marionnette " + puppetPlayer.getName() + ChatColor.RED +
                        " est morte suite à la mort de son marionnettiste!");
            }
        }

        marionettes.remove(marionnettiste);
    }
}
