package com.gunsystem;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class GunSystem extends JavaPlugin {
    private GunManager gunManager;
    private GunGUI gunGUI;
    
    @Override
    public void onEnable() {
        // สร้างโฟลเดอร์ config
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        
        // โหลด Manager
        gunManager = new GunManager(this);
        gunGUI = new GunGUI(this, gunManager);
        
        // ลงทะเบียน Listener
        getServer().getPluginManager().registerEvents(new GunListener(this, gunManager), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this, gunManager, gunGUI), this);
        
        getLogger().info("§a[GunSystem] เปิดใช้งานระบบปืนเรียบร้อย!");
    }
    
    @Override
    public void onDisable() {
        gunManager.saveGuns();
        getLogger().info("§c[GunSystem] ปิดระบบปืนเรียบร้อย!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("gun")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§c§lGUN §7» §cคำสั่งนี้ใช้ได้เฉพาะผู้เล่นเท่านั้น!");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("gunsystem.admin")) {
                player.sendMessage("§c§lGUN §7» §cคุณไม่มีสิทธิ์ใช้คำสั่งนี้!");
                return true;
            }
            
            gunGUI.openMainMenu(player);
            return true;
        }
        
        if (command.getName().equalsIgnoreCase("givebullet")) {
            if (!sender.hasPermission("gunsystem.admin")) {
                sender.sendMessage("§c§lGUN §7» §cคุณไม่มีสิทธิ์ใช้คำสั่งนี้!");
                return true;
            }
            
            if (args.length < 2) {
                sender.sendMessage("§c§lGUN §7» §cใช้: /givebullet <player> <amount>");
                return true;
            }
            
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§c§lGUN §7» §cไม่พบผู้เล่น!");
                return true;
            }
            
            int amount;
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c§lGUN §7» §cจำนวนต้องเป็นตัวเลข!");
                return true;
            }
            
            ItemStack bullet = createBullet(amount);
            target.getInventory().addItem(bullet);
            target.sendMessage("§a§lGUN §7» §aคุณได้รับกระสุน §f" + amount + " §aนัด!");
            sender.sendMessage("§a§lGUN §7» §aให้กระสุน §f" + amount + " §aนัดกับ §f" + target.getName());
            
            return true;
        }
        
        return false;
    }
    
    private ItemStack createBullet(int amount) {
        ItemStack bullet = new ItemStack(Material.GOLD_NUGGET, amount);
        ItemMeta meta = bullet.getItemMeta();
        
        meta.setDisplayName("§e§lกระสุน");
        meta.setLore(List.of(
            "§7กระสุนสากล ใช้ได้กับปืนทุกชนิด",
            "§71 ชิ้น = เติมกระสุนได้เต็มแม็ก"
        ));
        
        meta.getPersistentDataContainer().set(
            new NamespacedKey(this, "bullet"),
            PersistentDataType.STRING,
            "universal"
        );
        
        bullet.setItemMeta(meta);
        return bullet;
    }
    
    public GunManager getGunManager() {
        return gunManager;
    }
}
