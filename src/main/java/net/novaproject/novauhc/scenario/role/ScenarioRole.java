package net.novaproject.novauhc.scenario.role;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public abstract class ScenarioRole<T extends Role> extends Scenario {

    private final Map<Class<? extends T>, Integer> default_roles = new HashMap<>();
    private final Map<UHCPlayer, T> players_roles = new HashMap<>();
    public boolean isgived = false;

    public abstract Camps[] getCamps();
    private final List<T> roles = new ArrayList<>();

    public Map<T, Integer> getDefault_roles() {

        Map<T, Integer> roles = new TreeMap<>(Comparator.comparing(Role::getName));

        for (Map.Entry<Class<? extends T>, Integer> entry : default_roles.entrySet()) {
            roles.put(createRoleInstance(entry.getKey()), entry.getValue());
        }

        return roles;

    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public CustomInventory getMenu(Player player) {
        return new ScenarioCampUi<>(player, this);
    }

    public void addRole(Class<? extends T> roleClass) {
        default_roles.put(roleClass, 0);
    }

    public int getRoleAmount(Class<? extends T> roleClass) {
        return default_roles.getOrDefault(roleClass, 0);
    }

    public void incrementRole(Class<? extends T> roleClass) {
        default_roles.put(roleClass, getRoleAmount(roleClass) + 1);
    }

    public void decrementRole(Class<? extends T> roleClass) {
        if (getRoleAmount(roleClass) == 0) {
            return;
        }
        default_roles.put(roleClass, getRoleAmount(roleClass) - 1);
    }

    public T createRoleInstance(Class<? extends T> roleClass) {
        try {
            return roleClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException("Failed to create instance of role: " + roleClass.getName(), e);
        }
    }

    @Override
    public void onSec(Player p) {
        int timer = UHCManager.get().getTimer();
        players_roles.forEach((player, role) -> {
            role.onSec(p);
        });
        if (!isgived) {
            if (timer == UHCManager.get().getTimerpvp()) {
                giveRoles();
                isgived = true;
            }
        }
    }

    @Override
    public void setup() {
        super.setup();
        players_roles.forEach((player, role) -> {
            role.onSetup();
        });
    }

    public void giveRoles(){

        Bukkit.broadcastMessage(CommonString.GIVING_ROLES.getMessage());
        default_roles.forEach((role, amount) -> {
            for (int i = 0; i < amount; i++) {
                roles.add(createRoleInstance(role));
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

    public T getRoleByUHCPlayer(UHCPlayer player) {
        return players_roles.get(player);
    }

    public List<UHCPlayer> getPlayersByRoleName(String name) {
        List<UHCPlayer> players = new ArrayList<>(players_roles.keySet());

        List<UHCPlayer> validPlayers = new ArrayList<>();

        for (int i = 0; i < players.size(); i++) {
            UHCPlayer player = players.get(i);
            T role = getRoleByUHCPlayer(player);
            if (role != null && role.getName().equals(name)) {
                validPlayers.add(player);
            }
        }

        return validPlayers;
    }

    public Camps getWinningCamp() {
        Map<Camps, Integer> campCounts = new HashMap<>();

        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Role role = getRoleByUHCPlayer(uhcPlayer);
            if (role == null) continue;

            Camps camp = role.getCamp();
            campCounts.put(camp, campCounts.getOrDefault(camp, 0) + 1);
        }

        if (campCounts.size() == 1) {
            return campCounts.keySet().iterator().next();
        }

        return null;
    }


    @Override
    public void onConsume(Player player1, ItemStack item, PlayerItemConsumeEvent event) {
        players_roles.forEach((player, role) -> {
            role.onConsume(player1, item, event);
        });
    }

    @Override
    public void onPlayerInteract(Player player1, PlayerInteractEvent event) {
        players_roles.forEach((player, role) -> {
            role.onIteract(player1, event);
        });
    }

    @Override
    public void onMove(Player player, PlayerMoveEvent event) {
        players_roles.forEach((player1, role) -> {
            role.onMove(player1, event);
        });
    }

    @Override
    public void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event) {
        players_roles.forEach((uhcPlayer, role) ->
                role.onHit(entity, dammager, event)
        );
    }

    @Override
    public void onKill(UHCPlayer killer, UHCPlayer victim) {
        players_roles.forEach((uhcPlayer, role) ->
                role.onKill(killer, victim));
    }
}
