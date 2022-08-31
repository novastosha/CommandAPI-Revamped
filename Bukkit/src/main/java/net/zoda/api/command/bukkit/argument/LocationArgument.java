package net.zoda.api.command.bukkit.argument;

import net.zoda.api.command.ICommand;
import net.zoda.api.command.argument.target.TargetType;
import net.zoda.api.command.argument.type.ArgumentType;
import net.zoda.api.command.argument.type.ArgumentTypeImpl;
import net.zoda.api.command.argument.type.TargetManager;
import net.zoda.api.command.argument.type.completion.CompletionType;
import net.zoda.api.command.bukkit.manager.BukkitCommandManager;
import net.zoda.api.command.utils.Pair;
import org.bukkit.Location;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

@Retention(RetentionPolicy.RUNTIME)

@TargetManager(BukkitCommandManager.class)
@ArgumentType(typeClass = Location.class, name = "location", completionType = CompletionType.OPTIONALLY_AUTOMATIC)
public @interface LocationArgument {

    class Impl implements ArgumentTypeImpl<Location,LocationArgument> {

        @Override
        public String stringify(Object value, LocationArgument annotation, Method method, ICommand command) {
            return null;
        }

        @Override
        public Pair<Location, String> fromString(String[] args, LocationArgument annotation, Method method, ICommand command) {
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
