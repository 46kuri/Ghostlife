package com.github.sirokuri_.ghostlife.command;

import com.github.sirokuri_.ghostlife.ghostlife;
import com.github.sirokuri_.ghostlife.inventoryHolder.MyHolder;
import org.bukkit.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import java.util.ArrayList;
import java.util.List;

public class command implements CommandExecutor {

    private final ghostlife plugin;

    public command(ghostlife ghostlife){
        this.plugin = ghostlife;
    }

    @SuppressWarnings({"deprecation"})
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("ghostlife")) {
            if (args.length <= 0) {
                return true;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                //OP以外起動しないように設定
                if (sender.hasPermission("GhostLifeCommand.permission.Admin")) {
                    plugin.reloadConfig();
                    p.sendMessage("configリロードしました");
                }
                return true;
            }
        }

        if (cmd.getName().equalsIgnoreCase("playerskullgive")) {
            if (sender.hasPermission("GhostLifeCommand.permission.Admin")) {
                ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1, (short) 3);
                SkullMeta skull = (SkullMeta) item.getItemMeta();
                skull.setDisplayName(p.getName());
                skull.setOwner(p.getName());
                item.setItemMeta(skull);
                p.getInventory().addItem(item);
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("adddamege")) {
            if (sender.hasPermission("GhostLifeCommand.permission.Admin")) {
                if (args.length == 0) {
                    sender.sendMessage("コマンドを正しく入力してください");
                return true;
                } else {
                int damage = 0;
                    try {
                        damage = Integer.parseInt(args[0]);
                    } catch (NumberFormatException ex) {
                        sender.sendMessage("ダメージ量は数値で指定してください");
                    }
                    Player target = null;
                    if (args.length == 1) {
                        if (!(sender instanceof Player)) {
                            sender.sendMessage("ゲーム内から実行してください");
                            return true;
                        }
                        target = (Player) sender;
                    } else {
                        Player tar = Bukkit.getPlayer(args[1]);
                        if (tar == null || !tar.isOnline()) {
                            sender.sendMessage("指定されたプレイヤーはオンラインではありません");
                            return true;
                        }
                        target = tar;
                    }
                    target.damage(damage);
                }
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("sellmmgui") || cmd.getName().equalsIgnoreCase("smg")) {
            Inventory mirror = Bukkit.createInventory(new MyHolder("holder1"), 9, "§cSELLMMITEM MENU");
            ItemStack menu1 = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            ItemStack menu2 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemStack menu3 = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
            ItemStack menu4 = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta itemMeta1 = menu1.getItemMeta();
            ItemMeta itemMeta2 = menu2.getItemMeta();
            ItemMeta itemMeta3 = menu3.getItemMeta();
            ItemMeta itemMeta4 = menu4.getItemMeta();
            if (itemMeta1 == null || itemMeta2 == null || itemMeta3 == null || itemMeta4 == null) return true;
            itemMeta1.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aSHOPを開く"));
            itemMeta2.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&cSHOPを閉じる"));
            itemMeta3.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eSHOP注意点"));
            itemMeta4.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&8売却可能アイテム一覧を表示する"));
            List<String> lore3 = new ArrayList<String>();
            lore3.add(ChatColor.translateAlternateColorCodes('&', "&dSHOPのインベントリに"));
            lore3.add(ChatColor.translateAlternateColorCodes('&', "&d指定アイテム以外を入れてしまうと"));
            lore3.add(ChatColor.translateAlternateColorCodes('&', "&d一円にもならずアイテムが消えます"));
            lore3.add(ChatColor.translateAlternateColorCodes('&', "&d消えたアイテムに関しては&c補填対象外&dです"));
            itemMeta3.setLore(lore3);
            List<String> lore4 = new ArrayList<String>();
            List<String> itemDisplay = new ArrayList<String>();
            for (String key : plugin.getConfig().getConfigurationSection("mmitem").getKeys(false)) {
                String ItemDisplayName = plugin.getConfig().getString("mmitem." + key + ".itemdisplay");
                itemDisplay.add(ItemDisplayName);
            }
            lore4.add(ChatColor.translateAlternateColorCodes('&', "&d売却可能アイテム一覧は"));
            lore4.add(ChatColor.translateAlternateColorCodes('&', "クリックで閲覧できます"));
            itemMeta4.setLore(lore4);
            menu1.setItemMeta(itemMeta1);
            menu2.setItemMeta(itemMeta2);
            menu3.setItemMeta(itemMeta3);
            menu4.setItemMeta(itemMeta4);
            mirror.setItem(0, menu1);
            mirror.setItem(8, menu2);
            mirror.setItem(5, menu3);
            mirror.setItem(3, menu4);
            Location loc = p.getLocation();
            p.playSound(loc, Sound.BLOCK_CHEST_OPEN, 2, 1);
            p.openInventory(mirror);
        }
        return true;
    }
}
