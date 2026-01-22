package com.gunsystem;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GunGUI {
    private final GunSystem plugin;
    private final GunManager gunManager;
    
    public GunGUI(GunSystem plugin, GunManager gunManager) {
        this.plugin = plugin;
        this.gunManager = gunManager;
    }
    
    // เมนูหลัก
    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6§l⚔ ระบบจัดการปืน ⚔");
        
        // สร้างปืน
        ItemStack createItem = new ItemStack(Material.ANVIL);
        ItemMeta createMeta = createItem.getItemMeta();
        createMeta.setDisplayName("§a§lสร้างปืนใหม่");
        createMeta.setLore(List.of("§7คลิกเพื่อสร้างปืนใหม่"));
        createItem.setItemMeta(createMeta);
        inv.setItem(11, createItem);
        
        // แก้ไขปืน
        ItemStack editItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta editMeta = editItem.getItemMeta();
        editMeta.setDisplayName("§e§lแก้ไขปืน");
        editMeta.setLore(List.of("§7คลิกเพื่อแก้ไขปืนที่มีอยู่"));
        editItem.setItemMeta(editMeta);
        inv.setItem(13, editItem);
        
        // ลบปืน
        ItemStack deleteItem = new ItemStack(Material.BARRIER);
        ItemMeta deleteMeta = deleteItem.getItemMeta();
        deleteMeta.setDisplayName("§c§lลบปืน");
        deleteMeta.setLore(List.of("§7คลิกเพื่อลบปืนที่มีอยู่"));
        deleteItem.setItemMeta(deleteMeta);
        inv.setItem(15, deleteItem);
        
        player.openInventory(inv);
    }
    
    // เมนูเลือกปืนเพื่อแก้ไข
    public void openEditMenu(Player player) {
        List<Gun> guns = new ArrayList<>(gunManager.getAllGuns());
        int size = ((guns.size() + 8) / 9) * 9;
        if (size > 54) size = 54;
        if (size < 9) size = 9;
        
        Inventory inv = Bukkit.createInventory(null, size, "§e§lเลือกปืนที่ต้องการแก้ไข");
        
        for (int i = 0; i < guns.size() && i < 54; i++) {
            Gun gun = guns.get(i);
            ItemStack item = new ItemStack(Material.IRON_HORSE_ARMOR);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§6§l" + gun.getDisplayName());
            meta.setLore(List.of(
                "§7ID: §f" + gun.getId(),
                "§7ดาเมจ: §f" + gun.getDamage(),
                "§7กระสุน: §f" + gun.getMagazineSize(),
                "§7อัตราการยิง: §f" + gun.getFireRate() + "ms",
                "",
                "§eคลิกเพื่อแก้ไข"
            ));
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }
        
        player.openInventory(inv);
    }
    
    // เมนูเลือกปืนเพื่อลบ
    public void openDeleteMenu(Player player) {
        List<Gun> guns = new ArrayList<>(gunManager.getAllGuns());
        int size = ((guns.size() + 8) / 9) * 9;
        if (size > 54) size = 54;
        if (size < 9) size = 9;
        
        Inventory inv = Bukkit.createInventory(null, size, "§c§lเลือกปืนที่ต้องการลบ");
        
        for (int i = 0; i < guns.size() && i < 54; i++) {
            Gun gun = guns.get(i);
            ItemStack item = new ItemStack(Material.RED_CONCRETE);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§c§l" + gun.getDisplayName());
            meta.setLore(List.of(
                "§7ID: §f" + gun.getId(),
                "",
                "§cคลิกเพื่อลบ (ไม่สามารถกู้คืนได้!)"
            ));
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }
        
        player.openInventory(inv);
    }
    
    // เมนูแก้ไขปืน
    public void openEditGunMenu(Player player, Gun gun) {
        Inventory inv = Bukkit.createInventory(null, 45, "§e§lแก้ไข: " + gun.getDisplayName());
        
        // ข้อมูลปืน
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§6§lข้อมูลปืน");
        infoMeta.setLore(List.of(
            "§7ID: §f" + gun.getId(),
            "§7ชื่อ: §f" + gun.getDisplayName(),
            "§7ดาเมจ: §f" + gun.getDamage(),
            "§7กระสุน: §f" + gun.getMagazineSize(),
            "§7อัตราการยิง: §f" + gun.getFireRate() + "ms",
            "§7เวลารีโหลด: §f" + (gun.getReloadTime() / 20.0) + "s"
        ));
        infoItem.setItemMeta(infoMeta);
        inv.setItem(4, infoItem);
        
        // ดาเมจ
        ItemStack damageItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta damageMeta = damageItem.getItemMeta();
        damageMeta.setDisplayName("§c§lดาเมจ: §f" + gun.getDamage());
        damageMeta.setLore(List.of(
            "§7ซ้าย: -1",
            "§7ขวา: +1",
            "§7Shift+ซ้าย: -5",
            "§7Shift+ขวา: +5"
        ));
        damageItem.setItemMeta(damageMeta);
        inv.setItem(19, damageItem);
        
        // กระสุน
        ItemStack ammoItem = new ItemStack(Material.ARROW);
        ItemMeta ammoMeta = ammoItem.getItemMeta();
        ammoMeta.setDisplayName("§e§lกระสุน: §f" + gun.getMagazineSize());
        ammoMeta.setLore(List.of(
            "§7ซ้าย: -1",
            "§7ขวา: +1",
            "§7Shift+ซ้าย: -5",
            "§7Shift+ขวา: +5"
        ));
        ammoItem.setItemMeta(ammoMeta);
        inv.setItem(21, ammoItem);
        
        // อัตราการยิง
        ItemStack fireRateItem = new ItemStack(Material.CLOCK);
        ItemMeta fireRateMeta = fireRateItem.getItemMeta();
        fireRateMeta.setDisplayName("§a§lอัตราการยิง: §f" + gun.getFireRate() + "ms");
        fireRateMeta.setLore(List.of(
            "§7ซ้าย: -50ms",
            "§7ขวา: +50ms",
            "§7Shift+ซ้าย: -200ms",
            "§7Shift+ขวา: +200ms",
            "§7(ต่ำ = เร็ว, สูง = ช้า)"
        ));
        fireRateItem.setItemMeta(fireRateMeta);
        inv.setItem(23, fireRateItem);
        
        // เวลารีโหลด
        ItemStack reloadItem = new ItemStack(Material.HOPPER);
        ItemMeta reloadMeta = reloadItem.getItemMeta();
        reloadMeta.setDisplayName("§b§lเวลารีโหลด: §f" + (gun.getReloadTime() / 20.0) + "s");
        reloadMeta.setLore(List.of(
            "§7ซ้าย: -10 ticks",
            "§7ขวา: +10 ticks",
            "§7Shift+ซ้าย: -20 ticks",
            "§7Shift+ขวา: +20 ticks",
            "§7(20 ticks = 1 วินาที)"
        ));
        reloadItem.setItemMeta(reloadMeta);
        inv.setItem(25, reloadItem);
        
        // รับปืน
        ItemStack giveItem = new ItemStack(Material.EMERALD);
        ItemMeta giveMeta = giveItem.getItemMeta();
        giveMeta.setDisplayName("§a§lรับปืน");
        giveMeta.setLore(List.of("§7คลิกเพื่อรับปืนนี้"));
        giveItem.setItemMeta(giveMeta);
        inv.setItem(40, giveItem);
        
        // บันทึก
        ItemStack saveItem = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta saveMeta = saveItem.getItemMeta();
        saveMeta.setDisplayName("§a§lบันทึก");
        saveMeta.setLore(List.of("§7คลิกเพื่อบันทึกการเปลี่ยนแปลง"));
        saveItem.setItemMeta(saveMeta);
        inv.setItem(39, saveItem);
        
        // ย้อนกลับ
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("§7§lย้อนกลับ");
        backItem.setItemMeta(backMeta);
        inv.setItem(36, backItem);
        
        player.openInventory(inv);
    }
    
    // เมนูสร้างปืนใหม่
    public void openCreateGunMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§a§lสร้างปืนใหม่");
        
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§e§lวิธีสร้างปืน");
        infoMeta.setLore(List.of(
            "§71. พิมพ์ ID ปืนในแชท (ภาษาอังกฤษไม่มีช่องว่าง)",
            "§7   ตัวอย่าง: ak47, m4a1, pistol",
            "§72. ระบบจะสร้างปืนให้อัตโนมัติ",
            "§73. คุณสามารถแก้ไขค่าต่างๆ ได้ภายหลัง"
        ));
        info.setItemMeta(infoMeta);
        inv.setItem(13, info);
        
        player.openInventory(inv);
        player.sendMessage("§a§lGUN §7» §aพิมพ์ ID ปืนที่ต้องการสร้าง (พิมพ์ 'cancel' เพื่อยกเลิก)");
    }
}
