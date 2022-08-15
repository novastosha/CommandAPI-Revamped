package net.zoda.api.command.argument.type;

import net.zoda.api.command.ICommand;
import org.bukkit.command.CommandSender;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;

public interface ArgumentTypeImpl<T, A extends Annotation> {

    String stringify(T value, A annotation,
                     Method method, ICommand command);

    int maximumArgs(CommandSender sender, A annotation,
                    Method method, ICommand command);

    default boolean verifyAnnotation(A annotation, Logger logger,
                                     Method method, ICommand command) {
        return true;
    }

    default Class<?> customRequiredClass(Class<?> parameterType,A annotation,
                                         Method method, ICommand command) {
        return null;
    }
}
