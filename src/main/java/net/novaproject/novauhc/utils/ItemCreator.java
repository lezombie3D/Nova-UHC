package net.novaproject.novauhc.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;

import java.lang.reflect.Field;
import java.util.*;

import static org.apache.commons.codec.binary.Base64.encodeBase64;

public class ItemCreator {

    public enum BannerPreset {

        barre, precedent, suivant, coeur, cercleEtoile, croix, yinYang, losange, moin, plus;
    }

    public enum ComparatorType {
        ItemStack, Similar, Material, Amount, Durability, Name, Lores, Enchantements, ItemsFlags, Owner, BaseColor, Patterns, StoredEnchantements;
    }

    private ItemStack itemstack;
    private ArrayList<Pattern> patterns;

    public ItemCreator(Material material) {
        this.itemstack = new ItemStack(material);
    }

    public ItemCreator(String material, Player player){
        this.itemstack = new ItemStack(Material.getMaterial(material));
        if (material.equalsIgnoreCase("SKULL_ITEM")){
            this.setDurability((short) 3);
            setOwner(player.getName());
        }
    }

    public ItemCreator(ItemStack itemStack) {
        this.itemstack = itemStack;
    }

    public ItemCreator(ItemCreator itemCreator) {
        this.itemstack = itemCreator.getItemstack();
    }

    /**
     * @return l'ItemStack
     */

    public ItemStack getItemstack() {
        return new ItemStack(itemstack);
    }

    /**
     * Set un Material
     *
     * @param material material
     * @return l'objet
     */

    public ItemCreator setMaterial(Material material) {
        if (this.itemstack == null) this.itemstack = new ItemStack(material);
        else this.itemstack.setType(material);
        return this;
    }

    /**
     * @return le Material
     */

    public Material getMaterial() {
        return this.itemstack.getType();
    }


    /**
     * Set la quantiter d'item
     *
     * @param amount quantiter d'item
     * @return l'objet
     */

    public ItemCreator setAmount(int amount) {
        this.itemstack.setAmount(amount);
        return this;
    }

    public int getAmount() {
        return this.itemstack.getAmount();
    }

    /**
     * Set la durabiliter
     *
     * @param durability durabiliter
     * @return l'objet
     */

    public ItemCreator setDurability(Short durability) {
        this.itemstack.setDurability(durability);
        return this;
    }

    /**
     * @return la durabiliter
     */

    public int getDurability() {
        return this.itemstack.getDurability();
    }

    /**
     * Set le nom
     *
     * @param name nom
     * @return l'objet
     */

    public ItemCreator setName(String name) {
        ItemMeta meta = this.itemstack.getItemMeta();
        meta.setDisplayName(name);
        this.itemstack.setItemMeta(meta);
        return this;
    }

    /**
     * @return le nom
     */

    public String getName() {
        return this.itemstack.getItemMeta().getDisplayName();
    }

    /**
     * @return lores
     */

    public ArrayList<String> getLores() {
        return (ArrayList<String>) this.itemstack.getItemMeta().getLore();
    }

    /**
     * @param list set un lore
     * @return l'objet
     */

    public ItemCreator setLores(List<String> list) {
        ItemMeta meta = this.itemstack.getItemMeta();
        meta.setLore(list);
        this.itemstack.setItemMeta(meta);
        return this;
    }

    /**
     * Clear le lore
     *
     * @return l'objet
     */

    public ItemCreator clearLores() {
        ItemMeta meta = this.itemstack.getItemMeta();
        meta.setLore(new ArrayList<String>());
        this.itemstack.setItemMeta(meta);
        return this;
    }

    /**
     * @param lore a add
     * @return l'objet
     */

