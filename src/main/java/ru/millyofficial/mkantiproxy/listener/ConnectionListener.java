package ru.millyofficial.mkantiproxy.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import ru.millyofficial.mkantiproxy.MKAntiProxy;
import ru.millyofficial.mkantiproxy.api.APIClient;

import java.net.InetAddress;

public class ConnectionListener implements Listener {

    private final MKAntiProxy plugin;

    public ConnectionListener(MKAntiProxy plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!plugin.getConfigManager().isEnabled()) {
            return;
        }

        InetAddress address = event.getAddress();
        if (address.isLoopbackAddress() || address.isAnyLocalAddress()) {
            return;
        }

        try {
            int port = plugin.getServer().getPort();
            int online = plugin.getServer().getOnlinePlayers().size();

            APIClient.CheckResult result = plugin.getApiClient()
                    .checkIP(address.getHostAddress(), event.getName(), port, online).get();

            if (!result.hasError() && result.isBlocked()) {
                event.disallow(
                        AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                        plugin.getMessages().get("kick-message")
                );
            }
        } catch (Exception e) {
        }
    }
}