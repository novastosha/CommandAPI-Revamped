package net.zoda.api.command.resolved.argument;

import lombok.Getter;
import net.zoda.api.command.ICommand;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class ResolvedArgument {

    @Getter private final String name;
    @Getter private final boolean required;

    @Getter private final Class<? extends Annotation> argumentTypeAnnotationClass;
    @Getter private final Annotation annotation;

    @Getter private final int index;
    @Getter private final ICommand command;

    @Getter private final Method parentExecutor;

    @Getter private final String description;

    public ResolvedArgument(String name, boolean required, Class<? extends Annotation> argumentTypeAnnotationClass, int index, Annotation annotation, ICommand command, Method parentExecutor,String description) {
        this.name = name;
        this.description = description;
        this.parentExecutor = parentExecutor;
        this.command = command;
        this.annotation = annotation;
        this.index = index;
        this.required = required;
        this.argumentTypeAnnotationClass = argumentTypeAnnotationClass;
    }

    public boolean isOfType(Class<? extends Annotation> clazz) {
        return clazz.equals(argumentTypeAnnotationClass);
    }
}
