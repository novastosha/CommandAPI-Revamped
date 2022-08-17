package net.zoda.api.command.resolved.argument;

import lombok.Getter;

import java.lang.annotation.Annotation;

public class ResolvedArgument {

    @Getter private final String name;
    @Getter private final boolean required;

    @Getter private final Class<? extends Annotation> argumentTypeAnnotationClass;
    @Getter private final Annotation annotation;

    @Getter private final int index;

    public ResolvedArgument(String name, boolean required, Class<? extends Annotation> argumentTypeAnnotationClass,int index,Annotation annotation) {
        this.name = name;
        this.annotation = annotation;
        this.index = index;
        this.required = required;
        this.argumentTypeAnnotationClass = argumentTypeAnnotationClass;
    }
}
