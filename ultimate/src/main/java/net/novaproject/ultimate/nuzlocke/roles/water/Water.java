package net.novaproject.ultimate.nuzlocke.roles.water;
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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Water extends NuzlockeRole {

    @Var(name = "Nuzlocke Water Muddy", type = VariableType.ABILITY)
    private Ability muddy;

    @Var(name = "Nuzlocke Water Sponge", type = VariableType.ABILITY)
    private Ability sponge;

    public Water() {
        this.muddy = new WaterMuddyBow();
        this.sponge = new WaterSpongeUse();
    }

    @Override public int getId() { return 3; }
    @Override public String getName() { return "Water"; }
    @Override public String getTypeColor() { return "§9"; }
    @Override public Material getIconMaterial() { return Material.WATER_BUCKET; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.WATER_BUCKET).setName("§9§lWater"); }
    @Override public Lang getDescriptionLang() { return NuzlockeLang.ROLE_DESC_WATER; }

    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        super.onGive(uhcPlayer);
        Player p = uhcPlayer.getPlayer();
        if (p == null) return;
        ItemStack boots = new ItemStack(p.getInventory().getBoots() != null
                ? p.getInventory().getBoots().getType() : Material.LEATHER_BOOTS);
        ItemMeta m = boots.getItemMeta();
        boots.setItemMeta(m);
        boots.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, 2);
        p.getInventory().setBoots(boots);
    }

    @Override
    public void onSec(Player p) {
        super.onSec(p);
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;

        UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                new PotionEffect(PotionEffectType.WATER_BREATHING, 80, 0, true, false)
        }, owner);

        owner.setFireTicks(0);

        Block at = owner.getLocation().getBlock();
        Block under = at.getRelative(0, -1, 0);

        boolean nearLava = isNear(at, Material.LAVA, 2) || isNear(at, Material.STATIONARY_LAVA, 2);
        if (nearLava) {
            owner.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0, true, false));
        }

        boolean onGrassLeaves = under.getType() == Material.GRASS || under.getType() == Material.LEAVES
                || under.getType() == Material.LEAVES_2;
        if (onGrassLeaves) {
            owner.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 0, true, false));
        }
    }

    private boolean isNear(Block center, Material type, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (center.getRelative(dx, dy, dz).getType() == type) return true;
                }
            }
        }
        return false;
    }
}

