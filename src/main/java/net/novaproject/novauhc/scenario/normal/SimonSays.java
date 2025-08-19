package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
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
    public void toggleActive() {
        super.toggleActive();
        if (isActive()) {
            startSimonTask();
        } else {
            stopSimonTask();
        }
    }

    private void startSimonTask() {
        if (simonTask != null) {
            simonTask.cancel();
        }

        simonTask = new BukkitRunnable() {
            private int timer = 0;
            private int nextCommandIn = 120 + random.nextInt(180); // 2-5 minutes until first command

            @Override
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }

                timer++;

                if (!commandActive) {
                    // Waiting for next command
                    if (timer >= nextCommandIn) {
                        issueRandomCommand();
                        timer = 0;
                        nextCommandIn = 120 + random.nextInt(180); // Next command in 2-5 minutes
                    } else {
                        // Warning messages
                        int timeLeft = nextCommandIn - timer;
                        if (timeLeft == 30) {
                            Bukkit.broadcastMessage("§6[SimonSays] §fSimon va bientôt donner un ordre...");
                        }
                    }
                } else {
                    // Command is active
                    commandTimeLeft--;

                    if (commandTimeLeft <= 0) {
                        endCommand();
                    } else if (commandTimeLeft == 5) {
                        Bukkit.broadcastMessage("§6[SimonSays] §c5 secondes restantes !");
                    }
                }
            }
        };

        // Run every second
        simonTask.runTaskTimer(Main.get(), 0, 20);
    }

    private void stopSimonTask() {
        if (simonTask != null) {
            simonTask.cancel();
            simonTask = null;
        }
        commandActive = false;
        playersWhoComplied.clear();
    }

    private void issueRandomCommand() {
        List<SimonCommand> availableCommands = Arrays.asList(
                new SimonCommand("JUMP", "Sautez 3 fois !", 15, CommandType.JUMP),
                new SimonCommand("CROUCH", "Accroupissez-vous pendant 5 secondes !", 10, CommandType.CROUCH),
                new SimonCommand("BREAK_BLOCK", "Cassez un bloc !", 20, CommandType.BREAK_BLOCK),
                new SimonCommand("PLACE_BLOCK", "Placez un bloc !", 20, CommandType.PLACE_BLOCK),
                new SimonCommand("DROP_ITEM", "Jetez un objet !", 15, CommandType.DROP_ITEM),
                new SimonCommand("MOVE_NORTH", "Déplacez-vous vers le Nord !", 20, CommandType.MOVE_NORTH),
                new SimonCommand("MOVE_SOUTH", "Déplacez-vous vers le Sud !", 20, CommandType.MOVE_SOUTH),
                new SimonCommand("STOP_MOVING", "Arrêtez de bouger !", 10, CommandType.STOP_MOVING)
        );

        currentCommand = availableCommands.get(random.nextInt(availableCommands.size()));
        commandActive = true;
        commandTimeLeft = currentCommand.duration;
        playersWhoComplied.clear();

        // Announce the command
        Bukkit.broadcastMessage("§6§l[SimonSays] §fSimon dit : §e" + currentCommand.description);
        Bukkit.broadcastMessage("§6[SimonSays] §fVous avez " + currentCommand.duration + " secondes !");

        // Play sound
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.NOTE_PLING, 1.0f, 1.5f);
        }

        // Initialize tracking for specific commands
        if (currentCommand.type == CommandType.STOP_MOVING) {
            // Mark all players as initially complying (they need to stop moving)
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                playersWhoComplied.add(uhcPlayer.getPlayer().getUniqueId());
            }
        }
    }

    private void endCommand() {
        commandActive = false;

        // Check who didn't comply and apply penalties
        List<Player> nonCompliantPlayers = new ArrayList<>();

        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            UUID playerUuid = player.getUniqueId();

            if (!playersWhoComplied.contains(playerUuid)) {
                nonCompliantPlayers.add(player);
            }
        }

        // Apply penalties
        if (!nonCompliantPlayers.isEmpty()) {
            for (Player player : nonCompliantPlayers) {
                applyPenalty(player);
            }

            Bukkit.broadcastMessage("§6[SimonSays] §c" + nonCompliantPlayers.size() +
                    " joueur(s) n'ont pas obéi à Simon et subissent une pénalité !");
        } else {
            Bukkit.broadcastMessage("§6[SimonSays] §aTous les joueurs ont obéi à Simon ! Bien joué !");
        }

        // Reset
        playersWhoComplied.clear();
        currentCommand = null;
    }

    private void applyPenalty(Player player) {
        List<PenaltyType> penalties = Arrays.asList(
                PenaltyType.DAMAGE,
                PenaltyType.HUNGER,
                PenaltyType.SLOWNESS,
                PenaltyType.BLINDNESS,
                PenaltyType.WEAKNESS
        );

        PenaltyType penalty = penalties.get(random.nextInt(penalties.size()));

        switch (penalty) {
            case DAMAGE:
                player.damage(2.0);
                player.sendMessage("§6[SimonSays] §cVous prenez 1 cœur de dégâts pour désobéissance !");
                break;

            case HUNGER:
                int currentFood = player.getFoodLevel();
                player.setFoodLevel(Math.max(0, currentFood - 4));
                player.sendMessage("§6[SimonSays] §cVous perdez de la faim pour désobéissance !");
                break;

            case SLOWNESS:
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.SLOW, 200, 1));
                player.sendMessage("§6[SimonSays] §cVous êtes ralenti pour désobéissance !");
                break;

            case BLINDNESS:
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.BLINDNESS, 100, 0));
                player.sendMessage("§6[SimonSays] §cVous êtes aveuglé pour désobéissance !");
                break;

            case WEAKNESS:
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.WEAKNESS, 200, 0));
                player.sendMessage("§6[SimonSays] §cVous êtes affaibli pour désobéissance !");
                break;
        }
    }

    @Override
    public void onMove(Player player, PlayerMoveEvent event) {
        if (!isActive() || !commandActive || currentCommand == null) return;

        UUID playerUuid = player.getUniqueId();

        if (currentCommand.type == CommandType.MOVE_NORTH) {
            if (event.getTo().getZ() < event.getFrom().getZ()) {
                playersWhoComplied.add(playerUuid);
            }
        } else if (currentCommand.type == CommandType.MOVE_SOUTH) {
            if (event.getTo().getZ() > event.getFrom().getZ()) {
                playersWhoComplied.add(playerUuid);
            }
        } else if (currentCommand.type == CommandType.STOP_MOVING) {
            // If player moved, remove them from compliant list
            if (event.getFrom().distance(event.getTo()) > 0.1) {
                playersWhoComplied.remove(playerUuid);
            }
        }
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (!isActive() || !commandActive || currentCommand == null) return;

        if (currentCommand.type == CommandType.BREAK_BLOCK) {
            playersWhoComplied.add(player.getUniqueId());
            player.sendMessage("§6[SimonSays] §aVous avez obéi à Simon !");
        }
    }

    @Override
    public void onPlace(Player player, Block block, BlockPlaceEvent event) {
        if (!isActive() || !commandActive || currentCommand == null) return;

        if (currentCommand.type == CommandType.PLACE_BLOCK) {
            playersWhoComplied.add(player.getUniqueId());
            player.sendMessage("§6[SimonSays] §aVous avez obéi à Simon !");
        }
    }

    // Admin methods
    public void forceCommand(String commandName) {
        // Force a specific command (admin use)
        if (isActive() && !commandActive) {
            // Implementation would go here
        }
    }

    public boolean isCommandActive() {
        return commandActive;
    }

    public SimonCommand getCurrentCommand() {
        return currentCommand;
    }

    // Inner classes and enums
    private enum CommandType {
        JUMP, CROUCH, BREAK_BLOCK, PLACE_BLOCK, DROP_ITEM,
        MOVE_NORTH, MOVE_SOUTH, STOP_MOVING
    }

    private enum PenaltyType {
        DAMAGE, HUNGER, SLOWNESS, BLINDNESS, WEAKNESS
    }

    private static class SimonCommand {
        final String name;
        final String description;
        final int duration;
        final CommandType type;

        SimonCommand(String name, String description, int duration, CommandType type) {
            this.name = name;
            this.description = description;
            this.duration = duration;
            this.type = type;
        }
    }
}
