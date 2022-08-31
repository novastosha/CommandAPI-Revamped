package net.zoda.api.command.argument;

import net.zoda.api.command.ICommand;
import net.zoda.api.command.argument.type.ArgumentType;
import net.zoda.api.command.argument.type.BuiltinCompletionArgumentTypeImpl;
import net.zoda.api.command.argument.type.completion.CompletionType;
import net.zoda.api.command.utils.Pair;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;

@Retention(RetentionPolicy.RUNTIME)
@ArgumentType(typeClass = Enum.class, name = "enum", completionType = CompletionType.REQUIRED_AUTOMATIC)
public @interface EnumArgument {

    class Impl implements BuiltinCompletionArgumentTypeImpl<Enum<?>, EnumArgument> {

        @Override
        public String stringify(Object value, EnumArgument annotation, Method method, ICommand command) {
            return ((Enum<?>) value).name();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Pair<Enum<?>, String> fromString(String[] args, EnumArgument annotation, Method method, ICommand command) {
            try {
                return new Pair<>(Enum.valueOf(annotation.targetClass().getEnumConstants()[0].getClass(), args[0]), "");
            }catch (IllegalArgumentException exception) {
                return new Pair<>(null,"Unknown value: "+args[0]);
            }
        }

        /*Enum constants cannot have spaces*/
        @Override
        public int maximumArgs(EnumArgument annotation, Method method, ICommand command) {
            return 1;
        }

        @Override
        public @NotNull List<Enum<?>> completions(EnumArgument annotation, Method method, ICommand command) {
            return List.of(annotation.targetClass().getEnumConstants());
        }

        @Override
        public boolean verifyAnnotation(EnumArgument annotation, Logger logger, Method method, ICommand command) {

            if(annotation.targetClass().getEnumConstants().length == 0) {
                logger.severe("Target Enum Class: "+annotation.targetClass().getSimpleName()+" has no enum constants!");
                return false;
            }

            return true;
        }

        @Override
        public Class<?> customRequiredClass(EnumArgument annotation, Method method, ICommand command) {
            return annotation.targetClass();
        }
    }

    String name();
    String description() default "No description provided";

    boolean required() default true;

    Class<? extends Enum<?>> targetClass();
}
