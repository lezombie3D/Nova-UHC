package net.novaproject.scenarioplus.loot;

import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class SocialScenarios {

    private SocialScenarios() {
    }

    public static class ParanoiaScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.paranoia.desc",
                "§7Miner de l'or ou du diamant, crafter un objet clé ou mourir diffuse tes coordonnées à tout le monde.");

        private static final DynamicLang MINAGE = DynamicLang.of("scenario.paranoia.minage",
                "§e%joueur% §7a miné un minerai précieux en §e%x%, %y%, %z%§7.");

        private static final DynamicLang CRAFT = DynamicLang.of("scenario.paranoia.craft",
                "§e%joueur% §7a crafté un objet clé en §e%x%, %y%, %z%§7.");

        private static final DynamicLang MORT = DynamicLang.of("scenario.paranoia.mort",
                "§e%joueur% §7est mort en §e%x%, %y%, %z%§7.");

        private static final Set<Material> MINERAIS = EnumSet.of(
                Material.GOLD_ORE,
                Material.DIAMOND_ORE
        );

        private static final Set<Material> OBJETS_CLES = EnumSet.of(
                Material.ENCHANTMENT_TABLE,
                Material.ANVIL,
                Material.GOLDEN_APPLE,
                Material.SKULL_ITEM
        );

        private final Random random = new Random();

        @Var(name = "Annoncer le minage", desc = "Miner de l'or ou du diamant diffuse les coordonnées.", type = VariableType.BOOLEAN)
        private boolean annoncerMinage = true;

        @Var(name = "Annoncer les crafts", desc = "Crafter une table d'enchantement, une enclume, une pomme d'or ou une tête diffuse les coordonnées.", type = VariableType.BOOLEAN)
        private boolean annoncerCraft = true;

        @Var(name = "Annoncer les morts", desc = "Mourir diffuse les coordonnées du corps.", type = VariableType.BOOLEAN)
        private boolean annoncerMort = true;

        @Var(name = "Fréquence des alertes", desc = "Chance qu'une action surveillée diffuse réellement les coordonnées.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceAlerte = 100;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Paranoia"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.COMPASS); }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            if (!annoncerMinage || !MINERAIS.contains(block.getType())) return;
            annoncer(MINAGE, player, block.getLocation());
        }

        @Override
        public void onCraft(ItemStack result, CraftItemEvent event) {
            if (!isActive()) return;
            if (!annoncerCraft || result == null || !OBJETS_CLES.contains(result.getType())) return;
            if (!(event.getWhoClicked() instanceof Player player)) return;
            annoncer(CRAFT, player, player.getLocation());
        }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            if (!annoncerMort) return;
            Player player = event.getEntity();
            if (player == null) return;
            annoncer(MORT, player, player.getLocation());
        }

        protected String auteur(Player player) {
            return player.getName();
        }

        protected void annoncer(DynamicLang key, Player player, Location location) {
            if (!UHCUtils.Rng.chance(chanceAlerte)) return;
            LangManager.get().sendAll(key, Map.of(
                    "%joueur%", auteur(player),
                    "%x%", location.getBlockX(),
                    "%y%", location.getBlockY(),
                    "%z%", location.getBlockZ()));
        }
    }

    public static class ParafusionScenario extends ParanoiaScenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.parafusion.desc",
                "§7Comme Paranoia, mais les coordonnées sont diffusées sans nom : impossible de savoir qui vient de se trahir.");

        private static final DynamicLang ANONYME = DynamicLang.of("scenario.parafusion.anonyme",
                "§8???");

        @Override public String getName() { return "Parafusion"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.PAPER); }

        @Override
        protected String auteur(Player player) {
            return t(ANONYME, player);
        }
    }

    public static class PiggyBackScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.piggyback.desc",
                "§7Clic droit sur un coéquipier pour monter sur son dos. Accroupis-toi pour descendre.");

        @Var(name = "Coéquipiers uniquement", desc = "Seuls les membres de ta propre équipe peuvent être chevauchés.", type = VariableType.BOOLEAN)
        private boolean equipeUniquement = true;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Piggy Back"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SADDLE); }

        @Override
        public void onPlayerInteractEntity(Player player, PlayerInteractEntityEvent event) {
            if (!isActive()) return;
            if (!(event.getRightClicked() instanceof Player monture)) return;
            if (player.getVehicle() != null || monture.getPassenger() != null) return;

            UHCPlayer cavalier = UHCPlayerManager.get().getPlayer(player);
            UHCPlayer porteur = UHCPlayerManager.get().getPlayer(monture);
            if (cavalier == null || porteur == null) return;
            if (!cavalier.isPlaying() || !porteur.isPlaying()) return;
            if (equipeUniquement && (cavalier.getTeam().isEmpty() || porteur.getTeam().isEmpty()
                    || !cavalier.getTeam().get().equals(porteur.getTeam().get()))) return;

            event.setCancelled(true);
            monture.setPassenger(player);
        }
    }

    public static class RandomStarterItemsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.randomstarteritems.desc",
                "§7Chacun démarre avec §e%nombre% §7objets tirés au hasard, en quantités aléatoires.");

        private static final DynamicLang RECU = DynamicLang.of("scenario.randomstarteritems.recu",
                "§7Tu démarres avec §e%nombre% §7objets tirés au hasard.");

        private static final Material[] POOL = {
                Material.STONE_SWORD,
                Material.IRON_PICKAXE,
                Material.FISHING_ROD,
                Material.FLINT_AND_STEEL,
                Material.BOW,
                Material.ARROW,
                Material.LEATHER_CHESTPLATE,
                Material.IRON_INGOT,
                Material.GOLD_INGOT,
                Material.DIAMOND,
                Material.APPLE,
                Material.BREAD,
                Material.COOKED_BEEF,
                Material.GOLDEN_CARROT,
                Material.GOLDEN_APPLE,
                Material.LOG,
                Material.COBBLESTONE,
                Material.TORCH,
                Material.STICK,
                Material.STRING,
                Material.WEB,
                Material.FEATHER,
                Material.ENDER_PEARL,
                Material.EXP_BOTTLE,
                Material.WATER_BUCKET,
                Material.LAVA_BUCKET,
                Material.ROTTEN_FLESH,
                Material.OBSIDIAN
        };

        private final Random random = new Random();

        @Var(name = "Nombre d'objets", desc = "Nombre d'objets tirés au hasard donnés à chaque joueur au départ.", type = VariableType.INTEGER, min = 1)
        private int nombreItems = 9;

        @Var(name = "Quantité maximum", desc = "Quantité maximale tirée pour chaque objet de départ.", type = VariableType.INTEGER, min = 1)
        private int quantiteMax = 8;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Random Starter Items"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%nombre%", nombreItems));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.CHEST); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            for (int i = 0; i < nombreItems; i++) {
                Material material = POOL[random.nextInt(POOL.length)];
                int plafond = Math.max(1, Math.min(quantiteMax, material.getMaxStackSize()));
                player.getInventory().addItem(new ItemStack(material, 1 + random.nextInt(plafond)));
            }
            LangManager.get().send(RECU, player, Map.of("%nombre%", nombreItems));
        }
    }

    public static class RiskyRetrievalScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.riskyretrieval.desc",
                "§7L'or et les diamants ramassés partent directement dans un coffre au centre de la carte : à toi d'aller les chercher.");

        private static final DynamicLang ENVOYE = DynamicLang.of("scenario.riskyretrieval.envoye",
                "§7Ton butin précieux est parti au coffre en §e%x%, %z%§7.");

        private static final Set<Material> PRECIEUX = EnumSet.of(
                Material.GOLD_ORE,
                Material.GOLD_INGOT,
                Material.GOLD_BLOCK,
                Material.DIAMOND_ORE,
                Material.DIAMOND,
                Material.DIAMOND_BLOCK
        );

        private Location coffre;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Risky Retrieval"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ENDER_CHEST); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            World world = Common.get().getArena();
            if (world == null) return;

            Location centre = world.getWorldBorder().getCenter();
            int x = centre.getBlockX();
            int z = centre.getBlockZ();
            Block block = world.getBlockAt(x, world.getHighestBlockYAt(x, z), z);
            block.setTypeIdAndData(Material.CHEST.getId(), (byte) 0, false);
            coffre = block.getLocation();
        }

        @Override
        public void onPickUp(Player player, Item item, PlayerPickupItemEvent event) {
            if (!isActive()) return;
            if (coffre == null) return;
            ItemStack stack = item.getItemStack();
            if (stack == null || !PRECIEUX.contains(stack.getType())) return;

            event.setCancelled(true);
            item.remove();

            Location depot = coffre.clone().add(0.5D, 1.0D, 0.5D);
            if (coffre.getBlock().getState() instanceof Chest chest) {
                for (ItemStack reste : chest.getBlockInventory().addItem(stack).values()) {
                    depot.getWorld().dropItemNaturally(depot, reste);
                }
            } else {
                depot.getWorld().dropItemNaturally(depot, stack);
            }
            LangManager.get().send(ENVOYE, player, Map.of(
                    "%x%", coffre.getBlockX(),
                    "%z%", coffre.getBlockZ()));
        }

        @Override
        public void onStop() {
            coffre = null;
        }
    }

    public static class SafeLootScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.safeloot.desc",
                "§7Le stuff d'un mort tombe dans un coffre réservé à son tueur pendant §e%duree% §7seconde(s).");

        private static final DynamicLang RESERVE = DynamicLang.of("scenario.safeloot.reserve",
                "§7Le butin de ta victime t'attend dans un coffre verrouillé pendant §e%duree% §7seconde(s).");

        private static final DynamicLang VERROUILLE = DynamicLang.of("scenario.safeloot.verrouille",
                "§cCe coffre est réservé au tueur pendant encore §e%secondes% §cseconde(s).");

        private final Map<Location, UUID> proprietaires = new HashMap<>();

        private final Map<Location, Long> expirations = new HashMap<>();

        @Var(name = "Durée du verrou", desc = "Temps pendant lequel seul le tueur peut ouvrir le coffre du mort.", type = VariableType.TIME, min = 1)
        private int dureeVerrouSec = 60;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "SafeLoot"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%duree%", dureeVerrouSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.TRAPPED_CHEST); }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            Player mort = event.getEntity();
            if (mort == null) return;
            List<ItemStack> butin = new ArrayList<>(event.getDrops());
            if (butin.isEmpty()) return;

            event.getDrops().clear();
            Location tombe = mort.getLocation();
            Block block = tombe.getBlock();
            block.setTypeIdAndData(Material.CHEST.getId(), (byte) 0, false);
            if (!(block.getState() instanceof Chest chest)) {
                for (ItemStack drop : butin) tombe.getWorld().dropItemNaturally(tombe, drop);
                return;
            }

            for (ItemStack drop : butin) {
                for (ItemStack reste : chest.getBlockInventory().addItem(drop).values()) {
                    tombe.getWorld().dropItemNaturally(tombe, reste);
                }
            }
            if (killer == null) return;

            proprietaires.put(block.getLocation(), killer.getUniqueId());
            expirations.put(block.getLocation(), System.currentTimeMillis() + dureeVerrouSec * 1000L);
            Player tueur = killer.getPlayer();
            if (tueur != null) LangManager.get().send(RESERVE, tueur, Map.of("%duree%", dureeVerrouSec));
        }

        @Override
        public void onPlayerInteract(Player player, PlayerInteractEvent event) {
            if (!isActive()) return;
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
            Block block = event.getClickedBlock();
            if (block == null || block.getType() != Material.CHEST) return;

            Location cle = block.getLocation();
            UUID proprietaire = proprietaires.get(cle);
            if (proprietaire == null) return;

            long fin = expirations.getOrDefault(cle, 0L);
            long restant = fin - System.currentTimeMillis();
            if (restant <= 0) {
                proprietaires.remove(cle);
                expirations.remove(cle);
                return;
            }
            if (proprietaire.equals(player.getUniqueId())) return;

            event.setCancelled(true);
            DisplayService.actionBar(player, t(VERROUILLE, player, Map.of("%secondes%", (int) (restant / 1000L) + 1)));
        }

        @Override
        public void onStop() {
            proprietaires.clear();
            expirations.clear();
        }
    }

    public static class SchoolScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.school.desc",
                "§7Un professeur est tiré au sort : personne ne peut crafter un objet avant lui, sauf celui qui le tue.");

        private static final DynamicLang PROFESSEUR = DynamicLang.of("scenario.school.professeur",
                "§e%joueur% §7est le professeur : aucun élève ne peut crafter un objet avant lui.");

        private static final DynamicLang BLOQUE = DynamicLang.of("scenario.school.bloque",
                "§cLe professeur n'a pas encore crafté cet objet.");

        private static final DynamicLang DIPLOME = DynamicLang.of("scenario.school.diplome",
                "§aTu as tué le professeur : tu craftes désormais librement.");

        private final Set<Material> craftesParLeProfesseur = EnumSet.noneOf(Material.class);

        private final Set<UUID> diplomes = new HashSet<>();

        private final Random random = new Random();

        private UUID professeur;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "School"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BOOK); }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            if (professeur != null) return;
            List<UHCPlayer> vivants = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();
            if (vivants.isEmpty()) return;

            Player elu = vivants.get(random.nextInt(vivants.size())).getPlayer();
            if (elu == null) return;
            professeur = elu.getUniqueId();
            LangManager.get().sendAll(PROFESSEUR, Map.of("%joueur%", elu.getName()));
        }

        @Override
        public void onCraft(ItemStack result, CraftItemEvent event) {
            if (!isActive()) return;
            if (professeur == null || result == null) return;
            if (!(event.getWhoClicked() instanceof Player player)) return;

            if (player.getUniqueId().equals(professeur)) {
                craftesParLeProfesseur.add(result.getType());
                return;
            }
            if (diplomes.contains(player.getUniqueId())) return;
            if (craftesParLeProfesseur.contains(result.getType())) return;

            event.setCancelled(true);
            player.updateInventory();
            DisplayService.actionBar(player, t(BLOQUE, player));
        }

        @Override
        public void onKill(UHCPlayer killer, UHCPlayer victim) {
            if (!isActive()) return;
            if (killer == null || victim == null || professeur == null) return;
            if (!victim.getUniqueId().equals(professeur)) return;

            diplomes.add(killer.getUniqueId());
            Player tueur = killer.getPlayer();
            if (tueur != null) LangManager.get().send(DIPLOME, tueur);
        }

        @Override
        public void onStop() {
            professeur = null;
            craftesParLeProfesseur.clear();
            diplomes.clear();
        }
    }

    public static class SonarScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.sonar.desc",
                "§7Toutes les §e%min% §7à §e%max% §7secondes, on te donne la distance du joueur le plus proche.");

        private static final DynamicLang PING = DynamicLang.of("scenario.sonar.ping",
                "§bSonar §7: le joueur le plus proche est à §e%distance% §7blocs.");

        private static final DynamicLang SEUL = DynamicLang.of("scenario.sonar.seul",
                "§bSonar §7: aucun autre joueur détecté.");

        private final Map<UUID, Integer> comptes = new HashMap<>();

        private final Random random = new Random();

        @Var(name = "Délai minimum", desc = "Temps minimum entre deux relevés du sonar.", type = VariableType.TIME, min = 1)
        private int delaiMinSec = 60;

        @Var(name = "Délai maximum", desc = "Temps maximum entre deux relevés du sonar.", type = VariableType.TIME, min = 1)
        private int delaiMaxSec = 300;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Sonar"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%min%", delaiMinSec, "%max%", delaiMaxSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.REDSTONE_TORCH_ON); }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            UUID uuid = player.getUniqueId();
            Integer reste = comptes.get(uuid);
            if (reste == null || reste > 0) {
                comptes.put(uuid, reste == null ? tirage() : reste - 1);
                return;
            }
            comptes.put(uuid, tirage());

            double meilleure = -1;
            for (UHCPlayer autre : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player candidat = autre.getPlayer();
                if (candidat == null || candidat.getUniqueId().equals(uuid)) continue;
                if (!candidat.getWorld().equals(player.getWorld())) continue;
                double distance = candidat.getLocation().distanceSquared(player.getLocation());
                if (meilleure >= 0 && distance >= meilleure) continue;
                meilleure = distance;
            }
            if (meilleure < 0) {
                LangManager.get().send(SEUL, player);
                return;
            }
            LangManager.get().send(PING, player, Map.of("%distance%", (int) Math.sqrt(meilleure)));
        }

        @Override
        public void onStop() {
            comptes.clear();
        }

        private int tirage() {
            int min = Math.min(delaiMinSec, delaiMaxSec);
            int max = Math.max(delaiMinSec, delaiMaxSec);
            return min + random.nextInt(max - min + 1);
        }
    }

    public static class StockUpScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.stockup.desc",
                "§7Chaque mort offre §c%coeurs% cœur(s) vide(s) §7de vie maximum à tous les survivants.");

        private static final DynamicLang GAIN = DynamicLang.of("scenario.stockup.gain",
                "§7Une mort de plus : tout le monde gagne §c%coeurs% cœur(s) vide(s)§7.");

        @Var(name = "Cœurs par mort", desc = "Cœurs de vie maximum ajoutés à chaque survivant quand un joueur meurt.", type = VariableType.INTEGER, min = 1)
        private int coeursParMort = 1;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Stock Up"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%coeurs%", coeursParMort));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.APPLE); }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            for (UHCPlayer survivant : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player player = survivant.getPlayer();
                if (player == null) continue;
                player.setMaxHealth(player.getMaxHealth() + coeursParMort * 2.0);
            }
            LangManager.get().sendAll(GAIN, Map.of("%coeurs%", coeursParMort));
        }
    }

    public static class WishListScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.wishlist.desc",
                "§7Chacun reçoit §e%nombre% §7souhaits : ramasser l'objet demandé accorde un bonus de §e%duree% §7seconde(s).");

        private static final DynamicLang LISTE = DynamicLang.of("scenario.wishlist.liste",
                "§7Ta liste de souhaits : %objets%§7.");

        private static final DynamicLang EXAUCE = DynamicLang.of("scenario.wishlist.exauce",
                "§7Souhait exaucé : §e%objet% §7t'accorde un bonus.");

        private static final Material[] OBJETS = {
                Material.DIAMOND,
                Material.GOLDEN_APPLE,
                Material.ENDER_PEARL,
                Material.OBSIDIAN,
                Material.BLAZE_ROD,
                Material.FEATHER
        };

        private static final DynamicLang[] LIBELLES = {
                DynamicLang.of("scenario.wishlist.objet.diamant", "§bun diamant"),
                DynamicLang.of("scenario.wishlist.objet.pomme-doree", "§bune pomme dorée"),
                DynamicLang.of("scenario.wishlist.objet.perle", "§bune perle de l'Ender"),
                DynamicLang.of("scenario.wishlist.objet.obsidienne", "§bde l'obsidienne"),
                DynamicLang.of("scenario.wishlist.objet.baton-de-blaze", "§bun bâton de Blaze"),
                DynamicLang.of("scenario.wishlist.objet.plume", "§bune plume")
        };

        private static final PotionEffectType[] BONUS = {
                PotionEffectType.INCREASE_DAMAGE,
                PotionEffectType.REGENERATION,
                PotionEffectType.SPEED,
                PotionEffectType.DAMAGE_RESISTANCE,
                PotionEffectType.FIRE_RESISTANCE,
                PotionEffectType.JUMP
        };

        private final Map<UUID, Set<Integer>> souhaits = new HashMap<>();

        private final Random random = new Random();

        @Var(name = "Nombre de souhaits", desc = "Nombre d'objets tirés dans la liste de souhaits de chaque joueur.", type = VariableType.INTEGER, min = 1, max = 6)
        private int nombreSouhaits = 3;

        @Var(name = "Durée du bonus", desc = "Durée de l'effet accordé quand un souhait est exaucé.", type = VariableType.TIME, min = 1)
        private int dureeBonusSec = 120;

        @Var(name = "Niveau du bonus", desc = "Niveau de l'effet accordé quand un souhait est exaucé.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauBonus = 1;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Wish List"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%nombre%", nombreSouhaits, "%duree%", dureeBonusSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BOOK_AND_QUILL); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            Set<Integer> tirage = new LinkedHashSet<>();
            while (tirage.size() < Math.min(nombreSouhaits, OBJETS.length)) {
                tirage.add(random.nextInt(OBJETS.length));
            }
            souhaits.put(player.getUniqueId(), tirage);

            StringBuilder liste = new StringBuilder();
            for (int index : tirage) {
                if (liste.length() > 0) liste.append("§7, ");
                liste.append(t(LIBELLES[index], player));
            }
            LangManager.get().send(LISTE, player, Map.of("%objets%", liste.toString()));
        }

        @Override
        public void onPickUp(Player player, Item item, PlayerPickupItemEvent event) {
            if (!isActive()) return;
            Set<Integer> tirage = souhaits.get(player.getUniqueId());
            if (tirage == null || tirage.isEmpty()) return;
            ItemStack stack = item.getItemStack();
            if (stack == null) return;

            for (int index = 0; index < OBJETS.length; index++) {
                if (!tirage.contains(index) || OBJETS[index] != stack.getType()) continue;
                tirage.remove(index);
                player.addPotionEffect(new PotionEffect(BONUS[index], dureeBonusSec * 20, niveauBonus - 1, false, true), true);
                LangManager.get().send(EXAUCE, player, Map.of("%objet%", t(LIBELLES[index], player)));
                return;
            }
        }

        @Override
        public void onStop() {
            souhaits.clear();
        }
    }

    public static class YapperSirenScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.yappersiren.desc",
                "§7Parler dans le chat a §e%chance%% §7de chances de diffuser tes coordonnées à tout le monde.");

        private static final DynamicLang ALERTE = DynamicLang.of("scenario.yappersiren.alerte",
                "§c§lSIRÈNE §e%joueur% §7a trop parlé : §e%x%, %y%, %z%§7.");

        private final Random random = new Random();

        @Var(name = "Chance de sirène", desc = "Chance qu'un message de chat diffuse les coordonnées de son auteur.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chance = 10;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Yapper Siren"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%chance%", chance));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.NOTE_BLOCK); }

        @EventHandler(ignoreCancelled = true)
        public void onChat(AsyncPlayerChatEvent event) {
            if (!isRunning()) return;
            Player player = event.getPlayer();
            UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
            if (uhcPlayer == null || !uhcPlayer.isPlaying()) return;
            if (!UHCUtils.Rng.chance(chance)) return;

            Location location = player.getLocation();
            Bukkit.getScheduler().runTask(Main.get(), () -> LangManager.get().sendAll(ALERTE, Map.of(
                    "%joueur%", player.getName(),
                    "%x%", location.getBlockX(),
                    "%y%", location.getBlockY(),
                    "%z%", location.getBlockZ())));
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new ParanoiaScenario(),
                new ParafusionScenario(),
                new PiggyBackScenario(),
                new RandomStarterItemsScenario(),
                new RiskyRetrievalScenario(),
                new SafeLootScenario(),
                new SchoolScenario(),
                new SonarScenario(),
                new StockUpScenario(),
                new WishListScenario(),
                new YapperSirenScenario()
        );
    }
}
