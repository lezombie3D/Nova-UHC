package net.novaproject.ultimate.nuzlocke.roles.ground;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.ultimate.nuzlocke.NuzlockeLang;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.ultimate.nuzlocke.NuzlockeRole;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Ground extends NuzlockeRole {

    @Var(name = "Nuzlocke Ground Dig", type = VariableType.ABILITY)
    private Ability dig;

    @Var(name = "Nuzlocke Ground Jump", type = VariableType.ABILITY)
    private Ability jumpBlock;

    @Var(name = "Nuzlocke Ground Fall", type = VariableType.ABILITY)
    private Ability noFall;

    public Ground() {
        this.dig = new GroundDigUse();
        this.jumpBlock = new GroundJumpPreventBow();
        this.noFall = new GroundWaterBlindNoFallListener();
    }

    @Override public int getId() { return 18; }
    @Override public String getName() { return "Ground"; }
    @Override public String getTypeColor() { return "§6"; }
    @Override public Material getIconMaterial() { return Material.DIRT; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.DIRT).setName("§6§lGround"); }
    @Override public Lang getDescriptionLang() { return NuzlockeLang.ROLE_DESC_GROUND; }

    @Override
    public void onSec(Player p) {
        super.onSec(p);
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;
        UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                new PotionEffect(PotionEffectType.SLOW, 80, 0, true, false)
        }, owner);
        Material at = owner.getLocation().getBlock().getType();
        if (at == Material.WATER || at == Material.STATIONARY_WATER) {
            owner.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, true, false), true);
        }
    }
}

