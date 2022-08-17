package net.zoda.api.command.wrapper;

import lombok.Getter;
import net.zoda.api.command.Command;
import net.zoda.api.command.DefaultCommandRun;
import net.zoda.api.command.ICommand;
import net.zoda.api.command.argument.registry.ArgumentTypeRegistry;
import net.zoda.api.command.resolved.ClassResolvedCommand;
import net.zoda.api.command.resolved.ResolvedSubcommand;
import net.zoda.api.command.resolved.argument.ResolvedArgument;
import net.zoda.api.command.resolved.executor.ResolvedExecutor;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @param <T> The default actor class
 * @implNote A command manager in itself does nothing but resolve the {@link ICommand} instance and return {@link ClassResolvedCommand}
 */
public abstract class CommandManager<T> {

    protected static final ArgumentTypeRegistry argumentTypeRegistry = ArgumentTypeRegistry.instance();

    @Getter
    private final Class<T> defaultActorClass;
    protected final Logger logger;

    public CommandManager(Class<T> actorClass) {
        this.defaultActorClass = actorClass;
        this.logger = Logger.getLogger(getClass().getSimpleName());
    }

    public Class<? extends T> actorClassSubcommand(Method method) {
        return defaultActorClass;
    }

    public Class<? extends T> actorClassClass(ICommand command) {
        return defaultActorClass;
    }

    /**
     * This can include event classes etc...
     */
    public Class<?>[] extraSignatureClasses() {
        return new Class[0];
    }

    protected final <Actor extends T> void dispatch(ICommand instance, Method method, Object[] orderedResolvedArguments, Actor actor) {
        if (!actor.getClass().equals(defaultActorClass) &&
                !actor.getClass().equals(actorClassSubcommand(method)) &&
                !actor.getClass().equals(actorClassClass(instance))) {
            logger.severe("Actor is one of T's inheritors but is not returned in \"actorClassClass\" nor in \"actorClassSubcommand\"");
            return;
        }
    }

    /**
     * Resolves the {@link ICommand} instance and divides it into sections to be used separately
     *
     * @param instance the command instance
     * @return the resolved command including subcommands
     * @implNote If the command manager encounters any error it will log it and return null
     */
    @Nullable
    public ClassResolvedCommand resolveCommand(ICommand instance) {
        Class<? extends ICommand> commandClass = instance.getClass();

        if (!commandClass.isAnnotationPresent(Command.class)) {
            logger.severe(instance.getClass().getSimpleName() + " does not have the " + Command.class.getSimpleName() + " annotation!");
            return null;
        }

        Command commandAnnotation = commandClass.getAnnotation(Command.class);

        if (!commandAnnotation.manager().equals(this.getClass())) {
            logger.severe(instance.getClass().getSimpleName() + " target manager is not: " + this.getClass().getSimpleName());
            return null;
        }

        if (commandAnnotation.value().isBlank() || commandAnnotation.value().isEmpty()) {
            logger.severe(instance.getClass().getSimpleName() + " command name is blank!");
            return null;
        }

        if (commandExists(commandAnnotation.value())) {
            logger.severe("A command with the name: " + commandAnnotation.value() + " already exists!");
            return null;
        }

        List<ResolvedSubcommand> subcommands = new ArrayList<>();

        ResolvedExecutor defaultExecutor = null;

        for (Method method : commandClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(DefaultCommandRun.class)) {

                //SECTION: Default Executor

                if (defaultExecutor != null) {
                    logger.severe("Only 1 method can have the: " + DefaultCommandRun.class.getSimpleName() + " annotation!");
                    return null;
                }


                if (!verifyExecutorSignature(method)) {

                }

                defaultExecutor = new ResolvedExecutor(method, resolveArguments(method));

                if (!actorClassSubcommand(method).equals(defaultActorClass)) {
                    logger.severe("The default executor cannot have a custom actor class");
                    return null;
                }
            }
        }

        if (defaultExecutor == null) {
            logger.severe("Couldn't find the " + DefaultCommandRun.class.getSimpleName() + " annotation on command: " + commandAnnotation.value());
            return null;
        }

        return new ClassResolvedCommand(commandAnnotation.value(),commandAnnotation.description(),commandAnnotation.manager(),subcommands.toArray(new ResolvedSubcommand[0]), defaultExecutor);
    }

    private boolean verifyExecutorSignature(Method method) {

        if (method.getParameterTypes().length < extraSignatureClasses().length + 1) {
            logger.severe("Executor does not have enough parameter types! (expected: " + (extraSignatureClasses().length + 1) + ", got: " + method.getParameterTypes().length + ")");
            return false;
        }

        if (!method.getParameterTypes()[0].equals(actorClassSubcommand(method))) {
            logger.severe("The first parameter type of the executor method must be: " + actorClassSubcommand(method));
            return false;
        }

        for (int i = 0; i < extraSignatureClasses().length; i++) {
            if (method.getParameterTypes()[i + 1].equals(extraSignatureClasses()[i])) continue;


            logger.severe("Extra classes mismatch at: ("+i+", "+(i+1)+") (expected: "+extraSignatureClasses()[i].getSimpleName()+", got: "+method.getParameterTypes()[i +1].getSimpleName()+")");
            return false;
        }

        int argumentsFound = 0;

        for (int i = extraSignatureClasses().length + 1; i < method.getParameterTypes().length; i++) {
            boolean foundArgumentAnnotation = false;

            for(Annotation annotation : method.getAnnotatedParameterTypes()[i].getAnnotations()) {
                if(!argumentTypeRegistry.getRegisteredArgumentTypes().containsKey(annotation.annotationType())) continue;

                if(foundArgumentAnnotation) {
                    logger.severe("A parameter type cannot have multiple argument annotations!");
                    return false;
                }

                foundArgumentAnnotation = true;
                argumentsFound++;
            }
        }

        if(method.getParameterTypes().length > extraSignatureClasses().length + 1 + argumentsFound) {
            logger.severe("An executor method signature must be the actor, extra supplies and arguments (in: "+method.getName()+" got: "+method.getParameterTypes().length+" instead of: "+
                    (extraSignatureClasses().length + 1 + argumentsFound)+")");
            return false;
        }

        return true;
    }

    private ResolvedArgument[] resolveArguments(Method method) {
        List<ResolvedArgument> resolvedArguments = new ArrayList<>();

        for (int i = extraSignatureClasses().length + 1; i < method.getParameterTypes().length; i++) {
            Annotation annotation = null;

            for(Annotation annotationL : method.getAnnotatedParameterTypes()[i].getAnnotations()) {
                if(!argumentTypeRegistry.getRegisteredArgumentTypes().containsKey(annotationL.annotationType())) continue;
                annotation = annotationL;
                break;
            }

            if(annotation == null) {
                logger.severe("Couldn't find argument type annotation! (signature hasn't been verified yet?)");
                return null;
            }

        }

        return resolvedArguments.toArray(new ResolvedArgument[0]);
    }

    public boolean commandExists(String name) {
        return false;
    }
}
