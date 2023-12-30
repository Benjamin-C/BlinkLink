package dev.orangeben.blinklink;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class BlinkLink extends JavaPlugin {

    private TeleporterList tl;
    public static FileConfiguration config;

    @Override
    public void onEnable() {
        // Load plugin config
        this.saveDefaultConfig();
        config = this.getConfig();
        // Initialize TPer location
        tl = TeleporterList.fromFile();
        // Initialize listeners
        getServer().getPluginManager().registerEvents(new BlinkListener(tl), this);
        // Initialize commands
        this.getCommand("blinklink").setExecutor(new BlinkCommand(this));
        // Say hi
        getLogger().info("BlinkLink loaded");
    }

    @Override
    public void onDisable() {
        // Don't save config since we never change it
        // Save the config.
        // this.saveConfig();
        // Say bye
        getLogger().info("BlinkLink unloaded");
    }
}