    public ItemCreator addLore(String lore) {
        ItemMeta meta = this.itemstack.getItemMeta();
        ArrayList<String> lores = (ArrayList<String>) meta.getLore();
        if (lores == null) {
            lores = new ArrayList<>();
        }
        if (lore != null) {
            lores.add(lore);
        } else {
            lores.add(" ");
        }
        meta.setLore(lores);
        this.itemstack.setItemMeta(meta);
        return this;
    }


    /**
     * @return les enchantements
     */

    public HashMap<Enchantment, Integer> getEnchantments() {
        return new HashMap<>(this.itemstack.getItemMeta().getEnchants());
    }

    /**
     * @param map enchantements
     * @return l'objet
     */

    public ItemCreator setEnchantments(Map<Enchantment, Integer> map) {
        ItemMeta meta = this.itemstack.getItemMeta();
        if (meta.getEnchants() != null) {
            ArrayList<Enchantment> cloneenchantments = new ArrayList<>(meta.getEnchants().keySet());
            for (Enchantment enchantment : cloneenchantments) {
                meta.removeEnchant(enchantment);
            }
        }
        for (Map.Entry<Enchantment, Integer> e : map.entrySet()) {
            meta.addEnchant(e.getKey(), e.getValue(), true);
        }
        this.itemstack.setItemMeta(meta);
        return this;
    }

    /**
     * Clear l'enchantement
     *
     * @return l'objet
     */

    public ItemCreator clearEnchantments() {
        ItemMeta meta = this.itemstack.getItemMeta();
        if (meta.getEnchants() != null) {
            ArrayList<Enchantment> cloneenchantments = new ArrayList<>(meta.getEnchants().keySet());
            for (Enchantment enchantment : cloneenchantments) {
                meta.removeEnchant(enchantment);
            }
            this.itemstack.setItemMeta(meta);
        }
        return this;
    }

    /**
     * add un enchant
     *
     * @param enchantment enchant
     * @param lvl         niveau d'enchant
     * @return l'objet
     */

    public ItemCreator addEnchantment(Enchantment enchantment, int lvl) {
        ItemMeta meta = this.itemstack.getItemMeta();
        meta.addEnchant(enchantment, lvl, true);
        this.itemstack.setItemMeta(meta);
        return this;
    }

    /**
     * enlever un enchant
     *
     * @param enchantment enchant
     * @return
     */

    public ItemCreator removeEnchantment(Enchantment enchantment) {
        ItemMeta meta = this.itemstack.getItemMeta();
        if (meta.getEnchants() != null) {
            if (meta.getEnchants().containsKey(enchantment)) {
                meta.removeEnchant(enchantment);
                this.itemstack.setItemMeta(meta);
            }
        }
        return this;
    }

    /**
     * @return EnchantmentStorageMeta
     */


    public EnchantmentStorageMeta getEnchantmentStorageMeta() {
        if (this.itemstack.getType().equals(Material.ENCHANTED_BOOK)) {
            return (EnchantmentStorageMeta) this.itemstack.getItemMeta();
        }
        return null;
    }

    /**
     * set le EnchantmentStorageMeta
     *
     * @param enchantmentstoragemeta EnchantmentStorageMeta
     * @return l'objet
     */

    public ItemCreator setEnchantmentStorageMeta(EnchantmentStorageMeta enchantmentstoragemeta) {
        if (this.itemstack.getType().equals(Material.ENCHANTED_BOOK)) {
            this.itemstack.setItemMeta(enchantmentstoragemeta);
        }
        return this;
    }

    /**
     * @return EnchantmentStorageMeta
     */

    public HashMap<Enchantment, Integer> getStoredEnchantments() {
        if (this.itemstack.getType().equals(Material.ENCHANTED_BOOK)) {
            return (HashMap<Enchantment, Integer>) ((EnchantmentStorageMeta) this.itemstack.getItemMeta()).getEnchants();
        }
        return null;
    }

