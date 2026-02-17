package ru.millyofficial.mkantiproxy.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.millyofficial.mkantiproxy.MKAntiProxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final MKAntiProxy plugin;

    public CommandManager(MKAntiProxy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("mkantiproxy.admin")) {
            sender.sendMessage(plugin.getMessages().get("command.no-permission",
                    "{prefix}", plugin.getMessages().get("prefix")));
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(plugin.getMessages().get("command.usage", "{command}", label));
            return true;
        }

        plugin.getConfigManager().reload();
        plugin.getMessages().reload();

        sender.sendMessage(plugin.getMessages().get("command.reload-success",
                "{prefix}", plugin.getMessages().get("prefix")));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {

        if (!sender.hasPermission("mkantiproxy.admin")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Collections.singletonList("reload");
        }

        return new ArrayList<>();
    }
}
