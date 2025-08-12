package net.novaproject.novauhc.scenario;

import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;


public abstract class Scenario {

    public abstract String getName();

    public abstract String getDescription();

    public abstract ItemCreator getItem();

    private boolean active = false;

    public boolean isSpecial() {
        return false;
    }

    public boolean isWin() {
        return false;
    }

    public boolean hascustomDeathMessage() {
        return false;
    }

    public boolean hasCustomTeamTchat() {
        return false;
    }

    public void enable() {
        if (!isActive()) {
            active = true;
        }
    }

    public boolean needRooft() {
        return true;
    }

    public CustomInventory getMenu(Player player) {
        return null;
    }

    public void toggleActive() {
        active = !active;
    }

    public boolean isActive() {
        return active;
    }

    public void onBreak(Player player, Block block, BlockBreakEvent event) {

    }

    public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {

    }
    public void setup(){

    }

    public void onStart(Player player) {

    }

    public void onMeetup() {

    }
    public void onSec(Player p){

    }
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {

    }
    public void onCraft(ItemStack result, CraftItemEvent event){

    }

    public void onDrop(PlayerDropItemEvent event) {

    }
    public void onPlayerInteract(Player player,PlayerInteractEvent event){

    }
    public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {

    }
    public void onPlace(Player player, Block block, BlockPlaceEvent event){

    }

    public void onMove(Player player, PlayerMoveEvent event) {

    }

    public void noFood(FoodLevelChangeEvent event) {

    }

    public void onBow(Entity entity, Player player, EntityShootBowEvent event) {

    }

    public void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event) {

    }

    public void onPortal(PlayerPortalEvent event) {

    }

    public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {

    }

    public void onBlockIgnite(Block block, BlockIgniteEvent event) {

    }

    public void onEtityExplose(Entity entity, EntityExplodeEvent event) {

    }

    public void onEnchant(Player player, Enchantment enchant, ItemStack item, EnchantItemEvent event) {

    }

    public void onAnvilUse(ItemStack result, InventoryClickEvent event) {

    }

    public void onFurnace(ItemStack result, FurnaceSmeltEvent event) {
    }

    public void onFurnaceBurn(FurnaceBurnEvent event) {

    }

    public void onChatSpeek(Player player, String message, AsyncPlayerChatEvent event) {

    }

    public void onPlayerInteractonEntity(Player player, PlayerInteractEntityEvent event) {

    }

    public void onPvP() {

    }

    public void onDamage(Player player, EntityDamageEvent event) {

    }

    public void onCroCMD(Player player, String subCommand, String[] args) {

    }

    public void onLdCMD(Player player, String subCommand, String[] args) {

    }

    public void onTeamUpdate() {
    }

    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
    }

    public void onTaupeCMD(Player player, String subCommand, String[] args) {

    }

    public void onTeamCordCMD(Player player, int x, int y, int z, String coordsMessage) {

    }

    public void onTaupeTcCMD(Player player, int x, int y, int z, String coordsMessage) {

    }

    public void onPickUp(Player player, Item item, PlayerPickupItemEvent event) {
    }

    public void onAfterDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {

    }

    public void sendCustomDeathMessage(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
    }

    public void onFfCMD(Player player, String subCommand, String[] args) {

    }
}
