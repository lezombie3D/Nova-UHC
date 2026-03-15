package net.novaproject.ultimate.legend;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;


@Getter
@Setter
public abstract class LegendRole extends Role {


    private transient UHCPlayer owner;

    public LegendRole() {
        setCamp(LegendCamps.LEGEND);
    }


    public abstract int getId();


    public abstract Material getIconMaterial();


    public abstract Lang getDescriptionLang();


    @Override
    public void sendDescription(Player player) {
        LangManager.get().get(getDescriptionLang());
    }

    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        this.owner = uhcPlayer;
        Player player = uhcPlayer.getPlayer();
        if (player == null) return;


        String desc = LangManager.get().get(getDescriptionLang(), player);
        desc = applyPlaceholders(desc);
        player.sendMessage(desc);


        if (!getAbilities().isEmpty()) {
            getAbilities().forEach(ability -> ability.onGive(uhcPlayer));
        }
    }


    protected String applyPlaceholders(String desc) {
        return desc;
    }


    public boolean isOwner(org.bukkit.entity.Entity entity) {
        if (owner == null || !(entity instanceof Player p)) return false;
        Player ownerPlayer = owner.getPlayer();
        return ownerPlayer != null && ownerPlayer.equals(p);
    }


    public boolean isOwnerMelee(org.bukkit.entity.Entity damager) {
        return damager instanceof Player && isOwner(damager);
    }


    public boolean isOwnerArrow(org.bukkit.entity.Entity damager) {
        if (!(damager instanceof org.bukkit.entity.Arrow arrow)) return false;
        if (!(arrow.getShooter() instanceof Player shooter)) return false;
        return isOwner(shooter);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return getId() == ((LegendRole) obj).getId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getId());
    }
}
