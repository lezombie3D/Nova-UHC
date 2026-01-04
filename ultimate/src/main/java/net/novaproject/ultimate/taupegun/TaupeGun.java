package net.novaproject.ultimate.taupegun;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.CommandManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.scenario.ScenarioLangManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.normal.TeamInv;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.TeamsTagsManager;
import net.novaproject.novauhc.utils.Titles;
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
    private final HashMap<UHCPlayer, UHCTeam> taupeTeam = new HashMap<>();
    private final HashMap<UHCTeam, UHCPlayer> taupePlayer = new HashMap<>();
    private final HashMap<UHCPlayer, UHCTeam> oldTeam = new HashMap<>();
    private final HashMap<UHCPlayer, Integer> kit = new HashMap<>();
    private final List<UHCTeam> TeamsTaupe = new ArrayList<>();
    private final List<UHCPlayer> calimed = new ArrayList<>();
    private final Set<UHCPlayer> alreadyChosen = new HashSet<>();
    private int mole = 1;
    private int molesize = 3;
    private boolean taupeAssigned = false;

    public static TaupeGun getInstance() {
        return instance;
    }

    @Override
    public String getName() {
        return "TaupeGun";
    }

    @Override
    public String getDescription() {
        return "Certains joueurs deviennent des taupes secrètes avec des kits spéciaux.";
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
    public String getPath() {
        return "special/taupegun";
    }

    @Override
    public ScenarioLang[] getLang() {
        return TaupeGunLang.values();
    }

    public int getMole() {
        return mole;
    }

    public void setMole(int mole) {
        this.mole = mole;
    }

    public int getMolesize() {
        return molesize;
    }

    public void setMolesize(int molesize) {
        this.molesize = molesize;
    }

    @Override
    public void onTeamUpdate() {
        if (UHCManager.get().getTeam_size() == 1) {
            UHCManager.get().setTeam_size(2);
            CommonString.TEAM_REDFINIED_AUTO.sendAll();
        }

    }

    @Override
    public void toggleActive() {
        super.toggleActive();
        UHCManager.get().setTeam_size(2);
        CommonString.TEAM_REDFINIED_AUTO.sendAll();
        CommandManager.get().register("taupegun", new TaupeCMD(), "tg");
    }

    @Override
    public void onSec(Player p) {
        int timer = UHCManager.get().getTimer();
        int pvp = UHCManager.get().getTimerpvp();

        if (timer == pvp && !taupeAssigned) {
            if (taupeAssigned) return;

            taupeAssigned = true;

            ShuffleMultiTaupe(mole, molesize);

        }
    }


    @Override
    public void onTaupeTcCMD(Player player, int x, int y, int z, String coordsMessage) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%co%", coordsMessage);

        if (getTeamsTaupe().contains(uhcPlayer.getTeam().get())) {
            String taupeMessage = ScenarioLangManager.get(TaupeGunLang.TAUPE_COORDS_FORMAT, uhcPlayer, placeholders);
            uhcPlayer.getTeam().get().getPlayers().forEach(teamPlayer ->
                    teamPlayer.getPlayer().sendMessage(taupeMessage));
        } else {
            String teamMessage = ScenarioLangManager.get(TaupeGunLang.TEAM_COORDS_FORMAT, uhcPlayer, placeholders);
            uhcPlayer.getTeam().get().getPlayers().forEach(teamPlayer ->
                    teamPlayer.getPlayer().sendMessage(teamMessage));

            for (Map.Entry<UHCPlayer, UHCTeam> entry : getOldTeam().entrySet()) {
                if (entry.getValue().equals(uhcPlayer.getTeam().get())) {
                    UHCPlayer taupe = entry.getKey();
                    taupe.getPlayer().sendMessage(teamMessage);
                    break;
                }
            }
        }
    }


    @Override
    public CustomInventory getMenu(Player player) {
        return new TaupeGunUI(player);
    }

    private void ShuffleMultiTaupe(int mole, int size) {

        List<UHCTeam> picked = new ArrayList<>();
        Random random = new Random();
        createTeamTaupe(size);


        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player -> {
            if (player.getTeam().isPresent()) {
                UHCTeam team = player.getTeam().get();

                if (picked.contains(team)) {
                    return;
                }
                picked.add(team);

                List<UHCPlayer> availablePlayers = team.getPlayers().stream()
                        .filter(ps -> !alreadyChosen.contains(ps))
                        .collect(Collectors.toList());

                if (availablePlayers.isEmpty()) {
                    return;
                }

                List<UHCTeam> availableTeams = TeamsTaupe.stream()
                        .filter(t -> t.getPlayers().size() < t.teamSize())
                        .collect(Collectors.toList());

                if (availableTeams.isEmpty()) {
                    return;
                }

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
                    new Titles().sendTitle(chosenPlayer.getPlayer(),
                            ScenarioLangManager.get(TaupeGunLang.TAUPE_ASSIGNED_TITLE, chosenPlayer),
                            ScenarioLangManager.get(TaupeGunLang.TAUPE_ASSIGNED_SUBTITLE, chosenPlayer), 10);
                    sendKitDescription(kitnumber, chosenPlayer);

                    addTaupe(chosenPlayer, chosenTeam);
                }

                if (isScenarioActive("TeamInventory")) {
                    for (UHCTeam t : TeamsTaupe) {
                        TeamInv.inventory.put(t, Bukkit.createInventory(null, InventoryType.CHEST));
                    }
                }
            }
        });

    }


    private void sendKitDescription(int kitnumber, UHCPlayer chosenPlayer) {
        TaupeGunLang kitLang;

        switch (kitnumber) {
            case 0:
                kitLang = TaupeGunLang.KIT_DESCRIPTION_0;
                break;
            case 1:
                kitLang = TaupeGunLang.KIT_DESCRIPTION_1;
                break;
            case 2:
                kitLang = TaupeGunLang.KIT_DESCRIPTION_2;
                break;
            case 3:
                kitLang = TaupeGunLang.KIT_DESCRIPTION_3;
                break;
            case 4:
                kitLang = TaupeGunLang.KIT_DESCRIPTION_4;
                break;
            case 5:
                kitLang = TaupeGunLang.KIT_DESCRIPTION_5;
                break;
            case 6:
                kitLang = TaupeGunLang.KIT_DESCRIPTION_6;
                break;
            default:
                return;
        }

        ScenarioLangManager.send(chosenPlayer.getPlayer(), kitLang);
    }

    @Override
    public boolean hasCustomTeamTchat() {
        return true;
    }

    private void createTeamTaupe(int size) {

        Pattern[] patterns = new Pattern[]{};
        int totalPlayers = UHCTeamManager.get().getTeams().size();
        int numberTeamTaupe = (int) Math.ceil((double) totalPlayers / size);
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
            event.setFormat(CommonString.LOBBY_CHAT_FORMAT.getMessage(player));
            return;
        }

        if (!uhcPlayer.isPlaying()) {
            CommonString.CANT_TALK_DEATH.send(player);
            event.setCancelled(true);
            return;
        }

        if (UHCManager.get().isChatdisbale()) {
            CommonString.CHAT_DISABLED.send(player);
            event.setCancelled(true);
            return;
        }

        if (message.startsWith("!")) {
            event.setMessage(message.substring(1));
            event.setFormat(CommonString.CHAT_GLOBAL_FORMAT.getMessage(player));
            return;
        }

        if (message.startsWith("?")) {
            if (!TeamsTaupe.contains(uhcPlayer.getTeam().get())) {
                ScenarioLangManager.send(player, TaupeGunLang.NOT_TAUPE_ERROR);
                event.setCancelled(true);
                return;
            }

            if (taupeTeam.containsKey(uhcPlayer)) {
                UHCTeam taupeTeamGroup = taupeTeam.get(uhcPlayer);

                event.getRecipients().removeIf(p ->
                        taupeTeamGroup.getPlayers().stream().noneMatch(u -> u.getPlayer().equals(p))
                );

                event.getRecipients().add(player);

                event.setMessage(message.substring(1));
                Map<String, Object> placeholders = new HashMap<>();
                placeholders.put("%player%", player.getName());
                placeholders.put("%message%", message.substring(1));
                event.setFormat(ScenarioLangManager.get(TaupeGunLang.TAUPE_CHAT_FORMAT, uhcPlayer, placeholders));
                return;
            }
        }

        UHCTeam currentTeam = uhcPlayer.getTeam().get();

        if (taupeTeam.containsKey(uhcPlayer)) {
            UHCTeam originalTeam = getOldTeamforPlayer(uhcPlayer);
            if (originalTeam != null) {
                event.getRecipients().removeIf(p ->
                        originalTeam.getPlayers().stream().noneMatch(u -> u.getPlayer().equals(p))
                );

                for (Map.Entry<UHCPlayer, UHCTeam> entry : taupeTeam.entrySet()) {
                    if (entry.getValue().equals(originalTeam) && !entry.getKey().equals(uhcPlayer)) {
                        event.getRecipients().add(entry.getKey().getPlayer());
                    }
                }

                event.getRecipients().add(player);

                Map<String, Object> placeholders = new HashMap<>();
                placeholders.put("%player%", player.getName());
                placeholders.put("%message%", message);
                event.setFormat(ScenarioLangManager.get(TaupeGunLang.TEAM_CHAT_FORMAT, uhcPlayer, placeholders));
                return;
            }
        }

        event.getRecipients().removeIf(p ->
                currentTeam.getPlayers().stream().noneMatch(u -> u.getPlayer().equals(p))
        );

        for (Map.Entry<UHCPlayer, UHCTeam> entry : oldTeam.entrySet()) {
            if (entry.getValue().equals(currentTeam)) {
                event.getRecipients().add(entry.getKey().getPlayer());
            }
        }

        event.getRecipients().add(player);
        event.setFormat(ScenarioLangManager.get(TaupeGunLang.TEAM_CHAT_FORMAT, uhcPlayer));
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

    public HashMap<UHCPlayer, Integer> getKit() {
        return kit;
    }

    public List<UHCTeam> getTeamsTaupe() {
        return TeamsTaupe;
    }


    public HashMap<UHCTeam, UHCPlayer> getTaupePlayer() {
        return taupePlayer;
    }

    public HashMap<UHCPlayer, UHCTeam> getOldTeam() {
        return oldTeam;
    }
}

