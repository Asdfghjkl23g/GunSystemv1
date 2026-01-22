package com.gunsystem;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

public class Gun {
    private String id;
    private String displayName;
    private double damage;
    private int magazineSize;
    private int fireRate; // milliseconds
    private int reloadTime; // ticks (20 ticks = 1 second)
    private Sound shootSound;
    private float soundVolume;
    private float soundPitch;
    private Sound reloadSound;
    
    public Gun(String id, String displayName, double damage, int magazineSize, 
               int fireRate, int reloadTime, Sound shootSound, 
               float soundVolume, float soundPitch, Sound reloadSound) {
        this.id = id;
        this.displayName = displayName;
        this.damage = damage;
        this.magazineSize = magazineSize;
        this.fireRate = fireRate;
        this.reloadTime = reloadTime;
        this.shootSound = shootSound;
        this.soundVolume = soundVolume;
        this.soundPitch = soundPitch;
        this.reloadSound = reloadSound;
    }
    
    public Gun(ConfigurationSection section) {
        this.id = section.getName();
        this.displayName = section.getString("displayName", id);
        this.damage = section.getDouble("damage", 5.0);
        this.magazineSize = section.getInt("magazineSize", 10);
        this.fireRate = section.getInt("fireRate", 200);
        this.reloadTime = section.getInt("reloadTime", 40);
        
        String soundName = section.getString("shootSound", "ENTITY_GENERIC_EXPLODE");
        try {
            this.shootSound = Sound.valueOf(soundName);
        } catch (IllegalArgumentException e) {
            this.shootSound = Sound.ENTITY_GENERIC_EXPLODE;
        }
        
        this.soundVolume = (float) section.getDouble("soundVolume", 1.0);
        this.soundPitch = (float) section.getDouble("soundPitch", 1.0);
        
        String reloadSoundName = section.getString("reloadSound", "ITEM_ARMOR_EQUIP_IRON");
        try {
            this.reloadSound = Sound.valueOf(reloadSoundName);
        } catch (IllegalArgumentException e) {
            this.reloadSound = Sound.ITEM_ARMOR_EQUIP_IRON;
        }
    }
    
    // Getters
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public double getDamage() { return damage; }
    public int getMagazineSize() { return magazineSize; }
    public int getFireRate() { return fireRate; }
    public int getReloadTime() { return reloadTime; }
    public Sound getShootSound() { return shootSound; }
    public float getSoundVolume() { return soundVolume; }
    public float getSoundPitch() { return soundPitch; }
    public Sound getReloadSound() { return reloadSound; }
    
    // Setters
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setDamage(double damage) { this.damage = damage; }
    public void setMagazineSize(int magazineSize) { this.magazineSize = magazineSize; }
    public void setFireRate(int fireRate) { this.fireRate = fireRate; }
    public void setReloadTime(int reloadTime) { this.reloadTime = reloadTime; }
    public void setShootSound(Sound shootSound) { this.shootSound = shootSound; }
    public void setSoundVolume(float soundVolume) { this.soundVolume = soundVolume; }
    public void setSoundPitch(float soundPitch) { this.soundPitch = soundPitch; }
    public void setReloadSound(Sound reloadSound) { this.reloadSound = reloadSound; }
}
