package net.zoda.api.command.wrapper;

import lombok.Getter;
import net.zoda.api.command.ICommand;
import net.zoda.api.command.argument.registry.ArgumentTypeRegistry;

import java.lang.reflect.Method;


public abstract class CommandManager<T> {

    @Getter private final Class<T> defaultActorClass;

    public CommandManager(Class<T> actorClass) {
        this.defaultActorClass = actorClass;
    }

    public void registerCustomArgumentTypes(ArgumentTypeRegistry argumentTypeRegistry) {

    }

    public Class<? extends T> actorClassSubcommand(Method method) { return defaultActorClass; }
    public Class<? extends T> actorClassClass(ICommand command) { return defaultActorClass; }
}
