package net.novaproject.novauhc.scenario;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.novaproject.novauhc.ability.AbilitySpi.IAbility;
import net.novaproject.novauhc.game.GameSpi.Clearable;
import net.novaproject.novauhc.player.PlayerSpi.IUHCPlayer;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;

public final class ScenarioSpi {

    private ScenarioSpi() {
    }

    public interface IScenario {

        String getName();

        String getDescription(Player player);

        boolean isSpecial();

        String getPath();

        boolean isWin();

        boolean overridesVictory();

        boolean hascustomDeathMessage();

        boolean hasCustomTeamTchat();

        boolean needRooft();

        boolean canOpenInGameTeamUi();

        void enable();

        void toggleActive();

        void setup();

        void onGameStart();

        void onStop();

        void onBreak(Player player, Block block, BlockBreakEvent event);

        void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event);

        void onSec(Player p);

        void onStart(Player player);

        void onCraft(ItemStack result, CraftItemEvent event);

        void onDrop(PlayerDropItemEvent event);

        void onPlayerInteract(Player player, PlayerInteractEvent event);

        void onPlayerTakeDamage(Entity entity, EntityDamageEvent event);

        void onPlace(Player player, Block block, BlockPlaceEvent event);

        void onMove(Player player, PlayerMoveEvent event);

        void noFood(FoodLevelChangeEvent event);

        void onBow(Entity entity, Player player, EntityShootBowEvent event);

        void onProjectileHit(ProjectileHitEvent event);

        void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event);

        void onPortal(PlayerPortalEvent event);

        void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event);

        void onBlockIgnite(Block block, BlockIgniteEvent event);

        void onEntityExplode(Entity entity, EntityExplodeEvent event);

        void onFurnace(ItemStack result, FurnaceSmeltEvent event);

        void onFurnaceBurn(FurnaceBurnEvent event);

        void onChatSpeak(Player player, String message, AsyncPlayerChatEvent event);

        void onPlayerInteractEntity(Player player, PlayerInteractEntityEvent event);

        void onPvP();

        void onDamage(Player player, EntityDamageEvent event);

        void onTeamUpdate();

        void onTaupeTcCMD(Player player, int x, int y, int z, String coordsMessage);

        void onPickUp(Player player, Item item, PlayerPickupItemEvent event);
    }

    public interface IScenarioManager {

        Optional<? extends IScenario> getScenarioByName(String name);

        List<? extends IScenario> getScenarios();

        List<? extends IScenario> getActiveScenarios();

        boolean isScenarioActive(String scenarioName);

        List<? extends IScenario> getSpecialScenarios();

        List<? extends IScenario> getActiveSpecialScenarios();

        void setup();

        void markBooted();
    }

    public interface IBonds extends Clearable {

        void unlink(UUID first, UUID second);

        void onJoin(Player player);
    }

    public interface IRole {

        String getName();

        List<String> getDescriptionLines(Player player);

        void sendDescription(Player player);

        void registerKnowPlayers();

        void sendKnowPlayers();

        double getSpeedPercent();

        double getStrengthPercent();

        double getResistancePercent();

        double getStrengthCriticPercent();

        int getGoldenAppleAbsorptionHearts();

        int getGoldenAppleRegenLevel();

        int getGoldenAppleRegenHearts();

        List<UUID> getPartners();

        void addPartner(UUID partner);

        UUID getPartnerUuid();

        void setPartnerUuid(UUID partner);

        String getColor();

        Camps getCamp();

        void setCamp(Camps camp);

        String getPerceivedDisplay(Player viewer);

        Camps getPerceivedCamp(Player viewer);

        void clearFacades();

        boolean hasFacades();

        boolean hasFacadeFor(Player viewer);

        Material getIconMaterial();

        Set<? extends IAbility> getAbilities();

        IUHCPlayer getOwner();

        void onSetup();

        void onSec(Player player);

        void onBow(Entity shooter, Player target, EntityShootBowEvent event);

        void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event);

        void onInteract(Player player1, PlayerInteractEvent event);

        void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event);

        void onTakeDamage(EntityDamageEvent event);

        void onBlockBreak(BlockBreakEvent event);

        void onProjectileHit(ProjectileHitEvent event);

        void onMobDeath(EntityDeathEvent event);

        void onInteractEntity(PlayerInteractEntityEvent event);

        void onBlockPlace(BlockPlaceEvent event);
    }

    public interface IScenarioRole extends IScenario {

        int getRolesTimer();

        void setRolesTimer(int rolesTimer);

        boolean isRolesDistributed();

        boolean isCompositionOpen();

        boolean isGroupSizeAnnounce();

        void setGroupSizeAnnounce(boolean groupSizeAnnounce);

        int getGroupSizeDivisor();

        void setGroupSizeDivisor(int groupSizeDivisor);

        int getGroupSizeMin();

        void setGroupSizeMin(int groupSizeMin);

        int getGroupSizeMax();

        void setGroupSizeMax(int groupSizeMax);

        Camps[] getCamps();

        Camps getWinningCamp();

        void giveRoles();

        boolean hasCustomWinCondition();

        boolean showRoleScoreboard();

        List<? extends IUHCPlayer> getPlayersByRoleName(String name);
    }
}
