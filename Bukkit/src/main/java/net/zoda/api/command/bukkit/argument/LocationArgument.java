package net.zoda.api.command.bukkit.argument;

import net.zoda.api.command.ICommand;
import net.zoda.api.command.argument.target.TargetType;
import net.zoda.api.command.argument.type.ArgumentType;
import net.zoda.api.command.argument.type.ArgumentTypeImpl;
import net.zoda.api.command.argument.type.TargetManager;
import net.zoda.api.command.argument.type.completion.CompletionType;
import net.zoda.api.command.bukkit.manager.BukkitCommandManager;
import net.zoda.api.command.bukkit.WorldlessLocation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)

@TargetManager(BukkitCommandManager.class)
@ArgumentType(typeClass = WorldlessLocation.class, name = "location", completionType = CompletionType.OPTIONALLY_AUTOMATIC)
public @interface LocationArgument {

    class Impl implements ArgumentTypeImpl<WorldlessLocation,LocationArgument> {

        @Override
        public String stringify(WorldlessLocation value, LocationArgument annotation, Method method, ICommand command) {
            return null;
        }

        @Override
        public WorldlessLocation fromString(String[] args, LocationArgument annotation, Method method, ICommand command) {
            return null;
        }

        @Override
        public int maximumArgs(LocationArgument annotation, Method method, ICommand command) {
            return 0;
        }
    }

    String name();

    boolean required() default true;

    String completer();

    TargetType completerType() default TargetType.FIELD;

    boolean enforceCompletions() default true;
}
