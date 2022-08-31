package net.zoda.api.command.argument;

import net.zoda.api.command.ICommand;
import net.zoda.api.command.argument.target.TargetType;
import net.zoda.api.command.argument.type.ArgumentType;
import net.zoda.api.command.argument.type.ArgumentTypeImpl;
import net.zoda.api.command.argument.type.completion.CompletionType;
import net.zoda.api.command.utils.Pair;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.logging.Logger;

@Retention(RetentionPolicy.RUNTIME)
@ArgumentType(typeClass = Integer[].class, name = "integer", completionType = CompletionType.OPTIONALLY_AUTOMATIC)
public @interface IntegerArgument {
    class Impl implements ArgumentTypeImpl<Integer, IntegerArgument> {

        static boolean verifyRange(double min,double max) {
            if (min == 0 && max == 0) return true;
            return !(min >= max);
        }

        @Override
        public boolean verifyAnnotation(IntegerArgument annotation, Logger logger,
                                        Method method, ICommand command
                ) {

            for (StringIntegerRepresentation representation : annotation.stringIntegerRepresentations()) {
                if (!representation.name().contains(" ")) continue;

                logger.severe("Argument: " + annotation.name() + " string-integer representation: " + representation.name() + " contains spaces!");
                return false;
            }


            return verifyRange(annotation.range().min(),annotation.range().max());
        }

        @Override
        public String stringify(Object value, IntegerArgument annotation, Method method, ICommand command) {
            return null;
        }

        @Override
        public Pair<Integer, String> fromString(String[] args, IntegerArgument annotation, Method method, ICommand command) {
            return null;
        }

        @Override
        public int maximumArgs(IntegerArgument annotation, Method method, ICommand command) {
            return 1;
        }


    }

    /**
     * Basically a string that represents a number
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface StringIntegerRepresentation {
        String name();

        int value();
    }

    /**
     * @return the argument's name
     */
    String name();
    String description() default "No description provided";

    boolean required() default true;

    String completer() default "";
    TargetType completerType() default TargetType.FIELD;
    boolean enforceCompletions() default true;


    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Range {
        int min();

        int max();
    }

    Range range() default @Range(max = 0, min = 0);

    StringIntegerRepresentation[] stringIntegerRepresentations() default {};
}
