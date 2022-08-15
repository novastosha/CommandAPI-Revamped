package net.zoda.api.command.wrapper;

import net.zoda.api.command.argument.registry.ArgumentTypeRegistry;


public abstract class CommandManager<T> {

    public CommandManager(Class<T> actorClass) {

    }

    public void registerCustomArgumentTypes(ArgumentTypeRegistry argumentTypeRegistry) {

    }

    public abstract Class<? extends T> actorClass();
}
