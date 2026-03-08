package net.novaproject.ultimate.taupegun;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.CommandManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CommonLang;
import net.novaproject.novauhc.lang.special.TaupeGunLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.normal.TeamInv;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.TeamsTagsManager;
import net.novaproject.novauhc.utils.Titles;
import net.novaproject.novauhc.utils.VariableType;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class TaupeGun extends Scenario {

    private static TaupeGun instance;

    @ScenarioVariable(lang = TaupeGunLang.class, nameKey = "VAR_MOLE_COUNT_NAME", descKey = "VAR_MOLE_COUNT_DESC", type = VariableType.INTEGER)
    private int mole = 1;

    @ScenarioVariable(lang = TaupeGunLang.class, nameKey = "VAR_MOLE_TEAM_SIZE_NAME", descKey = "VAR_MOLE_TEAM_SIZE_DESC", type = VariableType.INTEGER)
    private int molesize = 3;

    private final HashMap<UHCPlayer, UHCTeam> taupeTeam = new HashMap<>();
    private final HashMap<UHCTeam, UHCPlayer> taupePlayer = new HashMap<>();
    private final HashMap<UHCPlayer, UHCTeam> oldTeam = new HashMap<>();
    private final HashMap<UHCPlayer, Integer> kit = new HashMap<>();
    private final List<UHCTeam> TeamsTaupe = new ArrayList<>();
    private final List<UHCPlayer> calimed = new ArrayList<>();
    private final Set<UHCPlayer> alreadyChosen = new HashSet<>();
    private boolean taupeAssigned = false;

    public static TaupeGun getInstance() {
        return instance;
    }

    @Override
    public String getName() {
        return LangManager.get().get(TaupeGunLang.TAUPE_GUN_NAME);
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(TaupeGunLang.TAUPE_GUN_DESC, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.SADDLE);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public void setup() {
        super.setup();
        instance = this;
        taupeTeam.clear();
        taupePlayer.clear();
        oldTeam.clear();
        TeamsTaupe.clear();
        kit.clear();
    }

    @Override
    public void onTeamUpdate() {
        if (UHCManager.get().getTeam_size() == 1) {
            UHCManager.get().setTeam_size(2);
            LangManager.get().sendAll(CommonLang.TEAM_REDFINIED_AUTO);
        }
    }

    @Override
    public void toggleActive() {
        super.toggleActive();
        UHCManager.get().setTeam_size(2);
        LangManager.get().sendAll(CommonLang.TEAM_REDFINIED_AUTO);
    }

    @Override
    public void onSec(Player p) {
        int timer = UHCManager.get().getTimer();
        int pvp = UHCManager.get().getTimerpvp();

        if (timer == pvp && !taupeAssigned) {
            taupeAssigned = true;
            ShuffleMultiTaupe(mole, molesize);
        }
    }

    @Override
    public void onTaupeTcCMD(Player player, int x, int y, int z, String coordsMessage) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        Map<String, Object> placeholders = Map.of("%co%", coordsMessage);

        if (getTeamsTaupe().contains(uhcPlayer.getTeam().get())) {
            String taupeMessage = LangManager.get().get(TaupeGunLang.TAUPE_COORDS_FORMAT, player, placeholders);
            uhcPlayer.getTeam().get().getPlayers().forEach(tp -> tp.getPlayer().sendMessage(taupeMessage));
        } else {
            String teamMessage = LangManager.get().get(TaupeGunLang.TEAM_COORDS_FORMAT, player, placeholders);
            uhcPlayer.getTeam().get().getPlayers().forEach(tp -> tp.getPlayer().sendMessage(teamMessage));

            for (Map.Entry<UHCPlayer, UHCTeam> entry : getOldTeam().entrySet()) {
                if (entry.getValue().equals(uhcPlayer.getTeam().get())) {
                    entry.getKey().getPlayer().sendMessage(teamMessage);
                    break;
                }
            }
        }
    }

    @Override
    public void onGameStart() {
        CommandManager.get().register("taupegun", new TaupeCMD(), "tg");
    }

    private void ShuffleMultiTaupe(int mole, int size) {
        List<UHCTeam> picked = new ArrayList<>();
        Random random = new Random();
        createTeamTaupe(size);

        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player -> {
            if (!player.getTeam().isPresent()) return;
            UHCTeam team = player.getTeam().get();
            if (picked.contains(team)) return;
            picked.add(team);

            List<UHCPlayer> availablePlayers = team.getPlayers().stream()
                    .filter(ps -> !alreadyChosen.contains(ps))
                    .collect(Collectors.toList());
            if (availablePlayers.isEmpty()) return;

            List<UHCTeam> availableTeams = TeamsTaupe.stream()
                    .filter(t -> t.getPlayers().size() < t.teamSize())
                    .collect(Collectors.toList());
            if (availableTeams.isEmpty()) return;

            int numMolesToAssign = Math.min(mole, availablePlayers.size());

            for (int i = 0; i < numMolesToAssign; i++) {
                UHCPlayer chosenPlayer = availablePlayers.get(random.nextInt(availablePlayers.size()));
                alreadyChosen.add(chosenPlayer);

                UHCTeam chosenTeam = availableTeams.get(random.nextInt(availableTeams.size()));
                saveOldTeam(chosenPlayer, team);
                chosenPlayer.setTeam(Optional.of(chosenTeam));

                int kitnumber = random.nextInt(7);
                kit.put(chosenPlayer, kitnumber);
                TeamsTagsManager.setNameTag(chosenPlayer.getPlayer(), team.name(), team.prefix(), "");

                new Titles().sendTitle(
                        chosenPlayer.getPlayer(),
                        LangManager.get().get(TaupeGunLang.TAUPE_ASSIGNED_TITLE, chosenPlayer.getPlayer()),
                        LangManager.get().get(TaupeGunLang.TAUPE_ASSIGNED_SUBTITLE, chosenPlayer.getPlayer()),
                        10
                );
                sendKitDescription(kitnumber, chosenPlayer);
                addTaupe(chosenPlayer, chosenTeam);
            }

            if (isScenarioActive("TeamInventory")) {
                for (UHCTeam t : TeamsTaupe) {
                    TeamInv.inventory.put(t, Bukkit.createInventory(null, InventoryType.CHEST));
                }
            }
        });
    }

    private void sendKitDescription(int kitnumber, UHCPlayer chosenPlayer) {
        TaupeGunLang[] kitLangs = {
                TaupeGunLang.KIT_DESCRIPTION_0,
                TaupeGunLang.KIT_DESCRIPTION_1,
                TaupeGunLang.KIT_DESCRIPTION_2,
                TaupeGunLang.KIT_DESCRIPTION_3,
                TaupeGunLang.KIT_DESCRIPTION_4,
                TaupeGunLang.KIT_DESCRIPTION_5,
                TaupeGunLang.KIT_DESCRIPTION_6
        };
        if (kitnumber >= 0 && kitnumber < kitLangs.length) {
            LangManager.get().send(kitLangs[kitnumber], chosenPlayer.getPlayer());
        }
    }

    @Override
    public boolean hasCustomTeamTchat() {
        return true;
    }

    private void createTeamTaupe(int size) {
        Pattern[] patterns = new Pattern[]{};
        int numberTeamTaupe = (int) Math.ceil((double) UHCTeamManager.get().getTeams().size() / size);
        for (int i = 0; i < numberTeamTaupe; i++) {
            UHCTeam taupe = new UHCTeam(DyeColor.CYAN, i + "", "Taupe " + i, patterns, size, true);
            UHCTeamManager.get().addTeams(taupe);
            TeamsTaupe.add(taupe);
        }
    }

    @Override
    public void onChatSpeek(Player player, String message, AsyncPlayerChatEvent event) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        if (UHCManager.get().getGameState() != UHCManager.GameState.INGAME) {
            event.setFormat(LangManager.get().get(CommonLang.LOBBY_CHAT_FORMAT, player));
            return;
        }

        if (!uhcPlayer.isPlaying()) {
            LangManager.get().send(CommonLang.CANT_TALK_DEATH, player);
            event.setCancelled(true);
            return;
        }

        if (UHCManager.get().isChatdisbale()) {
            LangManager.get().send(CommonLang.CHAT_DISABLED, player);
            event.setCancelled(true);
            return;
        }

        if (message.startsWith("!")) {
            event.setMessage(message.substring(1));
            event.setFormat(LangManager.get().get(CommonLang.CHAT_GLOBAL_FORMAT, player));
            return;
        }

        if (message.startsWith("?")) {
            if (!TeamsTaupe.contains(uhcPlayer.getTeam().get())) {
                LangManager.get().send(TaupeGunLang.NOT_TAUPE_ERROR, player);
                event.setCancelled(true);
                return;
            }

            if (taupeTeam.containsKey(uhcPlayer)) {
                UHCTeam taupeTeamGroup = taupeTeam.get(uhcPlayer);
                event.getRecipients().removeIf(p ->
                        taupeTeamGroup.getPlayers().stream().noneMatch(u -> u.getPlayer().equals(p)));
                event.getRecipients().add(player);
                event.setMessage(message.substring(1));
                Map<String, Object> placeholders = Map.of("%player%", player.getName(), "%message%", message.substring(1));
                event.setFormat(LangManager.get().get(TaupeGunLang.TAUPE_CHAT_FORMAT, player, placeholders));
                return;
            }
        }

        UHCTeam currentTeam = uhcPlayer.getTeam().get();

        if (taupeTeam.containsKey(uhcPlayer)) {
            UHCTeam originalTeam = getOldTeamforPlayer(uhcPlayer);
            if (originalTeam != null) {
                event.getRecipients().removeIf(p ->
                        originalTeam.getPlayers().stream().noneMatch(u -> u.getPlayer().equals(p)));

                for (Map.Entry<UHCPlayer, UHCTeam> entry : taupeTeam.entrySet()) {
                    if (entry.getValue().equals(originalTeam) && !entry.getKey().equals(uhcPlayer)) {
                        event.getRecipients().add(entry.getKey().getPlayer());
                    }
                }
                event.getRecipients().add(player);
                Map<String, Object> placeholders = Map.of("%player%", player.getName(), "%message%", message);
                event.setFormat(LangManager.get().get(TaupeGunLang.TEAM_CHAT_FORMAT, player, placeholders));
                return;
            }
        }

        event.getRecipients().removeIf(p ->
                currentTeam.getPlayers().stream().noneMatch(u -> u.getPlayer().equals(p)));

        for (Map.Entry<UHCPlayer, UHCTeam> entry : oldTeam.entrySet()) {
            if (entry.getValue().equals(currentTeam)) {
                event.getRecipients().add(entry.getKey().getPlayer());
            }
        }

        event.getRecipients().add(player);
        Map<String, Object> placeholders = Map.of("%player%", player.getName(), "%message%", message);
        event.setFormat(LangManager.get().get(TaupeGunLang.TEAM_CHAT_FORMAT, player, placeholders));
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
    }

    public UHCTeam getOldTeamforPlayer(UHCPlayer player) {
        return oldTeam.getOrDefault(player, null);
    }

    private boolean isScenarioActive(String scenarioName) {
        return ScenarioManager.get().getScenarioByName(scenarioName).map(Scenario::isActive).orElse(false);
    }

    public void addTaupe(UHCPlayer player, UHCTeam team) {
        taupePlayer.put(team, player);
        taupeTeam.put(player, team);
        TeamsTaupe.add(team);
    }

    public void saveOldTeam(UHCPlayer player, UHCTeam team) {
        oldTeam.put(player, team);
    }
}