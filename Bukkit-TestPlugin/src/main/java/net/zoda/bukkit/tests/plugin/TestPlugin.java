package net.zoda.bukkit.tests.plugin;

import net.zoda.api.command.bukkit.manager.BukkitCommandManager;
import net.zoda.bukkit.tests.commands.TestCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class TestPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        BukkitCommandManager manager = BukkitCommandManager.instance();

        manager.registerCommand(new TestCommand(),getName());
    }
}
