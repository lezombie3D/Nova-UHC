package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioLang;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PotentialPermanent extends Scenario {

    @Override
    public Family getFamily() { return Family.VIE; }

    private final Map<UUID, Double> permanentHealth = new HashMap<>();
    private final Map<UUID, Double> absorptionHealth = new HashMap<>();

    @Var(lang = ScenarioVarLang.class, nameKey = "POTENTIALPERMANENT_VAR_STARTING_PERMANENT_HEALTH_NAME", descKey = "POTENTIALPERMANENT_VAR_STARTING_PERMANENT_HEALTH_DESC", type = VariableType.DOUBLE)
    private double startingPermanentHealth = 20.0;

    @Var(lang = ScenarioVarLang.class, nameKey = "POTENTIALPERMANENT_VAR_STARTING_ABSORPTION_HEALTH_NAME", descKey = "POTENTIALPERMANENT_VAR_STARTING_ABSORPTION_HEALTH_DESC", type = VariableType.DOUBLE)
    private double startingAbsorptionHealth = 20.0;

    @Var(name = "Vie permanente minimum", desc = "Plancher sous lequel la vie maximale ne peut pas descendre.", type = VariableType.DOUBLE, min = 1)
    private double minPermanentHealth = 2.0;

    @Var(name = "Conversion pomme d'or", desc = "Vie d'absorption convertie en permanent par une pomme d'or mangée à vie pleine.", type = VariableType.DOUBLE, min = 0)
    private double appleConversion = 2.0;

    @Var(name = "Conversion tête dorée", desc = "Vie d'absorption convertie en permanent par une tête dorée mangée à vie pleine.", type = VariableType.DOUBLE, min = 0)
    private double headConversion = 4.0;

    @Override
    public String getName() {
        return "PotentialPermanent";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.POTENTIAL_PERMANENT, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.GOLDEN_APPLE);
    }

    @Override
    public void onStart(Player player) {
        if (!isActive()) return;

        UUID uuid = player.getUniqueId();
        permanentHealth.put(uuid, startingPermanentHealth);
        absorptionHealth.put(uuid, startingAbsorptionHealth);
        applyHealth(player);

        LangManager.get().send(ScenarioLang.POTENTIALPERMANENT_STARTING_HEALTH, player, Map.of(
                "%permanent_hearts%", startingPermanentHealth / 2,
                "%absorption_hearts%", startingAbsorptionHealth / 2));
        LangManager.get().send(ScenarioLang.POTENTIALPERMANENT_CONVERSION_INFO, player);
    }

    @Override
    public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {
        if (!isActive() || !(entity instanceof Player player)) return;

        double lost = event.getFinalDamage();
        if (lost <= 0) return;

        UUID uuid = player.getUniqueId();
        double perm = Math.max(minPermanentHealth, permanentHealth.getOrDefault(uuid, startingPermanentHealth) - lost);
        permanentHealth.put(uuid, perm);

        Bukkit.getScheduler().runTask(Main.get(), () -> {
            if (player.isOnline()) applyHealth(player);
        });
    }

    @Override
    public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
        if (!isActive() || item.getType() != Material.GOLDEN_APPLE) return;
        if (player.getHealth() < player.getMaxHealth()) return;

        UUID uuid = player.getUniqueId();
        double available = absorptionHealth.getOrDefault(uuid, 0.0);
        if (available <= 0) return;

        double converted = Math.min(available, isGoldenHead(item) ? headConversion : appleConversion);
        if (converted <= 0) return;

        absorptionHealth.put(uuid, available - converted);
        permanentHealth.put(uuid, permanentHealth.getOrDefault(uuid, startingPermanentHealth) + converted);
        applyHealth(player);

        player.sendMessage(t(CONVERTED, player, Map.of("%hearts%", converted / 2)));
    }

    private boolean isGoldenHead(ItemStack item) {
        return item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                && item.getItemMeta().getDisplayName().equals(t(ScenarioLang.GOLDENHEAD_ITEM_NAME));
    }

    @Override
    public void onDeath(UHCPlayer dead, UHCPlayer killer, PlayerDeathEvent event) {
        permanentHealth.remove(dead.getUniqueId());
        absorptionHealth.remove(dead.getUniqueId());
    }

    private void applyHealth(Player player) {
        double perm = Math.max(minPermanentHealth, permanentHealth.getOrDefault(player.getUniqueId(), startingPermanentHealth));
        player.setMaxHealth(perm);
        if (player.getHealth() > perm) player.setHealth(perm);
    }

    @Override
    public void onSec(Player player) {
        if (!isActive()) return;
        double absorp = absorptionHealth.getOrDefault(player.getUniqueId(), 0.0);
        if (absorp <= 0) return;
        int level = Math.max(0, Math.min((int) Math.ceil(absorp / 4.0) - 1, 19));
        UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                new PotionEffect(PotionEffectType.ABSORPTION, 80, level)
        }, player);
    }

    @Override
    public void onStop() {
        permanentHealth.clear();
        absorptionHealth.clear();
    }

    private static final DynamicLang CONVERTED = DynamicLang.of("scenario.potentialpermanent.converted",
            "§6✦ §7%hearts% ❤ d'absorption convertis en vie permanente.");
}
