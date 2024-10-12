package com.bojarsky.pmessage;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StarterKitCommandExecutor implements CommandExecutor {
    private final Pmessage plugin;

    public StarterKitCommandExecutor(Pmessage plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        if (player.isOp()) {
            plugin.giveStarterItems(player);
        } else {
                player.sendMessage(ChatColor.RED + "У вас нет прав для выполнения данной команды!");
            }

        return true;
    }
}
