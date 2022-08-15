package net.zoda.api.command.argument.registry;

import net.zoda.api.command.argument.EnumArgument;
import net.zoda.api.command.argument.IntegerArgument;
import net.zoda.api.command.argument.LocationArgument;
import net.zoda.api.command.argument.type.ArgumentType;
import net.zoda.api.command.argument.type.ArgumentTypeImpl;
import net.zoda.api.command.argument.type.completion.CompletionType;
import net.zoda.api.command.utils.Pair;

import java.lang.annotation.*;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class ArgumentTypeRegistry {

    private final Logger logger = Logger.getLogger("Argument Registry");

    static {
        instance().register(IntegerArgument.class, new IntegerArgument.Impl());
        instance().register(EnumArgument.class,new EnumArgument.Impl());
    }

    private final HashMap<Class<? extends Annotation>, Pair<ArgumentType, ArgumentTypeImpl<?,? extends Annotation>>> registeredArgumentTypes;

    public Map<Class<? extends Annotation>, Pair<ArgumentType, ArgumentTypeImpl<?, ? extends Annotation>>> getRegisteredArgumentTypes() {
        return Collections.unmodifiableMap(registeredArgumentTypes);
    }

    private ArgumentTypeRegistry() {
         this.registeredArgumentTypes = new HashMap<>();
    }

    private static ArgumentTypeRegistry instance;

    public static ArgumentTypeRegistry instance() {
        if (instance == null) instance = new ArgumentTypeRegistry();
        return instance;
    }

    public <T extends Annotation> void register(Class<T> argumentAnnotation, ArgumentTypeImpl<?, T> impl) {
        if(registeredArgumentTypes.containsKey(argumentAnnotation)) {
            logger.warning(argumentAnnotation.getSimpleName()+" is already registered!");
            return;
        }

        if (!argumentAnnotation.isAnnotationPresent(ArgumentType.class)) {
            logger.warning(ArgumentType.class.getSimpleName() + " annotation is missing from argument annotation: " + argumentAnnotation.getSimpleName());
            return;
        }

        ArgumentType argumentType = argumentAnnotation.getAnnotation(ArgumentType.class);

        ParameterizedType implementationGenericParameters = (ParameterizedType) impl.getClass().getGenericSuperclass();
        Class<?> targetClass = (Class<?>) implementationGenericParameters.getActualTypeArguments()[0];

        // Allow inheritance (for compatibility reasons)
        if (!targetClass.isAssignableFrom(argumentType.typeClass())) {
            logger.warning("Implementation target class of: "+argumentAnnotation.getSimpleName()+" does not match ArgumentType type class! "
                +"(expected: "+argumentType.typeClass().getSimpleName()+", got: "+targetClass.getSimpleName()+")");
            return;
        }

        if(!argumentAnnotation.isAnnotationPresent(Retention.class)) {
            logger.warning("Argument annotation: "+argumentAnnotation.getSimpleName()+" doesn't have a Retention annotation!");
            return;
        }

        if(!argumentAnnotation.isAnnotationPresent(Target.class)) {
            logger.warning("Argument annotation: "+argumentAnnotation.getSimpleName()+" doesn't have a Target annotation!");
            return;
        }

        Retention retention = argumentAnnotation.getAnnotation(Retention.class);
        Target target = argumentAnnotation.getAnnotation(Target.class);

        if(target.value().length == 0) {
            logger.warning("Argument annotation: "+argumentAnnotation.getSimpleName()+" targets nothing!");
            return;
        }

        if(target.value().length > 1) {
            logger.warning("Argument annotation: "+argumentAnnotation.getSimpleName()+" has more than 1 target!");
            return;
        }

        if(!target.value()[0].equals(ElementType.TYPE_USE)) {
            logger.warning("Argument annotation: "+argumentAnnotation.getSimpleName()+" doesn't target: "+ElementType.TYPE_USE.name()+"!");
            return;
        }

        if(!retention.value().equals(RetentionPolicy.RUNTIME)) {
            logger.warning("Argument annotation: "+argumentAnnotation.getSimpleName()+" doesn't retire at runtime!");
            return;
        }

        try {

        }catch ()

        if(argumentType.completionType().equals(CompletionType.OPTIONALLY_AUTOMATIC) || argumentType.completionType().equals(CompletionType.MANUAL)) {

        }
    }
}
