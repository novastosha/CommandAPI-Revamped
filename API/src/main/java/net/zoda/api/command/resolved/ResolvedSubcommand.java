package net.zoda.api.command.resolved;

import lombok.Getter;
import net.zoda.api.command.ICommand;
import net.zoda.api.command.resolved.executor.ResolvedExecutor;
import net.zoda.api.command.resolved.group.ResolvedChildSubcommandGroup;
import net.zoda.api.command.resolved.group.ResolvedSubcommandGroup;
import net.zoda.api.command.wrapper.CommandManager;

public final class ResolvedSubcommand extends ResolvedCommand {

    @Getter private final ResolvedExecutor executor;

    /**
     * This field is always assigned to the tail (aka. the last value in the groups map) so in case of nested groups, this field would be an instance of {@link ResolvedChildSubcommandGroup}
     *
     * @see CommandManager#resolveCommand(ICommand)
     * @see CommandManager#parseSubcommand(String[], ClassResolvedCommand)
     */
    @Getter private final ResolvedSubcommandGroup group;

    public ResolvedSubcommand(String name, String description, Class<? extends CommandManager<?>> manager
                                , ResolvedSubcommandGroup group, ResolvedExecutor executor) {
        super(name, description, manager);
        this.executor = executor;
        this.group = group;
    }

    public String getFullName() {
        if(group == null) return super.getName();

        return group.getFullName()+" "+super.getName();
    }

}
