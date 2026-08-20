package net.novaproject.ultimate.nuzlocke.roles.rock;
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

public class Rock extends NuzlockeRole {

    @Var(name = "Nuzlocke Rock Sturdy", type = VariableType.ABILITY)
    private Ability sturdy;

    @Var(name = "Nuzlocke Rock Fall", type = VariableType.ABILITY)
    private Ability fall;

    @Var(name = "Nuzlocke Rock Cobble", type = VariableType.ABILITY)
    private Ability cobble;

    public Rock() {
        this.sturdy = new RockSturdyPassive();
        this.fall = new RockFallDamageListener();
        this.cobble = new RockCobbleSmeltEnv();
    }

    @Override public int getId() { return 7; }
    @Override public String getName() { return "Rock"; }
    @Override public String getTypeColor() { return "§7"; }
    @Override public Material getIconMaterial() { return Material.COBBLESTONE; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.COBBLESTONE).setName("§7§lRock"); }
    @Override public Lang getDescriptionLang() { return NuzlockeLang.ROLE_DESC_ROCK; }

    @Override
    public void onSec(Player p) {
        super.onSec(p);
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;
        UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                new PotionEffect(PotionEffectType.FAST_DIGGING, 80, 3, true, false),
                new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 0, true, false),
                new PotionEffect(PotionEffectType.SLOW, 80, 0, true, false)
        }, owner);
    }
}

