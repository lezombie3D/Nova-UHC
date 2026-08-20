package net.novaproject.scenarioplus.monde;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.event.UhcGameEvents.UhcScatterStartEvent;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.scenarioplus.util.RisingFluid;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class StructureScenarios {

    private static final Random RANDOM = new Random();

    private static final Set<Material> PORTES = EnumSet.of(
            Material.WOODEN_DOOR,
            Material.IRON_DOOR_BLOCK,
            Material.SPRUCE_DOOR,
            Material.BIRCH_DOOR,
            Material.JUNGLE_DOOR,
            Material.ACACIA_DOOR,
            Material.DARK_OAK_DOOR
    );

    private StructureScenarios() {
    }

    private static Location basDePorte(Block block) {
        if ((block.getData() & 8) != 0) return block.getRelative(0, -1, 0).getLocation();
        return block.getLocation();
    }

    public static class MeteorsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.meteors.desc",
                "§7La nuit est éternelle. Après §e%delai% §7secondes, des météores s'écrasent sur la carte en laissant un coffre de butin.");

        private static final DynamicLang DEBUT = DynamicLang.of("scenario.meteors.debut",
                "§c● §7Le ciel se déchire : les premiers météores tombent.");

        private static final DynamicLang IMPACT = DynamicLang.of("scenario.meteors.impact",
                "§6● §7Un météore vient de s'écraser en §e%x%§7, §e%z%§7.");

        @Var(name = "Délai avant la pluie", desc = "Secondes de jeu avant la chute du premier météore.", type = VariableType.TIME, min = 1)
        private int delaiSec = 600;

        @Var(name = "Intervalle des vagues", desc = "Secondes entre deux vagues de météores.", type = VariableType.TIME, min = 1)
        private int intervalleSec = 120;

        @Var(name = "Météores par vague", desc = "Nombre de météores lâchés à chaque vague.", type = VariableType.INTEGER, min = 1)
        private int meteoresParVague = 2;

        @Var(name = "Hauteur de chute", desc = "Blocs au-dessus du joueur visé où naît le météore.", type = VariableType.INTEGER, min = 5)
        private int hauteurChute = 60;

        @Var(name = "Dispersion", desc = "Rayon en blocs autour du joueur visé pour le point de chute.", type = VariableType.INTEGER, min = 0)
        private int dispersion = 40;

        @Var(name = "Puissance d'impact", desc = "Puissance de l'explosion à l'atterrissage du météore.", type = VariableType.DOUBLE, min = 0)
        private double puissanceImpact = 4.0;

        @Var(name = "Casse les blocs", desc = "L'impact du météore détruit-il le terrain.", type = VariableType.BOOLEAN)
        private boolean casseBlocs = true;

        @Var(name = "Heure de la nuit", desc = "Heure du monde imposée par la nuit éternelle.", type = VariableType.INTEGER, min = 0, max = 24000)
        private int heureNuit = 18000;

        @Var(name = "Diamants du coffre", desc = "Diamants déposés dans le coffre du cratère.", type = VariableType.INTEGER, min = 0)
        private int diamants = 2;

        @Var(name = "Pommes d'or du coffre", desc = "Pommes d'or déposées dans le coffre du cratère.", type = VariableType.INTEGER, min = 0)
        private int pommesDor = 2;

        @Var(name = "Lingots d'or du coffre", desc = "Lingots d'or déposés dans le coffre du cratère.", type = VariableType.INTEGER, min = 0)
        private int lingotsOr = 8;

        @Var(name = "Lingots de fer du coffre", desc = "Lingots de fer déposés dans le coffre du cratère.", type = VariableType.INTEGER, min = 0)
        private int lingotsFer = 12;

        private final List<FallingBlock> enVol = new ArrayList<>();
        private int compteur = 0;
        private boolean annonce = false;
        private BukkitRunnable task;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Meteors"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%delai%", delaiSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.FIREBALL); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            compteur = 0;
            annonce = false;

            World world = Common.get().getArena();
            if (world != null) {
                world.setGameRuleValue("doDaylightCycle", "false");
                world.setTime(heureNuit);
            }

            if (task != null) task.cancel();
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isActive()) { cancel(); return; }
                    World arena = Common.get().getArena();
                    if (arena != null && arena.getTime() != heureNuit) arena.setTime(heureNuit);
                    ramasserImpacts();
                    if (UHCManager.get().getTimer() < delaiSec) return;
                    if (!annonce) {
                        annonce = true;
                        LangManager.get().sendAll(DEBUT);
                        lancerVague();
                        return;
                    }
                    compteur++;
                    if (compteur < intervalleSec * 4) return;
                    compteur = 0;
                    lancerVague();
                }
            };
            task.runTaskTimer(Main.get(), 5L, 5L);
        }

        private void lancerVague() {
            List<UHCPlayer> cibles = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();
            if (cibles.isEmpty()) return;

            for (int i = 0; i < meteoresParVague; i++) {
                Player cible = cibles.get(RANDOM.nextInt(cibles.size())).getPlayer();
                if (cible == null) continue;
                World world = cible.getWorld();
                double dx = dispersion == 0 ? 0 : RANDOM.nextInt(dispersion * 2 + 1) - dispersion;
                double dz = dispersion == 0 ? 0 : RANDOM.nextInt(dispersion * 2 + 1) - dispersion;
                Location depart = cible.getLocation().add(dx, hauteurChute, dz);
                if (depart.getY() > 254) depart.setY(254);

                FallingBlock meteore = world.spawnFallingBlock(depart, Material.NETHERRACK, (byte) 0);
                meteore.setDropItem(false);
                enVol.add(meteore);
                cible.playSound(cible.getLocation(), Sound.GHAST_SCREAM, 1.0f, 0.6f);
            }
        }

        private void ramasserImpacts() {
            List<FallingBlock> tombes = new ArrayList<>();
            for (FallingBlock meteore : enVol) {
                if (meteore.isDead() || meteore.isOnGround()) tombes.add(meteore);
            }
            for (FallingBlock meteore : tombes) {
                enVol.remove(meteore);
                creuserCratere(meteore.getLocation());
                meteore.remove();
            }
        }

        private void creuserCratere(Location loc) {
            World world = loc.getWorld();
            if (world == null) return;

            world.createExplosion(loc.getX(), loc.getY(), loc.getZ(), (float) puissanceImpact, false, casseBlocs);

            int x = loc.getBlockX();
            int z = loc.getBlockZ();
            Block support = world.getBlockAt(x, Math.max(1, world.getHighestBlockYAt(x, z)), z);
            support.setTypeIdAndData(Material.CHEST.getId(), (byte) 0, false);
            if (support.getState() instanceof Chest coffre) {
                Inventory inventaire = coffre.getBlockInventory();
                if (diamants > 0) inventaire.addItem(new ItemStack(Material.DIAMOND, diamants));
                if (pommesDor > 0) inventaire.addItem(new ItemStack(Material.GOLDEN_APPLE, pommesDor));
                if (lingotsOr > 0) inventaire.addItem(new ItemStack(Material.GOLD_INGOT, lingotsOr));
                if (lingotsFer > 0) inventaire.addItem(new ItemStack(Material.IRON_INGOT, lingotsFer));
            }
            LangManager.get().sendAll(IMPACT, Map.of("%x%", x, "%z%", z));
        }

        @Override
        public void onStop() {
            if (task != null) {
                task.cancel();
                task = null;
            }
            for (FallingBlock meteore : enVol) meteore.remove();
            enVol.clear();
            compteur = 0;
            annonce = false;
        }
    }

    public static class MonstersIncScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.monstersinc.desc",
                "§7Les portes posées se relient deux par deux : franchir l'une te téléporte à l'autre. Réutilisable toutes les §e%cooldown% §7secondes.");

        private static final DynamicLang RELIEE = DynamicLang.of("scenario.monstersinc.reliee",
                "§d● §7Cette porte vient d'être reliée à la précédente.");

        private static final DynamicLang EN_ATTENTE = DynamicLang.of("scenario.monstersinc.en-attente",
                "§7Cette porte attend une jumelle pour s'ouvrir sur ailleurs.");

        private static final DynamicLang PASSAGE = DynamicLang.of("scenario.monstersinc.passage",
                "§d● §7Tu ressors par la porte jumelle.");

        @Var(name = "Temps de recharge", desc = "Délai avant qu'un joueur puisse reprendre une porte.", type = VariableType.TIME, min = 1)
        private int cooldownSec = 5;

        private final Map<Location, Location> paires = new HashMap<>();
        private final List<Location> orphelines = new ArrayList<>();
        private final Map<UUID, Long> prochainPassage = new HashMap<>();

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Monsters Inc."; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%cooldown%", cooldownSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.WOOD_DOOR); }

        @Override
        public void onPlace(Player player, Block block, BlockPlaceEvent event) {
            if (!isActive()) return;
            if (!PORTES.contains(block.getType())) return;

            Location porte = basDePorte(block);
            if (paires.containsKey(porte) || orphelines.contains(porte)) return;

            if (orphelines.isEmpty()) {
                orphelines.add(porte);
                LangManager.get().send(EN_ATTENTE, player);
                return;
            }

            Location jumelle = orphelines.remove(0);
            paires.put(porte, jumelle);
            paires.put(jumelle, porte);
            LangManager.get().send(RELIEE, player);
            player.playSound(player.getLocation(), Sound.PORTAL_TRIGGER, 1.0f, 1.6f);
        }

        @Override
        public void onMove(Player player, PlayerMoveEvent event) {
            if (!isActive()) return;
            Location to = event.getTo();
            if (to == null) return;
            Location from = event.getFrom();
            if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) return;

            Block bloc = to.getBlock();
            if (!PORTES.contains(bloc.getType())) return;

            Location destination = paires.get(basDePorte(bloc));
            if (destination == null) return;

            long maintenant = System.currentTimeMillis();
            if (maintenant < prochainPassage.getOrDefault(player.getUniqueId(), 0L)) return;
            prochainPassage.put(player.getUniqueId(), maintenant + cooldownSec * 1000L);

            Location sortie = destination.clone().add(0.5D, 0.0D, 0.5D);
            sortie.setYaw(to.getYaw());
            sortie.setPitch(to.getPitch());
            player.teleport(sortie);
            player.playSound(sortie, Sound.PORTAL_TRAVEL, 0.6f, 1.4f);
            LangManager.get().send(PASSAGE, player);
        }

        @Override
        public void onStop() {
            paires.clear();
            orphelines.clear();
            prochainPassage.clear();
        }
    }

    public static class MountaineeringScenario extends Scenario {

        private static final int BIOME_MONTAGNES = 3;

        private static final DynamicLang DESC = DynamicLang.of("scenario.mountaineering.desc",
                "§7Toute la carte bascule en montagnes, et la table d'enchantement se craft à l'émeraude au lieu du diamant.");

        private static final DynamicLang CRAFT_BLOQUE = DynamicLang.of("scenario.mountaineering.craft-bloque",
                "§c● §7Ici la table d'enchantement se monte à l'émeraude, pas au diamant.");

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Mountaineering"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.EMERALD); }

        @Override
        public WorldType getWorldType() {
            if (!isActive()) return null;
            return WorldType.CUSTOMIZED;
        }

        @Override
        public Map<String, String> getGeneratorOverrides() {
            if (!isActive()) return null;
            return Map.of("fixedBiome", String.valueOf(BIOME_MONTAGNES));
        }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            ShapedRecipe recette = new ShapedRecipe(new ItemStack(Material.ENCHANTMENT_TABLE));
            recette.shape(" B ", "EOE", "OOO");
            recette.setIngredient('B', Material.BOOK);
            recette.setIngredient('E', Material.EMERALD);
            recette.setIngredient('O', Material.OBSIDIAN);
            Bukkit.addRecipe(recette);
        }

        @Override
        public void onCraft(ItemStack result, CraftItemEvent event) {
            if (!isActive()) return;
            if (result == null || result.getType() != Material.ENCHANTMENT_TABLE) return;

            for (ItemStack ingredient : event.getInventory().getMatrix()) {
                if (ingredient == null || ingredient.getType() != Material.DIAMOND) continue;
                event.setCancelled(true);
                if (event.getWhoClicked() instanceof Player player) {
                    LangManager.get().send(CRAFT_BLOQUE, player);
                }
                return;
            }
        }
    }

    public static class MovedCenterScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.moved00.desc",
                "§7Le vrai centre de la carte n'est pas 0,0 : il est tiré jusqu'à §e%rayon% §7blocs et révélé §e%avance% §7secondes avant le meetup.");

        private static final DynamicLang DEPLACE = DynamicLang.of("scenario.moved00.deplace",
                "§6● §7Le centre de la carte a été déplacé. Personne ne sait encore où.");

        private static final DynamicLang REVELATION = DynamicLang.of("scenario.moved00.revelation",
                "§6● §7Le vrai centre de la carte est §e%x%§7, §e%z%§7.");

        @Var(name = "Rayon du décalage", desc = "Distance maximale entre 0,0 et le vrai centre tiré au hasard.", type = VariableType.INTEGER, min = 1)
        private int rayonDecalage = 500;

        @Var(name = "Avance de l'annonce", desc = "Secondes avant le meetup où les coordonnées du centre sont révélées.", type = VariableType.TIME, min = 1)
        private int avanceAnnonceSec = 600;

        private int centreX = 0;
        private int centreZ = 0;
        private boolean revele = false;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Moved 0,0"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%rayon%", rayonDecalage, "%avance%", avanceAnnonceSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.COMPASS); }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onScatterStart(UhcScatterStartEvent event) {
            if (!isActive()) return;
            revele = false;
            centreX = RANDOM.nextInt(rayonDecalage * 2 + 1) - rayonDecalage;
            centreZ = RANDOM.nextInt(rayonDecalage * 2 + 1) - rayonDecalage;

            World world = Common.get().getArena();
            if (world != null) world.getWorldBorder().setCenter(centreX + 0.5D, centreZ + 0.5D);
            LangManager.get().sendAll(DEPLACE);
        }

        @Override
        public void onSec(Player p) {
            if (!isActive() || revele) return;
            if (UHCManager.get().getTimer() < UHCManager.get().getBorderTimer() - avanceAnnonceSec) return;
            revele = true;
            LangManager.get().sendAll(REVELATION, Map.of("%x%", centreX, "%z%", centreZ));
        }

        @Override
        public void onStop() {
            revele = false;
            centreX = 0;
            centreZ = 0;
        }
    }

    public static class MushroomWorldScenario extends Scenario {

        private static final int BIOME_ILE_CHAMPIGNON = 14;

        private static final DynamicLang DESC = DynamicLang.of("scenario.mushroomworld.desc",
                "§7La surface est entièrement mycélienne : aucun arbre, mais des champignons géants qui lâchent §e%buches% bûche(s)§7 par bloc.");

        private static final DynamicLang PREMIERE_BUCHE = DynamicLang.of("scenario.mushroomworld.premiere-buche",
                "§d● §7Ici le bois se récolte sur les champignons géants.");

        @Var(name = "Bûches par bloc", desc = "Bûches lâchées par un bloc de champignon géant cassé.", type = VariableType.INTEGER, min = 1)
        private int buchesParBloc = 1;

        private final Set<UUID> avertis = new HashSet<>();

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Mushroom World"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%buches%", buchesParBloc));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.RED_MUSHROOM); }

        @Override
        public WorldType getWorldType() {
            if (!isActive()) return null;
            return WorldType.CUSTOMIZED;
        }

        @Override
        public Map<String, String> getGeneratorOverrides() {
            if (!isActive()) return null;
            return Map.of("fixedBiome", String.valueOf(BIOME_ILE_CHAMPIGNON));
        }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            Material type = block.getType();
            if (type != Material.HUGE_MUSHROOM_1 && type != Material.HUGE_MUSHROOM_2) return;

            event.setCancelled(true);
            World world = block.getWorld();
            Location lieu = block.getLocation();
            block.setTypeIdAndData(0, (byte) 0, false);
            world.dropItemNaturally(lieu, new ItemStack(Material.LOG, buchesParBloc));

            if (avertis.add(player.getUniqueId())) LangManager.get().send(PREMIERE_BUCHE, player);
        }

        @Override
        public void onStop() {
            avertis.clear();
        }
    }

    public static class PermakillScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.permakill.desc",
                "§7Le cycle jour/nuit est figé et bascule à chaque mort de joueur.");

        private static final DynamicLang VERS_NUIT = DynamicLang.of("scenario.permakill.vers-nuit",
                "§9● §7Une mort de plus : la nuit s'installe pour de bon.");

        private static final DynamicLang VERS_JOUR = DynamicLang.of("scenario.permakill.vers-jour",
                "§e● §7Une mort de plus : le jour revient et ne repart plus.");

        @Var(name = "Heure du jour", desc = "Heure du monde imposée par la phase de jour.", type = VariableType.INTEGER, min = 0, max = 24000)
        private int heureJour = 1000;

        @Var(name = "Heure de la nuit", desc = "Heure du monde imposée par la phase de nuit.", type = VariableType.INTEGER, min = 0, max = 24000)
        private int heureNuit = 18000;

        private boolean nuit = false;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Permakill"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.WATCH); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            nuit = false;
            World world = Common.get().getArena();
            if (world == null) return;
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setTime(heureJour);
        }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            nuit = !nuit;
            World world = Common.get().getArena();
            if (world != null) {
                world.setGameRuleValue("doDaylightCycle", "false");
                world.setTime(nuit ? heureNuit : heureJour);
            }
            LangManager.get().sendAll(nuit ? VERS_NUIT : VERS_JOUR);
        }

        @Override
        public void onStop() {
            nuit = false;
        }
    }

    public static class PermakillStormScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.permakillstorm.desc",
                "§7Chaque mort fait tourner la météo : ciel clair, puis pluie, puis orage.");

        private static final DynamicLang CLAIR = DynamicLang.of("scenario.permakillstorm.clair",
                "§b● §7Le ciel se dégage.");

        private static final DynamicLang PLUIE = DynamicLang.of("scenario.permakillstorm.pluie",
                "§9● §7La pluie s'installe sur la carte.");

        private static final DynamicLang ORAGE = DynamicLang.of("scenario.permakillstorm.orage",
                "§8● §7L'orage éclate.");

        @Var(name = "Durée de la météo", desc = "Secondes pendant lesquelles la météo tirée est verrouillée.", type = VariableType.TIME, min = 1)
        private int dureeMeteoSec = 1800;

        private int phase = 0;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Permakill Storm"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BLAZE_ROD); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            phase = 0;
            appliquer();
        }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            phase = (phase + 1) % 3;
            appliquer();
            LangManager.get().sendAll(phase == 0 ? CLAIR : phase == 1 ? PLUIE : ORAGE);
        }

        private void appliquer() {
            World world = Common.get().getArena();
            if (world == null) return;
            int duree = dureeMeteoSec * 20;
            world.setStorm(phase >= 1);
            world.setThundering(phase >= 2);
            world.setWeatherDuration(duree);
            world.setThunderDuration(duree);
        }

        @Override
        public void onStop() {
            phase = 0;
        }
    }

    public static class PermanightScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.permanight.desc",
                "§7Le premier jour s'écoule normalement, puis la nuit tombe et ne se lève plus.");

        private static final DynamicLang TOMBEE = DynamicLang.of("scenario.permanight.tombee",
                "§9● §7Le soleil vient de se coucher pour la dernière fois.");

        @Var(name = "Heure de bascule", desc = "Heure du monde à partir de laquelle la nuit se verrouille.", type = VariableType.INTEGER, min = 0, max = 24000)
        private int heureBascule = 13000;

        @Var(name = "Heure de la nuit", desc = "Heure du monde imposée une fois la nuit verrouillée.", type = VariableType.INTEGER, min = 0, max = 24000)
        private int heureNuit = 18000;

        private boolean verrouille = false;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Permanight"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.COAL_BLOCK); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            verrouille = false;
        }

        @Override
        public void onSec(Player p) {
            if (!isActive() || verrouille) return;
            World world = Common.get().getArena();
            if (world == null || world.getTime() < heureBascule) return;

            verrouille = true;
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setTime(heureNuit);
            LangManager.get().sendAll(TOMBEE);
        }

        @Override
        public void onStop() {
            verrouille = false;
        }
    }

    public static class PlayerSwapScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.playerswap.desc",
                "§7Sans prévenir, toutes les §e%min% §7à §e%max% §7secondes, §e%paires% §7paire(s) de joueurs échangent leur position.");

        private static final DynamicLang ECHANGE = DynamicLang.of("scenario.playerswap.echange",
                "§5● §7Tu viens d'être échangé de place avec quelqu'un.");

        @Var(name = "Délai minimum", desc = "Temps minimum entre deux échanges de position.", type = VariableType.TIME, min = 1)
        private int delaiMinSec = 120;

        @Var(name = "Délai maximum", desc = "Temps maximum entre deux échanges de position.", type = VariableType.TIME, min = 1)
        private int delaiMaxSec = 420;

        @Var(name = "Paires échangées", desc = "Nombre de paires de joueurs échangées à chaque déclenchement.", type = VariableType.INTEGER, min = 1)
        private int pairesEchangees = 1;

        private BukkitRunnable task;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Player Swap"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%min%", delaiMinSec, "%max%", delaiMaxSec, "%paires%", pairesEchangees));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ENDER_PEARL); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            planifier();
        }

        private void planifier() {
            if (task != null) task.cancel();
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isActive()) return;
                    echanger();
                    planifier();
                }
            };
            int plancher = Math.min(delaiMinSec, delaiMaxSec);
            int plafond = Math.max(delaiMinSec, delaiMaxSec);
            task.runTaskLater(Main.get(), (plancher + RANDOM.nextInt(plafond - plancher + 1)) * 20L);
        }

        private void echanger() {
            List<UHCPlayer> vivants = new ArrayList<>(UHCPlayerManager.get().getPlayingOnlineUHCPlayers());
            for (int i = 0; i < pairesEchangees && vivants.size() >= 2; i++) {
                Player premier = vivants.remove(RANDOM.nextInt(vivants.size())).getPlayer();
                Player second = vivants.remove(RANDOM.nextInt(vivants.size())).getPlayer();
                if (premier == null || second == null) continue;

                Location lieuPremier = premier.getLocation().clone();
                Location lieuSecond = second.getLocation().clone();
                premier.teleport(lieuSecond);
                second.teleport(lieuPremier);
                premier.playSound(lieuSecond, Sound.ENDERMAN_TELEPORT, 1.0f, 1.0f);
                second.playSound(lieuPremier, Sound.ENDERMAN_TELEPORT, 1.0f, 1.0f);
                LangManager.get().send(ECHANGE, premier);
                LangManager.get().send(ECHANGE, second);
            }
        }

        @Override
        public void onStop() {
            if (task != null) {
                task.cancel();
                task = null;
            }
        }
    }

    public static class PrimedTntScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.primedtnt.desc",
                "§7La TNT posée s'amorce toute seule et explose au bout de §e%meche% §7ticks.");

        @Var(name = "Longueur de mèche", desc = "Ticks avant l'explosion d'une TNT posée (80 = mèche vanilla).", type = VariableType.INTEGER, min = 1)
        private int mecheTicks = 40;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Primed TNT"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%meche%", mecheTicks));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.TNT); }

        @Override
        public void onPlace(Player player, Block block, BlockPlaceEvent event) {
            if (!isActive()) return;
            if (block.getType() != Material.TNT) return;

            Location lieu = block.getLocation().add(0.5D, 0.0D, 0.5D);
            World world = block.getWorld();
            Bukkit.getScheduler().runTask(Main.get(), () -> {
                Block pose = lieu.getBlock();
                if (pose.getType() != Material.TNT) return;
                pose.setTypeIdAndData(0, (byte) 0, false);
                TNTPrimed amorcee = world.spawn(lieu, TNTPrimed.class);
                amorcee.setFuseTicks(mecheTicks);
                world.playSound(lieu, Sound.FUSE, 1.0f, 1.0f);
            });
        }
    }

    public static class ProgressiveFloodScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.progressiveflood.desc",
                "§7L'eau monte de §e%pas% §7bloc(s) toutes les §e%intervalle% §7secondes à partir du niveau §e%depart%§7,"
                        + " §7dans un rayon de §e%rayon% §7blocs autour de chaque joueur.");

        private static final DynamicLang MONTEE = DynamicLang.of("scenario.progressiveflood.montee",
                "§9● §7L'eau monte : elle atteint désormais le niveau §e%niveau%§7.");

        @Var(name = "Niveau de départ", desc = "Hauteur Y de la mer avant la première montée.", type = VariableType.INTEGER, min = 1, max = 255)
        private int niveauDepart = 62;

        @Var(name = "Montée par palier", desc = "Blocs d'eau ajoutés à chaque palier.", type = VariableType.INTEGER, min = 1)
        private int monteeParPalier = 2;

        @Var(name = "Intervalle des paliers", desc = "Secondes entre deux montées des eaux.", type = VariableType.TIME, min = 1)
        private int intervalleSec = 1200;

        @Var(name = "Niveau maximum", desc = "Hauteur Y au-delà de laquelle l'eau cesse de monter.", type = VariableType.INTEGER, min = 1, max = 255)
        private int niveauMax = 128;

        @Var(name = "Chunks par passe", desc = "Nombre de chunks noyés à chaque seconde de traitement.", type = VariableType.INTEGER, min = 1)
        private int chunksParPasse = 8;

        @Var(name = "Rayon d'inondation", desc = "Rayon en blocs, autour de chaque joueur, où l'eau est posée.", type = VariableType.INTEGER, min = 16, max = 256)
        private int rayon = 64;

        private final RisingFluid eau = new RisingFluid(Material.STATIONARY_WATER);

        private int compteur = 0;
        private BukkitRunnable task;

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Progressive Flood"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%pas%", monteeParPalier, "%intervalle%", intervalleSec, "%depart%", niveauDepart, "%rayon%", rayon));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.WATER_BUCKET); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            eau.demarrer(niveauDepart);
            compteur = 0;

            if (task != null) task.cancel();
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isActive()) { cancel(); return; }
                    World world = Common.get().getArena();
                    if (world == null) return;
                    compteur++;
                    if (compteur >= intervalleSec) {
                        compteur = 0;
                        if (eau.monter(monteeParPalier, niveauMax)) {
                            LangManager.get().sendAll(MONTEE, Map.of("%niveau%", eau.niveau()));
                        }
                    }
                    eau.empilerAutourDesJoueurs(world, rayon);
                    eau.traiter(world, chunksParPasse);
                }
            };
            task.runTaskTimer(Main.get(), 20L, 20L);
        }

        @EventHandler
        public void onChunkLoad(ChunkLoadEvent event) {
            if (!isRunning()) return;
            if (eau.niveau() <= niveauDepart) return;
            World world = Common.get().getArena();
            if (world == null || !world.equals(event.getWorld())) return;
            eau.empiler(event.getChunk().getX(), event.getChunk().getZ());
        }

        @Override
        public void onStop() {
            if (task != null) {
                task.cancel();
                task = null;
            }
            eau.vider();
            compteur = 0;
        }
    }

    public static class QuadrantBalanceScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.quadrantbalance.desc",
                "§7Chaque quadrant de la carte ne rend que §b%diamants% diamant(s) §7et §6%or% or §7par joueur. Un kill remet ton compteur à zéro dans le quadrant où tu te trouves.");

        private static final DynamicLang QUOTA_DIAMANT = DynamicLang.of("scenario.quadrantbalance.quota-diamant",
                "§c● §7Ce quadrant t'a déjà donné ses §b%diamants% diamant(s)§7 : va creuser ailleurs.");

        private static final DynamicLang QUOTA_OR = DynamicLang.of("scenario.quadrantbalance.quota-or",
                "§c● §7Ce quadrant t'a déjà donné son §6%or% or§7 : va creuser ailleurs.");

        private static final DynamicLang REMISE_A_ZERO = DynamicLang.of("scenario.quadrantbalance.remise-a-zero",
                "§a● §7Ce kill remet à zéro tes quotas de minerai dans ce quadrant.");

        @Var(name = "Plafond de diamants", desc = "Diamants extractibles par joueur et par quadrant.", type = VariableType.INTEGER, min = 1)
        private int plafondDiamants = 8;

        @Var(name = "Plafond d'or", desc = "Minerais d'or extractibles par joueur et par quadrant.", type = VariableType.INTEGER, min = 1)
        private int plafondOr = 48;

        private final Map<UUID, int[]> compteurs = new HashMap<>();

        @Override
        public Family getFamily() { return Family.MONDE; }

        @Override public String getName() { return "Quadrant Balance"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%diamants%", plafondDiamants, "%or%", plafondOr));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.EMPTY_MAP); }

        private int quadrant(Location lieu) {
            World world = lieu.getWorld();
            Location centre = world == null ? lieu : world.getWorldBorder().getCenter();
            return (lieu.getX() >= centre.getX() ? 1 : 0) + (lieu.getZ() >= centre.getZ() ? 2 : 0);
        }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            Material type = block.getType();
            boolean diamant = type == Material.DIAMOND_ORE;
            if (!diamant && type != Material.GOLD_ORE) return;

            int index = quadrant(block.getLocation()) * 2 + (diamant ? 0 : 1);
            int[] extraits = compteurs.computeIfAbsent(player.getUniqueId(), uuid -> new int[8]);
            int plafond = diamant ? plafondDiamants : plafondOr;

            if (extraits[index] >= plafond) {
                event.setCancelled(true);
                block.setTypeIdAndData(Material.STONE.getId(), (byte) 0, false);
                LangManager.get().send(diamant ? QUOTA_DIAMANT : QUOTA_OR, player,
                        Map.of(diamant ? "%diamants%" : "%or%", plafond));
                return;
            }
            extraits[index]++;
        }

        @Override
        public void onKill(UHCPlayer killer, UHCPlayer victim) {
            if (!isActive() || killer == null) return;
            Player tueur = killer.getPlayer();
            if (tueur == null) return;

            int[] extraits = compteurs.get(tueur.getUniqueId());
            if (extraits == null) return;
            int base = quadrant(tueur.getLocation()) * 2;
            extraits[base] = 0;
            extraits[base + 1] = 0;
            LangManager.get().send(REMISE_A_ZERO, tueur);
        }

        @Override
        public void onStop() {
            compteurs.clear();
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new MeteorsScenario(),
                new MonstersIncScenario(),
                new MountaineeringScenario(),
                new MovedCenterScenario(),
                new MushroomWorldScenario(),
                new PermakillScenario(),
                new PermakillStormScenario(),
                new PermanightScenario(),
                new PlayerSwapScenario(),
                new PrimedTntScenario(),
                new ProgressiveFloodScenario(),
                new QuadrantBalanceScenario()
        );
    }
}
