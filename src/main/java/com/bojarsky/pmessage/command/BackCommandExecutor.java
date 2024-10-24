package com.bojarsky.pmessage.command;

import com.bojarsky.pmessage.Pmessage;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;

public class BackCommandExecutor implements CommandExecutor, Listener {

    private HashMap<String, Location> deathLocations = new HashMap<>();
    private HashMap<String, Long> firstJoinTimes = new HashMap<>();

    private static final long TIME_LIMIT = 48 * 60 * 60 * 1000;

    public BackCommandExecutor(Pmessage plugin) {

    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String playerName = player.getName();

        if (!player.isOp()) {
            if (firstJoinTimes.containsKey(playerName)) {
                long firstJoinTime = firstJoinTimes.get(playerName);
                long currentTime = System.currentTimeMillis();

                if (currentTime - firstJoinTime > TIME_LIMIT) {
                    return;
                }
            }
        }

        deathLocations.put(playerName, player.getLocation());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        if (!firstJoinTimes.containsKey(playerName)) {
            firstJoinTimes.put(playerName, System.currentTimeMillis());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эту команду может использовать только игрок.");
            return true;
        }

        Player player = (Player) sender;
        String playerName = player.getName();

        if (!player.isOp()) {
            if (firstJoinTimes.containsKey(playerName)) {
                long firstJoinTime = firstJoinTimes.get(playerName);
                long currentTime = System.currentTimeMillis();

                if (currentTime - firstJoinTime > TIME_LIMIT) {
                    player.sendMessage(ChatColor.RED + "Вы уже не можете использовать команду /back.");
                    return true;
                }
            }
        }

        if (deathLocations.containsKey(playerName)) {
            Location deathLocation = deathLocations.get(playerName);
            player.teleport(deathLocation);
            player.sendMessage(ChatColor.GREEN + "Вы были возвращены на место своей смерти.");
            deathLocations.remove(playerName);
        } else {
            player.sendMessage(ChatColor.RED + "Нет сохранённого места смерти.");
        }

        return true;
    }
}