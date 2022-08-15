package net.zoda.api.command.argument;

import net.zoda.api.command.ICommand;
import net.zoda.api.command.argument.target.TargetType;
import net.zoda.api.command.argument.type.ArgumentType;
import net.zoda.api.command.argument.type.BuiltinCompletionArgumentTypeImpl;
import net.zoda.api.command.argument.type.completion.CompletionType;
import net.zoda.api.command.utils.Utils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
@ArgumentType(typeClass = Enum.class, name = "enum", completionType = CompletionType.REQUIRED_AUTOMATIC)
public @interface EnumArgument {

    class Impl implements BuiltinCompletionArgumentTypeImpl<Enum<?>, EnumArgument> {

        @Override
        public String stringify(Enum<?> value, EnumArgument annotation, Method method, ICommand command) {
            return null;
        }

        @Override
        public int maximumArgs(CommandSender sender, EnumArgument annotation, Method method, ICommand command) {
            return 1;
        }

        @Override
        public @NotNull List<Enum<?>> completions(CommandSender sender, EnumArgument annotation, Method method, ICommand command) {
            return List.of(annotation.targetClass().getEnumConstants());
        }

        @Override
        public boolean verifyAnnotation(EnumArgument annotation, Logger logger, Method method, ICommand command) {

            if(annotation.targetClass().getEnumConstants().length == 0) {
                logger.severe("Target Enum Class: "+annotation.targetClass().getSimpleName()+" has no enum constants!");
                return false;
            }

            if(!annotation.customDisplay().isBlank()) return true;
            if(!Utils.isPresent(command,annotation.customDisplayTarget(), annotation.customDisplay())) {
                logger.severe(annotation.customDisplayTarget().name().toLowerCase(Locale.ROOT)+" for custom enum display is not present in: "+command.getClass().getSimpleName());
                return false;
            }

            Class<?> clazz = Utils.getType(annotation.customDisplay(),annotation.customDisplayTarget(),command);
            if(clazz == null) {
                logger.severe("Couldn't get class type of enum custom display of: "+command.getClass().getSimpleName());
                return false;
            }

            if(!clazz.isAssignableFrom(Function.class)) {
                logger.severe("Class type is not a Function!");
                return false;
            }

            ParameterizedType parameterizedType = Utils.getGenericType(annotation.customDisplay(),annotation.customDisplayTarget(),command);

            Class<?> firstClazz = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            Class<?> secondClazz = (Class<?>) parameterizedType.getActualTypeArguments()[1];

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

    String customDisplay() default "";
    TargetType customDisplayTarget() default TargetType.FIELD;
}
