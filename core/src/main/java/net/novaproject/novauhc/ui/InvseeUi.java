package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.player.utils.GameStatsTracker;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.chat.TextUtils;
import net.novaproject.novauhc.utils.cooldown.CooldownService;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Arrays;
import java.util.Map;

public class InvseeUi extends CustomInventory {

    private static final String ONLINE = "inventaire-du-joueur-invsee";
    private static final String OFFLINE = "inventaire-du-joueur-invsee-hors-ligne";

    private static final String HEALTH_LORE = "§7» Accès §cHost\n\n §c┃ §fVie courante, vie maximale et absorption\n §c┃ §fde la cible, rafraîchies en continu.\n\n §c☰ §f%player_health% / %player_max_health%\n";
    private static final String HUNGER_LORE = "§7» Accès §6Host\n\n §6┃ §fNourriture et saturation actuelles\n §6┃ §fde la cible.\n\n §6☰ §f%player_food% / 20\n";
    private static final String EFFECTS_LORE = "§7» Élément §5dynamique\n\n §5┃ §fChaque effet actif est listé avec\n §5┃ §fson niveau et sa durée restante.\n\n §5☰ §f%player_effect_count% effet(s)\n";
    private static final String INFO_LORE = "§7» Élément §edynamique\n\n §e┃ §fXP : %player_xp%\n §e┃ §fPosition : %player_position%\n §e┃ §fPommes d'or : %player_golden_apples%\n";
    private static final String COMBAT_LORE = "§7» Élément §bdynamique\n\n §b┃ §fKills : %player_kills%\n §b┃ §fAssists : %player_assists%\n §b┃ §fDégâts infligés : %player_damage_dealt%\n §b┃ §fDégâts reçus : %player_damage_taken%\n";
    private static final String ABILITIES_LORE = "§7» Élément §ddynamique\n\n §d┃ §fPouvoirs du rôle et cooldowns\n §d┃ §frestants, rafraîchis en continu.\n";
    private static final String NO_ROLE_LORE = "§7» Élément §7dynamique\n\n §7┃ §fAucun rôle attribué.\n";
    private static final String ABILITY_READY = "§a Disponible";
    private static final String OFFLINE_LORE = "§7» Accès §cHost\n\n §c┃ §fAucun inventaire, aucune armure et\n §c┃ §faucune statistique ne sont lisibles.\n";

    private final Player target;
    private final String targetName;
    private final boolean onlineLayout;

