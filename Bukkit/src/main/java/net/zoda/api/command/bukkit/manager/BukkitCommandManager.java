package net.zoda.api.command.bukkit.manager;

import net.zoda.api.command.ICommand;
import net.zoda.api.command.bukkit.PlayerOnly;
import net.zoda.api.command.wrapper.CommandManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class BukkitCommandManager extends CommandManager<CommandSender> {
    public BukkitCommandManager() {
        super(CommandSender.class);
    }

    @Override
    public Class<? extends CommandSender> actorClassSubcommand(Method method) {
        return method.isAnnotationPresent(PlayerOnly.class) ? Player.class : CommandSender.class;
    }

    @Override
    public Class<? extends CommandSender> actorClassClass(ICommand command) {
        return command.getClass().isAnnotationPresent(PlayerOnly.class) ? Player.class : CommandSender.class;
    }
}
