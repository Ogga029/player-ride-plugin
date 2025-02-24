package com.ogga029.ride.events;

import com.ogga029.ride.Ride;
import org.bukkit.GameRule;
import org.bukkit.Tag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.util.Arrays;
import java.util.List;

public class Events implements Listener {
    private final List<EntityType> RideEntities = Arrays.asList(
            EntityType.BOAT,
            EntityType.CHEST_BOAT,
            EntityType.MINECART,
            EntityType.HORSE,
            EntityType.SKELETON_HORSE,
            EntityType.ZOMBIE_HORSE,
            EntityType.MULE,
            EntityType.DONKEY,
            EntityType.PIG,
            EntityType.STRIDER,
            EntityType.LLAMA,
            EntityType.TRADER_LLAMA,
            EntityType.CAMEL

    );

    @EventHandler
    void onDismount(EntityDismountEvent event) {
        Entity entity = event.getEntity();
        Entity dismounted = event.getDismounted();

        if (!(entity instanceof Player player)) {
            return;
        }

        if (dismounted instanceof Player target) {
            if (player.hasMetadata(Ride.riding_key) && target.hasMetadata(Ride.ridden_key)) {
                event.setCancelled(true);
                return;
            }
        }

        if (RideEntities.contains(entity.getType())) {
            if (player.hasMetadata(Ride.ridden_key)) {
                Player target = Ride.getTarget(player);
                if (target != null) {
                    player.addPassenger(target);
                }
            }
            if (player.hasMetadata(Ride.riding_key)) {
                Player target = Ride.getTarget(player);
                if (target != null) {
                    target.addPassenger(player);
                }
            }
        }
    }
    @EventHandler
    void onPlayerDeath(PlayerDeathEvent event){
        if (event.getPlayer().hasMetadata(Ride.riding_key) || event.getPlayer().hasMetadata(Ride.ridden_key))
            event.setKeepInventory(true);
    }

    @EventHandler
    public void onWantRide(PlayerInteractEntityEvent event) {
        if (event.getPlayer().hasMetadata(Ride.riding_key)) {
            if (RideEntities.contains(event.getRightClicked().getType())) {
                Player target = Ride.getTarget(event.getPlayer());
                target.addPassenger(event.getPlayer());
                event.getRightClicked().addPassenger(target);
                event.setCancelled(true);
            }
        }
        if (event.getPlayer().hasMetadata(Ride.ridden_key)) {
            if (RideEntities.contains(event.getRightClicked().getType()))
                event.setCancelled(true);
        }
    }
    
    private int sleepPlayerPercentage;
    @EventHandler
    void onInteractBed(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getClickedBlock() == null || !Tag.BEDS.isTagged(event.getClickedBlock().getType())) {
            return;
        }

        Player player = event.getPlayer();
        sleepPlayerPercentage = player.getWorld().getGameRuleValue(GameRule.PLAYERS_SLEEPING_PERCENTAGE);

        if (player.hasMetadata(Ride.riding_key)) {
            Player target = Ride.getTarget(player);
            if (!target.isSleeping()) {
                player.getWorld().setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, 1);
            }
        }
        if (player.hasMetadata(Ride.ridden_key)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBedLeave(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        Player target = Ride.getTarget(player);
        player.addPassenger(target);
        player.getWorld().setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, sleepPlayerPercentage);

//        if (player.hasMetadata(Ride.riding_key)) {
//            Player target = Ride.getTarget(Ride.ridden_key, player);
//            target.addPassenger(player);
//            player.getWorld().setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, sleepPlayerPercentage);
//        }

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Player target = Ride.getTarget(player);
        if (target == null) return;
        if (target.hasMetadata(Ride.ridden_key)){
            target.addPassenger(player);
        }
        if (target.hasMetadata(Ride.riding_key)){
            player.addPassenger(target);
        }
//        if (player.hasMetadata(Ride.ridden_key)){
//            Player target = Ride.getTarget(Ride.riding_key, player);
//            player.addPassenger(target);
//        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getPlayer().hasMetadata(Ride.ridden_key)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().hasMetadata(Ride.ridden_key)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Player target = Ride.getTarget(player);
        if (target == null) {
            if (player.hasMetadata(Ride.ridden_key))
                player.removeMetadata(Ride.ridden_key, Ride.getInstance());
            if (player.hasMetadata(Ride.riding_key))
                player.removeMetadata(Ride.riding_key, Ride.getInstance());
            return;
        }
        target.addPassenger(player);
//        if (player.hasMetadata(Ride.ridden_key)){
//            Player target = Ride.getTarget(Ride.riding_key, player);
//            assert target != null;
//            player.addPassenger(target);
//        }
    }
}