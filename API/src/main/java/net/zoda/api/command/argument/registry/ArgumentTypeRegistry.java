package net.zoda.api.command.argument.registry;

import net.zoda.api.command.argument.EnumArgument;
import net.zoda.api.command.argument.IntegerArgument;
import net.zoda.api.command.argument.NumberArgument;
import net.zoda.api.command.argument.StringArgument;
import net.zoda.api.command.argument.target.TargetType;
import net.zoda.api.command.argument.timestamp.TimestampArgument;
import net.zoda.api.command.argument.type.ArgumentType;
import net.zoda.api.command.argument.type.ArgumentTypeImpl;
import net.zoda.api.command.argument.type.completion.CompletionType;
import net.zoda.api.command.utils.Pair;
import net.zoda.api.command.utils.Utils;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class ArgumentTypeRegistry {

    private final Logger logger = Logger.getLogger("Argument Registry");

    private final HashMap<Class<? extends Annotation>, Pair<ArgumentType, ArgumentTypeImpl<?, ? extends Annotation>>> registeredArgumentTypes;

    public Map<Class<? extends Annotation>, Pair<ArgumentType, ArgumentTypeImpl<?, ? extends Annotation>>> getRegisteredArgumentTypes() {
        return Collections.unmodifiableMap(registeredArgumentTypes);
    }

    private ArgumentTypeRegistry() {
        this.registeredArgumentTypes = new HashMap<>();

        register(IntegerArgument.class, new IntegerArgument.Impl());
        register(EnumArgument.class, new EnumArgument.Impl());
        register(StringArgument.class, new StringArgument.Impl());
        register(NumberArgument.class,new NumberArgument.Impl());
        register(TimestampArgument.class, new TimestampArgument.Impl());
    }

    private static ArgumentTypeRegistry instance;

    public static ArgumentTypeRegistry instance() {
        if (instance == null) instance = new ArgumentTypeRegistry();
        return instance;
    }

    public <T extends Annotation> void register(Class<T> argumentAnnotation, ArgumentTypeImpl<?, T> impl) {
        if (registeredArgumentTypes.containsKey(argumentAnnotation)) {
            logger.warning(argumentAnnotation.getSimpleName() + " is already registered!");
            return;
        }

        if (!argumentAnnotation.isAnnotationPresent(ArgumentType.class)) {
            logger.warning(ArgumentType.class.getSimpleName() + " annotation is missing from argument annotation: " + argumentAnnotation.getSimpleName());
            return;
        }

        ArgumentType argumentType = argumentAnnotation.getAnnotation(ArgumentType.class);

        /*Class<?> targetClass = impl.objectClass();

        // Allow inheritance (for compatibility reasons)
        if (!targetClass.isAssignableFrom(argumentType.typeClass())) {
            logger.warning("Implementation target class of: "+argumentAnnotation.getSimpleName()+" does not match ArgumentType type class! "
                +"(expected: "+argumentType.typeClass().getSimpleName()+", got: "+targetClass.getSimpleName()+")");
            return;
        }*/

        if (!argumentAnnotation.isAnnotationPresent(Retention.class)) {
            logger.warning("Argument annotation: " + argumentAnnotation.getSimpleName() + " doesn't have a Retention annotation!");
            return;
        }

        Retention retention = argumentAnnotation.getAnnotation(Retention.class);

        if(!argumentAnnotation.isAnnotationPresent(Target.class)) {
            logger.warning("Argument annotation: "+argumentAnnotation.getSimpleName()+" doesn't have a Target annotation!");
            return;
        }

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

        Map<String, Class<?>> values = new HashMap<>(Map.of(
                "name", String.class,
                "description", String.class,
                "required", boolean.class
        ));

        if (argumentType.completionType().equals(CompletionType.OPTIONALLY_AUTOMATIC) || argumentType.completionType().equals(CompletionType.MANUAL)) {
            values.putAll(Map.of(
                    "completer", String.class,
                    "completerType", TargetType.class,
                    "enforceCompletions", boolean.class
            ));
        }
        if (!Utils.isPresent(argumentAnnotation, TargetType.METHOD, "name")) {
            logger.warning("Argument annotation: " + argumentAnnotation.getSimpleName() + " doesn't have a name value!");
            return;
        }

        for (Map.Entry<String, Class<?>> entry : values.entrySet()) {
            if (!Utils.isPresent(argumentAnnotation, TargetType.METHOD, entry.getKey())) {
                logger.warning("Argument annotation: " + argumentAnnotation.getSimpleName() + " doesn't have the \"" + entry.getKey() + "\" value of type: " + entry.getValue().getSimpleName());
                return;
            }

            try {
                Method method = argumentAnnotation.getMethod(entry.getKey());
                method.setAccessible(true);

                if (!method.getReturnType().equals(entry.getValue())) {
                    logger.warning("Argument annotation: " + argumentAnnotation.getSimpleName() + " value: " + entry.getKey() + " returns: " + method.getReturnType().getSimpleName() + " instead of: " + entry.getValue().getSimpleName());
                    return;
                }

                Object expectedReturnType = switch (entry.getKey()) {
                    case "required", "enforceCompletions" -> true;
                    case "description" -> "No description provided";
                    case "completer" -> new String("");
                    case "completerType" -> TargetType.FIELD;
                    default -> null;
                };
                if (expectedReturnType == null) continue;

                Object returnValue = method.getDefaultValue();
                if (returnValue == null) {
                    logger.warning("Missing default value of argument annotation: " + argumentAnnotation.getSimpleName() + " value: " + entry.getKey());
                    return;
                }

                if (returnValue.equals(expectedReturnType)) continue;

                logger.warning("Expected: " + expectedReturnType + " as a default value but got: " + returnValue + " instead in argument annotation: " + argumentAnnotation.getSimpleName() + " value: " + entry.getKey());
                return;
            } catch (NoSuchMethodException ignored) {
                //Already checked above
            }
        }

        registeredArgumentTypes.put(argumentAnnotation, new Pair<>(argumentType, impl));
    }


}
