package com.bojarsky.pmessage;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Pmessage extends JavaPlugin implements Listener {
    private final Map<UUID, UUID> lastConversations = new HashMap<>();
    private final Map<UUID, Long> playerLastActivity = new HashMap<>();
    private final Map<UUID, Boolean> playerAwayStatus = new HashMap<>();

    @Override
    public void onEnable() {
        this.getCommand("msg").setExecutor(new MsgCommandExecutor(this));
        this.getCommand("w").setExecutor(new MsgCommandExecutor(this));
        this.getCommand("m").setExecutor(new MsgCommandExecutor(this));
        this.getCommand("pm").setExecutor(new MsgCommandExecutor(this));
        this.getCommand("r").setExecutor(new ReplyCommandExecutor(this));
        this.getCommand("spawn").setExecutor(new SpawnCommandExecutor());
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getScheduler().runTaskTimer(this, this::checkPlayerActivity, 0L, 20L);
        this.getLogger().info("Private Messages has been enabled!");
    }

    @Override
    public void onDisable() {
        lastConversations.clear();
        playerLastActivity.clear();
        playerAwayStatus.clear();
        this.getLogger().info("Private Messages has been disabled!");
    }

    public UUID getLastConversation(UUID playerUUID) {
        return lastConversations.get(playerUUID);
    }

    public void setLastConversation(UUID playerUUID, UUID recipientUUID) {
        lastConversations.put(playerUUID, recipientUUID);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage(null);
        String message = player.hasPlayedBefore() ?
                ChatColor.GRAY + player.getName() + ChatColor.DARK_GRAY + " вошёл в игру." :
                ChatColor.GRAY + player.getName() + ChatColor.DARK_GRAY + " вошёл в игру впервые!";
        this.getServer().broadcastMessage(message);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        playerLastActivity.remove(playerUUID);
        playerAwayStatus.remove(playerUUID);
        event.setQuitMessage(null);
        this.getServer().broadcastMessage(ChatColor.GRAY + player.getName() + ChatColor.DARK_GRAY + " вышел из игры.");
        lastConversations.remove(playerUUID);
        lastConversations.values().remove(playerUUID);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        handlePlayerActivity(player);
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        handlePlayerActivity(player);
    }

    private void handlePlayerActivity(Player player) {
        UUID playerUUID = player.getUniqueId();
        playerLastActivity.put(playerUUID, System.currentTimeMillis());

        if (Boolean.TRUE.equals(playerAwayStatus.get(playerUUID))) {
            playerAwayStatus.put(playerUUID, false);
            if (!player.isOp()) {
                this.getServer().broadcastMessage(ChatColor.GRAY + player.getName() + ChatColor.DARK_GRAY + " вернулся.");
            }
        }
    }

    private void checkPlayerActivity() {
        long currentTime = System.currentTimeMillis();
        for (Player player : this.getServer().getOnlinePlayers()) {
            UUID playerUUID = player.getUniqueId();
            Long lastActivity = playerLastActivity.get(playerUUID);
            if (lastActivity != null && currentTime - lastActivity >= 120000L) {
                if (!Boolean.TRUE.equals(playerAwayStatus.get(playerUUID))) {
                    playerAwayStatus.put(playerUUID, true);
                    if (!player.isOp()) {
                        this.getServer().broadcastMessage(ChatColor.GRAY + player.getName() + ChatColor.DARK_GRAY + " отошёл.");
                    }
                }
            }
        }
    }
}