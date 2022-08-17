package net.zoda.api.command.argument;

import net.zoda.api.command.ICommand;
import net.zoda.api.command.argument.target.TargetType;
import net.zoda.api.command.argument.type.ArgumentType;
import net.zoda.api.command.argument.type.BuiltinCompletionArgumentTypeImpl;
import net.zoda.api.command.argument.type.completion.CompletionType;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
@ArgumentType(typeClass = String.class, name = "string", completionType = CompletionType.OPTIONALLY_AUTOMATIC)
public @interface StringArgument {

    class Impl implements BuiltinCompletionArgumentTypeImpl<String,StringArgument> {

        @Override
        public String stringify(String value, StringArgument annotation, Method method, ICommand command) {
            return value;
        }

        @Override
        public String fromString(String[] args, StringArgument annotation, Method method, ICommand command) {
            return args[0];
        }

        @Override
        public int maximumArgs(StringArgument annotation, Method method, ICommand command) {
            return 0;
        }


        @Override
        public @NotNull List<String> completions(StringArgument annotation, Method method, ICommand command) {
            return new ArrayList<>();
        }
    }

    String name();

    String completer() default "";
    TargetType completerType() default TargetType.FIELD;

    boolean required() default true;

}
