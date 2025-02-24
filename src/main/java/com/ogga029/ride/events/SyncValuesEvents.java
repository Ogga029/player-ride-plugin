package com.ogga029.ride.events;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.destroystokyo.paper.event.player.PlayerClientOptionsChangeEvent;
import org.bukkit.attribute.Attribute;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import com.ogga029.ride.Ride;

import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;


public class SyncValuesEvents implements Listener {
    private final Set<UUID> damageSyncInProgress = new HashSet<>();
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            Player player = (Player) event.getEntity();
            UUID playerId = player.getUniqueId();

            if (damageSyncInProgress.contains(playerId)) {
                return;
            }

            damageSyncInProgress.add(playerId);
            try {
                Player target = Ride.getTarget(player);
                if (target != null) {
                    if (event.getCause() == EntityDamageEvent.DamageCause.MAGIC) {
                        return;
                    }

                    double newHealth = player.getHealth() - event.getFinalDamage();

                    if (newHealth <= 0) {
                        target.setHealth(0.0);
                        player.setHealth(0.0);
                    } else {
                        target.setHealth(Math.max(0.0, target.getHealth() - event.getFinalDamage()));
                    }
                }
            } finally {
                damageSyncInProgress.remove(playerId);
            }
        }
    }
    private final Set<UUID> regainHealthSyncInProgress = new HashSet<>();
    @EventHandler
    public void onRegainHealth(EntityRegainHealthEvent event){
        if (event.getEntityType() == EntityType.PLAYER){
            Player player = (Player) event.getEntity();

            UUID playerId = player.getUniqueId();

            if (regainHealthSyncInProgress.contains(playerId)) {
                return;
            }
            try {
                regainHealthSyncInProgress.add(playerId);
                Player target = Ride.getTarget(player);
                if (target == null) return;
                double maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                target.setHealth(Math.min(target.getHealth() + event.getAmount(), maxHealth));
            }
            finally {
                regainHealthSyncInProgress.remove(playerId);
            }
        }
    }
    @EventHandler
    public void onHotbarSwitch(PlayerItemHeldEvent event) {
        if (event.getPlayer().hasMetadata(Ride.ridden_key)){
            Player target = Ride.getTarget(event.getPlayer());
            if (target == null) return;
            target.getInventory().setHeldItemSlot(event.getNewSlot());
        }
        if (event.getPlayer().hasMetadata(Ride.riding_key)){
            event.setCancelled(true);
        }

    }
    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            Player player = (Player) event.getEntity();
            Player target = Ride.getTarget(player);
            if (target == null) return;
            target.setFoodLevel(event.getFoodLevel());
        }
    }
    @EventHandler
    public void onInvChange(PlayerInventorySlotChangeEvent event) {
        Player player = event.getPlayer();

        Player target = Ride.getTarget(player);

        if (target != null && target.isOnline()) {
            copyInventory(player, target);
        }
    }
    private void copyInventory(Player from, Player to) {
        ItemStack[] contents = from.getInventory().getContents();
        ItemStack[] armor = from.getInventory().getArmorContents();

        to.getInventory().setContents(contents);
        to.getInventory().setArmorContents(armor);
    }

    private final Set<UUID> potionSyncInProgress = new HashSet<>();

    @EventHandler
    public void onEffectGet(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        UUID playerId = player.getUniqueId();

        if (potionSyncInProgress.contains(playerId)) {
            return;
        }

        try {
            potionSyncInProgress.add(playerId);

            Player target = Ride.getTarget(player);
            if (event.getNewEffect() != null && target != null){
                target.addPotionEffect(event.getNewEffect());
            }
        } finally {
            potionSyncInProgress.remove(playerId);
        }
    }
    @EventHandler
    public void onXPChange(PlayerExpChangeEvent event) {
        if (!event.getPlayer().hasMetadata(Ride.ridden_key) || !event.getPlayer().hasMetadata(Ride.riding_key)) return;
        Player target = Ride.getTarget(event.getPlayer());
        if (target == null) return;
        syncXP(event.getPlayer(), target, event);
    }
    private static void syncXP(Player player1, Player player2, PlayerExpChangeEvent event) {
        if (player1 == null || player2 == null) return;

        player2.setLevel(player1.getLevel());
        player2.setExp(player1.getExp());

        player2.giveExp(event.getAmount());
    }


}
