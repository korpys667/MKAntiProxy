package ru.millyofficial.mkantiproxy.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.millyofficial.mkantiproxy.MKAntiProxy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Messages {

    private final MKAntiProxy plugin;
    private File messagesFile;
    private FileConfiguration messagesConfig;

    public Messages(MKAntiProxy plugin) {
        this.plugin = plugin;
        saveDefaultMessages();
        reload();
    }

    public void reload() {
        if (messagesFile == null) {
            messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        InputStream defaultStream = plugin.getResource("messages.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream));
            messagesConfig.setDefaults(defaultConfig);
        }
    }

    private void saveDefaultMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
    }

    public String get(String path, String... replacements) {
        String message = messagesConfig.getString(path, path);

        for (int i = 0; i < replacements.length - 1; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
