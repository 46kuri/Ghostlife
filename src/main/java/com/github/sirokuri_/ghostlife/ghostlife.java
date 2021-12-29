package com.github.sirokuri_.ghostlife;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;
import java.util.logging.Logger;

public final class ghostlife extends JavaPlugin implements Listener {

    private static final Logger log = Logger.getLogger("Minecraft");
    private static Economy econ = null;
    private static Permission perms = null;
    private static Chat chat = null;

    @Override
    public void onEnable() {
        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        getCommand("ghostlife").setExecutor(new command(this));
        getCommand("playerskullgive").setExecutor(new command(this));
        getCommand("adddamege").setExecutor(new command(this));
        getCommand("sellmmgui").setExecutor(new command(this));
        getCommand("smg").setExecutor(new command(this));
        saveDefaultConfig();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getView().getPlayer();
        Inventory inventory = e.getClickedInventory();
        ItemStack slot = e.getCurrentItem();
        if (slot == null) return;
        if (inventory == null) return;
        InventoryHolder inventoryHolder = inventory.getHolder();
        if(!(inventoryHolder instanceof MyHolder)) return;
        MyHolder holder = (MyHolder) inventoryHolder;
        if(holder.tags.get(0).equals("holder1")) {
            if (slot.getType() == Material.GREEN_STAINED_GLASS_PANE) {
                if (slot.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&aSHOPを開く"))) {
                    Inventory mirror = Bukkit.createInventory(new MyHolder("holder2"), 54, "§cSELLMMITEM SHOP");
                    Location loc = player.getLocation();
                    player.playSound(loc, Sound.BLOCK_CHEST_OPEN, 2, 1);
                    player.openInventory(mirror);
                }
            } else if (slot.getType() == Material.RED_STAINED_GLASS_PANE) {
                if (slot.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&cSHOPを閉じる"))) {
                    player.closeInventory();
                    Location loc = player.getLocation();
                    player.playSound(loc, Sound.BLOCK_CHEST_CLOSE, 2, 1);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cSELLMMSHOP&fを閉じました"));
                }
            } else if (slot.getType() == Material.BLACK_STAINED_GLASS_PANE) {
                if (slot.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&8売却可能アイテム一覧を表示する"))) {
                    player.closeInventory();
                    Location loc = player.getLocation();
                    player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 2, 1);
                    String sellItemDisplay = getConfig().getString("SellItemDisplay");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "" + sellItemDisplay));
                }
            } else if (slot.getType() == Material.YELLOW_STAINED_GLASS_PANE) {
                if (slot.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&eSHOP注意点"))) {
                    Location loc = player.getLocation();
                    player.playSound(loc, Sound.ENTITY_VILLAGER_NO, 2, 1);
                    e.setCancelled(true);
                }
            } else {
                return;
            }
        }
    }

    @EventHandler
    private void inventoryCloseEvent(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        Inventory inventory = e.getInventory();
        InventoryHolder inventoryHolder = e.getInventory().getHolder();
        if(inventoryHolder == null) return;
        if(!(inventoryHolder instanceof MyHolder)) return;
        MyHolder holder = (MyHolder) inventoryHolder;
        if(holder.tags.get(0).equals("holder1")){
            ItemStack[] contents = inventory.getContents();
            for (int i = 0; i < 9; i++) {
                ItemStack content = contents[i];
                if (content == null) {
                    continue;
                }
                if(!(content.getType() == Material.GREEN_STAINED_GLASS_PANE || content.getType() == Material.RED_STAINED_GLASS_PANE || content.getType() == Material.BLACK_STAINED_GLASS_PANE || content.getType() == Material.YELLOW_STAINED_GLASS_PANE)) {
                    player.getInventory().addItem(content);
                }
            }
        }
        if(holder.tags.get(0).equals("holder2")){
            ItemStack[] contents = inventory.getContents();
            List<String> itemDisplayNameList = new ArrayList<>();
            double totalMoney = 0;
            for (String key : getConfig().getConfigurationSection("mmitem").getKeys(false)) {
                int moneyamount = getConfig().getInt("mmitem." + key + ".sellprice");
                String ItemDisplayName = getConfig().getString("mmitem." + key + ".itemdisplay");
                for (int i = 0; i < 54; i++) {
                    ItemStack content = contents[i];
                    if (content == null) {
                        continue;
                    }
                    int amount = content.getAmount();
                    int money = amount * moneyamount;
                    if (content.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "" + ItemDisplayName))) {
                        totalMoney += money;
                        itemDisplayNameList.add(ItemDisplayName);
                    }
                }
            }
            Location loc = player.getLocation();
            player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 2, 1);
            EconomyResponse r = econ.depositPlayer(player, totalMoney);
            if (r.transactionSuccess()) {
                player.sendMessage(String.format("[smg]\n\n今回の売却額 : %s\n現在の所持金 : %s", econ.format(r.amount), econ.format(r.balance)));
            } else {
                player.sendMessage(String.format("An error occured: %s", r.errorMessage));
            }
        }else {
            return;
        }
    }

    @EventHandler
    public void onBlockbreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Random random = new Random();
        String world = player.getWorld().getName();
        int num = random.nextInt(30);
        if(world.equals("resource")){
            if (e.getBlock().getType() == Material.OAK_LEAVES) {
                if (num <= 2) {
                    if ((Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta())).getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&cトマト採取剣"))) {
                        int emptySlot = player.getInventory().firstEmpty();
                        if (emptySlot == -1) return;
                        Location loc = player.getLocation();
                        player.playSound(loc,Sound.BLOCK_BELL_USE, 2, 1);
                        getServer().dispatchCommand(getServer().getConsoleSender(), "mm i give " + player.getName() + " tomato 2");
                    }
                }
            }
        }
    }
}