package com.ogga029.ride;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class RidePlayerCMD implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command!");
            return false;
        }

        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Missing 1 argument");
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player not found or offline.");
            return false;
        }

        if (player.equals(target)) {
            player.sendMessage(ChatColor.RED + "You cannot ride yourself!");
            return false;
        }

        return Ride.ride(player, target);
    }
}
