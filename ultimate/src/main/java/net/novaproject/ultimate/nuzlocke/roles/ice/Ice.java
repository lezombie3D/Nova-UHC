package net.novaproject.ultimate.nuzlocke.roles.ice;
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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Ice extends NuzlockeRole {

    @Var(name = "Nuzlocke Ice Freeze", type = VariableType.ABILITY)
    private Ability freeze;

    @Var(name = "Nuzlocke Ice Blizzard", type = VariableType.ABILITY)
    private Ability blizzard;

    @Var(name = "Nuzlocke Ice Vuln", type = VariableType.ABILITY)
    private Ability vuln;

    public Ice() {
        this.freeze = new IceFreezeDryUse();
        this.blizzard = new IceBlizzardBow();
        this.vuln = new IceMeleeVulnerableListener();
    }

    @Override public int getId() { return 11; }
    @Override public String getName() { return "Ice"; }
    @Override public String getTypeColor() { return "§b"; }
    @Override public Material getIconMaterial() { return Material.PACKED_ICE; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.PACKED_ICE).setName("§b§lIce"); }
    @Override public Lang getDescriptionLang() { return NuzlockeLang.ROLE_DESC_ICE; }

    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        super.onGive(uhcPlayer);
        Player p = uhcPlayer.getPlayer();
        if (p == null) return;
        ItemStack pi = new ItemStack(Material.PACKED_ICE);
        ItemMeta m = pi.getItemMeta();
        m.setDisplayName("§b§lIce Walking §7(Toggle)");
        pi.setItemMeta(m);
        p.getInventory().addItem(pi);
    }

    @Override
    public void onSec(Player p) {
        super.onSec(p);
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;
        Block under = owner.getLocation().getBlock().getRelative(0, -1, 0);
        boolean nearIce = isNearIce(owner.getLocation().getBlock());
        int speedAmp = nearIce ? 1 : 0;
        UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                new PotionEffect(PotionEffectType.SPEED, 80, speedAmp, true, false)
        }, owner);
    }

    private boolean isNearIce(Block c) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                Material t = c.getRelative(dx, -1, dz).getType();
                if (t == Material.ICE || t == Material.PACKED_ICE) return true;
            }
        }
        return false;
    }
}

