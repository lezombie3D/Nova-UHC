package net.novaproject.novauhc.ability;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ShortCooldownManager;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public abstract class Ability {


    private final FileConfiguration abilityconfig;
    private final String path = "api/ability/" + getAbilityPath() + ".yml";
    private int maxUse;

    public Ability() {
        ConfigUtils.createDefaultFiles(path);
        this.abilityconfig = ConfigUtils.getConfig(path);
    }

    public abstract String getAbilityPath();

    public abstract String getName();

    public void decrementMaxUse() {
        if (maxUse > 0) {
            maxUse--;
        }
    }

    public int getCooldown() {
        return abilityconfig.getInt("cooldown");
    }

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

        if (ShortCooldownManager.get(player, getAbilityPath()) != -1 || !uhcPlayer.isPlaying()) {
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
            ShortCooldownManager.put(player, getAbilityPath(), cd * 1000L);
            return true;
        }

        if (getMaxUse() == 0) {
            return false;
        }

        decrementMaxUse();

        if (cd == 0) {
            return true;
        }
        ShortCooldownManager.put(player, getAbilityPath(), cd * 1000L);
        return true;
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

}
