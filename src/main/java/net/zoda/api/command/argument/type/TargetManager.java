package net.zoda.api.command.argument.type;

import net.zoda.api.command.wrapper.CommandManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The manager this argument type targets
 *
 * @see ArgumentType
 * @see CommandManager
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface TargetManager {
    Class<? extends CommandManager<?>> value();
}
