package net.zoda.api.command.resolved.group;

import lombok.Getter;

public final class ResolvedChildSubcommandGroup extends ResolvedSubcommandGroup{
    @Getter private final ResolvedSubcommandGroup[] parents;

    /**
     *
     * @param name The name of the group
     * @param parents Last index is the last parent and index 0 is the top parent which can only be {@link ResolvedSubcommandGroup}
     */
    public ResolvedChildSubcommandGroup(String name, ResolvedSubcommandGroup[] parents) {
        super(name);
        this.parents = parents;
    }
}
