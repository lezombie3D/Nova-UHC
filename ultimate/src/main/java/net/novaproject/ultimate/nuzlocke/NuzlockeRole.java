package net.novaproject.ultimate.nuzlocke;

import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.RoleDescription;
import net.novaproject.novauhc.player.UHCPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public abstract class NuzlockeRole extends Role {

    public NuzlockeRole() {
        setCamp(NuzlockeCamps.NUZLOCKE);
    }

    public abstract int getId();

    public abstract Material getIconMaterial();

    public abstract String getTypeColor();

    public abstract Lang getDescriptionLang();

    @Override
    public void appendFeatures(RoleDescription features, Player player) {
        features.line(getDescriptionLang());
    }

    public boolean isOwner(Entity entity) {
        UHCPlayer owner = getOwner();
        if (owner == null || !(entity instanceof Player p)) return false;
        Player ownerPlayer = owner.getPlayer();
        return ownerPlayer != null && ownerPlayer.equals(p);
    }

    public boolean isOwnerMelee(Entity damager) {
        return damager instanceof Player && isOwner(damager);
    }

    public boolean isOwnerArrow(Entity damager) {
        if (!(damager instanceof org.bukkit.entity.Arrow arrow)) return false;
        if (!(arrow.getShooter() instanceof Player shooter)) return false;
        return isOwner(shooter);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return getId() == ((NuzlockeRole) obj).getId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getId());
    }
}

