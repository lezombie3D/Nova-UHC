package net.novaproject.ultimate.modes;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.event.UhcGameEvents.UhcGameStartEvent;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public final class CibleModes {

    private static final Random RANDOM = new Random();

    static void scatterDefault(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        Player player = uhcPlayer.getPlayer();
        if (player == null) return;
        if (UHCManager.get().getTeam_size() != 1 && uhcPlayer.getTeam().isPresent()) {
            UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
        } else {
            player.teleport(location);
        }
    }

    public static class AssassinsMode extends Scenario {

        @Var(name = "Tête sur kill hors-cible", desc = "Un kill qui n'est pas ta cible ne laisse que la tête de la victime.", type = VariableType.BOOLEAN)
        private boolean dropHeadOnNonTarget = true;

        @Var(name = "Héritage de cible", desc = "Quand ta cible meurt, tu hérites de la cible de ta victime.", type = VariableType.BOOLEAN)
        private boolean inheritTarget = true;

        protected final Map<UUID, UUID> targets = new HashMap<>();

        @Override
        public String getName() {
            return "Assassins";
        }

        @Override
        public String getDescription(Player player) {
            return t(DynamicLang.of("mode.assassins.desc",
                    "§7Au PvP, chaque joueur reçoit une §ccible secrète§7. Tuer ta cible fait tomber tout son stuff, les autres kills non."), player);
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
        public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
            scatterDefault(uhcPlayer, location, teamloc);
        }

        @Override
        public void onPvP() {
            if (!isActive()) return;
            List<UUID> alive = new ArrayList<>();
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                alive.add(uhcPlayer.getUuid());
            }
            if (alive.size() < 2) return;
            Collections.shuffle(alive);
            targets.clear();
            for (int i = 0; i < alive.size(); i++) {
                UUID hunter = alive.get(i);
                UUID prey = alive.get((i + 1) % alive.size());
                targets.put(hunter, prey);
                sendTarget(hunter, prey);
            }
        }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            UUID victim = uhcPlayer.getUuid();
            boolean wasTarget = killer != null && victim.equals(targets.get(killer.getUuid()));

            if (restrictDrops() && !targets.isEmpty() && !wasTarget) {
                event.getDrops().clear();
                if (dropHeadOnNonTarget && uhcPlayer.getPlayer() != null) {
                    event.getDrops().add(headOf(uhcPlayer.getPlayer().getName()));
                }
            }

            UUID chained = targets.remove(victim);
            UUID hunter = hunterOf(victim);
            if (hunter == null) return;
            if (inheritTarget && chained != null && !chained.equals(hunter)) {
                targets.put(hunter, chained);
                sendTarget(hunter, chained);
            } else {
                targets.remove(hunter);
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            targets.clear();
        }

        protected boolean restrictDrops() {
            return true;
        }

        protected UUID hunterOf(UUID prey) {
            for (Map.Entry<UUID, UUID> entry : targets.entrySet()) {
                if (entry.getValue().equals(prey)) return entry.getKey();
            }
            return null;
        }

        private void sendTarget(UUID hunter, UUID prey) {
            Player hunterPlayer = Bukkit.getPlayer(hunter);
            Player preyPlayer = Bukkit.getPlayer(prey);
            if (hunterPlayer == null || preyPlayer == null) return;
            LangManager.get().send(DynamicLang.of("mode.assassins.target", "§8» §7Ta cible : §c%player%"),
                    hunterPlayer, Map.of("%player%", preyPlayer.getName()));
            DisplayService.title(hunterPlayer,
                    t(DynamicLang.of("mode.assassins.target-title", "§cContrat"), hunterPlayer),
                    preyPlayer.getName(), 60);
            hunterPlayer.playSound(hunterPlayer.getLocation(), Sound.NOTE_PLING, 1f, 0.6f);
        }

        private ItemStack headOf(String owner) {
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwner(owner);
            skull.setItemMeta(meta);
            return skull;
        }
    }

    public static class ParassassinsMode extends AssassinsMode {

        @Override
        public String getName() {
            return "Parassassins";
        }

        @Override
        public String getDescription(Player player) {
            return t(DynamicLang.of("mode.parassassins.desc",
                    "§7Miner de l'or ou du diamant, crafter une table d'enchantement, une enclume, une pomme d'or ou une tête, ou mourir, diffuse des coordonnées §7???§7. Ta cible, elle, t'est révélée nommément. Tous les kills droppent le stuff."), player);
        }

        @Override
        public ItemCreator getItem() {
            return new ItemCreator(Material.EYE_OF_ENDER);
        }

        @Override
        protected boolean restrictDrops() {
            return false;
        }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            if (block.getType() != Material.GOLD_ORE && block.getType() != Material.DIAMOND_ORE) return;
            parafusion(player);
        }

        @Override
        public void onCraft(ItemStack result, CraftItemEvent event) {
            if (!isActive()) return;
            if (result == null) return;
            Material type = result.getType();
            if (type != Material.ENCHANTMENT_TABLE && type != Material.ANVIL
                    && type != Material.GOLDEN_APPLE && type != Material.SKULL_ITEM) return;
            if (event.getWhoClicked() instanceof Player player) parafusion(player);
        }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            super.onDeath(uhcPlayer, killer, event);
            if (!isActive()) return;
            if (uhcPlayer.getPlayer() != null) parafusion(uhcPlayer.getPlayer());
        }

        private void parafusion(Player source) {
            Location location = source.getLocation();
            Map<String, Object> coords = Map.of("%x%", location.getBlockX(), "%z%", location.getBlockZ());
            LangManager.get().sendAll(DynamicLang.of("mode.parassassins.blur", "§8» §7??? §fen §7%x% §f/ §7%z%"), coords);

            UUID hunter = hunterOf(source.getUniqueId());
            if (hunter == null) return;
            Player hunterPlayer = Bukkit.getPlayer(hunter);
            if (hunterPlayer == null) return;
            LangManager.get().send(DynamicLang.of("mode.parassassins.target-trace",
                            "§8» §cTa cible §f%player% §cest en §f%x% §c/ §f%z%"), hunterPlayer,
                    Map.of("%player%", source.getName(), "%x%", location.getBlockX(), "%z%", location.getBlockZ()));
            hunterPlayer.playSound(hunterPlayer.getLocation(), Sound.NOTE_PLING, 1f, 1.4f);
        }
    }

    public static class TeamAssassinsMode extends Scenario {

        @Var(name = "Diamants requis", desc = "Diamants minés par l'équipe pour obtenir les coordonnées de la cible.", type = VariableType.INTEGER, min = 1)
        private int diamondsRequired = 16;

        @Var(name = "Or requis", desc = "Or miné par l'équipe pour obtenir les coordonnées de la cible.", type = VariableType.INTEGER, min = 1)
        private int goldRequired = 48;

        @Var(name = "Blocs parcourus requis", desc = "Distance cumulée parcourue par l'équipe pour obtenir les coordonnées de la cible.", type = VariableType.INTEGER, min = 1)
        private int blocksRequired = 1000;

        @Var(name = "Un kill donne les coordonnées", desc = "Un kill offre immédiatement les coordonnées de l'équipe cible.", type = VariableType.BOOLEAN)
        private boolean killGrantsCoords = true;

        private final Map<UHCTeam, UHCTeam> hunted = new HashMap<>();
        private final Map<UHCTeam, Integer> diamonds = new HashMap<>();
        private final Map<UHCTeam, Integer> gold = new HashMap<>();
        private final Map<UHCTeam, Double> travelled = new HashMap<>();

        @Override
        public String getName() {
            return "Team Assassins";
        }

        @Override
        public String getDescription(Player player) {
            return t(DynamicLang.of("mode.team-assassins.desc",
                    "§7Chaque équipe chasse une §céquipe cible§7. Ses coordonnées se méritent : diamants minés, or miné, distance parcourue ou un kill."), player);
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
        public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
            scatterDefault(uhcPlayer, location, teamloc);
        }

        @Override
        public void onPvP() {
            if (!isActive()) return;
            List<UHCTeam> teams = new ArrayList<>(UHCTeamManager.get().getAliveTeams());
            if (teams.size() < 2) return;
            Collections.shuffle(teams);
            hunted.clear();
            for (int i = 0; i < teams.size(); i++) {
                UHCTeam team = teams.get(i);
                UHCTeam prey = teams.get((i + 1) % teams.size());
                hunted.put(team, prey);
                for (UHCPlayer member : team.getPlayers()) {
                    if (member.getPlayer() == null) continue;
                    LangManager.get().send(DynamicLang.of("mode.team-assassins.target", "§8» §7Votre équipe cible : §c%team%"),
                            member.getPlayer(), Map.of("%team%", prey.prefix() + prey.name()));
                }
            }
        }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive() || hunted.isEmpty()) return;
            UHCTeam team = teamOf(player);
            if (team == null) return;
            if (block.getType() == Material.DIAMOND_ORE) {
                int mined = diamonds.getOrDefault(team, 0) + 1;
                if (mined >= diamondsRequired) {
                    diamonds.put(team, 0);
                    grant(team);
                } else {
                    diamonds.put(team, mined);
                }
            } else if (block.getType() == Material.GOLD_ORE) {
                int mined = gold.getOrDefault(team, 0) + 1;
                if (mined >= goldRequired) {
                    gold.put(team, 0);
                    grant(team);
                } else {
                    gold.put(team, mined);
                }
            }
        }

        @Override
        public void onMove(Player player, PlayerMoveEvent event) {
            if (!isActive() || hunted.isEmpty()) return;
            if (event.getTo() == null || event.getFrom().getWorld() != event.getTo().getWorld()) return;
            UHCTeam team = teamOf(player);
            if (team == null) return;
            double distance = travelled.getOrDefault(team, 0.0) + event.getFrom().distance(event.getTo());
            if (distance >= blocksRequired) {
                travelled.put(team, 0.0);
                grant(team);
            } else {
                travelled.put(team, distance);
            }
        }

        @Override
        public void onKill(UHCPlayer killer, UHCPlayer victim) {
            if (!isActive() || !killGrantsCoords) return;
            killer.getTeam().ifPresent(this::grant);
        }

        @Override
        public void onStop() {
            super.onStop();
            hunted.clear();
            diamonds.clear();
            gold.clear();
            travelled.clear();
        }

        private UHCTeam teamOf(Player player) {
            UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
            return uhcPlayer == null ? null : uhcPlayer.getTeam().orElse(null);
        }

        private void grant(UHCTeam team) {
            UHCTeam prey = hunted.get(team);
            if (prey == null) return;
            List<UHCPlayer> preys = prey.getPlayers();
            if (preys.isEmpty()) return;
            UHCPlayer spotted = preys.get(RANDOM.nextInt(preys.size()));
            if (spotted.getPlayer() == null) return;
            Location location = spotted.getPlayer().getLocation();
            for (UHCPlayer member : team.getPlayers()) {
                if (member.getPlayer() == null) continue;
                LangManager.get().send(DynamicLang.of("mode.team-assassins.coords",
                                "§8» §c%player% §7repéré en §f%x% §7/ §f%z%"), member.getPlayer(),
                        Map.of("%player%", spotted.getPlayer().getName(), "%x%", location.getBlockX(), "%z%", location.getBlockZ()));
                member.getPlayer().playSound(member.getPlayer().getLocation(), Sound.NOTE_PLING, 1f, 1.2f);
            }
        }
    }

    public static class CaptainsMode extends Scenario implements Listener {

        @Var(name = "Temps de choix", desc = "Secondes laissées au capitaine avant un choix automatique.", type = VariableType.TIME, min = 5)
        private int pickTimeoutSec = 30;

        private final List<UUID> pool = new ArrayList<>();
        private final List<UUID> order = new ArrayList<>();
        private final List<UUID> choices = new ArrayList<>();
        private final Map<UUID, UHCTeam> captainTeams = new LinkedHashMap<>();
        private volatile UUID currentPicker;
        private int pickIndex = 0;
        private boolean drafting = false;
        private boolean drafted = false;
        private BukkitRunnable pickTask;

        @Override
        public String getName() {
            return "Captains";
        }

        @Override
        public String getDescription(Player player) {
            return t(DynamicLang.of("mode.captains.desc",
                    "§7Des capitaines draftent les joueurs en §eserpentin§7 (1,2,3,4,4,3,2,1) avant le début de la partie."), player);
        }

        @Override
        public ItemCreator getItem() {
            return new ItemCreator(Material.NAME_TAG);
        }

        @Override
        public boolean isSpecial() {
            return true;
        }

        @Override
        public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
            scatterDefault(uhcPlayer, location, teamloc);
        }

        @EventHandler
        public void onGameStartRequest(UhcGameStartEvent event) {
            if (!isActive() || drafted) return;
            if (event.isForced() || UHCManager.get().getTeam_size() < 2) return;
            event.setCancelled(true);
            if (drafting) return;
            startDraft();
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onCaptainChat(AsyncPlayerChatEvent event) {
            UUID picker = currentPicker;
            if (!isActive() || picker == null || !event.getPlayer().getUniqueId().equals(picker)) return;
            event.setCancelled(true);

            String typed = event.getMessage().trim();
            Player captain = event.getPlayer();
            Bukkit.getScheduler().runTask(Main.get(), () -> resolvePick(captain, typed));
        }

        @Override
        public void onStop() {
            super.onStop();
            cancelPickTask();
            currentPicker = null;
            pool.clear();
            order.clear();
            choices.clear();
            captainTeams.clear();
            pickIndex = 0;
            drafting = false;
            drafted = false;
        }

        protected List<UUID> candidates() {
            return new ArrayList<>(pool);
        }

        private void startDraft() {
            List<UUID> players = new ArrayList<>();
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                players.add(uhcPlayer.getUuid());
            }
            List<UHCTeam> teams = new ArrayList<>(UHCTeamManager.get().getTeams());
            int teamSize = UHCManager.get().getTeam_size();
            int captainCount = Math.min(teams.size(), (players.size() + teamSize - 1) / teamSize);

            if (captainCount < 2) {
                drafted = true;
                LangManager.get().sendAll(DynamicLang.of("mode.captains.impossible", "§8» §cPas assez d'équipes ou de joueurs pour drafter."));
                Bukkit.getScheduler().runTask(Main.get(), () -> UHCManager.get().onStart());
                return;
            }

            drafting = true;
            pickIndex = 0;
            currentPicker = null;
            captainTeams.clear();
            pool.clear();
            order.clear();
            Collections.shuffle(players);
            UHCTeamManager.get().setSelectionMode(UHCTeamManager.TeamSelectionMode.CHOSEN);
            UHCTeamManager.get().clearAssignments();

            for (int i = 0; i < captainCount; i++) {
                UUID captain = players.remove(0);
                UHCTeam team = teams.get(i);
                captainTeams.put(captain, team);
                UHCPlayer uhcCaptain = UHCPlayerManager.get().getPlayer(captain);
                if (uhcCaptain != null) uhcCaptain.forceSetTeam(Optional.of(team));
                Player captainPlayer = Bukkit.getPlayer(captain);
                if (captainPlayer != null) {
                    LangManager.get().sendAll(DynamicLang.of("mode.captains.captain-named", "§8» §e%player% §7est capitaine de §f%team%"),
                            Map.of("%player%", captainPlayer.getName(), "%team%", team.prefix() + team.name()));
                }
            }
            pool.addAll(players);

            List<UUID> captains = new ArrayList<>(captainTeams.keySet());
            for (int round = 0; round < teamSize - 1; round++) {
                List<UUID> line = new ArrayList<>(captains);
                if (round % 2 == 1) Collections.reverse(line);
                order.addAll(line);
            }
            while (order.size() > pool.size()) order.remove(order.size() - 1);

            LangManager.get().sendAll(DynamicLang.of("mode.captains.start", "§8» §7La draft commence. Les capitaines écrivent le pseudo de leur choix dans le chat."));
            nextPick();
        }

        private void nextPick() {
            cancelPickTask();
            currentPicker = null;
            if (pickIndex >= order.size() || pool.isEmpty()) {
                finishDraft();
                return;
            }

            Player captain = Bukkit.getPlayer(order.get(pickIndex));
            choices.clear();
            choices.addAll(candidates());
            if (captain == null || choices.isEmpty()) {
                autoPick();
                return;
            }
            currentPicker = captain.getUniqueId();

            List<String> names = new ArrayList<>();
            for (UUID candidate : choices) {
                Player player = Bukkit.getPlayer(candidate);
                if (player != null) names.add(player.getName());
            }

            LangManager.get().sendAll(DynamicLang.of("mode.captains.turn", "§8» §7Au tour de §e%player% §7de choisir."),
                    Map.of("%player%", captain.getName()));
            LangManager.get().send(DynamicLang.of("mode.captains.your-turn", "§8» §7Choisis dans le chat : §f%players%"),
                    captain, Map.of("%players%", String.join("§7, §f", names)));
            DisplayService.title(captain, t(DynamicLang.of("mode.captains.your-turn-title", "§eÀ toi de drafter"), captain), "", 60);
            startPickTask(captain.getUniqueId());
        }

        private void startPickTask(UUID captain) {
            pickTask = new BukkitRunnable() {
                private int remaining = pickTimeoutSec;

                @Override
                public void run() {
                    Player player = Bukkit.getPlayer(captain);
                    if (player != null) {
                        DisplayService.actionBar(player, LangManager.get().get(
                                DynamicLang.of("mode.captains.countdown", "§7Choix dans §e%seconds%s"),
                                player, Map.of("%seconds%", remaining)));
                    }
                    if (remaining <= 0) {
                        cancel();
                        autoPick();
                        return;
                    }
                    remaining--;
                }
            };
            pickTask.runTaskTimer(Main.get(), 0L, 20L);
        }

        private void autoPick() {
            if (choices.isEmpty()) {
                finishDraft();
                return;
            }
            pick(choices.get(RANDOM.nextInt(choices.size())));
        }

        private void resolvePick(Player captain, String typed) {
            if (!drafting || !captain.getUniqueId().equals(currentPicker)) return;
            for (UUID candidate : choices) {
                Player player = Bukkit.getPlayer(candidate);
                if (player != null && player.getName().equalsIgnoreCase(typed)) {
                    pick(candidate);
                    return;
                }
            }
            LangManager.get().send(DynamicLang.of("mode.captains.unknown", "§8» §cCe joueur n'est pas dans ta liste."), captain);
        }

        private void pick(UUID picked) {
            if (pickIndex >= order.size() || !pool.remove(picked)) return;
            currentPicker = null;
            cancelPickTask();

            UUID captain = order.get(pickIndex);
            UHCTeam team = captainTeams.get(captain);
            UHCPlayer uhcPicked = UHCPlayerManager.get().getPlayer(picked);
            if (uhcPicked != null && team != null) uhcPicked.forceSetTeam(Optional.of(team));

            Player pickedPlayer = Bukkit.getPlayer(picked);
            Player captainPlayer = Bukkit.getPlayer(captain);
            if (pickedPlayer != null && captainPlayer != null) {
                LangManager.get().sendAll(DynamicLang.of("mode.captains.picked", "§8» §e%captain% §7drafte §f%player%"),
                        Map.of("%captain%", captainPlayer.getName(), "%player%", pickedPlayer.getName()));
            }

            pickIndex++;
            nextPick();
        }

        private void finishDraft() {
            cancelPickTask();
            currentPicker = null;
            choices.clear();
            drafting = false;
            drafted = true;
            LangManager.get().sendAll(DynamicLang.of("mode.captains.done", "§8» §aDraft terminée, la partie démarre."));
            Bukkit.getScheduler().runTaskLater(Main.get(), () -> UHCManager.get().onStart(), 60L);
        }

        private void cancelPickTask() {
            if (pickTask == null) return;
            pickTask.cancel();
            pickTask = null;
        }
    }

    public static class DraftersMode extends CaptainsMode {

        @Var(name = "Taille de la liste", desc = "Nombre de joueurs tirés au sort proposés au capitaine à chaque tour.", type = VariableType.INTEGER, min = 2)
        private int candidateCount = 5;

        @Override
        public String getName() {
            return "Drafters";
        }

        @Override
        public String getDescription(Player player) {
            return t(DynamicLang.of("mode.drafters.desc",
                    "§7Comme Captains, mais chaque capitaine ne choisit que dans une §eliste restreinte§7 tirée au sort, pour que personne ne reste sur le banc."), player);
        }

        @Override
        public ItemCreator getItem() {
            return new ItemCreator(Material.PAPER);
        }

        @Override
        protected List<UUID> candidates() {
            List<UUID> shuffled = super.candidates();
            Collections.shuffle(shuffled);
            return new ArrayList<>(shuffled.subList(0, Math.min(candidateCount, shuffled.size())));
        }
    }

    public static List<Scenario> all() {
        return List.of(new AssassinsMode(), new TeamAssassinsMode(), new ParassassinsMode(),
                new CaptainsMode(), new DraftersMode());
    }
}
