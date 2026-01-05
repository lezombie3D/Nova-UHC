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

import java.util.*;

public abstract class ScenarioRole<T extends Role> extends Scenario {

    /** CONFIG **/
    private final Map<Class<? extends T>, Integer> default_roles = new HashMap<>();
    private final Map<Class<? extends T>, T> roleConfigs = new HashMap<>();

    /** GAME **/
    private final Map<UHCPlayer, T> players_roles = new HashMap<>();
    private boolean isgived = false;

    public abstract Camps[] getCamps();


    public void addRole(Class<? extends T> roleClass) {
        try {
            T role = roleClass.getDeclaredConstructor().newInstance();
            roleConfigs.put(roleClass, role);
            default_roles.put(roleClass, 0);
        } catch (Exception e) {
            throw new RuntimeException("Cannot register role " + roleClass.getName(), e);
        }
    }

    public int getRoleAmount(Class<? extends T> roleClass) {
        return default_roles.getOrDefault(roleClass, 0);
    }

    public void incrementRole(Class<? extends T> roleClass) {
        default_roles.put(roleClass, getRoleAmount(roleClass) + 1);
    }

    public void decrementRole(Class<? extends T> roleClass) {
        int current = getRoleAmount(roleClass);
        if (current > 0) {
            default_roles.put(roleClass, current - 1);
        }
    }

    public Map<T, Integer> getDefault_roles() {
        Map<T, Integer> result = new TreeMap<>(Comparator.comparing(Role::getName));
        default_roles.forEach((clazz, amount) ->
                result.put(roleConfigs.get(clazz), amount)
        );
        return result;
    }


    public void giveRoles() {

        Bukkit.broadcastMessage(CommonString.GIVING_ROLES.getMessage());

        List<T> pool = new ArrayList<>();

        default_roles.forEach((clazz, amount) -> {
            T base = roleConfigs.get(clazz);
            for (int i = 0; i < amount; i++) {
                pool.add((T) base.clone());
            }
        });

        Collections.shuffle(pool, new Random());

        for (UHCPlayer player : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            if (pool.isEmpty()) break;

            T role = pool.remove(0);
            players_roles.put(player, role);
            role.onGive(player);
        }

        isgived = true;
    }

    public T getRoleByUHCPlayer(UHCPlayer player) {
        return players_roles.get(player);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public CustomInventory getMenu(Player player) {
        return new ScenarioCampUi<>(player, this);
    }

    @Override
    public void onSec(Player p) {
        int timer = UHCManager.get().getTimer();

        players_roles.forEach((player, role) -> role.onSec(p));

        if (!isgived && timer == UHCManager.get().getTimerpvp()) {
            giveRoles();
        }
    }

    @Override
    public void setup() {
        super.setup();
        players_roles.forEach((player, role) -> role.onSetup());
    }

    @Override
    public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
        players_roles.forEach((p, role) -> role.onConsume(player, item, event));
    }

    @Override
    public void onPlayerInteract(Player player, PlayerInteractEvent event) {
        players_roles.forEach((p, role) -> role.onIteract(player, event));
    }

    @Override
    public void onMove(Player player, PlayerMoveEvent event) {
        players_roles.forEach((p, role) -> role.onMove(p, event));
    }

    @Override
    public void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event) {
        players_roles.forEach((p, role) -> role.onHit(entity, dammager, event));
    }

    @Override
    public void onKill(UHCPlayer killer, UHCPlayer victim) {
        players_roles.forEach((p, role) -> role.onKill(killer, victim));
    }

    public List<UHCPlayer> getPlayersByRoleName(String name) {
        List<UHCPlayer> list = new ArrayList<>();
        players_roles.forEach((player, role) -> {
            if (role.getName().equals(name)) list.add(player);
        });
        return list;
    }

    public Camps getWinningCamp() {
        Map<Camps, Integer> counts = new HashMap<>();

        players_roles.forEach((player, role) -> {
            Camps camp = role.getCamp();
            counts.put(camp, counts.getOrDefault(camp, 0) + 1);
        });

        return counts.size() == 1 ? counts.keySet().iterator().next() : null;
    }
}
