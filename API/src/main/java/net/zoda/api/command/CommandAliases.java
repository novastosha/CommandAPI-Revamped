package net.zoda.api.command;

import net.zoda.api.command.wrapper.CommandManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @implNote Manual implementation is required.
 * @apiNote This annotation isn't necessarily supported by the CommandManager
 *
 * @see Command
 * @see CommandManager#resolveCommand(ICommand)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandAliases {
    String[] value();
}
