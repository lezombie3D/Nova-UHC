package net.novaproject.novauhc.ability;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ShortCooldownManager;
import net.novaproject.novauhc.utils.VariableType;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

@Getter
@Setter
public abstract class Ability implements Cloneable {

    @AbilityVariable(name = "Nombre d'utilisation", description = "Nombre d'utilisation de la capacité", type = VariableType.INTEGER)
    private int maxUse = -1;


    public abstract String getName();

    public void decrementMaxUse() {
        if (maxUse > 0) {
            maxUse--;
        }
    }

    public abstract boolean active();

    public abstract int getCooldown();

    public Material getMaterial() {
        return Material.NETHER_STAR;
    }

    public ItemStack getItemStack() {
        if (getMaterial() == null) return new ItemStack(Material.AIR);
        return new ItemCreator(getMaterial()).setName("§8» §f§l" + getName() + " §8«").getItemstack();
    }
    


    /*public boolean hasAbility(Player player) {
        UHCPlayer UHCPlayer = getUHCPlayer(player);
        if (UHCPlayer == null || UHCPlayer.getPersoActuel() == null) return false;
        return UHCPlayer.getPersoActuel().getAbilities().contains(this);
    }*/

    public abstract boolean onEnable(Player player);

    @Override
    public boolean equals(Object o) {
        return o != null && this.getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

    public boolean tryUse(Player player) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        if (ShortCooldownManager.get(player, getName() + "Cooldown") != -1 || !uhcPlayer.isPlaying()) {
            return false;
        }

        if (!onEnable(player)) {
            return false;
        }

        int cd = getCooldown();

        if (getMaxUse() == -1) {
            if (cd == 0) {
                return true;
            }
            ShortCooldownManager.put(player, getName() + "Cooldown", cd * 1000L);
            return true;
        }

        if (getMaxUse() == 0) {
            return false;
        }

        decrementMaxUse();

        if (cd == 0) {
            return true;
        }
        ShortCooldownManager.put(player, getName() + "Cooldown", cd * 1000L);
        return true;
    }

    public void onGive(UHCPlayer uhcPlayer) {
        uhcPlayer.getPlayer().getInventory().addItem(getItemStack());
    }

    public UHCPlayer getUHCPlayer(Player player) {
        return UHCPlayerManager.get().getPlayer(player);
    }

    public void onSec(Player player) {
        AbilityManager.get().updateAbilityBar(player);
    }

    public void onMove(PlayerMoveEvent event) {
    }

    public void onAttack(UHCPlayer victimP, EntityDamageByEntityEvent event) {

    }


    public void onDeath() {
    }

    public void onKill(UHCPlayer killedP) {
    }

    public void onClick(PlayerInteractEvent event, ItemStack item) {
    }

    public void onConsume(PlayerItemConsumeEvent event) {
    }


    @Override
    public Ability clone() {
        try {
            return (Ability) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public Document abilityToDoc() {
        Document doc = new Document();
        Class<?> clazz = getClass();

        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {

                if (!field.isAnnotationPresent(AbilityVariable.class)) continue;

                field.setAccessible(true);
                AbilityVariable annotation = field.getAnnotation(AbilityVariable.class);

                try {
                    Object value = field.get(this);

                    if (value == null) continue;

                    switch (annotation.type()) {
                        case TIME -> {
                            if (value instanceof Integer i)
                                doc.append(field.getName(), i);
                        }
                        case PERCENTAGE -> {
                            if (value instanceof Double d)
                                doc.append(field.getName(), d);
                            else if (value instanceof Integer i)
                                doc.append(field.getName(), i);
                        }
                        default -> doc.append(field.getName(), value);
                    }

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            clazz = clazz.getSuperclass();
        }

        return doc;
    }

    public void docToAbility(Document doc) {
        if (doc == null) return;

        Class<?> clazz = getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {

                if (!field.isAnnotationPresent(AbilityVariable.class)) continue;

                field.setAccessible(true);

                try {
                    if (doc.containsKey(field.getName())) {
                        Object value = doc.get(field.getName());

                        AbilityVariable annotation = field.getAnnotation(AbilityVariable.class);

                        switch (annotation.type()) {
                            case TIME -> {
                                if (value instanceof Number) {
                                    field.set(this, ((Number) value).intValue());
                                }
                            }
                            case PERCENTAGE -> {
                                if (value instanceof Number) {
                                    if (field.getType() == double.class || field.getType() == Double.class) {
                                        field.set(this, ((Number) value).doubleValue());
                                    } else if (field.getType() == int.class || field.getType() == Integer.class) {
                                        field.set(this, ((Number) value).intValue());
                                    }
                                }
                            }
                            default -> field.set(this, value);
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            clazz = clazz.getSuperclass();
        }
    }

}
