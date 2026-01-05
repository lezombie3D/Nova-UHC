package net.novaproject.novauhc.scenario.role;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public abstract class Role implements Cloneable {
    private Set<Ability> abilities = new HashSet<>();

    public abstract String getName();
    public abstract String getDescription();

    private Camps camp;

    public String getColor() {
        return camp.getColor();
    }

    public abstract ItemCreator getItem();

    public void onGive(UHCPlayer uhcPlayer) {

        Player player = uhcPlayer.getPlayer();

        player.sendMessage(getDescription());

        if(!getAbilities().isEmpty()) getAbilities().forEach(ability -> ability.onGive(uhcPlayer));
    }


    public void onSec(Player player){
        if (!getAbilities().isEmpty()) getAbilities().forEach(ability -> ability.onSec(player));
    }

    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (!getAbilities().isEmpty()) getAbilities().forEach(Ability::onDeath);
    }

    public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
        if (!getAbilities().isEmpty()) getAbilities().forEach(ability -> ability.onConsume(event));

    }

    public void onIteract(Player player1, PlayerInteractEvent event) {
        if (!getAbilities().isEmpty()) getAbilities().forEach(ability -> ability.onClick(event, event.getItem()));

    }

    public void onMove(UHCPlayer player1, PlayerMoveEvent event) {
        if (!getAbilities().isEmpty()) getAbilities().forEach(ability -> ability.onMove(event));

    }

    public void onFfCMD(UHCPlayer player1, String subCommand, String[] args) {

    }

    public void onSetup() {

    }

    public void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event) {
        if (!getAbilities().isEmpty()) getAbilities().forEach(ability -> {
            if (entity instanceof Player) ability.onAttack(UHCPlayerManager.get().getPlayer((Player) entity), event);
        });

    }

    public void onKill(UHCPlayer killer, UHCPlayer victim) {
        if (!getAbilities().isEmpty()) getAbilities().forEach(ability -> ability.onKill(victim));
    }

    @Override
    public Role clone() {
        try {
            Role clone = (Role) super.clone();

            Set<Ability> clonedAbilities = new HashSet<>();
            for (Ability ability : this.abilities) {
                clonedAbilities.add(ability.clone());
            }

            clone.setAbilities(clonedAbilities);
            return clone;

        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
