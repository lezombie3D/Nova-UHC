package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SimonSays extends Scenario {

    /* ============================= */
    /*         VARIABLES             */
    /* ============================= */

    @ScenarioVariable(
            name = "Min Delay",
            description = "Temps minimum (en secondes) entre deux commandes de Simon.",
            type = VariableType.INTEGER
    )
    private int min_delay = 120;

    @ScenarioVariable(
            name = "Max Delay",
            description = "Temps maximum (en secondes) entre deux commandes de Simon.",
            type = VariableType.INTEGER
    )
    private int max_delay = 300;

    @ScenarioVariable(
            name = "Jump Duration",
            description = "Durée (en secondes) pour exécuter la commande SAUTER.",
            type = VariableType.INTEGER
    )
    private int jump_duration = 15;

    @ScenarioVariable(
            name = "Crouch Duration",
            description = "Durée (en secondes) pour exécuter la commande S'ACCROUPIR.",
            type = VariableType.INTEGER
    )
    private int crouch_duration = 10;

    @ScenarioVariable(
            name = "Move Duration",
            description = "Durée (en secondes) pour les commandes de déplacement.",
            type = VariableType.INTEGER
    )
    private int move_duration = 20;

    @ScenarioVariable(
            name = "Stop Duration",
            description = "Durée (en secondes) pour la commande ARRÊTER DE BOUGER.",
            type = VariableType.INTEGER
    )
    private int stop_duration = 10;

    @ScenarioVariable(
            name = "Penalty Damage",
            description = "Activer la pénalité de dégâts.",
            type = VariableType.BOOLEAN
    )
    private boolean penalty_damage = true;

    @ScenarioVariable(
            name = "Penalty Hunger",
            description = "Activer la pénalité de faim.",
            type = VariableType.BOOLEAN
    )
    private boolean penalty_hunger = true;

    @ScenarioVariable(
            name = "Penalty Effects",
            description = "Activer les pénalités d'effets (slowness, blindness, weakness).",
            type = VariableType.BOOLEAN
    )
    private boolean penalty_effects = true;

    /* ============================= */
    /*        LOGIQUE INTERNE        */
    /* ============================= */

    private final Set<UUID> playersWhoComplied = new HashSet<>();
    private final Random random = new Random();
    private BukkitRunnable simonTask;
    private SimonCommand currentCommand;
    private boolean commandActive = false;
    private int commandTimeLeft = 0;

    @Override
    public String getName() {
        return "SimonSays";
    }

    @Override
    public String getDescription() {
        return "Suivez les commandes de Simon ou subissez des pénalités !";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.COMMAND);
    }

    @Override
    public void onGameStart() {
        startSimonTask();
    }

    private void startSimonTask() {
        if (simonTask != null) simonTask.cancel();

        simonTask = (BukkitRunnable) new BukkitRunnable() {
            private int timer = 0;
            private int nextCommandIn = getRandomDelay();

            @Override
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }

                timer++;

                if (!commandActive) {
                    if (timer >= nextCommandIn) {
                        issueRandomCommand();
                        timer = 0;
                        nextCommandIn = getRandomDelay();
                    } else if (nextCommandIn - timer == 30) {
                        Bukkit.broadcastMessage("§6[SimonSays] §fSimon va bientôt donner un ordre...");
                    }
                } else {
                    commandTimeLeft--;

                    if (commandTimeLeft <= 0) {
                        endCommand();
                    } else if (commandTimeLeft == 5) {
                        Bukkit.broadcastMessage("§6[SimonSays] §c5 secondes restantes !");
                    }
                }
            }
        }.runTaskTimer(Main.get(), 0, 20);
    }

    private int getRandomDelay() {
        return min_delay + random.nextInt(Math.max(1, max_delay - min_delay + 1));
    }

    private void issueRandomCommand() {
        List<SimonCommand> commands = Arrays.asList(
                new SimonCommand("JUMP", "Sautez 3 fois !", jump_duration, CommandType.JUMP),
                new SimonCommand("CROUCH", "Accroupissez-vous pendant 5 secondes !", crouch_duration, CommandType.CROUCH),
                new SimonCommand("BREAK_BLOCK", "Cassez un bloc !", move_duration, CommandType.BREAK_BLOCK),
                new SimonCommand("PLACE_BLOCK", "Placez un bloc !", move_duration, CommandType.PLACE_BLOCK),
                new SimonCommand("MOVE_NORTH", "Déplacez-vous vers le Nord !", move_duration, CommandType.MOVE_NORTH),
                new SimonCommand("MOVE_SOUTH", "Déplacez-vous vers le Sud !", move_duration, CommandType.MOVE_SOUTH),
                new SimonCommand("STOP_MOVING", "Arrêtez de bouger !", stop_duration, CommandType.STOP_MOVING)
        );

        currentCommand = commands.get(random.nextInt(commands.size()));
        commandActive = true;
        commandTimeLeft = currentCommand.duration;
        playersWhoComplied.clear();

        Bukkit.broadcastMessage("§6§l[SimonSays] §fSimon dit : §e" + currentCommand.description);
        Bukkit.broadcastMessage("§6[SimonSays] §fVous avez " + currentCommand.duration + " secondes !");

        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            player.playSound(player.getLocation(), org.bukkit.Sound.NOTE_PLING, 1f, 1.5f);
        }

        if (currentCommand.type == CommandType.STOP_MOVING) {
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                playersWhoComplied.add(uhcPlayer.getPlayer().getUniqueId());
            }
        }
    }

    private void endCommand() {
        commandActive = false;

        List<Player> failed = new ArrayList<>();

        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            if (!playersWhoComplied.contains(player.getUniqueId())) {
                failed.add(player);
            }
        }

        if (!failed.isEmpty()) {
            failed.forEach(this::applyPenalty);
            Bukkit.broadcastMessage("§6[SimonSays] §c" + failed.size() + " joueur(s) ont échoué !");
        } else {
            Bukkit.broadcastMessage("§6[SimonSays] §aTous les joueurs ont obéi à Simon !");
        }

        playersWhoComplied.clear();
        currentCommand = null;
    }

    private void applyPenalty(Player player) {
        List<PenaltyType> penalties = new ArrayList<>();

        if (penalty_damage) penalties.add(PenaltyType.DAMAGE);
        if (penalty_hunger) penalties.add(PenaltyType.HUNGER);
        if (penalty_effects) {
            penalties.add(PenaltyType.SLOWNESS);
            penalties.add(PenaltyType.BLINDNESS);
            penalties.add(PenaltyType.WEAKNESS);
        }

        if (penalties.isEmpty()) return;

        PenaltyType penalty = penalties.get(random.nextInt(penalties.size()));

        switch (penalty) {
            case DAMAGE -> {
                player.damage(2.0);
                player.sendMessage("§6[SimonSays] §cVous prenez 1 cœur de dégâts !");
            }
            case HUNGER -> {
                player.setFoodLevel(Math.max(0, player.getFoodLevel() - 4));
                player.sendMessage("§6[SimonSays] §cVous perdez de la faim !");
            }
            case SLOWNESS ->
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.SLOW, 200, 1));
            case BLINDNESS ->
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.BLINDNESS, 100, 0));
            case WEAKNESS ->
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.WEAKNESS, 200, 0));
        }
    }

    @Override
    public void onMove(Player player, PlayerMoveEvent event) {
        if (!isActive() || !commandActive || currentCommand == null) return;

        UUID uuid = player.getUniqueId();

        if (currentCommand.type == CommandType.MOVE_NORTH && event.getTo().getZ() < event.getFrom().getZ())
            playersWhoComplied.add(uuid);

        else if (currentCommand.type == CommandType.MOVE_SOUTH && event.getTo().getZ() > event.getFrom().getZ())
            playersWhoComplied.add(uuid);

        else if (currentCommand.type == CommandType.STOP_MOVING &&
                event.getFrom().distance(event.getTo()) > 0.1)
            playersWhoComplied.remove(uuid);
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (isActive() && commandActive && currentCommand != null &&
                currentCommand.type == CommandType.BREAK_BLOCK)
            playersWhoComplied.add(player.getUniqueId());
    }

    @Override
    public void onPlace(Player player, Block block, BlockPlaceEvent event) {
        if (isActive() && commandActive && currentCommand != null &&
                currentCommand.type == CommandType.PLACE_BLOCK)
            playersWhoComplied.add(player.getUniqueId());
    }

    private enum CommandType {
        JUMP, CROUCH, BREAK_BLOCK, PLACE_BLOCK,
        MOVE_NORTH, MOVE_SOUTH, STOP_MOVING
    }

    private enum PenaltyType {
        DAMAGE, HUNGER, SLOWNESS, BLINDNESS, WEAKNESS
    }

    private record SimonCommand(String name, String description, int duration, CommandType type) {}
}
