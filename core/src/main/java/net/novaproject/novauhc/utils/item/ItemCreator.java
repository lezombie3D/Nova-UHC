package net.novaproject.novauhc.utils.item;

import java.util.UUID;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Objects;
import java.util.logging.Level;
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
import org.bukkit.Bukkit;
import java.util.concurrent.ThreadLocalRandom;

public class ItemCreator {

    public ItemCreator setGlow(boolean b) {
        if (!b) {
            this.itemstack.removeEnchantment(Enchantment.DURABILITY);
            return this;
        }
        return this.addEnchantment(Enchantment.DURABILITY, 1).addItemFlags(ItemFlag.HIDE_ENCHANTS);
    }

    public HashMap<Enchantment, Integer> getStoredEnchantments() {
        if (this.itemstack.getType().equals(Material.ENCHANTED_BOOK)) {
            return (HashMap<Enchantment, Integer>) this.itemstack.getItemMeta().getEnchants();
        }
        return null;
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

    public ItemStack getItemstack() {
        return new ItemStack(itemstack);
    }

    public ItemCreator setMaterial(Material material) {
        if (this.itemstack == null) this.itemstack = new ItemStack(material);
        else this.itemstack.setType(material);
        return this;
    }

    public enum BannerPreset {

        barre, precedent, suivant, coeur, cercleEtoile, croix, yinYang, losange, moin, plus
    }

    public Material getMaterial() {
        return this.itemstack.getType();
    }

    public ItemCreator setAmount(int amount) {
        if(amount > 64) amount = 64;
        this.itemstack.setAmount(amount);
        return this;
    }

    public int getAmount() {
        return this.itemstack.getAmount();
    }

    public ItemCreator setDurability(Short durability) {
        this.itemstack.setDurability(durability);
        return this;
    }

    public int getDurability() {
        return this.itemstack.getDurability();
    }

    public ItemCreator setName(String name) {
        ItemMeta meta = this.itemstack.getItemMeta();
        meta.setDisplayName(name);
        this.itemstack.setItemMeta(meta);
        return this;
    }

    public String getName() {
        return this.itemstack.getItemMeta().getDisplayName();
    }

    public ArrayList<String> getLores() {
        return (ArrayList<String>) this.itemstack.getItemMeta().getLore();
    }

    public ItemCreator setLores(List<String> list) {
        ItemMeta meta = this.itemstack.getItemMeta();
        meta.setLore(list);
        this.itemstack.setItemMeta(meta);
        return this;
    }

    public ItemCreator clearLores() {
        ItemMeta meta = this.itemstack.getItemMeta();
        meta.setLore(new ArrayList<String>());
        this.itemstack.setItemMeta(meta);
        return this;
    }

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

    public HashMap<Enchantment, Integer> getEnchantments() {
        return new HashMap<>(this.itemstack.getItemMeta().getEnchants());
    }

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

    public ItemCreator addEnchantment(Enchantment enchantment, int lvl) {
        ItemMeta meta = this.itemstack.getItemMeta();
        meta.addEnchant(enchantment, lvl, true);
        this.itemstack.setItemMeta(meta);
        return this;
    }

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

    public EnchantmentStorageMeta getEnchantmentStorageMeta() {
        if (this.itemstack.getType().equals(Material.ENCHANTED_BOOK)) {
            return (EnchantmentStorageMeta) this.itemstack.getItemMeta();
        }
        return null;
    }

    public ItemCreator setEnchantmentStorageMeta(EnchantmentStorageMeta enchantmentstoragemeta) {
        if (this.itemstack.getType().equals(Material.ENCHANTED_BOOK)) {
            this.itemstack.setItemMeta(enchantmentstoragemeta);
        }
        return this;
    }

    public enum ComparatorType {
        ItemStack, Similar, Material, Amount, Durability, Name, Lores, Enchantements, ItemsFlags, Owner, BaseColor, Patterns, StoredEnchantements
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

    public ArrayList<ItemFlag> getItemFlags() {
        ArrayList<ItemFlag> itemflags = new ArrayList<>();
        if (this.itemstack.getItemMeta().getItemFlags() != null) {
            itemflags.addAll(this.itemstack.getItemMeta().getItemFlags());
        }
        return itemflags;
    }

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

    public ItemCreator addItemFlags(ItemFlag itemflag) {
        ItemMeta meta = this.itemstack.getItemMeta();
        meta.addItemFlags(itemflag);
        this.itemstack.setItemMeta(meta);
        return this;
    }

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

    public ItemCreator addallItemsflags() {
        ItemMeta meta = this.itemstack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        this.itemstack.setItemMeta(meta);
        return this;
    }

    public SkullMeta getSkullMeta() {
        if (this.itemstack.getType().equals(Material.SKULL_ITEM)) {
            return (SkullMeta) this.itemstack.getItemMeta();
        }
        return null;
    }

    public ItemCreator setSkullMeta(SkullMeta skullmeta) {
        if (this.itemstack.getType().equals(Material.SKULL_ITEM)) {
            this.itemstack.setItemMeta(skullmeta);
        }
        return this;
    }

    public String getOwner() {
        if (this.itemstack.getType().equals(Material.SKULL_ITEM)) {
            return ((SkullMeta) this.itemstack.getItemMeta()).getOwner();
        }
        return null;
    }

    public ItemCreator setOwner(String owner) {
        if (this.itemstack.getType().equals(Material.SKULL_ITEM)) {
            SkullMeta meta = (SkullMeta) this.itemstack.getItemMeta();
            meta.setOwner(owner);
            this.itemstack.setItemMeta(meta);
        }
        return this;
    }

    public BannerMeta getBannerMeta() {
        if (this.itemstack.getType().equals(Material.BANNER)) {
            return (BannerMeta) this.itemstack.getItemMeta();
        }
        return null;
    }

    public ItemCreator setBannerMeta(BannerMeta bannermeta) {
        if (this.itemstack.getType().equals(Material.BANNER)) {
            this.itemstack.setItemMeta(bannermeta);
        }
        return this;
    }

    public DyeColor getBasecolor() {
        if (this.itemstack.getType().equals(Material.BANNER)) {
            return ((BannerMeta) this.itemstack.getItemMeta()).getBaseColor();
        }
        return null;
    }

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

                case barre:
                    addasyncronePattern(new Pattern(patterncolor, PatternType.STRIPE_DOWNRIGHT), true);
                    break;
                case precedent:

                    addasyncronePattern(new Pattern(patterncolor, PatternType.RHOMBUS_MIDDLE), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.SQUARE_BOTTOM_RIGHT), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.SQUARE_TOP_RIGHT), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.STRIPE_RIGHT), true);
                    break;
                case suivant:

                    addasyncronePattern(new Pattern(patterncolor, PatternType.RHOMBUS_MIDDLE), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.SQUARE_BOTTOM_LEFT), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.SQUARE_TOP_LEFT), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.STRIPE_LEFT), true);
                    break;
                case coeur:

                    addasyncronePattern(new Pattern(patterncolor, PatternType.RHOMBUS_MIDDLE), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.TRIANGLE_TOP), true);
                    break;
                case cercleEtoile:

                    addasyncronePattern(new Pattern(patterncolor, PatternType.RHOMBUS_MIDDLE), false);
                    addasyncronePattern(new Pattern(patterncolor, PatternType.FLOWER), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.CIRCLE_MIDDLE), true);
                    break;
                case croix:

                    addasyncronePattern(new Pattern(patterncolor, PatternType.CROSS), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.CURLY_BORDER), true);
                    break;
                case yinYang:

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

                    addasyncronePattern(new Pattern(patterncolor, PatternType.RHOMBUS_MIDDLE), true);
                    break;
                case moin:

                    addasyncronePattern(new Pattern(patterncolor, PatternType.STRIPE_MIDDLE), false);
                    addasyncronePattern(new Pattern(basecolor, PatternType.BORDER), true);
                    break;
                case plus:

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
                return Objects.equals(getName(), item.getName());
            case Lores:
                return new comparaison<String, Object>().islistequal(getLores(), item.getLores());
            case Enchantements:
                return new comparaison<Enchantment, Integer>().ismapequal(getEnchantments(), item.getEnchantments());
            case ItemsFlags:
                return new comparaison<ItemFlag, Object>().islistequal(getItemFlags(), item.getItemFlags());
            case Owner:
                return Objects.equals(getOwner(), item.getOwner());
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
                    if (!Objects.equals(list1.get(i), list2.get(i))) {
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
                    if (!Objects.equals(map2.get(e.getKey()), e.getValue())) {
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
            Bukkit.getLogger().log(Level.SEVERE, "Erreur inattendue", e1);
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
            Bukkit.getLogger().log(Level.SEVERE, "Erreur inattendue", e);
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


    public static final String ABILITY_NAME_FORMAT = "§8» §f§l%name% §8«";

    private static final String LEGACY_ABILITY_NAME_FORMAT = "§8» §f§l%name% §8«";

    private static final String[] KNOWN_ABILITY_NAME_FORMATS = {
            ABILITY_NAME_FORMAT, LEGACY_ABILITY_NAME_FORMAT };

    public static String abilityItemName(String name) {
        return ABILITY_NAME_FORMAT.replace("%name%", name == null ? "" : name);
    }

    public static boolean isAbilityItem(ItemStack item) {
        if (AbilityNbt.isAbilityItem(item)) return true;
        if (item == null || !item.hasItemMeta()) return false;
        return matchesAbilityNameFormat(item.getItemMeta().getDisplayName());
    }

    public static boolean matchesAbilityNameFormat(String name) {
        if (name == null) return false;
        for (String format : KNOWN_ABILITY_NAME_FORMATS) {
            int placeholder = format.indexOf("%name%");
            if (placeholder < 0) continue;
            String prefix = format.substring(0, placeholder);
            String suffix = format.substring(placeholder + "%name%".length());
            if (name.length() > prefix.length() + suffix.length()
                    && name.startsWith(prefix) && name.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    public static void consumeOne(Player player, ItemStack item) {
        if (item == null) return;
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().removeItem(item);
        }
    }

    public static String getTextItem(ItemStack item) {
        return item.getAmount() + "x" + item.getType().name();
    }

    public static boolean isDiamondArmor(ItemStack item) {
        if (item == null) return false;
        Material type = item.getType();
        return type == Material.DIAMOND_HELMET
                || type == Material.DIAMOND_CHESTPLATE
                || type == Material.DIAMOND_LEGGINGS
                || type == Material.DIAMOND_BOOTS;
    }

    public static boolean isDamageable(Material type) {
        if (type == null) return false;
        String[] split = type.toString().split("_");
        switch (split[split.length - 1]) {
            case "HELMET":
            case "CHESTPLATE":
            case "LEGGINGS":
            case "BOOTS":
            case "SWORD":
            case "AXE":
            case "PICKAXE":
            case "SHOVEL":
            case "BOW":
            case "SPADE":
            case "HOE":
            case "ELYTRA":
            case "TURTLE_HELMET":
            case "TRIDENT":
            case "HORSE_ARMOR":
            case "SHEARS":
                return true;
            default:
                return false;
        }
    }

    public static boolean changeEnchantmentItem(Player player, Enchantment enchantment, int actualLevel, int newLevel) {
        boolean hasItemChanged = false;
        for (int i = 0; i < player.getInventory().getSize(); ++i) {
            try {
                ItemStack item = player.getInventory().getItem(i);
                if (item != null && item.getType() != null && item.containsEnchantment(enchantment)) {
                    int level = item.getEnchantmentLevel(enchantment);
                    if (level == actualLevel) {
                        hasItemChanged = true;
                        item.addUnsafeEnchantment(enchantment, newLevel);
                    }
                }
            } catch (Exception e) {
                Bukkit.getLogger().info("(ERREUR) ItemCreator.changeEnchantmentItem");
                Bukkit.getLogger().log(Level.SEVERE, "Erreur inattendue", e);
            }
        }
        if (hasItemChanged) {
            player.updateInventory();
        }
        return hasItemChanged;
    }

    public static boolean changeEnchantmentItemWithInfos(Player player, Material material, boolean haveLore, Enchantment enchantment, int actualLevel, int newLevel) {
        boolean hasItemChanged = false;
        for (int i = 0; i < player.getInventory().getSize(); ++i) {
            try {
                ItemStack item = player.getInventory().getItem(i);
                if (item != null && item.getType() != null && item.getType() == material
                        && (!haveLore || item.hasItemMeta() && item.getItemMeta().hasLore())
                        && item.containsEnchantment(enchantment)) {
                    int level = item.getEnchantmentLevel(enchantment);
                    if (level == actualLevel) {
                        hasItemChanged = true;
                        item.addUnsafeEnchantment(enchantment, newLevel);
                    }
                }
            } catch (Exception e) {
                Bukkit.getLogger().info("(ERREUR) ItemCreator.changeEnchantmentItemWithInfos");
                Bukkit.getLogger().log(Level.SEVERE, "Erreur inattendue", e);
            }
        }
        if (hasItemChanged) {
            player.updateInventory();
        }
        return hasItemChanged;
    }

    public static ItemStack getTier1Book() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        int random = ThreadLocalRandom.current().nextInt(9) + 1;
        switch (random) {
            case 1:
                meta.addStoredEnchant(Enchantment.ARROW_INFINITE, 1, true);
                break;
            case 2:
                meta.addStoredEnchant(Enchantment.KNOCKBACK, 1, true);
                break;
            case 3:
                meta.addStoredEnchant(Enchantment.THORNS, 1, true);
                break;
            case 4:
                meta.addStoredEnchant(Enchantment.PROTECTION_PROJECTILE, 1, true);
                break;
            case 5:
                meta.addStoredEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
                break;
            case 6:
                meta.addStoredEnchant(Enchantment.DAMAGE_ALL, 1, true);
            case 7:
            default:
                break;
            case 8:
                meta.addStoredEnchant(Enchantment.ARROW_DAMAGE, 1, true);
                break;
            case 9:
                meta.addStoredEnchant(Enchantment.ARROW_DAMAGE, 1, true);
        }
        book.setItemMeta(meta);
        return book;
    }

    public static ItemStack getRandomEnchantedBook() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        int random = ThreadLocalRandom.current().nextInt(19) + 1;
        switch (random) {
            case 1:
                meta.addStoredEnchant(Enchantment.ARROW_INFINITE, 1, true);
                break;
            case 2:
                meta.addStoredEnchant(Enchantment.ARROW_FIRE, 1, true);
                break;
            case 3:
                meta.addStoredEnchant(Enchantment.FIRE_ASPECT, 1, true);
                break;
            case 4:
                meta.addStoredEnchant(Enchantment.FIRE_ASPECT, 2, true);
                break;
            case 5:
                meta.addStoredEnchant(Enchantment.KNOCKBACK, 1, true);
                break;
            case 6:
                meta.addStoredEnchant(Enchantment.KNOCKBACK, 2, true);
                break;
            case 7:
                meta.addStoredEnchant(Enchantment.THORNS, 1, true);
                break;
            case 8:
                meta.addStoredEnchant(Enchantment.THORNS, 2, true);
                break;
            case 9:
                meta.addStoredEnchant(Enchantment.THORNS, 3, true);
                break;
            case 10:
                meta.addStoredEnchant(Enchantment.PROTECTION_PROJECTILE, 1, true);
                break;
            case 11:
                meta.addStoredEnchant(Enchantment.PROTECTION_PROJECTILE, 2, true);
                break;
            case 12:
                meta.addStoredEnchant(Enchantment.PROTECTION_PROJECTILE, 3, true);
                break;
            case 13:
                meta.addStoredEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
                break;
            case 14:
                meta.addStoredEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, true);
                break;
            case 15:
                meta.addStoredEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3, true);
                break;
            case 16:
                meta.addStoredEnchant(Enchantment.DAMAGE_ALL, 1, true);
                break;
            case 17:
                meta.addStoredEnchant(Enchantment.DAMAGE_ALL, 2, true);
                break;
            case 18:
                meta.addStoredEnchant(Enchantment.DAMAGE_ALL, 3, true);
                break;
            case 19:
                meta.addStoredEnchant(Enchantment.ARROW_DAMAGE, 1, true);
        }
        book.setItemMeta(meta);
        return book;
    }

    public static final class AbilityNbt {

        private static final String KEY = "nova_ability";
        private static final String DROP_DEATH_KEY = "nova_dropdeath";

        public static ItemStack stamp(ItemStack item, String id) {
            if (item == null || id == null) return item;
            net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
            if (nms == null) return item;
            NBTTagCompound tag = nms.hasTag() ? nms.getTag() : new NBTTagCompound();
            tag.setString(KEY, id);
            nms.setTag(tag);
            return CraftItemStack.asBukkitCopy(nms);
        }

        public static String idOf(ItemStack item) {
            if (item == null) return null;
            net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
            if (nms == null || !nms.hasTag()) return null;
            NBTTagCompound tag = nms.getTag();
            return tag.hasKey(KEY) ? tag.getString(KEY) : null;
        }

        public static ItemStack setDroppedOnDeath(ItemStack item, boolean dropped) {
            if (item == null) return item;
            net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
            if (nms == null) return item;
            NBTTagCompound tag = nms.hasTag() ? nms.getTag() : new NBTTagCompound();
            tag.setBoolean(DROP_DEATH_KEY, dropped);
            nms.setTag(tag);
            return CraftItemStack.asBukkitCopy(nms);
        }

        public static boolean isDroppedOnDeath(ItemStack item) {
            if (item == null) return false;
            net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
            if (nms == null || !nms.hasTag()) return false;
            NBTTagCompound tag = nms.getTag();
            return tag.hasKey(DROP_DEATH_KEY) && tag.getBoolean(DROP_DEATH_KEY);
        }

        public static boolean isAbilityItem(ItemStack item) {
            return idOf(item) != null;
        }
    }

    public static final class Heads {


        public static ItemStack createCustomHead(String base64Texture) {
            ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) head.getItemMeta();

            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", base64Texture));

            try {
                Field profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, profile);
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "Erreur inattendue", e);
            }

            head.setItemMeta(meta);
            return head;
        }

        public static boolean isBannerMaterial(Material material) {
            return material == Material.BANNER || material == Material.WALL_BANNER
                    || material.name().endsWith("_BANNER") || material.name().endsWith("_WALL_BANNER");
        }
    }
}
