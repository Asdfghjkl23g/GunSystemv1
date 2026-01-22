package com.gunsystem;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.List;

public class GunListener implements Listener {
    private final GunSystem plugin;
    private final GunManager gunManager;
    
    public GunListener(GunSystem plugin, GunManager gunManager) {
        this.plugin = plugin;
        this.gunManager = gunManager;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || !item.hasItemMeta()) return;
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "gun_id"), PersistentDataType.STRING)) {
            return;
        }
        
        String gunId = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "gun_id"), PersistentDataType.STRING);
        Gun gun = gunManager.getGun(gunId);
        
        if (gun == null) return;
        
        // คลิกขวายิง
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            shoot(player, gun);
        }
    }
    
    private void shoot(Player player, Gun gun) {
        GunManager.PlayerGunData data = gunManager.getPlayerData(player);
        
        // ตรวจสอบว่ากำลังรีโหลดอยู่หรือไม่
        if (data.isReloading()) {
            return;
        }
        
        // ตรวจสอบอัตราการยิง
        if (!gunManager.canShoot(player, gun)) {
            return;
        }
        
        // ตรวจสอบกระสุนในปืน
        int currentAmmo = data.getAmmo(gun.getId(), gun.getMagazineSize());
        if (currentAmmo <= 0) {
            // รีโหลดอัตโนมัติ
            reload(player, gun);
            return;
        }
        
        // ลดกระสุนในปืน
        data.decreaseAmmo(gun.getId());
        gunManager.setLastShot(player);
        
        // เล่นเสียงยิง
        player.getWorld().playSound(player.getLocation(), gun.getShootSound(), 
                                    gun.getSoundVolume(), gun.getSoundPitch());
        
        // สร้างเอฟเฟกต์
        createShootEffect(player);
        
        // ยิง
        performRaycast(player, gun);
        
        // อัพเดทข้อมูลปืน
        updateGunLore(player, gun);
    }
    
    private void reload(Player player, Gun gun) {
        GunManager.PlayerGunData data = gunManager.getPlayerData(player);
        
        if (data.isReloading()) {
            return;
        }
        
        // ตรวจสอบกระสุนในกระเป๋า
        ItemStack bulletItem = findBullets(player);
        if (bulletItem == null || bulletItem.getAmount() < 1) {
            player.sendMessage("§c§lGUN §7» §cคุณไม่มีกระสุน!");
            return;
        }
        
        data.setReloading(true);
        player.sendMessage("§e§lGUN §7» §eกำลังรีโหลด...");
        
        // เล่นเสียงรีโหลด
        player.getWorld().playSound(player.getLocation(), gun.getReloadSound(), 1.0f, 1.0f);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                // ใช้กระสุน
                ItemStack bullets = findBullets(player);
                if (bullets != null) {
                    bullets.setAmount(bullets.getAmount() - 1);
                    
                    // เติมกระสุนเต็มแม็ก
                    data.setAmmo(gun.getId(), gun.getMagazineSize());
                    data.setReloading(false);
                    
                    player.sendMessage("§a§lGUN §7» §aรีโหลดเสร็จสิ้น!");
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
                    
                    // อัพเดทข้อมูลปืน
                    updateGunLore(player, gun);
                } else {
                    data.setReloading(false);
                    player.sendMessage("§c§lGUN §7» §cไม่พบกระสุน!");
                }
            }
        }.runTaskLater(plugin, gun.getReloadTime());
    }
    
    private void performRaycast(Player player, Gun gun) {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection();
        
        // Raycast สำหรับหาเป้าหมาย
        RayTraceResult blockResult = player.getWorld().rayTraceBlocks(eyeLocation, direction, 100);
        RayTraceResult entityResult = player.getWorld().rayTraceEntities(eyeLocation, direction, 100, 
                                                                          e -> e != player && (e instanceof LivingEntity));
        
        Location hitLocation = null;
        LivingEntity hitEntity = null;
        
        // เช็คว่าโดนบล็อคหรือ Entity ก่อน
        double blockDistance = blockResult != null ? blockResult.getHitPosition().distance(eyeLocation.toVector()) : Double.MAX_VALUE;
        double entityDistance = entityResult != null ? entityResult.getHitPosition().distance(eyeLocation.toVector()) : Double.MAX_VALUE;
        
        if (entityDistance < blockDistance && entityResult != null) {
            // โดน Entity
            hitEntity = (LivingEntity) entityResult.getHitEntity();
            hitLocation = entityResult.getHitPosition().toLocation(player.getWorld());
            
            // คำนวณดาเมจตามเกราะ
            double finalDamage = calculateDamage(gun.getDamage(), hitEntity, hitLocation);
            
            // ทำดาเมจ
            hitEntity.damage(finalDamage, player);
            
            // เอฟเฟกต์เลือด
            hitLocation.getWorld().spawnParticle(Particle.BLOCK, hitLocation, 10, 0.2, 0.2, 0.2, 
                                                Material.REDSTONE_BLOCK.createBlockData());
            
        } else if (blockResult != null) {
            // โดนบล็อค
            hitLocation = blockResult.getHitPosition().toLocation(player.getWorld());
            
            // เอฟเฟกต์บล็อค
            Material blockType = player.getWorld().getBlockAt(hitLocation).getType();
            if (blockType != Material.AIR) {
                hitLocation.getWorld().spawnParticle(Particle.BLOCK, hitLocation, 10, 0.2, 0.2, 0.2, 
                                                    blockType.createBlockData());
            }
        }
        
        if (hitLocation != null) {
            // เอฟเฟกต์การยิง
            hitLocation.getWorld().spawnParticle(Particle.FLAME, hitLocation, 5, 0.1, 0.1, 0.1, 0.02);
        }
    }
    
    private double calculateDamage(double baseDamage, LivingEntity entity, Location hitLocation) {
        if (!(entity instanceof Player)) {
            return baseDamage;
        }
        
        Player target = (Player) entity;
        double damage = baseDamage;
        
        // ตรวจสอบว่ายิงโดนหัวหรือตัว
        boolean isHeadshot = hitLocation.getY() > target.getEyeLocation().getY() - 0.3;
        
        if (isHeadshot) {
            // ตรวจเกราะหัว
            ItemStack helmet = target.getInventory().getHelmet();
            if (helmet != null && !helmet.getType().isAir()) {
                damage *= getArmorReduction(helmet.getType());
            }
        } else {
            // ตรวจเกราะตัว
            ItemStack chestplate = target.getInventory().getChestplate();
            if (chestplate != null && !chestplate.getType().isAir()) {
                damage *= getArmorReduction(chestplate.getType());
            }
        }
        
        return damage;
    }
    
    private double getArmorReduction(Material armorType) {
        // ลดดาเมจตามประเภทเกราะ
        switch (armorType) {
            case NETHERITE_HELMET:
            case NETHERITE_CHESTPLATE:
                return 0.6; // ลด 40%
            case DIAMOND_HELMET:
            case DIAMOND_CHESTPLATE:
                return 0.7; // ลด 30%
            case IRON_HELMET:
            case IRON_CHESTPLATE:
                return 0.8; // ลด 20%
            case CHAINMAIL_HELMET:
            case CHAINMAIL_CHESTPLATE:
            case GOLDEN_HELMET:
            case GOLDEN_CHESTPLATE:
                return 0.85; // ลด 15%
            case LEATHER_HELMET:
            case LEATHER_CHESTPLATE:
                return 0.9; // ลด 10%
            default:
                return 1.0; // ไม่ลด
        }
    }
    
    private void createShootEffect(Player player) {
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();
        
        // สะเก็ดไฟและควัน
        Location effectLoc = eyeLoc.clone().add(direction.multiply(0.5));
        effectLoc.getWorld().spawnParticle(Particle.FLAME, effectLoc, 3, 0.05, 0.05, 0.05, 0.05);
        effectLoc.getWorld().spawnParticle(Particle.SMOKE, effectLoc, 5, 0.1, 0.1, 0.1, 0.02);
        effectLoc.getWorld().spawnParticle(Particle.FIREWORK, effectLoc, 2, 0.05, 0.05, 0.05, 0.02);
    }
    
    private ItemStack findBullets(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "bullet"), PersistentDataType.STRING)) {
                    return item;
                }
            }
        }
        return null;
    }
    
    private void updateGunLore(Player player, Gun gun) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!item.hasItemMeta()) return;
        
        ItemMeta meta = item.getItemMeta();
        GunManager.PlayerGunData data = gunManager.getPlayerData(player);
        int currentAmmo = data.getAmmo(gun.getId(), gun.getMagazineSize());
        
        List<String> lore = List.of(
            "§7━━━━━━━━━━━━━━━━━━━━━",
            "§e⚡ ดาเมจ: §f" + gun.getDamage(),
            "§e📊 กระสุน: §f" + currentAmmo + "/" + gun.getMagazineSize(),
            "§e⏱ อัตราการยิง: §f" + gun.getFireRate() + "ms",
            "§e🔄 เวลารีโหลด: §f" + (gun.getReloadTime() / 20.0) + "s",
            "§7━━━━━━━━━━━━━━━━━━━━━"
        );
        
        meta.setLore(lore);
        item.setItemMeta(meta);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        gunManager.clearPlayerData(event.getPlayer());
    }
}
