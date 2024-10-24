package com.bojarsky.pmessage.command;

import com.bojarsky.pmessage.Pmessage;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MsgCommandExecutor implements CommandExecutor {
    private final Pmessage plugin;

    public MsgCommandExecutor(Pmessage plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эту команду могут использовать только игроки.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Использование: /" + label + " <игрок> <сообщение>");
            return false;
        }

        Player senderPlayer = (Player) sender;
        Player recipientPlayer = plugin.getServer().getPlayerExact(args[0]);

        if (recipientPlayer == null || !recipientPlayer.isOnline()) {
            senderPlayer.sendMessage(ChatColor.RED + "Игрок " + args[0] + " не найден.");
            return true;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            messageBuilder.append(args[i]);
            if (i < args.length - 1) {
                messageBuilder.append(" ");
            }
        }
        String message = messageBuilder.toString();

        senderPlayer.sendMessage(ChatColor.DARK_PURPLE + recipientPlayer.getName() + " <- " + message);
        recipientPlayer.sendMessage(ChatColor.LIGHT_PURPLE + senderPlayer.getName() + " -> " + message);

        plugin.setLastConversation(senderPlayer.getUniqueId(), recipientPlayer.getUniqueId());
        plugin.setLastConversation(recipientPlayer.getUniqueId(), senderPlayer.getUniqueId());

        return true;
    }
}