    public ItemCreator setStoredEnchantments(HashMap<Enchantment, Integer> storedenchantments) {
        if (this.itemstack.getType().equals(Material.ENCHANTED_BOOK)) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) this.itemstack.getItemMeta();
            if (meta.getStoredEnchants() != null) {
                ArrayList<Enchantment> clonestoredenchantments = new ArrayList<>(meta.getStoredEnchants().keySet());
                for (Enchantment storedenchantment : clonestoredenchantments) {
                    meta.removeStoredEnchant(storedenchantment);
                }
            }
            for (Map.Entry<Enchantment, Integer> e : storedenchantments.entrySet()) {
                meta.addEnchant(e.getKey(), e.getValue(), true);
            }
            this.itemstack.setItemMeta(meta);
        }
        return this;
    }

    public ItemCreator clearStoredEnchantments() {
        if (this.itemstack.getType().equals(Material.ENCHANTED_BOOK)) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) this.itemstack.getItemMeta();
            if (meta.getStoredEnchants() != null) {
                ArrayList<Enchantment> clonestoredenchantments = new ArrayList<>(meta.getStoredEnchants().keySet());
                for (Enchantment storedenchantment : clonestoredenchantments) {
                    meta.removeStoredEnchant(storedenchantment);
                }
                this.itemstack.setItemMeta(meta);
            }
        }
        return this;
    }

    public ItemCreator addStoredEnchantment(Enchantment storedenchantment, int lvl) {
        if (this.itemstack.getType().equals(Material.ENCHANTED_BOOK)) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) this.itemstack.getItemMeta();
            meta.addStoredEnchant(storedenchantment, lvl, true);
            this.itemstack.setItemMeta(meta);
        }
        return this;
    }

    public ItemCreator removeStoredEnchantment(Enchantment enchantment) {
        if (this.itemstack.getType().equals(Material.ENCHANTED_BOOK)) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) this.itemstack.getItemMeta();
            if (meta.getStoredEnchants() != null) {
                if (meta.getStoredEnchants().containsKey(enchantment)) {
                    meta.removeEnchant(enchantment);
                    this.itemstack.setItemMeta(meta);
                }
            }
        }
        return this;
    }


    /**
     * @return la normal des item flags
     */

    public ArrayList<ItemFlag> getItemFlags() {
        ArrayList<ItemFlag> itemflags = new ArrayList<>();
        if (this.itemstack.getItemMeta().getItemFlags() != null) {
            itemflags.addAll(this.itemstack.getItemMeta().getItemFlags());
        }
        return itemflags;
    }

    /**
     * set l'itemflags
     *
     * @param itemflags normal d'itemflags
     * @return l'objet
     */

    public ItemCreator setItemFlags(ArrayList<ItemFlag> itemflags) {
        ItemMeta meta = this.itemstack.getItemMeta();
        if (meta.getItemFlags() != null) {
            ArrayList<ItemFlag> cloneitemflags = new ArrayList<>(meta.getItemFlags());
            for (ItemFlag itemflag : cloneitemflags) {
                meta.removeItemFlags(itemflag);
            }
        }
        for (ItemFlag itemflag : itemflags) {
            meta.addItemFlags(itemflag);
        }
        this.itemstack.setItemMeta(meta);
        return this;
    }

    /**
     * Clear les items flags
     *
     * @return l'objet
     */

    public ItemCreator clearItemFlags() {
        ItemMeta meta = this.itemstack.getItemMeta();
        if (meta.getItemFlags() != null) {
            ArrayList<ItemFlag> cloneitemflags = new ArrayList<>(meta.getItemFlags());
            for (ItemFlag itemflag : cloneitemflags) {
                meta.removeItemFlags(itemflag);
            }
            this.itemstack.setItemMeta(meta);
        }
        return this;
    }

    /**
     * add un item flag
     *
     * @param itemflag item flag
     * @return l'objet
     */

    public ItemCreator addItemFlags(ItemFlag itemflag) {
        ItemMeta meta = this.itemstack.getItemMeta();
        meta.addItemFlags(itemflag);
        this.itemstack.setItemMeta(meta);
        return this;
    }

    /**
     * Enlever un item flag
     *
     * @param itemflag item flag
     * @return l'objet
     */

    public ItemCreator removeItemFlags(ItemFlag itemflag) {
        ItemMeta meta = this.itemstack.getItemMeta();
        if (meta.getItemFlags() != null) {
            if (meta.getItemFlags().contains(itemflag)) {
                meta.removeItemFlags(itemflag);
                this.itemstack.setItemMeta(meta);
            }
        }
        return this;
    }

    /**
     * Ajouter tout les items flags
     *
     * @return l'objet
     */

    public ItemCreator addallItemsflags() {
        ItemMeta meta = this.itemstack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        this.itemstack.setItemMeta(meta);
        return this;
    }


    /**
     * @return la meta de la tete
     */

    public SkullMeta getSkullMeta() {
        if (this.itemstack.getType().equals(Material.SKULL_ITEM)) {
            return (SkullMeta) this.itemstack.getItemMeta();
        }
        return null;
    }

    /**
     * set la meta de la tete
     *
     * @param skullmeta skull meta
     * @return l'objet
     */

    public ItemCreator setSkullMeta(SkullMeta skullmeta) {
        if (this.itemstack.getType().equals(Material.SKULL_ITEM)) {
            this.itemstack.setItemMeta(skullmeta);
        }
        return this;
    }

    /**
     * @return l'owner
     */

    public String getOwner() {
        if (this.itemstack.getType().equals(Material.SKULL_ITEM)) {
            return ((SkullMeta) this.itemstack.getItemMeta()).getOwner();
        }
        return null;
    }

    /**
     * set l'owner
     *
     * @param owner pseudo de l'owner pour changer le skin de la tete
     * @return l'objet
     */

    public ItemCreator setOwner(String owner) {
        if (this.itemstack.getType().equals(Material.SKULL_ITEM)) {
            SkullMeta meta = (SkullMeta) this.itemstack.getItemMeta();
            meta.setOwner(owner);
            this.itemstack.setItemMeta(meta);
        }
        return this;
    }

    /**
     * @return la meta de la banniere
     */
    public BannerMeta getBannerMeta() {
        if (this.itemstack.getType().equals(Material.BANNER)) {
            return (BannerMeta) this.itemstack.getItemMeta();
        }
        return null;
    }

    /**
     * Set la meta de la baniere
     *
     * @param bannermeta banner meta
     * @return l'objet
     */

    public ItemCreator setBannerMeta(BannerMeta bannermeta) {
        if (this.itemstack.getType().equals(Material.BANNER)) {
            this.itemstack.setItemMeta(bannermeta);
        }
        return this;
    }

    /**
     * @return la couleur de base du drapeau
     */

    public DyeColor getBasecolor() {
        if (this.itemstack.getType().equals(Material.BANNER)) {
            return ((BannerMeta) this.itemstack.getItemMeta()).getBaseColor();
        }
        return null;
    }

    /**
     * set la couleur de base du drapeau
     *
     * @param basecolor couleur
     * @return l'objet
     */

    public ItemCreator setBasecolor(DyeColor basecolor) {
        if (this.itemstack.getType().equals(Material.BANNER)) {
            BannerMeta meta = (BannerMeta) this.itemstack.getItemMeta();
            meta.setPatterns(Collections.singletonList(new Pattern(basecolor, PatternType.BASE)));
            this.itemstack.setItemMeta(meta);
        }
        return this;
    }

    public ArrayList<Pattern> getPatterns() {
        if (this.itemstack.getType().equals(Material.BANNER)) {
            return (ArrayList<Pattern>) ((BannerMeta) this.itemstack.getItemMeta()).getPatterns();
        }
        return null;
    }

    public ItemCreator setPatterns(ArrayList<Pattern> petterns) {
        if (this.itemstack.getType().equals(Material.BANNER)) {
            BannerMeta meta = (BannerMeta) this.itemstack.getItemMeta();
            meta.setPatterns(petterns);
            this.itemstack.setItemMeta(meta);
        }
        return this;
    }

    public ItemCreator clearPatterns() {
        if (this.itemstack.getType().equals(Material.BANNER)) {
            BannerMeta meta = (BannerMeta) this.itemstack.getItemMeta();
            meta.setPatterns(new ArrayList<>());
            this.itemstack.setItemMeta(meta);
        }
        return this;
    }

    public ItemCreator addPattern(Pattern pattern) {
        if (this.itemstack.getType().equals(Material.BANNER)) {
            BannerMeta meta = (BannerMeta) this.itemstack.getItemMeta();
            meta.addPattern(pattern);
            this.itemstack.setItemMeta(meta);
        }
        return this;
    }

    public ItemCreator removePattern(Pattern pattern) {
        if (this.itemstack.getType().equals(Material.BANNER)) {
            BannerMeta meta = (BannerMeta) this.itemstack.getItemMeta();
            ArrayList<Pattern> patterns = (ArrayList<Pattern>) meta.getPatterns();
            if (patterns != null) {
                if (patterns.contains(pattern)) {
                    patterns.remove(pattern);
                    meta.setPatterns(patterns);
                    this.itemstack.setItemMeta(meta);
                }
            }
        }
        return this;
    }


    public ItemCreator addBannerPreset(Integer ID, DyeColor patterncolor) {
        switch (ID) {
            case 0:
                break;
            case 1:
                addBannerPreset(BannerPreset.barre, patterncolor);
                break;
            case 2:
                addBannerPreset(BannerPreset.precedent, patterncolor);
                break;
            case 3:
                addBannerPreset(BannerPreset.suivant, patterncolor);
                break;
            case 4:
                addBannerPreset(BannerPreset.coeur, patterncolor);
                break;
            case 5:
                addBannerPreset(BannerPreset.cercleEtoile, patterncolor);
                break;
            case 6:
                addBannerPreset(BannerPreset.croix, patterncolor);
                break;
            case 7:
                addBannerPreset(BannerPreset.yinYang, patterncolor);
                break;
            case 8:
                addBannerPreset(BannerPreset.losange, patterncolor);
                break;
            case 9:
                addBannerPreset(BannerPreset.moin, patterncolor);
                break;
            case 10:
                addBannerPreset(BannerPreset.plus, patterncolor);
                break;
            default:
                break;
        }
        return this;
    }

    public ItemCreator addBannerPreset(BannerPreset type, DyeColor patterncolor) {
        if (type == null)
            return this;
        if (this.itemstack.getType().equals(Material.BANNER)) {
            BannerMeta meta = (BannerMeta) this.itemstack.getItemMeta();
            DyeColor basecolor = meta.getBaseColor();
            switch (type) {
                // rien, barre, precedent, suivant, Coeur, cercleEtoile, croix,
                // yinYang, Losange, Moin, Plus;
                case barre:
                    addasyncronePattern(new Pattern(patterncolor, PatternType.STRIPE_DOWNRIGHT), true);
                    break;
                case precedent:
                    // precedent
                    addasyncronePattern(new Pattern(patterncolor, PatternType.RHOMBUS_MIDDLE), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.SQUARE_BOTTOM_RIGHT), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.SQUARE_TOP_RIGHT), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.STRIPE_RIGHT), true);
                    break;
                case suivant:
                    // suivant
                    addasyncronePattern(new Pattern(patterncolor, PatternType.RHOMBUS_MIDDLE), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.SQUARE_BOTTOM_LEFT), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.SQUARE_TOP_LEFT), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.STRIPE_LEFT), true);
                    break;
                case coeur:
                    // Coeur
                    addasyncronePattern(new Pattern(patterncolor, PatternType.RHOMBUS_MIDDLE), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.TRIANGLE_TOP), true);
                    break;
                case cercleEtoile:
                    // cercleEtoile
                    addasyncronePattern(new Pattern(patterncolor, PatternType.RHOMBUS_MIDDLE), false);
                    addasyncronePattern(new Pattern(patterncolor, PatternType.FLOWER), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.CIRCLE_MIDDLE), true);
                    break;
                case croix:
                    // croix
                    addasyncronePattern(new Pattern(patterncolor, PatternType.CROSS), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.CURLY_BORDER), true);
                    break;
                case yinYang:
                    // yinYang
                    addasyncronePattern(new Pattern(patterncolor, PatternType.SQUARE_BOTTOM_RIGHT), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.STRIPE_RIGHT), false);
                    addasyncronePattern(new Pattern(patterncolor, PatternType.DIAGONAL_LEFT), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.SQUARE_TOP_LEFT), false);
                    addasyncronePattern(new Pattern(patterncolor, PatternType.TRIANGLE_TOP), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.STRIPE_RIGHT), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.TRIANGLE_BOTTOM), false);
                    addasyncronePattern(new Pattern(patterncolor, PatternType.STRIPE_LEFT), true);
                    break;
                case losange:
                    // Losange
                    addasyncronePattern(new Pattern(patterncolor, PatternType.RHOMBUS_MIDDLE), true);
                    break;
                case moin:
                    // Moin
                    addasyncronePattern(new Pattern(patterncolor, PatternType.STRIPE_MIDDLE), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.BORDER), true);
                    break;
                case plus:
                    // Plus
                    addasyncronePattern(new Pattern(patterncolor, PatternType.STRAIGHT_CROSS), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.STRIPE_TOP), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.STRIPE_BOTTOM), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.BORDER), true);
                    break;
                default:
                    break;
            }
        }
        return this;
    }

    private void addasyncronePattern(Pattern pattern, Boolean calcul) {
        if (calcul) {
            patterns.add(pattern);
            BannerMeta meta = (BannerMeta) this.itemstack.getItemMeta();
            for (Pattern currentpattern : patterns) {
                meta.addPattern(currentpattern);
            }
            patterns.clear();
            this.itemstack.setItemMeta(meta);

        } else {
            if (patterns == null) {
                patterns = new ArrayList<>();
            }
            patterns.add(pattern);
        }
    }

