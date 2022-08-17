package net.zoda.api.command.argument;

import net.zoda.api.command.ICommand;
import net.zoda.api.command.argument.type.ArgumentType;
import net.zoda.api.command.argument.type.BuiltinCompletionArgumentTypeImpl;
import net.zoda.api.command.argument.type.completion.CompletionType;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;

@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
@ArgumentType(typeClass = Enum.class, name = "enum", completionType = CompletionType.REQUIRED_AUTOMATIC)
public @interface EnumArgument {

    class Impl implements BuiltinCompletionArgumentTypeImpl<Enum<?>, EnumArgument> {

        @Override
        public String stringify(Enum<?> value, EnumArgument annotation, Method method, ICommand command) {
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Enum<?> fromString(String[] args, EnumArgument annotation, Method method, ICommand command) {
            return Enum.valueOf(annotation.targetClass().getEnumConstants()[0].getClass(),args[0]);
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
        public boolean verifyAnnotation(EnumArgument annotation, Logger logger, Method method, ICommand command,Class<?> parameterType) {

            if(annotation.targetClass().getEnumConstants().length == 0) {
                logger.severe("Target Enum Class: "+annotation.targetClass().getSimpleName()+" has no enum constants!");
                return false;
            }

            return true;
        }

        @Override
        public Class<?> customRequiredClass(Class<?> parameterType, EnumArgument annotation, Method method, ICommand command) {
            return annotation.targetClass();
        }
    }

    String name();
    boolean required() default true;

    Class<? extends Enum<?>> targetClass();
}
