package net.novaproject.scenarioplus.thematique;

import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Bat;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class ThematiqueScenarios {

    private static final Random RANDOM = new Random();

    private ThematiqueScenarios() {
    }

    private static boolean estLeMoment(int intervalleSec) {
        int timer = UHCManager.get().getTimer();
        return timer > 0 && timer % Math.max(1, intervalleSec) == 0;
    }

    public static class HalloweenScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.halloween.desc",
                "§7Toutes les §e%intervalle% §7secondes la foudre peut tomber près de toi et lâcher §5%chauves% chauve(s)-souris§7. Les araignées naissent venimeuses et §5%sorciere%% §7des monstres sont des sorcières.");

        private static final DynamicLang FOUDRE = DynamicLang.of("scenario.halloween.foudre",
                "§5Un éclair vient de déchirer le ciel juste à côté de toi.");

        @Var(name = "Intervalle de foudre", desc = "Temps entre deux tirages de foudre pour chaque joueur.", type = VariableType.TIME, min = 1)
        private int intervalleSec = 120;

        @Var(name = "Chance de foudre", desc = "Chance qu'un éclair tombe près d'un joueur à chaque tirage.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceFoudre = 25;

        @Var(name = "Rayon de foudre", desc = "Distance maximale, en blocs, entre le joueur et le point d'impact.", type = VariableType.INTEGER, min = 1)
        private int rayonFoudre = 15;

        @Var(name = "Chauves-souris libérées", desc = "Nombre de chauves-souris apparaissant au point d'impact.", type = VariableType.INTEGER, min = 1)
        private int chauvesSouris = 3;

        @Var(name = "Chance de sorcière", desc = "Chance qu'un monstre naturel apparaisse en sorcière à la place.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceSorciere = 25;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Halloween"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of(
                    "%intervalle%", intervalleSec,
                    "%chauves%", chauvesSouris,
                    "%sorciere%", chanceSorciere));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.JACK_O_LANTERN); }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            if (!estLeMoment(intervalleSec)) return;
            if (!UHCUtils.Rng.chance(chanceFoudre)) return;

            World world = player.getWorld();
            int x = player.getLocation().getBlockX() + RANDOM.nextInt(rayonFoudre * 2 + 1) - rayonFoudre;
            int z = player.getLocation().getBlockZ() + RANDOM.nextInt(rayonFoudre * 2 + 1) - rayonFoudre;
            Location impact = new Location(world, x + 0.5D, Math.max(world.getHighestBlockYAt(x, z), 1), z + 0.5D);

            world.strikeLightningEffect(impact);
            for (int i = 0; i < chauvesSouris; i++) {
                world.spawn(impact, Bat.class);
            }
            LangManager.get().send(FOUDRE, player);
        }

        @EventHandler
        public void onSpawn(CreatureSpawnEvent event) {
            if (!isRunning()) return;
            if (event.getSpawnReason() != SpawnReason.NATURAL) return;

            Entity entity = event.getEntity();
            Location lieu = entity.getLocation();

            if (entity instanceof Spider && !(entity instanceof CaveSpider)) {
                event.setCancelled(true);
                lieu.getWorld().spawn(lieu, CaveSpider.class);
                return;
            }
            if (!(entity instanceof Monster) || entity instanceof Witch) return;
            if (!UHCUtils.Rng.chance(chanceSorciere)) return;

            event.setCancelled(true);
            lieu.getWorld().spawn(lieu, Witch.class);
        }
    }

    public static class KrenzinatorScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.krenzinator.desc",
                "§7§c%redstone% blocs de redstone §7se craftent en §bun diamant§7, l'épée en diamant coûte §c%coeurs% cœur(s) §7et un œuf lancé inflige §c%oeuf% cœur(s)§7.");

        private static final DynamicLang PRIX_EPEE = DynamicLang.of("scenario.krenzinator.prix-epee",
                "§cForger cette épée t'a coûté du sang.");

        @Var(name = "Redstone par diamant", desc = "Nombre de blocs de redstone nécessaires pour crafter un diamant.", type = VariableType.INTEGER, min = 1, max = 9)
        private int redstoneParDiamant = 9;

        @Var(name = "Coût de l'épée", desc = "Nombre de cœurs perdus en craftant une épée en diamant.", type = VariableType.DOUBLE, min = 0, max = 10)
        private double coeursEpee = 1.0D;

        @Var(name = "Dégâts de l'œuf", desc = "Nombre de cœurs infligés par un œuf lancé sur un joueur.", type = VariableType.DOUBLE, min = 0, max = 10)
        private double coeursOeuf = 0.5D;

        @Override
        public Family getFamily() { return Family.CRAFT; }

        @Override public String getName() { return "Krenzinator"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of(
                    "%redstone%", redstoneParDiamant,
                    "%coeurs%", coeursEpee,
                    "%oeuf%", coeursOeuf));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.REDSTONE_BLOCK); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            ShapelessRecipe diamant = new ShapelessRecipe(new ItemStack(Material.DIAMOND));
            diamant.addIngredient(redstoneParDiamant, Material.REDSTONE_BLOCK);
            Bukkit.addRecipe(diamant);
        }

        @Override
        public void onCraft(ItemStack result, CraftItemEvent event) {
            if (!isActive()) return;
            if (result == null || result.getType() != Material.DIAMOND_SWORD) return;
            if (!(event.getWhoClicked() instanceof Player player)) return;
            if (coeursEpee <= 0) return;

            player.damage(coeursEpee * 2.0D);
            LangManager.get().send(PRIX_EPEE, player);
        }

        @Override
        public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (!(entity instanceof Player) || !(damager instanceof Egg)) return;
            event.setDamage(coeursOeuf * 2.0D);
        }
    }

    public static class MistasBirthdayScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.mistasbirthday.desc",
                "§7Les explosions ne cassent plus rien : elles partent en feu d'artifice, avec §e%chance%% §7de chances de lâcher §d%gateaux% gâteau(x)§7.");

        @Var(name = "Chance de gâteau", desc = "Chance qu'une explosion laisse un gâteau derrière elle.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceGateau = 50;

        @Var(name = "Gâteaux lâchés", desc = "Nombre de gâteaux lâchés quand le tirage est gagnant.", type = VariableType.INTEGER, min = 1)
        private int gateaux = 1;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Mista's Birthday"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%chance%", chanceGateau, "%gateaux%", gateaux));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.FIREWORK); }

        @Override
        public void onEntityExplode(Entity entity, EntityExplodeEvent event) {
            if (!isActive()) return;

            Location lieu = event.getLocation();
            World world = lieu.getWorld();
            if (world == null) return;

            event.blockList().clear();

            Firework fusee = world.spawn(lieu, Firework.class);
            FireworkMeta meta = fusee.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder()
                    .withColor(Color.RED, Color.YELLOW)
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .withFlicker()
                    .build());
            meta.setPower(1);
            fusee.setFireworkMeta(meta);

            if (!UHCUtils.Rng.chance(chanceGateau)) return;
            world.dropItemNaturally(lieu, new ItemStack(Material.CAKE, gateaux));
        }
    }

    public static class NaughtyOrNiceScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.naughtyornice.desc",
                "§7Toutes les §e%intervalle% §7secondes tu reçois un cadeau : au clic droit §7dans le vide, §a%chance%% §7de chances d'y trouver du bon, sinon tu le regrettes.");

        private static final DynamicLang RECU = DynamicLang.of("scenario.naughtyornice.recu",
                "§aUn cadeau vient d'atterrir dans ton inventaire : clic droit dans le vide pour l'ouvrir.");

        private static final DynamicLang GENTIL = DynamicLang.of("scenario.naughtyornice.gentil",
                "§aTu as été sage cette année : le cadeau tient ses promesses.");

        private static final DynamicLang MECHANT = DynamicLang.of("scenario.naughtyornice.mechant",
                "§cTu as été vilain cette année : le cadeau te le fait payer.");

        @Var(name = "Intervalle de cadeau", desc = "Temps entre deux cadeaux distribués à chaque joueur.", type = VariableType.TIME, min = 1)
        private int intervalleSec = 300;

        @Var(name = "Chance de bon cadeau", desc = "Chance que l'ouverture du cadeau donne une récompense.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceGentil = 50;

        @Var(name = "Diamants du cadeau", desc = "Nombre de diamants donnés par un bon cadeau.", type = VariableType.INTEGER, min = 1)
        private int diamants = 2;

        @Var(name = "Pommes d'or du cadeau", desc = "Nombre de pommes d'or données par un bon cadeau.", type = VariableType.INTEGER, min = 1)
        private int pommesDor = 1;

        @Var(name = "Durée du malus", desc = "Durée de l'effet infligé par un mauvais cadeau.", type = VariableType.TIME, min = 1)
        private int dureeMalusSec = 20;

        @Var(name = "Dégâts du malus", desc = "Dégâts infligés par un mauvais cadeau explosif.", type = VariableType.DOUBLE, min = 0, max = 40)
        private double degatsMalus = 4.0D;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Naughty or Nice"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%intervalle%", intervalleSec, "%chance%", chanceGentil));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.CHEST); }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            if (!estLeMoment(intervalleSec)) return;

            player.getInventory().addItem(new ItemStack(Material.CHEST, 1));
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 1f);
            LangManager.get().send(RECU, player);
        }

        @Override
        public void onPlayerInteract(Player player, PlayerInteractEvent event) {
            if (!isActive()) return;
            if (event.getAction() != Action.RIGHT_CLICK_AIR) return;

            ItemStack main = player.getItemInHand();
            if (main == null || main.getType() != Material.CHEST) return;

            event.setCancelled(true);
            ItemCreator.consumeOne(player, main);

            if (UHCUtils.Rng.chance(chanceGentil)) {
                if (RANDOM.nextBoolean()) player.getInventory().addItem(new ItemStack(Material.DIAMOND, diamants));
                else player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, pommesDor));
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1f);
                LangManager.get().send(GENTIL, player);
                return;
            }

            if (RANDOM.nextBoolean()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, dureeMalusSec * 20, 0), true);
            } else {
                player.damage(degatsMalus);
            }
            player.playSound(player.getLocation(), Sound.ANVIL_LAND, 1f, 1f);
            LangManager.get().send(MECHANT, player);
        }
    }

    public static class TrickOrTreatScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.trickortreat.desc",
                "§7Toutes les §e%intervalle% §7secondes tu reçois une citrouille : au clic droit §7dans le vide, §a%chance%% §7de chances d'y trouver une friandise, sinon c'est un mauvais tour.");

        private static final DynamicLang RECUE = DynamicLang.of("scenario.trickortreat.recue",
                "§6Une citrouille vient d'atterrir dans ton inventaire : clic droit dans le vide pour l'ouvrir.");

        private static final DynamicLang FRIANDISE = DynamicLang.of("scenario.trickortreat.friandise",
                "§aFriandise : de quoi tenir le ventre plein.");

        private static final DynamicLang MAUVAIS_TOUR = DynamicLang.of("scenario.trickortreat.mauvais-tour",
                "§cMauvais tour : la citrouille se retourne contre toi.");

        @Var(name = "Intervalle de citrouille", desc = "Temps entre deux citrouilles distribuées à chaque joueur.", type = VariableType.TIME, min = 1)
        private int intervalleSec = 420;

        @Var(name = "Chance de friandise", desc = "Chance que la citrouille donne une friandise plutôt qu'un mauvais tour.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceFriandise = 50;

        @Var(name = "Cookies donnés", desc = "Nombre de cookies donnés par une friandise.", type = VariableType.INTEGER, min = 1)
        private int cookies = 4;

        @Var(name = "Steaks donnés", desc = "Nombre de steaks cuits donnés par une friandise.", type = VariableType.INTEGER, min = 1)
        private int steaks = 2;

        @Var(name = "Durée du mauvais tour", desc = "Durée des effets infligés par un mauvais tour.", type = VariableType.TIME, min = 1)
        private int dureeMauvaisTourSec = 15;

        @Var(name = "Dégâts du mauvais tour", desc = "Dégâts infligés par un mauvais tour brutal.", type = VariableType.DOUBLE, min = 0, max = 40)
        private double degatsMauvaisTour = 2.0D;

        @Var(name = "Niveau de lenteur", desc = "Niveau de l'effet de Lenteur infligé par un mauvais tour.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauLenteur = 2;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Trick Or Treat"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%intervalle%", intervalleSec, "%chance%", chanceFriandise));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.PUMPKIN); }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            if (!estLeMoment(intervalleSec)) return;

            player.getInventory().addItem(new ItemStack(Material.PUMPKIN, 1));
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 1f);
            LangManager.get().send(RECUE, player);
        }

        @Override
        public void onPlayerInteract(Player player, PlayerInteractEvent event) {
            if (!isActive()) return;
            if (event.getAction() != Action.RIGHT_CLICK_AIR) return;

            ItemStack main = player.getItemInHand();
            if (main == null || main.getType() != Material.PUMPKIN) return;

            event.setCancelled(true);
            ItemCreator.consumeOne(player, main);

            if (UHCUtils.Rng.chance(chanceFriandise)) {
                if (RANDOM.nextBoolean()) player.getInventory().addItem(new ItemStack(Material.COOKIE, cookies));
                else player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, steaks));
                LangManager.get().send(FRIANDISE, player);
                return;
            }

            if (RANDOM.nextBoolean()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, dureeMauvaisTourSec * 20, 0), true);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, dureeMauvaisTourSec * 20, niveauLenteur - 1), true);
            } else {
                player.damage(degatsMauvaisTour);
            }
            LangManager.get().send(MAUVAIS_TOUR, player);
        }
    }

    public static class SantasGiftScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.santasgift.desc",
                "§7Un seau de lait et un cookie se craftent en pomme d'or, accompagnée de §b%diamants% diamant(s) §7et §6%or% lingot(s) d'or§7.");

        private static final DynamicLang CADEAU = DynamicLang.of("scenario.santasgift.cadeau",
                "§aLe père Noël est passé : tu récupères plus que prévu.");

        @Var(name = "Diamants du cadeau", desc = "Nombre de diamants offerts en plus de la pomme d'or.", type = VariableType.INTEGER, min = 0)
        private int diamants = 2;

        @Var(name = "Lingots d'or du cadeau", desc = "Nombre de lingots d'or offerts en plus de la pomme d'or.", type = VariableType.INTEGER, min = 0)
        private int lingotsOr = 4;

        @Override
        public Family getFamily() { return Family.CRAFT; }

        @Override public String getName() { return "Santa's Gift"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%diamants%", diamants, "%or%", lingotsOr));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.MILK_BUCKET); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            ShapelessRecipe cadeau = new ShapelessRecipe(new ItemStack(Material.GOLDEN_APPLE));
            cadeau.addIngredient(Material.MILK_BUCKET);
            cadeau.addIngredient(Material.COOKIE);
            Bukkit.addRecipe(cadeau);
        }

        @Override
        public void onCraft(ItemStack result, CraftItemEvent event) {
            if (!isActive()) return;
            if (result == null || result.getType() != Material.GOLDEN_APPLE) return;
            if (!(event.getWhoClicked() instanceof Player player)) return;

            boolean lait = false;
            boolean cookie = false;
            for (ItemStack item : event.getInventory().getMatrix()) {
                if (item == null) continue;
                if (item.getType() == Material.MILK_BUCKET) lait = true;
                if (item.getType() == Material.COOKIE) cookie = true;
            }
            if (!lait || !cookie) return;

            if (diamants > 0) player.getInventory().addItem(new ItemStack(Material.DIAMOND, diamants));
            if (lingotsOr > 0) player.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, lingotsOr));
            LangManager.get().send(CADEAU, player);
        }
    }

    public static class SantasHelperScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.santashelper.desc",
                "§7Toutes les §e%intervalle% §7secondes on te confie un objet à rapporter dans le coffre en §e%x%, %z%§7. Livraison réussie : §b%diamants% diamant(s) §7et §6%or% lingot(s) d'or§7.");

        private static final DynamicLang COFFRE_POSE = DynamicLang.of("scenario.santashelper.coffre-pose",
                "§7Le coffre de livraison vous attend en §e%x%, %z%§7.");

        private static final DynamicLang MISSION = DynamicLang.of("scenario.santashelper.mission",
                "§7Nouvelle livraison à effectuer : §e%objet%§7.");

        private static final DynamicLang LIVRE = DynamicLang.of("scenario.santashelper.livre",
                "§aLivraison acceptée : voilà ta récompense.");

        private static final Material[] OBJETS = {
                Material.DIAMOND,
                Material.GOLD_INGOT,
                Material.IRON_INGOT,
                Material.STRING,
                Material.FEATHER,
                Material.COOKED_BEEF
        };

        private static final DynamicLang[] NOMS = {
                DynamicLang.of("scenario.santashelper.objet.diamant", "§bun diamant"),
                DynamicLang.of("scenario.santashelper.objet.or", "§6un lingot d'or"),
                DynamicLang.of("scenario.santashelper.objet.fer", "§fun lingot de fer"),
                DynamicLang.of("scenario.santashelper.objet.fil", "§fun fil"),
                DynamicLang.of("scenario.santashelper.objet.plume", "§fune plume"),
                DynamicLang.of("scenario.santashelper.objet.steak", "§eun steak cuit")
        };

        private final Map<UUID, Integer> missions = new HashMap<>();

        @Var(name = "Intervalle de livraison", desc = "Temps entre deux objets à rapporter assignés à chaque joueur.", type = VariableType.TIME, min = 1)
        private int intervalleSec = 900;

        @Var(name = "Coffre : X", desc = "Coordonnée X du coffre de livraison posé au début de la partie.", type = VariableType.INTEGER)
        private int coffreX = 250;

        @Var(name = "Coffre : Z", desc = "Coordonnée Z du coffre de livraison posé au début de la partie.", type = VariableType.INTEGER)
        private int coffreZ = 250;

        @Var(name = "Diamants de récompense", desc = "Nombre de diamants donnés pour une livraison réussie.", type = VariableType.INTEGER, min = 0)
        private int diamants = 2;

        @Var(name = "Lingots d'or de récompense", desc = "Nombre de lingots d'or donnés pour une livraison réussie.", type = VariableType.INTEGER, min = 0)
        private int lingotsOr = 4;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Santa's Helper"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of(
                    "%intervalle%", intervalleSec,
                    "%x%", coffreX,
                    "%z%", coffreZ,
                    "%diamants%", diamants,
                    "%or%", lingotsOr));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.PAPER); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            World world = Common.get().getArena();
            if (world == null) return;

            int y = Math.max(world.getHighestBlockYAt(coffreX, coffreZ), 1);
            world.getBlockAt(coffreX, y, coffreZ).setType(Material.CHEST);
            LangManager.get().sendAll(COFFRE_POSE, Map.of("%x%", coffreX, "%z%", coffreZ));
        }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            if (!estLeMoment(intervalleSec)) return;

            int index = RANDOM.nextInt(OBJETS.length);
            missions.put(player.getUniqueId(), index);
            LangManager.get().send(MISSION, player, Map.of("%objet%", t(NOMS[index], player)));
        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            if (!isRunning()) return;
            if (!(event.getPlayer() instanceof Player player)) return;

            Integer index = missions.get(player.getUniqueId());
            if (index == null) return;

            Inventory inventaire = event.getInventory();
            if (!(inventaire.getHolder() instanceof Chest coffre)) return;

            Location lieu = coffre.getLocation();
            if (lieu.getBlockX() != coffreX || lieu.getBlockZ() != coffreZ) return;

            Material cible = OBJETS[index];
            if (!inventaire.contains(cible)) return;

            inventaire.removeItem(new ItemStack(cible, 1));
            missions.remove(player.getUniqueId());
            if (diamants > 0) player.getInventory().addItem(new ItemStack(Material.DIAMOND, diamants));
            if (lingotsOr > 0) player.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, lingotsOr));
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1f);
            LangManager.get().send(LIVRE, player);
        }

        @Override
        public void onStop() {
            missions.clear();
        }
    }

    public static class GrinchScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.grinch.desc",
                "§7À minuit, un zombie chargé de cadeaux apparaît au centre de la bordure. Le tuer rapporte §b%diamants% diamant(s) §7et §6%pommes% pomme(s) d'or§7.");

        private static final DynamicLang NOM = DynamicLang.of("scenario.grinch.nom",
                "§2Le Grinch");

        private static final DynamicLang APPARITION = DynamicLang.of("scenario.grinch.apparition",
                "§2Le Grinch vient d'apparaître au centre de la carte avec ses cadeaux.");

        private static final DynamicLang TUE = DynamicLang.of("scenario.grinch.tue",
                "§aTu as récupéré les cadeaux du Grinch.");

        private final Set<UUID> grinchs = new HashSet<>();

        private long dernierJour = -1L;

        @Var(name = "Vie du Grinch", desc = "Points de vie du zombie apparaissant à minuit.", type = VariableType.DOUBLE, min = 1, max = 200)
        private double vie = 40.0D;

        @Var(name = "Diamants de récompense", desc = "Nombre de diamants lâchés par le Grinch à sa mort.", type = VariableType.INTEGER, min = 0)
        private int diamants = 3;

        @Var(name = "Pommes d'or de récompense", desc = "Nombre de pommes d'or lâchées par le Grinch à sa mort.", type = VariableType.INTEGER, min = 0)
        private int pommesDor = 2;

        @Override
        public Family getFamily() { return Family.MOBS; }

        @Override public String getName() { return "Grinch"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%diamants%", diamants, "%pommes%", pommesDor));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.MONSTER_EGG); }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            World world = Common.get().getArena();
            if (world == null) return;

            long jour = world.getFullTime() / 24000L;
            if (jour == dernierJour) return;
            long heure = world.getTime();
            if (heure < 18000L || heure >= 19000L) return;
            dernierJour = jour;

            Location centre = world.getWorldBorder().getCenter();
            int x = centre.getBlockX();
            int z = centre.getBlockZ();
            Location apparition = new Location(world, x + 0.5D, Math.max(world.getHighestBlockYAt(x, z), 1), z + 0.5D);

            Zombie grinch = world.spawn(apparition, Zombie.class);
            grinch.setCustomName(t(NOM));
            grinch.setCustomNameVisible(true);
            grinch.setMaxHealth(vie);
            grinch.setHealth(vie);
            grinch.setRemoveWhenFarAway(false);
            grinch.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
            grinch.getEquipment().setHelmetDropChance(0f);
            grinchs.add(grinch.getUniqueId());

            LangManager.get().sendAll(APPARITION);
        }

        @Override
        public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
            if (!isActive()) return;
            if (!grinchs.remove(entity.getUniqueId())) return;

            if (diamants > 0) event.getDrops().add(new ItemStack(Material.DIAMOND, diamants));
            if (pommesDor > 0) event.getDrops().add(new ItemStack(Material.GOLDEN_APPLE, pommesDor));
            if (killer != null) LangManager.get().send(TUE, killer);
        }

        @Override
        public void onStop() {
            grinchs.clear();
            dernierJour = -1L;
        }
    }

    public static class BestBTCScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.bestbtc.desc",
                "§7Rester §e%duree% §7secondes sous §eY=%seuil% §7rapporte §c%coeurs% cœur(s)§7. Remonter te sort de la liste : mine un diamant pour y revenir.");

        private static final DynamicLang GAIN = DynamicLang.of("scenario.bestbtc.gain",
                "§cTu gagnes %coeurs% cœur(s) §7pour ta patience sous terre.");

        private static final DynamicLang SORTI = DynamicLang.of("scenario.bestbtc.sorti",
                "§cTu es remonté trop haut : te voilà hors de la liste.");

        private static final DynamicLang RETOUR = DynamicLang.of("scenario.bestbtc.retour",
                "§aCe diamant te remet sur la liste.");

        private final Map<UUID, Integer> secondes = new HashMap<>();

        private final Set<UUID> dehors = new HashSet<>();

        private final Map<UUID, Double> vieBase = new HashMap<>();

        @Var(name = "Altitude limite", desc = "Altitude sous laquelle le compteur avance.", type = VariableType.INTEGER, min = 0, max = 256)
        private int seuilY = 50;

        @Var(name = "Durée requise", desc = "Temps passé sous l'altitude limite avant de gagner des cœurs.", type = VariableType.TIME, min = 1)
        private int dureeSec = 600;

        @Var(name = "Cœurs gagnés", desc = "Nombre de cœurs de vie maximale ajoutés à chaque palier atteint.", type = VariableType.INTEGER, min = 1)
        private int coeursGagnes = 1;

        @Override
        public Family getFamily() { return Family.MINAGE; }

        @Override public String getName() { return "BestBTC"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%duree%", dureeSec, "%seuil%", seuilY, "%coeurs%", coeursGagnes));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.DIAMOND_ORE); }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            UUID id = player.getUniqueId();

            if (player.getLocation().getBlockY() >= seuilY) {
                boolean comptait = secondes.remove(id) != null;
                if (dehors.add(id) && comptait) LangManager.get().send(SORTI, player);
                return;
            }
            if (dehors.contains(id)) return;

            int cumul = secondes.getOrDefault(id, 0) + 1;
            if (cumul < dureeSec) {
                secondes.put(id, cumul);
                return;
            }

            secondes.put(id, 0);
            vieBase.putIfAbsent(id, player.getMaxHealth());
            player.setMaxHealth(player.getMaxHealth() + coeursGagnes * 2.0D);
            LangManager.get().send(GAIN, player, Map.of("%coeurs%", coeursGagnes));
        }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            if (block.getType() != Material.DIAMOND_ORE) return;
            if (!dehors.remove(player.getUniqueId())) return;
            LangManager.get().send(RETOUR, player);
        }

        @Override
        public void onStop() {
            for (Map.Entry<UUID, Double> entree : vieBase.entrySet()) {
                Player player = Bukkit.getPlayer(entree.getKey());
                if (player != null) player.setMaxHealth(entree.getValue());
            }
            vieBase.clear();
            secondes.clear();
            dehors.clear();
        }
    }

    public static class TommySXScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.tommysx.desc",
                "§7Les creepers infligent §c×%multiplicateur% §7de dégâts, les pelles en diamant sortent du craft avec §eEfficacité %efficacite% §7et les épées et arcs craftés s'appellent §e%nom%§7.");

        private static final DynamicLang NOM_ARME = DynamicLang.of("scenario.tommysx.nom-arme",
                "XDDDDDDDDDDE");

        @Var(name = "Multiplicateur creeper", desc = "Facteur appliqué aux dégâts infligés par un creeper.", type = VariableType.DOUBLE, min = 1, max = 20)
        private double multiplicateurCreeper = 2.0D;

        @Var(name = "Efficacité de la pelle", desc = "Niveau d'Efficacité appliqué aux pelles en diamant craftées.", type = VariableType.INTEGER, min = 1, max = 10)
        private int efficacitePelle = 5;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "TommySX"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of(
                    "%multiplicateur%", multiplicateurCreeper,
                    "%efficacite%", efficacitePelle,
                    "%nom%", t(NOM_ARME, player)));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.DIAMOND_SPADE); }

        @Override
        public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (!(entity instanceof Player) || !(damager instanceof Creeper)) return;
            event.setDamage(event.getDamage() * multiplicateurCreeper);
        }

        @Override
        public void onCraft(ItemStack result, CraftItemEvent event) {
            if (!isActive()) return;
            if (result == null) return;

            if (result.getType() == Material.DIAMOND_SPADE) {
                result.addUnsafeEnchantment(Enchantment.DIG_SPEED, efficacitePelle);
                event.getInventory().setResult(result);
                return;
            }
            if (result.getType() != Material.BOW && !result.getType().name().endsWith("_SWORD")) return;

            ItemMeta meta = result.getItemMeta();
            meta.setDisplayName(t(NOM_ARME));
            result.setItemMeta(meta);
            event.getInventory().setResult(result);
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new HalloweenScenario(),
                new KrenzinatorScenario(),
                new MistasBirthdayScenario(),
                new NaughtyOrNiceScenario(),
                new TrickOrTreatScenario(),
                new SantasGiftScenario(),
                new SantasHelperScenario(),
                new GrinchScenario(),
                new BestBTCScenario(),
                new TommySXScenario()
        );
    }
}
