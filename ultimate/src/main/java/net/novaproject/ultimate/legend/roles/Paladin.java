package net.novaproject.ultimate.legend.roles;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.ultimate.legend.LegendLang;
import net.novaproject.ultimate.legend.LegendRole;
import net.novaproject.ultimate.legend.roles.abilities.PaladinBlessingActive;
import net.novaproject.ultimate.legend.roles.abilities.PaladinLowHealthPassive;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

public class Paladin extends LegendRole {

    @Var(name = "Paladin Extra Hearts", desc = "Extra hearts.", type = VariableType.INTEGER)
    private int extraHearts = 4;

    @Var(name = "Low Health", type = VariableType.ABILITY)
    public Ability lowHealthPassive;

    @Var(name = "Blessing", type = VariableType.ABILITY)
    public Ability blessingActive;

    public Paladin() {
        this.lowHealthPassive = new PaladinLowHealthPassive();
        this.blessingActive   = new PaladinBlessingActive();
    }

    @Override public int getId() { return 18; }
    @Override public String getName() { return "Paladin"; }
    @Override public Material getIconMaterial() { return Material.GOLD_SWORD; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLD_SWORD); }
    @Override public Lang getDescriptionLang() { return LegendLang.ROLE_DESC_PALADIN; }
    @Override protected String applyPlaceholders(String d) { return d.replace("%extra_hearts%", String.valueOf(extraHearts / 2)); }

    @Override
    public void onGive(UHCPlayer u) {
        super.onGive(u); Player p = u.getPlayer(); if (p == null) return;
        p.setMaxHealth(20 + extraHearts); p.setHealth(20 + extraHearts);
        p.getInventory().addItem(new ItemCreator(Material.GOLD_SWORD)
                .addEnchantment(Enchantment.DAMAGE_ALL, 2)
                .setName("§6§lÉpée Sacrée").setUnbreakable(true).getItemstack());
    }
}