    public InvseeUi(Player player, Player target) {
        super(player);
        this.target = target;
        this.targetName = target.getName();
        this.onlineLayout = target.isOnline();
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu." + (onlineLayout ? ONLINE : OFFLINE) + ".title", "» Inventaire de %player%"),
                Map.<String, Object>of("%player%", targetName));
    }

    @Override
    public int getLines() {
        return 6;
    }

    @Override
    public boolean isRefreshAuto() {
        return true;
    }

    @Override
    public String broadcastKey() {
        return getClass().getName() + ":" + (onlineLayout ? ONLINE : OFFLINE);
    }

    @Override
    public void setup() {
        Player live = Bukkit.getPlayer(target.getUniqueId());
        if ((live != null) != onlineLayout) {
            new InvseeUi(getPlayer(), live != null ? live : target).open(1);
            return;
        }

        UiItems.panes(this, DyeColor.WHITE, 27, 28, 29, 30, 31, 32, 33, 34, 35);

        if (live == null) {
            addItem(new StaticItem(22, icon(OFFLINE, "offline", Material.BARRIER,
                    "§8┃ §c%pseudo% est hors-ligne", OFFLINE_LORE,
                    Map.<String, Object>of("%player%", targetName, "%pseudo%", targetName))));
            return;
        }

        ItemStack[] contents = live.getInventory().getContents();
        for (int i = 0; i < 36 && i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() == Material.AIR) continue;
            addItem(new StaticItem(i < 9 ? 27 + i : i - 9, item.clone()));
        }

        ItemStack[] armor = live.getInventory().getArmorContents();
        for (int i = 0; i < 4 && i < armor.length; i++) {
            ItemStack piece = armor[3 - i];
            if (piece == null || piece.getType() == Material.AIR) continue;
            addItem(new StaticItem(45 + i, piece.clone()));
        }

        Map<String, Object> state = Map.<String, Object>of(
                "%player%", targetName,
                "%pseudo%", targetName,
                "%player_health%", String.format("%.1f", live.getHealth()),
                "%player_max_health%", String.format("%.1f", live.getMaxHealth()),
                "%player_food%", live.getFoodLevel());

        addItem(new StaticItem(50, icon(ONLINE, "health", Material.GOLDEN_APPLE,
                "§8┃ §cVie", HEALTH_LORE, state)));
        addItem(new StaticItem(51, icon(ONLINE, "hunger", Material.COOKED_BEEF,
                "§8┃ §6Faim", HUNGER_LORE, state)));

        ItemCreator effects = icon(ONLINE, "effects", Material.POTION, "§8┃ §5Effets", EFFECTS_LORE,
                Map.<String, Object>of("%player_effect_count%", live.getActivePotionEffects().size()));
        for (PotionEffect effect : live.getActivePotionEffects()) {
            effects.addLore("§7- §f" + effect.getType().getName() + " " + (effect.getAmplifier() + 1)
                    + " §8(" + (effect.getDuration() / 20) + "s)");
        }
        addItem(new StaticItem(52, effects));

        addItem(new StaticItem(49, combatIcon()));
        addItem(new StaticItem(44, abilitiesIcon(live)));

        addItem(new ActionItem(36, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.WHITE),
                t(DynamicLang.of("menu." + ONLINE + ".back.name", "§8┃ §7§lActions")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ModerationMenus.SpectatorActions(getPlayer(), target.getUniqueId()).open();
            }
        });

        UHCPlayer up = UHCPlayerManager.get().getPlayer(live);
        Location location = live.getLocation();
        addItem(new StaticItem(53, icon(ONLINE, "player-info", Material.SKULL_ITEM,
                "§8┃ §e%pseudo%", INFO_LORE, Map.<String, Object>of(
                        "%pseudo%", targetName,
                        "%player_xp%", live.getLevel(),
                        "%player_golden_apples%", up == null ? 0 : up.getGoldenApplesEaten(),
                        "%player_position%", location.getBlockX() + ", " + location.getBlockY()
                                + ", " + location.getBlockZ()))
                .setDurability((short) 3)
                .setOwner(targetName)));
    }

    private ItemCreator combatIcon() {
        GameStatsTracker.PlayerGameStats stats = UHCManager.get().getStatsTracker().getStats(target.getUniqueId());
        return icon(ONLINE, "combat", Material.DIAMOND_SWORD, "§8┃ §bCombat", COMBAT_LORE,
                Map.<String, Object>of(
                        "%player_kills%", stats == null ? 0 : stats.kills,
                        "%player_assists%", stats == null ? 0 : stats.assists,
                        "%player_damage_dealt%", stats == null ? "0" : String.format("%.1f", stats.damageDealt),
                        "%player_damage_taken%", stats == null ? "0" : String.format("%.1f", stats.damageTaken)));
    }

    private ItemCreator abilitiesIcon(Player live) {
        UHCPlayer up = UHCPlayerManager.get().getPlayer(live);
        Role role = up == null ? null : KPIBuilder.roleOf(up);
        if (role == null) {
            return icon(ONLINE, "abilities-none", Material.NETHER_STAR, "§8┃ §7Pouvoirs", NO_ROLE_LORE, Map.of());
        }

        ItemCreator abilities = icon(ONLINE, "abilities", Material.NETHER_STAR, "§8┃ §dPouvoirs", ABILITIES_LORE,
                Map.<String, Object>of("%player_role%", role.getName()));
        abilities.addLore("§7» §f" + role.getName());
        for (Ability ability : role.getAbilities()) {
            if (!ability.active()) continue;
            long remainingMs = CooldownService.get(live, ability.getName() + "Cooldown");
            abilities.addLore("§7- §f" + ability.getName() + " §8:"
                    + (remainingMs == -1 ? ABILITY_READY
                    : " §e" + TextUtils.getFormattedTime((int) ((remainingMs + 999) / 1000))));
        }
        return abilities;
    }

    private ItemCreator icon(String menuId, String key, Material material, String name, String lore,
                             Map<String, Object> placeholders) {
        return new ItemCreator(material)
                .setName(t(DynamicLang.of("menu." + menuId + "." + key + ".name", name), placeholders))
                .setLores(Arrays.asList(t(DynamicLang.of("menu." + menuId + "." + key + ".lore", lore), placeholders)
                        .split("\n", -1)));
    }
}
