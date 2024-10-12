package com.bojarsky.pmessage;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class StarterKitCommandExecutor implements CommandExecutor {
    private final Pmessage plugin;
    private final HashMap<UUID, Boolean> hasUsedStarterKit;

    public StarterKitCommandExecutor(Pmessage plugin) {
        this.plugin = plugin;
        this.hasUsedStarterKit = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        if (player.isOp()) {
            plugin.giveStarterItems(player);
            player.sendMessage(ChatColor.GREEN + "Вы получили стартовые предметы!");
        } else {
            if (!hasUsedStarterKit.getOrDefault(playerId, false)) {
                plugin.giveStarterItems(player);
                hasUsedStarterKit.put(playerId, true);
                player.sendMessage(ChatColor.GREEN + "Вы использовали второй шанс, получив стартовые предметы!");
            } else {
                player.sendMessage(ChatColor.RED + "Вы уже воспользовались вторым шансом!");
            }
        }

        return true;
    }
}
