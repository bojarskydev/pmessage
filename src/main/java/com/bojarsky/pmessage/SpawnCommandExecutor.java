package com.bojarsky.pmessage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommandExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        Location location = Bukkit.getWorld("world").getSpawnLocation();

        if (args.length > 0 && args[0].equalsIgnoreCase("tp")) {
            if (player.isOp()) {
                player.teleport(location);
            }
        } else {

            int x = (int) Math.floor(location.getX());
            int z = (int) Math.floor(location.getZ());

            player.sendMessage(ChatColor.GRAY + "X=" + x + " Z=" + z);
        }

        return true;
    }
}