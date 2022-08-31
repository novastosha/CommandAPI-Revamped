package net.zoda.api.command.resolved.argument;

import lombok.Getter;
import net.zoda.api.command.ICommand;
import net.zoda.api.command.argument.target.TargetType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class ResolvedCompletionArgument extends ResolvedArgument{

    @Getter private final String completer;
    @Getter private final TargetType type;
    @Getter private final boolean enforceCompletions;

    public ResolvedCompletionArgument(String name, boolean required, Class<? extends Annotation> argumentTypeAnnotationClass, int index, String completer, TargetType type, Annotation annotation, ICommand instance, Method executor,String description,boolean enforceCompletions) {
        super(name, required, argumentTypeAnnotationClass,index,annotation,instance,executor,description);
        this.completer = completer;
        this.type = type;
        this.enforceCompletions = enforceCompletions;
    }
}
