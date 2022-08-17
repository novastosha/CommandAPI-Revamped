package net.zoda.api.command.resolved;

import lombok.Getter;
import net.zoda.api.command.resolved.executor.ResolvedExecutor;
import net.zoda.api.command.wrapper.CommandManager;

import java.util.ArrayList;

public final class ClassResolvedCommand extends ResolvedCommand{
    @Getter private final ResolvedSubcommand[] subcommands;
    @Getter private final ResolvedExecutor defaultExecutor;

    public ClassResolvedCommand(String name, String description, Class<? extends CommandManager<?>> manager
            , ResolvedSubcommand[] subcommands, ResolvedExecutor defaultExecutor) {
        super(name, description, manager);
        this.subcommands = subcommands;
        this.defaultExecutor = defaultExecutor;
    }
}
