package net.novaproject.ultimate.nuzlocke.roles.steel;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Steel extends NuzlockeRole {

    @Var(name = "Nuzlocke Steel Magnet", type = VariableType.ABILITY)
    private Ability magnet;

    @Var(name = "Nuzlocke Steel Double", type = VariableType.ABILITY)
    private Ability doubleOre;

    @Var(name = "Nuzlocke Steel Fire", type = VariableType.ABILITY)
    private Ability fireVuln;

    @Var(name = "Nuzlocke Steel Relief", type = VariableType.ABILITY)
    private Ability ironRelief;

    public static final Map<UUID, Long> RELIEF_UNTIL = new HashMap<>();

    public Steel() {
        this.magnet = new SteelMagnetMelee();
        this.doubleOre = new SteelDoubleOreListener();
        this.fireVuln = new SteelFireVulnerableListener();
        this.ironRelief = new SteelIronOreReliefListener();
    }

    @Override public int getId() { return 10; }
    @Override public String getName() { return "Steel"; }
    @Override public String getTypeColor() { return "§7"; }
    @Override public Material getIconMaterial() { return Material.IRON_INGOT; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.IRON_INGOT).setName("§7§lSteel"); }
    @Override public Lang getDescriptionLang() { return NuzlockeLang.ROLE_DESC_STEEL; }

    @Override
    public void onSec(Player p) {
        super.onSec(p);
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;
        Long reliefUntil = RELIEF_UNTIL.get(owner.getUniqueId());
        int slowAmp = (reliefUntil != null && System.currentTimeMillis() < reliefUntil) ? 0 : 1;
        UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 0, true, false),
                new PotionEffect(PotionEffectType.SLOW, 80, slowAmp, true, false)
        }, owner);
    }
}

