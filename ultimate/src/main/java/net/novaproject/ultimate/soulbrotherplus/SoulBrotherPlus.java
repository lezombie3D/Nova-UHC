package net.novaproject.ultimate.soulbrotherplus;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.utils.chat.TextUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.world.generation.UHCWorldSettings;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class SoulBrotherPlus extends Scenario {

    private static final String PREP_WORLD_PREFIX = "sbplus_prep_";
    private static final String FINAL_WORLD_NAME = "sbplus_final";

    private static final DynamicLang DESC = DynamicLang.of("mode.soulbrothersplus.desc",
            "§7Chaque coéquipier est isolé dans un monde à la §bgénération différente§7. Vous vous décrivez vos ressources, puis tout le monde est réuni dans un monde final au bout de §e%temps%§7.");
    private static final DynamicLang SEPARATED = DynamicLang.of("mode.soulbrothersplus.separated",
            "§8» §7Vos âmes sont §cséparées§7. Décrivez-vous vos §emines§7, §evillages §7et §eminerais §7avant la réunion.");
    private static final DynamicLang ASSIGNED = DynamicLang.of("mode.soulbrothersplus.assigned",
            "§8» §7Tu explores le monde §b%monde% §8(§7monde §b%numero%§8)§7.");
    private static final DynamicLang REUNION_WARN = DynamicLang.of("mode.soulbrothersplus.reunion_warn",
            "§8» §7Réunion des âmes dans §e%temps%§7.");
    private static final DynamicLang REUNION_ALL = DynamicLang.of("mode.soulbrothersplus.reunion_all",
            "§8» §aLes âmes se réunissent §7: tout le monde rejoint le monde final.");
    private static final DynamicLang REUNION_PERSONAL = DynamicLang.of("mode.soulbrothersplus.reunion_personal",
            "§8» §7Tu retrouves ton équipe dans le §bmonde final§7.");
    private static final DynamicLang SOUL_UPDATE = DynamicLang.of("mode.soulbrothersplus.soul_update",
            "§8» §b%joueur% §7est en §ex %x%§7, §ez %z% §8(§c%vie% ❤§8)§7.");
    private static final DynamicLang BROTHER_DIED = DynamicLang.of("mode.soulbrothersplus.brother_died",
            "§8» §c%joueur% §7est mort dans son monde : ton âme encaisse le choc.");
    private static final DynamicLang NETHER_CLOSED = DynamicLang.of("mode.soulbrothersplus.nether_closed",
            "§8» §cLe Nether est fermé pendant la phase de préparation.");
    private static final DynamicLang TEAM_SIZE_FORCED = DynamicLang.of("mode.soulbrothersplus.team_size_forced",
            "§8» §cSoul Brothers+ exige des équipes : taille forcée à 2.");
    private static final DynamicLang WORLDS_FAILED = DynamicLang.of("mode.soulbrothersplus.worlds_failed",
            "§8» §cLes mondes de préparation n'ont pas pu être générés.");

    private static final DynamicLang PROFIL_VILLAGES = DynamicLang.of("mode.soulbrothersplus.profil.villages", "§eTerres de Villages");
    private static final DynamicLang PROFIL_CAVERNES = DynamicLang.of("mode.soulbrothersplus.profil.cavernes", "§8Cavernes Géantes");
    private static final DynamicLang PROFIL_NOYE = DynamicLang.of("mode.soulbrothersplus.profil.noye", "§9Monde Noyé");
    private static final DynamicLang PROFIL_PLAT = DynamicLang.of("mode.soulbrothersplus.profil.plat", "§fPlaine Infinie");
    private static final DynamicLang PROFIL_AMPLIFIE = DynamicLang.of("mode.soulbrothersplus.profil.amplifie", "§6Pics Amplifiés");
    private static final DynamicLang PROFIL_DESERT = DynamicLang.of("mode.soulbrothersplus.profil.desert", "§eDésert Éternel");
    private static final DynamicLang PROFIL_JUNGLE = DynamicLang.of("mode.soulbrothersplus.profil.jungle", "§2Jungle Perdue");
    private static final DynamicLang PROFIL_SAUVAGE = DynamicLang.of("mode.soulbrothersplus.profil.sauvage", "§aNature Sauvage");
    private static final DynamicLang PROFIL_ETENDUES = DynamicLang.of("mode.soulbrothersplus.profil.etendues", "§3Grandes Étendues");
    private static final DynamicLang PROFIL_FILONS = DynamicLang.of("mode.soulbrothersplus.profil.filons", "§bFilons Profonds");
    private static final DynamicLang PROFIL_CLASSIQUE = DynamicLang.of("mode.soulbrothersplus.profil.classique", "§7Monde Classique");

    private record GenProfile(DynamicLang label, WorldType type, String settings) {
    }

    @Var(name = "Durée de préparation", desc = "Temps passé séparés avant la réunion dans le monde final.", type = VariableType.TIME, min = 60)
    private int reunionTimeSec = 3600;

    @Var(name = "Intervalle des nouvelles", desc = "Fréquence des nouvelles automatiques des coéquipiers (0 = aucune).", type = VariableType.TIME, min = 0)
    private int updateIntervalSec = 300;

    @Var(name = "Bordure des mondes de prep", desc = "Taille de bordure des mondes de préparation, volontairement petite (coût de génération).", type = VariableType.INTEGER, min = 100)
    private int prepBorderSize = 400;

    @Var(name = "Contrecoup de mort", desc = "Les coéquipiers encaissent la mort d'un des leurs pendant la préparation.", type = VariableType.BOOLEAN)
    private boolean deathBacklash = true;

    @Var(name = "Dégâts du contrecoup", desc = "Dégâts subis par chaque coéquipier survivant.", type = VariableType.DOUBLE, min = 0)
    private double deathBacklashDamage = 6.0;

    @Var(name = "Durée du contrecoup", desc = "Durée de la lenteur infligée par le contrecoup (cécité = moitié).", type = VariableType.TIME, min = 1)
    private int deathBacklashSec = 10;

    @Var(name = "Niveau de lenteur du contrecoup", desc = "Niveau de l'effet de lenteur infligé par le contrecoup.", type = VariableType.INTEGER, min = 1)
    private int deathBacklashSlowLevel = 2;

    @Var(name = "Profil Terres de Villages", desc = "Villages partout, or rare.", type = VariableType.BOOLEAN)
    private boolean profilVillages = true;

    @Var(name = "Profil Cavernes Géantes", desc = "Mer basse, cavernes, ravins, mineshafts et donjons partout.", type = VariableType.BOOLEAN)
    private boolean profilCavernes = true;

    @Var(name = "Profil Monde Noyé", desc = "Niveau de la mer très haut, monuments océaniques.", type = VariableType.BOOLEAN)
    private boolean profilNoye = true;

    @Var(name = "Profil Plaine Infinie", desc = "Superflat : pas de relief, tout est dans les structures.", type = VariableType.BOOLEAN)
    private boolean profilPlat = true;

    @Var(name = "Profil Pics Amplifiés", desc = "Terrain amplifié, verticalité extrême.", type = VariableType.BOOLEAN)
    private boolean profilAmplifie = true;

    @Var(name = "Profil Désert Éternel", desc = "Mono-biome désert, temples et villages, aucun lac.", type = VariableType.BOOLEAN)
    private boolean profilDesert = true;

    @Var(name = "Profil Jungle Perdue", desc = "Mono-biome jungle, temples, aucun mineshaft.", type = VariableType.BOOLEAN)
    private boolean profilJungle = true;

    @Var(name = "Profil Nature Sauvage", desc = "Aucune structure : ni village, ni temple, ni mineshaft.", type = VariableType.BOOLEAN)
    private boolean profilSauvage = true;

    @Var(name = "Profil Grandes Étendues", desc = "Large biomes, mineshafts et strongholds.", type = VariableType.BOOLEAN)
    private boolean profilEtendues = true;

    @Var(name = "Profil Filons Profonds", desc = "Aucune caverne, minerais enfouis mais abondants.", type = VariableType.BOOLEAN)
    private boolean profilFilons = true;

    private final List<World> prepWorlds = new ArrayList<>();
    private final List<DynamicLang> prepLabels = new ArrayList<>();
    private final Map<UUID, Integer> slots = new HashMap<>();

    private World finalWorld;
    private boolean reunionDone = false;
    private int lastTick = -1;
    private int builtFor = 0;

    @Override
    public String getName() {
        return "Soul Brothers+";
    }

    @Override
    public String getDescription(Player player) {
        return t(DESC, player, Map.of("%temps%", TextUtils.getFormattedTime(reunionTimeSec)));
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.EYE_OF_ENDER);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public void toggleActive() {
        super.toggleActive();
        if (!isActive()) {
            resetState();
            return;
        }
        forceTeams();
        ensureWorlds();
    }

    @Override
    public void onTeamUpdate() {
        if (!isActive()) return;
        forceTeams();
        ensureWorlds();
    }

    @Override
    public void onGameStart() {
        if (!isActive()) return;
        LangManager.get().sendAll(SEPARATED);
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        if (!isActive()) return;
        Player player = uhcPlayer.getPlayer();
        if (player == null) return;

        ensureWorlds();
        if (prepWorlds.isEmpty()) {
            LangManager.get().send(WORLDS_FAILED, player);
            player.teleport(location);
            return;
        }

        UUID uuid = player.getUniqueId();
        UHCTeam team = uhcPlayer.getTeam().orElse(null);
        if (team != null && !slots.containsKey(uuid)) {
            List<UHCPlayer> members = team.getPlayers();
            for (int i = 0; i < members.size(); i++) {
                Player member = members.get(i).getPlayer();
                if (member != null) slots.put(member.getUniqueId(), i % prepWorlds.size());
            }
        }

        int slot = slots.getOrDefault(uuid, ThreadLocalRandom.current().nextInt(prepWorlds.size()));
        slots.put(uuid, slot);

        player.teleport(spreadIn(prepWorlds.get(slot)));
        LangManager.get().send(ASSIGNED, player, Map.of(
                "%monde%", t(prepLabels.get(slot), player),
                "%numero%", slot + 1));
    }

    @Override
    public void onSec(Player player) {
        if (!isActive() || player == null) return;

        int timer = UHCManager.get().getTimer();
        if (timer != lastTick) {
            lastTick = timer;
            globalSec(timer);
        }

        if (reunionDone || updateIntervalSec <= 0 || timer % updateIntervalSec != 0) return;
        sendTeamNews(player);
    }

    @Override
    public void onPortal(PlayerPortalEvent event) {
        if (!isActive() || reunionDone) return;
        if (event.getCause() != TeleportCause.NETHER_PORTAL) return;
        event.setCancelled(true);
        LangManager.get().send(NETHER_CLOSED, event.getPlayer());
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (!isActive()) return;
        Player dead = uhcPlayer.getPlayer();
        if (dead == null) return;

        slots.remove(dead.getUniqueId());
        if (reunionDone || !deathBacklash) return;

        UHCTeam team = uhcPlayer.getTeam().orElse(null);
        if (team == null) return;

        for (UHCPlayer mate : team.getPlayers()) {
            Player brother = mate.getPlayer();
            if (brother == null || brother.getUniqueId().equals(dead.getUniqueId())) continue;
            LangManager.get().send(BROTHER_DIED, brother, Map.of("%joueur%", dead.getName()));
            brother.damage(deathBacklashDamage);
            brother.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, deathBacklashSec * 20, deathBacklashSlowLevel - 1));
            brother.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, deathBacklashSec * 10, 0));
            brother.playSound(brother.getLocation(), Sound.WITHER_DEATH, 1.0f, 0.5f);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        resetState();
    }

    private void resetState() {
        prepWorlds.clear();
        prepLabels.clear();
        slots.clear();
        finalWorld = null;
        reunionDone = false;
        lastTick = -1;
        builtFor = 0;
    }

    private void forceTeams() {
        if (UHCManager.get().getTeam_size() >= 2) return;
        UHCManager.get().setTeam_size(2);
        LangManager.get().sendAll(TEAM_SIZE_FORCED);
    }

    private void globalSec(int timer) {
        if (reunionDone) {
            mirrorBorder();
            return;
        }
        int left = reunionTimeSec - timer;
        if (left == 300 || left == 60 || left == 10) {
            LangManager.get().sendAll(REUNION_WARN, Map.of("%temps%", TextUtils.getFormattedTime(left)));
        }
        if (left <= 0) startReunion();
    }

    private void startReunion() {
        reunionDone = true;
        slots.clear();

        World arena = Common.get().getArena();
        if (finalWorld == null) finalWorld = arena;
        if (finalWorld == null) return;
        if (arena != null && arena != finalWorld) {
            finalWorld.getWorldBorder().setSize(arena.getWorldBorder().getSize());
        }

        LangManager.get().sendAll(REUNION_ALL);

        Map<UHCTeam, Location> spots = new HashMap<>();
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            if (player == null) continue;
            UHCTeam team = uhcPlayer.getTeam().orElse(null);
            Location target = team == null
                    ? spreadIn(finalWorld)
                    : spots.computeIfAbsent(team, key -> spreadIn(finalWorld));
            player.teleport(target);
            player.playSound(player.getLocation(), Sound.ENDERDRAGON_DEATH, 1.0f, 1.5f);
            LangManager.get().send(REUNION_PERSONAL, player);
        }
    }

    private void mirrorBorder() {
        World arena = Common.get().getArena();
        if (arena == null || finalWorld == null) return;
        WorldBorder source = arena.getWorldBorder();
        WorldBorder target = finalWorld.getWorldBorder();
        if (Math.abs(target.getSize() - source.getSize()) < 0.5) return;
        target.setSize(source.getSize(), 1L);
    }

    private void sendTeamNews(Player player) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null) return;
        UHCTeam team = uhcPlayer.getTeam().orElse(null);
        if (team == null) return;

        for (UHCPlayer mate : team.getPlayers()) {
            Player brother = mate.getPlayer();
            if (brother == null || brother.getUniqueId().equals(player.getUniqueId())) continue;
            Location at = brother.getLocation();
            LangManager.get().send(SOUL_UPDATE, player, Map.of(
                    "%joueur%", brother.getName(),
                    "%x%", at.getBlockX(),
                    "%z%", at.getBlockZ(),
                    "%vie%", (int) brother.getHealth()));
        }
    }

    private void ensureWorlds() {
        int count = Math.max(2, UHCManager.get().getTeam_size());
        if (builtFor == count) return;
        builtFor = count;

        prepWorlds.clear();
        prepLabels.clear();
        slots.clear();

        List<GenProfile> pool = enabledProfiles();
        Collections.shuffle(pool);

        for (int i = 0; i < count; i++) {
            GenProfile profile = pool.get(i % pool.size());
            World world = buildWorld(PREP_WORLD_PREFIX + (i + 1), profile, prepBorderSize);
            if (world == null) continue;
            prepWorlds.add(world);
            prepLabels.add(profile.label());
        }

        finalWorld = buildWorld(FINAL_WORLD_NAME, pool.get(count % pool.size()),
                UHCManager.get().getBorderDefaultSize());
    }

    private List<GenProfile> enabledProfiles() {
        List<GenProfile> profiles = new ArrayList<>();
        if (profilVillages) profiles.add(new GenProfile(PROFIL_VILLAGES, WorldType.NORMAL,
                "\"useVillages\":true,\"useTemples\":false,\"biomeSize\":7,\"goldCount\":1,\"goldSize\":4,\"goldMaxHeight\":20"));
        if (profilCavernes) profiles.add(new GenProfile(PROFIL_CAVERNES, WorldType.NORMAL,
                "\"seaLevel\":40,\"useCaves\":true,\"useRavines\":true,\"useMineShafts\":true,\"useDungeons\":true,\"dungeonChance\":16,\"useLavaLakes\":true,\"ironCount\":32"));
        if (profilNoye) profiles.add(new GenProfile(PROFIL_NOYE, WorldType.NORMAL,
                "\"seaLevel\":96,\"useWaterLakes\":true,\"useMonuments\":true,\"biomeSize\":6,\"riverSize\":6"));
        if (profilPlat) profiles.add(new GenProfile(PROFIL_PLAT, WorldType.FLAT,
                "3;minecraft:bedrock,60*minecraft:stone,3*minecraft:dirt,minecraft:grass;1;village,mineshaft,stronghold,decoration,dungeon,lake,lava_lake"));
        if (profilAmplifie) profiles.add(new GenProfile(PROFIL_AMPLIFIE, WorldType.AMPLIFIED,
                "\"seaLevel\":48,\"useRavines\":true,\"useCaves\":true,\"useStrongholds\":false"));
        if (profilDesert) profiles.add(new GenProfile(PROFIL_DESERT, WorldType.NORMAL,
                "\"fixedBiome\":2,\"useTemples\":true,\"useVillages\":true,\"useWaterLakes\":false,\"useMonuments\":false"));
        if (profilJungle) profiles.add(new GenProfile(PROFIL_JUNGLE, WorldType.NORMAL,
                "\"fixedBiome\":21,\"useTemples\":true,\"useMineShafts\":false,\"useCaves\":true,\"biomeSize\":6"));
        if (profilSauvage) profiles.add(new GenProfile(PROFIL_SAUVAGE, WorldType.NORMAL,
                "\"useVillages\":false,\"useTemples\":false,\"useMineShafts\":false,\"useStrongholds\":false,\"useMonuments\":false,\"useDungeons\":false,\"useRavines\":true"));
        if (profilEtendues) profiles.add(new GenProfile(PROFIL_ETENDUES, WorldType.LARGE_BIOMES,
                "\"useMineShafts\":true,\"useStrongholds\":true,\"biomeSize\":6,\"useVillages\":true"));
        if (profilFilons) profiles.add(new GenProfile(PROFIL_FILONS, WorldType.NORMAL,
                "\"useCaves\":false,\"useRavines\":false,\"useMineShafts\":true,\"diamondCount\":3,\"diamondMaxHeight\":12,\"goldCount\":8,\"goldMaxHeight\":24,\"ironCount\":28,\"ironMaxHeight\":48"));
        if (profiles.isEmpty()) profiles.add(new GenProfile(PROFIL_CLASSIQUE, WorldType.NORMAL, "\"useCaves\":true"));
        return profiles;
    }

    private World buildWorld(String name, GenProfile profile, double borderSize) {
        wipe(name);

        WorldCreator creator = new WorldCreator(name);
        creator.environment(World.Environment.NORMAL);
        creator.generateStructures(true);
        creator.type(profile.type());
        creator.seed(ThreadLocalRandom.current().nextLong());
        creator.generatorSettings(profile.type() == WorldType.FLAT
                ? profile.settings()
                : mergedSettings(profile.settings()));

        World world = creator.createWorld();
        if (world == null) return null;

        world.setKeepSpawnInMemory(false);
        world.setDifficulty(Difficulty.HARD);
        world.setGameRuleValue("naturalRegeneration", "false");
        world.setGameRuleValue("doFireTick", "false");
        world.setGameRuleValue("doDaylightCycle", "true");

        Location spawn = world.getSpawnLocation();
        WorldBorder border = world.getWorldBorder();
        border.setCenter(spawn.getBlockX(), spawn.getBlockZ());
        border.setSize(borderSize);
        border.setDamageAmount(UHCManager.get().getBorderDamageAmount());
        border.setDamageBuffer(UHCManager.get().getBorderDamageBuffer());
        return world;
    }

    private String mergedSettings(String extra) {
        String base = UHCWorldSettings.generateWorldSettings();
        return base.substring(0, base.length() - 1) + "," + extra + "}";
    }

    private void wipe(String name) {
        World loaded = Bukkit.getWorld(name);
        if (loaded != null) {
            Location fallback = Common.get().getLobbySpawn();
            if (fallback != null) loaded.getPlayers().forEach(player -> player.teleport(fallback));
            if (!Bukkit.unloadWorld(loaded, false)) return;
        }
        deleteRecursive(new File(Bukkit.getWorldContainer(), name));
    }

    private void deleteRecursive(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) deleteRecursive(child);
        }
        file.delete();
    }

    private Location spreadIn(World world) {
        WorldBorder border = world.getWorldBorder();
        double radius = Math.max(24.0, border.getSize() / 2.0 - 16.0);
        double centerX = border.getCenter().getX();
        double centerZ = border.getCenter().getZ();

        Location fallback = null;
        for (int attempt = 0; attempt < 8; attempt++) {
            int x = (int) (centerX + (ThreadLocalRandom.current().nextDouble() * 2 - 1) * radius);
            int z = (int) (centerZ + (ThreadLocalRandom.current().nextDouble() * 2 - 1) * radius);
            int y = world.getHighestBlockYAt(x, z);
            Location candidate = new Location(world, x + 0.5, y, z + 0.5);
            if (fallback == null) fallback = candidate;
            if (!world.getBlockAt(x, Math.max(0, y - 1), z).isLiquid()) return candidate;
        }
        return fallback;
    }
}
