package ru.millyofficial.mkantiproxy.config;

import org.bukkit.configuration.file.FileConfiguration;
import ru.millyofficial.mkantiproxy.MKAntiProxy;

public class ConfigManager {

    private final MKAntiProxy plugin;
    private FileConfiguration config;

    public ConfigManager(MKAntiProxy plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public boolean isEnabled() {
        return config.getBoolean("enabled", true);
    }

    public boolean isCacheEnabled() {
        return config.getBoolean("cache.enabled", true);
    }

    public long getCacheExpiration() {
        return config.getLong("cache.expiration-hours", 24);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
