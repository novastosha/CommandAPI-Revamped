package net.zoda.api.command.resolved;

import lombok.Getter;
import net.zoda.api.command.ICommand;
import net.zoda.api.command.resolved.executor.ResolvedExecutor;
import net.zoda.api.command.resolved.group.ResolvedSubcommandGroup;
import net.zoda.api.command.wrapper.CommandManager;

import java.util.Map;

public final class ClassResolvedCommand extends ResolvedCommand {
    @Getter private final Map<String, ResolvedSubcommand> subcommands;
    @Getter private final ResolvedExecutor defaultExecutor;
    @Getter private final Map<String, ResolvedSubcommandGroup> groups;
    @Getter private final ICommand instance;
    @Getter private final String[] aliases;

    public ClassResolvedCommand(String name, String description, Class<? extends CommandManager<?>> manager,
                                Map<String, ResolvedSubcommand> subcommands, Map<String, ResolvedSubcommandGroup> groups, ResolvedExecutor defaultExecutor,
                                ICommand instance, String[] aliases) {
        super(name, description, manager);
        
        this.aliases = aliases;
        this.instance = instance;
        this.subcommands = subcommands;
        this.groups = groups;
        this.defaultExecutor = defaultExecutor;
    }
}
