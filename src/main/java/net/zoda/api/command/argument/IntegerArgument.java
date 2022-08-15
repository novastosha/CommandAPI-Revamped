package net.zoda.api.command.argument;

import net.zoda.api.command.ICommand;
import net.zoda.api.command.argument.type.ArgumentType;
import net.zoda.api.command.argument.type.ArgumentTypeImpl;
import net.zoda.api.command.argument.type.completion.CompletionType;
import org.bukkit.command.CommandSender;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.logging.Logger;

@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
@ArgumentType(typeClass = Integer.class, name = "integer", completionType = CompletionType.OPTIONALLY_AUTOMATIC)
public @interface IntegerArgument {
    class Impl implements ArgumentTypeImpl<Integer, IntegerArgument> {

        @Override
        public boolean verifyAnnotation(IntegerArgument annotation, Logger logger,
                                        Method method, ICommand command) {
            for (StringIntegerRepresentation representation : annotation.stringIntegerRepresentations()) {
                if(!representation.name().contains(" ")) continue;

                logger.severe("Argument: "+annotation.name()+" string-integer representation: "+representation.name()+" contains spaces!");
                return false;
            }

            if(annotation.range().min() == 0 && annotation.range().max() == 0) return true;

            if(annotation.range().min() >= annotation.range().max())  {
                return false;
            }

            return true;
        }

        @Override
        public String stringify(Integer value, IntegerArgument annotation, Method method, ICommand command) {
            return null;
        }

        @Override
        public int maximumArgs(CommandSender sender, IntegerArgument annotation, Method method, ICommand command) {
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

    boolean required() default true;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Range {
        int min();

        int max();
    }

    Range range() default @Range(max = 0, min = 0);

    StringIntegerRepresentation[] stringIntegerRepresentations() default {};
}
