package net.novaproject.scenarioplus.loot;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lobby.RankManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class ChestScenarios {

    private static final Random RANDOM = new Random();

    private static final Set<Material> SOLS_INTERDITS = EnumSet.of(
            Material.LAVA,
            Material.STATIONARY_LAVA,
            Material.WATER,
            Material.STATIONARY_WATER,
            Material.AIR
    );

    private ChestScenarios() {
    }

    private static Location surface(World world, int x, int z) {
        return new Location(world, x + 0.5D, Math.max(world.getHighestBlockYAt(x, z), 1), z + 0.5D);
    }

    private static Location surfaceAleatoire(World world, double rayon) {
        Location centre = world.getWorldBorder().getCenter();
        double limite = Math.min(rayon, world.getWorldBorder().getSize() / 2.0D - 5.0D);
        if (limite < 1.0D) limite = 1.0D;

        for (int essai = 0; essai < 24; essai++) {
            int x = centre.getBlockX() + (int) ((RANDOM.nextDouble() * 2.0D - 1.0D) * limite);
            int z = centre.getBlockZ() + (int) ((RANDOM.nextDouble() * 2.0D - 1.0D) * limite);
            int y = world.getHighestBlockYAt(x, z);
            if (y < 2) continue;
            if (SOLS_INTERDITS.contains(world.getBlockAt(x, y - 1, z).getType())) continue;
            return new Location(world, x + 0.5D, y, z + 0.5D);
        }
        return surface(world, centre.getBlockX(), centre.getBlockZ());
    }

    public static class BookceptionScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.bookception.desc",
                "§7Chaque joueur qui meurt lâche §d%livres% livre(s) enchanté(s) §7au hasard.");

        @Var(name = "Livres lâchés", desc = "Nombre de livres enchantés aléatoires ajoutés au butin de mort.", type = VariableType.INTEGER, min = 1)
        private int nombreLivres = 1;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Bookception"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%livres%", nombreLivres));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ENCHANTED_BOOK); }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            Enchantment[] enchantements = Enchantment.values();
            if (enchantements.length == 0) return;

            for (int i = 0; i < nombreLivres; i++) {
                Enchantment enchantement = enchantements[RANDOM.nextInt(enchantements.length)];
                ItemStack livre = new ItemStack(Material.ENCHANTED_BOOK);
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) livre.getItemMeta();
                meta.addStoredEnchant(enchantement, 1 + RANDOM.nextInt(Math.max(1, enchantement.getMaxLevel())), true);
                livre.setItemMeta(meta);
                event.getDrops().add(livre);
            }
        }
    }

    public static class BreakupScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.breakup.desc",
                "§7Les grosses équipes sont éclatées en équipes de §e%taille% §7joueur(s) au démarrage.");

        private static final DynamicLang ANNONCE = DynamicLang.of("scenario.breakup.annonce",
                "§7Les équipes ont été éclatées : regarde bien qui est encore avec toi.");

        @Var(name = "Taille maximum", desc = "Nombre de joueurs au-delà duquel une équipe est découpée en sous-équipes.", type = VariableType.INTEGER, min = 1)
        private int tailleMax = 2;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Breakup"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%taille%", tailleMax));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SHEARS); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            World world = Common.get().getArena();
            if (world == null) return;

            UHCTeamManager manager = UHCTeamManager.get();
            boolean eclate = false;

            for (UHCTeam equipe : new ArrayList<>(manager.getTeams())) {
                List<UHCPlayer> joueurs = new ArrayList<>(equipe.getPlayers());
                if (joueurs.size() <= tailleMax) continue;

                for (int debut = tailleMax; debut < joueurs.size(); debut += tailleMax) {
                    int avant = manager.getTeams().size();
                    manager.createTeam(tailleMax);
                    if (manager.getTeams().size() == avant) return;

                    UHCTeam nouvelle = manager.getTeams().get(manager.getTeams().size() - 1);
                    Location point = surfaceAleatoire(world, world.getWorldBorder().getSize() / 2.0D);

                    for (int i = debut; i < Math.min(debut + tailleMax, joueurs.size()); i++) {
                        UHCPlayer membre = joueurs.get(i);
                        membre.forceSetTeam(Optional.of(nouvelle));
                        Player joueur = membre.getPlayer();
                        if (joueur != null) joueur.teleport(point);
                    }
                    eclate = true;
                }
            }

            if (eclate) LangManager.get().sendAll(ANNONCE);
        }
    }

    public static class ChildrenLeftUnattendedScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.childrenleftunattended.desc",
                "§7La mort d'un coéquipier laisse aux survivants §b%potions% potion(s) de vitesse §7et §e%loups% loup(s) apprivoisé(s)§7.");

        private static final DynamicLang RENFORT = DynamicLang.of("scenario.childrenleftunattended.renfort",
                "§7Ton coéquipier §c%joueur% §7est tombé : voilà de quoi tenir.");

        @Var(name = "Potions données", desc = "Nombre de potions de vitesse données à chaque survivant de l'équipe.", type = VariableType.INTEGER, min = 0)
        private int nombrePotions = 1;

        @Var(name = "Loups donnés", desc = "Nombre de loups apprivoisés apparaissant auprès de chaque survivant de l'équipe.", type = VariableType.INTEGER, min = 0)
        private int nombreLoups = 1;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Children Left Unattended"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%potions%", nombrePotions, "%loups%", nombreLoups));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BONE); }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            if (uhcPlayer.getTeam().isEmpty()) return;

            for (UHCPlayer membre : uhcPlayer.getTeam().get().getPlayers()) {
                Player survivant = membre.getPlayer();
                if (survivant == null || survivant.isDead()) continue;

                for (int i = 0; i < nombrePotions; i++) {
                    survivant.getInventory().addItem(new Potion(PotionType.SPEED).toItemStack(1));
                }
                for (int i = 0; i < nombreLoups; i++) {
                    Wolf loup = survivant.getWorld().spawn(survivant.getLocation(), Wolf.class);
                    loup.setTamed(true);
                    loup.setOwner(survivant);
                }
                LangManager.get().send(RENFORT, survivant, Map.of("%joueur%", event.getEntity().getName()));
            }
        }
    }

    public static class CorpsesScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.corpses.desc",
                "§7Un cadavre reste au sol à l'endroit exact où chaque joueur est mort.");

        private static final DynamicLang NOM = DynamicLang.of("scenario.corpses.nom",
                "§8☠ §c%joueur%");

        private final List<ArmorStand> cadavres = new ArrayList<>();

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Corpses"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ARMOR_STAND); }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            Player victime = event.getEntity();
            Location lieu = victime.getLocation();
            if (lieu.getWorld() == null) return;

            ArmorStand cadavre = lieu.getWorld().spawn(lieu, ArmorStand.class);
            cadavre.setGravity(false);
            cadavre.setArms(true);
            cadavre.setBasePlate(false);
            cadavre.setCustomName(t(NOM, Map.of("%joueur%", victime.getName())));
            cadavre.setCustomNameVisible(true);
            cadavre.setHelmet(new ItemCreator("SKULL_ITEM", victime).getItemstack());
            cadavres.add(cadavre);
        }

        @Override
        public void onStop() {
            cadavres.forEach(ArmorStand::remove);
            cadavres.clear();
        }
    }

    public static class DropScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.drop.desc",
                "§7Tout le monde démarre au centre. Pendant §e%fenetre% §7secondes, §e/drop §7te largue à §e%rayon% §7blocs avec Vitesse §e%niveau%§7.");

        private static final DynamicLang LARGUE = DynamicLang.of("scenario.drop.largue",
                "§7Largage effectué : file avant qu'on te rattrape.");

        private static final DynamicLang FENETRE_FERMEE = DynamicLang.of("scenario.drop.fenetre-fermee",
                "§cLa fenêtre de largage est terminée.");

        private static final DynamicLang EPUISE = DynamicLang.of("scenario.drop.epuise",
                "§cTu as déjà utilisé tous tes largages.");

        private final Map<UUID, Integer> usages = new HashMap<>();

        @Var(name = "Fenêtre de largage", desc = "Temps de jeu pendant lequel la commande /drop reste utilisable.", type = VariableType.TIME, min = 1)
        private int fenetreSec = 300;

        @Var(name = "Rayon de largage", desc = "Distance maximale au centre du point de largage tiré au hasard.", type = VariableType.INTEGER, min = 1)
        private int rayon = 500;

        @Var(name = "Largages autorisés", desc = "Nombre de fois qu'un joueur peut utiliser /drop.", type = VariableType.INTEGER, min = 1)
        private int utilisations = 1;

        @Var(name = "Durée de la vitesse", desc = "Durée de l'effet Vitesse accordé après un largage.", type = VariableType.TIME, min = 1)
        private int dureeVitesseSec = 300;

        @Var(name = "Niveau de vitesse", desc = "Niveau de l'effet Vitesse accordé après un largage.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauVitesse = 2;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Drop"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of(
                    "%fenetre%", fenetreSec,
                    "%rayon%", rayon,
                    "%niveau%", niveauVitesse));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.HOPPER); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            World world = Common.get().getArena();
            if (world == null) return;
            Location centre = world.getWorldBorder().getCenter();
            player.teleport(surface(world, centre.getBlockX(), centre.getBlockZ()));
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onCommand(PlayerCommandPreprocessEvent event) {
            if (!isRunning()) return;
            if (!event.getMessage().trim().equalsIgnoreCase("/drop")) return;
            event.setCancelled(true);

            Player player = event.getPlayer();
            UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
            if (uhcPlayer == null || !uhcPlayer.isPlaying()) return;

            if (UHCManager.get().getTimer() > fenetreSec) {
                LangManager.get().send(FENETRE_FERMEE, player);
                return;
            }

            int deja = usages.getOrDefault(player.getUniqueId(), 0);
            if (deja >= utilisations) {
                LangManager.get().send(EPUISE, player);
                return;
            }

            World world = Common.get().getArena();
            if (world == null) return;

            usages.put(player.getUniqueId(), deja + 1);
            player.teleport(surfaceAleatoire(world, rayon));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, dureeVitesseSec * 20, niveauVitesse - 1), true);
            LangManager.get().send(LARGUE, player);
        }

        @Override
        public void onStop() {
            usages.clear();
        }
    }

    public static class EmergencyCallScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.emergencycall.desc",
                "§e/ecall §7téléporte toute ton équipe sur toi, une fois toutes les §e%cooldown% §7secondes.");

        private static final DynamicLang APPEL_LANCE = DynamicLang.of("scenario.emergencycall.appel-lance",
                "§7Appel de détresse lancé : ton équipe arrive.");

        private static final DynamicLang APPEL_RECU = DynamicLang.of("scenario.emergencycall.appel-recu",
                "§c%joueur% §7lance un appel de détresse et te tire à lui.");

        private static final DynamicLang SANS_EQUIPE = DynamicLang.of("scenario.emergencycall.sans-equipe",
                "§cTu n'as pas d'équipe à appeler.");

        private static final DynamicLang EN_ATTENTE = DynamicLang.of("scenario.emergencycall.en-attente",
                "§cAppel indisponible pendant encore §e%secondes% §cseconde(s).");

        private final Map<String, Long> prochainAppel = new HashMap<>();

        @Var(name = "Temps de recharge", desc = "Délai avant qu'une équipe puisse relancer un appel de détresse.", type = VariableType.TIME, min = 1)
        private int cooldownSec = 600;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Emergency Call"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%cooldown%", cooldownSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.REDSTONE_TORCH_ON); }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onCommand(PlayerCommandPreprocessEvent event) {
            if (!isRunning()) return;
            if (!event.getMessage().trim().equalsIgnoreCase("/ecall")) return;
            event.setCancelled(true);

            Player player = event.getPlayer();
            UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
            if (uhcPlayer == null || !uhcPlayer.isPlaying()) return;
            if (uhcPlayer.getTeam().isEmpty()) {
                LangManager.get().send(SANS_EQUIPE, player);
                return;
            }

            UHCTeam equipe = uhcPlayer.getTeam().get();
            long maintenant = System.currentTimeMillis();
            long disponible = prochainAppel.getOrDefault(equipe.name(), 0L);
            if (maintenant < disponible) {
                LangManager.get().send(EN_ATTENTE, player,
                        Map.of("%secondes%", (disponible - maintenant) / 1000L));
                return;
            }

            prochainAppel.put(equipe.name(), maintenant + cooldownSec * 1000L);
            for (UHCPlayer membre : equipe.getPlayers()) {
                Player allie = membre.getPlayer();
                if (allie == null || allie.equals(player)) continue;
                allie.teleport(player);
                LangManager.get().send(APPEL_RECU, allie, Map.of("%joueur%", player.getName()));
            }
            LangManager.get().send(APPEL_LANCE, player);
        }

        @Override
        public void onStop() {
            prochainAppel.clear();
        }
    }

    public static class EnchantedDeathScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.enchanteddeath.desc",
                "§7Les morts lâchent §5%tables% table(s) d'enchantement§7, et il devient impossible d'en crafter.");

        private static final DynamicLang CRAFT_BLOQUE = DynamicLang.of("scenario.enchanteddeath.craft-bloque",
                "§cLa table d'enchantement ne se craft pas : il faut la prendre sur un cadavre.");

        @Var(name = "Tables lâchées", desc = "Nombre de tables d'enchantement ajoutées au butin de mort.", type = VariableType.INTEGER, min = 1)
        private int nombreTables = 1;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Enchanted Death"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%tables%", nombreTables));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ENCHANTMENT_TABLE); }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            event.getDrops().add(new ItemStack(Material.ENCHANTMENT_TABLE, nombreTables));
        }

        @Override
        public void onCraft(ItemStack result, CraftItemEvent event) {
            if (!isActive()) return;
            if (result == null || result.getType() != Material.ENCHANTMENT_TABLE) return;

            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                LangManager.get().send(CRAFT_BLOQUE, player);
            }
        }
    }

    public static class FlameRetardantItemsScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.flameretardantitems.desc",
                "§7Un objet au sol résiste §e%duree% §7secondes au feu et à la lave avant de partir en fumée.");

        private final Map<UUID, Long> premierFeu = new HashMap<>();

        @Var(name = "Résistance au feu", desc = "Temps qu'un objet au sol survit dans le feu ou la lave avant de brûler.", type = VariableType.TIME, min = 1)
        private int dureeSec = 10;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Flame Retardant Items"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%duree%", dureeSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BLAZE_POWDER); }

        @EventHandler(ignoreCancelled = true)
        public void onItemBurn(EntityDamageEvent event) {
            if (!isRunning()) return;
            if (!(event.getEntity() instanceof Item objet)) return;

            DamageCause cause = event.getCause();
            if (cause != DamageCause.FIRE && cause != DamageCause.FIRE_TICK && cause != DamageCause.LAVA) return;

            long maintenant = System.currentTimeMillis();
            Long debut = premierFeu.putIfAbsent(objet.getUniqueId(), maintenant);
            if (debut != null && maintenant - debut >= dureeSec * 1000L) return;

            event.setCancelled(true);
            objet.setFireTicks(0);
        }

        @Override
        public void onStop() {
            premierFeu.clear();
        }
    }

    public static class GenerousAdminScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.generousadmin.desc",
                "§7Le staff est relargué avec du butin toutes les §e%intervalle% §7secondes, sa boussole le trahit, et le tuer rapporte gros.");

        private static final DynamicLang RELARGUE = DynamicLang.of("scenario.generousadmin.relargue",
                "§7Le staff vient d'être relargué quelque part avec du butin frais.");

        private BukkitRunnable task;

        @Var(name = "Intervalle de largage", desc = "Temps entre deux largages du staff avec son butin.", type = VariableType.TIME, min = 1)
        private int intervalleSec = 600;

        @Var(name = "Diamants du butin", desc = "Nombre de diamants donnés au staff à chaque largage.", type = VariableType.INTEGER, min = 0)
        private int diamants = 3;

        @Var(name = "Pommes d'or du butin", desc = "Nombre de pommes d'or données au staff à chaque largage.", type = VariableType.INTEGER, min = 0)
        private int pommesDor = 2;

        @Var(name = "Lingots d'or du butin", desc = "Nombre de lingots d'or donnés au staff à chaque largage.", type = VariableType.INTEGER, min = 0)
        private int lingotsOr = 8;

        @Var(name = "Multiplicateur de mort", desc = "Facteur appliqué au butin lâché quand le staff se fait tuer.", type = VariableType.INTEGER, min = 1)
        private int multiplicateurMort = 2;

        @Var(name = "Boussole traçante", desc = "La boussole de tous les joueurs pointe vers le staff.", type = VariableType.BOOLEAN)
        private boolean boussole = true;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Generous Admin"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%intervalle%", intervalleSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLD_BLOCK); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            if (!RankManager.isStaff(player)) return;
            butin(1).forEach(item -> player.getInventory().addItem(item));
        }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    World world = Common.get().getArena();
                    if (world == null) return;

                    boolean largue = false;
                    for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                        Player staff = uhcPlayer.getPlayer();
                        if (staff == null || !RankManager.isStaff(staff)) continue;
                        staff.teleport(surfaceAleatoire(world, world.getWorldBorder().getSize() / 2.0D));
                        butin(1).forEach(item -> staff.getInventory().addItem(item));
                        staff.playSound(staff.getLocation(), Sound.ORB_PICKUP, 1f, 1f);
                        largue = true;
                    }
                    if (largue) LangManager.get().sendAll(RELARGUE);
                }
            };
            task.runTaskTimer(Main.get(), intervalleSec * 20L, intervalleSec * 20L);
        }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            if (!boussole) return;

            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player staff = uhcPlayer.getPlayer();
                if (staff == null || !RankManager.isStaff(staff)) continue;
                player.setCompassTarget(staff.getLocation());
                return;
            }
        }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            if (!RankManager.isStaff(event.getEntity())) return;
            event.getDrops().addAll(butin(multiplicateurMort));
        }

        @Override
        public void onStop() {
            if (task == null) return;
            task.cancel();
            task = null;
        }

        private List<ItemStack> butin(int facteur) {
            List<ItemStack> items = new ArrayList<>();
            if (diamants > 0) items.add(new ItemStack(Material.DIAMOND, Math.min(64, diamants * facteur)));
            if (pommesDor > 0) items.add(new ItemStack(Material.GOLDEN_APPLE, Math.min(64, pommesDor * facteur)));
            if (lingotsOr > 0) items.add(new ItemStack(Material.GOLD_INGOT, Math.min(64, lingotsOr * facteur)));
            return items;
        }
    }

    public static class GoodGameScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.goodgame.desc",
                "§7Après une mort, le premier à écrire §agg §7dans les §e%fenetre% §7secondes gagne une récompense. Les propos toxiques coûtent §c%degats% §7dégâts.");

        private static final DynamicLang RECOMPENSE = DynamicLang.of("scenario.goodgame.recompense",
                "§a%joueur% §7a été le plus fair-play et empoche sa récompense.");

        private static final DynamicLang TOXIQUE = DynamicLang.of("scenario.goodgame.toxique",
                "§cGarde ça pour toi : ça vient de te coûter de la vie.");

        private static final Material[] RECOMPENSES = {
                Material.DIAMOND,
                Material.CAKE,
                Material.GOLD_INGOT,
                Material.FIREWORK
        };

        private volatile long finFenetre = 0L;
        private volatile boolean reclame = false;

        @Var(name = "Fenêtre du gg", desc = "Temps après une mort pendant lequel un gg est récompensé.", type = VariableType.TIME, min = 1)
        private int fenetreSec = 30;

        @Var(name = "Quantité récompensée", desc = "Nombre d'exemplaires de la récompense donnée au premier gg.", type = VariableType.INTEGER, min = 1)
        private int quantiteRecompense = 1;

        @Var(name = "Dégâts de toxicité", desc = "Dégâts infligés à un joueur dont le message contient un mot interdit.", type = VariableType.DOUBLE, min = 0.5)
        private double degatsToxicite = 4.0;

        @Var(name = "Mots interdits", desc = "Mots déclenchant la sanction, séparés par des virgules.", type = VariableType.STRING)
        private String motsToxiques = "noob,ez,trash,nul,bot";

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Good Game"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%fenetre%", fenetreSec, "%degats%", degatsToxicite));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.CAKE); }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            finFenetre = System.currentTimeMillis() + fenetreSec * 1000L;
            reclame = false;
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onChat(AsyncPlayerChatEvent event) {
            if (!isRunning()) return;
            Player player = event.getPlayer();
            String message = event.getMessage().toLowerCase(Locale.ROOT);

            for (String mot : motsToxiques.split(",")) {
                String propre = mot.trim().toLowerCase(Locale.ROOT);
                if (propre.isEmpty() || !message.contains(propre)) continue;
                event.setCancelled(true);
                Bukkit.getScheduler().runTask(Main.get(), () -> {
                    if (!player.isOnline() || player.isDead()) return;
                    player.damage(degatsToxicite);
                    LangManager.get().send(TOXIQUE, player);
                });
                return;
            }

            if (reclame || System.currentTimeMillis() > finFenetre) return;
            if (!message.trim().equals("gg")) return;

            reclame = true;
            Material recompense = RECOMPENSES[RANDOM.nextInt(RECOMPENSES.length)];
            Bukkit.getScheduler().runTask(Main.get(), () -> {
                if (player.isOnline()) {
                    player.getInventory().addItem(new ItemStack(recompense, quantiteRecompense));
                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1f);
                }
                LangManager.get().sendAll(RECOMPENSE, Map.of("%joueur%", player.getName()));
            });
        }

        @Override
        public void onStop() {
            finFenetre = 0L;
            reclame = false;
        }
    }

    public static class GraveRobbersScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.graverobbers.desc",
                "§7Le stuff d'un mort finit dans un double coffre planté sur place, pas éparpillé au sol.");

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Grave Robbers"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.CHEST); }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;

            List<ItemStack> butin = new ArrayList<>();
            for (ItemStack drop : event.getDrops()) {
                if (drop != null && drop.getType() != Material.AIR) butin.add(drop);
            }
            if (butin.isEmpty()) return;

            Location lieu = event.getEntity().getLocation();
            World world = lieu.getWorld();
            if (world == null) return;

            Block gauche = world.getBlockAt(lieu);
            if (gauche.getType() != Material.AIR) {
                gauche = gauche.getRelative(BlockFace.UP);
            }
            Block droite = gauche.getRelative(BlockFace.EAST);
            gauche.setTypeIdAndData(Material.CHEST.getId(), (byte) 0, false);
            droite.setTypeIdAndData(Material.CHEST.getId(), (byte) 0, false);

            if (!(gauche.getState() instanceof Chest coffre)) return;

            event.getDrops().clear();
            Inventory tombe = coffre.getInventory();
            for (ItemStack reste : tombe.addItem(butin.toArray(new ItemStack[0])).values()) {
                world.dropItemNaturally(lieu, reste);
            }
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new BookceptionScenario(),
                new BreakupScenario(),
                new ChildrenLeftUnattendedScenario(),
                new CorpsesScenario(),
                new DropScenario(),
                new EmergencyCallScenario(),
                new EnchantedDeathScenario(),
                new FlameRetardantItemsScenario(),
                new GenerousAdminScenario(),
                new GoodGameScenario(),
                new GraveRobbersScenario()
        );
    }
}
