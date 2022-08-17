package net.zoda.api.command.argument.type;

import net.zoda.api.command.ICommand;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public interface ArgumentTypeImpl<T, A extends Annotation> {

    String stringify(T value, A annotation,
                     Method method, ICommand command);

    T fromString(String[] args, A annotation, Method method, ICommand command);

    int maximumArgs(A annotation,
                    Method method, ICommand command);

    default boolean verifyAnnotation(A annotation, Logger logger,
                                     Method method, ICommand command,Class<?> parameterType) {
        return true;
    }

    default Class<?> customRequiredClass(Class<?> parameterType,A annotation,
                                         Method method, ICommand command) {
        return null;
    }
}
