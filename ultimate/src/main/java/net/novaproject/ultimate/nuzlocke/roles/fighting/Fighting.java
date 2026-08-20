package net.novaproject.ultimate.nuzlocke.roles.fighting;
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

public class Fighting extends NuzlockeRole {

    @Var(name = "Nuzlocke Fighting Detect", type = VariableType.ABILITY)
    private Ability detect;

    @Var(name = "Nuzlocke Fighting Close", type = VariableType.ABILITY)
    private Ability close;

    @Var(name = "Nuzlocke Fighting Bow Malus", type = VariableType.ABILITY)
    private Ability bowMalus;

    @Var(name = "Nuzlocke Fighting Piercer", type = VariableType.ABILITY)
    private Ability piercer;

    public Fighting() {
        this.detect = new FightingDetectListener();
        this.close = new FightingCloseCombatMelee();
        this.bowMalus = new FightingBowMalusListener();
        this.piercer = new FightingResistancePiercerListener();
    }

    @Override public int getId() { return 5; }
    @Override public String getName() { return "Fighting"; }
    @Override public String getTypeColor() { return "§6"; }
    @Override public Material getIconMaterial() { return Material.IRON_AXE; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.IRON_AXE).setName("§6§lFighting"); }
    @Override public Lang getDescriptionLang() { return NuzlockeLang.ROLE_DESC_FIGHTING; }

    @Override
    public void onSec(Player p) {
        super.onSec(p);
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;
        UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, 0, true, false)
        }, owner);
    }
}

