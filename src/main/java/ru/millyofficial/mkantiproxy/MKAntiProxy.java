package ru.millyofficial.mkantiproxy;

import org.bukkit.plugin.java.JavaPlugin;
import ru.millyofficial.mkantiproxy.api.APIClient;
import ru.millyofficial.mkantiproxy.command.CommandManager;
import ru.millyofficial.mkantiproxy.config.ConfigManager;
import ru.millyofficial.mkantiproxy.config.Messages;
import ru.millyofficial.mkantiproxy.listener.ConnectionListener;

public final class MKAntiProxy extends JavaPlugin {

    private static MKAntiProxy instance;
    private ConfigManager configManager;
    private Messages messages;
    private APIClient apiClient;

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        this.messages = new Messages(this);
        this.apiClient = new APIClient(this);

        getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);

        var command = getCommand("mkantiproxy");
        if (command != null) {
            var executor = new CommandManager(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
    }

    @Override
    public void onDisable() {
    }

    public static MKAntiProxy getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public Messages getMessages() {
        return messages;
    }

    public APIClient getApiClient() {
        return apiClient;
    }
}
