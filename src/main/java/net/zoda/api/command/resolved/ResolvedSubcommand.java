package net.zoda.api.command.resolved;

import lombok.Getter;
import net.zoda.api.command.resolved.executor.ResolvedExecutor;
import net.zoda.api.command.resolved.group.ResolvedSubcommandGroup;
import net.zoda.api.command.wrapper.CommandManager;

public final class ResolvedSubcommand extends ResolvedCommand{

    @Getter private final ResolvedExecutor executor;
    @Getter private final ResolvedSubcommandGroup[] groups;

    public ResolvedSubcommand(String name, String description, Class<? extends CommandManager<?>> manager
                                ,ResolvedSubcommandGroup[] groups, ResolvedExecutor executor) {
        super(name, description, manager);
        this.executor = executor;
        this.groups = groups;
    }
}
