package com.bojarsky.pmessage;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ReplyCommandExecutor implements CommandExecutor {
    private final Pmessage plugin;

    public ReplyCommandExecutor(Pmessage plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эту команду могут использовать только игроки.");
            return true;
        }

        Player senderPlayer = (Player) sender;
        UUID senderUUID = senderPlayer.getUniqueId();

        UUID recipientUUID = plugin.getLastConversation(senderUUID);

        if (recipientUUID == null) {
            senderPlayer.sendMessage(ChatColor.RED + "У вас нет недавнего собеседника.");
            return true;
        }

        Player recipientPlayer = plugin.getServer().getPlayer(recipientUUID);

        if (recipientPlayer == null || !recipientPlayer.isOnline()) {
            senderPlayer.sendMessage(ChatColor.RED + "Ваш последний собеседник не в сети.");
            return true;
        }

        if (args.length == 0) {
            senderPlayer.sendMessage(ChatColor.RED + "Пожалуйста, введите сообщение.");
            return false;
        }

        String message = String.join(" ", args);

        senderPlayer.sendMessage(ChatColor.DARK_PURPLE + recipientPlayer.getName() + " <- " + message);
        recipientPlayer.sendMessage(ChatColor.LIGHT_PURPLE + senderPlayer.getName() + " -> " + message);

        plugin.setLastConversation(senderUUID, recipientUUID);
        plugin.setLastConversation(recipientUUID, senderUUID);

        return true;
    }
}