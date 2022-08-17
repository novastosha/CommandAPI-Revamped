package net.zoda.api.command.resolved;

import lombok.Getter;
import net.zoda.api.command.wrapper.CommandManager;

public class ResolvedCommand {

    @Getter private final Class<? extends CommandManager<?>> manager;
    @Getter private final String name;
    @Getter private final String description;

    ResolvedCommand(String name, String description, Class<? extends CommandManager<?>> manager) {
        this.name = name;
        this.manager = manager;
        this.description = description;
    }
}
