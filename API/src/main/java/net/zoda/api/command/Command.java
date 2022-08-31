package net.zoda.api.command;

import net.zoda.api.command.wrapper.CommandManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Command {

    String value();
    String description() default "No description provided";

    Class<? extends CommandManager<?>> manager();

}
