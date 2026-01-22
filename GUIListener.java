package com.gunsystem;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class GUIListener implements Listener {
    private final GunSystem plugin;
    private final GunManager gunManager;
    private final GunGUI gunGUI;
    private final Map<UUID, String> editingGun;
    private final Map<UUID, Boolean> creatingGun;
    
    public GUIListener(GunSystem plugin, GunManager gunManager, GunGUI gunGUI) {
        this.plugin = plugin;
        this.gunManager = gunManager;
        this.gunGUI = gunGUI;
        this.editingGun = new HashMap<>();
        this.creatingGun = new HashMap<>();
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (title.contains("ระบบจัดการปืน")) {
            event.setCancelled(true);
            handleMainMenuClick(player, event);
        } else if (title.contains("เลือกปืนที่ต้องการแก้ไข")) {
            event.setCancelled(true);
            handleEditSelectClick(player, event);
        } else if (title.contains("เลือกปืนที่ต้องการลบ")) {
            event.setCancelled(true);
            handleDeleteSelectClick(player, event);
        } else if (title.contains("แก้ไข:")) {
            event.setCancelled(true);
            handleEditGunClick(player, event);
        } else if (title.contains("สร้างปืนใหม่")) {
            event.setCancelled(true);
            handleCreateGunClick(player, event);
        }
    }
    
    private void handleMainMenuClick(Player player, InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        
        ItemStack item = event.getCurrentItem();
        if (!item.hasItemMeta()) return;
        
        String displayName = item.getItemMeta().getDisplayName();
        
        if (displayName.contains("สร้างปืนใหม่")) {
            gunGUI.openCreateGunMenu(player);
            creatingGun.put(player.getUniqueId(), true);
        } else if (displayName.contains("แก้ไขปืน")) {
            gunGUI.openEditMenu(player);
        } else if (displayName.contains("ลบปืน")) {
            gunGUI.openDeleteMenu(player);
        }
    }
    
    private void handleEditSelectClick(Player player, InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        
        ItemStack item = event.getCurrentItem();
        if (!item.hasItemMeta()) return;
        
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null || lore.isEmpty()) return;
        
        String idLine = lore.get(0);
        String gunId = idLine.replace("§7ID: §f", "");
        
        Gun gun = gunManager.getGun(gunId);
        if (gun != null) {
            editingGun.put(player.getUniqueId(), gunId);
            gunGUI.openEditGunMenu(player, gun);
        }
    }
    
    private void handleDeleteSelectClick(Player player, InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        
        ItemStack item = event.getCurrentItem();
        if (!item.hasItemMeta()) return;
        
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null || lore.isEmpty()) return;
        
        String idLine = lore.get(0);
        String gunId = idLine.replace("§7ID: §f", "");
        
        gunManager.deleteGun(gunId);
        player.sendMessage("§c§lGUN §7» §cลบปืน §f" + gunId + " §cเรียบร้อย!");
        player.closeInventory();
    }
    
    private void handleEditGunClick(Player player, InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        
        ItemStack item = event.getCurrentItem();
        if (!item.hasItemMeta()) return;
        
        String gunId = editingGun.get(player.getUniqueId());
        Gun gun = gunManager.getGun(gunId);
        if (gun == null) return;
        
        String displayName = item.getItemMeta().getDisplayName();
        ClickType click = event.getClick();
        
        if (displayName.contains("ดาเมจ")) {
            adjustDamage(gun, click);
            gunGUI.openEditGunMenu(player, gun);
        } else if (displayName.contains("กระสุน")) {
            adjustMagazine(gun, click);
            gunGUI.openEditGunMenu(player, gun);
        } else if (displayName.contains("อัตราการยิง")) {
            adjustFireRate(gun, click);
            gunGUI.openEditGunMenu(player, gun);
        } else if (displayName.contains("เวลารีโหลด")) {
            adjustReloadTime(gun, click);
            gunGUI.openEditGunMenu(player, gun);
        } else if (displayName.contains("บันทึก")) {
            gunManager.saveGuns();
            player.sendMessage("§a§lGUN §7» §aบันทึกการเปลี่ยนแปลงเรียบร้อย!");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
        } else if (displayName.contains("รับปืน")) {
            giveGun(player, gun);
        } else if (displayName.contains("ย้อนกลับ")) {
            editingGun.remove(player.getUniqueId());
            gunGUI.openMainMenu(player);
        }
    }
    
    private void handleCreateGunClick(Player player, InventoryClickEvent event) {
        // ไม่ต้องทำอะไร แค่รอให้ผู้เล่นพิมพ์ในแชท
    }
    
    private void adjustDamage(Gun gun, ClickType click) {
        double current = gun.getDamage();
        switch (click) {
            case LEFT:
                gun.setDamage(Math.max(0.5, current - 1));
                break;
            case RIGHT:
                gun.setDamage(current + 1);
                break;
            case SHIFT_LEFT:
                gun.setDamage(Math.max(0.5, current - 5));
                break;
            case SHIFT_RIGHT:
                gun.setDamage(current + 5);
                break;
        }
    }
    
    private void adjustMagazine(Gun gun, ClickType click) {
        int current = gun.getMagazineSize();
        switch (click) {
            case LEFT:
                gun.setMagazineSize(Math.max(1, current - 1));
                break;
            case RIGHT:
                gun.setMagazineSize(current + 1);
                break;
            case SHIFT_LEFT:
                gun.setMagazineSize(Math.max(1, current - 5));
                break;
            case SHIFT_RIGHT:
                gun.setMagazineSize(current + 5);
                break;
        }
    }
    
    private void adjustFireRate(Gun gun, ClickType click) {
        int current = gun.getFireRate();
        switch (click) {
            case LEFT:
                gun.setFireRate(Math.max(50, current - 50));
                break;
            case RIGHT:
                gun.setFireRate(current + 50);
                break;
            case SHIFT_LEFT:
                gun.setFireRate(Math.max(50, current - 200));
                break;
            case SHIFT_RIGHT:
                gun.setFireRate(current + 200);
                break;
        }
    }
    
    private void adjustReloadTime(Gun gun, ClickType click) {
        int current = gun.getReloadTime();
        switch (click) {
            case LEFT:
                gun.setReloadTime(Math.max(10, current - 10));
                break;
            case RIGHT:
                gun.setReloadTime(current + 10);
                break;
            case SHIFT_LEFT:
                gun.setReloadTime(Math.max(10, current - 20));
                break;
            case SHIFT_RIGHT:
                gun.setReloadTime(current + 20);
                break;
        }
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        if (!creatingGun.containsKey(player.getUniqueId())) return;
        
        event.setCancelled(true);
        String message = event.getMessage();
        
        if (message.equalsIgnoreCase("cancel")) {
            creatingGun.remove(player.getUniqueId());
            player.sendMessage("§c§lGUN §7» §cยกเลิกการสร้างปืน");
            Bukkit.getScheduler().runTask(plugin, () -> gunGUI.openMainMenu(player));
            return;
        }
        
        // ตรวจสอบ ID
        if (!message.matches("[a-zA-Z0-9_]+")) {
            player.sendMessage("§c§lGUN §7» §cID ต้องเป็นภาษาอังกฤษและตัวเลขเท่านั้น! ไม่มีช่องว่าง");
            return;
        }
        
        if (gunManager.getGun(message) != null) {
            player.sendMessage("§c§lGUN §7» §cมีปืน ID นี้อยู่แล้ว!");
            return;
        }
        
        // สร้างปืนใหม่ด้วยค่าเริ่มต้น
        Gun newGun = new Gun(
            message,
            message,
            5.0,
            10,
            200,
            40,
            Sound.ENTITY_GENERIC_EXPLODE,
            1.0f,
            1.0f,
            Sound.ITEM_ARMOR_EQUIP_IRON
        );
        
        gunManager.createGun(message, newGun);
        creatingGun.remove(player.getUniqueId());
        
        player.sendMessage("§a§lGUN §7» §aสร้างปืน §f" + message + " §aเรียบร้อย!");
        
        Bukkit.getScheduler().runTask(plugin, () -> {
            editingGun.put(player.getUniqueId(), message);
            gunGUI.openEditGunMenu(player, newGun);
        });
    }
    
    private void giveGun(Player player, Gun gun) {
        ItemStack gunItem = new ItemStack(Material.IRON_HORSE_ARMOR);
        ItemMeta meta = gunItem.getItemMeta();
        
        meta.setDisplayName("§6§l" + gun.getDisplayName());
        meta.setLore(List.of(
            "§7━━━━━━━━━━━━━━━━━━━━━",
            "§e⚡ ดาเมจ: §f" + gun.getDamage(),
            "§e📊 กระสุน: §f" + gun.getMagazineSize() + "/" + gun.getMagazineSize(),
            "§e⏱ อัตราการยิง: §f" + gun.getFireRate() + "ms",
            "§e🔄 เวลารีโหลด: §f" + (gun.getReloadTime() / 20.0) + "s",
            "§7━━━━━━━━━━━━━━━━━━━━━"
        ));
        
        meta.getPersistentDataContainer().set(
            new NamespacedKey(plugin, "gun_id"),
            PersistentDataType.STRING,
            gun.getId()
        );
        
        gunItem.setItemMeta(meta);
        player.getInventory().addItem(gunItem);
        player.sendMessage("§a§lGUN §7» §aได้รับปืน §f" + gun.getDisplayName());
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
    }
}
