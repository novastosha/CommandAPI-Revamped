package net.zoda.api.command.argument;

import net.zoda.api.command.ICommand;
import net.zoda.api.command.argument.target.TargetType;
import net.zoda.api.command.argument.type.ArgumentType;
import net.zoda.api.command.argument.type.BuiltinCompletionArgumentTypeImpl;
import net.zoda.api.command.argument.type.completion.CompletionType;
import net.zoda.api.command.utils.Pair;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;

@Target(ElementType.TYPE_USE)
@ArgumentType(typeClass = Double.class,name = "number",completionType = CompletionType.OPTIONALLY_AUTOMATIC)
@Retention(RetentionPolicy.RUNTIME)
public @interface NumberArgument {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface StringNumberRepresentation {
        String name();

        double value();
    }

    class Impl implements BuiltinCompletionArgumentTypeImpl<Double,NumberArgument> {


        @Override
        public String stringify(Object value, NumberArgument annotation, Method method, ICommand command) {
            return null;
        }

        @Override
        public Pair<Double, String> fromString(String[] args, NumberArgument annotation, Method method, ICommand command) {
            return null;
        }

        @Override
        public int maximumArgs(NumberArgument annotation, Method method, ICommand command) {
            return 0;
        }

        @Override
        public @NotNull List<Double> completions(NumberArgument annotation, Method method, ICommand command) {
            return null;
        }

        @Override
        public boolean verifyAnnotation(NumberArgument annotation, Logger logger, Method method, ICommand command) {
            for (StringNumberRepresentation representation : annotation.stringNumberRepresentations()) {
                if (!representation.name().contains(" ")) continue;

                logger.severe("Argument: " + annotation.name() + " string-integer representation: " + representation.name() + " contains spaces!");
                return false;
            }


            return IntegerArgument.Impl.verifyRange(annotation.range().min(),annotation.range().max());
        }
    }

    String name();
    String description() default "No description provided";

    String completer() default "";
    TargetType completerType() default TargetType.FIELD;
    boolean enforceCompletions() default true;

    boolean required() default true;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Range {
        double min();

        double max();
    }

    Range range() default @Range(max = 0, min = 0);

    StringNumberRepresentation[] stringNumberRepresentations() default {};
}