//    COMPARATE

    public Boolean comparate(ItemCreator item, ComparatorType type) {
        switch (type) {
            case Similar:
                return comparate(item, ComparatorType.Material) && comparate(item, ComparatorType.Durability)
                        && comparate(item, ComparatorType.Name) && comparate(item, ComparatorType.Lores)
                        && comparate(item, ComparatorType.Enchantements) && comparate(item, ComparatorType.ItemsFlags)
                        && comparate(item, ComparatorType.Owner) && comparate(item, ComparatorType.BaseColor)
                        && comparate(item, ComparatorType.Patterns) && comparate(item, ComparatorType.StoredEnchantements);
            case ItemStack:
                return comparate(item, ComparatorType.Material) && comparate(item, ComparatorType.Amount)
                        && comparate(item, ComparatorType.Durability) && comparate(item, ComparatorType.Name)
                        && comparate(item, ComparatorType.Lores) && comparate(item, ComparatorType.Enchantements)
                        && comparate(item, ComparatorType.ItemsFlags) && comparate(item, ComparatorType.Owner)
                        && comparate(item, ComparatorType.BaseColor) && comparate(item, ComparatorType.Patterns)
                        && comparate(item, ComparatorType.StoredEnchantements);
            case Material:
                return getMaterial() == item.getMaterial();
            case Amount:
                return getAmount() == item.getAmount();
            case Durability:
                return getDurability() == item.getDurability();
            case Name:
                return getName() == item.getName();
            case Lores:
                return new comparaison<String, Object>().islistequal(getLores(), item.getLores());
            case Enchantements:
                return new comparaison<Enchantment, Integer>().ismapequal(getEnchantments(), item.getEnchantments());
            case ItemsFlags:
                return new comparaison<ItemFlag, Object>().islistequal(getItemFlags(), item.getItemFlags());
            case Owner:
                return getOwner() == item.getOwner();
            case BaseColor:
                return getBasecolor() == item.getBasecolor();
            case Patterns:
                return new comparaison<Pattern, Object>().islistequal(getPatterns(), item.getPatterns());
            case StoredEnchantements:
                return new comparaison<Enchantment, Integer>().ismapequal(getStoredEnchantments(),
                        item.getStoredEnchantments());
            default:
                return false;
        }
    }

    private class comparaison<type1, type2> {
        public comparaison() {

        }

        public Boolean islistequal(List<type1> list1, List<type1> list2) {
            if (list1 == null && list2 == null) {
                return true;
            } else if (list1 == null || list2 == null) {
                return false;
            } else if (list1.size() == list2.size()) {
                for (Integer i = 0; i < list1.size() && i < list2.size(); i++) {
                    if (list1.get(i) != list2.get(i)) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        }

        public Boolean ismapequal(Map<type1, type2> map1, Map<type1, type2> map2) {
            if (map1 == null && map2 == null) {
                return true;
            } else if (map1 == null || map2 == null) {
                return false;
            } else if (map1.size() == map2.size()) {
                for (Map.Entry<type1, type2> e : map1.entrySet()) {
                    if (map2.get(e.getKey()) == null) {
                        return false;
                    }
                    if (map2.get(e.getKey()) != e.getValue()) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        }
    }

    public ItemCreator setUnbreakable(Boolean unbreakable) {
        ItemMeta meta = this.itemstack.getItemMeta();
        meta.spigot().setUnbreakable(unbreakable);
        this.itemstack.setItemMeta(meta);
        return this;
    }

    public ItemCreator addSkull(String url) {
        setMaterial(Material.SKULL_ITEM);
        setDurability((short) 3);
        if (url.isEmpty())
            return this;

        url = "https://textures.minecraft.net/texture/" + url;

        SkullMeta headMeta = (SkullMeta) this.itemstack.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "Sithey");
        byte[] encodedData = encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", new Object[]{url}).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField;
        try {
            profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
            e1.printStackTrace();
        }
        this.itemstack.setItemMeta(headMeta);
        return this;
    }

    public ItemCreator addSkullHash(String hash) {
        setMaterial(Material.SKULL_ITEM);
        setDurability((short) 3);
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        PropertyMap propertyMap = profile.getProperties();
        propertyMap.put("textures", new Property("textures", hash));
        SkullMeta skullMeta = (SkullMeta) this.itemstack.getItemMeta();
        Class<?> c_skullMeta = skullMeta.getClass();
        try {
            Field f_profile = c_skullMeta.getDeclaredField("profile");
            f_profile.setAccessible(true);
            f_profile.set(skullMeta, profile);
            f_profile.setAccessible(false);
            this.itemstack.setItemMeta(skullMeta);
            return this;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return this;
    }

    public ItemCreator addPotionEffect(PotionEffect effect, boolean splash){
        setMaterial(Material.POTION);
        PotionMeta meta = (PotionMeta) itemstack.getItemMeta();
        meta.setMainEffect(effect.getType());
        meta.addCustomEffect(effect, true);
        itemstack.setItemMeta(meta);
        Potion potion = new Potion(1);
        potion.setSplash(splash);
        potion.apply(itemstack);
        return this;
    }

    public ItemCreator setLeatherColor(Color color) {
        LeatherArmorMeta meta = (LeatherArmorMeta) this.itemstack.getItemMeta();
        meta.setColor(color);
        this.itemstack.setItemMeta(meta);
        return this;
    }

}
