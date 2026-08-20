package net.novaproject.ultimate.taupegunapocalypse;
import net.novaproject.novauhc.utils.variable.Var;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.event.UhcGameEvents.UhcPvpEnableEvent;
import net.novaproject.novauhc.utils.chat.ChatManager;
import net.novaproject.novauhc.command.CommandManager;
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

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class TaupeGunApocalypse extends Scenario implements Listener {

    private static TaupeGunApocalypse instance;

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
    private final Set<UHCPlayer> superTaupes = new HashSet<>();
    private final Set<UHCPlayer> revealedTaupes = new HashSet<>();
    private final Set<UHCPlayer> revealedSupers = new HashSet<>();
    private final List<UHCTeam> SuperTeams = new ArrayList<>();
    private final HashMap<UHCPlayer, UHCTeam> assignedSuperTeam = new HashMap<>();
    private boolean taupeAssigned = false;

    public static TaupeGunApocalypse getInstance() {
        return instance;
    }

    @Override
    public String getName() {
        return LangManager.get().get(TaupeGunApocalypseLang.TAUPE_GUN_APOC_NAME);
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(TaupeGunApocalypseLang.TAUPE_GUN_APOC_DESC, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.NETHER_STAR);
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
        superTaupes.clear();
        revealedTaupes.clear();
        revealedSupers.clear();
        SuperTeams.clear();
        assignedSuperTeam.clear();
        calimed.clear();
        alreadyChosen.clear();
        taupeAssigned = false;
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
        shuffleMultiTaupe(mole, molesize);
    }

    @Override
    public void onTaupeTcCMD(Player player, int x, int y, int z, String coordsMessage) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer.getTeam().isEmpty()) return;
        Map<String, Object> placeholders = Map.of("%co%", coordsMessage);

        if (TeamsTaupe.contains(uhcPlayer.getTeam().get())) {
            String taupeMessage = LangManager.get().get(TaupeGunApocalypseLang.TAUPE_COORDS_FORMAT, player, placeholders);
            uhcPlayer.getTeam().get().getPlayers().forEach(tp -> tp.getPlayer().sendMessage(taupeMessage));
        } else {
            String teamMessage = LangManager.get().get(TaupeGunApocalypseLang.TEAM_COORDS_FORMAT, player, placeholders);
            uhcPlayer.getTeam().get().getPlayers().forEach(tp -> tp.getPlayer().sendMessage(teamMessage));

            for (Map.Entry<UHCPlayer, UHCTeam> entry : oldTeam.entrySet()) {
                if (entry.getValue().equals(uhcPlayer.getTeam().get())) {
                    entry.getKey().getPlayer().sendMessage(teamMessage);
                    break;
                }
            }
        }
    }

    @Override
    public void onGameStart() {
        CommandManager.get().register("taupegunapocalypse", new TaupeApocCMD(), "tga");
    }

    private void shuffleMultiTaupe(int mole, int size) {
        List<UHCTeam> picked = new ArrayList<>();
        Random random = new Random();
        createTeamTaupe(size);

        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player -> {
            if (player.getTeam().isEmpty()) return;
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
                        LangManager.get().get(TaupeGunApocalypseLang.TAUPE_ASSIGNED_TITLE, chosenPlayer.getPlayer()),
                        LangManager.get().get(TaupeGunApocalypseLang.TAUPE_ASSIGNED_SUBTITLE, chosenPlayer.getPlayer()),
                        10
                );
                sendKitDescription(kitnumber, chosenPlayer);
                addTaupe(chosenPlayer, chosenTeam);
            }

            if (isScenarioActive("TeamInventory")) {
                for (UHCTeam t : TeamsTaupe) {
                    TeamInv.inventory.put(t, TeamInv.createShared());
                }
            }
        });

        designateSuperTaupes(random);
    }

    private void designateSuperTaupes(Random random) {
        Pattern[] patterns = new Pattern[]{};
        int idx = 0;
        for (UHCTeam taupeTeamGroup : TeamsTaupe) {
            List<UHCPlayer> members = new ArrayList<>(taupeTeamGroup.getPlayers());
            if (members.isEmpty()) continue;
            UHCPlayer chosen = members.get(random.nextInt(members.size()));
            superTaupes.add(chosen);

            UHCTeam superTeam = new UHCTeam(DyeColor.BLACK, "§4§lS§r", "ApocSuper " + idx, patterns, 1, true);
            UHCTeamManager.get().addTeams(superTeam);
            SuperTeams.add(superTeam);
            assignedSuperTeam.put(chosen, superTeam);
            idx++;

            UHCTeam originalTeam = oldTeam.get(chosen);
            chosen.forceSetTeam(Optional.of(superTeam));

            Player p = chosen.getPlayer();
            if (p != null) {
                if (originalTeam != null) {
                    TeamsTagsManager.setNameTag(p, originalTeam.name(), originalTeam.prefix(), "");
                }
                DisplayService.title(
                        p,
                        LangManager.get().get(TaupeGunApocalypseLang.SUPER_TAUPE_ASSIGNED_TITLE, p),
                        LangManager.get().get(TaupeGunApocalypseLang.SUPER_TAUPE_ASSIGNED_SUBTITLE, p),
                        10
                );
            }
        }
    }

    private void sendKitDescription(int kitnumber, UHCPlayer chosenPlayer) {
        TaupeGunApocalypseLang[] kitLangs = {
                TaupeGunApocalypseLang.KIT_DESCRIPTION_0,
                TaupeGunApocalypseLang.KIT_DESCRIPTION_1,
                TaupeGunApocalypseLang.KIT_DESCRIPTION_2,
                TaupeGunApocalypseLang.KIT_DESCRIPTION_3,
                TaupeGunApocalypseLang.KIT_DESCRIPTION_4,
                TaupeGunApocalypseLang.KIT_DESCRIPTION_5,
                TaupeGunApocalypseLang.KIT_DESCRIPTION_6
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
            UHCTeam taupe = new UHCTeam(DyeColor.PURPLE, i + "", "ApocTaupe " + i, patterns, size, true);
            UHCTeamManager.get().addTeams(taupe);
            TeamsTaupe.add(taupe);
            ChatManager.get().registerChannel(new ChatManager.ChatChannel(taupeChatChannelId(taupe))
                    .prefix("?")
                    .readerRule((player, uhcPlayer) -> canReadTaupeChat(uhcPlayer, taupe))
                    .writerRule((player, uhcPlayer) -> canWriteTaupeChat(uhcPlayer, taupe))
                    .formatter((sender, receiver, message, channel) ->
                            LangManager.get().get(TaupeGunApocalypseLang.TAUPE_CHAT_FORMAT, sender,
                                    Map.of("%player%", sender.getName(), "%message%", message))));
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
                    LangManager.get().get(TaupeGunApocalypseLang.TEAM_CHAT_FORMAT, sender,
                            Map.of("%player%", sender.getName(), "%message%", message)));
        }
        Player bukkitPlayer = player.getPlayer();
        if (bukkitPlayer != null && channel != null) {
            channel.addIndirectMember(bukkitPlayer);
        }
    }

    public boolean isSuperTaupe(UHCPlayer player) {
        return superTaupes.contains(player);
    }

    public boolean hasRevealedTaupe(UHCPlayer player) {
        return revealedTaupes.contains(player);
    }

    public boolean hasRevealedSuper(UHCPlayer player) {
        return revealedSupers.contains(player);
    }

    public void markRevealedTaupe(UHCPlayer player) {
        revealedTaupes.add(player);
    }

    public void revealAsSuperTaupe(UHCPlayer player) {
        revealedSupers.add(player);
        Player bp = player.getPlayer();
        if (bp != null) {
            TeamsTagsManager.setNameTag(bp, bp.getName(), "[§4§lSUPER§r] ", "");
            bp.getInventory().addItem(new ItemCreator(Material.GOLDEN_APPLE).setAmount(4).getItemstack());
        }
    }

    public UHCTeam getAssignedSuperTeam(UHCPlayer player) {
        return assignedSuperTeam.get(player);
    }

    public UHCTeam getTaupeTeamFor(UHCPlayer player) {
        return taupeTeam.get(player);
    }

    private boolean canReadTaupeChat(UHCPlayer player, UHCTeam team) {
        if (player == null || team == null) return false;
        if (player.getTeam().isPresent() && player.getTeam().get() == team) return true;
        return superTaupes.contains(player) && taupeTeam.get(player) == team;
    }

    private boolean canWriteTaupeChat(UHCPlayer player, UHCTeam team) {
        if (player == null || team == null) return false;
        if (player.getTeam().isPresent() && player.getTeam().get() == team) return true;
        return revealedSupers.contains(player) && taupeTeam.get(player) == team;
    }

    private String taupeChatChannelId(UHCTeam team) {
        return "taupegunapocalypse:taupe:" + team.name();
    }
}

