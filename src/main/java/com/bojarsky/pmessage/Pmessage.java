package com.bojarsky.pmessage;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
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
        this.getCommand("pmreload").setExecutor(new ReloadConfigCommandExecutor(this));
        this.getCommand("sc").setExecutor(new StarterKitCommandExecutor(this));
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getScheduler().runTaskTimer(this, this::checkPlayerActivity, 0L, 20L);
        this.getLogger().info("Private Messages has been enabled!");
        saveDefaultConfig();
    }

    public void giveStarterItems(Player player) {
        FileConfiguration config = getConfig();
        List<Map<?, ?>> items = config.getMapList("starter_items");

        if (items != null) {
            for (Map<?, ?> item : items) {
                String materialName = (String) item.get("material");
                int amount = (int) item.get("amount");

                Material material = Material.getMaterial(materialName);
                if (material == null) {
                    material = BukkitTools.matchRegistry(Registry.MATERIAL, materialName);
                }
                if (material != null) {
                    ItemStack itemStack = new ItemStack(material, amount);
                    NBTItem nbtItem = new NBTItem(itemStack);

                    if (item.containsKey("nbt")) {
                        Map<?, ?> nbtTags = (Map<?, ?>) item.get("nbt");

                        if (nbtTags.containsKey("tag")) {
                            Map<?, ?> tagData = (Map<?, ?>) nbtTags.get("tag");
                            if (tagData.containsKey("fluid")) {
                                Map<?, ?> fluidData = (Map<?, ?>) tagData.get("fluid");

                                NBTCompound fluidCompound = nbtItem.addCompound("fluid");

                                for (Map.Entry<?, ?> entry : fluidData.entrySet()) {
                                    String key = (String) entry.getKey();
                                    Object value = entry.getValue();

                                    if (key.equals("FluidName") && value instanceof String) {
                                        fluidCompound.setString("FluidName", (String) value);
                                    } else if (key.equals("Amount") && value instanceof Integer) {
                                        fluidCompound.setInteger("Amount", (Integer) value);
                                    }
                                }
                            }
                        }
                    }

                    itemStack = nbtItem.getItem();
                    player.getInventory().addItem(itemStack);
                }
            }
        }
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
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location deathLocation = player.getLocation();

        int x = (int) Math.floor(deathLocation.getX());
        int y = (int) Math.floor(deathLocation.getY());
        int z = (int) Math.floor(deathLocation.getZ());

        player.sendMessage("Координаты смерти: " + ChatColor.GREEN + "X=" + x + " Y=" + y + " Z=" + z);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage(null);
        if (!player.hasPlayedBefore()) {
            giveStarterItems(player);
            String message = ChatColor.GRAY + player.getName() + ChatColor.DARK_GRAY + " вошёл в игру впервые!";
            this.getServer().broadcastMessage(message);
        } else {
            String message = ChatColor.GRAY + player.getName() + ChatColor.DARK_GRAY + " вошёл в игру.";
            this.getServer().broadcastMessage(message);
        }
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