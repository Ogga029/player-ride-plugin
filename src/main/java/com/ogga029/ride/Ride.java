package com.ogga029.ride;

import com.ogga029.ride.events.Events;
import com.ogga029.ride.events.SyncValuesEvents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public final class Ride extends JavaPlugin {
    private static Ride instance;
    public static String riding_key;
    public static String ridden_key;

    @Override
    public void onEnable() {
        instance = this;
        riding_key = getName() + ":riding";
        ridden_key = getName() + ":ridden";
        getLogger().info("ʀɪᴅᴇ ᴇɴᴀʙʟᴇᴅ");
        Bukkit.getPluginManager().registerEvents(new Events(),this);
        Bukkit.getPluginManager().registerEvents(new SyncValuesEvents(), this);
        getCommand("rideplayer").setExecutor(new RidePlayerCMD());
    }

    @Override
    public void onDisable() {
    }

    public static Ride getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Ride instance has not been initialized yet!");
        }
        return instance;
    }


    public static Player getTarget(Player p) {
        Player r = null;

        try {
            if (p.hasMetadata(Ride.riding_key)) {
                r = Bukkit.getPlayer(p.getMetadata(Ride.riding_key).getFirst().asString());
            }
            if (p.hasMetadata(Ride.ridden_key)) {
                r = Bukkit.getPlayer(p.getMetadata(Ride.ridden_key).getFirst().asString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return r;
    }

    public static boolean ride(Player player, Player target){
        if (target == null || !target.isOnline()) {
            return false;
        }

        if (player.equals(target)) {
            return false;
        }

        boolean isAlreadyRiding = player.hasMetadata(Ride.riding_key) && target.hasMetadata(Ride.ridden_key);
        if (!isAlreadyRiding) {
            target.addPassenger(player);
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 5, true, false, false));

            player.setMetadata(Ride.riding_key, new FixedMetadataValue(Ride.getInstance(), target.getName()));
            target.setMetadata(Ride.ridden_key, new FixedMetadataValue(Ride.getInstance(), player.getName()));
            player.setHealth(20);
            target.setHealth(20);
            return true;
        } else {
            target.removePassenger(player);
            player.removePotionEffect(PotionEffectType.HASTE);
            player.removeMetadata(Ride.riding_key, Ride.getInstance());
            target.removeMetadata(Ride.ridden_key, Ride.getInstance());
            return true;
        }
    }
}
