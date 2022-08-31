package net.zoda.api.command.argument.type;

import net.zoda.api.command.ICommand;
import net.zoda.api.command.utils.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public interface ArgumentTypeImpl<T, A extends Annotation> {

    String stringify(Object value, A annotation,
                     Method method, ICommand command);

    Pair<T, String> fromString(String[] args, A annotation, Method method, ICommand command);

    int maximumArgs(A annotation,
                    Method method, ICommand command);

    default boolean verifyAnnotation(A annotation, Logger logger,
                                     Method method, ICommand command) {
        return true;
    }

    default Class<?> customRequiredClass(A annotation, Method method, ICommand instance) {
        return null;
    }

}
