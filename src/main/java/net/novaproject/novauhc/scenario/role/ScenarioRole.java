package net.novaproject.novauhc.scenario.role;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;

import java.util.*;
import java.util.stream.Collectors;

public abstract class ScenarioRole<T extends Role<T>> extends Scenario {

    private final Map<T, Integer> default_roles = new HashMap<>();

    private final List<T> roles = new ArrayList<>();

    private Map<UHCPlayer, T> players_roles = new HashMap<>();

    public void addRole(T role) {
        default_roles.put(role, 0);
    }

    public int getRoleAmount(T role) {
        return default_roles.get(role);
    }

    public void incrementRole(T role) {
        default_roles.put(role, default_roles.get(role) + 1);
    }

    public void decrementRole(T role) {
        if (getRoleAmount(role) == 0) {
            return;
        }
        default_roles.put(role, default_roles.get(role) - 1);
    }

    public void giveRoles(){

        default_roles.forEach((role, amount) -> {
            for (int i = 0; i < amount; i++) {
                roles.add(role.duplicate());
            }
        });

        Collections.shuffle(roles, new Random(System.currentTimeMillis()));

        for (UHCPlayer player : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {

            if (roles.isEmpty()) {
                break;
            }

            T role = roles.remove(new Random(System.currentTimeMillis()).nextInt(roles.size()));

            players_roles.put(player, role);

            role.onGive(player);

        }

    }

    public Role getRoleByUHCPlayer(UHCPlayer player) {
        return players_roles.get(player);
    }

    public List<UHCPlayer> getPlayersByRoleName(String name){

        List<UHCPlayer> players = new ArrayList<>(players_roles.keySet());

        for (UHCPlayer player : players) {

            Role role = getRoleByUHCPlayer(player);

            if (role == null || !role.getName().equals(name)) {
                players.remove(player);
            }

        }

        return players;
    }
}
