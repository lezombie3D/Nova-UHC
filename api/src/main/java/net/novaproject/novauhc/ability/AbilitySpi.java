package net.novaproject.novauhc.ability;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.novaproject.novauhc.game.GameSpi.Clearable;
import net.novaproject.novauhc.player.PlayerSpi.IUHCPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class AbilitySpi {

    private AbilitySpi() {
    }

    public interface IAbility {

        String getName();

        int getMaxUse();

        void setMaxUse(int maxUse);

        int getCooldown();

        void setCooldown(int cooldown);

        boolean isActive();

        void setActive(boolean active);

        boolean active();

        boolean isDisabled();

        void setDisabled(boolean disabled);

        boolean isStolen();

        void setStolen(boolean stolen);

        void decrementMaxUse();

        boolean canBeDisabled();

        String getDescription(Player player);

        boolean isUsable();

        Material getMaterial();

        String abilityId();

        ItemStack getItemStack();

        boolean isAbilityItem(ItemStack item);

        boolean onEnable(Player player);

        boolean isManualAbility();

        boolean tryUse(Player player);

        void fireUsed(Player player);

        String activeCooldownKey(Player player);

        String activeCooldownDisplayName(Player player);

        int activeCooldownTotalSeconds(Player player);

        int activeCooldownMaxUse(Player player);

        IUHCPlayer getUHCPlayer(Player player);

        IUHCPlayer getOwner();
    }

    public interface IAbilityManager {

        Set<? extends IAbility> abilitiesOf(Player owner);

        <H> void fire(Player owner, Class<H> hook, Consumer<H> call);

        <H> void fireGlobal(Class<H> hook, Consumer<H> call);

        boolean isAbilityItem(ItemStack item);
    }

    public interface IAbilitySuppression extends Clearable {

        int suppress(IUHCPlayer target, int seconds);

        int suppress(IUHCPlayer target, int seconds, boolean announce);

        void restore(UUID uuid);

        boolean isSuppressed(UUID uuid);

        int remainingSeconds(UUID uuid);
    }
}
