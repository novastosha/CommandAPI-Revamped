package net.zoda.api.command.jda.argument;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.zoda.api.command.ICommand;

import java.lang.annotation.*;
import java.lang.reflect.Method;

/**
 * When an argument is annotated with this, the {@link net.zoda.api.command.argument.type.ArgumentTypeImpl#fromString(String[], Annotation, Method, ICommand)} call will not be executedand the value will be directly passed
 *
 * @see net.zoda.api.command.argument.type.ArgumentTypeImpl
 * @see OptionType
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ArgumentTypeInJDA {
    OptionType value();
}
