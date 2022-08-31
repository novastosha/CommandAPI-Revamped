package net.zoda.api.command.resolved.group;

import lombok.Getter;

public final class ResolvedChildSubcommandGroup extends ResolvedSubcommandGroup {
    @Getter private final ResolvedSubcommandGroup parent;

    public ResolvedChildSubcommandGroup(String name,String description, ResolvedSubcommandGroup parent) {
        super(name,description);
        this.parent = parent;
    }

    public ResolvedSubcommandGroup head() {
        ResolvedSubcommandGroup p = parent;

        while (p instanceof ResolvedChildSubcommandGroup child) {
            p = child.parent;
        }

        return p;
    }
}
