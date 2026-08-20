package net.novaproject.ultimate.nuzlocke.roles.flying;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.ultimate.nuzlocke.NuzlockeLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.ultimate.nuzlocke.NuzlockeRole;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Flying extends NuzlockeRole {

    @Var(name = "Nuzlocke Flying Jump Toggle", type = VariableType.ABILITY)
    private Ability jumpToggle;

    @Var(name = "Nuzlocke Flying Punch", type = VariableType.ABILITY)
    private Ability punch;

    @Var(name = "Nuzlocke Flying Fall", type = VariableType.ABILITY)
    private Ability fall;

    @Var(name = "Nuzlocke Flying Kb", type = VariableType.ABILITY)
    private Ability kb;

    @Var(name = "Nuzlocke Flying Yslow", type = VariableType.INTEGER)
    private int lowYThreshold = 40;

    public static final Map<UUID, Boolean> JUMP_ON = new HashMap<>();

    public Flying() {
        this.jumpToggle = new FlyingJumpToggleUse();
        this.punch = new FlyingPunchBow();
        this.fall = new FlyingFallListener();
        this.kb = new FlyingKnockbackListener();
    }

    @Override public int getId() { return 6; }
    @Override public String getName() { return "Flying"; }
    @Override public String getTypeColor() { return "§b"; }
    @Override public Material getIconMaterial() { return Material.FEATHER; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.FEATHER).setName("§b§lFlying"); }
    @Override public Lang getDescriptionLang() { return NuzlockeLang.ROLE_DESC_FLYING; }

    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        super.onGive(uhcPlayer);
        Player p = uhcPlayer.getPlayer();
        if (p == null) return;
        JUMP_ON.put(p.getUniqueId(), true);
        ItemStack feather = new ItemStack(Material.FEATHER);
        org.bukkit.inventory.meta.ItemMeta m = feather.getItemMeta();
        m.setDisplayName("§b§lFeather §7(Toggle Jump)");
        feather.setItemMeta(m);
        p.getInventory().addItem(feather);
    }

    @Override
    public void onSec(Player p) {
        super.onSec(p);
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;
        boolean jumpOn = JUMP_ON.getOrDefault(owner.getUniqueId(), true);
        PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 80, 0, true, false);
        if (jumpOn) {
            UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                    speed,
                    new PotionEffect(PotionEffectType.JUMP, 80, 2, true, false)
            }, owner);
        } else {
            UHCUtils.applyInfiniteEffects(new PotionEffect[]{ speed }, owner);
            owner.removePotionEffect(PotionEffectType.JUMP);
        }
        if (owner.getLocation().getBlockY() < lowYThreshold) {
            owner.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0, true, false));
        } else {
            owner.removePotionEffect(PotionEffectType.SLOW);
        }
    }
}

