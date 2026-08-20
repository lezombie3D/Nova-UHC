package net.novaproject.ultimate.nuzlocke;

import net.novaproject.novauhc.scenario.role.ModeKit;
import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.ultimate.nuzlocke.NuzlockeLang;
import net.novaproject.novauhc.event.UhcRoleEvents.UhcRolesDistributedEvent;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.ultimate.nuzlocke.roles.rock.RockSturdyPassive;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class Nuzlocke extends ScenarioRole<NuzlockeRole> {

    private static Nuzlocke instance;

    public static Nuzlocke get() {
        return instance;
    }

    @Override
    public Camps[] getCamps() {
        return NuzlockeCamps.values();
    }

    @Override
    public boolean isWin() {
        long activeTeams = UHCPlayerManager.get().getPlayingOnlineUHCPlayers().stream()
                .filter(p -> p.getTeam().isPresent())
                .map(p -> p.getTeam().get())
                .distinct()
                .count();
        long solo = UHCPlayerManager.get().getPlayingOnlineUHCPlayers().stream()
                .filter(p -> p.getTeam().isEmpty())
                .count();
        return activeTeams + solo <= 1;
    }

    @Override
    public String getName() {
        return "Nuzlocke Types";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(NuzlockeLang.SCENARIO_DESC, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.EYE_OF_ENDER);
    }

    @Override
    public void setup() {
        super.setup();
        instance = this;
        ModeKit.of(this)
                .role(net.novaproject.ultimate.nuzlocke.roles.fire.Fire.class)
                .role(net.novaproject.ultimate.nuzlocke.roles.normal.Normal.class)
                .role(net.novaproject.ultimate.nuzlocke.roles.grass.Grass.class)
                .role(net.novaproject.ultimate.nuzlocke.roles.water.Water.class)
                .role(net.novaproject.ultimate.nuzlocke.roles.fighting.Fighting.class)
                .role(net.novaproject.ultimate.nuzlocke.roles.flying.Flying.class)
                .role(net.novaproject.ultimate.nuzlocke.roles.rock.Rock.class)
                .role(net.novaproject.ultimate.nuzlocke.roles.dark.Dark.class)
                .role(net.novaproject.ultimate.nuzlocke.roles.electric.Electric.class)
                .role(net.novaproject.ultimate.nuzlocke.roles.steel.Steel.class)
                .role(net.novaproject.ultimate.nuzlocke.roles.ice.Ice.class)
                .role(net.novaproject.ultimate.nuzlocke.roles.poison.Poison.class)
                .role(net.novaproject.ultimate.nuzlocke.roles.fairy.Fairy.class)
                .role(net.novaproject.ultimate.nuzlocke.roles.ghost.Ghost.class)
                .role(net.novaproject.ultimate.nuzlocke.roles.psychic.Psychic.class)
                .role(net.novaproject.ultimate.nuzlocke.roles.dragon.Dragon.class)
                .role(net.novaproject.ultimate.nuzlocke.roles.bug.Bug.class)
                .role(net.novaproject.ultimate.nuzlocke.roles.ground.Ground.class)
                .apply();
        Bukkit.getLogger().info("[Nuzlocke] Scénario Nuzlocke Types initialisé.");
    }

    @Override
    public void onGameStart() {
        if (!isgived) {
            giveRoles();
        }
        registerCommands();
    }

    private void registerCommands() {
        net.novaproject.novauhc.command.CommandManager.get().register("substitute",
                new NuzlockeRootCommand(net.novaproject.ultimate.nuzlocke.roles.normal.NormalSubstituteCommand.class));
        net.novaproject.novauhc.command.CommandManager.get().register("nearme",
                new NuzlockeRootCommand(net.novaproject.ultimate.nuzlocke.roles.grass.GrassNearCommand.class));
        net.novaproject.novauhc.command.CommandManager.get().register("sound",
                new NuzlockeRootCommand(net.novaproject.ultimate.nuzlocke.roles.dark.DarkSoundCommand.class));
        net.novaproject.novauhc.command.CommandManager.get().register("mindread",
                new NuzlockeRootCommand(net.novaproject.ultimate.nuzlocke.roles.psychic.PsychicMindreadCommand.class));
    }

    @Override
    public void giveRoles() {
        List<NuzlockeRole> allRoles = new ArrayList<>();
        for (Map.Entry<Class<? extends NuzlockeRole>, NuzlockeRole> entry : getRoleConfigs().entrySet()) {
            allRoles.add(entry.getValue());
        }
        if (allRoles.isEmpty()) {
            Bukkit.getLogger().warning("[Nuzlocke] Aucun rôle enregistré — distribution annulée.");
            isgived = true;
            Bukkit.getPluginManager().callEvent(new UhcRolesDistributedEvent(
                    Collections.unmodifiableMap(new HashMap<UHCPlayer, Role>(getPlayersRoles()))));
            return;
        }

        for (UHCPlayer player : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            if (getRoleByUHCPlayer(player) != null) continue;
            Player bp = player.getPlayer();
            if (bp == null) continue;

            NuzlockeRole picked = allRoles.get(ThreadLocalRandom.current().nextInt(allRoles.size()));
            NuzlockeRole clone = (NuzlockeRole) picked.clone();
            giveRole(player, clone);

            LangManager.get().send(NuzlockeLang.TYPE_ASSIGNED, bp, Map.of(
                    "%nuzlocke_name%", clone.getName(),
                    "%nuzlocke_color%", clone.getTypeColor()
            ));
        }
        isgived = true;
        Bukkit.getPluginManager().callEvent(new UhcRolesDistributedEvent(
                Collections.unmodifiableMap(new HashMap<UHCPlayer, Role>(getPlayersRoles()))));
    }

    @Override
    public void onStop() {
        super.onStop();
        RockSturdyPassive.clear();
    }
}

