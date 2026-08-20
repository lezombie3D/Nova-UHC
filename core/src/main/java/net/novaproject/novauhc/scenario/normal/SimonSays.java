package net.novaproject.novauhc.scenario.normal;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioLang;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SimonSays extends Scenario {

    @Override
    public Family getFamily() { return Family.TIMERS; }

    @Var(lang = ScenarioVarLang.class, nameKey = "SIMONSAYS_VAR_MIN_DELAY_NAME", descKey = "SIMONSAYS_VAR_MIN_DELAY_DESC", type = VariableType.INTEGER) private int min_delay = 120;
    @Var(lang = ScenarioVarLang.class, nameKey = "SIMONSAYS_VAR_MAX_DELAY_NAME", descKey = "SIMONSAYS_VAR_MAX_DELAY_DESC", type = VariableType.INTEGER) private int max_delay = 300;
    @Var(lang = ScenarioVarLang.class, nameKey = "SIMONSAYS_VAR_JUMP_DURATION_NAME", descKey = "SIMONSAYS_VAR_JUMP_DURATION_DESC", type = VariableType.INTEGER) private int jump_duration = 15;
    @Var(lang = ScenarioVarLang.class, nameKey = "SIMONSAYS_VAR_CROUCH_DURATION_NAME", descKey = "SIMONSAYS_VAR_CROUCH_DURATION_DESC", type = VariableType.INTEGER) private int crouch_duration = 10;
    @Var(lang = ScenarioVarLang.class, nameKey = "SIMONSAYS_VAR_MOVE_DURATION_NAME", descKey = "SIMONSAYS_VAR_MOVE_DURATION_DESC", type = VariableType.INTEGER) private int move_duration = 20;
    @Var(lang = ScenarioVarLang.class, nameKey = "SIMONSAYS_VAR_STOP_DURATION_NAME", descKey = "SIMONSAYS_VAR_STOP_DURATION_DESC", type = VariableType.INTEGER) private int stop_duration = 10;
    @Var(lang = ScenarioVarLang.class, nameKey = "SIMONSAYS_VAR_PENALTY_DAMAGE_NAME", descKey = "SIMONSAYS_VAR_PENALTY_DAMAGE_DESC", type = VariableType.BOOLEAN) private boolean penalty_damage = true;
    @Var(lang = ScenarioVarLang.class, nameKey = "SIMONSAYS_VAR_PENALTY_HUNGER_NAME", descKey = "SIMONSAYS_VAR_PENALTY_HUNGER_DESC", type = VariableType.BOOLEAN) private boolean penalty_hunger = true;
    @Var(lang = ScenarioVarLang.class, nameKey = "SIMONSAYS_VAR_PENALTY_EFFECTS_NAME", descKey = "SIMONSAYS_VAR_PENALTY_EFFECTS_DESC", type = VariableType.BOOLEAN) private boolean penalty_effects = true;

    private final Set<UUID> playersWhoComplied = new HashSet<>();
    private final Random random = new Random();
    private BukkitRunnable simonTask;
    private SimonCommand currentCommand;
    private boolean commandActive = false;
    private int commandTimeLeft = 0;


    @Override public String getName() { return "SimonSays"; }
    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.SIMON_SAYS, player);
    }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.COMMAND); }
    @Override public void onGameStart() { startSimonTask(); }

    private void startSimonTask() {
        if (simonTask != null) simonTask.cancel();
        simonTask = (BukkitRunnable) new BukkitRunnable() {
            int timer = 0; int nextCommandIn = getRandomDelay();
            @Override public void run() {
                if (!isActive()) { cancel(); return; }
                timer++;
                if (!commandActive) {
                    if (timer >= nextCommandIn) { issueRandomCommand(); timer = 0; nextCommandIn = getRandomDelay(); }
                    else if (nextCommandIn - timer == 30) Bukkit.broadcastMessage(t(ScenarioLang.SIMONSAYS_INCOMING_ORDER));
                } else {
                    commandTimeLeft--;
                    if (commandTimeLeft <= 0) endCommand();
                    else if (commandTimeLeft == 5) Bukkit.broadcastMessage(t(ScenarioLang.SIMONSAYS_FIVE_SECONDS));
                }
            }
        }.runTaskTimer(Main.get(), 0, 20);
    }

    private int getRandomDelay() { return min_delay + random.nextInt(Math.max(1, max_delay - min_delay + 1)); }

    private void issueRandomCommand() {
        List<SimonCommand> commands = Arrays.asList(
                new SimonCommand("BREAK_BLOCK", "Cassez un bloc !", move_duration, CommandType.BREAK_BLOCK),
                new SimonCommand("PLACE_BLOCK", "Placez un bloc !", move_duration, CommandType.PLACE_BLOCK),
                new SimonCommand("MOVE_NORTH", "Déplacez-vous vers le Nord !", move_duration, CommandType.MOVE_NORTH),
                new SimonCommand("MOVE_SOUTH", "Déplacez-vous vers le Sud !", move_duration, CommandType.MOVE_SOUTH),
                new SimonCommand("STOP_MOVING", "Arrêtez de bouger !", stop_duration, CommandType.STOP_MOVING));
        currentCommand = commands.get(random.nextInt(commands.size()));
        commandActive = true; commandTimeLeft = currentCommand.duration; playersWhoComplied.clear();
        Bukkit.broadcastMessage(t(ScenarioLang.SIMONSAYS_NEW_ORDER, Map.of("%order%", currentCommand.description)));
        Bukkit.broadcastMessage(t(ScenarioLang.SIMONSAYS_ORDER_DURATION, Map.of("%seconds%", currentCommand.duration)));
        for (UHCPlayer p : UHCPlayerManager.get().getPlayingOnlineUHCPlayers())
            p.getPlayer().playSound(p.getPlayer().getLocation(), Sound.NOTE_PLING, 1f, 1.5f);
        if (currentCommand.type == CommandType.STOP_MOVING)
            for (UHCPlayer p : UHCPlayerManager.get().getPlayingOnlineUHCPlayers())
                playersWhoComplied.add(p.getPlayer().getUniqueId());
    }

    private void endCommand() {
        commandActive = false;
        List<Player> failed = new ArrayList<>();
        for (UHCPlayer p : UHCPlayerManager.get().getPlayingOnlineUHCPlayers())
            if (!playersWhoComplied.contains(p.getPlayer().getUniqueId())) failed.add(p.getPlayer());
        if (!failed.isEmpty()) {
            failed.forEach(this::applyPenalty);
            Bukkit.broadcastMessage(t(ScenarioLang.SIMONSAYS_FAILED_COUNT, Map.of("%count%", failed.size())));
        } else {
            Bukkit.broadcastMessage(t(ScenarioLang.SIMONSAYS_ALL_OBEYED));
        }
        playersWhoComplied.clear(); currentCommand = null;
    }

    private void applyPenalty(Player player) {
        List<PenaltyType> penalties = new ArrayList<>();
        if (penalty_damage) penalties.add(PenaltyType.DAMAGE);
        if (penalty_hunger) penalties.add(PenaltyType.HUNGER);
        if (penalty_effects) { penalties.add(PenaltyType.SLOWNESS); penalties.add(PenaltyType.BLINDNESS); penalties.add(PenaltyType.WEAKNESS); }
        if (penalties.isEmpty()) return;
        switch (penalties.get(random.nextInt(penalties.size()))) {
            case DAMAGE -> { player.damage(2.0); player.sendMessage(t(ScenarioLang.SIMONSAYS_DAMAGE_MSG)); }
            case HUNGER -> { player.setFoodLevel(Math.max(0, player.getFoodLevel() - 4)); player.sendMessage(t(ScenarioLang.SIMONSAYS_HUNGER_MSG)); }
            case SLOWNESS -> player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 1));
            case BLINDNESS -> player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
            case WEAKNESS -> player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 0));
        }
    }

    @Override public void onMove(Player player, PlayerMoveEvent event) {
        if (!isActive() || !commandActive || currentCommand == null) return;
        UUID uuid = player.getUniqueId();
        if (currentCommand.type == CommandType.MOVE_NORTH && event.getTo().getZ() < event.getFrom().getZ()) playersWhoComplied.add(uuid);
        else if (currentCommand.type == CommandType.MOVE_SOUTH && event.getTo().getZ() > event.getFrom().getZ()) playersWhoComplied.add(uuid);
        else if (currentCommand.type == CommandType.STOP_MOVING && event.getFrom().distance(event.getTo()) > 0.1) playersWhoComplied.remove(uuid);
    }

    @Override public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (isActive() && commandActive && currentCommand != null && currentCommand.type == CommandType.BREAK_BLOCK) playersWhoComplied.add(player.getUniqueId());
    }

    @Override public void onPlace(Player player, Block block, BlockPlaceEvent event) {
        if (isActive() && commandActive && currentCommand != null && currentCommand.type == CommandType.PLACE_BLOCK) playersWhoComplied.add(player.getUniqueId());
    }

    private enum CommandType { BREAK_BLOCK, PLACE_BLOCK, MOVE_NORTH, MOVE_SOUTH, STOP_MOVING }
    private enum PenaltyType { DAMAGE, HUNGER, SLOWNESS, BLINDNESS, WEAKNESS }
    private record SimonCommand(String name, String description, int duration, CommandType type) {}
}

