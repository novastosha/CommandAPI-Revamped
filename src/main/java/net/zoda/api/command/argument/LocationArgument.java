package net.zoda.api.command.argument;

import net.zoda.api.command.argument.target.TargetType;
import net.zoda.api.command.argument.type.ArgumentType;
import net.zoda.api.command.argument.type.TargetManager;
import net.zoda.api.command.argument.type.completion.CompletionType;
import net.zoda.api.command.utils.WorldlessLocation;
import net.zoda.api.command.wrapper.bukkit.BukkitCommandManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)

@TargetManager(BukkitCommandManager.class)
@ArgumentType(typeClass = WorldlessLocation.class, name = "integer", completionType = CompletionType.OPTIONALLY_AUTOMATIC)
public @interface LocationArgument {

    String name();
    boolean required() default true;

    String completer();
    TargetType completerType() default TargetType.FIELD;

    boolean enforceCompletions() default true;
}
