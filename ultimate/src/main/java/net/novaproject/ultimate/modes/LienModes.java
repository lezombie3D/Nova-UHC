package net.novaproject.ultimate.modes;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.player.utils.PlayerUtils;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.role.Bonds;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.nms.DisguiseService;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.world.LobbyCreator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class LienModes {

    public static class LoveAtFirstLakeMode extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("mode.loveatfirstlake.desc",
                "§7Deux joueurs qui se tiennent dans le §bmême plan d'eau §7sont automatiquement liés : victoire commune et canal privé.");

        private static final BlockFace[] FACES = {
                BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN
        };

        @Var(name = "Taille max du lac", desc = "Nombre de blocs d'eau parcourus pour considérer deux joueurs dans le même plan d'eau.", type = VariableType.INTEGER, min = 8)
        private int tailleLacMax = 512;

        private final Set<UUID> lies = new HashSet<>();

        @Override
        public String getName() {
            return "Love at First Lake";
        }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override
        public ItemCreator getItem() {
            return new ItemCreator(Material.WATER_BUCKET);
        }

        @Override
        public boolean isSpecial() {
            return true;
        }

        @Override
        public void scatter(UHCPlayer up, Location location, HashMap<UHCTeam, Location> teamloc) {
            if (up.getPlayer() != null) up.getPlayer().teleport(location);
        }

        @Override
        public void onSec(Player p) {
            if (!isActive()) return;
            if (lies.contains(p.getUniqueId())) return;
            Block feet = p.getLocation().getBlock();
            if (!isWater(feet)) return;

            Set<String> lac = floodFill(feet);
            for (UHCPlayer other : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player o = other.getPlayer();
                if (o == null || o.getUniqueId().equals(p.getUniqueId())) continue;
                if (lies.contains(o.getUniqueId())) continue;
                if (!o.getWorld().equals(p.getWorld())) continue;
                if (!lac.contains(key(o.getLocation().getBlock()))) continue;

                lies.add(p.getUniqueId());
                lies.add(o.getUniqueId());
                Bonds.link(p.getUniqueId(), o.getUniqueId()).winTogether().announce().chat().apply();
                return;
            }
        }

        private Set<String> floodFill(Block start) {
            Set<String> visited = new HashSet<>();
            Deque<Block> queue = new ArrayDeque<>();
            visited.add(key(start));
            queue.add(start);
            while (!queue.isEmpty() && visited.size() < tailleLacMax) {
                Block current = queue.poll();
                for (BlockFace face : FACES) {
                    Block next = current.getRelative(face);
                    if (!isWater(next)) continue;
                    if (!visited.add(key(next))) continue;
                    queue.add(next);
                }
            }
            return visited;
        }

        private boolean isWater(Block block) {
            return block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER;
        }

        private String key(Block block) {
            return block.getX() + "," + block.getY() + "," + block.getZ();
        }

        @Override
        public void onStop() {
            super.onStop();
            lies.clear();
        }
    }

    public static class LoveWhenCoordsMatchMode extends Scenario {

        private static final String SECOND_WORLD = "world_coordsmatch";

        private static final DynamicLang DESC = DynamicLang.of("mode.lovewhencoordsmatch.desc",
                "§7La moitié des joueurs part dans un §bsecond monde§7. Deux joueurs de mondes différents aux §bmêmes coordonnées §7sont liés.");
        private static final DynamicLang OTHER_WORLD = DynamicLang.of("mode.lovewhencoordsmatch.otherworld",
                "§7Tu es dans le §bsecond monde§7. Rejoins les coordonnées de ton futur allié pour le lier.");
        private static final DynamicLang MAIN_WORLD = DynamicLang.of("mode.lovewhencoordsmatch.mainworld",
                "§7Tu es dans le §bmonde principal§7. Rejoins les coordonnées de ton futur allié pour le lier.");

        @Var(name = "Tolérance de coordonnées", desc = "Écart maximal en blocs sur X et Z pour lier deux joueurs.", type = VariableType.INTEGER, min = 0)
        private int toleranceBlocs = 5;

        private final Set<UUID> secondMonde = new HashSet<>();
        private final Set<UUID> lies = new HashSet<>();
        private int compteurScatter = 0;

        @Override
        public String getName() {
            return "Love When Coords Match";
        }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override
        public ItemCreator getItem() {
            return new ItemCreator(Material.COMPASS);
        }

        @Override
        public boolean isSpecial() {
            return true;
        }

        @Override
        public void enable() {
            super.enable();
            ensureSecondWorld();
        }

        @Override
        public void toggleActive() {
            super.toggleActive();
            ensureSecondWorld();
        }

        private void ensureSecondWorld() {
            if (isActive() && Bukkit.getWorld(SECOND_WORLD) == null) {
                LobbyCreator.cloneWorld(Common.get().getArenaName(), SECOND_WORLD);
            }
        }

        @Override
        public void scatter(UHCPlayer up, Location location, HashMap<UHCTeam, Location> teamloc) {
            Player player = up.getPlayer();
            if (player == null) return;

            World second = Bukkit.getWorld(SECOND_WORLD);
            if (second != null && compteurScatter++ % 2 == 1) {
                secondMonde.add(player.getUniqueId());
                player.teleport(new Location(second, location.getX(), location.getY(), location.getZ(),
                        location.getYaw(), location.getPitch()));
                player.sendMessage(t(OTHER_WORLD, player));
                return;
            }
            player.teleport(location);
            player.sendMessage(t(MAIN_WORLD, player));
        }

        @Override
        public void onSec(Player p) {
            if (!isActive()) return;
            if (lies.contains(p.getUniqueId())) return;

            boolean pSecond = secondMonde.contains(p.getUniqueId());
            for (UHCPlayer other : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player o = other.getPlayer();
                if (o == null || o.getUniqueId().equals(p.getUniqueId())) continue;
                if (lies.contains(o.getUniqueId())) continue;
                if (secondMonde.contains(o.getUniqueId()) == pSecond) continue;
                if (Math.abs(o.getLocation().getBlockX() - p.getLocation().getBlockX()) > toleranceBlocs) continue;
                if (Math.abs(o.getLocation().getBlockZ() - p.getLocation().getBlockZ()) > toleranceBlocs) continue;

                lies.add(p.getUniqueId());
                lies.add(o.getUniqueId());
                Bonds.link(p.getUniqueId(), o.getUniqueId()).winTogether().announce().chat().apply();
                return;
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            secondMonde.clear();
            lies.clear();
            compteurScatter = 0;
            if (Bukkit.getWorld(SECOND_WORLD) != null) {
                World arena = Common.get().getArena();
                LobbyCreator.deleteWorld(SECOND_WORLD,
                        (arena != null ? arena : Bukkit.getWorlds().get(0)).getSpawnLocation());
            }
        }
    }

    public static class AbsorptionPartnerMode extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("mode.absorptionpartner.desc",
                "§7Les pommes d'or donnent leurs cœurs d'absorption à ton §epartenaire §7à la place de toi.");
        private static final DynamicLang GIVEN = DynamicLang.of("mode.absorptionpartner.given",
                "§7Ton absorption est partie chez §e%player%§7.");
        private static final DynamicLang RECEIVED = DynamicLang.of("mode.absorptionpartner.received",
                "§e%player% §7t'offre l'absorption de sa pomme d'or.");

        private final Map<UUID, UUID> partenaires = new HashMap<>();

        @Override
        public String getName() {
            return "Absorption Partner";
        }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override
        public ItemCreator getItem() {
            return new ItemCreator(Material.GOLDEN_APPLE);
        }

        @Override
        public boolean isSpecial() {
            return true;
        }

        @Override
        public void scatter(UHCPlayer up, Location location, HashMap<UHCTeam, Location> teamloc) {
            if (up.getPlayer() != null) up.getPlayer().teleport(location);
        }

        @Override
        public void onGameStart() {
            if (!isActive()) return;

            List<List<UHCPlayer>> groupes = new ArrayList<>();
            Set<UUID> deja = new HashSet<>();
            for (UHCTeam team : UHCTeamManager.get().getTeams()) {
                List<UHCPlayer> membres = new ArrayList<>();
                for (UHCPlayer up : team.getPlayers()) {
                    if (up.getPlayer() != null && up.isPlaying()) membres.add(up);
                }
                if (membres.size() < 2) continue;
                groupes.add(membres);
                for (UHCPlayer up : membres) deja.add(up.getUniqueId());
            }

            List<UHCPlayer> solos = new ArrayList<>();
            for (UHCPlayer up : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                if (!deja.contains(up.getUniqueId())) solos.add(up);
            }
            groupes.add(solos);

            for (List<UHCPlayer> groupe : groupes) {
                Collections.shuffle(groupe);
                for (int i = 0; i + 1 < groupe.size(); i += 2) {
                    Player a = groupe.get(i).getPlayer();
                    Player b = groupe.get(i + 1).getPlayer();
                    if (a == null || b == null) continue;
                    partenaires.put(a.getUniqueId(), b.getUniqueId());
                    partenaires.put(b.getUniqueId(), a.getUniqueId());
                    Bonds.link(a.getUniqueId(), b.getUniqueId()).announce().apply();
                }
            }
        }

        @Override
        public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
            if (!isActive()) return;
            if (item == null || item.getType() != Material.GOLDEN_APPLE) return;

            UUID partenaire = partenaires.get(player.getUniqueId());
            if (partenaire == null) return;

            Bukkit.getScheduler().runTask(Main.get(), () -> transfer(player, partenaire));
        }

        private void transfer(Player player, UUID partenaire) {
            if (!player.isOnline()) return;
            Player receveur = Bukkit.getPlayer(partenaire);
            if (receveur == null || !receveur.isOnline()) return;

            UHCPlayer up = UHCPlayerManager.get().getPlayer(receveur);
            if (up == null || !up.isPlaying()) return;

            PotionEffect absorption = null;
            for (PotionEffect effect : player.getActivePotionEffects()) {
                if (effect.getType().equals(PotionEffectType.ABSORPTION)) absorption = effect;
            }
            if (absorption == null) return;

            double coeurs = PlayerUtils.getAbsorptionHearts(player);
            player.removePotionEffect(PotionEffectType.ABSORPTION);
            PlayerUtils.setAbsorptionHearts(player, 0.0);

            receveur.removePotionEffect(PotionEffectType.ABSORPTION);
            receveur.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,
                    absorption.getDuration(), absorption.getAmplifier(), false, true), true);
            if (coeurs > 0) PlayerUtils.setAbsorptionHearts(receveur, coeurs);

            player.sendMessage(t(GIVEN, player, Map.of("%player%", receveur.getName())));
            receveur.sendMessage(t(RECEIVED, receveur, Map.of("%player%", player.getName())));
        }

        @Override
        public void onStop() {
            super.onStop();
            partenaires.clear();
        }
    }

    public static class InfiniteAlliesMode extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("mode.infiniteallies.desc",
                "§7Clic droit sur un joueur pour lui proposer une alliance : §aaucune limite d'alliés§7, mais §cun seul peut gagner§7.");
        private static final DynamicLang PROPOSED = DynamicLang.of("mode.infiniteallies.proposed",
                "§7Alliance proposée à §e%player%§7.");
        private static final DynamicLang INVITED = DynamicLang.of("mode.infiniteallies.invited",
                "§e%player% §7te propose une alliance. Clic droit sur lui pour accepter.");

        private final Set<String> demandes = new HashSet<>();
        private final Set<String> alliances = new HashSet<>();

        @Override
        public String getName() {
            return "Infinite Allies";
        }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override
        public ItemCreator getItem() {
            return new ItemCreator(Material.BOOK);
        }

        @Override
        public boolean isSpecial() {
            return true;
        }

        @Override
        public void scatter(UHCPlayer up, Location location, HashMap<UHCTeam, Location> teamloc) {
            if (up.getPlayer() != null) up.getPlayer().teleport(location);
        }

        @Override
        public void onPlayerInteractEntity(Player player, PlayerInteractEntityEvent event) {
            if (!isActive() || !UHCManager.get().isGame()) return;
            if (!(event.getRightClicked() instanceof Player cible)) return;

            UHCPlayer source = UHCPlayerManager.get().getPlayer(player);
            UHCPlayer target = UHCPlayerManager.get().getPlayer(cible);
            if (source == null || !source.isPlaying()) return;
            if (target == null || !target.isPlaying()) return;

            String cle = paire(player.getUniqueId(), cible.getUniqueId());
            if (alliances.contains(cle)) return;

            if (demandes.remove(cible.getUniqueId() + ">" + player.getUniqueId())) {
                alliances.add(cle);
                Bonds.link(player.getUniqueId(), cible.getUniqueId()).announce().apply();
                return;
            }

            if (!demandes.add(player.getUniqueId() + ">" + cible.getUniqueId())) return;
            player.sendMessage(t(PROPOSED, player, Map.of("%player%", cible.getName())));
            cible.sendMessage(t(INVITED, cible, Map.of("%player%", player.getName())));
        }

        private String paire(UUID first, UUID second) {
            return first.compareTo(second) < 0 ? first + "|" + second : second + "|" + first;
        }

        @Override
        public void onStop() {
            super.onStop();
            demandes.clear();
            alliances.clear();
        }
    }

    public static class YourNameMode extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("mode.yourname.desc",
                "§7Régulièrement, les coéquipiers §béchangent leur position §7et prennent l'§bapparence §7de celui dont ils occupent la place.");
        private static final DynamicLang SWAP_TITLE = DynamicLang.of("mode.yourname.swaptitle",
                "§b✦ Échange d'identité");
        private static final DynamicLang SWAP_SUBTITLE = DynamicLang.of("mode.yourname.swapsubtitle",
                "§7Tu es maintenant §b%player%");

        @Var(name = "Intervalle d'échange", desc = "Temps entre deux échanges de position et d'apparence.", type = VariableType.TIME, min = 1)
        private int intervalleSec = 600;

        @Var(name = "Durée du titre", desc = "Durée d'affichage du titre lors d'un échange.", type = VariableType.TIME, min = 1)
        private int dureeTitreSec = 3;

        private int dernierCycle = -1;

        @Override
        public String getName() {
            return "Your Name";
        }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override
        public ItemCreator getItem() {
            return new ItemCreator(Material.SKULL_ITEM);
        }

        @Override
        public boolean isSpecial() {
            return true;
        }

        @Override
        public void enable() {
            super.enable();
            forcerEquipes();
        }

        @Override
        public void toggleActive() {
            super.toggleActive();
            forcerEquipes();
        }

        @Override
        public void onTeamUpdate() {
            forcerEquipes();
        }

        private void forcerEquipes() {
            if (isActive() && UHCManager.get().getTeam_size() < 2) {
                UHCManager.get().setTeam_size(2);
            }
        }

        @Override
        public void scatter(UHCPlayer up, Location location, HashMap<UHCTeam, Location> teamloc) {
            if (up.getPlayer() != null) up.getPlayer().teleport(location);
        }

        @Override
        public void onSec(Player p) {
            if (!isActive()) return;
            int timer = UHCManager.get().getTimer();
            if (timer <= 0 || intervalleSec <= 0 || timer % intervalleSec != 0) return;

            int cycle = timer / intervalleSec;
            if (cycle == dernierCycle) return;
            dernierCycle = cycle;

            for (UHCTeam team : UHCTeamManager.get().getTeams()) echangerEquipe(team);
        }

        private void echangerEquipe(UHCTeam team) {
            List<Player> membres = new ArrayList<>();
            for (UHCPlayer up : team.getPlayers()) {
                Player membre = up.getPlayer();
                if (membre != null && membre.isOnline() && up.isPlaying()) membres.add(membre);
            }
            if (membres.size() < 2) return;

            for (Player membre : membres) DisguiseService.get().undisguise(membre);

            List<Location> positions = new ArrayList<>();
            List<String> noms = new ArrayList<>();
            for (Player membre : membres) {
                positions.add(membre.getLocation());
                noms.add(membre.getName());
            }

            for (int i = 0; i < membres.size(); i++) {
                int suivant = (i + 1) % membres.size();
                Player courant = membres.get(i);
                courant.teleport(positions.get(suivant));
                DisguiseService.get().disguise(courant, membres.get(suivant));
                DisplayService.title(courant, t(SWAP_TITLE, courant),
                        t(SWAP_SUBTITLE, courant, Map.of("%player%", noms.get(suivant))), 20 * dureeTitreSec);
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            for (Player player : Bukkit.getOnlinePlayers()) DisguiseService.get().undisguise(player);
            dernierCycle = -1;
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new LoveAtFirstLakeMode(),
                new LoveWhenCoordsMatchMode(),
                new AbsorptionPartnerMode(),
                new InfiniteAlliesMode(),
                new YourNameMode()
        );
    }
}
