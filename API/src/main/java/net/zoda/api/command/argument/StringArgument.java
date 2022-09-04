package net.zoda.api.command.argument;

import net.zoda.api.command.ICommand;
import net.zoda.api.command.argument.target.TargetType;
import net.zoda.api.command.argument.type.ArgumentType;
import net.zoda.api.command.argument.type.ArgumentTypeImpl;
import net.zoda.api.command.argument.type.completion.CompletionType;
import net.zoda.api.command.argument.type.completion.QuotesState;
import net.zoda.api.command.utils.Pair;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.logging.Logger;

@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
@ArgumentType(typeClass = String.class, name = "string", completionType = CompletionType.OPTIONALLY_AUTOMATIC,quotesState = QuotesState.OPTIONAL)
public @interface StringArgument {

    class Impl implements ArgumentTypeImpl<String,StringArgument> {

        @Override
        public String stringify(Object value, StringArgument annotation, Method method, ICommand command) {
            return (String) value;
        }

        @Override
        public Pair<String, String> fromString(String[] args, StringArgument annotation, Method method, ICommand command) {
            return new Pair<>(String.join(" ",args),"");
        }

        @Override
        public int maximumArgs(StringArgument annotation, Method method, ICommand command) {
            return 0;
        }


        @Override
        public boolean verifyAnnotation(StringArgument annotation, Logger logger, Method method, ICommand command) {
            return true;
        }
    }

    String name();
    String description() default "No description provided";

    String completer() default "";
    TargetType completerType() default TargetType.FIELD;
    boolean enforceCompletions() default true;

    boolean required() default true;

}
