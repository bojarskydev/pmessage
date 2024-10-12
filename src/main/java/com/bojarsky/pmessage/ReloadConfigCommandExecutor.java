package com.bojarsky.pmessage;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ReloadConfigCommandExecutor implements CommandExecutor {

    private final JavaPlugin plugin;

    public ReloadConfigCommandExecutor(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender.isOp()) {
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Pmessage успешно перезагружен!");
        } else {
            sender.sendMessage(ChatColor.RED + "У вас нет прав.");
        }

        return true;
    }
}