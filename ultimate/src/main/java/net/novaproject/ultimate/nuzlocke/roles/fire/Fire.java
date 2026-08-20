package net.novaproject.ultimate.nuzlocke.roles.fire;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Fire extends NuzlockeRole {

    @Var(name = "Nuzlocke Fire Arrow", type = VariableType.ABILITY)
    private Ability arrow;

    @Var(name = "Nuzlocke Fire Cutclean", type = VariableType.ABILITY)
    private Ability cutclean;

    @Var(name = "Nuzlocke Fire Wood", type = VariableType.ABILITY)
    private Ability wood;

    @Var(name = "Nuzlocke Fire Trail", type = VariableType.ABILITY)
    private Ability trail;

    public Fire() {
        this.arrow = new FireArrowBow();
        this.cutclean = new FireCutcleanListener();
        this.wood = new FireWoodPenaltyListener();
        this.trail = new FireTrailEnv();
    }

    @Override public int getId() { return 1; }
    @Override public String getName() { return "Fire"; }
    @Override public String getTypeColor() { return "§c"; }
    @Override public Material getIconMaterial() { return Material.BLAZE_POWDER; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.BLAZE_POWDER).setName("§c§lFire"); }
    @Override public Lang getDescriptionLang() { return NuzlockeLang.ROLE_DESC_FIRE; }

    @Override
    public void onSec(Player p) {
        super.onSec(p);
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;

        UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, 0, true, false)
        }, owner);

        Block block = owner.getLocation().getBlock();
        boolean inWater = block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER;
        if (inWater) {
            owner.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 60, 0, true, false));
        }
    }
}

