package net.zoda.api.command.wrapper;

import lombok.Getter;
import lombok.SneakyThrows;
import net.zoda.api.command.Command;
import net.zoda.api.command.CommandAliases;
import net.zoda.api.command.DefaultCommandRun;
import net.zoda.api.command.ICommand;
import net.zoda.api.command.argument.registry.ArgumentTypeRegistry;
import net.zoda.api.command.argument.target.TargetType;
import net.zoda.api.command.argument.type.ArgumentType;
import net.zoda.api.command.argument.type.ArgumentTypeImpl;
import net.zoda.api.command.argument.type.TargetManager;
import net.zoda.api.command.argument.type.completion.QuotesState;
import net.zoda.api.command.resolved.ClassResolvedCommand;
import net.zoda.api.command.resolved.ResolvedSubcommand;
import net.zoda.api.command.resolved.argument.ResolvedArgument;
import net.zoda.api.command.resolved.argument.ResolvedCompletionArgument;
import net.zoda.api.command.resolved.executor.ResolvedExecutor;
import net.zoda.api.command.resolved.group.ResolvedChildSubcommandGroup;
import net.zoda.api.command.resolved.group.ResolvedSubcommandGroup;
import net.zoda.api.command.subcommand.Subcommand;
import net.zoda.api.command.subcommand.group.SubcommandGroup;
import net.zoda.api.command.subcommand.group.SubcommandGroups;
import net.zoda.api.command.utils.Pair;
import net.zoda.api.command.utils.TriPair;
import net.zoda.api.command.utils.Utils;
import net.zoda.api.command.wrapper.parsing.ParsedArgument;
import net.zoda.api.command.wrapper.parsing.StringParsingResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * @param <T> The default actor class
 * @implNote A command manager in itself does nothing but resolve the {@link ICommand} instance and return {@link ClassResolvedCommand} but provides utility functions
 * <p>
 * - {@link CommandManager#resolveCommand(ICommand)} to read the command class and verify completions and signatures and divide the {@link ICommand} instance into {@link ClassResolvedCommand}
 * </p>
 * - {@link CommandManager#getCompletions(ResolvedCompletionArgument, T)}
 * <p>
 * and a function to dispatch a subcommand or the default executor {@link CommandManager#dispatch(ICommand, Method, ParsedArgument[], Object)}
 * </p>
 */
public abstract class CommandManager<T> {

    protected static final ArgumentTypeRegistry argumentTypeRegistry = ArgumentTypeRegistry.instance();

    @Getter
    private final Class<T> defaultActorClass;
    protected final Logger logger;

    private final DefaultExecutorState defaultExecutorState;

    protected enum DefaultExecutorState {
        DISALLOW_WITH_SUBCOMMANDS,
        ALLOW_WITHOUT_ARGUMENTS,
        ALLOW
    }

    protected CommandManager(Class<T> actorClass, DefaultExecutorState state) {
        this.defaultActorClass = actorClass;
        this.defaultExecutorState = state;
        this.logger = Logger.getLogger(getClass().getSimpleName());
    }

    public Class<?> actorClassSubcommand(Method method, ICommand instance) {
        return defaultActorClass;
    }

    public Class<?> actorClassClass(ICommand command) {
        return defaultActorClass;
    }

    /**
     * This can include event classes etc...
     */
    public Class<?>[] extraSignatureClasses() {
        return new Class[0];
    }

    protected final <Actor extends T> void dispatch(ICommand instance, Method method, ParsedArgument[] arguments, Actor actor)
            throws InvocationTargetException {
        if (!actor.getClass().equals(defaultActorClass) &&
                !actor.getClass().equals(actorClassSubcommand(method, instance)) &&
                !actor.getClass().equals(actorClassClass(instance))) {
            logger.severe("Actor parameter type really does extend: " + defaultActorClass.getSimpleName() + " but is not returned in \"actorClassClass\" nor in \"actorClassSubcommand\"");
            return;
        }

        Map<Integer, Object> objects = new TreeMap<>();
        objects.put(0, actor);

        Arrays.stream(arguments).forEach(parsedArgument -> {
            objects.put(parsedArgument.baseArgument().getIndex(), parsedArgument.value());
        });

        method.setAccessible(true);
        try {
            method.invoke(instance, objects.values().toArray(new Object[0]));
        } catch (IllegalAccessException ignored) {
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
    protected ClassResolvedCommand resolveCommand(ICommand instance) {
        Class<? extends ICommand> commandClass = instance.getClass();

        if (!commandClass.isAnnotationPresent(Command.class)) {
            logger.severe(instance.getClass().getSimpleName() + " does not have the " + Command.class.getSimpleName() + " annotation!");
            return null;
        }

        Command commandAnnotation = commandClass.getAnnotation(Command.class);

        if (!commandAnnotation.manager().isAssignableFrom(this.getClass())) {
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

        Map<String, ResolvedSubcommand> subcommands = new HashMap<>();
        Map<String, ResolvedSubcommandGroup> groups = new HashMap<>();

        ResolvedExecutor defaultExecutor = null;

        for (Method method : commandClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(DefaultCommandRun.class)) {

                //SECTION: Default Executor

                if (defaultExecutor != null) {
                    logger.severe("Only 1 method can have the: " + DefaultCommandRun.class.getSimpleName() + " annotation!");
                    return null;
                }


                if (!verifyExecutorSignature(method, instance)) {
                    logger.severe("Default executor has an invalid signature, read logs above!");
                    return null;
                }

                ResolvedArgument[] arguments = resolveArguments(method, instance, commandAnnotation);
                if (arguments == null) {
                    logger.severe("Couldn't resolve the default executor's arguments, read logs above!");
                    return null;
                }

                defaultExecutor = new ResolvedExecutor(method, getArguments(arguments));
            } else if (method.isAnnotationPresent(Subcommand.class)) {
                Subcommand subcommand = method.getAnnotation(Subcommand.class);

                Map<String, ResolvedSubcommandGroup> subcommandGroups = new HashMap<>();
                List<SubcommandGroup> temporaryGroupList = new ArrayList<>();

                if (method.isAnnotationPresent(SubcommandGroup.class))
                    temporaryGroupList.addAll(List.of(method.getAnnotationsByType(SubcommandGroup.class)));
                if (method.isAnnotationPresent(SubcommandGroups.class))
                    temporaryGroupList.addAll(List.of(method.getAnnotation(SubcommandGroups.class).value()));

                int index = 0;
                for (SubcommandGroup group : temporaryGroupList) {
                    if (subcommandGroups.containsKey(group.value())) {
                        logger.severe("Duplicate group name: " + group.value() + " on unresolved subcommand: " + subcommand.value());
                        return null;
                    }

                    ResolvedSubcommandGroup resolvedSubcommandGroup;
                    if (index == 0) {
                        resolvedSubcommandGroup = new ResolvedSubcommandGroup(group.value(), group.description());
                    } else {
                        resolvedSubcommandGroup = new ResolvedChildSubcommandGroup(group.value(), group.description(), subcommandGroups.get(temporaryGroupList.get(index - 1).value()));
                    }

                    subcommandGroups.put(resolvedSubcommandGroup.getFullName(),resolvedSubcommandGroup);
                    index++;
                }

                if (!verifyExecutorSignature(method, instance)) {
                    logger.severe("Executor of subcommand: " + subcommand.value() + " has an invalid signature, read logs above!");
                    return null;
                }

                ResolvedArgument[] arguments = resolveArguments(method, instance, commandAnnotation);
                if (arguments == null) {
                    logger.severe("Couldn't resolve the subcommand: " + subcommand.value() + "'s arguments, read logs above!");
                    return null;
                }

                ResolvedSubcommandGroup tailGroup = subcommandGroups.size() == 0 ? null : subcommandGroups.values().toArray(new ResolvedSubcommandGroup[0])[subcommandGroups.size()-1];

                ResolvedExecutor executor = new ResolvedExecutor(method, getArguments(arguments));
                ResolvedSubcommand subcommand1 = new ResolvedSubcommand(subcommand.value(), subcommand.description(), commandAnnotation.manager(),
                        tailGroup, executor);

                if (subcommands.containsKey(subcommand1.getFullName())) {
                    logger.severe("Duplicate subcommands! (" + subcommand.value() + ")");
                    return null;
                }

                subcommands.put(subcommand1.getFullName(), subcommand1);

                groups.putAll(subcommandGroups);
            }
        }

        switch (defaultExecutorState) {
            case ALLOW, ALLOW_WITHOUT_ARGUMENTS -> {
                if (defaultExecutor == null) {
                    logger.severe("Couldn't find the " + DefaultCommandRun.class.getSimpleName() + " annotation on command: " + commandAnnotation.value());
                    return null;
                }
            }
        }

        switch (defaultExecutorState) {
            case ALLOW_WITHOUT_ARGUMENTS -> {
                if (defaultExecutor.getArguments().length != 0) {
                    logger.severe("The command manager: "+getClass().getSimpleName() + " disallows default executor arguments when using subcommands!");
                    return null;
                }
            }
            case DISALLOW_WITH_SUBCOMMANDS -> {
                if (defaultExecutor != null) {
                    logger.severe("The command manager: "+getClass().getSimpleName() + " disallows the default executor completely when using subcommands!");
                    return null;
                }
            }
        }
        String[] aliases = new String[0];
        if(commandClass.isAnnotationPresent(CommandAliases.class)) {
            aliases = commandClass.getAnnotation(CommandAliases.class).value();
        }

        return new ClassResolvedCommand(commandAnnotation.value(), commandAnnotation.description(), commandAnnotation.manager(),
                subcommands, groups, defaultExecutor, instance,aliases);
    }

    private boolean verifyExecutorSignature(Method method, ICommand instance) {

        if (method.getParameterTypes().length < extraSignatureClasses().length + 1) {
            logger.severe("Executor does not have enough parameter types! (expected: " + (extraSignatureClasses().length + 1) + ", got: " + method.getParameterTypes().length + ")");
            return false;
        }

        if (!method.getParameterTypes()[0].equals(actorClassSubcommand(method, instance))) {
            logger.severe("The first parameter type of the executor method must be: " + actorClassSubcommand(method, instance));
            return false;
        }

        for (int i = 0; i < extraSignatureClasses().length; i++) {
            if (method.getParameterTypes()[i + 1].equals(extraSignatureClasses()[i])) continue;


            logger.severe("Extra classes mismatch at: (" + i + ", " + (i + 1) + ") (expected: " + extraSignatureClasses()[i].getSimpleName() + ", got: " + method.getParameterTypes()[i + 1].getSimpleName() + ")");
            return false;
        }

        int argumentsFound = 0;

        for (int i = extraSignatureClasses().length + 1; i < method.getParameterTypes().length; i++) {
            boolean foundArgumentAnnotation = false;
            Annotation annotation = null;

            for (Annotation annotationA : method.getAnnotatedParameterTypes()[i].getAnnotations()) {
                if (!argumentTypeRegistry.getRegisteredArgumentTypes().containsKey(annotationA.annotationType()))
                    continue;

                if (foundArgumentAnnotation) {
                    logger.severe("A parameter type cannot have multiple argument annotations!");
                    return false;
                }

                foundArgumentAnnotation = true;
                annotation = annotationA;
                argumentsFound++;
            }

            if (annotation == null) continue;

            // Need to invoke the method through reflection because of generics type safety
            // We know that invoking this is safe because it has been verified above
            Class<?> clazz = getCustomRequiredClass(annotation, method, instance);
            if (!method.getParameterTypes()[i].equals(clazz)) {
                logger.severe("Parameter type of executor: " + method.getName() + " must be: " + clazz.getSimpleName() + " (got: " + method.getParameterTypes()[i].getSimpleName() + ")");
                return false;
            }
        }

        if (method.getParameterTypes().length > extraSignatureClasses().length + 1 + argumentsFound) {
            logger.severe("An executor method signature must be the actor, extra supplies and arguments (in: " + method.getName() + " got: " + method.getParameterTypes().length + " instead of: " +
                    (extraSignatureClasses().length + 1 + argumentsFound) + ")");
            return false;
        }

        if (!method.getReturnType().equals(void.class)) {
            logger.severe("An executor method return type must be primitive type void!");
            return false;
        }

        return true;
    }

    private Class<?> getCustomRequiredClass(Annotation annotation, Method method, ICommand instance) {
        try {
            Class<?> clazz = invokeUnsafe(argumentTypeRegistry.getRegisteredArgumentTypes().get(annotation.annotationType()).b(), "customRequiredClass", new Object[]{
                            annotation,
                            method,
                            instance
                    },
                    new Class[]{
                            annotation.annotationType(),
                            Method.class,
                            ICommand.class
                    });

            if (clazz == null) throw new Exception();
            return clazz;
        } catch (Exception e) {
            return argumentTypeRegistry.getRegisteredArgumentTypes().get(annotation.annotationType()).a().typeClass();
        }
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private <O> O invokeUnsafe(Object instance, String value, Object[] objects, Class[] classes) {
        return (O) instance.getClass().getMethod(value, classes).invoke(instance, objects);
    }

    private ResolvedArgument[] resolveArguments(Method method, ICommand instance, Command commandAnnotation) {
        List<ResolvedArgument> resolvedArguments = new ArrayList<>();

        for (int i = extraSignatureClasses().length + 1; i < method.getParameterTypes().length; i++) {
            Annotation annotation = null;

            for (Annotation annotationL : method.getAnnotatedParameterTypes()[i].getAnnotations()) {
                if (!argumentTypeRegistry.getRegisteredArgumentTypes().containsKey(annotationL.annotationType()))
                    continue;
                annotation = annotationL;
                break;
            }

            if (annotation == null) {
                logger.severe("Couldn't find argument type annotation! (signature hasn't been verified yet?)");
                return null;
            }

            try {
                boolean status = invokeUnsafe(
                        argumentTypeRegistry.getRegisteredArgumentTypes().get(annotation.annotationType()).b()
                        , "verifyAnnotation", new Object[]{
                                annotation,
                                Logger.getLogger("Verification of argument: " + getValue(annotation, "name") + " at executor: " + method.getName()),
                                method,
                                instance
                        },
                        new Class[]{
                                annotation.annotationType(),
                                Logger.class,
                                Method.class,
                                ICommand.class
                        });

                if (!status) {
                    logger.severe("Verification of argument: " + getValue(annotation, "name") + " at executor: " + method.getName() + ": verifyAnnotation returned false!");
                }
            } catch (Exception exception) {
                if (!(exception instanceof NoSuchMethodException)) {
                    logger.severe("There was error verifying the argument's annotation, read the stacktrace and the logs above (if available)");
                    exception.printStackTrace();
                }
            }

            Class<? extends CommandManager<?>> requiredManager = annotation.annotationType().isAnnotationPresent(TargetManager.class)
                    ? annotation.annotationType().getAnnotation(TargetManager.class).value()
                    : null;

            if (requiredManager != null) {
                if (!commandAnnotation.manager().isAssignableFrom(requiredManager)) {
                    logger.severe("ResolvedArgument type: " + annotation.annotationType().getSimpleName() + " can only be used on: " + requiredManager.getSimpleName());
                    return null;
                }
            }

            ResolvedArgument resolvedArgument = new ResolvedArgument(
                    getValue(annotation, "name"), getValue(annotation, "required"),
                    annotation.annotationType(),
                    i,
                    annotation,
                    instance,
                    method,
                    getValue(annotation, "description")
            );

            if (Utils.isPresent(annotation.annotationType(), TargetType.METHOD, "completer")) l:{
                String completer = getValue(annotation, "completer");
                if (completer.isEmpty() || completer.isBlank()) break l;

                TargetType type = getValue(annotation, "completerType");

                if (!Utils.isPresent(instance.getClass(), type, completer)) {
                    logger.severe("ResolvedArgument: " + getValue(annotation, "name") + " in executor: " + method.getName() + " has an invalid completer!");
                    return null;
                }

                if (!verifyCompleter(completer, type, instance, annotation, method)) {
                    logger.severe("ResolvedArgument: " + getValue(annotation, "name") + " in executor: " + method.getName() + " has an invalid completer!");
                    return null;
                }

                resolvedArgument = new ResolvedCompletionArgument(
                        getValue(annotation, "name"), getValue(annotation, "required"),
                        annotation.annotationType(),
                        i,
                        completer,
                        type,
                        annotation,
                        instance,
                        method,
                        getValue(annotation, "description"),
                        getValue(annotation, "enforceCompletions")
                );

            }

            resolvedArguments.add(resolvedArgument);
        }

        return resolvedArguments.toArray(new ResolvedArgument[0]);
    }

    private boolean verifyCompleter(String completer, TargetType type, ICommand instance, Annotation annotation, Method method) {
        Class<?> rawClass = Utils.getType(completer, type, instance.getClass());

        if (!rawClass.equals(Function.class)) {
            logger.severe("Completer: " + completer + " class type is not " + Function.class.getSimpleName());
            return false;
        }

        ParameterizedType genericType = Utils.getGenericType(completer, type, instance.getClass());

        Class<?> first = (Class<?>) genericType.getActualTypeArguments()[0];

        ParameterizedType listGenericType = (ParameterizedType) genericType.getActualTypeArguments()[1];
        Class<?> second = (Class<?>) listGenericType.getRawType();

        Class<?> requiredClass = getCustomRequiredClass(annotation, method, instance);

        if (!first.isAssignableFrom(actorClassSubcommand(method, instance))) {
            logger.severe("Function first generic must be of type: " + actorClassSubcommand(method, instance).getSimpleName());
            return false;
        }

        if (!second.equals(List.class)) {
            logger.severe("Function second generic must be of type: " + List.class.getSimpleName());
            return false;
        }


        Class<?> listType = (Class<?>) listGenericType.getActualTypeArguments()[0];

        if (!listType.equals(requiredClass)) {
            logger.severe("List generic must be of type: " + requiredClass.getSimpleName());
            return false;
        }

        return true;
    }

    /**
     * This method is considered unsafe if signature hasn't been verified
     *
     * @param annotation The target annotation
     * @param value      the name of the value
     * @return the value
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public <O> O getValue(Annotation annotation, String value) {
        return (O) annotation.getClass().getMethod(value).invoke(annotation);
    }

    public boolean commandExists(String name) {
        return false;
    }

    /**
     * Uses {@link CommandManager#getCompletionsAsString(ResolvedCompletionArgument, Actor)} and splits strings at " " and uses integer parameter as an index
     *
     * @param argument The completion argument
     * @param part     the index (if anything out of bounds is provided function will add the full string)
     * @return completions as a chopped string
     */
    protected <Actor extends T> List<String> getCompletionsAsString(ResolvedCompletionArgument argument, Actor actor, int part) {
        List<String> list = getCompletionsAsString(argument, actor);
        List<String> stringList = new ArrayList<>();

        for (String str : list) {
            try {
                stringList.add(str.split(" ")[part]);
            } catch (ArrayIndexOutOfBoundsException ignored) {
                stringList.add(str);
            }
        }

        return stringList;
    }

    protected <Actor extends T> List<String> getCompletionsAsString(ResolvedCompletionArgument argument, Actor actor) {
        List<String> stringArguments = new ArrayList<>();

        for (Object obj : getCompletions(argument, actor)) {
            String str = invokeUnsafe(
                    argumentTypeRegistry.getRegisteredArgumentTypes().get(argument.getAnnotation().annotationType()).b(),
                    "stringify",
                    new Object[]{
                            obj,
                            argument.getAnnotation(),
                            argument.getParentExecutor(),
                            argument.getCommand()
                    },
                    new Class[]{
                            obj.getClass(),
                            argument.getAnnotation().annotationType(),
                            Method.class,
                            ICommand.class
                    }
            );

            stringArguments.add(str);
        }

        return stringArguments;
    }

    @SuppressWarnings("unchecked")
    protected <Actor extends T> List<Object> getCompletions(ResolvedCompletionArgument argument, Actor actor) {
        // Casting it is necessary here

        Function<Actor, List<?>> instance = null;

        if (argument.getType().equals(TargetType.METHOD)) {
            try {
                Method method = argument.getCommand().getClass().getDeclaredMethod(argument.getCompleter());
                method.setAccessible(true);

                instance = (Function<Actor, List<?>>) method.invoke(argument.getCommand());
            } catch (Exception ignored) {
            }
        } else {
            try {
                Field field = argument.getCommand().getClass().getDeclaredField(argument.getCompleter());
                field.setAccessible(true);

                instance = (Function<Actor, List<?>>) field.get(argument.getCommand());
            } catch (Exception ignored) {
            }
        }

        return ((List<Object>) instance.apply(actor));
    }

    @NotNull
    private ResolvedArgument[] getArguments(ResolvedArgument[] arguments) {
        ArrayList<ResolvedArgument> sorted = new ArrayList<>();
        ArrayList<ResolvedArgument> sortedLast = new ArrayList<>();

        for (ResolvedArgument argument : arguments) {
            if (argument.isRequired()) {
                sorted.add(argument);
            } else {
                sortedLast.add(argument);
            }
        }

        sorted.addAll(sortedLast);
        sortedLast.clear();
        return sorted.toArray(new ResolvedArgument[0]);
    }

    protected <Actor extends T> StringParsingResult attemptStringParsing(Actor actor, ResolvedExecutor executor, String[] arguments) {
        if (arguments.length == 0 && executor.getArguments().length != 0) {
            return new StringParsingResult(
                    new StringParsingResult.ParsingResultFeedback(
                            StringParsingResult.EnumParsingResultType.EMPTY_ARGUMENT_LIST,
                            "Missing arguments!",
                            null),
                    new ParsedArgument[0]);
        }

        List<ParsedArgument> parsedArguments = new ArrayList<>();

        int index = 0;
        for (ResolvedArgument argument : executor.getArguments()) {
            try {
                TriPair<Pair<StringParsingResult.EnumParsingResultType, String>, Integer, Object> result = parseArgument(actor, arguments, index, arguments[index], executor.getMethod(), argument);

            } catch (ArrayIndexOutOfBoundsException exception) {
                if (!argument.isRequired()) continue;
            }

            index++;
        }

        return new StringParsingResult(null, parsedArguments.toArray(new ParsedArgument[0]));
    }

    /**
     * @return By how much the string argument index should be increased (use negative integers to decrease)
     */
    private <Actor extends T> TriPair<Pair<StringParsingResult.EnumParsingResultType, String>, Integer, Object> parseArgument(Actor actor, String[] args, int i,
                                                                                                                              String arg, Method method, ResolvedArgument argument)
            throws ArrayIndexOutOfBoundsException {

        int index = 0;
        Pair<?, String> result = null;

        Pair<ArgumentType, ArgumentTypeImpl<?, ? extends Annotation>> argumentImplementations = argumentTypeRegistry.getRegisteredArgumentTypes().get(argument.getArgumentTypeAnnotationClass());
        if (!argumentImplementations.a().quotesState().equals(QuotesState.OFF)) {
            if (!args[i].startsWith("\"") && argumentImplementations.a().quotesState().equals(QuotesState.REQUIRED)) {
                return new TriPair<>(new Pair<>(StringParsingResult.EnumParsingResultType.INCOMPLETE_ARGUMENT, argumentImplementations.a().name() + " arguments must be captured between double quotes (\")"), 0, null);
            }

            String[] newArgs = null;
            if (args[i].startsWith("\"\"")) {
                newArgs = new String[]{args[i].replaceFirst("\"", "")};
            } else {
                newArgs = fromBuilder(i, args);
            }

            if (newArgs == null) {
                return new TriPair<>(new Pair<>(StringParsingResult.EnumParsingResultType.INCOMPLETE_ARGUMENT, "argument never ends"), 0, null);
            }

            index += newArgs.length - 1;
            result = invokeUnsafe(argumentImplementations.b(), "fromString", new Object[]{
                    newArgs,
                    argument.getAnnotation(),
                    method,
                    argument.getCommand()
            }, new Class[]{
                    String[].class,
                    argument.getArgumentTypeAnnotationClass(),
                    Method.class,
                    ICommand.class
            });
        } else {
            if (args.length - i + 1 < getMaximumArguments(argument, method)) {
                return new TriPair<>(new Pair<>(StringParsingResult.EnumParsingResultType.INCOMPLETE_ARGUMENT, "Not enough sub-arguments"), 0, null);
            }

            int argsUse = getMaximumArguments(argument, method);


            String[] newArguments = new String[argsUse];
            System.arraycopy(args, i, newArguments, 0, argsUse);

            result = invokeUnsafe(argumentImplementations.b(), "fromString", new Object[]{
                    newArguments,
                    argument.getAnnotation(),
                    method,
                    argument.getCommand()
            }, new Class[]{
                    String[].class,
                    argument.getArgumentTypeAnnotationClass(),
                    Method.class,
                    ICommand.class
            });

            index += argsUse - 1;
        }


        if (argument.isRequired() && result == null) {
            return new TriPair<>(new Pair<>(StringParsingResult.EnumParsingResultType.MISSING_ARGUMENT, "Argument is missing"), 0, null);
        }

        if (argument.isRequired() && result.a() == null) {
            return new TriPair<>(new Pair<>(StringParsingResult.EnumParsingResultType.INVALID_ARGUMENT, result.b()), 0, null);
        }

        if (argument instanceof ResolvedCompletionArgument completionArgument) {
            if (!getCompletions(completionArgument, actor).isEmpty() && (!getCompletions(completionArgument, actor).contains(result.a()) && completionArgument.isEnforceCompletions())) {
                return new TriPair<>(new Pair<>(StringParsingResult.EnumParsingResultType.INVALID_ARGUMENT, "Argument is not in completions"), 0, null);
            }
        }

        return new TriPair<>(new Pair<>(StringParsingResult.EnumParsingResultType.SUCCESS, ""), index, result.a());
    }

    private String[] fromBuilder(int i, String[] args) {

        StringBuilder raw = new StringBuilder();

        boolean found_end = false;
        for (int index = i; index < args.length; index++) {
            boolean appendSelf = true;

            if (index == i) {
                raw.append(args[i].replaceFirst("\"", ""));
                appendSelf = false;
            }

            if (appendSelf) {
                raw.append(args[index]);
            }

            raw.append(" ");

            if (args[index].endsWith("\"")) {
                found_end = true;
                break;
            }
        }
        if (!found_end) {
            return null;
        }

        return raw.toString().replaceFirst("\"", "").split(" ");
    }

    protected ResolvedSubcommand parseSubcommand(String[] args,ClassResolvedCommand command) {
        ResolvedSubcommand resolvedSubcommand = null;
        ResolvedSubcommandGroup group = null;

        StringBuilder builder = new StringBuilder();
        int index = 0;

        for (String arg : args) {
            builder.append(index == 0 ? "" : " ").append(arg);

            if (!command.getSubcommands().containsKey(builder.toString())) {

                if (command.getGroups().containsKey(builder.toString())) {
                    group = command.getGroups().get(builder.toString());
                    index++;
                    continue;
                }

                if (group != null) {
                    resolvedSubcommand = command.getSubcommands().get(builder.toString());
                    break;
                }
            } else {
                resolvedSubcommand = command.getSubcommands().get(builder.toString());
                break;
            }

            index++;
        }

        return resolvedSubcommand;
    }
    protected int getMaximumArguments(ResolvedArgument argument, Method method) {
        return invokeUnsafe(argumentTypeRegistry.getRegisteredArgumentTypes().get(argument.getArgumentTypeAnnotationClass()).b(), "maximumArgs", new Object[]{
                        argument.getAnnotation(),
                        method,
                        argument.getCommand()
                },
                new Class[]{
                        argument.getArgumentTypeAnnotationClass(),
                        Method.class,
                        ICommand.class
                });
    }
}
