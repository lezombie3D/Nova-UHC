package net.novaproject.ultimate.legend.roles;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.ultimate.legend.LegendLang;
import net.novaproject.ultimate.legend.LegendRole;
import net.novaproject.ultimate.legend.roles.abilities.SoldatEquipmentPassive;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

public class Soldat extends LegendRole {

    @Var(name = "Equipment", type = VariableType.ABILITY)
    public Ability equipmentPassive;

    public Soldat() {
        this.equipmentPassive = new SoldatEquipmentPassive();
    }

    @Override public int getId() { return 9; }
    @Override public String getName() { return "Soldat"; }
    @Override public Material getIconMaterial() { return Material.IRON_CHESTPLATE; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.IRON_CHESTPLATE); }
    @Override public Lang getDescriptionLang() { return LegendLang.ROLE_DESC_SOLDAT; }

    @Override
    public void onGive(UHCPlayer u) {
        super.onGive(u); Player p = u.getPlayer(); if (p == null) return;
        p.getInventory().setHelmet(new ItemCreator(Material.IRON_HELMET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).getItemstack());
        p.getInventory().setChestplate(new ItemCreator(Material.IRON_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).getItemstack());
        p.getInventory().setLeggings(new ItemCreator(Material.IRON_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).getItemstack());
        p.getInventory().setBoots(new ItemCreator(Material.IRON_BOOTS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).getItemstack());
        p.getInventory().addItem(new ItemCreator(Material.IRON_SWORD).addEnchantment(Enchantment.DAMAGE_ALL, 1).getItemstack());
    }
}

