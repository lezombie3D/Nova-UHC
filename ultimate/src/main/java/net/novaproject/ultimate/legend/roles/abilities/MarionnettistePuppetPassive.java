package net.novaproject.ultimate.legend.roles.abilities;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.AbilityHooks.Dying;
import net.novaproject.novauhc.ability.template.PassiveAbility;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.awt.Color;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class MarionnettistePuppetPassive extends PassiveAbility implements Dying {

    private static final Color FIL_GLOW = new Color(150, 60, 200);

    private static final DynamicLang PUPPET_NOTIF_TITLE =
            DynamicLang.of("legend.marionnettiste.notif.title", "§5Tu es une marionnette");
    private static final DynamicLang PUPPET_NOTIF_BODY =
            DynamicLang.of("legend.marionnettiste.notif.body",
                    "§7Reste à moins de §f%range% §7blocs de ton maître, sinon tu seras empoisonné.");

    @Var(name = "Puppet Range", desc = "Max distance before Poison.", type = VariableType.DOUBLE)
    private double maxRange = 16.0;

    private final Set<UUID> puppets = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Integer> puppetTypes = new ConcurrentHashMap<>();

    public MarionnettistePuppetPassive() { setCooldown(0); }

    @Override public String getName() { return "Marionnettes"; }
    @Override public Material getMaterial() { return null; }

    @Override
    public boolean onEnable(Player player) {
        if (puppets.isEmpty()) return false;

        for (UUID uuid : puppets) {
            Player puppet = org.bukkit.Bukkit.getPlayer(uuid);
            if (puppet == null || !puppet.isOnline()) continue;

            if (puppet.getLocation().distance(player.getLocation()) > maxRange) {
                puppet.removePotionEffect(PotionEffectType.POISON);
                puppet.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 0, false, false));
            }

            Integer type = puppetTypes.get(uuid);
            if (type == null) continue;
            PotionEffect effect = switch (type) {
                case 0 -> new PotionEffect(PotionEffectType.INCREASE_DAMAGE,    80, 0, false, false);
                case 1 -> new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,  80, 0, false, false);
                case 2 -> new PotionEffect(PotionEffectType.SPEED,              80, 0, false, false);
                default -> null;
            };
            if (effect != null) {
                puppet.removePotionEffect(effect.getType());
                puppet.addPotionEffect(effect);
            }
        }
        return true;
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (!uhcPlayer.equals(getOwner())) return;
        Player master = uhcPlayer.getPlayer();
        for (UUID uuid : puppets) {
            Player puppet = Bukkit.getPlayer(uuid);
            if (puppet == null || !puppet.isOnline()) continue;
            if (master != null) {
                DisplayService.resetGlow(master, puppet);
                DisplayService.resetGlow(puppet, master);
            }
            puppet.setHealth(0.0);
        }
        puppets.clear();
        puppetTypes.clear();
    }

    public void createPuppet(UHCPlayer deadPlayer, UHCPlayer master) {
        Player pp = deadPlayer.getPlayer();
        Player mp = master.getPlayer();
        if (pp == null || mp == null) return;

        pp.getInventory().clear();
        pp.getInventory().setArmorContents(new org.bukkit.inventory.ItemStack[4]);
        pp.setMaxHealth(20); pp.setHealth(20);
        pp.setGameMode(GameMode.SURVIVAL);
        if (master.getTeam().isPresent()) deadPlayer.forceSetTeam(Optional.of(master.getTeam().get()));
        pp.teleport(mp.getLocation());

        UUID uuid = pp.getUniqueId();
        puppets.add(uuid);
        puppetTypes.put(uuid, ThreadLocalRandom.current().nextInt(3));
        givePuppetEquipment(pp);

        DisplayService.glow(mp, pp, FIL_GLOW);
        DisplayService.glow(pp, mp, FIL_GLOW);
        DisplayService.notification(pp,
                LangManager.get().get(PUPPET_NOTIF_TITLE, pp),
                LangManager.get().get(PUPPET_NOTIF_BODY, pp, Map.of("%range%", (int) maxRange)), 6);
    }

    public boolean isPuppet(UUID uuid) { return puppets.contains(uuid); }

    private void givePuppetEquipment(Player p) {
        var inv = p.getInventory();
        inv.setBoots(new ItemCreator(Material.IRON_BOOTS).setUnbreakable(true).getItemstack());
        inv.setLeggings(new ItemCreator(Material.IRON_LEGGINGS).setUnbreakable(true).getItemstack());
        inv.setChestplate(new ItemCreator(Material.IRON_CHESTPLATE)
                .addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).setUnbreakable(true).getItemstack());
        inv.setHelmet(new ItemCreator(Material.IRON_HELMET).setUnbreakable(true).getItemstack());
        inv.addItem(new ItemCreator(Material.WOOD_SWORD)
                .addEnchantment(Enchantment.DAMAGE_ALL, 2).setUnbreakable(true).getItemstack());
        inv.addItem(new ItemCreator(Material.BOW).setUnbreakable(true).getItemstack());
        inv.addItem(new ItemCreator(Material.ARROW).setAmount(32).getItemstack());
        inv.addItem(new ItemCreator(Material.COOKED_BEEF).setAmount(64).getItemstack());
        inv.addItem(new ItemCreator(Material.GOLDEN_APPLE).setAmount(5).getItemstack());
    }
}

