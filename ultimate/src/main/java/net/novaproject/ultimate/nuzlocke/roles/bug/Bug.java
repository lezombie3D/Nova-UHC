package net.novaproject.ultimate.nuzlocke.roles.bug;
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

public class Bug extends NuzlockeRole {

    @Var(name = "Nuzlocke Bug Cobweb", type = VariableType.ABILITY)
    private Ability cobweb;

    @Var(name = "Nuzlocke Bug Gapple", type = VariableType.ABILITY)
    private Ability gapple;

    @Var(name = "Nuzlocke Bug Spider", type = VariableType.ABILITY)
    private Ability spider;

    @Var(name = "Nuzlocke Bug Burst Dur", type = VariableType.TIME)
    private int burstSeconds = 300;

    public static final Map<UUID, Integer> LEAF_BURST_UNTIL = new HashMap<>();

    public Bug() {
        this.cobweb = new BugCobwebBow();
        this.gapple = new BugGappleNerfListener();
        this.spider = new BugSpiderImmunityEnv();
    }

    @Override public int getId() { return 17; }
    @Override public String getName() { return "Bug"; }
    @Override public String getTypeColor() { return "§2"; }
    @Override public Material getIconMaterial() { return Material.WEB; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.WEB).setName("§2§lBug"); }
    @Override public Lang getDescriptionLang() { return NuzlockeLang.ROLE_DESC_BUG; }

    public int getBurstSeconds() { return burstSeconds; }

    @Override
    public void onSec(Player p) {
        super.onSec(p);
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;
        int timer = net.novaproject.novauhc.UHCManager.get().getTimer();
        Integer until = LEAF_BURST_UNTIL.get(owner.getUniqueId());
        int speedAmp = (until != null && timer < until) ? 1 : 0;
        UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                new PotionEffect(PotionEffectType.SPEED, 80, speedAmp, true, false),
                new PotionEffect(PotionEffectType.WEAKNESS, 80, 1, true, false)
        }, owner);
    }
}

