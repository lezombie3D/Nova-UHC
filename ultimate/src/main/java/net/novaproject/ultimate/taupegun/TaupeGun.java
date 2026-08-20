package net.novaproject.ultimate.taupegun;
import net.novaproject.novauhc.utils.variable.Var;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.event.UhcGameEvents.UhcPvpEnableEvent;
import net.novaproject.novauhc.utils.chat.ChatManager;
import net.novaproject.novauhc.command.CommandManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.normal.TeamInv;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.display.TeamsTagsManager;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class TaupeGun extends Scenario implements Listener {

    private static TaupeGun instance;

    private static final Color TAUPE_GLOW = new Color(0, 170, 170);

    private static final DynamicLang NOTIF_TAUPE_TITLE =
            DynamicLang.of("mode.taupegun.notif.title", "§3Tu es une Taupe");
    private static final DynamicLang NOTIF_TAUPE_BODY =
            DynamicLang.of("mode.taupegun.notif.body", "§7Trahis ton équipe. Tes complices sont surlignés.");

    @Var(name = "Var Mole Count", type = VariableType.INTEGER)
    private int mole = 1;

    @Var(name = "Var Mole Team Size", type = VariableType.INTEGER)
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
            LangManager.get().sendAll(CoreLang.COMMON_TEAM_REDFINIED_AUTO);
        }
    }

    @Override
    public void toggleActive() {
        super.toggleActive();
        UHCManager.get().setTeam_size(2);
        LangManager.get().sendAll(CoreLang.COMMON_TEAM_REDFINIED_AUTO);
    }

    @EventHandler
    public void onPvp(UhcPvpEnableEvent event) {
        if (!isActive()) return;
        if (taupeAssigned) return;
        taupeAssigned = true;
        ShuffleMultiTaupe(mole, molesize);
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

            int numMolesToAssign = Math.min(mole, availablePlayers.size());

            for (int i = 0; i < numMolesToAssign; i++) {
                UHCTeam chosenTeam = TeamsTaupe.stream()
                        .filter(t -> t.getPlayers().size() < t.teamSize())
                        .findFirst()
                        .orElse(null);
                if (chosenTeam == null) return;

                UHCPlayer chosenPlayer = availablePlayers.get(random.nextInt(availablePlayers.size()));
                availablePlayers.remove(chosenPlayer);
                alreadyChosen.add(chosenPlayer);

                saveOldTeam(chosenPlayer, team);
                chosenPlayer.setTeam(Optional.of(chosenTeam));

                int kitnumber = random.nextInt(7);
                kit.put(chosenPlayer, kitnumber);
                TeamsTagsManager.setNameTag(chosenPlayer.getPlayer(), team.name(), team.prefix(), "");

                DisplayService.title(
                        chosenPlayer.getPlayer(),
                        LangManager.get().get(TaupeGunLang.TAUPE_ASSIGNED_TITLE, chosenPlayer.getPlayer()),
                        LangManager.get().get(TaupeGunLang.TAUPE_ASSIGNED_SUBTITLE, chosenPlayer.getPlayer()),
                        10
                );
                sendKitDescription(kitnumber, chosenPlayer);
                addTaupe(chosenPlayer, chosenTeam);
                markTaupe(chosenPlayer, chosenTeam);
            }

            if (isScenarioActive("TeamInventory")) {
                for (UHCTeam t : TeamsTaupe) {
                    TeamInv.inventory.put(t, TeamInv.createShared());
                }
            }
        });
    }

    private void markTaupe(UHCPlayer chosenPlayer, UHCTeam taupeTeam) {
        Player taupe = chosenPlayer.getPlayer();
        if (taupe == null) return;

        DisplayService.notification(taupe, t(NOTIF_TAUPE_TITLE, taupe), t(NOTIF_TAUPE_BODY, taupe), 8);

        for (UHCPlayer mate : taupeTeam.getPlayers()) {
            Player mp = mate.getPlayer();
            if (mp == null || mp.getUniqueId().equals(taupe.getUniqueId())) continue;
            DisplayService.glow(taupe, mp, TAUPE_GLOW);
            DisplayService.glow(mp, taupe, TAUPE_GLOW);
        }
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
        return false;
    }

    private void createTeamTaupe(int size) {
        Pattern[] patterns = new Pattern[]{};
        int numberTeamTaupe = (int) Math.ceil((double) UHCTeamManager.get().getTeams().size() / size);
        for (int i = 0; i < numberTeamTaupe; i++) {
            UHCTeam taupe = new UHCTeam(DyeColor.CYAN, i + "", "Taupe " + i, patterns, size, true);
            UHCTeamManager.get().addTeams(taupe);
            TeamsTaupe.add(taupe);
            ChatManager.get().createTeamChannel(taupeChatChannelId(taupe), taupe, "?")
                    .formatter((sender, receiver, message, channel) ->
                            LangManager.get().get(TaupeGunLang.TAUPE_CHAT_FORMAT, sender,
                                    Map.of("%player%", sender.getName(), "%message%", message)));
        }
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
        ChatManager.ChatChannel channel = ChatManager.get().getOrCreateTeamChannel(team);
        if (channel != null) {
            channel.eventFormatter((sender, message, chatChannel) ->
                    LangManager.get().get(TaupeGunLang.TEAM_CHAT_FORMAT, sender,
                            Map.of("%player%", sender.getName(), "%message%", message)));
        }
        Player bukkitPlayer = player.getPlayer();
        if (bukkitPlayer != null && channel != null) {
            channel.addIndirectMember(bukkitPlayer);
        }
    }

    private String taupeChatChannelId(UHCTeam team) {
        return "taupegun:taupe:" + team.name();
    }
}

