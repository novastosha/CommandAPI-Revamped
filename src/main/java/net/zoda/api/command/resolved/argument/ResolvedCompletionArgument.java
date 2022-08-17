package net.zoda.api.command.resolved.argument;

import lombok.Getter;
import net.zoda.api.command.argument.target.TargetType;

import java.lang.annotation.Annotation;

public class ResolvedCompletionArgument extends ResolvedArgument{

    @Getter private final String completer;
    @Getter private final TargetType type;

    public ResolvedCompletionArgument(String name, boolean required, Class<? extends Annotation> argumentTypeAnnotationClass, int index, String completer, TargetType type,Annotation annotation) {
        super(name, required, argumentTypeAnnotationClass,index,annotation);
        this.completer = completer;
        this.type = type;
    }
}
