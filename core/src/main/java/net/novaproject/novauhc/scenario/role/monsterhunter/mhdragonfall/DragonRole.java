package net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.normal.GoldenHead;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.resistance.ResistanceProfile;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public abstract class DragonRole extends Role {

    private final Map<ElementType, Double> elementPowers = new EnumMap<>(ElementType.class);
    private final Map<ElementType, Double> blightChances = new EnumMap<>(ElementType.class);
    private final Map<ElementType, Integer> blightDuration = new HashMap<>();

    private ResistanceProfile resistanceProfile;

    private int currentHP;
    private int currentStrength;
    private int currentResistance;
    private int currentCritDamage;
    private int currentCritChance;
    private int absortion = 0;

    public DragonRole() {
        this.resistanceProfile = new ResistanceProfile();
        initResistances();
        initElements();
    }

    public abstract int getMaxHP();
    public abstract int getStrength();
    public abstract int getResistance();
    public abstract int getCritDamage();
    public abstract int getCritChance();
    public abstract void initResistances();

    public abstract void initElements();

    public double getProjectileMultiplier() {
        return 1.0D;
    }

    public void addElement(ElementType type, double power) {
        elementPowers.put(type, power);
    }

    public double getBlightChance(ElementType type) {
        Double val = blightChances.get(type);
        return val == null ? 0.0D : val;
    }

    public void setBlightChance(ElementType type, double chance) {
        blightChances.put(type, Math.max(0.0D, Math.min(100.0D, chance)));
    }

    public void setBlightDuration(ElementType type, int duration) {
        blightDuration.put(type, Math.max(1, duration));
    }

    public int getBlightDuration(ElementType type) {
        Integer val = blightDuration.get(type);
        return val == null ? 0 : val;
    }

    public DragonRole getEvolution() {
        return null;
    }

    @Override
    public String getColor() {
        return getCamp().getColor();
    }

    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        super.onGive(uhcPlayer);
        currentHP = getMaxHP();
        currentCritChance = getCritChance();
        currentStrength = getStrength();
        currentResistance = getResistance();
        currentCritDamage = getCritDamage();
        new DragonStatsDisplay(uhcPlayer.getPlayer(), this).start();
    }

    public void onGoldenHeadUse(Player player) {
        setAbsortion((int) (getMaxHP() * 0.40));
        heal((int) (getMaxHP() * 0.60), player);
    }

    public void onGoldenAppleUse(Player player) {
        setAbsortion((int) (getMaxHP() * 0.20));
        heal((int) (getMaxHP() * 0.30), player);
    }

    public void heal(int amount, Player player) {
        if (amount <= 0) return;
        int newHP = getCurrentHP() + amount;

        newHP = Math.min(newHP, getMaxHP());
        setCurrentHP(newHP);

        UHCUtils.spawnFloatingDamage(player, "§a+§f§l" + amount);
    }

    public void damage(double damage, Player attacker) {

        if (getAbsortion() >= damage) {
            setAbsortion((int) (getAbsortion() - damage));
        } else {
            damage -= getAbsortion();
            setAbsortion(0);
            setCurrentHP((int) Math.max(0, getCurrentHP() - damage));
        }
        UHCUtils.spawnFloatingDamage(attacker, "§4✦ §c-§f§l" + damage + " §4✦");

        
    }

    @Override
    public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
        GoldenHead goldenHead = ScenarioManager.get().getScenario(GoldenHead.class);
        if (goldenHead != null && goldenHead.isActive()) {
            if (item.isSimilar(new ItemCreator(Material.GOLDEN_APPLE)
                    .setName(ChatColor.GOLD + "Golden Head").getItemstack())) {
                onGoldenHeadUse(player);
            }
        }
        if (item.getType() == Material.GOLDEN_APPLE) {
            onGoldenAppleUse(player);
        }
    }

    @Override
    public void onSec(Player p) {
        if (p == null || !p.isOnline()) return;

        double armorBonus = 0.0;
        double enchantArmorBonus = 0.0;

        double weaponBonus = 0.0;
        double enchantWeaponBonus = 0.0;

        ItemStack[] armor = p.getInventory().getArmorContents();
        if (armor != null) {
            for (ItemStack piece : armor) {
                if (piece == null || piece.getType() == Material.AIR) continue;

                Material type = piece.getType();
                if (type == Material.LEATHER_HELMET || type == Material.LEATHER_CHESTPLATE ||
                        type == Material.LEATHER_LEGGINGS || type == Material.LEATHER_BOOTS) {
                    armorBonus += 5;
                } else if (type == Material.GOLD_HELMET || type == Material.GOLD_CHESTPLATE ||
                        type == Material.GOLD_LEGGINGS || type == Material.GOLD_BOOTS) {
                    armorBonus += 10;
                } else if (type == Material.CHAINMAIL_HELMET || type == Material.CHAINMAIL_CHESTPLATE ||
                        type == Material.CHAINMAIL_LEGGINGS || type == Material.CHAINMAIL_BOOTS) {
                    armorBonus += 12;
                } else if (type == Material.IRON_HELMET || type == Material.IRON_CHESTPLATE ||
                        type == Material.IRON_LEGGINGS || type == Material.IRON_BOOTS) {
                    armorBonus += 15;
                } else if (type == Material.DIAMOND_HELMET || type == Material.DIAMOND_CHESTPLATE ||
                        type == Material.DIAMOND_LEGGINGS || type == Material.DIAMOND_BOOTS) {
                    armorBonus += 30;
                }

                if (piece.hasItemMeta() && piece.getItemMeta().hasEnchants()) {
                    int protection = piece.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL);
                    enchantArmorBonus += protection * 5.0;
                }
            }
        }
        ItemStack weapon = p.getInventory().getItemInHand();
        if (weapon != null && weapon.getType() != Material.AIR) {
            Material type = weapon.getType();
            if (type == Material.WOOD_SWORD) {
                weaponBonus += 5;
            } else if (type == Material.STONE_SWORD) {
                weaponBonus += 10;
            } else if (type == Material.IRON_SWORD) {
                weaponBonus += 20;
            } else if (type == Material.DIAMOND_SWORD) {
                weaponBonus += 40;
            } else if (type == Material.GOLD_SWORD) {
                weaponBonus += 10;
            }

            if (weapon.hasItemMeta() && weapon.getItemMeta().hasEnchants()) {
                int sharpness = weapon.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.DAMAGE_ALL);
                int smite = weapon.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.DAMAGE_UNDEAD);
                int bane = weapon.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.DAMAGE_ARTHROPODS);

                enchantWeaponBonus += sharpness * 5.0;
                enchantWeaponBonus += smite * 3.0;
                enchantWeaponBonus += bane * 3.0;
            }
        }

        double totalResistance = getResistance() + armorBonus + enchantArmorBonus;
        double totalStrength = getStrength() + weaponBonus + enchantWeaponBonus;

        if (totalResistance > 2000.0) totalResistance = 2000.0;
        if (totalStrength > 2000.0) totalStrength = 2000.0;

        setCurrentResistance((int) totalResistance);
        setCurrentStrength((int) totalStrength);

        if (p.getActivePotionEffects().stream().noneMatch(e -> e.getType() == PotionEffectType.ABSORPTION)) {
            setAbsortion(0);
        }


    }
}
