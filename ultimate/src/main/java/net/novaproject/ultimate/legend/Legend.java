package net.novaproject.ultimate.legend;

import net.novaproject.novauhc.scenario.role.ModeKit;import net.novaproject.novauhc.utils.variable.Var;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.CommandManager;
import net.novaproject.novauhc.lang.LangManager;

import net.novaproject.novauhc.event.UhcRoleEvents.UhcRolesDistributedEvent;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.item.ItemCreator;

import net.novaproject.novauhc.utils.variable.VariableType;

import net.novaproject.ultimate.legend.roles.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class Legend extends ScenarioRole<LegendRole> {

    private static Legend instance;

    @Var(name = "Legend Var Choose Time", type = VariableType.INTEGER)
    private int chooseTime = 3;

    private boolean canChooseClass = true;

    private final Set<UUID> manuallyChosen = new HashSet<>();

    public static Legend get() {
        return instance;
    }

    @Override
    public Camps[] getCamps() {
        return LegendCamps.values();
    }

    @Override
    public boolean isWin() {

        long activeTeams = UHCPlayerManager.get().getPlayingOnlineUHCPlayers().stream()
                .filter(p -> p.getTeam().isPresent())
                .map(p -> p.getTeam().get())
                .distinct()
                .count();
        return activeTeams <= 1;
    }

    @Override
    public String getName() {
        return "UHC Legends";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.LEGEND, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.DRAGON_EGG);
    }

    @Override
    public void setup() {
        super.setup();
        instance = this;
        canChooseClass = true;
        manuallyChosen.clear();
        ModeKit.of(this)
                .role(Assassin.class)
                .role(Mage.class)
                .role(Archer.class)
                .role(Tank.class)
                .role(Nain.class)
                .role(Zeus.class)
                .role(Necromancien.class)
                .role(Succube.class)
                .role(Soldat.class)
                .role(Princesse.class)
                .role(Cavalier.class)
                .role(Ogre.class)
                .role(Dragon.class)
                .role(Medecin.class)
                .role(Prisonnier.class)
                .role(Corne.class)
                .role(Marionnettiste.class)
                .role(Paladin.class)
                .apply();
        Bukkit.getLogger().info("[Legend] Scénario UHC Legends initialisé.");
    }

    @Override
    public void incrementRole(Class<? extends LegendRole> roleClass) {
        if (getRoleAmount(roleClass) >= 1) return;
        super.incrementRole(roleClass);
    }

    @Override
    public void onStart(Player player) {

        String msg = LangManager.get().get(LegendLang.CHOOSE_CLASS_WELCOME, player)
                .replace("%choose_time%", String.valueOf(chooseTime));
        player.sendMessage(msg);
    }

    @Override
    public void onSec(Player p) {
        super.onSec(p);

        int timer = UHCManager.get().getTimer();

        if (canChooseClass && timer >= chooseTime * 60) {
            canChooseClass = false;
            giveRoles();
        }
    }

    @Override
    public void onGameStart() {
        CommandManager.get().register("legend", new LdCMD(), "ld");
    }

    @Override
    public void giveRoles() {
        LangManager.get().sendAll(LegendLang.CHOOSE_CLASS_TIME_EXPIRED);

        List<LegendRole> pool = new ArrayList<>();
        getDefault_roles().forEach((role, amount) -> {
            if (amount >= 1) {
                pool.add(role);
            }
        });

        if (pool.isEmpty()) {
            Bukkit.getLogger().warning("[Legend] Aucun rôle activé dans le pool !");
            isgived = true;
            Bukkit.getPluginManager().callEvent(new UhcRolesDistributedEvent(
                    Collections.unmodifiableMap(new HashMap<UHCPlayer, Role>(getPlayersRoles()))));
            return;
        }

        for (UHCPlayer player : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {

            if (getRoleByUHCPlayer(player) != null) continue;

            Player bp = player.getPlayer();
            if (bp == null) continue;

            LegendRole selected = pickRandomAvailable(player, pool);
            if (selected == null) {

                selected = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
            }

            LegendRole clone = (LegendRole) selected.clone();
            giveRole(player, clone);

            String msg = LangManager.get().get(LegendLang.CLASS_ASSIGNED_RANDOM, bp)
                    .replace("%legend_name%", clone.getName());
            bp.sendMessage(msg);
        }

        isgived = true;
        Bukkit.getPluginManager().callEvent(new UhcRolesDistributedEvent(
                Collections.unmodifiableMap(new HashMap<UHCPlayer, Role>(getPlayersRoles()))));
    }

    public boolean chooseRole(UHCPlayer player, LegendRole role) {
        Player bp = player.getPlayer();
        if (bp == null) return false;

        if (getRoleByUHCPlayer(player) != null) {
            LangManager.get().send(LegendLang.CLASS_ALREADY_CHOSEN, bp);
            return false;
        }

        if (isRoleTakenInTeam(player, role)) {
            String msg = LangManager.get().get(LegendLang.CLASS_TAKEN_IN_TEAM, bp)
                    .replace("%legend_name%", role.getName());
            bp.sendMessage(msg);
            return false;
        }

        LegendRole clone = (LegendRole) role.clone();
        giveRole(player, clone);
        manuallyChosen.add(bp.getUniqueId());

        String msg = LangManager.get().get(LegendLang.CLASS_ASSIGNED_SUCCESS, bp)
                .replace("%legend_name%", clone.getName());
        bp.sendMessage(msg);

        return true;
    }

    public boolean isRoleTakenInTeam(UHCPlayer player, LegendRole role) {
        if (!player.getTeam().isPresent()) return false;

        for (UHCPlayer teammate : player.getTeam().get().getPlayers()) {
            if (teammate.equals(player)) continue;

            LegendRole teammateRole = getRoleByUHCPlayer(teammate);
            if (teammateRole != null && teammateRole.getId() == role.getId()) {
                return true;
            }
        }
        return false;
    }

    private LegendRole pickRandomAvailable(UHCPlayer player, List<LegendRole> pool) {
        List<LegendRole> available = new ArrayList<>();
        for (LegendRole role : pool) {
            if (!isRoleTakenInTeam(player, role)) {
                available.add(role);
            }
        }

        if (available.isEmpty()) return null;
        return available.get(ThreadLocalRandom.current().nextInt(available.size()));
    }

    public List<LegendRole> getActivatedRoles() {
        List<LegendRole> roles = new ArrayList<>();
        getDefault_roles().forEach((role, amount) -> {
            if (amount >= 1) roles.add(role);
        });
        return roles;
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        if(UHCManager.get().getTeam_size() > 1){
            UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
            return;
        }
        uhcPlayer.getPlayer().teleport(location);
    }
}

