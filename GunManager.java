package com.gunsystem;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GunManager {
    private final GunSystem plugin;
    private final Map<String, Gun> guns;
    private final Map<UUID, PlayerGunData> playerData;
    private final Map<UUID, Long> lastShot;
    private File gunsFile;
    
    public GunManager(GunSystem plugin) {
        this.plugin = plugin;
        this.guns = new HashMap<>();
        this.playerData = new HashMap<>();
        this.lastShot = new HashMap<>();
        loadGuns();
    }
    
    public void loadGuns() {
        gunsFile = new File(plugin.getDataFolder(), "guns.yml");
        if (!gunsFile.exists()) {
            plugin.saveResource("guns.yml", false);
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(gunsFile);
        ConfigurationSection gunsSection = config.getConfigurationSection("guns");
        
        if (gunsSection != null) {
            for (String key : gunsSection.getKeys(false)) {
                ConfigurationSection gunSection = gunsSection.getConfigurationSection(key);
                if (gunSection != null) {
                    guns.put(key, new Gun(gunSection));
                }
            }
        }
    }
    
    public void saveGuns() {
        try {
            FileConfiguration config = new YamlConfiguration();
            ConfigurationSection gunsSection = config.createSection("guns");
            
            for (Gun gun : guns.values()) {
                ConfigurationSection gunSection = gunsSection.createSection(gun.getId());
                gunSection.set("displayName", gun.getDisplayName());
                gunSection.set("damage", gun.getDamage());
                gunSection.set("magazineSize", gun.getMagazineSize());
                gunSection.set("fireRate", gun.getFireRate());
                gunSection.set("reloadTime", gun.getReloadTime());
                gunSection.set("shootSound", gun.getShootSound().name());
                gunSection.set("soundVolume", gun.getSoundVolume());
                gunSection.set("soundPitch", gun.getSoundPitch());
                gunSection.set("reloadSound", gun.getReloadSound().name());
            }
            
            config.save(gunsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void createGun(String id, Gun gun) {
        guns.put(id, gun);
        saveGuns();
    }
    
    public void deleteGun(String id) {
        guns.remove(id);
        saveGuns();
    }
    
    public Gun getGun(String id) {
        return guns.get(id);
    }
    
    public Collection<Gun> getAllGuns() {
        return guns.values();
    }
    
    public PlayerGunData getPlayerData(Player player) {
        return playerData.computeIfAbsent(player.getUniqueId(), k -> new PlayerGunData());
    }
    
    public boolean canShoot(Player player, Gun gun) {
        long currentTime = System.currentTimeMillis();
        long lastShotTime = lastShot.getOrDefault(player.getUniqueId(), 0L);
        
        if (currentTime - lastShotTime < gun.getFireRate()) {
            return false;
        }
        
        return true;
    }
    
    public void setLastShot(Player player) {
        lastShot.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    public void clearPlayerData(Player player) {
        playerData.remove(player.getUniqueId());
        lastShot.remove(player.getUniqueId());
    }
    
    public static class PlayerGunData {
        private final Map<String, Integer> ammoInGun;
        private boolean isReloading;
        
        public PlayerGunData() {
            this.ammoInGun = new HashMap<>();
            this.isReloading = false;
        }
        
        public int getAmmo(String gunId, int maxAmmo) {
            return ammoInGun.getOrDefault(gunId, maxAmmo);
        }
        
        public void setAmmo(String gunId, int ammo) {
            ammoInGun.put(gunId, ammo);
        }
        
        public void decreaseAmmo(String gunId) {
            int current = ammoInGun.getOrDefault(gunId, 0);
            if (current > 0) {
                ammoInGun.put(gunId, current - 1);
            }
        }
        
        public boolean isReloading() {
            return isReloading;
        }
        
        public void setReloading(boolean reloading) {
            this.isReloading = reloading;
        }
    }
}
