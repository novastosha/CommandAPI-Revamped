package net.zoda.api.command.bukkit.wrapper;

import net.zoda.api.command.resolved.ClassResolvedCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class BukkitCommandWrapper extends Command {
    private final CommandExecutor executor;

    public BukkitCommandWrapper(ClassResolvedCommand command, CommandExecutor executor) {
        super(command.getName(), command.getDescription(), "",List.of(command.getAliases()));
        this.executor = executor;
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
        return executor.onCommand(commandSender,this,s,strings);
    }
}
