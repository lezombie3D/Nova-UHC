package net.novaproject.scenarioplus.vie;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.display.HealthPackets;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.player.utils.PlayerUtils;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class RegenScenarios {

    private static final double BUKKIT_MAX_HEALTH = 2048.0D;

    private static final short PLAYER_SKULL_DATA = 3;

    private RegenScenarios() {
    }

    private static List<Player> teamMembers(Player player) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null || uhcPlayer.getTeam().isEmpty()) return List.of();
        return livingMembers(uhcPlayer.getTeam().get());
    }

    private static List<Player> livingMembers(UHCTeam team) {
        List<Player> members = new ArrayList<>();
        for (UHCPlayer member : team.getPlayers()) {
            Player online = member.getPlayer();
            if (online != null && !online.isDead()) members.add(online);
        }
        return members;
    }

    private static void syncTeamFrom(Player source) {
        double value = source.getHealth();
        if (value <= 0.0D) return;
        for (Player member : teamMembers(source)) {
            if (member.equals(source)) continue;
            double clamped = Math.min(value, member.getMaxHealth());
            if (clamped <= 0.0D) continue;
            member.setHealth(clamped);
        }
    }

    private static void syncTeamLater(Player source) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!source.isOnline() || source.isDead()) return;
                syncTeamFrom(source);
            }
        }.runTaskLater(Main.get(), 1L);
    }

    private static void wipeTeamLater(Player dead) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player member : teamMembers(dead)) {
                    if (member.equals(dead) || member.isDead()) continue;
                    member.setHealth(0.0D);
                }
            }
        }.runTaskLater(Main.get(), 1L);
    }

    public static class SelfDiagnosisScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.selfdiagnosis.desc",
                "§7Ta barre de vie affiche toujours le maximum : plus personne ne connaît sa vie réelle.");

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Self Diagnosis"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SPECKLED_MELON); }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            fakeFullHealth(player);
        }

        @Override
        public void onDamage(Player player, EntityDamageEvent event) {
            if (!isActive()) return;
            new BukkitRunnable() {
                @Override
                public void run() {
                    fakeFullHealth(player);
                }
            }.runTaskLater(Main.get(), 1L);
        }

        private void fakeFullHealth(Player player) {
            if (player == null || !player.isOnline() || player.isDead()) return;
            HealthPackets.fakeFullHealth(player);
        }
    }

    public static class SharedDamageScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.shareddamage.desc",
                "§7Les dégâts subis par un joueur sont répartis à parts égales sur toute son équipe.");

        private boolean propagating = false;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Shared Damage"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.IRON_CHESTPLATE); }

        @Override
        public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {
            if (!isActive()) return;
            if (propagating || event.isCancelled()) return;
            if (!(entity instanceof Player victim)) return;

            List<Player> members = teamMembers(victim);
            if (members.size() < 2) return;

            double share = event.getDamage() / members.size();
            if (share <= 0.0D) return;
            event.setDamage(share);

            propagating = true;
            try {
                for (Player member : members) {
                    if (member.equals(victim)) continue;
                    member.damage(share);
                }
            } finally {
                propagating = false;
            }
        }
    }

    public static class SharedHealthScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.sharedhealth.desc",
                "§7Toute l'équipe partage une seule barre de vie : soins et dégâts sont communs.");

        private static final DynamicLang WIPE = DynamicLang.of("scenario.sharedhealth.wipe",
                "§c☠ §7L'équipe de §f%joueur% §7s'éteint avec lui : leur barre de vie était commune.");

        @Var(name = "Mort collective", desc = "La mort d'un coéquipier élimine immédiatement toute l'équipe.", type = VariableType.BOOLEAN)
        private boolean mortCollective = true;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Shared Health"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLDEN_APPLE); }

        @Override
        public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {
            if (!isActive()) return;
            if (event.isCancelled()) return;
            if (!(entity instanceof Player victim)) return;
            syncTeamLater(victim);
        }

        @EventHandler
        public void onRegainHealth(EntityRegainHealthEvent event) {
            if (!isRunning()) return;
            if (event.isCancelled()) return;
            if (!(event.getEntity() instanceof Player healed)) return;
            syncTeamLater(healed);
        }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            if (!mortCollective) return;
            Player dead = uhcPlayer.getPlayer();
            if (dead == null) return;
            if (teamMembers(dead).isEmpty()) return;
            LangManager.get().sendAll(WIPE, Map.of("%joueur%", dead.getName()));
            wipeTeamLater(dead);
        }
    }

    public static class SiphonScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.siphon.desc",
                "§7Un kill siphonne la victime : §cRégénération %niveau% §7pendant §e%duree%s§7, §a%niveaux% niveau(x) §7et un livre enchanté.");

        private static final DynamicLang REWARD = DynamicLang.of("scenario.siphon.reward",
                "§5⚗ §7Tu siphonnes §f%victime% §7: régénération, expérience et un livre enchanté.");

        private static final List<Enchantment> BOOK_POOL = List.of(
                Enchantment.PROTECTION_ENVIRONMENTAL,
                Enchantment.PROTECTION_FIRE,
                Enchantment.PROTECTION_FALL,
                Enchantment.PROTECTION_EXPLOSIONS,
                Enchantment.PROTECTION_PROJECTILE,
                Enchantment.OXYGEN,
                Enchantment.WATER_WORKER,
                Enchantment.THORNS,
                Enchantment.DEPTH_STRIDER,
                Enchantment.DAMAGE_ALL,
                Enchantment.DAMAGE_UNDEAD,
                Enchantment.DAMAGE_ARTHROPODS,
                Enchantment.KNOCKBACK,
                Enchantment.FIRE_ASPECT,
                Enchantment.LOOT_BONUS_MOBS,
                Enchantment.DIG_SPEED,
                Enchantment.SILK_TOUCH,
                Enchantment.DURABILITY,
                Enchantment.LOOT_BONUS_BLOCKS,
                Enchantment.ARROW_DAMAGE,
                Enchantment.ARROW_KNOCKBACK,
                Enchantment.ARROW_FIRE
        );

        private final Random random = new Random();

        @Var(name = "Durée de régénération", desc = "Durée de la régénération accordée au tueur.", type = VariableType.TIME, min = 1)
        private int regenDureeSec = 4;

        @Var(name = "Niveau de régénération", desc = "Niveau de la régénération accordée au tueur.", type = VariableType.INTEGER, min = 1, max = 5)
        private int regenNiveau = 2;

        @Var(name = "Niveaux gagnés", desc = "Niveaux d'expérience donnés au tueur.", type = VariableType.INTEGER, min = 0)
        private int niveauxGagnes = 2;

        @Var(name = "Niveau du livre", desc = "Niveau de l'enchantement stocké dans le livre lâché par la victime.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauLivre = 1;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Siphon"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of(
                    "%niveau%", regenNiveau,
                    "%duree%", regenDureeSec,
                    "%niveaux%", niveauxGagnes));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ENCHANTED_BOOK); }

        @Override
        public void onKill(UHCPlayer killer, UHCPlayer victim) {
            if (!isActive()) return;
            if (killer == null || victim == null) return;
            Player player = killer.getPlayer();
            if (player == null) return;

            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,
                    regenDureeSec * 20, regenNiveau - 1), true);

            if (niveauxGagnes > 0) player.giveExpLevels(niveauxGagnes);

            Enchantment enchantment = BOOK_POOL.get(random.nextInt(BOOK_POOL.size()));
            PlayerUtils.giveOrDrop(player, new ItemCreator(Material.ENCHANTED_BOOK)
                    .addStoredEnchantment(enchantment, niveauLivre)
                    .getItemstack());

            Player dead = victim.getPlayer();
            LangManager.get().send(REWARD, player, Map.of(
                    "%victime%", dead == null ? victim.getUniqueId().toString() : dead.getName()));
        }
    }

    public static class SoupScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.soup.desc",
                "§7Un clic droit sur une soupe de champignons rend §c%coeurs% ❤ §7instantanément.");

        @Var(name = "Cœurs rendus", desc = "Vie rendue par une soupe de champignons, en cœurs.", type = VariableType.DOUBLE, min = 0.5)
        private double coeursRendus = 3.0D;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Soup"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%coeurs%", coeursRendus));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.MUSHROOM_SOUP); }

        @Override
        public void onPlayerInteract(Player player, PlayerInteractEvent event) {
            if (!isActive()) return;
            Action action = event.getAction();
            if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

            ItemStack hand = player.getItemInHand();
            if (hand == null || hand.getType() != Material.MUSHROOM_SOUP) return;

            double max = player.getMaxHealth();
            if (player.getHealth() >= max) return;

            event.setCancelled(true);
            player.setHealth(Math.min(max, player.getHealth() + coeursRendus * 2.0D));
            player.setItemInHand(new ItemStack(Material.BOWL));
            player.updateInventory();
        }
    }

    public static class SprainScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.sprain.desc",
                "§7Une chute te foule la cheville : §eLenteur %niveau% §7au maximum pendant §e%duree%s§7, aggravée à chaque nouvelle chute.");

        private static final DynamicLang SPRAINED = DynamicLang.of("scenario.sprain.sprained",
                "§6✖ §7Entorse : §eLenteur %niveau% §7pendant §e%duree% §7secondes.");

        @Var(name = "Durée de l'entorse", desc = "Durée de la lenteur infligée par une chute.", type = VariableType.TIME, min = 1)
        private int dureeSec = 8;

        @Var(name = "Dégâts par niveau", desc = "Dégâts de chute nécessaires pour gagner un niveau de lenteur.", type = VariableType.DOUBLE, min = 0.5)
        private double degatsParNiveau = 4.0D;

        @Var(name = "Niveau maximum", desc = "Niveau de lenteur au-delà duquel l'entorse n'empire plus.", type = VariableType.INTEGER, min = 1, max = 10)
        private int niveauMax = 5;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Sprain"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%niveau%", niveauMax, "%duree%", dureeSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.LEATHER_BOOTS); }

        @Override
        public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {
            if (!isActive()) return;
            if (event.isCancelled()) return;
            if (event.getCause() != DamageCause.FALL) return;
            if (!(entity instanceof Player player)) return;

            int scaled = (int) Math.ceil(event.getFinalDamage() / degatsParNiveau);
            int courant = 0;
            for (PotionEffect effect : player.getActivePotionEffects()) {
                if (effect.getType().equals(PotionEffectType.SLOW)) courant = effect.getAmplifier() + 1;
            }

            int niveau = Math.min(niveauMax, Math.max(scaled, courant + 1));
            if (niveau < 1) return;

            player.removePotionEffect(PotionEffectType.SLOW);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, dureeSec * 20, niveau - 1));
            LangManager.get().send(SPRAINED, player, Map.of("%niveau%", niveau, "%duree%", dureeSec));
        }
    }

    public static class TeamHealthScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.teamhealth.desc",
                "§7L'équipe met sa vie en commun : une seule barre de §c%coeurs% ❤ §7par coéquipier vivant.");

        private static final DynamicLang POOL = DynamicLang.of("scenario.teamhealth.pool",
                "§c❤ §7Réserve d'équipe : §f%coeurs% ❤ §7partagés entre vous.");

        @Var(name = "Cœurs par membre", desc = "Cœurs apportés par chaque coéquipier à la réserve commune.", type = VariableType.DOUBLE, min = 0.5)
        private double coeursParMembre = 10.0D;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Team Health"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%coeurs%", coeursParMembre));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BEACON); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            for (UHCTeam team : UHCTeamManager.get().getTeams()) {
                List<Player> members = livingMembers(team);
                if (members.isEmpty()) continue;

                double pool = Math.min(BUKKIT_MAX_HEALTH, members.size() * coeursParMembre * 2.0D);
                for (Player member : members) {
                    member.setMaxHealth(pool);
                    member.setHealth(pool);
                    LangManager.get().send(POOL, member, Map.of("%coeurs%", Math.round(pool / 2.0D)));
                }
            }
        }

        @Override
        public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {
            if (!isActive()) return;
            if (event.isCancelled()) return;
            if (!(entity instanceof Player victim)) return;
            syncTeamLater(victim);
        }

        @EventHandler
        public void onRegainHealth(EntityRegainHealthEvent event) {
            if (!isRunning()) return;
            if (event.isCancelled()) return;
            if (!(event.getEntity() instanceof Player healed)) return;
            syncTeamLater(healed);
        }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            Player dead = uhcPlayer.getPlayer();
            if (dead == null) return;
            wipeTeamLater(dead);
        }
    }

    public static class TerrariaScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.terraria.desc",
                "§7Le monde se réduit à une bande de §e%largeur% §7blocs le long de l'axe Z.");

        private static final DynamicLang DESC_CROIX = DynamicLang.of("scenario.terraria.desc-croix",
                "§7Le monde se réduit à deux bandes de §e%largeur% §7blocs en croix sur les axes X et Z.");

        private static final DynamicLang WALL = DynamicLang.of("scenario.terraria.wall",
                "§c✖ §7Le monde s'arrête ici.");

        @Var(name = "Largeur de la bande", desc = "Largeur en blocs de la bande de terrain jouable.", type = VariableType.INTEGER, min = 8)
        private int largeurBande = 100;

        @Var(name = "Seconde bande", desc = "Ajoute une seconde bande perpendiculaire, en croix (Terraria X).", type = VariableType.BOOLEAN)
        private boolean secondeBande = false;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Terraria"; }

        @Override
        public String getDescription(Player player) {
            return t(secondeBande ? DESC_CROIX : DESC, player, Map.of("%largeur%", largeurBande));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.MAP); }

        @Override
        public void onMove(Player player, PlayerMoveEvent event) {
            if (!isActive()) return;
            Location from = event.getFrom();
            Location to = event.getTo();
            if (to == null) return;
            if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) return;
            if (dansLaBande(to) || !dansLaBande(from)) return;

            event.setTo(from.clone());
            DisplayService.actionBar(player, t(WALL, player));
        }

        private boolean dansLaBande(Location location) {
            World world = location.getWorld();
            if (world == null) return true;
            WorldBorder border = world.getWorldBorder();
            Location center = border.getCenter();
            double half = largeurBande / 2.0D;
            if (Math.abs(location.getX() - center.getX()) <= half) return true;
            return secondeBande && Math.abs(location.getZ() - center.getZ()) <= half;
        }
    }

    public static class ThirteenScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.thirteen.desc",
                "§7Garder une pile de §e%nombre% §7objets plus de §e%duree% §7secondes est mortel.");

        private static final DynamicLang WARN = DynamicLang.of("scenario.thirteen.warn",
                "§c%nombre% §7objets en main... §c%restant%s");

        private static final DynamicLang DOOM = DynamicLang.of("scenario.thirteen.doom",
                "§4☠ §f%joueur% §7a gardé une pile de §c%nombre% §7objets trop longtemps.");

        private final Map<UUID, Integer> compteurs = new HashMap<>();

        @Var(name = "Nombre fatidique", desc = "Taille de pile qui condamne son porteur.", type = VariableType.INTEGER, min = 1, max = 64)
        private int nombreFatidique = 13;

        @Var(name = "Délai avant la mort", desc = "Temps de détention de la pile avant la mort.", type = VariableType.TIME, min = 1)
        private int delaiSec = 13;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Thirteen"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%nombre%", nombreFatidique, "%duree%", delaiSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BONE); }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            UUID uuid = player.getUniqueId();

            if (!portePileMaudite(player)) {
                compteurs.remove(uuid);
                return;
            }

            int ecoule = compteurs.merge(uuid, 1, Integer::sum);
            if (ecoule < delaiSec) {
                DisplayService.actionBar(player, t(WARN, player, Map.of(
                        "%nombre%", nombreFatidique,
                        "%restant%", delaiSec - ecoule)));
                return;
            }

            compteurs.remove(uuid);
            LangManager.get().sendAll(DOOM, Map.of(
                    "%joueur%", player.getName(),
                    "%nombre%", nombreFatidique));
            player.setHealth(0.0D);
        }

        @Override
        public void onStop() {
            compteurs.clear();
        }

        private boolean portePileMaudite(Player player) {
            for (ItemStack content : player.getInventory().getContents()) {
                if (content != null && content.getAmount() == nombreFatidique) return true;
            }
            return false;
        }
    }

    public static class UniversalHealthCareScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.universalhealthcare.desc",
                "§7Dès qu'un joueur se soigne, tous les autres regagnent §a%pourcent%% §7de leur vie maximale.");

        private boolean propagating = false;

        @Var(name = "Soin mutualisé", desc = "Part de la vie maximale rendue à tous quand un joueur se soigne.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int pourcentage = 1;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Universal Health Care"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%pourcent%", pourcentage));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.POTION); }

        @EventHandler
        public void onRegainHealth(EntityRegainHealthEvent event) {
            if (!isRunning()) return;
            if (propagating || event.isCancelled()) return;
            if (!(event.getEntity() instanceof Player source)) return;
            if (pourcentage <= 0) return;

            propagating = true;
            try {
                for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                    Player other = uhcPlayer.getPlayer();
                    if (other == null || other.isDead() || other.equals(source)) continue;
                    double max = other.getMaxHealth();
                    double soin = max * pourcentage / 100.0D;
                    if (soin <= 0.0D) continue;
                    other.setHealth(Math.min(max, other.getHealth() + soin));
                }
            } finally {
                propagating = false;
            }
        }
    }

    public static class VengefulSpiritsScenario extends Scenario {

        private static final String SPIRIT_OWNER = "vengeful-spirit-owner";

        private static final DynamicLang DESC = DynamicLang.of("scenario.vengefulspirits.desc",
                "§7Chaque mort libère un esprit — un ghast en surface, un blaze sous terre — qui garde la tête du défunt.");

        private static final DynamicLang SPIRIT_NAME = DynamicLang.of("scenario.vengefulspirits.spirit-name",
                "§5Esprit de §f%joueur%");

        private static final DynamicLang RISEN = DynamicLang.of("scenario.vengefulspirits.risen",
                "§5✦ §7L'esprit de §f%joueur% §7veille sur sa tête.");

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Vengeful Spirits"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GHAST_TEAR); }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            Player dead = uhcPlayer.getPlayer();
            if (dead == null) return;

            Location loc = dead.getLocation();
            World world = loc.getWorld();
            if (world == null) return;

            boolean surface = world.getHighestBlockYAt(loc) <= loc.getBlockY();
            LivingEntity spirit = surface ? world.spawn(loc, Ghast.class) : world.spawn(loc, Blaze.class);

            spirit.setCustomName(t(SPIRIT_NAME, Map.of("%joueur%", dead.getName())));
            spirit.setCustomNameVisible(true);
            spirit.setMetadata(SPIRIT_OWNER, new FixedMetadataValue(Main.get(), dead.getName()));

            LangManager.get().sendAll(RISEN, Map.of("%joueur%", dead.getName()));
        }

        @Override
        public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
            if (!isActive()) return;
            if (!entity.hasMetadata(SPIRIT_OWNER)) return;

            List<MetadataValue> values = entity.getMetadata(SPIRIT_OWNER);
            if (values.isEmpty()) return;
            String owner = values.get(0).asString();

            ItemStack skull = new ItemCreator(Material.SKULL_ITEM).setDurability(PLAYER_SKULL_DATA)
                    .setOwner(owner).getItemstack();

            event.getDrops().clear();
            event.getDrops().add(skull);
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new SelfDiagnosisScenario(),
                new SharedDamageScenario(),
                new SharedHealthScenario(),
                new SiphonScenario(),
                new SoupScenario(),
                new SprainScenario(),
                new TeamHealthScenario(),
                new TerrariaScenario(),
                new ThirteenScenario(),
                new UniversalHealthCareScenario(),
                new VengefulSpiritsScenario()
        );
    }
}
